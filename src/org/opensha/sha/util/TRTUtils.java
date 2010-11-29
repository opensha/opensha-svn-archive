package org.opensha.sha.util;

import java.util.HashMap;
import java.util.Map;

import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;

public class TRTUtils {

	/**
	 * This wraps a single IMR in a HashMap with a single TRT, Active Shallow.
	 * 
	 * @param imr - Intensity Measure Relationship to wrap
	 * @return mapping of IMR's to TRT's with only a single mapping of active shallow to the 
	 * given IMR.
	 */
	public static HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>
			wrapInHashMap(ScalarIntensityMeasureRelationshipAPI imr) {
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap =
			new HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>();
		// The type of tectonic region here is of no consequence (it just a dummy value)
		imrMap.put(TectonicRegionType.ACTIVE_SHALLOW, imr);
		return imrMap;
	}
	
	/**
	 * This will return the IMR for the given Tectonic Region Type. If the map has only
	 * a single mapping, the first (and only) IMR in the map is returned without checking
	 * that the Tectonic Region Types match.
	 * 
	 * The TRT is also set in the IMR. If the IMR doesn't support the TRT, then the TRT param
	 * is set as default.
	 * 
	 * @param imrMap - Mapping of IMR's to TRT's
	 * @param trt - Tectonic Region Type for which to retrieve an IMR
	 * @return Single IMR with TRT param set
	 */
	@SuppressWarnings("unchecked")
	public static ScalarIntensityMeasureRelationshipAPI getIMRForTRT(
			Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap,
			TectonicRegionType trt) {
		
		if (trt == null)
			// TODO maybe figure out another way to handle this?
			throw new IllegalArgumentException("Tectonic Region Type cannot be null!");
		
		ScalarIntensityMeasureRelationshipAPI imr;
		if(imrMap.size()>1) {
			imr = imrMap.get(trt);
		} else {  // only one IMR, so force all sources to be used with this one and assume the TectonicRegionTypeParam has already been set (e.g., in the gui)
			imr = getFirstIMR(imrMap);		// long winded way of getting the single imr		  
		}
		setTRTinIMR(imr, trt);
		return imr;
	}
	
	/**
	 * Sets the TRT param in the given IMR. If the IMR doesn't support the TRT (determined by
	 * <code>imr.isTectonicRegionSupported(trt.toString())</code>) then the TRT param is set
	 * to it's default value.
	 * 
	 * @param imr - Intensity Measure Relationship in which to set the TRT
	 * @param trt - Tectonic Region Type to set in the IMR
	 */
	public static void setTRTinIMR(ScalarIntensityMeasureRelationshipAPI imr, TectonicRegionType trt) {
		// now set the tectonic region in the imr if it supports this type 
		// (because it might support multiple Tectonic Regions), otherwise
		// do nothing to force it to take the ruptures anyway (and to avoid an exception)
		// what if it support two other types, but not this one?????????????????????
		if(imr.isTectonicRegionSupported(trt.toString()))  {
			imr.getParameter(TectonicRegionTypeParam.NAME).setValue(trt.toString());					  
		} else { // set to the default value
			imr.getParameter(TectonicRegionTypeParam.NAME).setValueAsDefault();
		}
	}
	
	/**
	 * Simply returns the first IMR in the map by calling <code>imrMap.values().iterator().next()</code>.
	 * This is useful for returning the only IMR out of a map with a single TRT.
	 * 
	 * @param imrMap - Mapping of IMR's to TRT's
	 * @return first IMR in the map
	 */
	public static ScalarIntensityMeasureRelationshipAPI getFirstIMR(
			Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap) {
		return imrMap.values().iterator().next();
	}
}
