package org.opensha.sha.nshmp;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Add comments here // TODO getNumSources(Location) ?
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public abstract class MultiSourceERF extends AbstractERF {

	private String name;
	private Map<SourceType, List<ERF>> erfMap;
	private SourceParams paramMgr;

	/**
	 * Instantiates a new ERF backed by the supplied Map
	 * @param name for this ERF
	 */
	public MultiSourceERF(String name) {
		checkArgument(StringUtils.isNotBlank(name),
			"Must supply a name for this ERF");
		this.name = name;
		erfMap = Maps.newEnumMap(SourceType.class);
		paramMgr = new SourceParams();
		
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(1);
		timeSpan.addParameterChangeListener(this);
	}

	/**
	 * Add the supplied Earthquake Rupture Forecast to the internal list of
	 * <code>ERF</code>s.
	 * @param type of <code>ERF</code> to add
	 * @param erf to add
	 */
	protected void addERF(SourceType type, ERF erf) {
		checkNotNull(type, "Supplied source type is null");
		checkNotNull(erf, "Supplied ERF is null");
		List<ERF> erfList = erfMap.get(type);
		if (erfList == null) {
			erfList = Lists.newArrayList();
			erfMap.put(type, erfList);
		}
		erfList.add(erf);
		
	}

	/**
	 * Add the supplied Earthquake Rupture Forecasts to the internal list of
	 * <code>ERF</code>s
	 * @param type of <code>ERF</code> to add
	 * @param erfs to add
	 */
	protected void addERFs(SourceType type, List<? extends ERF> erfs) {
		checkNotNull(erfs, "Supplied ERF list is null");
		for (ERF erf : erfs) {
			addERF(type, erf);
		}
	}

	@Override
	public void updateForecast() {
		for (List<ERF> erfList : erfMap.values()) {
			for (ERF erf : erfList) {
				erf.setTimeSpan(timeSpan);
				erf.updateForecast();
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNumSources() {
		int count = 0;
		for (SourceType type : erfMap.keySet()) {
			count += getNumSources(type);
		}
		return count;
	}

	/**
	 * Returns the total number of sources of the specified type.
	 * @param type requested
	 * @return the total number of the specified type
	 */
	public int getNumSources(SourceType type) {
		List<ERF> erfList = erfMap.get(type);
		if (erfList == null) return 0;
		int count = 0;
		for (ERF erf : erfList) {
			count += erf.getNumSources();
		}
		return count;
	}

	@Override
	public ProbEqkSource getSource(int idx) {
		// EnumMap always iterates in enum order
		int srcCount = 0;
		for (SourceType type : erfMap.keySet()) {
			List<ERF> erfList = erfMap.get(type);
			for (ERF erf : erfList) {
				int nextCount = srcCount + erf.getNumSources();
				if (idx < nextCount) {
					return erf.getSource(idx - srcCount);
				}
				srcCount = nextCount;
			}
		}
		return null; // shouldn't get here
	}

	@Override
	public List<ProbEqkSource> getSourceList() {
		return null;
		// if (srcList == null) {
		// srcList = Lists.newArrayList();
		// for (SourceType type : srcMap.keySet()) {
		// srcList.addAll(srcMap.get(type));
		// }
		// }
		// return srcList;
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
		paramMgr.addSourceParam(type, param);
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {

		// need to propogate time span changes among others
		// for any group disable -- disable children
	}

}
