package scratch.kevin.ucerf3.etas;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.FileNameComparator;
import org.opensha.nshmp2.erf.source.PointSource13b;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.cybershake.etas.ETASModProbConfig.ETAS_CyberShake_Scenarios;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.imr.AttenRelRef;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.erf.ETAS.ETAS_MultiSimAnalysisTools;
import scratch.UCERF3.erf.ETAS.ETAS_SimAnalysisTools;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator.TestScenario;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.LastEventData;
import scratch.kevin.ucerf3.eal.UCERF3_BranchAvgLossFetcher;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.primitives.Doubles;

public class ETAS_CatalogEALCalculator {
	
	private UCERF3_BranchAvgLossFetcher fetcher;
	private List<List<ETAS_EqkRupture>> catalogs;
	private FaultModels fm;
	
	// for getting fss index from Ned's "Nth" index
	private FaultSystemSolutionERF erf;
	private FaultSystemSolution meanSol;
	
	private ETAS_EqkRupture triggerRup;
	
	private static boolean rup_mean_loss = true; // otherwise propagate loss distribution
	
	private static final double outside_region_dist_tol = 10d; // km
	
	private boolean triggeredOnly = false;
	
	private static int id_for_scenario = 0;
	
	private static List<List<ETAS_EqkRupture>> loadCatalogs(File resultsBinFile) throws IOException {
		Preconditions.checkArgument(resultsBinFile.exists(), "catalog file doesn't exist");
		
		return loadCatalogs(resultsBinFile, AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF-0.05);
	}
	
	public ETAS_CatalogEALCalculator(UCERF3_BranchAvgLossFetcher fetcher, FaultSystemSolution meanSol,
			FaultModels fm, File resultsBinFile) throws IOException, DocumentException {
		this(fetcher, meanSol, fm, loadCatalogs(resultsBinFile));
	}
	
	public ETAS_CatalogEALCalculator(UCERF3_BranchAvgLossFetcher fetcher, FaultSystemSolution meanSol,
			FaultModels fm, List<List<ETAS_EqkRupture>> catalogs) throws IOException, DocumentException {
		this.fetcher = fetcher;
		this.fm = fm;
		
		this.catalogs = catalogs;
		
		Preconditions.checkState(!catalogs.isEmpty(), "No catalogs loaded!");
		System.out.println("Loaded "+catalogs.size()+" catalogs");
		
		LastEventData.populateSubSects(meanSol.getRupSet().getFaultSectionDataList(), LastEventData.load());
		this.meanSol = meanSol;
		System.out.println("Loading ERF");
		double origMinMag = AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF;
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = 2.55;
		erf = MPJ_ETAS_Simulator.buildERF(meanSol, false, 1d);
		erf.updateForecast();
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = origMinMag;
		System.out.println("Done loading ERF");
	}
	
//	static List<List<ETAS_EqkRupture>> loadCatalogs(File[] etasCatalogsDirs, double minGriddedMag) throws IOException {
//		return loadCatalogs(etasCatalogsDirs, minGriddedMag, null);
//	}
	
	public void setTriggeredOnly(boolean triggeredOnly) {
		this.triggeredOnly = triggeredOnly;
	}
	
	/**
	 * Load ETAS catalogs from file.
	 * @param etasCatalogsDirs
	 * @param catalogDirs if not null, sub directories will be added to this list so that you can keep track
	 * of which directory each catalog came from
	 * @return
	 * @throws IOException
	 */
	static List<List<ETAS_EqkRupture>> loadCatalogs(File resultsBinFile, double minGriddedMag) throws IOException {
		int numEmpty = 0;
		
		List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(resultsBinFile, minGriddedMag);
		for (List<ETAS_EqkRupture> catalog : catalogs)
			if (catalog.isEmpty())
				numEmpty++;
		
		System.out.println(numEmpty+"/"+catalogs.size()+" catalogs are empty "
				+ "(including only fault and gridded above "+minGriddedMag+")");
		return catalogs;
	}
	
