package scratch.kevin.ucerf3.eal;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.jfree.data.Range;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.FileNameComparator;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.imr.AttenRelRef;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.kevin.ucerf3.MPJ_ETAS_Simulator;

public class ETAS_CatalogEALCalculator {
	
	private UCERF3_BranchAvgLossFetcher fetcher;
	private List<List<ETAS_EqkRupture>> catalogs;
	private List<File> catalogDirs = Lists.newArrayList();
	private List<Double> prevLosses = Lists.newArrayList();
	private FaultModels fm;
	
	// for getting fss index from Ned's "Nth" index
	private FaultSystemSolutionERF erf;
	private FaultSystemSolution meanSol;
	
	private ETAS_EqkRupture triggerRup;
	
	private static boolean rup_mean_loss = true; // otherwise propagate loss distribution
	
	private static final double outside_region_dist_tol = 10d; // km
	
	public ETAS_CatalogEALCalculator(UCERF3_BranchAvgLossFetcher fetcher, FaultSystemSolution meanSol,
			FaultModels fm, File... etasCatalogsDirs) throws IOException, DocumentException {
		this.fetcher = fetcher;
		this.fm = fm;
		
		Preconditions.checkArgument(etasCatalogsDirs.length > 0, "must have at least one catalog dir");
		
		loadCatalogs(etasCatalogsDirs);
		Preconditions.checkState(!catalogs.isEmpty(), "No catalogs loaded!");
		System.out.println("Loaded "+catalogs.size()+" catalogs");
		
		this.meanSol = meanSol;
		System.out.println("Loading ERF");
		erf = new FaultSystemSolutionERF(meanSol);
		erf.updateForecast();
		System.out.println("Done loading ERF");
	}
	
	private void loadCatalogs(File[] etasCatalogsDirs) throws IOException {
		catalogs = Lists.newArrayList();
		catalogDirs = Lists.newArrayList();
		
		int numEmpty = 0;
		
		for (File etasCatalogsDir : etasCatalogsDirs) {
			System.out.println("Loading catalogs from "+etasCatalogsDir.getAbsolutePath());
			
			File[] catDirs = etasCatalogsDir.listFiles();
			Arrays.sort(catDirs, new FileNameComparator());
			
			dirLoop:
			for (File dir : catDirs) {
				if (!dir.isDirectory())
					continue;
				if (!MPJ_ETAS_Simulator.isAlreadyDone(dir))
					// this will only return true if it's a valid catalog dir and the catalog is complete
					continue;
				File catalogFile = new File(dir, "simulatedEvents.txt");
//				System.out.println("Loading from: "+catalogFile.getAbsolutePath());
				List<ETAS_EqkRupture> catalog = Lists.newArrayList();
				for (String line : Files.readLines(catalogFile, Charset.defaultCharset())) {
					line = line.trim();
					if (line.startsWith("#") || line.isEmpty())
						continue;
					try {
						ETAS_EqkRupture rup = ETAS_SimAnalysisTools.loadRuptureFromFileLine(line);
						if (rup.getRuptureSurface() == null && rup.getMag() < AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF)
							continue;
						catalog.add(rup);
					} catch (Exception e) {
						System.out.println("WARNING: catalog "+dir.getName()+" parse failed, skipping: "+e.getMessage());
						continue dirLoop;
					}
				}
				// actually catalogs can be empty, this just means no rups above the source min mag cutoff
				if (catalog.isEmpty())
					numEmpty++;
//				Preconditions.checkState(!catalog.isEmpty(), "catalog is empty: "+dir.getName());
				catalogs.add(catalog);
				catalogDirs.add(dir);
			}
		}
		System.out.println(numEmpty+"/"+catalogs.size()+" catalogs are empty "
				+ "(including only fault and gridded above "+AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF+")");
	}
	
