package scratch.peter.ucerf3;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opensha.commons.geo.Location;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sun.istack.internal.Nullable;

/**
 * Class provides one method, {@code getScale(Location)} that returns the value
 * of Karen Felzer's UCERF3 smoothed seismicity spatial PDF at the supplied
 * location. Class assumes X and Y are lat lon discretized on 0.1 and uses ints
 * as row and column keys to access a sparse {@code HashBasedTable}.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class GridReader {

	private static final String DATA_DIR = "data/";
	private static final String RATE_DAT = "SmoothedSeismicity.txt";

	private static final Splitter SPLIT;

	private static final Function<String, Double> FN_STR_TO_DBL;
	private static final Function<Double, Integer> FN_DBL_TO_KEY;
	private static final Function<String, Integer> FN_STR_TO_KEY;

	private static Table<Integer, Integer, Double> table;

	private GridReader() {}

	static {
		SPLIT = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();
		FN_STR_TO_DBL = new FnStrToDbl();
		FN_DBL_TO_KEY = new FnDblToKey();
		FN_STR_TO_KEY = Functions.compose(FN_DBL_TO_KEY, FN_STR_TO_DBL);
		table = initTable();
	}

	/**
	 * Returns the spatial PDF value at the point closest to the supplied
	 * {@code Location}
	 * @param loc {@code Location} of interest
	 * @return a PDF value or @code null} if supplied {@coed Location} is more
	 *         than 0.05&deg; outside the available data domain
	 */
	public static Double getScale(Location loc) {
		return table.get(FN_DBL_TO_KEY.apply(loc.getLatitude()),
			FN_DBL_TO_KEY.apply(loc.getLongitude()));

	}

	private static Table<Integer, Integer, Double> initTable() {
		Table<Integer, Integer, Double> table = HashBasedTable.create();
		try {
			File f = getSourceFile(RATE_DAT);
			List<String> lines = Files.readLines(f, Charsets.US_ASCII);
			Iterator<String> dat;
			for (String line : lines) {
				dat = SPLIT.split(line).iterator();
				table.put(FN_STR_TO_KEY.apply(dat.next()),
					FN_STR_TO_KEY.apply(dat.next()),
					FN_STR_TO_DBL.apply(dat.next()));
			}
		} catch (IOException ioe) {
			throw Throwables.propagate(ioe);
		}
		return table;
	}

	private static File getSourceFile(String file) {
		URL url = Resources.getResource(GridReader.class, DATA_DIR + file);
		return FileUtils.toFile(url);
	}

	// //////// Conversion Functions //////////
	// / TODO these would make good utilities ///

	private static class FnStrToInt implements Function<String, Integer> {
		@Override
		public Integer apply(String s) {
			return new Integer(s);
		}
	}

	private static class FnStrToDbl implements Function<String, Double> {
		@Override
		public Double apply(String s) {
			return new Double(s);
		}
	}

	private static class FnDblToKey implements Function<Double, Integer> {
		@Override
		public Integer apply(Double d) {
			return (int) Math.round(d * 10);
		}
	}

	public static void main(String[] args) {
		 double sum = 0;
		 for (Table.Cell<Integer, Integer, Double> cell : table.cellSet()) {
			 sum += cell.getValue();
		 }
		 System.out.println(sum);
		 System.out.println(GridReader.getScale(new Location(39.65,  -120.1)));
		 System.out.println(GridReader.getScale(new Location(20, -20)));
	}

}
