/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
*  eXist Open Source Native XML Database
*  Copyright (C) 2001-04 Wolfgang M. Meier (wolfgang@exist-db.org) 
*  and others (see http://exist-db.org)
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
*  $Id: XmldbRemoteTests.java 4832 2006-11-18 11:57:39Z dizzzz $
*/
package org.exist.xmldb;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author wolf
 */
public class XmldbRemoteTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Remote tests for org.exist.xmldb");
		suite.addTest(new TestSuite(RemoteCollectionTest.class));
		suite.addTest(new TestSuite(RemoteDatabaseImplTest.class));
		suite.addTest(new TestSuite(RemoteQueryTest.class));
		suite.addTest(new TestSuite(DOMTestJUnit.class));
		return suite;
	}
}
