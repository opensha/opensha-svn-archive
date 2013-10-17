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
 * 4) enable changing just the timeSpan and/or aperiodicity (would require setting dependent arrays to null)
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
		
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity, durationYears);
		
		// set normBPT_CDF
		BPT_DistCalc tempCalc = new BPT_DistCalc();
		double delta = max_time_for_normBPT_CDF/(num_for_normBPT_CDF-1);
		tempCalc.setAll(1.0, aperiodicity, delta, num_for_normBPT_CDF);
		normBPT_CDF=tempCalc.getCDF();

	}
	
	
	/**
	 * This is for tests
	 * @param 
	 */
	public ProbabilityModelsCalc(double aperiodicity, double durationYears) {
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity, durationYears);
		
		// set normBPT_CDF
		BPT_DistCalc tempCalc = new BPT_DistCalc();
		double delta = max_time_for_normBPT_CDF/(num_for_normBPT_CDF-1);
		tempCalc.setAll(1.0, aperiodicity, delta, num_for_normBPT_CDF);	// TODO check this discretization and overall look
		normBPT_CDF=tempCalc.getCDF();	
//		GraphWindow graph = new GraphWindow(normBPT_CDF, "test");

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
	
	
	/**
	 * This method returns normalized time since last event (timeSince/meanRecurInt)
	 *  averaged over fault sections that have such data (and weighted by section area). 
	 * 
	 *  Double.NaN is returned in none of the fault sections have a date of last event 
	 *  
	 *  The following global variables are also set for further diagnostics:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 * 		boolean noSectionsHadDateOfLast
	 *
	 * @param fltSystRupIndex
	 */
	public double getAveNormTimeSinceLastEventWhereKnown(int fltSystRupIndex) {
		
		List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumNormTimeSinceLast = 0;
		for(FaultSectionPrefData data:fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			int sectID = data.getSectionId();
			double area = sectionArea[sectID];
			totRupArea+=area;
			if(dateOfLast != Long.MIN_VALUE) {
				sumNormTimeSinceLast += area*((double)(startTimeMillis-dateOfLast)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sectID];
				totRupAreaWithDateOfLast += area;
			}
			else {
				allSectionsHadDateOfLast = false;
			}
		}
		if(totRupAreaWithDateOfLast>0.0)
			return sumNormTimeSinceLast/totRupAreaWithDateOfLast; 
		else {
			noSectionsHadDateOfLast=true;
			return Double.NaN;
		}
	}
	
	

	public int writeSectionsWithDateOfLastEvent() {
		List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataList();
		int numWith=0;
		System.out.println("Sections With Date of Last Event Data (timeSinceLastYears, dateOfLastMillis, sectName):");
		for(FaultSectionPrefData data:fltData) {
			long dateOfLastMillis = data.getDateOfLastEvent();
			if(dateOfLastMillis != Long.MIN_VALUE) {
				double timeSinceLastYears = (startTimeMillis-dateOfLastMillis)/MILLISEC_PER_YEAR;
				timeSinceLastYears = (double)Math.round(timeSinceLastYears*100)/100;
				System.out.println((float)timeSinceLastYears+"\t"+"\t"+dateOfLastMillis+"\t"+data.getName());
				numWith += 1;
			}
		}
		return numWith;
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
	 * This computes the BPT probability gain using the UCERF3 type2 methodology
	 * 
	 * This returns Double.NaN if onlyIfAllSectionsHaveDateOfLast=true and one or mare sections
	 * lack date of last event.
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 */
	public double getU3_ProbGain2_ForRup(int fltSysRupIndex, double histOpenInterval, boolean onlyIfAllSectionsHaveDateOfLast) {

		// make aveCondRecurIntervalForFltSysRups_type1 if it doesn't exist
		if(aveCondRecurIntervalForFltSysRups_type2 == null)
			aveCondRecurIntervalForFltSysRups_type2 = computeAveCondRecurIntervalForFltSysRups(2);
		
		double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type2[fltSysRupIndex];
				
		double aveNormTimeSinceLastEventWhereKnown = getAveNormTimeSinceLastEventWhereKnown(fltSysRupIndex);
		// the following global variables were just set by this method
		// 		double totRupArea
		// 		double totRupAreaWithDateOfLast
		// 		boolean allSectionsHadDateOfLast
		// 		boolean noSectionsHadDateOfLast
		
		double aveTimeSinceLastWhereKnownYears = aveNormTimeSinceLastEventWhereKnown*aveCondRecurInterval;
		
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
	 * This creates an XYZ data set containing the conditional probability when date of last 
	 * event is unknown as a function of log10(duration/meanRecurInt)  on the x axis) and 
	 * log10(histOpenInt/meanRecurInt) on the y axis.
	 * 
	 * That this function is independent of meanRecurInt can be seen by running in verbose 
	 * mode while changing the "mean" value set in the method. 
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




	
	


	/**
	 * This is made fast by using a reference calculator (with a reference RI), rather than
	 * redoing the calculation each time .
	 * 
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
	 * TODO this should be tested against the slower way (compare interpolated values to
	 * directly computed values).
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
	 * @param args
	 */
	public static void main(String[] args) {
		
		ProbabilityModelsCalc testCalc = new ProbabilityModelsCalc(0.2, 10d);
		
//		testComputeBPT_ProbGainFast();
		
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.1, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.2, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.3, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.4, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.5, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.6, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.7, true);
//		getXYZ_FuncOfCondProbForUnknownDateOfLastEvent(0.8, true);

	}

}
