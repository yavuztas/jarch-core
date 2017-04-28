package org.jarchframework.core.test;

import java.util.List;

import org.jarchframework.core.util.UtilsForCollections;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Tests should be implemented...
 * 
 * @author Yavuz S.Tas
 *
 */
public class UtilsForCollectionsTest {

	@Test
	public void primitiveIntArrayToListTest() {

		int[] array = new int[] { 1, 3, 5, 6, 7 };

		List<Integer> list = UtilsForCollections.toList(array);
		Assert.assertEquals(array.length, list.size());

		for (int i = 0; i < array.length; i++) {
			Assert.assertEquals(array[i], list.get(i).intValue());
		}

	}

	@Test
	public void primitiveLongArrayToListTest() {

		long[] array = new long[] { 1, 3, 5, 6, 7 };

		List<Long> list = UtilsForCollections.toList(array);
		Assert.assertEquals(array.length, list.size());

		for (int i = 0; i < array.length; i++) {
			Assert.assertEquals(array[i], list.get(i).longValue());
		}

	}

	@Test
	public void primitiveDoubleArrayToListTest() {

		double[] array = new double[] { 1.0, 3.0, 5.1, 6.2, 7.3 };

		List<Double> list = UtilsForCollections.toList(array);
		Assert.assertEquals(array.length, list.size());

		for (int i = 0; i < array.length; i++) {
			Assert.assertEquals(array[i], list.get(i).doubleValue());
		}

	}

	@Test
	public void objectArrayToListTest() {

		Integer[] array = new Integer[] { 1, 3, 5, 6, 7 };

		List<Integer> list = UtilsForCollections.toList(array);
		Assert.assertEquals(array.length, list.size());

		for (int i = 0; i < array.length; i++) {
			Assert.assertEquals(array[i], list.get(i));
		}

	}

}