	/**
	 * 
	 * @param attenRelRef
	 * @return loss dists for each catalog. it is a distribution because fault based ruptures have loss distributions
	 * rather than a single value.
	 * @throws IOException
	 */
	public synchronized List<DiscretizedFunc> getLossDists(AttenRelRef attenRelRef) throws IOException {
		// conditional loss distributions (x=loss, y=weight) for each rupture
		DiscretizedFunc[] condLossDists = fetcher.getFaultLosses(attenRelRef, fm, true);
		// mag/loss distributions at each grid node (x=mag, y=loss)
		GriddedRegion region = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		DiscretizedFunc[] griddedMagLossDists = fetcher.getGriddedMagLossDists(
				attenRelRef, region);
		
		List<DiscretizedFunc> catalogDists = Lists.newArrayList();
		prevLosses = Lists.newArrayList();
		
		for (int i = 0; i < catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			List<Double> singleLosses = Lists.newArrayList();
			List<DiscretizedFunc> lossDists = Lists.newArrayList();
			
			for (ETAS_EqkRupture rup : catalog) {
				int fssIndex = getFSSIndex(rup);
				
				double mag = rup.getMag();
				
				if (fssIndex >= 0) {
					// fault based source
					Preconditions.checkState((float)mag == (float)meanSol.getRupSet().getMagForRup(fssIndex));
					if (condLossDists[fssIndex].getNum() == 0)
						continue;
					lossDists.add(condLossDists[fssIndex]);
					// make sure weights sum to 1
					double sumY = 0;
					for (Point2D pt : condLossDists[fssIndex])
						sumY += pt.getY();
					Preconditions.checkState((float)sumY == 1f, "rup losses don't sum to 1: "+(float)sumY);
				} else {
					// grid source
					double loss = calcGridSourceLoss(rup, region, griddedMagLossDists, catalogDirs.get(i).getName());
					// single loss value with weight=1
					singleLosses.add(loss);
				}
			}
			
			// first sum up all single losses (easy)
			double totSingleLosses = 0d;
			for (double loss : singleLosses)
				totSingleLosses += loss;
			
			if (rup_mean_loss) {
				for (DiscretizedFunc lossDist : lossDists) {
					double loss = 0;
					double sumWeight = 0;
					for (Point2D pt : lossDist) {
						sumWeight += pt.getY();
						loss += pt.getX()*pt.getY();
					}
					Preconditions.checkState((float)sumWeight == 1f, "Weights don't sum to 1: "+(float)sumWeight);
					totSingleLosses += loss;
				}
			}
			
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			if (lossDists.isEmpty() || rup_mean_loss) {
				// only point sources
				func.set(totSingleLosses, 1d);
			} else {
				// calculate expected number of loss dists for verification
				int expectedNum = 1;
				for (DiscretizedFunc lossDist : lossDists)
					expectedNum *= lossDist.getNum();
				
				List<LossChain> lossChains = getLossChains(totSingleLosses, lossDists);
				Preconditions.checkState(lossChains.size() == expectedNum,
						"expected "+expectedNum+" chains, got "+lossChains.size());
				
				double sumWeight = 0d;
				for (LossChain chain : lossChains) {
					double weight = chain.weight;
					double loss = chain.totLoss;
					sumWeight += weight;
					int xInd = UCERF3_BranchAvgLossFetcher.getMatchingXIndexFloatPrecision(loss, func);
					if (xInd < 0)
						func.set(loss, weight);
					else
						func.set(loss, weight + func.getY(xInd));
				}
				Preconditions.checkState((float)sumWeight == 1f,
						"chain weights don't sum to 1: "+sumWeight+" ("+lossChains.size()+" chains)");
			}
			
			double meanLoss = 0d;
			for (Point2D pt : func)
				meanLoss += pt.getX()*pt.getY();
			prevLosses.add(meanLoss);
			
			catalogDists.add(func);
		}
		
		if (triggerRup != null) {
			int fssIndex = getFSSIndex(triggerRup);
			
			double mag = triggerRup.getMag();
			
			double triggerLoss;
			
			if (fssIndex >= 0) {
				// fault based source
				Preconditions.checkState((float)mag == (float)meanSol.getRupSet().getMagForRup(fssIndex));
				// make sure weights sum to 1
				double meanLoss = 0;
				double sumY = 0;
				for (Point2D pt : condLossDists[fssIndex]) {
					sumY += pt.getY();
					meanLoss += pt.getX()*pt.getY();
				}
				Preconditions.checkState((float)sumY == 1f, "rup losses don't sum to 1: "+(float)sumY);
				triggerLoss = meanLoss;
			} else {
				// grid source
				triggerLoss = calcGridSourceLoss(triggerRup, region, griddedMagLossDists, "TRIGGER");
			}
			System.out.println("Trigger rupture loss: "+triggerLoss);
		}
		
		return catalogDists;
	}
	
