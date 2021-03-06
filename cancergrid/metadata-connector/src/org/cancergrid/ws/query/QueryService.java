/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

/**
 * Copyright (c) 2005-2008 CancerGrid Consortium <http://www.cancergrid.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,  
 * and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.cancergrid.ws.query;

import java.util.List;

import org.apache.log4j.Logger;
import org.cancergrid.schema.query.Query;
import org.cancergrid.schema.result_set.ResultSetDocument;
import org.cancergrid.ws.config.QueryServiceConfig;
import org.cancergrid.ws.util.ChainTransformer;

/**
 * This class define properties and methods common to all CancerGrid query 
 * services. New/custom query services must extends this class and provide 
 * concrete definition for "executeQuery" methods.  
 * 
 * @author <a href="mailto:Andrew.Tsui@comlab.ox.ac.uk">Andrew Tsui</a> (<a href="http://www.cancergrid.org">CancerGrid Consortium</a>)
 * @version 1.0
 * 
 * @see org.cancergrid.ws.query.QueryOperation
 *
 */

public abstract class QueryService implements QueryOperation
{
	public enum QueryMode 
	{
		/**
		 * Concept query mode
		 */
		CONCEPT, 
		
		/**
		 * CDE query mode
		 */
		CDE,
		
		/**
		 * Unknown
		 */
		UNKNOWN
	};
	
	/**
	 * Log4J Logger
	 */
	private static Logger LOG = Logger.getLogger(QueryService.class);
	/**
	 * Selecting which information, Concept or CDE, to query and return
	 */
	protected QueryMode qMode;
	
	/**
	 * URL of remote service
	 */
	protected java.net.URL serviceUrl = null;
	
	/**
	 * ChainTransformer to process messages
	 */
	protected ChainTransformer transform = null;
	
	/**
	 * Sequence of labels for specifying templates to use in transforming query request 
	 * to vocabulary/metadata service specific format. 
	 */
	protected List<String> requestSequence = null;
	
	/**
	 * Sequence of labels for specifying templates to use in transforming query response 
	 * from vocabulary/metadata service to a common format. 
	 */
	protected List<String> digestSequence = null;
	
	/**
	 * Configuration information
	 */
	protected QueryServiceConfig config = null;
		
	public QueryService()
	{
		transform = new ChainTransformer();
	}
	
	public QueryService(QueryMode mode)
	{
		qMode = mode;
		transform = new ChainTransformer();
	}
	
	/**
	 * Initialize remote service
	 *
	 */
	public abstract void initService();
	
	/**
	 * Set the URL of the remote service. Useful when using web service URL different from
	 * the one embedded into the generated proxy classes. 
	 * 
	 * @param url URL of the remote service
	 */
	public void setServiceUrl(java.net.URL url)
	{
		serviceUrl = url;
	}
	
	@Override
	public ResultSetDocument query(Query query)
	{
		try
		{
			Query request  = constructRequest(query);
			String content = executeQuery(request);
			if (content == null)
			{
				return null;
			}
			String result = generateDigest(content);
			ResultSetDocument rs = ResultSetDocument.Factory.parse(result);
			return rs;
		} catch (Exception e)
		{
			LOG.error("Query fail to complete: "+e);
			return null;
		}
	}
	
	/**
	 * Set the query mode of this service
	 * 
	 * @param mode CDE or CONCEPT
	 */
	public void setQueryMode(QueryMode mode)
	{
		qMode = mode;
	}
	
	/**
	 * Get the query mode of this service
	 * 
	 * @return CDE or CONCEPT
	 */
	public QueryMode getQueryMode()
	{
		return qMode;
	}
	
	/**
	 * Convenient method for escaping illegal character in a given string
	 * @param s String to check for illegal XML character
	 * @return a string with all illegal XML character replace by escaped character
	 */
	public static String escapeChar(String s)
	{
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		return s;
	}
	
	/**
	 * Get the transform sequence for the digest phase
	 * @return digest transform sequence
	 */
	public List<String> getDigestSequence()
	{
		return digestSequence;
	}
	
	/**
	 * Create a new digest transform sequence
	 * @param seq digest transform sequence labels
	 * @param config configuration to use
	 */
	public void setDigestSequence(List<String> seq, QueryServiceConfig config)
	{
		try {
			this.digestSequence = seq;
			this.config = config;
			
			for (String s : digestSequence)
			{
				transform.addTemplate(s,ChainTransformer.createTemplate(config.getResourceAsStream("stylesheets/"+s+".xsl")));
			}
		}
		catch (Exception e)
		{
			LOG.error("Fail to create digest transform sequence: " + e);
		}
	}

	/**
	 * Reinitialize the digest transform sequence
	 */
	public void resetDigestSequence()
	{
		setDigestSequence(digestSequence, config);
	}

	/**
	 * Get the transform sequence for the request phase
	 * @return request transform sequence
	 */
	public List<String> getRequestSequence()
	{
		return requestSequence;
	}
	
	/**
	 * Create a new request sequence
	 * @param seq request transform sequence labels
	 * @param config configuration to use
	 */
	public void setRequestSequence(List<String> seq, QueryServiceConfig config)
	{
		try
		{
			this.requestSequence = seq;
			this.config = config;
			for (String s : requestSequence)
			{
				transform.addTemplate(s,ChainTransformer.createTemplate(config.getResourceAsStream("stylesheets/"+s+".xsl")));
			}
		}
		catch (Exception e)
		{
			LOG.error("Fail to create request transform sequence: " + e);
		}
	}

	/**
	 * Reinitialize the request transform sequence
	 */
	public void resetRequestSequence()
	{
		setRequestSequence(requestSequence, config);
	}
	
	/**
	 * Initialize request by adding vocabulary/metadata services specific information
	 * to query request.
	 * 
	 * @param query query request
	 * @return initialized query request
	 */
	protected Query constructRequest(Query query)
	{
		/*
		if (!term.startsWith("*") && !term.endsWith("*"))
		{
			term = "*"+term+"*";
		}
		*/
		query.setServiceUrl(serviceUrl.toExternalForm());
		LOG.debug("Set serviceUrl to: " + query.getServiceUrl());
		return query;
	}
	
	/**
	 * Make the query to vocabulary/metadata service
	 * @param query query request
	 * @return query response from vocabulary/metadata service
	 */
	protected abstract String executeQuery(Query query);
	
	/**
	 * Transform query response to a common format
	 * @param content query response to transform
	 * @return query response transformed into common format
	 */
	protected String generateDigest(String content)
	{
		if (digestSequence != null && digestSequence.size() > 0)
		{
			return transform.applyTemplates(content, digestSequence);
		}
		else
		{
			return content;
		}
	}
	
}

