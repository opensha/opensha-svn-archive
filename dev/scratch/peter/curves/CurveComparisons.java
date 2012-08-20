package scratch.peter.curves;

import java.io.File;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GeoDataSet;
import org.opensha.commons.data.xyz.GeoDataSetMath;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.nshmp.NEHRP_TestCity;

import com.google.common.base.Stopwatch;

public class CurveComparisons {

	private static final double PE2IN50 = 0.000404;
	private static final double PE5IN50 = 0.001026;
	private static final double PE10IN50 = 0.02107;
	
	public static void main(String[] args) {
		test();
	}

	public static GeoDataSet getFortranOverR3() {
		String path1 = "/Volumes/Scratch/nshmp-sources/FortranLatest/GM0P00/curves_us_all.pga";
		String path2 = "/Volumes/Scratch/nshmp-sources/DataR3/2008.US.pga.txt";
		File f1 = new File(path1);
		File f2 = new File(path2);
		GriddedRegion gr = CurveContainer.getNSHMP_Region();
		GriddedRegion caRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		CurveContainer cc = null;
		
		cc = CurveContainer.create(f1, gr);
		GeoDataSet fortDat = extractData(cc, caRegion, ProbOfExceed.PE2IN50);
		
		cc = CurveContainer.create(f1, gr);
		GeoDataSet r3Dat = extractData(cc, caRegion, ProbOfExceed.PE2IN50);
		
		return GeoDataSetMath.divide(fortDat, r3Dat);
		
	}
	
	public static GeoDataSet test() {
		String path = "/Volumes/Scratch/nshmp-sources/FortranLatest/GM0P00/curves_us_all.pga";
//		String path = "/Volumes/Scratch/nshmp-sources/HazardTool/2008.US.0p00.760.txt";
//		String path = "/Volumes/Scratch/nshmp-sources/DataR3/2008.US.pga.txt";
		File f = new File(path);
		GriddedRegion gr = CurveContainer.getNSHMP_Region();
		System.out.println("Reading data file...");
		CurveContainer cc = CurveContainer.create(f, gr);
		System.out.println("Extracting values...");
		
//		System.out.println(cc.getCurve(new Location(34.1, -118.3)));
//		System.out.println(cc.getCurve(NEHRP_TestCity.LOS_ANGELES.location()));
		
		GriddedRegion caRegion = new CaliforniaRegions.RELM_TESTING_GRIDDED();
		
		GeoDataSet gDat = extractData(cc, caRegion, ProbOfExceed.PE2IN50);
		System.out.println(gDat.get(new Location(34.1, -118.3)));
		
		return gDat;
	}
	
	
	public static GeoDataSet extractData(CurveContainer cc,
			GriddedRegion region, ProbOfExceed pe) {
		GriddedGeoDataSet gDat = new GriddedGeoDataSet(region, true);
		double targetRate = pe.annualRate();
		for (Location loc : region) {
			
			// two possible problems can arise here: (1) the region to extract
			// has points outside the data source region and (2) the data
			// source curve may have uniform very low values and fails
			// interpolation. In either case set ground motion value to 0
			
			double gm = 0;
			try {
				DiscretizedFunc f = cc.getCurve(loc);
				gm = f.getFirstInterpolatedX_inLogXLogYDomain(targetRate);
			} catch (Exception e) {
				// do nothing; let gm be 0
			}
			gDat.set(loc, gm);
		}
		return gDat;
	}
	
	
	
}
