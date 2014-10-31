package scratch.kevin.simulators.synch.prediction;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jfree.data.Range;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.simulators.iden.RuptureIdentifier;

import scratch.UCERF3.utils.IDPairing;
import scratch.kevin.simulators.PeriodicityPlotter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

public class SynchRIPredictor implements Predictor {
	
	private int maxLag;
	private int nDims;
	private double distSpacing;
	
	private Map<IDPairing, SynchRunningCalc[]> synchCalcsMap;
	
	private RecurrIntervalPredictor riPredict;
	
	private int[] prevState;
	
	private HistogramFunction[] lnSumGHists;
	
	public SynchRIPredictor(int maxLag) {
		this.maxLag = maxLag;
	}

	@Override
	public String getShortName() {
		return "SynchRI";
	}

	@Override
	public String getName() {
		return "Synch/RI Hybrid";
	}

	@Override
	public void init(List<int[]> path, double distSpacing) {
		synchCalcsMap = Maps.newHashMap();
		nDims = path.get(0).length;
		this.distSpacing = distSpacing;
		for (int i=0; i<nDims; i++) {
			for (int j=0; j<nDims; j++) {
				if (i == j)
					continue;
				IDPairing pair = new IDPairing(i, j);
				SynchRunningCalc[] calcs = new SynchRunningCalc[maxLag+1];
				for (int l=0; l<=maxLag; l++)
					calcs[l] = new SynchRunningCalc(i, j, l);
				synchCalcsMap.put(pair, calcs);
			}
		}
		lnSumGHists = new HistogramFunction[nDims];
		for (int i=0; i<nDims; i++)
			lnSumGHists[i] = new HistogramFunction(-2.0d, 41, 0.1d);
		
		for (int[] state : path)
			addState(state);
		
		riPredict = new RecurrIntervalPredictor();
		riPredict.init(path, distSpacing);
	}

	@Override
	public void addState(int[] state) {
		if (riPredict != null)
			// will be null during init
			riPredict.addState(state);
		
		for (SynchRunningCalc[] calcs : synchCalcsMap.values()) {
			for (SynchRunningCalc calc : calcs)
				calc.addState(state);
		}
		
		prevState = state;
	}

	@Override
	public double[] getRuptureProbabilities() {
		return getRuptureProbabilities(prevState);
	}

	@Override
	public double[] getRuptureProbabilities(int[] state) {
		Preconditions.checkArgument(state != null);
		double[] riProbs = riPredict.getRuptureProbabilities(state);
		
		double[] ret = Arrays.copyOf(riProbs, riProbs.length);
		
		for (int i=0; i<state.length; i++) {
			double riProb = riProbs[i];
			if (riProb == 0)
				continue;
			double multRate = riProb;
			double avgProb = 0d;
			
			double lnSumG = 0;
			for (int j=0; j<nDims; j++) {
				if (i == j)
					continue;
				
				IDPairing pair = new IDPairing(i, j);
//				IDPairing pair = new IDPairing(j, i); // this was a test, bad
				
				// we need synchronization with the destination states for the other fault
				// not the current state
				
				// this is the lag of the destination state, assuming that it doesn't rupture
				int l = state[j]+1;
				// if it doesn't rupture, this is the synchronization
				double g_norup;
				if (l > maxLag)
					g_norup = 1d;
				else
					g_norup = synchCalcsMap.get(pair)[l].getCatalogG();
				Preconditions.checkState(Doubles.isFinite(g_norup));
				// synchronization if it does rupture
				double g_rup = synchCalcsMap.get(pair)[0].getCatalogG();
				Preconditions.checkState(Doubles.isFinite(g_rup));
				double probJRup = riProbs[j];
//				lnSumG += Math.log(g_norup)*(1-probJRup) + Math.log(g_rup)*probJRup;
				lnSumG += Math.log(g_norup)*(1-probJRup)*riProb + Math.log(g_rup)*probJRup*riProb;
//				lnSumG += Math.log(g_rup)*probRup;
//				if (Math.random() <= probRup)
//					lnSumG += Math.log(g_rup);
//				else
//					lnSumG += Math.log(g_norup);
				
//				double avgGain = g_rup*probJRup + g_norup*(1-probJRup);
//				multRate *= avgGain;
				double avgGain = Math.exp(Math.log(g_rup)*probJRup + Math.log(g_norup)*(1-probJRup));
				multRate *=  Math.exp(Math.log(g_rup)*probJRup) * Math.exp(Math.log(g_norup)*(1-probJRup));
//				avgProb += riProb*avgGain;
				
				g_rup = Math.exp(Math.log(g_rup)*1.0);
				g_norup = Math.exp(Math.log(g_norup)*1.0);
				avgProb += g_rup*riProb*probJRup + g_norup*riProb*(1d-probJRup);
			}
			
			lnSumGHists[i].add(lnSumGHists[i].getClosestXIndex(lnSumG), 1d);
//			double prob = Math.exp(Math.log(riProb)+lnSumG);
//			double prob = multRate;
//			double prob = avgProb / (double)(nDims-1);
//			double prob = avgProb / (double)(nDims-1);
			double prob = avgProb;
			
			prob = 1-Math.exp(-prob);
			
			Preconditions.checkState(prob >= 0 && prob <= 1,
					"Bad probability: "+prob+", riProb="+riProb+", lnSumG="+lnSumG);
			
			ret[i] = prob;
		}
		
		return ret;
	}

