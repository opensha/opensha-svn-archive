package scratch.kevin.simulators.synch;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.plot.XYPlot;
import org.opensha.commons.data.Named;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.GraphPanel;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotPreferences;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.iden.RuptureIdentifier;

import scratch.kevin.markov.EmpiricalMarkovChain;
import scratch.kevin.markov.MarkovChain;
import scratch.kevin.markov.OccupancyBasedMarkovChain2D;
import scratch.kevin.markov.PossibleStates;
import scratch.kevin.simulators.MarkovChainBuilder;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class StateSpacePlotter {
	
	private File outputDir;
	private List<? extends Named> names;
	private double distSpacing;
	
	private MarkovChain chain;
	
	private int nDims;
	
	private double fractToInclude = 0.99;
	
	private List<Integer> minBinsList;
	
	public StateSpacePlotter(MarkovChain chain, List<? extends Named> names, File outputDir) {
		this.chain = chain;
		this.names = names;
		this.outputDir = outputDir;
		this.distSpacing = chain.getDistSpacing();
		
		this.nDims = chain.getNDims();
		Preconditions.checkArgument(nDims > 1, "must have at least 2D chain");
		Preconditions.checkArgument(names.size() == nDims);
		
		minBinsList = Lists.newArrayList();
		for (int i=0; i<nDims; i++)
			minBinsList.add(getNBinsForFract(chain, i, fractToInclude));
	}
	
	private int getNumBins(int i, int j) {
		return (int)Math.max(minBinsList.get(i), minBinsList.get(j));
	}
	
	private EvenlyDiscrXYZ_DataSet buildXYZ(int i, int j) {
		// calculate num bins
		int numBins = getNumBins(i, j);
		
		return new EvenlyDiscrXYZ_DataSet(numBins, numBins, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
	}
	
	public void plotOccupancies() throws IOException {
		for (int i=0; i<nDims; i++) {
			String name1 = names.get(i).getName();
			for (int j=i+1; j<nDims; j++) {
				String name2 = names.get(j).getName();
				
				EvenlyDiscrXYZ_DataSet xyz = getOccupancy(i, j);
				
				CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, xyz.getMaxZ());
				
				plot2D(xyz, cpt, name1, name2, "Occupancy", "occ", true);
			}
		}
	}
	
	public EvenlyDiscrXYZ_DataSet getOccupancy(int i, int j) {
		EvenlyDiscrXYZ_DataSet xyz = buildXYZ(i, j);
		
		PossibleStates occupancy = chain.getOccupancy();
		double sumZ = 0d;
		for (int[] state : occupancy.getStates()) {
			int xInd = state[i];
			int yInd = state[j];
			
			if (xInd >= xyz.getNumX() || yInd >= xyz.getNumY())
				continue;
			
			double freq = occupancy.getFrequency(state);
			
			xyz.set(xInd, yInd, xyz.get(xInd, yInd)+freq);
			sumZ += freq;
		}
		
		xyz.scale(1d/sumZ);
		
		return xyz;
	}
	
	public void plotProb(MarkovProb prob) throws IOException {
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
		cpt.setNanColor(Color.WHITE);
		
		for (int i=0; i<nDims; i++) {
			String name1 = names.get(i).getName();
			for (int j=i+1; j<nDims; j++) {
				String name2 = names.get(j).getName();
				
				EvenlyDiscrXYZ_DataSet xyz = getMarkovXYZ(prob, i, j);
				
				plot2D(xyz, cpt, name1, name2, prob.title, prob.prefix, false);
			}
		}
	}
	
	public void plotProbEither() throws IOException {
		plotProb(MarkovProb.EITHER);
	}
	
	public enum MarkovProb {
		E1("P(E1)", "prob_e1"),
		E2("P(E2)", "prob_e2"),
		EITHER("Prob Either", "prob_either"),
		BOTH("Prob Both", "prob_both"),
		NONE("Prob None", "prob_none");
		
		private String title;
		private String prefix;
		
		private MarkovProb(String title, String prefix) {
			this.title = title;
			this.prefix = prefix;
		}
	}
	
	private EvenlyDiscrXYZ_DataSet getMarkovXYZ(MarkovProb type, int i, int j) {
		MarkovChain collapsed = chain.getCollapsedChain(i, j);
//		collapsed = new OccupancyBasedMarkovChain2D(chain.getDistSpacing(), collapsed.getOccupancy());
		
		EvenlyDiscrXYZ_DataSet xyz = buildXYZ(i, j);
		
		for (int xInd=0; xInd<xyz.getNumX(); xInd++) {
			for (int yInd=0; yInd<xyz.getNumY(); yInd++) {
				PossibleStates possible = collapsed.getDestinationStates(new int[] {xInd,yInd});
				double prob;
				if (possible == null) {
					prob = Double.NaN;
				} else {
					double tot = 0;
					double numE1=0, numE2=0, numBoth=0, numEither=0;
					for (int[] dest : possible.getStates()) {
						double freq = possible.getFrequency(dest);
						if (dest[0] == 0)
							numE1 += freq;
						if (dest[1] == 0)
							numE2 += freq;
						if (dest[0] == 0 && dest[1] == 0)
							numBoth += freq;
						if (dest[0] == 0 || dest[1] == 0)
							numEither += freq;
						tot += freq;
					}
					switch (type) {
					case E1:
						prob = numE1/tot;
						break;
					case E2:
						prob = numE2/tot;
						break;
					case EITHER:
						prob = numEither/tot;
						break;
					case BOTH:
						prob = numBoth/tot;
						break;
					case NONE:
						prob = (tot-numEither)/tot;
						break;

					default:
						throw new IllegalStateException("unknown type: "+type);
					}
				}
				
				xyz.set(xInd, yInd, prob);
			}
		}
		
		return xyz;
	}
	
	public void plotDiagonals(double occThreshold) throws IOException {
		for (int i=0; i<nDims; i++) {
			String name1 = names.get(i).getName();
			for (int j=i+1; j<nDims; j++) {
				String name2 = names.get(j).getName();
				
				MarkovChain collapsed = chain.getCollapsedChain(i, j);
				PossibleStates occupancy = collapsed.getOccupancy();
				
				// TODO remove test
				// make it independent
//				PossibleStates marginalX = occupancy.getMarginal(0);
//				PossibleStates marginalY = occupancy.getMarginal(1);
//				PossibleStates indepOcc = new PossibleStates(null);
//				for (int[] state : occupancy.getStates()) {
//					double freqX = marginalX.getFrequency(new int[] {state[0]});
//					double freqY = marginalY.getFrequency(new int[] {state[1]});
//					indepOcc.add(state, freqX*freqY);
//				}
//				occupancy = indepOcc;
				
				int numBins = getNumBins(i, j);
				
				List<EvenlyDiscretizedFunc> diags = Lists.newArrayList();
				List<int[]> startStates = Lists.newArrayList();
				
				// first go down the y axis
				for (int yInd=numBins; --yInd>=0;)
					startStates.add(new int[] {0, yInd});
				// now go along the x axis, skipping zero (already got it)
				for (int xInd=1; xInd<numBins; xInd++)
					startStates.add(new int[] {xInd, 0});
				
				Preconditions.checkState(startStates.size() == numBins*2-1);
				
				double totOcc = occupancy.getTot();
				
				double maxStartOcc = 0d;
				for (int[] startState : startStates) {
					EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0.5*distSpacing, numBins, distSpacing);
					for (int k=0; k<numBins; k++) {
						int xInd = startState[0]+k;
						int yInd = startState[1]+k;
						double freq = occupancy.getFrequency(new int[] {xInd, yInd});
						if (freq == 0d)
							break;
						func.set(k, freq);
					}
					if (func.getY(0) > maxStartOcc)
						maxStartOcc = func.getY(0);
					float occPercent = (float)(100d*func.calcSumOfY_Vals()/totOcc);
					func.setName("Start: ["+startState[0]+","+startState[1]+"]. "+occPercent+" % Of Occ");
					diags.add(func);
				}
				
				int centerIndex = numBins-1;
				int[] centerState = startStates.get(centerIndex);
				Preconditions.checkState(centerState[0] == 0 && centerState[1] == 0,
						"Bad center index: ["+centerState[0]+","+centerState[1]+"]");
				int numPerSideToInclude = 0;
				for (int k=0; k<diags.size(); k++) {
					EvenlyDiscretizedFunc func = diags.get(k);
					double fractOfOcc = func.calcSumOfY_Vals()/totOcc;
					if (fractOfOcc >= occThreshold) {
						int dist = k-centerIndex;
						if (dist < 0)
							dist = -dist;
						if (dist > numPerSideToInclude)
							numPerSideToInclude = dist;
					}
				}
				if (numPerSideToInclude < 8)
					numPerSideToInclude = 8;
				
				List<DiscretizedFunc> funcs = Lists.newArrayList();
				List<PlotCurveCharacterstics> chars = Lists.newArrayList();
				int expectedNum = numPerSideToInclude*2+1;
				CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, expectedNum);
				for (int k=centerIndex-numPerSideToInclude; k<=centerIndex+numPerSideToInclude; k++) {
					EvenlyDiscretizedFunc func = diags.get(k).deepClone();
					func.scale(1d/func.getY(0));
					Color c;
					if (k == centerIndex)
						c = Color.BLACK;
					else
						c = cpt.getColor(funcs.size());
					funcs.add(func);
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, c));
				}
				Preconditions.checkState(funcs.size() == expectedNum);
				
				PlotSpec spec = new PlotSpec(funcs, chars, name1+" vs "+name2, "Time (years)", "Scaled Occ Diags");
				plot1D(spec, name1, name2, "diag_scaled");
				
				// now do a stacked version
				cpt = cpt.rescale(0, maxStartOcc);
				funcs = Lists.newArrayList();
				chars = Lists.newArrayList();
				
				for (int k=centerIndex-numPerSideToInclude; k<=centerIndex+numPerSideToInclude; k++) {
					EvenlyDiscretizedFunc func = diags.get(k).deepClone();
					double origStart = func.getY(0);
					func.scale(1d/func.getY(0));
					double add = k - centerIndex - 1;
					for (int l=0; l<func.getNum(); l++)
						func.add(l, add);
					Color c;
//					if (k == centerIndex)
//						c = Color.BLACK;
//					else
						c = cpt.getColor((float)origStart);
					funcs.add(func);
					chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, c));
				}
				Preconditions.checkState(funcs.size() == expectedNum);
				
				spec = new PlotSpec(funcs, chars, name1+" vs "+name2, "Time (years)", "Stacked Occ Diags");
				plot1D(spec, name1, name2, "diag_stacked");
			}
		}
	}
	
	private void plot2D(EvenlyDiscrXYZ_DataSet xyz, CPT cpt, String name1, String name2,
			String zLabel, String filePrefix, boolean marginals) throws IOException {
		plot2D(xyz, cpt, name1, name2, zLabel, filePrefix, marginals, outputDir);
	}
	
	public static void plot2D(EvenlyDiscrXYZ_DataSet xyz, CPT cpt, String name1, String name2,
			String zLabel, String filePrefix, boolean marginals, File outputDir) throws IOException {
		XYZPlotSpec xyzSpec = new XYZPlotSpec(xyz, cpt, name1+" "+name2+" "+zLabel,
				name1+" OI", name2+" OI", zLabel);
//		GraphWindow gw = new GraphWindow(marginal1);
//		gw = new GraphWindow(marginal2);
		
		List<XYPlot> extraPlots = null;
		List<Integer> weights = null;
		
		int width = 600;
		int height = 680;
		
		if (marginals) {
			List<DiscretizedFunc> marginals1 = Lists.newArrayList();
			List<DiscretizedFunc> marginals2 = Lists.newArrayList();
			EvenlyDiscretizedFunc margeFunc1 = xyz.calcMarginalXDist();
			EvenlyDiscretizedFunc margeFunc2 = xyz.calcMarginalYDist();
			marginals1.add(margeFunc1);
			marginals2.add(margeFunc2);
			List<PlotCurveCharacterstics> chars = Lists.newArrayList(
					new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			PlotSpec marginal1 = new PlotSpec(marginals1, chars, name1+" Marginal "+zLabel, "Years", zLabel);
			PlotSpec marginal2 = new PlotSpec(marginals2, chars, name2+" Marginal "+zLabel, "Years", zLabel);
			GraphPanel margGP = new GraphPanel(PlotPreferences.getDefault());
			margGP.drawGraphPanel(marginal1, false, false);
			extraPlots = Lists.newArrayList(margGP.getPlot());
			margGP.drawGraphPanel(marginal2, false, false);
			extraPlots.add(margGP.getPlot());
			weights = Lists.newArrayList(4,1,1);
			
			height = 950;
		}
		
		XYZGraphPanel panel = new XYZGraphPanel();
		panel.drawPlot(Lists.newArrayList(xyzSpec), false, false, null, null,
				extraPlots, weights);
		
		if (outputDir == null) {
			// display it
			XYZPlotWindow window = new XYZPlotWindow(panel);
			window.setSize(width, height);
		} else {
			// write plot
			panel.getChartPanel().setSize(width, height);
			File out = new File(outputDir, filePrefix+"_"+PeriodicityPlotter.getFileSafeString(name1)
					+"_"+PeriodicityPlotter.getFileSafeString(name2));
			panel.saveAsPNG(out.getAbsolutePath()+".png");
			panel.saveAsPDF(out.getAbsolutePath()+".pdf");
		}
	}
	
	private static int getNBinsForFract(MarkovChain chain, int index, double fractToInclude) {
		List<Integer> indexes = Lists.newArrayList();
		PossibleStates marginal = chain.getOccupancy().getMarginal(index);
		double totOcc = marginal.getTot();
		double scalar = 1d;
		if (totOcc < 1000)
			scalar = 1000d/totOcc;
		for (int[] state : marginal.getStates()) {
			double freq = marginal.getFrequency(state)*scalar;
			for (int i=0; i<freq; i++)
				indexes.add(state[0]);
		}
//		for (int[] state : fullPath)
//			indexes.add(state[index]);
		Collections.sort(indexes);
		int listIndex = (int)(indexes.size()*fractToInclude+0.5);
		return indexes.get(listIndex);
	}
	
	private void plot1D(PlotSpec spec, String name1, String name2, String filePrefix) throws IOException {
		plot1D(spec, name1, name2, filePrefix, outputDir);
	}
	
	public static void plot1D(PlotSpec spec, String name1, String name2, String filePrefix, File outputDir)
			throws IOException {
		if (outputDir == null) {
			// display it
			new GraphWindow(spec);
		} else {
			// write plot
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			gp.setBackgroundColor(Color.WHITE);
			
			gp.drawGraphPanel(spec);
			gp.getCartPanel().setSize(1000, 800);
			File out = new File(outputDir, filePrefix+"_"+PeriodicityPlotter.getFileSafeString(name1)
					+"_"+PeriodicityPlotter.getFileSafeString(name2));
			gp.saveAsPNG(out.getAbsolutePath()+".png");
			gp.saveAsPDF(out.getAbsolutePath()+".pdf");
		}
	}

	public static void main(String[] args) throws IOException {
		File outputDir = new File("/home/kevin/Simulators/synch/state_space_plots");
		
		List<List<RuptureIdentifier>> setIdens = Lists.newArrayList();
		List<String> setNames = Lists.newArrayList();
		
		// SoCal
		setNames.add("so_cal");
		setIdens.add(SynchIdens.getStandardSoCal());
		
		// NorCal
		setNames.add("nor_cal");
		setIdens.add(SynchIdens.getStandardNorCal());
		
		double distSpacing = 10d;
		boolean random = false;
		
		List<RuptureIdentifier> allIdens = Lists.newArrayList();
		for (List<RuptureIdentifier> idens : setIdens)
			allIdens.addAll(idens);
		
		List<EQSIM_Event> events = new SimAnalysisCatLoader(true, allIdens, true).getEvents();
		
		for (int s=0; s<setIdens.size(); s++) {
			List<RuptureIdentifier> rupIdens = setIdens.get(s);
			List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, events, rupIdens, 0d);
			
			MarkovChain chain = new EmpiricalMarkovChain(fullPath, distSpacing);
			
			File setOutputDir = new File(outputDir, setNames.get(s));
			Preconditions.checkState((setOutputDir.exists() && setOutputDir.isDirectory()) || setOutputDir.mkdir());
			
			StateSpacePlotter plot = new StateSpacePlotter(chain, rupIdens, setOutputDir);
			
//			plot.plotOccupancies();
//			plot.plotProbEither();
			
			plot.plotDiagonals(0.02);
		}
		System.exit(0);
	}

}
