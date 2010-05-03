package org.opensha.sha.calc.hazus.parallel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.sha.calc.hazardMap.HazardDataSetLoader;

public class HazusDataSetAssmbler {
	
	public static DecimalFormat df = new DecimalFormat("0.000000");
	
	HashMap<Location, ArbitrarilyDiscretizedFunc> pgaCurves;
	HashMap<Location, ArbitrarilyDiscretizedFunc> pgvCurves;
	HashMap<Location, ArbitrarilyDiscretizedFunc> sa03Curves;
	HashMap<Location, ArbitrarilyDiscretizedFunc> sa10Curves;
	
	public HazusDataSetAssmbler(String hazardMapDir) throws IOException {
		this(hazardMapDir + File.separator + "imrs1", hazardMapDir + File.separator + "imrs2",
				hazardMapDir + File.separator + "imrs3", hazardMapDir + File.separator + "imrs4");
	}
	
	public HazusDataSetAssmbler(String pgaDir, String pgvDir, String sa03Dir, String sa10Dir) throws IOException {
		System.out.println("Loading PGA curves");
		pgaCurves = HazardDataSetLoader.loadDataSet(new File(pgaDir));
		System.out.println("Loading PGV curves");
		pgvCurves = HazardDataSetLoader.loadDataSet(new File(pgvDir));
		System.out.println("Loading SA 0.3s curves");
		sa03Curves = HazardDataSetLoader.loadDataSet(new File(sa03Dir));
		System.out.println("Loading SA 1.0s curves");
		sa10Curves = HazardDataSetLoader.loadDataSet(new File(sa10Dir));
	}
	
	public HashMap<Location, double[]> assemble(double returnPeriod, int years) {
		HashMap<Location, double[]> results = new HashMap<Location, double[]>();
		for (Location loc : pgaCurves.keySet()) {
			double pgaVal = getValFromCurve(pgaCurves.get(loc), returnPeriod, years);
			double pgvVal = getValFromCurve(pgvCurves.get(loc), returnPeriod, years);
			double sa03Val = getValFromCurve(sa03Curves.get(loc), returnPeriod, years);
			double sa10Val = getValFromCurve(sa10Curves.get(loc), returnPeriod, years);
			
			double[] result = { pgaVal, pgvVal, sa03Val, sa10Val };
			results.put(loc, result);
		}
		return results;
	}
	
	public static void writeFile(String fileName, HashMap<Location, double[]> results) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		for (Location loc : results.keySet()) {
			double[] result = results.get(loc);
			
			String line = df.format(loc.getLatitude()) + "," + df.format(loc.getLongitude());
			
			for (int i=0; i<4; i++) {
				double val = result[i];
				if (Double.isNaN(val))
					line += ",NaN";
				else
					line += "," + df.format(val);
			}
			
//			String line = df.format(loc.getLatitude()) + "," + df.format(loc.getLongitude())
//					+ "," + df.format(result[0]) + "," + df.format(result[1])
//					+ "," + df.format(result[2]) + "," + df.format(result[3]);
			fw.write(line + "\n");
		}
	}
	
	private static double getValFromCurve(ArbitrarilyDiscretizedFunc curve, double returnPeriod, int years) {
		double probVal = ((double)years) / returnPeriod;
		try {
			return curve.getFirstInterpolatedX_inLogXLogYDomain(probVal);
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	
	public static void main(String args[]) throws IOException {
		System.out.println(Double.NaN);
		HazusDataSetAssmbler assem = new HazusDataSetAssmbler("/home/kevin/OpenSHA/hazus/gridTest/curves");
		
		HashMap<Location, double[]> results = assem.assemble(1000, 50);
		writeFile("/home/kevin/OpenSHA/hazus/gridTest/curves/final_1000.dat", results);
	}

}
