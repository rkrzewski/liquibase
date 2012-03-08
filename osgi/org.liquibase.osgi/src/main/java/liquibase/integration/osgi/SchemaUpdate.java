package liquibase.integration.osgi;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;

import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Utility class for validating or updating database schema using Liquibase.
 * 
 * <P>
 * This class provides static method intended to be used from Declarative
 * Services components (OSGi service compendium, ch. 112), configured with
 * Configuration Admin Service (OSGi service compendium, ch. 104).
 * </P>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class SchemaUpdate {

	/**
	 * Property specifying change log location within current bundle's
	 * classpath.
	 */
	public static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";

	/**
	 * Property specifying Liquibase contexts that should be validated /
	 * updated.
	 */
	public static final String LIQUIBASE_CONTEXTS = "liquibase.contexts";

	/**
	 * Property specifying mode of operation, should be one of
	 * {@value #LIQUIBASE_MODE_UPDATE}, {@value #LIQUIBASE_MODE_VALIDATE},
	 * {@value #LIQUIBASE_MODE_ADVISE}.
	 */
	public static final String LIQUIBASE_MODE = "liquibase.mode";

	/**
	 * Property value specifying {@code update} mode of operation.
	 */
	public static final String LIQUIBASE_MODE_UPDATE = "update";

	/**
	 * Property value specifying {@code validate} mode of operation.
	 */
	public static final String LIQUIBASE_MODE_VALIDATE = "validate";

	/**
	 * Property value specifying {@code advise} mode of operation.
	 */
	public static final String LIQUIBASE_MODE_ADVISE = "advise";

	/**
	 * Create a DataSource based on component's properties.
	 * 
	 * <P>
	 * Properties of the requested {@code DataSource} are obtained from all
	 * component properties with names starting with {@code jdbc.}. The
	 * {@code jdbc.} prefix is stripped from the names before passing them to
	 * {@code DataSourceFactory}.
	 * </P>
	 * <P>
	 * {@code jdbc.driver} and {@code jdbc.url} properties need to be defined
	 * for {@code DataSource} creation to succeed. {@code jdbc.user},
	 * {@code jdbc.password} and other properties may be added as necessary.
	 * </P>
	 * 
	 * @param dsf
	 *            {@code DataSourceFactory} appropriate for the requested
	 *            {@DataSource} type (see OSGi enterprise
	 *            specification, ch. 125).
	 * @param properties
	 *            component properties.
	 * @return a {@code DataSource} instance.
	 * @throws SQLException
	 *             when {@code DataSource} creation fails.
	 */
	public static DataSource createDataSource(DataSourceFactory dsf,
			Map<String, String> properties) throws SQLException {
		Properties dsfProps = new Properties();
		for (String key : properties.keySet()) {
			if (key.startsWith("jdbc.")) {
				dsfProps.put(key.substring(5), properties.get(key));
			}
		}
		return dsf.createDataSource(dsfProps);
	}

	/**
	 * Perform database schema validation or update.
	 * 
	 * <P>
	 * Behavior of this method depends on the value of {@value #LIQUIBASE_MODE}
	 * component property:
	 * <UL>
	 * <LI>{@value #LIQUIBASE_MODE_UPDATE}: database schema will be brought up
	 * to date if necessary, by executing all pending change sets</LI>
	 * <LI>{@value #LIQUIBASE_MODE_VALIDATE}: database schema will be validated
	 * and exception will be thrown if any pending change sets are detected.</LI>
	 * </UL>
	 * <LI>{@value #LIQUIBASE_MODE_ADVISE}: database schema will be validated
	 * and warning will be printed to the provided {@code PrintWriter} if any
	 * pending change sets are detected.</LI>
	 * </P>
	 * 
	 * @param dataSource
	 *            a {@code DataSource} for accessing the database.
	 * @param liquibase
	 *            Liquibase facade object.
	 * @param pw
	 *            an optional {@code PrintWriter} object to issuing warning
	 *            message in {@code adivse} mode.
	 * @param properties
	 *            component properties.
	 * 
	 * @return {@code true} if the database schema is up to date at the
	 *         termination of this method, {@code false} otherwise. Note that in
	 *         {@code validate} mode an exception is thrown instead of returning
	 *         {@code false}.
	 * @throws SQLException
	 *             when there is a problem accessing the database.
	 * @throws LiquibaseException
	 *             when chnagelog is invalid or there is a problem while
	 *             performing schema update.
	 */
	public static boolean updateSchema(DataSource dataSource,
			Liquibase liquibase, PrintWriter pw, Map<String, String> properties)
			throws SQLException, LiquibaseException {
		Connection conn = dataSource.getConnection();
		String changeLogFile = properties.get(LIQUIBASE_CHANGELOG);
		if (changeLogFile == null) {
			throw new LiquibaseException("missing " + LIQUIBASE_CHANGELOG
					+ " component property");
		}
		String mode = properties.get(LIQUIBASE_MODE);
		if (mode == null) {
			mode = LIQUIBASE_MODE_ADVISE;
		}
		String contexts = properties.get(LIQUIBASE_CONTEXTS);
		try {
			liquibase.open(changeLogFile, conn);
			if (mode.equals(LIQUIBASE_MODE_UPDATE)) {
				liquibase.update(contexts);
				return true;
			} else {
				List<String> pendingChangesets = liquibase
						.listUnrunChangeSets(contexts);
				if (pendingChangesets.size() > 0) {
					String msg = format(
							"Database schema is not up to date: %d pending changests detected",
							pendingChangesets.size());
					if (pw != null) {
						pw.println(msg);
						for (String changeset : pendingChangesets) {
							pw.println("  " + changeset);
						}
					}
					if (mode.equals(LIQUIBASE_MODE_VALIDATE)) {
						throw new LiquibaseException(msg);
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
}
