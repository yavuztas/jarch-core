package org.jarchframework.core.model;

import java.io.Serializable;

import org.jarchframework.core.util.UtilsForObject;

/**
 * Common abstract class for every java object that provides automatic
 * implementation for equals, hashCode and toString methods
 * 
 * @author Yavuz S.Tas
 * @since 1.0
 * @version 1.0
 */
public abstract class BaseObject implements Serializable {

	public boolean equals(Object obj) {
		return UtilsForObject.equals(this, obj);
	}

	public int hashCode() {
		return UtilsForObject.hashCode(this);
	}

	public String toString() {
		return UtilsForObject.toString(this, super.toString());
	}

}
