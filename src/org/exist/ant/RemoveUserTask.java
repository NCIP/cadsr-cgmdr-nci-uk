/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/**
 * Created by IntelliJ IDEA.
 * User: pak
 * Date: Apr 17, 2005
 * Time: 7:41:35 PM
 * To change this template use File | Settings | File Templates.
 */
package org.exist.ant;

import org.apache.tools.ant.BuildException;
import org.exist.security.User;
import org.xmldb.api.base.XMLDBException;

/**
 * an ant task to remove a name
 *
 * @author peter.klotz@blue-elephant-systems.com
 */
public class RemoveUserTask extends UserTask
{
  private String name = null;

  /* (non-Javadoc)
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException
  {
    super.execute();
    if (name == null)
      throw new BuildException("You have to specify a name");

    try
    {
      User u = service.getUser(name);
      if (u != null)
      {
        service.removeUser(u);
      }
    } catch (XMLDBException e)
    {
      throw new BuildException("XMLDB exception caught: " + e.getMessage(), e);
    }
  }

  public void setName(String name)
  {
    this.name = name;
  }
}
