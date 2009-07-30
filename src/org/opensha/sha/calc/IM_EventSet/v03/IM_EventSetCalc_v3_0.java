package org.opensha.sha.calc.IM_EventSet.v03;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.PropagationEffect;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceJBParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;

public abstract class IM_EventSetCalc_v3_0 implements IM_EventSetCalc_v3_0_API {
	
	public static Logger logger = Logger.getLogger("IMv3Log");
	
	public static void initLogger(Level level) {
		Logger parent = logger;
		while (parent != null) {
			for (Handler handler : parent.getHandlers())
				handler.setLevel(level);
			parent.setLevel(level);
			parent = parent.getParent();
		}
		logger.setLevel(level);
	}
	
	public static final float MIN_SOURCE_DIST = 200;

	/**
	 * This should ONLY be accessed through the getter method as it may
	 * be uninitialized
	 */
	private ArrayList<Site> sites = null;

	private ArrayList<ArrayList<SiteDataValue<?>>> sitesData = null;

	public IM_EventSetCalc_v3_0() {

	}

	public final ArrayList<Site> getSites() {
		if (sites == null) {
			logger.log(Level.FINE, "Generating site list");
			sites = new ArrayList<Site>();
			for (int i=0; i<getNumSites(); i++) {
				Site site = new Site(getSiteLocation(i));
				sites.add(site);
			}
		}
		return sites;
	}
	
	public static ArrayList<ArrayList<SiteDataValue<?>>> getSitesData(IM_EventSetCalc_v3_0_API calc) {
		ArrayList<ArrayList<SiteDataValue<?>>> sitesData = new ArrayList<ArrayList<SiteDataValue<?>>>();
		ArrayList<Site> sites = calc.getSites();
		OrderedSiteDataProviderList providers = calc.getSiteDataProviders();
		for (int i=0; i<sites.size(); i++) {
			Site site = sites.get(i);
			ArrayList<SiteDataValue<?>> dataVals = calc.getUserSiteDataValues(i);
			if (dataVals == null) {
				dataVals = new ArrayList<SiteDataValue<?>>();
			}
			if (providers != null) {
				ArrayList<SiteDataValue<?>> provData = providers.getAllAvailableData(site.getLocation());
				if (provData != null)
					dataVals.addAll(provData);
			}
			sitesData.add(dataVals);
		}
		return sitesData;
	}

	public final ArrayList<ArrayList<SiteDataValue<?>>> getSitesData() {
		if (sitesData == null) {
			logger.log(Level.FINE, "Generating site data providers lists");
			sitesData = getSitesData(this);
		}

		return sitesData;
	}
	
}
