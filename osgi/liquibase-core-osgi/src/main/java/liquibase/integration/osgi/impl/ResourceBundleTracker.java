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
 * A tracker for liquibase resource bundles. 
 *
  * <p>
 * The tracker detects bundles with
 * {@value ResourceBundle#LIQUIBASE_RESOURCES_HEADER} header as they appear in the
 * framework and maintains a list of them, ordered according to the same
 * criteria that the framework uses for natural bundle ordering.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class ResourceBundleTracker extends BundleTracker {

	private SortedSet<ResourceBundle> resourceBundles = new TreeSet<ResourceBundle>();

	public ResourceBundleTracker(BundleContext context) {
		super(context, Bundle.STARTING | Bundle.ACTIVE, null);
	}

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		if (bundle.getHeaders().get(ResourceBundle.LIQUIBASE_RESOURCES_HEADER) != null) {
			ResourceBundle rb = new ResourceBundle(bundle);
			synchronized (resourceBundles) {
				resourceBundles.add(rb);
			}
			return rb;
		} else {
			return null;
		}
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		synchronized (resourceBundles) {
			resourceBundles.remove((ResourceBundle) object);
		}
	}

	/**
	 * Returns the list of currently available resource bundles.
	 * 
	 * @return list of resource bundles.
	 */
	public List<ResourceBundle> getResourceBundles() {
		synchronized (resourceBundles) {
			return new ArrayList<ResourceBundle>(resourceBundles);
		}
	}
}
