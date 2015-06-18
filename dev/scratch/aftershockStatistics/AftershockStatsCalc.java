/**
 * 
 */
package scratch.aftershockStatistics;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jfree.data.Range;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * @author field
 *
 */
public class AftershockStatsCalc {
	
	public final static double MILLISEC_PER_YEAR = 1000*60*60*24*365.25;
	public final static long MILLISEC_PER_DAY = 1000*60*60*24;

	
	/**
	 * This computes the log-likelihood for the given modified Omori parameters according to 
	 * equation (6) of Ogata (1983; J. Phys. Earth, 31,115-124).
	 * @param k
	 * @param p
	 * @param c
	 * @param relativeEventTimes - in order of occurrence relative to main shock
	 * @return
	 */
	public static double getLogLikelihoodForOmoriParams(double k, double p, double c, double[] relativeEventTimes) {
		double funcA=Double.NaN;
		int n=relativeEventTimes.length;
		double t_beg = relativeEventTimes[0];
		double t_end = relativeEventTimes[n-1];
		if(p == 1)
			funcA = Math.log(t_end+c) - Math.log(t_beg+c);
		else
			funcA = (Math.pow(t_end+c,1-p) - Math.pow(t_beg+c,1-p)) / (1-p);
		double sumLn_t = 0;
		for(double t : relativeEventTimes)
			sumLn_t += Math.log(t+c);
		return n*Math.log(k) - p*sumLn_t - k*funcA;
	}
	
	
	
	/**
	 * This computes the maximum likelihood k values for constrained 
	 * values of p and c as given.
	 * @param p
	 * @param c
	 * @param relativeEventTimes
	 * @return
	 */
	public static double getMaxLikelihood_k(double p, double c, double[] relativeEventTimes) {
		double funcA=Double.NaN;
		int n=relativeEventTimes.length;
		double t_beg = relativeEventTimes[0];
		double t_end = relativeEventTimes[n-1];
		if(p == 1)
			funcA = Math.log(t_end+c) - Math.log(t_beg+c);
		else
			funcA = (Math.pow(t_end+c,1-p) - Math.pow(t_beg+c,1-p)) / (1-p);
		
		return (double)n/funcA;
	}
	

	
	
	/**
	 * This converts the productivity value from "a" to "k"
	 * @param a
	 * @param b
	 * @param magMain
	 * @param magMin
	 * @return
	 */
	public static double convertProductivityTo_k(double a, double b, double magMain, double magMin) {
		return Math.pow(10.0, a+b*(magMain-magMin));
	}
	
	/**
	 * This converts the productivity value from "k" to "a"
	 * @param k
	 * @param b
	 * @param magMain
	 * @param magMin
	 * @return
	 */
	public static double convertProductivityTo_a(double k, double b, double magMain, double magMin) {
		return Math.log10(k) - b*(magMain-magMin);
	}

	/**
	 * This returns the Reasenberg Jones (1989, 1994) expected number of primary aftershocks 
	 * between time tMin and tMax (days) for 
	 * the given arguments.
	 * @param a
	 * @param b
	 * @param magMain
	 * @param magMin
	 * @param p 
	 * @param c - days
	 * @param tMin - beginning of forecast time window (since origin time), in days
	 * @param tMax - end of forecast time window (since origin time), in days
	 * @return
	 */
	public static double getExpectedNumEvents(double a, double b, double magMain, double magMin, double p, double c, double tMinDays, double tMaxDays) {
		double k = convertProductivityTo_k(a, b, magMain, magMin);
		if(p!=1) {
			double oneMinusP= 1-p;
			return (k/oneMinusP)*(Math.pow(c+tMaxDays,oneMinusP) - Math.pow(c+tMinDays,oneMinusP));
		}
		else {
			return k*(c*(tMaxDays-tMinDays) + 0.5*(tMaxDays*tMaxDays-tMinDays*tMinDays));
		}
	}

	
	/**
	 * This returns the poisson probability given the expected number of events
	 * @param expectedNum
	 * @return
	 */
	public static double getPoissonProbability(double expectedNum) {
		return 1.0-Math.exp(-expectedNum);
	}
	
