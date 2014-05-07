package scratch.stirling;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.util.TectonicRegionType;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.common.primitives.Doubles;

/**
 * Add comments here
 *
 * @author Peter Powers
 */
public class NewZealandParser {

	private static final Splitter SPLIT = Splitter.on(" ").omitEmptyStrings();
	private static final String S = StandardSystemProperty.FILE_SEPARATOR.value();
	private static final String gridPath = "data" + S + "backgroundGrid.txt";
	private static final String faultPath = "data" + S + "FUN1111.DAT";
	
	public static void loadFaultSources() throws IOException {

		// Example input:
		//
		// AhuririR rv											fault-name
		// 3D													section-count
		// 45.0 280.0 12.0 0.0									dip dip-dir depth-base depth-top
		// 44 4.2 169 42.0 44 26.7 169 41.5    7.2    6100		endpoints: lat(deg min) lon(deg min) x2 mag recurrence
		// 44 4.2 169 42.0 44 12.8 169 37.4						sections: lat(deg min) lon(deg min)
		// 44 12.8 169 37.4 44 17.9 169 36.8					...
		// 44 17.9 169 36.8 44 26.7 169 41.5
		// -1													end	

		// aggregator lists
		List<String> names = Lists.newArrayList();
		List<TectonicRegionType> trts = Lists.newArrayList();
		List<Double> rakes = Lists.newArrayList();
		List<Double> mags = Lists.newArrayList();
		List<Double> recurs = Lists.newArrayList();
		List<Double> dips = Lists.newArrayList();
		List<Double> dipDirs = Lists.newArrayList();
		List<Double> zTops = Lists.newArrayList();
		List<Double> zBots = Lists.newArrayList();
		List<LocationList> traces = Lists.newArrayList();
		
//		List<EvenlyGriddedSurface> surfaces = Lists.newArrayList();

		URL url = Resources.getResource(NewZealandParser.class, faultPath);
		List<String> lines = Resources.readLines(url, Charsets.US_ASCII);
		Iterator<String> lineIterator = Iterables.skip(lines, 3).iterator(); // skip a and b data
		
		while (lineIterator.hasNext()) {
			
			// get name and slip style
			List<String> nameSlip = SPLIT.splitToList(lineIterator.next());
			names.add(nameSlip.get(0));
			NZ_SourceID id = NZ_SourceID.fromString(nameSlip.get(1));
			trts.add(id.tectonicType());
			rakes.add(id.rake());
			
			// get section count
			String sizeID = lineIterator.next().trim();
			int dIndex = sizeID.indexOf("D");
			String sizeStr = sizeID.substring(0, dIndex);
			int size = Integer.parseInt(sizeStr);
			
			// get geometry data
			List<Double> geomValues = lineToDoubleList(lineIterator.next());
			dips.add(geomValues.get(0));
			dipDirs.add(geomValues.get(1));
			zTops.add(geomValues.get(3));
			zBots.add(geomValues.get(2));
			
			// trace endpoint specification -- mostly ignored
			List<Double> traceData = lineToDoubleList(lineIterator.next());
			mags.add(traceData.get(8));
			recurs.add(traceData.get(9));
			
			// build trace
			LocationList locs = new LocationList();
			for (int i=0; i<size; i++) {
				String locLine = lineIterator.next();
				locs.add(parseLocation(locLine));
				if (i == size - 1) {
					locs.add(parseLocation2(locLine));
				}
			}
			traces.add(locs);
			
			// skip closing -1
			lineIterator.next();
			
		}
		
		for (int i=0; i<names.size(); i++) {
			System.out.println();
			System.out.println("  Name: " + names.get(i));
			System.out.println("   Mag: " + mags.get(i));
			System.out.println("   TRT: " + trts.get(i));
			System.out.println(" Recur: " + recurs.get(i));
			System.out.println("   Dip: " + dips.get(i));
			System.out.println("DipDir: " + dipDirs.get(i));
			System.out.println("  Rake: " + rakes.get(i));
			System.out.println("  zTop: " + zTops.get(i));
			System.out.println("  zBot: " + zBots.get(i));
			System.out.println(" Trace: " + traces.get(i));
		}
	}
	
	// All incoming lats need to be converted to southern hemi values.
	private static Location parseLocation(String line) {
		List<Double> vals = lineToDoubleList(line);
		double lat = vals.get(0) + vals.get(1) / 60.0;
		double lon = vals.get(2) + vals.get(3) / 60.0;
		return new Location(lat, lon);
	}
	
	// reads the second location; only used at last section
	private static Location parseLocation2(String line) {
		List<Double> vals = lineToDoubleList(line);
		double lat = vals.get(4) + vals.get(5) / 60.0;
		double lon = vals.get(6) + vals.get(7) / 60.0;
		return new Location(lat, lon);
	}
	
	// convert line of space delimited numbers to list of double values
	private static List<Double> lineToDoubleList(String line) {
		return FluentIterable
				.from(SPLIT.splitToList(line))
				.transform(Doubles.stringConverter())
				.toList();
	}
	
	public static void main(String[] args) throws IOException {
		loadFaultSources();
	}
}
