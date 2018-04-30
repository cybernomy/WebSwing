package org.webswing.server.services.swingprocess;

public interface SwingProcess {

	boolean isRunning();

	void destroy(int delayMs);

	void execute() throws Exception;

	boolean isForceKilled();

	void setProcessExitListener(ProcessExitListener object);

	SwingProcessConfig getConfig();

}
