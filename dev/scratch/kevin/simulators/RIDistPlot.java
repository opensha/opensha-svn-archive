package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.DataUtils;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.kevin.simulators.PeriodicityPlotter.LogNormalDistReturnPeriodProvider;
import scratch.kevin.simulators.PeriodicityPlotter.RandomDistType;
import scratch.kevin.simulators.PeriodicityPlotter.RandomReturnPeriodProvider;

import com.google.common.collect.Lists;

public class RIDistPlot {

	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		System.out.println("Loading geometry...");
		General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
		System.out.println("Loading events...");
		tools.read_EQSIMv04_EventsFile(eventFile);
		List<EQSIM_Event> events = tools.getEventsList();
		
		File writeDir = new File(dir, "period_plots");
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<String> rupIdenNames = Lists.newArrayList();
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Cholame 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Carrizo 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("Garlock 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Mojave 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("SAF Coachella 7+");
		
		rupIdens.add(new ElementMagRangeDescription(
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID, 7d, 10d));
		rupIdenNames.add("San Jacinto 7+");
		
		for (int i=0; i<rupIdens.size(); i++) {
			RuptureIdentifier rupIden = rupIdens.get(i);
			String name = rupIdenNames.get(i);
			
			File plotFile = new File(writeDir, "ri_dists_"+PeriodicityPlotter.getFileSafeString(name));
			
			System.out.println("Plotting: "+name);
			
			List<Color> colors = GraphWindow.generateDefaultColors();
			
			List<HistogramFunction> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			
			List<EQSIM_Event> matches = rupIden.getMatches(events);
			funcs.add(getHist(matches, null));
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.remove(0)));
			
			for (RandomDistType type : RandomDistType.values()) {
				matches = rupIden.getMatches(
						PeriodicityPlotter.getRandomResampledCatalog(events, Lists.newArrayList(rupIden), type, false));
				funcs.add(getHist(matches, type));
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, colors.remove(0)));
			}
			
			GraphWindow gw = new GraphWindow(funcs, name+" RI Dists", chars);
			gw.saveAsPDF(plotFile.getAbsolutePath()+".pdf");
			gw.saveAsPNG(plotFile.getAbsolutePath()+".png");
			gw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		CSVFile<String> distParamsCSV = new CSVFile<String>(true);
		distParamsCSV.addLine("Name", "True Mean", "Median", "Variance", "Std Dev", "COV",
				"Trimmed Mean", "Median", "Variance", "Std Dev", "COV", "Scale: ln(mean)", "Shape");
		for (int i=0; i<rupIdens.size(); i++) {
			RuptureIdentifier rupIden = rupIdens.get(i);
			String name = rupIdenNames.get(i);
			
			List<String> line = Lists.newArrayList();
			
			List<EQSIM_Event> matches = rupIden.getMatches(events);
			
			double[] rps = PeriodicityPlotter.getRPs(matches);
			double mean = StatUtils.mean(rps);
			double var = StatUtils.variance(rps, mean);
			double sd = Math.sqrt(var);
			double cov = sd/mean;
			line.add(name);
			line.add(mean+"");
			line.add(DataUtils.median(rps)+"");
			line.add(var+"");
			line.add(sd+"");
			line.add(cov+"");
			double[] trimmedRPs = LogNormalDistReturnPeriodProvider.getTrimmedRPs(rps, mean);
			mean = StatUtils.mean(trimmedRPs);
			var = StatUtils.variance(trimmedRPs, mean);
			sd = Math.sqrt(var);
			cov = sd/mean;
			line.add(mean+"");
			line.add(DataUtils.median(rps)+"");
			line.add(var+"");
			line.add(sd+"");
			line.add(cov+"");
			line.add(Math.log(mean)+"");
			line.add(cov+"");
			
			distParamsCSV.addLine(line);
		}
		
		distParamsCSV.writeToFile(new File(writeDir, "dist_params.csv"));
		
		events = null;
		System.gc();
	}
	
	private static HistogramFunction getHist(List<EQSIM_Event> matches, RandomDistType type) {
		double[] rps = PeriodicityPlotter.getRPs(matches);
		HistogramFunction func = getHistFunc(rps);
		if (type == null)
			func.setName("Actual Distribution");
		else
			func.setName("RANDOMIZED "+type.getName());
		return func;
	}
	
	private static HistogramFunction getHistFunc(double[] rps) {
		HistogramFunction func = new HistogramFunction(5d, 100, 10d);
		double max = func.getMaxX()+5d;
		for (double rp : rps)
			if (rp < max)
				func.add(rp, 1d);
		return func;
	}

}
