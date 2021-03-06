/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id$
 */
package org.exist.management;

public class Cache implements CacheMBean {

    private org.exist.storage.cache.Cache cache;

    public Cache(org.exist.storage.cache.Cache cache) {
        this.cache = cache;
    }

    public String getType() {
        return cache.getType();
    }

    public int getSize() {
        return cache.getBuffers();
    }

    public int getUsed() {
        return cache.getUsedBuffers();
    }

    public int getHits() {
        return cache.getHits();
    }

    public int getFails() {
        return cache.getFails();
    }

    public String getFileName() {
        return cache.getFileName();
    }
}
