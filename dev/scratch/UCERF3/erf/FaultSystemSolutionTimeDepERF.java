package scratch.UCERF3.erf;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.RandomDataImpl;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupOrigTimeComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.oldClasses.UCERF2_Final_StirlingGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_PrimaryEventSamplerAlt;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.erf.ETAS.ETAS_Utils;
import scratch.UCERF3.erf.ETAS.IntegerPDF_FunctionSampler;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.ned.ETAS_ERF.ETAS_PrimaryEventSampler;
import scratch.ned.ETAS_ERF.EqksInGeoBlock;
import scratch.ned.ETAS_ERF.EqksInGeoBlockUtils;
import scratch.ned.ETAS_ERF.sandbox.ERF_RatesAtPointsInSpace;
import scratch.ned.ETAS_ERF.sandbox.ERF_RatesInSpace;
import scratch.ned.ETAS_ERF.sandbox.ETAS_LocationWeightCalculator;
import scratch.ned.ETAS_ERF.sandbox.EqksAtPoint;

/**
 * This class adds elastic-rebound-based time dependence to the FaultSystemSolutionPoissonERF.
 * 
 * TODO:
 * 
 * 0) try tuning aper correction to match target
 * 
 * 1) try src.scaleRupProbs(probGain) - did it, same result.
 * 
 * 2) make sure timeSpan deals with UTC correctly
 * 
 * 3) finalize use of SIMULATION_MODE field
 */
