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

import scratch.peter.curves.ProbOfExceed;

/**
 * Utility class with static methods to generate GeoDataSets.
 *
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_GeoDataUtils {
	
	private static final String NSHMP_SRC_DIR = "/Volumes/Scratch/nshmp-sources/";
	private static final String SHA_SRC_DIR = "/Volumes/Scratch/nshmp-opensha/";
	private static final String SEP = File.separator;
	private static final String CURVE_CSV = "curves.csv";
	private static final String CURVE_DAT = "curves.dat";


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
	
	
	
	public static GeoDataSet getPE_Ratio(TestGrid tg, String nd, Period p,
			ProbOfExceed pe) {
		File f1 = new File(SHA_SRC_DIR + tg + SEP + p + SEP + CURVE_CSV);
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
	
	/**
	 * Returns a data set that is the % difference between two probability of
	 * exceedence level data sets. i.e. 1 - d1/d2. The data sets to be used
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
	
	public static GeoDataSet getRTGM_Ratio(String d1, String d2, Period p,
			ProbOfExceed pe, GriddedRegion gr) {
		return null;
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

}
