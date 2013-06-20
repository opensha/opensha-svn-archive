/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.cybershake;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;

import com.google.common.collect.Lists;

public class HazardCurveFetcher {
	
	HazardCurve2DB curve2db;
	CybershakeSiteInfo2DB site2db;
	
	ArrayList<Integer> ids;
	ArrayList<CybershakeSite> sites;
	ArrayList<DiscretizedFunc> funcs;
	
	ArrayList<CybershakeSite> allSites = null;
	
	public HazardCurveFetcher(DBAccess db, int datasetID, int imTypeID) {
		this.initDBConnections(db);
		init(curve2db.getAllHazardCurveIDsForDataset(datasetID, imTypeID));
	}
	
	public HazardCurveFetcher(DBAccess db, int erfID, int rupVarScenarioID, int sgtVarID, int velModelID, int imTypeID) {
		this.initDBConnections(db);
		System.out.println("rupV: " + rupVarScenarioID + " sgtV: " + sgtVarID + " velID: " + velModelID);
		init(curve2db.getAllHazardCurveIDs(erfID, rupVarScenarioID, sgtVarID, velModelID, imTypeID));
	}
	
	private void init(ArrayList<Integer> ids) {
		this.ids = ids;
		sites = new ArrayList<CybershakeSite>();
		funcs = new ArrayList<DiscretizedFunc>();
		// keep track of duplicates - we want the most recent curve (which will be the first in the list
		// as the accessor sorts by curve date desc
		HashSet<Integer> siteIDs = new HashSet<Integer>();
		List<Integer> duplicateCurveIDs = Lists.newArrayList();
		System.out.println("Start loop...");
		for (int i=0; i<ids.size(); i++) {
			int id = ids.get(i);
			int siteID = curve2db.getSiteIDFromCurveID(id);
			if (siteIDs.contains(siteID)) {
//				System.out.println("Removing duplicate for site "+siteID+". Deleting curve ID "+id);
				duplicateCurveIDs.add(id);
				continue;
			} else {
				siteIDs.add(siteID);
			}
			sites.add(site2db.getSiteFromDB(siteID));
			DiscretizedFunc curve = curve2db.getHazardCurve(id);
			funcs.add(curve);
		}
		for (int id : duplicateCurveIDs)
			// use indexof because remove(int) will do index not object
			ids.remove(ids.indexOf(id));
	}
	
	private void initDBConnections(DBAccess db) {
		curve2db = new HazardCurve2DB(db);
		site2db = new CybershakeSiteInfo2DB(db);
	}
	
	public ArrayList<Double> getSiteValues(boolean isProbAt_IML, double val) {
		ArrayList<Double> vals = new ArrayList<Double>();
		for (DiscretizedFunc func : funcs) {
			vals.add(HazardDataSetLoader.getCurveVal(func, isProbAt_IML, val));
		}
		return vals;
	}

	public ArrayList<Integer> getCurveIDs() {
		return ids;
	}

	public ArrayList<CybershakeSite> getCurveSites() {
		return sites;
	}

	public ArrayList<DiscretizedFunc> getFuncs() {
		return funcs;
	}
	
	public ArrayList<CybershakeSite> getAllSites() {
		if (allSites == null) {
			allSites = site2db.getAllSitesFromDB();
		}
		return allSites;
	}
	
	public void writeCurveToFile(DiscretizedFunc curve, String fileName) throws IOException {
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
		
		ArrayList<DiscretizedFunc> curves = this.getFuncs();
		ArrayList<CybershakeSite> curveSites = this.getCurveSites();
		
		for (int i=0; i<curves.size(); i++) {
			DiscretizedFunc curve = curves.get(i);
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
	
	public static void main(String args[]) throws IOException {
		String outDir = "/home/kevin/CyberShake/curve_data";
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		System.out.println("1");
		
		int erfID = 35;
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, erfID, 3, 5, 1, 21);
		
		System.out.println("2");
		
//		fetcher.saveAllCurvesToDir(outDir);
		
		WillsMap2006 wills = new WillsMap2006();
		CVM4BasinDepth cvm = new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_2_5);
		
		ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
		String tot = "";
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
			else
				str += "Unknown";
			double vs30 = wills.getValue(site.createLocation());
			double basin = cvm.getValue(site.createLocation());
			str += ", " + vs30 + ", " + basin;
			System.out.println(str);
			tot += str + "\n";
		}
		System.out.print(tot);
		
		System.exit(0);
	}
}