public class FaultSystemSolutionTimeDepERF extends FaultSystemSolutionPoissonERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = false;
	
	public static final String NAME = "Fault System Solution Time Dep ERF";
	
	public final static double MILLISEC_PER_YEAR = 1000*60*60*24*365.25;
	public final static long MILLISEC_PER_DAY = 1000*60*60*24;
	
	protected InversionFaultSystemRupSet invRupSet;
	protected InversionFaultSystemSolution invSol;
	
	// this is the probability gain for each fault system source
	double[] probGainForFaultSystemSource;
	double[] oldGainForFaultSystemSource;
	
	// aperiodicity parameter and primitive
	protected BPT_AperiodicityParam bpt_AperiodicityParam;
	double bpt_Aperiodicity;
	boolean bpt_AperiodicityChanged;
	
	// these fields are for simulation mode (stochastic event sets).
	public boolean SIMULATION_MODE = true;
	ArrayList<Double> normalizedRupRecurIntervals;
	ArrayList<Double> normalizedSectRecurIntervals;
	protected double totalRate;
	IntegerPDF_FunctionSampler spontaneousRupSampler=null;
	double[] longTermRateOfNthRups;
	double[] magOfNthRups;
	double[] aveCondRecurIntervalForFltSysRups = null;	// the recurrence interval of each rupture conditioned on the fact that it is the next event to occur
	double[] longTermPartRateForSectArray;
	
	// for BPT conditional prob calculations (200 year recurrence interval)
	static double refRI = 200;
	static double refSimDur = 1d;	// 1 year for the reference simulator duration
	static double deltaT = 0.1;
	BPT_DistCalc refBPT_DistributionCalc;	
	ArbitrarilyDiscretizedFunc bptTimeToPoisCondProbFunc;
	
	// for ave date of last event calculations
	double[] areaForSect;	// compute these oence to speed things up
	double totRupArea, totRupAreaWithDateOfLast;
	boolean allSectionsHadDateOfLast;
	
	// cached for speed:
	ArrayList<int[]> sectIndexArrayForSrcList;
	long[] dateOfLastForSect;
	double[] bptTimeToPoisCondProbFuncForSect;

	
	final static File dataDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR,File.separator+"erSimulations");
	
	/**
	 * This creates the ERF from the given FaultSystemSolution.  FileParameter is removed 
	 * from the adjustable parameter list (to prevent changes after instantiation).
	 * @param invSol
	 */
	public FaultSystemSolutionTimeDepERF(FaultSystemSolution invSol) {
		super(invSol);
		initAdjustableParams();
		initiateTimeSpan();
		aleatoryMagAreaStdDevChanged = true;	// set so everything is updated in updateForecast()
	}

	
	/**
	 * This creates the ERF from the given fullPathInputFile.  FileParameter is removed 
	 * from the adjustable parameter list (to prevent changes after instantiation).
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionTimeDepERF(String fullPathInputFile) {
		super(fullPathInputFile);
		initAdjustableParams();
		initiateTimeSpan();
	}

	
	/**
	 * This creates the ERF with a parameter for setting the input file
	 * (e.g., from a GUI).
	 */
	public FaultSystemSolutionTimeDepERF() {
		super();
		initAdjustableParams();
		initiateTimeSpan();
	}
	
	@Override
	protected void setSolution(FaultSystemSolution sol) {
		Preconditions.checkState(sol instanceof InversionFaultSystemSolution,
				"Only Inversion Fault System Solutions can be used with Time Dependent FSS ERF");
		this.invSol = (InversionFaultSystemSolution)sol;
		if (invSol != null)
			invRupSet = invSol.getRupSet();
		super.setSolution(sol);
	}


	/**
	 * This initializes the reference BPT calculator & bptTimeToPoisCondProbFunc.
	 */
	protected void initBPT_CondProbCalc() {
		long startTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		long endTime = timeSpan.getEndTimeCalendar().getTimeInMillis();
		double duration = (endTime-startTime)/MILLISEC_PER_YEAR;
		refBPT_DistributionCalc = getRef_BPT_DistCalc(bpt_Aperiodicity, duration);
		bptTimeToPoisCondProbFunc = getBPT_TimeToPoisCondProbFunc(bpt_Aperiodicity);
		
	}
	
	/**
	 *  these are some array used for faster caluclations
	 */
	protected void initArrays() {
		// sections areas
		areaForSect = new double[invSol.getRupSet().getNumSections()];
		for(int s=0; s<invSol.getRupSet().getNumSections();s++) {
			double ddw = invSol.getRupSet().getFaultSectionData(s).getReducedDownDipWidth();
			double length = invSol.getRupSet().getFaultSectionData(s).getFaultTrace().getTraceLength();
			areaForSect[s] = ddw*length;
		}
		
		// Make local sectIndexArrayForSrcList for faster simulations
		sectIndexArrayForSrcList = new ArrayList<int[]>();
		for(int s=0; s<numNonZeroFaultSystemSources;s++) {
			List<Integer> indexList = invSol.getRupSet().getSectionsIndicesForRup(fltSysRupIndexForSource[s]);
			int[] indexArray = new int[indexList.size()];
			for(int i=0;i<indexList.size();i++)
				indexArray[i] = indexList.get(i);
			sectIndexArrayForSrcList.add(indexArray);
		}
		
		// Make date of last event for section array for faster simulations 
		// TODO = note that any actual values in the fault section data objects are not mapped in)
		dateOfLastForSect = new long[invSol.getRupSet().getNumSections()];
		for(int s=0; s<invSol.getRupSet().getNumSections();s++) {
			dateOfLastForSect[s]=Long.MIN_VALUE;
		}
	}

	/**
	 * This creates a reference BPT distribution calculator
	 * @param bpt_Aperiodicity
	 * @param durationInYears
	 * @return
	 */
	protected static BPT_DistCalc getRef_BPT_DistCalc(double bpt_Aperiodicity, double durationInYears) {
		int numPts = (int)Math.round((7*refRI+durationInYears)/deltaT);
		BPT_DistCalc bptCalc = new BPT_DistCalc();
		bptCalc.setAll(refRI, bpt_Aperiodicity, deltaT, numPts, durationInYears);
		return bptCalc;
	}
	
	protected void initAdjustableParams() {
		bpt_AperiodicityParam = new BPT_AperiodicityParam();
		adjustableParams.addParameter(bpt_AperiodicityParam);
		bpt_AperiodicityParam.addParameterChangeListener(this);
		// set primitive
		bpt_Aperiodicity = bpt_AperiodicityParam.getValue();
	}
		
	
	/**
	 * This initiates the timeSpan.
	 */
	protected void initiateTimeSpan() {
		if(SIMULATION_MODE) {
			timeSpan = new TimeSpan(TimeSpan.MILLISECONDS, TimeSpan.YEARS);
			timeSpan.setDuration(1.);	
		}
		else {
			timeSpan = new TimeSpan(TimeSpan.YEARS, TimeSpan.YEARS);
			timeSpan.setDuration(30.);
		}
		timeSpan.addParameterChangeListener(this);			
	}
	
	
	@Override
	public void updateForecast() {
		
		// first cache the following (this info will be erased by parent updataForecast(), and it's needed below)
		boolean fileChange = fileParamChanged;
		boolean aleatoryMagAreaChange = aleatoryMagAreaStdDevChanged;
		boolean aftershockFilterChanged = applyAftershockFilterChanged;
		boolean timeSpanChanged = timeSpanChangeFlag;
		boolean gridSpacingChanged = faultGridSpacingChanged;
		
		// use parent to update forecast if an appropriate parent param has changed (creating long-term Poisson fault-based sources)
		super.updateForecast();
		
		boolean sourcesUpdatedInParent = (fileChange || aleatoryMagAreaChange || aftershockFilterChanged || gridSpacingChanged);
		// not that the above does not include timeSpanChanged
		
		System.out.println("time span duration = "+timeSpan.getDuration());
		
		// make/update the bpt calculator
		if(refBPT_DistributionCalc == null || timeSpanChanged || bpt_AperiodicityChanged)
			initBPT_CondProbCalc();
		
		// init some arrays used for faster calculations
		if(fileChange)
			initArrays();
		
		// fill in totalRate, longTermRateOfNthRups, and magOfNthRups if sources updated and we're in simulation mode
		if(SIMULATION_MODE && sourcesUpdatedInParent) {
			totalRate=0;
			longTermRateOfNthRups = new double[totNumRups];
			magOfNthRups = new double[totNumRups];
			int nthRup=0;
			for(ProbEqkSource src:this) {
				for(ProbEqkRupture rup:src) {
					longTermRateOfNthRups[nthRup] = rup.getMeanAnnualRate(timeSpan.getDuration());
//					longTermRateOfNthRups[nthRup] = rup.getProbability();	// results aren't different for this (at 1 yr steps and including aleatory on mag)
					magOfNthRups[nthRup] = rup.getMag();
					totalRate += longTermRateOfNthRups[nthRup];
					nthRup+=1;
				}
			}
			System.out.println("totalRate long term = "+totalRate);
		}
		
		System.out.println("totNumRups="+totNumRups);
		System.out.println("getNumSources()="+getNumSources());
		
		
		// compute/update longTermPartRateForSectArray and aveCondRecurIntervalForFltSysRups if needed
		// (and reset oldGainForFaultSystemSource to null)
		if(sourcesUpdatedInParent) {
			
			// the following can't be from invSol.calcTotParticRateForAllSects() due to ERF rate reductions and low-mag filtering
			longTermPartRateForSectArray = new double[invRupSet.getNumSections()];
			int nthRup=0;
			for(ProbEqkSource src:this) {
				for(ProbEqkRupture rup:src) {
					double rupRate = rup.getMeanAnnualRate(timeSpan.getDuration());
					int fltSysIndex = fltSysRupIndexForNthRup[nthRup];
					List<Integer> sectIndices = invRupSet.getSectionsIndicesForRup(fltSysIndex);
					for(int s:sectIndices)
						longTermPartRateForSectArray[s] += rupRate;
					nthRup+=1;
				}
			}
			
			initAveCondRecurIntervalForFltSysRups();
			
			oldGainForFaultSystemSource=null;
		}

		
		// now update the the prob gains and sources if needed (must be done after the above)
		if(timeSpanChanged || bpt_AperiodicityChanged || sourcesUpdatedInParent) {
			
			System.out.println("updating all prob gains");
			
			// set oldGainForFaultSystemSource (initialize if it does not yet exist)
			if(oldGainForFaultSystemSource == null) {
				oldGainForFaultSystemSource = new double[numNonZeroFaultSystemSources];
				for(int i=0; i<oldGainForFaultSystemSource.length;i++)
					oldGainForFaultSystemSource[i] = 1.0;
			}
			else
				oldGainForFaultSystemSource = probGainForFaultSystemSource;
			
			// create new prob gain
			probGainForFaultSystemSource = new double[numNonZeroFaultSystemSources];
			double duration = timeSpan.getDuration();
			for(int s=0; s<numNonZeroFaultSystemSources; s++) {
				int fltSystRupIndex = fltSysRupIndexForSource[s];
				double aveTimeSinceLastYears = (double)getAveDateOfLastEventCorrected(fltSystRupIndex)/MILLISEC_PER_YEAR;	
				// THE ABOVE COMPUTES FROM THE FAULT SECTION DATA!
				if(totRupAreaWithDateOfLast == 0) {	// shouldn't be necessary, but more efficient
					probGainForFaultSystemSource[s] = 1.0;
				}
				else {
					double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups[fltSystRupIndex];
					probGainForFaultSystemSource[s] = computeBPT_ProbGainFast(aveCondRecurInterval, aveTimeSinceLastYears, duration);					
				}
			}
			
			// now modify sources according to prob gains
			for(int s=0; s<numNonZeroFaultSystemSources; s++) {
				double newOverOldGain = probGainForFaultSystemSource[s]/oldGainForFaultSystemSource[s];
				if(newOverOldGain > 1.0001 || newOverOldGain < 0.9999) { 	// different from 1.0
					faultSources.get(s).scaleRupRates(newOverOldGain);
				}
			}
			
			
			// set ruptureSampler if in simulation mode
			if(SIMULATION_MODE) {
				if(spontaneousRupSampler==null) {		// loop over all sources
					totalRate=0;
					spontaneousRupSampler = new IntegerPDF_FunctionSampler(totNumRups);
					int nthRup=0;
					for(int s=0;s<getNumSources();s++) {
						ProbEqkSource src = getSource(s);
						for(ProbEqkRupture rup:src) {
							double rate = rup.getMeanAnnualRate(timeSpan.getDuration());
							totalRate += rate;
							spontaneousRupSampler.set(nthRup, rate);
							nthRup+=1;
						}
					}
				}
				else {			// loop only update fault system sources
					int nthRup=0;
					for(int s=0;s<numNonZeroFaultSystemSources;s++) {	
						ProbEqkSource src = getSource(s);
						for(ProbEqkRupture rup:src) {
							double rate = rup.getMeanAnnualRate(timeSpan.getDuration());
							totalRate += rate - spontaneousRupSampler.getY(nthRup);	// subtract out the old rate
							spontaneousRupSampler.set(nthRup, rate);
							nthRup+=1;
						}
					}
					// this will be different from the above printing of totalRate only if the fault sections have date and slip in last event info
					if (D) System.out.println("totalRate = "+totalRate+"\t"+spontaneousRupSampler.getSumOfY_vals());
				}
			}
		}
		
		if (D) System.out.println("totNumRups="+totNumRups+"\t"+spontaneousRupSampler.getNum());
		
		bpt_AperiodicityChanged = false;

	}
	
	public void parameterChange(ParameterChangeEvent event) {
		super.parameterChange(event);	// sets parent param changes and parameterChangeFlag = true;
		String paramName = event.getParameterName();
		if(paramName.equalsIgnoreCase(BPT_AperiodicityParam.NAME)) {
			bpt_Aperiodicity = bpt_AperiodicityParam.getValue();
			bpt_AperiodicityChanged=true;
		}
	}

	

	@Override
	public String getName() {
		return NAME;
	}

	
	/**
	 * TODO remove the legacy method (still used in ETAS simulations)?
	 * 
	 * This method resets the slip and date of last events for all FaultSectionPrefData
	 * utilized by the specified rupture (and for the given event time).  This also adds 
	 * the normalized RI to normalizedRecurIntervals if in simulation mode.  This does not
	 * update the probability gains because the next start time may differ from the origin
	 * time given here.
	 * @param nthRup
	 * @param eventTimeInMillis
	 */
	public void setRuptureOccurrenceTimePred(int nthRup, long eventTimeInMillis) {
		// only process if it's a fault system rupture
		if(nthRup < totNumRupsFromFaultSystem) {
			// save the normalize recurrence interval?
			if(SIMULATION_MODE) {
				double normRI = getNormalizedRecurIntervalTimePred(nthRup, eventTimeInMillis);
				if(!Double.isNaN(normRI)) {
					normalizedRupRecurIntervals.add(normRI);
					if(D)System.out.println("added normRI");
				}
			}
// if(srcIndexForNthRup[nthRup] == 4755) System.out.println("Resetting slip and time for s=4755; time="+eventTimeInMillis);

			// reset slip and date of last event in fault section data
			int fltSysIndex = fltSysRupIndexForNthRup[nthRup];
			List<FaultSectionPrefData> fltData = invSol.getRupSet().getFaultSectionDataForRupture(fltSysIndex);
			double[] slipArray = invSol.getRupSet().getSlipOnSectionsForRup(fltSysIndex);
			for(int i=0; i< fltData.size(); i++) {
				FaultSectionPrefData data = fltData.get(i);
				data.setDateOfLastEvent(eventTimeInMillis);
				data.setSlipInLastEvent(slipArray[i]);
			}
			// set the following to make sure gains are recomputed in updataForecast()
			timeSpanChangeFlag=true;
		}
	}
	
	/**
	 * TODO remove the legacy method (still used in ETAS simulations)?
	 * 
	 * This does the time-dependent calculation in my appendix.  A value of Double.NaN
	 * is returned if there were no fault sections with both date of and amount of slip
	 * in last event.
	 * @param faultSysRupIndex
	 * @return
	 */
	protected double computeTimePredProbGainForFaultSysRup(int faultSysRupIndex) {
		List<FaultSectionPrefData> fltData = invRupSet.getFaultSectionDataForRupture(faultSysRupIndex);
		double aveExpRI=0, totArea=0, usedArea=0;
		long sumDateOfLast = 0;
		for(FaultSectionPrefData data: fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double slipInLast = data.getSlipInLastEvent();
			double area = data.getTraceLength()*data.getReducedDownDipWidth();
			totArea += area;
			if(dateOfLast != Long.MIN_VALUE && slipInLast != Double.NaN) {
				aveExpRI += (slipInLast/(data.getReducedAveSlipRate()*1e-3))*area;  // weight averaged by area
				sumDateOfLast += (double)dateOfLast*area;
				usedArea += area;
			}
		}
		double gain = Double.NaN;	// default is NaN
		if(usedArea != 0.0) {
			aveExpRI /= usedArea;		// years
			long aveDateOfLast = Math.round(sumDateOfLast/usedArea);  // epoch millis

			long startTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
			long endTime = timeSpan.getEndTimeCalendar().getTimeInMillis();

			double timeSinceLast = (startTime-aveDateOfLast)/MILLISEC_PER_YEAR;
			if(timeSinceLast <0)
				throw new RuntimeException("timeSinceLast cannot be negative (startTime="+
						startTime+" and aveDateOfLast="+aveDateOfLast+"; "+timeSinceLast+" yrs)");
			
			double duration = (endTime-startTime)/MILLISEC_PER_YEAR;

			// this is to try to get the final result to be consistent in terms of rates and COV
			double alphaCorr=1d;
			double riCorr=1d;
//			double alphaCorr=0.9;
//			double riCorr=1.2;
			System.out.println("Replace following static method with more efficient and accurate Safe method"); // TODO
			double prob_bpt = BPT_DistCalc.getCondProb(aveExpRI*riCorr, bpt_Aperiodicity*alphaCorr, timeSinceLast, duration);
			double prob_pois = 1-Math.exp(-duration/aveExpRI);

			gain = (prob_bpt/prob_pois)*(usedArea/totArea) + 1.0*(totArea-usedArea)/totArea; // areas with no data get prob gain of 1.0
			
//if(faultSysRupIndex == 174542) {
//			System.out.println("\ncomputProbGainForFaultSysRup("+faultSysRupIndex+")\n");
//			System.out.println("\t"+"aveExpRI="+aveExpRI);
//			System.out.println("\t"+"timeSinceLast="+timeSinceLast);
//			System.out.println("\t"+"duration="+duration);
//			System.out.println("\t"+"prob_bpt="+prob_bpt);
//			System.out.println("\t"+"prob_pois="+prob_pois);
//			System.out.println("\t"+"gain="+gain);
//			System.out.println("\taveDateOfLast="+aveDateOfLast+"; startTime="+startTime+"; MILLISEC_PER_YEAR="+MILLISEC_PER_YEAR);
//			System.out.println("\t"+"usedArea="+usedArea);
//			System.out.println("\t"+"totArea="+totArea);
//}
		}
		return gain;
	}
	
	/**
	 * The initializes the array of average conditional recurrent intervals for each fault system rup
	 * (the recurrence interval assuming the rup is the next to occur, which is computed as one over the
	 * average long-term participation rate of all sections involved in the rupture ( where the average is
	 * weighted by section area)).
	 * 
	 * This also initializes longTermPartRateForSectArray.
	 * @return
	 */
	private void initAveCondRecurIntervalForFltSysRups() {
		
		aveCondRecurIntervalForFltSysRups = new double[invRupSet.getNumRuptures()];

		for(int r=0;r<invRupSet.getNumRuptures(); r++) {
			List<FaultSectionPrefData> fltData = invRupSet.getFaultSectionDataForRupture(r);
			double aveRate=0, totArea=0;
			for(FaultSectionPrefData data:fltData) {
				int sectID = data.getSectionId();
				double area = data.getTraceLength()*data.getReducedDownDipWidth();
				totArea += area;
				// TEST HERE AND BELOW
//				aveRate += longTermPartRateForSectArray[sectID]*area;  // weight averaged by area
				aveRate += area/longTermPartRateForSectArray[sectID];  // this one averages RIs
			}
//			aveCondRecurIntervalForFltSysRups[r] = 1/(aveRate/totArea);
			aveCondRecurIntervalForFltSysRups[r] = aveRate/totArea;	// this one averages RIs
		}
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
	 * TODO remove the legacy method (still used in ETAS simulations)?
	 * 
	 * This returns the ratio of the observed to predicted time-predictable recurrence 
	 * interval (considering slip in last event), or Double.NaN if any of the slip or 
	 * date-of-last-event-data are not available from any section involved.
	 * @param nthRup
	 * @param eventTimeInMillis
	 * @return
	 */
	private double getNormalizedRecurIntervalTimePred(int nthRup, long eventTimeInMillis) {
		List<FaultSectionPrefData> fltData = invRupSet.getFaultSectionDataForRupture(fltSysRupIndexForNthRup[nthRup]);
		double aveExpRI=0, totArea=0;
		long aveDateOfLast = 0;
		for(FaultSectionPrefData data: fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double slipInLast = data.getSlipInLastEvent();
			double area = data.getTraceLength()*data.getReducedDownDipWidth();
			totArea += area;
			if(dateOfLast != Long.MIN_VALUE && slipInLast != Double.NaN) {
				aveExpRI += (slipInLast/(data.getReducedAveSlipRate()*1e-3))*area;  // weight averaged by area
				aveDateOfLast += dateOfLast*area;
			}
			else {
				return Double.NaN;
			}
		}
		aveExpRI /= totArea;		// years
		aveDateOfLast /= totArea;  // epoch millis

		double timeSinceLast = (eventTimeInMillis-aveDateOfLast)/MILLISEC_PER_YEAR;
		double normRI= timeSinceLast/aveExpRI;
//		if(normRI<0.1) {
//			ProbEqkRupture rup = getNthRupture(nthRup);
//			System.out.println("Wierd RI for rup "+nthRup);
//			System.out.println("\ttimeSinceLast="+timeSinceLast+
//					"\teventTimeInMillis="+eventTimeInMillis+
//					"\taveDateOfLast="+aveDateOfLast+
//					"\taveExpRI="+aveExpRI+
//					"\tnormRI="+normRI+
//					"\ttotArea="+totArea+
//					"\trupProb="+rup.getProbability()+
//					"\trupMag="+rup.getMag()+
//					"\tprobGain="+probGainForFaultSystemSource[srcIndexForNthRup[nthRup]]);
////			if(nthRup==2503) {
//				int index=0;
//				System.out.println("\tSrcName="+getSource(srcIndexForNthRup[nthRup]).getName());
//				for(FaultSectionPrefData data: fltData) {
//					System.out.println("\t"+index+"\tdateOfLast="+ data.getDateOfLastEvent()+
//							"\tslipInLast="+data.getSlipInLastEvent()+
//							"\tslipRateReduced="+data.getReducedAveSlipRate()+
//							"\tareaReduced="+ data.getTraceLength()*data.getReducedDownDipWidth());
//					index += 1;
//				}
////			}
//		}
		return normRI;
	}
	
	
	
	
	/**
	 * This method returns the average date of last event (epoch milliseconds) for the given rupture, representing
	 * an area-weight averaged value.  Fault sections that have no date of last event are given the equivalent
	 * date of last that is closest to the Poisson distribution.
	 * Users will know this has been done by the following first two global variables being non equal
	 * and the third being false, after the calculation:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 * 
	 * @param fltSystRupIndex
	 * @return
	 */
	public long getAveDateOfLastEventCorrected(int fltSystRupIndex) {
		// compute average date of last event
		double aveExpRI = aveCondRecurIntervalForFltSysRups[fltSystRupIndex];
		double durOverRI = timeSpan.getDuration()/aveExpRI;
		double timeWhereBPT_CondProbSameAsPois = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI);
		long startTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		double defaultAveDateOfLast =  (double)startTime - Math.round(aveExpRI*MILLISEC_PER_YEAR*timeWhereBPT_CondProbSameAsPois);	// set at time that will give cond prob equiv to poisson

		List<FaultSectionPrefData> fltData = invRupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(FaultSectionPrefData data:fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double area = data.getTraceLength()*data.getReducedDownDipWidth();
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				sumDateOfLast += (double)dateOfLast*area;
			}
			else {
				sumDateOfLast += defaultAveDateOfLast;
				allSectionsHadDateOfLast = false;
			}
		}
		return Math.round(sumDateOfLast/totRupArea);  // epoch millis
	}
	
	
	/**
	 * This method returns the weight-averaged date of last event (epoch milliseconds, where weights are section ares) 
	 * for the given rupture.  This version gets date of last event and computes are from the fault section data objects.
	 * Fault sections that have no date of last event are ignored, and Long.MIN_VALUE is returned if no sections have a 
	 * date of last event.  Users can determine how much of the total area had date of last event by comparing the following 
	 * global variables after the calculation:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 * 
	 * @param fltSystRupIndex
	 * @return
	 */
	public long getAveDateOfLastEvent(int fltSystRupIndex) {
		List<FaultSectionPrefData> fltData = invRupSet.getFaultSectionDataForRupture(fltSystRupIndex);
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(FaultSectionPrefData data:fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double area = data.getTraceLength()*data.getReducedDownDipWidth();
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				sumDateOfLast += (double)dateOfLast*area;
			}
			else {
				allSectionsHadDateOfLast = false;
			}
		}
		if(totRupAreaWithDateOfLast>0.0)
			return Math.round(sumDateOfLast/totRupArea);  // epoch millis
		else
			return Long.MIN_VALUE;
	}
	
	
	/**
	 * This method returns the average date of last event (epoch milliseconds) for the given rupture, representing
	 * an area-weight averaged value.  Fault sections that have no date of last event are given the equivalent
	 * date of last that is closest to the Poisson distribution.  Users will know this has been done by the following 
	 * global variables being non equal after the calculation:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionsHadDateOfLast
	 * 
	 * This uses cached arrays for speed.
	 * 
	 * @param fltSystRupIndex
	 * @return
	 */
	public long getAveDateOfLastEventCorrectedFast(int fltSystRupIndex, long startTimeMillis, double durationYears) {
		// compute average date of last event
		double aveExpRI = aveCondRecurIntervalForFltSysRups[fltSystRupIndex];
		double durOverRI = durationYears/aveExpRI;
		double timeWhereBPT_CondProbSameAsPois = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI);
		double defaultAveDateOfLast =  (double)startTimeMillis - Math.round(aveExpRI*MILLISEC_PER_YEAR*timeWhereBPT_CondProbSameAsPois);	// set at time that will give cond prob equiv to poisson

		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(int sect : sectIndexArrayForSrcList.get(srcIndexForFltSysRup[fltSystRupIndex])) {
			long dateOfLast = dateOfLastForSect[sect];
			double area = areaForSect[sect];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				sumDateOfLast += (double)dateOfLast*area;
			}
			else {
				sumDateOfLast += defaultAveDateOfLast;
				allSectionsHadDateOfLast = false;
			}
		}
		return Math.round(sumDateOfLast/totRupArea);  // epoch millis
	}

	
	public long testAltAveDateOfLastEventCorrectedFast(int fltSystRupIndex, long startTimeMillis, double durationYears) {
		
		// compute average date of last event
		double aveExpRI = aveCondRecurIntervalForFltSysRups[fltSystRupIndex];
		double durOverRI = durationYears/aveExpRI;
		double timeWhereBPT_CondProbSameAsPois = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI);
		double defaultAveDateOfLast =  (double)startTimeMillis - Math.round(aveExpRI*MILLISEC_PER_YEAR*timeWhereBPT_CondProbSameAsPois);	// set at time that will give cond prob equiv to poisson

		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumNormDateOfLast = 0;
		for(int sect : sectIndexArrayForSrcList.get(srcIndexForFltSysRup[fltSystRupIndex])) {
			long dateOfLast = dateOfLastForSect[sect];
			double area = areaForSect[sect];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				sumNormDateOfLast += area*((double)(startTimeMillis-dateOfLast)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sect];
			}
			else {
				sumNormDateOfLast += area*((double)(startTimeMillis-defaultAveDateOfLast)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sect];
				allSectionsHadDateOfLast = false;
			}
		}
		double normDateOfLast = sumNormDateOfLast/totRupArea;
		long timeSinceLastMillis = Math.round(normDateOfLast*aveExpRI*MILLISEC_PER_YEAR);
		return startTimeMillis-timeSinceLastMillis;
	}

	
	/**
	 * This averages the normalized times since last event for each section 
	 * (normalized by long-term section RI), weighted by section area.
	 * If no date of last event is available for a section, it uses the normalized
	 * time since last that corresponds to the Poisson probability (which is why the
	 * duration is needed).
	 * @param fltSystRupIndex
	 * @param startTimeMillis
	 * @param durationYears
	 * @return
	 */
	public double getAveNormTimeSinceLastCorrectedFast(int fltSystRupIndex, long startTimeMillis, double durationYears) {
		
		// TODO this needs to deal with case where update forecast changes section event rates
		if(bptTimeToPoisCondProbFuncForSect == null) {
			bptTimeToPoisCondProbFuncForSect = new double[dateOfLastForSect.length];
			for(int s=0;s<dateOfLastForSect.length;s++) {
				double durOverRI = durationYears*longTermPartRateForSectArray[s];
				bptTimeToPoisCondProbFuncForSect[s] = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI); // these are normalized times
			}
		}
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumNormTimeSinceLast = 0;
		for(int sect : sectIndexArrayForSrcList.get(srcIndexForFltSysRup[fltSystRupIndex])) {
			long dateOfLast = dateOfLastForSect[sect];
			double area = areaForSect[sect];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				sumNormTimeSinceLast += area*((double)(startTimeMillis-dateOfLast)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sect];
			}
			else {
				sumNormTimeSinceLast += area*bptTimeToPoisCondProbFuncForSect[sect];
				allSectionsHadDateOfLast = false;
			}
		}
		return sumNormTimeSinceLast/totRupArea;
	}


	
	/**
	 * This method returns the weight-averaged date of last event (epoch milliseconds, where weights are section ares) 
	 * for the given rupture.  This version uses cached arrays to for section area, date of last event, and sections in rupture.
	 * Fault sections that have no date of last event are ignored, and Long.MIN_VALUE is returned if no sections have a 
	 * date of last event.  Users can determine how much of the total area had date of last event by comparing the following 
	 * global variables after the calculation:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionHadDateOfLast
	 * 
	 * @param fltSystRupIndex
	 * @return
	 */
	public long getAveDateOfLastEventFast(int fltSystRupIndex) {
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumDateOfLast = 0;
		for(int sect : sectIndexArrayForSrcList.get(srcIndexForFltSysRup[fltSystRupIndex])) {
			long dateOfLast = dateOfLastForSect[sect];
			double area = areaForSect[sect];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				sumDateOfLast += (double)dateOfLast*area;
			}
			else {
				allSectionsHadDateOfLast = false;
			}
		}
		if(totRupAreaWithDateOfLast>0.0)
			return Math.round(sumDateOfLast/totRupArea);  // epoch millis
		else
			return Long.MIN_VALUE;
	}


	/**
	 * This method returns the normalized, weight-averaged years since last event for the given rupture 
	 * (normalized by mean RIs of each section before averaging).  The weights in the average are section area.
	 * This uses cached arrays to for section area, date of last event, and sections in rupture.
	 * Fault sections that have no date of last event are ignored, and Long.MIN_VALUE is returned if no sections have a 
	 * date of last event.  Users can determine how much of the total area had date of last event by comparing the following 
	 * global variables after the calculation:
	 * 
	 * 		double totRupArea
	 * 		double totRupAreaWithDateOfLast
	 * 		boolean allSectionHadDateOfLast
	 * 
	 * @param fltSystRupIndex
	 * @param presentTimeMillis
	 * @return
	 */
	public double getAveNormTimeSinceLastEventFast(int fltSystRupIndex, long presentTimeMillis) {
		totRupArea=0;
		totRupAreaWithDateOfLast=0;
		allSectionsHadDateOfLast = true;
		double sumYrsSinceLast = 0;
		for(int sect : sectIndexArrayForSrcList.get(srcIndexForFltSysRup[fltSystRupIndex])) {
			long dateOfLast = dateOfLastForSect[sect];
			double area = areaForSect[sect];
			totRupArea += area;
			if(dateOfLast != Long.MIN_VALUE) {
				totRupAreaWithDateOfLast += area;
				double yearsSinceLast = ((double)(presentTimeMillis-dateOfLast))/MILLISEC_PER_YEAR;
				sumYrsSinceLast += yearsSinceLast*area*longTermPartRateForSectArray[sect];
			}
			else {
				allSectionsHadDateOfLast = false;
			}
		}
		if(totRupAreaWithDateOfLast>0.0)
			return sumYrsSinceLast/totRupArea;
		else
			return Long.MIN_VALUE;
	}

	
	
	/**
	 * This returns null if it's not a fault system rupture (use zero-length list instead?).
	 * This should be rewritten to use HashMap directly (rather than
	 * @param nthRup
	 * @return
	 */
	public ArrayList<Integer> getRupsThatChangeProbAfterRup(int nthRup) {
		if(nthRup>=totNumRupsFromFaultSystem) {	// it's a gridded seismicity source
			return null;
		}
		
		// this will store the indices of nth ruptures that changed
		ArrayList<Integer> nthRupsThatChanged = new ArrayList<Integer>();

		int fltSysRupId = fltSysRupIndexForNthRup[nthRup];
		
		List<Integer> sectionIndices = invRupSet.getSectionsIndicesForRup(fltSysRupId);

//		long runtime = System.currentTimeMillis();
		
		// get fault system rups that are influenced
		HashSet<Integer> fltSysRups = new HashSet<Integer>();

		// the following was a huge bottleneck
//		for(Integer sectIndex: sectionIndices) {
//			for(Integer rup:invSol.getRupturesForSection(sectIndex)) {
//				if(!fltSysRups.contains(rup)) // filter duplicates
//					fltSysRups.add(rup);  				
//			}
//		}
		
		// this is much faster, but there still may be a faster way (use HashSet in first place?)
		for(Integer sectIndex: sectionIndices) {
			fltSysRups.addAll(invRupSet.getRupturesForSection(sectIndex));  				
		}
		
//		runtime -= System.currentTimeMillis();
//		System.out.print((runtime/1000)+" sec for loop over getRupturesForSection(sectIndex), num sections="+
//				sectionIndices.size()+", and fltSysRups.size()="+fltSysRups.size()+"\n");

		// need to convert these to nthRup indices
		for(Integer fltSysRupIndex :fltSysRups) {
				int srcIndex = srcIndexForFltSysRup[fltSysRupIndex];
				if(srcIndex != -1) {
					int[] nthRups = nthRupIndicesForSource.get(srcIndex);
					for(int r=0; r<nthRups.length;r++)
						nthRupsThatChanged.add(nthRups[r]);
				}
		}


		return nthRupsThatChanged;
	}
	
	
	private int getNumSectWithDateOfLastEvent() {
		int numGoodDateOfLast=0;
		for(double lastDate:dateOfLastForSect) {
			if(lastDate != Long.MIN_VALUE)
				numGoodDateOfLast+=1;					
		}
		return numGoodDateOfLast;
	}
	
	/**
	 * This plots a histogram of the probability gains of all fault system sources
	 * at the point in time at which this method is called.
	 * @param info
	 */
	private void plotRupProbGainHistogram(String info) {
		HistogramFunction probGainFunc = new HistogramFunction(0.0, 71, 0.2);
		for(double gain:probGainForFaultSystemSource) {
			probGainFunc.add(gain, 1.0);
		}
		ArrayList<HistogramFunction> funcs = new ArrayList<HistogramFunction>();
		funcs.add(probGainFunc);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2, Color.BLUE));
		GraphWindow graph = new GraphWindow(funcs, "Prob Gain Distribution "+info, plotChars);
	}
	
	
	private void tempExamineMaxGain(long currentTimeMillis) {
		int maxGainIndex = -1;
		double maxGain=-1;
		for(int i=0; i<probGainForFaultSystemSource.length;i++) {
			if(probGainForFaultSystemSource[i]>maxGain) {
				maxGain = probGainForFaultSystemSource[i];
				maxGainIndex = i;
			}
		}
		System.out.println("maxGain="+maxGain+" for FSS src index "+maxGainIndex+"; currentTimeMillis="+currentTimeMillis);
		
		// test that calculation
		long aveDateOfLastMillis = getAveDateOfLastEventCorrectedFast(maxGainIndex, currentTimeMillis, 1.0);
		double aveTimeSinceLastYrs = (currentTimeMillis-aveDateOfLastMillis)/MILLISEC_PER_YEAR;
		double gainTest = computeBPT_ProbGainFast(aveCondRecurIntervalForFltSysRups[maxGainIndex], aveTimeSinceLastYrs, 1.0);					
		System.out.println("gainTest="+gainTest+"\taveTimeSinceLastYrs="+aveTimeSinceLastYrs+"\taveCondRecurInterval="+aveCondRecurIntervalForFltSysRups[maxGainIndex]+"\taveDateOfLastMillis="+aveDateOfLastMillis);
		
		// list section properties
		System.out.println("Section data:\n\tindex\tnormYrsSinceLast\ttimeOfLastMillis\tlongTermPartRate\tarea\tcurrentTimeMillis\tname\tflag");
		List<FaultSectionPrefData> sectList = invRupSet.getFaultSectionDataForRupture(maxGainIndex);
		
		// compute default date of last event
		double aveExpRI = aveCondRecurIntervalForFltSysRups[maxGainIndex];
		double durOverRI = 1.0/aveExpRI;
		double timeWhereBPT_CondProbSameAsPois = bptTimeToPoisCondProbFunc.getInterpolatedY(durOverRI);
		double defaultAveDateOfLast =  (double)currentTimeMillis - Math.round(aveExpRI*MILLISEC_PER_YEAR*timeWhereBPT_CondProbSameAsPois);	// set at time that will give cond prob equiv to poisson

		if(!allSectionsHadDateOfLast) System.out.println("Warning: not all sections had date of last");

		String flag;
		for(FaultSectionPrefData data:sectList) {
				int s= data.getSectionId();
				long timeOfLastMillis = dateOfLastForSect[s];
				if(timeOfLastMillis == Long.MIN_VALUE) {
					flag="Double.MINVAL!";
					timeOfLastMillis = Math.round(defaultAveDateOfLast);
				}
				else {
					flag="";
				}	
				double normYrsSinceLast = ((currentTimeMillis-timeOfLastMillis)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[s];
				System.out.println("\t"+s+"\t"+normYrsSinceLast+"\t"+timeOfLastMillis+"\t"+longTermPartRateForSectArray[s]+"\t"+
				areaForSect[s]+"\t"+currentTimeMillis+"\t"+data.getName()+"\t"+flag);
		}
	}
	
	
	private void plotSectNormTimeSinceLastEventHistogram(String info) {
		HistogramFunction histFunc = new HistogramFunction(-0.3, 73, 0.2);	// -0.3 bin for no date of last event
		long startTimeMillis = timeSpan.getStartTimeCalendar().getTimeInMillis();
		for(int s=0; s<dateOfLastForSect.length;s++) {
			long timeOfLastMillis = dateOfLastForSect[s];
			if(timeOfLastMillis != Long.MIN_VALUE) {
				double normYrsSinceLast = ((startTimeMillis-timeOfLastMillis)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[s];
				histFunc.add(normYrsSinceLast, 1.0);
			}
			else {
				histFunc.add(0, 1.0);	// this is the -0.1 x-axis bin
			}
		}
		ArrayList<HistogramFunction> funcs = new ArrayList<HistogramFunction>();
		funcs.add(histFunc);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2, Color.MAGENTA));
		GraphWindow graph = new GraphWindow(funcs, "Norm Time Since Last Distribution "+info, plotChars);
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
	 * @param probType - 0 for Poisson, 1 for U3, and 2 for WG02
	 * @param dirNameForSavingFiles - leave null if you don't want plots saved
	 */
	public void testER_Simulation(int probType, String inputDateOfLastFileName, String outputDateOfLastFileName, String dirNameForSavingFiles) {
		
		
		
		// temp correction MFD - to see if this will correct the MFD baises; it does, but messes up section part rates for parkfield
//		IncrementalMagFreqDist correctionMFD = new IncrementalMagFreqDist(5.05,8.95,40);
//		for(int i=0;i<correctionMFD.getNum();i++)
//			correctionMFD.set(i,1.0);
//		correctionMFD.set(5.55,0.68401*0.9);
//		correctionMFD.set(5.65,0.514048*0.9);
//		correctionMFD.set(5.75,0.790005*0.9);
//		correctionMFD.set(5.85,0.837815*0.9);
//		correctionMFD.set(5.95,0.871158*0.9);
//		correctionMFD.set(6.05,0.837409*0.8);
//		correctionMFD.set(6.15,0.817532*0.8);
//		correctionMFD.set(6.25,0.794629*0.8);
//		correctionMFD.set(6.35,0.819054*0.8);
//		correctionMFD.set(6.45,0.857520*0.8);
//		correctionMFD.set(6.55,0.866264*0.8);
//		correctionMFD.set(6.65,0.886100*0.8);
//		correctionMFD.set(6.75,0.925039*0.9);
//		correctionMFD.set(6.85,0.910973*0.9);
//		correctionMFD.set(6.95,0.948021*0.9);
//		correctionMFD.set(7.05,0.974252);
//		correctionMFD.set(7.15,0.9974);
//		correctionMFD.set(7.25,1.00688);
//		correctionMFD.set(7.35,1.0623);
//		correctionMFD.set(7.45,1.09672);
//		correctionMFD.set(7.55,0.991633);
//		correctionMFD.set(7.65,1.04239);
//		correctionMFD.set(7.75,1.05267);
//		correctionMFD.set(7.85,1.0803);
//		correctionMFD.set(7.95,1.34756);
//		correctionMFD.set(8.05,1.11993);
//		correctionMFD.set(8.15,1.43739);
//		correctionMFD.set(8.25,1.82691);

		
		
		String probTypeString;
		if(probType==0)
			probTypeString= "Poisson";
		else if(probType==1)
			probTypeString= "U3";
		else if(probType==2)
			probTypeString= "WG02";
		else
			throw new RuntimeException();
		
		
		// save original start time and total duration (these will get over ridden)
		long origStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		double origDuration = timeSpan.getDuration();
		double startYear = ((double)origStartTime)/MILLISEC_PER_YEAR+1970.0;

		// switch to simulation mode
		SIMULATION_MODE=true;
		initiateTimeSpan();	// just in case the non-simulation timeSpan was in use
		double simDuration = 1.0;	// 1 year; this could be the expected time to next event?
		timeSpan.setDuration(simDuration);	// use annual probability to sample events
		System.out.println("start time: "+origStartTime+ " millis ("+startYear+" yrs)");
		System.out.println("originalDuration: "+origDuration+" ("+timeSpan.getDurationUnits()+")");
		
		// initialize some things
		normalizedRupRecurIntervals = new ArrayList<Double>();
		normalizedSectRecurIntervals = new ArrayList<Double>();
		int numRups=0;
		RandomDataImpl randomDataSampler = new RandomDataImpl();	// apache tool for sampling from exponential distribution here
		
		// Update forecast
		if(D) System.out.println("Updating forecast");
		updateForecast();	// TODO date of last set here from fault section data

		
		// this is for storing the simulated rate of events for each section
		double[] obsSectRateArray = new double[invRupSet.getNumSections()];
		double[] obsSectRateArrayM6pt05to6pt65 = new double[invRupSet.getNumSections()];
		double[] obsSectRateArrayM7pt95to8pt25 = new double[invRupSet.getNumSections()];
		
		// this is for storing obs/simulated event rates
		double[] obsRupRateArray = new double[totNumRups];

		
		// make the target MFD - 
		// TODO This includes original time dependence (if date of last in fault-section data objects)? 
		// Make from longTermRateOfNthRups & magOfNthRups instead?
		if(D) System.out.println("Making target MFD");
		SummedMagFreqDist targetMFD = ERF_Calculator.getTotalMFD_ForERF(this, 5.05, 8.95, 40, true);
		double origTotMoRate = ERF_Calculator.getTotalMomentRateInRegion(this, null);
		System.out.println("originalTotalMomentRate: "+origTotMoRate);
		targetMFD.setName("Target MFD");
		String infoString = "total rate = "+(float)targetMFD.getTotalIncrRate();
		infoString += "\ntotal rate >= 6.7 = "+(float)targetMFD.getCumRate(6.75);
		infoString += "\ntotal MoRate = "+(float)origTotMoRate;
		targetMFD.setInfo(infoString);
		
//		System.out.println(targetMFD);

		// MFD for simulation
		SummedMagFreqDist obsMFD = new SummedMagFreqDist(5.05,8.95,40);
		double obsMoRate = 0;
		
		
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
		
		// do simulation loop
		double yr=startYear;

		int percDoneThresh=0;
		int percDoneIncrement=5;
		int numGoodDateOfLast=getNumSectWithDateOfLastEvent();
		int percentGood = (int)Math.round((100.0*(double)numGoodDateOfLast/(double)dateOfLastForSect.length));

		long startRunTime = System.currentTimeMillis();
		
		// read section date of last file if not null
		if(inputDateOfLastFileName != null)
			readSectTimeSinceLastEventFromFile(inputDateOfLastFileName, origStartTime);	// TODO Could differ from what was in the fault section data objects
		
		while (yr<origDuration+startYear) {
			
			// write progress
			int percDone = (int)Math.round(100*(yr-startYear)/origDuration);
			if(percDone >= percDoneThresh) {
				double timeInMin = ((double)(System.currentTimeMillis()-startRunTime)/(1000.0*60.0));
				numGoodDateOfLast=getNumSectWithDateOfLastEvent();
				percentGood = (int)Math.round((100.0*(double)numGoodDateOfLast/(double)dateOfLastForSect.length));
				System.out.println("\n"+percDoneThresh+"% done in "+(float)timeInMin+" minutes"+";  totalRate="+(float)totalRate+"; yr="+(float)yr+";  % sect with date of last = "+percentGood+"\n");	
				// plot prob-gain and/or norm date since last histogram
//				plotRupProbGainHistogram(percDoneThresh+"% done; "+probTypeString);
//				plotSectNormTimeSinceLastEventHistogram(percDoneThresh+"% done; "+probTypeString);
//				if(percDoneThresh == 100) {
//					tempExamineMaxGain(timeSpan.getStartTimeCalendar().getTimeInMillis());
//				}
				percDoneThresh += percDoneIncrement;
			}
			
//			System.out.println(numRups+"\t"+yr+"\t"+totalRate);
			
			// sample time of next event
			double timeOfNextInYrs = randomDataSampler.nextExponential(1.0/totalRate);
			long eventTimeMillis = timeSpan.getStartTimeCalendar().getTimeInMillis() + (long)(timeOfNextInYrs*MILLISEC_PER_YEAR);
			// System.out.println("Event time: "+eventTimeMillis+" ("+(yr+timeOfNextInYrs)+" yrs)");

			// sample an event
			int nthRup = spontaneousRupSampler.getRandomInt();
			int srcIndex = srcIndexForNthRup[nthRup];
			
			obsRupRateArray[nthRup] += 1;

			// set that fault system event has occurred (and save normalized RI)
			if(nthRup < totNumRupsFromFaultSystem) {	// ignore subseimo ruptures
				int fltSystRupIndex = fltSysRupIndexForNthRup[nthRup];
				
				
				// TEST HERE:

				// compute and save the normalize recurrence interval if all sections had date of last
				long aveDateOfLastMillis = getAveDateOfLastEventFast(fltSystRupIndex);
				if(allSectionsHadDateOfLast) {
					double timeSinceLast = (eventTimeMillis-aveDateOfLastMillis)/MILLISEC_PER_YEAR;
					normalizedRupRecurIntervals.add(timeSinceLast/aveCondRecurIntervalForFltSysRups[fltSystRupIndex]);
				}
				// Alternative to the calculation above:
//				double aveNormYearsSinceLast = getAveNormTimeSinceLastEventFast(fltSystRupIndex, eventTimeMillis);
//				if(allSectionsHadDateOfLast) {
//					normalizedRupRecurIntervals.add(aveNormYearsSinceLast);
//				}
				
				
				
				// save normalized fault section recurrence intervals
				for(int sect : sectIndexArrayForSrcList.get(srcIndexForFltSysRup[fltSystRupIndex])) {
					long timeOfLastMillis = dateOfLastForSect[sect];
					if(timeOfLastMillis != Long.MIN_VALUE) {
						double normYrsSinceLast = ((eventTimeMillis-timeOfLastMillis)/MILLISEC_PER_YEAR)*longTermPartRateForSectArray[sect];
						normalizedSectRecurIntervals.add(normYrsSinceLast);
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
			yr+=timeOfNextInYrs;
			timeSpan.setStartTimeInMillis(eventTimeMillis); // this is needed for the elastic rebound probs
			long newStartTimeMillis = timeSpan.getStartTimeCalendar().getTimeInMillis();
			
			// Now update gains for each source
//			long millis = System.currentTimeMillis();
			
			// nothing is done if probType=0;
			if(probType==1) {
				// TEST HERE:
//				computeU3_ProbGainsForRupsFast1(newStartTimeMillis, simDuration);
				computeU3_ProbGainsForRupsFast2(newStartTimeMillis, simDuration);
			}
			else if(probType==2)
				computeWG02_ProbGainsForRupsFast(newStartTimeMillis, simDuration);
			
			// System.out.println("allProbGains took (sec): "+(System.currentTimeMillis()-millis)/1e3);
			
//			System.out.println((float)probGainForFaultSystemSource[srcIndex]);
			
			
			// now update totalRate and ruptureSampler (for all rups since start time changed)
			for(int n=0; n<totNumRupsFromFaultSystem;n++) {
//				double newRate = longTermRateOfNthRups[n] * probGainForFaultSystemSource[srcIndexForNthRup[n]] * correctionMFD.getClosestY(magOfNthRups[n]);
				double newRate = longTermRateOfNthRups[n] * probGainForFaultSystemSource[srcIndexForNthRup[n]];

// Check whether simpler prob calc exceeds 1.0 for 50-yr forecast - it does (e.g.e, for a Parkfield rupture)!:
//				if(probType==1) {
//					double testProb = (1 - Math.exp(-longTermRateOfNthRups[n]*50)) * probGainForFaultSystemSource[srcIndexForNthRup[n]];
//					if(testProb>1) {
//						System.out.println("rate="+longTermRateOfNthRups[n]);
//						System.out.println("gain="+probGainForFaultSystemSource[srcIndexForNthRup[n]]);
//						throw new RuntimeException("testProb>1");
//					}
//				}
				
				spontaneousRupSampler.set(n, newRate);
			}
			totalRate = spontaneousRupSampler.getSumOfY_vals();
		}
		
		
		// write section date of last file if not null
		if(outputDateOfLastFileName != null)
			writeSectTimeSinceLastEventToFile(outputDateOfLastFileName, timeSpan.getStartTimeInMillis());

		
		System.out.println("numRups="+numRups);
		System.out.println("normalizedRecurIntervals.size()="+normalizedRupRecurIntervals.size());
		
		GraphWindow grapha_a = General_EQSIM_Tools.plotNormRI_Distribution(normalizedRupRecurIntervals, "Normalized Rupture RIs; "+probTypeString);
		GraphWindow graph2_b = General_EQSIM_Tools.plotNormRI_Distribution(normalizedSectRecurIntervals, "Normalized Section RIs; "+probTypeString);
		
//		System.out.println(obsMFD);

		// plot MFDs
		obsMFD.scale(1.0/origDuration);
		obsMFD.setName("Simulated MFD");
		obsMoRate /= origDuration;
		double obsTotRate = obsMFD.getTotalIncrRate();
		double rateRatio = obsTotRate/targetMFD.getTotalIncrRate();
		String infoString2 = "total rate = "+(float)obsTotRate+" (ratio="+(float)rateRatio+")";
		double obsTotRateAbove6pt7 = obsMFD.getCumRate(6.75);
		double rateAbove6pt7_Ratio = obsTotRateAbove6pt7/targetMFD.getCumRate(6.75);
		infoString2 += "\ntotal rate >= 6.7 = "+(float)obsTotRateAbove6pt7+" (ratio="+(float)rateAbove6pt7_Ratio+")";
		double moRateRatio = obsMoRate/origTotMoRate;
		infoString2 += "\ntotal MoRate = "+(float)obsMoRate+" (ratio="+(float)moRateRatio+")";
		obsMFD.setInfo(infoString2);

		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(targetMFD);
		funcs.add(obsMFD);
		funcs.add(targetMFD.getCumRateDistWithOffset());
		funcs.add(obsMFD.getCumRateDistWithOffset());
		GraphWindow graph = new GraphWindow(funcs, "Incremental Mag-Freq Dists; "+probTypeString); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setYLog(true);	// this causes problems
		graph.setY_AxisRange(1e-4, 1.0);
		graph.setX_AxisRange(5.5, 8.5);
		
		
		// plot observed versus imposed rup rates
		for(int i=0;i<obsRupRateArray.length;i++) {
			obsRupRateArray[i] = obsRupRateArray[i]/origDuration;
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
		GraphWindow graph4 = new GraphWindow(funcs4, "Obs vs Imposed Rup Rates; "+probTypeString, plotChars4); 
		graph4.setX_AxisRange(5d/origDuration, 0.01);
		graph4.setY_AxisRange(5d/origDuration, 0.01);
		graph4.setYLog(true);
		graph4.setXLog(true);
		graph4.setX_AxisLabel("Imposed Rup Rate (per yr)");
		graph4.setY_AxisLabel("Simulated Rup Rate (per yr)");

		
		
		
		
		
		
		
		// plot observed versus imposed section rates
		for(int i=0;i<obsSectRateArray.length;i++) {
			obsSectRateArray[i] = obsSectRateArray[i]/origDuration;
			obsSectRateArrayM6pt05to6pt65[i] = obsSectRateArrayM6pt05to6pt65[i]/origDuration;
			obsSectRateArrayM7pt95to8pt25[i] = obsSectRateArrayM7pt95to8pt25[i]/origDuration;
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
		graph2.setX_AxisRange(5d/origDuration, 0.05);
		graph2.setY_AxisRange(5d/origDuration, 0.05);
		graph2.setYLog(true);
		graph2.setXLog(true);
		graph2.setX_AxisLabel("Imposed Section Participation Rate (per yr)");
		graph2.setY_AxisLabel("Simulated Section Participation Rate (per yr)");
		
//		System.out.println(testSectName+"\tobsSectRateArray="+obsSectRateArray[testSectIndex]+
//				"\tlongTermPartRateForSectArray="+longTermPartRateForSectArray[testSectIndex]+"\tratio="+
//				(obsSectRateArray[testSectIndex]/longTermPartRateForSectArray[testSectIndex]));
		
		// write out test section rates
		ArrayList<String> outStringList = new ArrayList<String>();
		int numSect=invRupSet.getNumSections();
		double[] predSectRateArrayM6pt05to6pt65 = new double[numSect];
		double[] predSectRateArrayM7pt95to8pt25 = new double[numSect];
		for(int s=0;s<numSect;s++) {
			double partRateMlow=0;
			double partRateMhigh=0;
			for (int r : invRupSet.getRupturesForSection(s)) {
				double mag = invRupSet.getMagForRup(r);
				if(mag>6 && mag<6.7)
					partRateMlow += invSol.getRateForRup(r);
				else if (mag>7.9 && mag<8.3)
					partRateMhigh = invSol.getRateForRup(r);
			}
			predSectRateArrayM6pt05to6pt65[s]=partRateMlow;
			predSectRateArrayM7pt95to8pt25[s]=partRateMhigh;
			outStringList.add(s+"\t"+obsSectRateArray[s]+"\t"+longTermPartRateForSectArray[s]+"\t"+
					(obsSectRateArray[s]/longTermPartRateForSectArray[s])+"\t"+
					predSectRateArrayM6pt05to6pt65[s]+"\t"+
					obsSectRateArrayM6pt05to6pt65[s]+"\t"+
					predSectRateArrayM7pt95to8pt25[s]+"\t"+
					obsSectRateArrayM7pt95to8pt25[s]+"\t"+
					invRupSet.getFaultSectionData(s).getName()+"\n");
		}
//		if(!dataDir.exists()) dataDir.mkdir();
		File resultsDir = new File(dirNameForSavingFiles);
		if(!resultsDir.exists()) resultsDir.mkdir();
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
			if(predSectRateArrayM6pt05to6pt65[s] >= 10.0/origDuration) {	// only keep where 10 should have occurred
				obs_pred_ratioForSections.set(predSectRateArrayM6pt05to6pt65[s], obsSectRateArrayM6pt05to6pt65[s]/predSectRateArrayM6pt05to6pt65[s]);
			}
		}
		DefaultXY_DataSet perfectAgreementFunc2 = new DefaultXY_DataSet();
		perfectAgreementFunc2.set(10.0/origDuration,1d);
		perfectAgreementFunc2.set(0.1,1d);
		perfectAgreementFunc2.setName("Perfect agreement line");
		ArrayList<DefaultXY_DataSet> funcs3 = new ArrayList<DefaultXY_DataSet>();
		funcs3.add(obs_pred_ratioForSections);
		funcs3.add(perfectAgreementFunc2);
		ArrayList<PlotCurveCharacterstics> plotChars2 = new ArrayList<PlotCurveCharacterstics>();
		plotChars2.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 4f, Color.BLUE));
		plotChars2.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		GraphWindow graph3 = new GraphWindow(funcs3, "Obs/imposed vs Imposed Section Rates for M 6.0 to 6.7; "+probTypeString, plotChars2); 
		graph3.setX_AxisLabel("Imposed Section Participation Rate (per yr)");
		graph3.setY_AxisLabel("Ratio of Observed to Imposed");
		
		if(dirNameForSavingFiles != null) {
			try {
				grapha_a.saveAsPDF(dirNameForSavingFiles+"/normalizedRupRecurIntervals.pdf");
				graph2_b.saveAsPDF(dirNameForSavingFiles+"/normalizedSectRecurIntervals.pdf");
				graph.saveAsPDF(dirNameForSavingFiles+"/magFreqDists.pdf");
				graph2.saveAsPDF(dirNameForSavingFiles+"/obsVsImposedSectionPartRates.pdf");
				graph3.saveAsPDF(dirNameForSavingFiles+"/obsOverImposedVsImposedSectionPartRates.pdf");
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		}catch(Exception e) {
			e.printStackTrace();
		}
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

		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
	}


	
	/**
	 * This fills out probGainForFaultSystemSource array using the UCERF3 methodology
	 * That it's "Fast" means it uses cached arrays.
	 */
	private void computeU3_ProbGainsForRupsFastOld(long startTimeMillis, double durationYears) {
		for(int s=0;s<numNonZeroFaultSystemSources;s++) {
			
			int fltSysRupIndex = fltSysRupIndexForSource[s];
			
			// fast way:
			long aveDateOfLastRup = getAveDateOfLastEventCorrectedFast(fltSysRupIndex, startTimeMillis, durationYears);
			
			// slow way (100 time slower!!):
//			long aveDateOfLastRup = getAveDateOfLastEventCorrected(fltSysRupIndex);

			// now compute and set gain
			if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster?
				probGainForFaultSystemSource[s] = 1;
			}
			else {
				double timeSinceLast = (startTimeMillis-aveDateOfLastRup)/MILLISEC_PER_YEAR;
				if(timeSinceLast < 0) {
					if(timeSinceLast>-1) {	// allow very small values
						System.out.println("Warning - converting timeSinceLast ("+timeSinceLast+") to zero");
						timeSinceLast=0;
					}
					else
						throw new RuntimeException("timeSinceLast cannot be negative (startTime="+
							startTimeMillis+" and aveDateOfLast="+aveDateOfLastRup+"; "+timeSinceLast+" yrs)");
				}
				double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups[fltSysRupIndex];
				probGainForFaultSystemSource[s] = computeBPT_ProbGainFast(aveCondRecurInterval, timeSinceLast, durationYears);					
			}
		}
	}
	
	
	/**
	 * This fills out probGainForFaultSystemSource array using the UCERF3 methodology
	 * That it's "Fast" means it uses cached arrays.  This version uses the average
	 * normalized data of lasted event on each section.
	 */
	private void computeU3_ProbGainsForRupsFast1(long startTimeMillis, double durationYears) {
		for(int s=0;s<numNonZeroFaultSystemSources;s++) {
			int fltSysRupIndex = fltSysRupIndexForSource[s];
			// norm time since last
			double aveNormTimeSinceLast = getAveNormTimeSinceLastCorrectedFast(fltSysRupIndex, startTimeMillis, durationYears);
			// now compute and set gain
			if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster?
				probGainForFaultSystemSource[s] = 1;
			}
			else {
				if(aveNormTimeSinceLast < 0) {
					throw new RuntimeException("aveNormTimeSinceLast cannot be negative (aveNormTimeSinceLast="+aveNormTimeSinceLast+")");
				}
				double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups[fltSysRupIndex];
				probGainForFaultSystemSource[s] = computeBPT_ProbGainFast(aveCondRecurInterval, aveNormTimeSinceLast*aveCondRecurInterval, durationYears);					
			}
		}
	}

	
	/**
	 * This fills out probGainForFaultSystemSource array using the UCERF3 methodology
	 * That it's "Fast" means it uses cached arrays.  This version uses the average
	 * date of lasted event on each section (non normalized).
	 */
	private void computeU3_ProbGainsForRupsFast2(long startTimeMillis, double durationYears) {
		for(int s=0;s<numNonZeroFaultSystemSources;s++) {
			int fltSysRupIndex = fltSysRupIndexForSource[s];
			long aveTimeOfLastMillis = getAveDateOfLastEventCorrectedFast(fltSysRupIndex, startTimeMillis, durationYears);
			double timeSinceLastYears = (double)(startTimeMillis-aveTimeOfLastMillis)/MILLISEC_PER_YEAR;
			// now compute and set gain
			if(totRupAreaWithDateOfLast == 0.0) {	// this should not be necessary, but faster?
				probGainForFaultSystemSource[s] = 1;
			}
			else {
				if(timeSinceLastYears < 0) {
					if(timeSinceLastYears < -0.1)
						throw new RuntimeException("timeSinceLastYears cannot be negative (timeSinceLastYears="+timeSinceLastYears+")");
					else
						timeSinceLastYears = 0;
				}
				double aveCondRecurInterval = aveCondRecurIntervalForFltSysRups[fltSysRupIndex];
				probGainForFaultSystemSource[s] = computeBPT_ProbGainFast(aveCondRecurInterval, timeSinceLastYears, durationYears);					
			}
		}
	}

	

	
	
	/**
	 * This fills out probGainForFaultSystemSource array using the WG02 methodology, where weights are
	 * by area (not moment rate) for now.  That it's "Fast" means it uses cached arrays.
	 * @param startTimeMillis
	 * @param durationYears
	 */
	private void computeWG02_ProbGainsForRupsFast(long startTimeMillis, double durationYears) {
		
		// first compute the gains for each fault section
		double[] sectionGainArray = new double[dateOfLastForSect.length];
		for(int s=0; s<dateOfLastForSect.length;s++) {
			long timeOfLastMillis = dateOfLastForSect[s];
			if(timeOfLastMillis != Long.MIN_VALUE) {
				double timeSinceLastYears = ((double)(startTimeMillis-timeOfLastMillis))/MILLISEC_PER_YEAR;
				double refTimeSinceLast = timeSinceLastYears*refRI*longTermPartRateForSectArray[s];
				double refDuration = durationYears*refRI*longTermPartRateForSectArray[s];
				double prob_bpt = refBPT_DistributionCalc.getSafeCondProb(refTimeSinceLast, refDuration);
				double prob_pois = 1-Math.exp(-durationYears*longTermPartRateForSectArray[s]);
				sectionGainArray[s] = prob_bpt/prob_pois;
			}
			else {
				sectionGainArray[s] = 1.0;
			}
		}
		
		// now compute weight average gain for each rupture
		for(int src=0;src<numNonZeroFaultSystemSources;src++) {
			double totalArea=0;
			double sumGains = 0;			
			for(int sect : sectIndexArrayForSrcList.get(src)) {
				double area = areaForSect[sect];
				totalArea += area;
				sumGains += sectionGainArray[sect]*area;
			}
			probGainForFaultSystemSource[src] = sumGains/totalArea;
		}
	}
	
	
	/**
	 * This tests using one instance of BPT_DistCalc by normalizing time since last and forecast duration
	 * accordingly.
	 */
	public static void testFastBPT_Calc() {
//		double testDuration = refSimDur;
		double testDuration = 70;
		double bpt_Aper = 0.2;
		BPT_DistCalc bptCalc = getRef_BPT_DistCalc(bpt_Aper, testDuration);
		double[] ri_array = {20,2e2,2e3,2e4,2e5,2e6,2e7,2e8};
		double[] normTimeSinceLastArray = {1e-2,1e-1,1,3,6};
		for(double ri:ri_array) {
			for(double norm_tsl:normTimeSinceLastArray) {
				double timeSinceLast = norm_tsl*ri;

				double refTimeSinceLast = timeSinceLast*refRI/ri;
				double refDuration = testDuration*refRI/ri;
				
				boolean tooFarOut = refTimeSinceLast>bptCalc.getSafeTimeSinceLastCutoff();
				
				double prob_bpt_fast= bptCalc.getSafeCondProb(refTimeSinceLast, refDuration);
				
				// test against slower calculation
				BPT_DistCalc bptDistCalcAlt = new BPT_DistCalc();
				double deltaT_Alt = deltaT*ri/refRI;
				int numPts = (int)Math.round(7*refRI/deltaT);
				bptDistCalcAlt.setAll(ri, bpt_Aper, deltaT_Alt, numPts, testDuration);
				double prob_bpt = bptDistCalcAlt.getSafeCondProb(timeSinceLast, testDuration);
				double ratio = prob_bpt_fast/prob_bpt;
				if(prob_bpt_fast < 1e-20 && prob_bpt < 1e-20) ratio = 1;
				System.out.println((float)ratio+"\t"+ri+"\t"+timeSinceLast+"\t"+refTimeSinceLast+"\t"+refDuration+"\t"+
						(float)prob_bpt_fast+"\t"+(float)prob_bpt+"\t"+tooFarOut);
			}
		}
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
	 * This represents an ETAS simulation.  This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 */
	public void testETAS_SimulationOld(GriddedRegion griddedRegion, ArrayList<ObsEqkRupture> obsEqkRuptureList) {
		
		// this will store the aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
		double distDecay = 2;
		double minDist = 0.3;
		boolean useAdaptiveBlocks= true; 
		boolean includeBlockRates = true;
		
		double maxBlockDepth=24;
		
		normalizedRupRecurIntervals = new ArrayList<Double>();
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		if(!SIMULATION_MODE)
			throw new RuntimeException("This method can only be run if SIMULATION_MODE = true");

		long simStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		long simEndTime = timeSpan.getEndTimeCalendar().getTimeInMillis();
		double simDuration = timeSpan.getDuration();
		
		System.out.println("Updating forecast (twice)");
		// get the total rate over the duration of the forecast
		updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		double origTotRate = totalRate;	// this include ER time dependence, but diff shouldn't be noticeable.
		System.out.println("origTotRate="+origTotRate);
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		timeSpan.setDuration(1.0);	// annualize
		updateForecast();
		
		// make the EqksInGeoBlock lists
		System.out.println("Making initial EqksInGeoBlock lists");
		ArrayList<EqksInGeoBlock> blockList = EqksInGeoBlockUtils.makeAllEqksInGeoBlocks(this, griddedRegion, maxBlockDepth);
		ArrayList<ArrayList<EqksInGeoBlock>> subBlockList1 =  new ArrayList<ArrayList<EqksInGeoBlock>>(); // intermediate level of subdivision
		ArrayList<ArrayList<EqksInGeoBlock>> subBlockList2 =  new ArrayList<ArrayList<EqksInGeoBlock>>(); // highest level of subdivision
		// populate the sub-block lists
		for(int i=0;i<blockList.size();i++) {
			subBlockList1.add(null);
			subBlockList2.add(null);
		}

		
//		System.out.println("RATE OF NthRUP 4743 = "+this.getNthRupture(4743).getMeanAnnualRate(timeSpan.getDuration()));
		
		ETAS_Utils etas_utils = new ETAS_Utils();

		// Make list of primary events for given list of obs quakes 
		// (filling in origin time ID, and parentID, with the rest to be filled in later)
		System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>(); // this stores the active mainshocks
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a mainshock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTime-rupOT) / (double)MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
			// get a list of random primary event times
			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID,ot);
					newRup.setGeneration(1);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,randomAftShockTimes.length);
				parID += 1;				
			}
		}
		System.out.println("the "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" events");
		
		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		double fractionNonTriggered=0.5;	// really need to solve for this value
		double expectedNum = origTotRate*simDuration*fractionNonTriggered;
		System.out.println("expected num spontaneous: "+expectedNum+
				";\tfractionNonTriggered="+fractionNonTriggered+"; origTotRate="+origTotRate+"; origDuration="+simDuration);
		int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
