package scratch.peter.nshmp;

import static java.math.BigDecimal.ROUND_HALF_UP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.DataUtils;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfFromSimpleFaultData;
import org.opensha.sha.faultSurface.RuptureSurface;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import scratch.peter.ucerf3.NSHMP13_DeterminisiticERF;

/**
 * This class was created to export the NSHMP13_DeterminisiticERF rupture set.
 * Requested by Eric Thompson and David Wald for scenario shake map generation.
 */
class DetermEventSetExport {

	private static final String PATH = "tmp/forEric/UCERF3_EventSet.json";
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.create();
	private static final int MAG_PRECISION = 2;
	private static final int LOC_PRECISION = 5;
	private static final int DEGREE_PRECISION = 1;
	private static final int WIDTH_PRECISION = 2;
	private static final int AREA_PRECISION = 2;

	public static void main(String[] args) {
		exportEventSet();
	}

	static void exportEventSet() {
		NSHMP13_DeterminisiticERF erf = NSHMP13_DeterminisiticERF.create(false);
		erf.updateForecast();

		EventSet eventSet = new EventSet();
		
		// Single
//		ProbEqkSource source = erf.iterator().next();
//		Rupture rup = toRupture(source);
//		eventSet.events.add(rup);
		
		// All
		for (ProbEqkSource source : erf) {
			Rupture rup = toRupture(source);
			eventSet.events.add(rup);
		}
		
		String json = GSON.toJson(eventSet);
		
		try {
			FileWriter writer = new FileWriter(PATH);
			writer.write(json);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Rupture toRupture(ProbEqkSource source) {

		ProbEqkRupture rupture = source.getRupture(0);
		CompoundSurface surface = (CompoundSurface) rupture.getRuptureSurface();

		List<? extends RuptureSurface> surfaces = surface.getSurfaceList();
		List<Section> sections = new ArrayList<Section>();
		for (int i = 0; i < surfaces.size(); i++) {
			RuptureSurface subsurface = surfaces.get(i);
			double area = round(subsurface.getArea(), AREA_PRECISION);
			double dip = round(subsurface.getAveDip(), DEGREE_PRECISION);
			double dipDir = round(subsurface.getAveDipDirection(), DEGREE_PRECISION);
			double width = round(subsurface.getAveWidth(), WIDTH_PRECISION);
			boolean reversed = surface.isSubSurfaceReversed(i);
			
			EvenlyGriddedSurfFromSimpleFaultData egsfsfd =
					(EvenlyGriddedSurfFromSimpleFaultData) subsurface;
			List<double[]> trace = traceToList(egsfsfd.getFaultTrace());
			List<double[]> resampledTrace = traceToList(subsurface.getEvenlyDiscritizedUpperEdge());

			Section section = new Section(area, dip, dipDir, width, reversed, trace, resampledTrace);
			sections.add(section);
		}

		String name = source.getName();
		double magnitude = round(rupture.getMag(), MAG_PRECISION);
		double area = round(surface.getArea(), AREA_PRECISION);
		double dip = round(surface.getAveDip(), DEGREE_PRECISION);
		double rake = round(rupture.getAveRake(), DEGREE_PRECISION);
		double width = round(surface.getAveWidth(), WIDTH_PRECISION);
		List<double[]> trace = traceToList(surface.getEvenlyDiscritizedUpperEdge());

		return new Rupture(name, magnitude, area, dip, rake, width, trace, sections);
	}

	private static List<double[]> traceToList(LocationList trace) {
		List<double[]> locs = new ArrayList<double[]>();
		for (Location loc : trace) {
			locs.add(new double[] {
				round(loc.getLongitude(), LOC_PRECISION),
				round(loc.getLatitude(), LOC_PRECISION),
				round(loc.getDepth(), LOC_PRECISION) });
		}
		return locs;
	}
	
	private static double round(double value, int scale) {
		return BigDecimal.valueOf(value).setScale(scale, ROUND_HALF_UP).doubleValue();
	}


	@SuppressWarnings("unused")
	private static class EventSet {
		final String name = "2014 NSHMP Determinisitc Event Set";
		final String info = "Derived from NSHMP13_DeterminisiticERF, created in OpenSHA " +
			"to generate building code deterministic caps for 2014 NSHMP";
		final List<Rupture> events;

		EventSet() {
			events = new ArrayList<Rupture>();
		}
	}

	@SuppressWarnings("unused")
	private static class Rupture {

		final String name;
		final double magnitude;
		final double area;
		final double dip;
		final double rake;
		final double width;
		final List<double[]> trace;
		final transient List<Section> sections;

		Rupture(String name,
				double magnitude,
				double area,
				double dip,
				double rake,
				double width,
				List<double[]> trace,
				List<Section> sections) {

			this.name = name;
			this.magnitude = magnitude;
			this.area = area;
			this.dip = dip;
			this.rake = rake;
			this.width = width;
			this.trace = trace;
			this.sections = sections;
		}
	}

	@SuppressWarnings("unused")
	private static class Section {

		final double area;
		final double dip;
		final double dipDir;
		final double width;
		final boolean reversed;
		final List<double[]> trace;
		final List<double[]> resampledTrace;

		Section(double area,
				double dip,
				double dipDir,
				double width,
				boolean reversed,
				List<double[]> trace,
				List<double[]> resampledTrace) {

			this.area = area;
			this.dip = dip;
			this.dipDir = dipDir;
			this.width = width;
			this.reversed = reversed;
			this.trace = trace;
			this.resampledTrace = resampledTrace;
			
		}
	}

}
