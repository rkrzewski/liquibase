package liquibase.integration.osgi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import liquibase.resource.ResourceAccessor;

/**
 * {@link ResourceAccessor} implementation for OSGi platform.
 * 
 * <p>
 * The implementation is using OSGi extender pattern (see {@link ResourceBundleTracker}) to locate
 * bundles providing Liquibase resources (see {@link ResourceBundle}) in the framework.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class OsgiResourceAccessor implements ResourceAccessor {

	private final ResourceBundleTracker tracker;

	public OsgiResourceAccessor(ResourceBundleTracker tracker) {
		this.tracker = tracker;
	}

	/**
	 * Returns an {@code InputStream} for reading the specified resource.
	 * 
	 * <p>
	 * Each of the resource bundles available at the time of the call is
	 * searched in turn for the specified resource. When the first match is
	 * found the search is terminated and matched resource is opened for
	 * reading. When no match is found {@code null} is returned.
	 * </p>
	 * 
	 * <p>
	 * Please note that due to the dynamic character of OSGi platform, resource
	 * bundles may come and go at any time. To ensure that that the changelog
	 * file and other files referenced from it are available at the time you
	 * need them, either package them in the same bundle where you are invoking
	 * Liquibase from, or ensure that your client bundle has larger start level
	 * as the resource and extension bundle it requires.
	 * </p>
	 */
	public InputStream getResourceAsStream(String path) throws IOException {
		for (ResourceBundle resourceBundle : tracker.getResourceBundles()) {
			URL resource = resourceBundle.getResource(path);
			if (resource != null) {
				return resource.openStream();
			}
		}
		return null;
	}

	/**
	 * Not used by {@link OsgiResourceAccessor}, always returns an empty
	 * enumeration.
	 * 
	 * @param packageName
	 *            a package name
	 * @return an empty enumeration.
	 */
	public Enumeration<URL> getResources(String packageName) throws IOException {
		return new Vector<URL>().elements();
	}

	/**
	 * Not used by {@link OsgiResourceAccessor}, always returns {@code null}.
	 * 
	 * @return {@code null}
	 */
	public ClassLoader toClassLoader() {
		return null;
	}
}
