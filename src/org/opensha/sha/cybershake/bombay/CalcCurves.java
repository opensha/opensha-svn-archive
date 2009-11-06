package org.opensha.sha.cybershake.bombay;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.calc.RuptureVariationProbabilityModifier;
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
	
	public void calc(String dir, RuptureProbabilityModifier rupProbMod,
			RuptureVariationProbabilityModifier rupVarProbMod) throws IOException {
		calc.setRupRpobModifier(rupProbMod);
		calc.setRupVarProbModifier(rupVarProbMod);
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
			String modDir = baseDir + "modCurves_noMod/";
			
			Div365ProbModifier div365 = new Div365ProbModifier();
			
			calc.calc(origDir, div365, null);
			BombayBeachHazardCurveCalc bombay = new BombayBeachHazardCurveCalc(db, increaseMultFactor);
			calc.calc(modDir, div365, bombay);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

}
