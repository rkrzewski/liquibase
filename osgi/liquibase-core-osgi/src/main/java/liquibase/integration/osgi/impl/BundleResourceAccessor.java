package liquibase.integration.osgi.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import liquibase.resource.ResourceAccessor;

import org.osgi.framework.Bundle;

public class BundleResourceAccessor implements ResourceAccessor {
	
	private static final Vector<URL> EMPTY = new Vector<URL>();
	
	private final Bundle bundle;

	public BundleResourceAccessor(Bundle bundle) {
		this.bundle = bundle;
	}

	public InputStream getResourceAsStream(String file) throws IOException {
		URL resource = bundle.getResource(file);
		if (resource != null) {
			return resource.openStream();
		}		return null;
	}

	public Enumeration<URL> getResources(String packageName) throws IOException {
		return EMPTY.elements();
	}

	public ClassLoader toClassLoader() {
		return null;
	}
}
