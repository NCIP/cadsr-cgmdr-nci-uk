/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/* eXist Native XML Database
 * Copyright (C) 2000-2006, The eXist team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * $Id: FunEndsWith.java 6401 2007-08-22 15:18:10Z brihaye $
 */

package org.exist.xquery.functions;

import java.text.Collator;

import org.exist.dom.QName;
import org.exist.util.Collations;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Dependency;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

public class FunEndsWith extends CollatingFunction {

    public final static FunctionSignature signatures [] = {
	new FunctionSignature(
			      new QName("ends-with", Function.BUILTIN_FUNCTION_NS),

			      "Returns true if the string value of $b is a suffix of the " +
			      "string value of $a, false otherwise. If either $a or $b is the empty " +
			      "sequence, the empty sequence is returned.",			new SequenceType[] {
				  new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
				  new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE)},
			      new SequenceType(Type.BOOLEAN, Cardinality.ONE)),
	new FunctionSignature (
			       new QName("ends-with", Function.BUILTIN_FUNCTION_NS),
			       "Returns true if the string value of $b is a suffix of the " +
			       "string value of $a using collation $c, " + " false otherwise. If " +
			       "either $a or $b is the empty sequence, the empty sequence" +
			       " is returned.",
			       new SequenceType[] {
				   new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
				   new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
				   new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
			       },
			       new SequenceType(Type.BOOLEAN, Cardinality.ZERO_OR_ONE))			
    };

    public FunEndsWith(XQueryContext context, FunctionSignature signature) {
    	super(context, signature);
    }

    //Why override ?
    public int returnsType() {
    	return Type.BOOLEAN;
    }

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

        Sequence s1 = getArgument(0).eval(contextSequence, contextItem);
        Sequence s2 = getArgument(1).eval(contextSequence, contextItem);
		
        Sequence result;
        if (s1.isEmpty() || s2.isEmpty())
            result = Sequence.EMPTY_SEQUENCE;
        else {
		    Collator collator = getCollator(contextSequence, contextItem, 3);
		    if (Collations.endsWith(collator, s1.getStringValue(), s2.getStringValue()))
	            result = BooleanValue.TRUE;
		    else
                result = BooleanValue.FALSE;
        }
        
        if (context.getProfiler().isEnabled()) 
            context.getProfiler().end(this, "", result); 
        
        return result;        
    }

}
