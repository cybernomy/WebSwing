package org.webswing.server.services.security.modules.database;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractUserPasswordSecurityModule;
import org.webswing.server.services.security.modules.property.ShiroWebswingUser;

public class DatabaseSecurityModule extends AbstractUserPasswordSecurityModule<DatabaseSecurityModuleConfig> {

	private JdbcRealm realm;

	public DatabaseSecurityModule(DatabaseSecurityModuleConfig config) {
		super(config);
	}

	@Override
	public void init() {
		super.init();
		realm = new JdbcRealm();
		realm.setDataSource(initializeDataSource());
		realm.setAuthenticationQuery(getConfig().getAuthenticationQuery());
		realm.setUserRolesQuery(getConfig().getUserRolesQuery());
		realm.setPermissionsQuery(getConfig().getPermissionsQuery());
		realm.setCredentialsMatcher(initializeCredentialMatcher());
		realm.setSaltStyle(getConfig().isPasswordSalted() ? SaltStyle.COLUMN : SaltStyle.NO_SALT);
		realm.setPermissionsLookupEnabled(getConfig().isPermissionsEnabled());
		realm.init();
	}

	protected CredentialsMatcher initializeCredentialMatcher() {
		String hash = getConfig().getHashAlgorithm();
		try {
			hash = HashType.valueOf(hash).getValue();
		} catch (Exception e) {
		}
		if (hash == null) {
			return new SimpleCredentialsMatcher();
		} else {
			HashedCredentialsMatcher hashedMatcher = new HashedCredentialsMatcher();
			hashedMatcher.setHashAlgorithmName(hash);
			hashedMatcher.setHashIterations(getConfig().getHashIterations());
			hashedMatcher.setStoredCredentialsHexEncoded(getConfig().getHashHexEncoded());
			return hashedMatcher;
		}
	}

	protected DataSource initializeDataSource() throws RuntimeException {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> dsc = cl.loadClass(getConfig().getDataSourceClass());
			DataSource ds = (DataSource) dsc.newInstance();
			BeanInfo bean = Introspector.getBeanInfo(dsc);
			Map<String, String> values = getConfig().getDataSourceProperties();
			for (PropertyDescriptor d : bean.getPropertyDescriptors()) {
				String value = values.get(d.getName());
				if (d.getWriteMethod() != null && value != null) {
					try {
						Class<?> type = d.getWriteMethod().getParameterTypes()[0];
						if (type == String.class) {
							d.getWriteMethod().invoke(ds, value);
						} else if (type == Boolean.TYPE) {
							d.getWriteMethod().invoke(ds, Boolean.parseBoolean(value));
						} else if (type == Integer.TYPE) {
							d.getWriteMethod().invoke(ds, Integer.parseInt(value));
						}
					} catch (Exception e) {
						throw new IllegalStateException("Setting the value of " + getConfig().getDataSourceClass() + "." + d.getName() + " to " + value + " failed.", e);
					}
				}
			}
			return ds;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to initialize DataSource " + getConfig().getDataSourceClass(), e);
		}
	}

	@Override
	public AbstractWebswingUser verifyUserPassword(String user, String password) throws WebswingAuthenticationException {
		AuthenticationInfo authtInfo;
		try {
			authtInfo = realm.getAuthenticationInfo(new UsernamePasswordToken(user, password));
		} catch (AuthenticationException e) {
			throw new WebswingAuthenticationException("Username or password is not valid!", WebswingAuthenticationException.INVALID_USER_OR_PASSWORD, e);
		}
		if (authtInfo == null) {
			throw new WebswingAuthenticationException("User not found!", WebswingAuthenticationException.INVALID_USER_OR_PASSWORD);
		}
		return new ShiroWebswingUser(realm, authtInfo);
	}

}
