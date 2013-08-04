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
 *  $Id: RemoteCompiledExpression.java 2436 2006-01-07 21:47:15Z brihaye $
 */
package org.exist.xmldb;

import org.xmldb.api.base.CompiledExpression;
/**
 * This is just a placeholder for an expression that has been compiled
 * on the server. It only stores the xquery string.
 * 
 * @author wolf
 *
 */
public class RemoteCompiledExpression implements CompiledExpression {

	private String xquery;
	
	public RemoteCompiledExpression(String xquery) {
		this.xquery = xquery;
	}

	/* (non-Javadoc)
	 * @see org.exist.xmldb.CompiledExpression#reset()
	 */
	public void reset() {
	}

	public String getQuery() {
		return xquery;
	}
}
