package org.opensha.sha.cybershake.maps;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;
import org.opensha.sha.cybershake.db.AttenRelCurves2DB;
import org.opensha.sha.cybershake.db.AttenRelDataSets2DB;
import org.opensha.sha.cybershake.db.AttenRels2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;

public class ARCurveInserter {
	
	private static int MAX_CURVES_TO_INSERT = -1;
	
	private static HashMap<Location, ArbitrarilyDiscretizedFunc> loadCurves(File dir) {
		System.out.println("Loading curves form: "+dir.getAbsolutePath());
		HashMap<Location, ArbitrarilyDiscretizedFunc> map =
			new HashMap<Location, ArbitrarilyDiscretizedFunc>();
		
		for (File file : dir.listFiles()) {
			if (MAX_CURVES_TO_INSERT >= 0 && map.size() >= MAX_CURVES_TO_INSERT)
				return map;
			
			if (file.isDirectory()) {
				map.putAll(loadCurves(file));
				continue;
			}
			if (!file.getName().endsWith(".txt"))
				continue;
			
			try {
				Location loc = HazardDataSetLoader.decodeFileName(file.getName());
				if (loc == null)
					continue;
				
				ArbitrarilyDiscretizedFunc curve =
					ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile(file.getAbsolutePath());
				map.put(loc, curve);
			} catch (Exception e) {
				continue;
			}
		}
		
		return map;
	}
	
	private static void setTruncation(ScalarIMR imr, double trunc) {
		imr.getParameter(SigmaTruncLevelParam.NAME).setValue(trunc);
		if (trunc < 0)
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
		else
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String dir = "/home/kevin/CyberShake/baseMaps/ave2008/curves_3sec";
		ScalarIMR imr = AttenRelRef.NGA_2008_4AVG.instance(null);
		imr.setParamDefaults();
		setTruncation(imr, 3.0);
		int erfID = 35;
		int velModelID = 1;
		int probModelID = 1;
		int timeSpanID = 1;
		int imTypeID = 21;
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2009, 9, 2);
		Date calcDate = cal.getTime();
		Date timeSpanDate = null;
		// for small insert tests
//		MAX_CURVES_TO_INSERT = 0;
		
		// load the curves
		HashMap<Location, ArbitrarilyDiscretizedFunc> curves = loadCurves(new File(dir));
		System.out.println("Loaded "+curves.size()+" curves");
		
		DBAccess db = Cybershake_OpenSHA_DBApplication.getAuthenticatedDBAccess(true, true);
		
		AttenRelDataSets2DB arDataSets2DB = new AttenRelDataSets2DB(db);
		AttenRelCurves2DB arCurves2DB = new AttenRelCurves2DB(db);
		AttenRels2DB ar2db = new AttenRels2DB(db);
		
		try {
			int arID = ar2db.getAttenRelID(imr);
			if (arID < 0)
				throw new RuntimeException("AR not found!");
			
			int datasetID = arDataSets2DB.getDataSetID(arID, erfID, velModelID, probModelID, timeSpanID, timeSpanDate);
			if (datasetID < 0)
				throw new RuntimeException("AR Dataset not found!");
			
			arCurves2DB.insertARCurves(calcDate, datasetID, imTypeID, curves);
		} catch (Exception e) {
			e.printStackTrace();
		}
		db.destroy();
		System.exit(0);
	}

}
