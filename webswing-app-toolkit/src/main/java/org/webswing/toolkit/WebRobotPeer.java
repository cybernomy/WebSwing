package org.webswing.toolkit;

import com.sun.glass.events.KeyEvent;
import org.webswing.model.c2s.KeyboardEventMsgIn;
import org.webswing.model.c2s.MouseEventMsgIn;
import org.webswing.model.c2s.PixelsAreaResponseMsgIn;
import org.webswing.model.c2s.SimpleEventMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.PixelsAreaRequestMsgOut;
import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Services;
import org.webswing.toolkit.util.Util;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.peer.RobotPeer;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class WebRobotPeer implements RobotPeer {

	GraphicsDevice device;
	private Point mousePos = new Point(0, 0);
	private int button;
	private int buttons;
	HashSet<Integer> pressedKeys = new HashSet<Integer>();

	public WebRobotPeer(Robot robot, GraphicsDevice device) {
		this.device = device;
	}

	@Override
	public void mouseMove(int x, int y) {
		Point oldPos = mousePos;
		mousePos = getValidPoint(x, y);
		double diffx = (oldPos.x - mousePos.x) / 10;
		double diffy = (oldPos.y - mousePos.y) / 10;
		for (int i = 0; i < 10; i++) {
			int sx = (int) Math.round(oldPos.x + (diffx * i));
			int sy = (int) Math.round(oldPos.y + (diffy * i));
			sendMouseEvent(MouseEventMsgIn.MouseEventType.mousemove, 0, new Point(sx, sy));
		}
	}

	@Override
	public void mousePress(int swingmask) {
		buttons = toButtonsWebEventMask(swingmask);
		button = getButtonPressed(swingmask);
		sendMouseEvent(MouseEventMsgIn.MouseEventType.mousedown, 0, mousePos);
	}

	@Override
	public void mouseRelease(int swingmask) {
		buttons = toButtonsWebEventMask(swingmask);
		button = getButtonPressed(swingmask);
		sendMouseEvent(MouseEventMsgIn.MouseEventType.mouseup, 0, mousePos);
	}

	@Override
	public void mouseWheel(int wheelAmt) {
		sendMouseEvent(MouseEventMsgIn.MouseEventType.mousewheel, wheelAmt, mousePos);
	}

	@Override
	public void keyPress(int keycode) {
		pressedKeys.add(keycode);
		sendKeyEvent(KeyboardEventMsgIn.KeyEventType.keydown, keycode);
		sendKeyEvent(KeyboardEventMsgIn.KeyEventType.keypress, keycode);
	}

	@Override
	public void keyRelease(int keycode) {
		pressedKeys.remove(keycode);
		sendKeyEvent(KeyboardEventMsgIn.KeyEventType.keyup, keycode);
	}

	@Override
	public int getRGBPixel(int x, int y) {
		return getRGBPixels(new Rectangle(x, y, 1, 1))[0];
	}

	@Override
	public int[] getRGBPixels(Rectangle bounds) {
		BufferedImage resultImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
		AppFrameMsgOut msg = new AppFrameMsgOut();
		PixelsAreaRequestMsgOut pixelsRequest = new PixelsAreaRequestMsgOut();
		pixelsRequest.setCorrelationId(UUID.randomUUID().toString());
		pixelsRequest.setX(bounds.x);
		pixelsRequest.setY(bounds.y);
		pixelsRequest.setW(bounds.width);
		pixelsRequest.setH(bounds.height);
		msg.setPixelsRequest(pixelsRequest);
		try {
			PixelsAreaResponseMsgIn result = (PixelsAreaResponseMsgIn) Services.getConnectionService().sendObjectSync(msg, pixelsRequest.getCorrelationId());
			Image image = Services.getImageService().readFromDataUrl(result.getPixels());
			Graphics g = resultImage.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
		} catch (Exception e) {
			Logger.error("Failed to get pixels area from browser.", e);
		}
		return ((DataBufferInt) resultImage.getRaster().getDataBuffer()).getData();
	}

	@Override
	public void dispose() {
	}

	private void sendKeyEvent(KeyboardEventMsgIn.KeyEventType type, int keycode) {
		KeyboardEventMsgIn kme = new KeyboardEventMsgIn();
		kme.setType(type);
		kme.setShift(isShift());
		kme.setMeta(isMeta());
		kme.setAlt(isAlt());
		kme.setCtrl(isCtrl());
		kme.setKeycode(keycode);
		int character = (!isShift() && (keycode >= 65 && keycode <= 90)) ? keycode + 32 : keycode;
		kme.setCharacter(character);
		Util.getWebToolkit().getEventDispatcher().dispatchEvent(kme);
	}

	private void sendMouseEvent(MouseEventMsgIn.MouseEventType type, int wheelDelta, Point mousePos) {
		MouseEventMsgIn mme = new MouseEventMsgIn();
		mme.setShift(isShift());
		mme.setAlt(isAlt());
		mme.setCtrl(isCtrl());
		mme.setMeta(isMeta());
		mme.setButton(button);
		mme.setButtons(buttons);
		mme.setType(type);
		mme.setWheelDelta(wheelDelta);
		mme.setX(mousePos.x);
		mme.setY(mousePos.y);
		Util.getWebToolkit().getEventDispatcher().dispatchEvent(mme);
	}

	private boolean isMeta() {
		return pressedKeys.contains(KeyEvent.VK_WINDOWS);
	}

	private boolean isCtrl() {
		return pressedKeys.contains(KeyEvent.VK_CONTROL);
	}

	private boolean isAlt() {
		return pressedKeys.contains(KeyEvent.VK_ALT) || pressedKeys.contains(KeyEvent.VK_ALT_GRAPH);
	}

	private boolean isShift() {
		return pressedKeys.contains(KeyEvent.VK_SHIFT);
	}

	private Point getValidPoint(int x, int y) {
		Rectangle bounds = device.getDefaultConfiguration().getBounds();
		x = Math.max(x, bounds.x);
		x = Math.min(x, bounds.x + bounds.width);
		y = Math.max(y, bounds.y);
		y = Math.min(y, bounds.y + bounds.height);
		return new Point(x, y);
	}

	private int toButtonsWebEventMask(int swingMask) {
		int result = 0;
		if ((swingMask & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
			result = result | 2;
		}
		if ((swingMask & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK) {
			result = result | 4;
		}
		if ((swingMask & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
			result = result | 8;
		}
		return result;
	}

	private int getButtonPressed(int swingMask) {
		if ((swingMask & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
			return 1;
		}
		if ((swingMask & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK) {
			return 2;
		}
		if ((swingMask & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
			return 3;
		}
		return 0;
	}
}
