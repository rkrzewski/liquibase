package liquibase.integration.osgi.impl;

import liquibase.integration.osgi.Liquibase;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class LiquibaseFactory implements ServiceFactory<Liquibase> {

	public Liquibase getService(Bundle bundle,
			ServiceRegistration<Liquibase> registration) {
		return new LiquibaseImpl(new BundleResourceAccessor(bundle));
	}

	public void ungetService(Bundle bundle,
			ServiceRegistration<Liquibase> registration,
			Liquibase service) {
	}
}