numSpontEvents=0;
		System.out.println("Making spontaneous events (times and event IDs only) - "+numSpontEvents+" were sampled");

		for(int r=0;r<numSpontEvents;r++) {
			ETAS_EqkRupture rup = new ETAS_EqkRupture();
			double ot = simStartTime+Math.random()*(simEndTime-simStartTime);	// random time over time span
			rup.setOriginTime((long)ot);
			rup.setID(eventID);
			rup.setParentID(-1);		// parent is long-term model
			rup.setGeneration(0);
			eventsToProcess.add(rup);
			eventID += 1;
		}
		
		
		// Make container for active primary event samplers, where the key is the event ID
		HashMap<Integer,ETAS_PrimaryEventSampler> etasSamplerMap = new HashMap<Integer,ETAS_PrimaryEventSampler>();
		ETAS_PrimaryEventSampler firstSampler=null;
		
		System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")\n");
		long runTime = System.currentTimeMillis();
		
//		System.out.println("sleeping for 10 secs");
//		try {
//			Thread.sleep(10000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		CalcProgressBar progressBar = new CalcProgressBar("Events to process", "junk");
		progressBar.displayProgressBar();
		progressBar.showProgress(true);
		
		while(eventsToProcess.size()>0) {
			
			progressBar.updateProgress(simulatedRupsQueue.size(), eventsToProcess.size()+simulatedRupsQueue.size());
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			parID = rup.getParentID();
			int nthRup;
			int numToProcess=-1;
			EqksInGeoBlock block = null;
			EqkRupture mainshock = null;
			double blockDist= Double.NaN;
			
			if(parID == -1)	{ // it's a spontaneous event
				nthRup = spontaneousRupSampler.getRandomInt();	// sample from long-term model
			}
			else {
				// try to get sampler using parent ID (null if not yet there)
				ETAS_PrimaryEventSampler sampler = etasSamplerMap.get(parID);
				numToProcess = mainshockNumToProcess.get(parID);	// this is the number of events the sampler has yet to process
				if(sampler == null) {	// make the sampler and add to the list if it doesn't exist yet
					mainshock = mainshockHashMap.get(parID);
					sampler = new ETAS_PrimaryEventSampler(mainshock, blockList, subBlockList1, subBlockList2,
							this, distDecay, minDist, useAdaptiveBlocks, includeBlockRates);
					etasSamplerMap.put(parID, sampler);	// inefficient if there is only one to process
				}
				if(firstSampler == null) {	// save the first for plotting distance funcs
					firstSampler = sampler;
					if(obsEqkRuptureList.size() == 1) {	// plot stuff since it must be a large test event
//						firstSampler.plotDistDecayTestFuncs("Dist Decay", null);
						firstSampler.plotBlockProbMap("Test", true, "testHere");
//						firstSampler.writeRelBlockProbToFile();
					}
				}
				int blockIndex = sampler.sampleRandomBlockIndex();
				block = sampler.getRevisedBlockList().get(blockIndex);
				blockDist = sampler.getDistForRevisedBlock(blockIndex);
//				block = sampler.sampleRandomBlock();	// get a random block where the aftershock occurs
				nthRup = block.getRandomRuptureIndexN();  // get the nth index of the randomly chosen rupture
				numToProcess -= 1;	// decrement num to process
//				mainshock = sampler.getParentRup();
			}
			// get the rupture from the ERF
			ProbEqkRupture erf_rup = getNthRupture(nthRup);
			// transfer attributes
			rup.setAveRake(erf_rup.getAveRake());
			rup.setMag(erf_rup.getMag());
			RuptureSurface surf = erf_rup.getRuptureSurface();
			rup.setRuptureSurface(surf);
			rup.setNthERF_Index(nthRup);
			// set hypocenter & distance from parent if it's spontaneous
			if(parID == -1) {	// this should only pick a point on the surface that's in the block
				LocationList surfPts = surf.getEvenlyDiscritizedListOfLocsOnSurface();
				int hypIndex = (int)(Math.random()*(double)surfPts.size());	// choose random loc
				rup.setHypocenterLocation(surfPts.get(hypIndex));
			}
			else {	// set hypocenter using the block
				block.setRandomHypocenterLoc(rup);
//				double dist = LocationUtils.distanceToSurfFast(rup.getHypocenterLocation(), mainshock.getRuptureSurface());
				rup.setDistanceToParent(blockDist);
			}
			
			// add the rupture to the list
			simulatedRupsQueue.add(rup);
			
			// TO CHECK MEMORY CONSUMPTION
			int numBlock1=0;
			for(ArrayList<EqksInGeoBlock> blList : subBlockList1)
				if(blList != null)
					numBlock1+=1;
			int numBlock2=0;
			for(ArrayList<EqksInGeoBlock> blList : subBlockList2)
				if(blList != null)
					numBlock2+=1;
				
			// this isn't working:
			progressBar.setProgressMessage((float)rup.getMag()+"\t"+numBlock1+"\t"+numBlock2);
//			System.out.println("\t"+(float)rup.getMag()+"\t"+numBlock1+"\t"+numBlock2);
			
			// update num to process or clean up the ETAS samplers if this is zero
			if(parID != -1) {	// if not spontaneous
				if(numToProcess == 0) {
					mainshockNumToProcess.remove(parID);
					mainshockHashMap.remove(parID);
					etasSamplerMap.remove(parID);		
				}
				else {	// update the num to process
					mainshockNumToProcess.put(parID, numToProcess);
				}	
			}
			
			// now sample primary aftershock times for this event
			parID = rup.getID();	// rupture is now the parent
			int gen = rup.getGeneration()+1;
			long rupOT = rup.getOriginTime();
			double startDay = 0;	// starting at origin time since we're within the timespan
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
			double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(eventTimes.length>0) {
				for(int i=0; i<eventTimes.length;i++) {
					long ot = rupOT +  (long)(eventTimes[i]*(double)MILLISEC_PER_DAY);
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID, ot);
					newRup.setGeneration(gen);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,eventTimes.length);				
			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.

			if(nthRup<totNumRupsFromFaultSystem) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				
				Toolkit.getDefaultToolkit().beep();
				System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				System.out.println("nthRup="+"mag="+rup.getMag()+";  "+getSource(getSrcIndexForNthRup(nthRup)).getName());
				
// this can be turned off once it passes
// EqksInGeoBlockUtils.testSubBlockListRates(this, blockList, subBlockList1, subBlockList2);

				// set the start time for the time dependent calcs
				timeSpan.setStartTimeInMillis(rupOT);	
				
				// set the date of last event and slip for this rupture
				setRuptureOccurrenceTimePred(nthRup, rupOT);
				//				updateForecast();	// not needed

				double oldGain = probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)];
				System.out.println("s & r: "+getSrcIndexForNthRup(nthRup)+"\t"+getRupIndexInSourceForNthRup(nthRup));
				double oldProb = getNthRupture(nthRup).getProbability();
