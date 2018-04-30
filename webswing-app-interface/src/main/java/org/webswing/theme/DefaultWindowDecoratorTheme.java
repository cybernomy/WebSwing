package org.webswing.theme;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.IOUtils;
import org.webswing.Constants;
import org.webswing.common.WindowActionType;
import org.webswing.common.WindowDecoratorTheme;
import org.webswing.toolkit.extra.WindowManager;
import org.webswing.toolkit.util.Logger;

public class DefaultWindowDecoratorTheme implements WindowDecoratorTheme {
	private static final String DEFAULT_THEME = "Murrine";

	Insets insets = new Insets(0, 0, 0, 0);

	private String theme = System.getProperty(Constants.SWING_START_SYS_PROP_THEME, DEFAULT_THEME);

	private Properties themeProperties;
	private ImageSet active;
	private ImageSet inactive;

	public int BUTTON_OFFSET = 7;
	public int BUTTON_SPACING = 4;
	public int TITLE_HORIZONTAL_OFFSET = 0;

	private abstract class ImageSet {
		BufferedImage TOP_LEFT;
		BufferedImage TITLE;
		BufferedImage MENU;
		BufferedImage HIDE;
		BufferedImage MAXIMIZE;
		BufferedImage CLOSE;
		BufferedImage TOP_RIGHT;

		BufferedImage LEFT;
		BufferedImage RIGHT;

		BufferedImage BOTTOM_LEFT;
		BufferedImage BOTTOM;
		BufferedImage BOTTOM_RIGHT;

		Color TEXT_COLOR;
		Color TEXT_SHADOW_COLOR;

		boolean TITLE_SHADOW;
		int TITLE_VERTICAL_OFFSET;
	};

	public DefaultWindowDecoratorTheme() {
		try {
			init();
		} catch (Exception e) {
			Logger.error("Could not load theme " + theme + ". Falling back to default theme " + DEFAULT_THEME, e);
			theme = DEFAULT_THEME;
			try {
				init();
			} catch (IOException e1) {
				// should never happen
				Logger.fatal("Could not load default theme:", e1);
			}
		}
	}

	private void init() throws IOException {
		themeProperties = readProperties();
		active = new ImageSet() {
			{
				TOP_LEFT = readImage("top-left-active");
				TITLE = readImage("title-1-active");
				MENU = readImage("menu-active");
				HIDE = readImage("hide-active");
				MAXIMIZE = readImage("maximize-active");
				CLOSE = readImage("close-active");
				TOP_RIGHT = readImage("top-right-active");

				LEFT = readImage("left-active");
				RIGHT = readImage("right-active");

				BOTTOM_LEFT = readImage("bottom-left-active");
				BOTTOM = readImage("bottom-active");
				BOTTOM_RIGHT = readImage("bottom-right-active");

				TEXT_COLOR = Color.decode(themeProperties.getProperty("active_text_color", "#000000"));
				TEXT_SHADOW_COLOR = Color.decode(themeProperties.getProperty("active_text_shadow_color", "#111111"));
			}

		};
		inactive = new ImageSet() {
			{
				TOP_LEFT = readImage("top-left-inactive");
				TITLE = readImage("title-1-inactive");
				MENU = readImage("menu-inactive");
				HIDE = readImage("hide-inactive");
				MAXIMIZE = readImage("maximize-inactive");
				CLOSE = readImage("close-inactive");
				TOP_RIGHT = readImage("top-right-inactive");

				LEFT = readImage("left-inactive");
				RIGHT = readImage("right-inactive");

				BOTTOM_LEFT = readImage("bottom-left-inactive");
				BOTTOM = readImage("bottom-inactive");
				BOTTOM_RIGHT = readImage("bottom-right-inactive");

				TEXT_COLOR = Color.decode(themeProperties.getProperty("inactive_text_color", "#000000"));
				TEXT_SHADOW_COLOR = Color.decode(themeProperties.getProperty("inactive_text_shadow_color", "#111111"));
			}

		};
		insets = new Insets(active.TITLE.getHeight(), active.LEFT.getWidth(), active.BOTTOM.getHeight(), active.RIGHT.getWidth());
	}

	protected Properties readProperties() throws IOException {
		Properties properies = new Properties();

		ClassLoader classLoader = DefaultWindowDecoratorTheme.class.getClassLoader();
		URL resource = classLoader.getResource("themes/" + theme + "/xfwm4/themerc");
		if (resource == null) {
			resource = new File(theme + "/themerc").toURI().toURL();
		}
		if (resource != null) {
			properies.load(resource.openStream());
		} else {
			throw new IOException("File " + theme + "/themerc does not exist.");
		}
		return properies;
	}

