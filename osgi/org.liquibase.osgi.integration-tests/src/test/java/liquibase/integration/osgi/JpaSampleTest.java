package liquibase.integration.osgi;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.repository;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import liquibase.samples.osgi.jpa.model.Account;
import liquibase.samples.osgi.jpa.model.Customer;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.util.Filter;

@RunWith(JUnit4TestRunner.class)
public class JpaSampleTest {

	@Configuration
	public Option[] paxExamConfig() {
		return options(
				repositories(
						repository("http://repo1.maven.org/maven2@id=cerntral"),
						repository("https://raw.github.com/rkrzewski/liquibase/master/osgi/cnf/jdbc@id=liquibase-gemini-dbconnect"),
						repository("https://raw.github.com/rkrzewski/liquibase/master/osgi/cnf/jpa@id=liquibase-gemini-jpa")),
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
				mavenBundle(maven("org.eclipse.persistence", "asm", "2.3.2")),
				mavenBundle(maven("org.eclipse.persistence", "antlr", "2.3.2")),
				mavenBundle(maven("org.eclipse.persistence", "jpa", "2.3.2")),
				mavenBundle(maven("org.eclipse.persistence", "core", "2.3.2")),
				mavenBundle(maven("org.eclipse.persistence",
						"javax.persistence", "2.0.3")),
				mavenBundle(maven("org.eclipse.gemini", "jpa", "1.0.0")),
				mavenBundle(maven("org.liquibase.samples", "osgi-jpa")));
	}

	@Inject
	@Filter(value = "(osgi.unit.name=Accounts)")
	private EntityManagerFactory emf;

	@Test
	public void testPersistenceUnitAccess() throws SQLException {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Customer c = new Customer("Chan", "Jackie",
				"1034 KingFu Lane, Los Angeles, CA");
		em.persist(c);
		Account a = new Account(c);
		a.setBalance(100.0);
		em.persist(a);

		em.getTransaction().commit();

		TypedQuery<Account> q = em.createQuery("SELECT a FROM Account a",
				Account.class);
		List<Account> results = q.getResultList();
		System.out.println("\n*** Account Report ***");
		for (Account acct : results) {
			System.out.println("Account: " + acct);
		}
		em.close();
	}

	@After
	public void shutdownDB() {
		Driver driver = new EmbeddedDriver();
		try {
			driver.connect("jdbc:derby:target/derby/jpa-sample;shutdown=true",
					new Properties());
		} catch (SQLException e) {
			// OK - Derby driver throws an exception on successful shutdown
		}
	}
}
