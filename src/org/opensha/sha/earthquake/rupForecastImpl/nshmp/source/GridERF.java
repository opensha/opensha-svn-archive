package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static com.google.common.base.Preconditions.*;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.NSHMP_Utils.totalMoRate;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.util.DataUtils;
//import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

/**
 * Internally, all values required to define an MFD may be spatially varying.
 * Hopwever, arrays for such values are only initialized if necessary.
 * 
 * Very little data checking is performed so user should ensure that any MFD
 * dependent values (a, b, M) are correct prior to initialization.
 * 
 * TODO need to provide for immutability; user can set bVal or bVals but not
 * both; likewise, once one or the other is set, neither can be set again
 * 
 * TODO HazCurveCalc iterates over sources to get total numRups; this
 * instantiates and then discards PoissVertSS sources once before actually using
 * them
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class GridERF extends AbstractERF {

	private String name;
	private GriddedRegion region;
	private List<IncrementalMagFreqDist> mfds;
	private int[] srcIndices;
	private double ssMechWt, norMechWt, revMechWt; // TODO kill, pass mechWtMap to PointSource
	private Map<FocalMech, Double> mechWtMap;
	private double[] depths;
	
	private final static MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();

	// mfd data
	private Double aVal, bVal, minMag, maxMag;
	private double[] aVals, bVals, minMags, maxMags;

	GridERF(String name, GriddedRegion region,
		List<IncrementalMagFreqDist> mfds, double[] depths, Map<FocalMech, Double> mechWtMap) {
		this.name = name;
		this.region = region;
		this.mfds = mfds;
		ssMechWt = mechWtMap.get(FocalMech.STRIKE_SLIP); // mechWts[0];
		revMechWt = mechWtMap.get(FocalMech.REVERSE); //mechWts[1];
		norMechWt = mechWtMap.get(FocalMech.NORMAL); //mechWts[2];
		this.mechWtMap = mechWtMap;
		this.depths = depths;
		initIndices();
		
		// nshmp defaults
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(1);
		timeSpan.addParameterChangeListener(this);
	}

	private void initIndices() {
		// srcIndices.length == # of non-null mfds; the stored value points
		// to the mfd and location index
		List<Integer> list = Lists.newArrayList();
		for (int i = 0; i < mfds.size(); i++) {
			if (mfds.get(i) != null) list.add(i);
		}
		srcIndices = Ints.toArray(list);
	}
	
	public LocationList getNodes() {
		LocationList nodes = new LocationList();
		for (int i : srcIndices) {
			nodes.add(region.locationForIndex(i));
		}
		return nodes;
	}

	/**
	 * returns the grridded region associated with this ERF.
	 * @return the gridded region
	 */
	public GriddedRegion getRegion() {
		return region;
	}
	
	@Override
	public int getNumSources() {
		return srcIndices.length;
	}

	/**
	 * Method not supported to conserve memory.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ProbEqkSource> getSourceList() {
		throw new UnsupportedOperationException(
			"A GridSource does not allow access to the list "
				+ "of all possible sources.");
	}

	@Override
	public ProbEqkSource getSource(int idx) {
//		System.out.println(timeSpan);
//		System.out.println(printIthSourceInputs(idx));
//		System.out.println();
		// translate index
//		System.out.println("1: " + idx);
		idx = srcIndices[idx];
//		System.out.println("2: " + idx);
//		return new Point2Vert_FaultPoisSource(region.locationForIndex(idx),
//			mfds.get(idx), magLenRel, 0.0, timeSpan.getDuration(), 6.0,
//			ssMechWt, norMechWt, revMechWt);
		return new PointSource(region.locationForIndex(idx),
			mfds.get(idx), timeSpan.getDuration(), depths, mechWtMap);
	}
	
	/**
	 * Returns the magnitude-frequency distribution at the specified location.
	 * @param loc
	 * @return the MFD at the supplied location or <code>null</code> if none
	 *         exists
	 */
	public IncrementalMagFreqDist getMFD(Location loc) {
		int idx = region.indexForLocation(loc);
		return (idx == -1) ? null : mfds.get(idx);
	}

	@Override
	public void updateForecast() {

		System.out.println("Update forecast: " + getName() + " " +
			getNumSources());
		// TODO do nothing
		// check parameterChangeflag
	}

	@Override
	public String getName() {
		return name;
	}

	private String printIthSourceInputs(int idx) {
		StringBuilder sb = new StringBuilder(getName());
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append("Src idx: ").append(idx);
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(region.locationForIndex(idx));
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(mfds.size()).append(mfds.get(idx));
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(magLenRel);
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(0.0);
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(timeSpan.getDuration());
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(6.0);
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(ssMechWt);
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(norMechWt);
		sb.append(IOUtils.LINE_SEPARATOR).append("\t");
		sb.append(revMechWt);
		return sb.toString();
	}

	// ///////////// Initializers ///////////////

	/**
	 * Sets internal a-value to the supplied value. This value is used in the
	 * absence of spatially varying a-values.
	 * @param aVal to set
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed a-value has been set already
	 */
	public void set_A_Val(double aVal) {
		checkSupported(aValIsSet(), A_VAL_SET_ERR);
		this.aVal = aVal;
	}

	/**
	 * Populates internal array of a-values with supplied array (via copy).
	 * @param aVals to set
	 * @throws IllegalArgumentException if a-value array is not the same size as
	 *         the associated gridded region
	 * @throws NullPointerException if supplied array is <code>null</code>
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed a-value has been set already
	 */
	public void set_A_Vals(double[] aVals) {
		checkSupported(aValIsSet(), A_VAL_SET_ERR);
		checkNotNull(aVals, "Supplied a-value array must not be null");
		checkArgument(aVals.length == region.getNodeCount(),
			"Supplied a-value array does not match region size");
		this.aVals = Arrays.copyOf(aVals, aVals.length);
	}

	/**
	 * Sets internal b-value to the supplied value. This value is used in the
	 * absence of spatially varying b-values.
	 * @param bVal to set
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed b-value has been set already
	 */
	public void set_B_Val(double bVal) {
		checkSupported(bValIsSet(), B_VAL_SET_ERR);
		this.bVal = bVal;
	}

	/**
	 * Populates internal array of b-values with supplied array (via copy).
	 * @param bVals to set
	 * @throws IllegalArgumentException if b-value array is not the same size as
	 *         the associated gridded region
	 * @throws NullPointerException if supplied array is <code>null</code>
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed b-value has been set already
	 */
	public void set_B_Vals(double[] bVals) {
		checkSupported(bValIsSet(), B_VAL_SET_ERR);
		checkNotNull(bVals, "Supplied b-value array must not be null");
		checkArgument(bVals.length == region.getNodeCount(),
			"Supplied b-value array does not match region size");
		this.bVals = Arrays.copyOf(bVals, bVals.length);
	}

	/**
	 * Sets then min M value to the supplied value. This value is used in the
	 * absence of spatially varying min magnitudes.
	 * @param minMag to set
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed minMag has been set already
	 */
	public void setMinMag(double minMag) {
		checkSupported(minMagIsSet(), M_MIN_SET_ERR);
		this.minMag = minMag;
	}

	/**
	 * Populates internal array of min M values with supplied array (via copy).
	 * @param minMags to set
	 * @throws IllegalArgumentException if min M array is not the same size as
	 *         the associated gridded region
	 * @throws NullPointerException if supplied array is <code>null</code>
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed minMag has been set already
	 */
	public void setMinMags(double[] minMags) {
		checkSupported(minMagIsSet(), M_MIN_SET_ERR);
		checkNotNull(minMags, "Supplied a-value array must not be null");
		checkArgument(minMags.length == region.getNodeCount(),
			"Supplied a-value array does not match region size");
		this.minMags = Arrays.copyOf(minMags, minMags.length);
	}

	/**
	 * Sets internal max M value to the supplied value. This value is used in
	 * the absence of spatially varying max magnitudes.
	 * @param maxMag to set
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed maxMag has been set already
	 */
	public void setMaxMag(double maxMag) {
		checkSupported(maxMagIsSet(), M_MAX_SET_ERR);
		this.maxMag = maxMag;
	}

	/**
	 * Populates internal array of min M values with supplied array (via copy).
	 * @param maxMags to set
	 * @throws IllegalArgumentException if max M array is not the same size as
	 *         the associated gridded region
	 * @throws NullPointerException if supplied array is <code>null</code>
	 * @throws UnsupportedOperationException if either the single or
	 *         array-backed maxMag has been set already
	 */
	public void setMaxMags(double[] maxMags) {
		checkSupported(maxMagIsSet(), M_MAX_SET_ERR);
		checkNotNull(maxMags, "Supplied a-value array must not be null");
		checkArgument(maxMags.length == region.getNodeCount(),
			"Supplied a-value array does not match region size");
		this.maxMags = Arrays.copyOf(maxMags, maxMags.length);
	}

	// ///////////// State Checking ///////////////

	private static final String A_VAL_SET_ERR = "a-values are already set";
	private static final String B_VAL_SET_ERR = "b-values are already set";
	private static final String M_MIN_SET_ERR = "Min M values are already set";
	private static final String M_MAX_SET_ERR = "Max M values are already set";

	private void checkSupported(boolean condition, String message) {
		if (condition) throw new UnsupportedOperationException(message);
	}

	private boolean aValIsSet() {
		return this.aVal != null || aVals != null;
	}

	private boolean bValIsSet() {
		return this.bVal != null || bVals != null;
	}

	private boolean minMagIsSet() {
		return this.minMag != null || minMags != null;
	}

	private boolean maxMagIsSet() {
		return this.maxMag != null || maxMags != null;
	}

	public static GridERF getTestGrid() {
		String name = "Small Test GridERF";
		Location loc = new Location(35.15, -90.05); // NEHRP_TestCity.MEMPHIS.location();
		double d = 0.01;
		GriddedRegion region = new GriddedRegion(
			new Location(loc.getLatitude() - d, loc.getLongitude() - d),
			new Location(loc.getLatitude() + d, loc.getLongitude() + d),
			0.05, GriddedRegion.ANCHOR_0_0);
//		System.out.println(region);
//		System.out.println(region.getNodeCount());
//		System.out.println(region.getNumLocations());
//		System.out.println(region.locationForIndex(0));
//		0.95 5.0 7.0 0.1 3.0
		GR_Data gr = new GR_Data(1e-5, 0.95, 5.0, 7.0, 0.1);
		double tmr = totalMoRate(gr.mMin, gr.nMag, gr.dMag, gr.aVal, gr.bVal);
		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(
			gr.mMin, gr.nMag, gr.dMag);
		// set total moment rate
		mfd.setAllButTotCumRate(gr.mMin, gr.mMin + (gr.nMag - 1) * gr.dMag,
			tmr, gr.bVal);
		List<IncrementalMagFreqDist> mfds = Lists.newArrayList();
		mfds.add(mfd);
		double[] mechWts = {1d, 0d, 0d};
		Map<FocalMech, Double> mechMap = Maps.newEnumMap(FocalMech.class);
		mechMap.put(FocalMech.STRIKE_SLIP, 0.5);
		mechMap.put(FocalMech.REVERSE, 0.0);
		mechMap.put(FocalMech.NORMAL, 0.5);
		GridERF gerf = new GridERF(name, region, mfds, new double[] {5,1}, mechMap);
		return gerf;
	}

}
