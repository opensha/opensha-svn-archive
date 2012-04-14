package org.opensha.commons.data.function;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;

import com.google.common.base.Preconditions;

public abstract class AbstractXY_DataSetTest {
	
	static final double test_tol = 1e-10;
	
	abstract XY_DataSet newEmptyDataSet();
	
	/**
	 * 
	 * @param num number of values to add
	 * @param randomX if true then values will be inserted at random values, if false x values should be equal to index
	 * @param orderedYVals if true y values should be equal to index**2, if false they will be random
	 * @return
	 */
	XY_DataSet newPopulatedDataSet(int num, boolean randomX, boolean randomY) {
		Preconditions.checkState(isArbitrarilyDiscretized(), "Can only automatically populate arbitrarily discretized datasets!");
		
		XY_DataSet xy = newEmptyDataSet();
		for (int i=0; i<num; i++) {
			xy.set(defaultPt(xy, i, randomX, randomY));
		}
		return xy;
	}
	
	/**
	 * returns a new empty dataset if arbitrarily discretized, otherwise a populated dataset with one point
	 */
	XY_DataSet newQuickTestDataSet() {
		if (isArbitrarilyDiscretized())
			return newEmptyDataSet();
		return newPopulatedDataSet(1, false, false);
	}
	
	/**
	 * this should be the value of the x at the given index if randomX is false for populated datasets
	 * @param index
	 * @return
	 */
	static double defaultXValue(int index) {
		return index;
	}
	
	/**
	 * this should be the value of the y at the given index if randomY is false for populated datasets
	 * @param index
	 * @return
	 */
	static double defaultYValue(int index) {
		return index * index;
	}
	
	static double randomXNotContained(XY_DataSet xy) {
		double val = Double.NaN;
		while (Double.isNaN(val) || xy.hasPoint(val, 0d))
			val = Math.random() * 100d - 50d;
		return val;
	}
	
	static Point2D defaultPt(XY_DataSet xy, int index, boolean randomX, boolean randomY) {
		double x;
		if (randomX)
			x = randomXNotContained(xy);
		else
			x = defaultXValue(index);
		double y;
		if (randomY)
			y = Math.random();
		else
			y = defaultYValue(index);
		return new Point2D.Double(x, y);
	}
	
	abstract boolean isArbitrarilyDiscretized();
	
	static double randomNotEqualTo(double val) {
		double ret = Math.random();
		while (Math.abs(val - ret) < 1e-5) {
			ret = Math.random();
		}
		return ret;
	}
	
	@Test
	public void testSetGetName() {
		XY_DataSet xy = newQuickTestDataSet();
		String name = "new test name "+System.currentTimeMillis();
		xy.setName(name);
		assertEquals("name wasn't set correctly", name, xy.getName());
	}
	
	@Test
	public void testSetGetInfo() {
		XY_DataSet xy = newQuickTestDataSet();
		String info = "here is my new info! "+System.currentTimeMillis();
		xy.setInfo(info);
		assertEquals("info wasn't set correctly", info, xy.getInfo());
	}
	
	@Test
	public void testSetGetXAxis() {
		XY_DataSet xy = newQuickTestDataSet();
		String xAxis = "my x axis label! "+System.currentTimeMillis();
		xy.setXAxisName(xAxis);
		assertEquals("x axis name wasn't set correctly", xAxis, xy.getXAxisName());
	}
	
