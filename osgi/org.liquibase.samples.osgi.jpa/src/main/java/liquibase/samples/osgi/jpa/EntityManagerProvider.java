package liquibase.samples.osgi.jpa;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import liquibase.integration.osgi.Liquibase;
import liquibase.integration.osgi.SchemaUpdate;

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
			ds = SchemaUpdate.createDataSource(dsf, compProps);
		} catch (Exception e) {
			System.out.println("filed to create DataSource");
			e.printStackTrace();
			return;
		}
		
		try {
			PrintWriter pw = new PrintWriter(
					new OutputStreamWriter(System.out), true);
			SchemaUpdate.updateSchema(ds, liquibase, pw, compProps);
		} catch (Exception e) {
			System.out.println("database schema validation / update failed");
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
