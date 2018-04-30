package org.webswing.toolkit;

import java.applet.Applet;
import java.awt.AWTException;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.JobAttributes;
import java.awt.KeyboardFocusManager;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PageAttributes;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.ColorModel;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.MouseInfoPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.RobotPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.TrayIconPeer;
import java.awt.peer.WindowPeer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.webswing.Constants;
import org.webswing.dispatch.WebEventDispatcher;
import org.webswing.dispatch.WebPaintDispatcher;
import org.webswing.model.Msg;
import org.webswing.toolkit.api.WebswingMessagingApi;
import org.webswing.toolkit.api.WebswingApi;
import org.webswing.toolkit.api.WebswingApiProvider;
import org.webswing.toolkit.extra.WindowManager;
import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Util;

import sun.awt.SunToolkit;
import sun.awt.image.SurfaceManager;
import sun.java2d.SurfaceData;
import sun.print.PrintJob2D;

@SuppressWarnings("restriction")
public abstract class WebToolkit extends SunToolkit implements WebswingApiProvider {
	public static final Font defaultFont = new Font("Dialog", 0, 12);

	public static final String BACKGROUND_WINDOW_ID = "BG";
	private static Object TREELOCK = null;

	private WebEventDispatcher eventDispatcher;
	private WebPaintDispatcher paintDispatcher;
	private WebswingApiImpl api = new WebswingApiImpl();
	private WebswingMessagingApiImpl msgapi = new WebswingMessagingApiImpl();

	private WindowManager windowManager = WindowManager.getInstance();
	private ClassLoader swingClassLoader;

	public void init() {
		try {
			if (!System.getProperty("os.name", "").startsWith("Windows") && !System.getProperty("os.name", "").startsWith("Mac")) {
				Class<?> c = ClassLoader.getSystemClassLoader().loadClass("sun.awt.X11GraphicsEnvironment");
				Method initDisplayMethod = c.getDeclaredMethod("initDisplay", Boolean.TYPE);
				initDisplayMethod.setAccessible(true);
				initDisplayMethod.invoke(null, false);
			}
		} catch (Exception e) {
			Logger.error("Failed to init X11 display: ", e.getMessage());
		}
		installFonts();
	}

