/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001 Wolfgang M. Meier
 *  meier@ifs.tu-darmstadt.de
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
 *  $Id: RemoteCollection.java 5206 2007-01-17 22:30:30Z wolfgang_m $
 */
package org.exist.xmldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.exist.security.Permission;
import org.exist.security.PermissionFactory;
import org.exist.util.Compressor;
import org.exist.validation.service.RemoteValidationService;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;

/**
 * A remote implementation of the Collection interface. This
 * implementation communicates with the server through the XMLRPC
 * protocol.
 *
 * @author wolf
 * Updated Andy Foster - Updated code to allow child collection cache to
 * resync with the remote collection.
 */
public class RemoteCollection implements CollectionImpl {

	// max size of a resource to be send to the server
	// if the resource exceeds this limit, the data is split into
	// junks and uploaded to the server via the update() call
	private static final int MAX_CHUNK_LENGTH = 512 * 1024;
	private static final int MAX_UPLOAD_CHUNK = 10 * 1024 * 1024;

	protected Map childCollections = null;
	protected XmldbURI path;
	protected Permission permissions = null;
	protected RemoteCollection parent = null;
	protected XmlRpcClient rpcClient = null;
	protected Properties properties = null;

	public RemoteCollection(XmlRpcClient client, XmldbURI path)
		throws XMLDBException {
		this(client, null, path);
	}

	public RemoteCollection(
		XmlRpcClient client,
		RemoteCollection parent,
		XmldbURI path)
		throws XMLDBException {
		this.parent = parent;
		this.path = path.toCollectionPathURI();
		this.rpcClient = client;
	}

