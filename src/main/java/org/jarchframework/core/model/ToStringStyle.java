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
package org.jarchframework.core.model;

/**
 * 
 * @author Yavuz S.Tas
 * @since 1.0
 * @version 1.0
 */
public enum ToStringStyle {
	DEFAULT, MULTILINE, NO_FIELD_NAMES, SHORT_PREFIX, SIMPLE;

	public org.apache.commons.lang3.builder.ToStringStyle getToStringStyle() {
		switch (this) {
		case DEFAULT:
			return org.apache.commons.lang3.builder.ToStringStyle.DEFAULT_STYLE;
		case MULTILINE:
			return org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;
		case NO_FIELD_NAMES:
			return org.apache.commons.lang3.builder.ToStringStyle.NO_FIELD_NAMES_STYLE;
		case SHORT_PREFIX:
			return org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
		case SIMPLE:
			return org.apache.commons.lang3.builder.ToStringStyle.SIMPLE_STYLE;
		default:
			throw new IllegalArgumentException("Invalid style type :" + this);
		}
	}
}
