package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.hpc.mpj.taskDispatch.DispatcherThread;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.XMLUtils;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.calc.hazardMap.ThreadedHazardCurveSetCalculator;
import org.opensha.sha.calc.hazardMap.components.CalculationInputsXMLFile;

import com.google.common.base.Preconditions;

public class HazardCalcDriverMPJ extends MPJTaskCalculator {
	
	private static final int MIN_DISPATCH_DEFAULT = 5;
	private static final int MAX_DISPATCH_DEFAULT = 100;
	
	private ThreadedHazardCalc calc;
	private LocationList locs;
	
	private int rank;
	
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
		
		File props = new File(args[0]);
		// throws FNF exceptions
		HazardCalcConfig config = new HazardCalcConfig(props);
		
		TestGrid grid = config.grid;
		Preconditions.checkNotNull(grid);
		locs = grid.grid().getNodeList();
		
		Period period = config.period;
		Preconditions.checkNotNull(period);
		
		String name = config.name;
		Preconditions.checkArgument(StringUtils.isNotBlank(name));
		
		String out = config.out;
		Preconditions.checkArgument(StringUtils.isNotBlank(out));
		File outDir = new File(out + period);
		
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
	}
	

	@Override
	protected void doFinalAssembly() throws Exception {
		// do nothing
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
	

}
