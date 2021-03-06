/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2003-2007 The eXist Project
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
 *  $Id: LocalXMLResource.java 6427 2007-08-28 08:10:35Z brihaye $
 */
package org.exist.xmldb;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.exist.EXistException;
import org.exist.dom.DocumentImpl;
import org.exist.dom.NodeProxy;
import org.exist.dom.XMLUtil;
import org.exist.memtree.AttributeImpl;
import org.exist.memtree.NodeImpl;
import org.exist.numbering.NodeId;
import org.exist.security.Permission;
import org.exist.security.User;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.LockException;
import org.exist.util.MimeType;
import org.exist.util.serializer.DOMSerializer;
import org.exist.util.serializer.DOMStreamer;
import org.exist.util.serializer.SAXSerializer;
import org.exist.util.serializer.SerializerPool;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.AtomicValue;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Local implementation of XMLResource.
 */
public class LocalXMLResource extends AbstractEXistResource implements XMLResource {

	//protected DocumentImpl document = null;
	protected NodeProxy proxy = null;
	
	protected Properties outputProperties = null;
	protected LexicalHandler lexicalHandler = null;
	
	// those are the different types of content this resource
	// may have to deal with
	protected String content = null;
	protected File file = null;
	protected Node root = null;
	protected AtomicValue value = null;
	
	protected Date datecreated= null;
	protected Date datemodified= null;

	public LocalXMLResource(User user, BrokerPool pool, LocalCollection parent,
			XmldbURI did) throws XMLDBException {
		super(user, pool, parent, did, MimeType.XML_TYPE.getName());
	}

	public LocalXMLResource(User user, BrokerPool pool, LocalCollection parent,
			NodeProxy p) throws XMLDBException {
		this(user, pool, parent, p.getDocument().getFileURI());
		this.proxy = p;
	}

	public Object getContent() throws XMLDBException {
		if (content != null) {            
			return content;
        }

		// Case 1: content is an external DOM node
		else if (root != null && !(root instanceof NodeValue)) {
            StringWriter writer = new StringWriter();
			DOMSerializer serializer = new DOMSerializer(writer, getProperties());
			try {
				serializer.serialize(root);
				content = writer.toString();
			} catch (TransformerException e) {
				throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, e
						.getMessage(), e);
			}
			return content;

			// Case 2: content is an atomic value
		} else if (value != null) {
			try {
                if (Type.subTypeOf(value.getType(),Type.STRING)) {
                    return ((StringValue)value).getStringValue(true);
                }
                else {
				return value.getStringValue();
                }



			} catch (XPathException e) {
				throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, e
						.getMessage(), e);
			}