	/**
	 * This returns the maximum-likelihood b-value defined by Aki (1965, Bull. Earthq. Res. Inst., 43, 237-239)
	 * @param rups - obs eqk rupture list
	 * @param magComplete - the magnitude above which no events have gone undetected
	 * @param magPrecision - the degree to which magnitude have been rounded
	 * @return
	 */
	public static double getMaxLikelihood_b_value(ObsEqkRupList rups, double magComplete,
			double magPrecision) {
		double magMean = 0d;
		int num = 0;
		for (ObsEqkRupture rup : rups) {
			if (rup.getMag() >= magComplete) {
				num++;
				magMean += rup.getMag();
			}
		}
		Preconditions.checkState(num > 0, "No ruptures above mc="+magComplete);
		magMean /= (double)num;
		return getMaxLikelihood_b_value(magMean, magComplete, magPrecision);
	}
	
	/**
	 * This does not check for negative values
	 * @param mainShock
	 * @param aftershockList
	 * @return
	 */
	public static double[] getDaysSinceMainShockArray(ObsEqkRupture mainShock, ObsEqkRupList aftershockList) {
		double[] relativeEventTimesDays = new double[aftershockList.size()];
		for(int i=0; i<aftershockList.size();i++) {
			long epochDiff = aftershockList.get(i).getOriginTime()-mainShock.getOriginTime();
			relativeEventTimesDays[i] = (double)(epochDiff) / (double)MILLISEC_PER_DAY;
		}
		return relativeEventTimesDays;
	}
	
