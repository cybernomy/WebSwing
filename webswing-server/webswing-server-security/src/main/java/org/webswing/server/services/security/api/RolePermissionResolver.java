package org.webswing.server.services.security.api;

/**
 * Provides a logic to map Roles to Permissions for {@link AbstractWebswingUser#isPermitted(String)}'s
 * default implementation. 
 */
public interface RolePermissionResolver {
	/**
	 * @param permission permission name
	 * @return All roles the permission is allowed for
	 */
	String[] getRolesForPermission(String permission);
}
