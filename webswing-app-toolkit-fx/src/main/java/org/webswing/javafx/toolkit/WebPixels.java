package org.webswing.javafx.toolkit;

import com.sun.glass.ui.Pixels;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class WebPixels extends Pixels {
	public WebPixels(int width, int height, ByteBuffer data) {
		super(width, height, data);
	}

	public WebPixels(int width, int height, IntBuffer data) {
		super(width, height, data);
	}

	public WebPixels(int width, int height, IntBuffer data, float scale) {
		super(width, height, data, scale);
	}

	@Override
	protected void _fillDirectByteBuffer(ByteBuffer bb) {
	}

	@Override
	protected void _attachInt(long ptr, int w, int h, IntBuffer ints, int[] array, int offset) {
	}

	@Override
	protected void _attachByte(long ptr, int w, int h, ByteBuffer bytes, byte[] array, int offset) {
	}
}
