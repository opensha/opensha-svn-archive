package org.opensha.sha.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;

import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.SiteDataValue;
import org.opensha.data.siteType.util.SiteDataTypeParameterNameMap;
import org.opensha.param.*;
import org.opensha.sha.imr.*;
import org.opensha.sha.imr.attenRelImpl.*;

/**
 * <p>Title: SiteTranslator</p>
 * <p>Description: This object sets the value of a site parameter from one or more
 * of the following types of site data:<p>
 * <UL>
 * <LI> Vs30 (average shear-wave velocity in the upper 30 meters of a site)
 * <LI> Wills Site Type (Wills et al., 2000, BSSA, v. 90, S187-S208)
 * <LI> Basin-Depth-2.5 (the depth in m where the shear-wave velocity equals 2.5 km/sec)
 * <LI> Basin-Depth-1.0 (the depth in m where the shear-wave velocity equals 1.0 km/sec)
 * </UL>
 * <p>The conversions from the Wills Site Types (E, DE, D, CD, C, BC, B) and basin-depth
 * are given below (NA means nothing is set).  All of these translations were authorized
 * by the attenuation-rlationship authors (except for Sadigh, who used a dataset similar
 * to Abrahamson & Silve (1997) so that translation is applied).  The main method tests
 * the translations of all currently implemented attenuation-relationship site-related
 * parameters.<p>
 *
 *
 * AS_1997_AttenRel.SITE_TYPE_NAME (Abrahamson & Silva (1997) & Abrahamson (2000))<p>
 * <UL>
 * <LI> NA 				if E
 * <LI> Deep-Soil			if DE, D, or CD
 * <LI> Rock/Shallow-Soil		if C, BC, or B
 * </UL>
 *
 * SCEMY_1997_AttenRel.SITE_TYPE_NAME (Sadigh et al. (1997)):<p>
 * <UL>
 * <LI> NA 				if E
 * <LI> Deep-Soil			if DE, D, or CD
 * <LI> Rock		                if C, BC, or B
 * </UL>
 *
 * AttenuationRelationship.VS30_NAME (Boore et al. (1997) & Field (2000))<p>
 * <LI> <UL>
 * <LI> Vs30 = NA			if E
 * <LI> Vs30 = 180			if DE
 * <LI> Vs30 = 270			if D
 * <LI> Vs30 = 360			if CD
 * <LI> Vs30 = 560			if C
 * <LI> Vs30 = 760			if BC
 * <LI> Vs30 = 1000			if B
 * <LI> </UL>
 *
 * Campbell_1997_AttenRel.SITE_TYPE_NAME (Campbell (1997))<p>
 * <UL>
 * <LI> NA 				if E
 * <LI> Firm-Soil			if DE, D, or CD
 * <LI> Soft-Rock			if C
 * <LI> Hard-Rock			if BC or B
 * </UL>
 *
 * Campbell_1997_AttenRel.BASIN_DEPTH_NAME (Campbell (1997))<p>
 * <UL>
 * <LI> Campbell-Basin-Depth = NaN      if E
 * <LI> Campbell-Basin-Depth = 0.0      if B or BC
 * <LI> Campbell-Basin-Depth = 1.0      if C
 * <LI> Campbell-Basin-Depth = 5.0      if CD, D, or DE
 * </UL>
 *
 * Field_2000_AttenRel.BASIN_DEPTH_NAME (Field (2000))<p>
 * <UL>
 * <LI> Basin-Depth-2.5 = Basin-Depth-2.5
 * </UL>
 *
 * CB_2003_AttenRel.SITE_TYPE_NAME (Campbell & Bozorgnia (2003))<p>
 * <UL>
 * <LI> NA 			if E
 * <LI> Firm-Soil		if DE or D
 * <LI> Very-Firm-Soil	        if CD
 * <LI> Soft-Rock		if C
 * <LI> BC-Bounday              if BC
 * <LI> Firm-Rock		if B�
 * </UL>
 *
 * ShakeMap_2003_AttenRel.WILLS_SITE_NAME (ShakeMap (2003))<p>
 * <LI> <UL>
 * <LI> E                      if E
 * <LI> DE                     if DE
 * <LI> D                      if D
 * <LI> CD                     if CD
 * <LI> C                      if C
 * <LI> BC                     if BC
 * <LI> B                      if B
 * </UL>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field, Nitin Gupta, Vipin Gupta, and Kevin Milner
 * @version 1.0
 */

