package org.opensha.sha.cybershake.plot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.sha.cybershake.HazardCurveFetcher;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.util.cpt.CPT;

public class HazardCurves2XYZ {
	
	private DBAccess db;
	
	private HazardCurveFetcher fetcher;
	
	private ArrayList<Integer> siteTypeIDs;
	
	public HazardCurves2XYZ(DBAccess db, ArrayList<Integer> erfIDs, int rupVarScenarioID, int sgtVarID, int imTypeID) {
		this(db, erfIDs, rupVarScenarioID, sgtVarID, imTypeID, null);
	}
	
	public HazardCurves2XYZ(DBAccess db, ArrayList<Integer> erfIDs, int rupVarScenarioID, int sgtVarID, int imTypeID,
			ArrayList<Integer> siteTypeIDs) {
		this.db = db;
		this.siteTypeIDs = siteTypeIDs;
		fetcher = new HazardCurveFetcher(db, erfIDs, rupVarScenarioID, sgtVarID, imTypeID);
	}
	
	public void writeXYZ(String fileName, boolean isProbAt_IML, double level) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		
		ArrayList<Double> vals = fetcher.getSiteValues(isProbAt_IML, level);
		ArrayList<CybershakeSite> sites = fetcher.getCurveSites();
		
		if (vals.size() != sites.size()) {
			throw new RuntimeException("Number of curves and curve sites not consistant!");
		}
		
		for (int i=0; i<vals.size(); i++) {
			double val = vals.get(i);
			CybershakeSite site = sites.get(i);
			
			if (siteTypeIDs != null) {
				if (!siteTypeIDs.contains(site.type_id))
					continue;
			}
			
			fw.write(site.lon + "\t" + site.lat + "\t" + val + "\n");
		}
		
		fw.close();
	}
	
	public static void main(String args[]) {
		String curvesFile = null;
		String labelsFile = null;
		String types = "";
		
		if (args.length == 0) {
			System.err.println("WARNING: Running from debug mode!");
			curvesFile = "/home/kevin/CyberShake/interpolatedMap/allCurves.txt";
			curvesFile = "/home/kevin/CyberShake/interpolatedMap/20km.txt";
			labelsFile = "/home/kevin/CyberShake/interpolatedMap/markers.txt";
			types = "5";
		} else if (args.length == 2 || args.length == 3) {
			curvesFile = args[0];
			labelsFile = args[1];
			if (args.length == 3) {
				types = args[2];
			}
		} else {
			System.err.println("USAGE: HazardCurves2XYZ curvesFile labelsFile [siteTypeIDs]");
			System.exit(2);
		}
		
		ArrayList<Integer> typeIDs = null;
		
		if (types.length() > 0) {
			ArrayList<String> idSplit = HazardCurvePlotter.commaSplit(types);
			typeIDs = new ArrayList<Integer>();
			for (String idStr : idSplit) {
				int id = Integer.parseInt(idStr);
				typeIDs.add(id);
				System.out.println("Added site type: " + id);
			}
		}
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		HazardCurves2XYZ xyz = new HazardCurves2XYZ(db, null, 3, 5, 21, typeIDs);
		
		boolean isProbAt_IML = false;
		double level = 0.0004;
		try {
			System.out.println("Writing " + curvesFile);
			xyz.writeXYZ(curvesFile, isProbAt_IML, level);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		CPT cpt = null;

		ScatterSymbol circle = new ScatterSymbol(ScatterSymbol.SYMBOL_CIRCLE, CybershakeSite.TYPE_POI, 0.5 * 0.75);
		HazardMapScatterCreator scatter = new HazardMapScatterCreator(db, null, 3, 5, 21, cpt, isProbAt_IML, level);
		
		ArrayList<ScatterSymbol> symbols = HazardMapScatterCreator.getCyberShakeSymbols(0.5);
		if (typeIDs != null) {
			for (ScatterSymbol symbol : symbols) {
				if (!typeIDs.contains(symbol.getSiteTypeID())) {
					symbol.setSymbol(ScatterSymbol.SYMBOL_INVISIBLE);
					System.out.println("Making site type " + symbol.getSiteTypeID() + " invisible!");
				}
			}
		}
		
		try {
			System.out.println("Writing " + labelsFile);
			scatter.writeScatterMarkerScript(symbols, circle,
					labelsFile, true, true, 6);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		System.exit(0);
	}

}
