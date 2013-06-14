package scratch.kevin.cybershake;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.CVMHBasinDepth;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.util.SiteTranslator;

import com.google.common.collect.Lists;

public class AR_RupSACalc {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String shortName = "DBCN";
		ScalarIMR imr = AttenRelRef.CB_2008.instance(null);
		boolean cvmh = true;
		int sourceID = 4;
		int rupID = 7;
		double period = 10d;
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		SiteInfo2DB site2db = new SiteInfo2DB(db);
		
		CybershakeSite csSite = site2db.getSiteFromDB(shortName);
		System.out.println("Site: "+csSite);
		imr.setParamDefaults();
		imr.setIntensityMeasure(SA_Param.NAME);
		SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), period);
		Location loc = csSite.createLocation();
		Site site = new Site(loc);
		site.addParameterList(imr.getSiteParams());
		
		ArrayList<SiteData<?>> providers = Lists.newArrayList();
		providers.add(new WillsMap2006());
		if (cvmh) {
			providers.add(new CVMHBasinDepth(SiteData.TYPE_DEPTH_TO_1_0));
			providers.add(new CVMHBasinDepth(SiteData.TYPE_DEPTH_TO_2_5));
		} else {
			providers.add(new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_1_0));
			providers.add(new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_2_5));
		}
		OrderedSiteDataProviderList provs = new OrderedSiteDataProviderList(providers);
		SiteTranslator trans = new SiteTranslator();
		// set site params
		trans.setAllSiteParams(imr, provs.getAllAvailableData(loc));
		for (Parameter<?> param : imr.getSiteParams())
			System.out.println("Site Param: "+param.getName()+" = "+param.getValue());
		
		AbstractERF erf = MeanUCERF2_ToDB.createUCERF2ERF();
		EqkRupture rup = erf.getRupture(sourceID, rupID);
		
		imr.setSite(site);
		imr.setEqkRupture(rup);
		
		double logMean = imr.getMean();
		double mean = Math.exp(logMean);
		
//		double logStdDev = imr.getStdDev();
//		double stdDev = Math.exp(logStdDev);
		double stdDev = imr.getStdDev();
		
		System.out.println(rup);
		System.out.println("Mean SA (g): "+mean);
		System.out.println("Std Dev: "+stdDev);
		db.destroy();
	}

}
