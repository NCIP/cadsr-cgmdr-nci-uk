/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Project
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
 *  $Id: FunEscapeURI.java 4403 2006-09-27 12:27:37Z wolfgang_m $
 */
package org.exist.xquery.functions;

import org.exist.dom.QName;
import org.exist.util.UTF8;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Dependency;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/**
 * @author wolf
 *
 */
public class FunEscapeURI extends BasicFunction {

    public final static FunctionSignature signature =
        new FunctionSignature(
            new QName("escape-uri", Function.BUILTIN_FUNCTION_NS),
            "This function applies the URI escaping rules defined in section 2 " +
            "of [RFC 2396] as amended by [RFC 2732], with one exception, to " +
            "the string supplied as $a, which typically represents all or part " +
            "of a URI. The effect of the function is to escape a set of identified " +
            "characters in the string. Each such character is replaced in the string " +
            "by an escape sequence, which is formed by encoding the character " +
            "as a sequence of octets in UTF-8, and then representing each of these " +
            "octets in the form %HH, where HH is the hexadecimal representation " +
            "of the octet. $b indicates whether to escape reserved characters.",
            new SequenceType[] {
                 new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE),
                 new SequenceType(Type.BOOLEAN, Cardinality.EXACTLY_ONE)
            },
            new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE));
    
    public FunEscapeURI(XQueryContext context) {
        super(context, signature);
    }
    
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);       
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
        }
        
        Sequence result;
        if (args[0].isEmpty())
            result = StringValue.EMPTY_STRING;
        else {
            String uri = args[0].getStringValue();
            boolean escapeReserved = args[1].effectiveBooleanValue();
            return new StringValue(escape(uri, escapeReserved));
        }
        
        if (context.getProfiler().isEnabled()) 
            context.getProfiler().end(this, "", result); 
        
        return result;            
    }
    
    /**
     * Does the actual escaping. This method is copied from Michael Kay's
     * saxon (see http://saxon.sf.net).
     * 
     * @param s
     * @param escapeReserved
     */
    public static String escape(CharSequence s, boolean escapeReserved) {
        //TODO : use dedidated URIUtils... -pb
        StringBuffer sb = new StringBuffer(s.length());
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9')) {
                sb.append(c);
            } else if (c<=0x20 || c>=0x7f) {
                escapeChar(c, ((i+1)<s.length() ? s.charAt(i+1) : ' '), sb);
            } else if (escapeReserved) {
                if ("-_.!~*'()%".indexOf(c)>=0) {
                    sb.append(c);
                } else {
                    escapeChar(c, ' ', sb);
                }
            } else {
                if ("-_.!~*'()%;/?:@&=+$,#[]".indexOf(c)>=0) {
                    sb.append(c);
                } else {
                    escapeChar(c, ' ', sb);
                }
            }
        }
        return sb.toString();
    }

    private static final String hex = "0123456789ABCDEF";

    private static void escapeChar(char c, char c2, StringBuffer sb) {
        byte[] array = new byte[4];
        int used = UTF8.getUTF8Encoding(c, c2, array);
        for (int b=0; b<used; b++) {
            int v = (array[b]>=0 ? array[b] : 256 + array[b]);
            sb.append('%');
            sb.append(hex.charAt(v/16));
            sb.append(hex.charAt(v%16));
        }
    }
}
