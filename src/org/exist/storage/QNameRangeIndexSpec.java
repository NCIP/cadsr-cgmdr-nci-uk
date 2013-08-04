/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Team
 *
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
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id: QNameRangeIndexSpec.java 5693 2007-04-17 19:45:31Z ellefj $
 */
package org.exist.storage;

import java.util.Map;

import org.exist.dom.QName;
import org.exist.util.DatabaseConfigurationException;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Type;

public class QNameRangeIndexSpec extends RangeIndexSpec {

	private QName qname;
	
	public QNameRangeIndexSpec(Map namespaces, String name, String typeStr) 
	throws DatabaseConfigurationException {
		try {
            this.type = Type.getType(typeStr);
        } catch (XPathException e) {
            throw new DatabaseConfigurationException("Unknown type: " + typeStr);
        }
        
        boolean isAttribute = false;
        if (name.startsWith("@")) {
            isAttribute = true;
            name = name.substring(1);
        }
        String prefix = QName.extractPrefix(name);
        String localName = QName.extractLocalName(name);
        String namespaceURI = "";
        if (prefix != null) {
            namespaceURI = (String) namespaces.get(prefix);
            if(namespaceURI == null) {
                throw new DatabaseConfigurationException("No namespace defined for prefix: " + prefix +
                    " in index definition");
            }
        }
        qname = new QName(localName, namespaceURI, prefix);
        if (isAttribute)
            qname.setNameType(ElementValue.ATTRIBUTE);
	}

	public QName getQName() {
		return qname;
	}
}
