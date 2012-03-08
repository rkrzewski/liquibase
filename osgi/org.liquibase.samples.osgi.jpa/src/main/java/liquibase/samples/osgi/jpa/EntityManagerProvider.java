package liquibase.samples.osgi.jpa;

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
	
	private Map<String, Object> emfbProps;

	private ServiceRegistration<EntityManagerFactory> emfReg;

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
			EntityManagerFactoryBuilder emfb, Map<String, Object> emfbProperties) {
		this.emfb = emfb;
		this.emfbProps = emfbProperties;
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
			SchemaUpdate.updateSchema(ds, liquibase, System.out, compProps);
		} catch (Exception e) {
			System.out.println("database schema validation / update failed");
			e.printStackTrace();
			return;
		}

		try {
			Hashtable<String, Object> emfProperties = createEmfProperties(
					emfbProps, compProps);
			EntityManagerFactory emf = emfb
					.createEntityManagerFactory(emfProperties);
			emfReg = context.registerService(EntityManagerFactory.class, emf,
					emfProperties);
		} catch (Exception e) {
			System.out.println("JPA unit initialization failed");
			e.printStackTrace();
		}
	}

	@Deactivate
	protected void deactivate() {
		if (emfReg != null) {
			emfReg.unregister();
		}
	}

	private Hashtable<String, Object> createEmfProperties(
			Map<String, Object> emfbProperties, Map<String, String> compProps) {
		Hashtable<String, Object> emfProps = new Hashtable<String, Object>();

		for (String key : compProps.keySet()) {
			if (key.startsWith("jdbc.")) {
				emfProps.put("javax.persistence." + key, compProps.get(key));
			}
		}

		for (String key : emfbProperties.keySet()) {
			if (key.startsWith("osgi.unit.")
					|| key.equals("osgi.managed.bundles")) {
				emfProps.put(key, emfbProperties.get(key));
			}
		}
		return emfProps;
	}
}
