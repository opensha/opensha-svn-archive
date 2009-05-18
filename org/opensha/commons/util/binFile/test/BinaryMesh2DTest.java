package org.opensha.commons.util.binFile.test;

import org.opensha.commons.util.binFile.BinaryMesh2DCalculator;

import junit.framework.TestCase;

public class BinaryMesh2DTest extends TestCase {
	
	BinaryMesh2DCalculator singleRow;
	BinaryMesh2DCalculator singleCol;
	BinaryMesh2DCalculator rect;
	BinaryMesh2DCalculator rect_fast_yx;

	public BinaryMesh2DTest(String name) {
		super(name);
		singleRow = new BinaryMesh2DCalculator(BinaryMesh2DCalculator.TYPE_FLOAT, 10, 1);
		singleCol = new BinaryMesh2DCalculator(BinaryMesh2DCalculator.TYPE_FLOAT, 1, 10);
		rect = new BinaryMesh2DCalculator(BinaryMesh2DCalculator.TYPE_FLOAT, 7, 11);
		rect_fast_yx = new BinaryMesh2DCalculator(BinaryMesh2DCalculator.TYPE_FLOAT, 7, 11);
		rect_fast_yx.setMeshOrder(BinaryMesh2DCalculator.FAST_YX);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSingleRow() {
		for (int x=0; x<singleRow.getNX(); x++) {
			long ind = singleRow.calcMeshIndex(x, 0);
			assertTrue(ind == x);
			long fInd = singleRow.calcFileIndex(x, 0);
			assertTrue(fInd == (ind * 4));
		}
	}
	
	public void testSingleCol() {
		for (int y=0; y<singleCol.getNY(); y++) {
			long ind = singleCol.calcMeshIndex(0, y);
			assertTrue(ind == y);
			long fInd = singleCol.calcFileIndex(0, y);
			assertTrue(fInd == (ind * 4));
		}
	}
	
	public void testRect() {
		for (int x=0; x<rect.getNX(); x++) {
			for (int y=0; y<rect.getNY(); y++) {
				long ind = rect.calcMeshIndex(x, y);
				assertTrue(ind == (x + y*rect.getNX()));
				long fInd = rect.calcFileIndex(x, y);
				assertTrue(fInd == (ind * 4));
			}
		}
	}
	
	public void testRectYX() {
		for (int x=0; x<rect_fast_yx.getNX(); x++) {
			for (int y=0; y<rect_fast_yx.getNY(); y++) {
				long ind = rect_fast_yx.calcMeshIndex(x, y);
				assertTrue(ind == (y + x*rect.getNY()));
				long fInd = rect_fast_yx.calcFileIndex(x, y);
				assertTrue(fInd == (ind * 4));
			}
		}
	}

}
