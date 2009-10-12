package org.opensha.commons.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.opensha.commons.data.Location;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * This class wraps the NetCDF-Java library for use specifically with GMT-style NetCDF files (.grd files).
 * 
 * X and Y values are pre-loaded, and Z values can either be cached (faster when there is ample memory
 * for the file and either a large chunk of the file will be used, or lots of random access) or loaded
 * on demand.
 * 
 * @author kevin
 *
 */
public class GMT_GrdFile {
	
	private NetcdfFile file;
	
	private int xDim, yDim;
	private Array xData;
	private Array yData;
	
	private Array zData = null;
	private Index zInd = null;
	private Variable zVar;
	
	private static int singleShape[] = {1, 1};
	
	/**
	 * Load the given GRD file...it will be cached by default
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public GMT_GrdFile(String fileName) throws IOException {
		this(fileName, true);
	}
	
	/**
	 * Load the given GRD file...it will be cached if cacheZ is true
	 * 
	 * @param fileName
	 * @param cacheZ
	 * @throws IOException
	 */
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
			cacheZData();
		}
	}
	
	/**
	 * Get the number of X points in the GRD file
	 * 
	 * @return
	 */
	public int getNumX() {
		return xDim;
	}
	
	/**
	 * Get the number of Y points in the GRD file
	 * 
	 * @return
	 */
	public int getNumY() {
		return yDim;
	}
	
	/**
	 * Get the longitude (X) pt at the given X-index
	 * 
	 * @param x
	 * @return
	 */
	public double getX(int x) {
		return xData.getDouble(x);
	}
	
	/**
	 * Get a NetCDF Array with all of the longitude (X) data
	 * 
	 * @return
	 */
	public Array getXData() {
		return xData;
	}
	
	/**
	 * Get the latitude (Y) pt at the given X-index
	 * 
	 * @param x
	 * @return
	 */
	public double getY(int y) {
		return yData.getDouble(y);
	}
	
	/**
	 * Get a NetCDF Array with all of the latitude (Y) data
	 * 
	 * @return
	 */
	public Array getYData() {
		return yData;
	}
	
	/**
	 * Get the Location at the following X,Y indexes
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Location getLoc(int x, int y) {
		return new Location(getY(y), getX(x));
	}
	
	/**
	 * Get the Z value at the given X,Y indexes
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public double getZ(int x, int y) throws IOException, InvalidRangeException {
		int pt[] = {y, x};
		return getZ(pt);
	}
	
	/**
	 * Get the Z value at the given pt. The given point should be a 2-dimensional
	 * integer array in the format (y,x)
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	public double getZ(int pt[]) throws IOException, InvalidRangeException {
		if (zData != null) {
			zInd.set(pt);
			return zData.getDouble(zInd);
		} else {
			Array data = zVar.read(pt, singleShape).reduce();
			return data.getDouble(0);
		}
	}
	
	/**
	 * Load the entire Z data into memory if it hasn't already been loaded
	 * 
	 * @throws IOException
	 */
	public void cacheZData() throws IOException {
		if (zData == null) {
			zData = zVar.read();
		}
	}
	
	/**
	 * Get a NetCDF Array with all of the Z data. It will be cached if needed.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Array getZData() throws IOException {
		cacheZData();
		return zData;
	}
	
	/**
	 * Return the name field embedded into this GRD file.
	 * 
	 * @return
	 */
	public String getZName() {
		return zVar.getName();
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
		
		FileWriter fw = new FileWriter("/tmp/slab.txt");
		
		for (x=0; x<grd.getNumX(); x++) {
			for (y=0; y<grd.getNumY(); y++) {
				Location loc = grd.getLoc(x, y);
				double val = grd.getZ(x, y);
				fw.write(loc.getLatitude() + "\t" + loc.getLongitude() + "\t" + val + "\n");
			}
		}
		fw.close();
	}

}
