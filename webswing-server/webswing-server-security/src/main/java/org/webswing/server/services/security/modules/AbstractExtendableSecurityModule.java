package org.webswing.server.services.security.modules;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.LoginResponseClosedException;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.extension.api.BuiltInModuleExtensions;
import org.webswing.server.services.security.extension.api.SecurityModuleExtension;
import org.webswing.server.services.security.extension.api.SecurityModuleExtensionConfig;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;

/**
 * Adds extensions support to {@link AbstractSecurityModule}. Four extension points are provided ( See {@link SecurityModuleExtension}) prototype.
 * <p>
 * A list of extension can be registered using 
 * Security Module's JSON configuration. Extension must be one of the {@link BuiltInModuleExtensions} or a custom subclass of {@link SecurityModuleExtension}.
 * </p>
 * JSON configuration example : 
 * <pre>
 * "securityConfig" : {
 *   "securityModule" : "MyExtendableModule",
 *   "config" : {
 *     "extensions" : [ "org.webswing.MyExtension", "oneTimeUrl" ],
 *     "org.webswing.MyExtension" : {
 *       "myExtensionParam1" : "value"
 *     },
 *     "oneTimeUrl" : {
 *       "apiKeys" : {}
 *     }
 *   }
 * },
 * </pre>  
 * 
 * @param <T> configuration which extends {@link WebswingExtendableSecurityModuleConfig}
 */
public abstract class AbstractExtendableSecurityModule<T extends WebswingExtendableSecurityModuleConfig> extends AbstractSecurityModule<T> {
	private static final Logger log = LoggerFactory.getLogger(AbstractExtendableSecurityModule.class);

	private List<SecurityModuleExtension<?>> extensions = new ArrayList<>();

	public AbstractExtendableSecurityModule(T config) {
		super(config);
	}

	@Override
	public void init() {
		super.init();
		if (getConfig().getExtensions() != null) {
			for (String extensionName : getConfig().getExtensions()) {
				SecurityModuleExtension<?> extension = null;
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				try {
					Class<?> extensionClass = cl.loadClass(BuiltInModuleExtensions.getExtensionClassName(extensionName));
					Constructor<?> defaultConstructor = null;
					Constructor<?> configConstructor = null;
					for (Constructor<?> constructor : extensionClass.getConstructors()) {
						Class<?>[] parameterTypes = constructor.getParameterTypes();
						if (parameterTypes.length == 1) {
							if (SecurityModuleExtensionConfig.class.isAssignableFrom(parameterTypes[0])) {
								configConstructor = constructor;
								break;
							}
						} else if (parameterTypes.length == 0) {
							defaultConstructor = constructor;
						}
					}

					if (configConstructor != null) {
						Class<?> configClass = configConstructor.getParameterTypes()[0];
						try {
							extension = (SecurityModuleExtension<?>) configConstructor.newInstance(getConfig().getValueAs(extensionName, configClass));
						} catch (Exception e) {
							log.error("Could not construct security module extension class (using SecurityModuleExtensionConfig constructor).", e);
						}
					}
					if (extension == null && defaultConstructor != null) {
						try {
							extension = (SecurityModuleExtension<?>) defaultConstructor.newInstance();
						} catch (Exception e) {
							log.error("Could not construct security module extension class (using Default constructor).", e);
						}
					}
					if (extension != null) {
						extensions.add(extension);
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Failed to load Security module extensions.", e);
				}
			}
		}
	}

	@Override
	public AbstractWebswingUser doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		for (SecurityModuleExtension<?> extension : extensions) {
			try {
				AbstractWebswingUser result = extension.doSufficientPreValidation(this, request, response);
				if (result != null) {
					onAuthenticationSuccess(result, request, response);
					return result;
				} else {
					continue;
				}
			} catch (WebswingAuthenticationException e) {
				log.error("Extension failed to authenticate:", e);
				continue;
			} catch (LoginResponseClosedException e) {
				return null;
			}
		}

		return super.doLogin(request, response);
	}

	@Override
	protected void preVerify(HttpServletRequest request, HttpServletResponse response) throws WebswingAuthenticationException, LoginResponseClosedException {
		for (SecurityModuleExtension<?> extension : extensions) {
			try {
				extension.doRequiredPreValidation(this, request, response);
			} catch (WebswingAuthenticationException e) {
				throw e;
			} catch (LoginResponseClosedException e) {
				throw e;
			}
		}
	}

	@Override
	protected void postVerify(AbstractWebswingUser user, HttpServletRequest request, HttpServletResponse response) throws LoginResponseClosedException, WebswingAuthenticationException {
		for (SecurityModuleExtension<?> extension : extensions) {
			try {
				extension.doRequiredPostValidation(this, user, request, response);
			} catch (WebswingAuthenticationException e) {
				throw e;
			} catch (LoginResponseClosedException e) {
				throw e;
			}
		}
	}

	@Override
	protected AbstractWebswingUser decorateUser(AbstractWebswingUser user, HttpServletRequest request, HttpServletResponse response) {
		for (SecurityModuleExtension<?> extension : extensions) {
			user = extension.decorateUser(user, request, response);
		}
		return user;
	}

	@Override
	protected void serveAuthenticated(AbstractWebswingUser user, String path, HttpServletRequest req, HttpServletResponse res) {
		for (SecurityModuleExtension<?> extension : extensions) {
			boolean served = extension.serveAuthenticated(user, path, req, res);
			if (served) {
				break;
			}
		}
	}
}
