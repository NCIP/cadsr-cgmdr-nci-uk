/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2005-2007 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 * Original code is
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 * $Id: MultiReadReentrantLock.java 7543 2008-03-24 20:06:57Z wolfgang_m $
 */
package org.exist.storage.lock;

import org.apache.log4j.Logger;
import org.exist.util.DeadlockException;
import org.exist.util.LockException;

import java.util.ArrayList;
import java.util.List;

/**
 * A reentrant read/write lock, which allows multiple readers to acquire a lock.
 * Waiting writers are preferred.
 * <p/>
 * This is an adapted and bug-fixed version of code taken from Apache's Turbine
 * JCS.
 */
public class MultiReadReentrantLock implements Lock {

    private final static Logger LOG = Logger.getLogger(MultiReadReentrantLock.class);

    private Object id;

    /**
     * Number of threads waiting to read.
     */
    private int waitingForReadLock = 0;

    /**
     * Number of threads reading.
     */
    private List outstandingReadLocks = new ArrayList(4);

    /**
     * The thread that has the write lock or null.
     */
    private Thread writeLockedThread;

    /**
     * The number of (nested) write locks that have been requested from
     * writeLockedThread.
     */
    private int outstandingWriteLocks = 0;

    /**
     * Threads waiting to get a write lock are tracked in this ArrayList to
     * ensure that write locks are issued in the same order they are requested.
     */
    private List waitingForWriteLock = null;

    /**
     * Default constructor.
     */
    public MultiReadReentrantLock(Object id) {
        this.id = id;
    }

    public String getId() {
        return id.toString();
    }

    /* @deprecated Use other method
    * @see org.exist.storage.lock.Lock#acquire()
    */
    public boolean acquire() throws LockException {
        return acquire(Lock.READ_LOCK);
    }

    public boolean acquire(int mode) throws LockException {
        if (mode == Lock.NO_LOCK) {
            LOG.warn("acquired with no lock !");
            return true;
        }
        switch (mode) {
            case Lock.WRITE_LOCK:
                return writeLock();
            default:
                return readLock();
        }
    }

    /* (non-Javadoc)
	 * @see org.exist.util.Lock#attempt(int)
	 */
    public boolean attempt(int mode) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Issue a read lock if there is no outstanding write lock or threads
     * waiting to get a write lock. Caller of this method must be careful to
     * avoid synchronizing the calling code so as to avoid deadlock.
     */
    private synchronized boolean readLock() throws LockException {
        final Thread thisThread = Thread.currentThread();
        if (writeLockedThread == thisThread) {
            // add acquired lock to the current list of read locks
            outstandingReadLocks.add(new LockOwner(thisThread));
//            LOG.debug("Thread already holds a write lock");
            return true;
        }
        deadlockCheck();
        waitingForReadLock++;
        if (writeLockedThread != null) {
            WaitingThread waiter = new WaitingThread(thisThread, this, this, Lock.READ_LOCK);
            DeadlockDetection.addResourceWaiter(thisThread, waiter);
            while (writeLockedThread != null) {
//                LOG.debug("readLock wait by " + thisThread.getName() + " for " + getId());
                waiter.doWait();
    //            LOG.debug("wake up from readLock wait");
            }
            DeadlockDetection.clearResourceWaiter(thisThread);
        }
//        LOG.debug("readLock acquired by thread: " + Thread.currentThread().getName());

        waitingForReadLock--;
        // add acquired lock to the current list of read locks
        outstandingReadLocks.add(new LockOwner(thisThread));
        return true;
    }

