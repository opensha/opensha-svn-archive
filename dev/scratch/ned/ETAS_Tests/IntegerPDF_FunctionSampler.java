package scratch.ned.ETAS_Tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.exceptions.Point2DException;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;


/**
 * This class stores integer values in the X axis and the relative probability associated with each integer
 * in the Y axis (representing a PDF of integer values). The x-axis ranges from zero to the number of points 
 * minus one (i.e, the x-axis values are the same as the x-axis indices).  Once the relative probabilities have
 * been populated in the y-axis, random samples can be obtained from the distribution using the getRandomInt()
 * method.
 * @author field
 *
 */
public class IntegerPDF_FunctionSampler extends EvenlyDiscretizedFunc {
	
	boolean dataChange = true;
	double[] cumDistVals;
	
	/**
	 * 
	 * @param numInts - the number of integers
	 */
	public IntegerPDF_FunctionSampler(int numInts) {
		super(0.0, numInts, 1.0);
		cumDistVals = new double[numInts];
	}
	
	
	/**
	 * This makes the cumulative dist function
	 */
	private void updateCumDistVals() {
		double sum=0;
		for(int i=0;i<getNum();i++) {
			sum += getY(i);
			cumDistVals[i]=sum;
		}
		for(int i=0;i<getNum();i++) cumDistVals[i] /= sum;
//		for(int i=0;i<getNum();i++) System.out.println(i+"\t"+cumDistVals[i]);
	}
	
	
	/**
	 * This returns a random integer based on the probabilities of each
	 * @return
	 */
	public int getRandomInt() {
		return getInt(Math.random());
	}
	
	
	/**
	 * This returns the integer value corresponding to the given probability (between 0 and 1).
	 * @param prob - a value between 0 and 1.
	 * @return
	 */
	private int getInt(double prob) {
		// update if needed
		if(dataChange) {
			updateCumDistVals();
			dataChange=false;
		}
		
		// this is needed because the first one is never accessed in the algorithm below
		if(prob<cumDistVals[0]) return 0;
		
		// search for the index
		int indexLow=0;
		int indexHigh=getNum();
		while(indexHigh-indexLow > 1) {
			int testIndex = (int)Math.floor((indexHigh+indexLow)/2);
			if(prob<cumDistVals[testIndex])
				indexHigh=testIndex;
			else
				indexLow=testIndex;
		}
		/*
		if(indexHigh == this.getNum()) {
			System.out.println("Error: "+prob+"\n");
			ArrayList funcs = new ArrayList();
			funcs.add(this);
			GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(funcs, "");  
		}
		*/
		return indexHigh;
	}
	
	
	// override the following to record that data has changed
	public void set(Point2D point) throws Point2DException {
		super.set(point);
		dataChange = true;
	}
	public void set(double x, double y) throws Point2DException {
		super.set(x,y);
		dataChange = true;
	}
	public void add(double x, double y) throws Point2DException {
		super.add(x, y);
		dataChange = true;
	}
	public void set(int index, double y) throws Point2DException {
		super.set(index, y);
		dataChange = true;
	}
	public void add(int index, double y) throws Point2DException {
		super.add(index, y);
		dataChange = true;
	}
	public void set(double min, int num, double delta) {
		super.set(min, num, delta);
		dataChange = true;
	}
	public void set(double min, double max, int num) {
		super.set(min,max,num);
		dataChange = true;
	}

	


}
