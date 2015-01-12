package scratch.UCERF3.erf.ETAS;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.apache.commons.math3.random.RandomDataGenerator;

import com.google.common.base.Preconditions;

import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_DistanceDecayParam_q;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_MinDistanceParam_d;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_MinTimeParam_c;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ProductivityParam_k;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_TemporalDecayParam_p;
import scratch.ned.ETAS_Tests.PrimaryAftershock;

/**
 * This class provides various info and calculations related to the ETAS model.
 * @author field
 *
 */
public class ETAS_Utils {
	
	// The following are from Table 2 of Hardebeck (2012)
//	final static double k_DEFAULT = 0.008;			// Felzer (2000) value in units of number of events >= magMain per day
//	final static double p_DEFAULT = 1.34;			// Felzer (2000) value
//	final static double c_DEFAULT = 0.095;			// Felzer (2000) value in units of days
//	final static double k_DEFAULT = 0.00284*Math.pow(365.25,0.07);			// Hardebeck's value converted to units of days (see email to Ned Field on April 1, 2012)
//	final static double p_DEFAULT = 1.07;			// Hardebeck's value
//	final static double c_DEFAULT = 1.78e-5*365.25;	// Hardebeck's value converted to units of days
//	final static double magMin_DEFAULT = 2.5;		// as assumed in Hardebeck
//	final static double distDecay_DEFAULT = 1.96;	// this is "q" in Hardebeck's Table 2
//	final static double minDist_DEFAULT = 2d; //0.79;		// km; this is "d" in Hardebeck's Table 2

	public final static double magMin_DEFAULT = 2.5;
	public final static double k_DEFAULT = ETAS_ProductivityParam_k.DEFAULT_VALUE;
	public final static double p_DEFAULT = ETAS_TemporalDecayParam_p.DEFAULT_VALUE;
	public final static double c_DEFAULT = ETAS_MinTimeParam_c.DEFAULT_VALUE;
	public final static double distDecay_DEFAULT = ETAS_DistanceDecayParam_q.DEFAULT_VALUE;
	public final static double minDist_DEFAULT = ETAS_MinDistanceParam_d.DEFAULT_VALUE;

	private long randomSeed;
	
	RandomDataGenerator randomDataGen;
	
	/**
	 * This sets the seed for random number generation as System.currentTimeMillis()
	 */
	public ETAS_Utils() {
		this(System.currentTimeMillis());
	}
	
	/**
	 * 
	 * @param randomSeed - the seed for random number generation (set for reproducibility)
	 */
	public ETAS_Utils(long randomSeed) {
		randomDataGen = new RandomDataGenerator();
		randomDataGen.reSeed(randomSeed);
		this.randomSeed = randomSeed;
	}
	
