package liquibase.samples.osgi.jdbc;

import static aQute.bnd.annotation.component.ConfigurationPolicy.require;

import java.sql.Connection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import liquibase.integration.osgi.Liquibase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

@Component(configurationPolicy = require)
public class DataSourceProvider {

	private Liquibase liquibase;

	private DataSourceFactory dsf;

	private ServiceRegistration<DataSource> dsReg;

	@Reference
	protected void setLiquibase(Liquibase liquibase) {
		this.liquibase = liquibase;
	}

	@Reference
	protected void setDataSourceFactory(DataSourceFactory dsf) {
		this.dsf = dsf;
	}

	@Activate
	protected void activate(BundleContext context, Map<String, String> compProps) {
		Properties dsfProps = new Properties();
		for(String key : compProps.keySet()) {
			if(key.startsWith("jdbc.")) {
				dsfProps.put(key.substring(5), compProps.get(key));
			}
		}

		try {
			DataSource ds = dsf.createDataSource(dsfProps);

			Connection conn = ds.getConnection();
			String changeLogFile = compProps.get("liquibase.changelog");
			liquibase.open(changeLogFile, conn);
			
			liquibase.update(null);
			conn.close();

			Dictionary<String, Object> dsProps = new Hashtable<String, Object>();
			dsProps.put("liquibase.updated", "true");
			dsReg = context.registerService(DataSource.class, ds, dsProps);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Deactivate
	protected void deactivate(BundleContext context) {
		if (dsReg != null) {
			dsReg.unregister();
		}
	}
}
