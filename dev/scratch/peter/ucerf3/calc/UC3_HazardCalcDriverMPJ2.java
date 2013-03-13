package scratch.peter.ucerf3.calc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.nshmp2.calc.ERF_ID;
import org.opensha.nshmp2.calc.HazardResultWriter;
import org.opensha.nshmp2.calc.HazardResultWriterLocal;
import org.opensha.nshmp2.calc.HazardResultWriterMPJ;
import org.opensha.nshmp2.calc.ThreadedHazardCalc;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;

import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;

public class UC3_HazardCalcDriverMPJ2 extends MPJTaskCalculator {
	
	private static final String S = File.separator;
	private ThreadedHazardCalc calc;
	private LocationList locs;
	
	// these will only end up getting used by the dispatch (root)
	// node during doFinalAssembly(); ignored on other nodes
	private File outDir;
	private Period period;
	
	// FOR USE WITH SINGLE SOLUTIONS: DOES NOT EXPECT A BRANCH IDENTIFIER BUT
	// TAKES A NAME INSTEAD THAT IS USED FOR OUTPUT
	
	public UC3_HazardCalcDriverMPJ2(CommandLine cmd, String[] args)
			throws IOException, InvocationTargetException, FileNotFoundException {
		
		super(cmd);
		if (args.length != 7) {
			System.err.println("USAGE: UC3_HazardCalcDriverMPJ [<options>] " +
					"<solPath> <mapID> <grid> <spacing> <period> <outPath> " +
					"<bgInclude>");
			abortAndExit(2);
		}

		Preconditions.checkArgument(getNumThreads() >= 1, 
				"threads must be >= 1. you supplied: "+getNumThreads());
		debug(rank, null, "setup for "+getNumThreads()+" threads");
		
		String solPath = args[0];
		String mapID = args[1];
		TestGrid grid = TestGrid.valueOf(args[2]);
		double spacing = Double.parseDouble(args[3]);
		locs = grid.grid(spacing).getNodeList();
		period = Period.valueOf(args[4]);
		String outPath = args[5];
		IncludeBackgroundOption bg = IncludeBackgroundOption.valueOf(args[6]);

		outDir = new File(outPath + S + mapID + S + grid + S + period);
		
		// mpj flag ignored in this case
		HazardResultWriter writer = new HazardResultWriterMPJ(outDir);
		calc = new ThreadedHazardCalc(solPath, locs, period, false, bg, writer);
	}
	
	@Override
	public int getNumTasks() {
		return locs.size();
	}
	
	@Override
	public void calculateBatch(int[] batch) throws Exception, InterruptedException {
		calc.calculate(batch);
		System.out.println("Batch complete");
	}
	

	@Override
	protected void doFinalAssembly() throws Exception {
		if (rank == 0) aggregateResults(outDir, period);
	}
	
	
	// overridden for testing
	public static Options createOptions() {
		Options ops = MPJTaskCalculator.createOptions();
		
		Option erfOp = new Option("e", "mult-erfs", false, "If set, a copy of the ERF will be instantiated for each thread.");
		erfOp.setRequired(false);
		ops.addOption(erfOp);
		
		return ops;
	}
	
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			CommandLine cmd = parse(options, args, UC3_HazardCalcDriverMPJ2.class);
			args = cmd.getArgs();
			UC3_HazardCalcDriverMPJ2 driver = new UC3_HazardCalcDriverMPJ2(cmd, args);
			driver.run();
			finalizeMPJ();
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}
	
	/**
	 * Utility method to aggregate hazard curves stored in indivudual files
	 * with names lat_lon.txt.
	 * 
	 * @param dir containing curve files
	 * @param period for which curves were calculated
	 */
	public static void aggregateResults(File dir, Period period) {
		String[] exts = {"txt"};
		try {
			Collection<File> files = FileUtils.listFiles(dir, exts, false);
			File curves = new File(dir, "curves.csv");
			BufferedWriter br = Files.newWriter(curves, Charsets.US_ASCII);
			HazardResultWriterLocal.writeCurveHeader(br, period);
			for (File file : files) {
				StringBuilder sb = new StringBuilder();
				String latlon = StringUtils.replaceChars(StringUtils.substringBeforeLast(
					file.getName(), "."), '_', ',');
				sb.append(latlon).append(",");
				Files.copy(file, Charsets.US_ASCII, sb);
				br.write(sb.toString());
				br.newLine();
				file.delete();
			}
			Flushables.flushQuietly(br);
			Closeables.closeQuietly(br);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
