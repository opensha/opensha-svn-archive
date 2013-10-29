package scratch.UCERF3.erf.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.RandomDataImpl;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.AbstractDiscretizedFunc;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc_3D;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.xyz.ArbDiscrXYZ_DataSet;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.utils.ProbModelsPlottingUtils;
import scratch.UCERF3.erf.ETAS.IntegerPDF_FunctionSampler;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.utils.UCERF3_DataUtils;


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
	
	public final static boolean D=true;
	
	public final static double MILLISEC_PER_YEAR = 1000*60*60*24*365.25;
	public final static long MILLISEC_PER_DAY = 1000*60*60*24;
	
	// passed in values:
	FaultSystemSolution fltSysSolution;
	double[] longTermRateOfFltSysRup;	// this has zeros where events were filtered our by the ERF (mags too low); this includes aftershocks
	double aperiodicity;

	// computed from passed in values
	FaultSystemRupSet fltSysRupSet;
	int numRups;
	int numSections;	
	
	double[] longTermPartRateForSectArray;
	double[] sectionArea;
	long[] dateOfLastForSect;
	
	// The following ave recurrence interval of each rupture conditioned on the fact that it is the next event to occur
	double[] aveCondRecurIntervalForFltSysRups_type1; //  for averaging section recurrence intervals and time since last;
	double[] aveCondRecurIntervalForFltSysRups_type2; //  for averaging section rates and normalized time since last

	// for BPT reference calculator (200 year recurrence interval); this is used for efficiency
	static double refRI = 1.0;
	static double deltaT = 0.005;
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
	
	
	double[] sectionGainArray;	// for WG02-type calculations
	boolean[] sectionGainReal;	// for WG02-type calculations
	
	// these global variables are used as diagnostics
	double totRupArea;
	double totRupAreaWithDateOfLast;
	boolean allSectionsHadDateOfLast;
	boolean noSectionsHadDateOfLast;

	// data dir for Elastic Rebound simulations
	final static File dataDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR,File.separator+"erSimulations");

	
	/**
	 * 
	 * TODO this constructor is no longer necessary given the erf one below?
	 * 
	 * This is the main constructor.  Note that longTermRateOfFltSysRupInERF is provided, rather than 
	 * using what's obtained as fltSysSolution.getRateForAllRups(), because the ERF has filtered out
	 * small events (TODO not entirely sure this is necessary; depends on whether small sections should be
	 * included in total section rates; the same filtering could also be done here). 
	 * 
	 * @param fltSysSolution
	 * @param longTermRateOfFltSysRupInERF	- this rate includes aftershocks
	 * @param aperiodicity - aperiodicity of the BPT model
	 * 
	 */
	public ProbabilityModelsCalc(FaultSystemSolution fltSysSolution, double[] longTermRateOfFltSysRupInERF, double aperiodicity) {
		this.fltSysSolution=fltSysSolution;
		longTermRateOfFltSysRup = longTermRateOfFltSysRupInERF;
		this.aperiodicity = aperiodicity;
		
		fltSysRupSet = fltSysSolution.getRupSet();
		numRups = fltSysRupSet.getNumRuptures();
		numSections = fltSysRupSet.getNumSections();
		
		initializeArrays();
		
		if(!Double.isNaN(aperiodicity)) {
			refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity);
			
			// set normBPT_CDF
			BPT_DistCalc tempCalc = new BPT_DistCalc();
			double delta = max_time_for_normBPT_CDF/(num_for_normBPT_CDF-1);
			tempCalc.setAll(1.0, aperiodicity, delta, num_for_normBPT_CDF);
			normBPT_CDF=tempCalc.getCDF();		
		}	

	}
	
	
	public ProbabilityModelsCalc(FaultSystemSolutionERF erf) {
		this.fltSysSolution=erf.getSolution();
		longTermRateOfFltSysRup = erf.getLongTermRateOfFltSysRupInERF();
		if(erf.getAdjustableParameterList().containsParameter(BPT_AperiodicityParam.NAME)) {
			aperiodicity = ((BPT_AperiodicityParam)erf.getParameter(BPT_AperiodicityParam.NAME)).getValue();
		}
		else {
			aperiodicity = Double.NaN;
		}
		
		fltSysRupSet = fltSysSolution.getRupSet();
		numRups = fltSysRupSet.getNumRuptures();
		numSections = fltSysRupSet.getNumSections();
		
		initializeArrays();
		
		if(!Double.isNaN(aperiodicity)) {
			refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity);
			
			// set normBPT_CDF
			BPT_DistCalc tempCalc = new BPT_DistCalc();
			double delta = max_time_for_normBPT_CDF/(num_for_normBPT_CDF-1);
			tempCalc.setAll(1.0, aperiodicity, delta, num_for_normBPT_CDF);
			normBPT_CDF=tempCalc.getCDF();		
		}	
	}

	
	
	
	/**
	 * This is for tests
	 * @param 
	 */
	public ProbabilityModelsCalc(double aperiodicity) {
		this.aperiodicity=aperiodicity;
		refBPT_DistributionCalc = getRef_BPT_DistCalc(aperiodicity);
		
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
		
		// now make sectionArea[] & dateOfLastForSect[]
		sectionArea = new double[numSections];
		dateOfLastForSect = new long[numSections];
		for(int s=0;s<numSections;s++) {
			FaultSectionPrefData sectData = this.fltSysRupSet.getFaultSectionData(s);
			sectionArea[s]= sectData.getTraceLength()*sectData.getReducedDownDipWidth();
			dateOfLastForSect[s] = sectData.getDateOfLastEvent();
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
//		System.out.println("getAveDateOfLastEventWhereKnown");
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(int s:fltSysRupSet.getSectionsIndicesForRup(fltSystRupIndex)) {
			long dateOfLast = dateOfLastForSect[s];
			double area = sectionArea[s];
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
	 * @param presentTimeMillis - present time in epoch milliseconds
	 */
	public double getAveNormTimeSinceLastEventWhereKnown(int fltSystRupIndex, long presentTimeMillis) {
		
//		System.out.println("getAveNormTimeSinceLastEventWhereKnown");
		
//		List<FaultSectionPrefData> fltData = fltSysRupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumNormTimeSinceLast = 0;
		for(int s : fltSysRupSet.getSectionsIndicesForRup(fltSystRupIndex)) {
			long dateOfLast = dateOfLastForSect[s];
			double area = sectionArea[s];
			totRupArea+=area;
			if(dateOfLast != Long.MIN_VALUE) {
				sumNormTimeSinceLast += area*((double)(presentTimeMillis-dateOfLast)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[s];
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
				System.out.println(dateOfLastMillis+"\t"+data.getName());
				numWith += 1;
			}
		}
		return numWith;
	}
	
	
	
	/**
	 * This computes the BPT probability gain using the UCERF3 type2 methodology
	 * 
	 * This returns Double.NaN if onlyIfAllSectionsHaveDateOfLast=true and one or mare sections
	 * lack date of last event.
	 * 
	 * @param fltSystRupIndex
	 * @param onlyIfAllSectionsHaveDateOfLast
	 * @param histOpenInterval
	 * @param aveRecurIntervals - if false, rates will be averaged in get the conditional recurrence interval
	 * @param aveNormTimeSinceLast - if true, normalized time since last is averaged (divided by section intervals); otherwise time since last is averaged
	 */
	public double getU3_ProbGainForRup(int fltSysRupIndex, double histOpenInterval, boolean onlyIfAllSectionsHaveDateOfLast, 
			boolean aveRecurIntervals, boolean aveNormTimeSinceLast, long presentTimeMillis, double durationYears) {

		// get the average recurrence interval
		double aveCondRecurInterval;
		if(aveRecurIntervals) {
			if(aveCondRecurIntervalForFltSysRups_type1 == null)
				aveCondRecurIntervalForFltSysRups_type1 = computeAveCondRecurIntervalForFltSysRups(1);
			System.out.println("aveRecurIntervals");
			aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type1[fltSysRupIndex];
		}
		else {
			if(aveCondRecurIntervalForFltSysRups_type2 == null)
				aveCondRecurIntervalForFltSysRups_type2 = computeAveCondRecurIntervalForFltSysRups(2);
			System.out.println("aveRecurRates");
			aveCondRecurInterval = aveCondRecurIntervalForFltSysRups_type2[fltSysRupIndex];			
		}
		
		// get aveTimeSinceLastWhereKnownYears
		double aveTimeSinceLastWhereKnownYears;
		if(aveNormTimeSinceLast) {
			double aveNormTimeSinceLastEventWhereKnown = getAveNormTimeSinceLastEventWhereKnown(fltSysRupIndex, presentTimeMillis);
			aveTimeSinceLastWhereKnownYears = aveNormTimeSinceLastEventWhereKnown*aveCondRecurInterval;
		}
		else {
			long aveTimeOfLastMillisWhereKnown = getAveDateOfLastEventWhereKnown(fltSysRupIndex);
			aveTimeSinceLastWhereKnownYears = (double)(presentTimeMillis-aveTimeOfLastMillisWhereKnown)/MILLISEC_PER_YEAR;	
		}
		// the following global variables were just set by the above 
		// 		double totRupArea
		// 		double totRupAreaWithDateOfLast
		// 		boolean allSectionsHadDateOfLast
		// 		boolean noSectionsHadDateOfLast
		
		
		double poisProb = computePoissonProb(aveCondRecurInterval, durationYears);

		if(onlyIfAllSectionsHaveDateOfLast && !allSectionsHadDateOfLast) {
			return Double.NaN;
		}
		else if(allSectionsHadDateOfLast) {
			return computeBPT_ProbFast(aveCondRecurInterval, aveTimeSinceLastWhereKnownYears, durationYears)/poisProb;					
		}
		else if (noSectionsHadDateOfLast) {
			return computeBPT_ProbForUnknownDateOfLastFast(aveCondRecurInterval, histOpenInterval, durationYears)/poisProb;
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
					double condProb = computeBPT_ProbFast(aveCondRecurInterval, aveTimeSinceLast, durationYears);
					sumCondProbGain += (condProb/poisProb)*relProbForTimeSinceLast;
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
	public double getWG02_ProbGainForRup(int fltSysRupIndex, boolean onlyIfAllSectionsHaveDateOfLast, long presentTimeMillis, double durationYears) {
		
		// first compute the gains for each fault section if it does not exist
		if(sectionGainArray==null) {
			sectionGainArray = new double[numSections];
			sectionGainReal = new boolean[numSections];
			for(int s=0; s<numSections;s++) {
				long timeOfLastMillis = dateOfLastForSect[s];
				if(timeOfLastMillis != Long.MIN_VALUE) {
					double timeSinceLastYears = ((double)(presentTimeMillis-timeOfLastMillis))/MILLISEC_PER_YEAR;
					double refTimeSinceLast = timeSinceLastYears*refRI*longTermPartRateForSectArray[s];
					double refDuration = durationYears*refRI*longTermPartRateForSectArray[s];
					double prob_bpt = refBPT_DistributionCalc.getCondProb(refTimeSinceLast, refDuration);
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
	
	
	
	
	public void plotXYZ_FuncOfCondProbForUnknownDateOfLastEvent() {

		BPT_DistCalc bptCalc2 = getRef_BPT_DistCalc(aperiodicity);

		EvenlyDiscrXYZ_DataSet condProbForUnknownDateOfLast_xyzData;
		
		double minLogDurOverMean = -7;
		double maxLogDurOverMean = Math.log10(5.01187);	// this is 0.7
		double deltaLogDurOverMean = 0.1;
		int numLogDurOverMean = 1+(int)Math.ceil((maxLogDurOverMean-minLogDurOverMean)/deltaLogDurOverMean);

		double minLogHistOpenIntOverMean = -2;
		double maxLogHistOpenIntOverMean = Math.log10(5.01187);
		double deltaLogHistOpenIntOverMean = 0.025;
		int numLogHistOpenIntOverMean = 1+(int)Math.ceil((maxLogHistOpenIntOverMean-minLogHistOpenIntOverMean)/deltaLogHistOpenIntOverMean);

		// this is what we will return
		EvenlyDiscrXYZ_DataSet xyzDataCondProbForUnknown = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogDurOverMean,deltaLogHistOpenIntOverMean);
		
		EvenlyDiscrXYZ_DataSet xyzDataProbGain=xyzDataProbGain = new EvenlyDiscrXYZ_DataSet(numLogDurOverMean, numLogHistOpenIntOverMean, 
				minLogDurOverMean, minLogHistOpenIntOverMean, deltaLogDurOverMean,deltaLogHistOpenIntOverMean);
			
		for(int y=0;y<xyzDataCondProbForUnknown.getNumY();y++) {
			double logHistOpenIntOverMean = xyzDataCondProbForUnknown.getY(y);
			double histOpenIntOverMean = Math.pow(10,logHistOpenIntOverMean);
			double histOpenInterval = histOpenIntOverMean*refRI;
			for(int x=0;x<xyzDataCondProbForUnknown.getNumX();x++) {
				double logDurOverMean = xyzDataCondProbForUnknown.getX(x);
				double durOverMean = Math.pow(10,logDurOverMean);
				double duration = durOverMean*refRI;

				// get condProbForUnknownTimeSinceLast & condProbFunc from the calculator
				bptCalc2.setDurationAndHistOpenInterval(duration, histOpenInterval);
				double condProbForUnknownTimeSinceLast = bptCalc2.getCondProbForUnknownTimeSinceLastEvent();
				xyzDataCondProbForUnknown.set(x, y, Math.log10(condProbForUnknownTimeSinceLast));
				double probGain = condProbForUnknownTimeSinceLast/computePoissonProb(refRI, duration);
				xyzDataProbGain.set(x, y, Math.log10(probGain));

//				if(x==0 && y==0)	// print header
//					System.out.println("aperiodicity\tmean\tduration\thistOpenInterval\tlogDurOverMean\tlogHistOpenIntOverMean\tcondProb\tlog10_CondProb\tgain");
//
//				System.out.println(aperiodicity+"\t"+refRI+"\t"+(float)duration+"\t"+(float)histOpenInterval+"\t"+(float)logDurOverMean+"\t"+(float)logHistOpenIntOverMean+
//						"\t"+condProbForUnknownTimeSinceLast+"\t"+Math.log10(condProbForUnknownTimeSinceLast)+"\t"+probGain);

			}
		}

		CPT cpt_prob=null;
		CPT cpt_probGain=null;
		try {
			cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-8, 0);
			cpt_probGain = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-1, 2);
//			cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(xyzDataCondProbForUnknown.getMinZ(), 0);
//			cpt_probGain = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(xyzDataProbGain.getMinZ(), xyzDataProbGain.getMaxZ());
//			System.out.println("\t condProb min & max:\t"+(float)xyzDataCondProbForUnknown.getMinZ()+"\t"+(float)xyzDataCondProbForUnknown.getMaxZ());
//			System.out.println("\t probGain min & max:\t"+(float)xyzDataProbGain.getMinZ()+"\t"+(float)xyzDataProbGain.getMaxZ());
		} catch (IOException e) {
			e.printStackTrace();
		}
		XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProbForUnknown, cpt_prob, "CondProbForUnknownLast; aper="+(float)aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Probability");
		XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);
		XYZPlotSpec spec_probGain = new XYZPlotSpec(xyzDataProbGain, cpt_probGain, "Log10 Prob Gain (vs Poisson); aper="+(float)aperiodicity, "LogNormDuration", "LogNormHistOpenInt", "Log10 Prob Gain");
		XYZPlotWindow window_probGain = new XYZPlotWindow(spec_probGain);

	}

	
	public void plotXYZ_FuncOfCondProb() {

		BPT_DistCalc bptCalc2 = getRef_BPT_DistCalc(aperiodicity);

		double minLogDurOverMean = -1;
		double maxLogDurOverMean = Math.log10(5.01187);	// this is 0.7
		double deltaLogDurOverMean = 0.01;
		int numLogDurOverMean = 1+(int)Math.ceil((maxLogDurOverMean-minLogDurOverMean)/deltaLogDurOverMean);

		double minLogNormTimeSinceLast = -1;
		double maxLogNormTimeSinceLast = Math.log10(5.01187);
		double deltaLogNormTimeSinceLast = 0.01;
		int numLogNormTimeSinceLast = 1+(int)Math.ceil((maxLogNormTimeSinceLast-minLogNormTimeSinceLast)/deltaLogNormTimeSinceLast);

		EvenlyDiscrXYZ_DataSet xyzDataCondProb = new EvenlyDiscrXYZ_DataSet(numLogNormTimeSinceLast, numLogDurOverMean,
				minLogNormTimeSinceLast, minLogDurOverMean, deltaLogNormTimeSinceLast, deltaLogDurOverMean);
		
		EvenlyDiscrXYZ_DataSet xyzDataProbGain = new EvenlyDiscrXYZ_DataSet(numLogNormTimeSinceLast, numLogDurOverMean,
				minLogNormTimeSinceLast, minLogDurOverMean, deltaLogNormTimeSinceLast, deltaLogDurOverMean);
			
		for(int x=0;x<xyzDataCondProb.getNumX();x++) {
			double logNormTimeSinceLast = xyzDataCondProb.getX(x);
			double normTimeSinceLast = Math.pow(10,logNormTimeSinceLast);
			double timeSinceLast = normTimeSinceLast*refRI;
			for(int y=0;y<xyzDataCondProb.getNumY();y++) {
				double logDurOverMean = xyzDataCondProb.getY(y);
				double durOverMean = Math.pow(10,logDurOverMean);
				double duration = durOverMean*refRI;

				double condProb = bptCalc2.getCondProb(timeSinceLast, duration);
				if(condProb == 0) 
					condProb = Double.NaN;
				xyzDataCondProb.set(x, y, Math.log10(condProb));
//				xyzDataCondProb.set(x, y, condProb);
				double probGain = condProb/computePoissonProb(refRI, duration);
				xyzDataProbGain.set(x, y, Math.log10(probGain));
//				xyzDataProbGain.set(x, y, probGain);

			}
		}

		CPT cpt_prob=null;
		CPT cpt_probGain=null;
		try {
//			cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, 1);
			cpt_prob = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(xyzDataCondProb.getMinZ(), xyzDataCondProb.getMaxZ());
			cpt_probGain = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(xyzDataProbGain.getMinZ(), xyzDataProbGain.getMaxZ());
		} catch (IOException e) {
			e.printStackTrace();
		}
		XYZPlotSpec spec_prob = new XYZPlotSpec(xyzDataCondProb, cpt_prob, "Log10 BPT Cond Prob; aper="+(float)aperiodicity, "LogNormTimeSinceLast", "LogNormDuration", "Probability");
		XYZPlotWindow window_prob = new XYZPlotWindow(spec_prob);
		XYZPlotSpec spec_probGain = new XYZPlotSpec(xyzDataProbGain, cpt_probGain, "Log10 Prob Gain (vs Poisson); aper="+(float)aperiodicity, "LogNormTimeSinceLast", "LogNormDuration", "Log10 Prob Gain");
		XYZPlotWindow window_probGain = new XYZPlotWindow(spec_probGain);

	}

	

	
	
	



	
	


	/**
	 * This is made fast by using a reference calculator (with a reference RI), rather than
	 * redoing the calculation each time .
	 * 
	 * @param aveRecurIntervalYears
	 * @param duration
	 * @return
	 */
	public double computeBPT_ProbFast(double aveRecurIntervalYears, double aveTimeSinceLastYears, double durationYears) {
		return refBPT_DistributionCalc.getCondProb(aveTimeSinceLastYears*refRI/aveRecurIntervalYears, durationYears*refRI/aveRecurIntervalYears);
	}
	
	
	/**
	 * 
	 * @param aveRecurIntervalYears
	 * @param duration
	 * @return
	 */
	public double computeBPT_Prob(double aveRecurIntervalYears, double aveTimeSinceLastYears, double durationYears) {
		double delta = aveRecurIntervalYears/200d;
		int numPts = (int)Math.round((9*aveRecurIntervalYears)/delta);
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		bptCalc.setAll(aveRecurIntervalYears, aperiodicity, delta, numPts);
		return bptCalc.getCondProb(aveTimeSinceLastYears, durationYears);
	}
	
	
	/**
	 * This is made fast by using a reference calculator (with a reference RI), rather than
	 * redoing the calculation each time .
	 * 
	 * @return
	 */
	public double computeBPT_ProbForUnknownDateOfLastFast(double aveRecurIntervalYears, double histOpenIntervalYears, double durationYears) {
		refBPT_DistributionCalc.setDurationAndHistOpenInterval(durationYears*refRI/aveRecurIntervalYears, histOpenIntervalYears*refRI/aveRecurIntervalYears);
		return refBPT_DistributionCalc.getCondProbForUnknownTimeSinceLastEvent();	 
	}
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	public double computeBPT_ProbForUnknownDateOfLast(double aveRecurIntervalYears, double histOpenIntervalYears, double durationYears) {
		double delta = aveRecurIntervalYears/200d;
		int numPts = (int)Math.round((9*aveRecurIntervalYears)/delta);
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		bptCalc.setAll(aveRecurIntervalYears, aperiodicity, delta, numPts, durationYears, histOpenIntervalYears);
		return bptCalc.getCondProbForUnknownTimeSinceLastEvent();	 
	}

	
	/**
	 * This computes the poisson probability of one or more events
	 * @param aveRecurIntevalYears
	 * @param durationYears
	 * @return
	 */
	public static double computePoissonProb(double aveRecurIntevalYears, double durationYears) {
		return 1.0-Math.exp(-durationYears/aveRecurIntevalYears);
	}

	


	/**
	 * This creates a reference BPT distribution calculator for the given aperiodicity and duration
	 * (the calculator uses refRI and deltaT) 
	 * @param bpt_Aperiodicity
	 * @param durationInYears
	 * @return
	 */
	protected static BPT_DistCalc getRef_BPT_DistCalc(double bpt_Aperiodicity) {
		int numPts = (int)Math.round((9*refRI)/deltaT);
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		bptCalc.setAll(refRI, bpt_Aperiodicity, deltaT, numPts);
		return bptCalc;
	}

	
	

	
	/**
	 * This test the slow versus fact computations here, monte carlo sampling over a range of aver recur intervals, 
	 * normalized durations, normalized time since last, and normalize historic open intervals.  Fractional discrepancies
	 * greater than 0.001 are listed.  All are generally small and for large normalized durations or historic open intervals,
	 * where differences in how to avoid numerical problems arise.  I don't think any of these are significant for UCERF3, but
	 * the better test for the latter would be to do it both ways for UCERF3 (test the long way against the fast caclulations).
	 * @param numTests
	 */
	public static void testFastCalculations(int numTests) {
		
		System.out.println("Prob\tdiff\tprob\tprob_fast\taperiodicity\taveRI\tdur\ttimeSince\thistOpenInt\tnormDur\tnormTimeSince\tnormHistOpenInt");
		
		double[] apers = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		for(double aper:apers) {
			ProbabilityModelsCalc calc = new ProbabilityModelsCalc(aper);
			double diffThresh = 0.001;
			for(int i=0;i<numTests; i++) {
				double aveRI = 20 +Math.random()*1.0e5;	  // get mean between 20 and 100,020 years
				double normDur = 0.001+Math.random()*5.0; // get normalized duration between 0.001 and 5.001
				double normTimeSince = Math.random()*5.0; // get normalized time since last between and 5
				double normHistOpenInt = normTimeSince;
				double dur = normDur*aveRI;
				double timeSince = normTimeSince*aveRI;
				double histOpenInt = normHistOpenInt*aveRI;
				
				double prob1 = calc.computeBPT_Prob(aveRI, timeSince, dur);
				double prob2 = calc.computeBPT_ProbForUnknownDateOfLast(aveRI, histOpenInt, dur);
				double prob1_fast = calc.computeBPT_ProbFast(aveRI, timeSince, dur);
				double prob2_fast = calc.computeBPT_ProbForUnknownDateOfLastFast(aveRI, histOpenInt, dur);
				
				double diff1 = Math.abs((prob1-prob1_fast)/prob1);
				if(prob1<1e-12 && prob1_fast<1e-12)
					diff1 = 0;

				double diff2 = Math.abs((prob2-prob2_fast)/prob2);
				if(prob2<1e-12 && prob2_fast<1e-12)
					diff2 = 0;
				
				if(diff1>diffThresh)
					System.out.println("Prob1\t"+diff1+"\t"+prob1+"\t"+prob1_fast+"\t"+aper+"\t"+aveRI+"\t"+dur+"\t"+timeSince+"\t"+Double.NaN+"\t"+normDur+"\t"+normTimeSince+"\t"+Double.NaN);
//					throw new RuntimeException("problem with diff1");
				
				if(diff2>diffThresh)
					System.out.println("Prob2\t"+diff2+"\t"+prob2+"\t"+prob2_fast+"\t"+aper+"\t"+aveRI+"\t"+dur+"\t"+Double.NaN+"\t"+histOpenInt+"\t"+normDur+"\t"+Double.NaN+"\t"+normHistOpenInt);
//					throw new RuntimeException("problem with diff2");
			}			
		}
	}
	
	
	
	/**
	 * This simulates events from using elastic rebound probabilities
	 * 
	 * This assumes the rate of each rupture is constant up until the next event is sampled.
	 * 
	 * TODO:
	 * 
	 * Have this ignore non-fault=system ruptures (others take way too long this way).
	 * 
	 * add progress bar
	 * 
	 */
	public void testER_Simulation(String inputDateOfLastFileName, String outputDateOfLastFileName, FaultSystemSolutionERF erf, double numYears) {
		
		boolean aveRecurIntervals = erf.aveRecurIntervalsInU3_BPTcalc;
		boolean aveNormTimeSinceLast = erf.aveNormTimeSinceLastInU3_BPTcalc;
		
		
		// LABELING AND FILENAME STUFF
		String typeCalcForU3_Probs;
		if(aveRecurIntervals)
			typeCalcForU3_Probs = "aveRI";
		else
			typeCalcForU3_Probs = "aveRate";
		if(aveNormTimeSinceLast)
			typeCalcForU3_Probs += "_aveNormTimeSince";
		else
			typeCalcForU3_Probs += "_aveTimeSince";

		String aperString = "aper"+aperiodicity;
		aperString = aperString.replace(".", "pt");
		int tempDur = (int) Math.round(numYears/1000);
		
		String probTypeString;
		ProbabilityModelOptions probTypeEnum = (ProbabilityModelOptions)erf.getParameter(ProbabilityModelParam.NAME).getValue();
		if (probTypeEnum == ProbabilityModelOptions.POISSON) {
			probTypeString= "Pois";
		}
		else if(probTypeEnum == ProbabilityModelOptions.U3_BPT) {
			probTypeString= "U3BPT";
		}
		else if(probTypeEnum == ProbabilityModelOptions.WG02_BPT) {
			probTypeString= "WG02BPT";
		}
		else
			throw new RuntimeException("Porbability type unrecognized");
		
		String dirNameForSavingFiles = "U3ER_"+probTypeString+"_"+tempDur+"kyr";
		if(probTypeEnum != ProbabilityModelOptions.POISSON) {
			dirNameForSavingFiles += "_"+aperString;
			dirNameForSavingFiles += "_"+typeCalcForU3_Probs;
		}
		
		String plotLabelString = probTypeString;
		if(probTypeEnum == ProbabilityModelOptions.U3_BPT)
			plotLabelString += " (aper="+aperiodicity+", "+typeCalcForU3_Probs+")";
		else if(probTypeEnum == ProbabilityModelOptions.WG02_BPT)
			plotLabelString += " (aper="+aperiodicity+")";

		File resultsDir = new File(dirNameForSavingFiles);
		if(!resultsDir.exists()) resultsDir.mkdir();

		
		// INTIALIZE THINGS:
		
		double[] probGainForFaultSystemSource = new double[erf.getNumFaultSystemSources()];
		for(int s=0;s<probGainForFaultSystemSource.length;s++)
			probGainForFaultSystemSource[s] = 1.0;	// default is 1.0

		// set original start time and total duration
		long origStartTimeMillis = 0;
		if(probTypeEnum != ProbabilityModelOptions.POISSON)
			origStartTimeMillis = erf.getTimeSpan().getStartTimeInMillis();
		double origStartYear = ((double)origStartTimeMillis)/MILLISEC_PER_YEAR+1970.0;
		System.out.println("orig start time: "+origStartTimeMillis+ " millis ("+origStartYear+" yrs)");
		System.out.println("numYears: "+numYears);
		
		double simDuration = 1.0;	// 1 year; this could be the expected time to next event?
		
		// initialize some things
		ArrayList<Double> normalizedRupRecurIntervals = new ArrayList<Double>();
		ArrayList<Double> normalizedSectRecurIntervals = new ArrayList<Double>();
		ArrayList<Double> yearsIntoSimulation = new ArrayList<Double>();
		ArrayList<Double> totRateAtYearsIntoSimulation = new ArrayList<Double>();
    	ArbDiscrEmpiricalDistFunc_3D normRI_AlongStrike = new ArbDiscrEmpiricalDistFunc_3D(0.05d,0.95d,10);
		double[] obsSectRateArray = new double[numSections];
		double[] obsSectSlipRateArray = new double[numSections];
		double[] obsSectRateArrayM6pt05to6pt65 = new double[numSections];
		double[] obsSectRateArrayM7pt95to8pt25 = new double[numSections];
		double[] obsRupRateArray = new double[erf.getTotNumRups()];

		int numRups=0;
		RandomDataImpl randomDataSampler = new RandomDataImpl();	// apache tool for sampling from exponential distribution here
		
		// set the forecast as Poisson to get long-term rates (and update)
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.POISSON);
		erf.updateForecast();

		
		// fill in totalRate, longTermRateOfNthRups, magOfNthRups, and longTermSlipRateForSectArray
		double totalRate=0;
		IntegerPDF_FunctionSampler nthRupRandomSampler = new IntegerPDF_FunctionSampler(erf.getTotNumRups());
		double[] longTermRateOfNthRups = new double[erf.getTotNumRups()];	// this will include any aftershock reductions
		double[] magOfNthRups = new double[erf.getTotNumRups()];
		double[] longTermSlipRateForSectArray = new double[numSections];
		int nthRup=0;
		for(ProbEqkSource src:erf) {
			for(ProbEqkRupture rup:src) {
				double rate = rup.getMeanAnnualRate(erf.getTimeSpan().getDuration());
				longTermRateOfNthRups[nthRup] = rate;
				magOfNthRups[nthRup] = rup.getMag();
				totalRate += longTermRateOfNthRups[nthRup];
				nthRupRandomSampler.set(nthRup, rate);
				// slip rates
				int fltSysIndex = erf.getFltSysRupIndexForNthRup(nthRup);
				List<Integer> sectIndices = fltSysRupSet.getSectionsIndicesForRup(fltSysIndex);
				double[] slips =  ((InversionFaultSystemRupSet) fltSysRupSet).getSlipOnSectionsForRup(fltSysIndex);
				for(int s=0;s<sectIndices.size();s++) {
					int sectID = sectIndices.get(s);
					longTermSlipRateForSectArray[sectID] += rate*slips[s];
				}

				nthRup+=1;
			}
		}
		System.out.println("totalRate long term = "+totalRate);
		
		double totalLongTermRate = totalRate;
		

		// Make local sectIndexArrayForSrcList for faster simulations
		ArrayList<int[]> sectIndexArrayForSrcList = new ArrayList<int[]>();
		for(int s=0; s<erf.getNumFaultSystemSources();s++) {
			List<Integer> indexList = fltSysRupSet.getSectionsIndicesForRup(erf.getFltSysRupIndexForSource(s));
			int[] indexArray = new int[indexList.size()];
			for(int i=0;i<indexList.size();i++)
				indexArray[i] = indexList.get(i);
			sectIndexArrayForSrcList.add(indexArray);
		}

		
		// for plotting SAF events
		ArrayList<XY_DataSet> safEventFuncs = new ArrayList<XY_DataSet>();
		ArrayList<PlotCurveCharacterstics> safPlotChars4 = new ArrayList<PlotCurveCharacterstics>();

		
		// make the target MFD - 
		if(D) System.out.println("Making target MFD");
		SummedMagFreqDist targetMFD = ERF_Calculator.getTotalMFD_ForERF(erf, 5.05, 8.95, 40, true);
		double origTotMoRate = ERF_Calculator.getTotalMomentRateInRegion(erf, null);
		System.out.println("originalTotalMomentRate: "+origTotMoRate);
		targetMFD.setName("Target MFD");
		String tempString = "total rate = "+(float)targetMFD.getTotalIncrRate();
		tempString += "\ntotal rate >= 6.7 = "+(float)targetMFD.getCumRate(6.75);
		tempString += "\ntotal MoRate = "+(float)origTotMoRate;
		targetMFD.setInfo(tempString);
		
//		System.out.println(targetMFD);

		// MFD for simulation
		SummedMagFreqDist obsMFD = new SummedMagFreqDist(5.05,8.95,40);
		double obsMoRate = 0;
		
		// set the ave cond recurrence intervals
		double[] aveCondRecurIntervalForFltSysRups;
		if(aveRecurIntervals) {
			if(aveCondRecurIntervalForFltSysRups_type1 == null)
				aveCondRecurIntervalForFltSysRups_type1 = computeAveCondRecurIntervalForFltSysRups(1);
			aveCondRecurIntervalForFltSysRups = aveCondRecurIntervalForFltSysRups_type1;
		}
		else {
			if(aveCondRecurIntervalForFltSysRups_type2 == null)
				aveCondRecurIntervalForFltSysRups_type2 = computeAveCondRecurIntervalForFltSysRups(2);
			aveCondRecurIntervalForFltSysRups = aveCondRecurIntervalForFltSysRups_type2;			
		}

		// print minimum and maximum conditional rate of rupture
		double minCondRI=Double.MAX_VALUE,maxCondRI=0;
		for(double ri: aveCondRecurIntervalForFltSysRups) {
			if(!Double.isInfinite(ri)) {
				if(ri < minCondRI) minCondRI = ri;
				if(ri > maxCondRI) maxCondRI = ri;
			}
		}
		System.out.println("minCondRI="+minCondRI);
		System.out.println("maxCondRI="+maxCondRI);
		
		// initialize things
		double currentYear=origStartYear;
		long currentTimeMillis = origStartTimeMillis;
		
		// this is to track progress
		int percDoneThresh=0;
		int percDoneIncrement=5;

		long startRunTime = System.currentTimeMillis();
		
		// read section date of last file if not null
		if(inputDateOfLastFileName != null && probTypeEnum != ProbabilityModelOptions.POISSON)
			readSectTimeSinceLastEventFromFile(inputDateOfLastFileName, currentTimeMillis);
		else {
			checkTimeSinceLast(currentTimeMillis, "From Pref Data");
		}
		
		boolean firstEvent = true;
		while (currentYear<numYears+origStartYear) {
			
			// write progress
			int percDone = (int)Math.round(100*(currentYear-origStartYear)/numYears);
			if(percDone >= percDoneThresh) {
				double timeInMin = ((double)(System.currentTimeMillis()-startRunTime)/(1000.0*60.0));
				int numGoodDateOfLast=0;
				for(long dateOfLast:dateOfLastForSect) {
					if(dateOfLast != Long.MIN_VALUE)
						numGoodDateOfLast+=1;					
				}
				int percentGood = (int)Math.round((100.0*(double)numGoodDateOfLast/(double)dateOfLastForSect.length));
				System.out.println("\n"+percDoneThresh+"% done in "+(float)timeInMin+" minutes"+";  totalRate="+(float)totalRate+"; yr="+(float)currentYear+";  % sect with date of last = "+percentGood+"\n");	
				// plot prob-gain and/or norm date since last histogram
//				plotRupProbGainHistogram(percDoneThresh+"% done; "+probTypeString);
//				plotSectNormTimeSinceLastEventHistogram(percDoneThresh+"% done; "+probTypeString);
//				if(percDoneThresh == 100) {
//					tempExamineMaxGain(timeSpan.getStartTimeCalendar().getTimeInMillis());
//				}
				percDoneThresh += percDoneIncrement;
			}
			
			System.out.println(numRups+"\t"+currentYear+"\t"+totalRate);
			
			yearsIntoSimulation.add(currentYear);
			totRateAtYearsIntoSimulation.add(totalRate);

			
			// sample time of next event
			double timeToNextInYrs = randomDataSampler.nextExponential(1.0/totalRate);
			long eventTimeMillis = currentTimeMillis + (long)(timeToNextInYrs*MILLISEC_PER_YEAR);
			// System.out.println("Event time: "+eventTimeMillis+" ("+(timeToNextInYrs+timeOfNextInYrs)+" yrs)");

			// sample an event
			nthRup = nthRupRandomSampler.getRandomInt();
			int srcIndex = erf.getSrcIndexForNthRup(nthRup);
			
			obsRupRateArray[nthRup] += 1;

			// set that fault system event has occurred (and save normalized RI)
			if(srcIndex < erf.getNumFaultSystemSources()) {	// ignore other sources
				int fltSystRupIndex = erf.getFltSysRupIndexForSource(srcIndex);
				
				// compute and save the normalize recurrence interval if all sections had date of last
				if(aveNormTimeSinceLast) {	// average time since last
					double aveNormYearsSinceLast = getAveNormTimeSinceLastEventWhereKnown(fltSystRupIndex, eventTimeMillis);
					if(allSectionsHadDateOfLast) {
						normalizedRupRecurIntervals.add(aveNormYearsSinceLast);
					}					
				}
				else {
					long aveDateOfLastMillis = getAveDateOfLastEventWhereKnown(fltSystRupIndex);
					if(allSectionsHadDateOfLast) {
						double timeSinceLast = (eventTimeMillis-aveDateOfLastMillis)/MILLISEC_PER_YEAR;
						normalizedRupRecurIntervals.add(timeSinceLast/aveCondRecurIntervalForFltSysRups[fltSystRupIndex]);
					}					
				}
				
				// save normalized fault section recurrence intervals & RI along strike
				HistogramFunction sumRI_AlongHist = new HistogramFunction(normRI_AlongStrike.getMinX(), normRI_AlongStrike.getMaxX(), normRI_AlongStrike.getNumX());
				HistogramFunction numRI_AlongHist = new HistogramFunction(normRI_AlongStrike.getMinX(), normRI_AlongStrike.getMaxX(), normRI_AlongStrike.getNumX());
				int[] sectID_Array = sectIndexArrayForSrcList.get(erf.getSrcIndexForFltSysRup(fltSystRupIndex));
				double slips[] = ((InversionFaultSystemRupSet) fltSysRupSet).getSlipOnSectionsForRup(erf.getFltSysRupIndexForNthRup(nthRup));
				// obsSectSlipRateArray
				int numSectInRup=sectID_Array.length;
				int ithSectInRup=0;
				for(int sect : sectID_Array) {
					obsSectSlipRateArray[sect] += slips[ithSectInRup];
					long timeOfLastMillis = dateOfLastForSect[sect];
					if(timeOfLastMillis != Long.MIN_VALUE) {
						double normYrsSinceLast = ((eventTimeMillis-timeOfLastMillis)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sect];
						normalizedSectRecurIntervals.add(normYrsSinceLast);
						
						double normDistAlong = ((double)ithSectInRup+0.5)/(double)numSectInRup;
						sumRI_AlongHist.add(normDistAlong, normYrsSinceLast);
						numRI_AlongHist.add(normDistAlong, 1.0);
					}
					ithSectInRup += 1;
				}
				// now put above averages in normRI_AlongStrike
				if(numSectInRup>10) {
					for(int i =0;i<sumRI_AlongHist.getNum();i++) {
						double num = numRI_AlongHist.getY(i);
						if(num > 0) {
							normRI_AlongStrike.set(sumRI_AlongHist.getX(i), sumRI_AlongHist.getY(i)/num, 1.0);
						}
					}				
				}
				
				// make SAF event plotting funcs (ONLY A FIRST 10000 YEARS)
				double numYrs = (eventTimeMillis-origStartTimeMillis)/MILLISEC_PER_YEAR;
				if(numYrs < 11000 && numYrs > 1000) {
//					int[] sectID_Array = sectIndexArrayForSrcList.get(erf.getSrcIndexForFltSysRup(fltSystRupIndex));
					// make the function showing 10% RI at bottom of plot
					if(firstEvent) {
						for(int s=0;s<fltSysRupSet.getNumSections();s++) {
							double tenPercentRI = 0.1/longTermPartRateForSectArray[s];
							FaultSectionPrefData sectData= fltSysRupSet.getFaultSectionData(s);
							if(sectData.getParentSectionName().contains("San Andreas")) {
								ArbitrarilyDiscretizedFunc newFunc = new ArbitrarilyDiscretizedFunc();
								newFunc.set(sectData.getFaultTrace().first().getLatitude(),tenPercentRI);
								newFunc.set(sectData.getFaultTrace().last().getLatitude(),tenPercentRI);
								safEventFuncs.add(newFunc);
								safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.BLACK));
							}
						}
					}
					// make list of SAF sections in event
					ArrayList<Integer> safSections = new ArrayList<Integer>();
					for(int id : sectID_Array) {
						if(fltSysRupSet.getFaultSectionData(id).getParentSectionName().contains("San Andreas"))
								safSections.add(id);
					}
					if(safSections.size()>0) {
						double[] lats = new double[2*safSections.size()];	// one for each end of the fault section
						ArrayList<Double> shortSectRI_Lats = new ArrayList<Double>();
						for(int i=0;i<safSections.size();i++) {
							lats[2*i] = fltSysRupSet.getFaultSectionData(safSections.get(i)).getFaultTrace().first().getLatitude();
							lats[2*i+1] = fltSysRupSet.getFaultSectionData(safSections.get(i)).getFaultTrace().last().getLatitude();
							
							// check for short interval
							long timeOfLastMillis = dateOfLastForSect[safSections.get(i)];
							if(timeOfLastMillis != Long.MIN_VALUE) {
								double normYrsSinceLast = ((eventTimeMillis-timeOfLastMillis)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[safSections.get(i)];
								if(normYrsSinceLast<0.1) {
									double lat1 = fltSysRupSet.getFaultSectionData(safSections.get(i)).getFaultTrace().first().getLatitude();
									double lat2 = fltSysRupSet.getFaultSectionData(safSections.get(i)).getFaultTrace().last().getLatitude();
									shortSectRI_Lats.add((lat1+lat2)/2);
								}
							}

						}
						double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
						for(double val: lats) {
							if(min>val) min = val;
							if(max<val) max = val;
						}
						ArbitrarilyDiscretizedFunc newFunc = new ArbitrarilyDiscretizedFunc();
						newFunc.set(min,eventTimeMillis/MILLISEC_PER_YEAR);
						newFunc.set(max,eventTimeMillis/MILLISEC_PER_YEAR);
						
						safEventFuncs.add(newFunc);
						
						safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.GRAY));
						
//						double mag = magOfNthRups[nthRup];
//						if(mag<6.5)
//							safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.BLUE));
//						else if(mag<7)
//							safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.GREEN));
//						else if(mag<7.5)
//							safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.ORANGE));
//						else if(mag<8)
//							safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.RED));
//						else
//							safPlotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.MAGENTA));
						
						// plot circles where there are short section RIs
						if(shortSectRI_Lats.size()>0) {
							DefaultXY_DataSet shortRIsFunc = new DefaultXY_DataSet();
							for(double lat:shortSectRI_Lats) {
								shortRIsFunc.set(lat, eventTimeMillis/MILLISEC_PER_YEAR);
							}
							safEventFuncs.add(shortRIsFunc);
							safPlotChars4.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.BLACK));
						}
					}			
				}

				
				// reset last event time and increment simulated/obs rate on sections
				for(int sect:sectIndexArrayForSrcList.get(srcIndex)) {
					dateOfLastForSect[sect] = eventTimeMillis;
					obsSectRateArray[sect] += 1.0; // add the event
					
					double mag = magOfNthRups[nthRup];
					if(mag>6 && mag<6.7)
						obsSectRateArrayM6pt05to6pt65[sect] += 1;
					else if (mag>7.9 && mag<8.3)
						obsSectRateArrayM7pt95to8pt25[sect] += 1;
				}
			}

			numRups+=1;
			obsMFD.addResampledMagRate(magOfNthRups[nthRup], 1.0, true);
			obsMoRate += MagUtils.magToMoment(magOfNthRups[nthRup]);
			
			// increment time
			currentYear += timeToNextInYrs;
			currentTimeMillis = eventTimeMillis;
			
//			System.out.println("currentYear="+currentYear+"; currentTimeMillis="+currentTimeMillis+"; timeToNextInYrs="+timeToNextInYrs);
			
//ArrayList<Double> normRI_List=null;
//if(firstEvent)
//	normRI_List = new ArrayList<Double>();

			// update gains and sampler if not Poisson
			if(probTypeEnum != ProbabilityModelOptions.POISSON) {
				// first the gains
				if(probTypeEnum == ProbabilityModelOptions.U3_BPT) {
					for(int s=0;s<erf.getNumFaultSystemSources();s++) {
						int fltSysRupIndex = erf.getFltSysRupIndexForSource(s);
						probGainForFaultSystemSource[s] = getU3_ProbGainForRup(fltSysRupIndex, 0.0, false, aveRecurIntervals, aveNormTimeSinceLast, currentTimeMillis, simDuration);
//if(firstEvent) {
//							long aveTimeOfLastMillisWhereKnown = getAveDateOfLastEventWhereKnown(fltSysRupIndex);
//							double test = ((double)(currentTimeMillis-aveTimeOfLastMillisWhereKnown)/MILLISEC_PER_YEAR)/aveCondRecurIntervalForFltSysRups_type1[fltSysRupIndex];
//							if(test<0)
//								throw new RuntimeException("test is negative: "+test);
//							normRI_List.add(test);
//}
					}
//if(firstEvent) {
//	System.out.println("normRI_List.size()="+normRI_List.size());
//	HistogramFunction dist = ProbModelsPlottingUtils.getNormRI_Distribution(normRI_List, 0.1);
//	GraphWindow graph = new GraphWindow(dist, "test"); 	
//}

				}
				else if(probTypeEnum == ProbabilityModelOptions.WG02_BPT) {
					sectionGainArray=null; // set this null so it gets updated
					for(int s=0;s<erf.getNumFaultSystemSources();s++) {
						int fltSysRupIndex = erf.getFltSysRupIndexForSource(s);
						probGainForFaultSystemSource[s] = getWG02_ProbGainForRup(fltSysRupIndex, false, currentTimeMillis, simDuration);
					}
				}		
				// System.out.println("allProbGains took (sec): "+(System.currentTimeMillis()-millis)/1e3);
//				System.out.println((float)probGainForFaultSystemSource[srcIndex]);
				
				// now update totalRate and ruptureSampler (for all rups since start time changed)
				for(int n=0; n<erf.getTotNumRupsFromFaultSystem();n++) {
//					double newRate = longTermRateOfNthRups[n] * probGainForFaultSystemSource[srcIndexForNthRup[n]] * correctionMFD.getClosestY(magOfNthRups[n]);
					double newRate = longTermRateOfNthRups[n] * probGainForFaultSystemSource[erf.getSrcIndexForNthRup(n)];
					nthRupRandomSampler.set(n, newRate);
				}
				totalRate = nthRupRandomSampler.getSumOfY_vals();				
			}
			firstEvent=false;
		}
		
		
		// write section date of last file if not null
		if(outputDateOfLastFileName != null)
			writeSectTimeSinceLastEventToFile(outputDateOfLastFileName, currentTimeMillis);
		
		String infoString = dirNameForSavingFiles;
		infoString += "\ninputDateOfLastFileName: "+inputDateOfLastFileName;
		infoString += "\nnumRups="+numRups;
		
		// get normalized section recurrence interval plots
		ArrayList<EvenlyDiscretizedFunc> funcList = ProbModelsPlottingUtils.getNormRI_DistributionWithFits(normalizedRupRecurIntervals, aperiodicity);
		GraphWindow grapha_a = ProbModelsPlottingUtils.plotNormRI_DistributionWithFits(funcList, "Normalized Rupture RIs; "+plotLabelString);
//		GraphWindow grapha_a = General_EQSIM_Tools.plotNormRI_Distribution(normalizedRupRecurIntervals, "Normalized Rupture RIs; "+plotLabelString, aperiodicity);
		infoString += "\n\nRup "+funcList.get(0).getName()+":";
		infoString += "\n"+funcList.get(0).getInfo();
		infoString += "\n\n"+funcList.get(1).getName();
		infoString += "\n"+funcList.get(1).getInfo();

		
		ArrayList<EvenlyDiscretizedFunc> funcList2 = ProbModelsPlottingUtils.getNormRI_DistributionWithFits(normalizedSectRecurIntervals, aperiodicity);
		GraphWindow graph2_b = ProbModelsPlottingUtils.plotNormRI_DistributionWithFits(funcList2, "Normalized Section RIs; "+plotLabelString);
//		GraphWindow graph2_b = General_EQSIM_Tools.plotNormRI_Distribution(normalizedSectRecurIntervals, "Normalized Section RIs; "+plotLabelString, aperiodicity);
		infoString += "\n\nSect "+funcList2.get(0).getName()+":";
		infoString += "\n"+funcList2.get(0).getInfo();

		
		// plot long-term rate versus time
		DefaultXY_DataSet totRateVersusTime = new DefaultXY_DataSet(yearsIntoSimulation,totRateAtYearsIntoSimulation);
		double meanTotRate=0;
		for(double rt:totRateAtYearsIntoSimulation) {
			meanTotRate+=rt/totRateAtYearsIntoSimulation.size();
		}
		totRateVersusTime.setName("Total Rate vs Time");
		totRateVersusTime.setInfo("Mean Total Rate = "+meanTotRate+"\nLong Term Rate = "+totalLongTermRate);
		DefaultXY_DataSet longTermRateFunc = new DefaultXY_DataSet();
		longTermRateFunc.set(totRateVersusTime.getMinX(),totalLongTermRate);
		longTermRateFunc.set(totRateVersusTime.getMaxX(),totalLongTermRate);
		longTermRateFunc.setName("Long term rate");
		ArrayList<DefaultXY_DataSet> funcsTotRate = new ArrayList<DefaultXY_DataSet>();
		funcsTotRate.add(totRateVersusTime);
		funcsTotRate.add(longTermRateFunc);
		ArrayList<PlotCurveCharacterstics> plotCharsTotRate = new ArrayList<PlotCurveCharacterstics>();
		plotCharsTotRate.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLUE));
		plotCharsTotRate.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow graphTotalRateVsTime = new GraphWindow(funcsTotRate, "Total Rate vs Time; "+plotLabelString, plotCharsTotRate); 
		graphTotalRateVsTime.setX_AxisLabel("Times (years)");
		graphTotalRateVsTime.setY_AxisLabel("Total Rate (per year)");



		// plot MFDs
		obsMFD.scale(1.0/numYears);
		obsMFD.setName("Simulated MFD");
		obsMoRate /= numYears;
		double obsTotRate = obsMFD.getTotalIncrRate();
		double rateRatio = obsTotRate/targetMFD.getTotalIncrRate();
		String infoString2 = "total rate = "+(float)obsTotRate+" (ratio="+(float)rateRatio+")";
		double obsTotRateAbove6pt7 = obsMFD.getCumRate(6.75);
		double rateAbove6pt7_Ratio = obsTotRateAbove6pt7/targetMFD.getCumRate(6.75);
		infoString2 += "\ntotal rate >= 6.7 = "+(float)obsTotRateAbove6pt7+" (ratio="+(float)rateAbove6pt7_Ratio+")";
		double moRateRatio = obsMoRate/origTotMoRate;
		infoString2 += "\ntotal MoRate = "+(float)obsMoRate+" (ratio="+(float)moRateRatio+")";
		obsMFD.setInfo(infoString2);
		
		infoString += "\n\nSimulationStats:\n";
		infoString += "totRate\tratio\ttotRateM>=6.7\tratio\ttotMoRate\tratio\n";
		infoString += (float)obsTotRate+"\t"+(float)rateRatio+"\t"+(float)obsTotRateAbove6pt7+"\t"+(float)rateAbove6pt7_Ratio+"\t"+(float)obsMoRate+"\t"+(float)moRateRatio;
		
		// write this now in case of crash
		FileWriter info_fr;
		try {
			info_fr = new FileWriter(dirNameForSavingFiles+"/infoString.txt");
			info_fr.write(infoString);
			info_fr.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		
		System.out.println("INFO STRING:\n\n"+infoString);

		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(targetMFD);
		funcs.add(obsMFD);
		funcs.add(targetMFD.getCumRateDistWithOffset());
		funcs.add(obsMFD.getCumRateDistWithOffset());
		GraphWindow graph = new GraphWindow(funcs, "Incremental Mag-Freq Dists; "+plotLabelString); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setYLog(true);	// this causes problems
		graph.setY_AxisRange(1e-4, 1.0);
		graph.setX_AxisRange(5.5, 8.5);
		
		// plot observed versus imposed rup rates
		for(int i=0;i<obsRupRateArray.length;i++) {
			obsRupRateArray[i] = obsRupRateArray[i]/numYears;
		}
		
		// THIS FOR RUPTURES ON ONE PARKFIELD SECTION
//		int testSectIndex = 1927; // San Andreas (Parkfield), Subsection 7
//		String testSectName = invRupSet.getFaultSectionData(testSectIndex).getName();
//		System.out.println("testSection: "+testSectName);
//		ArrayList<Integer> nthRupsForSect = new ArrayList<Integer>();
//		List<Integer> fltSysRupIDsForTestSectIndex = invRupSet.getRupturesForSection(testSectIndex);
//		for(int fltSysRupIndex:fltSysRupIDsForTestSectIndex) {
//			int srcIndex = srcIndexForFltSysRup[fltSysRupIndex];
//			if(srcIndex != -1) {
//				int[] nthRups = nthRupIndicesForSource.get(srcIndex);
//				for(int r=0; r<nthRups.length;r++)
//					nthRupsForSect.add(nthRups[r]);
//			}
//		}
//		double totObs = 0;
//		double totImposed = 0;
//		double[] tempObs = new double[nthRupsForSect.size()];
//		double[] tempImposed = new double[nthRupsForSect.size()];
//		for(int i=0; i<nthRupsForSect.size();i++) {
//			tempObs[i] = obsRupRateArray[nthRupsForSect.get(i)];
//			totObs+=tempObs[i];
//			tempImposed[i] = longTermRateOfNthRups[nthRupsForSect.get(i)];
//			totImposed+=tempImposed[i];
//		}
//		System.out.println(testSectName+"\ttotObs="+totObs+";\ttotImposed="+totImposed+"\ttratio="+(totObs/totImposed));
//		DefaultXY_DataSet obsVsImposedRupRates = new DefaultXY_DataSet(tempImposed,tempObs);
		
		// OR THIS FOR ALL RUPTURES:
		DefaultXY_DataSet obsVsImposedRupRates = new DefaultXY_DataSet(longTermRateOfNthRups,obsRupRateArray);
		
		obsVsImposedRupRates.setName("Simulated vs Imposed Rup Rates");
		DefaultXY_DataSet perfectAgreementFunc4 = new DefaultXY_DataSet();
		perfectAgreementFunc4.set(1e-5,1e-5);
		perfectAgreementFunc4.set(0.05,0.05);
		perfectAgreementFunc4.setName("Perfect agreement line");
		ArrayList<DefaultXY_DataSet> funcs4 = new ArrayList<DefaultXY_DataSet>();
		funcs4.add(obsVsImposedRupRates);
		funcs4.add(perfectAgreementFunc4);
		ArrayList<PlotCurveCharacterstics> plotChars4 = new ArrayList<PlotCurveCharacterstics>();
		plotChars4.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLUE));
		plotChars4.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow graph4 = new GraphWindow(funcs4, "Obs vs Imposed Rup Rates; "+plotLabelString, plotChars4); 
		graph4.setX_AxisRange(5d/numYears, 0.01);
		graph4.setY_AxisRange(5d/numYears, 0.01);
		graph4.setYLog(true);
		graph4.setXLog(true);
		graph4.setX_AxisLabel("Imposed Rup Rate (per yr)");
		graph4.setY_AxisLabel("Simulated Rup Rate (per yr)");

		
		
		// plot SAF events
		GraphWindow graph9 = new GraphWindow(safEventFuncs, "SAF events; "+plotLabelString, safPlotChars4); 
		graph9.setX_AxisRange(36.8, 40.2);
		graph9.setY_AxisRange(1000, 11000);
		graph9.setX_AxisLabel("Latitute");
		graph9.setY_AxisLabel("Year");
		graph9.setSize(240, 800);

		
		// plot observed versus imposed section slip rates
		for(int i=0;i<obsSectSlipRateArray.length;i++) {
			obsSectSlipRateArray[i] = obsSectSlipRateArray[i]/numYears;
		}
		DefaultXY_DataSet obsVsImposedSectSlipRates = new DefaultXY_DataSet(longTermSlipRateForSectArray,obsSectSlipRateArray);
		obsVsImposedSectSlipRates.setName("Simulated vs Imposed Section Slip Rates");
		DefaultXY_DataSet perfectAgreementSlipRateFunc = new DefaultXY_DataSet();
		perfectAgreementSlipRateFunc.set(1e-5,1e-5);
		perfectAgreementSlipRateFunc.set(0.05,0.05);
		perfectAgreementSlipRateFunc.setName("Perfect agreement line");
		ArrayList<DefaultXY_DataSet> funcsSR = new ArrayList<DefaultXY_DataSet>();
		funcsSR.add(obsVsImposedSectSlipRates);
		funcsSR.add(perfectAgreementSlipRateFunc);
		ArrayList<PlotCurveCharacterstics> plotCharsSR = new ArrayList<PlotCurveCharacterstics>();
		plotCharsSR.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLUE));
		plotCharsSR.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow graphSR = new GraphWindow(funcsSR, "Obs vs Imposed Section Slip Rates; "+plotLabelString, plotCharsSR); 
		graphSR.setX_AxisRange(1e-5, 0.05);
		graphSR.setY_AxisRange(1e-5, 0.05);
		graphSR.setYLog(true);
		graphSR.setXLog(true);
		graphSR.setX_AxisLabel("Imposed Section Slip Rate (mm/yr)");
		graphSR.setY_AxisLabel("Simulated Section Slip Rate (mm/yr)");
		
		
		// plot observed versus imposed section rates
		for(int i=0;i<obsSectRateArray.length;i++) {
			obsSectRateArray[i] = obsSectRateArray[i]/numYears;
			obsSectRateArrayM6pt05to6pt65[i] = obsSectRateArrayM6pt05to6pt65[i]/numYears;
			obsSectRateArrayM7pt95to8pt25[i] = obsSectRateArrayM7pt95to8pt25[i]/numYears;
		}
		DefaultXY_DataSet obsVsImposedSectRates = new DefaultXY_DataSet(longTermPartRateForSectArray,obsSectRateArray);
		obsVsImposedSectRates.setName("Simulated vs Imposed Section Event Rates");
		DefaultXY_DataSet perfectAgreementFunc = new DefaultXY_DataSet();
		perfectAgreementFunc.set(1e-5,1e-5);
		perfectAgreementFunc.set(0.05,0.05);
		perfectAgreementFunc.setName("Perfect agreement line");
		ArrayList<DefaultXY_DataSet> funcs2 = new ArrayList<DefaultXY_DataSet>();
		funcs2.add(obsVsImposedSectRates);
		funcs2.add(perfectAgreementFunc);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow graph2 = new GraphWindow(funcs2, "Obs vs Imposed Section Rates; "+probTypeString, plotChars); 
		graph2.setX_AxisRange(5d/numYears, 0.05);
		graph2.setY_AxisRange(5d/numYears, 0.05);
		graph2.setYLog(true);
		graph2.setXLog(true);
		graph2.setX_AxisLabel("Imposed Section Participation Rate (per yr)");
		graph2.setY_AxisLabel("Simulated Section Participation Rate (per yr)");
		
		
		// plot ave norm RI along strike
		ArrayList<EvenlyDiscretizedFunc> funcs8 = new ArrayList<EvenlyDiscretizedFunc>();
		EvenlyDiscretizedFunc meanAlongFunc = normRI_AlongStrike.getMeanCurve();
		meanAlongFunc.setName("mean");
		funcs8.add(normRI_AlongStrike.getMeanCurve());
		EvenlyDiscretizedFunc alongFunc2pt5 = normRI_AlongStrike.getInterpolatedFractileCurve(0.025);
		EvenlyDiscretizedFunc alongFunc97pt5 = normRI_AlongStrike.getInterpolatedFractileCurve(0.975);
		alongFunc2pt5.setInfo("2.5 percentile");
		alongFunc97pt5.setInfo("97.5 percentile");
		funcs8.add(alongFunc2pt5);
		funcs8.add(alongFunc97pt5);
		ArrayList<PlotCurveCharacterstics> plotChars8 = new ArrayList<PlotCurveCharacterstics>();
		plotChars8.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotChars8.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars8.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		GraphWindow graph8 = new GraphWindow(funcs8, "Normalized RI vs Normalized Dist Along Strike; "+probTypeString, plotChars8); 
		graph8.setX_AxisLabel("Norm Dist Along Strike");
		graph8.setY_AxisLabel("Normalized RI");
		
		
