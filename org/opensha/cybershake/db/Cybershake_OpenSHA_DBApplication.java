package org.opensha.cybershake.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.util.FileUtils;


/**
 * Main application to put the Cybershake locations and Earthquake Rupture Forecast information
 * in the database.
 * @author nitingupta
 *
 */
public class Cybershake_OpenSHA_DBApplication {

	
	private static String HOST_NAME = "intensity.usc.edu";
	private static String DATABASE_NAME = "CyberShake";
	private static final DBAccess db = new DBAccess(HOST_NAME,DATABASE_NAME);
	
	public static boolean timer = false;
	
	/**
	 * putting the Cybershake location information in the database
	 * @param forecast
	 * @param erfId
	 */
	private void putSiteInfoInDB(EqkRupForecastAPI forecast,int erfId){
		/**
	     * Site List for Cybershake
	     */
		CybershakeSiteInfo2DB sites = new CybershakeSiteInfo2DB(db);
		
		//USC
		System.out.println("Doing Site USC");
		double USC_LAT = 34.019200;
		double USC_LON = -118.28600;
		int siteId = sites.getCybershakeSiteId("USC");
		//siteId= sites.putCybershakeLocationInDB("USC", "USC", USC_LAT, USC_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, USC_LAT, USC_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, USC_LAT, USC_LON);
	    
	    //PAS
	    System.out.println("Doing Site PAS");
	    double PAS_LAT = 34.148427;
	    double PAS_LON = -118.17119;
	    siteId = sites.getCybershakeSiteId("PAS");
	    //siteId = sites.putCybershakeLocationInDB("PAS", "PAS",PAS_LAT ,PAS_LON );
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, PAS_LAT, PAS_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, PAS_LAT, PAS_LON);

	    //LADT
	    System.out.println("Doing Site LADT");
	    double LADT_LAT = 34.052041;
	    double LADT_LON = -118.25713;
	    siteId = sites.getCybershakeSiteId("LADT");
	    //siteId= sites.putCybershakeLocationInDB("LADT", "LADT", LADT_LAT, LADT_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, LADT_LAT, LADT_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, LADT_LAT, LADT_LON);