    /**
     * Issue a write lock if there are no outstanding read or write locks.
     * Caller of this method must be careful to avoid synchronizing the calling
     * code so as to avoid deadlock.
     */
    private boolean writeLock() throws LockException {
        Thread thisThread = Thread.currentThread();
        WaitingThread waiter;
        synchronized (this) {
            if (writeLockedThread == thisThread) {
                outstandingWriteLocks++;
//                LOG.debug("acquired additional write lock on " + getId());
                return true;
            }
            
            if (writeLockedThread == null && grantWriteLock()) {
                writeLockedThread = thisThread;
                outstandingWriteLocks++;

//                LOG.debug( "writeLock on " + getId() + " acquired without waiting by " + writeLockedThread.getName());

                return true;
            }
//            if (writeLockedThread == thisThread) {
//                LOG.debug("nested write lock: " + outstandingWriteLocks);
//            }
            deadlockCheck();
            if (waitingForWriteLock == null)
                waitingForWriteLock = new ArrayList(3);
            waiter = new WaitingThread(thisThread, thisThread, this, Lock.WRITE_LOCK);
            addWaitingWrite(waiter);
            DeadlockDetection.addResourceWaiter(thisThread, waiter);
        }
        List deadlockedThreads = null;
        LockException exceptionCaught = null;
        synchronized (thisThread) {
            if (thisThread != writeLockedThread) {
                while (thisThread != writeLockedThread && deadlockedThreads == null) {
//                	LOG.debug("writeLock wait on " + getId() + ". held by " + (writeLockedThread == null ? "null" : writeLockedThread.getName())
//                            + ". outstanding: " + outstandingWriteLocks);
                    if (LockOwner.DEBUG) {
                        StringBuffer buf = new StringBuffer("Waiting for write: ");
                        for (int i = 0; i < waitingForWriteLock.size(); i++) {
                            buf.append(' ');
                            buf.append(((WaitingThread) waitingForWriteLock.get(i)).getThread().getName());
                        }
                        LOG.debug(buf.toString());
                        debugReadLocks("WAIT");
                    }
                    deadlockedThreads = checkForDeadlock(thisThread);
                    if (deadlockedThreads == null) {
                        try {
                            waiter.doWait();
                        } catch (LockException e) {
                            // Don't throw the exception now, leave the synchronized block and clean up first
                            exceptionCaught = e;
                            break;
                        }
                    }
                }
            }
            if (deadlockedThreads == null && exceptionCaught == null)
                outstandingWriteLocks++; //testing
//            LOG.debug( "writeLock on " + getId() + " acquired by " + writeLockedThread.getName());
        }
        synchronized (this) {
            DeadlockDetection.clearResourceWaiter(thisThread);
            removeWaitingWrite(waiter);
        }
        if (exceptionCaught != null)
            throw exceptionCaught;
        if (deadlockedThreads != null) {
            for (int i = 0; i < deadlockedThreads.size(); i++) {
                WaitingThread wt = (WaitingThread) deadlockedThreads.get(i);
                wt.signalDeadlock();
            }
            throw new DeadlockException();
        }
        return true;
    }

    private void addWaitingWrite(WaitingThread waiter) {
        waitingForWriteLock.add(waiter);
    }

    private void removeWaitingWrite(WaitingThread waiter) {
        for (int i = 0; i < waitingForWriteLock.size(); i++) {
            WaitingThread next = (WaitingThread) waitingForWriteLock.get(i);
            if (next.getThread() == waiter.getThread()) {
                waitingForWriteLock.remove(i);
                break;
            }
        }
    }

    /* @deprecated : use other method
     * @see org.exist.storage.lock.Lock#release()
     */
    public void release() {
        release(Lock.READ_LOCK);
    }

    public void release(int mode) {
        switch (mode) {
            case Lock.NO_LOCK:
                break;
            case Lock.WRITE_LOCK:
                releaseWrite(1);
                break;
            default:
                releaseRead(1);
                break;
        }
    }

    public void release(int mode, int count) {
        switch (mode) {
            case Lock.WRITE_LOCK:
                releaseWrite(count);
                break;
            default:
                releaseRead(count);
                break;
        }
    }

