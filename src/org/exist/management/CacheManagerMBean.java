/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

package org.exist.management;

/**
 * Created by IntelliJ IDEA.
 * User: wolf
 * Date: Jun 9, 2007
 * Time: 10:33:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CacheManagerMBean {

    long getMaxTotal();

    long getMaxSingle();

    long getCurrentSize();
}
