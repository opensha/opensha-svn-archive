package scratch.UCERF3.erf.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.xyz.ArbDiscrXYZ_DataSet;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;


/**
 * This class does time-dependent earthquake probability calculations for ruptures in a fault system solution (FSS).
 * 
 * TODO:
 * 
 * 1) apply unknown date of last using BPT (also with know open historic interval)
 * 
 * 2) enable changing just the timeSpan and/or aperiodicity
 * 
 * @author field
 *
 */
public class ProbabilityModelsCalc {
	
	public final static double MILLISEC_PER_YEAR = 1000*60*60*24*365.25;
	public final static long MILLISEC_PER_DAY = 1000*60*60*24;
	
	// passed in values:
	FaultSystemSolution fltSysSolution;
	double[] longTermRateOfFltSysRup;			// this has zeros where events were filtered our by the ERF (mags too low); this includes aftershocks
	double aperiodicity;
	TimeSpan timeSpan;

	// computed from passed in values
	FaultSystemRupSet fltSysRupSet;
	int numRups;
	int numSections;
	long startTimeMillis;
	double durationYears;
	
	
	double[] longTermPartRateForSectArray;
	double[] sectionArea;
	
	// The following ave recurrence interval of each rupture conditioned on the fact that it is the next event to occur
	// *_type1 for averaging section recurrence intervals and time since last;
	// *_type2 for averaging section rates and normalized time since last
	double[] aveCondRecurIntervalForFltSysRups_type1; 
	double[] aveCondRecurIntervalForFltSysRups_type2;

//	private int typeCalcForU3_Probs = 1;	// 1 for averaging section recurrence intervals and time since last;  
//											// 2 for averaging section rates and normalized time since last
	
	// for BPT reference calculations (200 year recurrence interval)
	static double refRI = 200;
	static double deltaT = 0.1;
	BPT_DistCalc refBPT_DistributionCalc;	
	
	
	ArbitrarilyDiscretizedFunc bptTimeToPoisCondProbFunc;
	double[] bptNormTimeToPoisCondProbFuncForSect;
	
	double[] sectionGainArray;	// for WG02-type calculations
	boolean[] sectionGainReal;	// for WG02-type calculations
	
