package org.webswing.server.services.security.api;

import java.io.Serializable;
import java.util.Map;

import org.webswing.toolkit.api.security.WebswingUser;

/**
 * Authenticated users in Webswing are represented by this class. Every {@link WebswingSecurityModule} should provide a subclass
 * of this abstract class.  
 */
public abstract class AbstractWebswingUser implements WebswingUser {

	/**
	 * Special role representing all authenticated users. 
	 */
	public static final String ROLE_AUTHENTICATED = "authenticated";

	private RolePermissionResolver resolver;

	public AbstractWebswingUser() {
		this(new WebswingAction.DefaultRolePermissionResolver());
	}

	/**
	 * @param resolver provide logic for mapping from permissions to roles
	 */
	public AbstractWebswingUser(RolePermissionResolver resolver) {
		this.resolver = resolver;
	}

	
	public abstract String getUserId();

	public abstract Map<String, Serializable> getUserAttributes();

	/**
	 * @param role name or the role
	 * @return true if user has the specified role. False otherwise.
	 */
	public abstract boolean hasRole(String role);

	/**
	 * Checks if current user has defined permission. Default implementation uses associated {@link  RolePermissionResolver} 
	 * to check if user has any of the the roles assigned to defined permission.
	 * @param permission permission name
	 * @return true if user has the permission.
	 */
	public boolean isPermitted(String permission) {
		if (resolver != null) {
			String[] roles = resolver.getRolesForPermission(permission);
			if (roles != null) {
				for (String role : roles) {
					if (ROLE_AUTHENTICATED.equals(role) || hasRole(role)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return getUserId();
	}
}
