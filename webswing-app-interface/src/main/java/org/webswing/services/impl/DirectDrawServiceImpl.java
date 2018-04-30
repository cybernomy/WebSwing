package org.webswing.services.impl;

import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.image.VolatileImage;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.toolkit.VolatileWebImageWrapper;
import org.webswing.directdraw.toolkit.WebImage;
import org.webswing.ext.services.DirectDrawService;
import org.webswing.services.impl.ddutil.FastDirectDrawServicesAdapter;

public class DirectDrawServiceImpl implements DirectDrawService {

	private static DirectDrawServiceImpl impl;
	protected DirectDraw dd = new DirectDraw(new FastDirectDrawServicesAdapter());

	public static DirectDrawServiceImpl getInstance() {
		if (impl == null) {
			impl = new DirectDrawServiceImpl();
		}
		return impl;
	}

	private DirectDrawServiceImpl() {
	}

	@Override
	public Image createImage(int width, int height) {
		return dd.createImage(width, height);
	}

	@Override
	public Image extractWebImage(Image webImage) {
		if (webImage instanceof WebImage) {
			return ((WebImage) webImage).extractReadOnlyWebImage(true);
		} else if (webImage instanceof VolatileWebImageWrapper) {
			return ((VolatileWebImageWrapper) webImage).getWebImage().extractReadOnlyWebImage(true);
		}

		return null;
	}

	@Override
	public void resetCache() {
		dd.resetConstantCache();
	}

	@Override
	public byte[] buildWebImage(Image webImage) {
		if (webImage instanceof WebImage) {
			return ((WebImage) webImage).toMessage(dd).toByteArray();
		} else if (webImage instanceof VolatileWebImageWrapper) {
			return ((VolatileWebImageWrapper) webImage).getWebImage().toMessage(dd).toByteArray();
		}
		return null;
	}

	@Override
	public boolean isDirty(Image webImage) {
		if (webImage instanceof WebImage) {
			return ((WebImage) webImage).isDirty();
		} else if (webImage instanceof VolatileWebImageWrapper) {
			return ((VolatileWebImageWrapper) webImage).getWebImage().isDirty();
		}
		return false;
	}

	@Override
	public VolatileImage createVolatileImage(int width, int height, ImageCapabilities caps, int transparency) {
		return dd.createVolatileImage(width, height, caps);
	}

	@Override
	public void resetImage(Image webImage) {
		if (webImage instanceof WebImage) {
			((WebImage) webImage).reset();
		} else if (webImage instanceof VolatileWebImageWrapper) {
			((VolatileWebImageWrapper) webImage).getWebImage().reset();
		}
	}

	@Override
	public void resetImageBeforeRepaint(Image webImage) {
		if (webImage instanceof WebImage) {
			((WebImage) webImage).resetBeforeRepaint();
		} else if (webImage instanceof VolatileWebImageWrapper) {
			((VolatileWebImageWrapper) webImage).getWebImage().resetBeforeRepaint();
		}
	}
}