//				System.out.println("s & r: "+getSrcIndexForNthRup(nthRup)+"\t"+getRupIndexInSourceForNthRup(nthRup));
				double oldRate = getNthRupture(nthRup).getMeanAnnualRate(1.0);
				double oldRate2 = -Math.log(1 - oldProb)/1.0;
//				System.out.println("\told rate = "+oldRate+"\n\told rate2 = "+oldRate2+"\n\told prob = "+oldProb);


				// update gains for next round (prevents running updateForecast())
				// need to do all since the start time has changed
				for(int s=0;s<numNonZeroFaultSystemSources;s++) {
					double probGain = computeTimePredProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
					if(Double.isNaN(probGain))  // NEEDED? (NOT DONE IN UPDATEFORECAST)
						probGainForFaultSystemSource[s] = 1;
					else
						probGainForFaultSystemSource[s] = probGain;
				}


				// now update rates in blocks (for rups that change probs)
				System.out.println("Updating block rates");
				for(int s=0; s<numNonZeroFaultSystemSources;s++) {
					ProbEqkSource src = getSource(s);
					// now assuming all rups in source have same rupture surface
					LocationList locsOnSurface = src.getRupture(0).getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
					HashSet<Integer> blockIndicesForSrc = new HashSet<Integer>();
					for(Location loc: locsOnSurface)
						blockIndicesForSrc.add(griddedRegion.indexForLocation(loc));
					for(int r=0; r<src.getNumRuptures();r++) {
						erf_rup = src.getRupture(r);
						double newRate = erf_rup.getMeanAnnualRate(timeSpan.getDuration());
						double newProb = erf_rup.getProbability();
						int nthRupIndex = getIndexN_ForSrcAndRupIndices(s, r);

						// TEST THAT RATE CHANGED PROPERLY
						if(nthRup == nthRupIndex) {	// check change for the one that occurred
							// write the before and after gain change for the event that occurred
							System.out.println("\told gain="+oldGain+"\tnew gain="+probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)]);
							System.out.println("\told prob = "+oldProb+"\tnew prob = "+newProb);
							System.out.println("\told rate = "+oldRate+"\toldRate2 = "+oldRate2+"\tnew rate = "+newRate);
						}

						for(Integer b : blockIndicesForSrc) {
//							double oldRateBl = blockList.get(b).tempGetRandomEqkRupSamplerY_Val(nthRupIndex);
							blockList.get(b).changeRate(newRate, nthRupIndex);
//							double newRateBl = blockList.get(b).tempGetRandomEqkRupSamplerY_Val(nthRupIndex);

//							if(nthRup == nthRupIndex) {	// check change for the one that occurred
//								System.out.println("\told rate in block = "+oldRateBl+"\n\tnew rate in block = "+newRateBl);
//							}

							// update sub-block lists
							ArrayList<EqksInGeoBlock> subBlocks1 = subBlockList1.get(b);
							if(subBlocks1 != null) {
								for(EqksInGeoBlock blk: subBlocks1) {
									blk.changeRate(newRate, nthRupIndex);
								}
							}
							ArrayList<EqksInGeoBlock> subBlocks2 = subBlockList2.get(b);
							if(subBlocks2 != null) {
								for(EqksInGeoBlock blk: subBlocks2) {
									blk.changeRate(newRate, nthRupIndex);
								}
							}
						}
