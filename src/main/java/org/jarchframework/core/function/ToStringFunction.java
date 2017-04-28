package org.jarchframework.core.function;

import java.util.function.Function;

/**
 * Null safe toString {@link Function} implementation with a mandatory null
 * expression
 * 
 * @author Yavuz S.Tas
 * @since 1.0
 * @version 1.0
 *
 */
public class ToStringFunction implements Function<Object, String> {

	private final String nullExpression;

	public ToStringFunction(String nullExpression) {
		this.nullExpression = nullExpression;
	}

	@Override
	public String apply(Object t) {
		return t == null ? nullExpression : t.toString();
	}

}
