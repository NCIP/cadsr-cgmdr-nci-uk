/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Apache FOP Transformation Extension
 *  Copyright (C) 2007 Craig Goodyer at the University of the West of England
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
 *  $Id: XSLFOModule.java 7787 2008-05-21 13:54:40Z deliriumsky $
 */

package org.exist.xquery.modules.xslfo;

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

/**
 * @author Craig Goodyer <craiggoodyer@gmail.com>
 * @author Adam Retter <adam.retter@devon.gov.uk>
 */
public class XSLFOModule extends AbstractInternalModule {

	public final static String NAMESPACE_URI = "http://exist-db.org/xquery/xslfo";
	public final static String PREFIX = "xslfo";

	private final static FunctionDef[] functions = {
			new FunctionDef(RenderFunction.signatures[0], RenderFunction.class),
			new FunctionDef(RenderFunction.signatures[1], RenderFunction.class) };

	public XSLFOModule() {
		super(functions);
	}

	public String getNamespaceURI() {
		return NAMESPACE_URI;
	}

	public String getDefaultPrefix() {
		return PREFIX;
	}

	public String getDescription() {
		return "Module for performing XSL-FO transformations";
	}
}
