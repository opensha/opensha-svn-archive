package org.opensha.data.siteType.util;

import java.util.Collection;
import java.util.ListIterator;

import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.SiteDataValue;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.Campbell_1997_AttenRel;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.util.NtoNMap;

public class SiteDataTypeParameterNameMap extends NtoNMap<String, String> {
	
	public SiteDataTypeParameterNameMap() {
		super();
	}
	
	/**
	 * Add a mapping
	 * 
	 * @param type
	 * @param paramName
	 */
	public void addMapping(String type, String paramName) {
		this.put(type, paramName);
	}
	
	/**
	 * Returns a list of all site data types that can set this parameter
	 * 
	 * @param paramName
	 * @return
	 */
	public Collection<String> getTypesForParameterName(String paramName) {
		return this.getOnes(paramName);
	}
	
	/**
	 * Returns a list of all of the parameter names that can be set from this
	 * site data type
	 * 
	 * @param type
	 * @return
	 */
	public Collection<String> getParameterNamesForType(String type) {
		return this.getTwos(type);
	}
	
	/**
	 * Returns true if the specified mapping exists
	 * 
	 * @param type
	 * @param paramName
	 * @return
	 */
	public boolean isValidMapping(String type, String paramName) {
		return this.containsMapping(type, paramName);
	}
	
	/**
	 * Returns true if the specified mapping exists
	 * 
	 * @param type
	 * @param paramName
	 * @return
	 */
	public boolean isValidMapping(SiteDataValue<?> value, String paramName) {
		return this.containsMapping(value.getType(), paramName);
	}
	
	/**
	 * Returns true if the given attenuation relationship has a parameter that can be set by
	 * this type.
	 * 
	 * @param type
	 * @param attenRel
	 * @return
	 */
	public boolean isTypeApplicable(String type, AttenuationRelationshipAPI attenRel) {
		ListIterator<ParameterAPI> it = attenRel.getSiteParamsIterator();
		while (it.hasNext()) {
			ParameterAPI param = it.next();
			if (isValidMapping(type, param.getName()))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the given attenuation relationship has a parameter that can be set by
	 * this type.
	 * 
	 * @param type
	 * @param attenRel
	 * @return
	 */
	public boolean isTypeApplicable(SiteDataValue<?> value, AttenuationRelationshipAPI attenRel) {
		return isTypeApplicable(value.getType(), attenRel);
	}
	
	private void printParamsForType(String type) {
		System.out.println("***** Type: " + type);
		Collection<String> names = this.getParameterNamesForType(type);
		if (names == null) {
			System.out.println("- <NONE>");
		} else {
			for (String name : names) {
				System.out.println("- " + name);
			}
		}
	}
	
	private void printTypesForParams(String paramName) {
		System.out.println("***** Param Name: " + paramName);
		Collection<String> types = this.getTypesForParameterName(paramName);
		if (types == null) {
			System.out.println("- <NONE>");
		} else {
			for (String name : types) {
				System.out.println("- " + name);
			}
		}
	}
	
	private void printValidTest(String type, String paramName) {
		System.out.println(type + " : " + paramName + " ? " + this.isValidMapping(type, paramName));
	}
	
	public static void main(String args[]) {
		SiteDataTypeParameterNameMap map = SiteTranslator.DATA_TYPE_PARAM_NAME_MAP;
		
		map.printParamsForType(SiteDataAPI.TYPE_VS30);
		map.printParamsForType(SiteDataAPI.TYPE_WILLS_CLASS);
		map.printParamsForType(SiteDataAPI.TYPE_DEPTH_TO_2_5);
		map.printParamsForType(SiteDataAPI.TYPE_DEPTH_TO_1_0);
		
		map.printTypesForParams(AttenuationRelationship.VS30_NAME);
		
		map.printValidTest(SiteDataAPI.TYPE_VS30, AttenuationRelationship.VS30_NAME);
		map.printValidTest(SiteDataAPI.TYPE_WILLS_CLASS, Campbell_1997_AttenRel.SITE_TYPE_NAME);
		map.printValidTest(SiteDataAPI.TYPE_VS30, Campbell_1997_AttenRel.SITE_TYPE_NAME);
		map.printValidTest(SiteDataAPI.TYPE_DEPTH_TO_2_5, AttenuationRelationship.VS30_NAME);
		
		System.out.println("Size: " + map.size());
	}

}
