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
 *  $Id: ExpressionDumper.java 853 2004-11-14 22:15:20Z wolfgang_m $
 */
package org.exist.xquery.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.exist.xquery.Expression;
import org.exist.xquery.parser.XQueryAST;

/**
 * @author wolf
 */
public class ExpressionDumper {

    public final static int DEFAULT_INDENT_AMOUNT = 4;
    
    public static String dump(Expression expr) {
        StringWriter writer = new StringWriter();
        ExpressionDumper dumper = new ExpressionDumper(writer);
        expr.dump(dumper);
        return writer.toString();
    }
    
    private PrintWriter out;
    private int indentAmount;
    private String spaces;
    
    private int verbosity;
    
    private int indent = 0;
    
    public ExpressionDumper(Writer writer) {
        this(writer, DEFAULT_INDENT_AMOUNT, 0);
    }
    
    public ExpressionDumper(Writer writer, int indentAmount) {
        this(writer, indentAmount, 0);
    }
    
    public ExpressionDumper(Writer writer, int indentAmount, int verbosity) {
        if(writer instanceof PrintWriter)
            this.out = (PrintWriter)writer;
        else
            this.out = new PrintWriter(writer);
        this.indentAmount = indentAmount;
        this.spaces = "";
        for(int i = 0; i < indentAmount; i++) 
            this.spaces += " ";
        this.verbosity = verbosity;
    }
    
    private void indent() {
        for(int i = 0; i < indent; i++)
            out.print(spaces);
    }
    
    public int verbosity() {
        return verbosity;
    }
    
    public ExpressionDumper display(Object object) {
        return display(object.toString());
    }
    
    public ExpressionDumper display(String s) {
        out.print(s);
        return this;
    }
    
    public ExpressionDumper display(String s, XQueryAST ast) {
        out.print(s);
        out.print(' ');
        if(ast != null && ast.getLine() > -1) {
	        out.print('<');
	        out.print(ast.getLine());
	        out.print("> ");
        }
        return this;
    }
    
    public ExpressionDumper display(char ch) {
        out.print(ch);
        return this;
    }
    
    public ExpressionDumper startIndent() {
        ++indent;
        nl();
        return this;
    }
    
    public ExpressionDumper endIndent() {
        if(indent > 0)
            --indent;
        return this;
    }
    
    public ExpressionDumper nl() {
        out.println();
        indent();
        return this;
    }
}
