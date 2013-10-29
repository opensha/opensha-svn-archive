package org.opensha.sra.calc.parallel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.params.MagDistCutoffParam;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.AbstractIMR;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sra.gui.portfolioeal.Asset;
import org.opensha.sra.gui.portfolioeal.CalculationExceptionHandler;
import org.opensha.sra.gui.portfolioeal.Portfolio;
import org.opensha.sra.gui.portfolioeal.PortfolioEALCalculatorController;

import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;

import com.google.common.base.Preconditions;

public class MPJ_EAL_Rupcalc extends MPJTaskCalculator implements CalculationExceptionHandler {
	
	public static final String BATCH_ELEMENT_NAME = "BatchCalculation";
	
	protected List<Asset> assets;
//	protected double maxSourceDistance = 200; // TODO set
	
	private double[][] my_results;
	
	private ThreadedEAL_IMR_Precalc calc;
	
	private File outputFile;
	
	private ERF refERF;
	
	private static final boolean FILE_DEBUG = false;
	
	public MPJ_EAL_Rupcalc(CommandLine cmd, Portfolio portfolio, Element el) throws IOException, DocumentException, InvocationTargetException {
		this(cmd, portfolio, el, null);
	}
	
	public MPJ_EAL_Rupcalc(CommandLine cmd, Portfolio portfolio, Element el, File outputFile) throws IOException, DocumentException, InvocationTargetException {
		super(cmd);
		
		assets = portfolio.getAssetList();
		System.gc();
		
		if (outputFile == null)
			outputFile = new File(el.attributeValue("outputFile"));
		this.outputFile = outputFile;
		
		int numThreads = getNumThreads();
		
		int numERFs;
		if (cmd.hasOption("mult-erfs"))
			numERFs = numThreads; // could set to 1 for single instance
		else
			numERFs = 1;
		
		debug("updating ERFs");
		ERF[] erfs = new ERF[numERFs];
		for (int i=0; i<numERFs; i++) {
			erfs[i] = loadERF(el);
			erfs[i].updateForecast();
		}
		debug("done updating ERFs");
		
		refERF = erfs[0];
		
		my_results = new double[refERF.getNumSources()][];
		for (int sourceID=0; sourceID<refERF.getNumSources(); sourceID++)
			my_results[sourceID] = new double[refERF.getNumRuptures(sourceID)];
		
//		ERF erf = loadERF(el);
//		erf.updateForecast();
		
		ScalarIMR[] imrs = new ScalarIMR[numThreads];
		for (int i=0; i<imrs.length; i++) {
			imrs[i] = (ScalarIMR)AbstractIMR.fromXMLMetadata(el.element(AbstractIMR.XML_METADATA_NAME), null);
		}
		
		// TODO mag thresh func
		ArbitrarilyDiscretizedFunc magThreshFunc = new MagDistCutoffParam().getDefaultValue();
		
		calc = new ThreadedEAL_IMR_Precalc(assets, erfs, imrs, this, magThreshFunc);
		
		if (cmd.hasOption("vuln-file")) {
			File vulnFile = new File(cmd.getOptionValue("vuln-file"));
			System.out.println("trying to load vulnerabilities from: "+vulnFile.getAbsolutePath());
			PortfolioEALCalculatorController.getVulnerabilities(vulnFile);
			System.out.println("DONE loading vulns.");
		}
	}
	
	private ERF loadERF(Element root) throws InvocationTargetException {
		Element epistemicEl = root.element(AbstractEpistemicListERF.XML_METADATA_NAME);
		if (epistemicEl != null)
			return (ERF) AbstractEpistemicListERF.fromXMLMetadata(epistemicEl);
		else
			return AbstractERF.fromXMLMetadata(root.element(AbstractERF.XML_METADATA_NAME));
	}

	@Override
	protected int getNumTasks() {
		return assets.size();
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		List<SiteResult> results = calc.calculateBatch(batch);
		
		for (SiteResult result : results)
			registerResult(result);
		
		System.gc();
		Runtime rt = Runtime.getRuntime();
		long totalMB = rt.totalMemory() / 1024 / 1024;
		long freeMB = rt.freeMemory() / 1024 / 1024;
		long usedMB = totalMB - freeMB;
		debug("post calc mem t/u/f: "+totalMB+"/"+usedMB+"/"+freeMB);
	}
	
	protected synchronized void registerResult(SiteResult result) {
		double[][] vals = result.results;
		Preconditions.checkState(vals.length == my_results.length,
				"Source count discrepancy. Expected "+my_results.length+", was "+vals.length);
		for (int sourceID=0; sourceID<vals.length; sourceID++) {
			if (vals[sourceID] != null) {
//				Preconditions.checkState(vals[sourceID].length == my_results[sourceID].length,
//						"Rup count discrepancy for source "+sourceID+". Expected "+my_results[sourceID].length
//						+", was "+vals[sourceID].length);
				for (int rupID=0; rupID<vals[sourceID].length; rupID++)
					my_results[sourceID][rupID] += vals[sourceID][rupID];
			}
		}
	}
	
