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
 *  $Id: ExtXCollection.java 6539 2007-09-12 10:00:58Z brihaye $
 */
package org.exist.xquery.functions;

import org.exist.dom.QName;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.functions.xmldb.XMLDBModule;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

/**
 * @author wolf
 */
public class ExtXCollection extends ExtCollection {

	public final static FunctionSignature signature =
        new FunctionSignature(
            new QName("xcollection", Function.BUILTIN_FUNCTION_NS),
            "Works like fn:collection(), but does not include documents " +
            "found in sub-collections of the specified collections.",
            new SequenceType[] {
                 new SequenceType(Type.STRING, Cardinality.ONE_OR_MORE)},
            new SequenceType(Type.NODE, Cardinality.ZERO_OR_MORE),
            true,
            "Moved to the '" + XMLDBModule.NAMESPACE_URI + "' namespace.");
				
	/**
	 * @param context
	 */
	public ExtXCollection(XQueryContext context) {
		super(context, signature, false);
	}

}
