package liquibase.samples.osgi.jpa;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;
import liquibase.integration.osgi.Liquibase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component(configurationPolicy = ConfigurationPolicy.require)
public class EntityManagerProvider {

	private Liquibase liquibase;

	private DataSourceFactory dsf;

	private EntityManagerFactoryBuilder emfb;
	
	private ServiceRegistration<EntityManagerFactory> emfReg;

	private Map<String, Object> emfbProperties;

	@Reference
	protected void setLiquibase(Liquibase liquibase) {
		this.liquibase = liquibase;
	}

	@Reference
	protected void setDataSourceFactory(DataSourceFactory dsf) {
		this.dsf = dsf;
	}

	@Reference
	protected void setEntityManagerFactoryBuilder(
			EntityManagerFactoryBuilder emfb, Map<String,Object> emfbProperties) {
		this.emfb = emfb;
		this.emfbProperties = emfbProperties;
	}

	@Activate
	protected void activate(BundleContext context, Map<String, String> compProps) {
		DataSource ds;
		try {
			ds = createDataSource(compProps);
		} catch (Exception e) {
			System.out.println("filed to create DataSource");
			e.printStackTrace();
			return;
		}
		
		String changeLogFile = compProps.get("liquibase.changelog");
		try {
			updateSchema(ds, changeLogFile);
		} catch (Exception e) {
			System.out.println("filed to update database schema");
			e.printStackTrace();
			return;
		}
		
		Dictionary<String, Object> emfProperties = createEmfProperties(compProps);
		EntityManagerFactory emf = createEntityManagerFactory(emfProperties);
		
		emfReg = context.registerService(EntityManagerFactory.class, emf, emfProperties);
	}

	@Deactivate
	protected void deactivate() {
		if (emfReg != null) {
			emfReg.unregister();
		}
	}

	private DataSource createDataSource(Map<String, String> compProps)
			throws SQLException {
		Properties dsfProps = new Properties();
		for (String key : compProps.keySet()) {
			if (key.startsWith("jdbc.")) {
				dsfProps.put(key.substring(5), compProps.get(key));
			}
		}
		return dsf.createDataSource(dsfProps);
	}

	private void updateSchema(DataSource dataSource, String changeLogFile)
			throws SQLException, LiquibaseException {
		Connection conn = dataSource.getConnection();
		try {
			liquibase.open(changeLogFile, conn);
			Writer out = new OutputStreamWriter(System.out);
			liquibase.update(null);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
	}

	private EntityManagerFactory createEntityManagerFactory(
			Dictionary<String, Object> emfProperties) {
		Map<String, Object> emfPropertiesMap = new HashMap<String, Object>();
		Enumeration<String> keys = emfProperties.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			emfPropertiesMap.put(key, emfProperties.get(key));
		}
		return emfb.createEntityManagerFactory(emfPropertiesMap);
	}
	
	private Dictionary<String, Object> createEmfProperties(
			Map<String, String> compProps) {
		Dictionary<String, Object> emfProps = new Hashtable<String, Object>();
		for (String key : compProps.keySet()) {
			if (key.startsWith("jdbc.")) {
				emfProps.put("javax.persistence." + key, compProps.get(key));
			}
		}
		for(String key : emfbProperties.keySet()) {
			if(key.startsWith("osgi.unit.") || key.equals("osgi.managed.bundles")) {
				emfProps.put(key, emfbProperties.get(key));
			}
		}
		return emfProps;
	}
}
