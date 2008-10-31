package org.opensha.cybershake;

import java.util.ArrayList;

import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.CybershakeSiteInfo2DB;
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
	
	public HazardCurveFetcher(DBAccess db, int erfID, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		this.initDBConnections(db);
		ids = curve2db.getAllHazardCurveIDs(erfID, rupVarScenarioID, sgtVarID, imTypeID);
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

}
