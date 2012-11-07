package scratch.kevin.simulators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.EventRecord;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

public class MomentRateVariation {

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
		
		int years = (int)General_EQSIM_Tools.getSimulationDurationYears(events);
		
		double[] yearlyMoRates = new double[years];
		
		// seconds
		double startTime = events.get(0).getTime();
		
		for (EQSIM_Event e : events) {
			double secsFromStart = e.getTime()-startTime;
			int year = (int)(secsFromStart / General_EQSIM_Tools.SECONDS_PER_YEAR);
			if (year == years)
				break;
			double moment = 0;
			for (EventRecord r : e)
				moment += r.getMoment();
			yearlyMoRates[year] = yearlyMoRates[year] + moment;
		}
		
		double meanMoRate = StatUtils.mean(yearlyMoRates);
		double maxMoRate = StatUtils.max(yearlyMoRates);
		double minMoRate = StatUtils.min(yearlyMoRates);
		
		System.out.println("Long term: mean="+meanMoRate+"\tmax="+maxMoRate+"\tmin="+minMoRate);
		
		int windowLen = 150;
		double[] windows = new double[years-windowLen];
		
		for (int i=0; (i+windowLen)<yearlyMoRates.length; i++) {
			double tot = 0;
			for (int j=i; j<i+windowLen; j++)
				tot += yearlyMoRates[j];
			double avg = tot / (double)windowLen;
			windows[i] = avg;
		}
		
		System.out.println("Windows: mean="+StatUtils.mean(windows)+"\tmax="+StatUtils.max(windows)
				+"\tmin="+StatUtils.min(windows));
		
		System.out.println("Max window / mean = "+(StatUtils.max(windows) / meanMoRate));
		System.out.println("mean / min window = "+(meanMoRate / StatUtils.min(windows)));
	}

}
