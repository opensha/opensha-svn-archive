package org.opensha.cybershake;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurve2DB;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.sha.calc.hazardMap.MakeXYZFromHazardMapDir;

public class HazardCurveFetcher {
	
	HazardCurve2DB curve2db;
	CybershakeSiteInfo2DB site2db;
	
	ArrayList<Integer> ids;
	ArrayList<CybershakeSite> sites;
	ArrayList<DiscretizedFuncAPI> funcs;
	
	ArrayList<CybershakeSite> allSites = null;
	
	public HazardCurveFetcher(DBAccess db, ArrayList<Integer> erfIDs, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		this.initDBConnections(db);
		System.out.println("rupV: " + rupVarScenarioID + " sgtV: " + sgtVarID);
		ids = curve2db.getAllHazardCurveIDs(erfIDs, rupVarScenarioID, sgtVarID, imTypeID);
		sites = new ArrayList<CybershakeSite>();
		funcs = new ArrayList<DiscretizedFuncAPI>();
		for (int id : ids) {
			sites.add(site2db.getSiteFromDB(curve2db.getSiteIDFromCurveID(id)));
			DiscretizedFuncAPI curve = curve2db.getHazardCurve(id);
			funcs.add(curve);
		}
	}
	
	private void initDBConnections(DBAccess db) {
		curve2db = new HazardCurve2DB(db);
		site2db = new CybershakeSiteInfo2DB(db);
	}
	
	public ArrayList<Double> getSiteValues(boolean isProbAt_IML, double val) {
		ArrayList<Double> vals = new ArrayList<Double>();
		for (DiscretizedFuncAPI func : funcs) {
			vals.add(MakeXYZFromHazardMapDir.getCurveVal(func, isProbAt_IML, val));
		}
		return vals;
	}

	public ArrayList<Integer> getCurveIDs() {
		return ids;
	}

	public ArrayList<CybershakeSite> getCurveSites() {
		return sites;
	}

	public ArrayList<DiscretizedFuncAPI> getFuncs() {
		return funcs;
	}
	
	public ArrayList<CybershakeSite> getAllSites() {
		if (allSites == null) {
			allSites = site2db.getAllSitesFromDB();
		}
		return allSites;
	}
	
	public void writeCurveToFile(DiscretizedFuncAPI curve, String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		
		for (int i = 0; i < curve.getNum(); ++i)
			fw.write(curve.getX(i) + " " + curve.getY(i) + "\n");
		
		fw.close();
	}
	
	public void saveAllCurvesToDir(String outDir) {
		File outDirFile = new File(outDir);
		
		if (!outDirFile.exists())
			outDirFile.mkdir();
		
		if (!outDir.endsWith(File.separator))
			outDir += File.separator;
		
		ArrayList<DiscretizedFuncAPI> curves = this.getFuncs();
		ArrayList<CybershakeSite> curveSites = this.getCurveSites();
		
		for (int i=0; i<curves.size(); i++) {
			DiscretizedFuncAPI curve = curves.get(i);
			CybershakeSite site = curveSites.get(i);
			
			String fileName = outDir + site.short_name + "_" + site.lat + "_" + site.lon + ".txt";
			
			System.out.println("Writing " + fileName);
			
			try {
				this.writeCurveToFile(curve, fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
		String outDir = "/home/kevin/CyberShake/curve_data";
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		ArrayList<Integer> erfIDs = new ArrayList<Integer>();
		erfIDs.add(34);
		erfIDs.add(35);
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, erfIDs, 3, 5, 21);
		
//		fetcher.saveAllCurvesToDir(outDir);
		
		ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
		for (CybershakeSite site : sites) {
			String str = site.lon + ", " + site.lat + ", " + site.short_name + ", ";
			if (site.type_id == CybershakeSite.TYPE_POI)
				str += "Point of Interest";
			else if (site.type_id == CybershakeSite.TYPE_BROADBAND_STATION)
				str += "Seismic Station";
			else if (site.type_id == CybershakeSite.TYPE_PRECARIOUS_ROCK)
				str += "Precarious Rock";
			else if (site.type_id == CybershakeSite.TYPE_TEST_SITE)
				continue;
			System.out.println(str);
		}
		
		System.exit(0);
	}
}
