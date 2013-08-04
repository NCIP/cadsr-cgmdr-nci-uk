/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
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
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id: ASCIICharSet.java 2436 2006-01-07 21:47:15Z brihaye $
 */
package org.exist.util.serializer.encodings;

/**
 * @author wolf
 */
public class ASCIICharSet extends CharacterSet {

	protected final static CharacterSet instance = new ASCIICharSet();
	
	/* (non-Javadoc)
	 * @see org.exist.util.serializer.encodings.CharacterSet#inCharacterSet(char)
	 */
	public boolean inCharacterSet(char ch) {
		return ch < 0x7F;
	}

	public static CharacterSet getInstance() {
		return instance;
	}
}
