package org.webswing.services.impl.ddutil;

import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.directdraw.DirectDrawServicesAdapter;
import org.webswing.directdraw.util.ImageConsumerAdapter;
import org.webswing.services.impl.ImageServiceImpl;

import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

public class FastDirectDrawServicesAdapter extends DirectDrawServicesAdapter {
	private static final Logger log = LoggerFactory.getLogger(FastDirectDrawServicesAdapter.class);
	XXHashFactory hashfactory = XXHashFactory.fastestInstance();
	Set<String> missingFonts = new HashSet<String>();
	long seed = 12345L;

	@Override
	public byte[] getPngImage(BufferedImage imageContent) {
		return ImageServiceImpl.getInstance().getPngImage(imageContent);
	}

	@Override
	public long getSignature(byte[] data) {
		return hashfactory.hash64().hash(data, 0, data.length, seed);
	}

	@Override
	public long computeHash(Image subImage) {
		final StreamingXXHash64 shash = hashfactory.newStreamingHash64(seed);
		final ByteBuffer byteBuffer = ByteBuffer.allocate(subImage.getWidth(null) * 4);
		final IntBuffer intBuffer = byteBuffer.asIntBuffer();
		ImageConsumerAdapter ic = new ImageConsumerAdapter() {
			@Override
			public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
				intBuffer.rewind();
				intBuffer.put(pixels, off, scansize);
				shash.update(byteBuffer.array(), 0, scansize * 4);
			}

			public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
				shash.update(pixels, 0, scansize);
			}
		};
		subImage.getSource().startProduction(ic);
		return shash.getValue();
	}

	@Override
	public String getFileForFont(Font font) {
		String fileForFont = super.getFileForFont(font);
		if (fileForFont == null && !missingFonts.contains(font.getFontName())) {
			missingFonts.add(font.getFontName());
			String fontFamily = font.getFamily();
			if (fontFamily.startsWith("Dialog") || fontFamily.startsWith("Monospaced") || fontFamily.startsWith("Serif") || fontFamily.startsWith("SansSerif")) {
				log.warn("Logical font " + fontFamily + " not defined in font configuration. Using default browser counterpart.");
			} else {
				log.warn("Font " + font.getFontName() + " not defined in font configuration. Falling back to glyph rendering.");
			}
		}
		return fileForFont;
	}
}
