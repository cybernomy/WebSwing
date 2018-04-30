package com.sun.swingset3;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.AWTException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class RobotDemo implements Runnable {
	JFrame frame;
	Robot robot;

	public RobotDemo(JFrame mainFrame) throws AWTException {
		frame = mainFrame;
		robot = new Robot();
	}

	public void start() {
		new Thread(this).start();
	}

	public void run() {

		Insets i = frame.getInsets();
		frame.setBounds(8 - i.left, 32 - i.top, 640 + i.right, 480 + i.bottom);
		robot.setAutoDelay(40);
		robot.setAutoWaitForIdle(true);

		robot.mouseMove(-1, -1);
		//open drag and drop panel
		robot.mouseMove(80, 340);
		robot.mouseWheel(-100);
		robot.delay(100);
		leftClick();

		//type text
		robot.mouseMove(444, 155);
		leftClick();
		type("Hello Robot");
		robot.delay(100);

		//mark text
		robot.mouseMove(445, 156);
		robot.delay(100);
		drag();
		robot.mouseMove(550, 200);
		robot.delay(1000);
		drop();

		//drag text
		robot.mouseMove(465, 160);
		robot.delay(100);
		drag();
		robot.mouseMove(291, 300);
		robot.delay(1000);
		drop();

		//press enter to confirm dialog
		robot.delay(1000);
		type(KeyEvent.VK_ENTER);

		//scroll down
		robot.delay(1000);
		robot.mouseMove(80, 340);
		robot.delay(100);
		robot.mouseWheel(100);

		//screenshot
		robot.delay(1000);
		BufferedImage image = robot.createScreenCapture(new Rectangle(32, 389, 30, 30));
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(image)));
	}

	private void leftClick() {
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(200);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(200);
	}

	private void drag() {
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(200);
	}

	private void drop() {
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(200);
	}

	private void type(int i) {
		robot.delay(40);
		robot.keyPress(i);
		robot.keyRelease(i);
	}

	private void type(String s) {
		byte[] bytes = s.getBytes();
		for (byte b : bytes) {
			int code = b;
			// keycode only handles [A-Z] (which is ASCII decimal [65-90])
			if (code > 96 && code < 123)
				code = code - 32;
			robot.delay(40);
			robot.keyPress(code);
			robot.keyRelease(code);
		}
	}
}
