package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import java.util.List;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultType;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class FaultERF extends AbstractERF {

	private String name;
	private List<FaultSource> sources;
	private List<ProbEqkSource> sourcesAsEqs;

	FaultERF(String name, List<FaultSource> sources) {
		this.name = name;
		this.sources = sources;

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
	
	List<FaultSource> getSources() { return sources; } 

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
		System.out.println("Update forecast: " + getName() + " " + getNumSources() + " " + count);
	}

	@Override
	public String getName() {
		return name;
	}
	
	public static void main(String[] args) {
		
//		FaultERF erf = new FaultERF();
	}

}
