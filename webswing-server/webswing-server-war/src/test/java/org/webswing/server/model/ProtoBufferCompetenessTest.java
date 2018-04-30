package org.webswing.server.model;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.Before;
import org.junit.Test;
import org.webswing.model.c2s.InputEventMsgIn;
import org.webswing.model.c2s.InputEventsFrameMsgIn;
import org.webswing.model.c2s.SimpleEventMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.ApplicationInfoMsg;
import org.webswing.model.s2c.SimpleEventMsgOut;
import org.webswing.model.s2c.WindowMsg;
import org.webswing.model.s2c.WindowPartialContentMsg;
import org.webswing.server.model.proto.Webswing.AppFrameMsgOutProto;
import org.webswing.server.model.proto.Webswing.ApplicationInfoMsgProto;
import org.webswing.server.model.proto.Webswing.InputEventMsgInProto;
import org.webswing.server.model.proto.Webswing.InputEventsFrameMsgInProto;
import org.webswing.server.model.proto.Webswing.MouseEventMsgInProto;
import org.webswing.server.model.proto.Webswing.SimpleEventMsgInProto;
import org.webswing.server.model.proto.Webswing.SimpleEventMsgInProto.SimpleEventTypeProto;
import org.webswing.server.util.ProtoMapper;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message.Builder;

import sun.net.www.protocol.file.FileURLConnection;

@SuppressWarnings("restriction")
public class ProtoBufferCompetenessTest {

