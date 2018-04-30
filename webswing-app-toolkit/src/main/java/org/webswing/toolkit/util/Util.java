package org.webswing.toolkit.util;

import org.webswing.Constants;
import org.webswing.dispatch.WebPaintDispatcher;
import org.webswing.model.c2s.KeyboardEventMsgIn;
import org.webswing.model.c2s.KeyboardEventMsgIn.KeyEventType;
import org.webswing.model.c2s.MouseEventMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.FileDialogEventMsg.FileDialogEventType;
import org.webswing.model.s2c.WindowMsg;
import org.webswing.model.s2c.WindowPartialContentMsg;
import org.webswing.toolkit.WebToolkit;
import org.webswing.toolkit.WebWindowPeer;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

public class Util {

	public static File convertAndSaveCursor(BufferedImage img, int x, int y) {
		String tempDir = System.getProperty(Constants.TEMP_DIR_PATH);
		try {
			byte[] bytes = convertToIco(img, x, y);
			int id = Arrays.hashCode(bytes);
			File f = new File(URI.create(tempDir + "/c" + id + ".cur"));
			if (!f.exists()) {
				FileOutputStream output = new FileOutputStream(f);
				output.write(bytes);
				output.close();
			}
			return f;
		} catch (Exception e) {
			Logger.error("Failed to save cursor to file.", e);
		}
		return null;
	}

	private static byte[] convertToIco(BufferedImage img, int hotspotx, int hotspoty) throws IOException {
		byte[] imgBytes = Services.getImageService().getPngImage(img);

		ByteBuffer bytes = ByteBuffer.allocate(imgBytes.length + 22);
		bytes.order(ByteOrder.LITTLE_ENDIAN);

		bytes.putShort((short) 0);
		bytes.putShort((short) 1);
		bytes.putShort((short) 1);
		bytes.put((byte) img.getWidth());
		bytes.put((byte) img.getHeight()); //no need to multiply
		bytes.put((byte) img.getColorModel().getNumColorComponents()); //the pallet size
		bytes.put((byte) 0);
		bytes.putShort((short) hotspotx);
		bytes.putShort((short) hotspoty);
		bytes.putInt(imgBytes.length);
		bytes.putInt(22);
		bytes.put(imgBytes);
		return bytes.array();

	}

	private static List<Integer> NO_CHAR_KEY_CODES = Arrays
			.asList(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12, KeyEvent.VK_PRINTSCREEN, KeyEvent.VK_SCROLL_LOCK, KeyEvent.VK_PAUSE, KeyEvent.VK_INSERT, KeyEvent.VK_HOME, KeyEvent.VK_PAGE_DOWN,
					KeyEvent.VK_END, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_WINDOWS, KeyEvent.VK_ALT_GRAPH);
	private static Map<Integer, Character> CONTROL_MAP;

	static {
		CONTROL_MAP = new HashMap<Integer, Character>();
		CONTROL_MAP.put(81, '\u0011');
		CONTROL_MAP.put(69, '\u0005');
		CONTROL_MAP.put(82, '\u0012');
		CONTROL_MAP.put(89, '\u0019');
		CONTROL_MAP.put(85, '\u0015');
		CONTROL_MAP.put(73, '\u0009');
		CONTROL_MAP.put(79, '\u000F');
		CONTROL_MAP.put(80, '\u0010');
		CONTROL_MAP.put(65, '\u0001');
		CONTROL_MAP.put(83, '\u0013');
		CONTROL_MAP.put(68, '\u0004');
		CONTROL_MAP.put(71, '\u0007');
		CONTROL_MAP.put(72, '\u0008');
		CONTROL_MAP.put(74, '\n');
		CONTROL_MAP.put(76, '\u000C');
		CONTROL_MAP.put(90, '\u001A');
		CONTROL_MAP.put(88, '\u0018');
		CONTROL_MAP.put(67, '\u0003');
		CONTROL_MAP.put(86, '\u0016');
		CONTROL_MAP.put(66, '\u0002');
		CONTROL_MAP.put(77, '\r');
	}