//if(nthRup == nthRupIndex) System.exit(-1);
					}
				}
				
// this can be turned off once it passes
// EqksInGeoBlockUtils.testSubBlockListRates(this, blockList, subBlockList1, subBlockList2);

				// now update samplers
//				System.out.println("Updating etasSamplers");
				for(Integer key:etasSamplerMap.keySet()) {
					etasSamplerMap.get(key).upDataRandomBlockSampler();	// THIS COULD BE DONE MORE EFFICIENTLY INSIDE THIS METHOD BY SAVING SPATIAL PROBS
				}
//				System.out.println("Done with fault system rupture updates");
			}
		}
		

		
		progressBar.showProgress(false);
		
		System.out.println("Looping over events took "+(System.currentTimeMillis()-runTime)/1000+" secs");

		System.out.println("Fault System Aftershocks:\n");
		for(Integer n : nthFaultSysRupAftershocks) {
			int s=srcIndexForNthRup[n];
			System.out.println("\t"+n+"\t"+s+"\t"+rupIndexForNthRup[n]+"\t"+fltSysRupIndexForNthRup[n]+
					"\tmag="+getNthRupture(n).getMag()+"\t"+getSource(s).getName());

		}

		String fileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/hypoTest.pdf";
		
//		ETAS_SimAnalysisTools.writeDataToFile("testRightHere.txt", simulatedRupsQueue);

		if(obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, obsEqkRuptureList.get(0), simulatedRupsQueue, null);
