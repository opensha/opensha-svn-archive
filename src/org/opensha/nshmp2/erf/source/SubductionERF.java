package org.opensha.nshmp2.erf.source;

import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.nshmp2.util.NSHMP_Utils;
import org.opensha.nshmp2.util.SourceIMR;
import org.opensha.nshmp2.util.SourceRegion;
import org.opensha.nshmp2.util.SourceType;
import org.opensha.sha.earthquake.ProbEqkSource;

import com.google.common.collect.Lists;

/**
 * The ERF for NHSMP subduction (Cascadia) sources.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class SubductionERF extends NSHMP_ERF {

	private String name;
	private List<SubductionSource> sources;
	private List<ProbEqkSource> sourcesAsEqs;
	private SourceRegion srcRegion;
	private SourceIMR srcIMR;
	private double weight;
	private double maxR;
	private Region bounds;

	SubductionERF(String name, List<SubductionSource> sources,
		SourceRegion srcRegion, SourceIMR srcIMR, double weight, double maxR) {
		this.name = name;
		this.sources = sources;
		this.srcRegion = srcRegion;
		this.srcIMR = srcIMR;
		this.weight = weight;
		this.maxR = maxR;

		// nshmp defaults
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(1);
		timeSpan.addParameterChangeListener(this);
	}

	@Override
	public int getNumSources() {
		return sources.size();
	}

	@Override
	public List<ProbEqkSource> getSourceList() {
		if (sourcesAsEqs == null) {
			sourcesAsEqs = Lists.newArrayList();
			for (ProbEqkSource pes : sources) {
				sourcesAsEqs.add(pes);
			}
		}
		return sourcesAsEqs;
	}

	@Override
	public ProbEqkSource getSource(int idx) {
		return sources.get(idx);
	}

	@Override
	public void updateForecast() {
		int count = 0;
		for (FaultSource source : sources) {
			source.init();
			count += source.getNumRuptures();
		}
		initBounds();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getRuptureCount() {
		int count = 0;
		for (FaultSource source : sources) {
			count += source.getNumRuptures();
		}
		return count;
	}

	@Override
	public SourceRegion getSourceRegion() {
		return srcRegion;
	}

	@Override
	public SourceType getSourceType() {
		return SourceType.SUBDUCTION;
	}

	@Override
	public SourceIMR getSourceIMR() {
		return srcIMR;
	}
	
	@Override
	public double getSourceWeight() {
		return weight;
	}
	
	@Override
	public double getMaxDistance() {
		return maxR;
	}

	@Override
	public Region getBounds() {
		return bounds;
	}
	
	private void initBounds() {
		double minLat = Double.MAX_VALUE;
		double maxLat = Double.MIN_VALUE;
		double minLon = Double.MAX_VALUE;
		double maxLon = Double.MIN_VALUE;
		for (FaultSource source : sources) {
			LocationList locs = source.getAllSourceLocs();
			minLat = Math.min(minLat, LocationUtils.calcMinLat(locs));
			maxLat = Math.min(maxLat, LocationUtils.calcMinLat(locs));
			minLon = Math.min(minLon, LocationUtils.calcMinLat(locs));
			maxLon = Math.min(maxLon, LocationUtils.calcMinLat(locs));
		}
		bounds = NSHMP_Utils.creatBounds(minLat, maxLat, minLon, maxLon, maxR);
	}

}