//		System.out.println(testSectName+"\tobsSectRateArray="+obsSectRateArray[testSectIndex]+
//				"\tlongTermPartRateForSectArray="+longTermPartRateForSectArray[testSectIndex]+"\tratio="+
//				(obsSectRateArray[testSectIndex]/longTermPartRateForSectArray[testSectIndex]));
		
		// write out test section rates
		ArrayList<String> outStringList = new ArrayList<String>();
		int numSect=fltSysRupSet.getNumSections();
		double[] predSectRateArrayM6pt05to6pt65 = new double[numSect];
		double[] predSectRateArrayM7pt95to8pt25 = new double[numSect];
		for(int s=0;s<numSect;s++) {
			double partRateMlow=0;
			double partRateMhigh=0;
			for (int r : fltSysRupSet.getRupturesForSection(s)) {
				double mag = fltSysRupSet.getMagForRup(r);
				if(mag>6 && mag<6.7)
					partRateMlow += fltSysSolution.getRateForRup(r);
				else if (mag>7.9 && mag<8.3)
					partRateMhigh = fltSysSolution.getRateForRup(r);
			}
			predSectRateArrayM6pt05to6pt65[s]=partRateMlow;
			predSectRateArrayM7pt95to8pt25[s]=partRateMhigh;
			outStringList.add(s+"\t"+obsSectRateArray[s]+"\t"+longTermPartRateForSectArray[s]+"\t"+
					(obsSectRateArray[s]/longTermPartRateForSectArray[s])+"\t"+
					predSectRateArrayM6pt05to6pt65[s]+"\t"+
					obsSectRateArrayM6pt05to6pt65[s]+"\t"+
					predSectRateArrayM7pt95to8pt25[s]+"\t"+
					obsSectRateArrayM7pt95to8pt25[s]+"\t"+
					fltSysRupSet.getFaultSectionData(s).getName()+"\n");
		}
		File dataFile = new File(resultsDir,File.separator+"testSectRates");
		try {
			FileWriter fileWriter = new FileWriter(dataFile);
			for(String line:outStringList) {
				fileWriter.write(line);
			}
			fileWriter.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		DefaultXY_DataSet obs_pred_ratioForSections = new DefaultXY_DataSet();
		for(int s=0;s<numSect;s++) {
			if(predSectRateArrayM6pt05to6pt65[s] >= 10.0/numYears) {	// only keep where 10 should have occurred
				obs_pred_ratioForSections.set(predSectRateArrayM6pt05to6pt65[s], obsSectRateArrayM6pt05to6pt65[s]/predSectRateArrayM6pt05to6pt65[s]);
			}
		}
		DefaultXY_DataSet perfectAgreementFunc2 = new DefaultXY_DataSet();
		perfectAgreementFunc2.set(10.0/numYears,1d);
		perfectAgreementFunc2.set(0.1,1d);
		perfectAgreementFunc2.setName("Perfect agreement line");
		ArrayList<DefaultXY_DataSet> funcs3 = new ArrayList<DefaultXY_DataSet>();
		funcs3.add(obs_pred_ratioForSections);
		funcs3.add(perfectAgreementFunc2);
		ArrayList<PlotCurveCharacterstics> plotChars2 = new ArrayList<PlotCurveCharacterstics>();
		plotChars2.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLUE));
		plotChars2.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow graph3 = new GraphWindow(funcs3, "Obs/imposed vs Imposed Section Rates for M 6.0 to 6.7; "+plotLabelString, plotChars2); 
		graph3.setX_AxisLabel("Imposed Section Participation Rate (per yr)");
		graph3.setY_AxisLabel("Ratio of Observed to Imposed");
		
		try {
			// plots
			grapha_a.saveAsPDF(dirNameForSavingFiles+"/normalizedRupRecurIntervals.pdf");
			graph2_b.saveAsPDF(dirNameForSavingFiles+"/normalizedSectRecurIntervals.pdf");
			graphTotalRateVsTime.saveAsPDF(dirNameForSavingFiles+"/totalRateVsTime.pdf");
			graph.saveAsPDF(dirNameForSavingFiles+"/magFreqDists.pdf");
			graph2.saveAsPDF(dirNameForSavingFiles+"/obsVsImposedSectionPartRates.pdf");
			graph3.saveAsPDF(dirNameForSavingFiles+"/obsOverImposedVsImposedSectionPartRates.pdf");
			graph8.saveAsPDF(dirNameForSavingFiles+"/normRI_AlongRupTrace.pdf");
			graph9.saveAsPDF(dirNameForSavingFiles+"/safEventsVsTime.pdf");
			graphSR.saveAsPDF(dirNameForSavingFiles+"/obsVsImposedSectionSlipRates.pdf");
			// data:
			FileWriter fr = new FileWriter(dirNameForSavingFiles+"/normalizedRupRecurIntervals.txt");
			for (double val : normalizedRupRecurIntervals)
				fr.write(val + "\n");
			fr.close();

			fr = new FileWriter(dirNameForSavingFiles+"/normalizedSectRecurIntervals.txt");
			for (double val : normalizedSectRecurIntervals)
				fr.write(val + "\n");
			fr.close();

			AbstractDiscretizedFunc.writeSimpleFuncFile(targetMFD, dirNameForSavingFiles+"/targetMFD.txt");
			AbstractDiscretizedFunc.writeSimpleFuncFile(obsMFD, dirNameForSavingFiles+"/simulatedMFD.txt");
			AbstractDiscretizedFunc.writeSimpleFuncFile(targetMFD.getCumRateDistWithOffset(), dirNameForSavingFiles+"/targetCumMFD.txt");
			AbstractDiscretizedFunc.writeSimpleFuncFile(obsMFD.getCumRateDistWithOffset(), dirNameForSavingFiles+"/simulatedCumMFD.txt");


		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void writeSectTimeSinceLastEventToFile(String fileName, long currentTimeMillis) {		
		if(!dataDir.exists())
			dataDir.mkdir();
		File dataFile = new File(dataDir,File.separator+fileName);
		try {
			FileWriter fileWriter = new FileWriter(dataFile);
			int numBad=0;
			for(int i=0; i<dateOfLastForSect.length;i++) {
				// time since last millis
				if(dateOfLastForSect[i] != Long.MIN_VALUE) {
					long timeSince = currentTimeMillis-dateOfLastForSect[i];	// ti
					if(timeSince < 0) {
						if(timeSince > -MILLISEC_PER_YEAR) {
							System.out.println("Converting slightly negative time since last ("+timeSince+") to zero");
							timeSince=0;
						}
						else {
							throw new RuntimeException("bad time since last");
						}
					}
					fileWriter.write(i+"\t"+timeSince+"\n");					
				}
				else {
					fileWriter.write(i+"\t"+Long.MIN_VALUE+"\n");
					numBad+=1;
				}
			}
			fileWriter.close();
			int percBad = (int)Math.round(100.0*(double)numBad/(double)dateOfLastForSect.length);
			System.out.println(numBad+" sections out of "+dateOfLastForSect.length+" had no date of last event in output file ("+percBad+"%)");
			
			checkTimeSinceLast(currentTimeMillis, "Final");

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void checkTimeSinceLast(long currentTimeMillis, String labelPrefix) {
		
		ArrayList<Double> normRI_List= new ArrayList<Double>();

		for(int s=0; s<dateOfLastForSect.length;s++) {
			long epochOfLast = dateOfLastForSect[s];
			if(epochOfLast != Long.MIN_VALUE) {
				double normTimeSinceLast = longTermPartRateForSectArray[s]*(((double)(currentTimeMillis-epochOfLast))/MILLISEC_PER_YEAR);
				normRI_List.add(normTimeSinceLast);
			}
		}
		
		System.out.println("normRI_List.size()="+normRI_List.size());
		HistogramFunction dist = ProbModelsPlottingUtils.getNormRI_Distribution(normRI_List, 0.1);
		dist.setName(labelPrefix+" NormSectTimeSinceLast");
		dist.setInfo(normRI_List.size()+" of "+dateOfLastForSect.length+" sections had date of last");
		GraphWindow graph = new GraphWindow(dist, labelPrefix+" NormSectTimeSinceLast"); 	
		
	}
	
	
	private void readSectTimeSinceLastEventFromFile(String fileName, long currentTimeMillis) {
		
		try {
			File dataFile = new File(dataDir,File.separator+fileName);
			
			System.out.println("Reading file "+fileName+"; currentTimeMillis+"+currentTimeMillis);
			
			BufferedReader reader = new BufferedReader(scratch.UCERF3.utils.UCERF3_DataUtils.getReader(dataFile.toURL()));
//			BufferedReader reader = new BufferedReader(scratch.UCERF3.utils.UCERF3_DataUtils.getReader(dataFile.getAbsolutePath()));
			int s=0;
			String line;
			int numBad=0;
			while ((line = reader.readLine()) != null) {
				String[] st = StringUtils.split(line,"\t");
				int sectIndex = Integer.valueOf(st[0]);
				long timeSince = Long.valueOf(st[1]);
				if(timeSince != Long.MIN_VALUE) {
					dateOfLastForSect[s] = currentTimeMillis-timeSince;
//					dateOfLastForSect[s] = Long.MIN_VALUE;
				}
				else {
					dateOfLastForSect[s] = Long.MIN_VALUE;
					numBad +=1;
				}
				if(s != sectIndex)
					throw new RuntimeException("bad index");
				s+=1;

			}
			int percBad = (int)Math.round(100.0*(double)numBad/(double)dateOfLastForSect.length);
			System.out.println(numBad+" sections out of "+dateOfLastForSect.length+" had no date of last event in input file ("+percBad+"%)");
			
			checkTimeSinceLast(currentTimeMillis, "Initial");

		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(fileName);
		
		String timeSinceLastFileNamePois = "timeSinceLastForSimulationPois.txt";
//		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.POISSON);
//		erf.updateForecast();
//		ProbabilityModelsCalc testCalc = new ProbabilityModelsCalc(erf);
//		testCalc.testER_Simulation(null, null, erf,100000d);
		
		erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
		boolean aveRecurIntervalsInU3_BPTcalc=false;
		boolean aveNormTimeSinceLastInU3_BPTcalc=true;
		erf.testSetBPT_CalcType(aveRecurIntervalsInU3_BPTcalc,aveNormTimeSinceLastInU3_BPTcalc);
		erf.updateForecast();
		ProbabilityModelsCalc testCalc = new ProbabilityModelsCalc(erf);
		testCalc.testER_Simulation(timeSinceLastFileNamePois, null, erf,10000d);
		
		
		
//		testFastCalculations(10000);
		
		
		

		// This shows that CondProb & CondProbForUnknownDateOfLastEvent look 
		// good for a variety of aperiodicities, nomralized durations, normalized 
		// time since last, and normalized historic open intervals (e.g., no
		// outliers from numerical artifacts).
//		double[] apers = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
//		for(double aper:apers) {
//			System.out.println("working on " +aper);
//			ProbabilityModelsCalc testCalc = new ProbabilityModelsCalc((float)aper);
//			testCalc.plotXYZ_FuncOfCondProbForUnknownDateOfLastEvent();		
//			testCalc.plotXYZ_FuncOfCondProb();
//		}

	}

}
