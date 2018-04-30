package org.webswing.server.services.swinginstance;

import main.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.MsgIn;
import org.webswing.model.MsgInternal;
import org.webswing.model.MsgOut;
import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.model.c2s.ParamMsg;
import org.webswing.model.c2s.SimpleEventMsgIn;
import org.webswing.model.c2s.SimpleEventMsgIn.SimpleEventType;
import org.webswing.model.c2s.TimestampsMsgIn;
import org.webswing.model.internal.*;
import org.webswing.model.internal.ApiEventMsgInternal.ApiEventType;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.CursorChangeEventMsg;
import org.webswing.model.s2c.LinkActionMsg;
import org.webswing.model.s2c.LinkActionMsg.LinkActionType;
import org.webswing.model.s2c.SimpleEventMsgOut;
import org.webswing.server.common.model.AppletLauncherConfig;
import org.webswing.server.common.model.DesktopLauncherConfig;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.common.model.SwingConfig.LauncherType;
import org.webswing.server.common.model.admin.SwingInstanceStatus;
import org.webswing.server.common.model.admin.SwingSession;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.VariableSubstitutor;
import org.webswing.server.model.EncodedMessage;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.files.FileTransferHandler;
import org.webswing.server.services.jvmconnection.JvmConnection;
import org.webswing.server.services.jvmconnection.JvmConnectionService;
import org.webswing.server.services.jvmconnection.JvmListener;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.modules.AbstractSecurityModule;
import org.webswing.server.services.stats.StatisticsLogger;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.services.swingprocess.ProcessExitListener;
import org.webswing.server.services.swingprocess.SwingProcess;
import org.webswing.server.services.swingprocess.SwingProcessConfig;
import org.webswing.server.services.swingprocess.SwingProcessService;
import org.webswing.server.services.websocket.WebSocketConnection;
import org.webswing.server.util.FontUtils;
import org.webswing.server.util.ServerUtil;
import org.webswing.toolkit.api.WebswingApi;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SwingInstanceImpl implements SwingInstance, JvmListener {
	private static final String LAUNCHER_CONFIG = "launcherConfig";
	private static final String WEB_TOOLKIT_CLASS_NAME = "org.webswing.toolkit.WebToolkit";
	private static final String WEB_GRAPHICS_ENV_CLASS_NAME = "org.webswing.toolkit.ge.WebGraphicsEnvironment";
	private static final String WEB_PRINTER_JOB_CLASS_NAME = "org.webswing.toolkit.WebPrinterJobWrapper";
	private static final String WIN_SHELL_FOLDER_MANAGER = "sun.awt.shell.Win32ShellFolderManager2";
	private static final String JAVA_FX_PATH = System.getProperty("java.home") + "/lib/ext/jfxrt.jar";
	private static final String JAVA_FX_TOOLKIT_CLASS_NAME = "org.webswing.javafx.ToolkitJarMarker";

	private static final Logger log = LoggerFactory.getLogger(SwingInstance.class);

	private final SwingInstanceManager manager;
	private final FileTransferHandler fileHandler;
	private final String instanceId;
	private final String clientId;
	private String clientIp;
	private AbstractWebswingUser user;
	private SwingProcess process;
	private JvmConnection jvmConnection;
	private SessionRecorder sessionRecorder;
	private WebSocketConnection webConnection;
	private WebSocketConnection mirroredWebConnection;
	private SwingConfig config;
	private Date disconnectedSince;
	private final Date startedAt = new Date();
	private final String queueId;
	private String customArgs = "";
	private int debugPort = 0;
	private String locale = null;
	private String userIp = null;
	private String userOs = null;

	private String userBrowser = null;
	//finished instances only
	private Date endedAt = null;
	private List<String> warningHistoryLog;
	private Map<Long, ThreadDumpMsgInternal> threadDumps = new ConcurrentHashMap<>();

	public SwingInstanceImpl(SwingInstanceManager manager, FileTransferHandler fileHandler, SwingProcessService processService, JvmConnectionService connectionService, ConnectionHandshakeMsgIn h, SwingConfig config, WebSocketConnection websocket) throws WsException {
		this.manager = manager;
		this.fileHandler = fileHandler;
		this.webConnection = websocket;
		this.instanceId = ServerUtil.resolveInstanceIdForMode(websocket, h, config);
		this.config = config;
		this.user = websocket.getUser();
		this.clientId = h.getClientId();
		this.customArgs = ServerUtil.getCustomArgs(websocket.getRequest());
		this.debugPort = ServerUtil.getDebugPort(websocket.getRequest());
		this.clientIp = ServerUtil.getClientIp(websocket);
		this.queueId = user.getUserId() + "-" + config.getName() + "-" + startedAt.getTime();
		updateUser(websocket);
		try {
			this.jvmConnection = connectionService.connect(this.queueId, this);
			process = start(processService, config, h);
			notifyUserConnected();
		} catch (Exception e) {
			notifyExiting();
			throw new WsException("Failed to create App instance.", e);
		}
		this.sessionRecorder = ServerUtil.isRecording(websocket.getRequest()) ? new SessionRecorder(this) : null;
		logStatValue(StatisticsLogger.WEBSOCKET_CONNECTED, websocket.isWebsocketTransport() ? 1 : 2);
	}

	public void connectSwingInstance(WebSocketConnection r, ConnectionHandshakeMsgIn h) {
		if (h.isMirrored()) {// connect as mirror viewer
			connectMirroredWebSession(r);
		} else {// continue old session?
			if (h.getSessionId() != null && h.getSessionId().equals(getSessionId())) {
				sendToSwing(r, h);
			} else {
				boolean result = connectPrimaryWebSession(r);
				if (result) {
					r.broadcastMessage(SimpleEventMsgOut.continueOldSession.buildMsgOut());
				} else {
					r.broadcastMessage(SimpleEventMsgOut.applicationAlreadyRunning.buildMsgOut());
				}
			}
		}
	}

	private boolean connectPrimaryWebSession(WebSocketConnection resource) {
		if (resource != null) {
			if (this.webConnection != null && config.isAllowStealSession()) {
				synchronized (this.webConnection) {
					this.webConnection.broadcastMessage(SimpleEventMsgOut.sessionStolenNotification.buildMsgOut());
				}
				notifyUserDisconnected();
				this.webConnection = null;
			}
			if (this.webConnection == null) {
				this.webConnection = resource;
				updateUser(resource);
				logStatValue(StatisticsLogger.WEBSOCKET_CONNECTED, resource.isWebsocketTransport() ? 1 : 2);
				this.disconnectedSince = null;
				notifyUserConnected();
				return true;
			}
		}
		return false;

	}

	private void updateUser(WebSocketConnection resource) {
		userIp = ServerUtil.getClientIp(webConnection);
		userOs = ServerUtil.getClientOs(webConnection);
		userBrowser = ServerUtil.getClientBrowser(webConnection);
	}

	private void disconnectPrimaryWebSession() {
		if (this.webConnection != null) {
			notifyUserDisconnected();
			this.webConnection = null;
			this.disconnectedSince = new Date();
			logStatValue(StatisticsLogger.WEBSOCKET_CONNECTED, 0);
		}
	}

	public void shutdown(boolean force) {
		if (force) {
			kill(0);
		} else {
			SimpleEventMsgIn simpleEventMsgIn = new SimpleEventMsgIn();
			simpleEventMsgIn.setType(SimpleEventType.killSwing);
			sendToSwing(null, simpleEventMsgIn);
		}
	}

	private void connectMirroredWebSession(WebSocketConnection resource) {
		if (resource != null) {
			if (this.mirroredWebConnection != null) {
				synchronized (this.mirroredWebConnection) {
					this.mirroredWebConnection.broadcastMessage(SimpleEventMsgOut.sessionStolenNotification.buildMsgOut());
				}
				notifyMirrorViewDisconnected();
			}
			this.mirroredWebConnection = resource;
			notifyMirrorViewConnected();
		}
	}

	private void disconnectMirroredWebSession() {
		if (this.mirroredWebConnection != null) {
			notifyMirrorViewDisconnected();
			this.mirroredWebConnection = null;
		}
	}

	public void sendToWeb(MsgOut o) {
		EncodedMessage serialized = new EncodedMessage(o);
		if (sessionRecorder != null) {
			sessionRecorder.saveFrame(serialized.getProtoMessage());
		}
		if (webConnection != null) {
			synchronized (webConnection) {
				webConnection.broadcastMessage(serialized);
				int length = serialized.getLength(webConnection.isBinary());
				logStatValue(StatisticsLogger.OUTBOUND_SIZE_METRIC, length);
			}
		}
		if (mirroredWebConnection != null) {
			synchronized (mirroredWebConnection) {
				mirroredWebConnection.broadcastMessage(serialized);
			}
		}
	}

	public boolean sendToSwing(WebSocketConnection r, MsgIn h) {
		if (isRunning()) {
			if (h instanceof SimpleEventMsgIn) {
				SimpleEventMsgIn m = (SimpleEventMsgIn) h;
				if (m.getType().equals(SimpleEventMsgIn.SimpleEventType.paintAck)) {
					if (((webConnection != null && r.uuid().equals(webConnection.uuid())) || (webConnection == null && mirroredWebConnection != null && r.uuid().equals(mirroredWebConnection.uuid())))) {
						jvmConnection.send(h);
					}
				} else if (m.getType().equals(SimpleEventMsgIn.SimpleEventType.unload)) {
					if (webConnection != null && r.uuid().equals(webConnection.uuid())) {
						jvmConnection.send(h);
					}
					disconnectPrimaryWebSession();
					disconnectMirroredWebSession();
				} else {
					jvmConnection.send(h);
				}
			} else if (h instanceof TimestampsMsgIn) {
				processTimestampMessage((TimestampsMsgIn) h);
				jvmConnection.send(h);
			} else {
				jvmConnection.send(h);
			}
			return true;
		} else {
			return false;
		}
	}

	private void processTimestampMessage(TimestampsMsgIn h) {
		if (StringUtils.isNotEmpty(h.getSendTimestamp())) {
			long currentTime = System.currentTimeMillis();
			long sendTime = Long.parseLong(h.getSendTimestamp());
			if (StringUtils.isNotEmpty(h.getRenderingTime()) && StringUtils.isNotEmpty(h.getStartTimestamp())) {
				long renderingTime = Long.parseLong(h.getRenderingTime());
				long startTime = Long.parseLong(h.getStartTimestamp());
				logStatValue(StatisticsLogger.LATENCY_SERVER_RENDERING, sendTime - startTime);
				logStatValue(StatisticsLogger.LATENCY_CLIENT_RENDERING, renderingTime);
				logStatValue(StatisticsLogger.LATENCY, currentTime - startTime);
				logStatValue(StatisticsLogger.LATENCY_NETWORK_TRANSFER, currentTime - sendTime - renderingTime);
			}
		}
		if (h.getPing() > 0) {
			logStatValue(StatisticsLogger.LATENCY_PING, h.getPing());
		}
	}

	@Override
	public void onJvmMessage(Serializable o) {
		if (o instanceof MsgInternal) {
			if (o instanceof ApiCallMsgInternal) {
				ApiCallMsgInternal query = (ApiCallMsgInternal) o;
				AbstractWebswingUser currentUser = webConnection != null ? webConnection.getUser() : null;
				Serializable result;
				switch (query.getMethod()) {
				case HasRole:
					if (currentUser == null) {
						query.setResult(null);
					} else {
						result = currentUser.hasRole((String) query.getArgs()[0]);
						query.setResult(result);
					}
					jvmConnection.send(query);
					break;
				case IsPermitted:
					if (currentUser == null) {
						query.setResult(null);
					} else {
						result = currentUser.isPermitted((String) query.getArgs()[0]);
						query.setResult(result);
					}
					jvmConnection.send(query);
					break;
				default:
					break;
				}
			} else if (o instanceof PrinterJobResultMsgInternal) {
				PrinterJobResultMsgInternal pj = (PrinterJobResultMsgInternal) o;
				boolean success = fileHandler.registerFile(pj.getPdfFile(), pj.getId(), 30, TimeUnit.MINUTES, getUser(), getInstanceId(), false, null);
				if (success) {
					AppFrameMsgOut f = new AppFrameMsgOut();
					LinkActionMsg linkAction = new LinkActionMsg(LinkActionType.print, pj.getId());
					f.setLinkAction(linkAction);
					sendToWeb(f);
				}
			} else if (o instanceof OpenFileResultMsgInternal) {
				OpenFileResultMsgInternal fr = (OpenFileResultMsgInternal) o;
				String id = UUID.randomUUID().toString();
				boolean success = fileHandler.registerFile(fr.getFile(), id, 30, TimeUnit.MINUTES, getUser(), getInstanceId(), fr.isWaitForFile(), fr.getOverwriteDetails());
				if (success) {
					AppFrameMsgOut f = new AppFrameMsgOut();
					LinkActionMsg linkAction = new LinkActionMsg(LinkActionType.file, id);
					f.setLinkAction(linkAction);
					sendToWeb(f);
				}
			} else if (o instanceof JvmStatsMsgInternal) {
				JvmStatsMsgInternal s = (JvmStatsMsgInternal) o;
				logStatValue(StatisticsLogger.MEMORY_ALLOCATED_METRIC, s.getHeapSize());
				logStatValue(StatisticsLogger.MEMORY_USED_METRIC, s.getHeapSizeUsed());
				logStatValue(StatisticsLogger.CPU_UTIL_METRIC, s.getCpuUsage());
				logStatValue(StatisticsLogger.EDT_BLOCKED_SEC_METRIC, s.getEdtPingSeconds());
				if(getAppConfig().isMonitorEdtEnabled()){
					if(s.getEdtPingSeconds()>2){
						sendToWeb(SimpleEventMsgOut.applicationBusy.buildMsgOut());
					}
				}
			} else if (o instanceof ExitMsgInternal) {
				close();
				ExitMsgInternal e = (ExitMsgInternal) o;
				kill(e.getWaitForExit());
			} else if (o instanceof ThreadDumpMsgInternal) {
				ThreadDumpMsgInternal e = (ThreadDumpMsgInternal) o;
				threadDumps.put(e.getTimestamp(), e);
			}
		} else if (o instanceof AppFrameMsgOut && ((AppFrameMsgOut) o).getCursorChange() != null) {
			CursorChangeEventMsg cmsg = ((AppFrameMsgOut) o).getCursorChange();
			if (cmsg.getCurFile() != null) {
				File cur = new File(cmsg.getCurFile());
				boolean success = fileHandler.registerFile(cur, cur.getName(), 1, TimeUnit.DAYS, getUser(), getInstanceId(), false, null);
				cmsg.setCurFile(cur.getName());
			}
			sendToWeb((MsgOut) o);
		} else if (o instanceof MsgOut) {
			sendToWeb((MsgOut) o);
		}
	}

	private void close() {
		if (config.isAutoLogout()) {
			sendToWeb(SimpleEventMsgOut.shutDownAutoLogoutNotification.buildMsgOut());
		}
		if (StringUtils.isNotBlank(config.getGoodbyeUrl())) {
			VariableSubstitutor subs = VariableSubstitutor.forSwingInstance(manager.getConfig(), user.getUserId(), user.getUserAttributes(), getClientId(), clientIp, locale, customArgs);
			String url = subs.replace(config.getGoodbyeUrl());
			if (url.startsWith("/")) {
				url = AbstractSecurityModule.getContextPath(manager.getServletContext()) + url;
			}
			AppFrameMsgOut result = new AppFrameMsgOut();
			result.setLinkAction(new LinkActionMsg(LinkActionType.redirect, url));
			sendToWeb(result);
		} else {
			sendToWeb(SimpleEventMsgOut.shutDownNotification.buildMsgOut());
		}
		jvmConnection.close();
		notifyExiting();

		if (process != null && config.isIsolatedFs() && config.isClearTransferDir()) {
			String transferDir = process.getConfig().getProperties().get(Constants.SWING_START_SYS_PROP_TRANSFER_DIR);
			try {
				if (transferDir.indexOf(File.pathSeparator) != -1) {
					throw new IOException("Can not clear upload folder if multiple roots are defined. Turn off the option in Webswing config. [" + transferDir + "]");
				} else if (transferDir != null) {
					FileUtils.deleteDirectory(new File(transferDir));
				}
			} catch (IOException e) {
				log.error("Failed to delete transfer dir " + transferDir, e);
			}
		}
	}

	public void notifyExiting() {
		endedAt = new Date();
		if (isRunning()) {
			process.setProcessExitListener(null);
		}
		if (sessionRecorder != null) {
			sessionRecorder.close();
		}
		manager.notifySwingClose(this);
	}

	@Override
	public void startRecording() {
		if (sessionRecorder == null) {
			sessionRecorder = new SessionRecorder(this);
			sendToSwing(webConnection, new SimpleEventMsgIn(SimpleEventType.repaint));
		}
	}

	public SwingSession toSwingSession(boolean stats) {
		SwingSession session = new SwingSession();
		session.setId(getInstanceId());
		session.setApplet(LauncherType.Applet.equals(getAppConfig().getLauncherType()));
		session.setApplication(getAppConfig().getName());
		session.setConnected(getSessionId() != null);
		if (!session.getConnected()) {
			session.setDisconnectedSince(getDisconnectedSince());
		}
		session.setStartedAt(getStartedAt());
		session.setUser(getUser());
		session.setUserIp(userIp);
		session.setUserOs(userOs);
		session.setUserBrowser(userBrowser);
		session.setEndedAt(getEndedAt());
		session.setStatus(getStatus());
		if (stats) {
			session.setStats(manager.getInstanceStats(getClientId()));
		}
		session.setMetrics(manager.getInstanceMetrics(getClientId()));
		session.setWarnings(manager.getInstanceWarnings(getClientId()));
		if (isRunning()) {
			session.setWarningHistory(manager.getInstanceWarningHistory(getClientId()));
		} else {
			session.setWarningHistory(warningHistoryLog);
		}
		session.setRecorded(isRecording());
		session.setRecordingFile(getRecordingFile());
		session.setThreadDumps(toMap(threadDumps));

		return session;
	}

	public void kill(int delayMs) {
		if (process != null) {
			process.destroy(delayMs);
		}
	}

	private SwingProcess start(SwingProcessService processService, final SwingConfig appConfig, final ConnectionHandshakeMsgIn handshake) throws Exception {
		this.locale = handshake.getLocale();
		final Integer screenWidth = handshake.getDesktopWidth();
		final Integer screenHeight = handshake.getDesktopHeight();
		final VariableSubstitutor subs = VariableSubstitutor.forSwingInstance(manager.getConfig(), user.getUserId(), user.getUserAttributes(), getClientId(), clientIp, locale, customArgs);
		SwingProcess swing = null;
		try {
			SwingProcessConfig swingConfig = new SwingProcessConfig();
			swingConfig.setName(getClientId());
			String java = getAbsolutePath(subs.replace(appConfig.getJreExecutable()), false);
			swingConfig.setJreExecutable(java);
			String homeDir = getAbsolutePath(subs.replace(appConfig.getUserDir()), true);
			swingConfig.setBaseDir(homeDir);
			swingConfig.setMainClass(Main.class.getName());
			swingConfig.setClassPath(new File(URI.create(CommonUtil.getWarFileLocation())).getAbsolutePath());
			String javaVersion = subs.replace(appConfig.getJavaVersion());
			boolean useJFX = config.isJavaFx();
			if (!new File(JAVA_FX_PATH).exists()) {
				log.warn("Java FX not supported with current java version (Try version 1.8). JavaFx library not found in '" + new File(JAVA_FX_PATH).getCanonicalPath() + "'. ");
				useJFX = false;
			}
			String webToolkitClass = WEB_TOOLKIT_CLASS_NAME;
			String webGraphicsEnvClass = WEB_GRAPHICS_ENV_CLASS_NAME;
			if (javaVersion.startsWith("1.7")) {
				webToolkitClass += "7";
				webGraphicsEnvClass += "7";
			} else if (javaVersion.startsWith("1.8")) {
				webToolkitClass += "8";
				webGraphicsEnvClass += "8";
			} else {
				log.error("Java version " + javaVersion + " not supported in this version of Webswing.");
				throw new RuntimeException("Java version not supported. (Version starting with 1.7 and 1.8 are supported.)");
			}
			String webSwingToolkitApiJarPath = CommonUtil.getBootClassPathForClass(WebswingApi.class.getName());
			String webSwingToolkitJarPath = CommonUtil.getBootClassPathForClass(WEB_TOOLKIT_CLASS_NAME);
			String webSwingToolkitJarPathSpecific = CommonUtil.getBootClassPathForClass(webToolkitClass);
			String rtWinShellJarPath = System.getProperty("os.name", "").startsWith("Windows") ? "" : (File.pathSeparator + CommonUtil.getBootClassPathForClass(WIN_SHELL_FOLDER_MANAGER));

			String bootCp = "-Xbootclasspath/a:" + webSwingToolkitApiJarPath + File.pathSeparatorChar + webSwingToolkitJarPathSpecific + File.pathSeparatorChar + webSwingToolkitJarPath + rtWinShellJarPath;

			if (useJFX) {
				bootCp += File.pathSeparator + CommonUtil.getBootClassPathForClass(JAVA_FX_TOOLKIT_CLASS_NAME) + File.pathSeparator + "\"" + new File(JAVA_FX_PATH).getCanonicalPath() + "\"";
			}

			String debug = appConfig.isDebug() && (debugPort != 0) ? " -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=" + debugPort + ",server=y,suspend=y " : "";
			String vmArgs = appConfig.getVmArgs() == null ? "" : subs.replace(appConfig.getVmArgs());
			swingConfig.setJvmArgs(bootCp + debug + " -noverify " + vmArgs);
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_CLIENT_ID, getClientId());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JMS_ID, this.queueId);
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APP_HOME, getAbsolutePath(".", false));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_CLASS_PATH, subs.replace(CommonUtil.generateClassPathString(appConfig.getClassPathEntries())));
			swingConfig.addProperty(Constants.TEMP_DIR_PATH, System.getProperty(Constants.TEMP_DIR_PATH));
			swingConfig.addProperty(Constants.JMS_URL, System.getProperty(Constants.JMS_URL, Constants.JMS_URL_DEFAULT));

			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_THEME, subs.replace(appConfig.getTheme()));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ISOLATED_FS, appConfig.isIsolatedFs());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSFER_DIR, getAbsolutePaths(subs.replace(appConfig.getTransferDir()), false));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_DOWNLOAD, appConfig.isAllowDownload());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_AUTO_DOWNLOAD, appConfig.isAllowAutoDownload());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_UPLOAD, appConfig.isAllowUpload());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_OPEN, appConfig.isTransparentFileOpen());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_SAVE, appConfig.isTransparentFileSave());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_DELETE, appConfig.isAllowDelete());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_LOCAL_CLIPBOARD, appConfig.isAllowLocalClipboard());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_JSLINK, appConfig.isAllowJsLink());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_INITIAL_URL, handshake.getUrl());

			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW, appConfig.isDirectdraw());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW_SUPPORTED, handshake.isDirectDrawSupported());
			swingConfig.addProperty(Constants.SWING_SESSION_TIMEOUT_SEC, appConfig.getSwingSessionTimeout());
			swingConfig.addProperty(Constants.SWING_SESSION_TIMEOUT_IF_INACTIVE, appConfig.isTimeoutIfInactive());
			swingConfig.addProperty("awt.toolkit", webToolkitClass);
			swingConfig.addProperty("java.awt.headless", false);
			swingConfig.addProperty("java.awt.graphicsenv", webGraphicsEnvClass);
			swingConfig.addProperty("java.awt.printerjob", WEB_PRINTER_JOB_CLASS_NAME);
			swingConfig.addProperty(Constants.PRINTER_JOB_CLASS, appConfig.isAllowServerPrinting() ? PrinterJob.getPrinterJob().getClass().getCanonicalName() : "org.webswing.toolkit.WebPrinterJob");
			swingConfig.addProperty("sun.awt.fontconfig", FontUtils.createFontConfiguration(appConfig, subs));
			swingConfig.addProperty(Constants.SWING_SCREEN_WIDTH, ((screenWidth == null) ? Constants.SWING_SCREEN_WIDTH_MIN : screenWidth));
			swingConfig.addProperty(Constants.SWING_SCREEN_HEIGHT, ((screenHeight == null) ? Constants.SWING_SCREEN_HEIGHT_MIN : screenHeight));

			if (useJFX) {
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JFX_TOOLKIT, Constants.SWING_START_SYS_PROP_JFX_TOOLKIT_WEB);
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JFX_PRISM, "web");//PrismSettings
				swingConfig.addProperty("prism.text", "t2k");//PrismFontFactory
				swingConfig.addProperty("prism.lcdtext", "false");//PrismFontFactory
				swingConfig.addProperty("javafx.live.resize", "false");//QuantumToolkit
			}

			switch (appConfig.getLauncherType()) {
			case Applet:
				AppletLauncherConfig applet = appConfig.getValueAs(LAUNCHER_CONFIG, AppletLauncherConfig.class);
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APPLET_DOCUMENT_BASE, handshake.getDocumentBase());
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APPLET_CLASS, applet.getAppletClass());
				for (String key : applet.getParameters().keySet()) {
					swingConfig.addProperty(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX + subs.replace(key), subs.replace(applet.getParameters().get(key)));
				}
				if (handshake.getParams() != null) {
					for (ParamMsg p : handshake.getParams()) {
						swingConfig.addProperty(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX + p.getName(), p.getValue());
					}
				}
				break;
			case Desktop:
				DesktopLauncherConfig desktop = appConfig.getValueAs(LAUNCHER_CONFIG, DesktopLauncherConfig.class);
				swingConfig.setArgs(subs.replace(desktop.getArgs()));
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_MAIN_CLASS, subs.replace(desktop.getMainClass()));
				break;
			default:
				throw new IllegalStateException("Launcher type not recognized.");
			}

			swing = processService.create(swingConfig);
			swing.execute();
			swing.setProcessExitListener(new ProcessExitListener() {

				@Override
				public void onClose() {
					close();
				}
			});
		} catch (Exception e1) {
			close();
			throw new Exception(e1);
		}
		return swing;
	}

	private String getAbsolutePaths(String paths, boolean b) throws IOException {
		String result = "";
		for (String s : paths.split(File.pathSeparator)) {
			result += getAbsolutePath(s, b) + File.pathSeparator;
		}
		return result.substring(0, Math.max(0, result.length() - 1));
	}

	private String getAbsolutePath(String path, boolean create) throws IOException {
		if (StringUtils.isBlank(path)) {
			path = ".";
		}
		File f = manager.resolveFile(path);
		if (f == null || !f.exists()) {
			path = path.replaceAll("\\\\", "/");
			String[] pathSegs = path.split("/");
			boolean absolute = pathSegs[0].length() == 0 || pathSegs[0].contains(":");
			if (!absolute) {
				File home = manager.resolveFile(".");
				f = new File(home, path);
			} else {
				f = new File(path);
			}
			if (create) {
				boolean done = f.mkdirs();
				if (!done) {
					throw new IOException("Unable to create path. " + f.getAbsolutePath());
				}
			}
		}
		return f.getCanonicalPath();
	}

	public String getClientId() {
		return clientId;
	}

	public SwingConfig getAppConfig() {
		return config;
	}

	public String getSessionId() {
		if (webConnection != null) {
			return webConnection.uuid();
		}
		return null;
	}

	public String getMirroredSessionId() {
		if (mirroredWebConnection != null) {
			return mirroredWebConnection.uuid();
		}
		return null;
	}

	public boolean isRunning() {
		return (process != null && process.isRunning());
	}

	public String getUser() {
		return user.getUserId();
	}

	public Date getDisconnectedSince() {
		return disconnectedSince;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public Boolean isRecording() {
		return sessionRecorder != null && !sessionRecorder.isFailed();
	}

	public String getRecordingFile() {
		return sessionRecorder != null ? sessionRecorder.getFileName() : null;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public SwingInstanceStatus getStatus() {
		if (process == null) {
			return SwingInstanceStatus.NOT_STARTED;
		} else {
			if (isRunning()) {
				if (getEndedAt() == null) {
					return SwingInstanceStatus.RUNNING;
				} else {
					return SwingInstanceStatus.EXITING;
				}
			} else {
				if (process.isForceKilled()) {
					return SwingInstanceStatus.FORCE_KILLED;
				} else {
					return SwingInstanceStatus.FINISHED;
				}
			}
		}
	}

	@Override
	public void webSessionDisconnected(String connectionId) {
		if (getSessionId() != null && getSessionId().equals(connectionId)) {
			disconnectPrimaryWebSession();
		} else if (getMirroredSessionId() != null && getMirroredSessionId().equals(connectionId)) {
			disconnectMirroredWebSession();
		}
	}

	@Override
	public String getMirrorSessionId() {
		return mirroredWebConnection != null ? mirroredWebConnection.uuid() : null;
	}

	public void logStatValue(String name, Number value) {
		if (StringUtils.isNotEmpty(name)) {
			manager.logStatValue(getClientId(), name, value);
		}
	}

	@Override
	public void logWarningHistory() {
		List<String> current = manager.getInstanceWarnings(getClientId());
		if (current != null) {
			current.addAll(manager.getInstanceWarningHistory(getClientId()));
		}
		warningHistoryLog = current;
	}

	private Map<Long, String> toMap(Map<Long, ThreadDumpMsgInternal> dumps) {
		LinkedHashMap<Long, String> result = new LinkedHashMap<>();
		for (ThreadDumpMsgInternal dump : dumps.values()) {
			result.put(dump.getTimestamp(), dump.getReason());
		}
		return result;
	}

	@Override
	public String getThreadDump(String id) {
		try {
			ThreadDumpMsgInternal dump = threadDumps.get(Long.parseLong(id));
			if(dump!=null){
				return FileUtils.readFileToString(new File(dump.getDump()));
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to load threaddump",e);
			return null;
		}
	}

	@Override
	public void requestThreadDump() {
		if (isRunning()) {
			jvmConnection.send(new ThreadDumpRequestMsgInternal());
		}
	}

	private void notifyUserConnected() {
		sendUserApiEventMsg(ApiEventType.UserConnected, webConnection);
	}

	private void notifyUserDisconnected() {
		sendUserApiEventMsg(ApiEventType.UserDisconnected, webConnection);
	}

	private void notifyMirrorViewConnected() {
		sendUserApiEventMsg(ApiEventType.MirrorViewConnected, mirroredWebConnection);
	}

	private void notifyMirrorViewDisconnected() {
		sendUserApiEventMsg(ApiEventType.MirrorViewDisconnected, mirroredWebConnection);
	}

	private void sendUserApiEventMsg(ApiEventType type, WebSocketConnection r) {
		ApiEventMsgInternal event;
		if (r != null && r.getUser() != null) {
			AbstractWebswingUser connectedUser = r.getUser();
			event = new ApiEventMsgInternal(type, connectedUser.getUserId(), new HashMap<String, Serializable>(connectedUser.getUserAttributes()));
		} else {
			event = new ApiEventMsgInternal(type, null, null);
		}
		jvmConnection.send(event);
	}

}
