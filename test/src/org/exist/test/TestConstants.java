/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

package org.exist.test;

import org.exist.xmldb.XmldbURI;
import org.exist.xquery.util.URIUtils;

public class TestConstants {

	/**
	 * String representing the decoded path: t[e s]tá열
	 */
	public static final String DECODED_SPECIAL_NAME = "t[e s]t\u00E0\uC5F4";
	
	/**
	 * String representing the encoded path: t%5Be%20s%5Dt%C3%A0%EC%97%B4
	 */
	public static final String SPECIAL_NAME = URIUtils.urlEncodeUtf8(DECODED_SPECIAL_NAME);

	/**
	 * XmldbURI representing the decoded path: t[e s]tá열
	 */
	public static final XmldbURI SPECIAL_URI = XmldbURI.create(SPECIAL_NAME);

	/**
	 * XmldbURI representing the decoded path: /db/test
	 */
	public static final XmldbURI TEST_COLLECTION_URI = XmldbURI.ROOT_COLLECTION_URI.append("test");
	/**
	 * XmldbURI representing the decoded path: /db/test/test2
	 */
	public static final XmldbURI TEST_COLLECTION_URI2 = TEST_COLLECTION_URI.append("test2");
	/**
	 * XmldbURI representing the decoded path: /db/test/test2/test3
	 */
	public static final XmldbURI TEST_COLLECTION_URI3 = TEST_COLLECTION_URI2.append("test3");
	
	/**
	 * XmldbURI representing the decoded path: /db/t[e s]tá열
	 */
	public static final XmldbURI SPECIAL_COLLECTION_URI = XmldbURI.ROOT_COLLECTION_URI.append(SPECIAL_NAME);

	/**
	 * XmldbURI representing the decoded path: /db/destination
	 */
	public static final XmldbURI DESTINATION_COLLECTION_URI = XmldbURI.ROOT_COLLECTION_URI.append("destination");
	/**
	 * XmldbURI representing the decoded path: /db/destination2
	 */
	public static final XmldbURI DESTINATION_COLLECTION_URI2 = XmldbURI.ROOT_COLLECTION_URI.append("destination2");
	/**
	 * XmldbURI representing the decoded path: /db/destination3
	 */
	public static final XmldbURI DESTINATION_COLLECTION_URI3 = XmldbURI.ROOT_COLLECTION_URI.append("destination3");
	
	/**
	 * XmldbURI representing the decoded path: test.xml
	 */
	public static final XmldbURI TEST_XML_URI = XmldbURI.create("test.xml");
	/**
	 * XmldbURI representing the decoded path: test2.xml
	 */
	public static final XmldbURI TEST_XML_URI2 = XmldbURI.create("test2.xml");
	/**
	 * XmldbURI representing the decoded path: test3.xml
	 */
	public static final XmldbURI TEST_XML_URI3 = XmldbURI.create("test3.xml");
	
	/**
	 * XmldbURI representing the decoded path: t[e s]tá열.xml
	 */
	public static final XmldbURI SPECIAL_XML_URI = XmldbURI.create(URIUtils.urlEncodeUtf8("t[es]t\u00E0\uC5F4.xml"));

	/**
	 * XmldbURI representing the decoded path: binary.txt
	 */
	public static final XmldbURI TEST_BINARY_URI = XmldbURI.create("binary.txt");
}