    private synchronized void releaseWrite(int count) {
        if (Thread.currentThread() == writeLockedThread) {
            //log.info( "outstandingWriteLocks= " + outstandingWriteLocks );
            if (outstandingWriteLocks > 0)
                outstandingWriteLocks -= count;
//            else {
//                LOG.info("extra lock release, writelocks are " + outstandingWriteLocks + "and done was called");
//            }

            if (outstandingWriteLocks > 0) {
//                LOG.debug("writeLock released for a nested writeLock request: " + outstandingWriteLocks +
//                    "; thread: " + writeLockedThread.getName());
                return;
            }

            // if another thread is waiting for a write lock, we immediately pass control to it.
            // no further checks should be required here.
            if (grantWriteLockAfterRead()) {
                WaitingThread waiter = (WaitingThread) waitingForWriteLock.get(0);
                removeWaitingWrite(waiter);
                DeadlockDetection.clearResourceWaiter(waiter.getThread());
                writeLockedThread = waiter.getThread();
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("writeLock released and before notifying a write lock waiting thread " + writeLockedThread);
//                }
                synchronized (writeLockedThread) {
                    writeLockedThread.notifyAll();
                }
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("writeLock released by " + Thread.currentThread().getName() +
//                            " after notifying a write lock waiting thread " + writeLockedThread.getName());
//                }
            } else {
                writeLockedThread = null;
                if (waitingForReadLock > 0) {
//                    LOG.debug("writeLock " + Thread.currentThread().getName() + " released, notified waiting readers");
                    // wake up pending read locks
                    notifyAll();
                }
//                } else {
//                    LOG.debug("writeLock released, no readers waiting");
//                }
            }
        } else {
            LOG.warn("Possible lock problem: a thread released a write lock it didn't hold. Either the " +
                "thread was interrupted or it never acquired the lock.", new Throwable());
        }
//        LOG.debug("writeLock released: " + getId() + "; outstanding: " + outstandingWriteLocks +
//            "; thread: " + Thread.currentThread().getName() + " suspended: " + suspendedThreads.size());
    }

    /**
     * Threads call this method to relinquish a lock that they previously got
     * from this object.
     *
     * @throws IllegalStateException if called when there are no outstanding locks or there is a
     *                               write lock issued to a different thread.
     */
    private synchronized void releaseRead(int count) {
        if (!outstandingReadLocks.isEmpty()) {
            removeReadLock(count);
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("readLock on " + getId() + " released by " + Thread.currentThread().getName());
//                LOG.debug("remaining read locks: " + listReadLocks());
//            }
            if (writeLockedThread == null && grantWriteLockAfterRead()) {
                WaitingThread waiter = (WaitingThread) waitingForWriteLock.get(0);
                removeWaitingWrite(waiter);
                DeadlockDetection.clearResourceWaiter(waiter.getThread());
                writeLockedThread = waiter.getThread();
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("readLock released and before notifying a write lock waiting thread " + writeLockedThread);
//                    LOG.debug("remaining read locks: " + outstandingReadLocks.size());
//                }
                synchronized (writeLockedThread) {
                    writeLockedThread.notifyAll();
                }
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("readLock released and after notifying a write lock waiting thread " + writeLockedThread);
//                }
            }
            return;
        } else {
            LOG.warn("Possible lock problem: thread " + Thread.currentThread().getName() +
                    " released a read lock it didn't hold. Either the " +
                    "thread was interrupted or it never acquired the lock. " +
                    "Write lock: " + (writeLockedThread != null ? writeLockedThread.getName() : "null"),
                    new Throwable());
            if (LockOwner.DEBUG)
                debugReadLocks("ILLEGAL RELEASE");
        }
    }

    public synchronized boolean isLockedForWrite() {
        return writeLockedThread != null || (waitingForWriteLock != null && waitingForWriteLock.size() > 0);
    }

    public synchronized boolean hasLock() {
        return !outstandingReadLocks.isEmpty() || isLockedForWrite();
    }

    public synchronized boolean isLockedForRead(Thread owner) {
        for (int i = outstandingReadLocks.size() - 1; i > -1; i--) {
            if (((LockOwner) outstandingReadLocks.get(i)).getOwner() == owner)
                return true;
        }
        return false;
    }

    private void removeReadLock(int count) {
        Object owner = Thread.currentThread();
        for (int i = outstandingReadLocks.size() - 1; i > -1 && count > 0; i--) {
            LockOwner current = (LockOwner) outstandingReadLocks.get(i);
            if (current.getOwner() == owner) {
                outstandingReadLocks.remove(i);
                --count;
            }
        }
    }

    private void deadlockCheck() throws DeadlockException {
//    	if (writeLockedThread != null) {
//    		Lock lock = DeadlockDetection.isWaitingFor(writeLockedThread);
//    		if (lock != null && lock.hasLock())
//    			throw new DeadlockException();
//    	}
        final int size = outstandingReadLocks.size();
        LockOwner next;
        for (int i = 0; i < size; i++) {
            next = (LockOwner) outstandingReadLocks.get(i);
            Lock lock = DeadlockDetection.isWaitingFor(next.getOwner());
            if (lock != null) {
//                LOG.debug("Checking for deadlock...");
                lock.wakeUp();
            }
        }
    }

    /**
     * Detect circular wait on different resources: thread A has a write lock on
     * resource R1; thread B has a write lock on resource R2; thread A tries to
     * acquire lock on R2; thread B now tries to acquire lock on R1. Solution:
     * suspend existing write lock of thread A and grant it to B.
     *
     * @return true if the write lock should be granted to the current thread
     */
    private List checkForDeadlock(Thread waiter) {
        ArrayList waiters = new ArrayList(10);
        if (DeadlockDetection.wouldDeadlock(waiter, writeLockedThread, waiters)) {
            LOG.warn("Potential deadlock detected on lock " + getId() + "; killing threads: " + waiters.size());
//            for (int i = 0; i < waiters.size(); i++) {
//                WaitingThread wt = (WaitingThread) waiters.get(i);
//                LOG.debug("Waiter: " + wt.getThread().getName() + " -> " + wt.getLock().getId());
//            }
            return waiters.size() > 0 ? waiters : null;
        }
        return null;
    }

    /**
     * Check if a write lock can be granted, either because there are no
     * read locks, the read lock belongs to the current thread and can be
     * upgraded or the thread which holds the lock is blocked by another
     * lock held by the current thread.
     *
     * @return true if the write lock can be granted
     */
    private boolean grantWriteLock() {
        Thread waiter = Thread.currentThread();
        final int size = outstandingReadLocks.size();
        if (size == 0) {
            return true;
        }
        LockOwner next;
        // walk through outstanding read locks
        for (int i = 0; i < size; i++) {
            next = (LockOwner) outstandingReadLocks.get(i);
            // if the read lock is owned by the current thread, all is ok and we continue
            if (next.getOwner() != waiter) {
                // otherwise, check if the lock belongs to a thread which is currently blocked
                // by a lock owned by the current thread. if yes, it will be safe to grant the
                // write lock: the other thread will be blocked anyway.
                if (!DeadlockDetection.isBlockedBy(waiter, next.getOwner())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if a write lock can be granted, either because there are no
     * read locks or the read lock belongs to the current thread and can be
     * upgraded. This method is called whenever a lock is released.
     *
     * @return true if the write lock can be granted
     */
    private boolean grantWriteLockAfterRead() {
        // waiting write locks?
        if (waitingForWriteLock != null && waitingForWriteLock.size() > 0) {
            // yes, check read locks
            final int size = outstandingReadLocks.size();
            if (size > 0) {
                // grant lock if all read locks are held by the write thread
                WaitingThread waiter = (WaitingThread) waitingForWriteLock.get(0);
                return isCompatible(waiter.getThread());
            } else
                return true;
        }
        return false;
    }

    /**
     * Check if the specified thread has a read lock on the resource.
     *
     * @param owner the thread
     * @return true if owner has a read lock
     */
    private boolean hasReadLock(Thread owner) {
        LockOwner next;
        for (int i = 0; i < outstandingReadLocks.size(); i++) {
            next = (LockOwner) outstandingReadLocks.get(i);
            if (next.getOwner() == owner)
                return true;
        }
        return false;
    }

    public Thread getWriteLockedThread() {
        return writeLockedThread;
    }
    
    /**
     * Check if the specified thread holds either a write or a read lock
     * on the resource.
     *
     * @param owner the thread
     * @return true if owner has a lock
     */
    public boolean hasLock(Thread owner) {
        if (writeLockedThread == owner)
            return true;
        return hasReadLock(owner);
    }

    public void wakeUp() {
    }

    /**
     * Check if the pending request for a write lock is compatible
     * with existing read locks and other write requests. A lock request is
     * compatible with another lock request if: (a) it belongs to the same thread,
     * (b) it belongs to a different thread, but this thread is also waiting for a write lock.
     *
     * @param waiting
     * @return true if the lock request is compatible with all other requests and the
     * lock can be granted.
     */
    private boolean isCompatible(Thread waiting) {
        LockOwner next;
        for (int i = 0; i < outstandingReadLocks.size(); i++) {
            next = (LockOwner) outstandingReadLocks.get(i);
            // if the read lock is owned by the current thread, all is ok and we continue
            if (next.getOwner() != waiting) {
                // otherwise, check if the lock belongs to a thread which is currently blocked
                // by a lock owned by the current thread. if yes, it will be safe to grant the
                // write lock: the other thread will be blocked anyway.
                if (!DeadlockDetection.isBlockedBy(waiting, next.getOwner())) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized LockInfo getLockInfo() {
        LockInfo info;
        String[] readers = new String[0];
        if (outstandingReadLocks != null) {
            readers = new String[outstandingReadLocks.size()];
            for (int i = 0; i < outstandingReadLocks.size(); i++) {
                LockOwner owner = (LockOwner) outstandingReadLocks.get(i);
                readers[i] = owner.getOwner().getName();
            }
        }
        if (writeLockedThread != null) {
            info = new LockInfo(LockInfo.RESOURCE_LOCK, LockInfo.WRITE_LOCK, getId(), new String[] { writeLockedThread.getName() });
            info.setReadLocks(readers);
        } else {
            info = new LockInfo(LockInfo.RESOURCE_LOCK, LockInfo.READ_LOCK, getId(), readers);
        }
        if (waitingForWriteLock != null) {
            String waitingForWrite[] = new String[waitingForWriteLock.size()];
            for (int i = 0; i < waitingForWriteLock.size(); i++) {
                waitingForWrite[i] = ((WaitingThread) waitingForWriteLock.get(i)).getThread().getName();
            }
            info.setWaitingForWrite(waitingForWrite);
        }
        return info;
    }

    private void debugReadLocks(String msg) {
        for (int i = 0; i < outstandingReadLocks.size(); i++) {
            LockOwner owner = (LockOwner) outstandingReadLocks.get(i);
            LOG.debug(msg + ": " + owner.getOwner(), owner.getStack());
        }
    }

    private String listReadLocks() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < outstandingReadLocks.size(); i++) {
            LockOwner owner = (LockOwner) outstandingReadLocks.get(i);
            buf.append(' ');
            buf.append(((Thread) owner.getOwner()).getName());
        }
        return buf.toString();
    }
}