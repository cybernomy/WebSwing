package org.webswing.server.services.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.c2s.UploadEventMsgIn;
import org.webswing.server.base.AbstractUrlHandler;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.swinginstance.SwingInstance;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.util.ServerUtil;

public class FileTransferHandlerImpl extends AbstractUrlHandler implements FileTransferHandler {

	private static final Logger log = LoggerFactory.getLogger(FileTransferHandlerImpl.class);
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	private final SwingInstanceManager manager;
	private Map<String, FileDescriptor> fileMap = new ConcurrentHashMap<String, FileDescriptor>();
	private ScheduledExecutorService validatorService = Executors.newSingleThreadScheduledExecutor();

	public FileTransferHandlerImpl(SwingInstanceManager parent) {
		super(parent);
		this.manager = parent;
	}

	@Override
	protected String getPath() {
		return "file";
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		try {
			if (req.getMethod().equals("GET")) {
				handleDownload(req, res);
				return true;
			} else if (req.getMethod().equals("POST")) {
				handleUpload(req, res);
				return true;
			}
		} catch (Exception e) {
			log.error("FileTransfer failed.", e);
			throw new WsException("Failed to process file transfer " + req.getMethod(), e);
		}
		return false;
	}

	private void handleDownload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, WsException {
		String fileId = request.getParameter("id");
		String userId = getUser() != null ? getUser().getUserId() : "null";

		checkPermission(WebswingAction.file_download);

		if (!fileMap.containsKey(fileId) || fileMap.get(fileId).file == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		if (!userId.equals(fileMap.get(fileId).userId)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN); // 403.
			return;
		}

		FileDescriptor fd = fileMap.get(fileId);
		synchronized (fd) {
			if (fd.waitForFile) {
				try {
					fd.wait(Long.getLong(Constants.FILE_SERVLET_WAIT_TIMEOUT, 300000));
				} catch (InterruptedException e) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
					fd.waitForFileTask.cancel(false);
					return;
				}
			}
		}

		if (fileMap.get(fileId) == null || !fileMap.get(fileId).file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		File file = fd.file;
		response.reset();
		response.setBufferSize(DEFAULT_BUFFER_SIZE);
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Length", String.valueOf(file.length()));
		response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		BufferedInputStream input = null;
		BufferedOutputStream output = null;

		try {
			input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
			output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
		} finally {
			close(output);
			close(input);
		}
	}

	private void handleUpload(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException, WsException {
		checkPermission(WebswingAction.file_upload);

		try {
			String uuid = request.getParameter("uuid");
			if (uuid != null) {
				SwingInstance instance = manager.findInstanceBySessionId(uuid);
				if (instance != null) {
					double maxMB = instance.getAppConfig().getUploadMaxSize();
					long maxsize = (long) (maxMB * 1024 * 1024);
					Part filePart = request.getPart("files[]"); // Retrieves <input type="file" name="file">
					String filename = getFilename(filePart);
					if (maxsize > 0 && filePart.getSize() > maxsize) {
						resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						resp.getWriter().write(String.format("File '%s' is too large. (Max. file size is %.1fMB)", filename, maxMB));
					} else {
						String tempDir = System.getProperty(Constants.TEMP_DIR_PATH);
						String tempName = UUID.randomUUID().toString();
						InputStream filecontent = filePart.getInputStream();
						File f = new File(URI.create(tempDir + "/" + tempName));
						FileOutputStream output = new FileOutputStream(f);
						IOUtils.copy(filecontent, output);
						output.close();
						filecontent.close();
						log.info("File " + filename + " uploaded (size:" + filePart.getSize() + ") to " + f.getAbsolutePath());
						UploadEventMsgIn msg = new UploadEventMsgIn();
						msg.setFileName(filename);
						msg.setTempFileLocation(f.getAbsolutePath());
						boolean sent = instance.sendToSwing(null, msg);
						if (!sent) {
							log.error("Failed to send upload notification to app session. File:" + filename + "+ClientID:" + instance.getClientId());
							f.delete();
						} else {
							resp.getWriter().write("{\"files\":[{\"name\":\"" + filename + "\"}]}"); // TODO size
						}
					}
				} else {
					throw new Exception("Related App instance not found.(" + uuid + ")");
				}
			} else {
				throw new Exception("UUID not specified in request");
			}
		} catch (Exception e) {
			if (e.getCause() instanceof EOFException) {
				log.warn("File upload canceled by user: " + e.getMessage());
			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().write("Upload finished with error...");
				log.error("Error while uploading file: " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void destroy() {
		validatorService.shutdownNow();
		super.destroy();
	}

	@Override
	public void init() {
		validatorService = Executors.newSingleThreadScheduledExecutor();
		super.init();
	}

	private void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getFilename(Part part) {
		for (String cd : part.getHeader("Content-Disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
			}
		}
		return null;
	}

	public boolean registerFile(File file, final String id, long validForTime, TimeUnit timeUnit, String validForUser, String instanceId, boolean waitForFile, String overwriteDetails) {
		return registerFileInternal(file, id, validForTime, timeUnit, validForUser, instanceId, false, waitForFile, overwriteDetails);
	}

	private boolean registerFileInternal(File file, final String id, long validForTime, TimeUnit timeUnit, String validForUser, String instanceId, boolean temp, boolean waitForFile, String overwriteDetails) {
		final FileDescriptor fd = new FileDescriptor(file, validForUser);
		fd.temporary = temp;
		fd.waitForFile = waitForFile;
		fd.overwriteDetails = overwriteDetails;
		fd.instanceId = instanceId;
		//if file download was triggered by Desktop.open() or similar method and there is already a autoDownload thread waiting for the same file, avoid double downloading same file by canceling the waiting thread
		if (notifyWaitingForSameFile(fd)) {
			return false;
		}
		fileMap.put(id, fd);
		if (validForTime > 0) {
			Future<?> invalidateTask = validatorService.schedule(new Runnable() {

				@Override
				public void run() {
					fileMap.remove(id);
					if (fd.temporary) {
						fd.file.delete();
					}
				}
			}, validForTime, timeUnit);
			fd.invalidateScheduleTask = invalidateTask;
		}
		if (waitForFile) {
			fd.waitForFileTask = validatorService.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					synchronized (fd) {
						if (fd.file.exists()) {
							String details = fd.file.length() + "|" + fd.file.lastModified();
							if (!details.equals(fd.overwriteDetails)) {
								if (details.equals(fd.lastFileAttributes)) {
									if (!ServerUtil.isFileLocked(fd.file)) {
										fd.waitForFile = false;
										fd.notifyAll();
										fd.waitForFileTask.cancel(false);
									}
								} else {
									fd.lastFileAttributes = details;
								}
							}
						}
					}
				}
			}, 1, 1, TimeUnit.SECONDS);
		}
		return true;
	}

	private boolean notifyWaitingForSameFile(FileDescriptor newFd) {
		for (String id : fileMap.keySet()) {
			FileDescriptor fd = fileMap.get(id);
			if (fd.instanceId.equals(newFd.instanceId) && fd.userId.equals(newFd.userId) && fd.file.equals(newFd.file) && fd.waitForFile) {
				synchronized (fd) {
					fd.waitForFile = false;
					fd.notifyAll();
					fd.waitForFileTask.cancel(false);
				}
				return true;
			}
		}
		return false;
	}
}