	// these global variables are used as diagnostics
	double totRupArea;
	double totRupAreaWithDateOfLast;
	boolean allSectionsHadDateOfLast;

	
	/**
	 * 
	 * @param 
	 */
	public ProbabilityModelsCalc(FaultSystemSolution fltSysSolution, double[] longTermRateOfFltSysRupInERF, double aperiodicity, TimeSpan timeSpan) {
		this.fltSysSolution=fltSysSolution;
		longTermRateOfFltSysRup = longTermRateOfFltSysRupInERF;
		this.aperiodicity = aperiodicity;
		this.timeSpan = timeSpan;
		
		fltSysRupSet = fltSysSolution.getRupSet();
		numRups = fltSysRupSet.getNumRuptures();
		numSections = fltSysRupSet.getNumSections();
		startTimeMillis = timeSpan.getStartTimeInMillis();
		durationYears = timeSpan.getDuration();
		if(!timeSpan.getDurationUnits().equals(timeSpan.YEARS))
			throw new RuntimeException("Duration units must be years");
		
		initializeArrays();
		
		bptTimeToPoisCondProbFunc = getBPT_TimeToPoisCondProbFunc(aperiodicity);
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity, durationYears);
	}
	
	
	/**
	 * This is for tests
	 * @param 
	 */
	public ProbabilityModelsCalc(double aperiodicity, double durationYears) {
		bptTimeToPoisCondProbFunc = getBPT_TimeToPoisCondProbFunc(aperiodicity);
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity, durationYears);
	}

	

	/**
	 * The initializes the following arrays: longTermPartRateForSectArray and sectionArea
	 * 
	 * @return
	 */
	private void initializeArrays() {
		
		// first make longTermPartRateForSectArray[]
		// this can't be from invSol.calcTotParticRateForAllSects() due to ERF rate reductions (e.g., low-mag filtering)
		longTermPartRateForSectArray = new double[numSections];
		for(int r=0; r<numRups; r++) {
			List<Integer> sectIndices = fltSysRupSet.getSectionsIndicesForRup(r);
			for(int s=0;s<sectIndices.size();s++) {
				int sectID = sectIndices.get(s);
				longTermPartRateForSectArray[sectID] += longTermRateOfFltSysRup[r];
			}

		}
		
		// now make sectionArea[]
		sectionArea = new double[numSections];
		for(int s=0;s<numSections;s++) {
			FaultSectionPrefData sectData = this.fltSysRupSet.getFaultSectionData(s);
			sectionArea[s]= sectData.getTraceLength()*sectData.getReducedDownDipWidth();
		}
	}
	
	
	/**
	 * This computes average conditional recurrent intervals for each fault system rup
	 * (the recurrence interval assuming the rup is the next to occur), either by averaging
	 * section recurrence intervals (typeCalc=1) or by computing one over the average section
	 * rate (typeCalc=2), both are weighted by section area.
	 * 
	 * @param typeCalc - set as 1 to average RIs, and 2 to average rates
	 * @return
	 */
	private double[] computeAveCondRecurIntervalForFltSysRups(int typeCalc) {
		// now make aveCondRecurIntervalForFltSysRups[]
		double[] aveCondRecurIntervalForFltSysRups = new double[numRups];
		for(int r=0;r<numRups; r++) {
			List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataForRupture(r);
			double ave=0, totArea=0;
			for(FaultSectionPrefData data:fltData) {
				int sectID = data.getSectionId();
				double area = sectionArea[sectID];
				totArea += area;
				// ave RIs or rates depending on which is set
				if(typeCalc==1)
					ave += area/longTermPartRateForSectArray[sectID];  // this one averages RIs; wt averaged by area
				else if(typeCalc==2)
					ave += longTermPartRateForSectArray[sectID]*area;  // this one averages rates; wt averaged by area
				else 
					throw new RuntimeException("Bad typeCalcForU3_Probs");
			}
			if(typeCalc==1)
				aveCondRecurIntervalForFltSysRups[r] = ave/totArea;	// this one averages RIs
			else
				aveCondRecurIntervalForFltSysRups[r] = 1/(ave/totArea); // this one averages rates
		}
		return aveCondRecurIntervalForFltSysRups;
	}

	
	
	/**
	 * This method returns the average date of last event (epoch milliseconds) for the 
	 * given rupture, representing an average over fault section (weight-averaged by area). 
	 * 
	 *  If onlyIfAllSectionsHaveDateOfLast=true, this will return Long.MIN_VALUE if any 
	 *  of the fault sections lack a date of last event.
	 *  
	 *  If onlyIfAllSectionsHaveDateOfLast=false; faults sections lacking a date of last 
	 *  event will be given one that produces the same probability as from a Poisson model
	 *  (for the conditional rupture RI and specified duration)
	 *  
	 *  The following global variables are also set for further diagnostics:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 *
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public long getAveDateOfLastEventCorrected(int fltSystRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {
		
		// make aveCondRecurIntervalForFltSysRups_type2 array if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type2 == null)
			aveCondRecurIntervalForFltSysRups_type2 = computeAveCondRecurIntervalForFltSysRups(2);

		// compute average date of last event
		double aveExpRI = aveCondRecurIntervalForFltSysRups_type2[fltSystRupIndex];
		double durOverRI = durationYears/aveExpRI;
		double timeWhereBPT_CondProbSameAsPois = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI);
		double defaultAveDateOfLast =  (double)startTimeMillis - Math.round(timeWhereBPT_CondProbSameAsPois*aveExpRI*MILLISEC_PER_YEAR);	// set at time that will give cond prob equiv to poisson

		List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(FaultSectionPrefData data:fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double area = sectionArea[data.getSectionId()];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				sumDateOfLast += (double)dateOfLast*area;
				totRupAreaWithDateOfLast += area;
			}
			else {
				sumDateOfLast += defaultAveDateOfLast*area;
				allSectionsHadDateOfLast = false;
			}
		}
		
		if(allSectionsHadDateOfLast || !onlyIfAllSectionsHaveDateOfLast)
			return Math.round(sumDateOfLast/totRupArea);  // epoch millis
		else
			return Long.MIN_VALUE;
	}
	
	
	/**
	 * This averages the normalized times since last event for each section 
	 * (normalized by long-term section RI), weighted by section area.
	 * 
	 * 	If onlyIfAllSectionsHaveDateOfLast=true, this will return Double.NaN if any 
	 *  of the fault sections lack a date of last event.
	 *  
	 *  If onlyIfAllSectionsHaveDateOfLast=false; faults sections lacking a date of last 
	 *  event will be given one that produces the same section probability as from a Poisson 
	 *  model.
	 *  
	 *  The following global variables are also set for further diagnostics:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 * @return
	 */
	public double getAveNormTimeSinceLastCorrected(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {
		
		if(bptNormTimeToPoisCondProbFuncForSect == null) {
			bptNormTimeToPoisCondProbFuncForSect = new double[numSections];
			for(int s=0;s<numSections;s++) {
				double durOverRI = durationYears*longTermPartRateForSectArray[s];
				bptNormTimeToPoisCondProbFuncForSect[s] = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI); // these are normalized times
			}
		}
		
		List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataForRupture(fltSysRupIndex);
		List<Integer> sectIndexList = fltSysRupSet.getSectionsIndicesForRup(fltSysRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumNormTimeSinceLast = 0;
		for(int s=0;s<sectIndexList.size();s++) {
			FaultSectionPrefData data = fltData.get(s);
			int sectIndex = sectIndexList.get(s);
			long dateOfLast = data.getDateOfLastEvent();
			double area = sectionArea[sectIndex];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				sumNormTimeSinceLast += area*((double)(startTimeMillis-dateOfLast)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sectIndex];
				totRupAreaWithDateOfLast += area;
			}
			else {
				sumNormTimeSinceLast += area*bptNormTimeToPoisCondProbFuncForSect[sectIndex];
				allSectionsHadDateOfLast = false;
			}
		}
		
		if(allSectionsHadDateOfLast || !onlyIfAllSectionsHaveDateOfLast)
			return sumNormTimeSinceLast/totRupArea;
		else
			return Double.NaN;
	}
	
	
	/**
	 * This computes the BPT probability gain using the UCERF3 type2 methodology
	 * (from averaging section rates (rather than RIs) and averaging normalized 
	 * time since last event (rather than date of last event)).
	 * 
	 * This returns Double.NaN if onlyIfAllSectionsHaveDateOfLast=true and one or mare sections
	 * lack date of last event.
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public double getU3_ProbGain2_ForRup(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {
		
		// make aveCondRecurIntervalForFltSysRups_type1 if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type2 == null)
			aveCondRecurIntervalForFltSysRups_type2 = this.computeAveCondRecurIntervalForFltSysRups(2);
		
		// get ave norm time since last
		double aveNormTimeSinceLast = getAveNormTimeSinceLastCorrected(fltSysRupIndex, onlyIfAllSectionsHaveDateOfLast);
		// quit if all having date of last event is not satisfied
		if(aveNormTimeSinceLast == Double.NaN)
			return Double.NaN;

		// now compute and set gain
		if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster?
			return 1.0;
		}
		else {
			if(aveNormTimeSinceLast < 0) {
				throw new RuntimeException("aveNormTimeSinceLast cannot be negative (aveNormTimeSinceLast="+aveNormTimeSinceLast+")");
			}
			double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type2[fltSysRupIndex];
			return computeBPT_ProbGainFast(aveCondRecurInterval, aveNormTimeSinceLast*aveCondRecurInterval, durationYears);					
		}
	}

	
	/**
	 * This computes the BPT probability gain using the UCERF3 type1 methodology
	 * (from averaging section recurrence intervals date of last event).
	 * 
	 * This returns Double.NaN if onlyIfAllSectionsHaveDateOfLast=true and one or mare sections
	 * lack date of last event.
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public double getU3_ProbGain1_ForRup(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {

		// make aveCondRecurIntervalForFltSysRups_type2 if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type1 == null)
			aveCondRecurIntervalForFltSysRups_type1 = this.computeAveCondRecurIntervalForFltSysRups(1);
		
		long aveTimeOfLastMillis = getAveDateOfLastEventCorrected(fltSysRupIndex, onlyIfAllSectionsHaveDateOfLast);
		// quit if all having date of last event is not satisfied
		if(aveTimeOfLastMillis == Long.MIN_VALUE)
			return Double.NaN;
		
		double timeSinceLastYears = (double)(startTimeMillis-aveTimeOfLastMillis)/MILLISEC_PER_YEAR;
		// now compute and set gain
		if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster?
			return 1.0;
		}
		else {
			if(timeSinceLastYears < 0) {		// check for problem values
				if(timeSinceLastYears < -0.1)
					throw new RuntimeException("timeSinceLastYears cannot be negative (timeSinceLastYears="+timeSinceLastYears+")");
				else
					timeSinceLastYears = 0;
			}
			double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type1[fltSysRupIndex];
			return computeBPT_ProbGainFast(aveCondRecurInterval, timeSinceLastYears, durationYears);					
		}
	}
	
	
	/**
	 * This returns the probability gain computed using the WG02 methodology, where the probability
	 * gain of each sections is averaged, weighted by section area (actually, this should be weighted
	 * by moment rate, but tests show no difference).
	 * 
	 * This returns Double.NaN if onlyIfAllSectionsHaveDateOfLast=true and one or mare sections
	 * lack date of last event.
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public double getWG02_ProbGainForRup(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {
		
		// first compute the gains for each fault section if it does not exist
		if(sectionGainArray==null) {
			sectionGainArray = new double[numSections];
			sectionGainReal = new boolean[numSections];
			for(int s=0; s<numSections;s++) {
				long timeOfLastMillis = fltSysRupSet.getFaultSectionData(s).getDateOfLastEvent();
				if(timeOfLastMillis != Long.MIN_VALUE) {
					double timeSinceLastYears = ((double)(startTimeMillis-timeOfLastMillis))/MILLISEC_PER_YEAR;
					double refTimeSinceLast = timeSinceLastYears*refRI*longTermPartRateForSectArray[s];
					double refDuration = durationYears*refRI*longTermPartRateForSectArray[s];
					double prob_bpt = refBPT_DistributionCalc.getSafeCondProb(refTimeSinceLast, refDuration);
//					double prob_pois = 1-Math.exp(-durationYears*longTermPartRateForSectArray[s]);
					double prob_pois = durationYears*longTermPartRateForSectArray[s];	// this is there exact calculation, which is a bit different for long durations
					sectionGainArray[s] = prob_bpt/prob_pois;
					sectionGainReal[s]=true;
				}
				else {
					sectionGainArray[s] = 1.0;
					sectionGainReal[s]=false;
				}
			}			
		}
		
		// now compute weight-average gain for rupture
		double totalWt=0;
		double sumGains = 0;
		boolean noneAreReal = true;
		for(int sect : fltSysRupSet.getSectionsIndicesForRup(fltSysRupIndex)) {
//test				double wt = sectionArea[sect]*this.fltSysRupSet.getSlipRateForSection(sect);
			double wt = sectionArea[sect];
			totalWt += wt;
			sumGains += sectionGainArray[sect]*wt;
			if(sectionGainReal[sect] == false && onlyIfAllSectionsHaveDateOfLast) {
				return Double.NaN;
			}
			if(sectionGainReal[sect] == true) {
				noneAreReal=false;
			}
		}
		if(noneAreReal)
			return 1d;
		else
			return sumGains/totalWt;
	}
	
	
	
	
	/**
	 * This returns a function giving the time since last event that will give 
	 * the same conditional probability as the Poisson model.  This depends on 
	 * aperiodicity (given) and on forecast_duration/recur_interval, the latter
	 * being the x-axis of the returned function.  This function is good for 
	 * forecast_duration/recur_interval values between 1e-12 and 1e2, but note that
	 * the latter is ridiculous because several BPT events would occur when the duration
	 * is 100 times the recurrence interval.  In UCERF3 the lowest RI is about 20 years, 
	 * so in simulations where duration is 1 year we're talking about values of 0.05, which
	 * is a safe level.  For 30-year forecasts, meaning the duration is 1.5 times the RI of 20
	 * years, implies a significant probability of a second event from a BPT perspective.
	 * Note also that this function returns zero when forecast_duration/recur_interval
	 * gets above about 1.0 because the Poisson probability becomes below the BPT 
	 * conditional probability; returning 0 for these cases (meaning the event just occurred) gives 
	 * the closest to the Poisson probability (all other values would have higher probabilities).
	 * @param aperiodicity
	 */
	public static ArbitrarilyDiscretizedFunc getBPT_TimeToPoisCondProbFunc(double aperiodicity) {
		
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		double logLowDurOverRI = -3;
		double logHighDurOverRI = 2;
		int numDurOverRI = 201;
		double deltaLogDurOverRI = (logHighDurOverRI-logLowDurOverRI)/(numDurOverRI-1);

		ArrayList<Double> durOverRI_List = new ArrayList<Double>();
		ArrayList<Double> distOutList = new ArrayList<Double>();
		
		for(int i=0;i<numDurOverRI;i++) {
			double durOverRI = Math.pow(10,logLowDurOverRI+i*deltaLogDurOverRI);
			durOverRI_List.add(durOverRI);
			double ri = 100;
			double dur = ri*durOverRI;
			double poisProb = 1-Math.exp(-dur/ri);
			BPT_DistCalc bptDistCalcAlt = new BPT_DistCalc();
			double deltaT = 0.1;
			int numPts = (int)Math.round((7*ri+dur)/deltaT);
			bptDistCalcAlt.setAll(ri, aperiodicity, deltaT, numPts, dur);
			double distOut;
			double probAtZeroTimeSinceLast = bptDistCalcAlt.getCondProb(0, dur);
			if(probAtZeroTimeSinceLast>=poisProb) {
				distOutList.add(0.0);
				// now add zero at the last point and break
				durOverRI_List.add(Math.pow(10, logHighDurOverRI));
				distOutList.add(0.0);
				break;
			}
			else {
					distOut = bptDistCalcAlt.getCondProbFunc(dur).getFirstInterpolatedX(poisProb)/ri;
					distOutList.add(distOut);
			}
		}
		
		// add a super low value with same dist out as first (to cover all cases)
		func.set(1e-12, distOutList.get(0));
		for(int i=0;i<distOutList.size();i++)
			func.set(durOverRI_List.get(i), distOutList.get(i));

//		System.out.println(func);
		func.setName("aperiodicity="+aperiodicity);
		return func;
	}
	
	
	
	
	
	
	
	
	/**
	 *   This returns an xyz function giving the time since last event that will produce the same probability as
	 *   as when the date is unknown and there is a historic open interval, as a function of log10(duration/mean)
	 *   on the x-axis and log10(histOpenInterval/mean) on the y axis, where mean is the mean recurrence interval.
	 *   
	 *   We now want to get the time since last that gives the same probability as condProbForUnknownTimeSinceLast.
	 *   The problem is there may be several x-axis values that satisfy this (at some level of precision), and we 
	 *   have to choose one.  For example, as duration increases, eventually the probability goes to ~1.0 at more
	 *   times since last (function goes flat), and is 1.0 at all x-axis values for extremely long durations.  
	 *   Similarly, the conditional probability function goes flat for very large historical open intervals.
	 *   
	 *   Options include:
	 *   
	 *   a) Choose the smallest time since last that satisfies the probability (this goes to zero at high duration/mean,
	 *      and the function seems to drop abruptly to zero at some point, which could be a problem if averaging time-since 
	 *      last over neighboring subsections)
	 *   
	 *   b) choose a mean or median among all x-axis values that are within the threshold (not good because this depends 
	 *      on x-axis length, which is arbitrary)
	 *   
	 *   The approach here does the following:
	 *   
	 *   1) choose the lowest time since last from the viable range (where probabilities are within 
	 *      probThresholdForMinMaxTime of the target value)
	 *   
	 *   2) if this exceeds the historical open interval, set time since last as the historic open interval
	 *   
	 *   3) to prevent time since last from dropping to zero with increasing duration, we cap it at the 
	 *      time since last for the lowest duration that produces a target probability of 1.0 
	 *      (within probNearOneThreshold).  This option can be turned off by setting
	 *      applyProbNearOneThreshold = false.
	 *   
	 */

	public static EvenlyDiscrXYZ_DataSet getEquivLastEventTimeForHistOpenIntervalXYZ_Func(double aperiodicity,boolean verbose) {

		boolean applyProbNearOneThreshold=true;
		double probNearOneThreshold=0.001;

		double probThresholdForMinMaxTime = 1e-5;

		// for BPT calculator:
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		double mean = 20;
		double deltaX = 0.01;	// 0.1 years
		int numPoints = (int)Math.round(12*mean/deltaX);

		// for data:
		double minLogDurOverMean = -2;
		double maxLogDurOverMean = Math.log10(5);
		int numLogDurOverMean = 51;
		double deltaLogDurOverMean = (maxLogDurOverMean-minLogDurOverMean)/(numLogDurOverMean-1);

		double minLogHistOpenIntOverMean = -2;
		double maxLogHistOpenIntOverMean = Math.log10(5);;
		int numLogHistOpenIntOverMean = 51;
		double deltaLogHistOpenIntOverMean = (maxLogHistOpenIntOverMean-minLogHistOpenIntOverMean)/(numLogHistOpenIntOverMean-1);

		EvenlyDiscrXYZ_DataSet xyzDataCondProbForUnknown=null;
		if(verbose) {
			System.out.println("mean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb\tnormEquivTimeSinceLast\ttestRatio\tproblem1\tproblem2\tproblem3\tminTimeSince\tmaxTimeSince\tratio");
			xyzDataCondProbForUnknown = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);

		}

		// this is what we will return
		EvenlyDiscrXYZ_DataSet xyzDataEquivNormTimeSinceLast = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);

		double smallDurNormTimeSinceLast=Double.NaN;
		for(int j=0;j<numLogHistOpenIntOverMean;j++) {
			double logHistOpenIntOverMean = minLogHistOpenIntOverMean + j*deltaLogHistOpenIntOverMean;
			double histOpenIntOverMean = Math.pow(10,logHistOpenIntOverMean);
			double histOpenInterval = histOpenIntOverMean*mean;
			boolean probNearOne=false;
			double timeSinceLastAtFirstProbNearOne=Double.NaN;
			for(int i=0;i<numLogDurOverMean;i++) {
				double logDurOverMean = minLogDurOverMean + i*deltaLogDurOverMean;
				double durOverMean = Math.pow(10,logDurOverMean);
				double duration = durOverMean*mean;

				if(verbose)
					System.out.print(mean+"\t"+(float)duration+"\t"+(float)histOpenInterval+"\t"+(float)logDurOverMean+"\t"+(float)logHistOpenIntOverMean);

				bptCalc.setAllParameters(mean, aperiodicity, deltaX, numPoints, duration, histOpenInterval);		
				double condProbForUnknownTimeSinceLast = bptCalc.getSafeCondProbForUnknownTimeSinceLastEvent();
				EvenlyDiscretizedFunc condProbFunc = bptCalc.getSafeCondProbFunc();


				double equivTimeSinceLast= Double.NaN;
				boolean problem=false;
				if(condProbForUnknownTimeSinceLast<=condProbFunc.getY(0)) {
					if(condProbFunc.getY(0)-condProbForUnknownTimeSinceLast > probThresholdForMinMaxTime)
						throw new RuntimeException("Problem");
					equivTimeSinceLast=condProbFunc.getX(0);
					problem=true;
				}
				else if(condProbForUnknownTimeSinceLast>=condProbFunc.getY(condProbFunc.getNum()-1)) {
					if(condProbForUnknownTimeSinceLast-condProbFunc.getY(condProbFunc.getNum()-1) > probThresholdForMinMaxTime)
						throw new RuntimeException("Problem");
					equivTimeSinceLast=condProbFunc.getX(condProbFunc.getNum()-1);
					problem=true;
				}
				else {
					equivTimeSinceLast = condProbFunc.getFirstInterpolatedX(condProbForUnknownTimeSinceLast);
				}

				// Find the minimum and maximum values from the function (that all have the same cond prob)
				double minTimeSince = equivTimeSinceLast;
				double maxTimeSince = equivTimeSinceLast;
				for(int z=0;z<condProbFunc.getNum();z++) {
					double prob = condProbFunc.getY(z);
					double fractDiff = Math.abs((prob-condProbForUnknownTimeSinceLast)/condProbForUnknownTimeSinceLast);
					if(fractDiff<probThresholdForMinMaxTime) {
						double timeSince = condProbFunc.getX(z);
						if(minTimeSince>timeSince) minTimeSince=timeSince;
						if(maxTimeSince<timeSince) maxTimeSince=timeSince;
					}
				}

				// set at minimum value
				equivTimeSinceLast = minTimeSince;
				double normEquivTimeSinceLast = equivTimeSinceLast/mean;

				// check whether it's in the unsafe range
				double safeTimeSinceLast = bptCalc.getSafeTimeSinceLastCutoff();
				boolean problem2=false;
				if(equivTimeSinceLast>safeTimeSinceLast) {
					problem2 = true;
					throw new RuntimeException("time since last in unsafe range");
				}

				// don't let time since last be less that the historic open interval
				boolean problem3=false;
				if(normEquivTimeSinceLast<histOpenIntOverMean) {
					normEquivTimeSinceLast=histOpenIntOverMean;
					equivTimeSinceLast=normEquivTimeSinceLast*mean;
					if(equivTimeSinceLast*1.02<minTimeSince || equivTimeSinceLast>1.02*maxTimeSince)	// don't worry about small deviations
						throw new RuntimeException("Problem: "+minTimeSince+" ?>? "+equivTimeSinceLast+" ?>? "+maxTimeSince);
					problem3=true;
				}

				// check to see if probability is near 1.0 (and if so, give it date of last at the first occurrence of this, 
				// rather than letting time since last values go to zero)
				if(applyProbNearOneThreshold) {
					if(!probNearOne) {
						double diff = Math.abs(1.0-condProbForUnknownTimeSinceLast);	// check and set if it is
						if(diff<probNearOneThreshold) {
							probNearOne=true;
							timeSinceLastAtFirstProbNearOne = equivTimeSinceLast;
						}
					}
					else {
						equivTimeSinceLast=timeSinceLastAtFirstProbNearOne;
						normEquivTimeSinceLast = equivTimeSinceLast/mean;
					}						
				}

				if(verbose) {
					// This was a check to see if timeSinceLast for smallest duration could be applied to other durations (if same conditional prob); the result is it can't
					if(i==0) {
						smallDurNormTimeSinceLast = normEquivTimeSinceLast;
					}
					double smallDurTimeSinceLast = smallDurNormTimeSinceLast*mean;
					if(smallDurTimeSinceLast>condProbFunc.getMaxX()) smallDurTimeSinceLast=condProbFunc.getMaxX();
					double testProb = condProbFunc.getInterpolatedY(smallDurTimeSinceLast);
					double testRatio = testProb/condProbForUnknownTimeSinceLast;


					System.out.println("\t"+condProbForUnknownTimeSinceLast+"\t"+(float)normEquivTimeSinceLast+"\t"+(float)testRatio+
							"\t"+problem+"\t"+problem2+"\t"+problem3+"\t"+minTimeSince/mean+"\t"+maxTimeSince/mean+"\t"+(minTimeSince/maxTimeSince));


					xyzDataCondProbForUnknown.set(i, j, condProbForUnknownTimeSinceLast);	
					
				}
				
				xyzDataEquivNormTimeSinceLast.set(i, j, normEquivTimeSinceLast);
			}
		}

		if(verbose) {
			CPT cpt_prob=null;
			CPT cpt_normRI=null;
			try {
				cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1.0);
				cpt_normRI = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, xyzDataEquivNormTimeSinceLast.getMaxZ());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProbForUnknown, cpt_prob, "CondProbForUnknownLast", "LogNormDuration", "LogNormHistOpenInt", "Probability");
			XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);

			XYZPlotSpec spec_normRI = new XYZPlotSpec(xyzDataEquivNormTimeSinceLast, cpt_normRI, "EquivNormTimeSinceLast", "LogNormDuration", "LogNormHistOpenInt", "NormRI");
			XYZPlotWindow window_normRI = new XYZPlotWindow(spec_normRI);
			//			wind.panel.saveAsPNG("/tmp/fig.png");
			//			wind.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		}

		return xyzDataEquivNormTimeSinceLast;
	}


	/**
	 * This is made fast by using a reference calculator (with a reference RI), rather than
	 * redoing the calculation each time 
	 * @param aveRecurInterval
	 * @param aveTimeSinceLastYears
	 * @param duration
	 * @return
	 */
	public double computeBPT_ProbGainFast(double aveRecurInterval, double aveTimeSinceLastYears, double duration) {
		double refTimeSinceLast = aveTimeSinceLastYears*refRI/aveRecurInterval;
		double refDuration = duration*refRI/aveRecurInterval;
		double prob_bpt= refBPT_DistributionCalc.getSafeCondProb(refTimeSinceLast, refDuration);
		double prob_pois = 1-Math.exp(-duration/aveRecurInterval);
		return prob_bpt/prob_pois;
	}


	/**
	 * This creates a reference BPT distribution calculator for the given aperiodicity and duration
	 * (the calculator uses refRI and deltaT) 
	 * @param bpt_Aperiodicity
	 * @param durationInYears
	 * @return
	 */
	protected static BPT_DistCalc getRef_BPT_DistCalc(double bpt_Aperiodicity, double durationInYears) {
		int numPts = (int)Math.round((9*refRI+durationInYears)/deltaT);
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		bptCalc.setAll(refRI, bpt_Aperiodicity, deltaT, numPts, durationInYears);
		return bptCalc;
	}

	
	
	/**
	 * This tests computeBPT_ProbGainFast(*).
	 * 
	 * Cases that fail here are not problematic (all pass at 0.01 (1%) threshold), and
	 * only for aperiodicity>=0.7, which we probably won't use.
	 */
	public static void testComputeBPT_ProbGainFast() {
		
		double testThresh = 0.001;
		
		boolean probFound = false;

		double[] durationArray = {1, 15, 30, 100};
		double[] aperArray = {0.1, 0.2,0.5,0.7,0.8,0.9};
		double[] ri_array = {20,2e2,2e3,2e4,2e5,2e6,2e7,2e8};
		double[] normTimeSinceLastArray = {1e-2,1e-1,1,3,6};
		
		for(double duration:durationArray) {
			for(double aper:aperArray) {
				ProbabilityModelsCalc calc = new ProbabilityModelsCalc(aper, duration);
				for(double ri:ri_array) {
					for(double norm_tsl:normTimeSinceLastArray) {
						double timeSinceLast = norm_tsl*ri;
						
//						System.out.println(ri+"\t"+timeSinceLast+"\t"+norm_tsl+"\t"+aper+"\t"+duration);
						
						double prob_gain_fast = calc.computeBPT_ProbGainFast(ri, timeSinceLast, duration);
						
						// test against slower calculation
						BPT_DistCalc bptDistCalcAlt = new BPT_DistCalc();
						double deltaT_Alt = deltaT*ri/refRI;
						int numPts = (int)Math.round(7*refRI/deltaT);
						bptDistCalcAlt.setAll(ri, aper, deltaT_Alt, numPts);
						double poisProb = 1-Math.exp(-duration/ri);
						double prob_gain = bptDistCalcAlt.getSafeCondProb(timeSinceLast, duration)/poisProb;
						
						double safeTimeSinceLast = bptDistCalcAlt.getSafeTimeSinceLastCutoff();
						
						double ratio = prob_gain_fast/prob_gain;
						if(prob_gain_fast < 1e-20 && prob_gain < 1e-20) ratio = 1;
						
//						if(duration>safeTimeSinceLast)
//							System.out.println((float)ratio+"\t"+ri+"\t"+timeSinceLast+"\t"+aper+"\t"+duration+"\t"+
//								(float)prob_gain_fast+"\t"+(float)prob_gain+"\t"+safeTimeSinceLast+"\t"+poisProb);
						
						if(ratio>1+testThresh || ratio<1-testThresh) {
							System.out.println((float)ratio+"\t"+ri+"\t"+timeSinceLast+"\t"+norm_tsl+"\t"+aper+"\t"+duration+"\t"+
								(float)prob_gain_fast+"\t"+(float)prob_gain+"\t"+safeTimeSinceLast+"\t"+(float)poisProb);
							probFound = true;
						}
					}
				}
			}
		}
		System.out.println("probFound="+probFound);
	}
	
	/**
	 * This tests how close the BPT conditional probability is to Poisson when the former is based
	 * on the distance out from the getBPT_TimeToPoisCondProb(aper) function.  The two are the same
	 * except in cases where they are expected to differ: when duration is 1.5 to 2 times the recurrence
	 * interval, the BPT probability is 25% to 16% higher than the Poisson (and can't get any closer).
	 * 
	 * This also tests that getBPT_TimeToPoisCondProb(aper) only depends on the ratio of duration/ri,
	 * and that the fast way of calculating things (by scaling ri and duration to a reference) gives 
	 * the same result as the slower way.
	 */
	public static void testTimeToPoisCondProb() {
		double[] ri_array = {10,1e2,1e3,1e4,1e5,1e6,1e7,1e8}; 	// recurrence intervals to loop over
//		double[] aperArray = {0.2};					// aperiodicities to loop over
		double[] aperArray = {0.2,0.4,0.6,0.8};					// aperiodicities to loop over
		
		double logLowDurOverRI = -2;
		double logHighDurOverRI = 1;
		int numDurOverRI = 31;
		double deltaLogDurOverRI = (logHighDurOverRI-logLowDurOverRI)/(numDurOverRI-1);
		
		System.out.println("bpt/pois\tdistOut\tdurOverRI\tri\tduration\taper\tpoisProb\tbptProb");

		for(double aper:aperArray) {
			ArbitrarilyDiscretizedFunc distOutFunc = getBPT_TimeToPoisCondProbFunc(aper);
			for(double ri:ri_array) {
				for(int i=0;i<numDurOverRI;i++) {
					double durOverRI = Math.pow(10,logLowDurOverRI+i*deltaLogDurOverRI);
					double duration = durOverRI*ri;

					// the fast way using reference RI:
					double refDuration = duration*refRI/ri;
					BPT_DistCalc bptCalc = getRef_BPT_DistCalc(aper, refDuration);
					double poisProbFast = 1-Math.exp(-refDuration/refRI);
					double distOutFast = distOutFunc.getInterpolatedY(refDuration/refRI);
					double bptEquivProbFast = bptCalc.getCondProb(refRI*distOutFast, refDuration);

					// the slower calculation
					double poisProb = 1-Math.exp(-duration/ri);
					BPT_DistCalc bptDistCalcAlt = new BPT_DistCalc();
					double deltaT_Alt = deltaT*ri/refRI;
					int numPts = (int)Math.round((7*ri+duration)/deltaT_Alt);
					bptDistCalcAlt.setAll(ri, aper, deltaT_Alt, numPts, duration);
					double distOut = distOutFunc.getInterpolatedY(duration/ri);
					double bptEquivProbAlt = bptDistCalcAlt.getCondProb(ri*distOut, duration);
					
					// this tests that fast calc is equivalent to slow calc
					double ratio = bptEquivProbFast/bptEquivProbAlt;
					if(ratio>1.001 || ratio <0.999)
						throw new RuntimeException("fast way not same as slow for BPT Prob; ratio="+ratio);
					
					ratio = distOutFast/distOut;
//					System.out.println(distOut+"\t"+distOutFast);
					if(ratio>1.001 || ratio <0.999)
						throw new RuntimeException("fast way not same as slow for distOut; ratio="+ratio);

					// we want the first column to be 1.0 (if possible)
					System.out.println((float)(bptEquivProbFast/poisProb)+"\t"+(float)distOut+"\t"+(float)durOverRI+"\t"+ri+"\t"+duration+"\t"+aper+"\t"+
							+(float)poisProb+"\t"+(float)bptEquivProbFast);
				}
			}
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		testComputeBPT_ProbGainFast();
//		testTimeToPoisCondProb();
		
		getEquivLastEventTimeForHistOpenIntervalXYZ_Func(0.1, false);

	}

}
