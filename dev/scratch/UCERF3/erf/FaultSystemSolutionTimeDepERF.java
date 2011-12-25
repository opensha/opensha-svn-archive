package scratch.UCERF3.erf;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.math.random.RandomDataImpl;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;
import org.opensha.sha.earthquake.param.BPT_AperiodicityParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.UCERF3.FaultSystemSolution;
import scratch.ned.ETAS_Tests.IntegerPDF_FunctionSampler;

/**
 * This class adds elastic-rebound-based time dependence to the FaultSystemSolutionPoissonERF.
 * 
 * TODO:
 * 
 * 0) try tuning aper correction to match target
 * 
 * 1) try src.scaleRupProbs(probGain);
 */
public class FaultSystemSolutionTimeDepERF extends FaultSystemSolutionPoissonERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = false;

	public static final String NAME = "Fault System Solution Time Dep ERF";
	
	public final static double MILLISEC_PER_YEAR = 1000*60*60*24*365.25;
	
	// this is the probability gain for each fault system source
	double[] probGainForFaultSystemSource;
	boolean timeSpanChangeFlag=true;
	
	BPT_AperiodicityParam bpt_AperiodicityParam;
	double bpt_Aperiodicity;
	
	// these fields are for simulation mode (stochastic event sets).
	public boolean SIMULATION_MODE = true;
	ArrayList<Double> normalizedRecurIntervals;
	double totalRate;
	IntegerPDF_FunctionSampler ruptureSampler;
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
		
		bpt_Aperiodicity = bpt_AperiodicityParam.getValue();
		
		// first check whether file changed (this info will be erased by parent updataForecast())
		boolean fileChange = true;
		if(fileParam.getValue() == prevFile)
			fileChange=false;
		
		// this is needed here because super.updateForecast() will call the local getSource method 
		// (which requires having this)
		probGainForFaultSystemSource = null;

		// now update forecast using super
		super.updateForecast();
		
		// rest this to be safe
		lastSrcRequested=-1;
		
		if(SIMULATION_MODE) {
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
			if (D) System.out.println("totalRate long term = "+totalRate);
		}

		
		// now update the the prob gains if needed (must be done after the above)
//		System.out.println("timeSpanChangeFlag="+timeSpanChangeFlag);
//		System.out.println("fileChange="+fileChange);
		if(timeSpanChangeFlag || fileChange) {
			if (D) System.out.println("updating all prob gains");
			probGainForFaultSystemSource = new double[numFaultSystemSources];
			for(int s=0; s<numFaultSystemSources; s++)
				probGainForFaultSystemSource[s] = computeProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
			timeSpanChangeFlag = false;
		}
		
//		long runTime = System.currentTimeMillis();
		// set ruptureSampler if in simulation mode
		if(SIMULATION_MODE) {
			totalRate=0;
			ruptureSampler = new IntegerPDF_FunctionSampler(totNumRups);
			int nthRup=0;
			for(ProbEqkSource src:this) {
				for(ProbEqkRupture rup:src) {
					double rate = rup.getMeanAnnualRate(timeSpan.getDuration());
					totalRate += rate;
					ruptureSampler.add(nthRup, rate);
					nthRup+=1;
				}
			}
			// this will be different from the above printing of totalRate only if the fault sections of date and slip in last event info
			if (D) System.out.println("totalRate = "+totalRate+"\t"+ruptureSampler.getSumOfY_vals());
		}
		
		if (D) System.out.println("totNumRups="+totNumRups+"\t"+ruptureSampler.getNum());

