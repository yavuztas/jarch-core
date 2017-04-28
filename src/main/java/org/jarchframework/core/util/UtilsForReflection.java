/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jarchframework.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jarchframework.core.function.ToStringFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for helping various reflection operations like getting nested
 * property values etc. <br>
 * <b>Example:</b><br>
 * <code>
 * UtilsForReflection.getValue(user, "group.name");
 * </code>
 * 
 * @author Yavuz S.Tas
 * @since 1.0
 * @version 1.0
 *
 */
public class UtilsForReflection {

	private static final Logger logger = LoggerFactory.getLogger(UtilsForReflection.class);

	private static final String NO_GETTER = "No getter found with isXXX syntax: %s for Class: %s";
	private static final String NO_SETTER = "No setter found with isXXX syntax: %s for Class: %s";

	private UtilsForReflection() {
	}

	public static Method getGetterMethod(Class type, String property) throws NoSuchMethodException {
		hasLength(property);
		String name = "get" + StringUtils.capitalize(property);
		Method method = findMethod(type, name, ArrayUtils.EMPTY_CLASS_ARRAY);
		if (method == null) {
			name = "is" + StringUtils.capitalize(property);
			method = findMethod(type, name, ArrayUtils.EMPTY_CLASS_ARRAY);
			if (method == null) {
				throw new NoSuchMethodException(String.format(NO_GETTER, property, type));
			}
		}
		return method;
	}

	private static void hasLength(String property) {
		if (property == null || property.trim().length() == 0) {
			throw new RuntimeException("property should not be empty");
		}
	}

	public static Method getNestedGetterMethod(Class type, String property) {
		return getDeepGetterMethod(type, property);
	}

