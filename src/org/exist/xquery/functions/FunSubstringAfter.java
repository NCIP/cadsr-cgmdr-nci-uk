/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2007 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: FunSubstringAfter.java 6391 2007-08-21 07:16:18Z ellefj $
 */
package org.exist.xquery.functions;

import java.text.Collator;

import org.exist.dom.QName;
import org.exist.util.Collations;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Constants;
import org.exist.xquery.Dependency;
import org.exist.xquery.Expression;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/**
 * Built-in function fn:substring-after($operand1 as xs:string?, $operand2 as xs:string?) as xs:string?
 *
 */
public class FunSubstringAfter extends CollatingFunction {

	public final static FunctionSignature signatures[] = {
		new FunctionSignature(
			new QName("substring-after", Function.BUILTIN_FUNCTION_NS),
			"Returns the substring of the value of $a that follows the first occurrence " +
			"of a sequence of the value of $b. If the value of $a or $b is the empty " +
			"sequence it is interpreted as the zero-length string. If the value of " +
			"$b is the zero-length string, the zero-length string is returned. " +
			"If the value of $a does not contain a string that is equal to the value " +
			"of $b, the zero-length string is returned.",
			new SequenceType[] {
				 new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
				 new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE)
			},
			new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE)),
		new FunctionSignature(
				new QName("substring-after", Function.BUILTIN_FUNCTION_NS),
			"Returns the substring of the value of $a that follows the first occurrence " +
			"of a sequence of the value of $b in the collation $c. If the value of $a or $b is the empty " +
			"sequence it is interpreted as the zero-length string. If the value of " +
			"$b is the zero-length string, the zero-length string is returned. " +
			"If the value of $a does not contain a string that is equal to the value " +
			"of $b, the zero-length string is returned.",
				new SequenceType[] {
					 new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
					 new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
					 new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
				},
				new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE))
	};
					
	public FunSubstringAfter(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	public Sequence eval(Sequence contextSequence, Item contextItem)	throws XPathException {
       if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);
            context.getProfiler().message(this, Profiler.DEPENDENCIES,
                    "DEPENDENCIES",
                    Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES,
                        "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES,
                        "CONTEXT ITEM", contextItem.toSequence());
        }
           
		Expression arg0 = getArgument(0);
		Expression arg1 = getArgument(1);

		if (contextItem != null)
			contextSequence = contextItem.toSequence();
			
		Sequence seq1 = arg0.eval(contextSequence);
		Sequence seq2 = arg1.eval(contextSequence);

        String value;
        String cmp;
        Sequence result;
		if (seq1.isEmpty()) {
            value = StringValue.EMPTY_STRING.getStringValue();
        } else {
            value = seq1.getStringValue();
        }
        
        if (seq2.isEmpty()) {
            cmp = StringValue.EMPTY_STRING.getStringValue();
        } else {
            cmp = seq2.getStringValue();
        }
        
        if(cmp.length() == 0)
            result = new StringValue(value);
        else {
            Collator collator = getCollator(contextSequence, contextItem, 3);
            int p = Collations.indexOf(collator, value, cmp);
            if (p == Constants.STRING_NOT_FOUND)
                result = StringValue.EMPTY_STRING;
            else
                result = new StringValue(p + cmp.length() < value.length() ? 
                                         value.substring(p + cmp.length()) : ""
                                         );        		
        }
        
        if (context.getProfiler().isEnabled())
            context.getProfiler().end(this, "", result);
        
        return result;        
        
	}
}
