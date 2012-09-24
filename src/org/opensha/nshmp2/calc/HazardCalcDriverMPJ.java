package org.opensha.nshmp2.calc;

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
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Flushables;

public class HazardCalcDriverMPJ extends MPJTaskCalculator {
	
	private static final String S = File.separator;
	private ThreadedHazardCalc calc;
	private LocationList locs;
	
	// these will only end up getting used by the dispatch (root)
	// node during doFinalAssembly(); ignored on other nodes
	private File outDir;
	private Period period;
	
	public HazardCalcDriverMPJ(CommandLine cmd, String[] args)
			throws IOException, InvocationTargetException, FileNotFoundException {
		
		super(cmd);
		if (args.length != 1) {
			System.err.println("USAGE: HazardCalcDriver [<options>] <properties file>");
			abortAndExit(2);
		}

		Preconditions.checkArgument(getNumThreads() >= 1, 
				"threads must be >= 1. you supplied: "+getNumThreads());
		debug(rank, "setup for "+getNumThreads()+" threads");
		
		URL props = new File(args[0]).toURI().toURL();
		// throws FNF exceptions
		HazardCalcConfig config = new HazardCalcConfig(props);
		
		TestGrid grid = config.grid;
		Preconditions.checkNotNull(grid);
		locs = grid.grid().getNodeList();
		
		period = config.period;
		Preconditions.checkNotNull(period);
		
		String name = config.name;
		Preconditions.checkArgument(StringUtils.isNotBlank(name));
		
		String out = config.out;
		Preconditions.checkArgument(StringUtils.isNotBlank(out));
		outDir = new File(out + S + name + S + grid + S + period);
		
		// mpj flag ignored in this case
		HazardResultWriter writer = new HazardResultWriterMPJ(outDir);
		calc = new ThreadedHazardCalc(grid.grid().getNodeList(), period, writer);
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
		aggregateResults(outDir, period);
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
			CommandLine cmd = parse(options, args, HazardCalcDriverMPJ.class);
			args = cmd.getArgs();
			HazardCalcDriverMPJ driver = new HazardCalcDriverMPJ(cmd, args);
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
				file.delete();
				br.write(sb.toString());
				br.newLine();
			}
			Flushables.flushQuietly(br);
			Closeables.closeQuietly(br);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
