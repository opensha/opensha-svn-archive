package org.opensha.commons.data.siteData.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.opensha.commons.data.siteData.AbstractSiteData;
import org.opensha.commons.data.siteData.servlet.SiteDataServletAccessor;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.binFile.BinaryMesh2DCalculator.DataType;
import org.opensha.commons.util.binFile.GeolocatedRectangularBinaryMesh2DCalculator;

abstract class AbstractCVMBasinDepth extends AbstractSiteData<Double> {
	
	private int nx;
	private int ny;
	private double minLat;
	private double minLon;
	private double gridSpacing;
	private boolean startBottom;
	private boolean startLeft;
	private String type;
	protected boolean useServlet;
	protected File dataFile; 
	
	GeolocatedRectangularBinaryMesh2DCalculator calc;
	private SiteDataServletAccessor<Double> servlet = null;
	private RandomAccessFile file;
	private byte[] recordBuffer;
	private FloatBuffer floatBuff;
	
	long maxFilePos;
	
	public AbstractCVMBasinDepth(int nx, int ny, double minLat, double minLon,
			double gridSpacing, boolean startBottom, boolean startLeft,
			String type, File dataFile, boolean useServlet) throws IOException {
		super();
		
		this.nx = nx;
		this.ny = ny;
		this.minLat = minLat;
		this.minLon = minLon;
		this.gridSpacing = gridSpacing;
		this.startBottom = startBottom;
		this.startLeft = startLeft;
		this.type = type;
		this.useServlet = useServlet;
		
		maxFilePos = (nx*ny - 1) * 4;
		
		calc = new GeolocatedRectangularBinaryMesh2DCalculator(
				DataType.FLOAT, nx, ny, minLat, minLon, gridSpacing);
		
		if (useServlet) {
			servlet = new SiteDataServletAccessor<Double>(getServletURL(type));
		} else {
			if (dataFile == null) {
				dataFile = getDefaultFile(type);
			}
			
			file = new RandomAccessFile(dataFile, "r");
			
			calc.setStartBottom(true);
			calc.setStartLeft(true);
			
			recordBuffer = new byte[4];
			ByteBuffer record = ByteBuffer.wrap(recordBuffer);
			record.order(ByteOrder.LITTLE_ENDIAN);
			
			floatBuff = record.asFloatBuffer();
		}
		initDefaultBasinParams();
		this.paramList.addParameter(minBasinDoubleParam);
		this.paramList.addParameter(maxBasinDoubleParam);
		
		this.dataFile = dataFile;
	}
	
	abstract File getDefaultFile(String type);
	
	abstract String getServletURL(String type);
	
	public final Region getApplicableRegion() {
		return calc.getApplicableRegion();
	}

	public final Location getClosestDataLocation(Location loc) {
		return calc.calcClosestLocation(loc);
	}
	
	public final double getResolution() {
		return gridSpacing;
	}

	public final String getDataType() {
		return type;
	}
	
	public Double getValue(Location loc) throws IOException {
		if (useServlet) {
			return certifyMinMaxBasinDepth(servlet.getValue(loc));
		} else {
			long pos = calc.calcClosestLocationFileIndex(loc);
			
			return getValue(pos);
		}
	}
	
	Double getValue(long pos) throws IOException {
		if (pos > maxFilePos || pos < 0)
			return Double.NaN;
		
		file.seek(pos);
		file.read(recordBuffer);
		
		// this is in meters
		double val = floatBuff.get(0);
		
		// convert to KM
		Double dobVal = (double)val / 1000d;
		return certifyMinMaxBasinDepth(dobVal);
	}
	
	public final ArrayList<Double> getValues(LocationList locs) throws IOException {
		if (useServlet) {
			ArrayList<Double> vals = servlet.getValues(locs);
			for (int i=0; i<vals.size(); i++) {
				vals.set(i, certifyMinMaxBasinDepth(vals.get(i)));
			}
			return vals;
		} else {
			return super.getValues(locs);
		}
	}

	public final boolean isValueValid(Double val) {
		return val != null && !Double.isNaN(val);
	}

}
