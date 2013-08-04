/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/* eXist Open Source Native XML Database
 * Copyright (C) 2001-2006 The eXist team
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
 *  along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: FunDocumentURI.java 6868 2007-11-05 19:14:55Z brihaye $
 */

package org.exist.xquery.functions;

import org.exist.dom.NodeProxy;
import org.exist.dom.QName;
import org.exist.memtree.DocumentImpl;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Dependency;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.AnyURIValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

/**
 * @author wolf
 */
public class FunDocumentURI extends Function {

	public final static FunctionSignature signature =
		new FunctionSignature(
			new QName("document-uri", Function.BUILTIN_FUNCTION_NS),
			"Returns the absolute URI of the resource from which the document node $a " + 
			"was constructed, if none such URI exists returns the empty sequence. " +
			"If $a is the empty sequence, returns the empty sequence.",
			new SequenceType[] {
				 new SequenceType(Type.NODE, Cardinality.ZERO_OR_ONE)
			},
			new SequenceType(Type.ANY_URI, Cardinality.ZERO_OR_ONE));
			
	/**
	 * 
	 */
	public FunDocumentURI(XQueryContext context) {
		super(context, signature);
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#eval(org.exist.xquery.StaticContext, org.exist.dom.DocumentSet, org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);       
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT ITEM", contextItem.toSequence());
        }          
        
        //if (contextItem != null)
            //contextSequence = contextItem.toSequence();        
		
		Sequence seq = getArgument(0).eval(contextSequence, contextItem);
        Sequence result = Sequence.EMPTY_SEQUENCE;
        if (!seq.isEmpty()) {
            NodeValue value = (NodeValue) seq.itemAt(0);
            if (value.getImplementationType() == NodeValue.PERSISTENT_NODE) { 
        		NodeProxy node = (NodeProxy) value;
        		//Returns the empty sequence if the node is not a document node. 
        		if (node.isDocument()) {
        			XmldbURI path = node.getDocument().getURI(); 
        			result = new AnyURIValue(path);
        		}        		
            } else {
        		if (value instanceof DocumentImpl && ((DocumentImpl)value).getDocumentURI() != null) {
        			result = new AnyURIValue(((DocumentImpl)value).getDocumentURI());
        		}        		
            }
        }
        
        if (context.getProfiler().isEnabled()) 
            context.getProfiler().end(this, "", result); 
        
        return result;           
	}

}
