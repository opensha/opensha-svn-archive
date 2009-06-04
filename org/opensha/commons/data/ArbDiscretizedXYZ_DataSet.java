package org.opensha.commons.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.data.xy.XYZDataset;

/**
 * <p>Title: ArbDiscretizedXYZ_DataSet</p>
 * <p>Description: This class creates a vector for the XYZ dataset.
 * FIX : FIX - The implementation is the quick and dirty solution for the time being and will needed to
 * modified later on based on our requirements.
 * Requires Fixation to be consistent with our implementation of the 2-D data representation</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ArbDiscretizedXYZ_DataSet implements XYZ_DataSetAPI,java.io.Serializable{

	ArrayList<Double> xValues, yValues, zValues;


	/**
	 * Default class constructor
	 */
	public ArbDiscretizedXYZ_DataSet(){};
	/**
	 * constructor that takes the xVals,yVals and zVals as the argument
	 * @param xVals = ArrayList containing the xValues
	 * @param yVals = ArrayList containing the yValues
	 * @param zVals = ArrayList containing the zValues
	 */
	public ArbDiscretizedXYZ_DataSet(ArrayList<Double> xVals, ArrayList<Double> yVals,
			ArrayList<Double> zVals) {

		xValues = xVals;
		yValues = yVals;
		zValues = zVals;
	}

	/**
	 * Initialises the x, y and z Values ArrayList
	 * @param xVals
	 * @param yVals
	 * @param zVals
	 */
	public void setXYZ_DataSet(ArrayList<Double> xVals, ArrayList<Double> yVals, ArrayList<Double> zVals){

		xValues = xVals;
		yValues = yVals;
		zValues = zVals;
	}

	/**
	 *
	 * @returns the X Values dataSet
	 */
	public ArrayList<Double> getX_DataSet(){
		return xValues;
	}

	/**
	 *
	 * @returns the Y value DataSet
	 */
	public ArrayList<Double> getY_DataSet(){
		return yValues;
	}


	/**
	 *
	 * @returns the Z value DataSet
	 */
	public ArrayList<Double> getZ_DataSet(){
		return zValues;
	}

	/**
	 *
	 * @returns the minimum of the X Values
	 */
	public double getMinX(){
		return getMin(xValues);
	}

	/**
	 *
	 * @returns the maximum of the X Values
	 */
	public double getMaxX(){
		return getMax(xValues);
	}

	/**
	 *
	 * @returns the minimum of the Y Values
	 */
	public double getMinY(){
		return getMin(yValues);
	}

	/**
	 *
	 * @returns the maximum of the Y values
	 */
	public double getMaxY(){
		return getMax(yValues);
	}

	/**
	 *
	 * @returns the minimum of the Z values
	 */
	public double getMinZ(){
		return getMin(zValues);
	}

	/**
	 *
	 * @returns the maximum of the Z values
	 */
	public double getMaxZ(){
		return getMax(zValues);
	}

	/**
	 *
	 * @returns true if size ArrayList for X,Y and Z dataset values is equal else return false
	 */
	public boolean checkXYZ_NumVals(){
		if((xValues.size() == yValues.size()) && (xValues.size() == zValues.size()))
			return true;
		else
			return false;
	}

	/**
	 * private function of the class that finds the minimum value in the ArrayList
	 * @param xyz
	 * @return
	 */
	private double getMin(ArrayList xyz){
		int size = xyz.size();
		double min = Double.POSITIVE_INFINITY;
		for(int i=1;i<size;++i){
			double val = ((Double)xyz.get(i)).doubleValue();
			if(Double.isNaN(val)) continue;
			if(val < min)
				min = val;
		}
		return min;
	}

	/**
	 * private function of the class that finds the maximum value in the ArrayList
	 * @param xyz
	 * @return
	 */
	private double getMax(ArrayList xyz){
		int size = xyz.size();
		double max = Double.NEGATIVE_INFINITY;
		for(int i=1;i<size;++i){
			double val = ((Double)xyz.get(i)).doubleValue();
			if(Double.isNaN(val)) continue;
			if(val > max)
				max = val;
		}
		return max;
	}
	
	public static void writeXYZFile(XYZ_DataSetAPI xyz, String fileName) throws IOException {
		if (!xyz.checkXYZ_NumVals())
			throw new RuntimeException("Bad XYZ dataset!");
		
		ArrayList<Double> xData = xyz.getX_DataSet();
		ArrayList<Double> yData = xyz.getY_DataSet();
		ArrayList<Double> zData = xyz.getZ_DataSet();
		
		int size = xData.size();
		
		FileWriter fw = new FileWriter(fileName);
		for (int i=0; i<size; i++) {
			fw.write(xData.get(i) + "\t" + yData.get(i) + "\t" + zData.get(i) + "\n");
		}
		fw.close();
	}

}
