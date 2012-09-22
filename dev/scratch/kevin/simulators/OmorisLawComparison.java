package scratch.kevin.simulators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class OmorisLawComparison {
	
	private static void doComparison(List<EQSIM_Event> events, RuptureIdentifier rupIden,
			int maxDays, double magBin, double binWidth) {
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		System.out.println("Loading geometry...");
		General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
		System.out.println("Loading events...");
		tools.read_EQSIMv04_EventsFile(eventFile);
		ArrayList<EQSIM_Event> events = tools.getEventsList();
		System.out.println("Calculating...");
		double minWindowDays = 1d/24d;
		int maxDays = 365;
		double binWidth = 0.1;
		
//		List<Double> magBins = Lists.newArrayList(7d, 7.6d);
		List<Double> magBins = Lists.newArrayList(7.6d);
		
		RuptureIdentifier rupIden = new ElementMagRangeDescription(ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, 7d, 10d);
		
		Region reg = new CaliforniaRegions.RELM_SOCAL();
		HashSet<Integer> elementsInRegion = MFDCalc.getElementsInsideRegion(tools.getElementsList(), reg);
		
		double totalEventDuration = General_EQSIM_Tools.getSimulationDurationYears(events);
		
		double daysPerYear = BatchPlotGen.DAYS_PER_YEAR;
		
		double minWindowYears = minWindowDays / daysPerYear;
		
		boolean[] randomizes = { false, true };
		
		for (boolean randomize : randomizes) {
			double magBin = 7.6;
			IncrementalMagFreqDist indepMFD = MFDCalc.calcMFD(events, elementsInRegion,
					totalEventDuration*daysPerYear, magBin, 1, binWidth);
			
			ArbitrarilyDiscretizedFunc eventFunc = new ArbitrarilyDiscretizedFunc();
			ArbitrarilyDiscretizedFunc omoriComp = new ArbitrarilyDiscretizedFunc();
			
			Preconditions.checkState(indepMFD.getNum() == 1);
			Preconditions.checkState(indepMFD.getX(0) == magBin);
			double indepVal = indepMFD.getY(0);
			
			double omoriK = -1;
			
			CSVFile<String> csv = new CSVFile<String>(true);
			double csvMinMag = 6;
			double csvMaxMag = 8;
			double csvDelta = 0.1;
			int csvMagNum = (int)((csvMaxMag-csvMinMag)/csvDelta) + 1;
			
			IncrementalMagFreqDist mfd = new IncrementalMagFreqDist(csvMinMag, csvMaxMag, csvMagNum);
			
			double myCSVMin = csvMinMag-0.5*csvDelta;
			double myCSVMax = mfd.getMaxX()+0.5*csvDelta;
			
			List<String> header = Lists.newArrayList("");
			for (int i=0; i<mfd.getNum(); i++)
				header.add(""+(float)mfd.getX(i));
			csv.addLine(header);
			
			double secondsPerDay = 60*60*24;
			
			double windowLenYrs = (double)maxDays / daysPerYear;
			EventsInWindowsMatcher match =
					new EventsInWindowsMatcher(events, rupIden, minWindowYears, windowLenYrs, randomize);
			
			List<EQSIM_Event> matches = match.getEventsInWindows();
			List<Double> matchTimes = match.getEventTimesFromWindowStarts();
			
			for (int days=1; days<=maxDays; days++) {
//				if (days > 100) {
//					if (days % 5 != 0)
//						continue;
//				} else if (days > 50) {
//					if (days % 3 != 0)
//						continue;
//				} else if (days > 10) {
//					if (days % 2 != 0)
//						continue;
//				}double windowLenYrs = (double)days / daysPerYear;
				
				double maxDurationSecs = days*secondsPerDay;
				double minDurationSecs = maxDurationSecs-secondsPerDay;
				
				for (int i=0; i<mfd.getNum(); i++)
					mfd.set(i, 0d);
				
				List<EQSIM_Event> eventsInWindows = Lists.newArrayList();
				
				for (int i=0; i<matches.size(); i++) {
					double timeFromStart = matchTimes.get(i);
					if (timeFromStart >= maxDurationSecs)
						continue;
					EQSIM_Event e = matches.get(i);
					eventsInWindows.add(e);
					if (timeFromStart < minDurationSecs)
						continue;
					double mag = e.getMagnitude();
					if (mag < myCSVMin || mag > myCSVMax)
						continue;
					int ind = mfd.getClosestXIndex(mag);
					mfd.set(ind, mfd.getY(ind)+1);
				}
				
				List<String> line = Lists.newArrayList(days+"");
				for (int i=0; i<mfd.getNum(); i++)
					line.add((int)mfd.getY(i)+"");
				csv.addLine(line);
				
				IncrementalMagFreqDist depMFD = MFDCalc.calcMFD(eventsInWindows, elementsInRegion,
						0, magBin, 1, binWidth);
				Preconditions.checkState(depMFD.getNum() == 1);
				Preconditions.checkState(depMFD.getX(0) == magBin);
				
				double t = days;
				double t0 = minWindowDays;
				
				double myEvents = depMFD.getY(0);
//				double myProbGain = depMFD.getY(0)/indepVal;
//				System.out.println("Days:\t"+days+"\tgain:"+myProbGain);
				System.out.println("Days:\t"+days+"\tcml events:"+myEvents);
				eventFunc.set((double)days, myEvents);
				if (omoriK < 0)
					omoriK = myEvents;
//				omoriComp.set((double)days, omoriK/(double)days);
				omoriComp.set((double)days, omoriK + Math.log(t) / t0);
			}
			
			if (randomize)
				csv.writeToFile(new File("/tmp/omori_mfds_randomized.csv"));
			else
				csv.writeToFile(new File("/tmp/omori_mfds.csv"));
			
			ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
			funcs.add(eventFunc);
			funcs.add(omoriComp);
			GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, "Omori's Law Comparison for M="+magBin);
		}
	}

}
