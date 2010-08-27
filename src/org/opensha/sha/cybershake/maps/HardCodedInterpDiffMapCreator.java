package org.opensha.sha.cybershake.maps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.elements.TopographicSlopeFile;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.maps.InterpDiffMap.InterpDiffMapType;
import org.opensha.sha.cybershake.maps.servlet.CS_InterpDiffMapServletAccessor;

public class HardCodedInterpDiffMapCreator {
	
	private static ArbDiscretizedXYZ_DataSet getMainScatter(boolean isProbAt_IML, double val, int imTypeID) {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		ArrayList<Integer> erfIDs = new ArrayList<Integer>();
		erfIDs.add(34);
		erfIDs.add(35);
		int rupVarScenarioID = 3;
		int sgtVarID = 5;
		HazardCurveFetcher fetcher =
			new HazardCurveFetcher(db, erfIDs, rupVarScenarioID, sgtVarID, imTypeID);
		ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
		ArrayList<Double> vals = fetcher.getSiteValues(isProbAt_IML, val);
		
		ArbDiscretizedXYZ_DataSet scatterData = new ArbDiscretizedXYZ_DataSet();
		for (int i=0; i<sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			double siteVal = vals.get(i);
			scatterData.addValue(site.lat, site.lon, siteVal);
		}
		return scatterData;
	}
	
	private static ArbDiscretizedXYZ_DataSet getCusomScatter(String singleName, int imTypeID,
			boolean isProbAt_IML, double val, boolean mod) throws FileNotFoundException, IOException {
		if (imTypeID != 21)
			throw new IllegalArgumentException("IM type must be 21 for custom map");
		if (!isProbAt_IML)
			throw new IllegalArgumentException("isProbAt_IML must be true for custom map");
		String dir = "/home/kevin/CyberShake/interpDiffInputFiles/"+singleName+"/";
		String fname;
		if (mod)
			fname = "mod_";
		else
			fname = "orig_";
		fname += (float)val+"g_singleDay.txt";
		String fileName = dir + fname;
		System.out.println("Loading scatter from: " + fileName);
		return ArbDiscretizedXYZ_DataSet.loadXYZFile(fileName);
	}
	
	private static ArbDiscretizedXYZ_DataSet loadBaseMap(boolean singleDay, boolean isProbAt_IML,
			double val, int imTypeID, String name) throws FileNotFoundException, IOException {
		int period;
		if (imTypeID == 11)
			period = 5;
		else if (imTypeID == 21)
			period = 3;
		else if (imTypeID == 26)
			period = 2;
		else
			throw new IllegalArgumentException("Unknown IM type id: " + imTypeID);
		String dir = "/home/kevin/CyberShake/baseMaps/"+name+"/";
		String fname = name+"_base_map_"+period+"sec_";
		if (isProbAt_IML) {
			fname += (float)val+"g";
		} else {
			if (val == 0.0004)
				fname += "2percent";
			else if (val == 0.002)
				fname += "10precent";
			else
				throw new IllegalArgumentException("Unown probability val: " + val);
		}
		if (singleDay)
			fname += "_singleDay";
		fname += "_hiRes.txt";
		String fileName = dir + fname;
		System.out.println("Loading basemap from: " + fileName);
		return ArbDiscretizedXYZ_DataSet.loadXYZFile(fileName);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args){
		try {
//			boolean isProbAt_IML = true;
//			double val = 0.2;
//			String baseMapName = "cb2008";
//			String singleName = "parkfield";
//			boolean mod = true;
			
			boolean isProbAt_IML = false;
			double val = 0.0004;
			String baseMapName = "cb2008";
			String singleName = null;
			boolean mod = false;
			
			
			boolean logPlot = true;
			int imTypeID = 21;
			
			
			boolean singleDay = singleName != null;
			double baseMapRes = 0.005;
			System.out.println("Loading basemap...");
			ArbDiscretizedXYZ_DataSet baseMap = loadBaseMap(singleDay, isProbAt_IML, val, imTypeID, baseMapName);
			System.out.println("Basemap has " + baseMap.getX_DataSet().size() + " points");
			String customLabel = "3sec SA, 2% in 50 yrs";
			
			System.out.println("Fetching curves...");
			ArbDiscretizedXYZ_DataSet scatterData;
			if (singleDay)
				scatterData = getCusomScatter(singleName, imTypeID, isProbAt_IML, val, mod);
			else
				scatterData = getMainScatter(isProbAt_IML, val, imTypeID);
			
			System.out.println("Creating map instance...");
			GMT_InterpolationSettings interpSettings = GMT_InterpolationSettings.getDefaultSettings();
			Region region = new CaliforniaRegions.CYBERSHAKE_MAP_REGION();
			
			InterpDiffMapType[] mapTypes = null;
			
			CPT cpt = CPT.loadFromStream(HardCodedInterpDiffMapCreator.class.getResourceAsStream(
					"/resources/cpt/MaxSpectrum2.cpt"));
			
			InterpDiffMap map = new InterpDiffMap(region, baseMap, baseMapRes, cpt, scatterData, interpSettings, mapTypes);
			map.setCustomLabel(customLabel);
			map.setTopoResolution(TopographicSlopeFile.CA_THREE);
			map.setLogPlot(logPlot);
			map.setDpi(300);
			map.isCustomScale();
			
			String metadata = "isProbAt_IML: " + isProbAt_IML + "\n" +
							"val: " + val + "\n" +
							"singleDay: " + singleDay + "\n" +
							"singleDayName: " + singleName + "\n" +
							"imTypeID: " + imTypeID + "\n";
			
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
