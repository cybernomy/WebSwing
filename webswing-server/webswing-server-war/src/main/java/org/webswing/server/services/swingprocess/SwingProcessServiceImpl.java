package org.webswing.server.services.swingprocess;

import com.google.inject.Singleton;

@Singleton
public class SwingProcessServiceImpl implements SwingProcessService {

	@Override
	public SwingProcess create(SwingProcessConfig config) {
		return new SwingProcessImpl(config);
	}

}
