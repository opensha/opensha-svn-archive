package org.opensha.sha.util;

import java.util.HashMap;
import java.util.Map;

import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;

public class TRTUtils {

	public static HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>
			wrapInHashMap(ScalarIntensityMeasureRelationshipAPI imr) {
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap =
			new HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>();
		// The type of tectonic region here is of no consequence (it just a dummy value)
		imrMap.put(TectonicRegionType.ACTIVE_SHALLOW, imr);
		return imrMap;
	}
	
	public static ScalarIntensityMeasureRelationshipAPI getIMRForTRT(
			Map<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap,
			TectonicRegionType trt) {
		ScalarIntensityMeasureRelationshipAPI imr;
		if(imrMap.size()>1) {
			imr = imrMap.get(trt);
			// now set the tectonic region in the imr if it supports this type 
			// (because it might support multiple Tectonic Regions), otherwise
			// do nothing to force it to take the ruptures anyway (and to avoid an exception)
			// what if it support two other types, but not this one?????????????????????
			if(imr.isTectonicRegionSupported(trt.toString()))  {
				imr.getParameter(TectonicRegionTypeParam.NAME).setValue(trt.toString());					  
			} else { // set to the default value
				imr.getParameter(TectonicRegionTypeParam.NAME).setValueAsDefault();
			}

		} else {  // only one IMR, so force all sources to be used with this one and assume the TectonicRegionTypeParam has already been set (e.g., in the gui)
			imr = imrMap.values().iterator().next();		// long winded way of getting the single imr		  
		}
		return imr;
	}
}
