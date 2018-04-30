package org.webswing.server.services.playback;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.model.c2s.PlaybackCommandMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.ApplicationInfoMsg;
import org.webswing.model.s2c.PlaybackInfoMsg;
import org.webswing.server.model.SessionRecordingHeader;
import org.webswing.server.services.websocket.WebSocketConnection;
import org.webswing.server.util.ServerUtil;

public class SessionRecordingPlayback {
	private static final Logger log = LoggerFactory.getLogger(SessionRecordingPlayback.class);

	private FileInputStream fis = null;

	private int numberOfFrames;
	private SessionRecordingHeader header;
	private ScheduledExecutorService sender = Executors.newSingleThreadScheduledExecutor();
	private WebSocketConnection conn;

	private int currentFrame = 0;
	private int allowedFrame = 1;

	private File recordingFile;

	private boolean fastForward;

	public SessionRecordingPlayback(WebSocketConnection r, File recordingFile) {
		try {
			this.conn = r;
			this.recordingFile = recordingFile;
			fis = new FileInputStream(recordingFile);
			int version = readInt(fis);
			if (SessionRecordingHeader.version == version) {
				SessionRecordingHeader h = (SessionRecordingHeader) readObject(fis);
				this.header = h;
				while (readInt(fis) != 0) {
					int length = readInt(fis);
					fis.skip(length);
					numberOfFrames++;
				}
				resetStream(recordingFile);
				sendNextFrame();
			} else {
				throw new IOException("Version " + version + " of recording file is not supported. Current supported version is " + SessionRecordingHeader.version);
			}
		} catch (IOException e) {
			log.error("Failed to read recording file " + recordingFile.getAbsolutePath(), e);
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
				//ignore
			}
		}
	}

	private void resetStream(File recordingFile) {
		try {
			fis.close();
			fis = new FileInputStream(recordingFile);
			readInt(fis);
			readObject(fis);
		} catch (Exception e) {
			log.error("Failed to reset recording file. ", e);
		}
	}

	public synchronized void close() {
		try {
			fis.close();
		} catch (IOException e) {
			log.error("Failed to close recording file. ", e);
		}
	}

	public ApplicationInfoMsg getApplicationInfo() {
		ApplicationInfoMsg aim = new ApplicationInfoMsg();
		aim.setName(this.recordingFile.getPath());
		return aim;
	}

	private static byte[] readFrame(FileInputStream fis) throws IOException {
		int length = readInt(fis);
		if (length == 0) {
			return null;
		} else {
			byte[] bytes = new byte[length];
			if (length == fis.read(bytes)) {
				return bytes;
			} else {
				throw new IOException("Unexpected end of file. Frame expected.");
			}
		}
	}

	private static int readInt(FileInputStream fis) throws IOException {
		byte[] b = new byte[4];
		int length = fis.read(b);
		if (4 == length) {
			return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
		} else if (length < 1) {
			return 0;
		} else {
			throw new IOException("Unexpected end of file. Integer expected.");
		}
	}

	private static Object readObject(FileInputStream fis) throws IOException {
		readInt(fis);//delay
		int headerLength = readInt(fis);
		byte[] headerBytes = new byte[headerLength];
		if (headerLength == fis.read(headerBytes)) {
			ByteArrayInputStream bis = new ByteArrayInputStream(headerBytes);
			ObjectInput in = null;
			try {
				in = new ObjectInputStream(bis);
				Object o = in.readObject();
				return o;
			} catch (ClassNotFoundException e) {
				throw new IOException("Class could not be deserialized");
			} finally {
				try {
					bis.close();
				} catch (IOException ex) {
					// ignore close exception
				}
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
					// ignore close exception
				}
			}
		} else {
			throw new IOException("Unexpected end of file. Object expected.");
		}
	}

	public synchronized void sendNextFrame() {
		if (currentFrame < allowedFrame && currentFrame < numberOfFrames) {
			try {
				currentFrame++;
				int delay = readInt(fis);
				byte[] b = readFrame(fis);
				final AppFrameMsgOut frame = ServerUtil.decodePlaybackProto(b);
				PlaybackInfoMsg playback = new PlaybackInfoMsg();
				playback.setCurrent(currentFrame);
				playback.setTotal(numberOfFrames);
				frame.setPlayback(playback);
				sender.schedule(new Runnable() {

					@Override
					public void run() {
						conn.broadcastMessage(frame);
						sendNextFrame();
					}
				}, fastForward ? 0 : delay, TimeUnit.MILLISECONDS);
			} catch (IOException e) {
				log.error("Failed to send next recording frame. ", e);
			}
		} else {
			if (currentFrame >= numberOfFrames) {
				close();
			}
		}
	}

	public synchronized void handlePlaybackControl(PlaybackCommandMsgIn playback) {
		if (playback != null && playback.getCommand() != null) {
			switch (playback.getCommand()) {
			case play:
				allowedFrame = numberOfFrames;
				fastForward = false;
				break;
			case stop:
				allowedFrame = currentFrame;
				break;
			case step:
				allowedFrame = currentFrame + 1;
				fastForward = true;
				break;
			case step10:
				allowedFrame = currentFrame + 10;
				fastForward = true;
				break;
			case step100:
				allowedFrame = currentFrame + 100;
				fastForward = true;
				break;
			case reset:
				resetStream(recordingFile);
				allowedFrame = 1;
				currentFrame = 0;
				break;
			default:
				break;
			}
			sendNextFrame();
		}
	}
}