//			ETAS_SimAnalysisTools.plotDistDecayForAshocks("test", null, simulatedRupsQueue,firstSampler, obsEqkRuptureList.get(0));
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("test", null, simulatedRupsQueue, obsEqkRuptureList.get(0), distDecay, minDist);
		}
		else {
			ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, null, simulatedRupsQueue, null);
//			ETAS_SimAnalysisTools.plotDistDecayForAshocks("test", null, simulatedRupsQueue,firstSampler, null);
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("test", null, simulatedRupsQueue, null, distDecay, minDist);

		}
		ETAS_SimAnalysisTools.plotMagFreqDists("test", null, simulatedRupsQueue);
		
		
		System.out.println("Total num ruptures: "+simulatedRupsQueue.size());

	}
	
	
	/**
	 * This represents an ETAS simulation.  This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 */
	public void testETAS_SimulationOld3(GriddedRegion griddedRegion, ArrayList<ObsEqkRupture> obsEqkRuptureList) {
		
		// this will store the aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
		double distDecay = 2;
		double minDist = 0.3;
//		double minDist = 3;
		boolean includeEqkRates = true;	// whether or not to include the long-term rate of events in sampling aftershocks
				
		normalizedRupRecurIntervals = new ArrayList<Double>();
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		if(!SIMULATION_MODE)
			throw new RuntimeException("This method can only be run if SIMULATION_MODE = true");

		long simStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		long simEndTime = timeSpan.getEndTimeCalendar().getTimeInMillis();
		double simDuration = timeSpan.getDuration();
		
		System.out.println("Updating forecast (twice)");
		// get the total rate over the duration of the forecast
		updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		double origTotRate = totalRate;	// this include ER time dependence, but diff shouldn't be noticeable.
		System.out.println("origTotRate="+origTotRate);
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		timeSpan.setDuration(1.0);	// annualize
		updateForecast();
		
		
		ETAS_Utils etas_utils = new ETAS_Utils();

		// Make list of primary events for given list of obs quakes 
		// (filling in origin time ID, and parentID, with the rest to be filled in later)
		System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>(); // this stores the active mainshocks
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a mainshock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTime-rupOT) / (double)MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
			// get a list of random primary event times
			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID,ot);
					newRup.setGeneration(1);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,randomAftShockTimes.length);
				parID += 1;				
			}
		}
		System.out.println("the "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" events");
		
		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		double fractionNonTriggered=0.5;	// really need to solve for this value
		double expectedNum = origTotRate*simDuration*fractionNonTriggered;
		System.out.println("expected num spontaneous: "+expectedNum+
				";\tfractionNonTriggered="+fractionNonTriggered+"; origTotRate="+origTotRate+"; origDuration="+simDuration);
		int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
numSpontEvents=0;
		System.out.println("Making spontaneous events (times and event IDs only) - "+numSpontEvents+" were sampled");

		for(int r=0;r<numSpontEvents;r++) {
			ETAS_EqkRupture rup = new ETAS_EqkRupture();
			double ot = simStartTime+Math.random()*(simEndTime-simStartTime);	// random time over time span
			rup.setOriginTime((long)ot);
			rup.setID(eventID);
			rup.setParentID(-1);		// parent is long-term model
			rup.setGeneration(0);
			eventsToProcess.add(rup);
			eventID += 1;
		}
		
		
		
		// Make the ERF_RatesInSpace and ETAS_LocationWeightCalculator
		double maxDistKm=1000.0;
		double maxDepthKm=24;
		double latLonDiscrDeg=0.01;
		double depthDiscr=1.0;
		double midLat=38;
		GriddedRegion gridRegForRatesInSpace = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(), latLonDiscrDeg, GriddedRegion.ANCHOR_0_0);

		System.out.println("/nMaking ETAS_LocationWeightCalculator");
		ETAS_LocationWeightCalculator etasLocWtCalc = new ETAS_LocationWeightCalculator(maxDistKm, maxDepthKm, latLonDiscrDeg, 
				depthDiscr, midLat, distDecay, minDist);
		
		System.out.println("/nMaking ERF_RatesAtPointsInSpace");
		double sourceRates[] = new double[this.getNumSources()];
		double duration = this.getTimeSpan().getDuration();
		for(int s=0;s<this.getNumSources();s++)
			sourceRates[s] = this.getSource(s).computeTotalEquivMeanAnnualRate(duration);
		ERF_RatesAtPointsInSpace erf_RatesAtPointsInSpace = new ERF_RatesAtPointsInSpace(gridRegForRatesInSpace, this, sourceRates, 24d,2d,0.1,null);


//		System.out.println("sleeping for 10 secs");
//		try {
//			Thread.sleep(10000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// this will store the active aftershock samplers (which give the index of an event inside the erf_RatesInSpace)
		HashMap<Integer,IntegerPDF_FunctionSampler> aftershockSamplerMap = new HashMap<Integer,IntegerPDF_FunctionSampler>();
		IntegerPDF_FunctionSampler firstAftershockSampler=null;

		
		CalcProgressBar progressBar = new CalcProgressBar("Events to process", "junk");
		progressBar.displayProgressBar();
		progressBar.showProgress(true);
		
		System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")\n");
		long runTime = System.currentTimeMillis();
		

		while(eventsToProcess.size()>0) {
			
			progressBar.updateProgress(simulatedRupsQueue.size(), eventsToProcess.size()+simulatedRupsQueue.size());
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			parID = rup.getParentID();
			int nthRup;
			int numToProcess=-1;
			EqkRupture mainshock = null;
//			double blockDist= Double.NaN;
			
			Location hypoLoc = null;
			ProbEqkRupture erf_rup;
			
			// the following samples an nth rup and populates the hypo loc.
			if(parID == -1)	{ // it's a spontaneous event
				nthRup = spontaneousRupSampler.getRandomInt();	// sample from long-term model
				erf_rup = getNthRupture(nthRup);
				LocationList surfPts = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				if(surfPts.size() == 1) {// point source
					Location ptLoc = surfPts.get(0);
					// FOLLOWING ASSUMES A GRID SPACING OF 0.1 FOR BACKGROUND SEIS, AND UNIFORM DIST OF DEPTHS UP TO maxDepthKm
					// "0.99" is to keep it in cell
					hypoLoc = new Location(ptLoc.getLatitude()+(Math.random()-0.5)*0.1*0.99,
							ptLoc.getLongitude()+(Math.random()-0.5)*0.1*0.99,
							Math.random()*maxDepthKm*0.999);
				}
				else {
					int hypIndex = (int)(Math.random()*(double)surfPts.size());	// choose random loc; randomize more if point source!
					hypoLoc = surfPts.get(hypIndex);
				}
			}
			else {
				// try to get sampler using parent ID (null if not yet there)
				IntegerPDF_FunctionSampler sampler = aftershockSamplerMap.get(parID);
				numToProcess = mainshockNumToProcess.get(parID);	// this is the number of events the sampler has yet to process
				if(sampler == null) {	// make the sampler and add to the list if it doesn't exist yet
					System.out.print("Making sampler for parID="+parID);
					long rt = System.currentTimeMillis();
					mainshock = mainshockHashMap.get(parID);
					// Create the new sampler
//					sampler = erf_RatesAtPointsInSpace.getPointSamplerWithDistDecay(mainshock, etasLocWtCalc);
					sampler = erf_RatesAtPointsInSpace.getPointSamplerWithOnlyDistDecay(mainshock, etasLocWtCalc);
//					sampler = erf_RatesAtPointsInSpace.getPointSampler();
					System.out.print("; that took "+(float)(System.currentTimeMillis()-rt)/(float)1000.0+" sec\n");
					if(numToProcess > 10)
						aftershockSamplerMap.put(parID, sampler);	// inefficient if there is only one to process
				}
				if(firstAftershockSampler == null) {	// save the first for plotting distance funcs
					firstAftershockSampler = sampler;
//					erf_RatesAtPointsInSpace.plotRatesMap(sampler, "First aftershock sampler", true, "firstSamplerMap");

//					if(obsEqkRuptureList.size() == 1) {	// plot stuff since it must be a large test event
//						firstSampler.plotDistDecayTestFuncs("Dist Decay", null);
//						firstSampler.plotBlockProbMap("Test", true, "testHere");
//						firstSampler.writeRelBlockProbToFile();
//					}
				}
//System.out.println("sampler.getNum()="+sampler.getNum()+"\t"+sampler.getY(sampler.getNum()-1));
//GraphWindow magDistsGraph = new GraphWindow(sampler, "Sampler");
				
				int randPtIndex = sampler.getRandomInt();
//System.out.println("sampler.getRandomInt()="+spaceIndex);
				
				int randSrcIndex = erf_RatesAtPointsInSpace.getRandomSourceAtPoint(randPtIndex);
				ProbEqkSource src = getSource(randSrcIndex);
				int r=0;
				if(src.getNumRuptures() > 1) {
					r = src.drawSingleRandomEqkRuptureIndex();
				}
				nthRup = getIndexN_ForSrcAndRupIndices(randSrcIndex,r);
				erf_rup = getNthRupture(nthRup);
					
				// set hypo location
				if(randSrcIndex<numNonZeroFaultSystemSources) {	// it's a fault system source
					// following should really sample one of the points on the surface?
					hypoLoc = erf_RatesAtPointsInSpace.getLocationForSamplerIndex(randPtIndex);
				}
				else {	// it's a point-source index
					Location sampLoc = erf_RatesAtPointsInSpace.getLocationForSamplerIndex(randPtIndex);
					// the "0.999" in the following is to keep the sample within block
					hypoLoc = new Location(sampLoc.getLatitude()+(Math.random()-0.5)*0.999*latLonDiscrDeg,
							sampLoc.getLongitude()+(Math.random()-0.5)*0.999*latLonDiscrDeg,
							sampLoc.getDepth()+(Math.random()-0.5)*0.999*depthDiscr);
				}
//nthRup=this.getIndexN_ForSrcAndRupIndices(4979, 0);
				
				numToProcess -= 1;	// decrement num to process
			}

			// transfer attributes
			rup.setAveRake(erf_rup.getAveRake());
			rup.setMag(erf_rup.getMag());
			RuptureSurface surf = erf_rup.getRuptureSurface();
			rup.setRuptureSurface(surf);
			rup.setNthERF_Index(nthRup);
			rup.setHypocenterLocation(hypoLoc);
			
			// add the rupture to the list
			simulatedRupsQueue.add(rup);
			
				
			// this isn't working:
			progressBar.setProgressMessage((float)rup.getMag()+"\t");
			
			// update num to process or clean up the ETAS samplers if this is zero
			if(parID != -1) {	// if not spontaneous
				if(numToProcess == 0) {
					mainshockNumToProcess.remove(parID);
					mainshockHashMap.remove(parID);
					aftershockSamplerMap.remove(parID);		
				}
				else {	// update the num to process
					mainshockNumToProcess.put(parID, numToProcess);
				}	
			}
			
			// now sample primary aftershock times for this event
			long rupOT = rup.getOriginTime();
//			parID = rup.getID();	// rupture is now the parent
//			int gen = rup.getGeneration()+1;
//			double startDay = 0;	// starting at origin time since we're within the timespan
//			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
//			double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
//			if(eventTimes.length>0) {
//				for(int i=0; i<eventTimes.length;i++) {
//					long ot = rupOT +  (long)(eventTimes[i]*(double)MILLISEC_PER_DAY);
//					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID, ot);
//					newRup.setGeneration(gen);
//					eventsToProcess.add(newRup);
//					eventID +=1;
//				}
//				mainshockHashMap.put(parID, rup);
//				mainshockNumToProcess.put(parID,eventTimes.length);				
//			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.

			if(nthRup<totNumRupsFromFaultSystem) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				
				Toolkit.getDefaultToolkit().beep();
				System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				System.out.println("nthRup="+"mag="+rup.getMag()+";  "+getSource(getSrcIndexForNthRup(nthRup)).getName());

				// set the start time for the time dependent calcs
				timeSpan.setStartTimeInMillis(rupOT);	
				
				// set the date of last event and slip for this rupture
				setRuptureOccurrenceTimePred(nthRup, rupOT);
				//				updateForecast();	// not needed

				double oldGain = probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)];
				System.out.println("s & r: "+getSrcIndexForNthRup(nthRup)+"\t"+getRupIndexInSourceForNthRup(nthRup));

				// update gains for next round (prevents running updateForecast())
				// need to do all since the start time has changed
				for(int s=0;s<numNonZeroFaultSystemSources;s++) {
					double probGain = computeTimePredProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
					if(Double.isNaN(probGain))  // NEEDED? (NOT DONE IN UPDATEFORECAST)
						probGainForFaultSystemSource[s] = 1;
					else
						probGainForFaultSystemSource[s] = probGain;
				}


				// now update source rates for erf_RatesAtPointsInSpace rates in space (for rups that change probs)
				System.out.println("Updating src rates for erf_RatesAtPointsInSpace");
				for(int s=0;s<this.getNumSources();s++) {
					double oldRate = sourceRates[s];
					sourceRates[s] = getSource(s).computeTotalEquivMeanAnnualRate(duration);
					double newRate = sourceRates[s];
					// TEST THAT RATE CHANGED PROPERLY
					if(s == getSrcIndexForNthRup(nthRup)) {
						System.out.println("\told gain="+oldGain+"\tnew gain="+probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)]);
						System.out.println("\told rate = "+oldRate+"\tnew rate = "+newRate);			
					}
				}

				// now update samplers
				// clear stored samplers because they are no longer applicable
				aftershockSamplerMap = new HashMap<Integer,IntegerPDF_FunctionSampler>();
				// tell erf_RatesInSpace to update the point sampler
				erf_RatesAtPointsInSpace.declareRateChange();	

				System.out.println("Done with fault system rupture updates");
			}
		}
		
		progressBar.showProgress(false);

		System.out.println("Looping over events took "+(System.currentTimeMillis()-runTime)/1000+" secs");

		System.out.println("Fault System Aftershocks:\n");
		for(Integer n : nthFaultSysRupAftershocks) {
			int s=srcIndexForNthRup[n];
			System.out.println("\t"+n+"\t"+s+"\t"+rupIndexForNthRup[n]+"\t"+fltSysRupIndexForNthRup[n]+
					"\tmag="+getNthRupture(n).getMag()+"\t"+getSource(s).getName());

		}

		String fileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/hypoTest.pdf";
		
		ETAS_SimAnalysisTools.writeDataToFile("testRightHere.txt", simulatedRupsQueue);

		if(obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, obsEqkRuptureList.get(0), simulatedRupsQueue, null);
