package liquibase.integration.osgi.impl;

import java.util.Hashtable;

import liquibase.integration.osgi.Liquibase;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.LiquibaseService;
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

	private ServiceRegistration<?> registration;

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

		ServiceLocator serviceLocator = new OsgiServiceLocator(
				extensionBundleTracker);
		ServiceLocator.setServiceLocator(serviceLocator);

		registration = context.registerService(
				new String[] { Liquibase.class.getName() },
				new LiquibaseFactory(), new Hashtable<String, Object>());
	}

	/**
	 * Closes trackers and unregisters the service.
	 * 
	 * @param context
	 *            the bundle context.
	 */
	public void stop(BundleContext context) throws Exception {
		extensionBundleTracker.close();
		registration.unregister();
	}
}