	    //LBP
	    System.out.println("Doing Site LBP");
	    double LBP_LAT = 33.754944;
	    double LBP_LON = -118.22300;
	    siteId = sites.getCybershakeSiteId("LBP");
	    //siteId = sites.putCybershakeLocationInDB("LBP", "LBP", LBP_LAT,LBP_LON );
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, LBP_LAT, LBP_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, LBP_LAT, LBP_LON);

	    
	    //WNGC
	    System.out.println("Doing Site WNGC");
	    double WNGC_LAT = 34.041823;
	    double WNGC_LON = -118.06530;
	    siteId = sites.getCybershakeSiteId("WNGC");
	    //siteId = sites.putCybershakeLocationInDB("WNGC", "WNGC", WNGC_LAT,WNGC_LON );
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, WNGC_LAT, WNGC_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, WNGC_LAT, WNGC_LON);

	    //SABD
	    System.out.println("Doing Site SABD");
	    double SABD_LAT = 33.754111;
	    double SABD_LON = -117.86778;
	    siteId = sites.getCybershakeSiteId("SABD");
	    //siteId = sites.putCybershakeLocationInDB("SABD", "SABD",SABD_LAT , SABD_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, SABD_LAT, SABD_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, SABD_LAT, SABD_LON);
	    
	    //SBSM
	    System.out.println("Doing Site SBSM");
	    double SBSM_LAT = 34.064986;
	    double SBSM_LON = -117.29201;
	    siteId = sites.getCybershakeSiteId("SBSM");
	    //siteId = sites.putCybershakeLocationInDB("SBSM", "SBSM", SBSM_LAT, SBSM_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, SBSM_LAT, SBSM_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, SBSM_LAT, SBSM_LON);

	    
	    //FFI
	    System.out.println("Doing Site FFI");
	    double FFI_LAT = 34.336030;
	    double FFI_LON = -118.50862;
	    siteId = sites.getCybershakeSiteId("FFI");
	    //siteId = sites.putCybershakeLocationInDB("FFI", "FFI", FFI_LAT, FFI_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, FFI_LAT, FFI_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, FFI_LAT, FFI_LON);

	    //CCP
	    System.out.println("Doing Site CCP");
	    double CCP_LAT = 34.054884;
	    double CCP_LON = -118.41302;
	    siteId = sites.getCybershakeSiteId("CCP");
	    //siteId = sites.putCybershakeLocationInDB("CCP", "CCP", CCP_LAT, CCP_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, CCP_LAT, CCP_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, CCP_LAT, CCP_LON);
	    
	    //SMCA
	    System.out.println("Doing Site SMCA");
	    double SMCA_LAT = 34.009092;
	    double SMCA_LON = -118.48939;
	    siteId = sites.getCybershakeSiteId("SMCA");
	    //siteId = sites.putCybershakeLocationInDB("SMCA", "SMCA", SMCA_LAT, SMCA_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, SMCA_LAT, SMCA_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, SMCA_LAT, SMCA_LON);
		
		//PTWN
	    System.out.println("Doing Site PTWN");
	    double PTWN_LAT = 34.14280;
	    double PTWN_LON = -116.49771;
	    siteId = sites.getCybershakeSiteId("PTWN");
	    //int siteId = sites.putCybershakeLocationInDB("Pioneer Town", "PTWN", PTWN_LAT, PTWN_LON);
	    sites.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, PTWN_LAT, PTWN_LON);
	    sites.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, PTWN_LAT, PTWN_LON);

	}
	
	/**
	 * puts a list of Cybershake location information into the database
	 * @param list of locations (SiteInsert's)
	 * @param forecast
	 * @param erfId
	 * @param siteDB object
	 */
	private void putSiteListInfoInDB(ArrayList<SiteInsert> sites, EqkRupForecastAPI forecast,int erfId, CybershakeSiteInfo2DB siteDB){
		ArrayList<int[]> newRups = new ArrayList<int[]>();
		int i=0;
		int numSites = sites.size();
		for (SiteInsert newsite : sites) {
			System.out.println("Doing Site " + newsite.name + " (" + newsite.short_name + "), " + ++i + " of " + numSites + " (" + getPercent(i, numSites) + ")");
			System.out.println("Putting location into DB");
			int siteId= siteDB.putCybershakeLocationInDB(newsite.name, newsite.short_name, newsite.lat, newsite.lon);
			siteId = siteDB.getCybershakeSiteId(newsite.short_name);
			System.out.println("Putting regional bounds into DB");
			siteDB.putCyberShakeLocationRegionalBounds(forecast, erfId, siteId, newsite.lat, newsite.lon);
			System.out.println("Putting Source Rupture info into DB");
			newRups.addAll(siteDB.putCyberShakeLocationSrcRupInfo(forecast, erfId, siteId, newsite.lat, newsite.lon, true, "newRupsForScott.txt"));
		}
		System.out.println("New ruptures...");
		for (int[] rup : newRups) {
			System.out.println(rup[0] + " " + rup[1]);
		}
	}
	
	private String getPercent(int small, int big) {
		double percent = (double)((int)(((double)small / (double)big * 10000) + 0.5)) / 100d;
		return Double.toString(percent);
	}
	
	/**
	 * Gets site info DB object
	 * @return CybershakeSiteInfo2DB
	 */
	private CybershakeSiteInfo2DB getSiteInfoObject() {
		return new CybershakeSiteInfo2DB(db);
	}
	
	public ArrayList<SiteInsert> getSiteListFromFile(String fileName) throws FileNotFoundException, IOException {
		ArrayList<SiteInsert> sites = new ArrayList<SiteInsert>();
		
		System.out.println("Loading sites from " + fileName);
		
		ArrayList<String> lines = FileUtils.loadFile(fileName);
		for (String line : lines) {
			StringTokenizer tok = new StringTokenizer(line);
			
			// get the Longitude from the file
			double lon = Double.parseDouble(tok.nextToken());
			// get the Latitude from the file
			double lat = Double.parseDouble(tok.nextToken());
			// short name
			String shortName = tok.nextToken().trim();
			// long name
			//String longName = tok.nextToken().trim();
			String longName = shortName;
			
			SiteInsert site = new SiteInsert(lat, lon, longName, shortName);
			System.out.println(site);
			sites.add(site);
		}
		
		System.out.println("Loaded " + sites.size() + " sites!");
		
		return sites;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean doIt = false;
		Cybershake_OpenSHA_DBApplication app = new Cybershake_OpenSHA_DBApplication();
		//NSHMP2002_ToDB erfDB = new NSHMP2002_ToDB(db);
		// String erfDescription = "NSHMP 2002 (Frankel02) Earthquake Rupture Forecast Model";
		System.out.println("Creating and Updating ERF...");
		MeanUCERF2_ToDB erfDB  = new MeanUCERF2_ToDB(db);
		String erfDescription = "Mean UCERF 2 - Single Branch Earthquake Rupture Forecast";
		// this puts the ERF into database, 
		//erfDB.insertForecaseInDB(erfDescription);
		EqkRupForecastAPI forecast = erfDB.getERF_Instance();
		System.out.println("ERF NAME: " + forecast.getName());
		int erfId = erfDB.getInserted_ERF_ID(forecast.getName());
		System.out.println("ERF ID: " + erfId);
		//make sites
		ArrayList<SiteInsert> site_list;
		try {
			site_list = app.getSiteListFromFile("/home/kevin/CyberShake/broadband_sites.txt");
			//app.putSiteInfoInDB(forecast,erfId);
			if (doIt) {
				System.out.println("Adding locations...");
				CybershakeSiteInfo2DB siteDB = app.getSiteInfoObject();
				app.putSiteListInfoInDB(site_list, forecast, erfId, siteDB);
				siteDB.closeWriter();
			}
			System.out.println("****DONE****");
			db.destroy();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//                            lat,        lon,       name,    shortname
//		site_list.add(new SiteInsert(33.79844, -117.39802, "Gavilan", "GAVI"));
//		site_list.add(new SiteInsert(33.96200, -117.37745, "Mockingbird", "MBRD"));
//		site_list.add(new SiteInsert(33.99014, -117.46292, "Pedley", "PEDL"));
//		site_list.add(new SiteInsert(34.59946, -117.83157, "Lovejoy Buttes", "LBUT"));
//		site_list.add(new SiteInsert(34.41973, -118.09137, "Aliso", "ALIS"));
//		site_list.add(new SiteInsert(34.34609, -117.97474, "Pacifico", "PACI"));
//		site_list.add(new SiteInsert(34.29296, -117.34775, "Silverwood Lake", "SLVW"));
		
		
	}
}