	private static final String inMsgs = "org.webswing.model.c2s";
	private static final String outMsgs = "org.webswing.model.s2c";
	private static final String jsLinkMsgs = "org.webswing.model.jslink";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCompletenessOfProtoBufferDefinition() throws ClassNotFoundException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		ArrayList<Class<?>> inMsgClasses = getClassesForPackage(inMsgs);
		ArrayList<Class<?>> outMsgClasses = getClassesForPackage(outMsgs);
		ArrayList<Class<?>> jsLinkMsgClasses = getClassesForPackage(jsLinkMsgs);
		ArrayList<Class<?>> allClasses = new ArrayList<Class<?>>();
		allClasses.addAll(inMsgClasses);
		allClasses.addAll(outMsgClasses);
		allClasses.addAll(jsLinkMsgClasses);
		Map<Class<?>, Class<?>> classProtoMap = new HashMap<Class<?>, Class<?>>();
		List<String> notFoundClasses = new ArrayList<String>();
		for (Class<?> c : allClasses) {
			try {
				String className = c.getName().substring(c.getName().lastIndexOf(".") + 1);
				String protoClassName = ProtoMapper.basicProtoPackage + "$" + (className.replaceAll("\\$", "Proto\\$")) + "Proto";
				Class<?> protoClass = cl.loadClass(protoClassName);
				classProtoMap.put(c, protoClass);
			} catch (Exception e) {
				e.printStackTrace();
				notFoundClasses.add(c.getName());
			}
		}
		assertTrue("Protocol Buffer counterpart class could not be resolved for these model classes: " + notFoundClasses, notFoundClasses.size() == 0);
		for (Class<?> c : classProtoMap.keySet()) {
			Class<?> protoClass = classProtoMap.get(c);
			if (c.isEnum()) {
				assertTrue("ProtoBuf class " + protoClass + " is not enum", protoClass.isEnum());
				List<?> enumCons = Arrays.asList(c.getEnumConstants());
				List<?> protoEnumCons = Arrays.asList(protoClass.getEnumConstants());
				for (Object e : enumCons) {
					Enum<?> enm = (Enum<?>) e;
					boolean found = false;
					for (Object pe : protoEnumCons) {
						Enum<?> penm = (Enum<?>) pe;
						if (penm.name().equals(enm.name())) {
							found = true;
						}
					}
					assertTrue("Enum " + enm.name() + " not found in " + protoClass, found);
				}
			} else {
				Field[] fields = c.getDeclaredFields();
				Builder b = null;
				try {
					b = (Builder) protoClass.getDeclaredMethod("newBuilder").invoke(null);
				} catch (Exception e) {
					assertTrue("NOT a protobuf model class" + classProtoMap.get(c), false);
				}
				for (Field field : fields) {
					if (Modifier.isStatic(field.getModifiers())) {
						continue;
					}
					field.setAccessible(true);
					String fieldName = field.getName();
					FieldDescriptor protoField = b.getDescriptorForType().findFieldByName(fieldName);
					assertTrue("Field " + fieldName + " not found in " + protoClass, protoField != null);
					if (List.class == field.getType()) {
						assertTrue("List field '" + fieldName + "' is not repeated in " + classProtoMap.get(c), protoField.isRepeated());
					} else {
						assertTrue("List field '" + fieldName + "' is repeated in " + classProtoMap.get(c), !protoField.isRepeated());
						if (protoField.getJavaType().equals(JavaType.MESSAGE)) {
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is not correct.", classProtoMap.get(field.getType()).getSimpleName().equals(protoField.toProto().getTypeName().substring(protoField.toProto().getTypeName().lastIndexOf(".") + 1)));
						} else if (protoField.getJavaType().equals(JavaType.ENUM)) {
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is not correct.", classProtoMap.get(field.getType()).getSimpleName().equals(protoField.toProto().getTypeName().substring(protoField.toProto().getTypeName().lastIndexOf(".") + 1)));
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is Enum.", field.getType().isEnum());
						} else if (protoField.getJavaType().equals(JavaType.BOOLEAN)) {
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is boolean", Boolean.class == field.getType() || Boolean.TYPE == field.getType());
						} else if (protoField.getJavaType().equals(JavaType.BYTE_STRING)) {
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is bytess", byte[].class.isAssignableFrom(field.getType()));
						} else if (protoField.getJavaType().equals(JavaType.INT)) {
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is integer", Integer.class == field.getType() || Integer.TYPE == field.getType());
						} else if (protoField.getJavaType().equals(JavaType.STRING)) {
							assertTrue("Type of field '" + fieldName + "' in " + classProtoMap.get(c) + " is string", String.class == field.getType());
						}
					}
				}
			}
		}
	}

	@Test
	public void testEncoding1() throws IOException {
		AppFrameMsgOut m = new AppFrameMsgOut();
		ApplicationInfoMsg a1 = new ApplicationInfoMsg();
		a1.setBase64Icon(new byte[] { 1, 2, 3, 4 });
		a1.setName("a1");
		ApplicationInfoMsg a2 = new ApplicationInfoMsg();
		a2.setBase64Icon(new byte[] { 4, 3, 2, 1 });
		a2.setName("a2");
		m.setApplications(Arrays.asList(a1, a2));

		ProtoMapper pm = new ProtoMapper();

		byte[] encoded = pm.encodeProto(m);
		AppFrameMsgOutProto decoded = AppFrameMsgOutProto.parseFrom(encoded);
		ApplicationInfoMsgProto da1 = decoded.getApplications(0);
		assertTrue("name not equals", da1.getName().equals("a1"));
		assertTrue("data not equals", Arrays.equals(da1.getBase64Icon().toByteArray(), new byte[] { 1, 2, 3, 4 }));
		ApplicationInfoMsgProto da2 = decoded.getApplications(1);
		assertTrue("name not equals", da2.getName().equals("a2"));
		assertTrue("data not equals", Arrays.equals(da2.getBase64Icon().toByteArray(), new byte[] { 4, 3, 2, 1 }));
	}

	@Test
	public void testEncoding2() throws IOException {
		AppFrameMsgOut m = SimpleEventMsgOut.continueOldSession.buildMsgOut();

		ProtoMapper pm = new ProtoMapper();

		byte[] encoded = pm.encodeProto(m);
		AppFrameMsgOutProto decoded = AppFrameMsgOutProto.parseFrom(encoded);
		assertTrue(decoded.getEvent().name().equals(SimpleEventMsgOut.continueOldSession.name()));
	}

	@Test
	public void testEncoding3() throws IOException {
		AppFrameMsgOut m = new AppFrameMsgOut();
		WindowMsg w1 = new WindowMsg();
		WindowPartialContentMsg content = new WindowPartialContentMsg();
		content.setPositionX(1);
		content.setPositionY(2);
		content.setWidth(3);
		content.setHeight(4);
		content.setBase64Content(new byte[] { 3, 2, 1 });
		w1.setContent(Arrays.asList(content));
		w1.setDirectDraw(new byte[] { 1, 2, 3 });
		w1.setPosX(1);
		w1.setPosY(2);
		w1.setTitle("title");
		w1.setId("id");
		m.setWindows(Arrays.asList(w1));
		ProtoMapper pm = new ProtoMapper();
		byte[] encoded = pm.encodeProto(m);
		AppFrameMsgOutProto decoded = AppFrameMsgOutProto.parseFrom(encoded);

		assertTrue(decoded.getWindows(0).getContent(0).getPositionX() == 1);
		assertTrue(Arrays.equals(decoded.getWindows(0).getContent(0).getBase64Content().toByteArray(), new byte[] { 3, 2, 1 }));
		assertTrue(decoded.getWindows(0).getTitle().equals("title"));
		assertTrue(decoded.getWindows(0).getPosX() == 1);

	}

	@Test
	public void testDecoding() throws IOException {
		org.webswing.server.model.proto.Webswing.InputEventMsgInProto.Builder b = InputEventMsgInProto.newBuilder();

		org.webswing.server.model.proto.Webswing.SimpleEventMsgInProto.Builder seb = SimpleEventMsgInProto.newBuilder();
		seb.setType(SimpleEventTypeProto.killSwing);
		b.setEvent(seb.build());
		ProtoMapper pm = new ProtoMapper();
		InputEventMsgIn ie = pm.decodeProto(b.build().toByteArray(), InputEventMsgIn.class);
		assertTrue(ie.getEvent().getType().equals(SimpleEventMsgIn.SimpleEventType.killSwing));
	}

	@Test
	public void testDecoding2() throws IOException {
		org.webswing.server.model.proto.Webswing.InputEventMsgInProto.Builder b = InputEventMsgInProto.newBuilder();

		org.webswing.server.model.proto.Webswing.MouseEventMsgInProto.Builder meb = MouseEventMsgInProto.newBuilder();
		meb.setAlt(true);
		meb.setCtrl(false);
		meb.setX(1);
		meb.setY(2);
		b.setMouse(meb.build());
		ProtoMapper pm = new ProtoMapper();
		InputEventMsgIn ie = pm.decodeProto(b.build().toByteArray(), InputEventMsgIn.class);
		assertTrue(ie.getMouse().isAlt());
		assertTrue(!ie.getMouse().isCtrl());
		assertTrue(ie.getMouse().getX() == 1);
		assertTrue(ie.getMouse().getY() == 2);
	}

	@Test
	public void testDecoding3() throws IOException {
		org.webswing.server.model.proto.Webswing.InputEventsFrameMsgInProto.Builder b = InputEventsFrameMsgInProto.newBuilder();

		org.webswing.server.model.proto.Webswing.InputEventMsgInProto.Builder ieb2 = InputEventMsgInProto.newBuilder();
		org.webswing.server.model.proto.Webswing.SimpleEventMsgInProto.Builder seb = SimpleEventMsgInProto.newBuilder();
		seb.setType(SimpleEventTypeProto.killSwing);
		ieb2.setEvent(seb.build());

		org.webswing.server.model.proto.Webswing.InputEventMsgInProto.Builder ieb = InputEventMsgInProto.newBuilder();
		org.webswing.server.model.proto.Webswing.MouseEventMsgInProto.Builder meb = MouseEventMsgInProto.newBuilder();
		meb.setAlt(true);
		meb.setCtrl(false);
		meb.setX(1);
		meb.setY(2);
		ieb.setMouse(meb.build());

		b.addEvents(ieb);
		b.addEvents(ieb2);
		ProtoMapper pm = new ProtoMapper();
		InputEventsFrameMsgIn ie = pm.decodeProto(b.build().toByteArray(), InputEventsFrameMsgIn.class);
		assertTrue(ie.getEvents().get(0).getMouse().isAlt());
		assertTrue(!ie.getEvents().get(0).getMouse().isCtrl());
		assertTrue(ie.getEvents().get(0).getMouse().getX() == 1);
		assertTrue(ie.getEvents().get(0).getMouse().getY() == 2);
		assertTrue(ie.getEvents().get(1).getEvent().getType().equals(SimpleEventMsgIn.SimpleEventType.killSwing));
	}

	/**
	 * Private helper method
	 * 
	 * @param directory
	 *            The directory to start with
	 * @param pckgname
	 *            The package name to search for. Will be needed for getting the Class object.
	 * @param classes
	 *            if a file isn't loaded but still is in the directory
	 * @throws ClassNotFoundException
	 */
	private static void checkDirectory(File directory, String pckgname, ArrayList<Class<?>> classes) throws ClassNotFoundException {
		File tmpDirectory;

		if (directory.exists() && directory.isDirectory()) {
			final String[] files = directory.list();

			for (final String file : files) {
				if (file.endsWith(".class")) {
					try {
						classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
					} catch (final NoClassDefFoundError e) {
						// do nothing. this class hasn't been found by the
						// loader, and we don't care.
					}
				} else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
					checkDirectory(tmpDirectory, pckgname + "." + file, classes);
				}
			}
		}
	}

	/**
	 * Private helper method.
	 * 
	 * @param connection
	 *            the connection to the jar
	 * @param pckgname
	 *            the package name to search for
	 * @param classes
	 *            the current ArrayList of all classes. This method will simply add new classes.
	 * @throws ClassNotFoundException
	 *             if a file isn't loaded but still is in the jar file
	 * @throws IOException
	 *             if it can't correctly read from the jar file.
	 */
	private static void checkJarFile(JarURLConnection connection, String pckgname, ArrayList<Class<?>> classes) throws ClassNotFoundException, IOException {
		final JarFile jarFile = connection.getJarFile();
		final Enumeration<JarEntry> entries = jarFile.entries();
		String name;

		for (JarEntry jarEntry = null; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null);) {
			name = jarEntry.getName();

			if (name.contains(".class")) {
				name = name.substring(0, name.length() - 6).replace('/', '.');

				if (name.contains(pckgname)) {
					classes.add(Class.forName(name));
				}
			}
		}
	}

	/**
	 * Attempts to list all the classes in the specified package as determined by the context class loader
	 * 
	 * @param pckgname
	 *            the package name to search
	 * @return a list of classes that exist within that package
	 * @throws ClassNotFoundException
	 *             if something went wrong
	 */
	public static ArrayList<Class<?>> getClassesForPackage(String pckgname) throws ClassNotFoundException {
		final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		try {
			final ClassLoader cld = Thread.currentThread().getContextClassLoader();

			if (cld == null)
				throw new ClassNotFoundException("Can't get class loader.");

			final Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
			URLConnection connection;

			for (URL url = null; resources.hasMoreElements() && ((url = resources.nextElement()) != null);) {
				try {
					connection = url.openConnection();

					if (connection instanceof JarURLConnection) {
						checkJarFile((JarURLConnection) connection, pckgname, classes);
					} else if (connection instanceof FileURLConnection) {
						try {
							checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname, classes);
						} catch (final UnsupportedEncodingException ex) {
							throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)", ex);
						}
					} else
						throw new ClassNotFoundException(pckgname + " (" + url.getPath() + ") does not appear to be a valid package");
				} catch (final IOException ioex) {
					throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, ioex);
				}
			}
		} catch (final NullPointerException ex) {
			throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)", ex);
		} catch (final IOException ioex) {
			throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, ioex);
		}

		return classes;
	}
}
