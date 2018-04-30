package org.webswing.server.model;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.webswing.server.common.model.Config;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.common.model.SwingConfig.SessionMode;
import org.webswing.server.common.model.meta.ConfigContext;
import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueBoolean;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueNumber;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueObject;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueString;
import org.webswing.server.common.model.meta.ConfigFieldEditorType;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.common.model.meta.ConfigFieldEditorType.EditorType;
import org.webswing.server.common.model.meta.MetaField;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.common.util.ConfigUtil;
import org.webswing.server.common.util.WebswingObjectMapper;
import org.webswing.server.services.security.api.BuiltInModules;
import org.webswing.server.services.security.extension.onetimeurl.OtpAccessConfig;

@SuppressWarnings("unchecked")
public class ConfigurationMetadataTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConfigurationLoad() throws Exception {
		Map<String, Object> readValue = WebswingObjectMapper.get().readValue(this.getClass().getClassLoader().getResourceAsStream("swingConfig1.json"), Map.class);
		SecuredPathConfig spc = ConfigUtil.instantiateConfig(readValue, SecuredPathConfig.class);
		SwingConfig c = spc.getSwingConfig();
		assertTrue(spc.getPath(), "/ss3".equals(spc.getPath()));
		assertTrue("${user.dir}/demo/SwingSet3/SwingSet3.jar".equals(c.getClassPathEntries().get(0)));
		assertTrue(c.getFontConfig().get("test1").equals("value1"));
		assertTrue(c.getMaxClients() == 2);
		assertTrue(c.getSessionMode().equals(SessionMode.CONTINUE_FOR_BROWSER));
		assertTrue(c.isAllowStealSession());
		assertTrue(c.getUploadMaxSize() == 5.0);
	}

	@Test
	public void testMetadataGenerator() throws Exception {
		Map<String, Object> readValue = WebswingObjectMapper.get().readValue(this.getClass().getClassLoader().getResourceAsStream("swingConfig1.json"), Map.class);
		SecuredPathConfig c = ConfigUtil.instantiateConfig(readValue, SecuredPathConfig.class);
		MetaObject configMetadata = ConfigUtil.getConfigMetadata(c, this.getClass().getClassLoader(), new MockConfigContext());

		//@ConfigFieldEditorType(editor = EditorType.Object, className = "org.webswing.server.services.security.api.WebswingSecurityConfig")
		for (MetaField f : configMetadata.getFields()) {
			if (f.getName().equals("security")) {
				assertTrue(f.getType().equals(EditorType.Object));
				assertTrue(f.getValue() instanceof MetaObject);
			}
		}

		//@ConfigFieldOrder({ "path", "homeDir", "webFolder","langFolder" "icon", "security", "swingConfig"})
		assertTrue(configMetadata.getFields().get(1).getName().equals("path"));
		assertTrue(configMetadata.getFields().get(2).getName().equals("homeDir"));
		assertTrue(configMetadata.getFields().get(3).getName().equals("webFolder"));
		assertTrue(configMetadata.getFields().get(6).getName().equals("security"));

	}

	@Test
	public void testTableMetadataGenerator() throws Exception {
		TestDefaultConfig c = ConfigUtil.instantiateConfig(null, TestDefaultConfig.class);
		MetaObject m = ConfigUtil.getConfigMetadata(c, this.getClass().getClassLoader(), new MockConfigContext());
		assertTrue(m.getFields().get(0).getType().equals(EditorType.ObjectListAsTable));
		MetaField tf = m.getFields().get(0);
		assertTrue(tf.getTableColumns().get(0).getName().equals("string"));
	}

	@Test
	public void testDefaultValues() throws Exception {
		TestDefaultConfig c = ConfigUtil.instantiateConfig(null, TestDefaultConfig.class);
		assertTrue("defaultValue".equals(c.getString()));
		assertTrue(BuiltInModules.INHERITED.equals(c.getEnum()));
		assertTrue(c.getBool());
		assertTrue(c.getBoolean());
		assertTrue(1 == c.getI());
		assertTrue(1 == c.getD());
		assertTrue(1 == c.getF());
		assertTrue(1 == c.getInteger());
		assertTrue(1 == c.getDouble());
		assertTrue(1 == c.getFloat());
		assertTrue(c.getConfig() != null);
		assertTrue(c.getMap() != null);
		assertTrue(c.getObject() != null);
		assertTrue(c.getConfigNull() == null);
		assertTrue(c.getMapNull() == null);
		assertTrue(c.getObjectNull() == null);
	}

	@Test
	public void testDefaultValueGenerator() throws Exception {
		OtpAccessConfig c = ConfigUtil.instantiateConfig(null, OtpAccessConfig.class);
		assertTrue(c.getSecret() != null);
	}

	@Test
	public void testNumberType() throws Exception {
		//inteter
		TestDefaultConfig c = numConfig(99, "i");
		assertTrue(c.getI() == 99);
		c = numConfig(99, "integer");
		assertTrue(c.getInteger() == 99);
		//double
		c = numConfig(99.99, "d");
		assertTrue(c.getD() == 99.99);
		c = numConfig(99.99, "double");
		assertTrue(c.getDouble() == 99.99);
		//float
		c = numConfig(99.99f, "f");
		assertTrue(c.getF() == 99.99f);
		c = numConfig(99.99f, "float");
		assertTrue(c.getFloat() == 99.99f);

		//int->double
		c = numConfig(99, "d");
		assertTrue(c.getD() == 99);
		c = numConfig(99, "double");
		assertTrue(c.getDouble() == 99);

		//double->int
		c = numConfig(99.99d, "i");
		assertTrue(c.getI() == 99);
		c = numConfig(99.99d, "integer");
		assertTrue(c.getInteger() == 99);

		//double->float
		c = numConfig(99.99d, "f");
		assertTrue(c.getF() == 99.99f);
		c = numConfig(99.99d, "float");
		assertTrue(c.getFloat() == 99.99f);

		//float->double
		c = numConfig(99.99f, "d");
		assertTrue(c.getD() == 99.99f);
		c = numConfig(99.99f, "double");
		assertTrue(c.getDouble() == 99.99f);
	}

	private TestDefaultConfig numConfig(Number input, String field) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(field, input);
		return ConfigUtil.instantiateConfig(map, TestDefaultConfig.class);
	}

	@ConfigFieldOrder({ "table", "string" })
	public static interface TestDefaultConfig extends Config {
		@ConfigField
		@ConfigFieldEditorType(editor = EditorType.ObjectListAsTable)
		@ConfigFieldDefaultValueObject(ArrayList.class)
		List<EmbededConfig> getTable();

		@ConfigFieldDefaultValueString("defaultValue")
		String getString();

		@ConfigFieldDefaultValueString("INHERITED")
		BuiltInModules getEnum();

		@ConfigFieldDefaultValueBoolean(true)
		boolean getBool();

		@ConfigFieldDefaultValueBoolean(true)
		Boolean getBoolean();

		@ConfigFieldDefaultValueNumber(1)
		double getD();

		@ConfigFieldDefaultValueNumber(1)
		Double getDouble();

		@ConfigFieldDefaultValueNumber(1)
		int getI();

		@ConfigFieldDefaultValueNumber(1)
		Integer getInteger();

		@ConfigFieldDefaultValueNumber(1)
		float getF();

		@ConfigFieldDefaultValueNumber(1)
		Float getFloat();

		@ConfigFieldDefaultValueObject
		TestDefaultConfig getConfig();

		@ConfigFieldDefaultValueObject(HashMap.class)
		Map<String, Object> getMap();

		@ConfigFieldDefaultValueObject
		Object getObject();

		TestDefaultConfig getConfigNull();

		Map<String, Object> getMapNull();

		Object getObjectNull();

	}

	@ConfigFieldOrder({ "object", "string" })
	public static interface EmbededConfig extends Config {
		@ConfigField
		Object getObject();

		@ConfigField
		String getString();

	}

	public class MockConfigContext implements ConfigContext {

		@Override
		public File resolveFile(String name) {
			return null;
		}

		@Override
		public String replaceVariables(String string) {
			return null;
		}

		@Override
		public URL getWebResource(String resource) {
			return null;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}
}
