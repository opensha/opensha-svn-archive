package scratch.kevin.ucerf3.eal;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
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
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sra.calc.parallel.MPJ_EAL_Rupcalc;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.mean.TrueMeanBuilder;
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

	public static void main(String[] args) throws IOException, DocumentException {
		File invSolDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File trueMeanSolFile = new File(invSolDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL_WITH_MAPPING.zip");
		File compoundSolFile = new File(invSolDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip");
		File rupLossesFile = new File("/home/kevin/OpenSHA/UCERF3/eal/2013_10_29-eal/output.bin");
		boolean isFSSMapped = false; // if false, then organized as erf source/rup. else, fss rup/mag
		BranchWeightProvider weightProv = new APrioriBranchWeightProvider();
		File csvFile = new File("/tmp/ucerf3_indep_fault_eals.csv");
		
		System.out.println("Loading true mean/compound");
		FaultSystemSolution trueMeanSol = FaultSystemIO.loadSol(trueMeanSolFile);
		// now load in the mappings
		Map<LogicTreeBranch, List<Integer>> mappings = TrueMeanBuilder.loadRuptureMappings(trueMeanSolFile);
		// get list of branches sorted by name
		List<LogicTreeBranch> branches = Lists.newArrayList(mappings.keySet());
		Collections.sort(branches);
		DiscretizedFunc[] rupMFDs = trueMeanSol.getRupMagDists();
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(compoundSolFile);
		
		// now load in rupture expected losses
		System.out.println("Loading losses");
		double[][] expectedLosses = MPJ_EAL_Rupcalc.loadResults(rupLossesFile);
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
		
		double trueMeanEAL = 0;
		for (int r=0; r<expectedLosses.length; r++) {
			if (expectedLosses[r] == null)
				continue;
			DiscretizedFunc mfd = rupMFDs[r];
			for (int m=0; m<expectedLosses[r].length; m++) {
				trueMeanEAL += mfd.getY(m)*expectedLosses[r][m];
			}
		}
		
		double[] eals = new double[branches.size()];
		
		System.out.println("calculating branch eals");
		for (int i=0; i<branches.size(); i++) {
			if (i % 100 == 0)
				System.out.println("Branch "+i);
			LogicTreeBranch branch = branches.get(i);
			double[] rates = cfss.getRates(branch);
			double[] mags = cfss.getMags(branch);
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
				if (expectedLosses[meanRupIndex].length == 0)
					continue;
				
				DiscretizedFunc mfd = rupMFDs[meanRupIndex];
				Preconditions.checkState(expectedLosses[meanRupIndex].length == mfd.getNum());
				
				if (expectedLosses[meanRupIndex].length == 1) {
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
				
				double rupLoss = expectedLosses[meanRupIndex][rupMFDIndex];
				
				// TODO aftershock removal, time dependence
				double rupEAL = rupLoss * rate;
				eals[i] += rupEAL;
			}
		}
		
		double minEAL = StatUtils.min(eals);
		double maxEAL = StatUtils.max(eals);
		System.out.println("min="+minEAL+"\tmax="+maxEAL);
		System.out.println("'true mean eal'="+trueMeanEAL);
		
//		double delta = 1000000d;
		double delta = 100d;
		double funcMin = ((int)(minEAL/delta))*delta;
		double funcMax = ((int)(maxEAL/delta)+1)*delta;
		int funcNum = (int)((funcMax - funcMin)/delta);
		
		double totWeights = 0d;
		for (int i=0; i<branches.size(); i++)
			totWeights += weightProv.getWeight(branches.get(i));
		System.out.println("Tot weight: "+totWeights);
		
		double calcMeanEAL = 0d;
		
		HistogramFunction func = new HistogramFunction(funcMin, funcNum, delta);
		for (int i=0; i<branches.size(); i++) {
			double eal = eals[i];
			double weight = weightProv.getWeight(branches.get(i))/totWeights;
			func.add(eal, weight);
			calcMeanEAL += eal*weight;
		}
		System.out.println("calc mean eal="+trueMeanEAL);
		
		List<PlotElement> elems = Lists.newArrayList();
		elems.add(func);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		new GraphWindow(elems, "UCERF3 Indep EAL Distribution", chars);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		List<String> header = Lists.newArrayList("Index", "Branch Weight", "EAL");
		for (Class<? extends LogicTreeBranchNode<?>> clazz : LogicTreeBranch.getLogicTreeNodeClasses())
			header.add(ClassUtils.getClassNameWithoutPackage(clazz));
		csv.addLine(header);
		for (int i=0; i<branches.size(); i++) {
			double eal = eals[i];
			LogicTreeBranch branch = branches.get(i);
			double weight = weightProv.getWeight(branch);
			weight /= totWeights;
			List<String> line = Lists.newArrayList();
			line.add(i+"");
			line.add(weight+"");
			line.add(eal+"");
			for (LogicTreeBranchNode<?> node : branch)
				line.add(node.getShortName());
			csv.addLine(line);
		}
		csv.writeToFile(csvFile);
	}

}
