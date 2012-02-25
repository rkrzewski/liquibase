package liquibase.integration.osgi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import liquibase.exception.ServiceNotFoundException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;

/**
 * {@link ResourceAccessor} implementation for OSGi platform.
 * 
 * <p>
 * The implementation is using OSGi extender pattern (see
 * {@link ExtensionBundleTracker}, OSGi Compendium specification ch. 701.4.5) to
 * locate bundles providing Liquibase extensions (see {@link ExtensionBundle})
 * in the framework.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class OsgiServiceLocator extends ServiceLocator {

	private final ExtensionBundleTracker tracker;

	public OsgiServiceLocator(ExtensionBundleTracker tracker) {
		super(new NullResourceAccessor());
		this.tracker = tracker;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] findClasses(Class requiredInterface)
			throws ServiceNotFoundException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (ExtensionBundle extensionBundle : tracker.getExtensionBundles()) {
			classes.addAll(extensionBundle.findClasses(requiredInterface));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static class NullResourceAccessor implements ResourceAccessor {

		private static final Vector<URL> EMPTY = new Vector<URL>();

		public InputStream getResourceAsStream(String file) throws IOException {
			return null;
		}

		public Enumeration<URL> getResources(String packageName)
				throws IOException {
			return EMPTY.elements();
		}

		public ClassLoader toClassLoader() {
			return null;
		}
	}
}
