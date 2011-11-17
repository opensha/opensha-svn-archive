package org.opensha.sha.nshmp;

import static com.google.common.base.Preconditions.*;

import java.util.Map;

import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.util.TectonicRegionType;

import com.google.common.collect.Maps;

/**
 * This is a container class for various configuration settings relating to
 * hazard calculations. Specifically it manages:
 * <ul>
 * <li>Distance cutoff by {@link TectonicRegionType}s (TRT)</li>
 * <li>Distance cutoff by magnitude</li>
 * <li>Association of an {@link IntensityMeasureRelationship} with a TRT</li>
 * </ul>
 * The dual distance cutoff settings <b>Distance cutoffs:</b> In the absence of
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcSettings {

	private Map<TectonicRegionType, Double> trt_dist_map;
	private Map<TectonicRegionType, ScalarIMR> imr_trt_map;

	public HazardCalcSettings() {
		trt_dist_map = Maps.newEnumMap(TectonicRegionType.class);
		for (TectonicRegionType trt : TectonicRegionType.values()) {
			trt_dist_map.put(trt, trt.defaultCutoffDist());
		}
		imr_trt_map = Maps.newEnumMap(TectonicRegionType.class);

	}

	// IMRs can supply applicable TRT - do we restrict in this class

	/** THe default distance */
	public static final double DEFAULT_DIST = 1000;

	/**
	 * Sets the maximum distance for which a calculation involving the supplied
	 * magnitude should proceed.
	 * @param mag
	 */
	public void setDistance(double distance, double mag) {

	}

	/**
	 * @param type
	 */
	public void setDistance(double distance, TectonicRegionType type) {

	}

	/**
	 * 
	 * @param type
	 * @param mag
	 * @return
	 */
	public boolean doCalc(double distance, TectonicRegionType type, double mag) {
		return false;
	}

	/**
	 * Sets the {@link IntensityMeasureRelationship} to use for the supplied
	 * {@link TectonicRegionType}.
	 * @param trt <code>TectonicRegionType</code> to set
	 * @param imr to associate with the supplied <code>trt</code>
	 * @throws NullPointerException if either <code>trt</code> or
	 *         <code>imr</code> are <code>null</code>
	 * @throws IllegalArgumentException if the supplied <code>imr</code> does
	 *         not support the specified <code>trt</code>
	 * @see ScalarIMR#isTectonicRegionSupported(TectonicRegionType)
	 */
	public void set(TectonicRegionType trt, ScalarIMR imr) {
		checkNotNull(trt, "Supplied TRT must not be null");
		checkNotNull(imr, "Supplied IMR must not be null");
		checkArgument(imr.isTectonicRegionSupported(trt),
			"Supplied IMR does not support the specified TRT");
		imr_trt_map.put(trt, imr);
	}

	/**
	 * Returns the {@code ScalarIMR} associated with the supplied
	 * {@code TectonicRegionType}.
	 * @param type to lookup
	 * @return the associated IMR or <code>null</code> if there is no entry for
	 *         the supplied <code>type</code>
	 * @throws NullPointerException if supplied <code>type</code> is
	 *         <code>null</code>
	 */
	public ScalarIMR getIMR(TectonicRegionType type) {
		checkNotNull(type, "Supplied TRT must not be null");
		return imr_trt_map.get(type);
	}

}
