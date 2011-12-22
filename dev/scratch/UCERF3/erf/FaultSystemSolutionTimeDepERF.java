package scratch.UCERF3.erf;

import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.math.random.RandomDataImpl;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.calc.recurInterval.BPT_DistCalc;
import org.opensha.sha.earthquake.param.FaultGridSpacingParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.ned.ETAS_Tests.IntegerPDF_FunctionSampler;

/**
 *
 */
public class FaultSystemSolutionTimeDepERF extends FaultSystemSolutionPoissonERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = false;

	public static final String NAME = "Fault System Solution Time Dep ERF";
	
	public final static double MILLISEC_PER_YEAR = 1000*60*60*24*365.25;
	
	public boolean SIMULATION_MODE = true;
	ArrayList<Double> normalizedRecurIntervals;
	
	boolean timeSpanChangeFlag=true;
	
	double totalRate;
	
	IntegerPDF_FunctionSampler ruptureSampler;
	
	
	/**
	 * This creates the ERF from the given file
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionTimeDepERF(FaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
		initiateTimeSpan();
	}

	
	/**
	 * This creates the ERF from the given file
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionTimeDepERF(String fullPathInputFile) {
		super(fullPathInputFile);
		initiateTimeSpan();
	}

	
	/**
	 * This creates the ERF with a parameter for choosing the input file
	 */
	public FaultSystemSolutionTimeDepERF() {
		super();
		initiateTimeSpan();
	}
	
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
		
		// first check whether file changed (which will be erased by parent updataForecast())
		boolean fileChange = true;
		if(fileParam.getValue() == prevFile)
			fileChange=false;
			
		// now update forecast using super
		super.updateForecast();
		
		// now update the the prob gains if needed
//		System.out.println("timeSpanChangeFlag="+timeSpanChangeFlag);
//		System.out.println("fileChange="+fileChange);
		if(timeSpanChangeFlag || fileChange) {
			// set probability gains (must be done after above)
			if(D) System.out.println("updating all prob gains");
			probGainForFaultSystemSource = new double[numFaultSystemSources];
			for(int s=0; s<numFaultSystemSources; s++)
				probGainForFaultSystemSource[s] = computeProbGainForFaultSysRup(fltSysRupIndexForSource[s]);
			timeSpanChangeFlag = false;
		}
		
//		long runTime = System.currentTimeMillis();
		if(SIMULATION_MODE) {
			totalRate=0;
			ruptureSampler = new IntegerPDF_FunctionSampler(totNumRups);
			int nthRup=0;
			for(ProbEqkSource src:this) {
				for(ProbEqkRupture rup:src) {
					totalRate += rup.getMeanAnnualRate(timeSpan.getDuration());
					ruptureSampler.add(nthRup, rup.getProbability());
					nthRup+=1;
				}
			}
		}
