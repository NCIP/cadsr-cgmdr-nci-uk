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
 *  $Id: XMLDBExistTask.java 7078 2007-12-30 13:01:04Z dizzzz $
 */
package org.exist.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;

/**
 * an ant task to check for the existance of a collection or resource
 * to be used as a ant condition
 *
 * @author peter.klotz@blue-elephant-systems.com
 */
public class XMLDBExistTask extends AbstractXMLDBTask implements Condition
{
  private String resource = null;

  /**
   * @return returns true if the eval is successful. 
   * @throws BuildException
   * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
   */
  public boolean eval() throws BuildException
  {
    boolean exist = false;

    if (uri == null)
      throw new BuildException("You have to specify an XMLDB collection URI");

    registerDatabase();
    try
    {
      log("Checking collection: " + uri, Project.MSG_INFO);
      Collection base = DatabaseManager.getCollection(uri, user, password);

      if (base != null)
      {
        log("Base collection found", Project.MSG_DEBUG);
        exist = true;
      }

      if (base != null && resource != null)
      {
        log("Checking resource: " + resource, Project.MSG_INFO);
        Resource res = base.getResource(resource);
        if (res == null)
        {
          log("Resource not found", Project.MSG_DEBUG);
          exist = false;
        }
      }
        
    } catch (XMLDBException e)
    {
      // ignore is false already
      log("Resource or collection cannot be retrieved", Project.MSG_DEBUG);
      exist = false;
    }

    return exist;
  }


  /**
   * @param resource
   */
  public void setResource(String resource)
  {
    this.resource = resource;
  }

}
