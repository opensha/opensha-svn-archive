package org.opensha.commons.data;

import org.junit.BeforeClass;
import org.junit.Test;

public class Container2DTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	// public Container2D( int numRows, int numCols )
	@Test (expected = IllegalArgumentException.class)
	public void testContainer2D_IAE1() { new Container2D(0,1); }
	@Test (expected = IllegalArgumentException.class)
	public void testContainer2D_IAE2() { new Container2D(1,0); }
	@Test (expected = IllegalArgumentException.class)
	public void testContainer2D_IAE3() { new Container2D(46341,46341); }


}