	public static int getMouseButtonsAWTFlag(int button) {
		switch (button) {
		case 1:
			return MouseEvent.BUTTON1;
		case 2:
			return MouseEvent.BUTTON2;
		case 3:
			return MouseEvent.BUTTON3;
		case 0:
			return MouseEvent.NOBUTTON;
		}
		return 0;
	}

	public static int getMouseModifiersAWTFlag(MouseEventMsgIn evt) {
		int result = 0;
		if ((evt.getButtons() & 2) == 2) {
			result = result | MouseEvent.BUTTON1_DOWN_MASK;
		}
		if ((evt.getButtons() & 4) == 4) {
			result = result | MouseEvent.BUTTON2_DOWN_MASK;
		}
		if ((evt.getButtons() & 8) == 8) {
			result = result | MouseEvent.BUTTON3_DOWN_MASK | MouseEvent.META_DOWN_MASK;
		}
		if (evt.isCtrl()) {
			result = result | MouseEvent.CTRL_DOWN_MASK;
		}
		if (evt.isAlt()) {
			result = result | MouseEvent.ALT_DOWN_MASK;
		}
		if (evt.isShift()) {
			result = result | MouseEvent.SHIFT_DOWN_MASK;
		}
		if (evt.isMeta()) {
			result = result | MouseEvent.META_DOWN_MASK;
		}
		return result;
	}

	public static void savePngImage(BufferedImage imageContent, String name) {
		try {
			OutputStream os = new FileOutputStream(new File(name));
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			ImageIO.write(imageContent, "png", ios);
			ios.close();
			os.close();
		} catch (IOException e) {
			Logger.error("Util:savePngImage", e);
		}
	}

	public static int getKeyModifiersAWTFlag(KeyboardEventMsgIn event) {
		int modifiers = 0;
		if (event.isAlt()) {
			modifiers = modifiers | KeyEvent.ALT_MASK;
		}
		if (event.isCtrl()) {
			modifiers = modifiers | KeyEvent.CTRL_MASK;
		}
		if (event.isShift()) {
			modifiers = modifiers | KeyEvent.SHIFT_MASK;
		}
		if (event.isAltgr()) {
			modifiers = modifiers | KeyEvent.ALT_GRAPH_MASK;
		}
		if (event.isMeta()) {
			modifiers = modifiers | KeyEvent.META_MASK;
		}
		if (Util.getKeyType(event.getType()) == KeyEvent.KEY_TYPED && event.isAlt() && event.isCtrl() && !event.isShift() && !event.isMeta()) {
			modifiers = KeyEvent.ALT_GRAPH_MASK;
		}

		return modifiers;
	}

	public static int getKeyType(KeyEventType type) {
		switch (type) {
		case keydown:
			return KeyEvent.KEY_PRESSED;
		case keypress:
			return KeyEvent.KEY_TYPED;
		case keyup:
			return KeyEvent.KEY_RELEASED;
		}
		return 0;
	}

	public static char getKeyCharacter(KeyboardEventMsgIn event) {
		if (NO_CHAR_KEY_CODES.contains(event.getKeycode())) {
			return KeyEvent.CHAR_UNDEFINED;
		} else if (event.isCtrl() && !event.isAlt() && !event.isMeta() && !event.isShift() && !event.isMeta() && CONTROL_MAP.containsKey(event.getKeycode())) {
			return CONTROL_MAP.get(event.getKeycode());
		} else {
			return (char) event.getCharacter();
		}
	}

