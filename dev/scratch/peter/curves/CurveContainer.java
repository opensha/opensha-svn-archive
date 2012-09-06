package scratch.peter.curves;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.util.NSHMP_Utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.primitives.Doubles;

/**
 * Class for storing and accessing grids of hazard curves. Used to mine
 * NSHMP result sets.
 *
 * @author Peter Powers
 * @version $Id:$
 */
@Deprecated
public class CurveContainer {
	
	private GriddedRegion region;
	private List<Double> xs;
	private Map<Integer, List<Double>> ysMap;
	
	/**
	 * Returns the hazard curve for the supplied location.
	 * @param loc of interest
	 * @return the associated hazard curve
	 * @throws IllegalArgumentException if loc is out of range of curve data
	 */
	public DiscretizedFunc getCurve(Location loc) {
		int idx = region.indexForLocation(loc);
		Preconditions.checkArgument(
			idx != -1, "Location is out of range: " + loc);
		List<Double> ys = ysMap.get(idx);
		ArbitrarilyDiscretizedFunc f = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<xs.size(); i++) {
			f.set(xs.get(i), ys.get(i));
		}
		return f;
	}
	
	/**
	 * Returns the number of curves stored in this container.
	 * @return the container size
	 */
	public int size() {
		return ysMap.size();
	}

	/**
	 * Creates a curve container from the supplied data file and region. The
	 * data locations should match the nodes in the gridded region. Results
	 * are unspecified if the two do not agree.
	 * @param f file
	 * @param r gridded region
	 * @return a new curve container object
	 */
	public static CurveContainer create(File f, GriddedRegion r) {
		CurveFileProcessor cfp = new CurveFileProcessor(r);
		CurveContainer curves = null;
		try {
			curves = Files.readLines(f, Charsets.US_ASCII, cfp);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return curves;
	}
	
	
	static class CurveFileProcessor implements LineProcessor<CurveContainer> {
		
		private Splitter split;
		private int xCount = 0;
		private int headCount = 0;
		private int headLines = 3;
		private CurveContainer cc;
		
		CurveFileProcessor(GriddedRegion region) {
			split = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();
			cc = new CurveContainer();
			cc.region = region;
			cc.xs = Lists.newArrayList();
			cc.ysMap = Maps.newHashMapWithExpectedSize(region.getNodeCount());
		}

		@Override
		public CurveContainer getResult() {
			return cc;
		}

		@Override
		public boolean processLine(String line) throws IOException {
			
			// skip first 3 lines for either format
			if (headCount < headLines) {
				headCount++;
				return true;
			}
//			if (line.startsWith("#") ||| line.startsWith(prefix)) return true;
			
			// short lines are going to be x values
			if (line.length() < 20) {
				cc.xs.add(Double.parseDouble(line));
				xCount++;
				return true;
			}
						
			addCurve(line);
			return true;
		}
		
		private void addCurve(String line) {
			Iterator<String> it = split.split(line).iterator();
			// read location
			Location loc = new Location(toNum(it.next()), toNum(it.next()));
			int idx = cc.region.indexForLocation(loc);
			double[] vals = new double[xCount];
			for (int i=0; i<xCount; i++) {
				vals[i] = toNum(it.next());
			}
			cc.ysMap.put(idx, Doubles.asList(vals));
		}
		
	}
	
	private static double toNum(String s) {
		return Double.parseDouble(s);
	}
	

	private static String path = "/Volumes/Scratch/nshmp-sources/FortranLatest/GM0P00/curves_us_all.pga";
	private static double minLat = 24.6;
	private static double maxLat = 50.0;
	private static double minLon = -125.0;
	private static double maxLon = -65.0;
	private static double spacing = 0.05;

	private static Location usHazLoc1 = new Location(minLat, minLon);
	private static Location usHazLoc2 = new Location(maxLat, maxLon);
	
	public static GriddedRegion getNSHMP_Region() {
		return new GriddedRegion(usHazLoc1, usHazLoc2, spacing,
			GriddedRegion.ANCHOR_0_0);
	}

	public static void main(String[] args) {
		File f = new File(path);
		GriddedRegion gr = new GriddedRegion(usHazLoc1, usHazLoc2, spacing, GriddedRegion.ANCHOR_0_0);
		System.out.println("region created: " + gr.getNodeCount());
		Stopwatch sw = new Stopwatch();
		sw.start();
		CurveContainer cc = CurveContainer.create(f, gr);
		sw.stop();
		System.out.println("time: " + sw.elapsedMillis());
		System.out.println("size: " + cc.size());
		System.out.println(cc.getCurve(new Location(34.1, -118.3)));
		System.out.println(cc.getCurve(NEHRP_TestCity.LOS_ANGELES.location()));
	}
}


// NOTE, data files can be formatted as...

