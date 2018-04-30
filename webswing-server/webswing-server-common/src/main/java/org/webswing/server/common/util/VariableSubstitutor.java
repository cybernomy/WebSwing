package org.webswing.server.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.webswing.Constants;
import org.webswing.server.common.model.SecuredPathConfig;

public class VariableSubstitutor {

	private Map<String, String> customVars;
	private StrSubstitutor subs;

	public static VariableSubstitutor basic() {
		return new VariableSubstitutor(new HashMap<String, String>());
	}

	public static VariableSubstitutor forSwingInstance(SecuredPathConfig config, String user, Map<String, Serializable> userAttributes, String sessionId, String clientIp, String locale, String customArgs) {
		Map<String, String> result = new HashMap<String, String>();
		if (user != null) {
			result.put(Constants.USER_NAME_SUBSTITUTE, user);
		}
		if (userAttributes != null) {
			for (Entry<String, Serializable> e : userAttributes.entrySet()) {
				try {
					result.put(Constants.USER_NAME_SUBSTITUTE + "." + e.getKey(), e.getValue().toString());
				} catch (Exception e1) {
					//ignore
				}
			}
		}
		if (sessionId != null) {
			result.put(Constants.SESSION_ID_SUBSTITUTE, sessionId);
		}
		if (clientIp != null) {
			result.put(Constants.SESSION_IP_SUBSTITUTE, clientIp);
		}
		if (locale != null) {
			result.put(Constants.SESSION_LOCALE_SUBSTITUTE, locale);
		}
		if (customArgs != null) {
			result.put(Constants.SESSION_CUSTOMARGS_SUBSTITUTE, customArgs);
		}
		if (config != null) {
			result.put(Constants.APP_HOME_FOLDER_SUBSTITUTE, config.getHomeDir());
			result.put(Constants.APP_CONTEXT_PATH_SUBSTITUTE, config.getPath());
		}
		return new VariableSubstitutor(result);
	}

	public static VariableSubstitutor forSwingApp(SecuredPathConfig config) {
		Map<String, String> result = new HashMap<String, String>();
		if (config != null) {
			result.put(Constants.APP_HOME_FOLDER_SUBSTITUTE, config.getHomeDir());
			result.put(Constants.APP_CONTEXT_PATH_SUBSTITUTE, config.getPath());
		}
		return new VariableSubstitutor(result);
	}

	public VariableSubstitutor(Map<String, String> customVars) {
		this.customVars = customVars;
		this.subs = new StrSubstitutor(new StrLookup<String>() {

			@Override
			public String lookup(String key) {
				String value = VariableSubstitutor.this.customVars.get(key);
				if (value != null) {
					return value;
				}
				value = System.getProperty(key);
				if (value != null) {
					return value;
				}
				value = System.getenv(key);
				if (value != null) {
					return value;
				}
				return null;
			}
		});
	}

	public String replace(String string) {
		return subs.replace(string);
	}

	public Map<String, String> getVariableMap() {
		Map<String, String> result = new HashMap<String, String>();
		result.putAll(System.getenv());
		for (final String name : System.getProperties().stringPropertyNames()) {
			result.put(name, System.getProperties().getProperty(name));
		}
		result.putAll(customVars);
		return result;
	}

}