//		int runTimeSec = (int)(System.currentTimeMillis()-runTime)/1000;
//		System.out.println("ruptureSampler took "+runTimeSec+" sec to make");

	}
	
	
	

	@Override
	public String getName() {
		return NAME;
	}


	
	/**
	 * This method sets the rupture time (it will set the date of last event in 
	 * each FaultSectionPrefData the rupture uses).
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
			// update all the probability gains of the influenced sources
//			ArrayList<Integer> rupsThatChanged = getRupsThatChangeProbAfterRup(nthRup);
//			ArrayList<Integer> sourcesUpdated = new ArrayList<Integer>();
//			for(Integer n:rupsThatChanged) {
//				int srcIndex = srcIndexForNthRup[n];
//				if(!sourcesUpdated.contains(new Integer(srcIndex))) {
//					probGainForFaultSystemSource[srcIndex] = computeProbGainForFaultSysRup(fltSysRupIndexForSource[srcIndex]);
//					sourcesUpdated.add(new Integer(srcIndex));
//				}
//			}
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
		double alpha = 0.2;
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

			double prob_bpt = BPT_DistCalc.getCondProb(aveExpRI, alpha, timeSinceLast, duration);
			double prob_pois = 1-Math.exp(-duration/aveExpRI);

			gain = (prob_bpt/prob_pois)*(usedArea/totArea) + 1.0*(totArea-usedArea)/totArea;
			
//			if(srcIndexForFltSysRup[faultSysRupIndex] == srcIndexForNthRup[2503]) {
			if(D) {
				System.out.println("\ncomputProbGainForFaultSysRup("+faultSysRupIndex+")\n");
				System.out.println("\t"+"aveExpRI="+aveExpRI);
				System.out.println("\t"+"timeSinceLast="+timeSinceLast);
				System.out.println("\t"+"duration="+duration);
				System.out.println("\t"+"prob_bpt="+prob_bpt);
				System.out.println("\t"+"prob_pois="+prob_pois);
				System.out.println("\t"+"gain="+gain);
				System.out.println("\taveDateOfLast="+aveDateOfLast+"; startTime="+startTime+"; MILLISEC_PER_YEAR="+MILLISEC_PER_YEAR);
				System.out.println("\t"+"usedArea="+usedArea);
				System.out.println("\t"+"totArea="+totArea);
			}
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
		if(normRI<0.1) {
			ProbEqkRupture rup = getNthRupture(nthRup);
			System.out.println("Wierd RI for rup "+nthRup);
			System.out.println("\ttimeSinceLast="+timeSinceLast+
					"\teventTimeInMillis="+eventTimeInMillis+
					"\taveDateOfLast="+aveDateOfLast+
					"\taveExpRI="+aveExpRI+
					"\tnormRI="+normRI+
					"\ttotArea="+totArea+
					"\trupProb="+rup.getProbability()+
					"\trupMag="+rup.getMag()+
					"\tprobGain="+probGainForFaultSystemSource[srcIndexForNthRup[nthRup]]);
			if(nthRup==2503) {
				int index=0;
				System.out.println("\tSrcName="+getSource(srcIndexForNthRup[nthRup]).getName());
				for(FaultSectionPrefData data: fltData) {
					System.out.println("\t"+index+"\tdateOfLast="+ data.getDateOfLastEvent()+
							"\tslipInLast="+data.getSlipInLastEvent()+
							"\tslipRateReduced="+data.getReducedAveSlipRate()+
							"\tareaReduced="+ data.getTraceLength()*data.getReducedDownDipWidth());
					index += 1;
				}
			}
		}
		return normRI;
	}
	
	
	/**
	 * 
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
		
		ArrayList<Integer> fltSysRups = new ArrayList<Integer>();
//		if(D) System.out.println("starting faultSysSolution.getSectionsIndicesForRup(fltSysRupId)");
		List<Integer> sectionIndices = faultSysSolution.getSectionsIndicesForRup(fltSysRupId);
		for(Integer sectIndex: sectionIndices) {
			fltSysRups.addAll(faultSysSolution.getRupturesForSection(sectIndex));  // duplicates are filtered below
		}
//		if(D) System.out.println("Done");

		
		// need to convert these to nthRup indices
		ArrayList<Integer> faultSysRupsProcessed = new ArrayList<Integer>();
		for(Integer fltSysRupIndex :fltSysRups) {
			if(!faultSysRupsProcessed.contains(fltSysRupIndex)) {	// skip if already processed
				int srcIndex = srcIndexForFltSysRup[fltSysRupIndex];
				if(srcIndex != -1) {
					int[] nthRups = nthRupIndicesForSource.get(srcIndex);
					for(int r=0; r<nthRups.length;r++)
						nthRupsThatChanged.add(nthRups[r]);
				}
				faultSysRupsProcessed.add(fltSysRupIndex);
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
	 * @param durationInYears
	 */
	public void testSimulations(int durationInYears) {
		normalizedRecurIntervals = new ArrayList<Double>();
		int startYear = 1970;
		long startTimeMillis = (long)((startYear-1970)*MILLISEC_PER_YEAR);

		timeSpan.setDuration(1.0);
		GregorianCalendar startTimeCal = new GregorianCalendar();
		startTimeCal.setTimeZone(TimeZone.getTimeZone("UTC"));
		startTimeCal.setTimeInMillis(startTimeMillis);
		System.out.println(startTimeCal.getTimeInMillis());
		timeSpan.setStartTime(startTimeCal);
		long diff = startTimeMillis-timeSpan.getStartTimeCalendar().getTimeInMillis();
		System.out.println(timeSpan.getStartTimeCalendar().getTimeInMillis()/(3600000));
		int numRups=0;
		
		RandomDataImpl randomDataSampler = new RandomDataImpl();
		
		System.out.println("Updating forecast");
		updateForecast();
		System.out.println("Making target MFD");
		SummedMagFreqDist targetMFD = ERF_Calculator.getTotalMFD_ForERF(this, 5.05, 8.95, 40, true);
		targetMFD.setName("Target MFD");
		targetMFD.setInfo("");

	
		SummedMagFreqDist obsMFD = new SummedMagFreqDist(5.05,8.95,40);
		
		double counter=0;
		int percDone=0;
		System.out.println(percDone+"% done");
		double yr=startYear;
		long startRunTime = System.currentTimeMillis();
		long runTimeForUpdateForecast = 0;
		while (yr<durationInYears+startYear) {
			if(counter > durationInYears/20) {
				counter =0;
				percDone += 5;
				double timeInMin = ((double)(System.currentTimeMillis()-startRunTime)/(1000.0*60.0));
				System.out.println("\n"+percDone+"% done in "+(float)timeInMin+" minutes\n");	
			}
			startTimeMillis = timeSpan.getStartTimeCalendar().getTimeInMillis();
//			System.out.println("Start time: "+startTimeMillis+"\t"+yr+"\t"+(1970+(double)startTimeMillis/MILLISEC_PER_YEAR));
			
			long time = System.currentTimeMillis();
			updateForecast();
			runTimeForUpdateForecast += System.currentTimeMillis()-time;
			double timeOfNextInYrs = randomDataSampler.nextExponential(1.0/totalRate);
			long eventTimeMillis = startTimeMillis + (long)(timeOfNextInYrs*MILLISEC_PER_YEAR);
//			System.out.println("Event time: "+eventTimeMillis);
			int nthRup = ruptureSampler.getRandomInt();
			setRuptureOccurrence(nthRup, eventTimeMillis);
//			System.out.print((float)timeOfNextInYrs+" ("+(float)totalRate+"); ");	
//			System.out.print(numRups+"\t"+nthRup+"\t"+(float)timeOfNextInYrs+" ("+(float)totalRate+"); \n");	

			numRups+=1;
			obsMFD.addResampledMagRate(getNthRupture(nthRup).getMag(), 1.0, true);
			yr+=timeOfNextInYrs;
			counter +=timeOfNextInYrs;
			startTimeCal.setTimeInMillis(eventTimeMillis);
//			System.out.println("Next Start time: "+startTimeCal.getTimeInMillis());
			timeSpan.setStartTime(startTimeCal);

			
//
//			
////			System.out.println("\tDone with updateForecast()");
//			for(int s=0; s<getNumSources();s++) {
//				ProbEqkSource src = getSource(s);
//				ArrayList<Integer> rupIndices = src.drawRandomEqkRuptureIndices();
//				if(rupIndices.size()>1)
//					System.out.println("\t"+rupIndices.size()+" in year "+yr+"; only using one!");
//				numRups+=rupIndices.size();
//				if(rupIndices.size()>0) {
//					int rupIndex = rupIndices.get(0); // only keep the first
//					int nthRup = nthRupForSrcAndRupIndices.get(s+","+rupIndex);
//					setRuptureOccurrence(nthRup, timeSpan.getStartTimeCalendar().getTimeInMillis());
//					obsMFD.addResampledMagRate(src.getRupture(rupIndex).getMag(), 1.0, true);
//				}
//			}			
		}
		System.out.println("numRups="+numRups);
		
		double percentUpdate = 100.0* (double)runTimeForUpdateForecast / (double)(System.currentTimeMillis()-startRunTime);
		
		System.out.println("Percent time updating forecast: "+percentUpdate);

		System.out.println("normalizedRecurIntervals.size()="+normalizedRecurIntervals.size());
//		for(Double nRI:normalizedRecurIntervals)
//			System.out.println(nRI);
		
		//THIS NEEDED ANYMORE?
		// filter out any negative numbers
//		for(int i=0;i<normalizedRecurIntervals.size();i++) {
//			if(normalizedRecurIntervals.get(i) < 0) {
//				System.out.println("Changing "+normalizedRecurIntervals.get(i)+" to 0.0");
//				normalizedRecurIntervals.set(i, 0.0);
//			}
//		}
		
		GraphiWindowAPI_Impl plot = General_EQSIM_Tools.plotNormRI_Distribution(normalizedRecurIntervals, 
				"Normalized RIs");
		
//		System.out.println(obsMFD);

		// plot MFDs
		obsMFD.scale(1.0/durationInYears);
		obsMFD.setName("Simulated MFD");
		obsMFD.setInfo("");

		ArrayList funcs = new ArrayList();
		funcs.add(targetMFD);
		funcs.add(obsMFD);
		funcs.add(targetMFD.getCumRateDistWithOffset());
		funcs.add(obsMFD.getCumRateDistWithOffset());
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Incremental Mag-Freq Dists"); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
//		graph.setYLog(true);
		graph.setY_AxisRange(1e-6, 1.0);

	}
}
