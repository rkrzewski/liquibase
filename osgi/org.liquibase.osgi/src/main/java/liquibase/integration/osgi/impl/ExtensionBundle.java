package liquibase.integration.osgi.impl;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import liquibase.servicelocator.LiquibaseService;

import org.osgi.framework.Bundle;

/**
 * Liquibase extension bundle wrapper.
 * 
 * <p>
 * Liquibase extension bundles are identified by
 * {@value #LIQUIBASE_PACKAGE_HEADER} in their manifest. Packages mentioned in
 * this header are searched for extension classes.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 */
public class ExtensionBundle implements Comparable<ExtensionBundle> {

	public static final String LIQUIBASE_PACKAGE_HEADER = "Liquibase-Package";

	private final Bundle bundle;

	private final String[] packages;

	private Map<Class<?>, Set<Class<?>>> implementationsMap = new HashMap<Class<?>, Set<Class<?>>>();

	public ExtensionBundle(Bundle bundle) {
		this.bundle = bundle;
		this.packages = (bundle.getHeaders().get(LIQUIBASE_PACKAGE_HEADER) + "")
				.split(",");
	}

	/**
	 * Ordering is delegated to {@link Bundle#compareTo(Bundle)} method defined
	 * by the framework.
	 * 
	 * @param o an ExtensionBundle
	 * @return integer value defined by {@link Comparable#compareTo(Object)} contract.
	 */
	public int compareTo(ExtensionBundle o) {
		return bundle.compareTo(o.bundle);
	}

	/**
	 * Searches extension packages provided by the bundle for implementations of
	 * a given interface.
	 * 
	 * <p>
	 * The implementation classes returned meet the following additional
	 * criteria:
	 * <ul>
	 * <li>are a concrete, public class,</li>
	 * <li>provide a public no-arg constructor,</li>
	 * <li>are not annotated with {@literal @LiquibaseService(skip=true)}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param requiredInterface
	 *            an interface.
	 * @return set of classes that implement given interface.
	 */
	public Set<Class<?>> findClasses(Class<?> requiredInterface) {
		synchronized (implementationsMap) {
			Set<Class<?>> implementations = implementationsMap
					.get(requiredInterface);
			if (implementations == null) {
				implementations = findClassesImpl(requiredInterface);
				implementationsMap.put(requiredInterface, implementations);
			}
			return implementations;
		}
	}

	private Set<Class<?>> findClassesImpl(Class<?> requiredInterface) {
		HashSet<Class<?>> classes = new HashSet<Class<?>>();
		for (String pkg : packages) {
			String pkgPath = pkg.replace('.', '/');
			Enumeration<URL> cls = bundle
					.findEntries(pkgPath, "*.class", false);
			if (cls != null) {
				while (cls.hasMoreElements()) {
					String clUrl = cls.nextElement().toString();
					String clName = pkg + "."
							+ clUrl.substring(clUrl.lastIndexOf('/') + 1);
					// strip .class from the end
					clName = clName.substring(0, clName.length() - 6);
					Class<?> clazz;
					try {
						clazz = bundle.loadClass(clName);
						if (!requiredInterface.isAssignableFrom(clazz)) {
							continue;
						}
						if (Modifier.isAbstract(clazz.getModifiers())
								|| Modifier.isInterface(clazz.getModifiers())
								|| !Modifier.isPublic(clazz.getModifiers())) {
							continue;
						}
						if (clazz.getAnnotation(LiquibaseService.class) != null
								&& clazz.getAnnotation(LiquibaseService.class)
										.skip()) {
							continue;
						}
						try {
							clazz.getConstructor();
						} catch (NoSuchMethodException e) {
							continue;
						}
						classes.add(clazz);
					} catch (ClassNotFoundException e) {
						// we've just discovered the class in a resolved bundle,
						// so it really should not happen
						throw new RuntimeException("Unexpected CNFE", e);
					}
				}
			}
		}
		return classes;
	}
}