public class SiteTranslator
implements java.io.Serializable {

	private final static String C = "SiteTranslator";
	private final static boolean D = false;

	public final static String WILLS_B = "B";
	public final static String WILLS_BC = "BC";
	public final static String WILLS_C = "C";
	public final static String WILLS_CD = "CD";
	public final static String WILLS_D = "D";
	public final static String WILLS_DE = "DE";
	public final static String WILLS_E = "E";
	
	public final static HashMap<String, Double> wills_vs30_map = new HashMap<String, Double>();
	
	static {
		wills_vs30_map.put(WILLS_B,		1000d);
		wills_vs30_map.put(WILLS_BC,	760d);
		wills_vs30_map.put(WILLS_C,		560d);
		wills_vs30_map.put(WILLS_CD,	360d);
		wills_vs30_map.put(WILLS_D,		270d);
		wills_vs30_map.put(WILLS_DE,	180d);
		wills_vs30_map.put(WILLS_E,		Double.NaN);
	}
	
	/**
	 * Translates a Wills Site classification to a Vs30 value
	 * <LI> <UL>
	 * <LI> Vs30 = NA			if E
	 * <LI> Vs30 = 180			if DE
	 * <LI> Vs30 = 270			if D
	 * <LI> Vs30 = 360			if CD
	 * <LI> Vs30 = 560			if C
	 * <LI> Vs30 = 760			if BC
	 * <LI> Vs30 = 1000			if B
	 * <LI> </UL>
	 * 
	 * @param wills
	 * @return
	 */
	public static double getVS30FromWillsClass(String wills) {
		if (wills_vs30_map.keySet().contains(wills))
			return wills_vs30_map.get(wills);
		else
			return Double.NaN;
	}
	
	/**
	 * Returns a String representation of the Wills Class -> Vs30 translation table
	 * 
	 * @return
	 */
	public static String getWillsVs30TranslationString() {
		String str = "";
		
		for (String wills : wills_vs30_map.keySet()) {
			if (str.length() > 0)
				str += "\n";
			str += wills + "\t=>\t" + wills_vs30_map.get(wills);
		}
		
		return str;
	}
	
	public static SiteDataTypeParameterNameMap DATA_TYPE_PARAM_NAME_MAP = createMap();

	public SiteTranslator() {
		
	}
	
	/**
	 * Creates a mapping between site data types and site parameter names. This is useful if you
	 * want to see if a given site data type can be used to set a given parameter.
	 * 
	 * @return
	 */
	private static SiteDataTypeParameterNameMap createMap() {
		SiteDataTypeParameterNameMap map = new SiteDataTypeParameterNameMap();
		
		/*				params that can be set from raw VS 30 only						*/
		// ...NONE!
		
		/*				params that can be set from Wills Classes only					*/
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	Campbell_1997_AttenRel.BASIN_DEPTH_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	ShakeMap_2003_AttenRel.WILLS_SITE_NAME);
		
		/*				params common to Vs30 and Wills Classes							*/
		map.addMapping(SiteDataAPI.TYPE_VS30,			AttenuationRelationship.VS30_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	AttenuationRelationship.VS30_NAME);
		map.addMapping(SiteDataAPI.TYPE_VS30,			AttenuationRelationship.VS30_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	AttenuationRelationship.VS30_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_VS30,			AS_1997_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	AS_1997_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_VS30,			SadighEtAl_1997_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	SadighEtAl_1997_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_VS30,			Campbell_1997_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	Campbell_1997_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_VS30,			CB_2003_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	CB_2003_AttenRel.SITE_TYPE_NAME);
		map.addMapping(SiteDataAPI.TYPE_VS30,			CS_2005_AttenRel.SOFT_SOIL_NAME);
		map.addMapping(SiteDataAPI.TYPE_WILLS_CLASS,	CS_2005_AttenRel.SOFT_SOIL_NAME);
		
		/*				params that can be set from Depth to Vs = 2.5 KM/sec			*/
		map.addMapping(SiteDataAPI.TYPE_DEPTH_TO_2_5,	AttenuationRelationship.DEPTH_2pt5_NAME);
		map.addMapping(SiteDataAPI.TYPE_DEPTH_TO_2_5,	Field_2000_AttenRel.BASIN_DEPTH_NAME);
		map.addMapping(SiteDataAPI.TYPE_DEPTH_TO_2_5,	Campbell_1997_AttenRel.BASIN_DEPTH_NAME);
		
		/*				params that can be set from Depth to Vs = 1.0 KM/sec			*/
		map.addMapping(SiteDataAPI.TYPE_DEPTH_TO_1_0,	AttenuationRelationship.DEPTH_1pt0_NAME);
		
		return map;
	}

	/**
	 * Method to set a site parameter from a single site data value
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return true if the parameter was set, false otherwise
	 */
	public boolean setParameterValue(ParameterAPI param, SiteDataValue<?> data) {
		return setParameterValue(param, data, D);
	}
	
	/**
	 * Method to set a site parameter from a single site data value
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return true if the parameter was set, false otherwise
	 */
	public boolean setParameterValue(ParameterAPI param, SiteDataValue<?> data, boolean debug) {
		ArrayList<SiteDataValue<?>> datas = new ArrayList<SiteDataValue<?>>();
		datas.add(data);
		return setParameterValue(param, datas, debug);
	}
	
	/**
	 * Method to set a site parameter from a given set of site data.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return true if the parameter was set, false otherwise
	 */
	public boolean setParameterValue(ParameterAPI param, Collection<SiteDataValue<?>> datas) {
		return setParameterValue(param, datas, D);
	}

	/**
	 * Method to set a site parameter from a given set of site data. The first site data value
	 * in the set that can be used to set the parameter will be used.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return true if the parameter was set, false otherwise
	 */
	public boolean setParameterValue(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		String paramName = param.getName();
		if (debug) System.out.println("setSiteParamsForData: Handling parameter: " + paramName);
		
		// first lets make sure there's a valid mapping, which means that the given parameter can be set
		// by at least one of the given site data types
		boolean mapping = false;
		for (SiteDataValue<?> data : datas) {
			if (DATA_TYPE_PARAM_NAME_MAP.isValidMapping(data, paramName)) {
				mapping = true;
				break;
			}
		}
		
		if (mapping) {
			// VS 30/Wills Site Class
			if (paramName.equals(AttenuationRelationship.VS30_NAME)) {
				return setVS30Param(param, datas, debug);
			} else if (paramName.equals(AttenuationRelationship.VS30_TYPE_NAME)) {
				return setVS30FlagParam(param, datas, debug);
			} else if (paramName.equals(AS_1997_AttenRel.SITE_TYPE_NAME)) {
				return setAS_SiteType(param, datas, debug);
			} else if (paramName.equals(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME)) {
				return setSCEMY_SiteType(param, datas, debug);
			} else if (paramName.equals(Campbell_1997_AttenRel.BASIN_DEPTH_NAME)) {
				return setCampbellBasinDepth(param, datas, debug);
			} else if (paramName.equals(Campbell_1997_AttenRel.SITE_TYPE_NAME)) {
				return setCampbellSiteType(param, datas, debug);
			} else if (paramName.equals(CB_2003_AttenRel.SITE_TYPE_NAME)) {
				return setCB03SiteType(param, datas, debug);
			} else if (paramName.equals(ShakeMap_2003_AttenRel.WILLS_SITE_NAME)) {
				return setWillsSiteTypeName(param, datas, debug);
			} else if (paramName.equals(CS_2005_AttenRel.SOFT_SOIL_NAME)){
				return setCS05SoftSoil(param, datas, debug);
			}
			
			// BASIN Depth
			if (paramName.equals(AttenuationRelationship.DEPTH_2pt5_NAME)) {
				return setDepthTo2p5Param(param, datas, debug);
			} else if (paramName.equals(AttenuationRelationship.DEPTH_1pt0_NAME)) {
				return setDepthTo1p0Param(param, datas, debug);
			} else if (paramName.equals(Field_2000_AttenRel.BASIN_DEPTH_NAME)){
				return setDepthTo2p5Param(param, datas, debug);
			}
		} else {
			if (debug) {
				String typeStr = "";
				for (SiteDataValue<?> data : datas) {
					if (typeStr.length() > 0)
						typeStr += ", ";
					typeStr += data.getType();
				}
				System.out.println("setSiteParamsForData: No mapping exists for type(s): " + typeStr);
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Convenience method to set all site params in the given attenuation relationship instance from a single
	 * site data value. Returns true if at least one parameter was set.
	 * 
	 * @param imr
	 * @param datas
	 * @return true if at least one parameter was set.
	 */
	public boolean setAllSiteParams(AttenuationRelationshipAPI imr, SiteDataValue<?> data) {
		return setAllSiteParams(imr, data, D);
	}
	
	/**
	 * Convenience method to set all site params in the given attenuation relationship instance from a single
	 * site data value. Returns true if at least one parameter was set.
	 * 
	 * @param imr
	 * @param datas
	 * @param debug
	 * @return true if at least one parameter was set.
	 */
	public boolean setAllSiteParams(AttenuationRelationshipAPI imr, SiteDataValue<?> data, boolean debug) {
		Collection<SiteDataValue<?>> datas = new ArrayList<SiteDataValue<?>>();
		datas.add(data);
		return setAllSiteParams(imr, datas, debug);
	}
	
	/**
	 * Convenience method to set all site params in the given attenuation relationship instance from the given
	 * set of data. Returns true if at least one parameter was set.
	 * 
	 * @param imr
	 * @param datas
	 * @return true if at least one parameter was set.
	 */
	public boolean setAllSiteParams(AttenuationRelationshipAPI imr, Collection<SiteDataValue<?>> datas) {
		return setAllSiteParams(imr, datas, D);
	}
	
	/**
	 * Convenience method to set all site params in the given attenuation relationship instance from the given
	 * set of data. Returns true if at least one parameter was set.
	 * 
	 * @param imr
	 * @param datas
	 * @param debug
	 * @return true if at least one parameter was set.
	 */
	public boolean setAllSiteParams(AttenuationRelationshipAPI imr, Collection<SiteDataValue<?>> datas, boolean debug) {
		boolean setSomething = false;
		
		ListIterator<ParameterAPI> it = imr.getSiteParamsIterator();
		
		while (it.hasNext()) {
			ParameterAPI param = it.next();
			if (this.setParameterValue(param, datas, debug))
				setSomething = true;
		}
		
		return setSomething;
	}
	
	/**
	 * Returns the first data value of a given type
	 * 
	 * @param datas
	 * @param type
	 * @return
	 */
	private SiteDataValue<?> getDataForType(Collection<SiteDataValue<?>> datas, String type) {
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(type)) {
				return data;
			}
		}
		return null;
	}
	
	/**
	 * Checks to see if the specified Vs30 value is greater than 0, not null, and not NaN.
	 * 
	 * @param vsValue
	 * @return
	 */
	private boolean isVS30ValueValid(Double vsValue) {
		return vsValue != null && !vsValue.isNaN() && vsValue > 0;
	}
	
	/**
	 * Set the Vs30 param for the given set to site data. If a Vs30 value is available in the data,
	 * and is highest priority,that is used. Otherwise if a Wills Site Classification is available,
	 * it is translated into Vs30 and used.
	 * 
	 * See <code>getVS30FromWillsClass</code> for Wills translation values.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setVS30Param(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		Double vsValue = null;
		
		// iterate over the data finding the first one you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				// this is just a plain VS 30 value
				vsValue = (Double)data.getValue();
				if (debug) System.out.println("setSiteParamsForData: Got VS: " + vsValue);
				if (isVS30ValueValid(vsValue)) {
					if (debug) System.out.println("setSiteParamsForData: +++ Set VS30 param: " + vsValue);
					param.setValue(vsValue);
					return true;
				}
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				// this is a Wills Site Class that needs to be translated
				vsValue = getVS30FromWillsClass((String)data.getValue());
				if (debug) System.out.println("setSiteParamsForData: Got translated VS: " + vsValue
						+ " from " + data.getValue());
				if (isVS30ValueValid(vsValue)) {
					if (debug) System.out.println("setSiteParamsForData: +++ Set VS30 param: " + vsValue);
					param.setValue(vsValue);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Sets the Vs30 flag parameter. It figures out what was used to set the Vs30 parameter, and then
	 * uses the measured/inferred flag from that data source.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setVS30FlagParam(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// Follow the same methodology as that used to set the Vs30 param so we make sure we get the flag
		// from the vs data source that was actually used
		
		// iterate over the data finding the first one you can use. once you find a valid Vs30 value
		// or wills class, use the flag from the data to set the parameter
		for (SiteDataValue<?> data : datas) {
			Double vsValue = null;
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				// this is just a plain VS 30 value
				vsValue = (Double)data.getValue();
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				// this is a Wills Site Class that needs to be translated
				vsValue = getVS30FromWillsClass((String)data.getValue());
			}
			if (isVS30ValueValid(vsValue)) {
				if (data.getFlag().equals(SiteDataAPI.TYPE_FLAG_MEASURED)) {
					if (debug) System.out.println("setSiteParamsForData: +++ Setting VS measured");
					param.setValue(AttenuationRelationship.VS30_TYPE_MEASURED); // set it to measured
					return true;
				} else {
					if (debug) System.out.println("setSiteParamsForData: +++ Setting VS inferred");
					param.setValue(AttenuationRelationship.VS30_TYPE_INFERRED); // set it to inferred
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Sets the Depth to Vs = 2.5 KM/sec param if appropriate data is available.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setDepthTo2p5Param(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// this will get the first (highest priority) data that works
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_DEPTH_TO_2_5)) {
				Double val = (Double)data.getValue();
				if (Double.isNaN(val)) {
					continue;
				}
				if (debug) System.out.println("setSiteParamsForData: +++ Setting dep 2.5: " + val);
				if (param instanceof WarningDoubleParameter)
					((WarningDoubleParameter)param).setValueIgnoreWarning(val);
				else
					param.setValue(val);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the Depth to Vs = 1.0 KM/sec param if appropriate data is available.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setDepthTo1p0Param(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// this will get the first (highest priority) data that works
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_DEPTH_TO_1_0)) {
				Double val = (Double)data.getValue();
				if (Double.isNaN(val)) {
					continue;
				} else {
					val = val * 1000d;
				}
				if (debug) System.out.println("setSiteParamsForData: +++ Setting dep 1.0: " + val);
				param.setValue(val);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the AS Site Type param as follows:
	 * 
	 * If using a Wills Site Classification:
	 * <UL>
	 * <LI> NA 						if E
	 * <LI> Deep-Soil				if DE, D, or CD
	 * <LI> Rock/Shallow-Soil		if C, BC, or B
	 * </UL>
	 * 
	 * Else if using a Vs30 value:
	 * <UL>
	 * <LI> NA 						if NaN
	 * <LI> Deep-Soil				if (Vs30 <= 400 AND no depth data) OR (Vs30 <= 400 AND Depth to 2.5 KM/sec > 100 m)
	 * <LI> Rock/Shallow-Soil		if Vs30 > 400 OR Depth to 2.5 KM/sec <= 100 m
	 * </UL>
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setAS_SiteType(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// iterate over the data finding the first one (highest priority) you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				Double vsVal = (Double)data.getValue();
				if (!isVS30ValueValid(vsVal))
					continue;
				// if we also have depth to 2.5...then use that to confirm soft soil
				SiteDataValue<?> dep2p5Data = getDataForType(datas, SiteDataAPI.TYPE_DEPTH_TO_2_5);
				Double dep = null;
				if (dep2p5Data != null)
					dep = (Double)dep2p5Data.getValue();
				// we want to set it to soil if vs <= 400 and:
				//		* we don't have depth to 2.5 data (or the data we have is null/NaN)
				//		* we have depth to 2.5 data, the depth is > 100 meters (0.1 KM)
				if (vsVal <= 400 && (dep == null || (!Double.isNaN(dep) && dep > 0.1))) {
					param.setValue(AS_1997_AttenRel.SITE_TYPE_SOIL);
				} else {
					param.setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
				}
				return true;
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				String wc = (String)data.getValue();
				if (wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
					param.setValue(AS_1997_AttenRel.SITE_TYPE_SOIL);
					return true;
				}
				else if (wc.equals(WILLS_C) || wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
					param.setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Sets the SCEMY Site Type param as follows:
	 * 
	 * If using a Wills Site Classification:
	 * <UL>
	 * <LI> NA 						if E
	 * <LI> Deep-Soil				if DE, D, or CD
	 * <LI> Rock/Shallow-Soil		if C, BC, or B
	 * </UL>
	 * 
	 * Else if using a Vs30 value:
	 * <UL>
	 * <LI> NA 					if NaN
	 * <LI> Deep-Soil			if (Vs30 <= 400 AND no depth data) OR (Vs30 <= 400 AND Depth to 2.5 KM/sec > 100 m)
	 * <LI> Rock				if Vs30 > 400 OR Depth to 2.5 KM/sec <= 100 m
	 * </UL>
	 *  
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setSCEMY_SiteType(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// iterate over the data finding the first one (highest priority) you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				Double vsVal = (Double)data.getValue();
				if (!isVS30ValueValid(vsVal))
					continue;
				// if we also have depth to 2.5...then use that to confirm soft soil
				SiteDataValue<?> dep2p5Data = getDataForType(datas, SiteDataAPI.TYPE_DEPTH_TO_2_5);
				Double dep = null;
				if (dep2p5Data != null)
					dep = (Double)dep2p5Data.getValue();
				// we want to set it to soil if vs <= 400 and:
				//		* we don't have depth to 2.5 data (or the data we have is null/NaN)
				//		* we have depth to 2.5 data, the depth is > 100 meters (0.1 KM)
				if (vsVal <= 400 && (dep == null || (!Double.isNaN(dep) && dep > 0.1))) {
					param.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_SOIL);
				} else {
					param.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);
				}
				return true;
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				String wc = (String)data.getValue();
				if (wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
					param.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_SOIL);
					return true;
				}
				else if (wc.equals(WILLS_C) || wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
					param.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * If using a Wills class is available, sets Campbell-Basin-Depth as follows:
	 * 
	 * <UL>
	 * <LI> Campbell-Basin-Depth = NaN      if E
	 * <LI> Campbell-Basin-Depth = 0.0      if B or BC
	 * <LI> Campbell-Basin-Depth = 1.0      if C
	 * <LI> Campbell-Basin-Depth = 5.0      if CD, D, or DE
	 * </UL>
	 * 
	 * Otherwise, if using Depth to Vs=2.5 KM/sec, use that value as an approximate
	 * (even though Campbell-Basin-Depth is for Depth to Vs=3.0 KM/sec)
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setCampbellBasinDepth(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// iterate over the data finding the first one (highest priority) you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				String wc = (String)data.getValue();
				if (wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
					param.setValue(new Double(5.0));
					return true;
				}
				else if (wc.equals(WILLS_C)) {
					param.setValue(new Double(1.0));
					return true;
				}
				else if (wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
					param.setValue(new Double(0.0));
					return true;
				}
			} else if (data.getType().equals(SiteDataAPI.TYPE_DEPTH_TO_2_5)) {
				Double depth = (Double)data.getValue();
				param.setValue(depth);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Campbell_1997_AttenRel.SITE_TYPE_NAME (Campbell (1997))<p>
	 * 
	 * If using a Wills class, set it as follows:
	 * 
	 * <UL>
	 * <LI> NA 					if E
	 * <LI> Firm-Soil			if DE, D, or CD
	 * <LI> Soft-Rock			if C
	 * <LI> Hard-Rock			if BC or B
	 * </UL>
	 * 
	 * Otherwise if using a Vs30 value:
	 * 
	 * <UL>
	 * <LI> NA 					if Vs30 <= 180
	 * <LI> Firm-Soil			if 180 > Vs30 <= 400
	 * <LI> Soft-Rock			if 400 > Vs30 <= 500
	 * <LI> Hard-Rock			if 500 > Vs30
	 * </UL>
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setCampbellSiteType(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// iterate over the data finding the first one (highest priority) you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				Double vsVal = (Double)data.getValue();
				if (!isVS30ValueValid(vsVal))
					continue;
				if(vsVal>180 && vsVal<=400) {
					param.setValue(Campbell_1997_AttenRel.SITE_TYPE_FIRM_SOIL);
					return true;
				} else if(vsVal>400 && vsVal<=500) {
					param.setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
					return true;
				} else if(vsVal>500) {
					param.setValue(Campbell_1997_AttenRel.SITE_TYPE_HARD_ROCK);
					return true;
				}
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				String wc = (String)data.getValue();
				if (wc.equals(WILLS_DE) || wc.equals(WILLS_D) || wc.equals(WILLS_CD)) {
					param.setValue(Campbell_1997_AttenRel.SITE_TYPE_FIRM_SOIL);
					return true;
				}
				else if (wc.equals(WILLS_C)) {
					param.setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
					return true;
				}
				else if (wc.equals(WILLS_BC) || wc.equals(WILLS_B)) {
					param.setValue(Campbell_1997_AttenRel.SITE_TYPE_HARD_ROCK);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * Set Campbell & Bozorgnia 2003 Site Type parameter.
	 * 
	 * If using a Wills class, set it as follows:
	 * 
	 * <UL>
	 * <LI> NA 						if E
	 * <LI> Firm-Soil				if DE, or D
	 * <LI> Very-Firm-Soil			if CD
	 * <LI> BC-Boundary				if BC
	 * <LI> Soft-Rock				if C
	 * <LI> Hard-Rock				if B
	 * </UL>
	 * 
	 * Otherwise if using a Vs30 value:
	 * 
	 * <UL>
	 * <LI> NA 						if Vs30 <= 180
	 * <LI> Firm-Soil				if 180 > Vs30 <= 300
	 * <LI> Very-Firm-Soil			if 300 > Vs30 <= 400
	 * <LI> Soft-Rock				if 400 > Vs30 <= 500
	 * <LI> Hard-Rock				if 500 > Vs30
	 * </UL>
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setCB03SiteType(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// iterate over the data finding the first one (highest priority) you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				Double vsVal = (Double)data.getValue();
				if(vsVal>180 && vsVal<=300) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_SOIL);
					return true;
				} else if(vsVal>300 && vsVal<=400) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_VERY_FIRM_SOIL);
					return true;
				} else if(vsVal >400 && vsVal <=500) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_SOFT_ROCK);
					return true;
				} else if(vsVal >500) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_ROCK);
					return true;
				}
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				String wc = (String)data.getValue();
				if (wc.equals(WILLS_DE) || wc.equals(WILLS_D)) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_SOIL);
					return true;
				}
				else if (wc.equals(WILLS_CD)) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_VERY_FIRM_SOIL);
					return true;
				}
				else if (wc.equals(WILLS_C)) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_SOFT_ROCK);
					return true;
				}
				else if (wc.equals(WILLS_BC)) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_NEHRP_BC);
					return true;
				}
				else if (wc.equals(WILLS_B)) {
					param.setValue(CB_2003_AttenRel.SITE_TYPE_FIRM_ROCK);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Set Wills Site Type Name parameter (e.g. ShakeMap 2003)
	 * 
	 * If we have a wills value, set the parameter.
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setWillsSiteTypeName(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// if we have a wills class, use that
		SiteDataValue<?> willsData = getDataForType(datas, SiteDataAPI.TYPE_WILLS_CLASS);
		if (willsData != null) {
			String wc = (String)willsData.getValue();
			if (param.isAllowed(wc)) {
				param.setValue(wc);
				return true;
			}
		}
		
		// TODO: figure out what to do with a VS 30 value here
		return false;
	}
	
	/**
	 * 
	 * Set the CS 2005 Soft Soil parameter
	 * 
	 * If a Wills Site Class is available:
	 * 
	 * <UL>
	 * <LI> True 				if E
	 * <LI> False				otherwise
	 * </UL>
	 * 
	 * Otherwise if we have a Vs30 value:
	 * 
	 * <UL>
	 * <LI> True 				if Vs30 < 180
	 * <LI> False				if 180 >= Vs30
	 * </UL>
	 * 
	 * @param param
	 * @param datas
	 * @param debug
	 * @return
	 */
	public boolean setCS05SoftSoil(ParameterAPI param, Collection<SiteDataValue<?>> datas, boolean debug) {
		// iterate over the data finding the first one (highest priority) you can use
		for (SiteDataValue<?> data : datas) {
			if (data.getType().equals(SiteDataAPI.TYPE_VS30)) {
				Double vsVal = (Double)data.getValue();
				if (vsVal < 180) {
					param.setValue(vsVal);
					return true;
				}
			} else if (data.getType().equals(SiteDataAPI.TYPE_WILLS_CLASS)) {
				String wc = (String)data.getValue();
				if (wc.equals(WILLS_E))
					param.setValue(new Boolean(true));
				else
					param.setValue(new Boolean(false));
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @param parameter: the parameter object to be set
	 * @param willsClass - a String with one of the folowing ("E", "DE", "D", "CD", "C", "BC", or "B")
	 * @param basinDepth - Depth (in meters) to where Vs = 2.5-km/sec
	 *
	 * @returns a boolean to tell if setting the value was successful (if false
	 * it means the parameter value was not changed).  A basinDepth value of NaN is allowed
	 * (it will not cause the returned value to be false).
	 * 
	 * ***NOTE: THIS NEEDS TO FIXED TO HANDLE THE SOFT SOIL CASE FOR CHOI AND STEWART MODEL 
	 */
	@Deprecated
	public boolean setParameterValue(ParameterAPI param, String willsClass,
			double basinDepth) {
		
		SiteDataValue<?> willsData = null;
		
		// shorten name for convenience
		String wc = willsClass;
		
		if (wills_vs30_map.keySet().contains(wc)) {
			// it's a wills class
			willsData = new SiteDataValue<String>(SiteDataAPI.TYPE_WILLS_CLASS, SiteDataAPI.TYPE_FLAG_MEASURED, wc);
		} else {
			// lets see if it's a Vs30 value
			try {
				double vs = Double.parseDouble(wc);
				willsData = new SiteDataValue<Double>(SiteDataAPI.TYPE_VS30,
						SiteDataAPI.TYPE_FLAG_MEASURED, vs);
			} catch (NumberFormatException e) {
				// it's not
			}
		}
		
		SiteDataValue<Double> basinData = null;
		// it's in meters here so we have to convert to KM
		basinData = new SiteDataValue<Double>(SiteDataAPI.TYPE_DEPTH_TO_2_5,
					SiteDataAPI.TYPE_FLAG_MEASURED, basinDepth / 1000d);
		boolean setWills = this.setParameterValue(param, willsData, D);
		boolean setBasin = this.setParameterValue(param, basinData, D);
		
		return setWills || setBasin;
//		if (willsData != null) {
//			// AS_1997_AttenRel.SITE_TYPE_NAME
//			// (e.g., used by Abrahamson & Silva (1997) & Abrahamson (2000))
//			if (param.getName().equals(AS_1997_AttenRel.SITE_TYPE_NAME)) {
//				return setAS_SiteType(param, willsData, D);
//			}
//
//			// SCEMY_1997_AttenRel.SITE_TYPE_NAME
//			else if (param.getName().equals(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME)) {
//				return setSCEMY_SiteType(param, willsData, D);
//			}
//			
//			// AttenuationRelationship.VS30_NAME
//			// (e.g., used by BJF-1997 and Field-2000) site type
//			else if (param.getName().equals(AttenuationRelationship.VS30_NAME)) {
//				return setVS30Param(param, willsData, D);
//			}
//
//			// Campbell_1997_AttenRel.BASIN_DEPTH_NAME
//			// (these are as Ken Campbell requested)
//			else if (param.getName().equals(Campbell_1997_AttenRel.BASIN_DEPTH_NAME)) {
//				return setCampbellBasinDepth(param, willsData, D);
//			}
//
//			// Campbell_1997_AttenRel.SITE_TYPE_NAME
//			else if (param.getName().equals(Campbell_1997_AttenRel.SITE_TYPE_NAME)) {
//				return setCampbellSiteType(param, willsData, D);
//			}
//
//			// CB_2003_AttenRel.SITE_TYPE_NAME
//			else if (param.getName().equals(CB_2003_AttenRel.SITE_TYPE_NAME)) {
//				return setCB03SiteType(param, willsData, D);
//			}
//			
//			// ShakeMap_2003_AttenRel.WILLS_SITE_NAME
//			else if (param.getName().equals(ShakeMap_2003_AttenRel.WILLS_SITE_NAME)) {
//				return setWillsSiteTypeName(param, willsData, D);
//			}
//			//CS_2005.SOFT_SOIL_CASE
//			else if(param.getName().equals(CS_2005_AttenRel.SOFT_SOIL_NAME)){
//				return setCS05SoftSoil(param, willsData, D);
//			}
//			else if(param.getName().equals(AttenuationRelationship.VS_FLAG_NAME)) {
//				return setVS30FlagParam(param, willsData, D);
//			}
//		}
//		
//		if (basinData != null) {
//			// Field_2000_AttenRel.BASIN_DEPTH_NAME
//			if (param.getName().equals(Field_2000_AttenRel.BASIN_DEPTH_NAME)) {
//				// set basin depth in kms
//				return setDepthTo2p5Param(param, basinData, D);
//			}
//			// Depth 2.5 km/sec Parameter
//			else if (param.getName().equals(AttenuationRelationship.DEPTH_2pt5_NAME)) {
//				return setDepthTo2p5Param(param, basinData, D);
//			}
//			// AS_2008
//			else if(param.getName().equals(AttenuationRelationship.DEPTH_1pt0_NAME))
//				return false;
//		}
//
//		throw new RuntimeException(C + " does not support the site type: " +
//					param.getName());
	}



	/**
	 * This will test the translation from all wills categories for the parameter given
	 * @param param
	 */
	public void test(ParameterAPI param) {
		System.out.println(param.getName() + "  Parameter (basin depth = NaN):");
		if (setParameterValue(param, WILLS_B, Double.NaN)) {
			System.out.println("\t" + WILLS_B + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_B + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_BC, Double.NaN)) {
			System.out.println("\t" + WILLS_BC + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_BC + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_C, Double.NaN)) {
			System.out.println("\t" + WILLS_C + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_C + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_CD, Double.NaN)) {
			System.out.println("\t" + WILLS_CD + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_CD + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_D, Double.NaN)) {
			System.out.println("\t" + WILLS_D + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_D + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_DE, Double.NaN)) {
			System.out.println("\t" + WILLS_DE + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_DE + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_E, Double.NaN)) {
			System.out.println("\t" + WILLS_E + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_E + " --> " + "*** can't set ***");
		}

		System.out.println(param.getName() + "  Parameter (basin depth = 1.0):");
		if (setParameterValue(param, WILLS_B, 1.0)) {
			System.out.println("\t" + WILLS_B + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_B + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_BC, 1.0)) {
			System.out.println("\t" + WILLS_BC + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_BC + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_C, 1.0)) {
			System.out.println("\t" + WILLS_C + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_C + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_CD, 1.0)) {
			System.out.println("\t" + WILLS_CD + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_CD + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_D, 1.0)) {
			System.out.println("\t" + WILLS_D + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_D + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_DE, 1.0)) {
			System.out.println("\t" + WILLS_DE + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_DE + " --> " + "*** can't set ***");
		}
		if (setParameterValue(param, WILLS_E, 1.0)) {
			System.out.println("\t" + WILLS_E + " --> " + param.getValue());
		}
		else {
			System.out.println("\t" + WILLS_E + " --> " + "*** can't set ***");
		}
	}

	/**
	 * This main method tests the translation of all currently implemented attenuation
	 * relationship site-dependent parameters.
	 * @param args
	 */
	public static void main(String args[]) {
		SiteTranslator siteTrans = new SiteTranslator();

		AttenuationRelationship ar;
		ar = new AS_1997_AttenRel(null);
		siteTrans.test(ar.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME));

		ar = new SadighEtAl_1997_AttenRel(null);
		siteTrans.test(ar.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME));

		ar = new BJF_1997_AttenRel(null);
		siteTrans.test(ar.getParameter(AttenuationRelationship.VS30_NAME));

		ar = new Campbell_1997_AttenRel(null);
		siteTrans.test(ar.getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME));
		siteTrans.test(ar.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME));

		ar = new Field_2000_AttenRel(null);
		siteTrans.test(ar.getParameter(AttenuationRelationship.VS30_NAME));
		siteTrans.test(ar.getParameter(Field_2000_AttenRel.BASIN_DEPTH_NAME));

		ar = new Abrahamson_2000_AttenRel(null);
		siteTrans.test(ar.getParameter(Abrahamson_2000_AttenRel.SITE_TYPE_NAME));

		ar = new CB_2003_AttenRel(null);
		siteTrans.test(ar.getParameter(CB_2003_AttenRel.SITE_TYPE_NAME));

		ar = new ShakeMap_2003_AttenRel(null);
		siteTrans.test(ar.getParameter(ShakeMap_2003_AttenRel.WILLS_SITE_NAME));

		ar = new USGS_Combined_2004_AttenRel(null);
		siteTrans.test(ar.getParameter(AttenuationRelationship.VS30_NAME));

		//  ar = new SEA_1999_AttenRel(null);
		//  siteTrans.test(ar.getParameter(SEA_1999_AttenRel.SITE_TYPE_NAME));


	}

}
