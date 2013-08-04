/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * IndexCallback.java - Mar 12, 2003
 * 
 * @author wolf
 */
package org.exist.util;

import org.exist.storage.btree.Value;

public interface IndexCallback {

	public boolean indexInfo(Value key, Value value);
}
