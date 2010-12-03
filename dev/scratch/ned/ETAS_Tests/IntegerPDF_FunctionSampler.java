package scratch.ned.ETAS_Tests;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.exceptions.Point2DException;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

public class IntegerPDF_FunctionSampler extends EvenlyDiscretizedFunc {
	
	boolean dataChane = true;
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
	 * This returns the integer value corresponding to the given probability (between 0 and 1)
	 * @param prob - a value between 0 and 1.
	 * @return
	 */
	private int getInt(double prob) {
		// update if needed
		if(dataChane) {
			updateCumDistVals();
			dataChane=false;
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
		return indexHigh;
	}
	
	
	// override the following to record that data has changed
	public void set(Point2D point) throws Point2DException {
		super.set(point);
		dataChane = true;
	}
	public void set(double x, double y) throws Point2DException {
		super.set(x,y);
		dataChane = true;
	}
	public void add(double x, double y) throws Point2DException {
		super.add(x, y);
		dataChane = true;
	}
	public void set(int index, double y) throws Point2DException {
		super.set(index, y);
		dataChane = true;
	}
	public void add(int index, double y) throws Point2DException {
		super.add(index, y);
		dataChane = true;
	}
	public void set(double min, int num, double delta) {
		super.set(min, num, delta);
		dataChane = true;
	}
	public void set(double min, double max, int num) {
		super.set(min,max,num);
		dataChane = true;
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int numIntegers = 100;
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(numIntegers);
		double total=0;
		for(int i=0;i<numIntegers;i++) {
			sampler.set(i,Math.random());
			total+=sampler.getY(i);
		}
		for(int i=0;i<numIntegers;i++) {
			sampler.set(i,sampler.getY(i)/total);
		}

		/*
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(5);
		sampler.set(0,1);
		sampler.set(1,1);
		sampler.set(2,1);
		sampler.set(3,1);
		sampler.set(4,1);
		
		System.out.println(sampler.getInt(0.8));
		*/
		
		/**/
		EvenlyDiscretizedFunc testFunc = new EvenlyDiscretizedFunc(0.0, numIntegers, 1.0);
		int numSamples=100000000;
		for(int i=0;i<numSamples;i++) {
			testFunc.add(sampler.getRandomInt(),1.0);
		}
		for(int i=0;i<testFunc.getNum();i++) testFunc.set(i,testFunc.getY(i)/numSamples);
//		System.out.println(testFunc);
		
		
		ArrayList funcs = new ArrayList();
		funcs.add(sampler);
		funcs.add(testFunc);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(funcs, "");  
		 
	}

}
