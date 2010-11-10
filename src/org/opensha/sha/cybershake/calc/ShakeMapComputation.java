package org.opensha.sha.cybershake.calc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.xyz.ArbDiscrGeographicDataSet;
import org.opensha.commons.data.xyz.GeographicDataSetAPI;
import org.opensha.commons.data.xyz.XYZ_DataSetAPI;
import org.opensha.commons.geo.Location;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.ERF2DB;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.Runs2DB;

public class ShakeMapComputation {
	
	private DBAccess db;
	private Runs2DB runs2db;
	private HazardCurve2DB hc2db;
	private PeakAmplitudesFromDB amps2db;
	private ERF2DB erf2db;
	
	public ShakeMapComputation(DBAccess db) {
		this.db = db;
		runs2db = new Runs2DB(db);
		hc2db = new HazardCurve2DB(db);
		amps2db = new PeakAmplitudesFromDB(db);
		erf2db = new ERF2DB(db);
	}
	
	public GeographicDataSetAPI getShakeMap(int datasetID, int erfID, int rupVarScenID, int imTypeID,
			int sourceID, int rupID, Location hypo) {
		CybershakeIM im = hc2db.getIMFromID(imTypeID);
		return getShakeMap(datasetID, erfID, rupVarScenID, im, sourceID, rupID, hypo);
	}
	
	public GeographicDataSetAPI getShakeMap(int datasetID, int erfID, int rupVarScenID, CybershakeIM im,
			int sourceID, int rupID, Location hypo) {
		ArrayList<Integer> rvsToInclude;
		if (hypo == null) {
			rvsToInclude = null;
		} else {
			HashMap<Integer, Location> rvHypos = erf2db.getHypocenters(erfID, sourceID, rupID, rupVarScenID);
			rvsToInclude = new ArrayList<Integer>();
			for (int rvID : rvHypos.keySet()) {
				Location rvHypo = rvHypos.get(rvID);
				if (hypo.equals(rvHypo))
					rvsToInclude.add(rvID);
			}
		}
		
		return getShakeMap(datasetID, erfID, rupVarScenID, im, sourceID, rupID, rvsToInclude);
	}
	
	public GeographicDataSetAPI getShakeMap(int datasetID, int erfID, int rupVarScenID, int imTypeID,
			int sourceID, int rupID, ArrayList<Integer> rvsToInclude) {
		CybershakeIM im = hc2db.getIMFromID(imTypeID);
		return getShakeMap(datasetID, erfID, rupVarScenID, im, sourceID, rupID, rvsToInclude);
	}
	
	public GeographicDataSetAPI getShakeMap(int datasetID, int erfID, int rupVarScenID, CybershakeIM im,
			int sourceID, int rupID, ArrayList<Integer> rvsToInclude) {
		
		GeographicDataSetAPI xyz = new ArbDiscrGeographicDataSet(true);
		
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, datasetID, im.getID());
		
		for (CybershakeSite site : fetcher.getCurveSites()) {
			ArrayList<CybershakeRun> runs = runs2db.getRuns(site.id);
			if (runs.size() > 0) {
				CybershakeRun run = runs.get(0);
				if (run.getERFID() != erfID || run.getRupVarScenID() != rupVarScenID)
					continue;
				ArrayList<Double> imVals;
				try {
					imVals = amps2db.getIM_Values(run.getRunID(), sourceID, rupID, im);
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
				
				System.out.println("loaded " + imVals.size() + " amps for site: " + site);
				
				if (rvsToInclude != null) {
					ArrayList<Double> newIMVals = new ArrayList<Double>();
					for (int rvID=0; rvID<imVals.size(); rvID++) {
						if (rvsToInclude.contains(rvID))
							newIMVals.add(imVals.get(rvID));
					}
					imVals = newIMVals;
				}
				
//				if (rvsToInclude != null) {
//					if (imVals.size() != rvHypos.size())
//						throw new RuntimeException("rv count mismatch!");
//					ArrayList<Double> newIMVals = new ArrayList<Double>();
//					for (int rvID=0; rvID<imVals.size(); rvID++) {
//						Location rvHypo = rvHypos.get(rvID);
//						if (hypo.equals(rvHypo))
//							newIMVals.add(imVals.get(rvID));
//					}
//					imVals = newIMVals;
//					System.out.println("RV's with correct hypo: " + imVals.size());
//				}
				
				double logTotal = 0;
				for (int i=0; i<imVals.size(); i++) {
					double logIM = Math.log(imVals.get(i) / HazardCurveComputation.CONVERSION_TO_G);
					logTotal += logIM;
				}
				double logMean = logTotal / (double)imVals.size();
				double mean = Math.exp(logMean);
				
				System.out.println("Mean: " + mean);
				
				xyz.set(new Location(site.lat, site.lon), mean);
				
//				try {
//					System.out.println("Getting amps for site: " + site);
//					ArrayList<Double> vals = amps2db.getIM_Values(runID, sourceID, rupID, im);
//					if (vals != null && vals.size() > 0) {
//						for (double val : vals) {
//							val /= HazardCurveComputation.CONVERSION_TO_G;
//						}
//						xyz.addValue(site.lat, site.lon, val);
//					}
//				} catch (Exception e) {
//					System.out.println("No amp for site: " + site);
//				}
			}
		}
		
		return xyz;
	}

}
