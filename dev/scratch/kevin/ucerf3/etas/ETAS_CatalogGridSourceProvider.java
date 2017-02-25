package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.griddedSeismicity.GridReader;

public class ETAS_CatalogGridSourceProvider extends AbstractGridSourceProvider {
	
	private static final CaliforniaRegions.RELM_TESTING_GRIDDED region = 
			new CaliforniaRegions.RELM_TESTING_GRIDDED();
	
	private static final double MIN_MAG = 5.05;
	private static final double DELTA_MAG = 0.1;
	private static final int NUM_MAG = (int)((9.05 - MIN_MAG)/DELTA_MAG) + 1;
	private static final double MIN_CATALOG_MAG = MIN_MAG - 0.5*DELTA_MAG;
	
	private GriddedRegion highResRegion;
	// high res to low res
	private Map<Integer, Integer> nodeIndexMap;
	
	private Map<Integer, IncrementalMagFreqDist> nodeMFDs;
	
	// these are indexed to the low res region
	private double[] fracStrikeSlip,fracNormal,fracReverse;
	
	public ETAS_CatalogGridSourceProvider(List<List<ETAS_EqkRupture>> catalogs, double resolution) {
		Preconditions.checkState(resolution <= region.getSpacing());
		
		if (resolution == region.getSpacing())
			highResRegion = region;
		else
			highResRegion = new GriddedRegion(region, resolution, region.getLocation(0));
		
		initMFDs(catalogs);
		initFochMechs();
		initIndexMap();
	}
	
	private void initMFDs(List<List<ETAS_EqkRupture>> catalogs) {
		nodeMFDs = Maps.newHashMap();
		
		double rateEach = 1d/catalogs.size();
		
		int numSkipped = 0;
		int tot = 0;
		
		int printMod = 1000;
		if (catalogs.size() > 10000)
			printMod = 10000;
		System.out.println("Initializing ETAS gridded MFDs for "+catalogs.size()+" catalogs");
		for (int i=0; i<catalogs.size(); i++) {
			if (i % printMod == 0)
				System.out.println("Processing catalog "+i);
			List<ETAS_EqkRupture> catalog = catalogs.get(i);
			for (ETAS_EqkRupture rup : catalog) {
				if (rup.getMag() < MIN_CATALOG_MAG || rup.getFSSIndex() >= 0)
					continue;
				Location loc = rup.getHypocenterLocation();
				int node = highResRegion.indexForLocation(loc);
				tot++;
				if (node < 0) {
					numSkipped++;
					continue;
				}
				IncrementalMagFreqDist mfd = nodeMFDs.get(node);
				if (mfd == null) {
					mfd = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
					nodeMFDs.put(node, mfd);
				}
				int magIndex = mfd.getClosestXIndex(rup.getMag());
				// TODO deal with bias of particularly productive sequences?
				mfd.add(magIndex, rateEach);
			}
		}
		
		System.out.println("Skipped "+numSkipped+"/"+tot+" gridded ruptures outside of region");
	}
	
	private void initFochMechs() {
		GridReader gRead;
		gRead = new GridReader("StrikeSlipWts.txt");
		fracStrikeSlip = gRead.getValues();
		gRead = new GridReader("ReverseWts.txt");
		fracReverse = gRead.getValues();
		gRead = new GridReader("NormalWts.txt");
		fracNormal = gRead.getValues();
	}
	
	private void initIndexMap() {
		nodeIndexMap = Maps.newHashMap();
		
		double bufferDegrees = 2d*region.getSpacing();
		
		int numHighRes = highResRegion.getNodeCount();
		
		System.out.println("Fixing index mappings");
		int numFixed = 0;
		for (int i=0; i<numHighRes; i++) {
			Location loc = highResRegion.locationForIndex(i);
			int mapped = region.indexForLocation(loc);
			if (mapped < 0) {
				// edge case, no direct mapping
				double minLat = loc.getLatitude() - bufferDegrees;
				double maxLat = loc.getLatitude() + bufferDegrees;
				double minLon = loc.getLongitude() - bufferDegrees;
				double maxLon = loc.getLongitude() + bufferDegrees;
				
				int closestIndex = -1;
				double closestDist = Double.POSITIVE_INFINITY;
				for (int j=0; j<region.getNodeCount(); j++) {
					Location loc2 = region.getLocation(j);
					if (loc2.getLatitude() < maxLat && loc2.getLatitude() > minLat
							&& loc2.getLongitude() < maxLon && loc2.getLongitude() > minLon) {
						double dist = LocationUtils.horzDistanceFast(loc, loc2);
						if (dist < closestDist) {
							closestDist = dist;
							closestIndex = j;
						}
					}
				}
				Preconditions.checkState(closestIndex >= 0, "Couldn't fix index mapping for index %s: %s", i, loc);
				numFixed++;
				mapped = closestIndex;
			}
			nodeIndexMap.put(i, mapped);
		}
		double percent = 100d*(double)numFixed/(double)numHighRes;
		System.out.println("Fixed "+numFixed+"/"+numHighRes+" ("+(float)percent+" %) index mappings");
	}

	@Override
	public IncrementalMagFreqDist getNodeUnassociatedMFD(int idx) {
		return nodeMFDs.get(idx);
	}

	@Override
	public IncrementalMagFreqDist getNodeSubSeisMFD(int idx) {
		// always null, treat ever rup as unsassociated
		return null;
	}

	@Override
	public GriddedRegion getGriddedRegion() {
		return highResRegion;
	}
	
	private int lowResIndex(int index) {
		Integer mapped = nodeIndexMap.get(index);
		if (mapped == null)
			return -1;
		return mapped;
	}

	@Override
	public double getFracStrikeSlip(int idx) {
		return fracStrikeSlip[lowResIndex(idx)];
	}

	@Override
	public double getFracReverse(int idx) {
		return fracReverse[lowResIndex(idx)];
	}

	@Override
	public double getFracNormal(int idx) {
		return fracNormal[lowResIndex(idx)];
	}
	
	public AbstractERF buildGriddedERF() {
		final List<Integer> sourceIndexes = Lists.newArrayList(nodeMFDs.keySet());
		final ETAS_CatalogGridSourceProvider gridProv = this;
		
		return new AbstractERF() {
			
			@Override
			public String getName() {
				return "ETAS Catalog Gridded ERF";
			}
			
			@Override
			public void updateForecast() {}
			
			@Override
			public ProbEqkSource getSource(int idx) {
				return gridProv.getSource(sourceIndexes.get(idx), 1d, false, BackgroundRupType.POINT);
			}
			
			@Override
			public int getNumSources() {
				return sourceIndexes.size();
			}
		};
	}
	
	public static void main(String[] args) throws IOException {
		File etasCatalogs = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations/"
				+ "2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k/"
				+ "results_descendents_m5_preserve.bin");
		List<List<ETAS_EqkRupture>> catalogs = ETAS_CatalogIO.loadCatalogsBinary(etasCatalogs, 5d);
		
		ETAS_CatalogGridSourceProvider gridded = new ETAS_CatalogGridSourceProvider(catalogs, 0.01);
		int numHighRes = gridded.highResRegion.getNodeCount();
		System.out.println("Created "+gridded.nodeMFDs.size()+" MFDs at "+numHighRes+" possible nodes");
	}

}
