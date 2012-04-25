/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.webservices.rest.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.openmrs.api.APIException;

import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

/**
 * Holds information related to a REST web service request
 */
public class RequestContext {
	
	private HttpServletRequest request;
	
	private Representation representation = new DefaultRepresentation();
	
	private Integer startIndex = 0;
	
	private Integer limit = RestUtil.getDefaultLimit();
	
	public RequestContext() {
	}
	
	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	
	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	/**
	 * @return the representation
	 */
	public Representation getRepresentation() {
		return representation;
	}
	
	/**
	 * @param representation
	 *            the representation to set
	 */
	public void setRepresentation(Representation representation) {
		this.representation = representation;
	}
	
	/**
	 * Should be used to limit the number of main results returned by search
	 * methods
	 * 
	 * @return the integer limit set in a request parameter
	 * @see RestUtil#getRequestContext(org.springframework.web.context.request.WebRequest)
	 * @see RestConstants#REQUEST_PROPERTY_FOR_LIMIT
	 */
	public Integer getLimit() {
		return limit;
	}
	
	/**
	 * @param limit the limit to set
	 * @should not accept a value less than one
	 * @should not accept a null value
	 */
	public void setLimit(Integer limit) {
		if (limit == null || limit <= 0)
			throw new APIException("If you specify a number of results to return, it must be >0 and not null");
		if (limit > RestUtil.getAbsoluteLimit())
			throw new APIException("Administrator has set absolute limit at " + RestUtil.getAbsoluteLimit());
		else
			this.limit = limit;
	}
	
	/**
	 * Should be used by search methods to jump results to start with this
	 * number in the list. Set by users in a request parameter
	 * 
	 * @return the integer startIndex
	 * @see RestUtil#getRequestContext(org.springframework.web.context.request.WebRequest)
	 * @see RestConstants#REQUEST_PROPERTY_FOR_START_INDEX
	 */
	public Integer getStartIndex() {
		return startIndex;
	}
	
	/**
	 * @param startIndex
	 *            the startIndex to set
	 */
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}
	
	/**
	 * (Assumes this was a search query)
	 * 
	 * @return the hyperlink you would GET to fetch the next page of results for the query
	 */
	public Hyperlink getNextLink() {
		String query = getQueryWithoutStartIndex();
		query += RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=" + (startIndex + limit);
		return new Hyperlink("next", request.getRequestURL().append(query).toString());
	}
	
	/**
	 * (Assumes this was a search query)
	 * 
	 * @return the hyperlink you would GET to fetch the previous page of results for the query
	 */
	public Hyperlink getPreviousLink() {
		String query = getQueryWithoutStartIndex();
		int prevStart = startIndex - limit;
		if (prevStart < 0)
			prevStart = 0;
		if (prevStart > 0)
			query += RestConstants.REQUEST_PROPERTY_FOR_START_INDEX + "=" + prevStart;
		return new Hyperlink("prev", request.getRequestURL().append(query).toString());
	}
	
	/**
	 * @return the query string from this request, with the startIndex query parameter removed if it was present 
	 */
	@SuppressWarnings("unchecked")
	private String getQueryWithoutStartIndex() {
		StringBuilder query = new StringBuilder("?");
		for (Map.Entry<String, String[]> e : ((Map<String, String[]>) (request.getParameterMap())).entrySet()) {
			String param = e.getKey();
			if (RestConstants.REQUEST_PROPERTY_FOR_START_INDEX.equals(param)) {
				continue;
			}
			for (int i = 0; i < e.getValue().length; ++i) {
				try {
					query.append(e.getKey() + "=" + URLEncoder.encode(e.getValue()[i], "UTF-8") + "&");
				}
				catch (UnsupportedEncodingException ex) {
					throw new RuntimeException("UTF-8 encoding should always be supported", ex);
				}
			}
		}
		return query.toString();
	}
	
}
