package org.webswing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.RepaintManager;

import org.apache.commons.codec.binary.Base64;
import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.toolkit.VolatileWebImageWrapper;
import org.webswing.directdraw.toolkit.WebImage;
import org.webswing.services.impl.ddutil.FastDirectDrawServicesAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DrawServlet extends HttpServlet {

	private static final long serialVersionUID = 2084660222487051245L;

	public static DirectDraw dd = new DirectDraw(new FastDirectDrawServicesAdapter());
	
	protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		if (request.getPathInfo() != null && request.getPathInfo().contains("tests")) {
			String encoded = encode(getTestMethods());
			response.getWriter().print(encoded);
			return;
		}

		String testMethod = request.getParameter("test");
		boolean resetCache = request.getParameter("reset") != null;

		if (resetCache) {
			dd.resetConstantCache();
		}
		RepaintManager.setCurrentManager(new RepaintManager() {
			@Override
			public Image getVolatileOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
				return new VolatileWebImageWrapper(c.getGraphicsConfiguration().getImageCapabilities(), new WebImage(dd, proposedWidth, proposedHeight));
			}

			@Override
			public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
				return new WebImage(dd, proposedWidth, proposedHeight);
			}
		});
		JsonMsg json = new JsonMsg();
		draw(testMethod, json);

		String encoded = encode(json);
		response.getWriter().print(encoded);
		System.out.println(testMethod);
	}

	private void draw(String testMethod, JsonMsg json) {
		try {
			boolean success = true;
			Method m = Tests.class.getDeclaredMethod(testMethod, Graphics2D.class, int.class);
			for (int j = 0; success; j++) {
				// image
				Image i = getImage(false);
				Graphics g = i.getGraphics();
				long start = System.currentTimeMillis();
				success = (Boolean) m.invoke(null, g, j);
				if (success) {
					json.originalImg.add(encodeImage((BufferedImage) i));
					json.originalRenderTime += (System.currentTimeMillis() - start);
					json.originalRenderSize += getPngImage((BufferedImage) i).length;
					g.dispose();

					// webimage
					Image wi = getImage(true);
					Graphics g2 = wi.getGraphics();
					start = System.currentTimeMillis();
					m.invoke(null, g2, j);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					((WebImage) wi).extractReadOnlyWebImage(true).toMessage(dd).writeTo(baos);
					json.protoImg.add(encodeBytes(baos.toByteArray()));
					json.protoRenderTime += (System.currentTimeMillis() - start);
					json.protoRenderSize += baos.size();
					g2.dispose();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Image getImage(boolean web) {
		return web ? dd.createImage(500, 100) : new BufferedImage(500, 100, BufferedImage.TYPE_INT_ARGB);
	}

	public static Image getImage(boolean web, int width, int height) {
		return web ? dd.createImage(width, height) : new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String encodeImage(BufferedImage window) {
		return Base64.encodeBase64String(getPngImage(window));
	}

	public static String encodeBytes(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}

	public static byte[] getPngImage(BufferedImage imageContent) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(imageContent, "png", baos);
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String[] getTestMethods() {
		List<String> result = new ArrayList<String>();
		for (Method m : Tests.class.getDeclaredMethods()) {
			if (m.getName().endsWith("Test"))
				result.add(m.getName());
		}
		Collections.sort(result);
		return result.toArray(new String[result.size()]);
	}

	public static String encode(Serializable m) {
		try {
			if (m instanceof String) {
				return (String) m;
			}
			return mapper.writeValueAsString(m);
		} catch (IOException e) {
			return null;
		}
	}
}
