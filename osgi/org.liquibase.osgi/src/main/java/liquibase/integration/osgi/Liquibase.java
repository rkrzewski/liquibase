package liquibase.integration.osgi;

import java.io.Writer;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import liquibase.exception.LiquibaseException;

/**
 * A simplified version of {@link liquibase.Liquibase} facade suitable for OSGi
 * clients.
 * 
 * <p>
 * The public API of {@link liquibase.Liquibase} facade is exposed through this
 * interface in a way that does not requires minimal number of package imports.
 * </p>
 * 
 * <p>
 * Liquibase Core bundle registers a service of this class using ServiceFactory
 * mechanism (see OSGi Core Specification ch. 5.6). This means that each
 * requesting bundle will receive a distinct instance of the service object. The
 * service object is aware of the client bundle and will use the bundles's
 * classloader to locate the requested changelog file. The framework caches the
 * service object therefore if a bundle acquires it more than once care must be
 * taken to schedule the operations appropriately.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public interface Liquibase {

	/**
	 * Prepare facade object for use.
	 * 
	 * <p>
	 * This method must be called before any other method is used.
	 * </p>
	 * 
	 * @param changeLogFile
	 *            change log file. The path is relative to the client bundle's
	 *            classpath.
	 * @param connection
	 *            JDBC connection.
	 * 
	 * @throws LiquibaseException
	 *             when facade initialization fails, e.g. database is not
	 *             supported.
	 */
	void open(String changeLogFile, Connection connection)
			throws LiquibaseException;

	void update(String contexts) throws LiquibaseException;

	void update(String contexts, Writer output) throws LiquibaseException;

	void update(int changesToApply, String contexts) throws LiquibaseException;

	void update(int changesToApply, String contexts, Writer output)
			throws LiquibaseException;

	void rollback(int changesToRollback, String contexts)
			throws LiquibaseException;

	void rollback(int changesToRollback, String contexts, Writer output)
			throws LiquibaseException;

	void rollback(String tagToRollBackTo, String contexts)
			throws LiquibaseException;

	void rollback(String tagToRollBackTo, String contexts, Writer output)
			throws LiquibaseException;

	void rollback(Date dateToRollBackTo, String contexts)
			throws LiquibaseException;

	void rollback(Date dateToRollBackTo, String contexts, Writer output)
			throws LiquibaseException;

	void changeLogSync(String contexts, Writer output)
			throws LiquibaseException;

	void markNextChangeSetRan(String contexts, Writer output)
			throws LiquibaseException;

	void futureRollbackSQL(String contexts, Writer output)
			throws LiquibaseException;

	void dropAll() throws LiquibaseException;

	void dropAll(String... schemata) throws LiquibaseException;

	void tag(String tag) throws LiquibaseException;

	void updateTestingRollback(String contexts) throws LiquibaseException;

	boolean isSafeToRunMigration() throws LiquibaseException;

	void reportLocks(Writer output) throws LiquibaseException;

	void forceReleaseLocks() throws LiquibaseException;

	List<String> listUnrunChangeSets(String contexts) throws LiquibaseException;

	void reportStatus(boolean verbose, String contexts, Writer output)
			throws LiquibaseException;

	void clearCheckSums() throws LiquibaseException;

	void validate() throws LiquibaseException;

	void setChangeLogParameter(String key, Object value)
			throws LiquibaseException;
}
