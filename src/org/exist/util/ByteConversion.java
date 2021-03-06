/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2000-04,  Wolfgang Meier (wolfgang@exist-db.org)
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 *  $Id: ByteConversion.java 680 2004-10-03 17:47:38Z wolfgang_m $
 */
package org.exist.util;

/**
 * A collection of static methods to write integer values from/to a
 * byte array.
 *
 *@author Wolfgang Meier <wolfgang@exist-db.org>
 */
public class ByteConversion {

    /**
     *  Read an integer value from the specified byte array, starting at start.
     */
    public final static int byteToInt( final byte data[], final int start ) {
        return ( data[start] & 0xff ) |
            ( ( data[start + 1] & 0xff ) << 8 ) |
            ( ( data[start + 2] & 0xff ) << 16 ) |
            ( ( data[start + 3] & 0xff ) << 24 );
    }

    /**
     *  Read a long value from the specified byte array, starting at start.
     */
    public final static long byteToLong( final byte[] data, final int start ) {
        return ( ( ( (long) data[start] ) & 0xffL ) << 56 ) |
            ( ( ( (long) data[start + 1] ) & 0xffL ) << 48 ) |
            ( ( ( (long) data[start + 2] ) & 0xffL ) << 40 ) |
            ( ( ( (long) data[start + 3] ) & 0xffL ) << 32 ) |
            ( ( ( (long) data[start + 4] ) & 0xffL ) << 24 ) |
            ( ( ( (long) data[start + 5] ) & 0xffL ) << 16 ) |
            ( ( ( (long) data[start + 6] ) & 0xffL ) << 8 ) |
            ( ( (long) data[start + 7] ) & 0xffL );
    }

    /**
     *  Read a short value from the specified byte array, starting at start.
     */
    public final static short byteToShort( final byte[] data, final int start ) {
        return (short) ( ( ( data[start + 1] & 0xff ) << 8 ) |
            ( data[start] & 0xff ) );
    }

    /**
     * Write an int value to the specified byte array. The first byte is written
     * into the location specified by start.
     *
     *@param  v the value
     *@param  data  the byte array to write into
     *@param  start  the offset
     *@return   the byte array
     */
    public final static byte[] intToByte( final int v, final byte[] data, final int start ) {
        data[start] = (byte) ( ( v >>> 0 ) & 0xff );
        data[start + 1] = (byte) ( ( v >>> 8 ) & 0xff );
        data[start + 2] = (byte) ( ( v >>> 16 ) & 0xff );
        data[start + 3] = (byte) ( ( v >>> 24 ) & 0xff );
        return data;
    }

    /**
     * Write a long value to the specified byte array. The first byte is written
     * into the location specified by start.
     *
     *@param  v the value
     *@param  data  the byte array to write into
     *@param  start  the offset
     *@return   the byte array
     */
    public final static byte[] longToByte( final long v, final byte[] data, final int start ) {
        data[start + 7] = (byte) ( ( v >>> 0 ) & 0xff );
        data[start + 6] = (byte) ( ( v >>> 8 ) & 0xff );
        data[start + 5] = (byte) ( ( v >>> 16 ) & 0xff );
        data[start + 4] = (byte) ( ( v >>> 24 ) & 0xff );
        data[start + 3] = (byte) ( ( v >>> 32 ) & 0xff );
        data[start + 2] = (byte) ( ( v >>> 40 ) & 0xff );
        data[start + 1] = (byte) ( ( v >>> 48 ) & 0xff );
        data[start] = (byte) ( ( v >>> 56 ) & 0xff );
        return data;
    }


    /**
     * Write an int value to a newly allocated byte array.
     *
     *@param  v the value
     *@return   the byte array
     */
    public final static byte[] longToByte( final long v ) {
        byte[] data = new byte[8];
        data[7] = (byte) ( ( v >>> 0 ) & 0xff );
        data[6] = (byte) ( ( v >>> 8 ) & 0xff );
        data[5] = (byte) ( ( v >>> 16 ) & 0xff );
        data[4] = (byte) ( ( v >>> 24 ) & 0xff );
        data[3] = (byte) ( ( v >>> 32 ) & 0xff );
        data[2] = (byte) ( ( v >>> 40 ) & 0xff );
        data[1] = (byte) ( ( v >>> 48 ) & 0xff );
        data[0] = (byte) ( ( v >>> 56 ) & 0xff );
        return data;
    }


    /**
     * Write a short value to the specified byte array. The first byte is written
     * into the location specified by start.
     *
     *@param  v the value
     *@param  data  the byte array to write into
     *@param  start  the offset
     *@return   the byte array
     */
    public final static byte[] shortToByte( final short v, final byte[] data, final int start ) {
        data[start] = (byte) ( ( v >>> 0 ) & 0xff );
        data[start + 1] = (byte) ( ( v >>> 8 ) & 0xff );
        return data;
    }


    /**
     * Write a short value to a newly allocated byte array.
     *
     *@param  v the value
     *@return   the byte array
     */
    public final static byte[] shortToByte( final short v ) {
        byte[] data = new byte[2];
        data[0] = (byte) ( ( v >>> 0 ) & 0xff );
        data[1] = (byte) ( ( v >>> 8 ) & 0xff );
        return data;
    }
}

