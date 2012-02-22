package liquibase.integration.osgi.impl;

import java.util.HashSet;
import java.util.Set;

import liquibase.exception.ServiceNotFoundException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;

/**
 * {@link ResourceAccessor} implementation for OSGi platform.
 * 
 * <p>
 * The implementation is using OSGi extender pattern (see {@link ExtensionBundleTracker}) to locate
 * bundles providing Liquibase extensions (see {@link ExtensionBundle}) in the framework.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class OsgiServiceLocator extends ServiceLocator {

	private final ExtensionBundleTracker tracker;

	public OsgiServiceLocator(ExtensionBundleTracker tracker, ResourceAccessor resourceAccessor) {
		super(resourceAccessor);
		this.tracker = tracker;		
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] findClasses(Class requiredInterface)
			throws ServiceNotFoundException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		for(ExtensionBundle extensionBundle: tracker.getExtensionBundles()) {
			classes.addAll(extensionBundle.findClasses(requiredInterface));
		}
		return classes.toArray(new Class[classes.size()]);
	}
}