	protected BufferedImage readImage(String key) throws IOException {
		ClassLoader classLoader = DefaultWindowDecoratorTheme.class.getClassLoader();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		Graphics2D g = null;
		BufferedImage bimage = null;

		URL resource = classLoader.getResource("themes/" + theme + "/xfwm4/" + key + ".xpm");
		if (resource == null) {
			resource = new File(theme + "/" + key + ".xpm").toURI().toURL();
		}
		if (resource != null) {
			String data;
			data = IOUtils.toString(resource);
			Image i = Xpm.XpmToImage(data);

			bimage = gc.createCompatibleImage(i.getWidth(null), i.getHeight(null), Transparency.BITMASK);
			g = bimage.createGraphics();
			g.drawImage(i, 0, 0, null);
		} else {
			throw new IOException("File " + theme + "/" + key + ".xpm does not exist.");
		}

		resource = classLoader.getResource("themes/" + theme + "/xfwm4/" + key + ".png");
		if (resource == null) {
			resource = new File(theme + "/" + key + ".png").toURI().toURL();
		}
		if (resource != null && g != null) {
			try {
				final Image i = ImageIO.read(resource);
				g.drawImage(i, 0, 0, null);
			} catch (Exception e) {
				Logger.debug("PNG image " + key + " not defined for theme " + theme);
			}
		}
		return bimage;
	}

	@Override
	public Insets getInsets() {
		return (Insets) insets.clone();
	}

	@Override
	public void paintWindowDecoration(Graphics g, Object window, int w, int h) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		ImageSet is = (window != null && window.equals(WindowManager.getInstance().getActiveWindow())) ? active : inactive;

		int xOffset = 0;
		int yOffset = 0;

		int x = xOffset;
		int y = yOffset + is.TITLE.getHeight();
		int effectiveH = h - is.TOP_LEFT.getHeight() - is.BOTTOM_LEFT.getHeight();
		g.drawImage(is.LEFT, x, y, is.LEFT.getWidth(), effectiveH, null);
		g.drawImage(is.RIGHT, xOffset + w - is.RIGHT.getWidth(), y, is.RIGHT.getWidth(), effectiveH, null);

		x = xOffset + is.TOP_LEFT.getWidth();
		y = yOffset;
		int effectiveW = w - is.TOP_LEFT.getWidth() - is.TOP_RIGHT.getWidth();
		g.drawImage(is.TITLE, x, y, effectiveW, is.TITLE.getHeight(), null);

		x = xOffset + is.BOTTOM_LEFT.getWidth();
		y = yOffset + h;
		effectiveW = w - is.BOTTOM_LEFT.getWidth() - is.BOTTOM_RIGHT.getWidth();
		g.drawImage(is.BOTTOM, x, y - is.BOTTOM.getHeight(), effectiveW, is.BOTTOM.getHeight(), null);

		x = xOffset;
		y = yOffset;
		g.drawImage(is.TOP_LEFT, x, y, null);
		g.drawImage(is.TOP_RIGHT, xOffset + w - is.TOP_RIGHT.getWidth(), y, null);

		x += is.TOP_LEFT.getWidth() + BUTTON_SPACING;
		g.drawImage(is.MENU, x, y + (is.TITLE.getHeight() - is.MENU.getHeight()) / 2, null);
		Rectangle menuRect = new Rectangle(x, y + (is.TITLE.getHeight() - is.MENU.getHeight()) / 2, is.MENU.getWidth(), is.MENU.getHeight());
		g.drawImage(getIcon(window), menuRect.x, menuRect.y, menuRect.width, menuRect.height, null);

		String title = getTitle(window)==null?"":getTitle(window);
		g.setFont(UIManager.getFont("TitledBorder.font"));
		float lineMetricsHeight = g.getFontMetrics().getLineMetrics(title, g).getHeight();

		x += is.MENU.getWidth() + BUTTON_SPACING;
		g.setColor(is.TEXT_COLOR);
		g.drawString(title, x, y + (is.TITLE.getHeight() - (int) lineMetricsHeight) / 2 + (int) lineMetricsHeight);

		x = xOffset + w - BUTTON_SPACING;
		x -= is.CLOSE.getWidth();
		g.drawImage(is.CLOSE, x, y + (is.TITLE.getHeight() - is.CLOSE.getHeight()) / 2, null);

		// Dialogs can be RESIZABLE too, at least on Linux/Unix
		if ((window instanceof Dialog && ((Dialog) window).isResizable()) || (window instanceof Frame) && ((Frame) window).isResizable()) {
			x -= BUTTON_SPACING + is.MAXIMIZE.getWidth();
			g.drawImage(is.MAXIMIZE, x, y + (is.TITLE.getHeight() - is.MAXIMIZE.getHeight()) / 2, null);

			x -= BUTTON_SPACING + is.HIDE.getWidth();
			g.drawImage(is.HIDE, x, y + (is.TITLE.getHeight() - is.HIDE.getHeight()) / 2, null);
		}

