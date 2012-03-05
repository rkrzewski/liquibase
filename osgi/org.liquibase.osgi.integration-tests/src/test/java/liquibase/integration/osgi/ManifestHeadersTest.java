package liquibase.integration.osgi;

import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import liquibase.servicelocator.LiquibaseService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * Check manifest headers of the {@code org.liquibase.osgi} bundle.
 * 
 * @author rafal.krzewski@caltha.pl
 */
@RunWith(JUnit4TestRunner.class)
public class ManifestHeadersTest {
	@Configuration
	public Option[] paxExamConfig() {
		return options(junitBundles(),
				mavenBundle(maven("org.liquibase", "liquibase-osgi")));
	}

	@Inject
	private BundleContext context;

	/**
	 * Verify that Liquibase-Package header contains all packages that contain
	 * classes that could be discovered through ServiceLocator.
	 * 
	 * <p>
	 * This ensures that Liquibase can discover all of it's core components at
	 * runtime.
	 * </p>
	 */
	@Test
	public void testLiquibasePackage() {
		Bundle liquibaseBundle = findLiquibaseBundle(context);
		Set<String> declared = getLiquibasePackageHeader(liquibaseBundle);
		Map<Class<?>, Set<Class<?>>> implMap = buildImplMap(loadLiquibaseClasses(liquibaseBundle));
		Set<Class<?>> allImpls = new HashSet<Class<?>>();
		for (Set<Class<?>> implSet : implMap.values()) {
			allImpls.addAll(implSet);
		}
		Set<String> required = toPackages(allImpls);
		required.removeAll(declared);
		if (required.size() > 0) {
			fail("Liquibase-Package header is missing the following packages that contain Liquibase API implementations: "
					+ required.toString());
		}
	}

	/**
	 * Verify that Export-Package header contains all packages that contain
	 * Liquibase API interfaces.
	 * 
	 * <p>
	 * API interfaces are defined for this purpose as those interfaces that have
	 * implementations in liquibase core bundle that are discoverable though
	 * ServiceLocator.
	 * </p>
	 */
	@Test
	public void testExportPackage() {
		Bundle liquibaseBundle = findLiquibaseBundle(context);
		Set<String> declared = getExportPackageHeader(liquibaseBundle);
		Map<Class<?>, Set<Class<?>>> implMap = buildImplMap(loadLiquibaseClasses(liquibaseBundle));
		Set<String> required = toPackages(implMap.keySet());
		required.removeAll(declared);
		if (required.size() > 0) {
			fail("Export-Package header is missing the following packages that contain Liquibase API interfaces: "
					+ required.toString());
		}
	}

	private Bundle findLiquibaseBundle(BundleContext context) {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals("org.liquibase.osgi")) {
				if (bundle.getState() != Bundle.ACTIVE) {
					try {
						bundle.start();
					} catch (BundleException e) {
						e.printStackTrace();
						fail(e.getMessage());
					}
				}
				return bundle;
			}
		}
		fail("org.liquibase.osgi bundle not found");
		return null; // unreachable
	}

	private Set<String> getLiquibasePackageHeader(Bundle bundle) {
		Set<String> packages = new HashSet<String>();
		String header = bundle.getHeaders().get("Liquibase-Package");
		if (header == null) {
			fail("Liquibase-Package header is missing");
		}
		for (String pkg : header.split(",")) {
			packages.add(pkg);
		}
		return packages;
	}

	private Set<String> getExportPackageHeader(Bundle bundle) {
		Set<String> packages = new HashSet<String>();
		String header = bundle.getHeaders().get("Export-Package");
		header = header.replaceAll(";version=[0-9.]*", "");
		header = header.replaceAll(";uses:=\"[^\"]*\"", "");
		for (String pkg : header.split(",")) {
			packages.add(pkg);
		}
		return packages;
	}

	private Set<Class<?>> loadLiquibaseClasses(Bundle bundle) {
		Enumeration<URL> entries = bundle.findEntries("/liquibase", "*.class",
				true);
		Set<Class<?>> classes = new HashSet<Class<?>>();
		while (entries.hasMoreElements()) {
			String clUrl = entries.nextElement().toString();
			String clName = clUrl.substring(clUrl.indexOf("liquibase"),
					clUrl.indexOf(".class")).replace('/', '.');
			try {
				classes.add(bundle.loadClass(clName));
			} catch (ClassNotFoundException e) {
				fail(e.getMessage());
			}
		}
		return classes;
	}

	private Map<Class<?>, Set<Class<?>>> buildImplMap(Set<Class<?>> classes) {
		Map<Class<?>, Set<Class<?>>> if2impl = new HashMap<Class<?>, Set<Class<?>>>();
		for (Class<?> cl : classes) {
			if (cl.isInterface()) {
				Set<Class<?>> impls = findImpls(cl, classes);
				if (impls.size() > 0) {
					if2impl.put(cl, impls);
				}
			}
		}
		return if2impl;
	}

	private Set<Class<?>> findImpls(Class<?> iface, Set<Class<?>> classes) {
		Set<Class<?>> impls = new HashSet<Class<?>>();
		for (Class<?> clazz : classes) {
			if (!iface.isAssignableFrom(clazz)) {
				continue;
			}

			if (Modifier.isAbstract(clazz.getModifiers())
					|| Modifier.isInterface(clazz.getModifiers())
					|| !Modifier.isPublic(clazz.getModifiers())) {
				continue;
			}

			if (clazz.getAnnotation(LiquibaseService.class) != null
					&& clazz.getAnnotation(LiquibaseService.class).skip()) {
				continue;
			}

			try {
				clazz.getConstructor();
			} catch (NoSuchMethodException e) {
				continue;
			}

			impls.add(clazz);
		}
		return impls;
	}

	private Set<String> toPackages(Collection<Class<?>> classes) {
		Set<String> packages = new HashSet<String>();
		for (Class<?> clazz : classes) {
			String name = clazz.getName();
			String pkgName = name.substring(0, name.lastIndexOf('.'));
			packages.add(pkgName);
		}
		return packages;
	}
}
