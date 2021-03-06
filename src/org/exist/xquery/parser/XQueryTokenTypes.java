/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

// $ANTLR 2.7.7 (2006-11-01): "XQuery.g" -> "XQueryParser.java"$

	package org.exist.xquery.parser;

	import antlr.debug.misc.*;
	import java.io.StringReader;
	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Iterator;
	import java.util.Stack;
	import org.exist.storage.BrokerPool;
	import org.exist.storage.DBBroker;
	import org.exist.storage.analysis.Tokenizer;
	import org.exist.EXistException;
	import org.exist.dom.DocumentSet;
	import org.exist.dom.DocumentImpl;
	import org.exist.dom.QName;
	import org.exist.security.PermissionDeniedException;
	import org.exist.security.User;
	import org.exist.xquery.*;
	import org.exist.xquery.value.*;
	import org.exist.xquery.functions.*;

public interface XQueryTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int QNAME = 4;
	int PREDICATE = 5;
	int FLWOR = 6;
	int PARENTHESIZED = 7;
	int ABSOLUTE_SLASH = 8;
	int ABSOLUTE_DSLASH = 9;
	int WILDCARD = 10;
	int PREFIX_WILDCARD = 11;
	int FUNCTION = 12;
	int UNARY_MINUS = 13;
	int UNARY_PLUS = 14;
	int XPOINTER = 15;
	int XPOINTER_ID = 16;
	int VARIABLE_REF = 17;
	int VARIABLE_BINDING = 18;
	int ELEMENT = 19;
	int ATTRIBUTE = 20;
	int ATTRIBUTE_CONTENT = 21;
	int TEXT = 22;
	int VERSION_DECL = 23;
	int NAMESPACE_DECL = 24;
	int DEF_NAMESPACE_DECL = 25;
	int DEF_COLLATION_DECL = 26;
	int DEF_FUNCTION_NS_DECL = 27;
	int GLOBAL_VAR = 28;
	int FUNCTION_DECL = 29;
	int PROLOG = 30;
	int OPTION = 31;
	int ATOMIC_TYPE = 32;
	int MODULE = 33;
	int ORDER_BY = 34;
	int GROUP_BY = 35;
	int POSITIONAL_VAR = 36;
	int MODULE_DECL = 37;
	int MODULE_IMPORT = 38;
	int SCHEMA_IMPORT = 39;
	int ATTRIBUTE_TEST = 40;
	int COMP_ELEM_CONSTRUCTOR = 41;
	int COMP_ATTR_CONSTRUCTOR = 42;
	int COMP_TEXT_CONSTRUCTOR = 43;
	int COMP_COMMENT_CONSTRUCTOR = 44;
	int COMP_PI_CONSTRUCTOR = 45;
	int COMP_NS_CONSTRUCTOR = 46;
	int COMP_DOC_CONSTRUCTOR = 47;
	int PRAGMA = 48;
	int LITERAL_xpointer = 49;
	int LPAREN = 50;
	int RPAREN = 51;
	int NCNAME = 52;
	int LITERAL_xquery = 53;
	int LITERAL_version = 54;
	int SEMICOLON = 55;
	int LITERAL_module = 56;
	int LITERAL_namespace = 57;
	int EQ = 58;
	int STRING_LITERAL = 59;
	int LITERAL_declare = 60;
	int LITERAL_default = 61;
	// "boundary-space" = 62
	int LITERAL_ordering = 63;
	int LITERAL_construction = 64;
	// "base-uri" = 65
	// "copy-namespaces" = 66
	int LITERAL_option = 67;
	int LITERAL_function = 68;
	int LITERAL_variable = 69;
	int LITERAL_import = 70;
	int LITERAL_encoding = 71;
	int LITERAL_collation = 72;
	int LITERAL_element = 73;
	int LITERAL_order = 74;
	int LITERAL_empty = 75;
	int LITERAL_greatest = 76;
	int LITERAL_least = 77;
	int LITERAL_preserve = 78;
	int LITERAL_strip = 79;
	int LITERAL_ordered = 80;
	int LITERAL_unordered = 81;
	int COMMA = 82;
	// "no-preserve" = 83
	int LITERAL_inherit = 84;
	// "no-inherit" = 85
	int DOLLAR = 86;
	int LCURLY = 87;
	int RCURLY = 88;
	int COLON = 89;
	int LITERAL_external = 90;
	int LITERAL_schema = 91;
	int LITERAL_as = 92;
	int LITERAL_at = 93;
	// "empty-sequence" = 94
	int QUESTION = 95;
	int STAR = 96;
	int PLUS = 97;
	int LITERAL_item = 98;
	int LITERAL_for = 99;
	int LITERAL_let = 100;
	int LITERAL_some = 101;
	int LITERAL_every = 102;
	int LITERAL_if = 103;
	int LITERAL_typeswitch = 104;
	int LITERAL_update = 105;
	int LITERAL_replace = 106;
	int LITERAL_value = 107;
	int LITERAL_insert = 108;
	int LITERAL_delete = 109;
	int LITERAL_rename = 110;
	int LITERAL_with = 111;
	int LITERAL_into = 112;
	int LITERAL_preceding = 113;
	int LITERAL_following = 114;
	int LITERAL_where = 115;
	int LITERAL_return = 116;
	int LITERAL_in = 117;
	int LITERAL_by = 118;
	int LITERAL_stable = 119;
	int LITERAL_ascending = 120;
	int LITERAL_descending = 121;
	int LITERAL_group = 122;
	int LITERAL_satisfies = 123;
	int LITERAL_case = 124;
	int LITERAL_then = 125;
	int LITERAL_else = 126;
	int LITERAL_or = 127;
	int LITERAL_and = 128;
	int LITERAL_instance = 129;
	int LITERAL_of = 130;
	int LITERAL_treat = 131;
	int LITERAL_castable = 132;
	int LITERAL_cast = 133;
	int BEFORE = 134;
	int AFTER = 135;
	int LITERAL_eq = 136;
	int LITERAL_ne = 137;
	int LITERAL_lt = 138;
	int LITERAL_le = 139;
	int LITERAL_gt = 140;
	int LITERAL_ge = 141;
	int NEQ = 142;
	int GT = 143;
	int GTEQ = 144;
	int LT = 145;
	int LTEQ = 146;
	int LITERAL_is = 147;
	int LITERAL_isnot = 148;
	int ANDEQ = 149;
	int OREQ = 150;
	int LITERAL_to = 151;
	int MINUS = 152;
	int LITERAL_div = 153;
	int LITERAL_idiv = 154;
	int LITERAL_mod = 155;
	int PRAGMA_START = 156;
	int PRAGMA_END = 157;
	int LITERAL_union = 158;
	int UNION = 159;
	int LITERAL_intersect = 160;
	int LITERAL_except = 161;
	int SLASH = 162;
	int DSLASH = 163;
	int LITERAL_text = 164;
	int LITERAL_node = 165;
	int LITERAL_attribute = 166;
	int LITERAL_comment = 167;
	// "processing-instruction" = 168
	// "document-node" = 169
	int LITERAL_document = 170;
	int SELF = 171;
	int XML_COMMENT = 172;
	int XML_PI = 173;
	int LPPAREN = 174;
	int RPPAREN = 175;
	int AT = 176;
	int PARENT = 177;
	int LITERAL_child = 178;
	int LITERAL_self = 179;
	int LITERAL_descendant = 180;
	// "descendant-or-self" = 181
	// "following-sibling" = 182
	int LITERAL_parent = 183;
	int LITERAL_ancestor = 184;
	// "ancestor-or-self" = 185
	// "preceding-sibling" = 186
	int DOUBLE_LITERAL = 187;
	int DECIMAL_LITERAL = 188;
	int INTEGER_LITERAL = 189;
	// "schema-element" = 190
	int END_TAG_START = 191;
	int QUOT = 192;
	int APOS = 193;
	int QUOT_ATTRIBUTE_CONTENT = 194;
	int ESCAPE_QUOT = 195;
	int APOS_ATTRIBUTE_CONTENT = 196;
	int ESCAPE_APOS = 197;
	int ELEMENT_CONTENT = 198;
	int XML_COMMENT_END = 199;
	int XML_PI_END = 200;
	int XML_CDATA = 201;
	int LITERAL_collection = 202;
	int LITERAL_validate = 203;
	int XML_PI_START = 204;
	int XML_CDATA_START = 205;
	int XML_CDATA_END = 206;
	int LETTER = 207;
	int DIGITS = 208;
	int HEX_DIGITS = 209;
	int NMSTART = 210;
	int NMCHAR = 211;
	int WS = 212;
	int EXPR_COMMENT = 213;
	int PREDEFINED_ENTITY_REF = 214;
	int CHAR_REF = 215;
	int S = 216;
	int NEXT_TOKEN = 217;
	int CHAR = 218;
	int BASECHAR = 219;
	int IDEOGRAPHIC = 220;
	int COMBINING_CHAR = 221;
	int DIGIT = 222;
	int EXTENDER = 223;
}
