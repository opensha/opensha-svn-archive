/**
 * 
 */
package scratch.peter.nshmp;

import static org.opensha.nshmp2.util.Period.*;
import static scratch.peter.curves.ProbOfExceed.*;

import java.io.File;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.sra.rtgm.RTGM.Frequency;

import scratch.peter.curves.ProbOfExceed;

/**
 * Utility class with static methods to generate GeoDataSets.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_GeoDataUtils {
	
	private static final String NSHMP_SRC_DIR = "/Volumes/Scratch/nshmp-sources/";
	private static final String SHA_SRC_DIR = "/Volumes/Scratch/nshmp-opensha-";
	private static final String SEP = File.separator;
	private static final String CURVE_CSV = "curves.csv";
	private static final String CURVE_DAT = "curves.dat";
	private static final String RTGM_DAT = "RTGM.dat";
	


//	public static void main(String[] args) {
//		System.out.println("xyz1...");
//		GeoDataSet xyz1 = getFortranOverR3();
//		System.out.println("xyz2...");
//		GriddedRegion gr = NSHMP_UtilsDev.getNSHMP_Region(0.1);
//		GeoDataSet xyz2 = NSHMP_GeoDataUtils.getPE_Ratio("FortranLatest", "DataR3",
//			GM0P00, PE2IN50, gr);
//		System.out.println(xyz1.get(new Location(34.1, -118.3)));
//		System.out.println(xyz2.get(new Location(34.1, -118.3)));
//		
//	}
	
//	public static GeoDataSet getFortranOverR3() {
////		String path1 = NSHMP_SRC_DIR + "FortranLatest/GM0P00/curves.dat";
//		String path1 = NSHMP_SRC_DIR + "HazardTool/GM0P00/curves.dat";
//		String path2 = NSHMP_SRC_DIR + "DataR3/GM0P00/curves.dat";
//		File f1 = new File(path1);
//		File f2 = new File(path2);
////		GriddedRegion caRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED();
////		GriddedRegion dataRegion = CurveComparisonPlotter.getNationalGrid();
//		GriddedRegion dataRegion = NSHMP_UtilsDev.getNSHMP_Region(0.1);
//		
//		CurveContainer cc = null;
//		
//		cc = CurveContainer.createNational(f1);
//		GeoDataSet fortDat = NSHMP_DataUtils.extractPE(cc, dataRegion, PE2IN50);
//		System.out.println(fortDat.get(NEHRP_TestCity.MEMPHIS.location()));
//		
//		cc = CurveContainer.createNational(f2);
//		GeoDataSet r3Dat = NSHMP_DataUtils.extractPE(cc, dataRegion, PE2IN50);
//		System.out.println(r3Dat.get(NEHRP_TestCity.MEMPHIS.location()));
////		System.out.println(r3Dat.get(new Location(34.1, -118.3)));
//		
//		return GeoDataSetMath.divide(fortDat, r3Dat);
//	}
	
	
//	public static GeoDataSet getPE_Ratio(TestGrid grid, String nd, Period p,
//			ProbOfExceed pe) {
//		File f1 = new File(SHA_SRC_DIR + tg + SEP + p + SEP + CURVE_CSV);
//		File f2 = new File(NSHMP_SRC_DIR + nd + SEP + p + SEP + CURVE_DAT);
//		
//		GriddedRegion gr = tg.grid();
//		
//		CurveContainer cc = null;
//		cc = CurveContainer.create(f1, tg);
//		GeoDataSet xyz1 = NSHMP_DataUtils.extractPE(cc, gr, pe);
////		System.out.println(xyz1.getValueList());
////		System.out.println(xyz1.get(new Location(35.6, -90.4)));
//		
//		cc = CurveContainer.create(f2);
//		GeoDataSet xyz2 = NSHMP_DataUtils.extractPE(cc, gr, pe);
////		System.out.println(xyz2.getValueList());
////		System.out.println(xyz2.get(new Location(35.6, -90.4)));
//		
//		return GeoDataSetMath.divide(xyz1, xyz2);
//	}
	
	public static GeoDataSet getPE_SHA(String dir, String group, TestGrid tg, Period p, ProbOfExceed pe) {
		File f = new File(SHA_SRC_DIR + dir + "/" + group + SEP + tg + SEP + p + SEP + CURVE_CSV);
		GriddedRegion gr = tg.grid();
		CurveContainer cc = CurveContainer.create(f, tg);
		GeoDataSet xyz = NSHMP_DataUtils.extractPE(cc, gr, pe);
		return xyz;
	}

	public static GeoDataSet getPE_NSHMP(TestGrid tg, String dir, Period p, ProbOfExceed pe) {
		File f = new File(NSHMP_SRC_DIR + dir + SEP + p + SEP + CURVE_DAT);
		GriddedRegion gr = tg.grid();
		CurveContainer cc = CurveContainer.create(f);
		GeoDataSet xyz = NSHMP_DataUtils.extractPE(cc, gr, pe);
		return xyz;
	}

	public static GeoDataSet getPE_Ratio_SHAoNSHMP(String shaDir, String dir, TestGrid tg, String nd, Period p,
			ProbOfExceed pe) {
		File f1 = new File(SHA_SRC_DIR + shaDir + SEP + dir + SEP + tg + SEP + p + SEP + CURVE_CSV);
		File f2 = new File(NSHMP_SRC_DIR + nd + SEP + p + SEP + CURVE_DAT);
		
		GriddedRegion gr = tg.grid();
		
		CurveContainer cc = null;
		cc = CurveContainer.create(f1, tg);
		GeoDataSet xyz1 = NSHMP_DataUtils.extractPE(cc, gr, pe);
//		System.out.println(xyz1.getValueList());
//		System.out.println(xyz1.get(new Location(35.6, -90.4)));
		
		cc = CurveContainer.create(f2);
		GeoDataSet xyz2 = NSHMP_DataUtils.extractPE(cc, gr, pe);
//		System.out.println(xyz2.getValueList());
//		System.out.println(xyz2.get(new Location(35.6, -90.4)));
		
		return GeoDataSetMath.divide(xyz1, xyz2);
	}
	
	public static GeoDataSet getPE_Ratio_SHA(String shaDir, String dir1, String dir2, TestGrid tg, Period p,
			ProbOfExceed pe) {
		File f1 = new File(SHA_SRC_DIR + shaDir + SEP + dir1 + SEP + tg + SEP + p + SEP + CURVE_CSV);
		File f2 = new File(SHA_SRC_DIR + shaDir + SEP + dir2 + SEP + tg + SEP + p + SEP + CURVE_CSV);
//		File f2 = new File(NSHMP_SRC_DIR + nd + SEP + p + SEP + CURVE_DAT);
		
		GriddedRegion gr = tg.grid();
		
		CurveContainer cc = null;
		cc = CurveContainer.create(f1, tg);
		GeoDataSet xyz1 = NSHMP_DataUtils.extractPE(cc, gr, pe);
//		System.out.println(xyz1.getValueList());
//		System.out.println(xyz1.get(new Location(42.0, -124.5)));
		
		cc = CurveContainer.create(f2, tg);
		GeoDataSet xyz2 = NSHMP_DataUtils.extractPE(cc, gr, pe);
//		System.out.println(xyz2.getValueList());
//		System.out.println(xyz2.get(new Location(42.0, -124.5)));
		
		return GeoDataSetMath.divide(xyz1, xyz2);
	}

	
	public static GeoDataSet tmpPEratio(String shaDir1, String dir1, String shaDir2, String dir2, TestGrid tg1, TestGrid tg2, Period p,
			ProbOfExceed pe) {
		File f1 = new File(SHA_SRC_DIR + shaDir1 + SEP + dir1 + SEP + tg1 + SEP + p + SEP + CURVE_CSV);
		File f2 = new File(SHA_SRC_DIR + shaDir2 + SEP + dir2 + SEP + tg2 + SEP + p + SEP + CURVE_CSV);
//		File f2 = new File(NSHMP_SRC_DIR + nd + SEP + p + SEP + CURVE_DAT);
		
		
		CurveContainer cc = null;
		cc = CurveContainer.create(f1, tg1);
		GeoDataSet xyz1 = NSHMP_DataUtils.extractPE(cc, tg1.grid(), pe);
		
		cc = CurveContainer.create(f2, tg2);
		GeoDataSet xyz2 = NSHMP_DataUtils.extractPE(cc, tg1.grid(), pe);

		GeoDataSet xyz3 = GeoDataSetMath.divide(xyz1, xyz2);
		
		System.out.println("lat   meanUC2   nshmp   div   check");
		double lon = -118.8;
		double[]  lats = { 35.8, 35.9, 36.0, 36.1, 36.2 };
		for (double lat : lats) {
			double muc = xyz1.get(new Location(lat, lon));
			double nshmp = xyz2.get(new Location(lat, lon));
			double toolsDiff = xyz3.get(new Location(lat, lon));
			double diff = muc / nshmp;
			System.out.println(
				String.format("%.2f", lat) + " " +
				String.format("%.5f", muc) + " " + 
				String.format("%.5f", nshmp) + " " + 
				String.format("%.5f", toolsDiff) + " " + 
				String.format("%.5f", diff));
		}
		
		return xyz3;
	}


/**
	 * Returns a data set that is the ratio of two probability of
	 * exceedence level data sets (d1/d2). The data sets to be used
	 * are indicated by their folder name and period. A Region of data to
	 * extract must also be provided.
	 * @param d1 
	 * @param d2 
	 * @param p 
	 * @param pe 
	 * @param gr 
	 * @return the geo data set
	 */
	public static GeoDataSet getPE_Ratio(String d1, String d2, Period p,
			ProbOfExceed pe, GriddedRegion gr) {
		File f1 = new File(NSHMP_SRC_DIR + d1 + SEP + p + SEP + CURVE_DAT);
		File f2 = new File(NSHMP_SRC_DIR + d2 + SEP + p + SEP + CURVE_DAT);

		CurveContainer cc = null;
		cc = CurveContainer.create(f1);
		GeoDataSet xyz1 = NSHMP_DataUtils.extractPE(cc, gr, pe);
//		System.out.println(xyz1.get(new Location(34.1, -118.3)));
		
		cc = CurveContainer.create(f2);
		GeoDataSet xyz2 = NSHMP_DataUtils.extractPE(cc, gr, pe);
//		System.out.println(xyz1.get(new Location(34.1, -118.3)));
		
		return GeoDataSetMath.divide(xyz1, xyz2);
	}
	
	public static GeoDataSet getRTGM_Ratio(String d1, String d2,
			Frequency f, GriddedRegion gr) {
		File f1 = new File(NSHMP_SRC_DIR + d1 + SEP + RTGM_DAT);
		File f2 = new File(NSHMP_SRC_DIR + d2 + SEP + RTGM_DAT);

		RTGM_Container rc = null;
		rc = RTGM_Container.create(f1);
		GeoDataSet xyz1 = NSHMP_DataUtils.extractRTGM(rc, gr, f);
//		System.out.println(xyz1.get(new Location(34.1, -118.3)));
		
		rc = RTGM_Container.create(f2);
		GeoDataSet xyz2 = NSHMP_DataUtils.extractRTGM(rc, gr, f);
//		System.out.println(xyz1.get(new Location(34.1, -118.3)));
		
		return GeoDataSetMath.divide(xyz1, xyz2);
	}
	
	public static GeoDataSet getRTGM_Ratio(TestGrid tg, String shaDir, 
			String nd, Frequency f) {
		File f1 = new File(SHA_SRC_DIR + shaDir + "/" + tg + SEP + RTGM_DAT);
		File f2 = new File(NSHMP_SRC_DIR + nd + SEP + RTGM_DAT);
		
		GriddedRegion gr = tg.grid();
		
		RTGM_Container rc = null;
		rc = RTGM_Container.create(f1);
		GeoDataSet xyz1 = NSHMP_DataUtils.extractRTGM(rc, gr, f);
//		System.out.println(xyz1.getValueList());
//		System.out.println(xyz1.get(new Location(35.6, -90.4)));
		
		rc = RTGM_Container.create(f2);
		GeoDataSet xyz2 = NSHMP_DataUtils.extractRTGM(rc, gr, f);
//		System.out.println(xyz2.getValueList());
//		System.out.println(xyz2.get(new Location(35.6, -90.4)));
		
		return GeoDataSetMath.divide(xyz1, xyz2);
	}

	
	
