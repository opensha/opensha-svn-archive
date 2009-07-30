package org.opensha.sha.calc.IM_EventSet.v03;

import java.util.ArrayList;

import org.dom4j.Element;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

public class IM_EventSetCalculation implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "IMEventSetCalculation";
	public static final String XML_ERFS_NAME = "ERFs";
	public static final String XML_IMRS_NAME = "IMRs";
	public static final String XML_IMTS_NAME = "IMTs";
	public static final String XML_IMT_String_NAME = "IMTString";
	public static final String XML_SITES_NAME = "Sites";
	public static final String XML_SITE_NAME = "Site";
	public static final String XML_SITE_DATA_VALS_NAME = "SiteDataValues";
	
	private ArrayList<Location> sites;
	private ArrayList<ArrayList<SiteDataValue<?>>> sitesData;
	private ArrayList<EqkRupForecastAPI> erfs;
	private ArrayList<ScalarIntensityMeasureRelationshipAPI> attenRels;
	private ArrayList<String> imts;
	
	public IM_EventSetCalculation(ArrayList<Location> sites, ArrayList<ArrayList<SiteDataValue<?>>> sitesData,
			ArrayList<EqkRupForecastAPI> erfs, ArrayList<ScalarIntensityMeasureRelationshipAPI> attenRels,
			ArrayList<String> imts) {
		this.sites = sites;
		this.sitesData = sitesData;
		this.erfs = erfs;
		this.attenRels = attenRels;
		this.imts = imts;
	}

	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		// ERFs
		Element erfsEL = el.addElement(XML_ERFS_NAME);
		for (EqkRupForecastAPI erf : erfs) {
			if (erf instanceof XMLSaveable) {
				XMLSaveable xmlERF = (XMLSaveable)erf;
				xmlERF.toXMLMetadata(erfsEL);
			} else {
				throw new RuntimeException("ERF cannot to be saved to XML!");
			}
		}
		
		// IMRs
		Element imrsEL = el.addElement(XML_IMRS_NAME);
		for (ScalarIntensityMeasureRelationshipAPI attenRel : attenRels) {
			attenRel.toXMLMetadata(imrsEL);
		}
		
		// IMTs
		Element imtsEL = el.addElement(XML_IMTS_NAME);
		for (String imt : imts) {
			Element imtEl = imtsEL.addElement(XML_IMT_String_NAME);
			imtEl.addAttribute("value", imt);
		}
		
		// Sites
		Element sitesEl = el.addElement(XML_SITES_NAME);
		for (int i=0; i<sites.size(); i++) {
			Element siteEl = el.addElement(XML_SITE_NAME);
			Location loc = sites.get(i);
			loc.toXMLMetadata(siteEl);
			ArrayList<SiteDataValue<?>> siteDatas = sitesData.get(i);
			if (siteDatas.size() > 0) {
				Element siteDatasEl = siteEl.addElement(XML_SITE_DATA_VALS_NAME);
				for (SiteDataValue<?> val : siteDatas) {
					val.toXMLMetadata(siteDatasEl);
				}
			}
		}
		
		return root;
	}

	public ArrayList<Location> getSites() {
		return sites;
	}

	public ArrayList<ArrayList<SiteDataValue<?>>> getSitesData() {
		return sitesData;
	}

	public ArrayList<EqkRupForecastAPI> getErfs() {
		return erfs;
	}

	public ArrayList<ScalarIntensityMeasureRelationshipAPI> getIMRs() {
		return attenRels;
	}

	public ArrayList<String> getIMTs() {
		return imts;
	}

}
