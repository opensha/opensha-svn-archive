package org.opensha.sha.earthquake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.util.TectonicRegionType;

public class ERFTestSubset implements ERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	HashMap<Integer, Integer> sourceIDMap = new HashMap<Integer, Integer>();
	
	private AbstractERF baseERF;
	
	/** fields for nth rupture info */
	protected int totNumRups=-1;
	protected ArrayList<int[]> nthRupIndicesForSource;	// this gives the nth indices for a given source
	protected int[] srcIndexForNthRup;
	protected int[] rupIndexForNthRup;
	
	public ERFTestSubset(AbstractERF baseERF) {
		this.baseERF = baseERF;
	}

	@Override
	public ParameterList getAdjustableParameterList() {
		return baseERF.getAdjustableParameterList();
	}

	@Override
	public Region getApplicableRegion() {
		return baseERF.getApplicableRegion();
	}

	@Override
	public ArrayList<TectonicRegionType> getIncludedTectonicRegionTypes() {
		return baseERF.getIncludedTectonicRegionTypes();
	}

	@Override
	public String getName() {
		return baseERF.getName() + "_TEST";
	}

	@Override
	public TimeSpan getTimeSpan() {
		return baseERF.getTimeSpan();
	}

	@Override
	public void setParameter(String name, Object value) {
		baseERF.setParameter(name, value);
	}

	@Override
	public void setTimeSpan(TimeSpan time) {
		baseERF.setTimeSpan(time);
	}

	@Override
	public String updateAndSaveForecast() {
		return baseERF.updateAndSaveForecast();
	}

	@Override
	public void updateForecast() {
		baseERF.updateForecast();
	}

	@Override
	public ArrayList<EqkRupture> drawRandomEventSet() {
		throw new RuntimeException("WARNING: drawRandomEventSet not implemented for test ERF!");
	}

	@Override
	public int getNumRuptures(int iSource) {
		// TODO Auto-generated method stub
		return baseERF.getNumRuptures(getBaseSourceID(iSource));
	}

	@Override
	public int getNumSources() {
		return sourceIDMap.size();
	}

	@Override
	public ProbEqkRupture getRupture(int iSource, int nRupture) {
		return baseERF.getRupture(getBaseSourceID(iSource), nRupture);
	}

	@Override
	public ProbEqkSource getSource(int iSource) {
		return baseERF.getSource(getBaseSourceID(iSource));
	}
	
	private int getBaseSourceID(int sourceID) {
		return sourceIDMap.get(new Integer(sourceID));
	}

	@Override
	public ArrayList<ProbEqkSource> getSourceList() {
		ArrayList<ProbEqkSource> sources = new ArrayList<ProbEqkSource>();
		for (int i=0; i<getNumSources(); i++) {
			sources.add(getSource(i));
		}
		return sources;
	}
	
	public void includeSource(int sourceID) {
		if (sourceIDMap.containsValue(new Integer(sourceID))) {
			System.out.println("source "+sourceID+" already included!");
			return; // it's already included
		}
		if (sourceID < 0 || sourceID >= baseERF.getNumSources())
			throw new IndexOutOfBoundsException("source ID to include is out of bounds!");
		int newID = this.getNumSources();
		sourceIDMap.put(new Integer(newID), new Integer(sourceID));
	}
	
	public ProbEqkSource getOrigSource(int sourceID) {
		return baseERF.getSource(sourceID);
	}
	
	@Override
	public int compareTo(BaseERF o) {
		return baseERF.compareTo(o);
	}

	@Override
	public Iterator<ProbEqkSource> iterator() {
		return getSourceList().iterator();
	}
	
	/**
	 * This returns the nth rup indices for the given source
	 */
	@Override
	public int[] get_nthRupIndicesForSource(int iSource) {
		return baseERF.get_nthRupIndicesForSource(iSource);
	}
	
	/**
	 * This returns the total number of ruptures (the sum of all ruptures in all sources)
	 */
	@Override
	public int getTotNumRups() {
		return baseERF.getTotNumRups();
	}
	
	/**
	 * This returns the nth rupture index for the given source and rupture index
	 * (where the latter is the rupture index within the source)
	 */	
	@Override
	public int getIndexN_ForSrcAndRupIndices(int s, int r) {
		return baseERF.getIndexN_ForSrcAndRupIndices(s, r);
	}
	
	/**
	 * This returns the source index for the nth rupture
	 * @param nthRup
	 * @return
	 */
	@Override
	public int getSrcIndexForNthRup(int nthRup) {
		return baseERF.getSrcIndexForNthRup(nthRup);
	}

	/**
	 * This returns the rupture index (with its source) for the
	 * given nth rupture.
	 * @param nthRup
	 * @return
	 */
	@Override
	public int getRupIndexInSourceForNthRup(int nthRup) {
		return baseERF.getRupIndexInSourceForNthRup(nthRup);
	}
	
	/**
	 * This returns the nth rupture in the ERF
	 * @param n
	 * @return
	 */
	@Override
	public ProbEqkRupture getNthRupture(int n) {
		return baseERF.getNthRupture(n);
	}
	
	/**
	 * This sets the following: totNumRups, totNumRupsFromFaultSystem, nthRupIndicesForSource,
	 * srcIndexForNthRup[], rupIndexForNthRup[], fltSysRupIndexForNthRup[]
	 * 
	 */
	protected void setAllNthRupRelatedArrays() {
		baseERF.setAllNthRupRelatedArrays();
	}
	
	/**
	 * This checks whether what's returned from get_nthRupIndicesForSource(s) gives
	 *  successive integer values when looped over all sources.
	 *  TODO move this to a test class?
	 *  
	 */
	public void testNthRupIndicesForSource() {
		baseERF.testNthRupIndicesForSource();
	}
	


}
