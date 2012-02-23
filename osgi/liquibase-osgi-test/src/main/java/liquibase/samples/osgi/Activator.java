package liquibase.samples.osgi;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import liquibase.integration.osgi.LiquibaseFacade;
import liquibase.integration.osgi.LiquibaseService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.service.jdbc.DataSourceFactory;

public class Activator implements BundleActivator {

	private static final String DSF_FILTER = "(&(objectclass=org.osgi.service.jdbc.DataSourceFactory)"
			+ "(osgi.jdbc.driver.class=org.apache.derby.jdbc.EmbeddedDriver)"
			+ "(osgi.jdbc.driver.version=4.0))";

	private static final String LS_FILTER = "(objectclass=liquibase.integration.osgi.LiquibaseService)";

	private static final String JDBC_URL = "jdbc:derby:target/derby/db;create=true";

	private static final String CHANGELOG = "META-INF/liquibase/changelog.xml";

	private MultiServiceTracker<State, Process> tracker;

	public void start(BundleContext context) throws Exception {
		Filter filter = context.createFilter("(|" + DSF_FILTER + LS_FILTER
				+ ")");
		tracker = new MultiServiceTracker<State, Process>(context, filter,
				new State(), new Process());
		tracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		tracker.close();
	}

	private static class State extends MultiServiceTracker.State {
		
		private DataSourceFactory dsf;

		private LiquibaseService ls;
	}

	private static class Process extends MultiServiceTracker.Process<State> {

		private boolean started;

		@Override
		public void enter(State state) {
			if (state.dsf != null && state.ls != null && !started) {
				runUpdate(state.dsf, state.ls);
				started = true;
			}
		}

		private void runUpdate(DataSourceFactory dsf, LiquibaseService ls) {
			Connection conn = null;
			try {
				conn = getConnection(dsf);
				LiquibaseFacade liquibase = ls.getInstance(CHANGELOG, conn);
				Writer out = new OutputStreamWriter(System.out);
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

		private Connection getConnection(DataSourceFactory dsf)
				throws SQLException {
			Properties props = new Properties();
			props.put(DataSourceFactory.JDBC_URL, JDBC_URL);
			DataSource ds = dsf.createDataSource(props);
			return ds.getConnection();
		}
	}
}
