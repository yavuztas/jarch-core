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
