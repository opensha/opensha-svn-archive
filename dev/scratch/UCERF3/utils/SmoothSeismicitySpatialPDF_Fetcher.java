package scratch.UCERF3.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.ExceptionUtils;

import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;

/**
 * This reads and provides the smoothed seismicity spatial PDFs 
 * @author field
 *
 */
public class SmoothSeismicitySpatialPDF_Fetcher {
	
	public static final String SUBDIR = "SeismicityGrids";
	public static final String FILENAME_UCERF2 = "SmoothSeis_UCERF2.txt";
	public static final String FILENAME_UCERF3 = "SmoothSeis_KF_5-5-2012.txt";
	
	final static CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion  = new CaliforniaRegions.RELM_TESTING_GRIDDED();
	
	GriddedGeoDataSet ucerf2_pdfData, ucerf3_pdfData;
	
	
	public static GriddedGeoDataSet getUCERF2_PDF() {
		return readPDF_Data(FILENAME_UCERF2);
	}
	
	
	public static GriddedGeoDataSet getUCERF3_PDF() {
		return readPDF_Data(FILENAME_UCERF3);
	}
	

	private static GriddedGeoDataSet readPDF_Data(String filename) {
		GriddedGeoDataSet pdfData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		try {
			BufferedReader reader = new BufferedReader(UCERF3_DataUtils.getReader(SUBDIR, filename));
			int l=-1;
			String line;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				Location loc = new Location(Double.valueOf(tokenizer.nextElement().toString()),Double.valueOf(tokenizer.nextElement().toString()));
				int index = griddedRegion.indexForLocation(loc);
				if(index >=0)
					pdfData.set(index, Double.valueOf(tokenizer.nextElement().toString()));
			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
//		System.out.println("min="+pdfData.getMinZ());
//		System.out.println("max="+pdfData.getMaxZ());
//		System.out.println("sum="+getSumOfData(pdfData));
		return pdfData;
	}
	
	
	
	/**
	 * this normalizes the data so they sum to 1.0
	 * @param data
	 */
	private static double getSumOfData(GriddedGeoDataSet data) {
		double sum=0;
		for(int i=0;i<data.size();i++) 
			sum += data.get(i);
		return sum;
	}
	
	private static void plotMaps() {
		try {
			GMT_CA_Maps.plotSpatialPDF_Map(getUCERF2_PDF(), "UCERF2_SmoothSeisPDF", "test meta data", "UCERF2_SmoothSeisPDF_Map");
			GMT_CA_Maps.plotSpatialPDF_Map(getUCERF3_PDF(), "UCERF3_SmoothSeisPDF", "test meta data", "UCERF3_SmoothSeisPFD_Map");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		plotMaps();
	}

}
