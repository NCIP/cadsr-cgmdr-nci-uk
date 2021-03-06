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
 *  $Id: XMLDBXQueryTask.java 7078 2007-12-30 13:01:04Z dizzzz $
 */
package org.exist.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.exist.source.FileSource;
import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.source.URLSource;
import org.exist.util.serializer.SAXSerializer;
import org.exist.util.serializer.SerializerPool;
import org.exist.xmldb.XQueryService;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * Ant task to execute an XQuery. <p/> The query is either passed as nested text
 * in the element, or via an attribute "query" or via a URL or via a query file.
 * External variables declared in the XQuery can be set via one or more nested
 * &lt;variable&gt; elements.
 * 
 * @author peter.klotz@blue-elephant-systems.com
 */
public class XMLDBXQueryTask extends AbstractXMLDBTask {
    private String text = null;

    private String queryUri = null;

    private String query = null;

    private File queryFile = null;

    private File destDir = null;

    private String outputproperty;

    private List variables = new ArrayList();
    
    /** Defines a nested element to set an XQuery variable */
    public static class Variable {
    	
    	private String name = null;
    	private String value = null;
    	
    	public Variable() {
    	}
    	
    	public void setName(String name) {
    		this.name = name;
    	}
    	
    	public void setValue(String value) {
    		this.value = value;
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        if (uri == null)
            throw new BuildException(
                    "you have to specify an XMLDB collection URI");
        if (text != null) {
            PropertyHelper helper = PropertyHelper
                    .getPropertyHelper(getProject());
            query = helper.replaceProperties(null, text, null);
        }
        if (queryFile == null && query == null && queryUri == null)
            throw new BuildException(
                    "you have to specify a query either as attribute, text, URI or in a file");

        registerDatabase();
        try {
            log("Get base collection: " + uri, Project.MSG_DEBUG);
            Collection base = DatabaseManager
                    .getCollection(uri, user, password);

            if(base==null){
               throw new BuildException("Collection " + uri + " could not be found.");
            }

            XQueryService service = (XQueryService) base.getService(
                    "XQueryService", "1.0");
            // set pretty-printing on
            service.setProperty(OutputKeys.INDENT, "yes");
            service.setProperty(OutputKeys.ENCODING, "UTF-8");

            for (Iterator i = variables.iterator(); i.hasNext();) {
            	Variable var = (Variable) i.next();
            	System.out.println("Name: " + var.name);
            	System.out.println("Value: " + var.value);
            	service.declareVariable(var.name, var.value);
            }
            
            ResourceSet results = null;
            Source source = null;
            if (queryUri != null) {
                log("XQuery url " + queryUri, Project.MSG_DEBUG);
                source = new URLSource(new URL(queryUri));
            } else if (queryFile != null) {
                log("XQuery file " + queryFile.getAbsolutePath(),
                        Project.MSG_DEBUG);
                source = new FileSource(queryFile, "UTF-8", true);
            } else {
                log("XQuery string: " + query, Project.MSG_DEBUG);
                source = new StringSource(query);
            }
            results = service.execute(source);
            log("Found " + results.getSize() + " results", Project.MSG_INFO);

            if (destDir != null && results != null) {
                log("write results to directory " + destDir.getAbsolutePath(),
                        Project.MSG_INFO);
                ResourceIterator iter = results.getIterator();
                XMLResource res = null;

                log(
                        "Writing results to directory "
                                + destDir.getAbsolutePath(), Project.MSG_DEBUG);
                while (iter.hasMoreResources()) {
                    res = (XMLResource) iter.nextResource();
                    log("Writing resource " + res.getId(), Project.MSG_DEBUG);
                    writeResource(res, destDir);
                }
            } else if (outputproperty != null) {
                ResourceIterator iter = results.getIterator();
                XMLResource res = null;
                String result = null;
                while (iter.hasMoreResources()) {
                    res = (XMLResource) iter.nextResource();
                    result = res.getContent().toString();
                }
                getProject().setNewProperty(outputproperty, result);
            }
        } catch (XMLDBException e) {
            throw new BuildException(
                    "XMLDB exception caught while executing query: "
                            + e.getMessage(), e);
        } catch (IOException e) {
            throw new BuildException(
                    "XMLDB exception caught while writing destination file: "
                            + e.getMessage(), e);
        }
    }

    private void writeResource(XMLResource resource, File dest)
            throws IOException, XMLDBException {
        if (dest != null) {
            Properties outputProperties = new Properties();
            outputProperties.setProperty(OutputKeys.INDENT, "yes");

            SAXSerializer serializer = (SAXSerializer) SerializerPool
                    .getInstance().borrowObject(SAXSerializer.class);

            Writer writer = null;
            if (dest.isDirectory()) {
                if (!dest.exists()) {
                    dest.mkdirs();
                }
                String fname = resource.getId();
                if (!fname.endsWith(".xml")) {
                    fname += ".xml";
                }
                File file = new File(dest, fname);
                writer = new FileWriter(file);
            } else {
                writer = new FileWriter(dest);
            }
            serializer.setOutput(writer, outputProperties);
            resource.getContentAsSAX(serializer);
            writer.close();
            SerializerPool.getInstance().returnObject(serializer);
        } else {
            throw new BuildException("Destionation target does not exist.");
        }
    }

    public void addText(String text) {
        this.text = text;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQueryFile(File queryFile) {
        this.queryFile = queryFile;
    }

    public void setQueryUri(String queryUri) {
        this.queryUri = queryUri;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void addVariable(Variable variable) {
    	variables.add(variable);
    }
    
    public void setOutputproperty(String outputproperty) {
        this.outputproperty = outputproperty;
    }
}
