package liquibase.samples.osgi;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import liquibase.integration.osgi.Liquibase;

import org.osgi.service.jdbc.DataSourceFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class TestComponent {

	private static final String JDBC_URL = "jdbc:derby:target/derby/db;create=true";

	private static final String CHANGELOG = "META-INF/liquibase/changelog.xml";
	
	private Liquibase liquibase;

	private DataSourceFactory dsf;

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
	protected void activate() {
		Connection conn = null;
		try {
			conn = getConnection(dsf);
			liquibase.open(CHANGELOG, conn);
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
