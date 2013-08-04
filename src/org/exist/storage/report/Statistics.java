/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * Created on 5 sept. 2004
$Id: Statistics.java 5418 2007-02-28 18:16:40Z chrisdutz $
 */
package org.exist.storage.report;

import java.util.Map;

import org.exist.storage.IndexStats;
import org.exist.storage.NativeElementIndex;
import org.exist.storage.NativeTextEngine;
import org.exist.storage.NativeValueIndex;
import org.exist.storage.dom.DOMFile;
import org.exist.storage.index.BFile;
import org.exist.storage.index.CollectionStore;
import org.exist.util.Configuration;

/**
 * @author jmv
 */
public class Statistics {

	/** generate index statistics
	 * @param conf
	 * @param indexStats
	 */
	public static void generateIndexStatistics(Configuration conf, Map indexStats) {
		DOMFile dom = (DOMFile) conf.getProperty(DOMFile.FILE_KEY_IN_CONFIG);
		if(dom != null)
			indexStats.put(DOMFile.FILE_NAME, new IndexStats(dom));
		BFile db = (BFile) conf.getProperty(CollectionStore.FILE_KEY_IN_CONFIG);
		if(db != null)
			indexStats.put(CollectionStore.FILE_NAME, new IndexStats(db));
		db = (BFile) conf.getProperty(NativeElementIndex.FILE_KEY_IN_CONFIG);
		if(db != null) 
			indexStats.put(NativeElementIndex.FILE_NAME, new IndexStats(db));	
		db = (BFile) conf.getProperty(NativeValueIndex.FILE_KEY_IN_CONFIG);
		if(db != null) 
			indexStats.put(NativeValueIndex.FILE_NAME, new IndexStats(db));			
		db = (BFile) conf.getProperty(NativeTextEngine.FILE_KEY_IN_CONFIG);
		if(db != null)
			indexStats.put(NativeTextEngine.FILE_NAME, new IndexStats(db));

	}

}
