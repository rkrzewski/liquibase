package liquibase.samples.osgi;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.Dictionary;
import java.util.Hashtable;
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

@Component
public class DataSourceProvider {

	private static final String JDBC_URL = "jdbc:derby:target/derby/db;create=true";

	private static final String CHANGELOG = "META-INF/liquibase/changelog.xml";

	private Liquibase liquibase;

	private DataSourceFactory dsf;
	
	private ServiceRegistration<DataSource> dsReg;

	@Reference
	protected void setLiquibaseService(Liquibase liquibase) {
		this.liquibase = liquibase;
	}

	@Reference(target = "(&(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)"
			+ "(osgi.jdbc.driver.version=4.0))")
	protected void setDataSourceFactory(DataSourceFactory dsf) {
		this.dsf = dsf;
	}

	@Activate
	protected void activate(BundleContext context) {
		Properties dsfProps = new Properties();
		dsfProps.put(DataSourceFactory.JDBC_URL, JDBC_URL);
		try {
			DataSource ds = dsf.createDataSource(dsfProps);
			Connection conn = ds.getConnection();

			liquibase.open(CHANGELOG, conn);
			Writer out = new OutputStreamWriter(System.out);
			liquibase.update(null, out);
			conn.close();
			
			Dictionary<String,Object> dsProps = new Hashtable<String,Object>();
			dsProps.put("liquibase.updated", "true");
			dsReg = context.registerService(DataSource.class, ds, dsProps);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Deactivate
	protected void deactivate(BundleContext context) {
		if(dsReg != null) {
			dsReg.unregister();
		}
	}
}
