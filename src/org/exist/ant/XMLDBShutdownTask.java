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
 *  $Id: XMLDBShutdownTask.java 7078 2007-12-30 13:01:04Z dizzzz $
 */
package org.exist.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.exist.xmldb.DatabaseInstanceManager;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

/**
 * an ant task to shutdown a XMLDB database
 *
 * @author Wolfgang Meier (wolfgang@exist-db.org)
 *         <p/>
 *         slightly modified by:
 * @author peter.klotz@blue-elephant-systems.com
 */
public class XMLDBShutdownTask extends AbstractXMLDBTask
{

  /* (non-Javadoc)
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    if (uri == null)
      throw new BuildException("you have to specify an XMLDB collection URI");

    registerDatabase();
    try
    {
      log("Get base collection: " + uri, Project.MSG_DEBUG);
      Collection root = DatabaseManager.getCollection(uri, user, password);

      if(root==null){
         throw new BuildException("Collection " + uri + " could not be found.");
      }

      DatabaseInstanceManager mgr = (DatabaseInstanceManager) root.getService("DatabaseInstanceManager", "1.0");
      log("Shutdown database instance", Project.MSG_INFO);
      mgr.shutdown();
    } catch (XMLDBException e)
    {
      throw new BuildException("Error during database shutdown: " + e.getMessage(), e);
    }
  }

}
