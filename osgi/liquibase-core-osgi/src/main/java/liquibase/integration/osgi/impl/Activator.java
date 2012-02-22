package liquibase.integration.osgi.impl;

import java.util.Hashtable;

import liquibase.integration.osgi.LiquibaseService;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Liquibase core bundle activator.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class Activator implements BundleActivator {

	private ExtensionBundleTracker extensionBundleTracker;

	private ResourceBundleTracker resourceBundleTracker;

	private ServiceRegistration<LiquibaseService> registration;

	/**
	 * Opens extension and resource bundle trackers and registers
	 * {@link LiquibaseService}.
	 * 
	 * @param context
	 *            the bundle context.
	 */
	public void start(BundleContext context) throws Exception {
		extensionBundleTracker = new ExtensionBundleTracker(context);
		extensionBundleTracker.open();
		resourceBundleTracker = new ResourceBundleTracker(context);
		resourceBundleTracker.open();

		ResourceAccessor resourceAccessor = new OsgiResourceAccessor(
				resourceBundleTracker);
		ServiceLocator serviceLocator = new OsgiServiceLocator(
				extensionBundleTracker, resourceAccessor);
		ServiceLocator.setServiceLocator(serviceLocator);

		LiquibaseService service = new LiquibaseServiceImpl(resourceAccessor);
		registration = context.registerService(LiquibaseService.class, service,
				new Hashtable<String, Object>());
	}

	/**
	 * Closes trackers and unregisters the service.
	 * 
	 * @param context
	 *            the bundle context.
	 */
	public void stop(BundleContext context) throws Exception {
		extensionBundleTracker.close();
		resourceBundleTracker.close();
		registration.unregister();
	}
}
