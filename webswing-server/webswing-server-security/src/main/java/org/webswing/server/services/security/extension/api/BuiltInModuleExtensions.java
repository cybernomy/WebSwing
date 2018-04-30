package org.webswing.server.services.security.extension.api;

import org.webswing.server.services.security.extension.onetimeurl.OneTimeUrlSecurityExtension;
import org.webswing.server.services.security.extension.accessmapping.AccessMappingSecurityExtension;

public enum BuiltInModuleExtensions {
	OneTimeUrl(OneTimeUrlSecurityExtension.class.getName()),
	AccessMapping(AccessMappingSecurityExtension.class.getName());

	private String className;

	BuiltInModuleExtensions(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public static String getExtensionClassName(String name) {
		try {
			BuiltInModuleExtensions builtInExtensions = BuiltInModuleExtensions.valueOf(name);
			return builtInExtensions.getClassName();
		} catch (Exception e) {
			return name;
		}
	}
}