	@Override
	public void printDiagnostics() {
		// do nothing
	}

	@Override
	public Predictor getCollapsed(int... indexes) {
		SynchRIPredictor p = new SynchRIPredictor(maxLag);
		p.riPredict = (RecurrIntervalPredictor)riPredict.getCollapsed(indexes);
		p.nDims = indexes.length;
		p.synchCalcsMap = Maps.newHashMap();
		p.lnSumGHists = new HistogramFunction[p.nDims];
		for (int i=0; i<indexes.length; i++) {
			for (int j=0; j<indexes.length; j++) {
				if (i == j)
					continue;
				p.synchCalcsMap.put(new IDPairing(i, j), synchCalcsMap.get(
						new IDPairing(indexes[i], indexes[j])));
			}
			p.lnSumGHists[i] = lnSumGHists[indexes[i]];
		}
		return p;
	}
	
	public void writePlots(File dir, List<RuptureIdentifier> idens) throws IOException {
		Preconditions.checkState((dir.exists() && dir.isDirectory()) || dir.mkdir());
		
		for (int i=0; i<nDims; i++) {
			String name1 = idens.get(i).getName();
			for (int j=0; j<nDims; j++) {
				if (i == j)
					continue;
				String name2 = idens.get(j).getName();
				String prefix = PeriodicityPlotter.getFileSafeString(name1)
						+"_"+PeriodicityPlotter.getFileSafeString(name2);
				SynchRunningCalc[] synchCalcs = synchCalcsMap.get(new IDPairing(i, j));
				EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0d, synchCalcs.length, distSpacing);
				for (int l=0; l<synchCalcs.length; l++)
					func.set(l, Math.log(synchCalcs[l].getCatalogG()));
				List<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
				List<PlotCurveCharacterstics> chars = Lists.newArrayList();
				funcs.add(func);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
				PlotSpec spec = new PlotSpec(funcs, chars, name1+", "+name2, "Lag (years)", "Ln(Gain)");
				
				HeadlessGraphPanel gp = new HeadlessGraphPanel();
				gp.setTickLabelFontSize(18);
				gp.setAxisLabelFontSize(20);
				gp.setPlotLabelFontSize(21);
				gp.setBackgroundColor(Color.WHITE);
				
				gp.setUserBounds(null, new Range(-2, 2));
				gp.drawGraphPanel(spec);
				gp.getCartPanel().setSize(800, 400);
				gp.saveAsPNG(new File(dir, prefix+".png").getAbsolutePath());
				gp.saveAsPDF(new File(dir, prefix+".pdf").getAbsolutePath());
			}
		}
		
		File histDir = new File(dir, "ln_sum_g_hists");
		Preconditions.checkState((histDir.exists() && histDir.isDirectory()) || histDir.mkdir());
		
		for (int i=0; i<nDims; i++) {
			String name = idens.get(i).getName();
			
			List<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
			List<PlotCurveCharacterstics> chars = Lists.newArrayList();
			funcs.add(lnSumGHists[i]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 2f, Color.BLACK));
			PlotSpec spec = new PlotSpec(funcs, chars, name, "sum(Ln(G))", "Number");
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(18);
			gp.setAxisLabelFontSize(20);
			gp.setPlotLabelFontSize(21);
			gp.setBackgroundColor(Color.WHITE);
			
			gp.drawGraphPanel(spec);
			gp.getCartPanel().setSize(800, 400);
			gp.saveAsPNG(new File(histDir,
					PeriodicityPlotter.getFileSafeString(name)+".png").getAbsolutePath());
		}
	}
	
	private class SynchRunningCalc {
		
		private int m, n, lag;
		
		private int numWindows = 0;
		private int numMN=0, numM=0, numN=0;
		
		private ArrayDeque<Integer> nDeque;
		
		private int[] prevState;
		
		private static final boolean CALC_DIAG = false;
		
		public SynchRunningCalc(int m, int n, int lag) {
			this.m = m;
			this.n = n;
			this.lag = lag;
			Preconditions.checkState(lag >= 0);
			if (lag != 0)
				// this will store previous states for index n
				nDeque = new ArrayDeque<Integer>(maxLag);
		}
		
		public void addState(int[] state) {
			int[] newState = { state[m], state[n]};
			
			if (CALC_DIAG) {
				if (prevState == null) {
					prevState = state;
					return;
				}
				int delta = prevState[1] - prevState[0];
				if (delta != lag) {
					prevState = state;
					return;
				}
			}
			
			if (lag > 0) {
				nDeque.addLast(state[n]);
				if (nDeque.size() > lag)
					// we have enough
					newState[1] = nDeque.removeFirst();
				else
					return;
			}
			numWindows++;
			if (newState[0] == 0)
				numM++;
			if (newState[1] == 0)
				numN++;
			if (newState[0] == 0 && newState[1] == 0)
				numMN++;
			
			prevState = newState;
		}
		
		public double getCatalogG() {
			if (numWindows == 0 || numM == 0 || numN == 0)
				return 1d;
			return (double)numWindows * (double)numMN/(double)(numM*numN);
		}
	}

}