	private double calcGridSourceLoss(ETAS_EqkRupture rup, GriddedRegion region,
			DiscretizedFunc[] griddedMagLossDists, String catName) {
		double mag = rup.getMag();
		if ((float)mag < (float)AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF)
			// below our min mag
			return 0;
		Location loc = rup.getHypocenterLocation();
		int nodeIndex = region.indexForLocation(loc);
		if (nodeIndex < 0) {
			// find closest
			Location closestLoc = null;
			double closestDist = Double.POSITIVE_INFINITY;
			int closestIndex = -1;
			LocationList nodeList = region.getNodeList();
			for (int j = 0; j < nodeList.size(); j++) {
				Location nodeLoc = nodeList.get(j);
				double dist = LocationUtils.horzDistanceFast(nodeLoc, loc);
				if (dist < closestDist) {
					closestDist = dist;
					closestLoc = nodeLoc;
					closestIndex = j;
				}
			}
			if (closestDist <= outside_region_dist_tol) {
				System.out.println("Location ("+loc+") outside region, mapped to node "
						+(float)closestDist+" km away");
				nodeIndex = closestIndex;
			} else {
				System.out.println("Location ("+loc+") outside region, closest node is "
						+(float)closestDist+" km away. Did not map.");
			}
		}
		Preconditions.checkState(nodeIndex >= 0, "Node not found for loc: "+loc);
		DiscretizedFunc magLossDist = griddedMagLossDists[nodeIndex];
		int magIndex = UCERF3_BranchAvgLossFetcher.getMatchingXIndexFloatPrecision(mag, magLossDist);
		if (magIndex < 0) {
			List<Float> xVals = Lists.newArrayList();
			List<Float> yVals = Lists.newArrayList();
			for (Point2D pt : magLossDist) {
				xVals.add((float)pt.getX());
				yVals.add((float)pt.getY());
			}
			System.out.println("Mag "+mag+" not found in cat "+catName+" for loc="+loc+", gridLoc="
				+region.getNodeList().get(nodeIndex)+"."
				+"\n\tNode mags:\t"+Joiner.on("\t").join(xVals)
				+"\n\tNode loss:\t"+Joiner.on("\t").join(yVals));
			if (rup == triggerRup) {
				// interpolate
				if (mag > magLossDist.getMaxX()) {
					double binWidth;
					if (magLossDist.getNum() >= 2)
						binWidth = magLossDist.getX(magLossDist.getNum()-1)
							- magLossDist.getX(magLossDist.getNum()-2);
					else
						binWidth = 0d;
					if (mag < magLossDist.getMaxX()+binWidth) {
						System.out.println("Above but within last bin, returning that");
						return magLossDist.getY(magLossDist.getNum()-1);
					} else {
						System.out.println("Above mag loss dist, returning zero");
						return 0d;
					}
				}
				System.out.println("Interpolating losses for trigger rupture");
				return magLossDist.getInterpolatedY(mag);
			}
//			System.out.println("Dist x vals: "+Joiner.on(",").join(xVals));
//			System.out.println("Node loc: "+region.getNodeList().get(nodeIndex));
			return 0; // TODO figure out what's going on and don't skip!
		}
		Preconditions.checkState(magIndex >= 0, "Mag index not found for grid node. mag="+mag+", loc="+loc);
		double loss = magLossDist.getY(magIndex);
		return loss;
	}
	
	private int getFSSIndex(ETAS_EqkRupture rup) {
		return ETAS_SimAnalysisTools.getFSSIndex(rup, erf);
	}
	
	public void setTriggerGridRup(Location loc, double mag) {
		triggerRup = new ETAS_EqkRupture();
		triggerRup.setPointSurface(loc);
		triggerRup.setHypocenterLocation(loc);
		triggerRup.setMag(mag);
		triggerRup.setNthERF_Index(Integer.MAX_VALUE);
	}
	
