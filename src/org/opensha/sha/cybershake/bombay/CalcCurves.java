package org.opensha.sha.cybershake.bombay;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.sha.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;

import scratch.kevin.cybershake.BulkCSCurveReplacer;

public class CalcCurves {
	
	private DBAccess db;
	
	private BulkCSCurveReplacer calc;
	
	public CalcCurves(DBAccess db, ArrayList<Integer> ims) {
		this.db = db;
		calc = new BulkCSCurveReplacer(db);
		calc.setRecalcIMs(ims);
	}
	
	public void calc(String dir, RuptureProbabilityModifier rupProbMod) throws IOException {
		calc.setRupRpobModifier(rupProbMod);
		calc.recalculateAllCurves(dir);
	}
	
	public static void main(String args[]) {
		try {
			DBAccess db = Cybershake_OpenSHA_DBApplication.db;
			ArrayList<Integer> ims = new ArrayList<Integer>();
			ims.add(21);
			CalcCurves calc = new CalcCurves(db, ims);
			
			String baseDir = "/home/kevin/CyberShake/bombay/";
			double increaseMultFactor = 1000;
			
			String origDir = baseDir + "origCurves/";
			String modDir = baseDir + "modCurves/";
			
			calc.calc(origDir, new Div365ProbModifier());
			BombayBeachHazardCurveCalc bombay = new BombayBeachHazardCurveCalc(db, increaseMultFactor);
			calc.calc(modDir, bombay);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

}
