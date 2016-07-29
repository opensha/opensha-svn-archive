package scratch.kevin.simulators;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.simulators.SimulatorEvent;
import org.opensha.sha.simulators.iden.MagRangeRuptureIdentifier;
import org.opensha.sha.simulators.parsers.RSQSimFileReader;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

import com.google.common.collect.Lists;

import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.RSQSimEvent;
import org.opensha.sha.simulators.SimulatorElement;

public class MFDCalc {
	
	public static IncrementalMagFreqDist calcMFD(
			List<? extends SimulatorEvent> events, HashSet<Integer> elementsInRegion,
			double duration, double minMag, int num, double delta) {
		
		IncrementalMagFreqDist mfd = new IncrementalMagFreqDist(minMag, num, delta);
		double myMin = minMag-0.5*delta;
		double myMax = mfd.getMaxX()+0.5*delta;
		for (SimulatorEvent e : events) {
			double mag = e.getMagnitude();
			if (mag < myMin || mag > myMax)
				continue;
			int ind = mfd.getClosestXIndex(mag);
			double eventRate;
			if (elementsInRegion == null)
				eventRate = 1;
			else
				eventRate = getFractInsideRegion(e, elementsInRegion);
			mfd.set(ind, mfd.getY(ind)+eventRate);
		}
		if (duration > 0)
			for (int i=0; i<mfd.size(); i++)
				mfd.set(i, mfd.getY(i) / duration);
		
		return mfd;
	}
	
	public static double getFractInsideRegion(SimulatorEvent e, HashSet<Integer> elementsInRegion) {
		double tot = e.getNumElements();
		double inside = 0d;
		for (int elemID : e.getAllElementIDs())
			if (elementsInRegion.contains(elemID))
				inside++;
		return inside / tot;
	}
	
	public static HashSet<Integer> getElementsInsideRegion(
			List<SimulatorElement> elements, Region region) {
		HashSet<Integer> elementsInRegion = new HashSet<Integer>();
		for (SimulatorElement elem : elements) {
			double lat = 0; 
			double lon = 0;
			int num = 0;
			// just averaging to get middle, should be fine for this use
			for (Location loc : elem.getVertices()) {
				lat += loc.getLatitude();
				lon += loc.getLongitude();
				num++;
			}
			lat /= (double)num;
			lon /= (double)num;
			if (region.contains(new Location(lat, lon)))
				elementsInRegion.add(elem.getID());
		}
		return elementsInRegion;
	}
	
	public static double calcMinBelow(double eventMin, double delta) {
		double min;
		for (min=0; min<=eventMin; min+=delta);
		return min - delta;
	}
	
	public static int calcNum(double min, double eventMax, double delta) {
		int num = 0;
		for (double max=min; max<=eventMax; max+=delta)
			num++;
		return num;
	}
	
	public static void writeMFDPlots(List<SimulatorElement> elements, List<? extends SimulatorEvent> events, File outputDir,
			Region... regions) throws IOException {
		if (regions.length == 0)
			regions = new Region[] { null };
		
		double duration = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
		double minMag = 10d;
		for (SimulatorEvent e : events)
			minMag = Math.min(minMag, e.getMagnitude());
		// round minMag
		minMag = (int)(minMag*100d + 0.5)/100d;
		double delta = 0.1;
		int num = (int)((8.5d - minMag)/delta + 0.5);
		
		for (Region reg : regions) {
			HashSet<Integer> elementsInRegion;
			if (reg == null)
				elementsInRegion = null;
			else
				elementsInRegion = getElementsInsideRegion(elements, reg);
			IncrementalMagFreqDist mfd = calcMFD(events, elementsInRegion, duration, minMag+0.5*delta, num, delta);
			
			double magMean = 0d;
			for (SimulatorEvent e : events)
				magMean += e.getMagnitude();
			magMean /= events.size();
			double magComplete = minMag;
			double magPrecision = 0d;
			
			// estimate b
			double bVal = Math.log10(Math.E) /(magMean - (magComplete-0.5*magPrecision));
			System.out.println("Estimated b-value: "+bVal);
			GutenbergRichterMagFreqDist grMFD = new GutenbergRichterMagFreqDist(bVal, 1d, mfd.getMinX(), mfd.getMaxX(), mfd.size());
			double totCmlRate = mfd.getCumRateDistWithOffset().getY(0);
			grMFD.scaleToCumRate(0, totCmlRate);
			
			List<DiscretizedFunc> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			funcs.add(mfd);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			mfd.setName("Catalog");
			funcs.add(grMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
			grMFD.setName("G-R b="+(float)bVal);
			
			String title = (int)(duration+0.5)+" yr MFD";
			if (reg != null)
				title += ", "+reg.getName();
			String xAxisLabel = "Magnitude";
			String yAxisLabel = "Incremental Rate (1/yr)";
			PlotSpec spec = new PlotSpec(funcs, chars, title, xAxisLabel, yAxisLabel);
			spec.setLegendVisible(true);
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			gp.setBackgroundColor(Color.WHITE);
			
			gp.setUserBounds(minMag, mfd.getMaxX()+0.5*delta, 1e-7, 1e1);
			gp.drawGraphPanel(spec, false, true);
			gp.getChartPanel().setSize(1000, 800);
			
			String prefix = "mfd";
			if (reg == null)
				prefix += "_all";
			else
				prefix += "_"+reg.getName().replaceAll(" ", "_");
			File file = new File(outputDir, prefix);
			
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
			gp.saveAsTXT(file.getAbsolutePath()+".txt");
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators/UCERF3_125kyrs");
		File geomFile = new File(dir, "UCERF3.D3.1.1km.tri.2.flt");
		List<SimulatorElement> elements = RSQSimFileReader.readGeometryFile(geomFile, 11, 'S');
		Region[] regions =  { new CaliforniaRegions.RELM_SOCAL(), new CaliforniaRegions.RELM_TESTING() };
		File eventDir = new File("/home/kevin/Simulators/UCERF3_interns/combine340");
		double minMag = 5d;
		List<RSQSimEvent> events = RSQSimFileReader.readEventsFile(eventDir, elements,
				Lists.newArrayList(new MagRangeRuptureIdentifier(minMag, 10d)));
		
		writeMFDPlots(elements, events, new File("/tmp"), regions);
	}

}
