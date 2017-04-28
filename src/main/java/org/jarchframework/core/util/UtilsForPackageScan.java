package org.jarchframework.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Yavuz S.TAS
 * @since 1.0
 * @version 1.0
 */
public class UtilsForPackageScan {

	private static Logger logger = LoggerFactory.getLogger(UtilsForPackageScan.class);

	private UtilsForPackageScan() {
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages.
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');

		List<Class<?>> classes = new ArrayList<>();
		Enumeration<URL> resources = classLoader.getResources(path);
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			File directory = new File(resource.getFile());
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs.
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		for (File file : directory.listFiles()) {
			String filename = file.getName();
			if (file.isDirectory()) {
				assert !filename.contains(".");
				classes.addAll(findClasses(file, packageName + "." + filename));
			} else if (filename.endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + filename.substring(0, filename.length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * Scans the package and find classes with specified annotation
	 * 
	 * @param sourcePackage
	 * @param annotationClass
	 * @return a set of classes with given annotation
	 */
	public static Set<Class<?>> getAnnotatedClasses(String sourcePackage, Class<? extends Annotation> annotationClass) {
		LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();
		try {
			for (Class<?> clazz : getClasses(sourcePackage)) {
				if (clazz.isAnnotationPresent(annotationClass)) {
					classes.add(clazz);
				}
			}
		} catch (ClassNotFoundException e) {
			logger.debug(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return classes;
	}
}