//#Pgm hazallXL.v4.f (harmsen) sums  2 hazard curves from ../conf/combine/curves_us.pga 
//#Lat Long   Rex for spectral period 0.00
//#GM set(g) 19
//0.50000E-02
//0.70000E-02
//0.98000E-02
//0.13700E-01
//0.19200E-01
//0.26900E-01
//0.37600E-01
//0.52700E-01
//0.73800E-01
//0.10300E+00
//0.14500E+00
//0.20300E+00
//0.28400E+00
//0.39700E+00
//0.55600E+00
//0.77800E+00
//0.10900E+01
//0.15200E+01
//0.21300E+01
//   50.000  -125.000 4.99469E-02 4.11959E-02 3.25710E-02 2.47907E-02 1.82196E-02 1.30642E-02 9.22078E-03 6.37273E-03 4.27867E-03 2.73385E-03 1.58381E-03 8.28554E-04 3.82728E-04 1.54050E-04 5.20416E-05 1.41056E-05 2.58034E-06 1.93185E-07 2.47693E-08
//   50.000  -124.950 5.07659E-02 4.18204E-02 3.30050E-02 2.50571E-02 1.83528E-02 1.31039E-02 9.20262E-03 6.32359E-03 4.21818E-03 2.67578E-03 1.53755E-03 7.97565E-04 3.65283E-04 1.45810E-04 4.88500E-05 1.31283E-05 2.36483E-06 1.95222E-07 2.59898E-08
//   50.000  -124.900 5.15849E-02 4.24449E-02 3.34389E-02 2.53236E-02 1.84861E-02 1.31436E-02 9.18446E-03 6.27445E-03 4.15768E-03 2.61770E-03 1.49128E-03 7.66577E-04 3.47839E-04 1.37569E-04 4.56583E-05 1.21511E-05 2.14932E-06 1.97259E-07 2.72103E-08
//   50.000  -124.850 5.23902E-02 4.30591E-02 3.38673E-02 2.55895E-02 1.86229E-02 1.31892E-02 9.17283E-03 6.23128E-03 4.10218E-03 2.56377E-03 1.44840E-03 7.38148E-04 3.32114E-04 1.30299E-04 4.29240E-05 1.13473E-05 1.99972E-06 2.04181E-07 2.86878E-08
//   50.000  -124.800 5.31954E-02 4.36733E-02 3.42956E-02 2.58554E-02 1.87597E-02 1.32349E-02 9.16121E-03 6.18811E-03 4.04668E-03 2.50984E-03 1.40552E-03 7.09720E-04 3.16390E-04 1.23029E-04 4.01896E-05 1.05434E-05 1.85012E-06 2.11104E-07 3.01653E-08

// ... OR ...

//CEUShazard.200809.pga                                                           
//WUShazard.2008.pga                                                              
// 0.0E+0
//0.5000E-02
//0.7000E-02
//0.9800E-02
//0.1370E-01
//0.1920E-01
//0.2690E-01
//0.3760E-01
//0.5270E-01
//0.7380E-01
//0.1030E+00
//0.1450E+00
//0.2030E+00
//0.2840E+00
//0.3970E+00
//0.5560E+00
//0.7780E+00
//0.1090E+01
//0.1520E+01
//0.2130E+01
//50.00 -125.00 0.4950E-01 0.4074E-01 0.3212E-01 0.2435E-01 0.1782E-01 0.1271E-01 0.8927E-02 0.6140E-02 0.4105E-02 0.2613E-02 0.1507E-02 0.7847E-03 0.3606E-03 0.1444E-03 0.4850E-04 0.1305E-04 0.2349E-05 0.1625E-06 0.1919E-07
//50.00 -124.95 0.5031E-01 0.4135E-01 0.3254E-01 0.2461E-01 0.1794E-01 0.1275E-01 0.8905E-02 0.6090E-02 0.4046E-02 0.2557E-02 0.1463E-02 0.7555E-03 0.3443E-03 0.1367E-03 0.4552E-04 0.1213E-04 0.2146E-05 0.1643E-06 0.2013E-07
//50.00 -124.90 0.5112E-01 0.4197E-01 0.3296E-01 0.2487E-01 0.1807E-01 0.1278E-01 0.8883E-02 0.6040E-02 0.3986E-02 0.2501E-02 0.1419E-02 0.7262E-03 0.3279E-03 0.1290E-03 0.4254E-04 0.1122E-04 0.1942E-05 0.1660E-06 0.2107E-07
//50.00 -124.85 0.5192E-01 0.4257E-01 0.3338E-01 0.2513E-01 0.1820E-01 0.1282E-01 0.8869E-02 0.5996E-02 0.3932E-02 0.2449E-02 0.1378E-02 0.6995E-03 0.3132E-03 0.1223E-03 0.4000E-04 0.1048E-04 0.1802E-05 0.1723E-06 0.2221E-07

