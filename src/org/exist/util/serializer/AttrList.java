/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
*  eXist Open Source Native XML Database
*  Copyright (C) 2001-04 Wolfgang M. Meier (wolfgang@exist-db.org) 
*  and others (see http://exist-db.org)
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
*  $Id: AttrList.java 6320 2007-08-01 18:01:06Z ellefj $
*/
package org.exist.util.serializer;

import org.exist.dom.AttrImpl;
import org.exist.dom.QName;

/**
 * Represents a list of attributes. Each attribute is defined by
 * a {@link org.exist.dom.QName} and a value. Instances
 * of this class can be passed to 
 * {@link org.exist.util.serializer.Receiver#startElement(QName, AttrList)}.
 * 
 * @author wolf
 */
public class AttrList {

	protected QName names[] = new QName[4];
	protected String values[] = new String[4];
    protected int type[] = new int[4];
    protected int size = 0;
	
	/**
	 * 
	 */
	public AttrList() {
		super();
	}

    public void addAttribute(QName name, String value) {
        addAttribute(name, value, AttrImpl.CDATA);
    }

    public void addAttribute(QName name, String value, int attrType) {
		ensureCapacity();
		names[size] = name;
		values[size] = value;
        type[size] = attrType;
        size++;
	}
	
	public int getLength() {
		return size;
	}
	
	public QName getQName(int pos) {
		return names[pos];
	}
	
	public String getValue(int pos) {
		return values[pos];
	}
	
	public String getValue(QName name) {
		for(int i = 0; i < size; i++) {
			if(names[i].equalsSimple(name))
				return values[i];
		}
		return null;
	}

    public int getType(int pos) {
        return type[pos];
    }
    
    private void ensureCapacity() {
		if(size == names.length) {
			// resize
			final int newSize = names.length * 3 / 2;
			QName tnames[] = new QName[newSize];
			System.arraycopy(names, 0, tnames, 0, names.length);
			
			String tvalues[] = new String[newSize];
			System.arraycopy(values, 0, tvalues, 0, values.length);

            int ttype[] = new int[newSize];
            System.arraycopy(type, 0, ttype, 0, type.length);

            names = tnames;
			values = tvalues;
            type = ttype;
        }
	}
}
