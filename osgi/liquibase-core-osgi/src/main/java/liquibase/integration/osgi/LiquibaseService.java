package liquibase.integration.osgi;

import java.sql.Connection;

import liquibase.exception.LiquibaseException;

/**
 * OSGi Service providing Liquibase facade access.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public interface LiquibaseService {

	/**
	 * Provides Liquibase facade for specific changelog and JDBC connection.
	 * 
	 * <p>
	 * Note that the caller is responsible for closing the connection when
	 * facade object is no longer needed.
	 * </p
	 * 
	 * @param changelog
	 *            location of changelog file.
	 * @param connection
	 *            a JDBC connection.
	 * @return Liquibase facade object.
	 * @throws LiquibaseException
	 *             when facade object could not be created, eg. database in use
	 *             is not supported.
	 */
	LiquibaseFacade getInstance(String changelog, Connection connection)
			throws LiquibaseException;

}
