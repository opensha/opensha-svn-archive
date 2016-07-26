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
		int num = 51;
		double delta = 0.1;
		List<RSQSimEvent> events = RSQSimFileReader.readEventsFile(eventDir, elements,
				Lists.newArrayList(new MagRangeRuptureIdentifier(minMag, 10d)));
		double duration = General_EQSIM_Tools.getSimulationDurationYears(events);
		
		for (Region reg : regions) {
			HashSet<Integer> elementsInRegion = getElementsInsideRegion(elements, reg);
			IncrementalMagFreqDist mfd = calcMFD(events, elementsInRegion, duration, minMag+0.5*delta, num, delta);
			
			double magMean = 0d;
			for (RSQSimEvent e : events)
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
			mfd.setName("RSQSim");
			funcs.add(grMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
			grMFD.setName("G-R b="+(float)bVal);
			
			String title = (int)(duration+0.5)+" yr MFD, "+reg.getName();
			String xAxisLabel = "Magnitude";
			String yAxisLabel = "Incremental Rate (1/yr)";
			PlotSpec spec = new PlotSpec(funcs, chars, title, xAxisLabel, yAxisLabel);
			spec.setLegendVisible(true);
			
			GraphWindow gw = new GraphWindow(spec);
			gw.setYLog(true);
			gw.setAxisRange(minMag, 8.2, 1e-7, 1e1);
			gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
			gw.saveAsPNG("/tmp/mfd_rsqsim_"+reg.getName().replaceAll(" ", "_")+".png");
		}
	}

}
