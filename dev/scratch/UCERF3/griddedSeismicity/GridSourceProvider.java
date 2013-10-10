package scratch.UCERF3.griddedSeismicity;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * Interface implemented by providers of gridded (sometimes referred to as
 * 'other') seismicity sources.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public interface GridSourceProvider {

	/**
	 * Returns the number of sources in the provider.
	 * @return the number of sources
	 */
	public int size();

	/**
	 * Returne the source at {@code index}.
	 * @param index of source to retrieve
	 * @param duration of forecast
	 * @param filterAftershocks
	 * @param crosshair sources if true,
	 * @return the source at {@code index}
	 */
	public ProbEqkSource getSource(int index, double duration,
			boolean filterAftershocks, BackgroundRupType bgRupType);
	
//	/**
//	 * Set whether all sources should just be treated as point sources, not just
//	 * those with M&leq;6.0
//	 * 
//	 * @param usePoints
//	 */
//	public void setAsPointSources(boolean usePoints);

	/**
	 * Returns the unassociated MFD of a grid node.
	 * @param idx node index
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeUnassociatedMFD(int idx);
	
	/**
	 * Returns the sub-seismogenic MFD associated with a grid node, if any
	 * exists.
	 * @param idx node index
	 * @return the MFD
	 */
	public IncrementalMagFreqDist getNodeSubSeisMFD(int idx);
	
	/**
	 * Returns the gridded region associated with these grid sources.
	 * @return the gridded region
	 */
	public GriddedRegion getGriddedRegion();

}
