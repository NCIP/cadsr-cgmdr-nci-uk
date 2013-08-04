/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

package org.exist.fluent;

/**
 * An action being undertaken on the database, used to characterize an event.
 * Note that for folders, <code>UPDATE</code> means rename.
 *
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 */
public enum Trigger {
	BEFORE_CREATE,
	AFTER_CREATE,
	BEFORE_UPDATE,
	AFTER_UPDATE,
	BEFORE_DELETE,
	AFTER_DELETE
}
