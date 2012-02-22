package liquibase.integration.osgi;

import java.io.Writer;
import java.util.Date;

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
 * @author rafal.krzewski@caltha.pl
 */
public interface LiquibaseFacade {

	void update(String contexts, Writer output) throws LiquibaseException;

	void update(int changesToApply, String contexts, Writer output)
			throws LiquibaseException;

	void rollback(int changesToRollback, String contexts, Writer output)
			throws LiquibaseException;

	void rollback(String tagToRollBackTo, String contexts, Writer output)
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

	void reportStatus(boolean verbose, String contexts, Writer output)
			throws LiquibaseException;

	void clearCheckSums() throws LiquibaseException;

	void validate() throws LiquibaseException;

	void setChangeLogParameter(String key, Object value)
			throws LiquibaseException;
}
