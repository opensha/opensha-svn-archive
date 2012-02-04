package scratch.UCERF3.erf.ETAS;

import java.awt.geom.Point2D;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.exceptions.Point2DException;



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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean dataChange = true;
	double[] cumDistVals;
	double sumOfYvals;
	
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
		sumOfYvals=0;
		for(int i=0;i<getNum();i++) {
			sumOfYvals += getY(i);
			cumDistVals[i]=sumOfYvals;
		}
		for(int i=0;i<getNum();i++) cumDistVals[i] /= sumOfYvals;
//		for(int i=0;i<getNum();i++) System.out.println(i+"\t"+cumDistVals[i]);
	}
	
	public double getSumOfY_vals() {
		if(dataChange) {
			updateCumDistVals();
			dataChange=false;
		}
		return sumOfYvals;
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
	public int getInt(double prob) {
		// update if needed
		if(dataChange) {
			updateCumDistVals();
			dataChange=false;
		}
//		System.out.println("prob="+prob);
		
		// this is needed because the first one is never accessed in the algorithm below
		if(prob<cumDistVals[0]) return 0;
		
		// search for the index
		int indexLow=0;
		int indexHigh=getNum();
		long st = System.currentTimeMillis();
		while(indexHigh-indexLow > 1) {
			int testIndex = (int)Math.floor((indexHigh+indexLow)/2);
			if(prob<cumDistVals[testIndex]) {
				indexHigh=testIndex;
			}
			else {
				indexLow=testIndex;
			}
//			if(System.currentTimeMillis()-st > 10000) {	// 100 sec
//				System.out.println("prob="+prob+"\tindexLow="+indexLow+"\tindexHigh="+indexHigh+"\ttestIndex="+testIndex);
//				try{
//					FileWriter fw1 = new FileWriter("tempIntPDF_Data");
//					fw1.write("prob="+prob+"\tindexLow="+indexLow+"\tindexHigh="+indexHigh+"\ttestIndex="+testIndex+"\n");
//					for(int i=0;i<this.getNum();i++) {
//						fw1.write(i+"\t"+(float)getX(i)+"\t"+getY(i)+"\t"+cumDistVals[i]+"\n");
//					}
//					fw1.close();
//				}catch(Exception e) {
//					e.printStackTrace();
//
//				}
//				System.exit(0);
//
//			}
		}
		if(indexHigh == this.getNum()) 
			throw new RuntimeException("Problem: chosen int above x-axis bounds; could Y'axis contain NaNs?");

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
