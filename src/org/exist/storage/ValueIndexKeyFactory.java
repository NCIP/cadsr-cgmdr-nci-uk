/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/* Created on 27 mai 2005
$Id: ValueIndexKeyFactory.java 6320 2007-08-01 18:01:06Z ellefj $ */
package org.exist.storage;


/** Factory for Keys for Value Indices;
 * provides through serialize() the persistant storage key. */
public interface ValueIndexKeyFactory extends Comparable {

	/** this is called from {@link NativeValueIndex} 
	 * @return the persistant storage key */
	//public byte[] serialize(short collectionId, boolean caseSensitive) throws EXistException;
}