//			ETAS_SimAnalysisTools.plotDistDecayForAshocks("test", null, simulatedRupsQueue,firstSampler, obsEqkRuptureList.get(0));
		}
		else {
			ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, null, simulatedRupsQueue, null);
//			ETAS_SimAnalysisTools.plotDistDecayForAshocks("test", null, simulatedRupsQueue,firstSampler, null);
		}
		ETAS_SimAnalysisTools.plotMagFreqDists("test", null, simulatedRupsQueue);
		
		
		System.out.println("Total num ruptures: "+simulatedRupsQueue.size());

	}

	
	
	
	/**
	 * This represents an ETAS simulation.  This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 * @param includeSpontEvents
	 * @param includeIndirectTriggering - include secondary, tertiary, etc events
	 * @param includeEqkRates - whether or not to include the long-term rate of events in sampling aftershocks
	 * @param gridSeisDiscr - lat lon discretization of gridded seismicity (degrees)

	 */
	public void testETAS_Simulation(GriddedRegion griddedRegion, ArrayList<ObsEqkRupture> obsEqkRuptureList, 
			boolean includeSpontEvents, boolean includeIndirectTriggering, boolean includeEqkRates, double gridSeisDiscr) {
		
		// this will store the aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
				
		normalizedRupRecurIntervals = new ArrayList<Double>();
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		if(!SIMULATION_MODE)
			throw new RuntimeException("This method can only be run if SIMULATION_MODE = true");

		long simStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		long simEndTime = timeSpan.getEndTimeCalendar().getTimeInMillis();
		double simDuration = timeSpan.getDuration();
		
		System.out.println("Updating forecast (twice)");
		// get the total rate over the duration of the forecast
		updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		double origTotRate = totalRate;	// this include ER time dependence, but diff shouldn't be noticeable.
		System.out.println("origTotRate="+origTotRate);
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		timeSpan.setDuration(1.0);	// annualize
		updateForecast();
		
		ETAS_Utils etas_utils = new ETAS_Utils();

		// Make list of primary events for given list of obs quakes 
		// (filling in origin time ID, and parentID, with the rest to be filled in later)
		System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>(); // this stores the active mainshocks
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a mainshock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTime-rupOT) / (double)MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
			// get a list of random primary event times
			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID,ot);
					newRup.setGeneration(1);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,randomAftShockTimes.length);
				parID += 1;				
			}
		}
		System.out.println("the "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" events");
		
		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		if(includeSpontEvents) {
			double fractionNonTriggered=0.5;	// really need to solve for this value
			double expectedNum = origTotRate*simDuration*fractionNonTriggered;
			System.out.println("expected num spontaneous: "+expectedNum+
					";\tfractionNonTriggered="+fractionNonTriggered+"; origTotRate="+origTotRate+"; origDuration="+simDuration);
			int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
			System.out.println("Making spontaneous events (times and event IDs only) - "+numSpontEvents+" were sampled");

			for(int r=0;r<numSpontEvents;r++) {
				ETAS_EqkRupture rup = new ETAS_EqkRupture();
				double ot = simStartTime+Math.random()*(simEndTime-simStartTime);	// random time over time span
				rup.setOriginTime((long)ot);
				rup.setID(eventID);
				rup.setParentID(-1);		// parent is long-term model
				rup.setGeneration(0);
				eventsToProcess.add(rup);
				eventID += 1;
			}
		}
		
//		Region regionForRates = new CaliforniaRegions.RELM_TESTING();
		Region regionForRates = new Region(griddedRegion.getBorder(),BorderType.MERCATOR_LINEAR);


		System.out.println("\nMaking ETAS_PrimaryEventSamplerAlt");
		long st = System.currentTimeMillis();
		// first make array of rates for each source
		double sourceRates[] = new double[getNumSources()];
		double duration = getTimeSpan().getDuration();
		for(int s=0;s<getNumSources();s++)
			sourceRates[s] = getSource(s).computeTotalEquivMeanAnnualRate(duration);

		// this is not yet used for anything
		String testFileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/testBinaryFile";
		
		
		
		
		
		// Make the ERF_RatesInSpace and ETAS_LocationWeightCalculator
//		double maxDepthKm=24;
//		double depthDiscr=2.0;
//		double latLonDiscrDeg=0.02;
//		double distDecay = 2;
//		double minDist = 0.3;
//		boolean includeDistDecay = true;
//		ETAS_PrimaryEventSamplerAlt etas_PrimEventSampler = new ETAS_PrimaryEventSamplerAlt(regionForRates, latLonDiscrDeg, this, 
//				sourceRates, maxDepthKm,depthDiscr,0.1,null, distDecay, minDist, includeEqkRates, includeDistDecay);
//		System.out.println("that took "+(System.currentTimeMillis()-st)/1000+ " sec");

		

		ETAS_PrimaryEventSamplerAlt etas_PrimEventSampler = new ETAS_PrimaryEventSamplerAlt(regionForRates, this, sourceRates, 
				gridSeisDiscr,null, includeEqkRates);
		System.out.println("that took "+(System.currentTimeMillis()-st)/1000+ " sec");
		
		double distDecay = etas_PrimEventSampler.getDistDecay();
		double minDist = etas_PrimEventSampler.getMinDist();
		double maxDepthKm = etas_PrimEventSampler.getMaxDepth();
		
		
		
		
		
		System.out.println("Testing the etas_PrimEventSampler");
		etas_PrimEventSampler.testRates();
		// time consuming:
//		etas_PrimEventSampler.testMagFreqDist();


//		System.out.println("sleeping for 10 secs");
//		try {
//			Thread.sleep(10000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// this will store the active aftershock samplers (which give the index of an event inside the erf_RatesInSpace)
//		HashMap<Integer,IntegerPDF_FunctionSampler> aftershockSamplerMap = new HashMap<Integer,IntegerPDF_FunctionSampler>();
//		IntegerPDF_FunctionSampler firstAftershockSampler=null;

		
		CalcProgressBar progressBar = new CalcProgressBar("Primary aftershocks to process", "junk");
		progressBar.showProgress(true);
		
		System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")\n");
		st = System.currentTimeMillis();
		
//		int counter =0;
		while(eventsToProcess.size()>0) {
			
			progressBar.updateProgress(simulatedRupsQueue.size(), eventsToProcess.size()+simulatedRupsQueue.size());
			
//			System.out.print(counter+", ");
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			parID = rup.getParentID();
			int numToProcess=-1;
			
			
			// the following samples an nth rup and populates the hypo loc.
			if(parID == -1)	{ // it's a spontaneous event
				Location hypoLoc = null;
				ProbEqkRupture erf_rup;
				int nthRup = spontaneousRupSampler.getRandomInt();	// sample from long-term model
				erf_rup = getNthRupture(nthRup);
				LocationList surfPts = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				if(surfPts.size() == 1) {// point source
					Location ptLoc = surfPts.get(0);
					// FOLLOWING ASSUMES A GRID SPACING OF 0.1 FOR BACKGROUND SEIS, AND UNIFORM DIST OF DEPTHS UP TO maxDepthKm
					// "0.99" is to keep it in cell
					hypoLoc = new Location(ptLoc.getLatitude()+(Math.random()-0.5)*0.1*0.99,
							ptLoc.getLongitude()+(Math.random()-0.5)*0.1*0.99,
							Math.random()*maxDepthKm*0.999);
				}
				else {
					int hypIndex = (int)(Math.random()*(double)surfPts.size());	// choose random loc; randomize more if point source!
					hypoLoc = surfPts.get(hypIndex);
				}
				
				rup.setAveRake(erf_rup.getAveRake());
				rup.setMag(erf_rup.getMag());
				RuptureSurface surf = erf_rup.getRuptureSurface();
				rup.setRuptureSurface(surf);
				rup.setNthERF_Index(nthRup);
				rup.setHypocenterLocation(hypoLoc);

			}
			else {
				numToProcess = mainshockNumToProcess.get(parID);	// this is the number of events the sampler has yet to process
				EqkRupture mainshock = mainshockHashMap.get(parID);
				etas_PrimEventSampler.setRandomPrimaryEvent(mainshock, rup);
//etas_PrimEventSampler.getTriggerProbOfEachSource( mainshock);
//System.exit(0);
				numToProcess -= 1;	// decrement num to process
			}

			// add the rupture to the list
			simulatedRupsQueue.add(rup);
				
			// this isn't working:
// 			progressBar.setProgressMessage((float)rup.getMag()+"\t");
			
			// update num to process or remove mainshock if this is zero
			if(parID != -1) {	// if not spontaneous
				if(numToProcess == 0) {
					mainshockNumToProcess.remove(parID);
					mainshockHashMap.remove(parID);
				}
				else {	// update the num to process
					mainshockNumToProcess.put(parID, numToProcess);
				}	
			}
			
			long rupOT = rup.getOriginTime();
			
			// now sample primary aftershock times for this event
			if(includeIndirectTriggering) {
				parID = rup.getID();	// rupture is now the parent
				int gen = rup.getGeneration()+1;
				double startDay = 0;	// starting at origin time since we're within the timespan
				double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
				double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
				if(eventTimes.length>0) {
					for(int i=0; i<eventTimes.length;i++) {
						long ot = rupOT +  (long)(eventTimes[i]*(double)MILLISEC_PER_DAY);
						ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID, ot);
						newRup.setGeneration(gen);
						eventsToProcess.add(newRup);
						eventID +=1;
					}
					mainshockHashMap.put(parID, rup);
					mainshockNumToProcess.put(parID,eventTimes.length);				
				}		
			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.

			int nthRup = rup.getNthERF_Index();
			if(nthRup<totNumRupsFromFaultSystem) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				
				Toolkit.getDefaultToolkit().beep();
				System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				System.out.println("nthRup="+"mag="+rup.getMag()+";  "+getSource(getSrcIndexForNthRup(nthRup)).getName());

				// set the start time for the time dependent calcs
				timeSpan.setStartTimeInMillis(rupOT);	
				
				// set the date of last event and slip for this rupture
				setRuptureOccurrenceTimePred(nthRup, rupOT);

				double oldGain = probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)];
				System.out.println("s & r: "+getSrcIndexForNthRup(nthRup)+"\t"+getRupIndexInSourceForNthRup(nthRup));

				// update gains for next round (prevents running updateForecast())
				// need to do all since the start time has changed
				for(int s=0;s<numNonZeroFaultSystemSources;s++) {
					// TODO change this to renewal case
					double probGain = computeTimePredProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
					if(Double.isNaN(probGain))  // NEEDED? (NOT DONE IN UPDATEFORECAST)
						probGainForFaultSystemSource[s] = 1;
					else
						probGainForFaultSystemSource[s] = probGain;
				}


				// now update source rates for etas_PrimEventSampler
				
				System.out.println("Updating src rates for etas_PrimEventSampler");
				for(int s=0;s<this.getNumSources();s++) {
					double oldRate = sourceRates[s];
					sourceRates[s] = getSource(s).computeTotalEquivMeanAnnualRate(duration);
					double newRate = sourceRates[s];
					// TEST THAT RATE CHANGED PROPERLY
					if(s == getSrcIndexForNthRup(nthRup)) {
						System.out.println("\told gain="+oldGain+"\tnew gain="+probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)]);
						System.out.println("\told rate = "+oldRate+"\tnew rate = "+newRate);			
					}
				}

				// now update the sampler
				etas_PrimEventSampler.declareRateChange();	

				System.out.println("Done with fault system rupture updates");
			}
		}
		
		progressBar.showProgress(false);

		System.out.println("Looping over events took "+(System.currentTimeMillis()-st)/1000+" secs");

		System.out.println("Fault System Aftershocks:\n");
		for(Integer n : nthFaultSysRupAftershocks) {
			int s=srcIndexForNthRup[n];
			System.out.println("\t"+n+"\t"+s+"\t"+rupIndexForNthRup[n]+"\t"+fltSysRupIndexForNthRup[n]+
					"\tmag="+getNthRupture(n).getMag()+"\t"+getSource(s).getName());

		}

		String dirName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/";
		
//		ETAS_SimAnalysisTools.writeDataToFile("testRightHere.txt", simulatedRupsQueue);

		if(obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			ETAS_SimAnalysisTools.plotEpicenterMap("", dirName+"hypoMap.pdf", obsEqkRuptureList.get(0), simulatedRupsQueue, griddedRegion.getBorder());
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("", dirName+"distDecay.pdf", simulatedRupsQueue, obsEqkRuptureList.get(0), distDecay, minDist);
			ETAS_SimAnalysisTools.plotNumVsLogTime("", dirName+"logTimeDecay.pdf", simulatedRupsQueue, obsEqkRuptureList.get(0));
			ETAS_SimAnalysisTools.plotNumVsTime("", dirName+"timeDecay.pdf", simulatedRupsQueue, obsEqkRuptureList.get(0));
		}
		else {
			ETAS_SimAnalysisTools.plotEpicenterMap("test", dirName+"hypoMap.pdf", null, simulatedRupsQueue, griddedRegion.getBorder());
			ETAS_SimAnalysisTools.plotDistDecayHistForAshocks("test", null, simulatedRupsQueue, null, distDecay, minDist);
		}
		ETAS_SimAnalysisTools.plotMagFreqDists("", dirName+"aftMFD.pdf", simulatedRupsQueue);
		
		
		
		System.out.println("Total num ruptures: "+simulatedRupsQueue.size());

	}

	
	/**
	 * This represents an ETAS simulation.  This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 */
	public void testETAS_SimulationOld2(GriddedRegion griddedRegion, ArrayList<ObsEqkRupture> obsEqkRuptureList) {
		
		// this will store the aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
		double distDecay = 2;
		double minDist = 0.3;
		boolean includeEqkRates = true;	// whether or not to include the long-term rate of events in sampling aftershocks
				
		normalizedRupRecurIntervals = new ArrayList<Double>();
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		if(!SIMULATION_MODE)
			throw new RuntimeException("This method can only be run if SIMULATION_MODE = true");

		long simStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		long simEndTime = timeSpan.getEndTimeCalendar().getTimeInMillis();
		double simDuration = timeSpan.getDuration();
		
		System.out.println("Updating forecast (twice)");
		// get the total rate over the duration of the forecast
		updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		double origTotRate = totalRate;	// this include ER time dependence, but diff shouldn't be noticeable.
		System.out.println("origTotRate="+origTotRate);
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		timeSpan.setDuration(1.0);	// annualize
		updateForecast();
		
		
		ETAS_Utils etas_utils = new ETAS_Utils();

		// Make list of primary events for given list of obs quakes 
		// (filling in origin time ID, and parentID, with the rest to be filled in later)
		System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>(); // this stores the active mainshocks
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a mainshock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();	// start IDs after input events
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTime-rupOT) / (double)MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
			// get a list of random primary event times
			double[] randomAftShockTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(randomAftShockTimes.length>0) {
				for(int i=0; i<randomAftShockTimes.length;i++) {
					long ot = rupOT +  (long)(randomAftShockTimes[i]*(double)MILLISEC_PER_DAY);	// convert to milliseconds
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID,ot);
					newRup.setGeneration(1);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,randomAftShockTimes.length);
				parID += 1;				
			}
		}
		System.out.println("the "+obsEqkRuptureList.size()+" input events produced "+eventsToProcess.size()+" events");
		
		
		// make the list of spontaneous events, filling in only event IDs and origin times for now
		double fractionNonTriggered=0.5;	// really need to solve for this value
		double expectedNum = origTotRate*simDuration*fractionNonTriggered;
		System.out.println("expected num spontaneous: "+expectedNum+
				";\tfractionNonTriggered="+fractionNonTriggered+"; origTotRate="+origTotRate+"; origDuration="+simDuration);
		int numSpontEvents = etas_utils.getPoissonRandomNumber(expectedNum);