		x = xOffset;
		y = yOffset + h;
		g.drawImage(is.BOTTOM_LEFT, x, y - is.BOTTOM_LEFT.getHeight(), null);
		g.drawImage(is.BOTTOM_RIGHT, xOffset + w - is.BOTTOM_RIGHT.getWidth(), y - is.BOTTOM_RIGHT.getHeight(), null);

		insets = new Insets(is.TITLE.getHeight(), is.LEFT.getWidth(), is.BOTTOM.getHeight(), is.RIGHT.getWidth());
	}

	private Rectangle getHideRect(Window w) {
		ImageSet is = (w != null && w.equals(WindowManager.getInstance().getActiveWindow())) ? active : inactive;
		int x = w.getWidth() - BUTTON_SPACING - is.CLOSE.getWidth() - BUTTON_SPACING - is.MAXIMIZE.getWidth() - BUTTON_SPACING - is.HIDE.getWidth();
		return new Rectangle(x, 0 + (is.TITLE.getHeight() - is.HIDE.getHeight()) / 2, is.HIDE.getWidth(), is.HIDE.getHeight());
	}

	private Rectangle getMaximizeRect(Window w) {
		ImageSet is = (w != null && w.equals(WindowManager.getInstance().getActiveWindow())) ? active : inactive;
		int x = w.getWidth() - BUTTON_SPACING - is.CLOSE.getWidth() - BUTTON_SPACING - is.MAXIMIZE.getWidth();
		return new Rectangle(x, 0 + (is.TITLE.getHeight() - is.CLOSE.getHeight()) / 2, is.CLOSE.getWidth(), is.CLOSE.getHeight());
	}

	private Rectangle getCloseRect(Window w) {
		ImageSet is = (w != null && w.equals(WindowManager.getInstance().getActiveWindow())) ? active : inactive;
		int x = w.getWidth() - BUTTON_SPACING - is.CLOSE.getWidth();
		return new Rectangle(x, 0 + (is.TITLE.getHeight() - is.CLOSE.getHeight()) / 2, is.CLOSE.getWidth(), is.CLOSE.getHeight());
	}

	private static String getTitle(Object o) {
		if (o instanceof Frame) {
			return ((Frame) o).getTitle();
		} else if (o instanceof Dialog) {
			return ((Dialog) o).getTitle();
		} else {
			return null;
		}
	}

	private static Image getIcon(Object o) {
		if (o instanceof Frame) {
			return ((Frame) o).getIconImage();
		} else if (o instanceof Dialog) {
			List<Image> images = ((Dialog) o).getIconImages();
			if (images.size() > 0) {
				return images.get(0);
			}
		}
		return null;
	}

	@Override
	public WindowActionType getAction(Window w, Point e) {
		Rectangle eventPoint = new Rectangle((int) e.getX(), (int) e.getY(), 1, 1);
		Insets i = w.getInsets();

		// Dialogs can be RESIZABLE too, at least on Linux/Unix
		if ((w instanceof Dialog && ((Dialog) w).isResizable()) || (w instanceof Frame) && ((Frame) w).isResizable()) {
			if (SwingUtilities.isRectangleContainingRectangle(getHideRect(w), eventPoint)) {
				return WindowActionType.minimize;
			}
			if (SwingUtilities.isRectangleContainingRectangle(getMaximizeRect(w), eventPoint)) {
				return WindowActionType.maximize;
			}
		}
		if (SwingUtilities.isRectangleContainingRectangle(getCloseRect(w), eventPoint)) {
			return WindowActionType.close;
		}

		// resize
		if((w instanceof Dialog && ((Dialog) w).isResizable()) || (w instanceof Frame) && ((Frame) w).isResizable()){
			if (e.getX() < 10 && e.getY() < 10) {
				return WindowActionType.resizeUniTopLeft;
			}
			if (e.getX() > (w.getWidth() - 10)  && e.getY() < 10) {
				return WindowActionType.resizeUniTopRight;
			}
			if (e.getX() < 10 && e.getY() > (w.getHeight() - 10)) {
				return WindowActionType.resizeUniBottomLeft;
			}
			if (e.getX() > (w.getWidth() - 10) && e.getY() > (w.getHeight() - 10)) {
				return WindowActionType.resizeUniBottomRight;
			}
			if (e.getX() > (w.getWidth() - i.right)) {
				return WindowActionType.resizeRight;
			}
			if (e.getX() < i.left) {
				return WindowActionType.resizeLeft;
			}
			if (e.getY() > (w.getHeight() - i.bottom)) {
				return WindowActionType.resizeBottom;
			}
			if (e.getY() < i.bottom) {
				return WindowActionType.resizeTop;
			}
			
		}
		if (e.getY() < i.top) {
			// move
			return WindowActionType.move;
		}
		return WindowActionType.cursorChanged;

	}

}
