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
 *  $Id: DocumentBuilderReceiver.java 7573 2008-03-29 19:51:50Z wolfgang_m $
 */
package org.exist.memtree;

import org.exist.dom.NodeProxy;
import org.exist.dom.QName;
import org.exist.dom.StoredNode;
import org.exist.util.serializer.AttrList;
import org.exist.util.serializer.Receiver;
import org.exist.xquery.XQueryContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Builds an in-memory DOM tree from SAX {@link org.exist.util.serializer.Receiver}
 * events.
 * 
 * @author Wolfgang <wolfgang@exist-db.org>
 */
public class DocumentBuilderReceiver implements ContentHandler, LexicalHandler, Receiver {

	private MemTreeBuilder builder = null;
	
	public DocumentBuilderReceiver() {
		super();
	}

	public DocumentBuilderReceiver(MemTreeBuilder builder) {
		super();
		this.builder = builder;
	}
	
	public Document getDocument() {
		return builder.getDocument();
	}
	
	public XQueryContext getContext() {
	    return builder.getContext();
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator arg0) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		if(builder == null) {
			builder = new MemTreeBuilder();
			builder.startDocument();
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		builder.endDocument();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String arg0) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localName,	String qName, Attributes attrs)
            throws SAXException {
		builder.startElement(namespaceURI, localName, qName, attrs);
	}

	public void startElement(QName qname, AttrList attribs) {
		builder.startElement(qname, null);
		if(attribs != null) {
			for (int i = 0; i < attribs.getLength(); i++) {
				builder.addAttribute(attribs.getQName(i), attribs.getValue(i));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String arg0, String arg1, String arg2) throws SAXException {
		builder.endElement();
	}

	public void endElement(QName qname) throws SAXException {
		builder.endElement();
	}

	public void addReferenceNode(NodeProxy proxy) throws SAXException {
	    builder.addReferenceNode(proxy);
	}
	
	public void addNamespaceNode(QName qname) throws SAXException {
		builder.namespaceNode(qname);
	}
	
	public void characters(CharSequence seq) throws SAXException {
		builder.characters(seq);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int len) throws SAXException {
		builder.characters(ch, start, len);
	}

	public void attribute(QName qname, String value) throws SAXException {
        try {
            builder.addAttribute(qname, value);
        } catch (DOMException e) {
            throw new SAXException(e.getMessage());
        }
    }
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String target, String data) throws SAXException {
	    builder.processingInstruction(target, data);
	}

    /* (non-Javadoc)
     * @see org.exist.util.serializer.Receiver#cdataSection(char[], int, int)
     */
    public void cdataSection(char[] ch, int start, int len) throws SAXException {
        builder.cdataSection(new String(ch, start, len));
    }
    
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String arg0) throws SAXException {
	}

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
        // TODO ignored
        
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
        // TODO Ignored
    }

	public void documentType(String name, String publicId, String systemId)throws SAXException {
		builder.documentType(name, publicId, systemId);
	}
	
    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        builder.comment(ch, start, length);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public void endEntity(String name) throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public void startEntity(String name) throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void highlightText(CharSequence seq) {
        // not supported with this receiver
    }

    public void setCurrentNode(StoredNode node) {
        // ignored
    }
}
