package org.opensha.sra.calc.parallel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.metadata.MetadataLoader;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.nshmp2.calc.HazardCalc;
import org.opensha.nshmp2.calc.HazardResult;
import org.opensha.sha.calc.params.MagDistCutoffParam;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.AbstractIMR;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sra.gui.portfolioeal.Asset;
import org.opensha.sra.gui.portfolioeal.CalculationExceptionHandler;
import org.opensha.sra.gui.portfolioeal.Portfolio;
import org.opensha.sra.gui.portfolioeal.PortfolioEALCalculatorController;
import org.opensha.sra.vulnerability.Vulnerability;

import cern.colt.list.tint.IntArrayList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MPJ_EAL_IMR_Precalc extends MPJTaskCalculator implements CalculationExceptionHandler {
	
	public static final String BATCH_ELEMENT_NAME = "BatchCalculation";
	
	protected List<Asset> assets;
//	protected double maxSourceDistance = 200; // TODO set
	
	private ArrayList<SiteResult> results = Lists.newArrayList();
	
	private ThreadedEAL_IMR_Precalc calc;
	
	private File outputFile;
	
	private ERF refERF;
	
	private static final boolean FILE_DEBUG = false;
	
	public MPJ_EAL_IMR_Precalc(CommandLine cmd, Portfolio portfolio, Element el) throws IOException, DocumentException, InvocationTargetException {
		this(cmd, portfolio, el, null);
	}
	
	public MPJ_EAL_IMR_Precalc(CommandLine cmd, Portfolio portfolio, Element el, File outputFile) throws IOException, DocumentException, InvocationTargetException {
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
	}
	
	protected synchronized void registerResult(SiteResult result) {
		results.add(result);
	}
	
	@Override
	protected void doFinalAssembly() throws Exception {
		// gather the loss
		
		SiteResult[] my_results = this.results.toArray(new SiteResult[0]);
		
		int TAG_GET_NUM = 0;
		int TAG_GET_RESULTS = 1;
		
		if (rank == 0) {
			SiteResult[] global_results = new SiteResult[assets.size()];
			
			for (int source=0; source<size; source++) {
				SiteResult[] srcResults;
				
				if (source == rank) {
					srcResults = my_results;
				} else {
					// ask for size
					int[] size = new int[1];
					MPI.COMM_WORLD.Recv(size, 0, 1, MPI.INT, source, TAG_GET_NUM);
					
					// get results
					srcResults = new SiteResult[size[0]];
					MPI.COMM_WORLD.Recv(srcResults, 0, srcResults.length, MPI.OBJECT, source, TAG_GET_RESULTS);
				}
				
				for (SiteResult result : srcResults)
					global_results[result.index] = result;
				
			}
			
			for (SiteResult result : global_results)
				Preconditions.checkNotNull(result);
			
			// TODO
//			writeOutputFile(global_results);
			writeResults(outputFile, global_results, refERF);
		} else {
			int[] size = { my_results.length };
			MPI.COMM_WORLD.Send(size, 0, 1, MPI.INT, 0, TAG_GET_NUM);
			
			// send results
			MPI.COMM_WORLD.Send(my_results, 0, my_results.length, MPI.OBJECT, 0, TAG_GET_RESULTS);
		}
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
	 * [num assets]
	 * [num ERF sources]
	 * 		[num ruptures for source]
	 * 			[num assets for rupture]
	 * 				[asset index]
	 * 				[double: ln mean]
	 * 				[double: ln std dev]
	 * @param results
	 * @param file
	 * @throws IOException 
	 */
	public static void writeResults(File file, SiteResult[] results, ERF erf) throws IOException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		
		int numSources = erf.getNumSources();
		
		out.writeInt(results.length);
		out.writeInt(numSources);
		
		List<IntArrayList> sourceMappings = Lists.newArrayList();
		
		// get asset mappings for each source
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			
			IntArrayList sourceMapping = new IntArrayList();
			sourceMappings.add(sourceMapping);

			for (int i=0; i<results.length; i++) {
				double[][][] a = results[i].results;
				if (a[sourceID] != null)
					sourceMapping.add(i);
			}
		}
		
		// get asset counts for each source/rup
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			int numRups = erf.getNumRuptures(sourceID);
			
			IntArrayList sourceMapping = sourceMappings.get(sourceID);
			if (sourceMapping.isEmpty()) {
				// no assets have this source, set to zero rups
				out.writeInt(0);
				continue;
			} else {
				out.writeInt(numRups);
			}
			
			for (int rupID=0; rupID<numRups; rupID++) {
				List<Integer> assetIndexes = Lists.newArrayList();
				for (int i=0; i<sourceMapping.size(); i++) {
					int assetIndex = sourceMapping.get(i);
					double[][][] a = results[assetIndex].results;
					if (a[sourceID][rupID] != null) {
						assetIndexes.add(assetIndex);
					}
				}
				out.writeInt(assetIndexes.size());
				for (int assetIndex : assetIndexes) {
					double[] vals = results[assetIndex].results[sourceID][rupID];
					// index
					out.writeInt(assetIndex);
					// ln mean
					out.writeDouble(vals[0]);
					// ln std dev
					out.writeDouble(vals[1]);
				}
			}
		}
		
		out.close();
		
		if (FILE_DEBUG) {
			System.out.println("Auditing file IO");
			SiteResult[] results2 = loadResults(file);
			int checks = 0;
			Preconditions.checkState(results.length == results2.length);
			checks++;
			for (int i=0; i<results.length; i++) {
				SiteResult r1 = results[i];
				SiteResult r2 = results2[i];
				Preconditions.checkState(r1.index == r2.index);
				checks++;
				double[][][] a1 = r1.results;
				double[][][] a2 = r2.results;
				Preconditions.checkState(a1.length == a2.length);
				checks++;
				for (int sourceID=0; sourceID<a1.length; sourceID++) {
					if (a1[sourceID] == null) {
						Preconditions.checkState(a2[sourceID] == null);
						checks++;
					} else {
						Preconditions.checkState(a1[sourceID].length == a2[sourceID].length);
						checks++;
						for (int rupID=0; rupID<a1[sourceID].length; rupID++) {
							double[] v1 = a1[sourceID][rupID];
							double[] v2 = a2[sourceID][rupID];
							
							if (v1 == null) {
								Preconditions.checkState(v2 == null);
								checks++;
							} else {
								Preconditions.checkState((float)v1[0] == (float)v2[0]);
								checks++;
								Preconditions.checkState((float)v1[1] == (float)v2[1]);
								checks++;
							}
						}
					}
				}
			}
			System.out.println("Done auditing file IO ("+checks+" checks)");
		}
	}
	
	/**
	 * [num assets]
	 * [num ERF sources]
	 * 		[num ruptures for source]
	 * 			[num assets for rupture]
	 * 				[asset index]
	 * 				[double: ln mean]
	 * 				[double: ln std dev]
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static SiteResult[] loadResults(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		Preconditions.checkNotNull(is, "InputStream cannot be null!");
		is = new BufferedInputStream(is);

		DataInputStream in = new DataInputStream(is);

		int numAssets = in.readInt();
		int numSources = in.readInt();

		Preconditions.checkState(numAssets > 0, "Size must be > 0!");
		
		SiteResult[] results = new SiteResult[numAssets];
		for (int i=0; i<numAssets; i++) {
			results[i] = new SiteResult(i, null);
			results[i].results = new double[numSources][][];
		}
		
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			int numRups = in.readInt();
			for (int rupID=0; rupID<numRups; rupID++) {
				int numRupAssets = in.readInt();
				for (int i=0; i<numRupAssets; i++) {
					int assetIndex = in.readInt();
					double[][][] res = results[assetIndex].results;
					if (res[sourceID] == null)
						res[sourceID] = new double[numRups][];
					double[] rupVals = { in.readDouble(), in.readDouble() };
					res[sourceID][rupID] = rupVals;
				}
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
			
			CommandLine cmd = parse(options, args, MPJ_EAL_IMR_Precalc.class);
			
			args = cmd.getArgs();
			
			if (args.length < 2 || args.length > 3) {
				System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(MPJ_EAL_IMR_Precalc.class)
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
					MPJ_EAL_IMR_Precalc driver = new MPJ_EAL_IMR_Precalc(cmd, portfolio, it.next());
					
					driver.run();
				}
			} else {
				File outputFile = new File(args[2]);
				
				MPJ_EAL_IMR_Precalc driver = new MPJ_EAL_IMR_Precalc(cmd, portfolio, root, outputFile);
				
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
		
		// results: [sourceID][rupID][mean,std dev]
		private double[][][] results;
		
		public SiteResult(int index, Asset asset) {
			super();
			this.index = index;
			this.asset = asset;
		}
		
		void calculate(ERF erf, ScalarIMR imr, Site initialSite, ArbitrarilyDiscretizedFunc magThreshFunc) {
			System.out.println("Calculating asset "+index);
			asset.siteSetup(initialSite);
			Site site = asset.getSite();
			imr.setSite(site);
			Vulnerability vulnModel = PortfolioEALCalculatorController.getVulnerabilities()
					.get(asset.getVulnModelName());
			String imt = vulnModel.getIMT();
			imr.setIntensityMeasure(imt);
			if (imt.equals(SA_Param.NAME))
				SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), vulnModel.getPeriod());
			results = new double[erf.getNumSources()][][];
			for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
				System.out.println("Calculating asset "+index+" source "+sourceID);
				ProbEqkSource source = erf.getSource(sourceID);
				double distance = source.getMinDistance(site);
				
				if (distance > magThreshFunc.getMaxX())
					continue;
				double magThresh = magThreshFunc.getInterpolatedY(distance);
				
				for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
					EqkRupture rupture = source.getRupture(rupID);
					
					if (rupture.getMag() < magThresh)
						continue;
					
					// set the EqkRup in the IMR
					imr.setEqkRupture(rupture);
					
					double[] myResults = { imr.getMean(), imr.getStdDev() };
					
					if (results[sourceID] == null)
						results[sourceID] = new double[source.getNumRuptures()][];
					results[sourceID][rupID] = myResults;
				}
			}
		}

		@Override
		public int compareTo(SiteResult o) {
			return new Integer(index).compareTo(o.index);
		}
	}

}
