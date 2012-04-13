package org.opensha.commons.data.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class AbstractDiscretizedFuncTest extends
		AbstractXY_DataSetTest {
	
	/* redefined here to require them to be DiscretizedFunc instnaces */
	
	abstract DiscretizedFunc newEmptyDataSet();
	
	@Override
	DiscretizedFunc newPopulatedDataSet(int num, boolean randomX, boolean randomY) {
		return (DiscretizedFunc)super.newPopulatedDataSet(num, randomX, randomY);
	}
	
	DiscretizedFunc newQuickTestDataSet() {
		return (DiscretizedFunc)super.newQuickTestDataSet();
	}
	
	@Test
	public void testPopulatedGetByX() {
		int num = 100;
		DiscretizedFunc xy = newPopulatedDataSet(num, isArbitrarilyDiscretized(), true);
		
		for (int i=0; i<num; i++) {
			double x = xy.getX(i);
			double yByIndex = xy.getY(i);
			double yByX = xy.getY(x);
			assertEquals("getY different by index vs by x", yByIndex, yByX, 1e-10);
		}
	}

}