	public void setTriggerFaultRup(int fssRupID) {
		triggerRup = new ETAS_EqkRupture();
		triggerRup.setMag(meanSol.getRupSet().getMagForRup(fssRupID));
		triggerRup.setRuptureSurface(meanSol.getRupSet().getSurfaceForRupupture(fssRupID, 1d, false));
		triggerRup.setNthERF_Index(erf.get_nthRupIndicesForSource(erf.getSrcIndexForFltSysRup(fssRupID))[0]);
	}
	
	private List<LossChain> getLossChains(double totSingleLosses, List<DiscretizedFunc> distsToProcess) {
		return getLossChains(new LossChain(totSingleLosses), distsToProcess, 0);
	}
	
	private List<LossChain> getLossChains(LossChain prevChain,
			List<DiscretizedFunc> distsToProcess, int distIndex) {
		if (distIndex == distsToProcess.size())
			// we're done
			return Lists.newArrayList(prevChain);
		DiscretizedFunc lossDist = distsToProcess.get(distIndex);
		
		List<LossChain> ret = Lists.newArrayList();
		
		for (Point2D pt : lossDist) {
			LossChain newChain = prevChain.copy();
			newChain.add(pt.getX(), pt.getY());
			ret.addAll(getLossChains(newChain, distsToProcess, distIndex+1));
		}
		
		return ret;
	}
	
	private class LossChain {
		private double totLoss;
		private double weight;
		
		public LossChain(double totSingleLosses) {
			this(totSingleLosses, 1d);
		}
		
		private LossChain(double totLoss, double weight) {
			this.totLoss = totLoss;
			this.weight = weight;
		}
		
		public void add(double loss, double weight) {
			this.totLoss += loss;
			this.weight *= weight;
		}
		
		public LossChain copy() {
			return new LossChain(totLoss, weight);
		}
	}
	
	public HistogramFunction getLossHist(List<DiscretizedFunc> catalogDists, double delta, boolean isLog10) {
		Range range = calcSmartHistRange(catalogDists, delta, isLog10);
		int num = (int)Math.round((range.getUpperBound()-range.getLowerBound())/delta) + 1;
		return getLossHist(catalogDists, range.getLowerBound(), num, delta, isLog10);
	}
	
	/**
	 * Calculates a nicely rounded range that will exactly cover the data range with the given discretization.
	 * Histogram num points can be calculated as num = (range.getUpperBound()-range.getLowerBound())/delta + 1;
	 * @param delta
	 * @return
	 */
	public Range calcSmartHistRange(List<DiscretizedFunc> catalogDists, double delta, boolean isLog10) {
		MinMaxAveTracker xTrack = new MinMaxAveTracker();
		for (DiscretizedFunc func : catalogDists) {
			xTrack.addValue(func.getMinX());
			xTrack.addValue(func.getMaxX());
		}
		Range range = new Range(xTrack.getMin(), xTrack.getMax());
		if (isLog10)
			range = new Range(Math.log10(range.getLowerBound()), Math.log10(range.getUpperBound()));
		double min = Math.floor(range.getLowerBound()/delta) * delta;
		min += 0.5*delta;
		// it's possible that this was too conservative and that the bin above could hold the range min val
		// due to the bin width
		if (min + 0.5*delta < range.getLowerBound())
			min += delta;
		double max = min;
		while (max+0.5*delta < range.getUpperBound())
			max += delta;
		
		return new Range(min, max);
	}
	