	public long getRandomSeed() {
		return randomSeed;
	}

	
	public static final ArrayList<String> getDefaultParametersAsStrings() {
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("k="+k_DEFAULT);
		strings.add("p="+p_DEFAULT);
		strings.add("c="+c_DEFAULT);
		strings.add("magMin="+magMin_DEFAULT);
		return strings;
	}

	
	/**
	 * This computes the density of aftershocks for the given distance according to equations (5) to (8) of
	 * Hardebeck (2013; http://pubs.usgs.gov/of/2013/1165/pdf/ofr2013-1165_appendixS.pdf) assuming max distance
	 * of 1000 km.
	 * @param distance (km)
	 * @return
	 */
	public static double getHardebeckDensity(double distance, double distDecay, double minDist, double seismoThickness) {
		double maxDist = 1000d;
//		double seismoThickness = 24d;
		if(distance>maxDist) {
			return 0d;
		}
		double oneMinusDecay = 1-distDecay;
		double cs = oneMinusDecay/(Math.pow(maxDist+minDist,oneMinusDecay)-Math.pow(minDist,oneMinusDecay));
		if(distance < seismoThickness/2d) {
			return cs*Math.pow(distance+minDist, -distDecay)/(4*Math.PI*distance*distance);
		}
		else {
			return cs*Math.pow(distance+minDist, -distDecay)/(2*Math.PI*distance*seismoThickness);
		}
	}
	
	
	public static void testHardebeckDensity() {
		double histLogMinDistKm=-2,histLogMaxDistKm=3;
		int histNum=31;
		
		EvenlyDiscretizedFunc targetLogDistDecay = ETAS_Utils.getTargetDistDecayFunc(histLogMinDistKm, histLogMaxDistKm, histNum, distDecay_DEFAULT, minDist_DEFAULT);
		EvenlyDiscretizedFunc testLogHistogram = new EvenlyDiscretizedFunc(histLogMinDistKm,histLogMaxDistKm,histNum);
		EvenlyDiscretizedFunc numLogHistogram = new EvenlyDiscretizedFunc(histLogMinDistKm,histLogMaxDistKm,histNum);
		testLogHistogram.setTolerance(testLogHistogram.getDelta());
		numLogHistogram.setTolerance(numLogHistogram.getDelta());

		double totWt=0, totVol=0;
		double[] minXY = {0d, 1d, 4d};
		double[] maxXY = {1d, 4d, 1000d};
		double[] minZ = {0d, 1d, 4d};
		double[] maxZ = {1d, 4d, 6d};
		double[] discr = {0.005, 0.01, 0.5};
		int totNumX = 0;
		for(int i=0;i<minXY.length;i++)
			totNumX += (maxXY[i]-minXY[i])/discr[i];
		CalcProgressBar progressBar = new CalcProgressBar("testDefaultHardebeckDensity()", "junk");
		progressBar.showProgress(true);
		for(int i=0;i<minXY.length;i++) {
			System.out.println(i);
			int numXY = (int)Math.round((maxXY[i]-minXY[i])/discr[i]);
			int numZ =  (int)Math.round((maxZ[i]-minZ[i])/discr[i]);
			for(int x=0; x<numXY;x++) {
				progressBar.updateProgress(x, totNumX);
				for(int y=0; y<numXY;y++) {
					for(int z=0; z<numZ;z++) {
						double xDist=minXY[i]+x*discr[i]+discr[i]/2;
						double yDist=minXY[i]+y*discr[i]+discr[i]/2;
						double zDist=minZ[i]+z*discr[i]+discr[i]/2;
						double dist = Math.pow(xDist*xDist+yDist*yDist+zDist*zDist,0.5);
						double vol = discr[i]*discr[i]*discr[i];
						double wt = 8d*getHardebeckDensity(dist, ETAS_Utils.distDecay_DEFAULT, ETAS_Utils.minDist_DEFAULT, 24d)*vol;	// 8 is for the other area of space
						totWt += wt;
						if(dist<=1000)
							totVol += vol;
						double logDist = Math.log10(dist);
						if(logDist<testLogHistogram.getX(0)){
							testLogHistogram.add(0, wt);
							numLogHistogram.add(0, 1d);
						}
						else if (logDist<histLogMaxDistKm) {
							testLogHistogram.add(logDist,wt);
							numLogHistogram.add(logDist, 1d);
						}


//						if(dist<20)
//							System.out.println((float)xDist+"\t"+(float)yDist+"\t"+(float)zDist);
					}
				}
			}			
		}
		progressBar.showProgress(false);
		System.out.println("totWt="+totWt);
		double expVol = Math.PI*1000d*1000d*12d;
		System.out.println("totVol="+totVol*8+"\texpVol="+expVol);
		
		ArrayList funcs1 = new ArrayList();
		funcs1.add(testLogHistogram);
		funcs1.add(targetLogDistDecay);

		GraphWindow graph = new GraphWindow(funcs1, "testLogHistogram"); 
		graph.setAxisRange(-2, 3, 1e-6, 1);
		graph.setYLog(true);

		EvenlyDiscretizedFunc testLogFunction = new EvenlyDiscretizedFunc(histLogMinDistKm,histLogMaxDistKm,histNum);
		for(int i=0;i<testLogFunction.getNum();i++) {
			double dist = Math.pow(10d, testLogFunction.getX(i));
			testLogFunction.set(i,getHardebeckDensity(dist, ETAS_Utils.distDecay_DEFAULT, ETAS_Utils.minDist_DEFAULT, 24d));
		}
		GraphWindow graph2 = new GraphWindow(testLogFunction, "testLogFunction"); 
		GraphWindow graph3 = new GraphWindow(numLogHistogram, "numLogHistogram"); 

		
	}