	@Test
	public void testSetGetYAxis() {
		XY_DataSet xy = newQuickTestDataSet();
		String yAxis = "my y axis label! "+System.currentTimeMillis();
		xy.setYAxisName(yAxis);
		assertEquals("y axis name wasn't set correctly", yAxis, xy.getYAxisName());
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testEmptyGetMinX() {
		if (!isArbitrarilyDiscretized())
			throw new IndexOutOfBoundsException(); // hack to just pass these as non arb doesn't allow empty
		newEmptyDataSet().getMinX();
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testEmptyGetMaxX() {
		if (!isArbitrarilyDiscretized())
			throw new IndexOutOfBoundsException(); // hack to just pass these as non arb doesn't allow empty
		newEmptyDataSet().getMaxX();
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testEmptyGetMinY() {
		if (!isArbitrarilyDiscretized())
			throw new IndexOutOfBoundsException(); // hack to just pass these as non arb doesn't allow empty
		newEmptyDataSet().getMinY();
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testEmptyGetMaxY() {
		if (!isArbitrarilyDiscretized())
			throw new IndexOutOfBoundsException(); // hack to just pass these as non arb doesn't allow empty
		newEmptyDataSet().getMaxY();
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testEmptySetByIndex() {
		if (!isArbitrarilyDiscretized())
			throw new IndexOutOfBoundsException(); // hack to just pass these as non arb doesn't allow empty
		newEmptyDataSet().set(1, 0.142124);
	}
	
	@Test
	public void testGetOutOfBoundsBelow() {
		XY_DataSet populated = newPopulatedDataSet(100, false, false);
		assertNull("should be null for a get out of bounds", populated.get(-1));
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetXOutOfBounds() {
		XY_DataSet populated = newPopulatedDataSet(100, false, false);
		populated.getX(-1);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testGetYOutOfBounds() {
		XY_DataSet populated = newPopulatedDataSet(100, false, false);
		populated.getX(-1);
	}
	
	@Test
	public void testGetOutOfBoundsAbove() {
		XY_DataSet populated = newPopulatedDataSet(100, false, false);
		assertNull("should be null for a get out of bounds", populated.get(100));
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void testPopulatedSetByIndexOutOfBounds() {
		XY_DataSet populated = newPopulatedDataSet(100, false, false);
		populated.set(populated.getNum(), 0.142124);
	}
	
	@Test
	public void testGetNum() {
		XY_DataSet xy;
		if (isArbitrarilyDiscretized()) {
			xy = newEmptyDataSet();
			assertEquals("should be 0 for empty dataset", 0, xy.getNum());
		}
		
		xy = newPopulatedDataSet(100, isArbitrarilyDiscretized(), false);
		assertEquals("should be 100 for pupulated dataset", 100, xy.getNum());
	}
	
	@Test
	public void testGetClosestX() {
		if (isArbitrarilyDiscretized())
			assertTrue("should be NaN on an empty list!", Double.isNaN(newEmptyDataSet().getClosestX(0)));
		
		int num = 10;
		XY_DataSet xy = newPopulatedDataSet(num, false, false);
		
		// check below min
		double minY = xy.getMinY();
		double xAtMinY = Double.NaN;
		for (int i=0; i<xy.getNum(); i++) {
			Point2D pt = xy.get(i);
			if (pt.getY() == minY) {
				xAtMinY = pt.getX();
				break;
			}
		}
		assertEquals("getClosestX for below min Y should return x at first smallest y", xAtMinY, xy.getClosestX(minY-1d), test_tol);
		
		// check above max
		double maxY = xy.getMaxY();
		double xAtMaxY = Double.NaN;
		for (int i=0; i<xy.getNum(); i++) {
			Point2D pt = xy.get(i);
			if (pt.getY() == maxY) {
				xAtMaxY = pt.getX();
				break;
			}
		}
		assertEquals("getClosestX for below min Y should return x at first smallest y", xAtMaxY, xy.getClosestX(maxY+1d), test_tol);
		
		// test equals
		for (int i=0; i<num; i++)
			assertEquals("getClosestX for exact matches should return respective x", xy.getX(i), xy.getClosestX(xy.getY(i)), test_tol);
		
		// test inbetweens
		for (int i=0; i<num; i++) {
			double x = xy.getX(i);
			double y = xy.getY(i);
			assertEquals("getClosestX messed up for inbetweens!", x, xy.getClosestX(y+0.2), test_tol);
			assertEquals("getClosestX messed up for inbetweens!", x, xy.getClosestX(y-0.2), test_tol);
		}
	}
	
	@Test
	public void testGetClosestY() {
		if (isArbitrarilyDiscretized())
			assertTrue("should be NaN on an empty list!", Double.isNaN(newEmptyDataSet().getClosestY(0)));
		
		int num = 10;
		XY_DataSet xy = newPopulatedDataSet(num, false, false);
		
		// check below min
		assertEquals("getClosestY for below min x should return y at smallest x", xy.getY(0), xy.getClosestY(xy.getMinX()-1d), test_tol);
		
		// check above max
		assertEquals("getClosestY for above max x should return y at largest x", xy.getY(num-1), xy.getClosestY(xy.getMaxX()+1d), test_tol);
		
		// test equals
		for (int i=0; i<num; i++)
			assertEquals("getClosestY for exact matches should return respective y", xy.getY(i), xy.getClosestY(xy.getX(i)), test_tol);
		
		// test inbetweens
		for (int i=0; i<num; i++) {
			double x = xy.getX(i);
			double y = xy.getY(i);
			assertEquals("getClosestY messed up for inbetweens!", y, xy.getClosestY(x+0.2), test_tol);
			assertEquals("getClosestY messed up for inbetweens!", y, xy.getClosestY(x-0.2), test_tol);
		}
	}
	
	@Test
	public void testPopulatedMinMax() {
		int num = 100;
		XY_DataSet xy = newPopulatedDataSet(num, isArbitrarilyDiscretized(), true);
		
		// these are tested elsewhere
		MinMaxAveTracker xTrack = new MinMaxAveTracker();
		MinMaxAveTracker yTrack = new MinMaxAveTracker();
		
		for (int i=0; i<xy.getNum(); i++) {
			xTrack.addValue(xy.getX(i));
			yTrack.addValue(xy.getY(i));
		}
		
		assertEquals("x min is wrong", xTrack.getMin(), xy.getMinX(), test_tol);
		assertEquals("x max is wrong", xTrack.getMax(), xy.getMaxX(), test_tol);
		assertEquals("y min is wrong", yTrack.getMin(), xy.getMinY(), test_tol);
		assertEquals("y max is wrong", yTrack.getMax(), xy.getMaxY(), test_tol);
		
		if (isArbitrarilyDiscretized()) {
			// add a bunch more random points
			
			for (int i=0; i<1000; i++) {
				double x = Math.random() * 2d - 1d;
				double y = Math.random() * 2d - 1d;
				xy.set(x, y);
				xTrack.addValue(x);
				yTrack.addValue(y);
			}
			
			assertEquals("x min is wrong", xTrack.getMin(), xy.getMinX(), test_tol);
			assertEquals("x max is wrong", xTrack.getMax(), xy.getMaxX(), test_tol);
			assertEquals("y min is wrong", yTrack.getMin(), xy.getMinY(), test_tol);
			assertEquals("y max is wrong", yTrack.getMax(), xy.getMaxY(), test_tol);
		}
	}
	
	private static void checkListVals(XY_DataSet xy, List<Double> expectedX,
			List<Double> expectedY) {
		int num = expectedX.size();
		Preconditions.checkState(num == expectedY.size());
		
		assertEquals("size is inconsistant", num, xy.getNum());
		
		for (int i=0; i<num; i++) {
			assertEquals("x value off at pt: "+i, expectedX.get(i), xy.getX(i), test_tol);
			assertEquals("y value off at pt: "+i, expectedY.get(i), xy.getY(i), test_tol);
		}
	}
	
	@Test
	public void testPopulation() {
		if (!isArbitrarilyDiscretized())
			return; // only works on arb discr functions
		XY_DataSet xy = newEmptyDataSet();
		
		assertEquals("initial size is off", 0, xy.getNum());
		
		ArrayList<Double> xVals = new ArrayList<Double>();
		ArrayList<Double> yVals = new ArrayList<Double>();
		
		// do some sequential insertions
		xy.set(100d, 200d);
		xVals.add(100d);
		yVals.add(200d);
		
		checkListVals(xy, xVals, yVals);
		
		// add something after that
		xy.set(500d, 2000d);
		xVals.add(500d);
		yVals.add(2000d);
		
		checkListVals(xy, xVals, yVals);
		
		// add something at the start
		xy.set(1d, 220d);
		xVals.add(0, 1d);
		yVals.add(0, 220d);
		
		checkListVals(xy, xVals, yVals);
		
		// add something in the middle
		xy.set(101d, 222d);
		xVals.add(2, 101d);
		yVals.add(2, 222d);
		
		checkListVals(xy, xVals, yVals);
		
		// add something at the end again
		xy.set(10000d, 222000d);
		xVals.add(10000d);
		yVals.add(222000d);
		
		checkListVals(xy, xVals, yVals);
	}
	
	/**
	 * This can be overridden for functions that do something special such as adding instead of overriding values
	 * 
	 * @param prevY
	 * @param newY
	 * @return
	 */
	double expectedSetValue(double prevY, double newY) {
		return newY;
	}
	
	@Test
	public void testOverrideValue() {
		int num = 100;
		XY_DataSet xy = newPopulatedDataSet(num, isArbitrarilyDiscretized(), true);
		
		for (int i=0; i<num; i++) {
			double newY = Math.random();
			double x = xy.getX(i);
			
			// test settig by index
			double prevY = xy.getY(i);
			double expectedY = expectedSetValue(prevY, newY);
			xy.set(i, newY);
			
			// test setting by x value
			newY = Math.random();
			prevY = xy.getY(i);
			expectedY = expectedSetValue(prevY, newY);
			xy.set(x, newY);
			assertEquals("override y value by x val didn't work", expectedY, xy.getY(i), test_tol);
			
			// test setting by Point2d object
			newY = Math.random();
			prevY = xy.getY(i);
			expectedY = expectedSetValue(prevY, newY);
			xy.set(new Point2D.Double(x, newY));
			assertEquals("override y value by x val didn't work", expectedY, xy.getY(i), test_tol);
		}
	}
	
	@Test
	public void testGets() {
		int num = 100;
		XY_DataSet xy = newPopulatedDataSet(num, isArbitrarilyDiscretized(), true);
		for (int i=0; i<num; i++) {
			Point2D pt = xy.get(i);
			double x = xy.getX(i);
			double y = xy.getY(i);
			assertEquals("get(index).getX() != getX(index)", pt.getX(), x, test_tol);
			assertEquals("get(index).getY() != getY(index)", pt.getY(), y, test_tol);
		}
	}
	
	@Test
	public void testHasPoint() {
		int num = 100;
		XY_DataSet xy = newQuickTestDataSet();
		
		assertFalse("hasPoint should return false for null", xy.hasPoint(null));
		
		for (int i=0; i<num; i++)
			assertFalse("hasPoint should return false for random vals on empty", xy.hasPoint(Math.random(), Math.random()));
		
		xy = newPopulatedDataSet(num, isArbitrarilyDiscretized(), false);
		for (int i=0; i<num; i++) {
			double x = xy.getX(i);
			double y = xy.getY(i);
			
			double diffY = randomNotEqualTo(y);
			
			assertTrue("hasPoint should return true when given values from getters", xy.hasPoint(new Point2D.Double(x, y)));
			assertTrue("hasPoint should return true when given values from getters", xy.hasPoint(x, y));
			if (isArbitrarilyDiscretized()) {
				//TODO remove this hack!!!!
				assertTrue("hasPoint should still return true when given a different y value", xy.hasPoint(new Point2D.Double(x, diffY)));
				assertTrue("hasPoint should still return true when given a different y value", xy.hasPoint(x, diffY));
			}
		}
	}
	
	@Test
	public void testIterators() {
		int num = 100;
		XY_DataSet xy = newPopulatedDataSet(num, isArbitrarilyDiscretized(), true);
		
		Iterator<Point2D> ptIt = xy.iterator();
		Iterator<Double> xIt = xy.getXValuesIterator();
		Iterator<Double> yIt = xy.getYValuesIterator();
		
		for (int i=0; i<xy.getNum(); i++) {
			assertTrue("hasNext() (point iterator) should be true!", ptIt.hasNext());
			assertTrue("hasNext() (x iterator) should be true!", xIt.hasNext());
			assertTrue("hasNext() (y iterator) should be true!", yIt.hasNext());
			
			Point2D pt = ptIt.next();
			double x = xIt.next();
			double y = yIt.next();
			
			assertEquals("x value from pt iterator is incorrect!", xy.getX(i), pt.getX(), test_tol);
			assertEquals("y value from pt iterator is incorrect!", xy.getY(i), pt.getY(), test_tol);
			assertEquals("x value from x iterator is incorrect!", xy.getX(i), x, test_tol);
			assertEquals("y value from y iterator is incorrect!", xy.getY(i), y, test_tol);
		}
		
		assertFalse("hasNext() (point iterator) should be false when done!", ptIt.hasNext());
		assertFalse("hasNext() (x iterator) should be false when done!", xIt.hasNext());
		assertFalse("hasNext() (y iterator) should be false when done!", yIt.hasNext());
	}
	
	@Test
	public void testToString() {
		// just make sure it's not null or empty, even for empty functions
		String str = newQuickTestDataSet().toString();
		assertNotNull("to string is null for empty dataset", str);
		assertFalse("to string is empty for empty dataset", str.isEmpty());
		
		str = newPopulatedDataSet(100, isArbitrarilyDiscretized(), true).toString();
		assertNotNull("to string is null for pupulated dataset", str);
		assertFalse("to string is empty for pupulated dataset", str.isEmpty());
	}
	
	private void testDeepClone(XY_DataSet xy) {
		if (xy.getName() == null || xy.getName().isEmpty())
			xy.setName("test name "+System.currentTimeMillis());
		if (xy.getInfo() == null || xy.getInfo().isEmpty())
			xy.setInfo("test info "+System.currentTimeMillis());
		if (xy.getXAxisName() == null || xy.getXAxisName().isEmpty())
			xy.setXAxisName("test x axis name "+System.currentTimeMillis());
		if (xy.getYAxisName() == null || xy.getYAxisName().isEmpty())
			xy.setYAxisName("test y axis name "+System.currentTimeMillis());
		XY_DataSet cloned = xy.deepClone();
		
		int num = xy.getNum();
		assertEquals("size of clone incorrect", num, cloned.getNum());
		assertEquals("name not copied over with clone", xy.getName(), cloned.getName());
		assertEquals("info not copied over with clone", xy.getInfo(), cloned.getInfo());
		assertEquals("x axis name not copied over with clone", xy.getXAxisName(), cloned.getXAxisName());
		assertEquals("y axis name not copied over with clone", xy.getYAxisName(), cloned.getYAxisName());
		
		// test that values got copied correctly
		for (int i=0; i<num; i++) {
			assertEquals("x value in clone wrong at ind "+i, xy.getX(i), cloned.getX(i), test_tol);
			assertEquals("y value in clone wrong at ind "+i, xy.getY(i), cloned.getY(i), test_tol);
		}
		
		// test that changing one doesn't change the other
		for (int i=0; i<num; i++) {
			double origY = xy.getY(i);
			double clonedNewY = randomNotEqualTo(origY);
			cloned.set(i, clonedNewY);
			assertEquals("y value shouldn't change in orig function after clone was changed", origY, xy.getY(i), test_tol);
			double newY = randomNotEqualTo(clonedNewY);
			xy.set(i, newY);
			assertEquals("y value shouldn't change in cloned function after orig was changed", clonedNewY, cloned.getY(i), test_tol);
		}
		
		// now make sure adding new vals does affect anything
		if (isArbitrarilyDiscretized()) {
			for (int i=0; i<10; i++) {
				double newX = 0;
				while (newX == 0 || xy.hasPoint(newX, 0)) {
					 newX = Math.random() * 100d - 50d;
				}
				double newY = Math.random();
				
				int clonedNum = cloned.getNum();
				xy.set(newX, newY);
				assertEquals("cloned shouldn't have grown with addition of new pt to orig", clonedNum, cloned.getNum());
				if (i % 2 == 0) {
					int origNum = xy.getNum();
					newX = 0;
					while (newX == 0 || xy.hasPoint(newX, 0)) {
						 newX = Math.random() * 100d - 50d;
					}
					newY = Math.random();
					cloned.set(newX, newY);
					assertEquals("oritg shouldn't have grown with addition of new pt to cloned", origNum, xy.getNum());
				}
			}
		}
	}
	
	@Test
	public void testEmptyDeepClone() {
		testDeepClone(newQuickTestDataSet());
	}
	
	@Test
	public void testPopulatedDeepClone() {
		testDeepClone(newPopulatedDataSet(100, isArbitrarilyDiscretized(), false));
		testDeepClone(newPopulatedDataSet(100, isArbitrarilyDiscretized(), true));
	}
	
	@Test
	public void testAreAllXValuesInteger() {
		XY_DataSet xy = newPopulatedDataSet(10, false, false);
		
		assertTrue("initially populated dataset should be all integers", xy.areAllXValuesInteger(0d));
		if (isArbitrarilyDiscretized()) {
			xy.set(0.1, 0);
			xy.set(-0.1, 0);
			assertFalse("add non integer x value and it still says all vals are ints!", xy.areAllXValuesInteger(0d));
			assertTrue("should be true here", xy.areAllXValuesInteger(0.1d));
			xy.set(0.15, 0);
			xy.set(-0.15, 0);
			assertTrue("should be true here", xy.areAllXValuesInteger(0.15d));
			xy.set(0.2, 0);
			xy.set(-0.2, 0);
			assertTrue("should be true here", xy.areAllXValuesInteger(0.2d));
		}
		
		if (isArbitrarilyDiscretized()) {
			xy = newPopulatedDataSet(1000, true, false);
			assertFalse("xy vals should not be all integers when set randomly", xy.areAllXValuesInteger(0d));
			// every number is within 0.5 of an integer
			assertTrue("every random number is within 0.5 of an int, therefore this should return true", xy.areAllXValuesInteger(0.5d));
		}
	}

}