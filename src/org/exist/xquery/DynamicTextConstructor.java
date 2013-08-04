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
 *  $Id: DynamicTextConstructor.java 7733 2008-05-09 09:11:27Z wolfgang_m $
 */
package org.exist.xquery;

import org.exist.memtree.MemTreeBuilder;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;


/**
 * Implements a dynamic text constructor. Contrary to {@link org.exist.xquery.TextConstructor},
 * the character content of a DynamicTextConstructor is determined only at evaluation time.
 * 
 * @author wolf
 */
public class DynamicTextConstructor extends NodeConstructor {

    final private Expression content;
    
    /**
     * @param context
     */
    public DynamicTextConstructor(XQueryContext context, Expression contentExpr) {
        super(context);
        this.content = new Atomize(context, contentExpr);
    }

    public Expression getContent() {
        return content;
    }
    
    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#analyze(org.exist.xquery.AnalyzeContextInfo)
     */
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        super.analyze(contextInfo);
        contextInfo.setParent(this);
        content.analyze(contextInfo);
    }
    
    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#eval(org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
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

        if (newDocumentContext)
            context.pushDocumentContext();
        
        Sequence result;
        try {
            Sequence contentSeq = content.eval(contextSequence, contextItem);

            //It is possible for a text node constructor to construct a text node containing a zero-length string.
            //However, if used in the content of a constructed element or document node,
            //such a text node will be deleted or merged with another text node.
            //TODO : how to enforce this behaviour ? We have to detect that the parent of the text node
            //is an element or a document node. Unfortunately, we are using a *new* MemTreeBuilder
            if(contentSeq.isEmpty())
                result = Sequence.EMPTY_SEQUENCE;
            else {
                MemTreeBuilder builder = context.getDocumentBuilder();
                //The line below if necessayr to make fn-root-17 pass (but no other test AFAICT)
                //let $var := text {"a text Node"} return  fn:root(text {"A text Node"})
                //<expected-result compare="Text">A text Node</expected-result>
                //The result of the constructed text node is reused
                //TODO : how to avoid this ?
                builder.startDocument();
                context.proceed(this, builder);
                StringBuffer buf = new StringBuffer();
                for(SequenceIterator i = contentSeq.iterate(); i.hasNext(); ) {
                    context.proceed(this, builder);
                    Item next = i.nextItem();
                    if(buf.length() > 0)
                        buf.append(' ');
                    buf.append(next.toString());
                }
                if (buf.length() == 0)
                    result = Sequence.EMPTY_SEQUENCE;
                else {
                    int nodeNr = builder.characters(buf);
                    result = builder.getDocument().getNode(nodeNr);
                }
            }
        } finally {
            if (newDocumentContext)
                context.popDocumentContext();
        }

        if (context.getProfiler().isEnabled())           
            context.getProfiler().end(this, "", result);
        
        return result;
        
    }
    
    /* (non-Javadoc)
     * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
        dumper.display("text {");
        dumper.startIndent();
        content.dump(dumper);
        dumper.endIndent();
        dumper.nl().display("}");
    }
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	result.append("text {");        
    	result.append(content.toString());        
    	result.append("}");
    	return result.toString();
    }    

    public void resetState(boolean postOptimization) {
    	super.resetState(postOptimization);
    	content.resetState(postOptimization);
    }

    public void accept(ExpressionVisitor visitor) {
        visitor.visitTextConstructor(this);
    }
}
