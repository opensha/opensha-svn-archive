package org.opensha.commons.data;

public class Point2DToleranceSortedTreeSetTest extends
		Point2DToleranceSortedListTest {

	@Override
	protected Point2DToleranceSortedList buildList() {
		return new Point2DToleranceSortedTreeSet(new Point2DToleranceComparator());
	}

}