	public HistogramFunction getLossHist(List<DiscretizedFunc> catalogDists,
			double minX, int numX, double deltaX, boolean isLog10) {
		HistogramFunction lossHist = new HistogramFunction(minX, numX, deltaX);
		
		double distMin = minX - 0.5*deltaX;
		double distMax = lossHist.getMaxX() + 0.5*deltaX;
		
		double weightMult = 1d/catalogDists.size();
		
		double[] lossVals = new double[catalogDists.size()];
		
		for (int i = 0; i < catalogDists.size(); i++) {
			DiscretizedFunc catalogDist = catalogDists.get(i);
			double meanVal = 0d;
			for (int index=0; index<catalogDist.getNum(); index++) {
				double loss = catalogDist.getX(index);
				double weight = catalogDist.getY(index);
				
				meanVal += loss*weight;
				
				weight *= weightMult;
				
				if (loss == 0) {
					lossHist.add(0, weight);
					continue;
				}
				
				if (isLog10)
					loss = Math.log10(loss);
				
				if (loss > distMax)
					// above max, put in last bin
					lossHist.add(lossHist.getNum()-1, weight);
				else if (loss < distMin)
					// below min, put in first bin
					lossHist.add(0, weight);
				else
					// put in actual bin
					lossHist.add(loss, weight);
			}
			lossVals[i] = meanVal;
		}
		
		double sumY = lossHist.calcSumOfY_Vals();
//		System.out.println("Sum Y values: "+sumY);
		Preconditions.checkState((float)sumY == 1f, "loss hist sum of y vals doesn't equal 1: "+(float)sumY);
		
		System.out.println("Mean loss: "+StatUtils.mean(lossVals));
		System.out.println("Median loss: "+DataUtils.median(lossVals));
		
		return lossHist;
	}
	
