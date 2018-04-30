package org.webswing.toolkit.ge;

import sun.awt.FontConfiguration;

@SuppressWarnings("restriction")
public class WebGraphicsEnvironment7 extends WebGraphicsEnvironment {

	public WebGraphicsEnvironment7() {
		if (hasFontConfiguration()) {
			System.setProperty("sun.font.fontmanager", WebFontManager.class.getName());
		}
	}

	@Override
	public FontConfiguration createFontConfiguration(boolean b1, boolean b2) {
		return null;// not used in java7
	}

}
