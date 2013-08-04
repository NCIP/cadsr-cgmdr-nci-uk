/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Team
 *
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
 *  $Id: ValueOccurrences.java 4403 2006-09-27 12:27:37Z wolfgang_m $
 */
package org.exist.util;

import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.xquery.Constants;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.AtomicValue;

/**
 * @author wolf
 *
 */
public class ValueOccurrences {

	private AtomicValue value;
	private int occurrences = 0;
	private DocumentSet docs = new DocumentSet();
	
	/**
	 * 
	 */
	public ValueOccurrences(AtomicValue value) {
		this.value = value;
	}

	public AtomicValue getValue() {
		return value;
	}
	
	/**
     * Returns the overall frequency of this term
     * in the document set.
     */
	public int getOccurrences() {
		return occurrences;
	}

	public void addOccurrences(int count) {
		occurrences += count;
	}

    public void addDocument(DocumentImpl doc) {
        if(!docs.contains(doc.getDocId()))
            docs.add(doc);
    }
    
    public void add(ValueOccurrences other) {
    	addOccurrences(other.occurrences);
    	docs.addAll(other.docs);
    }
    
    /**
     * Returns the number of documents from the set in
     * which the term has been found.
     */
    public int getDocuments() {
        return docs.getLength();
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		try {
			return value.compareTo(null, ((ValueOccurrences) o).value);
		} catch (XPathException e) {
			e.printStackTrace();
            //TODO : what does this mean ? -pb
			return Constants.INFERIOR;
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		try {
			buf.append("Value: '" + value.getStringValue() +"'");
		} catch (XPathException e) {
			buf.append("Value: '" + e.getMessage() +"'");
		}
		buf.append(" occurences: '" + occurrences +"'");
		buf.append(" documents: '" + docs.getLength() +"'");
		return buf.toString();
	}
}
