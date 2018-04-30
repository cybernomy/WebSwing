package org.webswing.server.common.util;

import main.Main;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.ProtectionDomain;
import java.util.*;

public class CommonUtil {
	public static final int bufferSize = 4 * 1024;
	private static final String DEFAULT = "default";
	private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);
	private static final Map<String, byte[]> iconMap = new HashMap<String, byte[]>();
	private static URLClassLoader swingBootClassLoader;

	public static byte[] loadImage(File iconFile) {
		String icon;
		if (iconFile == null || !iconFile.isFile()) {
			icon = null;
		} else {
			icon = iconFile.getAbsolutePath();
		}
		try {
			if (icon == null) {
				if (iconMap.containsKey(DEFAULT)) {
					return iconMap.get(DEFAULT);
				} else {
					BufferedImage defaultIcon = ImageIO.read(CommonUtil.class.getClassLoader().getResourceAsStream("images/java.png"));
					byte[] b64icon = getPngImage(defaultIcon);
					iconMap.put(DEFAULT, b64icon);
					return b64icon;
				}
			} else {
				if (iconMap.containsKey(icon)) {
					return iconMap.get(icon);
				} else {
					BufferedImage defaultIcon = ImageIO.read(new File(icon));
					byte[] b64icon = getPngImage(defaultIcon);
					iconMap.put(icon, b64icon);
					return b64icon;
				}
			}
		} catch (IOException e) {
			log.error("Failed to load image " + icon, e);
			return null;
		}
	}

	public static File resolveFile(String name, String homeDir, VariableSubstitutor subs) {
		if (name == null) {
			return null;
		}
		name = subs.replace(name);
		File relativeToHomeInRoot = new File(Main.getRootDir(), homeDir + File.separator + name).getAbsoluteFile();
		if (relativeToHomeInRoot.exists()) {
			return relativeToHomeInRoot;
		}
		File relativeToHome = new File(homeDir + File.separator + name).getAbsoluteFile();
		if (relativeToHome.exists()) {
			return relativeToHome;
		}
		File absolute = new File(name).getAbsoluteFile();
		if (absolute.exists()) {
			return absolute;
		}
		return null;
	}

	private static byte[] getPngImage(BufferedImage imageContent) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			ImageIO.write(imageContent, "png", ios);
			byte[] result = baos.toByteArray();
			baos.close();
			return result;
		} catch (IOException e) {
			log.error("Writing image interupted:" + e.getMessage(), e);
		}
		return null;
	}

	public static String getWarFileLocation() {
		String warFile = System.getProperty(Constants.WAR_FILE_LOCATION);
		if (warFile == null) {
			ProtectionDomain domain = Main.class.getProtectionDomain();
			URL location = domain.getCodeSource().getLocation();
			String locationString = location.toExternalForm();
			if (locationString.endsWith("/WEB-INF/classes/")) {
				locationString = locationString.substring(0, locationString.length() - "/WEB-INF/classes/".length());
			}
			System.setProperty(Constants.WAR_FILE_LOCATION, locationString);
			return locationString;
		}
		return warFile;
	}

	public static void transferStreams(InputStream is, OutputStream os) throws IOException {
		try {
			byte[] buf = new byte[bufferSize];
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1)
				os.write(buf, 0, bytesRead);
		} finally {
			is.close();
			os.close();
		}
	}

	public static File getConfigFile() {
		String configFile = System.getProperty(Constants.CONFIG_FILE_PATH);
		if (configFile == null) {
			String war = CommonUtil.getWarFileLocation();
			configFile = war.substring(0, war.lastIndexOf("/") + 1) + Constants.DEFAULT_CONFIG_FILE_NAME;
			System.setProperty(configFile, Constants.CONFIG_FILE_PATH);
		}
		File config = new File(URI.create(configFile));
		return config;
	}

	public static String generateClassPathString(Collection<String> classPathEntries) {
		String result = "";
		if (classPathEntries != null) {
			for (String cpe : classPathEntries) {
				result += cpe + ";";
			}
			result = result.length() > 0 ? result.substring(0, result.length() - 1) : result;
		}
		return result;
	}

	public static boolean isSubPath(String subpath, String path) {
		return path.equals(subpath) || path.startsWith(subpath + "/");
	}

	public static boolean isSubPathIgnoreCase(String subpath, String path) {
		return path.equalsIgnoreCase(subpath) || path.toLowerCase().startsWith(subpath.toLowerCase() + "/");
	}

	public static String toPath(String path) {
		String mapping = path == null ? "/" : path;
		mapping = mapping.startsWith("/") ? mapping : ("/" + mapping);
		mapping = mapping.endsWith("/") ? mapping.substring(0, mapping.length() - 1) : mapping;
		return mapping;
	}

	public static <T extends Annotation> T findAnnotation(Method readMethod, Class<T> ann) {
		T annotation = readMethod.getAnnotation(ann);
		if (annotation != null) {
			return annotation;
		} else {
			Set<Method> overrideHierarchy = MethodUtils.getOverrideHierarchy(readMethod, Interfaces.INCLUDE);
			for (Method m : overrideHierarchy) {
				annotation = m.getAnnotation(ann);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		return null;
	}

	private static URLClassLoader getSwingBootClassLoader() throws IOException {
		if (swingBootClassLoader == null) {
			URL swingBootFolder = null;
			if (new File(URI.create(getWarFileLocation())).isFile()) {
				swingBootFolder = new URL("jar:" + getWarFileLocation() + "!/WEB-INF/swing-boot");
			} else if (new File(URI.create(getWarFileLocation())).isDirectory()) {
				swingBootFolder = new URL(getWarFileLocation() + "WEB-INF/swing-boot");
			}
			List<URL> filesFromPath = Main.getFilesFromPath(swingBootFolder);
			swingBootClassLoader = new URLClassLoader(filesFromPath.toArray(new URL[filesFromPath.size()]));
		}
		return swingBootClassLoader;
	}

	public static String getBootClassPathForClass(String className) throws Exception {
		String classfile = className.replace(".", "/") + ".class";
		URL url = getSwingBootClassLoader().getResource(classfile);
		if (url != null) {
			String cp = URLDecoder.decode(url.getPath(), "UTF-8");
			if (cp.endsWith(classfile)) {
				cp = cp.substring(0, cp.length() - (classfile.length() + 2));
				if (cp.startsWith("file:")) {
					cp = cp.substring(5);
				}
				if (cp.startsWith("/") && cp.contains(":")) {//remove leading slash if windows
					cp = cp.substring(1);
				}
			}
			return "\"" + cp + "\"";

		} else {
			throw new IllegalStateException("Class " + className + " not found in bootclasspath folder of webswing-server.war. ");
		}
	}

	public static String addParam(String url, String param) {
		if (url.contains("?")) {
			url = url + "&" + param;
		} else {
			url = url + "?" + param;
		}
		return url;
	}
}
