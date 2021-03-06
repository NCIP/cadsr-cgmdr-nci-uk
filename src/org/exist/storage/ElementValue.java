/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
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
 *  $Id: ElementValue.java 6795 2007-10-27 06:36:41Z brihaye $
 */
package org.exist.storage;

import org.exist.collections.Collection;
import org.exist.dom.SymbolTable;
import org.exist.storage.btree.Value;
import org.exist.util.ByteConversion;
import org.exist.util.UTF8;

/**
 * @author wolf
 */
public class ElementValue extends Value {

	public static final byte UNKNOWN = -1;
	public static final byte ELEMENT = 0;
	public static final byte ATTRIBUTE = 1;
	public static final byte ATTRIBUTE_ID = 2;
	public static final byte ATTRIBUTE_IDREF = 3;
	public static final byte ATTRIBUTE_IDREFS = 4;
	
	public static int LENGTH_TYPE = 1; //sizeof byte

	public static int OFFSET_COLLECTION_ID = 0;	
	public static int OFFSET_TYPE = OFFSET_COLLECTION_ID + Collection.LENGTH_COLLECTION_ID; //2
	public static int OFFSET_SYMBOL = OFFSET_TYPE + ElementValue.LENGTH_TYPE; //3
	public static int OFFSET_NSSYMBOL = OFFSET_SYMBOL + SymbolTable.LENGTH_LOCAL_NAME; //5
	public static int OFFSET_ID_STRING_VALUE = OFFSET_TYPE + LENGTH_TYPE; //3
	
	public static final String[] type = { "element", "attribute", "id" };

    ElementValue(int collectionId) {
		len = Collection.LENGTH_COLLECTION_ID;
		data = new byte[len];
		ByteConversion.intToByte(collectionId, data, OFFSET_COLLECTION_ID);
		pos = OFFSET_COLLECTION_ID;
	}

	ElementValue(byte type, int collectionId) {
		len = Collection.LENGTH_COLLECTION_ID + ElementValue.LENGTH_TYPE;
		data = new byte[len];
		ByteConversion.intToByte(collectionId, data, OFFSET_COLLECTION_ID);
		data[OFFSET_TYPE] = type;
		pos = OFFSET_COLLECTION_ID;
	}

	ElementValue(byte type, int collectionId, short symbol) {
		len = Collection.LENGTH_COLLECTION_ID + ElementValue.LENGTH_TYPE + SymbolTable.LENGTH_LOCAL_NAME;
		data = new byte[len];
		ByteConversion.intToByte(collectionId, data, OFFSET_COLLECTION_ID);
		data[OFFSET_TYPE] = type;
		ByteConversion.shortToByte(symbol, data, OFFSET_SYMBOL);
		pos = OFFSET_COLLECTION_ID;
	}

	ElementValue(byte type, int collectionId, short symbol, short nsSymbol) {
		len = Collection.LENGTH_COLLECTION_ID + ElementValue.LENGTH_TYPE + SymbolTable.LENGTH_LOCAL_NAME + OFFSET_NSSYMBOL;
		data = new byte[len];
		ByteConversion.intToByte(collectionId, data, OFFSET_COLLECTION_ID);
		data[OFFSET_TYPE] = type;
		ByteConversion.shortToByte(symbol, data, OFFSET_SYMBOL);
		ByteConversion.shortToByte(nsSymbol, data, OFFSET_NSSYMBOL);
		pos = OFFSET_COLLECTION_ID;
	}

	ElementValue(byte type, int collectionId, String idStringValue) {
		//Note that the type expected to be ElementValue.ATTRIBUTE_ID
		//TODO : add sanity check for this ?
		len = Collection.LENGTH_COLLECTION_ID + ElementValue.LENGTH_TYPE + UTF8.encoded(idStringValue);
		data = new byte[len];
		ByteConversion.intToByte(collectionId, data, OFFSET_COLLECTION_ID);
		data[OFFSET_TYPE] = type;
		UTF8.encode(idStringValue, data, OFFSET_ID_STRING_VALUE);
		//TODO : reset pos, just like in other contructors ?
	}

	int getCollectionId() {
		return ByteConversion.byteToInt(data, OFFSET_COLLECTION_ID);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Collection id : " + ByteConversion.byteToInt(data, OFFSET_COLLECTION_ID));
		if (len > OFFSET_COLLECTION_ID) {
			buf.append(" Type : " + type[data[OFFSET_TYPE]]);
			if (data[OFFSET_TYPE] == ElementValue.ATTRIBUTE_ID) {
				 //untested 4 is strange (would have expected 3, i.e. OFFSET_ID_STRING_VALUE)
				buf.append(" id : " + UTF8.decode(data, 4, data.length - (Collection.LENGTH_COLLECTION_ID + LENGTH_TYPE)));
			} else if (data[OFFSET_TYPE] == ElementValue.ATTRIBUTE_IDREF) {
					 //untested 4 is strange (would have expected 3, i.e. OFFSET_ID_STRING_VALUE)
					buf.append(" idref : " + UTF8.decode(data, 4, data.length - (Collection.LENGTH_COLLECTION_ID + LENGTH_TYPE)));
			} if (data[OFFSET_TYPE] == ElementValue.ATTRIBUTE_IDREFS) {
				 //untested 4 is strange (would have expected 3, i.e. OFFSET_ID_STRING_VALUE)
				buf.append(" idrefs : " + UTF8.decode(data, 4, data.length - (Collection.LENGTH_COLLECTION_ID + LENGTH_TYPE)));
			} else {
				if (len == Collection.LENGTH_COLLECTION_ID + ElementValue.LENGTH_TYPE + SymbolTable.LENGTH_LOCAL_NAME)
					buf.append(" Symbol id : " + ByteConversion.byteToShort(data, OFFSET_SYMBOL));
				else if (len == Collection.LENGTH_COLLECTION_ID + ElementValue.LENGTH_TYPE + 
						SymbolTable.LENGTH_LOCAL_NAME + SymbolTable.LENGTH_NS_URI) {
					buf.append(" Symbol id : " + ByteConversion.byteToShort(data, OFFSET_SYMBOL));
					buf.append(" NSSymbol id : " + ByteConversion.byteToShort(data, OFFSET_NSSYMBOL));
				} else 
					buf.append("Invalid data length !!!");
			}
		}
		return buf.toString();
	}
}
