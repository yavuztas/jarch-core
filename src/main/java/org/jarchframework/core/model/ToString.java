package org.jarchframework.core.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Yavuz S.Tas
 * @since 1.0
 */
@Target(value = { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ToString {
	boolean inherit() default true;

	ToStringStyle style() default ToStringStyle.SHORT_PREFIX;

	String[] value() default StringUtils.EMPTY;
}
