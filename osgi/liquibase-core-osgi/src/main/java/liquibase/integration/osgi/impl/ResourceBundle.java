package liquibase.integration.osgi.impl;

import java.net.URL;

import org.osgi.framework.Bundle;

/**
 * Liqbuibase resource bundle wrapper.
 * 
 * <p>
 * Resource bundles are used to load Liquibase changelog (@see
 * {@link liquibase.integration.osgi.LiquibaseService#getInstance(String, java.sql.Connection)}
 * ) and it's optional included files.
 * </p>
 * 
 * <p>
 * Resource bundles are identified by {@value #LIQUIBASE_RESOURCES_HEADER} header in
 * their manifest. Only packages (directories) mentioned in this header are
 * searched for resources. To make all resources in the bundle available for
 * lookup and empty value, or a single dot can be used.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class ResourceBundle implements Comparable<ResourceBundle> {

	public static final String LIQUIBASE_RESOURCES_HEADER = "Liquibase-Resources";

	private final Bundle bundle;

	private final String[] paths;

	public ResourceBundle(Bundle bundle) {
		this.bundle = bundle;
		this.paths = (bundle.getHeaders().get(LIQUIBASE_RESOURCES_HEADER) + "")
				.split(",");
		for (int i = 0; i < paths.length; i++) {
			paths[i] = normalizePath(paths[i]);
		}
	}

	/**
	 * Ordering is delegated to {@link Bundle#compareTo(Bundle)} method defined
	 * by the framework.
	 * 
	 * @param o
	 *            a ResourceBundle
	 * @return integer value defined by {@link Comparable#compareTo(Object)}
	 *         contract.
	 */
	public int compareTo(final ResourceBundle o) {
		return bundle.compareTo(o.bundle);
	}

	/**
	 * Finds the specified resource using bundle classloader.
	 * 
	 * <p>
	 * If the requested path does not start with any of package (directory)
	 * names listed in {@value #LIQUIBASE_RESOURCES_HEADER} header method will return
	 * {@code null}.
	 * </p>
	 * 
	 * @param path
	 *            resource path.
	 * @return resource URL or {@code null} if not found.
	 */
	public URL getResource(final String path) {
		String normalizedPath = normalizePath(path);
		for (String exposedPath : paths) {
			if (normalizedPath.startsWith(exposedPath)) {
				return bundle.getResource(normalizedPath);
			}
		}
		return null;
	}

	private static String normalizePath(final String path) {
		if (path.equals(".")) {
			return "";
		}
		if (path.startsWith("./")) {
			return path.substring(2);
		}
		if (path.startsWith("/")) {
			return path.substring(1);
		}
		return path;
	}
}