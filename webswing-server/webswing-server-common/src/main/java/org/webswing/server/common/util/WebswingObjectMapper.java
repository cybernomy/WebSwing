package org.webswing.server.common.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class WebswingObjectMapper extends ObjectMapper {
	private static final long serialVersionUID = -2451626191300063935L;

	private static final WebswingObjectMapper mapper = new WebswingObjectMapper();

	public static WebswingObjectMapper get() {
		return mapper;
	}

	public WebswingObjectMapper() {
		setSerializationInclusion(Include.NON_NULL);
		disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
	}
}