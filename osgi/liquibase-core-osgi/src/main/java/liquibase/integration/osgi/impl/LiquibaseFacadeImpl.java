package liquibase.integration.osgi.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.LiquibaseException;
import liquibase.integration.osgi.LiquibaseFacade;
import liquibase.resource.ResourceAccessor;

/**
 * A trivial facade implementation.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class LiquibaseFacadeImpl implements LiquibaseFacade {

	private final Liquibase facade;

	public LiquibaseFacadeImpl(String changeLogFile,
			ResourceAccessor resourceAccessor, DatabaseConnection database)
			throws LiquibaseException {
		this.facade = new Liquibase(changeLogFile, resourceAccessor, database);
	}

	public void update(String contexts, Writer output)
			throws LiquibaseException {
		facade.update(contexts, output);
	}

	public void update(int changesToApply, String contexts, Writer output)
			throws LiquibaseException {
		facade.update(changesToApply, contexts, output);
	}

	public void rollback(int changesToRollback, String contexts, Writer output)
			throws LiquibaseException {
		facade.rollback(changesToRollback, contexts, output);
	}

	public void rollback(String tagToRollBackTo, String contexts, Writer output)
			throws LiquibaseException {
		facade.rollback(tagToRollBackTo, contexts, output);
	}

	public void rollback(Date dateToRollBackTo, String contexts, Writer output)
			throws LiquibaseException {
		facade.rollback(dateToRollBackTo, contexts, output);
	}

	public void changeLogSync(String contexts, Writer output)
			throws LiquibaseException {
		facade.changeLogSync(contexts, output);
	}

	public void markNextChangeSetRan(String contexts, Writer output)
			throws LiquibaseException {
		facade.markNextChangeSetRan(contexts, output);
	}

	public void futureRollbackSQL(String contexts, Writer output)
			throws LiquibaseException {
		facade.futureRollbackSQL(contexts, output);
	}

	public void dropAll() throws LiquibaseException {
		facade.dropAll();
	}

	public void dropAll(String... schemata) throws LiquibaseException {
		facade.dropAll(schemata);
	}

	public void tag(String tagString) throws LiquibaseException {
		facade.tag(tagString);
	}

	public void updateTestingRollback(String contexts)
			throws LiquibaseException {
		facade.updateTestingRollback(contexts);
	}

	public boolean isSafeToRunMigration() throws LiquibaseException {
		return facade.isSafeToRunMigration();
	}

	public void reportLocks(Writer output) throws LiquibaseException {
		// Liquibase.reportLocks takes a PrintStream, but we're taking a Writer
		// for consistency.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		facade.reportLocks(new PrintStream(baos));

		// PrintStream uses platform default encoding, let's decode it to UTF-8
		ByteBuffer bBuff = ByteBuffer.allocate(baos.size());
		bBuff.put(baos.toByteArray());
		CharBuffer cBuff = Charset.defaultCharset().decode(bBuff);
		
		try {
			output.append(cBuff);
		} catch (IOException e) {
			throw new LiquibaseException("unexpected IOException", e);
		}
	}

	public void forceReleaseLocks() throws LiquibaseException {
		facade.forceReleaseLocks();
	}

	public void reportStatus(boolean verbose, String contexts, Writer output)
			throws LiquibaseException {
		facade.reportStatus(verbose, contexts, output);
	}

	public void clearCheckSums() throws LiquibaseException {
		facade.clearCheckSums();
	}

	public void validate() throws LiquibaseException {
		facade.validate();
	}

	public void setChangeLogParameter(String key, Object value)
			throws LiquibaseException {
		facade.setChangeLogParameter(key, value);
	}
}
