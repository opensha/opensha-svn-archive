package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.EventRecord;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.collect.Lists;

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
		double minTime = events.get(0).getTimeInYears();
		
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
		
		int windowLen = 100;
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
		
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(minTime+((double)windowLen*0.5), windows.length, 1d);
		for (int i=0; i<windows.length; i++)
			func.set(i, windows[i]);
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(func);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
		
		String title = windowLen+"yr Moving Average of Seismic Moment Release";
		
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, title, chars);
		gw.setX_AxisLabel("Years");
		gw.setY_AxisLabel("Moment Rate (N-m/yr)");
		gw.setYLog(true);
		gw.setPlotLabelFontSize(24);
		gw.setAxisLabelFontSize(18);
		gw.setTickLabelFontSize(16);
	}

}