	private static Method getDeepGetterMethod(Class type, String property) {
		int index = property.indexOf('.');
		String getterName = index > -1 ? property.substring(0, index) : property;
		Method method = null;
		try {
			hasLength(property);
			String name = "get" + StringUtils.capitalize(getterName);
			method = findMethod(type, name, ArrayUtils.EMPTY_CLASS_ARRAY);
			if (method == null) {
				name = "is" + StringUtils.capitalize(getterName);
				method = findMethod(type, name, ArrayUtils.EMPTY_CLASS_ARRAY);
				if (method == null) {
					if (logger.isWarnEnabled()) {
						logger.warn(String.format(NO_GETTER, getterName, type));
					}
					throw new RuntimeException(String.format(NO_GETTER, getterName, type));
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		if (index != -1 && method != null) {
			return getNestedGetterMethod(method.getReturnType(), property.substring(index + 1));
		}
		return method;
	}

	public static Method getNestedSetterMethod(Class type, String property) {
		Method nestedGetterMethod = getNestedGetterMethod(type, property);
		if (nestedGetterMethod == null) {
			throw new RuntimeException(type + " tipinde ['" + property + "'] getter methodu bulunamadı");
		}

		return getNestedSetterMethod(type, property, nestedGetterMethod.getReturnType());
	}

	public static Method getNestedSetterMethod(Class type, String property, Class parameterType) {
		int index = property.indexOf('.');
		String setterName = index > -1 ? property.substring(0, index) : property;
		Method method = null;

		if (index != -1) {
			method = getPreviousGetterMethod(type, property);
			if (method != null) {
				return getNestedSetterMethod(method.getReturnType(), baseNameOfFieldPath(property));
			}
		}

		try {
			hasLength(property);
			String name = "set" + StringUtils.capitalize(setterName);
			method = findMethod(type, name, new Class[] { parameterType });
			if (method == null && logger.isWarnEnabled()) {
				logger.warn(String.format(NO_SETTER, setterName, type));
			}
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
		}
		return method;
	}

	private static final Method findMethod(Class type, String name, Class[] parameterTypes) {
		try {
			return type.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			return findMethodInObject(name, parameterTypes);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Method findMethodInObject(String name, Class[] parameterTypes) {
		try {
			return Object.class.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			logger.info(e.getMessage(), e);
		}
		return null;
	}

	public static Method getMethod(Class type, String name, Class[] parameterTypes) {
		Method method = findMethod(type, name, parameterTypes);
		if (method == null && logger.isInfoEnabled()) {
			logger.info(String.format("No method with given name %s found in type %s", name, type));
		}
		return method;
	}

	public static Field getField(Class classz, String fieldPath) {
		hasLength(fieldPath);
		int index = fieldPath.indexOf('.');
		String fieldName = index > -1 ? fieldPath.substring(0, index) : fieldPath;
		Field field = null;

		try {
			field = classz.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			Class superClass = classz.getSuperclass();
			if (superClass != null) {
				field = getField(superClass, fieldName);
			} else {
				throw new IllegalArgumentException(e);
			}
		}

		if (index != -1) {
			return getField(field.getType(), fieldPath.substring(index + 1));
		} else {
			return field;
		}
	}

	public static Field getNestedField(Class clazz, String fieldPath) {
		hasLength(fieldPath);

		int index = fieldPath.indexOf('.');
		String fieldName = index > -1 ? fieldPath.substring(0, index) : fieldPath;
		Field field = null;

		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass != null) {
				field = getField(superClass, fieldName);
			} else {
				superClass = getField(superClass, fieldPath.substring(0, fieldPath.indexOf('.'))).getClass();
				if (superClass != null) {
					field = getField(superClass, fieldName);
				} else {
					throw new IllegalArgumentException(e);
				}
			}

		}

		if (index != -1) {
			return getField(field.getType(), fieldPath.substring(index + 1));
		} else {
			return field;
		}
	}

	public static void setValue(Object item, String methodName, Object valueToSet) {
		try {
			Object value = item;
			if (methodName.contains(".")) {
				value = getPreviousInstanceOfProperty(item, methodName);
			}
			if (item != null) {
				Method nestedSetterMethod = UtilsForReflection.getNestedSetterMethod(item.getClass(), methodName);
				if (nestedSetterMethod != null) {
					nestedSetterMethod.setAccessible(true);
					nestedSetterMethod.invoke(value, valueToSet);
				} else {
					if (logger.isWarnEnabled()) {
						logger.warn(String.format(NO_SETTER, methodName, item.getClass()));
					}
				}
			} else {
				throw new RuntimeException("item must not be null");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	public static Method getSetterMethod(Class type, String property, Class parameterType) {
		hasLength(property);
		String name = "set" + StringUtils.capitalize(property);
		Method method = findMethod(type, name, new Class[] { parameterType });
		if (method == null && logger.isWarnEnabled()) {
			logger.warn(String.format(NO_SETTER, property, type));
		}
		return method;
	}

	public static boolean isGetterMethod(Class clazz, Method method) {
		try {
			return getGetterMethod(clazz, method.getName()) != null;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public static Object getValue(Object obj, String fieldPath) {
		if (obj == null)
			return null;

		if (fieldPath.contains("[")) {
			return getValueByExpression(obj, fieldPath);
		}

		Object value = null;
		hasLength(fieldPath);

		int index = fieldPath.indexOf('.');
		String field = index > -1 ? fieldPath.substring(0, index) : fieldPath;
		Object fieldValue = getFieldValue(obj, field);

		if (index != -1 && fieldValue != null) {
			value = getValue(fieldValue, fieldPath.substring(index + 1));
		} else {
			value = fieldValue;
		}

		return value;
	}

	public static String getValueByExpression(Object obj, String expression) {
		return getValueByExpression(obj, expression, "");
	}

	public static String getValueByExpression(final Object obj, String expression, final String nullExpression) {
		return getValueByExpression(obj, expression, new ToStringFunction(nullExpression));
	}

	public static String getValueByExpression(Object obj, String expression,
			Function<Object, String> toStringFunction) {
		if (obj == null)
			return "";
		hasLength(expression);

		if (!expression.contains("[") && !expression.contains("]")) {
			return toStringFunction.apply(getValue(obj, expression));
		}

		String pattern = "\\[.*?]";
		String labelDefinition = expression;

		String[] arr = labelDefinition.split(pattern);
		Matcher matcher = Pattern.compile(pattern).matcher(labelDefinition);

		int i = 0;
		StringBuilder builder = new StringBuilder();
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();

			String delimeter = arr.length == 0 ? "" : arr[i];
			String fieldPath = labelDefinition.substring(start + 1, end - 1);

			Object fieldValue = getValue(obj, fieldPath);
			builder.append(delimeter);
			builder.append(toStringFunction.apply(fieldValue));
			i++;
		}
		return builder.toString();
	}

	private static Object getFieldValue(Object obj, String fieldName) {
		if (obj == null)
			return null;

		if (obj instanceof Map) {
			return ((Map) obj).get(fieldName);
		}

		if (obj instanceof Collection) {
			Collection collection = (Collection) obj;
			LinkedHashSet items = new LinkedHashSet();
			for (Object object : collection) {
				items.add(getValue(object, fieldName));
			}
			return items;
		}

		Method method = null;
		try {
			method = getGetterMethod(obj.getClass(), fieldName);
		} catch (NoSuchMethodException e) {
			method = getMethod(obj.getClass(), fieldName, null);
		}

		if (method == null)
			return null;

		try {
			method.setAccessible(true);
			return method.invoke(obj, null);
		} catch (Exception e) {
			logger.warn("object : " + obj + " fieldName : " + fieldName, e);
		}

		return null;
	}

	public static Collection collectProperties(Collection collection, String propertyName) {
		if (collection == null) {
			return new ArrayList<>();
		}

		List items = new ArrayList<>();
		for (Object object : collection) {
			items.add(getValue(object, propertyName));
		}
		return items;
	}

	public static List<Field> getAllFields(Class type) {
		List<Field> fields = new LinkedList<>();
		for (Field field : type.getDeclaredFields()) {
			fields.add(field);
		}
		if (type.getSuperclass() != null) {
			fields.addAll(getAllFields(type.getSuperclass()));
		}
		return fields;
	}

	public static List<Method> getGetterMethods(Class clazz) {
		LinkedList<Method> getterMethods = new LinkedList<>();
		for (Method method : getAllDeclaredMethods(clazz)) {
			String methodName = method.getName();
			if (methodName.startsWith("get") || methodName.startsWith("is")) {

				// skip getClass() method
				if ("class".equalsIgnoreCase(methodName.replace("get", ""))) {
					continue;
				}

				getterMethods.add(method);
			}
		}
		return getterMethods;
	}

	public static Collection<String> getFieldNames(Class clazz) {
		List<Field> fields = getAllFields(clazz);
		Set<String> names = new LinkedHashSet<>();
		for (Field field : fields) {
			names.add(field.getName());
		}
		return names;
	}

	public static List<String> getFieldNamesByGetterMethod(Class clazz) {
		List<Method> getters = getGetterMethods(clazz);
		List<String> names = new LinkedList<>();
		for (Method method : getters) {
			String propertyName = null;
			if (method.getName().startsWith("is")) {
				propertyName = StringUtils.replaceOnce(method.getName(), "is", "");
			} else {
				propertyName = StringUtils.replaceOnce(method.getName(), "get", "");
			}
			names.add(StringUtils.uncapitalize(propertyName));
		}
		return names;
	}

	public static String baseNameOfGetterMethod(String name) {
		return StringUtils.uncapitalize(StringUtils.replaceOnce(name, "get", ""));
	}

	public static String baseNameOfFieldPath(String fieldPath) {
		return fieldPath.substring(fieldPath.lastIndexOf('.') + 1);
	}

	public static String getPreviousPathOfProperty(String fullPath, String property) {
		StringBuilder b = new StringBuilder(fullPath);
		b.delete(fullPath.lastIndexOf("." + property), b.capacity());
		return b.toString();
	}

	public static Object getPreviousInstanceOfProperty(Object object, String path) {
		return getPreviousInstanceOfProperty(object, path, baseNameOfFieldPath(path));
	}

	public static Object getPreviousInstanceOfProperty(Object object, String fullPath, String property) {
		return UtilsForReflection.getValue(object, getPreviousPathOfProperty(fullPath, property));
	}

	public static Class getPreviousClassOfProperty(Class clazz, String fullPath, String property) {
		String path = getPreviousPathOfProperty(fullPath, property);
		Method nestedGetterMethod = getNestedGetterMethod(clazz, path);
		if (nestedGetterMethod != null) {
			return nestedGetterMethod.getReturnType();
		}
		if (logger.isWarnEnabled()) {
			logger.warn(String.format(NO_GETTER, path, clazz));
		}
		return null;
	}

	public static Method getPreviousGetterMethod(Class parameterType, String property) {
		String baseNameOfFieldPath = UtilsForReflection.baseNameOfFieldPath(property);
		return getNestedGetterMethod(parameterType, property.replace("." + baseNameOfFieldPath, ""));
	}

	public static Class getGenericTypeOfProperty(Class clazz, String propertyName) {
		Type[] types = getGenericTypesOfProperty(clazz, propertyName);
		if (types.length > 0)
			return (Class) types[0];
		return null;
	}

	public static Type[] getGenericTypes(Class<?> clz) {
		return clz.getGenericInterfaces();
	}

	public static Type[] getGenericTypesOfProperty(Class clazz, String propertyName) {
		Method nestedGetterMethod = getNestedGetterMethod(clazz, propertyName);
		if (nestedGetterMethod != null) {
			return ((ParameterizedType) nestedGetterMethod.getGenericReturnType()).getActualTypeArguments();
		}
		return new Type[0];
	}

	public static boolean isAnnotationDeclaredLocally(Class<? extends Annotation> annotationType, Class<?> clazz) {
		boolean declaredLocally = false;
		for (Annotation annotation : clazz.getDeclaredAnnotations()) {
			if (annotation.annotationType().equals(annotationType)) {
				declaredLocally = true;
				break;
			}
		}
		return declaredLocally;
	}

	/**
	 * Find a single {@link Annotation} of {@code annotationType} on the
	 * supplied {@link Class}, traversing its interfaces, annotations, and
	 * superclasses if the annotation is not <em>present</em> on the given class
	 * itself.
	 * <p>
	 * This method explicitly handles class-level annotations which are not
	 * declared as {@link java.lang.annotation.Inherited inherited} <em>as well
	 * as meta-annotations and annotations on interfaces</em>.
	 * <p>
	 * The algorithm operates as follows:
	 * <ol>
	 * <li>Search for the annotation on the given class and return it if found.
	 * <li>Recursively search through all interfaces that the given class
	 * declares.
	 * <li>Recursively search through all annotations that the given class
	 * declares.
	 * <li>Recursively search through the superclass hierarchy of the given
	 * class.
	 * </ol>
	 * <p>
	 * Note: in this context, the term <em>recursively</em> means that the
	 * search process continues by returning to step #1 with the current
	 * interface, annotation, or superclass as the class to look for annotations
	 * on.
	 * 
	 * @param clazz
	 *            the class to look for annotations on
	 * @param annotationType
	 *            the type of annotation to look for
	 * @return the annotation if found, or {@code null} if not found
	 */
	public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
		return findAnnotation(clazz, annotationType, new HashSet<Annotation>());
	}

	/**
	 * Perform the search algorithm for {@link #findAnnotation(Class, Class)},
	 * avoiding endless recursion by tracking which annotations have already
	 * been <em>visited</em>.
	 * 
	 * @param clazz
	 *            the class to look for annotations on
	 * @param annotationType
	 *            the type of annotation to look for
	 * @param visited
	 *            the set of annotations that have already been visited
	 * @return the annotation if found, or {@code null} if not found
	 */
	private static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType,
			Set<Annotation> visited) {
		if (isAnnotationDeclaredLocally(annotationType, clazz)) {
			return clazz.getAnnotation(annotationType);
		}
		for (Class<?> ifc : clazz.getInterfaces()) {
			A annotation = findAnnotation(ifc, annotationType, visited);
			if (annotation != null) {
				return annotation;
			}
		}
		for (Annotation ann : clazz.getDeclaredAnnotations()) {
			boolean isInJavaLangAnnotationPackage = ann.annotationType().getName().startsWith("java.lang.annotation");
			if (!isInJavaLangAnnotationPackage && visited.add(ann)) {
				A annotation = findAnnotation(ann.annotationType(), annotationType, visited);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		if (superclass == null || superclass.equals(Object.class)) {
			return null;
		}
		return findAnnotation(superclass, annotationType, visited);
	}

	private static List<Method> getAllDeclaredMethods(Class clazz) {
		ArrayList<Method> methodList = new ArrayList<>();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			methodList.add(method);
		}

		if (clazz.getSuperclass() != null) {
			methodList.addAll(getAllDeclaredMethods(clazz.getSuperclass()));
		} else if (clazz.isInterface()) {
			for (Class<?> superIfc : clazz.getInterfaces()) {
				methodList.addAll(getAllDeclaredMethods(superIfc));
			}
		}

		return methodList;
	}

}
