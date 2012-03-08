package liquibase.samples.osgi.jdbc;

import static aQute.bnd.annotation.component.ConfigurationPolicy.require;

import java.util.Map;

import javax.sql.DataSource;

import liquibase.integration.osgi.Liquibase;
import liquibase.integration.osgi.SchemaUpdate;

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
	protected void activate(BundleContext context,
			Map<String, String> properties) {
		DataSource ds;
		try {
			ds = SchemaUpdate.createDataSource(dsf, properties);
		} catch (Exception e) {
			System.out.println("filed to create DataSource");
			e.printStackTrace();
			return;
		}
		
		try {
			SchemaUpdate.updateSchema(ds, liquibase, System.out, properties);

			dsReg = context.registerService(DataSource.class, ds, null);
		} catch (Exception e) {
			System.out.println("database schema validation / update failed");
			e.printStackTrace();
			return;
		}
	}

	@Deactivate
	protected void deactivate(BundleContext context) {
		if (dsReg != null) {
			dsReg.unregister();
		}
	}
}
