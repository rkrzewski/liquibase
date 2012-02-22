package liquibase.integration.osgi.impl;

import java.sql.Connection;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.integration.osgi.LiquibaseFacade;
import liquibase.integration.osgi.LiquibaseService;
import liquibase.resource.ResourceAccessor;

/**
 * A trivial facade factory service implementation. 
 *
 * @author rafal.krzewski@caltha.pl
 */
public class LiquibaseServiceImpl implements LiquibaseService {

	private final ResourceAccessor resourceAccessor;

	LiquibaseServiceImpl(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}
	
	public LiquibaseFacade getInstance(String changelog, Connection conn)
			throws LiquibaseException {
		return new LiquibaseFacadeImpl(changelog, resourceAccessor, new JdbcConnection(conn));
	}

}
