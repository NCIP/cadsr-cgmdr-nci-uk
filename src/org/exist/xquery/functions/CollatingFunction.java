/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 Wolfgang M. Meier
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
 *  $Id: CollatingFunction.java 715 2004-10-15 13:59:28Z wolfgang_m $
 */
package org.exist.xquery.functions;

import java.text.Collator;

import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;

/**
 * Base class for functions accepting an optional collation argument. 
 * @author wolf
 */
public abstract class CollatingFunction extends Function {

	/**
	 * @param context
	 * @param signature
	 */
	public CollatingFunction(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	protected Collator getCollator(Sequence contextSequence, Item contextItem, int arg) throws XPathException {
		if(getSignature().getArgumentCount() == arg) {
			String collationURI = getArgument(arg - 1).eval(contextSequence, contextItem).getStringValue();
			LOG.debug("Using collation: " + collationURI);
			return context.getCollator(collationURI);
		} else
			return context.getDefaultCollator();
	}
}
