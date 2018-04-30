package org.webswing.server.services.resources;

import org.webswing.server.base.UrlHandler;
import org.webswing.server.services.security.api.SecurityContext;

public interface ResourceHandlerService {
	ResourceHandler create(UrlHandler manager, SecurityContext context);
}
