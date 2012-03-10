package liquibase.integration.osgi;

import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repository;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import liquibase.exception.LiquibaseException;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Run basic sanity checks on {@code org.liquibase.osgi} bundle.
 * 
 * @author rafal.krzewski@caltha.pl
 */
@RunWith(JUnit4TestRunner.class)
public class SimpleIntegrationTest {

	@Configuration
	public Option[] paxExamConfig() {
		return options(
				repository("https://raw.github.com/rkrzewski/liquibase/master/osgi/cnf/jdbc@id=liquibase-gemini-dbconnect"),
				junitBundles(),
				mavenBundle(maven("org.liquibase", "liquibase-osgi")),
				mavenBundle(maven("org.osgi", "org.osgi.enterprise", "4.2.0")),
				mavenBundle(maven("org.eclipse", "org.apache.derby", "10.8.2.2")));
	}

	@Inject
	private Liquibase liquibase;

	/**
	 * Connect to Derby database directly and attempt to load and verify a blank
	 * changelog.
	 */
	@Test
	public void testBlankChangelog() throws LiquibaseException, SQLException {
		if (liquibase == null) {
			fail("liquibase.integration.osgi.Liquibase service not found");
		}
		Connection conn = openConnection();
		try {
			liquibase.open("blank.changelog.xml", conn);
			List<String> pending = liquibase.listUnrunChangeSets(null);
			System.out.println("detected " + pending.size()
					+ " pending changes");
		} finally {
			conn.close();
		}
	}

	private Connection openConnection() throws SQLException {
		Driver driver = new EmbeddedDriver();
		return driver.connect("jdbc:derby:target/derby/db;create=true",
				new Properties());
	}

	@After
	public void shutdownDB() throws SQLException {
		Driver driver = new EmbeddedDriver();
		try {
			driver.connect("jdbc:derby:target/derby/db;shutdown=true",
					new Properties());
		} catch (SQLException e) {
			// OK - Derby driver throws an exception on successful shutdown
		}
	}
}
