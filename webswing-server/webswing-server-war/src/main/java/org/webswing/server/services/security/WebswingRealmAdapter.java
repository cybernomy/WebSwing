package org.webswing.server.services.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

public class WebswingRealmAdapter extends AuthorizingRealm {

	public static final String WEBSWING_REALM = "webswingRealm";

	public WebswingRealmAdapter() {
		setCredentialsMatcher(new AllowAllCredentialsMatcher());
	}

	@Override
	public String getName() {
		return WEBSWING_REALM;
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		if (token instanceof LoginTokenAdapter) {
			return true;
		}
		return false;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		AuthenticationInfo info = null;
		if (token instanceof LoginTokenAdapter) {
			WebswingPrincipal user = (WebswingPrincipal) token.getPrincipal();
			if (SecurityUtils.getSubject().getPrincipals() == null && !(token instanceof LogoutTokenAdapter)) {
				info = new WebswingAuthenticationInfo(new SimplePrincipalCollection(user, WebswingRealmAdapter.WEBSWING_REALM), token.getCredentials());
			} else {
				List<WebswingPrincipal> principals = new ArrayList<WebswingPrincipal>(SecurityUtils.getSubject().getPrincipals().byType(WebswingPrincipal.class));
				for (Iterator<WebswingPrincipal> i = principals.iterator(); i.hasNext();) {
					WebswingPrincipal p = i.next();
					if (user.getSecuredPath().equals(p.getSecuredPath())) {
						i.remove();
					}
				}
				if (!(token instanceof LogoutTokenAdapter)) {
					principals.add(user);
				}
				if (principals.size() > 0) {
					info = new WebswingAuthenticationInfo(new SimplePrincipalCollection(principals, WebswingRealmAdapter.WEBSWING_REALM), token.getCredentials());
				} else {
					throw new AuthenticationException("No principal found.");
				}
			}
		}
		return info;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
		SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
		for (WebswingPrincipal p : principals.byType(WebswingPrincipal.class)) {
			sai.addObjectPermission(p);
		}
		return sai;
	}

}
