package scratch.kevin.ucerf3.eal;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.LightFixedXFunc;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sra.calc.parallel.MPJ_CondLossCalc;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.FaultSystemSolutionFetcher;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.mean.TrueMeanBuilder;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

/**
 * This class will combine the expected loss for each rupture in the true mean solution to get branch
 * specific EAL values. Similar techniques could be used for a time dependent quick recalculation as well
 * 
 * TODO: add aftershock reduction?
 * TODO: add time dependence
 * @author kevin
 *
 */
public class UCERF3_EAL_Combiner {
	
	private FaultSystemSolutionFetcher fetcher;
	private Map<LogicTreeBranch, List<Integer>> mappings;
	private FaultSystemSolution trueMeanSol;
	private double[][] faultLosses;
	private DiscretizedFunc[] griddedLosses;
	
	private List<LogicTreeBranch> branches;
	private double[] faultEALs;
	private double[] griddedEALs;
	private double[] totalEALs;
	
	public UCERF3_EAL_Combiner(FaultSystemSolutionFetcher fetcher, Map<LogicTreeBranch, List<Integer>> mappings,
			FaultSystemSolution trueMeanSol, double[][] fssLosses, DiscretizedFunc[] griddedLosses)
					throws DocumentException, IOException {
		this.fetcher = fetcher;
		this.mappings = mappings;
		this.trueMeanSol = trueMeanSol;
		this.faultLosses = fssLosses;
		this.griddedLosses = griddedLosses;
		
		// get list of branches sorted by name
		branches = Lists.newArrayList(mappings.keySet());
		Collections.sort(branches);
		
		calcEALs();
	}
	
	private void calcEALs() throws DocumentException, IOException {
		DiscretizedFunc[] rupMFDs = trueMeanSol.getRupMagDists();
		
		faultEALs = new double[branches.size()];
		griddedEALs = new double[branches.size()];
		totalEALs = new double[branches.size()];
		
		System.out.println("calculating branch eals");
		for (int i=0; i<branches.size(); i++) {
			if (i % 100 == 0)
				System.out.println("Branch "+i);
			LogicTreeBranch branch = branches.get(i);
			double[] rates = fetcher.getRates(branch);
			double[] mags = fetcher.getMags(branch);
			List<Integer> meanRupIndexes = mappings.get(branch);
			
			for (int r=0; r<rates.length; r++) {
				int meanRupIndex = meanRupIndexes.get(r);
				double rate = rates[r];
				double mag = mags[r];
				if (rate == 0 || meanRupIndex < 0)
					// skip if rate=0, or if sub seismo
					continue;
//				System.out.println("Rupture "+r+"=>"+meanRupIndex);
				
				// now find the correct index in the rup mfd
				// this is also the rup index in the source
				int rupMFDIndex;
				if (faultLosses[meanRupIndex].length == 0)
					continue;
				
				DiscretizedFunc mfd = rupMFDs[meanRupIndex];
				Preconditions.checkState(faultLosses[meanRupIndex].length == mfd.getNum());
				
				if (faultLosses[meanRupIndex].length == 1) {
					rupMFDIndex = 0;
				} else {
					rupMFDIndex = mfd.getXIndex(mag);
					if (rupMFDIndex < 0) {
						// this is an insertion point, not exact match. find closest
						rupMFDIndex = -(rupMFDIndex+1);
						if (rupMFDIndex > 0 && (float)mfd.getX(rupMFDIndex-1) == (float)mag)
							rupMFDIndex = rupMFDIndex-1;
						else
							Preconditions.checkState(rupMFDIndex < mfd.getNum() && (float)mfd.getX(rupMFDIndex) == (float)mag,
								"Bad mag. Mine="+mag+". MFD=["+Joiner.on(",").join(Doubles.asList(((LightFixedXFunc)mfd).getXVals()))+"]");
					}
				}
				Preconditions.checkState((float)mag == (float)mfd.getX(rupMFDIndex));
				
				double rupLoss = faultLosses[meanRupIndex][rupMFDIndex];
				
				// TODO aftershock removal, time dependence
				double rupEAL = rupLoss * rate;
				faultEALs[i] += rupEAL;
			}
			
			// now gridded
			if (griddedLosses != null) {
				GridSourceProvider gridProv;
				if (fetcher instanceof CompoundFaultSystemSolution)
					gridProv = ((CompoundFaultSystemSolution)fetcher).loadGridSourceProviderFile(branch);
				else
					gridProv = fetcher.getSolution(branch).getGridSourceProvider();
				for (int n=0; n<gridProv.getGriddedRegion().getNodeCount(); n++) {
					DiscretizedFunc lossDist = griddedLosses[n];
					if (lossDist == null)
						continue;
//					ProbEqkSource source = gridProv.getSource(n, 1d, false, gridType);
//					if (lossDist.getNum() != source.getNumRuptures()) {
//						List<Float> fileMags = Lists.newArrayList();
//						for (double mag : lossDist.xValues())
//							fileMags.add((float)mag);
//						System.out.println("File mags: "+Joiner.on(",").join(fileMags));
//						List<Float> srcMags = Lists.newArrayList();
//						for (ProbEqkRupture rup : source)
//							srcMags.add((float)rup.getMag());
//						System.out.println("Source mags: "+Joiner.on(",").join(srcMags));
//						System.out.flush();
//					}
//					Preconditions.checkState(lossDist.getNum() == source.getNumRuptures(),
//							"Grid source rup count inconsistency. Loaded: "+lossDist.getNum()
//							+", from prov: "+source.getNumRuptures());
					// do mag lookups in floating point precision
					float[] lossMags = new float[lossDist.getNum()];
					for (int j=0; j<lossDist.getNum(); j++)
						lossMags[j] = (float)lossDist.getX(j);
					IncrementalMagFreqDist mfd = gridProv.getNodeMFD(n, AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF);
					for (int j=0; j<mfd.getNum(); j++) {
						double mag = mfd.getX(j);
						double rate = mfd.getY(j);
						if (rate == 0d)
							continue;
						int lossIndex = Arrays.binarySearch(lossMags, (float)mag);
						if (lossIndex < 0) {
							System.out.println("Mag: "+mag);
							System.out.println("Rate: "+rate);
							List<Float> fileMags = Lists.newArrayList();
							for (double fmag : lossDist.xValues())
								fileMags.add((float)fmag);
							System.out.println("File mags: "+Joiner.on(",").join(fileMags));
						}
						Preconditions.checkState(lossIndex >= 0, "Loss function doesn't have mag but we do!");
						double loss = lossDist.getY(lossIndex);
//						try {
//							loss = lossDist.getY(mag);
//						} catch (Exception e) {
//							System.out.println("Mag: "+mag);
//							List<Float> fileMags = Lists.newArrayList();
//							for (double fmag : lossDist.xValues())
//								fileMags.add((float)fmag);
//							System.out.println("File mags: "+Joiner.on(",").join(fileMags));
//							throw ExceptionUtils.asRuntimeException(e);
//						}
						if (loss == 0d)
							continue;
						// TODO aftershock removal
						double rupEAL = loss * rate;
						griddedEALs[i] += rupEAL;
					}
				}
			}
			totalEALs[i] = faultEALs[i] + griddedEALs[i];
		}
	}

