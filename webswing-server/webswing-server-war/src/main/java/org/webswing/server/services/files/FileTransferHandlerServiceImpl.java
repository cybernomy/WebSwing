package org.webswing.server.services.files;

import org.webswing.server.services.swingmanager.SwingInstanceManager;

import com.google.inject.Singleton;

@Singleton
public class FileTransferHandlerServiceImpl implements FileTransferHandlerService {

	public FileTransferHandler create(SwingInstanceManager manager) {
		return new FileTransferHandlerImpl(manager);
	}
}