	public static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static Rectangle[] concatRectangleArrays(Rectangle[] A, Rectangle[] B) {
		int aLen = A.length;
		int bLen = B.length;
		Rectangle[] C = new Rectangle[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

	public static WebToolkit getWebToolkit() {
		return ((WebToolkit) Toolkit.getDefaultToolkit());
	}

	public static WebWindowPeer findWindowPeerById(String id) {
		for (Window w : Window.getWindows()) {
			Object peer = WebToolkit.targetToPeer(w);
			if (peer != null && peer instanceof WebWindowPeer) {
				if (((WebWindowPeer) peer).getGuid().equals(id)) {
					return (WebWindowPeer) peer;
				}

			}
		}
		return null;
	}

	public static Map<String, Map<Integer, BufferedImage>> extractWindowImages(AppFrameMsgOut json, Map<String, Map<Integer, BufferedImage>> windowImages) {
		for (WindowMsg window : json.getWindows()) {
			WebWindowPeer w = findWindowPeerById(window.getId());
			if (window.getId().equals(WebToolkit.BACKGROUND_WINDOW_ID)) {
				windowImages.put(window.getId(), new HashMap<Integer, BufferedImage>());// background image is handled on client
			} else {
				Map<Integer, BufferedImage> imageMap = new HashMap<Integer, BufferedImage>();
				for (int i = 0; i < window.getContent().size(); i++) {
					WindowPartialContentMsg wpc = window.getContent().get(i);
					imageMap.put(i, w.extractBufferedImage(new Rectangle(wpc.getPositionX(), wpc.getPositionY(), wpc.getWidth(), wpc.getHeight())));
				}
				windowImages.put(window.getId(), imageMap);
			}
		}
		return windowImages;
	}

	public static Map<String, Image> extractWindowWebImages(AppFrameMsgOut json, Map<String, Image> webImages) {
		for (Iterator<WindowMsg> i = json.getWindows().iterator(); i.hasNext(); ) {
			WindowMsg window = i.next();
			WebWindowPeer w = findWindowPeerById(window.getId());
			if (!window.getId().equals(WebToolkit.BACKGROUND_WINDOW_ID)) {
				Image webimageString = w.extractWebImage();
				webImages.put(window.getId(), webimageString);
			}
		}
		return webImages;
	}

	public static void encodeWindowImages(Map<String, Map<Integer, BufferedImage>> windowImages, AppFrameMsgOut json) {
		for (WindowMsg window : json.getWindows()) {
			if (!window.getId().equals(WebToolkit.BACKGROUND_WINDOW_ID)) {
				Map<Integer, BufferedImage> imageMap = windowImages.get(window.getId());
				for (int i = 0; i < window.getContent().size(); i++) {
					WindowPartialContentMsg c = window.getContent().get(i);
					if (imageMap.containsKey(i)) {
						c.setBase64Content(Services.getImageService().getPngImage(imageMap.get(i)));
					}
				}
			}
		}
	}

	public static void encodeWindowWebImages(Map<String, Image> windowWebImages, AppFrameMsgOut json) {
		for (WindowMsg window : json.getWindows()) {
			if (!window.getId().equals(WebToolkit.BACKGROUND_WINDOW_ID)) {
				Image wi = windowWebImages.get(window.getId());
				window.setDirectDraw(Services.getDirectDrawService().buildWebImage(wi));
			}
		}
	}

	@SuppressWarnings("restriction")
	public static AppFrameMsgOut fillJsonWithWindowsData(Map<String, Set<Rectangle>> currentAreasToUpdate, Map<String, List<Rectangle>> windowNonVisibleAreas) {
		AppFrameMsgOut json = new AppFrameMsgOut();
		for (String windowId : currentAreasToUpdate.keySet()) {
			WebWindowPeer ww = Util.findWindowPeerById(windowId);
			if (ww != null || windowId.equals(WebToolkit.BACKGROUND_WINDOW_ID)) {
				WindowMsg window = json.getOrCreateWindowById(windowId);
				if (windowId.equals(WebToolkit.BACKGROUND_WINDOW_ID)) {
					window.setPosX(0);
					window.setPosY(0);
					window.setWidth(getWebToolkit().getScreenSize().width);
					window.setHeight(getWebToolkit().getScreenSize().height);
				} else {
					Point location = ww.getLocationOnScreen();
					window.setPosX(location.x);
					window.setPosY(location.y);
					window.setWidth(ww.getBounds().width);
					window.setHeight(ww.getBounds().height);
				}
				List<Rectangle> toPaint = joinRectangles(getGrid(new ArrayList<Rectangle>(currentAreasToUpdate.get(windowId)), windowNonVisibleAreas.get(windowId)));
				List<WindowPartialContentMsg> partialContentList = new ArrayList<WindowPartialContentMsg>();
				for (Rectangle r : toPaint) {
					if (r.x < window.getWidth() && r.y < window.getHeight()) {
						WindowPartialContentMsg content = new WindowPartialContentMsg();
						content.setPositionX(r.x);
						content.setPositionY(r.y);
						content.setWidth(Math.min(r.width, window.getWidth() - r.x));
						content.setHeight(Math.min(r.height, window.getHeight() - r.y));
						partialContentList.add(content);
					}
				}
				window.setContent(partialContentList);
			}
		}
		return json;
	}

	public static Map<String, Set<Rectangle>> postponeNonShowingAreas(Map<String, Set<Rectangle>> currentAreasToUpdate) {
		Map<String, Set<Rectangle>> forLaterProcessing = new HashMap<String, Set<Rectangle>>();
		for (String windowId : currentAreasToUpdate.keySet()) {
			WebWindowPeer ww = Util.findWindowPeerById(windowId);
			if (ww != null) {
				if (!((Window) ww.getTarget()).isShowing()) {
					forLaterProcessing.put(windowId, currentAreasToUpdate.get(windowId));
				}
			}
		}
		for (String later : forLaterProcessing.keySet()) {
			currentAreasToUpdate.remove(later);
		}
		return forLaterProcessing;
	}

	public static boolean isWindowDecorationEvent(Window w, AWTEvent e) {
		if (e instanceof MouseEvent && MouseEvent.MOUSE_WHEEL != e.getID() && w!=null && w.isEnabled() && w.isShowing()) {
			return isWindowDecorationPosition((Window) w, new Point(((MouseEvent) e).getXOnScreen(), ((MouseEvent) e).getYOnScreen()));
		}
		return false;
	}

	public static boolean isWindowDecorationPosition(Window w, Point locationOnScreen) {
		if (w != null && locationOnScreen != null) {
			Rectangle inner = w.getBounds();
			Insets i = w.getInsets();
			inner.x = i.left;
			inner.y = i.top;
			inner.width -= i.left + i.right;
			inner.height -= i.top + i.bottom;
			boolean isInInnerWindow = SwingUtilities.isRectangleContainingRectangle(inner, new Rectangle(locationOnScreen.x - w.getX(), locationOnScreen.y - w.getY(), 0, 0));
			boolean isInWindow = SwingUtilities.isRectangleContainingRectangle(w.getBounds(), new Rectangle(locationOnScreen.x, locationOnScreen.y, 0, 0));
			return !isInInnerWindow && isInWindow;
		} else {
			return false;
		}
	}

	public static Set<Rectangle> getGrid(List<Rectangle> dirtyAreas, List<Rectangle> topWindows) {
		Set<Rectangle> result = new HashSet<Rectangle>();
		Set<Integer> xLines = new TreeSet<Integer>();
		Set<Integer> yLines = new TreeSet<Integer>();
		for (Rectangle r : dirtyAreas) {
			xLines.add(r.x);
			xLines.add(r.x + r.width);
			yLines.add(r.y);
			yLines.add(r.y + r.height);
		}
		if (topWindows != null) {
			for (Rectangle r : topWindows) {
				xLines.add(r.x);
				xLines.add(r.x + r.width);
				yLines.add(r.y);
				yLines.add(r.y + r.height);
			}
		}
		Integer[] y = yLines.toArray(new Integer[yLines.size()]);
		Integer[] x = xLines.toArray(new Integer[xLines.size()]);
		for (int row = 0; row < y.length - 1; row++) {
			for (int col = 0; col < x.length - 1; col++) {
				Rectangle potential = new Rectangle(x[col], y[row], x[col + 1] - x[col], y[row + 1] - y[row]);
				// filter
				boolean insideDirtyAreas = false;
				for (Rectangle da : dirtyAreas) {
					if (SwingUtilities.isRectangleContainingRectangle(da, potential)) {
						insideDirtyAreas = true;
						break;
					}
				}
				if (insideDirtyAreas) {
					boolean insideTopWindow = false;
					if (topWindows != null) {
						for (Rectangle tw : topWindows) {
							if (SwingUtilities.isRectangleContainingRectangle(tw, potential)) {
								insideTopWindow = true;
								break;
							}
						}
					}
					if (!insideTopWindow) {
						result.add(potential);
					}
				}
			}
		}
		return result;
	}

	public static List<Rectangle> joinRectangles(Set<Rectangle> grid) {
		List<Rectangle> result = new ArrayList<Rectangle>();
		List<Rectangle> gridList = new ArrayList<Rectangle>(grid);
		// join by rows
		List<Rectangle> joinedRows = new ArrayList<Rectangle>();
		Collections.sort(gridList, new Comparator<Rectangle>() {

			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				if (o1.y > o2.y) {
					return 1;
				} else if (o1.y == o2.y) {
					if (o1.x > o2.x) {
						return 1;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			}
		});
		Rectangle current = null;
		for (Rectangle r : gridList) {
			if (current == null) {
				current = r;
			} else {
				if (current.y == r.y && current.height == r.height && current.x + current.width == r.x) {// is
					// joinable
					// on
					// row
					current.width += r.width;
				} else {
					joinedRows.add(current);
					current = r;
				}
			}
		}
		if (current != null) {
			joinedRows.add(current);
		}
		// join by cols
		Collections.sort(joinedRows, new Comparator<Rectangle>() {

			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				if (o1.x > o2.x) {
					return 1;
				} else if (o1.x == o2.x) {
					if (o1.y > o2.y) {
						return 1;
					} else {
						return -1;
					}
				} else {
					return -1;
				}
			}
		});
		Rectangle currentX = null;
		for (Rectangle r : joinedRows) {
			if (currentX == null) {
				currentX = r;
			} else {
				if (currentX.x == r.x && currentX.width == r.width && currentX.y + currentX.height == r.y) {// is joinable on row
					currentX.height += r.height;
				} else {
					result.add(currentX);
					currentX = r;
				}
			}
		}
		if (currentX != null) {
			result.add(currentX);
		}
		return result;
	}

	public static void resetWindowsGC(int width, int height) {
		List<Window> windows = new ArrayList<Window>(Arrays.asList(Window.getWindows()));//to avoid concurent-modification-exception
		for (Window w : windows) {
			try {
				Class<?> windowClazz = w.getClass();
				while (windowClazz != Window.class && windowClazz != null) {
					windowClazz = windowClazz.getSuperclass();
				}
				if (windowClazz != null) {
					try {
						Method m = windowClazz.getDeclaredMethod("resetGC");
						m.setAccessible(true);
						m.invoke(w);
					} catch (Exception e) {
						// do nothing (java7 don't have this method)
					}
				}
				RepaintManager.currentManager(w).setDoubleBufferMaximumSize(new Dimension(width, height));
			} catch (Exception e) {
				Logger.error("Util:resetWindowsGC", e);
			}
		}
	}

	public static JFileChooser discoverFileChooser(WebWindowPeer windowPeer) {
		Window w = (Window) windowPeer.getTarget();
		if (w instanceof JDialog) {
			Container pane = ((JDialog) w).getContentPane();
			if (pane != null) {
				Component[] coms = pane.getComponents();
				if (coms != null && coms.length > 0 && coms[0] instanceof JFileChooser) {
					return (JFileChooser) coms[0];
				}
			}
		}
		return null;
	}

	public static String resolveUploadFilename(File currentDir, String fileName) {
		if (!existsFilename(currentDir, fileName)) {
			return fileName;
		} else {
			int i = fileName.lastIndexOf('.');
			String base = i > 0 ? fileName.substring(0, i) : fileName;
			String ext = i > 0 ? fileName.substring(i) : null;
			int next = 1;
			while (true) {
				String nextFN = base + " " + next + ext;
				if (!existsFilename(currentDir, nextFN)) {
					return nextFN;
				}
				next++;
			}
		}
	}

	public static boolean existsFilename(File currentDir, String fileName) {
		if (currentDir != null && currentDir.exists() && currentDir.isDirectory()) {
			for (File f : currentDir.listFiles()) {
				if (f.getName().equals(fileName)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isDD() {
		boolean startDD = Boolean.valueOf(System.getProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW, "false"));
		boolean supportedDD = Boolean.valueOf(System.getProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW_SUPPORTED, "true"));
		return startDD && supportedDD;
	}

	public static void repaintAllWindow() {
		synchronized (WebPaintDispatcher.webPaintLock) {
			for (Window w : Window.getWindows()) {
				if (w.isShowing()) {
					final Object peer = WebToolkit.targetToPeer(w);
					if (peer != null && peer instanceof WebWindowPeer) {
						((WebWindowPeer) peer).updateWindowDecorationImage();
						Util.getWebToolkit().getPaintDispatcher().notifyWindowRepaint(w);
						RepaintManager.currentManager(null).addDirtyRegion(w, 0, 0, w.getWidth(), w.getHeight());
					}
				}
			}
		}
	}

	public static Panel findHwComponentParent(JComponent c) {
		for (Container p = c.getParent(); p != null; p = p.getParent()) {
			if (p instanceof Panel && !(p instanceof Applet)) {
				return (Panel) p;
			}
		}
		return null;
	}

	public static AWTEvent createKeyEvent(Component src, int type, long when, int modifiers, int keycode, char character, int keyLocationStandard) {
		KeyEvent e = new KeyEvent(src, type, when, modifiers, keycode, character, KeyEvent.KEY_LOCATION_STANDARD);
		try {
			java.lang.reflect.Field f = KeyEvent.class.getDeclaredField("extendedKeyCode");
			f.setAccessible(true);
			f.set(e, keycode);
		} catch (Exception e1) {
			Logger.error("Failed to update extendedKeyCode of KeyEvent", e);
		}
		return e;
	}

	public static FileDialogEventType getFileChooserEventType(JFileChooser fileChooserDialog) {
		if (Boolean.getBoolean(Constants.SWING_START_SYS_PROP_ISOLATED_FS)) {
			if (Boolean.getBoolean(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_OPEN) && fileChooserDialog.getDialogType() == JFileChooser.OPEN_DIALOG) {
				return FileDialogEventType.AutoUpload;
			}
			if (Boolean.getBoolean(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_SAVE) && fileChooserDialog.getDialogType() == JFileChooser.SAVE_DIALOG) {
				return FileDialogEventType.AutoSave;
			}
		}
		return FileDialogEventType.Open;
	}

	public static String getFileChooserSelection(JFileChooser fileChooserDialog) {
		StringBuilder sb = new StringBuilder();
		if (fileChooserDialog.isMultiSelectionEnabled()) {
			if (fileChooserDialog.getSelectedFiles() != null) {
				for (File f : fileChooserDialog.getSelectedFiles()) {
					sb.append(f.getName()).append(",");
				}
				return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
			}
		} else {
			if (fileChooserDialog.getSelectedFile() != null) {
				return fileChooserDialog.getSelectedFile().getName();
			}
		}
		return "";
	}

}