	static List<List<ETAS_EqkRupture>> loadCatalogsZip(File zipFile, double minGriddedMag,
			List<String> catalogNames) throws IOException {
		ZipFile zip = new ZipFile(zipFile);
		
		List<List<ETAS_EqkRupture>> catalogs = Lists.newArrayList();
		
		List<? extends ZipEntry> entries = Collections.list(zip.entries());
		// sort for constant ordering
		Collections.sort(entries, new Comparator<ZipEntry>() {

			@Override
			public int compare(ZipEntry o1, ZipEntry o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		for (ZipEntry entry : entries) {
			if (!entry.isDirectory())
				continue;
//			System.out.println(entry.getName());
			String subEntryName = entry.getName()+"simulatedEvents.txt";
			ZipEntry catEntry = zip.getEntry(subEntryName);
			String infoEntryName = entry.getName()+"infoString.txt";
			ZipEntry infoEntry = zip.getEntry(infoEntryName);
			if (catEntry == null || infoEntry == null)
				continue;
			
			// make sure it's actually done
			BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(infoEntry)));
			
			boolean done = false;
			for (String line : CharStreams.readLines(reader)) {
				if (line.contains("Total num ruptures: ")) {
					done = true;
					break;
				}
			}
			if (!done)
				continue;
//			System.out.println("Loading "+catEntry.getName());
			
			List<ETAS_EqkRupture> catalog;
			try {
				catalog = ETAS_CatalogIO.loadCatalog(zip.getInputStream(catEntry), minGriddedMag);
			} catch (Exception e) {
				continue;
			}
			
			if (catalogNames != null)
				catalogNames.add(entry.getName());
			catalogs.add(catalog);
		}
		
		return catalogs;
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
		
		for (int i = 0; i < catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			List<Double> singleLosses = Lists.newArrayList();
			List<DiscretizedFunc> lossDists = Lists.newArrayList();
			
			if (triggeredOnly)
				catalog = ETAS_SimAnalysisTools.getChildrenFromCatalog(catalog, id_for_scenario);
			
			for (ETAS_EqkRupture rup : catalog) {
				int fssIndex = getFSSIndex(rup);
				
				double mag = rup.getMag();
				
				if (fssIndex >= 0) {
					// fault based source
					double solMag = meanSol.getRupSet().getMagForRup(fssIndex);
					Preconditions.checkState((float)mag == (float)solMag, "Bad fault mag! %s != %s", mag, solMag);
					if (condLossDists[fssIndex].size() == 0)
						continue;
					lossDists.add(condLossDists[fssIndex]);
					// make sure weights sum to 1
					double sumY = 0;
					for (Point2D pt : condLossDists[fssIndex])
						sumY += pt.getY();
					Preconditions.checkState((float)sumY == 1f, "rup losses don't sum to 1: "+(float)sumY);
				} else {
					// grid source
					double loss = calcGridSourceLoss(rup, region, griddedMagLossDists, "Catalog "+i);
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
					expectedNum *= lossDist.size();
				
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
			
//			double meanLoss = 0d;
//			for (Point2D pt : func)
//				meanLoss += pt.getX()*pt.getY();
			
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
				Preconditions.checkState((float)sumY == 1f || condLossDists[fssIndex].size()==0,
						"rup losses don't sum to 1: "+(float)sumY+" ("+condLossDists[fssIndex].size()+"");
				triggerLoss = meanLoss;
			} else {
				// grid source
				triggerLoss = calcGridSourceLoss(triggerRup, region, griddedMagLossDists, "TRIGGER");
			}
			System.out.println("Trigger M"+(float)mag+" rupture loss: "+triggerLoss);
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
					if (magLossDist.size() >= 2)
						binWidth = magLossDist.getX(magLossDist.size()-1)
							- magLossDist.getX(magLossDist.size()-2);
					else
						binWidth = 0d;
					if (mag < magLossDist.getMaxX()+binWidth) {
						System.out.println("Above but within last bin, returning that");
						return magLossDist.getY(magLossDist.size()-1);
					} else {
						System.out.println("Above mag loss dist, returning zero");
						return 0d;
					}
				}
				System.out.println("Interpolating losses for trigger rupture");
				return magLossDist.getInterpolatedY(mag);
			}
			if (rup.getNthERF_Index() >= 0) {
				int nth = rup.getNthERF_Index();
				int sourceID = erf.getSrcIndexForNthRup(nth);
				System.out.println("Using Nth ERF Index ("+nth+", src="+sourceID+") instead of catalog location");
				Preconditions.checkState(sourceID >= 0);
				ProbEqkSource source = erf.getSource(sourceID);
				Location sourceLoc = null;
				if (source instanceof PointSource13b)
					sourceLoc = ((PointSource13b)source).getLocation();
				else if (source instanceof Point2Vert_FaultPoisSource)
					sourceLoc = ((Point2Vert_FaultPoisSource)source).getLocation();
				else
					System.out.println("Unkown grid source type, skipping: "+source.getClass().getName());
				if (sourceLoc != null) {
					double dist = LocationUtils.horzDistance(sourceLoc, loc);
					Preconditions.checkState(dist < 15d, "Source location via Nth index is wrong! "
							+dist+" km away, catLoc="+loc+", sourceLoc="+sourceLoc);
					nodeIndex = region.indexForLocation(sourceLoc);
					magLossDist = griddedMagLossDists[nodeIndex];
					magIndex = UCERF3_BranchAvgLossFetcher.getMatchingXIndexFloatPrecision(mag, magLossDist);
					if (magIndex < 0) {
						xVals = Lists.newArrayList();
						yVals = Lists.newArrayList();
						for (Point2D pt : magLossDist) {
							xVals.add((float)pt.getX());
							yVals.add((float)pt.getY());
						}
						System.out.println("Same problem with Nth rup node ("+sourceLoc+"), skipping"
								+"\n\tNode mags:\t"+Joiner.on("\t").join(xVals)
								+"\n\tNode loss:\t"+Joiner.on("\t").join(yVals));
						return 0;
					}
					double loss = magLossDist.getY(magIndex);
					return loss;
				}
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
	
	public static HistogramFunction getLossHist(List<DiscretizedFunc> catalogDists, double delta, boolean isLog10) {
		int num = -1;
		Range range = null;
		while (num == -1 || num == 1) {
			range = calcSmartHistRange(catalogDists, delta, isLog10);
			num = (int)Math.round((range.getUpperBound()-range.getLowerBound())/delta) + 1;
			if (num == 1)
				delta /= 2d;
		}
		return getLossHist(catalogDists, range.getLowerBound(), num, delta, isLog10);
	}
	
	/**
	 * Calculates a nicely rounded range that will exactly cover the data range with the given discretization.
	 * Histogram num points can be calculated as num = (range.getUpperBound()-range.getLowerBound())/delta + 1;
	 * @param delta
	 * @return
	 */
	public static Range calcSmartHistRange(List<DiscretizedFunc> catalogDists, double delta, boolean isLog10) {
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
	
	public static HistogramFunction getLossHist(List<DiscretizedFunc> catalogDists,
			double minX, int numX, double deltaX, boolean isLog10) {
		HistogramFunction lossHist = new HistogramFunction(minX, numX, deltaX);
		
		double distMin = minX - 0.5*deltaX;
		double distMax = lossHist.getMaxX() + 0.5*deltaX;
		
		double weightMult = 1d/catalogDists.size();
		
		double[] lossVals = new double[catalogDists.size()];
		
		for (int i = 0; i < catalogDists.size(); i++) {
			DiscretizedFunc catalogDist = catalogDists.get(i);
			double meanVal = 0d;
			for (int index=0; index<catalogDist.size(); index++) {
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
					lossHist.add(lossHist.size()-1, weight);
				else if (loss < distMin || lossHist.size() == 1)
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
		Preconditions.checkState(DataUtils.getPercentDiff(sumY, 1d) < 0.1, "loss hist sum of y vals doesn't equal 1: "+(float)sumY);
		
		System.out.println("Mean loss: "+StatUtils.mean(lossVals));
		System.out.println("Median loss: "+DataUtils.median(lossVals));
		
		return lossHist;
	}
	
	public static void writeLossHist(File outputDir, String outputPrefix, HistogramFunction lossHist, boolean isLogX,
			boolean triggeredOnly, String xAxisLabel, double xAxisScale, double maxX) throws IOException {
		if (!outputDir.exists())
			outputDir.mkdir();
		
//		double logMinY = (1d/catalogs.size())/10d; // one order of magnitude below the smallest possible
		double logMinY = 1e-4;
		
		List<PlotElement> elems = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		if (xAxisScale != 1d && xAxisScale != 0d) {
			HistogramFunction newLossHist = new HistogramFunction(
					lossHist.getMinX()*xAxisScale, lossHist.size(), lossHist.getDelta()*xAxisScale);
			System.out.println("Scaled minX: "+newLossHist.getMinX()+" maxX="+newLossHist.getMaxX());
			for (int i=0; i<lossHist.size(); i++)
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
			gp.getChartPanel().setSize(1000, 800);
			String myPrefix = outputPrefix;
			if (logY)
				myPrefix += "_logY";
			if (triggeredOnly)
				myPrefix += "_triggered";
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
			gp.saveAsTXT(new File(outputDir, myPrefix+".txt").getAbsolutePath());
		}
		
		// now loss excedence
		HistogramFunction cumDist = lossHist.getCumulativeDistFunctionWithHalfBinOffset();
		// convert to exceedance
		for (int i=0; i<cumDist.size(); i++)
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
			gp.getChartPanel().setSize(1000, 800);
			String myPrefix = outputPrefix+"_exceed";
			if (logY)
				myPrefix += "_logY";
			if (triggeredOnly)
				myPrefix += "_triggered";
			gp.saveAsPNG(new File(outputDir, myPrefix+".png").getAbsolutePath());
			gp.saveAsPDF(new File(outputDir, myPrefix+".pdf").getAbsolutePath());
			gp.saveAsTXT(new File(outputDir, myPrefix+".txt").getAbsolutePath());
		}
	}
	
	public void writeLossesToCSV(File csvFile, List<DiscretizedFunc> catalogLosses) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		double cutoffMag = AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF;
		csv.addLine("Index", "Total Mean Loss", "# FSS Ruptures",
				"# M>="+(float)cutoffMag, "Max Mag");
		
		for (int i=0; i<catalogs.size(); i++) {
			double totLoss = 0d;
			for (Point2D pt : catalogLosses.get(i))
				totLoss += pt.getX()*pt.getY();
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
			csv.addLine(i+"", totLoss+"", numFSSRups+"", numAbove+"", maxMag+"");
		}
		
		csv.writeToFile(csvFile);
	}
	
	private void scenarioSearch() {
		// this method can be used to search for certain scenarios
		
		// fault based in LA box but not SAF
		Region region = new CaliforniaRegions.LA_BOX();
		HashSet<Integer> sectIDs = new HashSet<Integer>();
		HashSet<Integer> safParents = new HashSet<Integer>(
				FaultModels.FM3_1.getNamedFaultsMapAlt().get("San Andreas"));
		
		FaultSystemRupSet rupSet = meanSol.getRupSet();
		
		long ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		
		sectLoop:
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
			if (safParents.contains(sect.getParentSectionId()))
				continue;
			for (Location loc : sect.getFaultTrace()) {
				if (region.contains(loc)) {
					sectIDs.add(sect.getSectionId());
					continue sectLoop;
				}
			}
		}
		
		for (int i=0; i<catalogs.size(); i++) {
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			rupLoop:
			for (ETAS_EqkRupture rup : catalog) {
				int fssIndex = getFSSIndex(rup);
				if (fssIndex >= 0) {
					List<FaultSectionPrefData> data = rupSet.getFaultSectionDataForRupture(fssIndex);
					for (int sectID : rupSet.getSectionsIndicesForRup(fssIndex)) {
						if (sectIDs.contains(sectID)) {
							String name = data.size()+" SECTIONS BETWEEN "+data.get(0).getName()
									+" AND "+data.get(data.size()-1).getName();
							float mag = (float)rup.getMag();
							float deltaDays = (float)((rup.getOriginTime()-ot)/1000d/60d/60d/24d);
							System.out.println("catalog "+i+" has a M"+mag+" match "+deltaDays+" days after on: "+name);
							continue rupLoop;
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, DocumentException {
//		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-la_habra/");
//		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-mojave_7/");
//		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_05_28-spontaneous/");
//		File parentDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_06_26-mojave_7/");
		
		// get result dirs
//		List<File> resultDirs = Lists.newArrayList();
//		File[] subDirs = parentDir.listFiles();
//		Arrays.sort(subDirs, new FileNameComparator());
//		for (File subDir : subDirs) {
//			if (subDir.isDirectory() && subDir.getName().startsWith("results"))
//				resultDirs.add(subDir);
//		}
//		File[] etasCatalogsDirs = resultDirs.toArray(new File[0]);
		
//		File zipFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_09_02-mojave_7/results.zip");
//		File zipFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_09_02-napa/results.zip");
//		File zipFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/2014_09_02-spontaneous/results.zip");
		File resultsFile;
		if (args.length == 1)
			resultsFile = new File(args[0]);
		else
			resultsFile = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin");
//				+ "2016_02_25-surprise_valley_5p0-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14/results_descendents.bin");
		
		boolean triggeredOnly = true;
		
		// true mean FSS which includes rupture mapping information. this must be the exact file used to calculate EALs
		File trueMeanSolFile = new File("dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_"
				+ "COMPOUND_SOL_TRUE_HAZARD_MEAN_SOL_WITH_MAPPING.zip");

		// directory which contains EAL data
		File ealMainDir = new File("/home/kevin/OpenSHA/UCERF3/eal");
		if (!ealMainDir.exists())
			ealMainDir = new File("/home/scec-02/kmilner/ucerf3/eal");
		Preconditions.checkState(ealMainDir.exists(), "Directory doesn't exist: %s", ealMainDir.getAbsolutePath());
		
		// constants for all catalogs
//		double xAxisScale = 1d/1e6; // portfolio units are in thousands (1e3), so convert to billions by dividing by 1e6
		String xAxisLabel = "$ (Billions)";
		double maxX = 200;
		double deltaX = 1e6;
		double thousandsToBillions = 1d/1e6; // portfolio units are in thousands (1e3), so convert to billions by dividing by 1e6
		
		double inflationScalar = 1d/0.9d;
		
		double xAxisScale = thousandsToBillions*inflationScalar;
		
		List<File> dataDirs = Lists.newArrayList();
		
		dataDirs.add(new File(ealMainDir, "2014_05_28-ucerf3-99percent-wills-smaller"));
		dataDirs.add(new File(ealMainDir, "2016_06_06-ucerf3-90percent-wald"));
		
		// CEA proxy wald
//		File dataDir = new File(ealMainDir, "2014_05_05-ucerf3-eal-calc-wald-vs30");
//		String xAxisLabel = "$ (Billions)";
//		double xAxisScale = 1d/1e6; // portfolio units are in thousands (1e3), so convert to billions by dividing by 1e6
//		double maxX = 120;
//		double deltaX = 1e6;
//		String catOutputDirName = "cea_proxy_wald";
		
//		// 99% Wills
//		File dataDir = new File(ealMainDir, "2014_05_28-ucerf3-99percent-wills-smaller");
//		
//		
//		String catOutputDirName = "ca_99_wills";
		
		// Fatality portfolio
//		File dataDir = new File(ealMainDir, "2014_05_28-ucerf3-fatality-smaller");
//		String xAxisLabel = "Fatalities";
//		double xAxisScale = 1d;
//		double maxX = 3000;
//		double deltaX = 1;
//		String catOutputDirName = "fatalities_wills";

		// IMR for which EAL data has already been computed
		Map<AttenRelRef, Double> imrWeightsMap = Maps.newHashMap();
		imrWeightsMap.put(AttenRelRef.CB_2014, 0.22);
		imrWeightsMap.put(AttenRelRef.CY_2014, 0.22);
		imrWeightsMap.put(AttenRelRef.ASK_2014, 0.22);
		imrWeightsMap.put(AttenRelRef.BSSA_2014, 0.22);
		imrWeightsMap.put(AttenRelRef.IDRISS_2014, 0.12);

		// Fault model of interest
		FaultModels fm = FaultModels.FM3_1;

		// Branch averaged FSS
		FaultSystemSolution baSol = FaultSystemIO.loadSol(
				new File("dev/scratch/UCERF3/data/scratch/"
				+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_"
				+ "COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));

		// Compound fault system solution
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(
				new File("dev/scratch/UCERF3/data/scratch/"
						+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip"));
		
		File lossOutputDir = new File(resultsFile.getParentFile(), "loss_results");
		Preconditions.checkState(lossOutputDir.exists() || lossOutputDir.mkdir());
		
		List<List<DiscretizedFunc>> lossDistsList = Lists.newArrayList();
		List<Double> lossWeights = Lists.newArrayList();
		
		TestScenario scenario = ETAS_MultiSimAnalysisTools.detectScenario(resultsFile.getParentFile());
		if (scenario != null && scenario.getFSS_Index() >= 0)
			scenario.updateMag(baSol.getRupSet().getMagForRup(scenario.getFSS_Index()));
		
		if (resultsFile.getParentFile().getName().startsWith("2016_02_19-mojave")) {
			System.out.println("Changing scenario ID");
			id_for_scenario = 9893;
		} else {
			id_for_scenario = 0;
		}
		
		List<List<ETAS_EqkRupture>> catalogs = null;
		
		ETAS_CatalogEALCalculator calc = null;
		
		boolean isLog10 = false; // x axis
		
		for (int i=0; i<dataDirs.size(); i++) {
			File dataDir = dataDirs.get(i);
			System.out.println("Handling data dir: "+dataDir.getAbsolutePath());
			
			UCERF3_BranchAvgLossFetcher fetcher = new UCERF3_BranchAvgLossFetcher(trueMeanSolFile, cfss, dataDir);
			
			if (catalogs == null) {
				calc = new ETAS_CatalogEALCalculator(fetcher, baSol, fm, resultsFile);
				catalogs = calc.catalogs;
			} else {
				calc = new ETAS_CatalogEALCalculator(fetcher, baSol, fm, catalogs);
			}
			calc.setTriggeredOnly(triggeredOnly);
			if (scenario.getFSS_Index() >= 0)
				calc.setTriggerFaultRup(scenario.getFSS_Index());
			else
				calc.setTriggerGridRup(scenario.getLocation(), scenario.getMagnitude());
			
			List<List<DiscretizedFunc>> myLossDists = Lists.newArrayList();
			List<Double> myWeights = Lists.newArrayList();
			
			File outputDir = new File(lossOutputDir, dataDir.getName());
			if (!outputDir.exists())
				outputDir.mkdir();
			
			for (AttenRelRef attenRelRef : imrWeightsMap.keySet()) {
				double imrWeight = imrWeightsMap.get(attenRelRef);
				
				System.out.println("Calculating catalog losses");
				List<DiscretizedFunc> lossDists = calc.getLossDists(attenRelRef);
				
				myLossDists.add(lossDists);
				myWeights.add(imrWeight);
				
				HistogramFunction lossHist = getLossHist(lossDists, deltaX, isLog10);
//				writeLossHist(outputDir, attenRelRef.name(), lossHist, isLog10);
				writeLossHist(outputDir, attenRelRef.name(), lossHist, isLog10, triggeredOnly, xAxisLabel, xAxisScale, maxX);
				
				calc.writeLossesToCSV(new File(outputDir, attenRelRef.name()+"_losses.csv"), lossDists);
			}
			
			// combined for all atten rels
			if (imrWeightsMap.size() > 1) {
				List<DiscretizedFunc> imrCombined = getCombinedLossDists(myLossDists, myWeights);
				HistogramFunction lossHist = getLossHist(imrCombined, deltaX, isLog10);
				writeLossHist(outputDir, "gmpes_combined", lossHist, isLog10, triggeredOnly, xAxisLabel, xAxisScale, maxX);
				calc.writeLossesToCSV(new File(outputDir, "gmpes_combined_losses.csv"), imrCombined);
			}
			
			lossDistsList.addAll(myLossDists);
			lossWeights.addAll(myWeights);
		}
		// combine
		if (lossDistsList.size() > 1) {
			File outputDir = new File(lossOutputDir, "combined");
			if (!outputDir.exists())
				outputDir.mkdir();
			
			List<DiscretizedFunc> combined = getCombinedLossDists(lossDistsList, lossWeights);
			HistogramFunction lossHist = getLossHist(combined, deltaX, isLog10);
			writeLossHist(outputDir, "gmpes_combined", lossHist, isLog10, triggeredOnly, xAxisLabel, xAxisScale, maxX);
			calc.writeLossesToCSV(new File(outputDir, "gmpes_combined_losses.csv"), combined);
		}
	}
	
	private static List<DiscretizedFunc> getCombinedLossDists(List<List<DiscretizedFunc>> lossDistsList, List<Double> lossWeights) {
		List<DiscretizedFunc> combinedLosses = Lists.newArrayList();
		
		for (int i=0; i<lossDistsList.get(0).size(); i++)
			combinedLosses.add(new ArbitrarilyDiscretizedFunc());
		
		double totWeight = 0d;
		for (double weight : lossWeights)
			totWeight += weight;
		
		for (int i=0; i<lossDistsList.size(); i++) {
			List<DiscretizedFunc> lossDists = lossDistsList.get(i);
			double weight = lossWeights.get(i)/totWeight;
			
			Preconditions.checkState(lossDists.size() == combinedLosses.size());
			
			for (int n=0; n<combinedLosses.size(); n++) {
				DiscretizedFunc combined = combinedLosses.get(n);
				for (Point2D pt : lossDists.get(n)) {
					double x = pt.getX();
					double y = pt.getY()*weight;
					int xInd = UCERF3_BranchAvgLossFetcher.getMatchingXIndexFloatPrecision(x, combined);
					if (xInd < 0)
						combined.set(x, y);
					else
						combined.set(x, y + combined.getY(xInd));
				}
			}
		}
		
		return combinedLosses;
	}

}
