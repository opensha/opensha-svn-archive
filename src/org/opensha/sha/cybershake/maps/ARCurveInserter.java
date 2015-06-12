package org.opensha.sha.cybershake.maps;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;
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
	
	private static Map<Location, ArbitrarilyDiscretizedFunc> loadCurves(File dir) {
		System.out.println("Loading curves form: "+dir.getAbsolutePath());
		HashMap<Location, ArbitrarilyDiscretizedFunc> map =
			new HashMap<Location, ArbitrarilyDiscretizedFunc>();
		
		if (dir.isFile() && dir.getName().endsWith(".bin")) {
			// binary format
			
			try {
				BinaryHazardCurveReader reader = new BinaryHazardCurveReader(dir.getAbsolutePath());
				return reader.getCurveMap();
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
		
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
//		String dir = "/home/kevin/CyberShake/baseMaps/ave2008/curves_3sec";
//		String dir = "/home/kevin/CyberShake/baseMaps/2012_05_22-cvmh/AVG2008";
//		String dir = "/home/kevin/CyberShake/baseMaps/2013_11_07-cvm4-cs-nga2/CY2013/curves/imrs1/";
//		String dir = "/home/kevin/CyberShake/baseMaps/2014_03_03-cvm4i26-cs-nga-3sec/CB2008/curves/imrs1.bin";
//		String dir = "/home/kevin/CyberShake/baseMaps/2014_03_05-cvmhnogtl-cs-nga-3sec/AVE2008/curves/imrs1.bin";
//		String dir = "/home/kevin/CyberShake/baseMaps/2015_05_27-cvm4i26-cs-nga-2sec/NGA_2008/curves/imrs1.bin";
		String dir = "/home/kevin/CyberShake/baseMaps/2014_03_18-cvm4i26-cs-nga-5sec/AVE2008/curves/imrs1.bin";
		// UPDATE IM TYPE AND DATE BELOW!!!!!!!!!!!!!!!!!!!!!!!
		boolean deleteOld = false;
		ScalarIMR imr = AttenRelRef.NGA_2008_4AVG.instance(null);
		imr.setParamDefaults();
		setTruncation(imr, 3d);
		int erfID = 35;
//		int velModelID = 1;
//		int velModelID = 8; // BBP 1D
		int velModelID = 5; // CVMS-4i26
//		int velModelID = 7; // CVM-H 11.9 no gtl
		int probModelID = 1;
		int timeSpanID = 1;
//		int imTypeID = 21; // 3sec GEOM
//		int imTypeID = 26; // 2sec GEOM
		int imTypeID = 11; // 5sec GEOM
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(2014, 3, 18);
		Date calcDate = cal.getTime();
		Date timeSpanDate = null;
		// for small insert tests
//		MAX_CURVES_TO_INSERT = 0;
		
		// load the curves
		Map<Location, ArbitrarilyDiscretizedFunc> curves = loadCurves(new File(dir));
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
			if (datasetID < 0) {
				int ret = JOptionPane.showConfirmDialog(null, "Add new Dataset ID?", "Dataset ID not found", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					double gridSpacing = Double.POSITIVE_INFINITY;
					MinMaxAveTracker latTrack = new MinMaxAveTracker();
					MinMaxAveTracker lonTrack = new MinMaxAveTracker();
					Location prevLoc = null;
					for (Location loc : curves.keySet()) {
						double lat = loc.getLatitude();
						double lon = loc.getLongitude();
						latTrack.addValue(lat);
						lonTrack.addValue(lon);
						if (prevLoc != null) {
							double diff = Math.abs(lon - prevLoc.getLongitude());
							if (diff < gridSpacing && (float)diff > 0f)
								gridSpacing = diff;
						}
						
						prevLoc = loc;
					}
					datasetID = arDataSets2DB.addDataSetID(arID, erfID, velModelID, probModelID, timeSpanID, timeSpanDate,
							latTrack.getMin(), latTrack.getMax(), lonTrack.getMin(), lonTrack.getMax(), gridSpacing);
				} else {
					System.exit(1);
				}
			}
			
			if (deleteOld)
				arCurves2DB.deleteAllCurvesFromDataset(datasetID, imTypeID);
			
			arCurves2DB.insertARCurves(calcDate, datasetID, imTypeID, curves);
		} catch (Exception e) {
			e.printStackTrace();
		}
		db.destroy();
		System.exit(0);
	}

}
