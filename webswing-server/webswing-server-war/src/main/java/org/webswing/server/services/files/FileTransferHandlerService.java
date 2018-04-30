package org.webswing.server.services.files;

import org.webswing.server.services.swingmanager.SwingInstanceManager;

public interface FileTransferHandlerService {

	FileTransferHandler create(SwingInstanceManager manager);
}
