package liquibase.integration.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.repository;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class JdbcSampleTest {

	@Configuration
	public Option[] paxExamConfig() {
		return options(
				repositories(
						repository("http://repo1.maven.org/maven2@id=central"),
						repository("https://raw.github.com/rkrzewski/liquibase/master/osgi/cnf/jdbc@id=liquibase-gemini-dbconnect")),
				junitBundles(),
				mavenBundle(maven("org.apache.felix",
						"org.apache.felix.fileinstall", "3.1.10")),
				mavenBundle(maven("org.apache.felix",
						"org.apache.felix.configadmin", "1.2.8")),
				mavenBundle(maven("org.apache.felix", "org.apache.felix.scr",
						"1.6.0")),
				mavenBundle(maven("org.osgi", "org.osgi.enterprise", "4.2.0")),
				mavenBundle(maven("org.eclipse",
						"org.eclipse.gemini.dbaccess.util", "1.1.0-SNAPSHOT")),
				mavenBundle(maven("org.eclipse",
						"org.eclipse.gemini.dbaccess.derby", "1.1.0-SNAPSHOT")),
				mavenBundle(maven("org.eclipse", "org.apache.derby", "10.8.2.2")),
				mavenBundle(maven("org.liquibase", "liquibase-osgi")),
				mavenBundle(maven("org.liquibase.samples", "osgi-jdbc")));
	}

	@Inject
	private DataSource dataSource;

	@Test
	public void testDataSourceAccess() throws SQLException {
		assertNotNull(dataSource);
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt
				.executeQuery("select count(*) from fruit where color='green'");
		assert rset.isBeforeFirst();
		rset.next();
		assertEquals(1, rset.getInt(1));
		rset.close();
		stmt.close();
		conn.close();
	}

	@After
	public void shutdownDB() {
		Driver driver = new EmbeddedDriver();
		try {
			driver.connect("jdbc:derby:target/derby/jdbc-sample;shutdown=true",
					new Properties());
		} catch (SQLException e) {
			// OK - Derby driver throws an exception on successful shutdown
		}
	}
}