	@Override
	protected void doFinalAssembly() throws Exception {
		// gather the loss
		
		int TAG_GET_NUM = 0;
		int TAG_GET_RESULTS = 1;
		
		// pack results into one dimensional array
		int rupCount = 0;
		for (int sourceID=0; sourceID<my_results.length; sourceID++)
			rupCount += my_results[sourceID].length;
		double[] packed_results = new double[rupCount];
		int cnt = 0;
		for (double[] vals : my_results)
			for (double val : vals)
				packed_results[cnt++] = val;
		
		
		if (rank == 0) {
			double[] global_results = new double[rupCount];
			
			for (int source=0; source<size; source++) {
				double[] srcResults;
				
				if (source == rank) {
					srcResults = packed_results;
				} else {
					// get results
					srcResults = new double[rupCount];
					MPI.COMM_WORLD.Recv(srcResults, 0, srcResults.length, MPI.DOUBLE, source, TAG_GET_RESULTS);
				}
				
				for (int i=0; i<rupCount; i++)
					global_results[i] += srcResults[i];
			}
			
			// now unpack
			double[][] unpacked_results = new double[my_results.length][];
			cnt = 0;
			for (int sourceID=0; sourceID<my_results.length; sourceID++) {
				unpacked_results[sourceID] = new double[my_results[sourceID].length];
				for (int rupID=0; rupID<my_results[sourceID].length; rupID++)
					unpacked_results[sourceID][rupID] = global_results[cnt++];
			}
			writeResults(outputFile, unpacked_results);
			
			if (refERF instanceof FaultSystemSolutionERF) {
				double[][] fssResults = mapResultsToFSS((FaultSystemSolutionERF)refERF, unpacked_results);
				
				String name = outputFile.getName();
				if (name.toLowerCase().endsWith(".bin"))
					name = name.substring(0, name.toLowerCase().indexOf(".bin"));
				File fssOutputFile = new File(outputFile.getParentFile(), name+"_fss_index.bin");
				writeResults(fssOutputFile, fssResults);
				File fssGridOutputFile = new File(outputFile.getParentFile(), name+"_fss_gridded.bin");
				writeFSSGridSourcesFile((FaultSystemSolutionERF)refERF, unpacked_results, fssGridOutputFile);
			}
		} else {
			// send results
			MPI.COMM_WORLD.Send(packed_results, 0, packed_results.length, MPI.DOUBLE, 0, TAG_GET_RESULTS);
		}
	}
	
	public static double[][] mapResultsToFSS(FaultSystemSolutionERF erf, double[][] origResults) throws IOException {
		// write it out by rupture index as well. we can use the same file format
		int numFSSRups = erf.getSolution().getRupSet().getNumRuptures();
		double[][] fssResults = new double[numFSSRups][];
		for (int r=0; r<numFSSRups; r++) {
			int sourceIndex = erf.getSrcIndexForFltSysRup(r);
			if (sourceIndex < 0)
				fssResults[r] = new double[0];
			else
				fssResults[r] = origResults[sourceIndex];
		}
		return fssResults;
	}
	
	public static void writeFSSGridSourcesFile(FaultSystemSolutionERF erf, double[][] origResults, File file) throws IOException {
		int fssSources = erf.getNumFaultSystemSources();
		int numSources = erf.getNumSources();
		int numGridded = numSources - fssSources;
		
		if (numGridded <= 0)
			return;
		
		GridSourceProvider prov = erf.getSolution().getGridSourceProvider();

		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		out.writeInt(numGridded);
		
		for (int srcIndex=fssSources; srcIndex<erf.getNumSources(); srcIndex++) {
			// returned in nodeList order
			int nodeIndex = srcIndex - fssSources;
			Location loc = prov.getGriddedRegion().locationForIndex(nodeIndex);
			
			// write location to be safe in case gridding changes in the future
			out.writeDouble(loc.getLatitude());
			out.writeDouble(loc.getLongitude());
			
			ProbEqkSource source = erf.getSource(srcIndex);
			
			out.writeInt(source.getNumRuptures());
			
			for (int r=0; r<source.getNumRuptures(); r++) {
				ProbEqkRupture rup = source.getRupture(r);
				out.writeDouble(rup.getMag());
				// expected loss
				out.writeDouble(origResults[srcIndex][r]);
			}
		}
		
		out.close();
	}
	
	// TODO
//	private void writeOutputFile(SiteResult[] results) throws IOException {
//		FileWriter fw = new FileWriter(outputFile);
//		
//		double portfolioEAL = StatUtils.sum(eal_vals);
//		
//		// TODO add metadata
//		fw.write("Portfolio EAL: "+portfolioEAL+"\n");
//		fw.write("\n");
//		for (int i=0; i<eal_vals.length; i++) {
//			int id = (Integer)assets.get(i).getParameterList().getParameter("AssetID").getValue();
//			fw.write(id+","+eal_vals[i]+"\n");
//		}
//		fw.close();
//	}
	