//numSpontEvents=0;
		System.out.println("Making spontaneous events (times and event IDs only) - "+numSpontEvents+" were sampled");

		for(int r=0;r<numSpontEvents;r++) {
			ETAS_EqkRupture rup = new ETAS_EqkRupture();
			double ot = simStartTime+Math.random()*(simEndTime-simStartTime);	// random time over time span
			rup.setOriginTime((long)ot);
			rup.setID(eventID);
			rup.setParentID(-1);		// parent is long-term model
			rup.setGeneration(0);
			eventsToProcess.add(rup);
			eventID += 1;
		}
		
		
		
		// Make the ERF_RatesInSpace and ETAS_LocationWeightCalculator
		double maxDistKm=1000.0;
		double maxDepthKm=24;
		double latLonDiscrDeg=0.02;
		double depthDiscr=2.0;
		double midLat=38;
		GriddedRegion gridRegForRatesInSpace = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(), 0.02, GriddedRegion.ANCHOR_0_0);

		System.out.println("/nMaking ETAS_LocationWeightCalculator");
		ETAS_LocationWeightCalculator etasLocWtCalc = new ETAS_LocationWeightCalculator(maxDistKm, maxDepthKm, latLonDiscrDeg, 
				depthDiscr, midLat, distDecay, minDist);
		
		System.out.println("/nMaking ERF_RatesInSpace");
		String testFileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/testBinaryFile2km";
		// create file
//		ERF_RatesInSpace erf_RatesInSpace = new ERF_RatesInSpace(gridRegForRatesInSpace,this,maxDepthKm,depthDiscr,0.1,testFileName);
		// read file
		ERF_RatesInSpace erf_RatesInSpace = new ERF_RatesInSpace(gridRegForRatesInSpace,this,maxDepthKm,depthDiscr,testFileName);

		
//		// TEST TEST TEST
//		// Test (why just large rups sampled?):
//		System.out.println("/nTesting 10 samples");
//		IntegerPDF_FunctionSampler testSampler = erf_RatesInSpace.getPointSampler();
//		for(int i=0;i<10;i++) {
//			int[] rupOrSrcIndex = erf_RatesInSpace.getEqksAtPointForSamplerIndex(testSampler.getRandomInt()).getRandomRupOrSrc();
//			if(rupOrSrcIndex[0] == 0) {	// it's a rutpure index
//				System.out.println("rup sampled from source :"+this.getSource(getSrcIndexForNthRup(rupOrSrcIndex[1])).getName());
//			}
//			else {	// it's a point-source index
//				System.out.println("src sampled :"+this.getSource(rupOrSrcIndex[1]).getName());
//			}			
//		}

//		System.out.println("sleeping for 10 secs");
//		try {
//			Thread.sleep(10000L);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// this will store the active aftershock samplers (which give the index of an event inside the erf_RatesInSpace)
		HashMap<Integer,IntegerPDF_FunctionSampler> aftershockSamplerMap = new HashMap<Integer,IntegerPDF_FunctionSampler>();
		IntegerPDF_FunctionSampler firstAftershockSampler=null;

		
		CalcProgressBar progressBar = new CalcProgressBar("Events to process", "junk");
		progressBar.displayProgressBar();
		progressBar.showProgress(true);
		
		System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")\n");
		long runTime = System.currentTimeMillis();
		

		while(eventsToProcess.size()>0) {
			
			progressBar.updateProgress(simulatedRupsQueue.size(), eventsToProcess.size()+simulatedRupsQueue.size());
			
			ETAS_EqkRupture rup = eventsToProcess.poll();	//Retrieves and removes the head of this queue, or returns null if this queue is empty.
			
			parID = rup.getParentID();
			int nthRup;
			int numToProcess=-1;
			EqkRupture mainshock = null;
//			double blockDist= Double.NaN;
			
			Location hypoLoc = null;
			ProbEqkRupture erf_rup;
			
			// the following samples an nth rup and populates the hypo loc.
			if(parID == -1)	{ // it's a spontaneous event
				nthRup = spontaneousRupSampler.getRandomInt();	// sample from long-term model
				erf_rup = getNthRupture(nthRup);
				LocationList surfPts = erf_rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				if(surfPts.size() == 1) {// point source
					Location ptLoc = surfPts.get(0);
					// FOLLOWING ASSUMES A GRID SPACING OF 0.1 FOR BACKGROUND SEIS, AND UNIFORM DIST OF DEPTHS UP TO maxDepthKm
					// "0.99" is to keep it in cell
					hypoLoc = new Location(ptLoc.getLatitude()+(Math.random()-0.5)*0.1*0.99,
							ptLoc.getLongitude()+(Math.random()-0.5)*0.1*0.99,
							Math.random()*maxDepthKm*0.999);
				}
				else {
					int hypIndex = (int)(Math.random()*(double)surfPts.size());	// choose random loc; randomize more if point source!
					hypoLoc = surfPts.get(hypIndex);
				}
			}
			else {
				// try to get sampler using parent ID (null if not yet there)
				IntegerPDF_FunctionSampler sampler = aftershockSamplerMap.get(parID);
				numToProcess = mainshockNumToProcess.get(parID);	// this is the number of events the sampler has yet to process
				if(sampler == null) {	// make the sampler and add to the list if it doesn't exist yet
					System.out.print("Making sampler for parID="+parID);
					long rt = System.currentTimeMillis();
					mainshock = mainshockHashMap.get(parID);
					// Create the new sampler
					sampler = erf_RatesInSpace.getPointSamplerWithDistDecay(mainshock, etasLocWtCalc);
					System.out.print("; that took "+(System.currentTimeMillis()-rt)/1000+" sec\n");
//					sampler = erf_RatesInSpace.getPointSampler();
					if(numToProcess > 10)
						aftershockSamplerMap.put(parID, sampler);	// inefficient if there is only one to process
				}
				if(firstAftershockSampler == null) {	// save the first for plotting distance funcs
					firstAftershockSampler = sampler;
//					if(obsEqkRuptureList.size() == 1) {	// plot stuff since it must be a large test event
//						firstSampler.plotDistDecayTestFuncs("Dist Decay", null);
//						firstSampler.plotBlockProbMap("Test", true, "testHere");
//						firstSampler.writeRelBlockProbToFile();
//					}
				}
//System.out.println("sampler.getNum()="+sampler.getNum()+"\t"+sampler.getY(sampler.getNum()-1));
//GraphWindow magDistsGraph = new GraphWindow(sampler, "Sampler");
				
				int spaceIndex = sampler.getRandomInt();
//System.out.println("sampler.getRandomInt()="+spaceIndex);

				int[] rupOrSrcIndex = erf_RatesInSpace.getEqksAtPointForSamplerIndex(spaceIndex).getRandomRupOrSrc();
				if(rupOrSrcIndex[0] == 0) {	// it's a rupture index
					nthRup = rupOrSrcIndex[1];
					// following should really sample one of the points on the surface?
					hypoLoc = erf_RatesInSpace.getLocationForSamplerIndex(spaceIndex);
				}
				else {	// it's a point-source index
					int r = getSource(rupOrSrcIndex[1]).drawSingleRandomEqkRuptureIndex();
					nthRup = getIndexN_ForSrcAndRupIndices(rupOrSrcIndex[1], r);
					Location sampLoc = erf_RatesInSpace.getLocationForSamplerIndex(spaceIndex);
					// the "0.999" in the following is to keep the sample within block
					hypoLoc = new Location(sampLoc.getLatitude()+(Math.random()-0.5)*0.999*latLonDiscrDeg,
							sampLoc.getLongitude()+(Math.random()-0.5)*0.999*latLonDiscrDeg,
							sampLoc.getDepth()+(Math.random()-0.5)*0.999*depthDiscr);
				}
//nthRup=this.getIndexN_ForSrcAndRupIndices(4979, 0);
				erf_rup = getNthRupture(nthRup);
				numToProcess -= 1;	// decrement num to process
//				mainshock = sampler.getParentRup();
			}

			// transfer attributes
			rup.setAveRake(erf_rup.getAveRake());
			rup.setMag(erf_rup.getMag());
			RuptureSurface surf = erf_rup.getRuptureSurface();
			rup.setRuptureSurface(surf);
			rup.setNthERF_Index(nthRup);
			rup.setHypocenterLocation(hypoLoc);

			
			// add the rupture to the list
			simulatedRupsQueue.add(rup);
			
				
			// this isn't working:
			progressBar.setProgressMessage((float)rup.getMag()+"\t");
			
			// update num to process or clean up the ETAS samplers if this is zero
			if(parID != -1) {	// if not spontaneous
				if(numToProcess == 0) {
					mainshockNumToProcess.remove(parID);
					mainshockHashMap.remove(parID);
					aftershockSamplerMap.remove(parID);		
				}
				else {	// update the num to process
					mainshockNumToProcess.put(parID, numToProcess);
				}	
			}
			
			// now sample primary aftershock times for this event
			long rupOT = rup.getOriginTime();
			parID = rup.getID();	// rupture is now the parent
			int gen = rup.getGeneration()+1;
			double startDay = 0;	// starting at origin time since we're within the timespan
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
			double[] eventTimes = etas_utils.getDefaultRandomEventTimes(rup.getMag(), startDay, endDay);
			if(eventTimes.length>0) {
				for(int i=0; i<eventTimes.length;i++) {
					long ot = rupOT +  (long)(eventTimes[i]*(double)MILLISEC_PER_DAY);
					ETAS_EqkRupture newRup = new ETAS_EqkRupture(parID, eventID, ot);
					newRup.setGeneration(gen);
					eventsToProcess.add(newRup);
					eventID +=1;
				}
				mainshockHashMap.put(parID, rup);
				mainshockNumToProcess.put(parID,eventTimes.length);				
			}
			
			
			// if it was a fault system rupture, need to update time span, rup rates, block, and samplers.

			if(nthRup<totNumRupsFromFaultSystem) {
				
				nthFaultSysRupAftershocks.add(nthRup);
				
				Toolkit.getDefaultToolkit().beep();
				System.out.println("GOT A FAULT SYSTEM RUPTURE!");
				System.out.println("nthRup="+"mag="+rup.getMag()+";  "+getSource(getSrcIndexForNthRup(nthRup)).getName());

				// set the start time for the time dependent calcs
				timeSpan.setStartTimeInMillis(rupOT);	
				
				// set the date of last event and slip for this rupture
				setRuptureOccurrenceTimePred(nthRup, rupOT);
				//				updateForecast();	// not needed

				double oldGain = probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)];
				System.out.println("s & r: "+getSrcIndexForNthRup(nthRup)+"\t"+getRupIndexInSourceForNthRup(nthRup));
				double oldProb = getNthRupture(nthRup).getProbability();
//				System.out.println("s & r: "+getSrcIndexForNthRup(nthRup)+"\t"+getRupIndexInSourceForNthRup(nthRup));
				double oldRate = getNthRupture(nthRup).getMeanAnnualRate(1.0);
				double oldRate2 = -Math.log(1 - oldProb)/1.0;
//				System.out.println("\told rate = "+oldRate+"\n\told rate2 = "+oldRate2+"\n\told prob = "+oldProb);


				// update gains for next round (prevents running updateForecast())
				// need to do all since the start time has changed
				for(int s=0;s<numNonZeroFaultSystemSources;s++) {
					double probGain = computeTimePredProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
					if(Double.isNaN(probGain))  // NEEDED? (NOT DONE IN UPDATEFORECAST)
						probGainForFaultSystemSource[s] = 1;
					else
						probGainForFaultSystemSource[s] = probGain;
				}


				// now update rates in space (for rups that change probs)
				System.out.println("Updating rates in space");
				for(int s=0; s<numNonZeroFaultSystemSources;s++) {
					ProbEqkSource src = getSource(s);
					// now assuming all rups in source have same rupture surface
					LocationList locsOnSurface = src.getRupture(0).getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
					HashSet<EqksAtPoint> eqksAtPointForSourceHashSet = new HashSet<EqksAtPoint>();	// this avoids duplicates
					for(Location loc: locsOnSurface)
						eqksAtPointForSourceHashSet.add(erf_RatesInSpace.getEqksAtPointForLoc(loc));
					for(int r=0; r<src.getNumRuptures();r++) {
						erf_rup = src.getRupture(r);
						double newRate = erf_rup.getMeanAnnualRate(timeSpan.getDuration());
						double newProb = erf_rup.getProbability();
						int nthRupIndex = getIndexN_ForSrcAndRupIndices(s, r);
						
						if(Double.isInfinite(newRate) || Double.isNaN(newRate))
							throw new RuntimeException("Error: newRate="+newRate+"\tnewProb="+newProb+
									"\ttimeSpan.getDuration()="+timeSpan.getDuration());


						// TEST THAT RATE CHANGED PROPERLY
						if(nthRup == nthRupIndex) {	// check change for the one that occurred
							// write the before and after gain change for the event that occurred
							System.out.println("\told gain="+oldGain+"\tnew gain="+probGainForFaultSystemSource[getSrcIndexForNthRup(nthRup)]);
							System.out.println("\told prob = "+oldProb+"\tnew prob = "+newProb);
							System.out.println("\told rate = "+oldRate+"\toldRate2 = "+oldRate2+"\tnew rate = "+newRate);
						}
						
						
						for(EqksAtPoint qksAtPt : eqksAtPointForSourceHashSet) {
							qksAtPt.changeRupRate(newRate, nthRupIndex);
						}
					}
				}

				// now update samplers
//				System.out.println("Updating etasSamplers");
				// clear stored samplers because they are no longer applicable
				aftershockSamplerMap = new HashMap<Integer,IntegerPDF_FunctionSampler>();
				// tell erf_RatesInSpace to update the point sampler
				erf_RatesInSpace.declareRateChange();	

				System.out.println("Done with fault system rupture updates");
			}
		}
		
		progressBar.showProgress(false);

		System.out.println("Looping over events took "+(System.currentTimeMillis()-runTime)/1000+" secs");

		System.out.println("Fault System Aftershocks:\n");
		for(Integer n : nthFaultSysRupAftershocks) {
			int s=srcIndexForNthRup[n];
			System.out.println("\t"+n+"\t"+s+"\t"+rupIndexForNthRup[n]+"\t"+fltSysRupIndexForNthRup[n]+
					"\tmag="+getNthRupture(n).getMag()+"\t"+getSource(s).getName());

		}

		String fileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/hypoTest.pdf";
		
		ETAS_SimAnalysisTools.writeDataToFile("testRightHere.txt", simulatedRupsQueue);

		if(obsEqkRuptureList.size()==1) {	// assume the one event is some big test event (e.g., Landers)
			ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, obsEqkRuptureList.get(0), simulatedRupsQueue, null);
//			ETAS_SimAnalysisTools.plotDistDecayForAshocks("test", null, simulatedRupsQueue,firstSampler, obsEqkRuptureList.get(0));
		}
		else {
			ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, null, simulatedRupsQueue, null);
//			ETAS_SimAnalysisTools.plotDistDecayForAshocks("test", null, simulatedRupsQueue,firstSampler, null);
		}
		ETAS_SimAnalysisTools.plotMagFreqDists("test", null, simulatedRupsQueue);
		
		
		System.out.println("Total num ruptures: "+simulatedRupsQueue.size());

	}

	
//	
//	public ProbEqkSource getSource(int iSource) {
////		if(iSource == lastSrcRequested)
////			return currentSrc;
////		else {
//			ProbEqkSource src = super.getSource(iSource);
//			// ajust for time dependence if it's a fault-based source
//			if (iSource < numNonZeroFaultSystemSources && probGainForFaultSystemSource != null) { 
//				// the latter test is needed because the parent updateForecast ends up calling this 
//				// method, and probGainForFaultSystemSource isn't populated until the 
//				// parent constructor is called (because we don't know it's size)
//				((FaultRuptureSource)src).scaleRupRates(probGainForFaultSystemSource[iSource]);
//			}
////			currentSrc = src;
////			lastSrcRequested = iSource;		
//			return src;
////		}
//	}
}
