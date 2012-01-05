package scratch.UCERF3.erf;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.math.random.RandomDataImpl;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupOrigTimeComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.UCERF3.FaultSystemSolution;
import scratch.ned.ETAS_ERF.ETAS_EqkRupture;
import scratch.ned.ETAS_ERF.ETAS_PrimaryEventSampler;
import scratch.ned.ETAS_ERF.ETAS_SimAnalysisTools;
import scratch.ned.ETAS_ERF.EqksInGeoBlock;
import scratch.ned.ETAS_ERF.EqksInGeoBlockUtils;
import scratch.ned.ETAS_Tests.ETAS_Utils;
import scratch.ned.ETAS_Tests.IntegerPDF_FunctionSampler;

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
	
	// this is the probability gain for each fault system source
	double[] probGainForFaultSystemSource;
	
	// this keeps track of time span changes
	boolean timeSpanChangeFlag=true;
	
	// aperiodicity parameter and primitive
	BPT_AperiodicityParam bpt_AperiodicityParam;
	double bpt_Aperiodicity;
	boolean bpt_AperiodicityChanged;
	
	// these fields are for simulation mode (stochastic event sets).
	public boolean SIMULATION_MODE = true;
	ArrayList<Double> normalizedRecurIntervals;
	double totalRate;
	IntegerPDF_FunctionSampler ruptureSampler=null;
	double[] longTermRateOfNthRups;
	double[] magOfNthRups;
	
	
	/**
	 * This creates the ERF from the given FaultSystemSolution.  FileParameter is removed 
	 * from the adjustable parameter list (to prevent changes after instantiation).
	 * @param faultSysSolution
	 */
	public FaultSystemSolutionTimeDepERF(FaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
		initAdjustableParams();
		initiateTimeSpan();
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
		
		// first check whether file changed (this info will be erased by parent updataForecast(), and it's needed below)
		boolean fileChange = fileParamChanged;
		
		// the following is needed here because super.updateForecast() will call the local getSource 
		// method, which uses this array (values are set as 1.0 when this array is null)
		if(timeSpanChangeFlag || fileChange || bpt_AperiodicityChanged)
			probGainForFaultSystemSource = null;

		// now update forecast using super (only does something if a param has changed)
		super.updateForecast();	// inefficient if only bpt_Aperiodicity has changed
		
		// rest this to be safe
		lastSrcRequested=-1;
		
		System.out.println("time span duration = "+timeSpan.getDuration());

		// fill in totalRate, longTermRateOfNthRups, and magOfNthRups, where the first two do not include time dependence
		if(SIMULATION_MODE && (fileChange || aleatoryMagAreaStdDevChanged)) {
			// note that all gains should be 1.0 at this point
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

		
		// now update the the prob gains if needed (must be done after the above)
		if(timeSpanChangeFlag || fileChange || bpt_AperiodicityChanged) {
			System.out.println("updating all prob gains");
			probGainForFaultSystemSource = new double[numFaultSystemSources];
			for(int s=0; s<numFaultSystemSources; s++)
				probGainForFaultSystemSource[s] = computeProbGainForFaultSysRup(fltSysRupIndexForSource[s]);	// NaN if no data available
			timeSpanChangeFlag = false;
			
			//			long runTime = System.currentTimeMillis();
			// set ruptureSampler if in simulation mode
			if(SIMULATION_MODE) {
				if(ruptureSampler==null) {		// loop over all sources
					totalRate=0;
					ruptureSampler = new IntegerPDF_FunctionSampler(totNumRups);
					int nthRup=0;
					for(int s=0;s<getNumSources();s++) {
						ProbEqkSource src = getSource(s);
						for(ProbEqkRupture rup:src) {
							double rate = rup.getMeanAnnualRate(timeSpan.getDuration());
							totalRate += rate;
							ruptureSampler.set(nthRup, rate);
							nthRup+=1;
						}
					}
				}
				else {			// loop only over fault system sources
					int nthRup=0;
					for(int s=0;s<numFaultSystemSources;s++) {	
						ProbEqkSource src = getSource(s);
						for(ProbEqkRupture rup:src) {
							double rate = rup.getMeanAnnualRate(timeSpan.getDuration());
							totalRate += rate - ruptureSampler.getY(nthRup);	// sutracting our the old rate
							ruptureSampler.set(nthRup, rate);
							nthRup+=1;
						}
					}
					// this will be different from the above printing of totalRate only if the fault sections have date and slip in last event info
					if (D) System.out.println("totalRate = "+totalRate+"\t"+ruptureSampler.getSumOfY_vals());
				}
			}
		}
		
		if (D) System.out.println("totNumRups="+totNumRups+"\t"+ruptureSampler.getNum());

//		int runTimeSec = (int)(System.currentTimeMillis()-runTime)/1000;
//		System.out.println("ruptureSampler took "+runTimeSec+" sec to make");

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
	 * This method resets the slip and date of last events for all FaultSectionPrefData
	 * utilized by the specified rupture (and for the given event time).  This also adds 
	 * the normalized RI to normalizedRecurIntervals if in simulation mode.  This does not
	 * update the probability gains because the next start time may differ from the origin
	 * time given here.
	 * @param nthRup
	 * @param eventTimeInMillis
	 */
	public void setRuptureOccurrence(int nthRup, long eventTimeInMillis) {
		// only process if it's a fault system rupture
		if(nthRup < totNumRupsFromFaultSystem) {
			// save the normalize recurrence interval?
			if(SIMULATION_MODE) {
				double normRI = getNormalizedRecurInterval(nthRup, eventTimeInMillis);
				if(!Double.isNaN(normRI)) {
					normalizedRecurIntervals.add(normRI);
					if(D)System.out.println("added normRI");
				}
			}
// if(srcIndexForNthRup[nthRup] == 4755) System.out.println("Resetting slip and time for s=4755; time="+eventTimeInMillis);

			// reset slip and date of last event in fault section data
			int fltSysIndex = fltSysRupIndexForNthRup[nthRup];
			List<FaultSectionPrefData> fltData = faultSysSolution.getFaultSectionDataForRupture(fltSysIndex);
			double[] slipArray = faultSysSolution.getSlipOnSectionsForRup(fltSysIndex);
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
	 * This does the time-dependent calculation in my appendix.  A value of Double.NaN
	 * is returned if there were no fault sections with both date of and amount of slip
	 * in last event.
	 * @param faultSysRupIndex
	 * @return
	 */
	protected double computeProbGainForFaultSysRup(int faultSysRupIndex) {
		List<FaultSectionPrefData> fltData = faultSysSolution.getFaultSectionDataForRupture(faultSysRupIndex);
		double aveExpRI=0, totArea=0, usedArea=0;
		long aveDateOfLast = 0;
		for(FaultSectionPrefData data: fltData) {
			long dateOfLast = data.getDateOfLastEvent();
			double slipInLast = data.getSlipInLastEvent();
			double area = data.getTraceLength()*data.getReducedDownDipWidth();
			totArea += area;
			if(dateOfLast != Long.MIN_VALUE && slipInLast != Double.NaN) {
				aveExpRI += (slipInLast/(data.getReducedAveSlipRate()*1e-3))*area;  // weight averaged by area
				aveDateOfLast += dateOfLast*area;
				usedArea += area;
			}
		}
		double gain = Double.NaN;	// default is NaN
		if(usedArea != 0.0) {
			aveExpRI /= usedArea;		// years
			aveDateOfLast /= usedArea;  // epoch millis

			long startTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
			long endTime = timeSpan.getEndTimeCalendar().getTimeInMillis();

			double timeSinceLast = (startTime-aveDateOfLast)/MILLISEC_PER_YEAR;
			if(timeSinceLast <0)
				throw new RuntimeException("timeSinceLast cannot be negative (startTime="+
						startTime+" and aveDateOfLast="+aveDateOfLast+"; "+timeSinceLast+" yrs)");
			
			double duration = (endTime-startTime)/MILLISEC_PER_YEAR;

			// this is to get the final result to be consistent in terms of rates and COV
			double alphaCorr=1d;
			double riCorr=1d;
//			double alphaCorr=0.9;
//			double riCorr=1.2;
			double prob_bpt = BPT_DistCalc.getCondProb(aveExpRI*riCorr, bpt_Aperiodicity*alphaCorr, timeSinceLast, duration);
			double prob_pois = 1-Math.exp(-duration/aveExpRI);

			gain = (prob_bpt/prob_pois)*(usedArea/totArea) + 1.0*(totArea-usedArea)/totArea; // areas with no data get prob gain of 1.0
			
			// bump up gain to ensure events occur
//			gain *= 10;
			
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
	 * This returns the ratio of the observed to predicted recurrence interval, or
	 * Double.NaN if any of the slip or date-of-last-event-data are not available
	 * from any section involved.
	 * @param nthRup
	 * @param eventTimeInMillis
	 * @return
	 */
	private double getNormalizedRecurInterval(int nthRup, long eventTimeInMillis) {
		List<FaultSectionPrefData> fltData = faultSysSolution.getFaultSectionDataForRupture(fltSysRupIndexForNthRup[nthRup]);
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
		
		List<Integer> sectionIndices = faultSysSolution.getSectionsIndicesForRup(fltSysRupId);

//		long runtime = System.currentTimeMillis();
		
		// get fault system rups that are influenced
		HashSet<Integer> fltSysRups = new HashSet<Integer>();

		// the following was a huge bottleneck
//		for(Integer sectIndex: sectionIndices) {
//			for(Integer rup:faultSysSolution.getRupturesForSection(sectIndex)) {
//				if(!fltSysRups.contains(rup)) // filter duplicates
//					fltSysRups.add(rup);  				
//			}
//		}
		
		// this is much faster, but there still may be a faster way (use HashSet in first place?)
		for(Integer sectIndex: sectionIndices) {
			fltSysRups.addAll(faultSysSolution.getRupturesForSection(sectIndex));  				
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
	

	public void timeSpanChange(EventObject event) {
		timeSpanChangeFlag = true;
		if(D) System.out.println("TimeSpan changed");
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
	 * @param durationInYears
	 */
	public void testER_Simulation() {
		long origStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		double origDuration = timeSpan.getDuration();
		SIMULATION_MODE=true;
		initiateTimeSpan();	// just in case the non-simulation timeSpan was in use
		normalizedRecurIntervals = new ArrayList<Double>();
		double startYear = ((double)origStartTime)*MILLISEC_PER_YEAR+1970.0;
		long startTimeMillis = origStartTime;

		timeSpan.setDuration(1.0);
		System.out.println("start time: "+origStartTime+ " millis ("+startYear+" yrs)");
		System.out.println("originalDuration: "+origDuration+" ("+timeSpan.getDurationUnits()+")");
		int numRups=0;
		
		// apache tool for sampling from exponential distribution here
		RandomDataImpl randomDataSampler = new RandomDataImpl();
		
		// create the ERF and make the target MFD
		if(D) System.out.println("Updating forecast");
		updateForecast();
		if(D) System.out.println("Making target MFD");
		SummedMagFreqDist targetMFD = ERF_Calculator.getTotalMFD_ForERF(this, 2.05, 8.95, 70, true);
		targetMFD.setName("Target MFD");
		targetMFD.setInfo(" ");
//		System.out.println(targetMFD);

		// MFD for simulation
		SummedMagFreqDist obsMFD = new SummedMagFreqDist(5.05,8.95,40);
		
		double counter=0;
		int percDone=0;
		System.out.println(percDone+"% done");
		double yr=startYear;
		long startRunTime = System.currentTimeMillis();
		while (yr<origDuration+startYear) {
			// write progress
			if(counter > origDuration/50) {
				counter =0;
				percDone += 2;
				double timeInMin = ((double)(System.currentTimeMillis()-startRunTime)/(1000.0*60.0));
				System.out.println("\n"+percDone+"% done in "+(float)timeInMin+" minutes\n");	
			}
			
//			System.out.println(numRups+"\t"+yr);
			
			startTimeMillis = timeSpan.getStartTimeCalendar().getTimeInMillis();
//			System.out.println("Start time: "+startTimeMillis+"\t"+yr+"\t"+(1970+(double)startTimeMillis/MILLISEC_PER_YEAR));
			
//			long time = System.currentTimeMillis();
//			updateForecast();
//			runTimeForUpdateForecast += System.currentTimeMillis()-time;

			double timeOfNextInYrs = randomDataSampler.nextExponential(1.0/totalRate);
			long eventTimeMillis = startTimeMillis + (long)(timeOfNextInYrs*MILLISEC_PER_YEAR);
//			System.out.println("Event time: "+eventTimeMillis+" ("+(yr+timeOfNextInYrs)+" yrs)");
			int nthRup = ruptureSampler.getRandomInt();
			setRuptureOccurrence(nthRup, eventTimeMillis);
//			System.out.print((float)timeOfNextInYrs+" ("+(float)totalRate+"); ");	
//			System.out.print(numRups+"\t"+nthRup+"\t"+(float)timeOfNextInYrs+" ("+(float)totalRate+"); \n");	

			numRups+=1;
			obsMFD.addResampledMagRate(magOfNthRups[nthRup], 1.0, true);
			yr+=timeOfNextInYrs;
			counter +=timeOfNextInYrs;
			timeSpan.setStartTimeInMillis(eventTimeMillis); // this is needed for the elastic rebound probs

			// update gains for next loop
			for(int s=0;s<numFaultSystemSources;s++) {
				double probGain = computeProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
				if(Double.isNaN(probGain))
					probGainForFaultSystemSource[s] = 1;
				else
					probGainForFaultSystemSource[s] = probGain;
			}
			// now update totalRate and ruptureSampler (for rups that change probs)
			for(int n=0; n<totNumRupsFromFaultSystem;n++) {
//				double newRate = longTermRateOfNthRups[n];	// poisson probs
				double newRate = longTermRateOfNthRups[n] * probGainForFaultSystemSource[srcIndexForNthRup[n]];
				ruptureSampler.set(n, newRate);
			}
			totalRate = ruptureSampler.getSumOfY_vals();
		}
		System.out.println("numRups="+numRups);
		

		System.out.println("normalizedRecurIntervals.size()="+normalizedRecurIntervals.size());
//		for(Double nRI:normalizedRecurIntervals)
//			System.out.println(nRI);
		
		GraphiWindowAPI_Impl plot = General_EQSIM_Tools.plotNormRI_Distribution(normalizedRecurIntervals, 
				"Normalized RIs");
		
//		System.out.println(obsMFD);

		// plot MFDs
		obsMFD.scale(1.0/origDuration);
		obsMFD.setName("Simulated MFD");
		obsMFD.setInfo(" ");

		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(targetMFD);
		funcs.add(obsMFD);
		funcs.add(targetMFD.getCumRateDistWithOffset());
		funcs.add(obsMFD.getCumRateDistWithOffset());
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Incremental Mag-Freq Dists"); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
//		graph.setYLog(true);	// this causes problems
		graph.setY_AxisRange(1e-6, 1.0);

	}
	
	
	/**
	 * This represents an ETAS simulation.  This assume ER probabilities are constant up until 
	 * the next fault-system event (only works if fault system events occur every few years or
	 * less).
	 * @param griddedRegion
	 * @param obsEqkRuptureList
	 */
	public void testETAS_Simulation(GriddedRegion griddedRegion, ArrayList<ObsEqkRupture> obsEqkRuptureList) {
		
		double distDecay = 2;
		double minDist = 0.3;
		boolean useAdaptiveBlocks= true; 
		boolean includeBlockRates = true;
		
		double maxBlockDepth=24;
		
		normalizedRecurIntervals = new ArrayList<Double>();
		
		// this is for keeping track of aftershocks on the fault system
		ArrayList<Integer> nthFaultSysRupAftershocks = new ArrayList<Integer>();
		
		if(!SIMULATION_MODE)
			throw new RuntimeException("This method can only be run if SIMULATION_MODE = true");

		long simStartTime = timeSpan.getStartTimeCalendar().getTimeInMillis();
		long simEndTime = timeSpan.getEndTimeCalendar().getTimeInMillis();
		double simDuration = timeSpan.getDuration();
		
		System.out.println("Updating forecast (twice)");
		updateForecast();	// do this to get annual rate over the entire forecast (used to sample spontaneous events)
		double origTotRate = totalRate;	// this include ER time dependence, but diff shouldn't be noticeable.
		
		// set to yearly probabilities for simulation forecast (in case input was not a 1-year forecast)
		timeSpan.setDuration(1.0);	// annualize
		updateForecast();

		System.out.println("origTotRate="+origTotRate);
		
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
		
//		System.out.println("RIGHT HERE");
////		blockList.get(630).testThisBlock(this);
//		EqksInGeoBlock testBlock = blockList.get(630);
//		Location blockCenterLoc = testBlock.getBlockCenterLoc();
//		System.out.println("Working on Rupture 97");
//		LocationList list = this.getNthRupture(97).getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
//		for(Location loc: list) {
//			boolean containedHere = testBlock.isLocInside(loc);
//			Location locFromRegion = griddedRegion.getLocation(griddedRegion.indexForLocation(loc));
//			double latDiff = Math.abs(locFromRegion.getLatitude()-blockCenterLoc.getLatitude());
//			double lonDiff = Math.abs(locFromRegion.getLongitude()-blockCenterLoc.getLongitude());
//			boolean locFromRegionSame=false;
//			if(latDiff<0.05 && lonDiff<0.05)
//				locFromRegionSame = true;
//			
//			if(containedHere != locFromRegionSame) {
//				System.out.println("Location "+loc+"\tlocFromRegion = "+locFromRegion+"\tblockCenterLoc = "+blockCenterLoc);
//			}
//		}

		
//		System.out.println("RATE OF NthRUP 4743 = "+this.getNthRupture(4743).getMeanAnnualRate(timeSpan.getDuration()));
		
		ETAS_Utils etas_utils = new ETAS_Utils();

		// Make list of primary events for given list of obs quakes (filling in origin time ID, and parentID, with the rest to be filled in later)
		System.out.println("Making primary aftershocks from input obsEqkRuptureList, size = "+obsEqkRuptureList.size());
		ObsEqkRupOrigTimeComparator otComparator = new ObsEqkRupOrigTimeComparator();	// this will keep the event in order of origin time
		PriorityQueue<ETAS_EqkRupture>  eventsToProcess = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);	// not sure about the first field
		HashMap<Integer,ObsEqkRupture> mainshockHashMap = new HashMap<Integer,ObsEqkRupture>();
		HashMap<Integer,Integer> mainshockNumToProcess = new HashMap<Integer,Integer>();	// this keeps track of how many more aftershocks a mainshock needs to generate
		int parID=0;	// this will be used to assign an id to the given events
		int eventID = obsEqkRuptureList.size();
		for(ObsEqkRupture rup: obsEqkRuptureList) {
			long rupOT = rup.getOriginTime();
			double startDay = (double)(simStartTime-rupOT) / (double)MILLISEC_PER_DAY;	// convert epoch to days from event origin time
			double endDay = (double)(simEndTime-rupOT) / (double)MILLISEC_PER_DAY;
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
		double fractionNonTriggered=0.5;
		System.out.println("expected num spontaneous: "+origTotRate*simDuration*fractionNonTriggered+
				";\tfractionNonTriggered="+fractionNonTriggered+"; origTotRate="+origTotRate+"; origDuration="+simDuration);
		int numSpontEvents = etas_utils.getPoissonRandomNumber(fractionNonTriggered*origTotRate*simDuration);
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
		
		// this will store the aftershocks & spontaneous events (in order of occurrence) - ObsEqkRuptureList? (they're added in order anyway)
		PriorityQueue<ETAS_EqkRupture>  simulatedRupsQueue = new PriorityQueue<ETAS_EqkRupture>(1000, otComparator);
		
		// also make the primary event samplers, where the key is the event ID
		HashMap<Integer,ETAS_PrimaryEventSampler> etasSamplerMap = new HashMap<Integer,ETAS_PrimaryEventSampler>();
		
		System.out.println("Looping over eventsToProcess (initial num = "+eventsToProcess.size()+")\n");
		
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
			if(parID == -1)	{ // it's a spontaneous event
				nthRup = ruptureSampler.getRandomInt();	// sample from long-term model
			}
			else {
				// try to get sampler using parent ID (null if not yet there)
				ETAS_PrimaryEventSampler sampler = etasSamplerMap.get(parID);
				numToProcess = mainshockNumToProcess.get(parID);	// this is the number of events the sampler has yet to process
				if(sampler == null) {	// make the sampler and add to the list if it doesn't exist yet
					ObsEqkRupture mainshock = mainshockHashMap.get(parID);
					sampler = new ETAS_PrimaryEventSampler(mainshock, blockList, subBlockList1, subBlockList2,
							this, distDecay, minDist, useAdaptiveBlocks, includeBlockRates);
					etasSamplerMap.put(parID, sampler);	// inefficient if there is only one to process
// the following is not needed because this test is now in the ETAS_PrimaryEventSampler constructor
// EqksInGeoBlockUtils.testSubBlockListRates(this, blockList, subBlockList1, subBlockList2);
				}
				block = sampler.sampleRandomBlock();	// get the block where the aftershock occurs
				nthRup = block.getRandomRuptureIndexN();  // get the nth index of the randomly chosen rupture
				numToProcess -= 1;	// decrement num to process
			}
			// get the rupture from the ERF
			ProbEqkRupture erf_rup = getNthRupture(nthRup);
			// transfer attributes
			rup.setAveRake(erf_rup.getAveRake());
			rup.setMag(erf_rup.getMag());
			RuptureSurface surf = erf_rup.getRuptureSurface();
			rup.setRuptureSurface(surf);
			rup.setNthERF_Index(nthRup);
			// set hypocenter if it's spontaneous
			if(parID == -1) {
				LocationList surfPts = surf.getEvenlyDiscritizedListOfLocsOnSurface();
				int hypIndex = (int)(Math.random()*(double)surfPts.size());	// choose random loc
				rup.setHypocenterLocation(surfPts.get(hypIndex));
			}
			else {	// set hypocenter usig the box
				block.setRandomHypocenterLoc(rup);
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
			
			// clean up the ETAS simulators if no longer needed
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
				System.out.println("nthRup="+nthRup+";  "+getSource(getSrcIndexForNthRup(nthRup)).getName());
				
// this can be turned off once it passes
EqksInGeoBlockUtils.testSubBlockListRates(this, blockList, subBlockList1, subBlockList2);

				
				// set the start time for the time dependent calcs
				timeSpan.setStartTimeInMillis(rupOT);	
				
				// set the date of last event and slip for this rupture
				setRuptureOccurrence(nthRup, rupOT);
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
				for(int s=0;s<numFaultSystemSources;s++) {
					double probGain = computeProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
					
					// IS THE FOLLOWING NEEDED (NOT DONE IN UPDATEFORECAST)?
					if(Double.isNaN(probGain))
						probGainForFaultSystemSource[s] = 1;
					else
						probGainForFaultSystemSource[s] = probGain;
				}


				// now update rates in blocks (for rups that change probs)
				System.out.println("Updating block rates");
				for(int s=0; s<numFaultSystemSources;s++) {
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
							double oldRateBl = blockList.get(b).tempGetRandomEqkRupSamplerY_Val(nthRupIndex);
							blockList.get(b).changeRate(newRate, nthRupIndex);
							double newRateBl = blockList.get(b).tempGetRandomEqkRupSamplerY_Val(nthRupIndex);

//							if(nthRup == nthRupIndex) {	// check change for the one that occurred
//								System.out.println("\told rate in block = "+oldRateBl+"\n\tnew rate in block = "+newRateBl);
//							}

							// update sub-block lists
							ArrayList<EqksInGeoBlock> subBlocks1 = subBlockList1.get(b);
							if(subBlocks1 != null) {
								for(EqksInGeoBlock blk: subBlocks1) {
//									double oldRateBl1 = blk.tempGetRandomEqkRupSamplerY_Val(nthRupIndex);
									
									blk.changeRate(newRate, nthRupIndex);
									
//									double newRateBl1 = blk.tempGetRandomEqkRupSamplerY_Val(nthRupIndex);
//									if(nthRup == nthRupIndex) {	// check change for the one that occurred
//										if(oldRateBl1>0 || newRateBl1>0)
//											System.out.println("\told rate in block1 = "+oldRateBl1+"\n\tnew rate in block1 = "+newRateBl1);
//									}
								}
							}
							ArrayList<EqksInGeoBlock> subBlocks2 = subBlockList2.get(b);
							if(subBlocks2 != null) {
								for(EqksInGeoBlock blk: subBlocks2) {
//									double oldRateBl2 = blk.tempGetRandomEqkRupSamplerY_Val(nthRupIndex);

									blk.changeRate(newRate, nthRupIndex);
									
//									double newRateBl2 = blk.tempGetRandomEqkRupSamplerY_Val(nthRupIndex);
//									if(nthRup == nthRupIndex) {	// check change for the one that occurred
//										if(oldRateBl2>0 || newRateBl2>0)
//											System.out.println("\told rate in block2 = "+oldRateBl2+"\n\tnew rate in block2 = "+newRateBl2);
//									}
								}
							}
						}
//if(nthRup == nthRupIndex) System.exit(-1);
					}
				}
				
// this can be turned off once it passes
EqksInGeoBlockUtils.testSubBlockListRates(this, blockList, subBlockList1, subBlockList2);

				// now update samplers
//				System.out.println("Updating etasSamplers");
				for(Integer key:etasSamplerMap.keySet()) {
					etasSamplerMap.get(key).upDataRandomBlockSampler();	// THIS COULD BE DONE MORE EFFICIENTLY INSIDE THIS METHOD BY SAVING SPATIAL PROBS
				}
//				System.out.println("Done with fault system rupture updates");
			}
		}
		
		progressBar.showProgress(false);

		System.out.println("Fault System Aftershocks:\n");
		for(Integer n : nthFaultSysRupAftershocks) {
			int s=srcIndexForNthRup[n];
			System.out.println("\t"+n+"\t"+s+"\t"+rupIndexForNthRup[n]+"\t"+fltSysRupIndexForNthRup[n]+"\t"+getSource(s).getName());

		}

		String fileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/hypoTest.pdf";
		//		ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, null, simulatedRupsQueue);
		ETAS_SimAnalysisTools.plotEpicenterMap("test", fileName, obsEqkRuptureList.get(0), simulatedRupsQueue);
		ETAS_SimAnalysisTools.plotMagFreqDists("test", null, simulatedRupsQueue);
		
		System.out.println("Total num ruptures: "+simulatedRupsQueue.size());

	}
	
	
	public ProbEqkSource getSource(int iSource) {
		if(iSource == lastSrcRequested)
			return currentSrc;
		else {
			ProbEqkSource src = super.getSource(iSource);
//System.out.println(src.getNumRuptures());
			if (iSource <numFaultSystemSources) {
				// get poisson source from parent
				double probGain = Double.NaN;
				// the following is needed because the parent updateForecast ends up calling this 
				// method, and probGainForFaultSystemSource isn't populated until the 
				// parent constructor is called (because we don't know it's size)
				if(probGainForFaultSystemSource != null) // skip if being called by parent
					probGain = probGainForFaultSystemSource[iSource];
				if(Double.isNaN(probGain))
					probGain = 1.0;
				((FaultRuptureSource)src).scaleRupRates(probGain);
//if(probGain != 1.0) System.out.println("non-unit prob gain ("+probGain+") for s="+iSource+" named "+src.getName());
// if(iSource == 4755) System.out.println("prob gain ="+probGain+" for s="+iSource+" named "+src.getName());
			}
			currentSrc = src;
			lastSrcRequested = iSource;		
			return src;
		}
	}
}
