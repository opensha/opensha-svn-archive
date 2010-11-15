package org.opensha.commons.data.xyz;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.FileUtils;

public class TestEvenlyDiscretizedXYZ_DataSet {

	protected static final int ncols = 10;
	protected static final int nrows = 5;
	protected static final double minX = 0.5;
	protected static final double minY = 1.5;
	protected static final double gridSpacing = 0.15;
	
	@Test
	public void testConstructors() {
		EvenlyDiscrXYZ_DataSet data = new EvenlyDiscrXYZ_DataSet(ncols, nrows, minX, minY, gridSpacing);
		
		assertTrue("ncols not set correctly", ncols == data.getNumX());
		assertTrue("nrows not set correctly", nrows == data.getNumY());
		
//		double maxX = minX + gridSpacing * (ncols-1);
		double maxX = 1.85;
		double maxY = 2.1;
		
		assertEquals("maxX not set correctly", maxX, data.getMaxX(), 0.00000001);
		assertEquals("maxY not set correctly", maxY, data.getMaxY(), 0.00000001);
	}
	
	@Test
	public void testGet() {
		EvenlyDiscrXYZ_DataSet data = new EvenlyDiscrXYZ_DataSet(ncols, nrows, minX, minY, gridSpacing);
		
		for (int i=0; i<data.size(); i++) {
			double valByIndex = data.get(i);
			double valByIndexPt = data.get(data.getPoint(i));
			double valByIndexDoubleDouble = data.get(data.getPoint(i).getX(), data.getPoint(i).getY());
			
			assertEquals("val by index point isn't equal", valByIndex, valByIndexPt, 0d);
			assertEquals("val by index doubles isn't equal", valByIndex, valByIndexDoubleDouble, 0d);
		}
	}
	
	@Test
	public void testSet() {
		EvenlyDiscrXYZ_DataSet data = new EvenlyDiscrXYZ_DataSet(ncols, nrows, minX, minY, gridSpacing);
		try {
			data.set(0, minY, 0);
			fail("Should throw InvalidRangeException because x less than minX");
		} catch (InvalidRangeException e) {}
		
		try {
			data.set(minX, 0, 0);
			fail("Should throw InvalidRangeException because y less than minY");
		} catch (InvalidRangeException e) {}
		
		try {
			data.set(data.getMaxX() + 1, minY, 0);
			fail("Should throw InvalidRangeException because x greater than maxX");
		} catch (InvalidRangeException e) {}
		
		try {
			data.set(minX, data.getMaxY() + 1, 0);
			fail("Should throw InvalidRangeException because y greater than maxY");
		} catch (InvalidRangeException e) {}
		
		data.set(minX, minY, 0.35);
		assertEquals("set didn't work", 0.35, data.get(minX, minY), 0.0000001);
		assertEquals("set didn't work", 0.35, data.get(0, 0), 0.0000001);
		
		data.set(minX + 0.06, minY + 0.06, 0.35);
		assertEquals("set didn't work", 0.35, data.get(minX, minY), 0.0000001);
		assertEquals("set didn't work", 0.35, data.get(0, 0), 0.0000001);
		
		data.set(minX + gridSpacing - 0.06, minY + gridSpacing - 0.06, 0.35);
		assertEquals("set didn't work", 0.35, data.get(minX + gridSpacing, minY + gridSpacing), 0.0000001);
		assertEquals("set didn't work", 0.35, data.get(1, 1), 0.0000001);
		
		data.set(data.getPoint(0), 5.5);
		assertEquals("set by point didn't work", 5.5, data.get(0), 0d);
		data.set(data.indexOf(data.getPoint(0)), 5.6);
		assertEquals("set by index didn't work", 5.6, data.get(0), 0d);
	}
	
	@Test
	public void testBinaryIO() throws IOException {
		File tempDir = FileUtils.createTempDir();
		String fileNamePrefix = tempDir.getAbsolutePath() + File.separator + "data";
		EvenlyDiscrXYZ_DataSet data = new EvenlyDiscrXYZ_DataSet(ncols, nrows, minX, minY, gridSpacing);
		
		for (int row=0; row<nrows; row++) {
			for (int col=0; col<ncols; col++) {
				data.set(col, row, Math.random());
			}
		}
		
		data.writeXYZBinFile(fileNamePrefix);
		
		EvenlyDiscrXYZ_DataSet newData = EvenlyDiscrXYZ_DataSet.readXYZBinFile(fileNamePrefix);
		
		for (int row=0; row<nrows; row++) {
			for (int col=0; col<ncols; col++) {
				double origVal = data.get(col, row);
				double newVal = newData.get(col, row);
				
				assertEquals("", origVal, newVal, 0.00001);
			}
		}
		
		for (File file : tempDir.listFiles()) {
			file.delete();
		}
		tempDir.delete();
	}

}
