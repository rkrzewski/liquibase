package liquibase.samples.osgi;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import liquibase.integration.osgi.LiquibaseFacade;
import liquibase.integration.osgi.LiquibaseService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, ServiceTrackerCustomizer {

	private static final String JDBC_URL = "jdbc:derby:target/derby/db;create=true";

	private static final String CHANGELOG = "META-INF/liquibase/changelog.xml";

	private static final String LS_FILTER = "(objectclass=liquibase.integration.osgi.LiquibaseService)";

	private static final String DSF_FILTER = "(&(objectclass=org.osgi.service.jdbc.DataSourceFactory)"
			+ "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)"
			+ "(osgi.jdbc.driver.version=4.0))";

	private Executor executor = Executors.newSingleThreadExecutor();

	private Filter lsFilter;

	private ServiceTracker lsTracker;

	private LiquibaseService ls;

	private Filter dsfFilter;

	private ServiceTracker dsfTracker;

	private DataSourceFactory dsf;

	private boolean started = false;

	private BundleContext context;

	// BundleActivator

	public void start(BundleContext context) throws Exception {
		this.context = context;
		lsFilter = context.createFilter(LS_FILTER);
		dsfFilter = context.createFilter(DSF_FILTER);
		lsTracker = new ServiceTracker(context, lsFilter, this);
		dsfTracker = new ServiceTracker(context, dsfFilter, this);
		lsTracker.open();
		dsfTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		dsfTracker.close();
		lsTracker.close();
		started = false;
	}

	// ServiceTrackerCustomizer

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object addingService(ServiceReference reference) {
		Object object = context.getService(reference);
		if (lsFilter.match(reference)) {
			ls = (LiquibaseService) object;
		}
		if (dsfFilter.match(reference)) {
			dsf = (DataSourceFactory) object;
		}
		if (!started) {
			executor.execute(new RunUpdate());
		}
		return object;
	}

	@SuppressWarnings("rawtypes")
	public void modifiedService(ServiceReference reference, Object service) {
		// ignore
	}

	@SuppressWarnings("rawtypes")
	public void removedService(ServiceReference reference, Object service) {
		context.ungetService(reference);
	}

	//

	private class RunUpdate implements Runnable {

		public void run() {
			if (ls != null && dsf != null) {
				runUpdate(ls, dsf);
				started = true;
			}
		}
		
		private void runUpdate(LiquibaseService ls, DataSourceFactory dsf) {
			Connection conn = null;
			try {
				conn = getConnection(dsf);
				LiquibaseFacade liquibase = ls.getInstance(CHANGELOG, conn);
				Writer out = new OutputStreamWriter(System.out);
				liquibase.reportLocks(out);
				liquibase.forceReleaseLocks();
				liquibase.update(null, out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						// suppress
					}
				}
			}
		}
		
		private Connection getConnection(DataSourceFactory dsf) throws SQLException {
			Properties props = new Properties();
			props.put(DataSourceFactory.JDBC_URL, JDBC_URL);
			DataSource ds = dsf.createDataSource(props);
			return ds.getConnection();
		}
	}
}
