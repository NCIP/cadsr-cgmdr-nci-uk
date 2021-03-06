/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Context Module Extension GetVarFunction
 *  Copyright (C) 2006 Adam Retter <adam.retter@devon.gov.uk>
 *  www.adamretter.co.uk
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
 *  $Id: GetVarFunction.java 4565 2006-10-12 12:42:18Z deliriumsky $
 */

package org.exist.xquery.modules.context;

import org.exist.dom.QName;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.xquery.XPathException;
import org.exist.xquery.XPathUtil;
import org.exist.xquery.XQueryContext;

/**
 * eXist Context Module Extension GetVarFunction 
 * 
 * The Variable Getting functionality of the eXist Context Module Extension that
 * allows variables to be retreived from the Context of the executing XQuery that have previously
 * been set with set-var()  
 * 
 * @author Adam Retter <adam.retter@devon.gov.uk>
 * @serial 2006-03-01
 * @version 1.3
 *
 * @see org.exist.xquery.Function
 */
public class GetVarFunction extends Function {

	public final static FunctionSignature signature = new FunctionSignature(
			new QName("get-var", ContextModule.NAMESPACE_URI, ContextModule.PREFIX),
			"get's a variable named $a from the current context",
			new SequenceType[] {
				new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
			},
			new SequenceType(Type.ITEM, Cardinality.ZERO_OR_MORE)
	);
		
	/**
	 * GetVarFunction Constructor
	 * 
	 * @param context	The Context of the calling XQuery
	 */
	public GetVarFunction(XQueryContext context)
	{
		super(context, signature);
	}

	/**
	 * evaluate the call to the xquery get-var() function,
	 * it is really the main entry point of this class
	 * 
	 * @param contextSequence	the Context Sequence to operate on
	 * @param contextItem		the Context Item to operate on
	 * @return		The sequence stored in the context by set-var()
	 * 
	 * @see org.exist.xquery.Function#eval(org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException
	{
		String name = getArgument(0).eval(contextSequence, contextItem).getStringValue();
		
		Object o = context.getXQueryContextVar(name);
		if(o == null)
		{
			return Sequence.EMPTY_SEQUENCE;
		}
		return XPathUtil.javaObjectToXPath(o, context);
	}
}
