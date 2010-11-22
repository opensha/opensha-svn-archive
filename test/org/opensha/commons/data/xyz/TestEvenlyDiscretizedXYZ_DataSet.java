package org.opensha.commons.data.xyz;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
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
	
	private EvenlyDiscrXYZ_DataSet buildTestData() {
		EvenlyDiscrXYZ_DataSet data = new EvenlyDiscrXYZ_DataSet(ncols, nrows, minX, minY, gridSpacing);
		
		for (int i=0; i<data.size(); i++) {
			data.set(i, (double)i * 0.01);
		}
		
		return data;
	}
	
	@Test
	public void testConstructors() {
		EvenlyDiscrXYZ_DataSet data = buildTestData();
		
		assertTrue("ncols not set correctly", ncols == data.getNumX());
		assertTrue("nrows not set correctly", nrows == data.getNumY());
		
		assertEquals("gridSpacing not set correctly", gridSpacing, data.getGridSpacing(), 0.00000001);
		
//		double maxX = minX + gridSpacing * (ncols-1);
		
	}
	
	@Test
	public void testMinMax() {
		EvenlyDiscrXYZ_DataSet data = buildTestData();
		
		double maxX = 1.85;
		double maxY = 2.1;
		
		double minZ = 0.0;
		double maxZ = (data.size()-1) * 0.01;
		
		assertEquals("minX not set correctly", minX, data.getMinX(), 0.00000001);
		assertEquals("minY not set correctly", minY, data.getMinY(), 0.00000001);
		assertEquals("minZ not set correctly", minZ, data.getMinZ(), 0.00000001);
		assertEquals("maxX not set correctly", maxX, data.getMaxX(), 0.00000001);
		assertEquals("maxY not set correctly", maxY, data.getMaxY(), 0.00000001);
		assertEquals("maxZ not set correctly", maxZ, data.getMaxZ(), 0.00000001);
	}
	
	@Test
	public void testGet() {
		EvenlyDiscrXYZ_DataSet data = buildTestData();
		
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
		EvenlyDiscrXYZ_DataSet data = buildTestData();
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
		EvenlyDiscrXYZ_DataSet data = buildTestData();
		
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
	
	@Test
	public void testContains() {
		EvenlyDiscrXYZ_DataSet data = buildTestData();
		
		assertTrue("data doesn't contain origin!", data.contains(new Point2D.Double(minX, minY)));
		assertTrue("data doesn't contain last point!",
				data.contains(new Point2D.Double(data.getMaxX(), data.getMaxY())));
		assertTrue("data doesn't contain origin plus a little!",
				data.contains(new Point2D.Double(minX+gridSpacing*0.0001, minY+gridSpacing*0.0001)));
		assertFalse("data contains vals before origin!", data.contains(new Point2D.Double(minX-0.001, minY)));
		assertFalse("data contains vals before origin!", data.contains(new Point2D.Double(minX, minY-0.001)));
		assertFalse("data contains vals before origin!", data.contains(new Point2D.Double(minX-0.001, minY-0.001)));
		
		assertFalse("data contains vals after last point!",
				data.contains(new Point2D.Double(data.getMaxX()+0.001, data.getMaxY())));
		assertFalse("data contains vals after last point!",
				data.contains(new Point2D.Double(data.getMaxX(), data.getMaxY()+0.001)));
		assertFalse("data contains vals after last point!",
				data.contains(new Point2D.Double(data.getMaxX()+0.001, data.getMaxY()+0.001)));
	}
	
	@Test
	public void testIndexOf() {
		EvenlyDiscrXYZ_DataSet data = buildTestData();
		
		assertEquals("origin should be index 0", 0, data.indexOf(new Point2D.Double(minX, minY)));
		assertEquals("lat point should be last index", data.size()-1,
				data.indexOf(new Point2D.Double(data.getMaxX(), data.getMaxY())));
	}
	
	@Test
	public void testSetAll() {
		EvenlyDiscrXYZ_DataSet origData = buildTestData();
		EvenlyDiscrXYZ_DataSet data = (EvenlyDiscrXYZ_DataSet)origData.clone();
		EvenlyDiscrXYZ_DataSet constData = buildTestData();
		double constVal = 1.2345;
		for (int i=0; i<constData.size(); i++)
			constData.set(i, constVal);
		
		data.setAll(constData);
		
		for (int i=0; i<data.size(); i++)
			assertEquals("setAll didn't work!", constVal, data.get(i), 0.0000001);
		
		data.setAll(origData);
		for (int i=0; i<data.size(); i++)
			assertEquals("setAll didn't work!", origData.get(i), data.get(i), 0.0000001);
	}

}
