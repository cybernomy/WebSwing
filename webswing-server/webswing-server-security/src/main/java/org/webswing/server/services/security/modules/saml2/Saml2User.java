package org.webswing.server.services.security.modules.saml2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.webswing.server.services.security.api.AbstractWebswingUser;

public class Saml2User extends AbstractWebswingUser {
	private String name = null;
	private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

	public Saml2User(String samlToken, String name, Map<String, List<String>> map) {
		this.name = name;
		for (Entry<String, List<String>> e : map.entrySet()) {
			if (e.getValue() != null && e.getValue().size() == 1) {
				attributes.put(e.getKey(), e.getValue().get(0));
			} else {
				attributes.put(e.getKey(), new ArrayList<String>(e.getValue()));
			}
		}
	}

	@Override
	public String getUserId() {
		return name;
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return attributes;
	}

	@Override
	public boolean hasRole(String role) {
		return true;
	}

}
