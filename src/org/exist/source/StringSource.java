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
 *  $Id: StringSource.java 1013 2005-02-02 19:14:14Z wolfgang_m $
 */
package org.exist.source;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.exist.storage.DBBroker;


/**
 * A simple source object wrapping around a single string value.
 * 
 * @author wolf
 */
public class StringSource extends AbstractSource {

    private String data;
    
    /**
     * 
     */
    public StringSource(String content) {
        this.data = content;
    }

    /* (non-Javadoc)
     * @see org.exist.source.Source#getKey()
     */
    public Object getKey() {
        return data;
    }

    /* (non-Javadoc)
     * @see org.exist.source.Source#isValid()
     */
    public int isValid(DBBroker broker) {
        return Source.VALID;
    }

    /* (non-Javadoc)
     * @see org.exist.source.Source#isValid(org.exist.source.Source)
     */
    public int isValid(Source other) {
        return Source.VALID;
    }

    /* (non-Javadoc)
     * @see org.exist.source.Source#getReader()
     */
    public Reader getReader() throws IOException {
        return new StringReader(data);
    }

    /* (non-Javadoc)
     * @see org.exist.source.Source#getContent()
     */
    public String getContent() throws IOException {
        return data;
    }

}
