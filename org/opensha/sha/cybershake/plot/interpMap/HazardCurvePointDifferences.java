package org.opensha.sha.cybershake.plot.interpMap;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.util.XYZClosestPointFinder;
import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.plot.HazardCurves2XYZ;

/**
 * This class takes the CyberShake hazard curves, and computes the difference with a base map
 * at each point. The closest point in the base map is taken.
 * 
 * @author kevin
 *
 */
public class HazardCurvePointDifferences {
	
	private XYZClosestPointFinder xyz;
	private ArrayList<CybershakeSite> sites;
	private HazardCurveFetcher fetcher;
	
	public HazardCurvePointDifferences(HazardCurveFetcher fetcher, String comparisonFile) throws IOException {
		this.fetcher = fetcher;
		xyz = new XYZClosestPointFinder(comparisonFile);
		
		sites = fetcher.getCurveSites();
	}
	
	public ArrayList<Double> getSiteDifferenceValues(boolean isProbAt_IML, double level) {
		ArrayList<Double> csVals = fetcher.getSiteValues(isProbAt_IML, level);
		
		ArrayList<Double> diff = new ArrayList<Double>();
		
		for (int i=0; i<sites.size(); i++) {
			CybershakeSite site = sites.get(i);
			double csVal = csVals.get(i);
			double compVal = xyz.getClosestVal(site.lat, site.lon);
			
			diff.add(csVal - compVal);
		}
		
		return diff;
	}
	
	public void writeXYZ(String fileName, boolean isProbAt_IML, double level) throws IOException {
		ArrayList<Double> vals = getSiteDifferenceValues(isProbAt_IML, level);
		HazardCurves2XYZ.writeXYZ(fileName, sites, vals, null);
	}
	
	public void writeLabelsFile(String labelsFile) throws IOException {
		HazardCurves2XYZ.writeLabelsFile(labelsFile, sites);
	}
	
	public static void main(String args[]) throws IOException {
		String compFile = null;
		String outFile = null;
		String labelsFile = null;
		
		if (args.length == 0) {
			System.err.println("WARNING: Running from debug mode!");
			compFile = "/home/kevin/CyberShake/baseMaps/cb2008/cb2008_base_map_2percent_hiRes_0.005.txt";
			outFile = "/home/kevin/CyberShake/interpolatedDiffMap/diffs.txt";
			labelsFile = "/home/kevin/CyberShake/interpolatedDiffMap/markers.txt";
		} else if (args.length == 1) {
			compFile = args[0];
			outFile = args[1];
			labelsFile = args[2];
		} else {
			System.err.println("USAGE: HazardCurvePointDifferences base_map_file outFile labelsFile");
			System.exit(1);
		}
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		HazardCurveFetcher fetcher = new HazardCurveFetcher(db, null, 3, 5, 21);
		
		boolean isProbAt_IML = false;
		double level = 0.0004;
		
		HazardCurvePointDifferences diff = new HazardCurvePointDifferences(fetcher, compFile);
		
		diff.writeXYZ(outFile, isProbAt_IML, level);
		diff.writeLabelsFile(labelsFile);
		
		System.exit(0);
	}

}
