/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

package org.exist.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Helper interface for writing backups. Serves as an abstraction for writing
 * to different targets like directories or zip files.
 */
public interface BackupWriter {

    Writer newContents() throws IOException;

    void closeContents() throws IOException;

    OutputStream newEntry(String name) throws IOException;

    void closeEntry() throws IOException;

    void newCollection(String name);

    void closeCollection();

    void close() throws IOException;
}