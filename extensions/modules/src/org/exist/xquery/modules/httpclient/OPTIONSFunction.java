/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-03 Wolfgang M. Meier
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
 *  $Id$
 */
package org.exist.xquery.modules.httpclient;

import org.apache.commons.httpclient.methods.OptionsMethod;

import org.exist.dom.QName;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

import java.io.IOException;

/**
 * @author Adam Retter <adam.retter@devon.gov.uk>
 * @author Andrzej Taramina <andrzej@chaeron.com>
 * @serial 20070905
 * @version 1.2
 */
public class OPTIONSFunction extends BaseHTTPClientFunction {
    
    public final static FunctionSignature signature =
        new FunctionSignature(
        new QName( "options", NAMESPACE_URI, PREFIX ),
        "Performs a HTTP OPTIONS request. $a is the URL, $b determines if cookies persist for the query lifetime. $c defines any HTTP Request Headers to set in the form <headers><header name=\"\" value=\"\"/></headers>."
        + " This method returns the HTTP response encoded as an XML fragment, that looks as follows: <httpclient:response  xmlns:httpclient=\"http://exist-db.org/xquery/httpclient\" statusCode=\"200\"><httpclient:headers><httpclient:header name=\"name\" value=\"value\"/>...</httpclient:headers></httpclient:response>",
        new SequenceType[] {
            new SequenceType( Type.ANY_URI, Cardinality.EXACTLY_ONE ),
            new SequenceType( Type.BOOLEAN, Cardinality.EXACTLY_ONE ),
            new SequenceType( Type.ELEMENT, Cardinality.ZERO_OR_ONE )
            },
        new SequenceType( Type.ITEM, Cardinality.EXACTLY_ONE ) 
        );
    
    
    public OPTIONSFunction( XQueryContext context )
    {
        super( context, signature );
    }
    
    
    public Sequence eval( Sequence[] args, Sequence contextSequence ) throws XPathException
    {
        Sequence    response = null;
        
        // must be a URL
        if( args[0].isEmpty() ) {
            return( Sequence.EMPTY_SEQUENCE );
        }
        
        //get the url
        String url = args[0].itemAt( 0 ).getStringValue();
        //get the persist cookies
        boolean persistCookies = args[1].effectiveBooleanValue();
        
        //setup OPTIONS request
        OptionsMethod options = new OptionsMethod( url );
        
        //setup OPTIONS Request Headers
        if( !args[2].isEmpty() ) {
            setHeaders( options, ( (NodeValue)args[2].itemAt( 0 ) ).getNode() );
        }
        
        try {
            //execute the request
            response = doRequest( context, options, persistCookies );	
        }
        catch( IOException ioe ) {
            throw( new XPathException( ioe ) );
        }
        finally {
            options.releaseConnection();
        }
        
        return( response );
    }
}
