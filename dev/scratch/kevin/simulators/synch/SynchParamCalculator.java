package scratch.kevin.simulators.synch;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.iden.ElementMagRangeDescription;
import org.opensha.sha.simulators.eqsim_v04.iden.RuptureIdentifier;

import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.catBuild.RandomCatalogBuilder;
import scratch.kevin.simulators.dists.RandomDistType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SynchParamCalculator {
	
	// inputs
	private MarkovChainBuilder chain;
	private int lag;
	
	// results
	private List<Double> synchs = Lists.newArrayList();
	private List<Double> probMs = Lists.newArrayList();
	private List<Double> probNs = Lists.newArrayList();
	private List<Double> probMNs = Lists.newArrayList();
	private List<Double> freqEithers = Lists.newArrayList();
	private List<IndicesKey> synch_indices = Lists.newArrayList();
	private double totEithers = 0d;
	
	private double gBar_numerator;
	private double gBar_denominator;
	private double gBar;
	
	// plot files if generated
	private File synch2DPlotFile;
	private File scatterPlotFile;
	
	private static int m = 0;
	private static int n = 1;
	
//	public static Map<IndicesKey, List<int[]>> getBinnedIndices(MarkovChainBuilder chain, int m, int n) {
//		Map<IndicesKey, List<int[]>> binnedIndices = Maps.newHashMap();
//		for (int[] indices : chain.getStateTransitionDataset().getPopulatedIndices()) {
//			int[] myInd = { indices[m], indices[n] };
//			IndicesKey key = new IndicesKey(myInd);
//			List<int[]> binned = binnedIndices.get(key);
//			if (binned == null) {
//				binned = Lists.newArrayList();
//				binnedIndices.put(key, binned);
//			}
//			binned.add(indices);
//		}
//		return binnedIndices;
//	}
	
	public SynchParamCalculator(MarkovChainBuilder chain, int m, int n, int lag) {
		this(chain.getCollapsedChain(m, n), lag);
	}
	
	public SynchParamCalculator(MarkovChainBuilder chain, int lag) {
		Preconditions.checkArgument(chain.getNDims() == 2, "Chain must be collapsed");
		this.chain = chain;
		this.lag = lag;
		
		calculate();
	}
	
	private void calculate() {
		int numSums = 0;
		int numPossibleSums = 0;
		int numSubSums = 0;
		int numSubBails = 0;
		
		boolean useIndepProbs = false;
		Map<Integer, Double> mIndepProbs = null;
		Map<Integer, Double> nIndepProbs = null;
		if (useIndepProbs) {
			mIndepProbs = calcIndepProbs(m);
			nIndepProbs = calcIndepProbs(n);
		}
		
		for (int[] fromState : chain.getStateTransitionDataset().getPopulatedIndices()) {
			PossibleStates possible = chain.getStateTransitionDataset().get(fromState);
			if (possible == null || possible.getStates() == null || possible.getTot() == 0) {
				// last state in the catalog can be a dead end if never reached earlier
				numSubBails++;
				continue;
			}
			
			double freqM = 0;
			double freqN = 0;
			double freqMN = 0;
			double freqEither = 0;
			double tot = possible.getTot();
			
			for (int[] state : possible.getStates()) {
				// frequency that we transition to state
				double freq = possible.getFrequency(state);
				if (lag == 0) {
					// simple case, no lag
					if (state[m] == 0)
						freqM += freq;
					if (state[n] == 0)
						freqN += freq;
					if (state[m] == 0 && state[n] == 0)
						freqMN += freq;
					if (state[m] == 0 || state[n] == 0)
						freqEither += freq;
				} else {
					// incorporates lag
					if (lag < 0) {
						// n precedes m
						// we want m=0, and n=abs(lag)
						double probNBefore = calcProbRupturedBefore(-lag, 1, state, fromState, chain);
						double freqNBefore = probNBefore*freq;
						Preconditions.checkState((float)freqNBefore <= (float)freq,
								"FreqNBefore > Freq!: "+freqNBefore+">"+freq);
						if (state[m] == 0) {
							freqM += freq;
							freqMN += freqNBefore;
							freqEither += freq;
						} else {
							freqEither += freqNBefore;
						}
						freqN += freqNBefore;
					} else {
						// m precedes n
						// we want n=0, and m=lag
						double probMBefore = calcProbRupturedBefore(lag, 0, state, fromState, chain);
						double freqMBefore = probMBefore*freq;
						Preconditions.checkState((float)freqMBefore <= (float)freq,
								"FreqMBefore > Freq!: "+freqMBefore+">"+freq);
						if (state[n] == 0) {
							freqN += freq;
							freqMN += freqMBefore;
							freqEither += freq;
						} else {
							freqEither += freqMBefore;
						}
						freqM += freqMBefore;
					}
				}
//				int mCheckIndex, nCheckIndex;
//				if (lag == 0) {
//					mCheckIndex = 0;
//					nCheckIndex = 0;
//				} else if (lag < 0) {
//					// n precedes m
//					// we want m=0, and n=abs(lag)
//					mCheckIndex = 0;
//					nCheckIndex = -lag;
//				} else {
//					// lag > 0
//					// m precedes n
//					// we want n=0, and m=lag
//					nCheckIndex = 0;
//					mCheckIndex = lag;
//				}
				
			}
			
			numSubSums++;
			
			// convert to probs
			freqM /= tot;
			freqN /= tot;
			freqMN /= tot;
			
			if (useIndepProbs) {
//				int mIndex = indicesKey.indices[0];
//				int nIndex = indicesKey.indices[1];
				int mIndex = 0;
				int nIndex = 1;
				if (mIndepProbs.containsKey(mIndex))
					freqM = mIndepProbs.get(mIndex);
				else
					freqM = 0;
				if (nIndepProbs.containsKey(nIndex))
					freqN = nIndepProbs.get(nIndex);
				else
					freqN = 0;
			}
			
			double synch = freqMN/(freqM*freqN);
//			double prob_state = tot / totStateCount;
			
			numPossibleSums++;
			
//			if (!Double.isInfinite(synch) && !Double.isNaN(synch)) {
//			if (freqEither > 0) {
			if (freqM > 0 && freqN > 0) {
				if (Double.isInfinite(synch) || Double.isNaN(synch))
					synch = 0d;
				totEithers += freqEither;
				numSums++;
				
				synchs.add(synch);
				freqEithers.add(freqEither);
				synch_indices.add(new IndicesKey(fromState));
				
				probMs.add(freqM);
				probNs.add(freqN);
				probMNs.add(freqMN);
			}
		}
		for (int i=0; i<synchs.size(); i++) {
			double weight = freqEithers.get(i)/totEithers;
			gBar_numerator += synchs.get(i)*weight;
			gBar_denominator += weight;
		}
		gBar = gBar_numerator/gBar_denominator;
	}
	
	private Map<Integer, Double> calcIndepProbs(int index) {
		Map<Integer, Double> freqs = Maps.newHashMap();
		Map<Integer, Double> tots = Maps.newHashMap();
		
		for (int[] indices : chain.getStateTransitionDataset().getPopulatedIndices()) {
			int myIndex = indices[index];
			double tot, freq;
			if (freqs.containsKey(myIndex)) {
				freq = freqs.get(myIndex);
				tot = tots.get(myIndex);
			} else {
				freq = 0;
				tot = 0;
			}
			PossibleStates poss = chain.getStateTransitionDataset().get(indices);
			tot += poss.getTot();
			for (int[] state : poss.getStates()) {
				if (state[index] == 0)
					freq += poss.getFrequency(state);
			}
			freqs.put(myIndex, freq);
			tots.put(myIndex, tot);
		}
		
		Map<Integer, Double> probs = Maps.newHashMap();
		
		for (Integer key : freqs.keySet())
			probs.put(key, freqs.get(key)/tots.get(key));
		return probs;
	}
	
	/**
	 * Returns the synchronization parameter, gBar in linear space
	 * @return
	 */
	public double getGBar() {
		return gBar;
	}
	
	public void generatePlots(File synchXYZDir, File synchScatterDir, String name1, String name2) throws IOException {
		double distSpacing = chain.getDistSpacing();
		
		DefaultXY_DataSet synchFunc = new DefaultXY_DataSet();
		DefaultXY_DataSet contribFunc = new DefaultXY_DataSet();
		
		double highestContrib = Double.NEGATIVE_INFINITY;
		int contribIndex = -1;
		
		EvenlyDiscrXYZ_DataSet freqXYZ = new EvenlyDiscrXYZ_DataSet(
				100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
		
		EvenlyDiscrXYZ_DataSet rXYZ = new EvenlyDiscrXYZ_DataSet(
				100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
		
		EvenlyDiscrXYZ_DataSet weightedR_XYZ = new EvenlyDiscrXYZ_DataSet(
				100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
		
		for (int x=0; x<rXYZ.getNumX(); x++) {
			for (int y=0; y<rXYZ.getNumY(); y++) {
				freqXYZ.set(x, y, Double.NaN);
				rXYZ.set(x, y, Double.NaN);
				weightedR_XYZ.set(x, y, Double.NaN);
			}
		}
		
		for (int i=0; i<synchs.size(); i++) {
			double synch = synchs.get(i);
			double weight = freqEithers.get(i)/totEithers;
			
			double rBar_without = (gBar_numerator - synch*weight)/(gBar_denominator - weight);
			double contrib = gBar - rBar_without;
			
			if (contrib > highestContrib) {
				highestContrib = contrib;
				contribIndex = i;
			}
			
			int[] indices = synch_indices.get(i).getIndices();
			if (indices[0] < freqXYZ.getNumX() && indices[1] < freqXYZ.getNumY()) {
				freqXYZ.set(indices[0], indices[1], freqEithers.get(i));
				rXYZ.set(indices[0], indices[1], synch);
				if (synch == 0)
					weightedR_XYZ.set(indices[0], indices[1], 1e-14);
				else
					weightedR_XYZ.set(indices[0], indices[1], synch*weight);
			}
			
			if (synch == 0)
				synch = 1e-14;
			synchFunc.set(synch, weight);
			if (contrib < 1e-14 || weight < 1e-14)
				continue;
			contribFunc.set(synch, contrib);
		}
		
		CPT freqCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, freqXYZ.getMaxZ());
		freqCPT.setNanColor(Color.WHITE);
		CPT rCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-1, 1);
		rXYZ.log10();
		rCPT.setNanColor(Color.WHITE);
		CPT weightedCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-3, -1);
		weightedR_XYZ.log10();
		weightedCPT.setNanColor(Color.WHITE);
		String title = name1+" vs "+name2;
		String xAxisLabel = "Years since prev "+name1;
		String yAxisLabel = "Years since prev "+name2;
		XYZPlotSpec freqSpec = new XYZPlotSpec(freqXYZ, freqCPT, title, xAxisLabel, yAxisLabel, null);
//		title = "r: freqMN/(freqM*freqN)";
		XYZPlotSpec rSpec = new XYZPlotSpec(rXYZ, rCPT, title, xAxisLabel, yAxisLabel, null);
		XYZPlotSpec rWeightedSpec = new XYZPlotSpec(weightedR_XYZ, weightedCPT, title, xAxisLabel, yAxisLabel, null);
		
		Range xyzXRange = new Range(0d, rXYZ.getMaxX()+0.5*distSpacing);
		Range xyzYRange = new Range(0d, 0.6*rXYZ.getMaxX()+0.5*distSpacing);
		List<Range> xyzXRanges = Lists.newArrayList(xyzXRange);
		List<Range> xyzYRanges = Lists.newArrayList(xyzYRange, xyzYRange, xyzYRange);
		
		double annX = xyzXRange.getUpperBound()*0.95;
		double annY = xyzYRange.getUpperBound()*0.9;
		Font font = new Font(Font.SERIF, Font.PLAIN, 18);
		XYTextAnnotation freqAnn = new XYTextAnnotation("Freq A OR B", annX, annY);
		freqAnn.setFont(font);
		freqAnn.setTextAnchor(TextAnchor.TOP_RIGHT);
		freqSpec.setPlotAnnotations(Lists.newArrayList(freqAnn));
		XYTextAnnotation rAnn = new XYTextAnnotation("Log10(r=freqMN/(freqM*freqN))", annX, annY);
		rAnn.setFont(font);
		rAnn.setTextAnchor(TextAnchor.TOP_RIGHT);
		rSpec.setPlotAnnotations(Lists.newArrayList(rAnn));
		XYTextAnnotation weightedAnn = new XYTextAnnotation("Log10(r*weight)", annX, annY);
		weightedAnn.setFont(font);
		weightedAnn.setTextAnchor(TextAnchor.TOP_RIGHT);
		rWeightedSpec.setPlotAnnotations(Lists.newArrayList(weightedAnn));
		
		if (name2.toLowerCase().contains("garlock")) {
			System.out.println("SWAPPING!!!");
			title = name2+" vs "+name1;
			// swap
			freqSpec = swapSpec(freqSpec);
			rSpec = swapSpec(rSpec);
			rWeightedSpec = swapSpec(rWeightedSpec);
		}
		
		List<XYZPlotSpec> xyzSpecs = Lists.newArrayList(rSpec, freqSpec, rWeightedSpec);
		XYZGraphPanel xyzGP = new XYZGraphPanel();
		xyzGP.drawPlot(xyzSpecs, false, false, xyzXRanges, xyzYRanges, null);
		xyzGP.getChartPanel().setSize(1000, 1500);
		synch2DPlotFile = new File(synchXYZDir, PeriodicityPlotter.getFileSafeString(name1)
				+"_"+PeriodicityPlotter.getFileSafeString(name2)+".pdf");
		xyzGP.saveAsPDF(synch2DPlotFile.getAbsolutePath());
		
		if (contribIndex >= 0) {
			System.out.println("Highest contrib of "+(float)highestContrib+" at "+synch_indices.get(contribIndex)+". r="
					+probMNs.get(contribIndex).floatValue()+"/("+probMs.get(contribIndex).floatValue()
					+"*"+probNs.get(contribIndex).floatValue()+")="
					+synchs.get(contribIndex).floatValue()+", Weight(Sij)="+(float)(freqEithers.get(contribIndex)/totEithers));
		}
		
		
		File scatterPlotFile = new File(synchScatterDir, "synch_scatter_"
				+PeriodicityPlotter.getFileSafeString(name1)+"_"
				+PeriodicityPlotter.getFileSafeString(name2));
		
		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLACK));
		title = "Synch Param "+name1+" vs "+name2+": "+gBar;
		xAxisLabel = "r: freqMN/(freqM*freqN)";
		yAxisLabel = "Weight";
		PlotSpec spec = new PlotSpec(asList(synchFunc), chars, title, xAxisLabel, yAxisLabel);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(14);
		gp.setAxisLabelFontSize(16);
		gp.setPlotLabelFontSize(18);

		Range xRange = new Range(1e-15, 1e3);
		Range yRange = new Range(1e-6, 1e-0);
		gp.drawGraphPanel(spec, true, true, xRange, yRange);
		
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(scatterPlotFile.getAbsolutePath()+".png");
		gp.saveAsPDF(scatterPlotFile.getAbsolutePath()+".pdf");
		this.scatterPlotFile = new File(scatterPlotFile.getAbsolutePath()+".pdf");
		
		File contribScatterFile = new File(synchScatterDir, "synch_scatter_contrib_"
				+PeriodicityPlotter.getFileSafeString(name1)+"_"
				+PeriodicityPlotter.getFileSafeString(name2));
		title = "Synch Param "+name1+" vs "+name2+": "+gBar;
		xAxisLabel = "r: freqMN/(freqM*freqN)";
		yAxisLabel = "Contribution: rBar - rBar(without point)";
		spec = new PlotSpec(asList(contribFunc), chars, title, xAxisLabel, yAxisLabel);
		
		gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(14);
		gp.setAxisLabelFontSize(16);
		gp.setPlotLabelFontSize(18);
		
		yRange = new Range(1e-6, 1e1);
		gp.drawGraphPanel(spec, true, true, xRange, yRange);
		
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(contribScatterFile.getAbsolutePath()+".png");
		
		if (name1.contains("Mojave") && name2.contains("Coachella")) {
			// write poster images
			
			// G
			xyzGP = new XYZGraphPanel();
			xyzXRange = xyzYRange;
			rSpec.setTitle("Gain Factor");
			xyzGP.drawPlot(rSpec, false, false, xyzXRange, xyzYRange);
			xyzGP.getChartPanel().setSize(1000, 1000);
			File gPlotFile = new File(synchXYZDir, "gain_"+PeriodicityPlotter.getFileSafeString(name1)
					+"_"+PeriodicityPlotter.getFileSafeString(name2)+".pdf");
			xyzGP.saveAsPDF(gPlotFile.getAbsolutePath());
			
			// Occupancy
			EvenlyDiscrXYZ_DataSet occFreqXYZ = new EvenlyDiscrXYZ_DataSet(
					100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
			SparseNDimensionalHashDataset<Double> totalStatesDataset = chain.getTotalStatesDataset();
			for (int[] indices : totalStatesDataset.getPopulatedIndices()) {
				if (indices[m] < occFreqXYZ.getNumX() && indices[n] < occFreqXYZ.getNumY()) {
					Double val = totalStatesDataset.get(indices);
					if (val != null && val > 0)
						occFreqXYZ.set(indices[m], indices[n], occFreqXYZ.get(indices[m], indices[n])+val);
				}
			}
			for (int x=0; x<occFreqXYZ.getNumX(); x++)
				for (int y=0; y<occFreqXYZ.getNumY(); y++)
					if (occFreqXYZ.get(x, y) == 0)
						occFreqXYZ.set(x, y, Double.NaN);
			
			CPT occCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, occFreqXYZ.getMaxZ());
			occCPT.setNanColor(Color.WHITE);
			
			XYZPlotSpec occSpec = new XYZPlotSpec(occFreqXYZ, occCPT, "State Occupancy Frequency",
					rSpec.getXAxisLabel(), rSpec.getYAxisLabel(), null);
			
			xyzGP = new XYZGraphPanel();
			xyzXRange = xyzYRange;
			xyzGP.drawPlot(occSpec, false, false, xyzXRange, xyzYRange);
			xyzGP.getChartPanel().setSize(1000, 1000);
			File freqPlotFile = new File(synchXYZDir, "freq_"+PeriodicityPlotter.getFileSafeString(name1)
					+"_"+PeriodicityPlotter.getFileSafeString(name2)+".pdf");
			xyzGP.saveAsPDF(freqPlotFile.getAbsolutePath());
			
			
		}
	}
	
	public File getSynch2DPlotFile() {
		return synch2DPlotFile;
	}

	public File getScatterPlotFile() {
		return scatterPlotFile;
	}
	
	public static double[][] writeSynchParamsStdDev(
			File stdDevCSV, List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens,
			int lag, int numTrials, double distSpacing) throws IOException {
		int nDims = rupIdens.size();
		
		double[][][] gBars = new double[nDims][nDims][numTrials];
		RandomDistType dist = RandomDistType.ACTUAL;
		
		for (int t=0; t<numTrials; t++) {
			List<EQSIM_Event> randEvents = RandomCatalogBuilder.getRandomResampledCatalog(events, rupIdens, dist, true);
			
			List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
			for (RuptureIdentifier rupIden : rupIdens)
				matchesLists.add(rupIden.getMatches(randEvents));
			
			MarkovChainBuilder chain = new MarkovChainBuilder(distSpacing, randEvents, matchesLists);
			
			for (int m=0; m<nDims; m++) {
				for (int n=0; n<nDims; n++) {
//					Map<IndicesKey, List<int[]>> binnedIndices = getBinnedIndices(chain, m, n);
					
					SynchParamCalculator calc = new SynchParamCalculator(chain, m, n, lag);
					
					gBars[m][n][t] = calc.getGBar();
				}
			}
		}
		
		double[][] stdDevs = new double[nDims][nDims];
		double[][] means = new double[nDims][nDims];
		for (int m=0; m<nDims; m++) {
			for (int n=0; n<nDims; n++) {
				double[] lnVals = new double[numTrials];
				for (int t=0; t<numTrials; t++)
					lnVals[t] = Math.log(gBars[m][n][t]);
				double mean = StatUtils.mean(lnVals);
				double var = StatUtils.variance(lnVals, mean);
				stdDevs[m][n] = Math.sqrt(var);
				means[m][n] = mean;
			}
		}
		
		List<String> header = Lists.newArrayList("");
		for (RuptureIdentifier iden : rupIdens)
			header.add(iden.getName());
		
		CSVFile<String> csv = new CSVFile<String>(false);
		
		csv.addLine("Std Dev of "+numTrials+" rand realizations (in Ln space)");
		addTableToCSV(csv, header, stdDevs, true);
		csv.addLine("");
		csv.addLine("");
		csv.addLine("Mean of "+numTrials+" rand realizations (in Ln space)");
		addTableToCSV(csv, header, means, true);
		csv.addLine("");
		csv.addLine("");
		csv.addLine("Print ready +/- (in Ln space)");
		csv.addLine(header);
		List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
		for (RuptureIdentifier rupIden : rupIdens)
			matchesLists.add(rupIden.getMatches(events));
		MarkovChainBuilder chain = new MarkovChainBuilder(distSpacing, events, matchesLists);
		
		DecimalFormat df = new DecimalFormat("0.00");
		for (int i=0; i<nDims; i++) {
			List<String> line = Lists.newArrayList();
			
			line.add(header.get(i+1));
			for (int j=0; j<nDims; j++) {
				if (i == j)
					line.add("");
				else {
					double stdDev = stdDevs[i][j];
					double mean = Math.log(new SynchParamCalculator(chain, i, j, lag).getGBar());
					line.add(df.format(mean)+" ± "+df.format(stdDev));
				}
			}
			
			csv.addLine(line);
		}
		
		csv.writeToFile(stdDevCSV);
		
		return stdDevs;
	}
	
	private static void addTableToCSV(CSVFile<String> csv, List<String> header, double[][] table, boolean skipIdentities) {
		int nDims = table.length;
		csv.addLine(header);
		for (int i=0; i<nDims; i++) {
			List<String> line = Lists.newArrayList();
			
			Preconditions.checkState(table[i].length == nDims, "table not square!");
			
			line.add(header.get(i+1));
			for (int j=0; j<nDims; j++) {
				if (skipIdentities && i == j)
					line.add("");
				else
					line.add(table[i][j]+"");
			}
			
			csv.addLine(line);
		}
	}

	public static void writeSynchParamsTable(File file, List<RuptureIdentifier> idens, MarkovChainBuilder chain) throws IOException {
			int nDims = chain.getNDims();
			
			int lagMax = 20;
			int lags = lagMax*2+1;
			
			double[][][] params = new double[nDims][nDims][lags];
			
	//		double totStateCount = 0;
	//		for (int[] indices : totalStatesDataset.getPopulatedIndices())
	//			totStateCount += totalStatesDataset.get(indices);
			
			List<PlotSpec> lagSpecs = Lists.newArrayList();
			List<PlotCurveCharacterstics> lagChars = Lists.newArrayList(
					new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			String lagTitle = "Synchronization Lag Functions";
			String lagXAxisLabel = "Lag (years)";
			String lagYAxisLabel = "Ln(Synchronization)";
	
			double lagFuncMin = -lagMax*chain.getDistSpacing();
			
			Range lagXRange = new Range(lagFuncMin, -lagFuncMin);
			Range lagYRange = new Range(-2, 2);
			
			List<File> synch2DPDFs = Lists.newArrayList();
			File synchXYZDir = new File(file.getParentFile(), "synch_xyz_param_plots");
			if (!synchXYZDir.exists())
				synchXYZDir.mkdir();
			
			List<File> synchScatterPDFs = Lists.newArrayList();
			File synchScatterDir = new File(file.getParentFile(), "synch_scatter_plots");
			if (!synchScatterDir.exists())
				synchScatterDir.mkdir();
			
			for (int m=0; m<nDims; m++) {
				// TODO start at m+1?
				for (int n=m; n<nDims; n++) {
					// first bin by only the indices we care about
//					Map<IndicesKey, List<int[]>> binnedIndices = getBinnedIndices(chain, m, n);
					
					String name1 = idens.get(m).getName();
					String name2 = idens.get(n).getName();
					
					int lagIndex = 0;
	//				EvenlyDiscretizedFunc synchLagFunc = new EvenlyDiscretizedFunc((double)-lagMax, lags, 1d);
					EvenlyDiscretizedFunc synchLagFunc = new EvenlyDiscretizedFunc(lagFuncMin, lags, chain.getDistSpacing());
					
					for (int lag=-lagMax; lag<=lagMax; lag++) {
						SynchParamCalculator calc = new SynchParamCalculator(chain, m, n, lag);
						
						double gBar = calc.getGBar();
						
						params[m][n][lagIndex] = gBar;
						params[n][m][lagIndex] = gBar;
						
						synchLagFunc.set(lagIndex, gBar);
						lagIndex++;
						
						if (lag != 0)
							continue;
						
						System.out.println(name1+" vs "+name2+": "+gBar);
	//					System.out.println(numerator+"/"+denominator+" = "+params[n][m][lagIndex-1]);
	//					System.out.println("Sums: "+numSums+"/"+numPossibleSums
	//							+" ("+numSubSums+" sub, "+numSubBails+" bails)");
						
						if (m == n)
							continue;
						
						calc.generatePlots(synchXYZDir, synchScatterDir, name1, name2);
						
						synch2DPDFs.add(calc.getSynch2DPlotFile());
						synchScatterPDFs.add(calc.getScatterPlotFile());
					}
					if (m == n)
						continue;
					
					// lag plot spec
	//				String title = "Synch Param "+name1+" vs "+name2+" ("+nDims+"D): "+params[m][n];
					EvenlyDiscretizedFunc lnSynchFunch = new EvenlyDiscretizedFunc(
							synchLagFunc.getMinX(), synchLagFunc.getNum(), synchLagFunc.getDelta());
					for (int i=0; i<synchLagFunc.getNum(); i++)
						lnSynchFunch.set(i, Math.log(synchLagFunc.getY(i)));
					PlotSpec spec = new PlotSpec(asList(lnSynchFunch), lagChars, lagTitle, lagXAxisLabel, lagYAxisLabel);
					double annY = lagYRange.getLowerBound()*0.95;
					double annX = lagXRange.getLowerBound()*0.9;
					Font font = new Font(Font.SERIF, Font.PLAIN, 14);
					XYTextAnnotation leftAnn = new XYTextAnnotation(name2+" BEFORE", annX, annY);
					leftAnn.setFont(font);
					leftAnn.setTextAnchor(TextAnchor.BOTTOM_LEFT);
					XYTextAnnotation rightAnn = new XYTextAnnotation(name2+" AFTER", -annX, annY);
					rightAnn.setFont(font);
					rightAnn.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
					XYTextAnnotation centerAnn = new XYTextAnnotation(name1+" at t=0", 0d, annY*0.75);
					centerAnn.setFont(font);
					centerAnn.setTextAnchor(TextAnchor.BOTTOM_CENTER);
					List<XYTextAnnotation> annotations = Lists.newArrayList(leftAnn, rightAnn, centerAnn);
					spec.setPlotAnnotations(annotations);
					lagSpecs.add(spec);
				}
			}
			
			List<List<PlotSpec>> lagSpecPages = Lists.newArrayList();
			lagSpecPages.add(new ArrayList<PlotSpec>());
			int specsToBin = 4;
			for (int i=0; i<lagSpecs.size(); i++) {
				PlotSpec spec = lagSpecs.get(i);
				List<PlotSpec> specs = lagSpecPages.get(lagSpecPages.size()-1);
				if (specs.size() == specsToBin) {
					specs = Lists.newArrayList();
					lagSpecPages.add(specs);
				}
				specs.add(spec);
			}
			
			File synchLagDir = new File(file.getParentFile(), "synch_lag");
			if (!synchLagDir.exists())
				synchLagDir.mkdir();
			List<File> synchLagFiles = Lists.newArrayList();
			EvenlyDiscretizedFunc blankFunc = new EvenlyDiscretizedFunc(lagFuncMin, lags, chain.getDistSpacing());
			List<PlotCurveCharacterstics> lagBlankChars = Lists.newArrayList(
					new PlotCurveCharacterstics(PlotLineType.SOLID, 0f, Color.BLACK));
			List<Range> lagXRanges = Lists.newArrayList(lagXRange);
			List<Range> lagYRanges = Lists.newArrayList();
			for (int i=0; i<specsToBin; i++)
				lagYRanges.add(lagYRange);
			for (int i=0; i<lagSpecPages.size(); i++) {
				List<PlotSpec> lagSpecPage = lagSpecPages.get(i);
				while (lagSpecPage.size() < specsToBin) {
					// add blank plots
					lagSpecPage.add(new PlotSpec(asList(blankFunc),
							lagBlankChars, lagTitle, lagXAxisLabel, lagYAxisLabel));
				}
				
				File synchLagFile = new File(synchLagDir, "synch_lag_page"+i+".pdf");
				
				HeadlessGraphPanel gp = new HeadlessGraphPanel();
				gp.setBackgroundColor(Color.WHITE);
				gp.setTickLabelFontSize(14);
				gp.setAxisLabelFontSize(16);
				gp.setPlotLabelFontSize(18);
				
				gp.setCombinedOnYAxis(false);
	
				gp.drawGraphPanel(lagSpecPage, false, false, lagXRanges, lagYRanges);
				
				// 8.5x11
				gp.getCartPanel().setSize(850, 1100);
				gp.saveAsPDF(synchLagFile.getAbsolutePath());
				synchLagFiles.add(synchLagFile);
			}
			if (!synchLagFiles.isEmpty())
				PeriodicityPlotter.combinePDFs(synchLagFiles, new File(synchLagDir, "synch_lags.pdf"));
			if (!synch2DPDFs.isEmpty())
				PeriodicityPlotter.combinePDFs(synch2DPDFs, new File(synchXYZDir, "synch_xyzs.pdf"));
			if (!synchScatterPDFs.isEmpty())
				PeriodicityPlotter.combinePDFs(synchScatterPDFs, new File(synchScatterDir, "synch_scatters.pdf"));
			
			CSVFile<String> csv = new CSVFile<String>(false);
			
			List<String> header = Lists.newArrayList("");
			for (RuptureIdentifier iden : idens)
				header.add(iden.getName());
			
			int lag0Index = lagMax;
			
			for (int type=0; type<3; type++) {
				double[][] myParams;
				switch (type) {
				case 0:
					csv.addLine("Linear");
					myParams = new double[nDims][nDims];
					for (int m=0; m<nDims; m++)
						for (int n=0; n<nDims; n++)
							myParams[m][n] = params[m][n][lag0Index];
					break;
				case 1:
					csv.addLine("Log10");
					myParams = new double[nDims][nDims];
					for (int m=0; m<nDims; m++)
						for (int n=0; n<nDims; n++)
							myParams[m][n] = Math.log10(params[m][n][lag0Index]);
					break;
				case 2:
					csv.addLine("Ln");
					myParams = new double[nDims][nDims];
					for (int m=0; m<nDims; m++)
						for (int n=0; n<nDims; n++)
							myParams[m][n] = Math.log(params[m][n][lag0Index]);
					break;
	
				default:
					throw new IllegalStateException();
				}
				addTableToCSV(csv, header, myParams, true);
				csv.addLine("");
				csv.addLine("");
			}
			
			csv.writeToFile(file);
		}

	private static XYZPlotSpec swapSpec(XYZPlotSpec spec) {
		EvenlyDiscrXYZ_DataSet orig = (EvenlyDiscrXYZ_DataSet) spec.getXYZ_Data();
		EvenlyDiscrXYZ_DataSet swapped = new EvenlyDiscrXYZ_DataSet(
				orig.getNumX(), orig.getNumY(), orig.getMinX(), orig.getMinY(), orig.getGridSpacingX(), orig.getGridSpacingY());
		for (int x=0; x<orig.getNumX(); x++)
			for (int y=0; y<orig.getNumY(); y++)
				swapped.set(y, x, orig.get(x, y));
		
		XYZPlotSpec swappedSpec = new XYZPlotSpec(swapped, spec.getCPT(), spec.getTitle(),
				spec.getYAxisLabel(), spec.getXAxisLabel(), spec.getZAxisLabel());
		swappedSpec.setPlotAnnotations(spec.getPlotAnnotations());
		return swappedSpec;
	}
	
	private static List<PlotElement> asList(PlotElement... elems) {
		return Lists.newArrayList(elems);
	}
	
	private static double calcProbRupturedBefore(int numStatesBefore, int index, int[] transDestSate, int[] transFromState, MarkovChainBuilder chain) {
		int[] toState;
		if (index == 0)
			toState = new int[] {0, -1};
		else if (index == 1)
			toState = new int[] {-1, 0};
		else
			throw new IllegalStateException("Index must be 0 or 1");
		
		double prob;
		if (transFromState == null)
			prob = chain.getActualTransPathsProbBetweenStates(transDestSate, toState, -numStatesBefore);
		else
			prob = chain.getActualTransPathsProbBetweenStates(transDestSate, toState, -numStatesBefore, transFromState);
//		double prob = (double)count/timesInState;
//		Preconditions.checkState(prob <= 1d, "Bad prob: "+prob+", count="+count+", times="+timesInState);
		return prob;
	}
	
	private static final String getPathStr(List<int[]> path, int[] start) {
		String str = "PATH: ["+start[0]+","+start[1]+"]";
		for (int[] elem : path)
			str += " ["+elem[0]+","+elem[1]+"]";
		return str;
	}
	
	public static void main(String[] args) throws IOException {
		double minMag = 7;
		double maxMag = 10d;
		
		int[] include_elems = {
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID,
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID,
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID,
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID,
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID,
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID
				};
		boolean gen_2d_corr_pdfs = false;
		
		RandomDistType origScrambleDist = null;
//		RandomDistType origScrambleDist = RandomDistType.ACTUAL;
		
		RandomDistType randDistType = RandomDistType.STATE_BASED;
		
		File writeDir = new File("/tmp");
		if (!writeDir.exists())
			writeDir.mkdir();
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<Color> colors = Lists.newArrayList();
		
		SimAnalysisCatLoader.loadElemMagIdens(include_elems, rupIdens, colors, minMag, maxMag);
		
		int[] all_elems = {
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID,
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID,
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID,
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID,
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID,
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID
				};
		List<RuptureIdentifier> allIdens = Lists.newArrayList();
		SimAnalysisCatLoader.loadElemMagIdens(all_elems, allIdens, null, minMag, maxMag);
		
		List<EQSIM_Event> events = new SimAnalysisCatLoader(true, allIdens).getEvents();
		
		List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
		for (int i=0; i<rupIdens.size(); i++)
			matchesLists.add(rupIdens.get(i).getMatches(events));
		
		double distSpacing = 10d;
		
		// generate Markov Chain
		MarkovChainBuilder chain = new MarkovChainBuilder(distSpacing, events, matchesLists);
		
		// tests
//		MarkovChainBuilder collapsed = chain.getCollapsedChain(4, 3);
//		int numStates = 3;
//		int[] fromState = {0, 0};
//		int[] toState = {numStates, numStates};
//		System.out.println("Paths from [0,0] to [3,3]:");
//		for (List<int[]> path : collapsed.getPathsBetweenStates(fromState, toState, numStates)) {
//			String str = getPathStr(path, fromState);
//			System.out.println("\t"+str);
//		}
//		System.out.println("Paths from [0,0] to [x,3]:");
//		for (List<int[]> path : collapsed.getPathsBetweenStates(fromState, new int[] {-1,numStates}, numStates)) {
//			String str = getPathStr(path, fromState);
//			System.out.println("\t"+str);
//		}
//		double tot = collapsed.getStateTransitionDataset().get(toState).tot;
//		double freqMBefore = calcFreqRupturedBefore(numStates+10, 0, toState, collapsed);
//		double freqNBefore = calcFreqRupturedBefore(numStates+10, 1, toState, collapsed);
//		System.out.println("Freq M before: "+freqMBefore+"/"+tot);
//		System.out.println("Freq N before: "+freqNBefore+"/"+tot);
//		System.exit(0);
		
		// write synch CSV
		File synchCSVFile = new File(writeDir, "synch_params.csv");
		writeSynchParamsTable(synchCSVFile, rupIdens, chain);
		
		// now write std devs
//		File stdDevCSVFile = new File(writeDir, "synch_params_std_devs.csv");
//		writeSynchParamsStdDev(stdDevCSVFile, events, rupIdens, 0, 50, distSpacing);
	}

}
