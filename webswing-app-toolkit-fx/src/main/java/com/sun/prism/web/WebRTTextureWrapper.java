package com.sun.prism.web;

import com.sun.glass.ui.Screen;
import com.sun.prism.Graphics;
import com.sun.prism.RTTexture;

import java.nio.Buffer;

public class WebRTTextureWrapper extends WebTextureWrapper implements RTTexture {
	private final RTTexture original;

	public WebRTTextureWrapper(RTTexture original) {
		super(original);
		this.original = original;
	}

	@Override
	public int[] getPixels() {
		return null;
	}

	@Override
	public boolean readPixels(Buffer pixels) {
		textureLookup.put(System.identityHashCode(pixels),this);
		return original.readPixels(pixels);
	}

	@Override
	public boolean readPixels(Buffer pixels, int x, int y, int width, int height) {
		textureLookup.put(System.identityHashCode(pixels),this);
		return original.readPixels(pixels, x, y, width, height);
	}

	@Override
	public boolean isVolatile() {
		return original.isVolatile();
	}

	@Override
	public Screen getAssociatedScreen() {
		return original.getAssociatedScreen();
	}

	@Override
	public Graphics createGraphics() {
		Graphics originalGraphics = original.createGraphics();
		return new WebPrismGraphicsWrapper(originalGraphics,this);
	}

	@Override
	public boolean isOpaque() {
		return original.isOpaque();
	}

	@Override
	public void setOpaque(boolean opaque) {
		original.setOpaque(opaque);
	}

	@Override
	public boolean isMSAA() {
		return original.isMSAA();
	}
}
