package org.webswing.toolkit.ge;

import sun.awt.FontConfiguration;

@SuppressWarnings("restriction")
public class WebGraphicsEnvironment8 extends WebGraphicsEnvironment {

	public WebGraphicsEnvironment8() {
		if (hasFontConfiguration()) {
			System.setProperty("sun.font.fontmanager", WebFontManager.class.getName());
		}

	}

	public FontConfiguration createFontConfiguration(boolean b1, boolean b2) {
		return null; //not used in java8 (see WebFontManager) 
	}

}
