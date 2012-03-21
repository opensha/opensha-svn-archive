package scratch.UCERF3.griddedSeismicity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.ExceptionUtils;

import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class DefModelSpatialPDF_Generator {
	
	public static final String SUBDIR = "DeformationModels";
	public static final String FILENAME = "gridded_moment_latlon_3_21.txt";
	
	final static CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion  = new CaliforniaRegions.RELM_TESTING_GRIDDED();
	GriddedGeoDataSet neoKin_xyzData, zeng_xyzData, aveBlockMod_xyzData, geoBlockMod_xyzData;
	
	
	public DefModelSpatialPDF_Generator() {
		readData();
	}

	
	
	private void readData() {
		neoKin_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		zeng_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		aveBlockMod_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		geoBlockMod_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude

		try {
			BufferedReader reader = new BufferedReader(UCERF3_DataUtils.getReader(SUBDIR, FILENAME));
			int l=-1;
			String line;
			while ((line = reader.readLine()) != null) {
				l+=1;
				if (l == 0)
					continue;
				String[] st = StringUtils.split(line,"\t");
				Location loc = new Location(Double.valueOf(st[0]),Double.valueOf(st[1]));
				int index = griddedRegion.indexForLocation(loc);
				neoKin_xyzData.set(index, Double.valueOf(st[2]));
				zeng_xyzData.set(index, Double.valueOf(st[3]));
				aveBlockMod_xyzData.set(index, Double.valueOf(st[4]));
				geoBlockMod_xyzData.set(index, Double.valueOf(st[5]));
			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		normalizeData(neoKin_xyzData);
		normalizeData(zeng_xyzData);
		normalizeData(aveBlockMod_xyzData);
		normalizeData(geoBlockMod_xyzData);

	}
	
	
	/**
	 * this normalizes the data so they sum to 1.0
	 * @param data
	 */
	private static void normalizeData(GriddedGeoDataSet data) {
		double sum=0;
		for(int i=0;i<data.size();i++) 
			sum += data.get(i);
		for(int i=0;i<data.size();i++) 
			data.set(i, data.get(i)/sum);
	}
	
	private void testPlotMap() {
		try {
			GMT_CA_Maps.plotSpatialPDF_Map(neoKin_xyzData, "test", "test meta data", "testMap");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DefModelSpatialPDF_Generator test = new DefModelSpatialPDF_Generator();
		test.testPlotMap();
	}

}
