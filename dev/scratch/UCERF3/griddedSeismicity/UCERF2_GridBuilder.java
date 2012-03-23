package scratch.UCERF3.griddedSeismicity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;

import com.google.common.collect.Lists;
import com.google.common.io.LittleEndianDataInputStream;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class UCERF2_GridBuilder {

	
	public static void main(String[] args) {
		generateUCERF2pdf();
	}
	
	private static void generateUCERF2pdf() {
		double minLat = 24.6;
		double maxLat = 50.0;
		double dLat  = 0.1;
		double minLon = -125.0;
		double maxLon = -100.0;
		double dLon = 0.1;
		
		GriddedRegion gridRegion = new GriddedRegion(
			new Location(minLat, minLon+0.001),
			new Location(maxLat, maxLon+0.001),
			dLat, GriddedRegion.ANCHOR_0_0);
		GriddedRegion ucerfRegion = 
				new CaliforniaRegions.RELM_TESTING_GRIDDED();
		
		int nRows = (int) Math.rint((maxLat - minLat) / dLat) + 1;
		int nCols = (int) Math.rint((maxLon - minLon) / dLon) + 1;

		List<String> gridNames = Lists.newArrayList();
		gridNames.add("CA/gridded/GR_DOS/agrd_brawly.out");
		gridNames.add("CA/gridded/GR_DOS/agrd_mendos.out");
		gridNames.add("CA/gridded/GR_DOS/agrd_creeps.out");
		gridNames.add("CA/gridded/GR_DOS/agrd_deeps.out");
		gridNames.add("CA/gridded/GR_DOS/agrd_impext.out");
		gridNames.add("CA/gridded/GR_DOS/agrd_cstcal.out");
		gridNames.add("WUS/gridded/GR_DOS/agrd_wuscmp.out");
		gridNames.add("WUS/gridded/GR_DOS/agrd_wusext.out");
		
		double[] gridSum = null;
		
		for (String gridName : gridNames) {
			File gridFile = new File("src/resources/data/nshmp/sources/" + 
				gridName);
//			System.out.println(gridFile);
//			System.out.println(gridFile.exists());
			double[] aDat = readGrid(gridFile, nRows, nCols);
			if (gridSum == null) {
				gridSum = aDat;
				continue;
			}
			addArray(gridSum, aDat);
		}
		
		double regionSum = 0.0;
		for (Location loc : ucerfRegion) {
			int idx = gridRegion.indexForLocation(loc);
			regionSum += (idx == -1) ? 0.0 : gridSum[idx];
		}
		
		List<String> records = Lists.newArrayList();
		for (Location loc : ucerfRegion) {
			int idx = gridRegion.indexForLocation(loc);
			double value = (idx == -1) ? 0.0 : gridSum[idx] / regionSum;
			records.add(String.format(
				"%.3f %.3f %.10f", 
				loc.getLatitude(), 
				loc.getLongitude(),
				value));
		}
		File dir = new File("tmp");
		File out = new File(dir, "SmoothSeis_UCERF2.txt");
		try {
			FileUtils.writeLines(out, records);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		
//		System.out.println(gridSum.length);
//		System.out.println(region.getNodeCount());
		

		
	}
	
	private static void addArray(double[] a1, double[] a2) {
		for (int i=0; i<a1.length; i++) {
			a1[i] += a2[i];
		}
	}

	/*
	 * This method converts an NSHMP index to the correct GriddedRegion index
	 */
	private static int calcIndex(int idx, int nRows, int nCols) {
		return (nRows - (idx / nCols) - 1) * nCols + (idx % nCols);
		// compact form of:
		// int col = idx % nCols;
		// int row = idx / nCols;
		// int targetRow = nRows - row - 1;
		// return targetRow * nCols + col;
	}

	/**
	 * Method reads a binary file of data into an array. This method is tailored
	 * to the NSHMP grid files that are stored from top left to bottom right,
	 * reading across. The nodes in OpenSHA <code>GriddedRegion</code>s are
	 * stored from bottom left to top right, also reading across. This method
	 * places values at their proper index. <i><b>NOTE</b></i>: NSHMP binary
	 * grid files are all currently little-endian. The grid files in some other
	 * parts of the USGS seismic hazard world are big-endian. Beware.
	 * @param file to read
	 * @param nRows
	 * @param nCols
	 * @return a 1D array of appropriately ordered values
	 */
	private static double[] readGrid(File file, int nRows, int nCols) {
		int count = nRows * nCols;
		double[] data = new double[count];
		try {
			LittleEndianDataInputStream in = new LittleEndianDataInputStream(
				FileUtils.openInputStream(file));
			for (int i = 0; i < count; i++) {
				double value = new Float(in.readFloat()).doubleValue();
				data[calcIndex(i, nRows, nCols)] = value;
			}
			in.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		return data;
	}


}