//	public static GeoDataSet test() {
//		String path = "/Volumes/Scratch/nshmp-sources/FortranLatest/GM0P00/curves_us_all.pga";
////		String path = "/Volumes/Scratch/nshmp-sources/HazardTool/2008.US.0p00.760.txt";
////		String path = "/Volumes/Scratch/nshmp-sources/DataR3/2008.US.pga.txt";
//		File f = new File(path);
//		System.out.println("Reading data file...");
//		CurveContainer cc = CurveContainer.createNational(f);
//		System.out.println("Extracting values...");
//		
////		System.out.println(cc.getCurve(new Location(34.1, -118.3)));
////		System.out.println(cc.getCurve(NEHRP_TestCity.LOS_ANGELES.location()));
//		
//		GriddedRegion caRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED();
//		
//		GeoDataSet gDat = NSHMP_DataUtils.extractPE(cc, caRegion, ProbOfExceed.PE2IN50);
//		System.out.println(gDat.get(new Location(34.1, -118.3)));
//		
//		return gDat;
//	}
	
	public static GeoDataSet oneMinus(GeoDataSet xyz) {
		for (int i=0;i<xyz.size();i++) {
			xyz.set(i, 1 - xyz.get(i));
		}
		return xyz;
	}
	
	public static GeoDataSet minusOne(GeoDataSet xyz) {
		for (int i=0;i<xyz.size();i++) {
			xyz.set(i, xyz.get(i) - 1);
		}
		return xyz;
	}
	
	public static GeoDataSet multiply(GeoDataSet xyz, double value) {
		for (int i=0;i<xyz.size();i++) {
			xyz.set(i, xyz.get(i) * value);
		}
		return xyz;
	}

}
