package org.opensha.nshmp2.erf.source;

import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.nshmp.SourceIMR;
import org.opensha.sha.nshmp.SourceRegion;
import org.opensha.sha.nshmp.SourceType;

/**
 * Parent class for NSHMP ERFs that supplies 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public abstract class NSHMP_ERF extends AbstractERF {

	/**
	 * Returns the total rupture count for this ERF.
	 * @return the rupture count
	 */
	public abstract int getRuptureCount();
	
	/**
	 * Returns the {@code SourceType}
	 * @return the {@code SourceType}
	 * @see SourceType
	 */
	public abstract SourceType getSourceType();

	public abstract SourceRegion getSourceRegion();
	
	public abstract SourceIMR getSourceIMR();
	
	public abstract double getSourceWeight();
	
	public abstract double getMaxDistance();
	
	
	
	// GridERF        <-- List<IncrMFD>
	// FaultERF       <-- List<FaultSource>
	// SubductionERF  <-- List<SubductionSource extends FaultSource>
	// ClusterERF     <-- List<ClusterSource>  <-- List<FaultSource>
	
}