	private void installFonts() {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Properties fontsProp=new Properties();
			String fontConfig=System.getProperty("sun.awt.fontconfig");
			if(fontConfig!=null) {
				fontsProp.load(new FileInputStream(new File(fontConfig)));
				for (String name : fontsProp.stringPropertyNames()) {
					if (name.startsWith("filename.")) {
						String file = fontsProp.getProperty(name);
						Font font = Font.createFont(Font.TRUETYPE_FONT, new File(file));
						ge.registerFont(font);
					}
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to install fonts",e);
		}
	}

	public void startDispatchers() {
		eventDispatcher = new WebEventDispatcher();
		paintDispatcher = new WebPaintDispatcher();
	}

	public void initSize(final Integer desktopWidth, final Integer desktopHeight) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					initSize(desktopWidth, desktopHeight);
				}
			});
		} else {
			int oldWidht = screenWidth;
			int oldHeight = screenHeight;
			screenWidth = desktopWidth;
			screenHeight = desktopHeight;
			displayChanged();
			resetGC();
			Util.resetWindowsGC(screenWidth, screenHeight);
			getPaintDispatcher().clientReadyToReceive();
			getPaintDispatcher().resetWindowsPosition(oldWidht, oldHeight);// in case windows moved out of screen by resizing screen.
			getPaintDispatcher().notifyWindowRepaintAll();
		}
	}

	protected WindowManager getWindowManager() {
		return windowManager;
	}

	public WebEventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public WebPaintDispatcher getPaintDispatcher() {
		return paintDispatcher;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////// Toolkit Implementation//////////////////////////////////////////////////
	private static WebMouseInfoPeer mPeer;

	public WebToolkit() {
		if (System.getProperty("java.version").startsWith("1.6") || System.getProperty("java.version").startsWith("1.7")) {
			try {
				Method m = SunToolkit.class.getDeclaredMethod("setDataTransfererClassName", String.class);
				m.setAccessible(true);
				m.invoke(null, "org.webswing.toolkit.WebDataTransfer");
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	private static GraphicsConfiguration config;
	private Hashtable<String, FontPeer> cacheFontPeer;
	private WebClipboard clipboard;
	private Clipboard selectionClipboard;

	private boolean exiting = false;

	public static int screenWidth = Integer.parseInt(System.getProperty(Constants.SWING_SCREEN_WIDTH, Constants.SWING_SCREEN_WIDTH_MIN + ""));
	public static int screenHeight = Integer.parseInt(System.getProperty(Constants.SWING_SCREEN_HEIGHT, Constants.SWING_SCREEN_HEIGHT_MIN + ""));

	public static final Object targetToPeer(Object paramObject) {
		return SunToolkit.targetToPeer(paramObject);
	}

	public static final void targetDisposedPeer(Object paramObject1, Object paramObject2) {
		SunToolkit.targetDisposedPeer(paramObject1, paramObject2);
	}

	@Override
	protected void initializeDesktopProperties() {
		if (System.getProperty(Constants.SWING_START_SYS_PROP_ISOLATED_FS, "").equalsIgnoreCase("true")) {
			this.desktopProperties.put("Shell.shellFolderManager", "org.webswing.toolkit.extra.WebShellFolderManager");
		} else {
			if (System.getProperty("os.name", "").startsWith("Windows")) {
				this.desktopProperties.put("Shell.shellFolderManager", "sun.awt.shell.Win32ShellFolderManager2");
			}
		}

		this.desktopProperties.put("DnD.gestureMotionThreshold", 2);
		this.desktopProperties.put("DnD.Autoscroll.initialDelay", 100);
		this.desktopProperties.put("DnD.Autoscroll.interval", 100);
		this.desktopProperties.put("DnD.Autoscroll.cursorHysteresis", 10);
		this.desktopProperties.put("awt.dynamicLayoutSupported", true);
		this.desktopProperties.put("awt.file.showAttribCol", false);
		this.desktopProperties.put("awt.file.showHiddenFiles", false);
		this.desktopProperties.put("awt.mouse.numButtons", 5);
		this.desktopProperties.put("awt.multiClickInterval", 500);
		this.desktopProperties.put("awt.wheelMousePresent", true);
		this.desktopProperties.put("win.3d.backgroundColor", new java.awt.Color(240, 240, 240));
		this.desktopProperties.put("win.3d.darkShadowColor", new java.awt.Color(105, 105, 105));
		this.desktopProperties.put("win.3d.highlightColor", new java.awt.Color(255, 255, 255));
		this.desktopProperties.put("win.3d.lightColor", new java.awt.Color(227, 227, 227));
		this.desktopProperties.put("win.3d.shadowColor", new java.awt.Color(160, 160, 160));
		this.desktopProperties.put("win.ansiFixed.font", Font.decode("Monospaced 0 13"));
		this.desktopProperties.put("win.ansiFixed.font.height", 13);
		this.desktopProperties.put("win.ansiVar.font", Font.decode("Dialog 0 11"));
		this.desktopProperties.put("win.ansiVar.font.height", 11);
		this.desktopProperties.put("win.button.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.caret.width", 1);
		this.desktopProperties.put("win.defaultGUI.font", Font.decode("Dialog 0 11"));
		this.desktopProperties.put("win.defaultGUI.font.height", 11);
		this.desktopProperties.put("win.desktop.backgroundColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.deviceDefault.font", Font.decode("Dialog 1 13"));
		this.desktopProperties.put("win.deviceDefault.font.height", 13);
		this.desktopProperties.put("win.drag.height", 4);
		this.desktopProperties.put("win.drag.width", 4);
		this.desktopProperties.put("win.frame.activeBorderColor", new java.awt.Color(180, 180, 180));
		this.desktopProperties.put("win.frame.activeCaptionColor", new java.awt.Color(153, 180, 209));
		this.desktopProperties.put("win.frame.activeCaptionGradientColor", new java.awt.Color(185, 209, 234));
		this.desktopProperties.put("win.frame.backgroundColor", new java.awt.Color(255, 255, 255));
		this.desktopProperties.put("win.frame.captionButtonHeight", 22);
		this.desktopProperties.put("win.frame.captionButtonWidth", 36);
		this.desktopProperties.put("win.frame.captionFont", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.frame.captionFont.height", 12);
		this.desktopProperties.put("win.frame.captionGradientsOn", true);
		this.desktopProperties.put("win.frame.captionHeight", 22);
		this.desktopProperties.put("win.frame.captionTextColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.frame.color", new java.awt.Color(100, 100, 100));
		this.desktopProperties.put("win.frame.fullWindowDragsOn", true);
		this.desktopProperties.put("win.frame.inactiveBorderColor", new java.awt.Color(244, 247, 252));
		this.desktopProperties.put("win.frame.inactiveCaptionColor", new java.awt.Color(191, 205, 219));
		this.desktopProperties.put("win.frame.inactiveCaptionGradientColor", new java.awt.Color(215, 228, 242));
		this.desktopProperties.put("win.frame.inactiveCaptionTextColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.frame.sizingBorderWidth", 5);
		this.desktopProperties.put("win.frame.smallCaptionButtonHeight", 22);
		this.desktopProperties.put("win.frame.smallCaptionButtonWidth", 22);
		this.desktopProperties.put("win.frame.smallCaptionFont", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.frame.smallCaptionFont.height", 12);
		this.desktopProperties.put("win.frame.smallCaptionHeight", 22);
		this.desktopProperties.put("win.frame.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.highContrast.on", false);
		this.desktopProperties.put("win.icon.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.icon.font.height", 12);
		this.desktopProperties.put("win.icon.hspacing", 75);
		this.desktopProperties.put("win.icon.titleWrappingOn", true);
		this.desktopProperties.put("win.icon.vspacing", 75);
		this.desktopProperties.put("win.item.highlightColor", new java.awt.Color(51, 153, 255));
		this.desktopProperties.put("win.item.highlightTextColor", new java.awt.Color(255, 255, 255));
		this.desktopProperties.put("win.item.hotTrackedColor", new java.awt.Color(0, 102, 204));
		this.desktopProperties.put("win.item.hotTrackingOn", true);
		this.desktopProperties.put("win.mdi.backgroundColor", new java.awt.Color(171, 171, 171));
		this.desktopProperties.put("win.menu.backgroundColor", new java.awt.Color(240, 240, 240));
		this.desktopProperties.put("win.menu.buttonWidth", 19);
		this.desktopProperties.put("win.menu.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.menu.font.height", 12);
		this.desktopProperties.put("win.menu.height", 19);
		this.desktopProperties.put("win.menu.keyboardCuesOn", false);
		this.desktopProperties.put("win.menu.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.menubar.backgroundColor", new java.awt.Color(240, 240, 240));
		this.desktopProperties.put("win.messagebox.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.messagebox.font.height", 12);
		this.desktopProperties.put("win.oemFixed.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.oemFixed.font.height", 12);
		this.desktopProperties.put("win.properties.version", 3);
		this.desktopProperties.put("win.scrollbar.backgroundColor", new java.awt.Color(200, 200, 200));
		this.desktopProperties.put("win.scrollbar.height", 17);
		this.desktopProperties.put("win.scrollbar.width", 17);
		this.desktopProperties.put("win.status.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.status.font.height", 12);
		this.desktopProperties.put("win.system.font", Font.decode("Dialog 1 13"));
		this.desktopProperties.put("win.system.font.height", 13);
		this.desktopProperties.put("win.systemFixed.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.systemFixed.font.height", 12);
		this.desktopProperties.put("win.text.fontSmoothingContrast", 1200);
		this.desktopProperties.put("win.text.fontSmoothingOn", true);
		this.desktopProperties.put("win.text.fontSmoothingOrientation", 1);
		this.desktopProperties.put("win.text.fontSmoothingType", 2);
		this.desktopProperties.put("win.text.grayedTextColor", new java.awt.Color(109, 109, 109));
		this.desktopProperties.put("win.tooltip.backgroundColor", new java.awt.Color(255, 255, 225));
		this.desktopProperties.put("win.tooltip.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.tooltip.font.height", 12);
		this.desktopProperties.put("win.tooltip.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.xpstyle.colorName", "NormalColor");
		this.desktopProperties.put("win.xpstyle.dllName", "C:\\WINDOWS\\resources\\themes\\Aero\\Aero.msstyles");
		this.desktopProperties.put("win.xpstyle.sizeName", "NormalSize");
		this.desktopProperties.put("win.xpstyle.themeActive", true);
		if (System.getProperty("os.name", "").startsWith("Windows")) {
			try {
				Field xpStyleEnabledField = sun.awt.windows.ThemeReader.class.getDeclaredField("xpStyleEnabled");
				xpStyleEnabledField.setAccessible(true);
				xpStyleEnabledField.setBoolean(null, true);
			} catch (Exception e) {
				Logger.debug("Failed to set xpStyleEnabled to true", e);
			}
		}
	}

	public boolean needUpdateWindow() {
		return true;
	}

	public boolean isTranslucencyCapable(GraphicsConfiguration paramGraphicsConfiguration) {
		return true;
	}

	public boolean isFrameStateSupported(int state) throws HeadlessException {
		return true;
	}

	public KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager paramKeyboardFocusManager) throws HeadlessException {
		return new WebKeyboardFocusManagerPeer();
	}

	public FramePeer createFrame(Frame frame) throws HeadlessException {
		WebFramePeer localWFramePeer = new WebFramePeer(frame);
		targetCreatedPeer(frame, localWFramePeer);
		return localWFramePeer;
	}

	public DialogPeer createDialog(Dialog paramDialog) throws HeadlessException {
		WebDialogPeer localdialogPeer = new WebDialogPeer(paramDialog);
		targetCreatedPeer(paramDialog, localdialogPeer);
		return localdialogPeer;
	}

	public boolean isModalityTypeSupported(ModalityType mt) {
		return true;
	}

	public WindowPeer createWindow(Window paramWindow) throws HeadlessException {
		WebWindowPeer localwindowPeer = new WebWindowPeer(paramWindow);
		targetCreatedPeer(paramWindow, localwindowPeer);
		return localwindowPeer;
	}

	public PanelPeer createPanel(Panel panel) {
		if (panel instanceof Applet) {
			return super.createPanel(panel);
		}
		WebPanelPeer localpanelPeer = new WebPanelPeer(panel);
		targetCreatedPeer(panel, localpanelPeer);
		return localpanelPeer;
	}

	@Override
	protected synchronized MouseInfoPeer getMouseInfoPeer() {
		{
			if (mPeer == null) {
				mPeer = new WebMouseInfoPeer();
			}
			return mPeer;
		}
	}

	public FontPeer getFontPeer(String paramString, int paramInt) {
		FontPeer localObject = null;
		String str = paramString.toLowerCase();
		if (null != this.cacheFontPeer) {
			localObject = this.cacheFontPeer.get(str + paramInt);
			if (null != localObject) {
				return localObject;
			}
		}
		localObject = new WebFontPeer(paramString, paramInt);
		if (localObject != null) {
			if (null == this.cacheFontPeer) {
				this.cacheFontPeer = new Hashtable<String, FontPeer>(5, 0.9F);
			}
			if (null != this.cacheFontPeer) {
				this.cacheFontPeer.put(str + paramInt, localObject);
			}
		}
		return ((FontPeer) localObject);
	}

	public Clipboard getSystemClipboard() throws HeadlessException {
		return getWebswingClipboard();
	}

	public WebClipboard getWebswingClipboard() {
		synchronized (this) {
			if (this.clipboard == null) {
				this.clipboard = new WebClipboard("default", true);
			}
		}
		return this.clipboard;
	}

	public Clipboard getSystemSelection() throws HeadlessException {
		synchronized (this) {
			if (this.selectionClipboard == null) {
				this.selectionClipboard = new WebClipboard("selection", false);
			}
		}
		return this.selectionClipboard;
	}

	@Override
	protected Object lazilyLoadDesktopProperty(String name) {
		if ("awt.font.desktophints".equals(name)) {
			return SunToolkit.getDesktopFontHints();
		}

		return super.lazilyLoadDesktopProperty(name);
	}

	@Override
	protected RenderingHints getDesktopAAHints() {
		RenderingHints hints = new RenderingHints(null);
		hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_TEXT_LCD_CONTRAST, 140);
		return hints;

	}

	public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent paramDragGestureEvent) throws InvalidDnDOperationException {
		return WebDragSourceContextPeer.createDragSourceContextPeer(paramDragGestureEvent);
	}

	@SuppressWarnings("unchecked")
	public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> type, DragSource dragSource, Component component, int act, DragGestureListener listener) {
		if (MouseDragGestureRecognizer.class.equals(type)) {
			return (T) new WebMouseDragGestureRecognizer(dragSource, component, act, listener);
		}
		return null;
	}

	protected int getScreenWidth() {
		return screenWidth;
	}

	protected int getScreenHeight() {
		return screenHeight;
	}

	public int getScreenResolution() throws HeadlessException {
		return 72;
	}

	public ColorModel getColorModel() throws HeadlessException {
		if (config == null) {
			resetGC();
		}
		return config.getColorModel();
	}

	public void sync() {
	}

	public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight paramInputMethodHighlight) throws HeadlessException {
		return null;
	}

	public static void resetGC() {
		config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	public abstract void displayChanged();

	// EventQueue.invokeLater(new Runnable() {
	//
	// public void run() {
	// ((WebGraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
	// }
	// });
	// }

	public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
		return new WebInputMethodDescriptor();
	}

	protected boolean syncNativeQueue() {
		return true;
	}

	public void grab(Window paramWindow) {
	}

	public void ungrab(Window paramWindow) {
	}

	public ButtonPeer createButton(Button paramButton) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public TextFieldPeer createTextField(TextField paramTextField) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ChoicePeer createChoice(Choice paramChoice) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public LabelPeer createLabel(Label paramLabel) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ListPeer createList(List paramList) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public CheckboxPeer createCheckbox(Checkbox paramCheckbox) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ScrollbarPeer createScrollbar(Scrollbar paramScrollbar) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ScrollPanePeer createScrollPane(ScrollPane paramScrollPane) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public TextAreaPeer createTextArea(TextArea paramTextArea) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public FileDialogPeer createFileDialog(FileDialog paramFileDialog) throws HeadlessException {
		return new WebFileDialogPeer(paramFileDialog);
	}

	public MenuBarPeer createMenuBar(MenuBar paramMenuBar) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public MenuPeer createMenu(Menu paramMenu) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public PopupMenuPeer createPopupMenu(PopupMenu paramPopupMenu) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public MenuItemPeer createMenuItem(MenuItem paramMenuItem) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem paramCheckboxMenuItem) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public TrayIconPeer createTrayIcon(TrayIcon paramTrayIcon) throws HeadlessException, AWTException {
		throw new UnsupportedOperationException();
	}

	public SystemTrayPeer createSystemTray(SystemTray paramSystemTray) {
		return new WebSystemTrayPeer();
	}

	public boolean isTraySupported() {
		return false;
	}

	public RobotPeer createRobot(Robot robot, GraphicsDevice device) throws AWTException {
		return new WebRobotPeer(robot, device);
	}

	public boolean isDesktopSupported() {
		return true;
	}

	public boolean isWindowOpacityControlSupported() {
		return true;
	}

	public boolean isWindowShapingSupported() {
		return false;
	}

	public boolean isWindowTranslucencySupported() {
		return true;
	}

	protected DesktopPeer createDesktopPeer(Desktop paramDesktop) throws HeadlessException {
		return new WebDesktopPeer(paramDesktop);
	}

	public PrintJob getPrintJob(Frame frame, String jobtitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
		PrintJob2D localPrintJob2D = new PrintJob2D(frame, jobtitle, jobAttributes, pageAttributes);

		if (!localPrintJob2D.printDialog()) {
			localPrintJob2D = null;
		}

		return localPrintJob2D;
	}

	public PrintJob getPrintJob(Frame frame, String jobtitle, Properties paramProperties) {
		return getPrintJob(frame, jobtitle, null, null);
	}

	public void beep() {

	}

	@Override
	public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
		return false;
	}

	public GraphicsConfiguration getGraphicsConfig() {
		if (config == null) {
			resetGC();
		}
		return config;
	}

	public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
		return true;
	}

	// update system colors to win7 default theme (light)
	protected void loadSystemColors(int[] systemColors) throws HeadlessException {
		if (systemColors != null && systemColors.length == 26) {
			systemColors[0] = 0xff000000;
			systemColors[1] = 0xff99b4d1;
			systemColors[2] = 0xff000000;
			systemColors[3] = 0xffb4b4b4;
			systemColors[4] = 0xffbfcddb;
			systemColors[5] = 0xff434e54;
			systemColors[6] = 0xfff4f7fc;
			systemColors[7] = 0xffffffff;
			systemColors[8] = 0xff646464;
			systemColors[9] = 0xff000000;
			systemColors[10] = 0xfff0f0f0;
			systemColors[11] = 0xff000000;
			systemColors[12] = 0xffffffff;
			systemColors[13] = 0xff000000;
			systemColors[14] = 0xff3399ff;
			systemColors[15] = 0xffffffff;
			systemColors[16] = 0xff6d6d6d;
			systemColors[17] = 0xfff0f0f0;
			systemColors[18] = 0xff000000;
			systemColors[19] = 0xffe3e3e3;
			systemColors[20] = 0xffffffff;
			systemColors[21] = 0xffa0a0a0;
			systemColors[22] = 0xff696969;
			systemColors[23] = 0xffc8c8c8;
			systemColors[24] = 0xffffffe1;
			systemColors[25] = 0xff000000;
		}
	}

	abstract public boolean webConpoenentPeerUpdateGraphicsData();

	abstract public SurfaceData webComponentPeerReplaceSurfaceData(SurfaceManager mgr);

	public synchronized void exitSwing(final int i) {
		if (!exiting) {
			exiting = true;
			Thread shutdownThread = new Thread(new Runnable() {
				@Override
				public void run() {
					//tell server to kill this application after defined time
					try {
						getPaintDispatcher().notifyApplicationExiting();
						api.fireShutdownListeners();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			shutdownThread.setName("Webswing shutdown thread");
			shutdownThread.setDaemon(true);
			shutdownThread.start();
		}
	}

	public void defaultShutdownProcedure() {
		//first send windows closing event to all windows
		for (Window w : Window.getWindows()) {
			w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				//make sure we close windows created by window close listeners executed above
				for (Window w : Window.getWindows()) {
					w.setVisible(false);
					w.dispose();
				}
			}
		});
	}

	public Object getTreeLock() {
		if (TREELOCK == null) {
			TREELOCK = new JPanel().getTreeLock();
		}
		return TREELOCK;
	}

	@Override
	public WebswingApi getApi() {
		return api;
	}

	@Override
	public WebswingMessagingApi getMessagingApi() {
		return msgapi;
	}

	public void processApiEvent(Msg event) {
		api.processEvent(event);
	}

	public boolean messageApiHasListenerForClass(String msgtype) {
		return msgapi.hasListenerForClass(msgtype);
	}

	public void messageApiProcessMessage(Serializable object) {
		msgapi.processMessage(object);
	}

	@Override
	public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException, HeadlessException {
		return new WebCursor(cursor, hotSpot, name);
	}

	public void setSwingClassLoader(ClassLoader swingClassLoader) {
		this.swingClassLoader = swingClassLoader;
	}

	public ClassLoader getSwingClassLoader() {
		return swingClassLoader;
	}
}
