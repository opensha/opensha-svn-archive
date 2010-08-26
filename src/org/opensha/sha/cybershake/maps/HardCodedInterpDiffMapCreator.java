package org.opensha.sha.cybershake.maps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;

public class HardCodedInterpDiffMapCreator {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args){
		try {
			boolean isProbAt_IML = false;
			double val = 0.0004;
			String baseMapFile = "/home/kevin/CyberShake/baseMaps/cb2008/cb2008_base_map_2sec_2percent_hiRes.txt";
			double baseMapRes = 0.005;
			System.out.println("Loading basemap...");
			ArbDiscretizedXYZ_DataSet baseMap = ArbDiscretizedXYZ_DataSet.loadXYZFile(baseMapFile);
			String customLabel = "3sec SA, 2% in 50 yrs";
			
			System.out.println("Fetching curves...");
			DBAccess db = Cybershake_OpenSHA_DBApplication.db;
			ArrayList<Integer> erfIDs = new ArrayList<Integer>();
			erfIDs.add(34);
			erfIDs.add(35);
			int rupVarScenarioID = 3;
			int sgtVarID = 5;
			int imTypeID = 21;
			HazardCurveFetcher fetcher =
				new HazardCurveFetcher(db, erfIDs, rupVarScenarioID, sgtVarID, imTypeID);
			ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
			ArrayList<Double> vals = fetcher.getSiteValues(isProbAt_IML, val);
			
			System.out.println("Creating map instance...");
			
			ArbDiscretizedXYZ_DataSet scatterData = new ArbDiscretizedXYZ_DataSet();
			for (int i=0; i<sites.size(); i++) {
				CybershakeSite site = sites.get(i);
				double siteVal = vals.get(i);
				scatterData.addValue(site.lat, site.lon, siteVal);
			}
			
			GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
			Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
			
			InterpDiffMapType[] mapTypes = null;
			
			CPT cpt = CPT.loadFromStream(HardCodedInterpDiffMapCreator.class.getResourceAsStream(
					"/resources/cpt/MaxSpectrum2.cpt"));
			
			InterpDiffMap map = new InterpDiffMap(region, baseMap, baseMapRes, cpt, scatterData, interpSettings, mapTypes);
			map.setCustomLabel(customLabel);
			
			String metadata = "My map";
			
			System.out.println("Making map...");
			System.out.println("map address: " + CS_InterpDiffMapServletAccessor.makeMap(null, map, metadata));
			
			System.exit(0);
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
			System.exit(1);
		} 
	}

}