	// TODO
	/**
	 * File format:<br>
	 * [num ERF sources]
	 * 		[num ruptures for source]
	 *      	[expected loss for rupture]
	 * @param results
	 * @param file
	 * @throws IOException 
	 */
	public static void writeResults(File file, double[][] results) throws IOException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		
		int numSources = results.length;
		
		out.writeInt(numSources);
		
		// get asset counts for each source/rup
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			int numRups = results[sourceID].length;
			out.writeInt(numRups);
			
			for (int rupID=0; rupID<numRups; rupID++)
				out.writeDouble(results[sourceID][rupID]);
		}
		
		out.close();
		
		if (FILE_DEBUG) {
			System.out.println("Auditing file IO");
			double[][] results2 = loadResults(file);
			int checks = 0;
			Preconditions.checkState(results.length == results2.length);
			checks++;
			for (int sourceID=0; sourceID<results.length; sourceID++) {
				int numRups = results[sourceID].length;
				Preconditions.checkState(numRups == results2[sourceID].length);
				checks++;
				for (int rupID=0; rupID<numRups; rupID++) {
					double v1 = results[sourceID][rupID];
					double v2 = results2[sourceID][rupID];
					Preconditions.checkState((float)v1 == (float)v2);
					checks++;
				}
			}
			System.out.println("Done auditing file IO ("+checks+" checks)");
		}
	}
	
	/**
	 * [num ERF sources]
	 * 		[num ruptures for source]
	 *      	[expected loss for rupture]
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static double[][] loadResults(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		is = new BufferedInputStream(is);

		DataInputStream in = new DataInputStream(is);

		int numSources = in.readInt();

		Preconditions.checkState(numSources > 0, "Size must be > 0!");
		
		double[][] results = new double[numSources][];
		
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			int numRups = in.readInt();
			results[sourceID] = new double[numRups];
			for (int rupID=0; rupID<numRups; rupID++) {
				results[sourceID][rupID] = in.readDouble();
			}
		}
		
		return results;
	}
	
	public static Options createOptions() {
		Options ops = MPJTaskCalculator.createOptions();
		
		Option vulnOp = new Option("v", "vuln-file", true, "VUL06 file");
		vulnOp.setRequired(false);
		ops.addOption(vulnOp);
		
		Option erfOp = new Option("e", "mult-erfs", false, "If set, a copy of the ERF will be instantiated for each thread.");
		erfOp.setRequired(false);
		ops.addOption(erfOp);
		
		return ops;
	}
	
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			
			CommandLine cmd = parse(options, args, MPJ_EAL_Rupcalc.class);
			
			args = cmd.getArgs();
			
			if (args.length < 2 || args.length > 3) {
				System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(MPJ_EAL_Rupcalc.class)
						+" [options] <portfolio_file> <calculation_params_file> [<output_file>]");
				abortAndExit(2);
			}

			Portfolio portfolio = Portfolio.createPortfolio(new File(args[0]));

			Document doc = XMLUtils.loadDocument(new File(args[1]));
			Element root = doc.getRootElement();
			
			if (args.length == 2) {
				// batch mode
				
				Iterator<Element> it = root.elementIterator(BATCH_ELEMENT_NAME);
				
				while (it.hasNext()) {
					MPJ_EAL_Rupcalc driver = new MPJ_EAL_Rupcalc(cmd, portfolio, it.next());
					
					driver.run();
				}
			} else {
				File outputFile = new File(args[2]);
				
				MPJ_EAL_Rupcalc driver = new MPJ_EAL_Rupcalc(cmd, portfolio, root, outputFile);
				
				driver.run();
			}
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

	@Override
	public void calculationException(String errorMessage) {
		abortAndExit(new RuntimeException(errorMessage));
	}
	
	public static class SiteResult implements Serializable, Comparable<SiteResult> {
		
		private int index;
		private transient Asset asset;
		private transient CalculationExceptionHandler handler;
		
		// expected loss results per rupture: [sourceID][rupID]
		private double[][] results;
		
		public SiteResult(int index, Asset asset, CalculationExceptionHandler handler) {
			super();
			this.index = index;
			this.asset = asset;
			this.handler = handler;
		}
		
		void calculate(ERF erf, ScalarIMR imr, Site initialSite, ArbitrarilyDiscretizedFunc magThreshFunc) {
			results = asset.calculateExpectedLossPerRup(imr, magThreshFunc, initialSite, erf, handler);
		}

		@Override
		public int compareTo(SiteResult o) {
			return new Integer(index).compareTo(o.index);
		}
	}

}