	protected void addChildCollection(Collection child) throws XMLDBException {
		if (childCollections == null)
			readCollection();
		try {
			childCollections.put(XmldbURI.xmldbUriFor(child.getName()), child);
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
	}

	public void close() throws XMLDBException {
		try {
			rpcClient.execute("sync", new Vector());
		} catch (XmlRpcException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, "failed to close collection", e);
		} catch (IOException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, "failed to close collection", e);
		}
	}

	public String createId() throws XMLDBException {
	    Vector params = new Vector(1);
	    params.addElement(getPath());
	    try {
			return (String)rpcClient.execute("createResourceId", params);
		} catch (XmlRpcException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, "failed to close collection", e);
		} catch (IOException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, "failed to close collection", e);
		}
	}

	public Resource createResource(String id, String type) throws XMLDBException {
		XmldbURI newId;
		try {
			newId = (id == null) ? XmldbURI.xmldbUriFor(createId()) : XmldbURI.xmldbUriFor(id);
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
		Resource r;
		if(type.equals("XMLResource"))
			r = new RemoteXMLResource(this, -1, -1, newId, null);
		else if(type.equals("BinaryResource"))
			r = new RemoteBinaryResource(this, newId);
		else
			throw new XMLDBException(ErrorCodes.UNKNOWN_RESOURCE_TYPE, "unknown resource type: " + type);
		return r;
	}

	public Collection getChildCollection(String name) throws XMLDBException {
		try {
			return getChildCollection(XmldbURI.xmldbUriFor(name));
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
	}

	public Collection getChildCollection(XmldbURI name) throws XMLDBException {
		// AF get the child collection refreshing cache from server if not found
		return getChildCollection(name,true);
	}

	// AF - NEW METHOD
	protected Collection getChildCollection(XmldbURI name, boolean refreshCacheIfNotFound) throws XMLDBException {
		if (childCollections == null) {
			readCollection();
			refreshCacheIfNotFound = false;
		}

		// stores reference to the collection found
		Collection foundCollection = null;
		if (name.numSegments()>1)
			foundCollection = (Collection) childCollections.get(name);
		else
			foundCollection = (Collection) childCollections.get(getPathURI().append(name));

		// if we did not find collection in cache set cache back to null to force full refresh
		if (foundCollection == null && refreshCacheIfNotFound) {
			childCollections = null;
			return getChildCollection(name,false);
		}
		// return the found collection
		return foundCollection;
	}


	public int getChildCollectionCount() throws XMLDBException {
		//  AF Always refresh cache for latest set - if (childCollections == null)
		readCollection();
		return childCollections.size();
	}

	protected XmlRpcClient getClient() {
		return rpcClient;
	}

	public String getName() throws XMLDBException {
		return path.toString();
	}

	public Collection getParentCollection() throws XMLDBException {
	    if(parent == null && !path.equals(XmldbURI.ROOT_COLLECTION_URI)) {
	        XmldbURI parentUri = path.removeLastSegment();
	        return new RemoteCollection(rpcClient, null, parentUri);
	    }
		return parent;
	}

	public String getPath() throws XMLDBException {
		return getPathURI().toString();
	}

	public XmldbURI getPathURI() {
		if (parent == null) {
			/*
		    if(name != null)
		        return name;
		    else
		    */
		        return XmldbURI.ROOT_COLLECTION_URI;
		}
		return path;
	}

	public String getProperty(String property) throws XMLDBException {
		if(properties == null) return null;
		return (String)properties.get(property);
	}

	public Properties getProperties() {
		if(properties == null)
			properties = new Properties();
		return properties;
	}

	public int getResourceCount() throws XMLDBException {
	    Vector params = new Vector(1);
	    params.addElement(getPath());
	    try {
			return ((Integer)rpcClient.execute("getResourceCount", params)).intValue();
		} catch (XmlRpcException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, "failed to close collection", e);
		} catch (IOException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, "failed to close collection", e);
		}
	}

	public Service getService(String name, String version) throws XMLDBException {
		if (name.equals("XPathQueryService"))
			return new RemoteXPathQueryService(this);
        if (name.equals("XQueryService"))
            return new RemoteXPathQueryService(this);
		if (name.equals("CollectionManagementService") || name.equals("CollectionManager"))
			return new RemoteCollectionManagementService(this, rpcClient);
		if (name.equals("UserManagementService"))
			return new RemoteUserManagementService(this);
		if (name.equals("DatabaseInstanceManager"))
			return new RemoteDatabaseInstanceManager(rpcClient);
		if (name.equals("IndexQueryService"))
			return new RemoteIndexQueryService(rpcClient, this);
		if (name.equals("XUpdateQueryService"))
			return new RemoteXUpdateQueryService(this);
		if (name.equals("ValidationService"))
			return new RemoteValidationService(this, rpcClient);
		throw new XMLDBException(ErrorCodes.NO_SUCH_SERVICE);
	}

	public Service[] getServices() throws XMLDBException {
		Service[] services = new Service[7];
		services[0] = new RemoteXPathQueryService(this);
		services[1] = new RemoteCollectionManagementService(this, rpcClient);
		services[2] = new RemoteUserManagementService(this);
		services[3] = new RemoteDatabaseInstanceManager(rpcClient);
		services[4] = new RemoteIndexQueryService(rpcClient, this);
		services[5] = new RemoteXUpdateQueryService(this);
                services[6] = new RemoteValidationService(this, rpcClient);
		return services;
	}

	protected boolean hasChildCollection(String name) throws XMLDBException {
		//  AF Always refresh cache for latest set - if (childCollections == null)
		readCollection();
		try {
			return childCollections.containsKey(XmldbURI.xmldbUriFor(name));
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
	}

	public boolean isOpen() throws XMLDBException {
		return true;
	}

	/**
	 *  Returns a list of collection names naming all child collections of the
	 *  current collection. Only the name of the collection is returned - not
	 *  the entire path to the collection.
	 *
	 *@return                     Description of the Return Value
	 *@exception  XMLDBException  Description of the Exception
	 */
	public String[] listChildCollections() throws XMLDBException {
		// Always refresh cache for latest set - if (childCollections == null)
		readCollection();
		String coll[] = new String[childCollections.size()];
		int j = 0;
		XmldbURI uri;
		for (Iterator i = childCollections.keySet().iterator(); i.hasNext(); j++) {
			uri = (XmldbURI) i.next();
			coll[j] = uri.lastSegment().toString();
		}
		return coll;
	}

	public String[] getChildCollections() throws XMLDBException {
		return listChildCollections();
	}

	public String[] listResources() throws XMLDBException {
	    Vector params = new Vector();
		params.addElement(getPath());
		try {
			Vector vec = (Vector)rpcClient.execute("getDocumentListing", params);
			String[] resources = new String[vec.size()];
			return (String[])vec.toArray(resources);
		} catch (XmlRpcException xre) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, xre.getMessage(), xre);
		} catch (IOException ioe) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, "An IO error occurred: " + ioe.getMessage(), ioe);
		}
	}

	/* (non-Javadoc)
	 * @see org.exist.xmldb.CollectionImpl#getResources()
	 */
	public String[] getResources() throws XMLDBException {
		return listResources();
	}

	public Resource getResource(String name) throws XMLDBException {
	    Vector params = new Vector();
		XmldbURI docUri;
		try {
			docUri = XmldbURI.xmldbUriFor(name);
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
		params.addElement(getPathURI().append(docUri).toString());
		Hashtable hash;
		try {
			hash = (Hashtable) rpcClient.execute("describeResource", params);
		} catch (XmlRpcException xre) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, xre.getMessage(), xre);
		} catch (IOException ioe) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, "An IO error occurred: " + ioe.getMessage(), ioe);
		}
		String docName = (String) hash.get("name");
		if(docName == null)
			return null;	// resource does not exist!
		
		try {
			docUri = XmldbURI.xmldbUriFor(docName).lastSegment();
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
		Permission perm = PermissionFactory.getPermission(
				(String) hash.get("owner"),
				(String) hash.get("group"),
				((Integer) hash.get("permissions")).intValue());
		String type = (String)hash.get("type");
		int contentLen = 0;
		if(hash.containsKey("content-length"))
			contentLen = ((Integer)hash.get("content-length")).intValue();
		if(type == null || type.equals("XMLResource")) {
			RemoteXMLResource r = new RemoteXMLResource(this, -1, -1, docUri, null);
			r.setPermissions(perm);
			r.setContentLength(contentLen);
            r.setDateCreated((Date) hash.get("created"));
            r.setDateModified((Date) hash.get("modified"));
            if (hash.containsKey("mime-type"))
                r.setMimeType((String) hash.get("mime-type"));
			return r;
		} else {
			RemoteBinaryResource r = new RemoteBinaryResource(this, docUri);
			r.setContentLength(contentLen);
			r.setPermissions(perm);
            r.setDateCreated((Date) hash.get("created"));
            r.setDateModified((Date) hash.get("modified"));
            if (hash.containsKey("mime-type"))
                r.setMimeType((String) hash.get("mime-type"));
			return r;
		}
	}

	private void readCollection() throws XMLDBException {
		childCollections = new HashMap();
		Vector params = new Vector();
		params.addElement(getPath());

		Hashtable collection;
		try {
			collection = (Hashtable) rpcClient.execute("describeCollection", params);
		} catch (XmlRpcException xre) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, xre.getMessage(), xre);
		} catch (IOException ioe) {
			throw new XMLDBException(ErrorCodes.INVALID_COLLECTION, "An IO error occurred: " + ioe.getMessage(), ioe);
		}
		Vector collections = (Vector) collection.get("collections");
		permissions = PermissionFactory.getPermission(
				(String) collection.get("owner"),
				(String) collection.get("group"),
				((Integer) collection.get("permissions")).intValue());
		String childName;
		for (int i = 0; i < collections.size(); i++) {
			childName = (String) collections.elementAt(i);
			try {
                //TODO: Should this use the checked version instead?
				RemoteCollection child =
					new RemoteCollection(rpcClient, this, getPathURI().append(XmldbURI.create(childName)));
				addChildCollection(child);
			} catch (XMLDBException e) {
			}
		}
	}

	public void registerService(Service serv) throws XMLDBException {
		throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED);
	}

	public void removeChildCollection(String name) throws XMLDBException {
		try {
			removeChildCollection(XmldbURI.xmldbUriFor(name));
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
	}

	public void removeChildCollection(XmldbURI name) throws XMLDBException {
		if (childCollections == null)
			readCollection();
		childCollections.remove(name);
	}

	public void removeResource(Resource res) throws XMLDBException {
		Vector params = new Vector();
		try {
			params.addElement(getPathURI().append(XmldbURI.xmldbUriFor(res.getId())).toString());
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}

		try {
			rpcClient.execute("remove", params);
		} catch (XmlRpcException xre) {
			throw new XMLDBException(ErrorCodes.INVALID_RESOURCE, xre.getMessage(), xre);
		} catch (IOException ioe) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ioe.getMessage(), ioe);
		}
	}

	public Date getCreationTime() throws XMLDBException {
		Vector params = new Vector(1);
		params.addElement(getPath());
		try {
			return (Date) rpcClient.execute("getCreationDate", params);
		} catch (XmlRpcException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(), e);
		} catch (IOException e) {
			throw new XMLDBException(ErrorCodes.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}

	public void setProperty(String property, String value) throws XMLDBException {
		if(properties == null)
			properties = new Properties();
		properties.setProperty(property, value);
	}

	public void storeResource(Resource res) throws XMLDBException {
		storeResource(res, null, null);
	}

	public void storeResource(Resource res, Date a, Date b) throws XMLDBException {
		Object content = res.getContent();
		if (content instanceof File) {
			File file = (File) content;
			if (!file.canRead())
				throw new XMLDBException(
					ErrorCodes.INVALID_RESOURCE,
					"failed to read resource from file " + file.getAbsolutePath());
			if (file.length() < MAX_CHUNK_LENGTH) {
				((RemoteXMLResource)res).dateCreated =a;
				((RemoteXMLResource)res).dateModified =b;
				store((RemoteXMLResource)res);
			} else {
				((RemoteXMLResource)res).dateCreated =a;
				((RemoteXMLResource)res).dateModified =b;
				uploadAndStore(res);
			}
		} else if(res.getResourceType().equals("BinaryResource"))
		{
			((RemoteBinaryResource)res).dateCreated =a;
	        ((RemoteBinaryResource)res).dateModified =b;
			store((RemoteBinaryResource)res);
		}
		else {
			((RemoteXMLResource)res).dateCreated =a;
		    ((RemoteXMLResource)res).dateModified =b;
			store((RemoteXMLResource)res);
	}
	}


	private void store(RemoteXMLResource res) throws XMLDBException {
		byte[] data = res.getData();
		Vector params = new Vector();
		params.addElement(data);
		try {
			params.addElement(getPathURI().append(XmldbURI.xmldbUriFor(res.getId())).toString());
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
		params.addElement(new Integer(1));

		if (res.dateCreated != null) {
		params.addElement(res.dateCreated );
		params.addElement(res.dateModified );
		}

		try {
			rpcClient.execute("parse", params);
		} catch (XmlRpcException xre) {
			throw new XMLDBException(
				ErrorCodes.INVALID_RESOURCE,
				xre == null ? "unknown error" : xre.getMessage(),
				xre);
		} catch (IOException ioe) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ioe.getMessage(), ioe);
		}
	}

	private void store(RemoteBinaryResource res) throws XMLDBException {
		byte[] data = (byte[])res.getContent();
		Vector params = new Vector();
		params.addElement(data);
		try {
			params.addElement(getPathURI().append(XmldbURI.xmldbUriFor(res.getId())).toString());
		} catch(URISyntaxException e) {
			throw new XMLDBException(ErrorCodes.INVALID_URI,e);
		}
        params.addElement(res.getMimeType());
		params.addElement(Boolean.TRUE);


		if ((Date)res.dateCreated != null) {
			params.addElement((Date)res.dateCreated );
			params.addElement((Date)res.dateModified );
			}

		try {
			rpcClient.execute("storeBinary", params);
		} catch (XmlRpcException xre) {
		    /* the error code previously was INVALID_RESOURCE, but this was also thrown
		     * in case of insufficient persmissions. As you cannot tell here any more what the
		     * error really was, use UNKNOWN_ERROR. The reason is in XmlRpcResponseProcessor#processException
		     * which will only pass on the error message.
		     */
			throw new XMLDBException(
					ErrorCodes.UNKNOWN_ERROR,
					xre == null ? "unknown error" : xre.getMessage(),
					xre);
		} catch (IOException ioe) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, ioe.getMessage(), ioe);
		}
	}

	private void uploadAndStore(Resource res) throws XMLDBException {
		File file = (File) res.getContent();
		byte[] chunk = new byte[MAX_UPLOAD_CHUNK];
		try {
			FileInputStream is = new FileInputStream(file);
			int len;
			String fileName = null;
			Vector params;
			byte[] compressed;
			while ((len = is.read(chunk)) > -1) {
			    compressed = Compressor.compress(chunk, len);
				params = new Vector();
				if (fileName != null)
					params.addElement(fileName);
				params.addElement(compressed);
				params.addElement(new Integer(len));
				fileName = (String) rpcClient.execute("uploadCompressed", params);
			}
			params = new Vector();
			params.addElement(fileName);
			try {
				params.addElement(getPathURI().append(XmldbURI.xmldbUriFor(res.getId())).toString());
			} catch(URISyntaxException e) {
				throw new XMLDBException(ErrorCodes.INVALID_URI,e);
			}
			params.addElement(Boolean.TRUE);
            params.addElement(((RemoteXMLResource)res).getMimeType());
			if ( ((RemoteXMLResource)res).dateCreated  != null ) {
				params.addElement( ((RemoteXMLResource)res).dateCreated );
				params.addElement( ((RemoteXMLResource)res).dateModified );
				}

			rpcClient.execute("parseLocal", params);
		} catch (FileNotFoundException e) {
			throw new XMLDBException(
				ErrorCodes.INVALID_RESOURCE,
				"could not read resource from file " + file.getAbsolutePath(),
				e);
		} catch (IOException e) {
			throw new XMLDBException(
				ErrorCodes.INVALID_RESOURCE,
				"failed to read resource from file " + file.getAbsolutePath(),
				e);
		} catch (XmlRpcException e) {
			throw new XMLDBException(ErrorCodes.VENDOR_ERROR, "networking error", e);
		}
	}

	public Permission getPermissions() {
		return permissions;
	}

    /* (non-Javadoc)
     * @see org.exist.xmldb.CollectionImpl#isRemoteCollection()
     */
    public boolean isRemoteCollection() throws XMLDBException {
        return true;
    }

    //You probably will have to call this method from this cast :
    //((org.exist.xmldb.CollectionImpl)collection).getURI()
    public XmldbURI getURI() {
    	StringBuffer accessor = new StringBuffer(XmldbURI.XMLDB_URI_PREFIX);
    	//TODO : get the name from client
    	accessor.append("exist");
    	accessor.append("://");
    	accessor.append(rpcClient.getURL().getHost());
    	if (rpcClient.getURL().getPort() != -1)
    		accessor.append(":").append(rpcClient.getURL().getPort());
    	accessor.append(rpcClient.getURL().getPath());
    	try {
    		//TODO : cache it when constructed
    		return XmldbURI.create(accessor.toString(), getPath());
    	} catch (XMLDBException e) {
    		//TODO : should never happen
    		return null;
    	}
    }
}

