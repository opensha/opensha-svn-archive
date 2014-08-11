/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with the Southern California
 * Earthquake Center (SCEC, http://www.scec.org) at the University of Southern
 * California and the UnitedStates Geological Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.opensha.sha.earthquake;

import java.util.List;

/**
 * This is the base interface for an Earthquake Rupture Forecast</b> <br>
 * 
 * @author Nitin Gupta
 * @author Vipin Gupta
 * @version $Id$
 */

public interface ERF extends BaseERF, Iterable<ProbEqkSource> {

	/**
	 * 
	 * @return the total number os sources
	 */
	public int getNumSources();

	/**
	 * Returns the list of all earthquake sources.
	 * @return list of all possible earthquake sources
	 */
	public List<ProbEqkSource> getSourceList();

	/**
	 * Returns the earthquake source at the supplied index.
	 * @param idx the index requested
	 * @return the source at <code>idx</code>
	 */
	public ProbEqkSource getSource(int idx);

	/**
	 * Returns the number of ruptures associated wit the source at the supplied
	 * index.
	 * @param idx the index requested
	 * @return the number of ruptures associated with the source at
	 *         <code>idx</code>
	 */
	public int getNumRuptures(int idx);

	/**
	 * Returns the rupture at the supplied source index and rupture index.
	 * @param srcIdx source index requested
	 * @param rupIdx rupture index requested
	 * @return the rupture at <code>rupIdx</code> associated with the source at
	 *         <code>srcIdx</code>
	 */
	public ProbEqkRupture getRupture(int srcIdx, int rupIdx);

	/**
	 * Returns a random set of ruptures.
	 * @return a random set of ruptures
	 */
	public List<EqkRupture> drawRandomEventSet();
	
	
	
	/**
	 * This returns the nth rup indices for the given source
	 */
	public int[] get_nthRupIndicesForSource(int iSource);

	
	/**
	 * This returns the total number of ruptures (the sum of all ruptures in all sources)
	 */
	public int getTotNumRups();
	
	/**
	 * This returns the nth rupture index for the given source and rupture index
	 * (where the latter is the rupture index within the source)
	 */	
	public int getIndexN_ForSrcAndRupIndices(int s, int r);
	
	/**
	 * This returns the source index for the nth rupture
	 * @param nthRup
	 * @return
	 */
	public int getSrcIndexForNthRup(int nthRup);

	/**
	 * This returns the rupture index (with its source) for the
	 * given nth rupture.
	 * @param nthRup
	 * @return
	 */
	public int getRupIndexInSourceForNthRup(int nthRup);
	
	/**
	 * This returns the nth rupture in the ERF
	 * @param n
	 * @return
	 */
	public ProbEqkRupture getNthRupture(int n);

}
