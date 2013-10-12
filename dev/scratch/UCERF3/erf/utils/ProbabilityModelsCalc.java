package scratch.UCERF3.erf.utils;

import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
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
	 * This creates a reference BPT distribution calculator
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
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
