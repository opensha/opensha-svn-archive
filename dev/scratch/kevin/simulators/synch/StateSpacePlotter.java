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
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotPreferences;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.iden.RuptureIdentifier;

import scratch.kevin.markov.EmpiricalMarkovChain;
import scratch.kevin.markov.PossibleStates;
import scratch.kevin.simulators.MarkovChainBuilder;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class StateSpacePlotter {
	
	private File outputDir;
	private List<int[]> fullPath;
	private List<? extends Named> names;
	private double distSpacing;
	
	private EmpiricalMarkovChain chain;
	
	private int nDims;
	
	private double fractToInclude = 0.99;
	
	private List<Integer> minBinsList;
	
	public StateSpacePlotter(List<int[]> fullPath, List<? extends Named> names, double distSpacing, File outputDir) {
		this.fullPath = fullPath;
		this.names = names;
		this.outputDir = outputDir;
		this.distSpacing = distSpacing;
		
		this.nDims = fullPath.get(0).length;
		Preconditions.checkArgument(nDims > 1, "must have at least 2D chain");
		Preconditions.checkArgument(names.size() == nDims);
		
		minBinsList = Lists.newArrayList();
		for (int i=0; i<nDims; i++)
			minBinsList.add(getNBinsForFract(fullPath, i, fractToInclude));
		
		this.chain = new EmpiricalMarkovChain(fullPath, distSpacing);
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
				
				EvenlyDiscrXYZ_DataSet xyz = buildXYZ(i, j);
				
				double sumZ = 0d;
				for (int[] state : fullPath) {
					int xInd = state[i];
					int yInd = state[j];
					
					if (xInd >= xyz.getNumX() || yInd >= xyz.getNumY())
						continue;
					
					xyz.set(xInd, yInd, xyz.get(xInd, yInd)+1d);
					sumZ++;
				}
				
				xyz.scale(1d/sumZ);
				
				CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, xyz.getMaxZ());
				
				plot2D(xyz, cpt, name1, name2, "Occupancy", "occ", true);
			}
		}
	}
	
	public void plotProbEither() throws IOException {
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
		cpt.setNanColor(Color.WHITE);
		
		for (int i=0; i<nDims; i++) {
			String name1 = names.get(i).getName();
			for (int j=i+1; j<nDims; j++) {
				String name2 = names.get(j).getName();
				
				EvenlyDiscrXYZ_DataSet xyz = getMarkovXYZ(MarkovProb.EITHER, i, j);
				
				plot2D(xyz, cpt, name1, name2, "Prob Either", "prob_either", false);
			}
		}
	}
	
	private enum MarkovProb {
		E1,
		E2,
		EITHER,
		BOTH,
		NONE
	}
	
	private EvenlyDiscrXYZ_DataSet getMarkovXYZ(MarkovProb type, int i, int j) {
		EmpiricalMarkovChain collapsed = chain.getCollapsedChain(i, j);
		
		EvenlyDiscrXYZ_DataSet xyz = buildXYZ(i, j);
		
		for (int xInd=0; xInd<xyz.getNumX(); xInd++) {
			for (int yInd=0; yInd<xyz.getNumY(); yInd++) {
				PossibleStates possible = collapsed.getStateTransitionDataset().get(new int[] {xInd,yInd});
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
	
	private void plot2D(EvenlyDiscrXYZ_DataSet xyz, CPT cpt, String name1, String name2,
			String zLabel, String filePrefix, boolean marginals) throws IOException {
		XYZPlotSpec xyzSpec = new XYZPlotSpec(xyz, cpt, name1+" "+name2+" "+zLabel,
				name1+" OI", name2+" OI", zLabel);
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
		
//		GraphWindow gw = new GraphWindow(marginal1);
//		gw = new GraphWindow(marginal2);
		
		List<XYPlot> extraPlots = null;
		List<Integer> weights = null;
		
		int width = 600;
		int height = 680;
		
		if (marginals) {
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
	
	private static int getNBinsForFract(List<int[]> fullPath, int index, double fractToInclude) {
		List<Integer> indexes = Lists.newArrayList();
		for (int[] state : fullPath)
			indexes.add(state[index]);
		Collections.sort(indexes);
		int listIndex = (int)(indexes.size()*fractToInclude+0.5);
		return indexes.get(listIndex);
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
			
			File setOutputDir = new File(outputDir, setNames.get(s));
			Preconditions.checkState((setOutputDir.exists() && setOutputDir.isDirectory()) || setOutputDir.mkdir());
			
			StateSpacePlotter plot = new StateSpacePlotter(fullPath, rupIdens, distSpacing, setOutputDir);
			
			plot.plotOccupancies();
			plot.plotProbEither();
		}
		System.exit(0);
	}

}
