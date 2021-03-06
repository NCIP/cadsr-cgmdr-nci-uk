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
 *  $Id: FuzzyIndexTerms.java 5843 2007-05-18 15:42:38Z wolfgang_m $
 */
package org.exist.xquery.functions.text;

import org.exist.dom.DocumentSet;
import org.exist.dom.NodeSet;
import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;


/**
 * @author Wolfgang Meier (wolfgang@exist-db.org)
 */
public class FuzzyIndexTerms extends BasicFunction {

	public final static FunctionSignature signature = new FunctionSignature(
			new QName("fuzzy-index-terms", TextModule.NAMESPACE_URI, TextModule.PREFIX),
			"Compares the specified argument against the contents of the fulltext index. Returns " +
            "a sequence of strings which are similar to the argument. Similarity is based on Levenshtein " +
            "distance. This function may not be useful in its current form and is subject to change.",
			new SequenceType[]{
					new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE)},
			new SequenceType(Type.STRING, Cardinality.ZERO_OR_MORE));
	
	public FuzzyIndexTerms(XQueryContext context) {
		super(context, signature);
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
	 */
	public Sequence eval(Sequence[] args, Sequence contextSequence)
		throws XPathException {
		if(args[0].isEmpty())
			return Sequence.EMPTY_SEQUENCE;
		DocumentSet docs;
		if(contextSequence instanceof NodeSet)
			docs = contextSequence.getDocumentSet();
		else
			docs = context.getStaticallyKnownDocuments();
		String term = args[0].getStringValue();
		String[] matches =
			context.getBroker().getTextEngine().getIndexTerms(docs, new FuzzyMatcher(term, 0.65));
		ValueSequence result = new ValueSequence();
		for(int i = 0; i < matches.length; i++)
			result.add(new StringValue(matches[i]));
		return result;
	}
}
