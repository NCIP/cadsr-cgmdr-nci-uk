/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

//$Id: CreateCollectionClusterEvent.java 2435 2006-01-07 20:54:45Z brihaye $
package org.exist.cluster;

import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.CollectionManagementService;

/**
 * Created by Francesco Mondora.
 *
 * @author Francesco Mondora aka Makkina
 *         Date: 14-dic-2004
 *         Time: 18.21.12
 *         Revision $Revision: 2435 $
 */
public class CreateCollectionClusterEvent extends ClusterEvent {

    String parent;
    String collectionName;
    private static final long serialVersionUID = 0L;

    public CreateCollectionClusterEvent(String parent, String collectionName) {
        this.parent = parent;
        this.collectionName = collectionName;
    }

    public String getCollectionName()
    {
        return this.collectionName;
    }

    public String getParent()
    {
        return this.parent;
    }
    /**
     * Execute the current command.
     */
    public void execute() throws ClusterException {
        try {
            Collection parent = getCollection(this.parent);
            CollectionManagementService mgtService =
                    (CollectionManagementService) parent.getService("CollectionManagementService", "1.0");
            mgtService.createCollection(this.collectionName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int hashCode() {
        return this.collectionName.hashCode() ^
               this.parent.hashCode() ^
               this.getClass().getName().hashCode() ;
    }
}
