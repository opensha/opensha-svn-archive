package scratch.UCERF3.erf.ETAS;


import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JOptionPane;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.Parameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractNthRupERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupOrigTimeComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.data.U3_EqkCatalogStatewideCompleteness;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_DistanceDecayParam_q;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_MinDistanceParam_d;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_MinTimeParam_c;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ParameterList;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_ProductivityParam_k;
import scratch.UCERF3.erf.ETAS.ETAS_Params.ETAS_TemporalDecayParam_p;
import scratch.UCERF3.erf.ETAS.ETAS_Params.U3ETAS_ProbabilityModelOptions;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools.EpicenterMapThread;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
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
		for(int i=0;i<testLogFunction.size();i++) {
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
		for(int i=1;i<logTargetDecay.size();i++) {
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
	 * This returns a distance decay density function, where x-axis is log10-distance values, and the averaging over
	 * each x-axis bin is done accurately.  "Density" means the bin values are divided by the bin width in linear space.
	 * The first bin goes all the way to zero distance, which does not cause a spike because of the density normalization.
	 * @param minLogDist - minimum  x-axis log10-distance (km) value
	 * @param maxLogDist - maximum x-axis log10-distance (km) value
	 * @param num - number of points
	 * @param minDist - minimum distance in km
	 * @param distDecay	- positive value (negative sign is added within this method)
	 * @return
	 */
	public static EvenlyDiscretizedFunc getTargetDistDecayDensityFunc(double minLogDist, double maxLogDist, int num, double distDecay, double minDist) {
		// make target distances decay histogram (this is what we will match_
		EvenlyDiscretizedFunc logTargetDecay = new EvenlyDiscretizedFunc(minLogDist,maxLogDist,num);
		logTargetDecay.setTolerance(logTargetDecay.getDelta());
		double logBinHalfWidth = logTargetDecay.getDelta()/2;
		for(int i=0;i<logTargetDecay.size();i++) {
			double logLowerEdge = logTargetDecay.getX(i)-logBinHalfWidth;
			double lowerBinEdge=0;	// go all the way to zero for the first bin
			if(i != 0)
				lowerBinEdge = Math.pow(10,logLowerEdge);
			double logUpperEdge = logTargetDecay.getX(i)+logBinHalfWidth;
			double upperBinEdge = Math.pow(10,logUpperEdge);
			double wtLower=0;
			if(i != 0)
				wtLower = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, lowerBinEdge);
			double wtUpper = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, upperBinEdge);
			double binWt = wtUpper-wtLower;
			logTargetDecay.set(i,binWt/(upperBinEdge-lowerBinEdge));
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
		for(int i=0;i<logTargetDecay.size();i++) {
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
		for(int i=0;i<func.size();i++) {
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
		for(int i=0;i<func.size();i++) {
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

	
	public static HistogramFunction getRateWithLogTimeFunc(double k, double p, double magMain, double magMin, double c, double log_tMin, 
			double log_tMax, double log_tDelta) {
		HistogramFunction func = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		for(int i=0;i<func.size();i++) {
			double binTmin = Math.pow(10, func.getX(i) - log_tDelta/2);
			double binTmax = Math.pow(10, func.getX(i) + log_tDelta/2);
			double expNum = getExpectedNumEvents(k, p, magMain, magMin, c, binTmin, binTmax);
			func.set(i,expNum/(binTmax-binTmin));
		}
		func.setName("Expected Rate of Primary Aftershocks for log-day intervals of "+log_tDelta);
		func.setInfo("for k="+k+", p="+p+", c="+c+", magMain="+magMain+", magMin="+magMin);
		return func;
	}

	
	public static EvenlyDiscretizedFunc getDefaultNumWithLogTimeFunc(double magMain, double log_tMin, double log_tMax, double log_tDelta) {
		return getNumWithLogTimeFunc(k_DEFAULT, p_DEFAULT, magMain, magMin_DEFAULT, c_DEFAULT, log_tMin, log_tMax, log_tDelta);
	}
	
	
	
	public double[] JUNK_getRandomNumberForEachGeneration(double mainMag, IncrementalMagFreqDist mfd, double k, double p, double magMin, double c, double numDays, int numGen) {
		double[] numForEachGeneration = new double[numGen+1];	// ignore 0th element; 1st is for 1st gen, etc.
		double[] mfdValsArray = new double[mfd.size()];
		double[] mfdMagsArray = new double[mfd.size()];
		for(int i=0;i<mfd.size();i++) {
			mfdValsArray[i] = mfd.getY(i);
			mfdMagsArray[i] = mfd.getX(i);
		}
		IntegerPDF_FunctionSampler randMFD_Sampler = new IntegerPDF_FunctionSampler(mfdValsArray);
		double[] primaryEventTimesArray = getRandomEventTimes(k, p, mainMag, magMin, c, 0.0, numDays);
		List<Double> timesForCurrentGenList = Doubles.asList(primaryEventTimesArray);
		numForEachGeneration[1] = timesForCurrentGenList.size();
		int currentGen = 2;
		while(currentGen<=15) {
			List<Double>  timesForNextGeneration = new ArrayList<Double>();
			for(double eventTime:timesForCurrentGenList) {
				double mag = mfdMagsArray[randMFD_Sampler.getRandomInt()];
				double numDaysLeft = numDays-eventTime;
				double[] eventTimesArray = getRandomEventTimes(k, p, mag, magMin, c, 0.0, numDaysLeft);
				for(int j=0;j<eventTimesArray.length;j++)
					eventTimesArray[j] += eventTime;	// convert to time relative to main shock
				numForEachGeneration[currentGen] += eventTimesArray.length;
				timesForNextGeneration.addAll(Doubles.asList(eventTimesArray));
			}
			currentGen+=1;
			timesForCurrentGenList = timesForNextGeneration;
		}
		return numForEachGeneration;
	}
	
	
	public String listExpNumForEachGenerationAlt(double mainMag, IncrementalMagFreqDist mfd, double k, double p, double magMin, double c, double numDays, int numGen, int numRandomSamples) {
		
		boolean debug = true;
		
		double[] mfdValsArray = new double[mfd.size()];
		double[] mfdMagsArray = new double[mfd.size()];
		for(int j=0;j<mfd.size();j++) {
			mfdValsArray[j] = mfd.getY(j);
			mfdMagsArray[j] = mfd.getX(j);
		}
		IntegerPDF_FunctionSampler randMFD_Sampler = new IntegerPDF_FunctionSampler(mfdValsArray);
		
		double[] aveNumForGen = new double[numGen+1];	// 0th element ignored
		double totNum=0;
		
		double numPrimaryAboveMainMag = 0;
		double numTotalAboveMainMag = 0;
		
		double magMinusOne = mainMag-1.0;
		boolean minMagOK = (magMinusOne>=mfd.getMinX());
		double numPrimaryAtMainMagMinusOne=0;
		double numTotalAtMainMagMinusOne=0;
		if(!minMagOK) {
			numPrimaryAtMainMagMinusOne = Double.NaN;
			numTotalAtMainMagMinusOne = Double.NaN;
		}		
		
//		SummedMagFreqDist summedDist = new SummedMagFreqDist(mfd.getMinX(),mfd.getMaxMagWithNonZeroRate(),mfd.size());
		
		CalcProgressBar progressBar = new CalcProgressBar(mainMag+"; "+numDays,"junk");
		progressBar.showProgress(true);


		for(int i=0;i<numRandomSamples;i++) {
			progressBar.updateProgress(i, numRandomSamples);
			double[] numForEachGeneration = new double[numGen+1];	// ignore 0th element; 1st is for 1st gen, etc.
			double[] primaryEventTimesArray = getRandomEventTimes(k, p, mainMag, magMin, c, 0.0, numDays);
			List<Double> timesForCurrentGenList = Doubles.asList(primaryEventTimesArray);
			numForEachGeneration[1] = timesForCurrentGenList.size();
			int currentGen = 2;
			while(currentGen<=15) {
				List<Double>  timesForNextGeneration = new ArrayList<Double>();
				for(double eventTime:timesForCurrentGenList) {
					double mag = mfdMagsArray[randMFD_Sampler.getRandomInt()];
//					summedDist.add(mag, 1.0);
					if(currentGen==2 && mag>=mainMag)
						numPrimaryAboveMainMag+=1.0/(double)numRandomSamples;
					if(mag>=mainMag)
						numTotalAboveMainMag+=1.0/(double)numRandomSamples;
					if(minMagOK && currentGen==2 && mag>=magMinusOne)
						numPrimaryAtMainMagMinusOne+=1.0/(double)numRandomSamples;
					if(minMagOK && mag>=magMinusOne)
						numTotalAtMainMagMinusOne+=1.0/(double)numRandomSamples;
					double numDaysLeft = numDays-eventTime;
					if(numDaysLeft<0)
						throw new RuntimeException("Problem");
					double[] eventTimesArray = getRandomEventTimes(k, p, mag, magMin, c, 0.0, numDaysLeft);
					for(int j=0;j<eventTimesArray.length;j++) {
						eventTimesArray[j] += eventTime;	// convert to time relative to main shock
					}
					numForEachGeneration[currentGen] += eventTimesArray.length;
					timesForNextGeneration.addAll(Doubles.asList(eventTimesArray));
				}
				currentGen+=1;
				timesForCurrentGenList = timesForNextGeneration;
			}

			
			for(int j=1;j<numForEachGeneration.length;j++) {
				aveNumForGen[j] += numForEachGeneration[j]/(double)numRandomSamples;
				totNum += numForEachGeneration[j]/(double)numRandomSamples;
			}
			
		}
		
		progressBar.showProgress(false);
		
		System.out.println("numPrimaryAboveMainMag="+numPrimaryAboveMainMag);
		
		String resultString = Double.toString(mainMag);
		
//		summedDist.scale(1.0/(double)numRandomSamples);
		
//		ArrayList<IncrementalMagFreqDist> funcs = new ArrayList<IncrementalMagFreqDist>();
//		funcs.add(mfd);
//		funcs.add(summedDist);
//		GraphWindow graph = new GraphWindow(funcs, "Sampled MFD for M="+mainMag); 
//		graph.setY_AxisLabel("Num Sampled");
//		graph.setX_AxisLabel("Mag");
//		graph.setYLog(true);


		for(int i=1; i<=numGen;i++) {
			resultString+="\t"+Float.toString((float)aveNumForGen[i]);
			if(debug) System.out.println("Gen "+(i+1)+"\t"+(float)aveNumForGen[i]+"\t"+(float)aveNumForGen[i]/totNum);
		}

		if(debug) System.out.println("Total: "+(float)totNum);

		resultString += "\t"+Float.toString((float)numPrimaryAboveMainMag)+"\t"+Float.toString((float)numTotalAboveMainMag);
		resultString += "\t"+Float.toString((float)numPrimaryAtMainMagMinusOne)+"\t"+Float.toString((float)numTotalAtMainMagMinusOne)+"\n";
				
		return resultString;


	}
	
	/**
	 * This lists the relative number of events in each generation expected from the given MFD
	 * (relative to the number of primary events)
	 * @param mainMag
	 * @param mfd
	 * @param k
	 * @param p
	 * @param magMin
	 * @param c
	 * @param numDays - length of timespan following main shock
	 */
	public static String JUNK_wrong_listExpNumForEachGeneration(double mainMag, IncrementalMagFreqDist mfd, double k, double p, double magMin, double c, double numDays, int numGen) {
		
		boolean debug = false;
		
		double[] numForGen = new double[numGen];
		
		double minMagCheck = Math.abs(mfd.getMinMagWithNonZeroRate()-mfd.getDelta()/2.0-magMin);
		if(minMagCheck>0.0001)
			throw new RuntimeException("Min mags must be the same");
		
		IncrementalMagFreqDist normMFD = mfd.deepClone();
		normMFD.scale(1.0/mfd.getTotalIncrRate());

		double expPrimary = getExpectedNumEvents(k, p, mainMag, magMin, c, 0.0, numDays);
		numForGen[0] = expPrimary;
		IncrementalMagFreqDist expPrimaryMFD = mfd.deepClone();
		expPrimaryMFD.scale(expPrimary/mfd.getTotalIncrRate());
				
		int startMagIndex = mfd.getClosestXIndex(magMin+mfd.getDelta()/2.0);
		int endMagIndex = mfd.getXIndex(mfd.getMaxMagWithNonZeroRate());

		double numForLastGen = expPrimary;
		for(int i=1; i<numGen;i++) {	// loop over generations
			double expNum = 0;
			for(int m=startMagIndex; m<=endMagIndex;m++) {
				double expNumAtMag = numForLastGen*normMFD.getY(m)*getExpectedNumEvents(k, p, mfd.getX(m), magMin, c, 0.0, numDays);
				expNum += expNumAtMag;
			}
			numForGen[i] = expNum;
			numForLastGen=expNum;
		}
		
		double totNum = 0;
		for(int i=0; i<numGen;i++) 
			totNum += numForGen[i];
		
		IncrementalMagFreqDist expTotalMFD = mfd.deepClone();
		expTotalMFD.scale(totNum/mfd.getTotalIncrRate());

		
		String resultString = Double.toString(mainMag);

		for(int i=0; i<numGen;i++) {
			resultString+="\t"+Float.toString((float)numForGen[i]);
			if(debug) System.out.println("Gen "+(i+1)+"\t"+(float)numForGen[i]+"\t"+(float)numForGen[i]/totNum);
		}

		if(debug) System.out.println("Total: "+(float)totNum);

		double numPrimaryAtMainMag = expPrimaryMFD.getCumRate(mainMag+mfd.getDelta()/2.0);
		double numTotalAtMainMag = expTotalMFD.getCumRate(mainMag+mfd.getDelta()/2.0);
		
		double magMinusOne = mainMag-1.0+mfd.getDelta()/2.0;
		double numPrimaryAtMainMagMinusOne, numTotalAtMainMagMinusOne;
		if(magMinusOne>=mfd.getMinX()) {
			numPrimaryAtMainMagMinusOne = expPrimaryMFD.getCumRate(magMinusOne);
			numTotalAtMainMagMinusOne = expTotalMFD.getCumRate(magMinusOne);
		}
		else {
			numPrimaryAtMainMagMinusOne = Double.NaN;
			numTotalAtMainMagMinusOne = Double.NaN;
		}
		
		resultString += "\t"+Float.toString((float)numPrimaryAtMainMag)+"\t"+Float.toString((float)numTotalAtMainMag);
		resultString += "\t"+Float.toString((float)numPrimaryAtMainMagMinusOne)+"\t"+Float.toString((float)numTotalAtMainMagMinusOne)+"\n";
		
		if(debug)  {
			System.out.println("numPrimaryAtMainMag: "+(float)numPrimaryAtMainMag);
			System.out.println("numTotalAtMainMag: "+(float)numTotalAtMainMag);
			System.out.println("numPrimaryAtMainMagMinusOne: "+(float)numPrimaryAtMainMagMinusOne);
			System.out.println("numTotalAtMainMagMinusOne: "+(float)numTotalAtMainMagMinusOne);
			
			System.out.println("expPrimaryMFD.getTotalIncrRate() = "+expPrimaryMFD.getTotalIncrRate());
			System.out.println("expTotalMFD.getTotalIncrRate() = "+expTotalMFD.getTotalIncrRate());
//			System.out.println("Primary\n"+expPrimaryMFD);
//			System.out.println("Total\n"+expTotalMFD);
			
			ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
			expPrimaryMFD.setName("expPrimaryMFD");
			funcs.add(expPrimaryMFD);
			funcs.add(expPrimaryMFD.getCumRateDistWithOffset());
			expTotalMFD.setName("expTotalMFD");
			funcs.add(expTotalMFD);
			funcs.add(expTotalMFD.getCumRateDistWithOffset());
			GraphWindow sectGraph = new GraphWindow(funcs, "Expected MFDs"); 
			sectGraph.setX_AxisLabel("Mag");
			sectGraph.setY_AxisLabel("Rate");
			sectGraph.setYLog(true);
			sectGraph.setAxisLabelFontSize(20);
			sectGraph.setTickLabelFontSize(18);
			sectGraph.setPlotLabelFontSize(22);
		}
		
		return resultString;
	}

	
	public static void tempCriticality(IncrementalMagFreqDist mfd, double k, double p, double magMin, double c, double numDays) {
		// normalize MFD to PDF between M 2.5 and max mag
		int startMagIndex = mfd.getClosestXIndex(2.55);
		int endMagIndex = mfd.getXIndex(mfd.getMaxMagWithNonZeroRate());
		double sum=0;
		for(int m=startMagIndex; m<=endMagIndex;m++) {
			sum += mfd.getY(m);
		}
		mfd.scale(1.0/sum);

		double criticality = 0;
		for(int m=startMagIndex; m<=endMagIndex;m++) {
			criticality += mfd.getY(m)*getExpectedNumEvents(k, p, mfd.getX(m), magMin, c, 0.0, numDays);
		}
		
		System.out.println("criticality = "+criticality);

	}
	
	public static void writeTriggerStatsToFiles() {
		
		ETAS_Utils etasUtils = new ETAS_Utils();
		
		ETAS_ParameterList etasParams = new ETAS_ParameterList();
		
//		System.out.println("2.5\t"+etasUtils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 2.5, 2.5, etasParams.get_c(), 0d, 1e10));
//		System.out.println("5.0\t"+etasUtils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 5.0, 2.5, etasParams.get_c(), 0d, 1e10));
//		System.out.println("7.8\t"+etasUtils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 7.8, 2.5, etasParams.get_c(), 0d, 1e10));
		
		// Felzer Params:
		etasParams.set_c(0.095);
		etasParams.set_p(1.34);
		etasParams.set_k(0.008);

//		System.out.println("2.5\t"+etasUtils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 2.5, 2.5, etasParams.get_c(), 0d, 1e10));
//		System.out.println("5.0\t"+etasUtils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 5.0, 2.5, etasParams.get_c(), 0d, 1e10));
//		System.out.println("7.8\t"+etasUtils.getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 7.8, 2.5, etasParams.get_c(), 0d, 1e10));
//		System.exit(0);
		
		String Prefix = "Felzer";
		
		double numDays[] = {7.0,365.25, 365250.0};	// 1 week, 1 year, and 1000 years
		String filenames[] = {Prefix+"ETAS_TriggerStats_1week.txt",Prefix+"ETAS_TriggerStats_1year.txt",Prefix+"ETAS_TriggerStats_1000year.txt"};
		
		GutenbergRichterMagFreqDist grDist = new GutenbergRichterMagFreqDist(1.0, 1.0, 2.55, 8.25, 58);
//		GraphWindow graph = new GraphWindow(grDist, "grDist"); 

		
		for(int i=0;i<numDays.length;i++) {
			FileWriter fw;
			try {
				fw = new FileWriter(new File(filenames[i]));
				fw.write("mag\tgen1\tgen2\tgen3\tgen4\tgen5\tgen6\tgen7\tgen8\tgen9\tgen10\tgen11\tgen12\tgen13\tgen14\tgen15\t");
				fw.write("gen1_AtMainMag\ttotAtMainMag\tgen1_AtMainMagMinus1\ttot_AtMainMagMinus1\n");
				for(double mainMag=2.5;mainMag<8.1;mainMag+=0.5) {
					int numRandomSamples = (int) Math.pow(10000.0, 1+(8-mainMag)/8);
					System.out.println("Working on mag "+mainMag+"; numDays="+numDays[i]+"; "+numRandomSamples);
					String line = etasUtils.listExpNumForEachGenerationAlt(mainMag, grDist, etasParams.get_k(), etasParams.get_p(), magMin_DEFAULT, etasParams.get_c(), numDays[i], 15, numRandomSamples);
//					System.out.println(line);
					fw.write(line);
				}
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static EvenlyDiscretizedFunc getSpontanousEventRateFunction(IncrementalMagFreqDist mfd, long histCatStartTime, long forecastStartTime, 
			long forecastEndTime, int numTimeSamples, double k, double p, double magMin, double c) {
		
		double deltaTimeMillis = (double)(forecastEndTime-forecastStartTime)/(double)numTimeSamples;
		EvenlyDiscretizedFunc rateVsEpochTimeFunc = new EvenlyDiscretizedFunc((double)forecastStartTime+deltaTimeMillis/2.0,(double)forecastEndTime-deltaTimeMillis/2.0,numTimeSamples);
		double totalRatePerYear = mfd.getCumRate(2.45);
		int firstMagIndex = mfd.getXIndex(2.45);
		for(int i=0;i<rateVsEpochTimeFunc.size();i++) {
			// compute time over which we have observations
			double histDurationDays = (rateVsEpochTimeFunc.getX(i)-(double)histCatStartTime)/(double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			// loop over magnitudes
			double rate=0;
			for(int m=firstMagIndex;m<mfd.size();m++) {
				if(mfd.getY(m)>1e-10) {	// skip low rate bins
					double mag = mfd.getX(m);
					rate += getExpectedNumEvents(k, p, mag, magMin, c, 0.0, histDurationDays)*mfd.getY(m);
				}
			}
			rateVsEpochTimeFunc.set(i,(totalRatePerYear-rate));
		}
		return rateVsEpochTimeFunc;
	}
	
	
	
	public static EvenlyDiscretizedFunc getSpontanousEventRateFunction(IncrementalMagFreqDist mfd, IncrementalMagFreqDist yrCompleteForMagFunc, long forecastStartTime, 
			long forecastEndTime, int numTimeSamples, double k, double p, double magMin, double c) {
		
		double deltaTimeMillis = (double)(forecastEndTime-forecastStartTime)/(double)numTimeSamples;
		EvenlyDiscretizedFunc rateVsEpochTimeFunc = new EvenlyDiscretizedFunc((double)forecastStartTime+deltaTimeMillis/2.0,(double)forecastEndTime-deltaTimeMillis/2.0,numTimeSamples);
		double totalRatePerYear = mfd.getCumRate(2.55);
		int firstMagIndex = mfd.getXIndex(2.55);
		for(int i=0;i<rateVsEpochTimeFunc.size();i++) {
			double rate=0;
			for(int m=firstMagIndex;m<mfd.size();m++) {
				if(mfd.getY(m)>1e-10) {	// skip low rate bins
					double mag = mfd.getX(m);
					double magCompleteTimeMillis = (yrCompleteForMagFunc.getY(mag)-1970)*ProbabilityModelsCalc.MILLISEC_PER_YEAR;
					double histDurationDays = (rateVsEpochTimeFunc.getX(i)-magCompleteTimeMillis)/(double)ProbabilityModelsCalc.MILLISEC_PER_DAY;					
					rate += getExpectedNumEvents(k, p, mag, magMin, c, 0.0, histDurationDays)*mfd.getY(m);
				}
			}
			rateVsEpochTimeFunc.set(i,(totalRatePerYear-rate));
		}
		return rateVsEpochTimeFunc;
	}

	
	
	public static EvenlyDiscretizedFunc getSpontanousEventRateFunctionOLD(IncrementalMagFreqDist mfd, long histCatStartTime, long forecastStartTime, 
			long forecastEndTime, int numTimeSamples, double k, double p, double magMin, double c) {
		
		EvenlyDiscretizedFunc rateVsEpochTimeFunc = new EvenlyDiscretizedFunc((double)forecastStartTime,(double)forecastEndTime,numTimeSamples);
		double totalRatePerYear = mfd.calcSumOfY_Vals();
		double meanAftRate=0;
		for(int i=0;i<rateVsEpochTimeFunc.size();i++) {
			// compute time over which we have observations
			double histDurationDays = (rateVsEpochTimeFunc.getX(i)-(double)histCatStartTime)/(double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
			// loop over magnitudes
			double rate=0;
			for(int m=0;m<mfd.size();m++) {
				if(mfd.getY(m)>1e-10) {	// skip low rate bins
					double mag = mfd.getX(m);
					rate += getExpectedNumEvents(k, p, mag, magMin, c, 0.0, histDurationDays)*mfd.getY(m);
				}
			}
			meanAftRate+=rate;
			rateVsEpochTimeFunc.set(i,(totalRatePerYear-rate));
		}
		meanAftRate /= (double)numTimeSamples;
//System.out.println("n="+maenAftRate/totalRatePerYear);
		return rateVsEpochTimeFunc;
	}
	
	/**
	 * 
	 * @param rateFunc - x-axisis epoch time (doubles) and the y-axis is the yearly rate at that time
	 * @return
	 */
	public long[] getRandomSpontanousEventTimes(IncrementalMagFreqDist mfd, long histCatStartTime, long forecastStartTime, 
			long forecastEndTime, int numTimeSamples, double k, double p, double magMin, double c) {
		
		EvenlyDiscretizedFunc rateFunc = getSpontanousEventRateFunction(mfd, histCatStartTime, forecastStartTime, 
				forecastEndTime, numTimeSamples, k, p, magMin, c);
		
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(rateFunc);
		
		double meanRatePerYear = 0;
		for(int i=0;i<rateFunc.size();i++) {
			meanRatePerYear+= rateFunc.getY(i)/rateFunc.size();	// fact that it should be only half the first and last bin doesn't seem to matter
		}
		
		double numYears = (rateFunc.getMaxX()-rateFunc.getMinX()+rateFunc.getDelta())/ProbabilityModelsCalc.MILLISEC_PER_YEAR;
		int numEvents = getPoissonRandomNumber(meanRatePerYear*numYears);
		long[] eventTimesMillis = new long[numEvents];
		for(int i=0;i<numEvents;i++) {
			int randIndex = sampler.getRandomInt(getRandomDouble());
			double time = rateFunc.getX(randIndex) + (getRandomDouble()-0.5)*rateFunc.getDelta();	// latter term randomizes within the bin
			eventTimesMillis[i] = (long)time;
		}
		
		return eventTimesMillis;		
	}
	
	
	
	/**
	 * 
	 * @param rateFunc - x-axisis epoch time (doubles) and the y-axis is the yearly rate at that time
	 * @return
	 */
	public long[] getRandomSpontanousEventTimes(IncrementalMagFreqDist mfd, IncrementalMagFreqDist yrCompleteForMagFunc, long forecastStartTime, 
			long forecastEndTime, int numTimeSamples, double k, double p, double magMin, double c) {
		
		EvenlyDiscretizedFunc rateFunc = getSpontanousEventRateFunction(mfd, yrCompleteForMagFunc, forecastStartTime, 
				forecastEndTime, numTimeSamples, k, p, magMin, c);
		
		IntegerPDF_FunctionSampler sampler = new IntegerPDF_FunctionSampler(rateFunc);
		
		double meanRatePerYear = 0;
		for(int i=0;i<rateFunc.size();i++) {
			meanRatePerYear+= rateFunc.getY(i)/rateFunc.size();	// fact that it should be only half the first and last bin doesn't seem to matter
		}
		
		double numYears = (rateFunc.getMaxX()-rateFunc.getMinX()+rateFunc.getDelta())/ProbabilityModelsCalc.MILLISEC_PER_YEAR;
		int numEvents = getPoissonRandomNumber(meanRatePerYear*numYears);
		long[] eventTimesMillis = new long[numEvents];
		for(int i=0;i<numEvents;i++) {
			int randIndex = sampler.getRandomInt(getRandomDouble());
			double time = rateFunc.getX(randIndex) + (getRandomDouble()-0.5)*rateFunc.getDelta();	// latter term randomizes within the bin
			eventTimesMillis[i] = (long)time;
		}
		
		return eventTimesMillis;		
	}

	
	public long[] getRandomSpontanousEventTimesOLD(IncrementalMagFreqDist mfd, long histCatStartTime, long forecastStartTime, 
			long forecastEndTime, int numTimeSamples, double k, double p, double magMin, double c) {
		
		EvenlyDiscretizedFunc rateFunc = getSpontanousEventRateFunctionOLD(mfd, histCatStartTime, forecastStartTime, 
				forecastEndTime, numTimeSamples, k, p, magMin, c);
		
		double meanRatePerYear = 0;
		ArbitrarilyDiscretizedFunc inverseFunc = new ArbitrarilyDiscretizedFunc();
		for(int i=0;i<rateFunc.size();i++) {
			inverseFunc.set(rateFunc.getY(i), rateFunc.getX(i));
			meanRatePerYear+= rateFunc.getY(i)/rateFunc.size();	// fact that it should be only half the first and last bin doesn't seem to matter
		}
		
		GraphWindow rateFuncGraph = new GraphWindow(rateFunc, "rateFunc"); 
		GraphWindow inverseFuncGraph = new GraphWindow(inverseFunc, "inverseFunc"); 

		
		double numYears = (rateFunc.getMaxX()-rateFunc.getMinX())/ProbabilityModelsCalc.MILLISEC_PER_YEAR;
// System.out.print("\texpNum="+(meanRatePerYear*numYears));
		int numEvents = getPoissonRandomNumber(meanRatePerYear*numYears);
		long[] eventTimesMillis = new long[numEvents];
		double xMin = inverseFunc.getMinX();
		double xMax = inverseFunc.getMaxX();
		for(int i=0;i<numEvents;i++) {
			double randX = xMin + getRandomDouble()*(xMax-xMin);
			eventTimesMillis[i] = (long)inverseFunc.getInterpolatedY(randX);
		}
		
		return eventTimesMillis;		
	}

	
	public static void main(String[] args) {
		
//		plotExpectedNumPrimaryVsTime();
		
//		ETAS_Simulator.plotCatalogMagVsTime(ETAS_Simulator.getHistCatalog(2012));
//		ETAS_Simulator.plotCatalogMagVsTime(ETAS_Simulator.getHistCatalogFiltedForStatewideCompleteness(2012));
		
		String simulationName = "MagTimeCatalogSimulation_JeanneParams_30yrs_5000_HistCat_U3MFD_correctSpont";
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF(2012, 1.0);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.updateForecast();
		SummedMagFreqDist mfd = ERF_Calculator.getTotalMFD_ForERF(erf, 2.55, 8.45, 60, true);
		ETAS_Simulator.plotFilteredCatalogMagFreqDist(ETAS_Simulator.getHistCatalogFiltedForStatewideCompleteness(2012),
				new U3_EqkCatalogStatewideCompleteness(), mfd);
		
//		runMagTimeCatalogSimulation();
		
//		writeTriggerStatsToFiles();
		
//		EvenlyDiscretizedFunc func = getTargetDistDecayDensityFunc(-2.1, 3.9, 31, 1.96, 0.79);
//		System.out.println(func);
//		GraphWindow distDecayGraph2 = new GraphWindow(func, "Dist Decay"); 
//		distDecayGraph2.setYLog(true);
		
		
		
		
//		EvenlyDiscretizedFunc magFunc = new EvenlyDiscretizedFunc(2d, 50, 0.1d);
//		for (int i=0; i<magFunc.size(); i++)
//			magFunc.set(i, getRuptureRadiusFromMag(magFunc.getX(i)));
//		new GraphWindow(magFunc, "Radius vs Mag");
//		
//		new ETAS_Utils().getRandomLocationOnRupSurface(
//				new ETAS_EqkRupture(new ObsEqkRupture("", 0l, new Location(32.25570, -115.26100, 33.54000), 4.19)));

//		// THIS EXPLORES THE NUMBER OF EXPECTED EVENTS FOR EACH GENERATION FOR GR VS CHAR DISTRIBUTIONS
//		double mainMag = 5.5;
//		double numDays = 7;
//		GutenbergRichterMagFreqDist grDist = new GutenbergRichterMagFreqDist(1.0, 1.0, 2.55, 8.25, 58);
////		System.out.println(grDist);
//		System.out.println("Perfect GR:");
//		listExpNumForEachGeneration(mainMag, grDist, k_DEFAULT, p_DEFAULT, magMin_DEFAULT, c_DEFAULT, numDays, 15);

		//tempCriticality(grDist, k_DEFAULT, p_DEFAULT, magMin_DEFAULT, c_DEFAULT, numDays);
////		System.exit(0);	
//		
//		
//		GutenbergRichterMagFreqDist subSeisDist = new GutenbergRichterMagFreqDist(1.0, 1.0, 2.55, 6.25, 38);
//		GaussianMagFreqDist supraSeisDist = new GaussianMagFreqDist(6.35, 20, 0.1, 7.35, 0.5, 1.0, 2.0, 2);
//		supraSeisDist.scaleToCumRate(6.35, grDist.getCumRate(6.35)*20);	// this make it look pretty typical
//		
//		double grCorr = ETAS_Utils.getScalingFactorToImposeGR(supraSeisDist, subSeisDist, false);
//
////		System.out.println("\nGR scale factor = "+grCorr);
//		
//		double charFactor=5.0;	// this has a criticality of about 1.0
////		double charFactor=1.0;	// test perfect GR
//		supraSeisDist.scaleToCumRate(0, supraSeisDist.getTotalIncrRate()*grCorr*charFactor);
////		supraSeisDist.scaleToCumRate(0, supraSeisDist.getTotalIncrRate());
//				
//		SummedMagFreqDist totDist = new SummedMagFreqDist(2.55, 8.25, 58);
//		totDist.addIncrementalMagFreqDist(subSeisDist);
//		totDist.addIncrementalMagFreqDist(supraSeisDist);
//		
//		double grCorrFinal = ETAS_Utils.getScalingFactorToImposeGR(supraSeisDist, subSeisDist, false);
//		System.out.println("\nGR scale factor = "+grCorrFinal);
//		listExpNumForEachGeneration(mainMag, totDist, k_DEFAULT, p_DEFAULT, magMin_DEFAULT, c_DEFAULT, numDays);
//		tempCriticality(totDist, k_DEFAULT, p_DEFAULT, magMin_DEFAULT, c_DEFAULT, numDays);
//		
//		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
//		mfdList.add(subSeisDist);
//		mfdList.add(supraSeisDist);
//		mfdList.add(totDist);
//			// Plot these MFDs
//			GraphWindow magProbDistsGraph = new GraphWindow(mfdList, "MFDs"); 
//			magProbDistsGraph.setX_AxisLabel("Mag");
//			magProbDistsGraph.setY_AxisLabel("Number");
////			magProbDistsGraph.setY_AxisRange(1e-5, 1e3);
////			magProbDistsGraph.setX_AxisRange(2d, 9d);
//			magProbDistsGraph.setYLog(true);
//			magProbDistsGraph.setPlotLabelFontSize(18);
//			magProbDistsGraph.setAxisLabelFontSize(16);
//			magProbDistsGraph.setTickLabelFontSize(14);
		

		
		
//		ETAS_Utils etas_utils = new ETAS_Utils(100);
//		etas_utils.getRandomInt(0);
//		
//		for(int i=0;i<10;i++)
//			System.out.println(i+"\t"+etas_utils.getRandomDouble());
//		System.exit(0);

		
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
		
//		double distDecayArray[] = {ETAS_Utils.distDecay_DEFAULT, ETAS_Utils.distDecay_DEFAULT, 1.4};
//		double minDistArray[] = {ETAS_Utils.minDist_DEFAULT, 2.0, ETAS_Utils.minDist_DEFAULT};
//		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
//		for(int j=0;j<distDecayArray.length;j++) {
//			EvenlyDiscretizedFunc cumDecayFunc = new EvenlyDiscretizedFunc(-3d,25,0.25);
//			for(int i=0;i<cumDecayFunc.size();i++) {
//				double dist = Math.pow(10d, cumDecayFunc.getX(i));
//				cumDecayFunc.set(i,getDecayFractionInsideDistance(distDecayArray[j], minDistArray[j], dist));
//			}
//			cumDecayFunc.setName("distDecay="+distDecayArray[j]+";\tminDist="+minDistArray[j]);
//			cumDecayFunc.setInfo(" ");
//			funcs.add(cumDecayFunc);
//		}
//		GraphWindow graph = new GraphWindow(funcs, "Probability of Aftershock Within Distance");
//		graph.setX_AxisLabel("log10 Distance");
//		graph.setX_AxisLabel("Cum Density");

		
		
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
	 * This provides a rupture surface where there is no creep/aseismicity reduction
	 * @param fssRupIndex
	 * @param erf
	 * @param gridSpacing
	 * @return
	 */
	public RuptureSurface getRuptureSurfaceWithNoCreepReduction(int fssRupIndex, FaultSystemSolutionERF erf, double gridSpacing) {
		List<RuptureSurface> rupSurfs = Lists.newArrayList();
		if ((Boolean)erf.getParameter(FaultSystemSolutionERF.QUAD_SURFACES_PARAM_NAME).getValue()) {
			for(FaultSectionPrefData fltData: erf.getSolution().getRupSet().getFaultSectionDataForRupture(fssRupIndex))
				rupSurfs.add(fltData.getQuadSurface(false, gridSpacing));
		} else {
			for(FaultSectionPrefData fltData: erf.getSolution().getRupSet().getFaultSectionDataForRupture(fssRupIndex))
				rupSurfs.add(fltData.getStirlingGriddedSurface(gridSpacing, false, false));
		}
		if (rupSurfs.size() == 1)
			return rupSurfs.get(0);
		else
			return new CompoundSurface(rupSurfs);
	}
	
	/**
	 * This returns a random location from the given surface.  For point surfaces,
	 * this returns the hypocenter if mag<=4.0, otherwise it returns a random
	 * location uniformly distributed in a spherical radius defined by
	 * this.getRuptureRadiusFromMag(mag).  For finite surfaces this gets a random 
	 * location from the evenly discretized surface (uniformly distributed).
	 * 
	 * TODO remove hard coding of depthBottom and point-source magnitude
	 * @param rupSurf
	 * @return
	 */
	public Location getRandomLocationOnRupSurface(ETAS_EqkRupture parRup) {
		if(parRup.getRuptureSurface().isPointSurface()) {
			Location hypoLoc = parRup.getRuptureSurface().getFirstLocOnUpperEdge();
			if(parRup.getMag()<=4.0)
				return hypoLoc;
			double radius = getRuptureRadiusFromMag(parRup.getMag());
			double testRadius = radius+1;
			Location loc = null;
			int count = 0;
			double minDist = Double.POSITIVE_INFINITY;
			if (hypoLoc.getDepth() > 24d || hypoLoc.getDepth() < 0) {
				System.err.println("WARNING: Temporary fix for bad input hypo depth.\nParent rup: "
						+ETAS_CatalogIO.getEventFileLine(parRup));
				if (hypoLoc.getDepth() > 24d)
					hypoLoc = new Location(hypoLoc.getLatitude(), hypoLoc.getLongitude(), 24d);
				else
					hypoLoc = new Location(hypoLoc.getLatitude(), hypoLoc.getLongitude(), 0d);
			}
			while(testRadius>radius) {
				double lat = hypoLoc.getLatitude()+(2.0*getRandomDouble()-1.0)*(radius/111.0);
				double lon = hypoLoc.getLongitude()+(2.0*getRandomDouble()-1.0)*(radius/(111*Math.cos(hypoLoc.getLatRad())));
				double depthBottom = hypoLoc.getDepth()+radius;
				if(depthBottom>24.0)
					depthBottom=24.0;
				double depthTop = hypoLoc.getDepth()-radius;
				if(depthTop<0.0)
					depthTop=0.0;
				double depth = depthTop + getRandomDouble()*(depthBottom-depthTop);
				loc = new Location(lat,lon,depth);
				testRadius=LocationUtils.linearDistanceFast(loc, hypoLoc);
				minDist = Math.min(minDist, testRadius);
				count++;
				if (count == 1000000) {
					String debug = "Stuck in loop, quitting after "+count+" tries.\nHypo loc: "+hypoLoc
							+"\nCur loc: "+loc+"\nRadius: "+radius+"\nCur dist: "+testRadius+"\nClosest so far: "+minDist;
					debug += "\ndepthTop="+depthTop+", depthBottom="+depthBottom;
					debug += "\nParent rup: "+ETAS_CatalogIO.getEventFileLine(parRup);
					throw new IllegalStateException(debug);
				}
			}
			return loc;
		}
		else {
			LocationList locList = parRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
			return locList.get(getRandomInt(locList.size()-1));
		}
	}
	
	
	
	/**
	 * This returns the rupture area given by mag=Log10(area)+4 
	 * (consistent with Ellsworth A and Hanks and Bakun at low mags)
	 * @param mag
	 * @return
	 */
	public static double getRuptureAreaFromMag(double mag) {
		return Math.pow(10.0, mag-4.0);
	}
	
	
	/**
	 * This returns the rupture radius assuming a circle with area given by 
	 * getRuptureAreaFromMag(mag)
	 * @param mag
	 * @return
	 */
	public static double getRuptureRadiusFromMag(double mag) {
		return Math.sqrt(getRuptureAreaFromMag(mag)/Math.PI);
	}
	
	
	/**
	 * This returns the amount by which the supra-seismogenic MFD has to be scaled in order for the total MFD (sub+supra) to
	 * have the same expected number of primary aftershocks as a perfect GR (extrapolated from the sub MFD to the max-non-zero-mag
	 * of the supra MFD).
	 * 
	 * @param supraSeisMFD
	 * @param subSeisMFD
	 * @return
	 */
	public static double getScalingFactorToImposeGR_numPrimary(IncrementalMagFreqDist supraSeisMFD, IncrementalMagFreqDist subSeisMFD, boolean debug) {
		if (supraSeisMFD.getMaxY() == 0d || subSeisMFD.getMaxY() == 0d)
			// fix for empty cells, weird solutions (such as UCERF2 mapped) with zero rate faults, or zero subSeis MFDs because section outside gridded seis region
			return 1d;
		
		double minMag = subSeisMFD.getMinMagWithNonZeroRate();
//		double minMag = subSeisMFD.getMinX();
		double maxMagWithNonZeroRate = supraSeisMFD.getMaxMagWithNonZeroRate();
		if(Double.isNaN(maxMagWithNonZeroRate)) {
			System.out.println("ISSUE: maxMagWithNonZeroRate="+maxMagWithNonZeroRate);
			return 1d;
		}
		int numMag = (int)Math.round((maxMagWithNonZeroRate-minMag)/supraSeisMFD.getDelta()) + 1;
		Preconditions.checkState(numMag > 1 || minMag == maxMagWithNonZeroRate,
				"only have 1 bin but min != max: "+minMag+" != "+maxMagWithNonZeroRate+"\n"+supraSeisMFD);
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(1.0, 1.0, minMag, maxMagWithNonZeroRate, numMag);
		gr.scaleToIncrRate(minMag, subSeisMFD.getY(minMag));
		
		// Since b=1 (and a=1, as implicit in Felzer, and explicit in equation (3) of http://pubs.usgs.gov/of/2013/1165/pdf/ofr2013-1165_appendixS.pdf),
		// each magnitude has an equal number of expected
		
		double expNumGR = 0;
		for(int i=0;i<gr.size();i++) {
			double mag = gr.getX(i);
			expNumGR += gr.getY(i)*Math.pow(10, mag);
		}
		
		double expNumSubSeis = 0;
		for(int i=0;i<subSeisMFD.size();i++) {
			double mag = subSeisMFD.getX(i);
			expNumSubSeis += subSeisMFD.getY(i)*Math.pow(10, mag);
		}

		double expNumSupraSeis = 0;
		for(int i=0;i<supraSeisMFD.size();i++) {
			double mag = supraSeisMFD.getX(i);
			expNumSupraSeis += supraSeisMFD.getY(i)*Math.pow(10, mag);
		}
		double result = (expNumGR-expNumSubSeis)/expNumSupraSeis;
	
		if(debug) {
			ArrayList<IncrementalMagFreqDist> funcs = new ArrayList<IncrementalMagFreqDist>();
			funcs.add(supraSeisMFD);
			funcs.add(subSeisMFD);
			funcs.add(gr);
			GraphWindow graph = new GraphWindow(funcs, "getScalingFactorToImposeGR "+result);
			graph.setX_AxisLabel("Mag");
			graph.setY_AxisLabel("Incr Rate");
			System.out.println("result="+(expNumGR-expNumSubSeis)/expNumSupraSeis);
			System.out.println("minMag="+minMag+"\nmaxMagWithNonZeroRate="+maxMagWithNonZeroRate+"\nexpNumGR="+
			expNumGR+"\nexpNumSubSeis="+expNumSubSeis+"\nexpNumSupraSeis="+expNumSupraSeis);			
		}
		
		return result;
	}
	
	
	public static double getMinMagSupra(IncrementalMagFreqDist supraSeisMFD, IncrementalMagFreqDist subSeisMFD) {
		if (supraSeisMFD.getMaxY() == 0d || subSeisMFD.getMaxY() == 0d)
			// fix for empty cells, weird solutions (such as UCERF2 mapped) with zero rate faults, or zero subSeis MFDs because section outside gridded seis region
			return Double.NaN;

		double minMag = subSeisMFD.getMinMagWithNonZeroRate();

		double minMagSupra;
		double minMagSupraAlt1 = supraSeisMFD.getMinMagWithNonZeroRate();	// can't use this because there are some very low mags with low rates on some branches
//		double minMagSupra = subSeisMFD.getMaxMagWithNonZeroRate()+subSeisMFD.getDelta();	// this not good either

		if(minMagSupraAlt1>subSeisMFD.getMaxMagWithNonZeroRate()) {
			minMagSupra=minMagSupraAlt1;
		}
		else {
			// Define the minSupraMag as the bin above the last perfect GR value on the subseismo MFD (non-perfect for branch averaged solutions)
			// as long as it remains above minMagSupraAlt1
			// mags below this point should really be filtered from the forecast
			double thresh = 0.9;
			minMagSupra=subSeisMFD.getMaxMagWithNonZeroRate();
			int indexForMinMagSupra = subSeisMFD.getXIndex(minMagSupra);
			double testVal = subSeisMFD.getY(indexForMinMagSupra)/(subSeisMFD.getY(minMag)*Math.pow(10, minMag-minMagSupra));
			while(testVal<thresh && indexForMinMagSupra>0) {
				indexForMinMagSupra-=1;
				minMagSupra=subSeisMFD.getX(indexForMinMagSupra);
				testVal = subSeisMFD.getY(indexForMinMagSupra)/(subSeisMFD.getY(minMag)*Math.pow(10, minMag-minMagSupra));
			}
			if(indexForMinMagSupra==0) {
				throw new RuntimeException("Problem");
//				System.out.println("indexForMinMagSupra="+indexForMinMagSupra+"\tfor\t"+supraSeisMFD.getName());
//				GraphWindow graph = new GraphWindow(subSeisMFD, supraSeisMFD.getName());
//				System.out.println(subSeisMFD);
//				minMagSupra=subSeisMFD.getMaxMagWithNonZeroRate();
//				indexForMinMagSupra = subSeisMFD.getXIndex(minMagSupra);
//				testVal = subSeisMFD.getY(indexForMinMagSupra)/Math.pow(10, minMag-minMagSupra);
//				System.out.println(testVal+"t"+minMagSupra+"\t"+indexForMinMagSupra);
//				while(testVal<thresh && indexForMinMagSupra>0) {
//					indexForMinMagSupra-=1;
//					minMagSupra=subSeisMFD.getX(indexForMinMagSupra);
//					testVal = subSeisMFD.getY(indexForMinMagSupra)/Math.pow(10, minMag-minMagSupra);
//					System.out.println(testVal+"\t"+minMagSupra+"\t"+indexForMinMagSupra);
//				}
			}
			indexForMinMagSupra+=1;
			minMagSupra=subSeisMFD.getX(indexForMinMagSupra);
		}
		
		// don't let it go below minMagSupraAlt1
		if(minMagSupra<minMagSupraAlt1)
			minMagSupra=minMagSupraAlt1;

		if(minMagSupra>supraSeisMFD.getMaxMagWithNonZeroRate()) {
			minMagSupra=supraSeisMFD.getMaxMagWithNonZeroRate();
		}
		
		return minMagSupra;

	}



	/**
	 * This returns the amount by which the supra-seismogenic MFD has to be scaled in order for the total MFD (sub+supra) to
	 * have the same expected number of primary aftershocks as a perfect GR (extrapolated from the sub MFD to the max-non-zero-mag
	 * of the supra MFD).
	 * 
	 * @param supraSeisMFD
	 * @param subSeisMFD
	 * @return
	 */
	public static double getScalingFactorToImposeGR_supraRates(IncrementalMagFreqDist supraSeisMFD, IncrementalMagFreqDist subSeisMFD, boolean debug) {
		if (supraSeisMFD.getMaxY() == 0d || subSeisMFD.getMaxY() == 0d)
			// fix for empty cells, weird solutions (such as UCERF2 mapped) with zero rate faults, or zero subSeis MFDs because section outside gridded seis region
			return 1d;

		double minMag = subSeisMFD.getMinMagWithNonZeroRate();
		
		double minMagSupra = getMinMagSupra(supraSeisMFD, subSeisMFD);
		
		// MOVED TO OTHER METHOD:
//		double minMagSupra;
//		double minMagSupraAlt1 = supraSeisMFD.getMinMagWithNonZeroRate();	// can't use this because there are some very low mags with low rates on some branches
////		double minMagSupra = subSeisMFD.getMaxMagWithNonZeroRate()+subSeisMFD.getDelta();	// this not good either
//
//		if(minMagSupraAlt1>subSeisMFD.getMaxMagWithNonZeroRate()) {
//			minMagSupra=minMagSupraAlt1;
//		}
//		else {
//			// Define the minSupraMag as the bin above the last perfect GR value on the subseismo MFD (non-perfect for branch averaged solutions)
//			// as long as it remains above minMagSupraAlt1
//			// mags below this point should really be filtered from the forecast
//			double thresh = 0.9;
//			minMagSupra=subSeisMFD.getMaxMagWithNonZeroRate();
//			int indexForMinMagSupra = subSeisMFD.getXIndex(minMagSupra);
//			double testVal = subSeisMFD.getY(indexForMinMagSupra)/(subSeisMFD.getY(minMag)*Math.pow(10, minMag-minMagSupra));
//			while(testVal<thresh && indexForMinMagSupra>0) {
//				indexForMinMagSupra-=1;
//				minMagSupra=subSeisMFD.getX(indexForMinMagSupra);
//				testVal = subSeisMFD.getY(indexForMinMagSupra)/(subSeisMFD.getY(minMag)*Math.pow(10, minMag-minMagSupra));
//			}
//			if(indexForMinMagSupra==0) {
//				throw new RuntimeException("Problem");
////				System.out.println("indexForMinMagSupra="+indexForMinMagSupra+"\tfor\t"+supraSeisMFD.getName());
////				GraphWindow graph = new GraphWindow(subSeisMFD, supraSeisMFD.getName());
////				System.out.println(subSeisMFD);
////				minMagSupra=subSeisMFD.getMaxMagWithNonZeroRate();
////				indexForMinMagSupra = subSeisMFD.getXIndex(minMagSupra);
////				testVal = subSeisMFD.getY(indexForMinMagSupra)/Math.pow(10, minMag-minMagSupra);
////				System.out.println(testVal+"t"+minMagSupra+"\t"+indexForMinMagSupra);
////				while(testVal<thresh && indexForMinMagSupra>0) {
////					indexForMinMagSupra-=1;
////					minMagSupra=subSeisMFD.getX(indexForMinMagSupra);
////					testVal = subSeisMFD.getY(indexForMinMagSupra)/Math.pow(10, minMag-minMagSupra);
////					System.out.println(testVal+"\t"+minMagSupra+"\t"+indexForMinMagSupra);
////				}
//			}
//			indexForMinMagSupra+=1;
//			minMagSupra=subSeisMFD.getX(indexForMinMagSupra);
//		}
//		
//		// don't let it go below minMagSupraAlt1
//		if(minMagSupra<minMagSupraAlt1)
//			minMagSupra=minMagSupraAlt1;
//
//		if(minMagSupra>supraSeisMFD.getMaxMagWithNonZeroRate()) {
//			minMagSupra=supraSeisMFD.getMaxMagWithNonZeroRate();
//		}
		
		double maxMagWithNonZeroRate = supraSeisMFD.getMaxMagWithNonZeroRate();
		if(Double.isNaN(maxMagWithNonZeroRate)) {
			System.out.println("ISSUE: maxMagWithNonZeroRate="+maxMagWithNonZeroRate);
			return 1d;
		}
		int numMag = (int)Math.round((maxMagWithNonZeroRate-minMag)/supraSeisMFD.getDelta()) + 1;
		Preconditions.checkState(numMag > 1 || minMag == maxMagWithNonZeroRate,
				"only have 1 bin but min != max: "+minMag+" != "+maxMagWithNonZeroRate+"\n"+supraSeisMFD);
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(1.0, 1.0, minMag, maxMagWithNonZeroRate, numMag);
		gr.scaleToIncrRate(minMag, subSeisMFD.getY(minMag));

		double result=Double.NaN;
		try {
			result = gr.getCumRate(minMagSupra)/supraSeisMFD.getCumRate(minMagSupra);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: "+minMagSupra+"\t"+supraSeisMFD.getName());
			System.out.println(supraSeisMFD);
			System.exit(0);
		}

// this shows that values using minMagSupraOld are always greater
//double test =  (gr.getCumRate(minMagSupraOld)/supraSeisMFD.getCumRate(minMagSupraOld))/result;	// want this to be greater than 1.0 (need to push lower mags up more in this case)
//if(test<1)
//	System.out.println(test+"\t"+supraSeisMFD.getName());

		if(debug) {
			supraSeisMFD.setName("supraSeisMFD");
			supraSeisMFD.setInfo("minMagSupra="+minMagSupra+"\nminMag="+minMag);
			subSeisMFD.setName("subSeisMFD");
			ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
			funcs.add(supraSeisMFD);
			funcs.add(subSeisMFD);
			funcs.add(gr);
			EvenlyDiscretizedFunc cumGR = gr.getCumRateDistWithOffset();
			cumGR.setName("cumGR");
			funcs.add(cumGR);
			EvenlyDiscretizedFunc cumSupraSeisMFD = supraSeisMFD.getCumRateDistWithOffset();
			cumSupraSeisMFD.setName("cumSupraSeisMFD");
			funcs.add(cumSupraSeisMFD);
			EvenlyDiscretizedFunc cumSupraSeisMFD_scaled = cumSupraSeisMFD.deepClone();
			cumSupraSeisMFD_scaled.scale(result);
			cumSupraSeisMFD_scaled.setName("cumSupraSeisMFD_scaled");
			funcs.add(cumSupraSeisMFD_scaled);
			GraphWindow graph = new GraphWindow(funcs, "getScalingFactorToImposeGR_supraRates "+result);
			graph.setX_AxisLabel("Mag");
			graph.setY_AxisLabel("Incr Rate");
			graph.setYLog(true);
			System.out.println("minMag="+minMag+" ;minMagSupra="+minMagSupra+"; maxMagWithNonZeroRate="+maxMagWithNonZeroRate);
			System.out.println("result="+result);
		}

		return result;
	}
	
	
	public static double getScalingFactorToImposeGR_MoRates(IncrementalMagFreqDist supraSeisMFD, IncrementalMagFreqDist subSeisMFD, boolean debug) {
		if (supraSeisMFD.getMaxY() == 0d || subSeisMFD.getMaxY() == 0d)
			// fix for empty cells, weird solutions (such as UCERF2 mapped) with zero rate faults, or zero subSeis MFDs because section outside gridded seis region
			return 1d;

		double minMag = subSeisMFD.getMinMagWithNonZeroRate();
		
		double minMagSupra = getMinMagSupra(supraSeisMFD, subSeisMFD);
		
		
		double maxMagWithNonZeroRate = supraSeisMFD.getMaxMagWithNonZeroRate();
		if(Double.isNaN(maxMagWithNonZeroRate)) {
			System.out.println("ISSUE: maxMagWithNonZeroRate="+maxMagWithNonZeroRate);
			return 1d;
		}
		int numMag = (int)Math.round((maxMagWithNonZeroRate-minMag)/supraSeisMFD.getDelta()) + 1;
		Preconditions.checkState(numMag > 1 || minMag == maxMagWithNonZeroRate,
				"only have 1 bin but min != max: "+minMag+" != "+maxMagWithNonZeroRate+"\n"+supraSeisMFD);
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(1.0, 1.0, minMag, maxMagWithNonZeroRate, numMag);
		gr.scaleToTotalMomentRate(supraSeisMFD.getTotalMomentRate()+subSeisMFD.getTotalMomentRate());

		double result=Double.NaN;
		try {
			result = subSeisMFD.getCumRate(minMag)/gr.getCumRate(minMag);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(debug) {
			supraSeisMFD.setName("supraSeisMFD");
			supraSeisMFD.setInfo("minMagSupra="+minMagSupra+"\nminMag="+minMag);
			subSeisMFD.setName("subSeisMFD");
			ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
			funcs.add(supraSeisMFD);
			funcs.add(subSeisMFD);
			funcs.add(gr);
			EvenlyDiscretizedFunc cumGR = gr.getCumRateDistWithOffset();
			cumGR.setName("cumGR");
			funcs.add(cumGR);
			EvenlyDiscretizedFunc cumSupraSeisMFD = supraSeisMFD.getCumRateDistWithOffset();
			cumSupraSeisMFD.setName("cumSupraSeisMFD");
			funcs.add(cumSupraSeisMFD);
			EvenlyDiscretizedFunc cumSupraSeisMFD_scaled = cumSupraSeisMFD.deepClone();
			cumSupraSeisMFD_scaled.scale(result);
			cumSupraSeisMFD_scaled.setName("cumSupraSeisMFD_scaled");
			funcs.add(cumSupraSeisMFD_scaled);
			GraphWindow graph = new GraphWindow(funcs, "getScalingFactorToImposeGR_supraRates "+result);
			graph.setX_AxisLabel("Mag");
			graph.setY_AxisLabel("Incr Rate");
			graph.setYLog(true);
			System.out.println("minMag="+minMag+" ;minMagSupra="+minMagSupra+"; maxMagWithNonZeroRate="+maxMagWithNonZeroRate);
			System.out.println("result="+result);
		}

		return result;
	}
	
	public static void plotExpectedNumPrimaryVsTime() {
		
		double magMin=2.5;
		double log_tMin=-4; 
		double log_tMax=10;
		double log_tDelta=0.01;
		
		double magMain=7.8;
		
		// Jeanne's params in her Table S2
		double c=1.78E-05*365.25;
		double k=2.84E-03*Math.pow(365.25,0.07);
		double p=1.07;
		// this is really num in each bin until converted below
		HistogramFunction jeanneDefaultNumPerBin = getNumWithLogTimeFunc(k, p, magMain, magMin, c, log_tMin, log_tMax, log_tDelta);
		double totNum = jeanneDefaultNumPerBin.calcSumOfY_Vals();
		HistogramFunction jeanneDefaultNumOccurred = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		double sum=0;
		for(int i=0;i<jeanneDefaultNumPerBin.size();i++) {
			sum += jeanneDefaultNumPerBin.getY(i);
			jeanneDefaultNumOccurred.set(i,sum);
		}
		HistogramFunction jeanneDefaultNumRemaining = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		sum=0;
		for(int i=jeanneDefaultNumPerBin.size()-1;i>=0;i--) {
			sum += jeanneDefaultNumPerBin.getY(i);
			jeanneDefaultNumRemaining.set(i,sum);
		}
		jeanneDefaultNumRemaining.setName("Num Remaining to occur for Jeanne's Table S2 Params");
		jeanneDefaultNumRemaining.setInfo("c="+(float)c+"\tk="+(float)k+"\tp="+(float)p+"\nTotal Num = "+(float)totNum+
				"\nTest Num = "+(float)getExpectedNumEvents(k, p, magMain, magMin, c, 0.0, Math.pow(10, log_tMax)));
		jeanneDefaultNumOccurred.setName("Num that have occurred for Jeanne's Table S2 Params");
		jeanneDefaultNumOccurred.setInfo("");
		jeanneDefaultNumPerBin.setName("Num per bin for Jeanne's Table S2 Params");
		jeanneDefaultNumPerBin.setInfo("");

		
//		// TEST the slope
//		int index1 = jeanneDefault.getClosestXIndex(4.0);
//		double xWidthLinear1 = Math.pow(10.0, jeanneDefault.getX(index1)+log_tDelta/2.0)-Math.pow(10.0, jeanneDefault.getX(index1)-log_tDelta/2.0);
//		double rate1 = jeanneDefault.getY(index1)/xWidthLinear1;
//		int index2 = jeanneDefault.getClosestXIndex(3.0);
//		double xWidthLinear2 = Math.pow(10.0, jeanneDefault.getX(index2)+log_tDelta/2.0)-Math.pow(10.0, jeanneDefault.getX(index2)-log_tDelta/2.0);
//		double rate2 = jeanneDefault.getY(index2)/xWidthLinear2;
//		double slope = Math.log10(rate1/rate2);
//		System.out.println("slope="+(float)slope);
		
		// Jeanne's alt params; c=2.00*10^-5, p=1.08, k=2.69*10^-3
		c=2.00E-05*365.25;
		k=2.69E-03*Math.pow(365.25,0.08);
		p=1.08;
		// this is really num in each bin until converted below
		HistogramFunction jeanneAltNumPerBin = getNumWithLogTimeFunc(k, p, magMain, magMin, c, log_tMin, log_tMax, log_tDelta);
		totNum = jeanneAltNumPerBin.calcSumOfY_Vals();
		HistogramFunction jeanneAltNumOccurred = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		sum=0;
		for(int i=0;i<jeanneAltNumPerBin.size();i++) {
			sum += jeanneAltNumPerBin.getY(i);
			jeanneAltNumOccurred.set(i,sum);
		}
		HistogramFunction jeanneAltNumRemaining = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		sum=0;
		for(int i=jeanneAltNumPerBin.size()-1;i>=0;i--) {
			sum += jeanneAltNumPerBin.getY(i);
			jeanneAltNumRemaining.set(i,sum);
		}
		jeanneAltNumRemaining.setName("Num Remaining to occur for Jeanne's Alt Params");
		jeanneAltNumRemaining.setInfo("c="+(float)c+"\tk="+(float)k+"\tp="+(float)p+"\nTotal Num = "+(float)totNum+
				"\nTest Num = "+(float)getExpectedNumEvents(k, p, magMain, magMin, c, 0.0, Math.pow(10, log_tMax)));
		jeanneAltNumOccurred.setName("Num that have occurred for Jeanne's Alt Params");
		jeanneAltNumOccurred.setInfo("");
		jeanneAltNumPerBin.setName("Num per bin for Jeanne's Alt Params");
		jeanneAltNumPerBin.setInfo("");


		// Karen's params
		c=0.095;
		k=0.008;
		p=1.34;
		// this is really num in each bin until converted below
		HistogramFunction karenParamsNumPerBin = getNumWithLogTimeFunc(k, p, magMain, magMin, c, log_tMin, log_tMax, log_tDelta);
		totNum = karenParamsNumPerBin.calcSumOfY_Vals();
		HistogramFunction karenParamsNumOccurred = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		sum=0;
		for(int i=0;i<karenParamsNumPerBin.size();i++) {
			sum += karenParamsNumPerBin.getY(i);
			karenParamsNumOccurred.set(i,sum);
		}
		HistogramFunction karenParamsNumRemaining = new HistogramFunction(log_tMin+log_tDelta/2, log_tMax-log_tDelta/2, (int)Math.round((log_tMax-log_tMin)/log_tDelta));
		sum=0;
		for(int i=karenParamsNumPerBin.size()-1;i>=0;i--) {
			sum += karenParamsNumPerBin.getY(i);
			karenParamsNumRemaining.set(i,sum);
		}
		karenParamsNumRemaining.setName("Num Remaining to occur for Karen's Params");
		karenParamsNumRemaining.setInfo("c="+(float)c+"\tk="+(float)k+"\tp="+(float)p+"\nTotal Num = "+(float)totNum+
				"\nTest Num = "+(float)getExpectedNumEvents(k, p, magMain, magMin, c, 0.0, Math.pow(10, log_tMax)));
		karenParamsNumOccurred.setName("Num that have occurred for Karen's Params");
		karenParamsNumOccurred.setInfo("");
		karenParamsNumPerBin.setName("Num per bin for Karen's Params");
		karenParamsNumPerBin.setInfo("");
			
		ArrayList<HistogramFunction> funcList = new ArrayList<HistogramFunction>();
		funcList.add(jeanneDefaultNumRemaining);
		funcList.add(jeanneAltNumRemaining);
		funcList.add(karenParamsNumRemaining);
		funcList.add(jeanneDefaultNumOccurred);
		funcList.add(jeanneAltNumOccurred);
		funcList.add(karenParamsNumOccurred);
		funcList.add(jeanneDefaultNumPerBin);
		funcList.add(jeanneAltNumPerBin);
		funcList.add(karenParamsNumPerBin);
		ArrayList<PlotCurveCharacterstics> curveCharList = new ArrayList<PlotCurveCharacterstics>();
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GREEN));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.GREEN));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.DOTTED_AND_DASHED, 2f, Color.BLACK));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.DOTTED_AND_DASHED, 2f, Color.BLUE));
		curveCharList.add(new PlotCurveCharacterstics(PlotLineType.DOTTED_AND_DASHED, 2f, Color.GREEN));
		GraphWindow numVsTimeGraph = new GraphWindow(funcList, "Num Primay With Time for M = "+magMain,curveCharList); 
		numVsTimeGraph.setX_AxisLabel("Log10 Days");
		numVsTimeGraph.setY_AxisLabel("Num Per Bin, Yet To Occur, or Have Occurred");
		numVsTimeGraph.setX_AxisRange(-4, 7);
		numVsTimeGraph.setYLog(true);
		numVsTimeGraph.setPlotLabelFontSize(18);
		numVsTimeGraph.setAxisLabelFontSize(16);
		numVsTimeGraph.setTickLabelFontSize(14);
		
		String pathName = new File("numPrimaryEventsVsTime_M7pt8.pdf").getAbsolutePath();
		try {
			numVsTimeGraph.saveAsPDF(pathName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	
	
	public static void runMagTimeCatalogSimulation() {
		
//		U3_EqkCatalogStatewideCompleteness test = new U3_EqkCatalogStatewideCompleteness();
//		System.exit(0);
		
//		System.out.println("Jeanne's Min k: "+ETAS_ProductivityParam_k.MIN);
//		System.out.println("Jeanne's Max k: "+ETAS_ProductivityParam_k.MAX);
//		System.exit(0);
		
		ETAS_ParameterList etasParams = new ETAS_ParameterList();
		
//		// Jeanne's altParams:	c=2.00*10^-5, p=1.08, k=2.69*10^-3
//		etasParams.set_c(2.00e-5*365.25);
//		etasParams.set_p(1.08);
//		etasParams.set_k(2.69e-3*Math.pow(365.25,0.08));
//		etasParams.setFractSpont(0.23);

		
//		// Felzer params from Hardebeck et al. (2008) Appendix
//		System.out.println(ETAS_ProductivityParam_k.MIN);
//		System.out.println(ETAS_ProductivityParam_k.MAX);	// need to increase this one to allow Karen's value
//		etasParams.set_c(0.095);
//		etasParams.set_p(1.34);
//		etasParams.set_k(0.008);
		
		// Jeanne's default paramets with fract spontaneous changed (computed as n from Equation (14) with T=5000)
//		etasParams.setFractSpont(1.0-0.642);

		
		String simulationName = "MagTimeCatalogSimulation_JeanneParams_30yrs_5000_HistCat_U3MFD_correctSpont";
		FaultSystemSolutionERF_ETAS erf = ETAS_Simulator.getU3_ETAS_ERF(2012, 1.0);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.updateForecast();
		SummedMagFreqDist mfd = ERF_Calculator.getTotalMFD_ForERF(erf, 2.55, 8.45, 60, true);

//		String simulationName = "MagTimeCatalogSimulation_JeanneParams_18yrs_5000_NoHistCat_correctSpont";
////		String simulationName = "MagTimeCatalogSimulation_FelzerAltParams_18yrs_1000_HistCat";
//		double mMin = 2.55;
//		double mMax = 7.85;
//		int numMag = (int)Math.round((mMax-mMin)/0.1)+1;
//		double cumRateAtM5 = 10.0;
//		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(1.0, 1.0, mMin, mMax, numMag);
//		mfd.scaleToCumRate(5.05, cumRateAtM5);
				
		double startTimeYear=2012;
		double durationYears=30;
		double numYearsBinWidth=1.0;
		
//		
//		long forecastStartTime = (long) ((startTimeYear-1970d)*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
////		long histCatStartTime = forecastStartTime;
//		long histCatStartTime = (long) ((1990d-1970d)*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
//		long forecastEndTime =  (long) ((startTimeYear+durationYears-1970d)*ProbabilityModelsCalc.MILLISEC_PER_YEAR);
//		EvenlyDiscretizedFunc rateFunc = getSpontanousEventRateFunction(mfd, histCatStartTime, forecastStartTime, 
//				forecastEndTime, 1000, etasParams.get_k(), etasParams.get_p(), 2.5, etasParams.get_c());
//		double mean =0;
//		for(int i=0;i<rateFunc.size();i++) {
//				mean += rateFunc.getY(i)/(double)rateFunc.size();
//		}
//		double n = 1.0 - mean/mfd.calcSumOfY_Vals();
//		rateFunc.setInfo("n="+(float)n+"\nexpNumSpont="+(mean*durationYears));
//		System.out.println("n="+(float)n+"\nexpNumSpont="+(mean*durationYears));
//		
//		int numForHist = 1000;
//		ETAS_Utils etasUtils = new ETAS_Utils();
//		double deltaHistMillis = (double)(forecastEndTime-forecastStartTime)/(double)numForHist;
//		double deltaHistYears = deltaHistMillis/ProbabilityModelsCalc.MILLISEC_PER_YEAR;
//		System.out.println("deltaHistYears="+deltaHistYears);
//		HistogramFunction rateVsEpochTimeHist = new HistogramFunction((double)forecastStartTime+deltaHistMillis/2.0,(double)forecastEndTime-deltaHistMillis/2.0,numForHist);
//		int numSamples = 1000;
//		double meanNum=0;
//		for(int i=0;i<numSamples;i++) {
//			System.out.println("Working on "+i);
//			long[] eventTimes = etasUtils.getRandomSpontanousEventTimes(mfd, histCatStartTime, forecastStartTime, 
//					forecastEndTime, 10000, etasParams.get_k(), etasParams.get_p(), 2.5, etasParams.get_c());
//			meanNum += eventTimes.length;
//			for(long time:eventTimes)
//				rateVsEpochTimeHist.add((double)time, 1.0/(deltaHistYears*(double)numSamples));
//		}
//		meanNum /= numSamples;
//		System.out.println("meanNum="+meanNum);
//		
//		ArrayList<EvenlyDiscretizedFunc> funcList = new ArrayList<EvenlyDiscretizedFunc>();
//		funcList.add(rateVsEpochTimeHist);
//		funcList.add(rateFunc);
//		ArrayList<PlotCurveCharacterstics> plotCharList = new ArrayList<PlotCurveCharacterstics>();
//		plotCharList.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLACK));
//		plotCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
//		GraphWindow numVsTimeGraph = new GraphWindow(funcList, "Ave histOfAveNumVsTime",plotCharList); 
//		numVsTimeGraph.setX_AxisLabel("Epoch");
//		numVsTimeGraph.setY_AxisLabel("SpontanousRate");

		
		
		
		int numCatalogs = 5000;
		
//		ObsEqkRupList histCat = null;
		ObsEqkRupList histCat = ETAS_Simulator.getHistCatalogFiltedForStatewideCompleteness(startTimeYear);
		
		try {
			magTimeCatalogSimulation(new File(simulationName), mfd, histCat, simulationName, etasParams, startTimeYear, 
					durationYears, numCatalogs, numYearsBinWidth);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	
	public static void magTimeCatalogSimulation(File resultsDir, IncrementalMagFreqDist mfd, List<? extends ObsEqkRupture> histQkList, String simulationName, 
			ETAS_ParameterList etasParams, double startYear, double numYears, int numCatalogs, double binWidthYears)
					throws IOException {
		
		boolean D = false;
		
		ETAS_Utils etas_utils = new ETAS_Utils(System.currentTimeMillis());
		
// System.out.println("1906 Expect: "+getExpectedNumEvents(etasParams.get_k(), etasParams.get_p(), 7.8, 2.5, etasParams.get_c(), 40177d, 40177d+1000*365.25));

		// directory for saving results
		if(!resultsDir.exists()) resultsDir.mkdir();
		
		// set file for writing simulation info & write some preliminary stuff to it
		FileWriter info_fr = new FileWriter(new File(resultsDir, "infoString.txt"));	// TODO this is closed below; why the warning?

		info_fr.write(simulationName+"\n");
		if(histQkList == null)
			info_fr.write("\nhistQkList.size()=null"+"\n");
		else
			info_fr.write("\nnumCatalogs="+numCatalogs+"\n");
		
		info_fr.write("\nETAS Paramteres:\n\n");
		info_fr.write("\nETAS Paramteres:\n\n");
		if(D) System.out.println("\nETAS Paramteres:\n\n");
		for(Parameter param : etasParams) {
			info_fr.write("\t"+param.getName()+" = "+param.getValue()+"\n");
			if(D) System.out.println("\t"+param.getName()+" = "+param.getValue());
		}
		
		info_fr.flush();	// this writes the above out now in case of crash
		
		// Make the list of observed ruptures, plus scenario if that was included
		ArrayList<ETAS_EqkRupture> obsEqkRuptureList = new ArrayList<ETAS_EqkRupture>();
		
		if(histQkList != null) {
			int id=0;
			for(ObsEqkRupture qk : histQkList) {
				ETAS_EqkRupture etasRup = new ETAS_EqkRupture(qk);
				etasRup.setID(id);
				obsEqkRuptureList.add(etasRup);
				id+=1;
			}
			if(D) {
				System.out.println("histQkList.size()="+histQkList.size());
				System.out.println("obsEqkRuptureList.size()="+obsEqkRuptureList.size());
			}
		}
		
		
		// get simulation timespan info
		long simStartTimeMillis = (long)((startYear-1970d)*(double)ProbabilityModelsCalc.MILLISEC_PER_YEAR);
		long simEndTimeMillis = (long)((startYear+numYears-1970d)*(double)ProbabilityModelsCalc.MILLISEC_PER_YEAR);
		
		// Make the MFD x-axis index sampler
		IntegerPDF_FunctionSampler mfdMagIndexSampler = new IntegerPDF_FunctionSampler(mfd.size());
		for(int i=0;i<mfd.size();i++)
			mfdMagIndexSampler.set(i,mfd.getY(i));
		
//		double binWidthYears = 50;
		int numBins = (int)Math.round(numYears/binWidthYears);
		HistogramFunction[] histOfAveNumVsTimeArray=null;
		if(!Double.isNaN(binWidthYears)) {
			histOfAveNumVsTimeArray = new HistogramFunction[numCatalogs];
		}
		
		ArbIncrementalMagFreqDist allEventsMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		ArbIncrementalMagFreqDist spontaneousMagProbDist = new ArbIncrementalMagFreqDist(2.05,8.95, 70);
		
		CalcProgressBar progressBar=null;
		try {
			progressBar = new CalcProgressBar("Num simulations to process ", "junk");
			progressBar.showProgress(true);
		} catch (Throwable t) {
			// headless, don't show it
			progressBar = null;
		}


		for(int catIndex=0; catIndex<numCatalogs; catIndex++) {
			
			if (progressBar != null) progressBar.updateProgress(catIndex, numCatalogs);
//			System.out.print(catIndex+", ");

			if(histOfAveNumVsTimeArray != null)
				histOfAveNumVsTimeArray[catIndex] = new HistogramFunction(binWidthYears/2, numYears-binWidthYears/2,numBins);
			
			System.gc();
			
//			FileWriter simulatedEventsFileWriter = new FileWriter(new File(resultsDir, "simulatedEvents"+catIndex+".txt"));
//			ETAS_CatalogIO.writeEventHeaderToFile(simulatedEventsFileWriter);

			// this will store the simulated aftershocks & spontaneous events (in order of occurrence)
			ObsEqkRupOrigTimeComparator oigTimeComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
			PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, oigTimeComparator);

			// Make list of primary aftershocks for given list of obs quakes 
			if (D) System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
			PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, oigTimeComparator);	
			int testParID=0;	// this will be used to test IDs
			int eventID = obsEqkRuptureList.size();	// start IDs after input events
			for(ETAS_EqkRupture parRup: obsEqkRuptureList) {
				int parID = parRup.getID();
				if(parID != testParID) 
					throw new RuntimeException("problem with ID");
				long rupOT = parRup.getOriginTime();
				double startDay = (double)(simStartTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;	// convert epoch to days from event origin time
				double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
				// get a list of random primary event times, in units of days since main shock
				double[] randomAftShockTimes = etas_utils.getRandomEventTimes(etasParams.get_k(), etasParams.get_p(), parRup.getMag(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c(), startDay, endDay);
				if(randomAftShockTimes.length>0) {
					for(int i=0; i<randomAftShockTimes.length;i++) {
						long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);	// convert to milliseconds
						ETAS_EqkRupture newRup = new ETAS_EqkRupture(parRup, eventID, ot);
						newRup.setParentID(parID);	// TODO don't need this if it's set from parent rup in above constructor
						newRup.setGeneration(1);	// TODO shouldn't need this either since it's 1 plus that of parent (also set in costructor)
						eventsToProcess.add(newRup);
						eventID +=1;
					}
				}
				testParID += 1;				
			}
			if (D) System.out.println("The "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" primary aftershocks");
			info_fr.write("\nThe "+obsEqkRuptureList.size()+" input observed events produced "+eventsToProcess.size()+" primary aftershocks\n");
			info_fr.flush();

			// make the list of spontaneous events, filling in only event IDs and origin times for now
			if (D) System.out.println("Making spontaneous events and times of primary aftershocks...");
			
//			// Approximate Way:
//			double fractionNonTriggered=etasParams.getFractSpont();	// one minus branching ratio TODO fix this; this is not what branching ratio is
//			double expectedNum = mfd.getTotalIncrRate()*fractionNonTriggered*numYears;
//			int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
//			for(int r=0;r<numSpontEvents;r++) {
//				ETAS_EqkRupture rup = new ETAS_EqkRupture();
//				double ot = simStartTimeMillis+etas_utils.getRandomDouble()*(simEndTimeMillis-simStartTimeMillis);	// random time over time span
//				rup.setOriginTime((long)ot);
//				rup.setID(eventID);
//				rup.setGeneration(0);
//				eventsToProcess.add(rup);
//				eventID += 1;
//			}
//			String spEvStringInfo = "Spontaneous Events:\n\n\tAssumed fraction non-triggered = "+fractionNonTriggered+
//					"\n\texpectedNum="+expectedNum+"\n\tnumSampled="+numSpontEvents+"\n";
//			if(D) System.out.println(spEvStringInfo);
//			info_fr.write("\n"+spEvStringInfo);
//			info_fr.flush();
			
			// More Accurate Way:
			ETAS_Utils etasUtils = new ETAS_Utils();
			long histCatStartTime = simStartTimeMillis;
			long[] spontEventTimes;
			if(histQkList==null)
				spontEventTimes = etasUtils.getRandomSpontanousEventTimes(mfd, histCatStartTime, simStartTimeMillis, simEndTimeMillis, 1000, 
						etasParams.get_k(), etasParams.get_p(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c());
			else
				spontEventTimes = etasUtils.getRandomSpontanousEventTimes(mfd, new U3_EqkCatalogStatewideCompleteness(), simStartTimeMillis, 
						simEndTimeMillis, 1000, etasParams.get_k(), etasParams.get_p(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c());

			for(int r=0;r<spontEventTimes.length;r++) {
				ETAS_EqkRupture rup = new ETAS_EqkRupture();
				rup.setOriginTime(spontEventTimes[r]);
				rup.setID(eventID);
				rup.setGeneration(0);
				eventsToProcess.add(rup);
				eventID += 1;
			}
			
			
			
			
			
			if (D) System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")...\n");

			long st = System.currentTimeMillis();

			int numSimulatedEvents = 0;

			info_fr.flush();	// this writes the above out now in case of crash

			while(eventsToProcess.size()>0) {

				ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.

				double mag = mfd.getX(mfdMagIndexSampler.getRandomInt());
				
				rup.setMag(mag);	

				double year = (double)((rup.getOriginTime()-simStartTimeMillis)/ProbabilityModelsCalc.MILLISEC_PER_YEAR);
				if(mag>=5.0 && histOfAveNumVsTimeArray !=null)
					histOfAveNumVsTimeArray[catIndex].add(year, 1.0);

				allEventsMagProbDist.addResampledMagRate(mag, 1.0, true);
				if(rup.getGeneration() == 0)
					spontaneousMagProbDist.addResampledMagRate(mag, 1.0, true);

				// add the rupture to the list
				simulatedRupsQueue.add(rup);	
				numSimulatedEvents += 1;

//				ETAS_CatalogIO.writeEventToFile(simulatedEventsFileWriter, rup);

				long rupOT = rup.getOriginTime();

				int parID = rup.getID();	// rupture is now the parent
				int gen = rup.getGeneration()+1;
				double startDay = 0;	// starting at origin time since we're within the timespan
				double endDay = (double)(simEndTimeMillis-rupOT) / (double)ProbabilityModelsCalc.MILLISEC_PER_DAY;
				double[] eventTimes = etas_utils.getRandomEventTimes(etasParams.get_k(), etasParams.get_p(), rup.getMag(), ETAS_Utils.magMin_DEFAULT, etasParams.get_c(), startDay, endDay);

				if(eventTimes.length>0) {
					for(int i=0; i<eventTimes.length;i++) {
						long ot = rupOT +  (long)(eventTimes[i]*(double)ProbabilityModelsCalc.MILLISEC_PER_DAY);
						ETAS_EqkRupture newRup = new ETAS_EqkRupture(rup, eventID, ot);
						newRup.setGeneration(gen);	// TODO have set in above constructor?
						newRup.setParentID(parID);	// TODO have set in above constructor?
						eventsToProcess.add(newRup);
						eventID +=1;
					}
				}		

			}
			
			if(D) System.out.println("\nLooping over events took "+(System.currentTimeMillis()-st)/1000+" secs\n");
			info_fr.write("\nLooping over events took "+(System.currentTimeMillis()-st)/1000+" secs\n\n");

			int[] numInEachGeneration = ETAS_SimAnalysisTools.getNumAftershocksForEachGeneration(simulatedRupsQueue, 10);
			String numInfo = "Total num ruptures: "+simulatedRupsQueue.size()+"\n";
			numInfo += "Num spontaneous: "+numInEachGeneration[0]+"\n";
			numInfo += "Num 1st Gen: "+numInEachGeneration[1]+"\n";
			numInfo += "Num 2nd Gen: "+numInEachGeneration[2]+"\n";
			numInfo += "Num 3rd Gen: "+numInEachGeneration[3]+"\n";
			numInfo += "Num 4th Gen: "+numInEachGeneration[4]+"\n";
			numInfo += "Num 5th Gen: "+numInEachGeneration[5]+"\n";
			numInfo += "Num 6th Gen: "+numInEachGeneration[6]+"\n";
			numInfo += "Num 7th Gen: "+numInEachGeneration[7]+"\n";
			numInfo += "Num 8th Gen: "+numInEachGeneration[8]+"\n";
			numInfo += "Num 9th Gen: "+numInEachGeneration[9]+"\n";
			numInfo += "Num 10th Gen: "+numInEachGeneration[10]+"\n";

			if(D) System.out.println(numInfo);
			info_fr.write(numInfo+"\n");
//			simulatedEventsFileWriter.close();

//			if(D) {
			if(catIndex==0)
				ETAS_SimAnalysisTools.plotRateVsLogTimeForPrimaryAshocks(simulationName, new File(resultsDir,"logRateDecayPDF_ForAllPrimaryEvents.pdf").getAbsolutePath(), simulatedRupsQueue,
						etasParams.get_k(), etasParams.get_p(), etasParams.get_c());
//			}

		}
		
		if (progressBar != null) progressBar.showProgress(false);

		
		info_fr.close();
	
		if(histOfAveNumVsTimeArray != null) {
			HistogramFunction meanAveNumVsTime = new HistogramFunction(binWidthYears/2, numYears-binWidthYears/2,numBins);
			HistogramFunction meanPlus2stdomAveNumVsTime = new HistogramFunction(binWidthYears/2, numYears-binWidthYears/2,numBins);
			HistogramFunction meanMinus2stdomAveNumVsTime = new HistogramFunction(binWidthYears/2, numYears-binWidthYears/2,numBins);

			for(int i=0;i<meanAveNumVsTime.size();i++) {
				double[] vals = new double[numCatalogs];
				for(int c=0;c<numCatalogs;c++) {
					vals[c] = histOfAveNumVsTimeArray[c].getY(i)/binWidthYears;
				}
				
				double mean = StatUtils.mean(vals);
				double stdom = Math.sqrt(StatUtils.variance(vals)/numCatalogs);
				meanAveNumVsTime.set(i,mean);
				meanPlus2stdomAveNumVsTime.set(i,mean+2*stdom);
				meanMinus2stdomAveNumVsTime.set(i,mean-2*stdom);
			}
			
			// Plot average num M≥5 versus time
			ArrayList<HistogramFunction> funcList = new ArrayList<HistogramFunction>();
			funcList.add(meanAveNumVsTime);
			funcList.add(meanPlus2stdomAveNumVsTime);
			funcList.add(meanMinus2stdomAveNumVsTime);
			ArrayList<PlotCurveCharacterstics> curveCharList = new ArrayList<PlotCurveCharacterstics>();
			curveCharList.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLUE));
			curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			curveCharList.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			GraphWindow numVsTimeGraph = new GraphWindow(funcList, "Ave histOfAveNumVsTime",curveCharList); 
			numVsTimeGraph.setX_AxisLabel("Year");
			numVsTimeGraph.setY_AxisLabel("N(M≥5)");
			numVsTimeGraph.setPlotLabelFontSize(18);
			numVsTimeGraph.setAxisLabelFontSize(16);
			numVsTimeGraph.setTickLabelFontSize(14);
			
			String pathName = new File(resultsDir,"numVsTimeGraph.pdf").getAbsolutePath();
			numVsTimeGraph.saveAsPDF(pathName);

			pathName = new File(resultsDir,"numVsTimeGraph.txt").getAbsolutePath();
			numVsTimeGraph.saveAsTXT(pathName);
		}
			
		// plot MFDs
		allEventsMagProbDist.scale(1.0/(double)(numYears*numCatalogs));
		allEventsMagProbDist.setName("All Simulated Events MFD");
		double totNumSim = allEventsMagProbDist.calcSumOfY_Vals();
		allEventsMagProbDist.setInfo("Total Num = "+totNumSim+"; numSim/numExpected = "+((double)totNumSim/(double)mfd.calcSumOfY_Vals()));
		spontaneousMagProbDist.scale(1.0/(double)(numYears*numCatalogs));
		spontaneousMagProbDist.setName("Spontaneous Simulated Events MFD");
		spontaneousMagProbDist.setInfo("Total Num = "+spontaneousMagProbDist.calcSumOfY_Vals());
		mfd.setName("Target MFD");
		mfd.setInfo("Total Num = "+mfd.calcSumOfY_Vals());
		ArrayList<EvenlyDiscretizedFunc> magProbDists = new ArrayList<EvenlyDiscretizedFunc>();
		magProbDists.add(allEventsMagProbDist);
		magProbDists.add(spontaneousMagProbDist);
		magProbDists.add(mfd);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
		// Plot these MFDs
		GraphWindow incrMFDsGraph = new GraphWindow(magProbDists, "MFDs",plotChars); 
		incrMFDsGraph.setX_AxisLabel("Mag");
		incrMFDsGraph.setY_AxisLabel("Number");
		incrMFDsGraph.setY_AxisRange(1e-5, 1e3);
		incrMFDsGraph.setX_AxisRange(2.5d, 8.5d);
		incrMFDsGraph.setYLog(true);
		incrMFDsGraph.setPlotLabelFontSize(18);
		incrMFDsGraph.setAxisLabelFontSize(16);
		incrMFDsGraph.setTickLabelFontSize(14);

		ArrayList<EvenlyDiscretizedFunc> cumMagProbDists = new ArrayList<EvenlyDiscretizedFunc>();
		cumMagProbDists.add(allEventsMagProbDist.getCumRateDistWithOffset());
		cumMagProbDists.add(spontaneousMagProbDist.getCumRateDistWithOffset());
		cumMagProbDists.add(mfd.getCumRateDistWithOffset());

		ArrayList<PlotCurveCharacterstics> plotCharsCum = new ArrayList<PlotCurveCharacterstics>();
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		plotCharsCum.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));

		// Plot these MFDs
		GraphWindow cumMFDsGraph = new GraphWindow(cumMagProbDists, "Cumulative MFDs",plotCharsCum); 
		cumMFDsGraph.setX_AxisLabel("Mag");
		cumMFDsGraph.setY_AxisLabel("Number");
		cumMFDsGraph.setY_AxisRange(1e-4, 1e4);
		cumMFDsGraph.setX_AxisRange(2.5d, 8.5d);
		cumMFDsGraph.setYLog(true);
		cumMFDsGraph.setPlotLabelFontSize(18);
		cumMFDsGraph.setAxisLabelFontSize(16);
		cumMFDsGraph.setTickLabelFontSize(14);			
				
		String pathName = new File(resultsDir,"incrMFDsGraph.pdf").getAbsolutePath();
		incrMFDsGraph.saveAsPDF(pathName);
		
		pathName = new File(resultsDir,"cumMFDsGraph.pdf").getAbsolutePath();
		cumMFDsGraph.saveAsPDF(pathName);

		pathName = new File(resultsDir,"incrMFDsGraph.txt").getAbsolutePath();
		incrMFDsGraph.saveAsTXT(pathName);
		
		pathName = new File(resultsDir,"cumMFDsGraph.txt").getAbsolutePath();
		cumMFDsGraph.saveAsTXT(pathName);

		if(D)
			ETAS_SimAnalysisTools.writeMemoryUse("Memory at end of simultation");
		
	}
}

