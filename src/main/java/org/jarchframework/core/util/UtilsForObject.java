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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jarchframework.core.model.Identity;
import org.jarchframework.core.model.ToString;

/**
 * 
 * @author Yavuz S.TAS
 * @since 1.0
 * @version 1.0
 *
 */
public class UtilsForObject {

	private UtilsForObject() {
	}

	public static int compareTo(Object thisObj, Object thatObj) {
		if (thatObj == null) {
			return 1;
		} else if (!getClass(thisObj).isAssignableFrom(getClass(thatObj))) {
			throw new ClassCastException(getClass(thatObj) + " is not assignable to " + getClass(thisObj));
		} else {
			String[] properties = getIdentityKeysFromCache(getClass(thisObj));
			CompareToBuilder compareToBuilder = new CompareToBuilder();

			for (String property : properties) {
				if (property.length() == 0)
					continue;
				Object valueOfThis = UtilsForReflection.getValue(thisObj, property);
				Object valueOfObj = UtilsForReflection.getValue(thatObj, property);
				compareToBuilder = compareToBuilder.append(valueOfThis, valueOfObj);
			}
			return compareToBuilder.toComparison();
		}
	}

	public static String toString(Object thisObj, String defaultValue) {
		ToStringConfig config = getToStringConfigFromCache(getClass(thisObj));
		String[] properties = config.properties;
		ToStringStyle toStringStyle = config.toStringStyle;
		if (properties.length == 0) {
			return defaultValue;
		} else {
			ToStringBuilder toStringBuilder = new ToStringBuilder(thisObj, toStringStyle);
			for (String property : properties) {
				if (property.length() == 0)
					continue;
				Object value = UtilsForReflection.getValue(thisObj, property);
				toStringBuilder.append(property, value);
			}
			return toStringBuilder.toString();
		}
	}

	public static boolean equals(Object thisObj, Object obj) {
		if (obj != null && getClass(thisObj).isAssignableFrom(getClass(obj))) {

			String[] properties = getIdentityKeysFromCache(getClass(thisObj));

			if (properties.length == 0) {
				return false;
			} else {
				EqualsBuilder equalsBuilder = new EqualsBuilder();

				for (String property : properties) {
					if (property.length() == 0)
						continue;
					Object valueOfThis = UtilsForReflection.getValue(thisObj, property);
					Object valueOfObj = UtilsForReflection.getValue(obj, property);
					equalsBuilder = equalsBuilder.append(valueOfThis, valueOfObj);
				}
				return equalsBuilder.isEquals();
			}
		} else {
			return false;
		}
	}

	public static int hashCode(Object thisObj) {
		String[] properties = getIdentityKeysFromCache(getClass(thisObj));

		if (properties.length == 0) {
			return 0;
		} else {
			HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
			for (String property : properties) {
				if (property.length() == 0)
					continue;
				Object valueOfThis = UtilsForReflection.getValue(thisObj, property);
				hashCodeBuilder.append(valueOfThis);
			}
			return hashCodeBuilder.toHashCode();
		}
	}

	public static final String[] getIdentityKeys(Class<?> classz) {
		if (classz == null) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		} else {
			Identity keySet = UtilsForReflection.isAnnotationDeclaredLocally(Identity.class, classz)
					? UtilsForReflection.findAnnotation(classz, Identity.class) : null;
			if (keySet == null) {
				return getIdentityKeys(classz.getSuperclass());
			} else {
				if (keySet.inherit()) {
					return ArrayUtils.addAll(getIdentityKeys(classz.getSuperclass()), keySet.value());
				} else
					return keySet.value();
			}
		}
	}

	public static Object[] getIdentityValues(Object o) {
		String[] properties = getIdentityKeys(getClass(o));

		if (properties.length == 0) {
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		} else {
			List<Object> values = new ArrayList<>();
			for (String property : properties) {
				if (property.length() == 0)
					continue;
				Object propValue = UtilsForReflection.getValue(o, property);
				values.add(propValue);
			}
			return values.toArray();
		}
	}

	/**
	 * Makes a copy of given object by serialize and deserialize
	 * 
	 * @param obj
	 * @return
	 */
	public static Object copyObject(Object obj) {
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bout)) {
			out.writeObject(obj);

			try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
				return in.readObject();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class ToStringConfig {
		public String[] properties;
		public ToStringStyle toStringStyle;

		public ToStringConfig(String[] properties, ToStringStyle toStringStyle) {
			this.properties = properties;
			this.toStringStyle = toStringStyle;
		}
	}

	private static class IdentityConfig {
		public String[] properties;

		public IdentityConfig(String[] properties) {
			this.properties = properties;
		}
	}

	private static final Map<Class<?>, IdentityConfig> identityCache = new HashMap<>();
	private static final Map<Class<?>, ToStringConfig> toStringCache = new HashMap<>();

	private static synchronized final String[] getIdentityKeysFromCache(Class<?> type) {
		String[] properties = null;
		IdentityConfig config = identityCache.get(type);
		if (config == null) {
			properties = getIdentityKeys(type);
			identityCache.put(type, new IdentityConfig(properties));
		} else {
			properties = config.properties;
		}
		return properties;
	}

	private static synchronized final ToStringConfig getToStringConfigFromCache(Class<?> type) {
		ToStringConfig config = toStringCache.get(type);
		if (config == null) {
			String[] properties = getToStringAttributes(type);
			ToStringStyle toStringStyle = getToStringStyle(type);
			config = new ToStringConfig(properties, toStringStyle);
			toStringCache.put(type, config);
		}
		return config;
	}

	private static final String[] getToStringAttributes(Class<?> classz) {
		if (classz == null) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		} else {
			ToString toString = UtilsForReflection.isAnnotationDeclaredLocally(ToString.class, classz)
					? UtilsForReflection.findAnnotation(classz, ToString.class) : null;
			if (toString == null) {
				return getToStringAttributes(classz.getSuperclass());
			} else {
				if (toString.inherit()) {
					return ArrayUtils.addAll(toString.value(), getToStringAttributes(classz.getSuperclass()));
				} else {
					return toString.value();
				}
			}
		}
	}

	private static final ToStringStyle getToStringStyle(Class<?> classz) {
		if (classz == null) {
			return ToStringStyle.DEFAULT_STYLE;
		} else {
			ToString toString = UtilsForReflection.findAnnotation(classz, ToString.class);
			if (toString != null) {
				return toString.style().getToStringStyle();
			} else {
				return getToStringStyle(classz.getSuperclass());
			}
		}
	}

	private static final Class<?> getClass(Object obj) {
		return obj.getClass();
	}

}
