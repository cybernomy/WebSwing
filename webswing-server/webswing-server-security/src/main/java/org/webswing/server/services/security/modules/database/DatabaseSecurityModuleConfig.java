package org.webswing.server.services.security.modules.database;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueBoolean;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueNumber;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueObject;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueString;
import org.webswing.server.common.model.meta.ConfigFieldDiscriminator;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.common.model.meta.ConfigFieldPresets;
import org.webswing.server.common.model.meta.ConfigType;
import org.webswing.server.common.model.meta.MetadataGenerator;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;
import org.webswing.server.services.security.modules.database.DatabaseSecurityModuleConfig.DatabaseSecurityModuleConfigMetaGenerator;

@ConfigType(metadataGenerator = DatabaseSecurityModuleConfigMetaGenerator.class)
@ConfigFieldOrder({ "dataSourceClass", "dataSourceProperties", "authenticationQuery", "userRolesQuery", "permissionsQuery", "permissionsEnabled", "passwordSalted", "hashAlgorithm" })
public interface DatabaseSecurityModuleConfig extends WebswingExtendableSecurityModuleConfig {

	@ConfigField(label = "DataSource Class", description = "Database specific implementation of javax.sql.DataSource. Jar with this class should be available on classpath defined by security module.")
	@ConfigFieldPresets({ "org.apache.derby.jdbc.ClientDataSource", "org.firebirdsql.pool.FBSimpleDataSource", "org.h2.jdbcx.JdbcDataSource", "org.hsqldb.jdbc.JDBCDataSource", "com.ibm.db2.jcc.DB2SimpleDataSource", "com.informix.jdbcx.IfxDataSource", "com.microsoft.sqlserver.jdbc.SQLServerDataSource",
			"com.mysql.jdbc.jdbc2.optional.MysqlDataSource", "org.mariadb.jdbc.MySQLDataSource", "oracle.jdbc.pool.OracleDataSource", "com.orientechnologies.orient.jdbc.OrientDataSource", "com.impossibl.postgres.jdbc.PGDataSource", "org.postgresql.ds.PGSimpleDataSource", "com.sap.dbtech.jdbc.DriverSapDB", "org.sqlite.SQLiteDataSource",
			"com.sybase.jdbc4.jdbc.SybDataSource" })
	@ConfigFieldDiscriminator
	String getDataSourceClass();

	@ConfigField(label = "DataSource Settings", description = "Java bean property names and value pairs for the DataSource class specified.")
	@ConfigFieldDefaultValueObject(HashMap.class)
	Map<String, String> getDataSourceProperties();

	@ConfigField(label = "Authentication Query")
	@ConfigFieldDefaultValueString("select password, password_salt from users where username = ?")
	String getAuthenticationQuery();

	@ConfigField(label = "User Roles Query")
	@ConfigFieldDefaultValueString("select role_name from user_roles where username = ?")
	String getUserRolesQuery();

	@ConfigField(label = "Permissions Query")
	@ConfigFieldDefaultValueString("select permission from roles_permissions where role_name = ?")
	String getPermissionsQuery();

	@ConfigField(label = "Resolve Permissions")
	@ConfigFieldDefaultValueBoolean(false)
	boolean isPermissionsEnabled();

	@ConfigField(label = "Salted Password Hash")
	@ConfigFieldDefaultValueBoolean(true)
	boolean isPasswordSalted();

	@ConfigField(label = "Hash Matcher Algorithm")
	@ConfigFieldPresets(enumClass = HashType.class)
	@ConfigFieldDefaultValueString("NONE")
	String getHashAlgorithm();

	@ConfigField(label = "Hash Iterations")
	@ConfigFieldDefaultValueNumber(1)
	int getHashIterations();

	@ConfigField(label = "Hash Hex Encoded", description = "Select if password hash in stored as Hex value, otherwise Base64 encoded hash is expected.")
	@ConfigFieldDefaultValueBoolean(false)
	boolean getHashHexEncoded();

	public class DatabaseSecurityModuleConfigMetaGenerator extends MetadataGenerator<DatabaseSecurityModuleConfig> {
		@Override
		protected String[] getPresets(DatabaseSecurityModuleConfig config, ClassLoader cl, String propertyName, Method readMethod) {
			if (propertyName.equals("dataSourceProperties")) {
				try {
					List<String> presets = new ArrayList<>();
					Class<?> dsc = cl.loadClass(config.getDataSourceClass());
					BeanInfo bean = Introspector.getBeanInfo(dsc);
					for (PropertyDescriptor d : bean.getPropertyDescriptors()) {
						if (d.getWriteMethod() != null) {
							Class<?> type = d.getWriteMethod().getParameterTypes()[0];
							if (type == String.class || type == Boolean.TYPE || type == Integer.TYPE) {
								presets.add(d.getName());
							}
						}
					}
					return presets.toArray(new String[presets.size()]);
				} catch (Throwable e) {
					return null;
				}
			}
			return super.getPresets(config, cl, propertyName, readMethod);
		}
	}

}
