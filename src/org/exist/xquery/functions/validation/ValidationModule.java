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
 *  $Id: ValidationModule.java 5915 2007-05-28 16:52:11Z dizzzz $
 */
package org.exist.xquery.functions.validation;

import org.exist.dom.QName;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;
import org.exist.xquery.XPathException;

/**
 * @author Dannes Wessels (dizzzz@exist-db.org)
 */
public class ValidationModule extends AbstractInternalModule {
    
    public final static String NAMESPACE_URI = "http://exist-db.org/xquery/validation";
    
    public final static String PREFIX = "validation";
    
    public final static FunctionDef[] functions = {
       new FunctionDef(Validation.signatures[0], Validation.class),
       new FunctionDef(Validation.signatures[1], Validation.class),
       new FunctionDef(Validation.signatures[2], Validation.class),
       new FunctionDef(Validation.signatures[3], Validation.class),
       new FunctionDef(GrammarTooling.signatures[0], GrammarTooling.class),
       new FunctionDef(GrammarTooling.signatures[1], GrammarTooling.class)
    };
    
    public final static QName EXCEPTION_QNAME =
            new QName("exception", ValidationModule.NAMESPACE_URI, ValidationModule.PREFIX);
    public final static QName EXCEPTION_MESSAGE_QNAME =
            new QName("exception-message", ValidationModule.NAMESPACE_URI, ValidationModule.PREFIX);
    
    public ValidationModule() throws XPathException {
        super(functions);
        declareVariable(EXCEPTION_QNAME, null);
        declareVariable(EXCEPTION_MESSAGE_QNAME, null);
    }
    
        /* (non-Javadoc)
         * @see org.exist.xquery.ValidationModule#getDescription()
         */
    public String getDescription() {
        return "XML validation and grammars functions.";
    }
    
        /* (non-Javadoc)
         * @see org.exist.xquery.ValidationModule#getNamespaceURI()
         */
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }
    
        /* (non-Javadoc)
         * @see org.exist.xquery.ValidationModule#getDefaultPrefix()
         */
    public String getDefaultPrefix() {
        return PREFIX;
    }
}
