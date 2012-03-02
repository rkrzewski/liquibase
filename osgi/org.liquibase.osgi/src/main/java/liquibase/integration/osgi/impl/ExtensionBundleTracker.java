package liquibase.integration.osgi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

/**
 * A tracker for Liquibase extension bundles.
 * 
 * <p>
 * The tracker detects bundles with
 * {@value ExtensionBundle#LIQUIBASE_PACKAGE_HEADER} header as they appear in the
 * framework and maintains a list of them, ordered according to the same
 * criteria that the framework uses for natural bundle ordering.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class ExtensionBundleTracker extends BundleTracker {

	private SortedSet<ExtensionBundle> extensionBundles = new TreeSet<ExtensionBundle>();

	public ExtensionBundleTracker(BundleContext context) {
		super(context, Bundle.STARTING | Bundle.ACTIVE, null);
	}

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		if (bundle.getHeaders().get(ExtensionBundle.LIQUIBASE_PACKAGE_HEADER) != null) {
			ExtensionBundle rb = new ExtensionBundle(bundle);
			synchronized (extensionBundles) {
				extensionBundles.add(rb);
			}
			return rb;
		} else {
			return null;
		}
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		synchronized (extensionBundles) {
			extensionBundles.remove((ExtensionBundle) object);
		}
	}

	/**
	 * Returns the list of currently available extension bundles.
	 * 
	 * @return list of extension bundles.
	 */
	public List<ExtensionBundle> getExtensionBundles() {
		synchronized (extensionBundles) {
			return new ArrayList<ExtensionBundle>(extensionBundles);
		}
	}
}