	public void writeLossHist(File outputDir, String outputPrefix, HistogramFunction lossHist, boolean isLogX,
			String xAxisLabel, double xAxisScale, double maxX) throws IOException {
		if (!outputDir.exists())
			outputDir.mkdir();
		
//		double logMinY = (1d/catalogs.size())/10d; // one order of magnitude below the smallest possible
		double logMinY = 1e-4;
		
		List<PlotElement> elems = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		if (xAxisScale != 1d && xAxisScale != 0d) {
			HistogramFunction newLossHist = new HistogramFunction(
					lossHist.getMinX()*xAxisScale, lossHist.getNum(), lossHist.getDelta()*xAxisScale);
			System.out.println("Scaled minX: "+newLossHist.getMinX()+" maxX="+newLossHist.getMaxX());
			for (int i=0; i<lossHist.getNum(); i++)
				newLossHist.set(i, lossHist.getY(i));
			lossHist = newLossHist;
		}
		
		elems.add(lossHist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		
		if (isLogX)
			xAxisLabel = "Log10("+xAxisLabel+")";
		
		PlotSpec spec = new PlotSpec(elems, chars, "Simulated Loss Distribution", xAxisLabel, "Frequency");
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setBackgroundColor(Color.WHITE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
		
		double minX = lossHist.getMinX()-0.5*lossHist.getDelta();
		if (!Doubles.isFinite(maxX) || maxX <= 0)
			maxX = lossHist.getMaxX()+0.5*lossHist.getDelta();
		double maxY = lossHist.getMaxY()*1.2;
		
		for (boolean logY : new boolean[] {false, true}) {
			if (logY)
				gp.setUserBounds(minX, maxX, logMinY, maxY);
			else
				gp.setUserBounds(minX, maxX, 0, maxY);
			gp.drawGraphPanel(spec, false, logY);
			gp.getCartPanel().setSize(1000, 800);
			String myPrefix = outputPrefix;
			if (logY)
				myPrefix += "_logY";
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
		}
		
		// now loss excedence
		HistogramFunction cumDist = lossHist.getCumulativeDistFunctionWithHalfBinOffset();
		// convert to exceedance
		for (int i=0; i<cumDist.getNum(); i++)
			cumDist.set(i, 1d-cumDist.getY(i));
		
		elems = Lists.newArrayList();
		chars = Lists.newArrayList();
		
		elems.add(cumDist);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		spec = new PlotSpec(elems, chars, "Simulated Loss Exceedance Probabilities", xAxisLabel, "Exceedance Prob");
		
		maxY = cumDist.getMaxY()*1.2;
		if (maxY > 1d)
			maxY = 1.05;
		
		for (boolean logY : new boolean[] {false, true}) {
			if (logY)
				gp.setUserBounds(minX, maxX, logMinY, maxY);
			else
				gp.setUserBounds(minX, maxX, 0, maxY);
			gp.drawGraphPanel(spec, false, logY);
			gp.getCartPanel().setSize(1000, 800);
			String myPrefix = outputPrefix+"_exceed";
			if (logY)
				myPrefix += "_logY";
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
		}
	}
	
	public void writePrevLossesToCSV(File csvFile) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		double cutoffMag = AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF;
		csv.addLine("Index", "Parent Dir Name", "Dir Name", "Total Mean Loss", "# FSS Ruptures",
				"# M>="+(float)cutoffMag, "Max Mag");
		
		for (int i=0; i<catalogs.size(); i++) {
			double totLoss = prevLosses.get(i);
			int numFSSRups = 0;
			int numAbove = 0;
			double maxMag = 0;
			for (ETAS_EqkRupture rup : catalogs.get(i)) {
				if (getFSSIndex(rup) >= 0)
					numFSSRups++;
				if (rup.getMag() > maxMag)
					maxMag = rup.getMag();
				if ((float)rup.getMag() >= (float)cutoffMag)
					numAbove++;
			}
			File catDir = catalogDirs.get(i);
			csv.addLine(i+"", catDir.getParentFile().getName(), catDir.getName(), totLoss+"",
					numFSSRups+"", numAbove+"", maxMag+"");
		}
		
		csv.writeToFile(csvFile);
	}

	public static void main(String[] args) throws IOException, DocumentException {
//		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-la_habra/");
//		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-mojave_7/");
		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-spontaneous/");
		
		// true mean FSS which includes rupture mapping information. this must be the exact file used to calulate EALs
		File trueMeanSolFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_"
				+ "COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL_WITH_MAPPING.zip");

		// directory which contains EAL data
		File dataDir = new File("/home/kevin/OpenSHA/UCERF3/eal/2014_05_05-ucerf3-eal-calc-wald-vs30");
		String xAxisLabel = "$ (Billions)";
		double xAxisScale = 1d/1e6; // portfolio units are in thousands (1e3), so convert to billions by dividing by 1e6
		double maxX = 120;

		// IMR for which EAL data has already been computed
		AttenRelRef attenRelRef = AttenRelRef.BSSA_2014;

		// Fault model of interest
		FaultModels fm = FaultModels.FM3_1;

		// Branch averaged FSS
		FaultSystemSolution baSol = FaultSystemIO.loadSol(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_"
				+ "COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));

		// Compound fault system solution
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
						+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip"));
		
		UCERF3_BranchAvgLossFetcher fetcher = new UCERF3_BranchAvgLossFetcher(trueMeanSolFile, cfss, dataDir);
		
		// get result dirs
		List<File> resultDirs = Lists.newArrayList();
		File[] subDirs = parentDir.listFiles();
		Arrays.sort(subDirs, new FileNameComparator());
		for (File subDir : subDirs) {
			if (subDir.isDirectory() && subDir.getName().startsWith("results"))
				resultDirs.add(subDir);
		}
		File[] etasCatalogsDirs = resultDirs.toArray(new File[0]);
		
		ETAS_CatalogEALCalculator calc = new ETAS_CatalogEALCalculator(fetcher, baSol, fm, etasCatalogsDirs);
		
		if (parentDir.getName().contains("mojave"))
			calc.setTriggerFaultRup(197792);
		else if (parentDir.getName().contains("la_habra"))
			calc.setTriggerGridRup(new Location(33.932,-117.917,4.8), 6.2);
		
		System.out.println("Calculating catalog losses");
		List<DiscretizedFunc> lossDists = calc.getLossDists(attenRelRef);
		
		boolean isLog10 = false;
		double deltaX = 1e6;
		HistogramFunction lossHist = calc.getLossHist(lossDists, deltaX, isLog10);
		File outputDir = new File(parentDir, "outputs");
		if (!outputDir.exists())
			outputDir.mkdir();
//		calc.writeLossHist(outputDir, attenRelRef.name(), lossHist, isLog10);
		calc.writeLossHist(outputDir, attenRelRef.name(), lossHist, isLog10, xAxisLabel, xAxisScale, maxX);
		
		calc.writePrevLossesToCSV(new File(outputDir, attenRelRef.name()+"_losses.csv"));
	}

}