			// Case 3: content is a file
		} else if (file != null) {
			try {
				content = XMLUtil.readFile(file);
				return content;
			} catch (IOException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
						"error while reading resource contents", e);
			}

			// Case 4: content is a document or internal node
		} else {
		    DocumentImpl document = null;
			DBBroker broker = null;
			try {
				broker = pool.get(user);
				Serializer serializer = broker.getSerializer();
				serializer.setUser(user);
				serializer.setProperties(getProperties());
				if (root != null) {
					content = serializer.serialize((NodeValue) root);
                    
                } else if (proxy != null) {
                    content = serializer.serialize(proxy);
                    
                } else {
				    document = openDocument(broker, Lock.READ_LOCK);
					if (!document.getPermissions().validate(user,
							Permission.READ))
						throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
								"permission denied to read resource");
					content = serializer.serialize(document);
                }
				return content;
			} catch (SAXException saxe) {
				saxe.printStackTrace();
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, saxe
						.getMessage(), saxe);
			} catch (EXistException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} catch (Exception e) {
				e.printStackTrace();
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} finally {
			    closeDocument(document, Lock.READ_LOCK);
				pool.release(broker);
			}
		}
	}

	public Node getContentAsDOM() throws XMLDBException {
		if (root != null) {
            if(root instanceof NodeImpl) {
                ((NodeImpl)root).expand();
            }
			return root;
        } else if (value != null) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"cannot return an atomic value as DOM node");
		} else {
		    DocumentImpl document = null;
			DBBroker broker = null;
			try {
				broker = pool.get(user);
				document = getDocument(broker, Lock.READ_LOCK);
				if (!document.getPermissions().validate(user, Permission.READ))
					throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
							"permission denied to read resource");
				if (proxy != null)
					return document.getNode(proxy);
                // <frederic.glorieux@ajlsm.com> return a full to get root PI and comments 
                return document;
			} catch (EXistException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} finally {
			    parent.getCollection().releaseDocument(document, Lock.READ_LOCK);
				pool.release(broker);
			}
		}
	}

	public void getContentAsSAX(ContentHandler handler) throws XMLDBException {
		DBBroker broker = null;
		// case 1: content is an external DOM node
		if (root != null && !(root instanceof NodeValue)) {
			try {
				String option = parent.properties.getProperty(
						Serializer.GENERATE_DOC_EVENTS, "false");
                DOMStreamer streamer = (DOMStreamer) SerializerPool.getInstance().borrowObject(DOMStreamer.class);
				streamer.setContentHandler(handler);
				streamer.setLexicalHandler(lexicalHandler);
				streamer.serialize(root, option.equalsIgnoreCase("true"));
				SerializerPool.getInstance().returnObject(streamer);
			} catch (Exception e) {
				throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, e
						.getMessage(), e);
			}
			
		// case 2: content is an atomic value
		} else if (value != null) {
			try {
				broker = pool.get(user);
				value.toSAX(broker, handler, getProperties());
			} catch (EXistException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} catch (SAXException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} finally {
				pool.release(broker);
			}
			
		// case 3: content is an internal node or a document
		} else {
			try {
				broker = pool.get(user);
				Serializer serializer = broker.getSerializer();
				serializer.setUser(user);
				serializer.setProperties(getProperties());
				serializer.setSAXHandlers(handler, lexicalHandler);
				if (root != null) {
					serializer.toSAX((NodeValue) root);
                    
                } else if (proxy != null) {
                    serializer.toSAX(proxy);
                    
                } else {
					DocumentImpl document = null;
					try {
						document = openDocument(broker, Lock.READ_LOCK);
						if (!document.getPermissions().validate(user,
								Permission.READ))
							throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
							"permission denied to read resource");
						serializer.toSAX(document);
					} finally {
					    closeDocument(document, Lock.READ_LOCK);
					}
				}
			} catch (EXistException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} catch (SAXException e) {
				throw new XMLDBException(ErrorCodes.VENDOR_ERROR, e
						.getMessage(), e);
			} finally {
				pool.release(broker);
			}
		}
	}

	//TODO: use xmldbURI?
	public String getDocumentId() throws XMLDBException {
		return docId.toString();
	}

	//TODO: use xmldbURI?
	public String getId() throws XMLDBException {
		return docId.toString();
	}

	public Collection getParentCollection() throws XMLDBException {
		if (parent == null)
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR,
					"collection parent is null");
		return parent;
	}

	public String getResourceType() throws XMLDBException {
		return "XMLResource";
	}

	public Date getCreationTime() throws XMLDBException {
		DBBroker broker = null;
		try {
			broker = pool.get(user);
			DocumentImpl document = getDocument(broker, Lock.NO_LOCK);
			if (!document.getPermissions().validate(user, Permission.READ))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
						"permission denied to read resource");
			return new Date(document.getMetadata().getCreated());
		} catch (EXistException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}

	public Date getLastModificationTime() throws XMLDBException {
		DBBroker broker = null;
		try {
			broker = pool.get(user);
			DocumentImpl document = getDocument(broker, Lock.NO_LOCK);
			if (!document.getPermissions().validate(user, Permission.READ))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
						"permission denied to read resource");
			return new Date(document.getMetadata().getLastModified());
		} catch (EXistException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xmldb.EXistResource#getContentLength()
	 */
	public int getContentLength() throws XMLDBException {
		DBBroker broker = null;
		try {
			broker = pool.get(user);
			DocumentImpl document = getDocument(broker, Lock.NO_LOCK);
			if (!document.getPermissions().validate(user, Permission.READ))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
						"permission denied to read resource");
			return document.getContentLength();
		} catch (EXistException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
	}

	/**
	 * Sets the content for this resource. If value is of type File, it is
	 * directly passed to the parser when Collection.storeResource is called.
	 * Otherwise the method tries to convert the value to String.
	 * 
	 * Passing a File object should be preferred if the document is large. The
	 * file's content will not be loaded into memory but directly passed to a
	 * SAX parser.
	 * 
	 * @param obj
	 *                   the content value to set for the resource.
	 * @exception XMLDBException
	 *                         with expected error codes. <br /><code>ErrorCodes.VENDOR_ERROR</code>
	 *                         for any vendor specific errors that occur. <br />
	 */
	public void setContent(Object obj) throws XMLDBException {
		content = null;
		if (obj instanceof File)
			file = (File) obj;
		else if (obj instanceof AtomicValue)
			value = (AtomicValue) obj;
		else {
			content = obj.toString();
		}
	}

	public void setContentAsDOM(Node root) throws XMLDBException {
		if (root instanceof AttributeImpl)
			throw new XMLDBException(ErrorCodes.WRONG_CONTENT_TYPE,
					"SENR0001: can not serialize a standalone attribute");
		this.root = root;
	}

	public ContentHandler setContentAsSAX() throws XMLDBException {
		return new InternalXMLSerializer();
	}

	private class InternalXMLSerializer extends SAXSerializer {

		public InternalXMLSerializer() {
			super(new StringWriter(), null);
		}

		/**
		 * @see org.xml.sax.DocumentHandler#endDocument()
		 */
		public void endDocument() throws SAXException {
			super.endDocument();
			content = getWriter().toString();
		}
	}

	public boolean getSAXFeature(String arg0) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		return false;
	}

	public void setSAXFeature(String arg0, boolean arg1)
			throws SAXNotRecognizedException, SAXNotSupportedException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.xmldb.EXistResource#getPermissions()
	 */
	public Permission getPermissions() throws XMLDBException {
	    DBBroker broker = null;
	    try {
	        broker = pool.get(user);
		    DocumentImpl document = getDocument(broker, Lock.NO_LOCK);
			return document != null ? document.getPermissions() : null;
	    } catch (EXistException e) {
            throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, e.getMessage(), e);
        } finally {
	        pool.release(broker);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xmldb.EXistResource#setLexicalHandler(org.xml.sax.ext.LexicalHandler)
	 */
	public void setLexicalHandler(LexicalHandler handler) {
		lexicalHandler = handler;
	}
	
	protected void setProperties(Properties properties) {
		this.outputProperties = properties;
	}
	
	private Properties getProperties() {
		return outputProperties == null ? parent.properties : outputProperties;
	}

	protected DocumentImpl getDocument(DBBroker broker, int lock) throws XMLDBException {
	    DocumentImpl document = null;
	    if(lock != Lock.NO_LOCK) {
            try {
                document = parent.getCollection().getDocumentWithLock(broker, docId, lock);
            } catch (LockException e) {
                throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
                        "Failed to acquire lock on document " + docId);
            }
	    } else {
	        document = parent.getCollection().getDocument(broker, docId);
	    }
	    if (document == null) {
	        throw new XMLDBException(ErrorCodes.INVALID_RESOURCE);
	    }
	    return document;
	}
	
	public NodeProxy getNode() throws XMLDBException {
	    if(proxy != null)
	        return proxy;
	    DBBroker broker = null;
	    try {
	        broker = pool.get(user);
	        DocumentImpl document = getDocument(broker, Lock.NO_LOCK);
	        // this XMLResource represents a document
			return new NodeProxy(document, NodeId.DOCUMENT_NODE);
	    } catch (EXistException e) {
            throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, e.getMessage(), e);
        } finally {
	        pool.release(broker);
	    }
	}

	public  DocumentType getDocType() throws XMLDBException {
		DBBroker broker = null;
		try {
			broker = pool.get(user);
			DocumentImpl document = getDocument(broker, Lock.NO_LOCK);
			if (!document.getPermissions().validate(user, Permission.READ))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED,
						"permission denied to read resource");

			return  document.getDoctype();			
		} catch (EXistException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(),
					e);
		} finally {
			pool.release(broker);
		}
}
	
	public void setDocType(DocumentType doctype) throws XMLDBException {
		DBBroker broker = null;
		DocumentImpl document = null;
		 TransactionManager transact = pool.getTransactionManager();
	        Txn transaction = transact.beginTransaction();
		try {
			broker = pool.get(user);
			document = openDocument(broker, Lock.WRITE_LOCK);
           	
			if (document == null) {
                throw new EXistException("Resource "
                        + document.getFileURI() + " not found");
            }
			
			if (!document.getPermissions().validate(user, Permission.UPDATE))
				throw new XMLDBException(ErrorCodes.PERMISSION_DENIED, 
						"User is not allowed to lock resource " + document.getFileURI());
			
			document.setDocumentType(doctype);
         	broker.storeXMLResource(transaction, document);
            transact.commit(transaction);


		} catch (EXistException e) {
            transact.abort(transaction);
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(),
					e);
		} finally {
			closeDocument(document, Lock.WRITE_LOCK);
			pool.release(broker);
		}
}
}