	/**
	 * This computes the fraction of events inside a distance from the hypocenter analytically
	 * @param distDecay
	 * @param minDist
	 * @param distance
	 * @return
	 */
	public static double getDecayFractionInsideDistance(double distDecay, double minDist, double distance) {
		double oneMinus = 1-distDecay;
		return -(Math.pow(distance+minDist,oneMinus) - Math.pow(minDist,oneMinus))/Math.pow(minDist,oneMinus);
	}
	
	
	/**
	 * This returns Math.pow(dist+minDist, -distDecay)
	 * @param dist - distance in km
	 * @param minDist - minimum distance in km
	 * @param distDecay	- positive value (negative sign is added within this method)
	 * @return
	 */
	public static double getDistDecayValue(double dist, double minDist, double distDecay) {
		return Math.pow(dist+minDist, -distDecay);
	}
	
	/**
	 * This returns a distance decay function, where x-axis is log10-distance values, and the averaging over
	 * each x-axis bin is done accurately.  The values don't sum to one unless a very large distance is specified.
	 * @param minLogDist - minimum  x-axis log10-distance (km) value
	 * @param maxLogDist - maximum x-axis log10-distance (km) value
	 * @param num - number of points
	 * @param minDist - minimum distance in km
	 * @param distDecay	- positive value (negative sign is added within this method)
	 * @return
	 */
	public static EvenlyDiscretizedFunc getTargetDistDecayFunc(double minLogDist, double maxLogDist, int num, double distDecay, double minDist) {
		// make target distances decay histogram (this is what we will match_
		EvenlyDiscretizedFunc logTargetDecay = new EvenlyDiscretizedFunc(minLogDist,maxLogDist,num);
		logTargetDecay.setTolerance(logTargetDecay.getDelta());
		double logBinHalfWidth = logTargetDecay.getDelta()/2;
		double upperBinEdge = Math.pow(10,logTargetDecay.getX(0)+logBinHalfWidth);
		double lowerBinEdge;
		double binWt = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, upperBinEdge);	// everything within the upper edge of first bin
		logTargetDecay.set(0,binWt);
		for(int i=1;i<logTargetDecay.getNum();i++) {
			double logLowerEdge = logTargetDecay.getX(i)-logBinHalfWidth;
			lowerBinEdge = Math.pow(10,logLowerEdge);
			double logUpperEdge = logTargetDecay.getX(i)+logBinHalfWidth;
			upperBinEdge = Math.pow(10,logUpperEdge);
			double wtLower = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, lowerBinEdge);
			double wtUpper = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, upperBinEdge);
			binWt = wtUpper-wtLower;
			logTargetDecay.set(i,binWt);
		}
		return logTargetDecay; 
	}




	/**
	 * This returns a distance decay function, where x-axis is log10-distance values, and the Y axis is the
	 * fraction of events expected within each distance.
	 * @param minLogDist - minimum  x-axis log10-distance (km) value
	 * @param maxLogDist - maximum x-axis log10-distance (km) value
	 * @param num - number of points
	 * @param minDist - minimum distance in km
	 * @param distDecay	- positive value (negative sign is added within this method)
	 * @return
	 */
	public static EvenlyDiscretizedFunc getDecayFractionInsideDistFunc(double minLogDist, double maxLogDist, int num, double distDecay, double minDist) {
		EvenlyDiscretizedFunc logTargetDecay = new EvenlyDiscretizedFunc(minLogDist,maxLogDist,num);
		for(int i=0;i<logTargetDecay.getNum();i++) {
			logTargetDecay.set(i,getDecayFractionInsideDistance(distDecay, minDist, Math.pow(10d,logTargetDecay.getX(i))));
		}
		logTargetDecay.setName("logTargetDecay");
		return logTargetDecay; 
	}


	
	/**
	 * This returns the expected number of primary aftershocks between time tMin and tMax for an ETAS sequence
	 * from an integration of Equation (1) in Felzer (2009, SRL, v 80, p 21-25, doi: 10.1785/gssrl.80.1.21)
	 * @param k
	 * @param p - must be > 1.0 (not checked)
	 * @param magMain - main shock magnitude
	 * @param magMin - minimum magnitude
	 * @param c - days
	 * @param tMin - beginning of forecast time window (since origin time), in days
	 * @param tMax - end of forecast time window (since origin time), in days
	 * @return
	 */
	public static double getExpectedNumEvents(double k, double p, double magMain, double magMin, double c, double tMinDays, double tMaxDays) {
		double oneMinusP= 1-p;
		double lambda = k*Math.pow(10,magMain-magMin)/oneMinusP;
		lambda *= Math.pow(c+tMaxDays,oneMinusP) - Math.pow(c+tMinDays,oneMinusP);
		return lambda;
	}
	
	/**
	 * This applies the default ETAS parameter values for CA from Hardebeck et al. 
	 * (2008, JGR, v 113, B08310, doi:10.1029/2007JB005410): 
	 * k=0.008, p=1.34, c=0.095, and magMin=2.5
	 * @param magMain
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static double getDefaultExpectedNumEvents(double magMain, double tMinDays, double tMaxDays) {
		return getExpectedNumEvents(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, tMinDays, tMaxDays);
	}
	
	
	/**
	 * Knuth's algorithm for generating random Poisson distributed numbers
	 * @param lambda - the expected number of events
	 * @return
	 */
	public int getPoissonRandomNumber(double lambda) {
		return (int) randomDataGen.nextPoisson(lambda);
		/*
	    double L = Math.exp(-lambda);
	    int k = 0;
	    double p = 1;
	    do {
	        k = k + 1;
	        double u = Math.random();
	        p = p * u;
	    } while (p > L);
	    return k - 1;
	    */
	}
	
	
	/**
	 * This returns a random double that is uniformly distributed between 0 (inclusive)
	 * and 1 (exclusive).
	 * @return
	 */
	public double getRandomDouble() {
		return randomDataGen.nextUniform(0.0, 1.0, true);
	}
	
	
	/**
	 * This returns a random int uniformly distributed between zero the given value (both inclusive).
	 * @return
	 */
	public int getRandomInt(int maxInt) {
		if(maxInt==0)
			return 0;
		else
			return randomDataGen.nextInt(0, maxInt);
	}
	
	
	/**
	 * This gives a random event time for an ETAS sequence.  This algorithm was provided by 
	 * Karen Felzer (from her Matlab code).
	 * @param c
	 * @param tMin
	 * @param tMax
	 * @return
	 */
	public double getRandomTimeOfEvent(double c, double p, double tMin, double tMax) {
//		double r= Math.random();
		double r = getRandomDouble();
		double t;
		if(p != 1.0) {
		    double a1 = Math.pow(tMax + c,1-p);
		    double a2 = Math.pow(tMin + c,1-p);
		    double a3 = r*a1 + (1-r)*a2;
		    t = Math.pow(a3,1/(1-p)) - c;
		}
		else {
			double a1 = Math.log(tMax+c);
			double a2 = Math.log(tMin + c);
			double a3 = r*a1 + (1-r)*a2;
			t = Math.exp(a3) - c;
		}
		return t;
	}
	
	/**
	 * This gives a random event time for an ETAS sequence using the 
	 * default ETAS parameter values for CA from Hardebeck et al. 
	 * (2008, JGR, v 113, B08310, doi:10.1029/2007JB005410): 
	 * p=1.34, c=0.095.  
	 * @param tMin
	 * @param tMax
	 * @return
	 */
	public double getDefaultRandomTimeOfEvent(double tMin, double tMax) {
		return getRandomTimeOfEvent(c_DEFAULT, p_DEFAULT, tMin, tMax);
	}
	
	/**
	 * This returns a random set of primary aftershock event times for the given parameters
	 * @param magMain - main shock magnitude
	 * @param tMinDays
	 * @param tMaxDays
	 * @return - event times in days since the main shock
	 */
	public double[] getDefaultRandomEventTimes(double magMain, double tMinDays, double tMaxDays) {
		return getRandomEventTimes(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, tMinDays, tMaxDays);
	}
	
	
	/**
	 * This gives a random set of primary aftershock event times for the given parameters
	 * @param k
	 * @param p
	 * @param magMain
	 * @param magMin
	 * @param c
	 * @param tMinDays
	 * @param tMaxDays
	 * @return - event times in days since the main shock
	 */
	public double[] getRandomEventTimes(double k, double p, double magMain, double magMin, double c, double tMinDays, double tMaxDays) {
		int numAft = getPoissonRandomNumber(getExpectedNumEvents(k, p, magMain, magMin, c, tMinDays, tMaxDays));
		double[] eventTimes = new double[numAft];
		for(int i=0;i<numAft;i++)
			eventTimes[i] = this.getRandomTimeOfEvent(c, p, tMinDays, tMaxDays);
		return eventTimes;
	}

	
	
	/**
	 * This returns the expected number of primary aftershocks as a function of time using
	 * Equation (1) of Felzer (2009, SRL, v 80, p 21-25, doi: 10.1785/gssrl.80.1.21)
	 * @param k
	 * @param p
	 * @param magMain
	 * @param magMin
	 * @param c - days
	 * @param tMin - days
	 * @param tMax - days
	 * @param tDelta - days
	 * @return
	 */
	public static  EvenlyDiscretizedFunc getNumWithTimeFunc(double k, double p, double magMain, double magMin, double c, double tMin, double tMax, double tDelta) {
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(tMin+tDelta/2, tMax-tDelta/2, (int)Math.round((tMax-tMin)/tDelta));
		for(int i=0;i<func.getNum();i++) {
			double binTmin = func.getX(i) - tDelta/2;
			double binTmax = func.getX(i) + tDelta/2;
			double yVal = getExpectedNumEvents(k, p, magMain, magMin, c, binTmin, binTmax);
	//		double yVal = k*Math.pow(10,magMain-magMin)*Math.pow(c+func.getX(i), -p);
			func.set(i,yVal);
		}
		func.setName("Expected Number of Primary Aftershocks for "+tDelta+"-day intervals");
		func.setInfo("for k="+k+", p="+p+", c="+c+", magMain="+magMain+", magMin="+magMin);
		return func;
	}
	
	

	/**
	 * This returns the expected number of primary aftershocks as a function of 
	 * time using the default ETAS parameter values for CA from Hardebeck et al. 
	 * (2008, JGR, v 113, B08310, doi:10.1029/2007JB005410):
	 * @param magMain
	 * @param tMin - days
	 * @param tMax - days
	 * @param tDelta - days
	 * @return
	 */
	public static EvenlyDiscretizedFunc getDefaultNumWithTimeFunc(double magMain, double tMin, double tMax, double tDelta) {
		return getNumWithTimeFunc(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, tMin, tMax, tDelta);
	}
	
	
	
	public static HistogramFunction getNumWithLogTimeFunc(double k, double p, double magMain, double magMin, double c, double log_tMin, 
			double log_tMax, double log_tDelta) {
		HistogramFunction func = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		for(int i=0;i<func.getNum();i++) {
			double binTmin = Math.pow(10, func.getX(i) - log_tDelta/2);
			double binTmax = Math.pow(10, func.getX(i) + log_tDelta/2);
			double yVal = getExpectedNumEvents(k, p, magMain, magMin, c, binTmin, binTmax);
	//		double yVal = k*Math.pow(10,magMain-magMin)*Math.pow(c+func.getX(i), -p);
			func.set(i,yVal);
		}
		func.setName("Expected Number of Primary Aftershocks for log-day intervals of "+log_tDelta);
		func.setInfo("for k="+k+", p="+p+", c="+c+", magMain="+magMain+", magMin="+magMin);
		return func;
	}

	
	public static EvenlyDiscretizedFunc getDefaultNumWithLogTimeFunc(double magMain, double log_tMin, double log_tMax, double log_tDelta) {
		return getNumWithLogTimeFunc(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, log_tMin, log_tMax, log_tDelta);
	}

	
	public static void main(String[] args) {
		
		ETAS_Utils etas_utils = new ETAS_Utils(100);
		etas_utils.getRandomInt(0);
		
		for(int i=0;i<10;i++)
			System.out.println(i+"\t"+etas_utils.getRandomDouble());
		System.exit(0);

		
//		testDefaultHardebeckDensity();

		
//		EvenlyDiscretizedFunc distDecay = getTargetDistDecayFunc(-2, 3, 51, distDecay_DEFAULT, minDist_DEFAULT);
//		double sum=0;
//		for(int i=0;i<distDecay.getNum();i++) {
//			sum += distDecay.getY(i);
//			System.out.println((float)distDecay.getX(i)+"\t"+(float)distDecay.getY(i)+"\t"+(float)(1d-sum));
//		}
		
//		System.out.println("k_DEFAULT: "+ETAS_Utils.k_DEFAULT);
//		System.out.println("c_DEFAULT: "+ETAS_Utils.c_DEFAULT);
//		System.out.println("p_DEFAULT: "+ETAS_Utils.p_DEFAULT);
//		System.out.println("M7: "+getDefaultExpectedNumEvents(7.0, 0, 360));
//		System.out.println("M6: "+getDefaultExpectedNumEvents(6.0, 0, 360));
//		
		
		double distDecayArray[] = {ETAS_Utils.distDecay_DEFAULT, ETAS_Utils.distDecay_DEFAULT, 1.4};
		double minDistArray[] = {ETAS_Utils.minDist_DEFAULT, 2.0, ETAS_Utils.minDist_DEFAULT};
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		for(int j=0;j<distDecayArray.length;j++) {
			EvenlyDiscretizedFunc cumDecayFunc = new EvenlyDiscretizedFunc(-3d,25,0.25);
			for(int i=0;i<cumDecayFunc.getNum();i++) {
				double dist = Math.pow(10d, cumDecayFunc.getX(i));
				cumDecayFunc.set(i,getDecayFractionInsideDistance(distDecayArray[j], minDistArray[j], dist));
			}
			cumDecayFunc.setName("distDecay="+distDecayArray[j]+";\tminDist="+minDistArray[j]);
			cumDecayFunc.setInfo(" ");
			funcs.add(cumDecayFunc);
		}
		GraphWindow graph = new GraphWindow(funcs, "Probability of Aftershock Within Distance");
		graph.setX_AxisLabel("log10 Distance");
		graph.setX_AxisLabel("Cum Density");

		
		
//		GraphWindow graph = new GraphWindow(getDefaultNumWithTimeFunc(7.0, 0.5, 365d, 1), "Num aftershocks vs time");
//		GraphWindow graph2 = new GraphWindow(getDefaultNumWithLogTimeFunc(7.0, 0, 2.56, 0.0256), "Num aftershocks vs time");
//		System.out.println("Double.MAX_VALUE="+Double.MAX_VALUE+"\t"+Double.MAX_VALUE/(1000*60*60*24*265.25));
//		System.out.println("Long.MAX_VALUE="+Long.MAX_VALUE+"\t"+Long.MAX_VALUE/(1000*60*60*24*265));
		
	}

	private static SimpleDateFormat cat_df = new SimpleDateFormat("yyyy MM dd HH mm ss");
	
	public static void writeEQCatFile(File file, List<PrimaryAftershock> aftershocks) throws IOException {
		Date orig = new Date();
		GregorianCalendar cal = new GregorianCalendar();
		
		ArrayList<Date> dates = new ArrayList<Date>();
		ArrayList<String> lines = new ArrayList<String>();
		
		for (PrimaryAftershock eq : aftershocks) {
			cal.setTime(orig);
			cal.add(Calendar.SECOND, (int)(60d*eq.getOriginTime()+0.5));
			Date myDate = cal.getTime();
			
			int insertionPoint;
			for (insertionPoint=0; insertionPoint<dates.size(); insertionPoint++) {
				if (myDate.after(dates.get(insertionPoint)))
					break;
			}
			
			Location loc = eq.getHypocenterLocation();
			
			// id date/time lon lat depth mag
			String line = eq.getID()+" "+cat_df.format(myDate)+" "
			+loc.getLongitude()+" "+loc.getLatitude()+" "+loc.getDepth()+" "+eq.getMag();
			
			dates.add(insertionPoint, myDate);
			lines.add(insertionPoint, line);
		}
		
		Collections.reverse(lines);
		
		FileWriter fw = new FileWriter(file);
		
		for (String line : lines)
			fw.write(line+"\n");
		
		fw.close();
	}
	
	/**
	 * This returns a random location from the given surface (uniformly distributed)
	 * @param rupSurf
	 * @return
	 */
	public Location getRandomLocationOnRupSurface(RuptureSurface rupSurf) {
		if(rupSurf.isPointSurface()) {
			return rupSurf.getFirstLocOnUpperEdge();
		}
		else {
			LocationList locList = rupSurf.getEvenlyDiscritizedListOfLocsOnSurface();
			return locList.get(getRandomInt(locList.size()-1));
		}
	}
	
	
	/**
	 * This returns the amount by which the supra-seismogenic MFD has to be scaled in order for the total MFD (sub+supra) to
	 * have the same expected number of primary aftershocks as a perfect GR (extrapolated from the sub MFD to the max-mag
	 * of the supra MFD).
	 * 
	 * @param supraSeisMFD
	 * @param subSeisMFD
	 * @return
	 */
	public static double getScalingFactorToImposeGR(IncrementalMagFreqDist supraSeisMFD, IncrementalMagFreqDist subSeisMFD) {
		if (supraSeisMFD.getMaxY() == 0d)
			// fix for empty cells, weird solutions (such as UCERF2 mapped) with zero rate faults
			return 1d;
		
		double minMag = subSeisMFD.getMinX();
		double maxMagWithNonZeroRate = supraSeisMFD.getMaxMagWithNonZeroRate();
		int numMag = (int)Math.round((maxMagWithNonZeroRate-minMag)/supraSeisMFD.getDelta()) + 1;
		Preconditions.checkState(numMag > 1 || minMag == maxMagWithNonZeroRate,
				"only have 1 bin but min != max: "+minMag+" != "+maxMagWithNonZeroRate+"\n"+supraSeisMFD);
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(1.0, 1.0, minMag, maxMagWithNonZeroRate, numMag);
		gr.scaleToIncrRate(5.05, subSeisMFD.getY(5.05));
		
		// Since b=1 (and a=1, as implicit in Felzer, and explicit in equation (3) of http://pubs.usgs.gov/of/2013/1165/pdf/ofr2013-1165_appendixS.pdf),
		// each magnitude has an equal number of expected
		
		double expNumGR = 0;
		for(int i=0;i<gr.getNum();i++) {
			double mag = gr.getX(i);
			expNumGR += gr.getY(i)*Math.pow(10, mag);
		}
		
		double expNumSubSeis = 0;
		for(int i=0;i<subSeisMFD.getNum();i++) {
			double mag = subSeisMFD.getX(i);
			expNumSubSeis += subSeisMFD.getY(i)*Math.pow(10, mag);
		}

		double expNumSupraSeis = 0;
		for(int i=0;i<supraSeisMFD.getNum();i++) {
			double mag = supraSeisMFD.getX(i);
			expNumSupraSeis += supraSeisMFD.getY(i)*Math.pow(10, mag);
		}
		double result = (expNumGR-expNumSubSeis)/expNumSupraSeis;
	
//		ArrayList<IncrementalMagFreqDist> funcs = new ArrayList<IncrementalMagFreqDist>();
//		funcs.add(supraSeisMFD);
//		funcs.add(subSeisMFD);
//		funcs.add(gr);
//		GraphWindow graph = new GraphWindow(funcs, "getScalingFactorToImposeGR "+result);
//		graph.setX_AxisLabel("Mag");
//		graph.setY_AxisLabel("Incr Rate");
//
//		System.out.println("result="+(expNumGR-expNumSubSeis)/expNumSupraSeis);
		
		return result;
	}
}
