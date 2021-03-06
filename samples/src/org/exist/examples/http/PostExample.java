/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2008 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: PostExample.java 7173 2008-01-10 16:35:36Z ellefj $
 */
package org.exist.examples.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.exist.Namespaces;
import org.exist.storage.DBBroker;

/**
 * PostExample
 * Execute: bin\run.bat org.exist.examples.http.PostExample
 * Make sure you have the server started with bin\startup.bat beforehand.
 *
 * @author wolf
 */
public class PostExample {

	public final static String REQUEST_HEADER =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<query xmlns=\"" + Namespaces.EXIST_NS + "\" ";
	
	public final static String REQUEST_FOOTER =
		"</query>";
	
	public final static String PROPERTIES =
		"<properties>" +
		"<property name=\"indent\" value=\"yes\"/>" +
		"<property name=\"encoding\" value=\"UTF-8\"/>" +
		"</properties>";
	
	public void query(String query) throws IOException {
		String request = REQUEST_HEADER +
			" howmany=\"-1\">" +
			"<text>" + query  + "</text>" +
			PROPERTIES +
			REQUEST_FOOTER;
		doPost(request);
	}
	
	private void doPost(String request) throws IOException {
		URL url = new URL("http://localhost:8080/exist/rest" + DBBroker.ROOT_COLLECTION);
		HttpURLConnection connect = (HttpURLConnection) url.openConnection();
		connect.setRequestMethod("POST");
		connect.setDoOutput(true);
		
		OutputStream os = connect.getOutputStream();
		os.write(request.getBytes("UTF-8"));
		connect.connect();

        BufferedReader is = new BufferedReader(new InputStreamReader(connect.getInputStream()));
        String line;
        while((line = is.readLine()) != null)
            System.out.println(line);
    }
	
	public static void main(String[] args) {
		PostExample client = new PostExample();
		try {
			client.query("declare namespace rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\ndeclare namespace dc=\"http://purl.org/dc/elements/1.1/\";\n//rdf:Description[dc:subject &amp;= 'umw*']");
		} catch (IOException e) {
			System.err.println("An exception occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