	/**
	 * This returns the maximum-likelihood b-value defined by Aki (1965, Bull. Earthq. Res. Inst., 43, 237-239)
	 * @param magMean - mean magnitude above magComplete
	 * @param magComplete - the magnitude above which no events have gone undetected
	 * @param magPrecision - the degree to which magnitude have been rounded
	 * @return
	 */
	public static double getMaxLikelihood_b_value(double magMean, double magComplete, double magPrecision) {
		return Math.log10(Math.E) /(magMean - (magComplete-0.5*magPrecision));
	}
	
	
	private static double[] readAndysFile() {
		
		try {
			BufferedReader buffRead = new BufferedReader(new InputStreamReader(
					AftershockStatsCalc.class.getResourceAsStream("AndysSimulationData.txt")));
			ArrayList<Double> eventTimeList = new ArrayList<Double>();
			String line = buffRead.readLine();
			while (line != null) {
				StringTokenizer tok = new StringTokenizer(line);
				while(tok.hasMoreTokens()) {
					eventTimeList.add(Double.parseDouble(tok.nextToken()));
				}
				line = buffRead.readLine();
			}
			double[] eventTimeArray = new double[eventTimeList.size()];
			for(int i=0;i<eventTimeList.size();i++) {
				eventTimeArray[i] = eventTimeList.get(i);
//				System.out.println(eventTimeArray[i]);
			}
			buffRead.close();
			return eventTimeArray;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void plot2D_PDF(EvenlyDiscrXYZ_DataSet pdf2D, String title,
			String xAxisLabel, String yAxisLabel, String zAxisLabel) {
		CPT cpt=null;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(pdf2D.getMinZ(), pdf2D.getMaxZ());
		} catch (IOException e) {
			e.printStackTrace();
		}
		XYZPlotSpec logLikeSpec = new XYZPlotSpec(pdf2D, cpt, title, xAxisLabel, yAxisLabel, zAxisLabel);
		XYZPlotWindow window_logLikeSpec = new XYZPlotWindow(logLikeSpec, new Range(pdf2D.getMinX(),pdf2D.getMaxX()), new Range(pdf2D.getMinY(),pdf2D.getMaxY()));
	}
	
	
	public static void testAndyCalc() {
		
		double c = 0.05;
		
		double k_min = 17;
		double k_max = 34;
		double k_delta = 0.25;

		double p_min = 0.9; 
		double p_max = 1.15; 
		double p_delta = 0.0125;
		
		int k_num = (int)Math.round((k_max-k_min)/k_delta) + 1;		
		int p_num = (int)Math.round((p_max-p_min)/p_delta) + 1;
		
		if(true) {
			System.out.println("k1\t"+k_min+"\t"+k_max+"\t"+k_num+"\t"+k_delta);
			System.out.println("p1\t"+p_min+"\t"+p_max+"\t"+p_num+"\t"+p_delta);
		}

		
		double[] relativeEventTimes = readAndysFile();

		// x-axis is k and y-axis is p
		EvenlyDiscrXYZ_DataSet xyzLogLikelihood = new EvenlyDiscrXYZ_DataSet(k_num, p_num, k_min, p_min, k_delta, p_delta);
		
		double maxLike=-Double.MAX_VALUE;
		double maxLike_k=Double.NaN;
		double maxLike_p=Double.NaN;
		for(int x=0;x<xyzLogLikelihood.getNumX();x++) {
			for(int y=0;y<xyzLogLikelihood.getNumY();y++) {
				double logLike = getLogLikelihoodForOmoriParams(xyzLogLikelihood.getX(x), xyzLogLikelihood.getY(y), c, relativeEventTimes);
				xyzLogLikelihood.set(x, y, logLike);
				if(logLike>maxLike) {
					maxLike=logLike;
					maxLike_k = xyzLogLikelihood.getX(x);
					maxLike_p = xyzLogLikelihood.getY(y);					
				}
			}
		}

		System.out.println("maxLike_k="+maxLike_k+"\nmaxLike_p="+maxLike_p);
		
		CPT cpt=null;
		try {
			cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(xyzLogLikelihood.getMinZ(), xyzLogLikelihood.getMaxZ());
		} catch (IOException e) {
			e.printStackTrace();
		}
		XYZPlotSpec logLikeSpec = new XYZPlotSpec(xyzLogLikelihood, cpt, "Log Likelihood", "k", "p", "log-likelihood");
		XYZPlotWindow window_logLikeSpec = new XYZPlotWindow(logLikeSpec, new Range(k_min,k_max), new Range(p_min,p_max));
		
		
		// test model assuming b=1
		double b = 1;
		double magMain = 7;	// assumed value for Andy's data
		double magComplete = 5;	// assumed value for Andy's data
		double a_min = convertProductivityTo_a(k_min, b, magMain, magComplete);
		double a_max = convertProductivityTo_a(k_max, b, magMain, magComplete);
		int a_num = k_num;
		ObsEqkRupture dummyMainShock = new ObsEqkRupture();
		dummyMainShock.setMag(magMain);
		dummyMainShock.setOriginTime(0l);
		ObsEqkRupList dummyAftershocks = new ObsEqkRupList();
		for(double relTime:relativeEventTimes) {
			ObsEqkRupture newRup = new ObsEqkRupture();
			long ot = (long)(relTime*(double)MILLISEC_PER_DAY);
			newRup.setOriginTime(ot);
			newRup.setMag(magComplete+1); // anything above magComplete);
			dummyAftershocks.add(newRup);
		}
		
		ReasenbergJonesAftershockModel distArray = new ReasenbergJonesAftershockModel(dummyMainShock, dummyAftershocks, magComplete, b,
				a_min, a_max, a_num, p_min, p_max, p_num, c, c, 1);
		System.out.println("max likelihood gridded k =  "+distArray.getMaxLikelihood_k());
		System.out.println("distArray.getPDF_a():\n"+distArray.getPDF_a());
		System.out.println("distArray.getPDF_p():\n"+distArray.getPDF_p());
		System.out.println("distArray.getPDF_c():\n"+distArray.getPDF_c());
		plot2D_PDF(distArray.get2D_PDF_for_a_and_p(), "PDF for a vs p", "a", "p", "density");
		GutenbergRichterMagFreqDist mfd = distArray.getExpectedNumMFD(3.0, 9.0, 61, 0.0, 7.0);
		System.out.println("distArray.getExpectedNumMFD():\n"+mfd);

		
		// test the maximum likelihood k value for constrained p and c
		double p = distArray.getMaxLikelihood_p();
		ReasenbergJonesAftershockModel distArray2 = new ReasenbergJonesAftershockModel(dummyMainShock, dummyAftershocks, magComplete, b, 
				a_min, a_max, a_num, p, p, 1, c, c, 1);
		System.out.println("2nd max likelihood gridded k =  "+distArray2.getMaxLikelihood_k());
		

		System.out.println("2nd max likelihood analytic k =  "+ getMaxLikelihood_k(distArray2.getMaxLikelihood_p(), c, relativeEventTimes));

	}
	
	public static double getMmaxC(IncrementalMagFreqDist mfd) {
		List<Double> magsAtMax = Lists.newArrayList();
		double max = 0d;
		
		for (Point2D pt : mfd) {
			if (pt.getY() == max)
				magsAtMax.add(pt.getX());
			else if (pt.getY() > max) {
				// start over
				magsAtMax = Lists.newArrayList(pt.getX());
				max = pt.getY();
			}
		}
		
		double mmaxc = 0;
		for (double mag : magsAtMax)
			mmaxc += mag;
		mmaxc /= (double)magsAtMax.size();
		System.out.println("Mmaxc="+(float)mmaxc+" from MFD mode(s): "+Joiner.on(",").join(magsAtMax));
		return mmaxc;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		testAndyCalc();
	}

}
