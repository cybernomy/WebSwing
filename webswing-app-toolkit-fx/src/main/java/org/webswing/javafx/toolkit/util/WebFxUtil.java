package org.webswing.javafx.toolkit.util;

import com.sun.glass.ui.Pixels;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by vikto on 06-Mar-17.
 */
public class WebFxUtil {

	public static BufferedImage pixelsToImage(Pixels pixels) {
		Pixels p = pixels;
		if (p != null) {
			Buffer data = p.getPixels();
			int width = p.getWidth();
			int height = p.getHeight();
			if (p.getBytesPerComponent() == 1) {
				ByteBuffer bytes = (ByteBuffer) data;
				return paintByte(width, height, bytes);
			} else {
				IntBuffer ints = (IntBuffer) data;
				return paintInt(width, height, ints);
			}
		}
		return null;
	}

	private static BufferedImage paintInt(int width, int height, IntBuffer ints) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		DataBufferInt dataBuf = (DataBufferInt) bi.getRaster().getDataBuffer();
		ints.get(dataBuf.getData(), 0, width * height);
		return bi;
	}

	private static BufferedImage paintByte(int width, int height, ByteBuffer bytes) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		DataBufferByte dataBuf = (DataBufferByte) bi.getRaster().getDataBuffer();
		bytes.get(dataBuf.getData(), 0, width * height);
		return bi;
	}

}
