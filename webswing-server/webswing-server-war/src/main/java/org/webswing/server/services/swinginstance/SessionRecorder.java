package org.webswing.server.services.swinginstance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.model.SessionRecordingHeader;

public class SessionRecorder {
	private static final Logger log = LoggerFactory.getLogger(SessionRecorder.class);

	private boolean failed = false;
	private FileOutputStream out;
	private SessionRecordingHeader header;

	private String fileName;

	private long lastFrame;

	public SessionRecorder(SwingInstance swingInstance) {
		try {
			URI uri = URI.create(System.getProperty(Constants.TEMP_DIR_PATH) + URLEncoder.encode(swingInstance.getClientId(), "UTF-8") + ".wss");
			File file = new File(uri);
			this.fileName = file.getCanonicalPath();
			log.info("Starting session recording for " + swingInstance.getClientId() + " into file:" + file);
			if (out == null) {
				out = new FileOutputStream(file);
				header = new SessionRecordingHeader();
				header.setClientId(swingInstance.getClientId());
				header.setStartDate(new Date());
				this.lastFrame = header.getStartDate().getTime();
				byte[] version = ByteBuffer.allocate(4).putInt(SessionRecordingHeader.version).array();
				byte[] headerbytes = serializeObject(header);
				out.write(version);
				saveFrame(headerbytes);
			}
		} catch (FileNotFoundException e) {
			log.error("Failed to create session recording file.", e);
			failed = true;
		} catch (IOException e) {
			failed = true;
		}
	}

	private static byte[] serializeObject(Serializable o) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(o);
			return bos.toByteArray();
		} catch (IOException ex) {
			log.error("Failed to serialize object.", ex);
			throw ex;
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				// ignore close exception
			}
			try {
				bos.close();
			} catch (IOException e) {
				// ignore close exception
			}
		}
	}

	public void saveFrame(byte[] serialized) {
		if (!failed) {
			try {
				if (out != null) {
					byte[] length = ByteBuffer.allocate(4).putInt(serialized.length).array();
					long now = new Date().getTime();
					byte[] delay = ByteBuffer.allocate(4).putInt((int) (now - this.lastFrame)).array();
					this.lastFrame = now;
					try {
						out.write(delay);
						out.write(length);
						out.write(serialized);
						out.flush();
					} catch (IOException e) {
						log.error("Failed to write to session recording file.", e);
						throw e;
					}
				}
			} catch (Exception e) {
				failed = true;
				close();
			}
		}
	}

	public void close() {
		try {
			if (out != null) {
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			log.error("Failed to close recording file.", e);
		}
	}

	public boolean isFailed() {
		return failed;
	}

	public String getFileName() {
		return fileName;
	}

}
