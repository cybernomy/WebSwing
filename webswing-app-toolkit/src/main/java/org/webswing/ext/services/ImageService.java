package org.webswing.ext.services;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.webswing.common.WindowDecoratorTheme;

public interface ImageService {

	byte[] getPngImage(BufferedImage image);

	WindowDecoratorTheme getWindowDecorationTheme();

	void moveFile(File srcFile, File destFile) throws IOException;

	Image readFromDataUrl(String img);
}
