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
 * This class does various time-dependent earthquake probability calculations for ruptures in a fault system solution (FSS).
 * 
 * TODO:
 * 
 * 0) implement the getU3_ProbGain2_ForRup method
 * 
 * 1) junit tests
 * 
 * 2) improve efficiency?
 * 
 * 3) remove the "OLD*" methods here, as they were experimental and are no longer needed.
 * 
 * 4) enable just changing just the timeSpan and/or aperiodicity (would require setting arrays fields to null)
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
	double[] aveCondRecurIntervalForFltSysRups_type1; //  for averaging section recurrence intervals and time since last;
	double[] aveCondRecurIntervalForFltSysRups_type2; //  for averaging section rates and normalized time since last

	// for BPT reference calculator (200 year recurrence interval); this is used for efficiency
	static double refRI = 200;
	static double deltaT = 0.1;
	BPT_DistCalc refBPT_DistributionCalc;	
	
	// Normalized CDF function used for when looping over possible dates of last event
	static double max_time_for_normBPT_CDF=5;
	static int num_for_normBPT_CDF=501;
	EvenlyDiscretizedFunc normBPT_CDF;
	
	// this is for getting the BPT time since last that is equivalent to the poisson probability TODO no longer needed?
	ArbitrarilyDiscretizedFunc OLDbptTimeToPoisCondProbFunc;
	double[] OLDbptNormTimeToPoisCondProbFuncForSect;
	
	// this is for getting equivalent date of last event when we only know the open interval
	EvenlyDiscrXYZ_DataSet equivLastEventTimeForHistOpenInterval_XYZ_Func;	// TODO no longer used?
	
	// these are for pre-computed probabilities when date of last event is unknown, and as a 
	// function of duration/mean (x-axis) and histOpenInterval/mean (y-axis)
	EvenlyDiscrXYZ_DataSet condProbForUnknownDateOfLast_xyzData;
	final static double minLogDurOverMean = -2;
	final static double maxLogDurOverMean = Math.log10(5);
	final static int numLogDurOverMean = 51;
	final static double deltaLogDurOverMean = (maxLogDurOverMean-minLogDurOverMean)/(numLogDurOverMean-1);
	final static double minLogHistOpenIntOverMean = -2;
	final static double maxLogHistOpenIntOverMean = Math.log10(5);;
	final static int numLogHistOpenIntOverMean = 51;
	final static double deltaLogHistOpenIntOverMean = (maxLogHistOpenIntOverMean-minLogHistOpenIntOverMean)/(numLogHistOpenIntOverMean-1);

	
	double[] sectionGainArray;	// for WG02-type calculations
	boolean[] sectionGainReal;	// for WG02-type calculations
	
	// these global variables are used as diagnostics
	double totRupArea;
	double totRupAreaWithDateOfLast;
	boolean allSectionsHadDateOfLast;
	boolean noSectionsHadDateOfLast;

	
	/**
	 * This is the main constructor.  Note that longTermRateOfFltSysRupInERF is provided, rather than 
	 * using what's obtained as fltSysSolution.getRateForAllRups(), because the ERF has filtered out
	 * small events (TODO not entirely sure this is necessary; depends on whether small sections should be
	 * included in total section rates; the same filtering could also be done here). 
	 * 
	 * @param fltSysSolution
	 * @param longTermRateOfFltSysRupInERF	- this rate includes aftershocks
	 * @param aperiodicity - aperiodicity of the BPT model
	 * @param timeSpan - 
	 * 
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
		
		OLDbptTimeToPoisCondProbFunc = OLDgetBPT_TimeToPoisCondProbFunc(aperiodicity);
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity, durationYears);
		
		// set normBPT_CDF
		BPT_DistCalc tempCalc = new BPT_DistCalc();
		double delta = max_time_for_normBPT_CDF/(num_for_normBPT_CDF-1);
		tempCalc.setAll(1.0, aperiodicity, delta, num_for_normBPT_CDF);	// TODO check this discretization and overall look
		normBPT_CDF=tempCalc.getCDF();

	}
	
	
	/**
	 * This is for tests
	 * @param 
	 */
	public ProbabilityModelsCalc(double aperiodicity, double durationYears) {
		OLDbptTimeToPoisCondProbFunc = OLDgetBPT_TimeToPoisCondProbFunc(aperiodicity);
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity, durationYears);
		
		// set normBPT_CDF
		BPT_DistCalc tempCalc = new BPT_DistCalc();
		double delta = max_time_for_normBPT_CDF/(num_for_normBPT_CDF-1);
		tempCalc.setAll(1.0, aperiodicity, delta, num_for_normBPT_CDF);	// TODO check this discretization and overall look
		normBPT_CDF=tempCalc.getCDF();

	}

	

	/**
	 * The initializes the following arrays: longTermPartRateForSectArray and sectionArea
	 * 
	 * @return
	 */
	private void initializeArrays() {
		
		// first make longTermPartRateForSectArray[]
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
	public long OLDgetAveDateOfLastEventCorrected(int fltSystRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {
		
		// make aveCondRecurIntervalForFltSysRups_type2 array if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type2 == null)
			aveCondRecurIntervalForFltSysRups_type2 = computeAveCondRecurIntervalForFltSysRups(2);

		// compute average date of last event
		double aveExpRI = aveCondRecurIntervalForFltSysRups_type2[fltSystRupIndex];
		double durOverRI = durationYears/aveExpRI;
		double timeWhereBPT_CondProbSameAsPois = OLDbptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI);
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
	 * This method returns last-event date (epoch milliseconds) averaged over fault sections
	 * that have such data (and weighted by section area). 
	 * 
	 *  Long.MIN_VALUE is returned in none of the fault sections had a date of last event 
	 *  
	 *  The following global variables are also set for further diagnostics:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 * 		boolean noSectionsHadDateOfLast
	 *
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public long getAveDateOfLastEventWhereKnown(int fltSystRupIndex) {
		
		List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(FaultSectionPrefData data:fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double area = sectionArea[data.getSectionId()];
			totRupArea+=area;
			if(dateOfLast != Long.MIN_VALUE) {
				sumDateOfLast += (double)dateOfLast*area;
				totRupAreaWithDateOfLast += area;
			}
			else {
				allSectionsHadDateOfLast = false;
			}
		}
		if(totRupAreaWithDateOfLast>0.0)
			return Math.round(sumDateOfLast/totRupAreaWithDateOfLast);  // epoch millis
		else {
			noSectionsHadDateOfLast=true;
			return Long.MIN_VALUE;
		}
	}

	
	
	public double OLDgetBPT_TimeSinceLastThatIsEquivToOpenInterval(double recurInteval, double histOpenInterval) {
		double log10_NormalizedDuration = Math.log10(durationYears/recurInteval);
		double log10_NormalizedHistOpenInt = Math.log10(histOpenInterval/recurInteval);
		
		// make xyz function if it's null
		if(equivLastEventTimeForHistOpenInterval_XYZ_Func == null) {
			equivLastEventTimeForHistOpenInterval_XYZ_Func = OLD3getEquivLastEventTimeForHistOpenIntervalXYZ_Func(aperiodicity,false);
		}
		
		// check the range
		if(log10_NormalizedDuration<minLogDurOverMean)
			log10_NormalizedDuration=minLogDurOverMean;
		else if(log10_NormalizedDuration>maxLogDurOverMean)
			log10_NormalizedDuration=maxLogDurOverMean;

		if(log10_NormalizedHistOpenInt<minLogHistOpenIntOverMean)
			log10_NormalizedHistOpenInt=minLogHistOpenIntOverMean;
		else if(log10_NormalizedHistOpenInt>maxLogHistOpenIntOverMean)
			log10_NormalizedHistOpenInt=maxLogHistOpenIntOverMean;

		return equivLastEventTimeForHistOpenInterval_XYZ_Func.bilinearInterpolation(log10_NormalizedDuration, log10_NormalizedHistOpenInt)*recurInteval;
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
	public double OLDgetAveNormTimeSinceLastCorrected(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {
		
		if(OLDbptNormTimeToPoisCondProbFuncForSect == null) {
			OLDbptNormTimeToPoisCondProbFuncForSect = new double[numSections];
			for(int s=0;s<numSections;s++) {
				double durOverRI = durationYears*longTermPartRateForSectArray[s];
				OLDbptNormTimeToPoisCondProbFuncForSect[s] = OLDbptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI); // these are normalized times
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
				sumNormTimeSinceLast += area*OLDbptNormTimeToPoisCondProbFuncForSect[sectIndex];
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
		
		throw new RuntimeException("Not yet implemented");
		
//		// make aveCondRecurIntervalForFltSysRups_type1 if it doesn't exist
//		if(aveCondRecurIntervalForFltSysRups_type2 == null)
//			aveCondRecurIntervalForFltSysRups_type2 = this.computeAveCondRecurIntervalForFltSysRups(2);
//		
//		// get ave norm time since last
//		double aveNormTimeSinceLast = OLDgetAveNormTimeSinceLastCorrected(fltSysRupIndex, onlyIfAllSectionsHaveDateOfLast);
//		// quit if all having date of last event is not satisfied
//		if(aveNormTimeSinceLast == Double.NaN)
//			return Double.NaN;
//
//		// now compute and set gain
//		if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster? 	// this should not be necessary, but faster?	// TODO actually this is not right, as we don't want to end up with the poiss prob given open interval
//			return 1.0;
//		}
//		else {
//			if(aveNormTimeSinceLast < 0) {
//				throw new RuntimeException("aveNormTimeSinceLast cannot be negative (aveNormTimeSinceLast="+aveNormTimeSinceLast+")");
//			}
//			double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type2[fltSysRupIndex];
//			return computeBPT_ProbGainFast(aveCondRecurInterval, aveNormTimeSinceLast*aveCondRecurInterval, durationYears);					
//		}
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
	public double OLDgetU3_ProbGain1_ForRup(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast) {

		// make aveCondRecurIntervalForFltSysRups_type2 if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type1 == null)
			aveCondRecurIntervalForFltSysRups_type1 = this.computeAveCondRecurIntervalForFltSysRups(1);
		
		long aveTimeOfLastMillis = OLDgetAveDateOfLastEventCorrected(fltSysRupIndex, onlyIfAllSectionsHaveDateOfLast);
		
		// quit if all having date of last event is not satisfied
		if(aveTimeOfLastMillis == Long.MIN_VALUE)
			return Double.NaN;
		
		double timeSinceLastYears = (double)(startTimeMillis-aveTimeOfLastMillis)/MILLISEC_PER_YEAR;
		
		// now compute and set gain
		if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster?	// TODO actually this is not right, as we don't want to end up with the poiss prob given open interval
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
	 * This computes the BPT probability gain using the UCERF3 type1 methodology
	 * 
	 * This returns Double.NaN if onlyIfAllSectionsHaveDateOfLast=true and one or mare sections
	 * lack date of last event.
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public double getU3_ProbGain1_ForRup(int fltSysRupIndex, double histOpenInterval, boolean onlyIfAllSectionsHaveDateOfLast) {

		// make aveCondRecurIntervalForFltSysRups_type1 if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type1 == null)
			aveCondRecurIntervalForFltSysRups_type1 = computeAveCondRecurIntervalForFltSysRups(1);
		
		double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type1[fltSysRupIndex];
				
		long aveTimeOfLastMillisWhereKnown = getAveDateOfLastEventWhereKnown(fltSysRupIndex);
		// the following global variables were just set by this method
		// 		double totRupArea
		// 		double totRupAreaWithDateOfLast
		// 		boolean allSectionsHadDateOfLast
		// 		boolean noSectionsHadDateOfLast
		
		double aveTimeSinceLastWhereKnownYears = (double)(startTimeMillis-aveTimeOfLastMillisWhereKnown)/MILLISEC_PER_YEAR;
		
		if(onlyIfAllSectionsHaveDateOfLast && !allSectionsHadDateOfLast) {
			return Double.NaN;
		}
		else if(allSectionsHadDateOfLast) {
			return computeBPT_ProbGainFast(aveCondRecurInterval, aveTimeSinceLastWhereKnownYears, durationYears);					
		}
		else if (noSectionsHadDateOfLast) {
			return computeBPT_ProbGainForUnknownDateOfLastFast(aveCondRecurInterval, histOpenInterval);
		}
		else {	// case where some have date of last; loop over all possibilities for those that don't.
			double sumCondProbGain=0;
			double totWeight=0;
			for(int i=0;i<normBPT_CDF.getNum();i++) {
				double timeSinceYears = normBPT_CDF.getX(i)*aveCondRecurInterval;
				double relProbForTimeSinceLast = 1.0-normBPT_CDF.getY(i);	// this is the probability of the date of last event (not considering hist open interval)
				if(timeSinceYears>=histOpenInterval && relProbForTimeSinceLast>0.0) {
					// average the time since last between known and unknown sections
					double areaWithOutDateOfLast = totRupArea-totRupAreaWithDateOfLast;
					double aveTimeSinceLast = (timeSinceYears*areaWithOutDateOfLast + aveTimeSinceLastWhereKnownYears*totRupAreaWithDateOfLast)/totRupArea;
					double condProbGain = computeBPT_ProbGainFast(aveCondRecurInterval, aveTimeSinceLast, durationYears);
					sumCondProbGain += condProbGain*relProbForTimeSinceLast;
					totWeight += relProbForTimeSinceLast;
				}
			}
			return sumCondProbGain/totWeight;
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
	public static ArbitrarilyDiscretizedFunc OLDgetBPT_TimeToPoisCondProbFunc(double aperiodicity) {
		
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

	public static EvenlyDiscrXYZ_DataSet OLDgetEquivLastEventTimeForHistOpenIntervalXYZ_Func(double aperiodicity,boolean verbose) {

		boolean applyProbNearOneThreshold=true;
		double probNearOneThreshold=0.001;

		double probThresholdForMinMaxTime = 1e-5;

		// for BPT calculator:
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		double mean = 20;
		double deltaX = 0.01;	// 0.1 years
		int numPoints = (int)Math.round(12*mean/deltaX);

		EvenlyDiscrXYZ_DataSet xyzDataCondProbForUnknown=null, xyzDataLogProbOfGettingToTime=null;
		
		if(verbose) {
//			System.out.println("mean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb\tnormEquivTimeSinceLast"+
//					"\tequivTimeSinceLast\ttestRatio\tproblem1\tproblem2\tproblem3\tminTimeSince\tmaxTimeSince\tmaxMinDiff\tprobOfGettingToTime\tprobRatio");
			System.out.println("mean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb\tnormEquivTimeSinceLast"+
					"\tequivTimeSinceLast\ttestRatio\tproblem1\tproblem2\tproblem3\tproblem4\telementUsed\tminTimeSince\tmaxTimeSince\tmaxMinDiff\tprobOfGettingToTime\tprobRatio\tnumEquivTimes");
			
			
			xyzDataCondProbForUnknown = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
			xyzDataLogProbOfGettingToTime = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);

		}

		// this is what we will return
		EvenlyDiscrXYZ_DataSet xyzDataEquivNormTimeSinceLast = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
		
		double minProbRatio = 10;
		double maxProbRatio = -1;

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

				
//				double normEquivTimeSinceLast=Double.NaN;
//				for(int k = condProbFunc.getClosestXIndex(histOpenInterval); k<condProbFunc.getNum();k++) {
//					double fractDiff = Math.abs((condProbFunc.getY(k)-condProbForUnknownTimeSinceLast)/condProbForUnknownTimeSinceLast);
//					if(fractDiff<0.001) {
//						equivTimeSinceLast = condProbFunc.getClosestX(k);
//						normEquivTimeSinceLast = equivTimeSinceLast/mean;
//						break;
//					}
//				}


				double equivTimeSinceLast= Double.NaN;
				
				int elementUsed = -1;
				ArrayList<Double> equivTimes= new ArrayList<Double>(); // make an empty list inc case the first two below are true
				boolean problem1=false;
				boolean lastPoint=false;
				boolean firstPoint=false;
				// check whether condProbForUnknownTimeSinceLast is within range of the function, and set to first or last point if not
				if(condProbForUnknownTimeSinceLast<=condProbFunc.getMinY()) {	// must use min rather than last point because func is not necessarily monotonically increasing
					if(condProbFunc.getY(0)-condProbForUnknownTimeSinceLast > probThresholdForMinMaxTime)	// TODO
						throw new RuntimeException("\nProblem: diff="+(condProbFunc.getY(0)-condProbForUnknownTimeSinceLast)+
								"; thresh = "+probThresholdForMinMaxTime+"; \ncondProbForUnknownTimeSinceLast="+condProbForUnknownTimeSinceLast+
								"\naperiocidity="+aperiodicity+"\nmean="+mean+"\nduration="+duration+"\nhistOpenInterval="+histOpenInterval+
								"\nminFuncVal="+condProbFunc.getMinY()+"\nmaxFuncVal="+condProbFunc.getMaxY());
					equivTimeSinceLast=condProbFunc.getX(0);
					firstPoint=true;
					problem1=true;
				}
				else if(condProbForUnknownTimeSinceLast>=condProbFunc.getMaxY()) {	// must use max rather than last point because func is not necessarily monotonically increasing
					if(condProbForUnknownTimeSinceLast-condProbFunc.getY(condProbFunc.getNum()-1) > probThresholdForMinMaxTime)	// check against last point
						throw new RuntimeException("\nProblem: diff="+(condProbForUnknownTimeSinceLast-condProbFunc.getY(condProbFunc.getNum()-1))+
								"; thresh = "+probThresholdForMinMaxTime+"; \ncondProbForUnknownTimeSinceLast="+condProbForUnknownTimeSinceLast+
								"\naperiocidity="+aperiodicity+"\nmean="+mean+"\nduration="+duration+"\nhistOpenInterval="+histOpenInterval);
					equivTimeSinceLast=condProbFunc.getX(condProbFunc.getNum()-1); //use last point
					problem1=true;
					lastPoint = true;
				}
				else {
//					equivTimeSinceLast = condProbFunc.getFirstInterpolatedX_afterXindex(condProbForUnknownTimeSinceLast, condProbFunc.getClosestXIndex(histOpenInterval));
//					equivTimeSinceLast = condProbFunc.getFirstInterpolatedX(condProbForUnknownTimeSinceLast);
					equivTimes = condProbFunc.getAllInterpolatedX(condProbForUnknownTimeSinceLast);
					int elNum=0;
					for(double time:equivTimes) {
						if(time>=histOpenInterval) {
							equivTimeSinceLast = time;	// put first one first
							elementUsed=elNum;
							break;
						}
						elNum+=1;
					}
				}

				// Find the minimum and maximum values from the function (that all have the same cond prob)
				double minTimeSince = Double.MAX_VALUE;
				double maxTimeSince = -1;
				for(int z=0;z<condProbFunc.getNum();z++) {
					double prob = condProbFunc.getY(z);
					double fractDiff = Math.abs((prob-condProbForUnknownTimeSinceLast)/condProbForUnknownTimeSinceLast);
					if(fractDiff<probThresholdForMinMaxTime) {
						double timeSince = condProbFunc.getX(z);
						if(minTimeSince>timeSince) minTimeSince=timeSince;
						if(maxTimeSince<timeSince) maxTimeSince=timeSince;
					}
				}
				
				// if no point found, put it as minimum (move above?)
				if(Double.isNaN(equivTimeSinceLast) || lastPoint) {
					equivTimeSinceLast = minTimeSince;
				}
				

				double normEquivTimeSinceLast = equivTimeSinceLast/mean;

				// check whether it's in the unsafe range
				double safeTimeSinceLast = bptCalc.getSafeTimeSinceLastCutoff();
				boolean problem2=false;
				if(equivTimeSinceLast>safeTimeSinceLast) {
					problem2 = true;
//					throw new RuntimeException("time since last in unsafe range");
				}
				
				
				boolean problem3=false;
				if(normEquivTimeSinceLast<histOpenIntOverMean) {
					// try historic open interval
					double testDiff = Math.abs(1- condProbFunc.getInterpolatedY(histOpenIntOverMean*mean)/condProbForUnknownTimeSinceLast);
					if(testDiff<0.001) {
						normEquivTimeSinceLast=histOpenIntOverMean;
						equivTimeSinceLast=normEquivTimeSinceLast*mean;
					}
					else {	// live with the fact that normEquivTimeSinceLast<histOpenIntOverMean - this causes jumps
						problem3=true;
					}
				}
				
						
				
				// don't let time since last be less that the historic open interval
//				for(int k=1;k<equivTimes.size();k++) {	// start at the second element in list
//					if(normEquivTimeSinceLast<histOpenIntOverMean) {
//						equivTimeSinceLast=equivTimes.get(k);
//						normEquivTimeSinceLast=equivTimeSinceLast/mean;
//						elementUsed = k;
//					}
//					else {
//						break;
//					}
//				}


//				// OLD don't let time since last be less that the historic open interval
//				boolean problem3=false;
//				if(normEquivTimeSinceLast<histOpenIntOverMean) {
//					normEquivTimeSinceLast=histOpenIntOverMean;
//					equivTimeSinceLast=normEquivTimeSinceLast*mean;
//					if(equivTimeSinceLast*1.02<minTimeSince || equivTimeSinceLast>1.02*maxTimeSince) {	// don't worry about small deviations
//						// try looking for a second occurrence
//						double altEquivTimeSinceLast = condProbFunc.getFirstInterpolatedX_afterXindex(condProbForUnknownTimeSinceLast, condProbFunc.getXindexForMaxY());
//						
//						double probRatio=condProbFunc.getClosestY(equivTimeSinceLast)/condProbForUnknownTimeSinceLast;
//						double probRatio2=condProbFunc.getClosestY(altEquivTimeSinceLast)/condProbForUnknownTimeSinceLast;
//						throw new RuntimeException("\nProblem: "+minTimeSince+" ?>? "+equivTimeSinceLast+" ?>? "+maxTimeSince+
//								"; \ncondProbForUnknownTimeSinceLast="+condProbForUnknownTimeSinceLast+
//								"\naperiocidity="+aperiodicity+"\nmean="+mean+"\nduration="+duration+"\nhistOpenInterval="+histOpenInterval+
//								"\nminTimeSince="+minTimeSince+"\nmaxTimeSince="+maxTimeSince+"\nprobRatio="+probRatio+"\naltEquivTimeSinceLast="+
//								altEquivTimeSinceLast+"\nprobRatio2="+probRatio2);
//						
////						equivTimeSinceLast=altEquivTimeSinceLast;
////						normEquivTimeSinceLast = equivTimeSinceLast/mean;
//					}
//					problem3=true;
//				}

				// check to see if probability is near 1.0 (and if so, give it date of last at the first occurrence of this, 
				// rather than letting time since last values go to zero)
				boolean problem4=false;
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
						problem4=true;
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
					
					
					double probOfGettingToEquivTimeSinceLast = 1.0-bptCalc.getCDF().getInterpolatedY(equivTimeSinceLast);
					
//					if(i==0 && j==0) {
//						GraphWindow graph = new GraphWindow(bptCalc.getCDF(), "bptCalc.getCDF()"); 
//					}

					double probRatio=condProbFunc.getClosestY(equivTimeSinceLast)/condProbForUnknownTimeSinceLast;
					
					if(minProbRatio>probRatio) minProbRatio=probRatio;
					if(maxProbRatio<probRatio) maxProbRatio=probRatio;

//					System.out.println("\t"+condProbForUnknownTimeSinceLast+"\t"+(float)normEquivTimeSinceLast+"\t"+(float)equivTimeSinceLast+"\t"+(float)testRatio+
//							"\t"+problem+"\t"+problem2+"\t"+problem3+"\t"+minTimeSince/mean+"\t"+maxTimeSince/mean+"\t"+(maxTimeSince-minTimeSince)+
//							"\t"+probOfGettingToEquivTimeSinceLast+"\t"+probRatio);
					System.out.println("\t"+condProbForUnknownTimeSinceLast+"\t"+(float)normEquivTimeSinceLast+"\t"+(float)equivTimeSinceLast+"\t"+(float)testRatio+
							"\t"+problem1+"\t"+problem2+"\t"+problem3+"\t"+problem4+"\t"+elementUsed+"\t"+minTimeSince/mean+"\t"+maxTimeSince/mean+"\t"+(maxTimeSince-minTimeSince)+
							"\t"+probOfGettingToEquivTimeSinceLast+"\t"+probRatio+"\t"+equivTimes.size());


					xyzDataCondProbForUnknown.set(i, j, condProbForUnknownTimeSinceLast);	
					xyzDataLogProbOfGettingToTime.set(i, j, probOfGettingToEquivTimeSinceLast);
					
				}
				
				xyzDataEquivNormTimeSinceLast.set(i, j, normEquivTimeSinceLast);
			}
		}

		if(verbose) {
			
//			System.out.println("\nminProbRatio="+minProbRatio+"\nmaxProbRatio="+maxProbRatio);
			
			CPT cpt_prob=null;
			CPT cpt_normRI=null;
			CPT cpt_logProbTime=null;
			try {
				cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
				cpt_normRI = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, xyzDataEquivNormTimeSinceLast.getMaxZ());
				cpt_logProbTime = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
			} catch (IOException e) {
				e.printStackTrace();
			}

			XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProbForUnknown, cpt_prob, "CondProbForUnknownLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Probability");
			XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);

			XYZPlotSpec spec_normRI = new XYZPlotSpec(xyzDataEquivNormTimeSinceLast, cpt_normRI, "EquivNormTimeSinceLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "NormRI");
			XYZPlotWindow window_normRI = new XYZPlotWindow(spec_normRI);

			XYZPlotSpec spec_logProbTime = new XYZPlotSpec(xyzDataLogProbOfGettingToTime, cpt_logProbTime, "ProbOfGettingToEquivTime; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Log10 Prob");
			XYZPlotWindow window_logProbTime = new XYZPlotWindow(spec_logProbTime);
			//			wind.panel.saveAsPNG("/tmp/fig.png");
			//			wind.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		}

		return xyzDataEquivNormTimeSinceLast;
	}
	
	/**
	 * This creates an XYZ data set containing the conditional probability when date of last 
	 * event is unknown as a function of log10(duration/meanRecurInt)  on the x axis) and 
	 * log10(histOpenInt/meanRecurInt) on the y axis.
	 * 
	 * That this function is independent of meanRecurInt can be seen by running in verbose 
	 * mode while changing the "mean" value set in the method. 
	 * 
	 * TODO need to test how close interpolated values are to true values
	 * 
	 * @param aperiodicity
	 * @param verbose - this will print to system.out and generate an XYZ plot of the probability
	 * @return
	 */
	public static EvenlyDiscrXYZ_DataSet getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(double aperiodicity,boolean verbose) {

		// for BPT calculator:
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		double mean = 20;
		double deltaX = 0.01;	// 0.1 years
		int numPoints = (int)Math.round(12*mean/deltaX);

		// this is what we will return
		EvenlyDiscrXYZ_DataSet xyzDataCondProbForUnknown = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
		
		for(int y=0;y<xyzDataCondProbForUnknown.getNumY();y++) {
			double logHistOpenIntOverMean = xyzDataCondProbForUnknown.getY(y);
			double histOpenIntOverMean = Math.pow(10,logHistOpenIntOverMean);
			double histOpenInterval = histOpenIntOverMean*mean;
			for(int x=0;x<xyzDataCondProbForUnknown.getNumX();x++) {
				double logDurOverMean = xyzDataCondProbForUnknown.getX(x);
				double durOverMean = Math.pow(10,logDurOverMean);
				double duration = durOverMean*mean;

				// get condProbForUnknownTimeSinceLast & condProbFunc from the calculator
				bptCalc.setAllParameters(mean, aperiodicity, deltaX, numPoints, duration, histOpenInterval);		
				double condProbForUnknownTimeSinceLast = bptCalc.getSafeCondProbForUnknownTimeSinceLastEvent();
				xyzDataCondProbForUnknown.set(x, y, condProbForUnknownTimeSinceLast);
						
				if(verbose) {
					if(x==0 && y==0 & aperiodicity==0.1)	// print header
						System.out.println("aperiodicity\tmean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb");
						
						System.out.println(aperiodicity+"\t"+mean+"\t"+(float)duration+"\t"+(float)histOpenInterval+"\t"+(float)logDurOverMean+"\t"+(float)logHistOpenIntOverMean+
							"\t"+condProbForUnknownTimeSinceLast);	
				}
			}
		}

		if(verbose) {	// plot result
			
			CPT cpt_prob=null;
			try {
				cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProbForUnknown, cpt_prob, "CondProbForUnknownLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Probability");
			XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);
		}

		return xyzDataCondProbForUnknown;
	}



	public static EvenlyDiscrXYZ_DataSet OLD3getEquivLastEventTimeForHistOpenIntervalXYZ_Func(double aperiodicity,boolean verbose) {

		boolean applyProbNearOneThreshold=true;
		double probNearOneThreshold=0.001;
		
		double probThresholdForMinMaxTime = 1e-3;

		// for BPT calculator:
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		double mean = 20;
		double deltaX = 0.01;	// 0.1 years
		int numPoints = (int)Math.round(12*mean/deltaX);

		// this is what we will return
		EvenlyDiscrXYZ_DataSet xyzDataEquivNormTimeSinceLast = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
		
		// these are for diagnostic plots:
		EvenlyDiscrXYZ_DataSet xyzDataCondProbForUnknown=null, xyzDataLogProbOfGettingToTime=null;
		if(verbose) {
			xyzDataCondProbForUnknown = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
			xyzDataLogProbOfGettingToTime = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
		}

		double minProbRatio = 10;
		double maxProbRatio = -1;

		double smallDurNormTimeSinceLast=Double.NaN;
		for(int y=0;y<xyzDataEquivNormTimeSinceLast.getNumY();y++) {
			double logHistOpenIntOverMean = xyzDataEquivNormTimeSinceLast.getY(y);
			double histOpenIntOverMean = Math.pow(10,logHistOpenIntOverMean);
			double histOpenInterval = histOpenIntOverMean*mean;
			boolean probNearOne=false;
			double timeSinceLastAtFirstProbNearOne=Double.NaN;
			for(int x=0;x<xyzDataEquivNormTimeSinceLast.getNumX();x++) {
				double logDurOverMean = xyzDataEquivNormTimeSinceLast.getX(x);
				double durOverMean = Math.pow(10,logDurOverMean);
				double duration = durOverMean*mean;

				// get condProbForUnknownTimeSinceLast & condProbFunc from the calculator
				bptCalc.setAllParameters(mean, aperiodicity, deltaX, numPoints, duration, histOpenInterval);		
				double condProbForUnknownTimeSinceLast = bptCalc.getSafeCondProbForUnknownTimeSinceLastEvent();
				EvenlyDiscretizedFunc condProbFunc = bptCalc.getSafeCondProbFunc();
				EvenlyDiscretizedFunc probOfLastEventFunc = bptCalc.getTimeSinceLastEventPDF();

				
				
				// get x-axis values that equal condProbForUnknownTimeSinceLast
				ArrayList<Double> equivTimes = condProbFunc.getAllInterpolatedX(condProbForUnknownTimeSinceLast);
				int elementUsed = -1;
				
				double equivTimeSinceLast = Double.NaN;
				int elNum = 0;
				double max = -1;
				for(double time:equivTimes) {
					double probOfTime = probOfLastEventFunc.getInterpolatedY(time);
					if(probOfTime>max) {
						equivTimeSinceLast=time;
						max=probOfTime;
						elementUsed=elNum;
					}
					elNum += 1;
				}
				
				
				
//				double minTimeSinceLast = histOpenInterval;
//				int minTimeSinceLastIndex = condProbFunc.getClosestXIndex(histOpenInterval);
//				if(condProbFunc.getX(minTimeSinceLastIndex) < histOpenInterval) minTimeSinceLastIndex+=1;	// make sure it's really above

				double equivTimeSinceLastAlt = Double.NaN;

				EvenlyDiscretizedFunc threshMaxFunc = new EvenlyDiscretizedFunc(0d, numPoints,  deltaX);
				double smallestDiff = Double.MAX_VALUE;
				int numWithinThresh=0;
				for(int z=0;z<condProbFunc.getNum();z++) {
					double fractDiff = Math.abs((condProbFunc.getY(z)-condProbForUnknownTimeSinceLast)/condProbForUnknownTimeSinceLast);
					if(smallestDiff>fractDiff) 
						smallestDiff=fractDiff;
					if(fractDiff<probThresholdForMinMaxTime) {
						double probOfTime = probOfLastEventFunc.getY(z);
						if(Double.isNaN(probOfTime) || Double.isInfinite(probOfTime))
								throw new RuntimeException("Problem");
						threshMaxFunc.set(z,probOfLastEventFunc.getY(z));
						numWithinThresh += 1;
					}
				}
				if(numWithinThresh>0)
					equivTimeSinceLastAlt = threshMaxFunc.getX(threshMaxFunc.getXindexForMaxY());
				


				double normEquivTimeSinceLast = equivTimeSinceLast/mean;
				double normEquivTimeSinceLastAlt = equivTimeSinceLastAlt/mean;
						
				if(verbose) {
					double probOfGettingToEquivTimeSinceLast = -1, probRatio=-1;
//					double probOfGettingToEquivTimeSinceLast = 1.0-bptCalc.getCDF().getInterpolatedY(equivTimeSinceLast);
					
//					if(i==0 && j==0) {
//						GraphWindow graph = new GraphWindow(bptCalc.getCDF(), "bptCalc.getCDF()"); 
//					}

//					double probRatio=condProbFunc.getClosestY(equivTimeSinceLast)/condProbForUnknownTimeSinceLast;
//					
//					if(minProbRatio>probRatio) minProbRatio=probRatio;
//					if(maxProbRatio<probRatio) maxProbRatio=probRatio;
					
					if(x==0 && y==0 & aperiodicity==0.1)	// print header
						System.out.println("aperiodicity\tmean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb\tnormEquivTimeSinceLast"+
								"\tequivTimeSinceLast\ttelementUsed\tprobOfGettingToTime\tprobRatio\tnumEquivTimes\tnumWithinThresh\tequivTimeSinceLastAlt\tratio");
						
//					if(equivTimes.size()==0 || equivTimes.size()>2 || numWithinThresh==0)
						System.out.println(aperiodicity+"\t"+mean+"\t"+(float)duration+"\t"+(float)histOpenInterval+"\t"+(float)logDurOverMean+"\t"+(float)logHistOpenIntOverMean+
							"\t"+condProbForUnknownTimeSinceLast+"\t"+(float)normEquivTimeSinceLast+"\t"+(float)equivTimeSinceLast+"\t"+elementUsed+
							"\t"+probOfGettingToEquivTimeSinceLast+"\t"+probRatio+"\t"+equivTimes.size()+"\t"+numWithinThresh+"\t"+(float)equivTimeSinceLastAlt+"\t"+
							(float)(equivTimeSinceLast/equivTimeSinceLastAlt));
						
						
//						if(numWithinThresh>0 && histOpenInterval>10) {
//							System.out.println(threshMaxFunc.getMaxY()+"\t"+threshMaxFunc.getXindexForMaxY()+"\t"+threshMaxFunc.getX(threshMaxFunc.getXindexForMaxY()));
//							System.out.println(threshMaxFunc);
//							System.exit(0);
//						}


					xyzDataCondProbForUnknown.set(x, y, condProbForUnknownTimeSinceLast);	
					xyzDataLogProbOfGettingToTime.set(x, y, probOfGettingToEquivTimeSinceLast);
					
				}
				
				xyzDataEquivNormTimeSinceLast.set(x, y, normEquivTimeSinceLast);
			}
		}

		if(verbose) {
			
//			System.out.println("\nminProbRatio="+minProbRatio+"\nmaxProbRatio="+maxProbRatio);
			
			CPT cpt_prob=null;
			CPT cpt_normRI=null;
			CPT cpt_logProbTime=null;
//			try {
//				cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
//				cpt_normRI = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, xyzDataEquivNormTimeSinceLast.getMaxZ());
//				cpt_logProbTime = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

//			XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProbForUnknown, cpt_prob, "CondProbForUnknownLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Probability");
//			XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);
//
//			XYZPlotSpec spec_normRI = new XYZPlotSpec(xyzDataEquivNormTimeSinceLast, cpt_normRI, "EquivNormTimeSinceLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "NormRI");
//			XYZPlotWindow window_normRI = new XYZPlotWindow(spec_normRI);
//
//			XYZPlotSpec spec_logProbTime = new XYZPlotSpec(xyzDataLogProbOfGettingToTime, cpt_logProbTime, "ProbOfGettingToEquivTimeWithoutEvent; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Log10 Prob");
//			XYZPlotWindow window_logProbTime = new XYZPlotWindow(spec_logProbTime);
			//			wind.panel.saveAsPNG("/tmp/fig.png");
			//			wind.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		}

		return xyzDataEquivNormTimeSinceLast;
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

	public static EvenlyDiscrXYZ_DataSet OLD2getEquivLastEventTimeForHistOpenIntervalXYZ_Func(double aperiodicity,boolean verbose) {

		boolean applyProbNearOneThreshold=true;
		double probNearOneThreshold=0.001;
		
		boolean preventFromGoingBelowHistOpenInterval=false;

		double probThresholdForMinMaxTime = 1e-4;

		// for BPT calculator:
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		double mean = 20;
		double deltaX = 0.01;	// 0.1 years
		int numPoints = (int)Math.round(12*mean/deltaX);

		// this is what we will return
		EvenlyDiscrXYZ_DataSet xyzDataEquivNormTimeSinceLast = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
		
		// these are for diagnostic plots:
		EvenlyDiscrXYZ_DataSet xyzDataCondProbForUnknown=null, xyzDataLogProbOfGettingToTime=null;
		if(verbose) {
			xyzDataCondProbForUnknown = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
			xyzDataLogProbOfGettingToTime = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
					minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogHistOpenIntOverMean);
		}

		double minProbRatio = 10;
		double maxProbRatio = -1;

		double smallDurNormTimeSinceLast=Double.NaN;
		for(int y=0;y<xyzDataEquivNormTimeSinceLast.getNumY();y++) {
			double logHistOpenIntOverMean = xyzDataEquivNormTimeSinceLast.getY(y);
			double histOpenIntOverMean = Math.pow(10,logHistOpenIntOverMean);
			double histOpenInterval = histOpenIntOverMean*mean;
			boolean probNearOne=false;
			double timeSinceLastAtFirstProbNearOne=Double.NaN;
			for(int x=0;x<xyzDataEquivNormTimeSinceLast.getNumX();x++) {
				double logDurOverMean = xyzDataEquivNormTimeSinceLast.getX(x);
				double durOverMean = Math.pow(10,logDurOverMean);
				double duration = durOverMean*mean;

				// get condProbForUnknownTimeSinceLast & condProbFunc from the calculator
				bptCalc.setAllParameters(mean, aperiodicity, deltaX, numPoints, duration, histOpenInterval);		
				double condProbForUnknownTimeSinceLast = bptCalc.getSafeCondProbForUnknownTimeSinceLastEvent();
				EvenlyDiscretizedFunc condProbFunc = bptCalc.getSafeCondProbFunc();

				double equivTimeSinceLast= Double.NaN;
				
				// get x-axis values that equal condProbForUnknownTimeSinceLast
				ArrayList<Double> equivTimes = condProbFunc.getAllInterpolatedX(condProbForUnknownTimeSinceLast);
				int elementUsed = -1;
				
				// set the minTimeSinceLast as historic open interval if desired
				double minTimeSinceLast = -1d;
				int minTimeSinceLastIndex = 0;
				if(preventFromGoingBelowHistOpenInterval) {
					minTimeSinceLast = histOpenInterval;
					minTimeSinceLastIndex = condProbFunc.getClosestXIndex(histOpenInterval);
					if(condProbFunc.getX(minTimeSinceLastIndex) < histOpenInterval) minTimeSinceLastIndex+=1;	// make sure it's really above
				}
				
				int elNum=0;
				for(double time:equivTimes) {
					if(time>=minTimeSinceLast) {
						equivTimeSinceLast = time;	// keep the first one first
						elementUsed=elNum;
						break;
					}
					elNum+=1;
				}

				// if still NaN, either all were within histOpenInterval or target out of Y range in function
				
				boolean problem1 = false;
				double smallestDiff = Double.MAX_VALUE;
				if(Double.isNaN(equivTimeSinceLast)) {
					problem1 = true;	// was NaN
					for(int z=minTimeSinceLastIndex;z<condProbFunc.getNum();z++) {
						double fractDiff = Math.abs((condProbFunc.getY(z)-condProbForUnknownTimeSinceLast)/condProbForUnknownTimeSinceLast);
						if(smallestDiff>fractDiff) smallestDiff=fractDiff;
						if(fractDiff<probThresholdForMinMaxTime) {
							equivTimeSinceLast = condProbFunc.getX(z);
							break;
						}
					}
				}
				
				// if it's still NaN, then the only option is to go below histOpenInterval
				boolean wentBelow=false;
				if(Double.isNaN(equivTimeSinceLast)) {
					// this will look for a crossing below histOpenInterval and get the closest one to histOpenInterval
					elNum=0;
					for(double time:equivTimes) {
						if(time<minTimeSinceLast) {
							equivTimeSinceLast = time;	// put first one first
							elementUsed=elNum;
							wentBelow=true;
						}
						elNum+=1;
					}
				}
				
				if(Double.isNaN(equivTimeSinceLast)) {
					throw new RuntimeException("\nequivTimeSinceLast is NaN; need to decrease probThresholdForMinMaxTime?\nsmallestDiff="+
					smallestDiff+" and probThresholdForMinMaxTime="+probThresholdForMinMaxTime+
					"\ncondProbForUnknownTimeSinceLast="+condProbForUnknownTimeSinceLast+
					"\nduration="+duration+"\nhistOpenInterval="+histOpenInterval+"\naperiodicity="+aperiodicity+
					"\ncondProbFunc.getX(startIndex)="+condProbFunc.getX(minTimeSinceLastIndex));
				}
				
				// check to see if it went below improperly
				if(preventFromGoingBelowHistOpenInterval) {
					if(equivTimeSinceLast<histOpenInterval && !wentBelow) {
						throw new RuntimeException("\n(equivTimeSinceLast<histOpenInterval && !wentBelow)\nequivTimeSinceLast="+
								equivTimeSinceLast+"\nhistOpenInterval="+histOpenInterval+"\nwentBelow="+wentBelow+
						"\ncondProbForUnknownTimeSinceLast="+condProbForUnknownTimeSinceLast+
						"\nduration="+duration+"\naperiodicity="+aperiodicity+
						"\ncondProbFunc.getX(startIndex)="+condProbFunc.getX(minTimeSinceLastIndex));
					}					
				}
				
				// check whether it's in the unsafe range
				boolean problem2=false;
				if(equivTimeSinceLast>bptCalc.getSafeTimeSinceLastCutoff()) {
					problem2 = true;
				}


				double normEquivTimeSinceLast = equivTimeSinceLast/mean;
						
				// check to see if probability is near 1.0 (and if so, give it the date of last at the first occurrence of this, 
				// rather than letting time since last values go to zero)
				boolean problem4=false;
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
						problem4=true;
					}						
				}


				if(verbose) {
					// This was a check to see if timeSinceLast for smallest duration could be applied to other durations (if same conditional prob); the result is it can't
					if(x==0) {
						smallDurNormTimeSinceLast = normEquivTimeSinceLast;
					}
					double smallDurTimeSinceLast = smallDurNormTimeSinceLast*mean;
					if(smallDurTimeSinceLast>condProbFunc.getMaxX()) smallDurTimeSinceLast=condProbFunc.getMaxX();
					double testProb = condProbFunc.getInterpolatedY(smallDurTimeSinceLast);
					double testRatio = testProb/condProbForUnknownTimeSinceLast;
					
					
					double probOfGettingToEquivTimeSinceLast = 1.0-bptCalc.getCDF().getInterpolatedY(equivTimeSinceLast);
					
//					if(i==0 && j==0) {
//						GraphWindow graph = new GraphWindow(bptCalc.getCDF(), "bptCalc.getCDF()"); 
//					}

					double probRatio=condProbFunc.getClosestY(equivTimeSinceLast)/condProbForUnknownTimeSinceLast;
					
					if(minProbRatio>probRatio) minProbRatio=probRatio;
					if(maxProbRatio<probRatio) maxProbRatio=probRatio;
					
					if(x==0 && y==0)	// print header
						System.out.println("aperiodicity\tmean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb\tnormEquivTimeSinceLast"+
								"\tequivTimeSinceLast\ttestRatio\tproblem1\tproblem2\twentBelow\tproblem4\telementUsed\tprobOfGettingToTime\tprobRatio\tnumEquivTimes");
						
					if(equivTimes.size()==0 || equivTimes.size()>2)
						System.out.println(aperiodicity+"\t"+mean+"\t"+(float)duration+"\t"+(float)histOpenInterval+"\t"+(float)logDurOverMean+"\t"+(float)logHistOpenIntOverMean+
							"\t"+condProbForUnknownTimeSinceLast+"\t"+(float)normEquivTimeSinceLast+"\t"+(float)equivTimeSinceLast+"\t"+(float)testRatio+
							"\t"+problem1+"\t"+problem2+"\t"+wentBelow+"\t"+problem4+"\t"+elementUsed+
							"\t"+probOfGettingToEquivTimeSinceLast+"\t"+probRatio+"\t"+equivTimes.size());

					xyzDataCondProbForUnknown.set(x, y, condProbForUnknownTimeSinceLast);	
					xyzDataLogProbOfGettingToTime.set(x, y, probOfGettingToEquivTimeSinceLast);
					
				}
				
				xyzDataEquivNormTimeSinceLast.set(x, y, normEquivTimeSinceLast);
			}
		}

		if(verbose) {
			
//			System.out.println("\nminProbRatio="+minProbRatio+"\nmaxProbRatio="+maxProbRatio);
			
			CPT cpt_prob=null;
			CPT cpt_normRI=null;
			CPT cpt_logProbTime=null;
			try {
				cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
				cpt_normRI = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, xyzDataEquivNormTimeSinceLast.getMaxZ());
				cpt_logProbTime = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0.0, 1.0);
			} catch (IOException e) {
				e.printStackTrace();
			}

//			XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProbForUnknown, cpt_prob, "CondProbForUnknownLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Probability");
//			XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);
//
//			XYZPlotSpec spec_normRI = new XYZPlotSpec(xyzDataEquivNormTimeSinceLast, cpt_normRI, "EquivNormTimeSinceLast; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "NormRI");
//			XYZPlotWindow window_normRI = new XYZPlotWindow(spec_normRI);
//
//			XYZPlotSpec spec_logProbTime = new XYZPlotSpec(xyzDataLogProbOfGettingToTime, cpt_logProbTime, "ProbOfGettingToEquivTimeWithoutEvent; aper="+aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Log10 Prob");
//			XYZPlotWindow window_logProbTime = new XYZPlotWindow(spec_logProbTime);
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
	 * This gives the BPT conditional probability gain, when date of last event is unknown, 
	 * for the given recurrence interval and historic open interval.
	 * 
	 * This is "fast" in that it interpolates from a cached XYZ function.
	 * 
	 * TODO this should be tested against the slower way.
	 * 
	 * @param recurIntevalYears
	 * @param histOpenIntervalYears
	 * @return
	 */
	public double computeBPT_ProbGainForUnknownDateOfLastFast(double recurIntevalYears, double histOpenIntervalYears) {
		
		// make xyz function if it's null
		if(condProbForUnknownDateOfLast_xyzData == null) {
			condProbForUnknownDateOfLast_xyzData = getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(aperiodicity,false);
		}
		
		double log10_NormalizedDuration = Math.log10(durationYears/recurIntevalYears);
		double log10_NormalizedHistOpenInt = Math.log10(histOpenIntervalYears/recurIntevalYears);
		
		// check the range
		if(log10_NormalizedDuration<minLogDurOverMean)
			log10_NormalizedDuration=minLogDurOverMean;
		else if(log10_NormalizedDuration>maxLogDurOverMean)
			log10_NormalizedDuration=maxLogDurOverMean;

		if(log10_NormalizedHistOpenInt<minLogHistOpenIntOverMean)
			log10_NormalizedHistOpenInt=minLogHistOpenIntOverMean;
		else if(log10_NormalizedHistOpenInt>maxLogHistOpenIntOverMean)
			log10_NormalizedHistOpenInt=maxLogHistOpenIntOverMean;

		double condProb = condProbForUnknownDateOfLast_xyzData.bilinearInterpolation(log10_NormalizedDuration, log10_NormalizedHistOpenInt);
		double poisProb = 1.0-Math.exp(-durationYears/recurIntevalYears);
		return condProb/poisProb;
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
	 * 
	 * TODO move this to a test class
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
	public static void OLDtestTimeToPoisCondProb() {
		double[] ri_array = {10,1e2,1e3,1e4,1e5,1e6,1e7,1e8}; 	// recurrence intervals to loop over
//		double[] aperArray = {0.2};					// aperiodicities to loop over
		double[] aperArray = {0.2,0.4,0.6,0.8};					// aperiodicities to loop over
		
		double logLowDurOverRI = -2;
		double logHighDurOverRI = 1;
		int numDurOverRI = 31;
		double deltaLogDurOverRI = (logHighDurOverRI-logLowDurOverRI)/(numDurOverRI-1);
		
		System.out.println("bpt/pois\tdistOut\tdurOverRI\tri\tduration\taper\tpoisProb\tbptProb");

		for(double aper:aperArray) {
			ArbitrarilyDiscretizedFunc distOutFunc = OLDgetBPT_TimeToPoisCondProbFunc(aper);
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
		
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.1, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.2, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.3, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.4, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.5, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.6, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.7, true);
		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.8, true);

	}

}