	public List<LogicTreeBranch> getBranches() {
		return branches;
	}

	public double[] getFaultEALs() {
		return faultEALs;
	}

	public double[] getGriddedEALs() {
		return griddedEALs;
	}

	public double[] getTotalEALs() {
		return totalEALs;
	}

	public static void main(String[] args) throws IOException, DocumentException {
		File invSolDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File trueMeanSolFile = new File(invSolDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL_WITH_MAPPING.zip");
		File compoundSolFile = new File(invSolDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_WITH_GRIDDED.zip");
		File rupLossesFile = new File("/home/kevin/OpenSHA/UCERF3/eal/2013_10_29-eal/output_fss_index.bin");
		File rupGriddedFile = new File("/home/kevin/OpenSHA/UCERF3/eal/2013_10_29-eal/output_fss_gridded.bin");
//		File rupGriddedFile = null;
//		BackgroundRupType gridType = BackgroundRupType.CROSSHAIR;
		boolean isFSSMapped = true; // if false, then organized as erf source/rup. else, fss rup/mag
		BranchWeightProvider weightProv = new APrioriBranchWeightProvider();
		File csvFile = new File("/tmp/ucerf3_indep_fault_eals.csv");
		
		System.out.println("Loading true mean/compound");
		FaultSystemSolution trueMeanSol = FaultSystemIO.loadSol(trueMeanSolFile);
		// now load in the mappings
		Map<LogicTreeBranch, List<Integer>> mappings = TrueMeanBuilder.loadRuptureMappings(trueMeanSolFile);
		DiscretizedFunc[] rupMFDs = trueMeanSol.getRupMagDists();
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(compoundSolFile);
		
		// now load in rupture expected losses
		System.out.println("Loading losses");
		double[][] expectedLosses = MPJ_CondLossCalc.loadResults(rupLossesFile);
		if (!isFSSMapped) {
			System.out.println("Remapping losses");
//			FaultSystemSolutionERF erf = new FaultSystemSolutionERF(trueMeanSol);
//			erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
//			erf.updateForecast();
//			expectedLosses = MPJ_EAL_Rupcalc.mapResultsToFSS(erf, expectedLosses);
			double[][] mappedLosses = new double[trueMeanSol.getRupSet().getNumRuptures()][];
			int sourceCount = 0;
			for (int i=0; i<mappedLosses.length; i++) {
				double rate = trueMeanSol.getRateForRup(i);
				if (rate > 0)
					mappedLosses[i] = expectedLosses[sourceCount++];
				else
					mappedLosses[i] = new double[0];
			}
			Preconditions.checkState(sourceCount == expectedLosses.length);
			expectedLosses = mappedLosses;
		}
		DiscretizedFunc[] griddedFuncs = null;
		if (rupGriddedFile != null) {
			griddedFuncs = MPJ_CondLossCalc.loadGridSourcesFile(rupGriddedFile,
					trueMeanSol.getGridSourceProvider().getGriddedRegion());
			double totCond = 0;
			int gridNonNull = 0;
			for (DiscretizedFunc func : griddedFuncs) {
				if (func != null) {
					gridNonNull++;
					for (Point2D pt : func)
						totCond += pt.getY();
				}
			}
			System.out.println("Tot grid conditional "+totCond+" ("+gridNonNull+" non null)");
		}
		
		double trueMeanEAL = 0;
		for (int r=0; r<expectedLosses.length; r++) {
			if (expectedLosses[r] == null)
				continue;
			DiscretizedFunc mfd = rupMFDs[r];
			for (int m=0; m<expectedLosses[r].length; m++) {
				trueMeanEAL += mfd.getY(m)*expectedLosses[r][m];
			}
		}
		
		UCERF3_EAL_Combiner comb = new UCERF3_EAL_Combiner(cfss, mappings, trueMeanSol, expectedLosses, griddedFuncs);
		
		double[] eals = comb.getFaultEALs();
		double[] gridEALs = comb.getGriddedEALs();
		
		List<LogicTreeBranch> branches = comb.getBranches();
		
		double totWeights = 0d;
		for (int i=0; i<branches.size(); i++)
			totWeights += weightProv.getWeight(branches.get(i));
		System.out.println("Tot weight: "+totWeights);
		double[] branchWeights = new double[branches.size()];
		for (int i=0; i<branches.size(); i++) {
			LogicTreeBranch branch = branches.get(i);
			branchWeights[i] = weightProv.getWeight(branch)/totWeights;
		}
		
		System.out.println("'true mean fault eal'="+trueMeanEAL);
		plotDist(eals, branchWeights, "UCERF3 Fault Based Indep EAL Distribution");
		if (griddedFuncs != null)
			plotDist(gridEALs, branchWeights, "UCERF3 Gridded Indep EAL Distribution");
		double[] combEALs = comb.getTotalEALs();
		for (int i=0; i<eals.length; i++)
			combEALs[i] = eals[i]+gridEALs[i];
		plotDist(combEALs, branchWeights, "UCERF3 Total Indep EAL Distribution");
		
		CSVFile<String> csv = new CSVFile<String>(true);
		List<String> header = Lists.newArrayList("Index", "Branch Weight", "Total EAL", "Fault EAL", "Gridded EAL");
		for (Class<? extends LogicTreeBranchNode<?>> clazz : LogicTreeBranch.getLogicTreeNodeClasses())
			header.add(ClassUtils.getClassNameWithoutPackage(clazz));
		csv.addLine(header);
		for (int i=0; i<branches.size(); i++) {
			LogicTreeBranch branch = branches.get(i);
			double weight = branchWeights[i];
			List<String> line = Lists.newArrayList();
			line.add(i+"");
			line.add(weight+"");
			line.add(combEALs[i]+"");
			line.add(eals[i]+"");
			line.add(gridEALs[i]+"");
			for (LogicTreeBranchNode<?> node : branch)
				line.add(node.getShortName());
			csv.addLine(line);
		}
		csv.writeToFile(csvFile);
	}
	
	private static void plotDist(double[] eals, double[] branchWeights, String title) {
		double minEAL = StatUtils.min(eals);
		double maxEAL = StatUtils.max(eals);
		System.out.println("min="+minEAL+"\tmax="+maxEAL);
		
//		double delta = 1000000d;
		// we want about 10 bins
		double delta = (maxEAL - minEAL) / 10d;
		System.out.println("Calc delta: "+delta);
		delta = Math.pow(10, Math.round(Math.log10(delta)));
		System.out.println("Round delta: "+delta);
//		double delta = 100d;
		double funcMin = ((int)(minEAL/delta))*delta;
		double funcMax = ((int)(maxEAL/delta)+1)*delta;
		int funcNum = (int)((funcMax - funcMin)/delta);
		
		double calcMeanEAL = 0d;
		
		HistogramFunction func = new HistogramFunction(funcMin, funcNum, delta);
		for (int i=0; i<branchWeights.length; i++) {
			double eal = eals[i];
			double weight = branchWeights[i];
			func.add(eal, weight);
			calcMeanEAL += eal*weight;
		}
		System.out.println("calc mean eal="+calcMeanEAL);
		
		List<PlotElement> elems = Lists.newArrayList();
		elems.add(func);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		new GraphWindow(elems, title, chars);
	}

}
