package org.opensha.commons.util;

import java.io.IOException;
import java.util.List;

import org.opensha.commons.data.Location;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class GMT_GrdFile {
	
	private NetcdfFile file;
	
	private int xDim, yDim;
	private Array xData;
	private Array yData;
	
	private Array zData = null;
	private Index zInd = null;
	private Variable zVar;
	
	private static int singleShape[] = {1, 1};
	
	public GMT_GrdFile(String fileName) throws IOException {
		this(fileName, true);
	}
	
	public GMT_GrdFile(String fileName, boolean cacheZ) throws IOException {
		file = NetcdfFile.openInMemory(fileName);
		List<Dimension> dims = file.getDimensions();
		xDim = dims.get(0).getLength();
		yDim = dims.get(1).getLength();
		
		List<Variable> vars = file.getVariables();
		Variable xVar = vars.get(0);
		Variable yVar = vars.get(1);
		
		xData = xVar.read();
		yData = yVar.read();
		
		zVar = vars.get(2);
		if (cacheZ) {
			zData = zVar.read();
			zInd = zData.getIndex();
		}
	}
	
	public int getXSize() {
		return xDim;
	}
	
	public int getYSize() {
		return yDim;
	}
	
	public double getX(int x) {
		return xData.getDouble(x);
	}
	
	public Array getXData() {
		return xData;
	}
	
	public double getY(int y) {
		return yData.getDouble(y);
	}
	
	public Array getYData() {
		return yData;
	}
	
	public Location getLoc(int x, int y) {
		return new Location(getY(y), getX(x));
	}
	
	public double getZ(int x, int y) throws IOException, InvalidRangeException {
		int pt[] = {y, x};
		return getZ(pt);
	}
	
	public double getZ(int pt[]) throws IOException, InvalidRangeException {
		if (zData != null) {
			zInd.set(pt);
			return zData.getDouble(zInd);
		} else {
			Array data = zVar.read(pt, singleShape).reduce();
			return data.getDouble(0);
		}
	}
	
	public Array getZData() throws IOException {
		if (zData == null) {
			zData = zVar.read();
		}
		return zData;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidRangeException 
	 */
	public static void main(String[] args) throws IOException, InvalidRangeException {
		boolean cacheZ = true;
		GMT_GrdFile grd = new GMT_GrdFile("/tmp/sum_slab1.0_clip.grd", cacheZ);
		
		int x = 1649;
		int y = 146;
		
		System.out.println(grd.getLoc(x, y) + ": " + grd.getZ(x, y));
	}

}
