package liquibase.integration.osgi.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.DerbyConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

/**
 * A trivial facade implementation.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class LiquibaseImpl implements liquibase.integration.osgi.Liquibase {

	private Liquibase facade;

	private final ResourceAccessor resourceAccessor;

	public LiquibaseImpl(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	public void open(String changeLogFile, Connection connection)
			throws LiquibaseException {
		this.facade = new Liquibase(changeLogFile, resourceAccessor,
				createConnection(connection));
	}

	private DatabaseConnection createConnection(Connection connection)
			throws LiquibaseException {
		try {
			String productName = connection.getMetaData()
					.getDatabaseProductName();
			if (productName.equals("Apache Derby")) {
				return new DerbyConnection(connection);
			}
		} catch (SQLException e) {
			throw new LiquibaseException("database metatadata check failed", e);
		}
		return new JdbcConnection(connection);
	}

	private void checkFacade() throws LiquibaseException {
		if (this.facade == null) {
			throw new LiquibaseException("facade not ready, use open() first");
		}
	}

	public void update(String contexts) throws LiquibaseException {
		checkFacade();
		facade.update(contexts);
	}

	public void update(String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.update(contexts, output);
	}

	public void update(int changesToApply, String contexts)
			throws LiquibaseException {
		checkFacade();
		facade.update(changesToApply, contexts);
	}

	public void update(int changesToApply, String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.update(changesToApply, contexts, output);
	}

	public void rollback(int changesToRollback, String contexts)
			throws LiquibaseException {
		checkFacade();
		facade.rollback(changesToRollback, contexts);
	}

	public void rollback(int changesToRollback, String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.rollback(changesToRollback, contexts, output);
	}

	public void rollback(String tagToRollBackTo, String contexts)
			throws LiquibaseException {
		checkFacade();
		facade.rollback(tagToRollBackTo, contexts);
	}

	public void rollback(String tagToRollBackTo, String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.rollback(tagToRollBackTo, contexts, output);
	}

	public void rollback(Date dateToRollBackTo, String contexts)
			throws LiquibaseException {
		checkFacade();
		facade.rollback(dateToRollBackTo, contexts);
	}

	public void rollback(Date dateToRollBackTo, String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.rollback(dateToRollBackTo, contexts, output);
	}

	public void changeLogSync(String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.changeLogSync(contexts, output);
	}

	public void markNextChangeSetRan(String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.markNextChangeSetRan(contexts, output);
	}

	public void futureRollbackSQL(String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.futureRollbackSQL(contexts, output);
	}

	public void dropAll() throws LiquibaseException {
		checkFacade();
		facade.dropAll();
	}

	public void dropAll(String... schemata) throws LiquibaseException {
		checkFacade();
		facade.dropAll(schemata);
	}

	public void tag(String tagString) throws LiquibaseException {
		checkFacade();
		facade.tag(tagString);
	}

	public void updateTestingRollback(String contexts)
			throws LiquibaseException {
		checkFacade();
		facade.updateTestingRollback(contexts);
	}

	public boolean isSafeToRunMigration() throws LiquibaseException {
		checkFacade();
		return facade.isSafeToRunMigration();
	}

	public void reportLocks(Writer output) throws LiquibaseException {
		checkFacade();
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
		checkFacade();
		facade.forceReleaseLocks();
	}

	public List<String> listUnrunChangeSets(String contexts)
			throws LiquibaseException {
		checkFacade();
		List<ChangeSet> changeSets = facade.listUnrunChangeSets(contexts);
		List<String> changeSetInfo = new ArrayList<String>(changeSets.size());
		for (ChangeSet changeSet : changeSets) {
			changeSetInfo.add(changeSet.toString(false));
		}
		return changeSetInfo;
	}

	public void reportStatus(boolean verbose, String contexts, Writer output)
			throws LiquibaseException {
		checkFacade();
		facade.reportStatus(verbose, contexts, output);
	}

	public void clearCheckSums() throws LiquibaseException {
		checkFacade();
		facade.clearCheckSums();
	}

	public void validate() throws LiquibaseException {
		checkFacade();
		facade.validate();
	}

	public void setChangeLogParameter(String key, Object value)
			throws LiquibaseException {
		checkFacade();
		facade.setChangeLogParameter(key, value);
	}
}
