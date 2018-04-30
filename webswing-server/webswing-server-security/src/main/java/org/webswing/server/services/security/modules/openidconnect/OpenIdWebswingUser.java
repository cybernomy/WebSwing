package org.webswing.server.services.security.modules.openidconnect;

import com.google.api.client.auth.openidconnect.IdToken;
import org.apache.commons.lang3.StringUtils;
import org.webswing.server.services.security.api.AbstractWebswingUser;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vikto on 03-Feb-17.
 */
public class OpenIdWebswingUser extends AbstractWebswingUser {

	IdToken token;
	String user;
	Map<String, Serializable> attrs = new HashMap<>();
	List<String> roles=new ArrayList<>();

	public OpenIdWebswingUser(IdToken token, String usernameAttr, String roleAttr, Map<String, Serializable> extraAttribs) {
		this.token = token;
		for (String key : token.getPayload().keySet()) {
			Object value = token.getPayload().get(key);
			if (value instanceof Serializable) {
				attrs.put(key, (Serializable) value);
			}
			if (value instanceof String && StringUtils.equalsIgnoreCase(roleAttr, key)) {
				roles = toList((String) value);
			}
		}
		if (extraAttribs != null) {
			attrs.putAll(extraAttribs);
		}
		if (attrs.get(usernameAttr) != null && attrs.get(usernameAttr) instanceof String) {
			user = (String) attrs.get(usernameAttr);
		} else {
			user = token.getPayload().getSubject();
		}
	}

	private List<String> toList(String value) {
		if (value != null) {
			if (StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) {
				value = value.substring(1, value.length() - 1);
			}
			String[] values = StringUtils.split(value, ",");
			values = StringUtils.stripAll(values);
			return Arrays.asList(values);
		}
		return Collections.emptyList();
	}

	@Override
	public String getUserId() {
		return user;
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return Collections.unmodifiableMap(attrs);
	}

	@Override
	public boolean hasRole(String role) {
		return roles.contains(role);
	}
}
