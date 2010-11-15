package org.opensha.commons.data.xyz;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.FileUtils;

/**
 * This class represents an evenly discretized XYZ dataset. Data is stored as an array, and set/get
 * operations will use the closest point in the data if it's not exact.
 * 
 * @author kevin
 *
 */
public class EvenlyDiscrXYZ_DataSet implements XYZ_DataSetAPI {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double data[][];
	
	private int ny;
	private int nx;
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private double gridSpacing;
	
	public EvenlyDiscrXYZ_DataSet(int nx, int ny, double minX, double minY, double gridSpacing) {
		this(new double[ny][nx], minX, minY, gridSpacing);
	}
	
	public EvenlyDiscrXYZ_DataSet(double[][] data, double minX, double minY, double gridSpacing) {
		this.data = data;
		this.minX = minX;
		this.minY = minY;
		this.gridSpacing = gridSpacing;
		
		this.ny = data.length;
		this.nx = data[0].length;
		
		maxX = minX + gridSpacing * (nx-1);
		maxY = minY + gridSpacing * (ny-1);
		
//		System.out.println("EvenlyDiscretizedXYZ_DataSet: minX: " + minX + ", maxX: " + maxX
//				+ ", minY: " + minY + ", maxY: " + maxY);
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMaxZ() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		
		for (int row=0; row<nx; row++) {
			for (int col=0; col<ny; col++) {
				double val = get(col, row);
				tracker.addValue(val);
			}
		}
		
		return tracker.getMax();
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMinZ() {
		MinMaxAveTracker tracker = new MinMaxAveTracker();
		
		for (int row=0; row<nx; row++) {
			for (int col=0; col<ny; col++) {
				double val = get(col, row);
				tracker.addValue(val);
			}
		}
		
		return tracker.getMin();
	}
	
	/**
	 * Get the grid spacing of this evenly discretized dataset
	 * @return
	 */
	public double getGridSpacing() {
		return gridSpacing;
	}
	
	public int getNumX() {
		return nx;
	}
	
	public int getNumY() {
		return ny;
	}
	
	public void writeXYZBinFile(String fileNamePrefix) throws IOException {
		FileWriter header = new FileWriter(fileNamePrefix + ".hdr");
		header.write("ncols" + "\t" + nx + "\n");
		header.write("nrows" + "\t" + ny + "\n");
		header.write("xllcorner" + "\t" + minX + "\n");
		header.write("yllcorner" + "\t" + minY + "\n");
		header.write("cellsize" + "\t" + gridSpacing + "\n");
		header.write("NODATA_value" + "\t" + "-9999" + "\n");
		header.write("byteorder" + "\t" + "LSBFIRST" + "\n");
		
		header.close();
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(fileNamePrefix + ".flt"));
		
		for (int row=0; row<ny; row++) {
			for (int col=0; col<nx; col++) {
				double val = get(col, row);
				out.writeFloat((float)val);
			}
		}
		
		out.close();
	}
	
	private static String getHeaderValue(ArrayList<String> lines, String key) {
		for (String line : lines) {
			if (line.startsWith(key)) {
				StringTokenizer tok = new StringTokenizer(line);
				tok.nextToken();
				return tok.nextToken();
			}
		}
		return null;
	}
	
	public static EvenlyDiscrXYZ_DataSet readXYZBinFile(String fileNamePrefix) throws IOException {
		ArrayList<String> lines = FileUtils.loadFile(fileNamePrefix + ".hdr");
		
		int ncols = Integer.parseInt(getHeaderValue(lines, "ncols"));
		int nrows = Integer.parseInt(getHeaderValue(lines, "nrows"));
		double minX = Double.parseDouble(getHeaderValue(lines, "xllcorner"));
		double minY = Double.parseDouble(getHeaderValue(lines, "yllcorner"));
		double gridSpacing = Double.parseDouble(getHeaderValue(lines, "cellsize"));
		
		DataInputStream reader = new DataInputStream(new FileInputStream(fileNamePrefix + ".flt"));
		
		EvenlyDiscrXYZ_DataSet data = new EvenlyDiscrXYZ_DataSet(ncols, nrows, minX, minY, gridSpacing);
		
		for (int row=0; row<nrows; row++) {
			for (int col=0; col<ncols; col++) {
				double val = (double)reader.readFloat();
				
				data.set(col, row, val);
			}
		}
		
		return data;
	}
	
	private double getX(int xIndex) {
		return minX + (double)xIndex * gridSpacing;
	}
	
	private double getY(int yIndex) {
		return minY + (double)yIndex * gridSpacing;
	}
	
	private int getIndex(double x, double y) {
		int yInd = getYIndex(y);
		int xInd = getXIndex(x);
		return xInd + nx*yInd;
	}
	
	private int getXIndex(int index) {
		return index % nx;
	}
	
	private int getYIndex(int index) {
		return index / nx;
	}
	
	private int getYIndex(double y) {
		return (int)((y - minY) / gridSpacing + 0.5);
	}
	
	private int getXIndex(double x) {
		return (int)((x - minX) / gridSpacing + 0.5);
	}
	
	@Override
	public void set(Point2D point, double z) {
		this.set(point.getX(), point.getY(), z);
	}
	
	@Override
	public void set(double x, double y, double z) {
		if (!contains(x, y))
			throw new InvalidRangeException("point must be within range");
		this.set(getXIndex(x), getYIndex(y), z);
	}

	@Override
	public void set(int index, double z) {
//		System.out.println("nx: " + nx + ", ny: " + ny);
//		System.out.println("set: index="+index+", x="+getXIndex(index)+", y="+getYIndex(index));
		this.set(getXIndex(index), getYIndex(index), z);
	}

	@Override
	public double get(double x, double y) {
		return get(getXIndex(x), getYIndex(y));
	}

	@Override
	public double get(int index) {
		return get(getXIndex(index), getYIndex(index));
	}

	@Override
	public int size() {
		return nx * ny;
	}
	
	public void set(int xInd, int yInd, double z) {
		this.data[yInd][xInd] = z;
	}
	
	public double get(int xInd, int yInd) {
		return this.data[yInd][xInd];
	}

	@Override
	public double get(Point2D point) {
		return this.get(point.getX(), point.getY());
	}

	@Override
	public Point2D getPoint(int index) {
		return new Point2D.Double(getX(getXIndex(index)), getY(getYIndex(index)));
	}
	
	public int indexOf(Point2D point) {
		return getIndex(point.getX(), point.getY());
	}

	@Override
	public boolean contains(Point2D point) {
		return contains(point.getX(), point.getY());
	}
	
	public boolean contains(double x, double y) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}
	
	public void setAll(XYZ_DataSetAPI dataset) {
		for (int i=0; i<dataset.size(); i++) {
			set(dataset.getPoint(i), dataset.get(i));
		}
	}

	@Override
	public Object clone() {
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(nx, ny, minX, minY, gridSpacing);
		for (int x=0; x<nx; x++) {
			for (int y=0; y<ny; y++) {
				xyz.set(x, y, get(x, y));
			}
		}
		return xyz;
	}

	@Override
	public int indexOf(double x, double y) {
		return indexOf(new Point2D.Double(x, y));
	}

}