//		int runTimeSec = (int)(System.currentTimeMillis()-runTime)/1000;
//		System.out.println("ruptureSampler took "+runTimeSec+" sec to make");

	}
	
	
	

	@Override
	public String getName() {
		return NAME;
	}


	
	/**
	 * This method sets the rupture time (it will set the date of last event in 
	 * each FaultSectionPrefData the rupture uses).  This also adds the normalized
	 * RI to normalizedRecurIntervals if in simulation mode.
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
			
			int fltSysIndex = fltSysRupIndexForNthRup[nthRup];
			List<FaultSectionPrefData> fltData = faultSysSolution.getFaultSectionDataForRupture(fltSysIndex);
			double[] slipArray = faultSysSolution.getSlipOnSectionsForRup(fltSysIndex);
			for(int i=0; i< fltData.size(); i++) {
				FaultSectionPrefData data = fltData.get(i);
				data.setDateOfLastEvent(eventTimeInMillis);
				data.setSlipInLastEvent(slipArray[i]);
			}
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

			double alphaCorr=1d;
			double riCorr=1d;
//			double alphaCorr=0.9;
//			double riCorr=1.2;
			double prob_bpt = BPT_DistCalc.getCondProb(aveExpRI*riCorr, bpt_Aperiodicity*alphaCorr, timeSinceLast, duration);
			double prob_pois = 1-Math.exp(-duration/aveExpRI);

			gain = (prob_bpt/prob_pois)*(usedArea/totArea) + 1.0*(totArea-usedArea)/totArea;

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

		long runtime = System.currentTimeMillis();
		
		// get fault system rups that are influenced
		ArrayList<Integer> fltSysRups = new ArrayList<Integer>();

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
		// now reduce the list to unique values
		ArrayList<Integer> uniqueFltSysRups = new ArrayList<Integer>(new HashSet<Integer>(fltSysRups));
		fltSysRups =uniqueFltSysRups;
		
		
		runtime -= System.currentTimeMillis();
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
	 * This assumes the rate of each rupture is constant up until the next event is sampled.
	 * 
	 * TODO:
	 * 
	 * use timeSpan better (e.g. don't take durationInYears); make sure timeSpan deals with UTC correctly
	 * 
	 * add progress bar
	 * 
	 * @param durationInYears
	 */
	public void testSimulations(int durationInYears) {
		SIMULATION_MODE=true;
		normalizedRecurIntervals = new ArrayList<Double>();
		int startYear = 1970;
		long startTimeMillis = (long)((startYear-1970)*MILLISEC_PER_YEAR);

		timeSpan.setDuration(1.0);
		GregorianCalendar startTimeCal = new GregorianCalendar();
		startTimeCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		startTimeCal.setTimeInMillis(startTimeMillis);
		System.out.println(startTimeCal.getTimeInMillis());
		timeSpan.setStartTime(startTimeCal);
		System.out.println(timeSpan.getStartTimeCalendar().getTimeInMillis()/(3600000));
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
		System.out.println(targetMFD);

		// MFD for simulation
		SummedMagFreqDist obsMFD = new SummedMagFreqDist(5.05,8.95,40);
		
		double counter=0;
		int percDone=0;
		System.out.println(percDone+"% done");
		double yr=startYear;
		long startRunTime = System.currentTimeMillis();
		while (yr<durationInYears+startYear) {
			// write progress
			if(counter > durationInYears/100) {
				counter =0;
				percDone += 1;
				double timeInMin = ((double)(System.currentTimeMillis()-startRunTime)/(1000.0*60.0));
				System.out.println("\n"+percDone+"% done in "+(float)timeInMin+" minutes\n");	
			}
			
			System.out.println(numRups+"\t"+yr);
			
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
			startTimeCal.setTimeInMillis(eventTimeMillis);
//			System.out.println("Next Start time: "+startTimeCal.getTimeInMillis());
			timeSpan.setStartTime(startTimeCal);

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
		obsMFD.scale(1.0/durationInYears);
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
	
	public ProbEqkSource getSource(int iSource) {
		if(iSource == lastSrcRequested)
			return currentSrc;
		else {
			ProbEqkSource src = super.getSource(iSource);
//			FaultRuptureSource src = (FaultRuptureSource)super.getSource(iSource);
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
			}
			currentSrc = src;
			lastSrcRequested = iSource;		
			return src;
		}
	}
	
//	public ProbEqkSource getSource(int iSource) {
//		if(iSource == lastSrcRequested)
//			return currentSrc;
//		else if (iSource <numFaultSystemSources) {
//			// get poisson source from parent
//			FaultRuptureSource src = (FaultRuptureSource)super.getSource(iSource);
//			double probGain = Double.NaN;
//			// the following is needed because the parent updateForecast ends up calling this 
//			// method, and probGainForFaultSystemSource isn't populated until the 
//			// parent constructor is called (because we don't know it's size)
//			if(probGainForFaultSystemSource != null) // skip if being called by parent
//				probGain = probGainForFaultSystemSource[iSource];
//			if(Double.isNaN(probGain))
//				probGain = 1.0;
//			src.scaleRupRates(probGain);
//			currentSrc = src;
//			lastSrcRequested = iSource;		
//			return src;
//		}
//		else	// this is where grid based sources can go
//			return null;
//	}


}
