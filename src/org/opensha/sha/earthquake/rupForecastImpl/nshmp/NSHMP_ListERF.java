package org.opensha.sha.earthquake.rupForecastImpl.nshmp;

import static com.google.common.base.Preconditions.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.source.NSHMP_ERF;
import org.opensha.sha.nshmp.SourceRegion;
import org.opensha.sha.nshmp.SourceType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Custom {@code EpistemicListERF} implementation for NSHAMP. Implementation
 * allows subsets of nested {@code ERF}s to be retreived by {@code SourceType}
 * or {@code SourceRegion} and provides {@code add()} and {@code get()} methods
 * that take and return a {@code NSHMP_ERF} type, respectively.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public abstract class NSHMP_ListERF extends AbstractEpistemicListERF implements
		Iterable<NSHMP_ERF> {

	private String name;
	private Multimap<SourceType, NSHMP_ERF> typeMap;
	private Multimap<SourceRegion, NSHMP_ERF> regionMap;
	
	/**
	 * Instantiates a new ERF backed by the supplied Map
	 * @param name for this ERF
	 */
	public NSHMP_ListERF(String name) {
		checkArgument(StringUtils.isNotBlank(name),
			"Must supply a name for this ERF");
		this.name = name;
		typeMap = ArrayListMultimap.create();
		regionMap = ArrayListMultimap.create();
		
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(1);
		timeSpan.addParameterChangeListener(this);
	}

	/**
	 * Add the supplied {@code NSHMP_ERF} to the internal list of {@code ERF}s.
	 * @param type of {@code ERF} to add
	 * @param erf to add
	 */
	protected void addERF(NSHMP_ERF erf) {
		checkNotNull(erf, "Supplied ERF is null");
		typeMap.put(erf.getSourceType(), erf);
		regionMap.put(erf.getSourceRegion(), erf);
		super.addERF(erf, erf.getSourceWeight());
	}

	/**
	 * Add the supplied {@code NSHMP_ERF}s to the internal list of {@code ERF}s.
	 * @param type of {@code ERF} to add
	 * @param erfs to add
	 */
	protected void addERFs(List<? extends NSHMP_ERF> erfs) {
		checkNotNull(erfs, "Supplied ERF list is null");
		for (NSHMP_ERF erf : erfs) {
			addERF(erf);
		}
	}
	
	@Override
	public NSHMP_ERF getERF(int idx) {
		return (NSHMP_ERF) super.getERF(idx);
	}

//	@Override
//	public void updateForecast() {
//		for (List<ERF> erfList : erf_List.values()) {
//			for (ERF erf : erfList) {
//				erf.setTimeSpan(timeSpan);
//				erf.updateForecast();
//			}
//		}
//	}

	@Override
	public String getName() {
		return name;
	}

	public int getRuptureCount() {
		int count = 0;
		for (int i=0; i<getNumERFs(); i++) {
			count += getERF(i).getRuptureCount();
		}
		return count;
	}
	
	/**
	 * Returns the number or sources contained in the ERF at idx.
	 * @param idx
	 * @return
	 */
	public int getSourceCount(int idx) {
		return getERF(idx).getNumSources();
	}
	
	/**
	 * Returns the total number of sources that this {@code ERF} represents.
	 * @return the total number of {@code ERF}s
	 */
	public int getSourceCount() {
		int count = 0;
		for (ERF erf : erf_List) {
			count += erf.getNumSources();
		}
		return count;
	}

	/**
	 * Returns the total number of sources of the specified type.
	 * @param type requested
	 * @return the total number of the specified type
	 */
	public int getNumSources(SourceType type) {
		Collection<NSHMP_ERF> erfList = typeMap.get(type);
		if (erfList == null) return 0;
		int count = 0;
		for (ERF erf : erfList) {
			count += erf.getNumSources();
		}
		return count;
	}
	
	@Override
	public Iterator<NSHMP_ERF> iterator() {
		return new Iterator<NSHMP_ERF>() {
			Iterator<ERF> it = erf_List.iterator();
			// @formatter:off
			@Override public boolean hasNext() { return it.hasNext(); }
			@Override public NSHMP_ERF next() { return (NSHMP_ERF) it.next(); }
			@Override public void remove() { it.remove(); }
			// @formatter:off
		};
	}

	// rebuild a param list
	private void initParams() {
	}

	/**
	 * Returns the
	 * @param type
	 * @return
	 */
	protected void addSourceParam(SourceType type, Parameter<?> param) {
//		paramMgr.addSourceParam(type, param);
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {

		// need to propogate time span changes among others
		// for any group disable -- disable children
	}
	
	
	
}
