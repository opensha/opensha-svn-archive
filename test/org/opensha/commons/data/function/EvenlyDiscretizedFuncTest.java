package org.opensha.commons.data.function;

import com.google.common.base.Preconditions;

public class EvenlyDiscretizedFuncTest extends AbstractDiscretizedFuncTest {

	@Override
	DiscretizedFunc newEmptyDataSet() {
		return new EvenlyDiscretizedFunc(0d, 0, 0d);
	}

	@Override
	DiscretizedFunc newPopulatedDataSet(int num, boolean randomX,
			boolean randomY) {
		Preconditions.checkArgument(!randomX, "randomX is not allowed for evenly discretized functions");
		double min = defaultXValue(0);
		double max = defaultXValue(num-1);
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(min, max, num);
		
		for (int i=0; i<num; i++) {
			if (randomY)
				func.set(i, Math.random());
			else
				func.set(i, defaultYValue(i));
		}
		
		return func;
	}

	@Override
	boolean isArbitrarilyDiscretized() {
		return false;
	}

}
