package org.opensha.sha.cybershake.calc.mcer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;

public class MPJ_GMPE_CacheGen extends MPJTaskCalculator {
	
	public MPJ_GMPE_CacheGen(CommandLine cmd) {
		super(cmd);
	}

	@Override
	protected int getNumTasks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doFinalAssembly() throws Exception {
		// TODO Auto-generated method stub

	}
	
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
			
			CommandLine cmd = parse(options, args, MPJ_GMPE_CacheGen.class);
			
			MPJ_GMPE_CacheGen driver = new MPJ_GMPE_CacheGen(cmd);
			
			driver.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
