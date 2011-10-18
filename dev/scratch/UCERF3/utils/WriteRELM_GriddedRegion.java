package scratch.UCERF3.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.ExceptionUtils;

public class WriteRELM_GriddedRegion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CaliforniaRegions.RELM_GRIDDED griddedRegion  = new CaliforniaRegions.RELM_GRIDDED();
		File file = new File ("dev/scratch/UCERF3/preComputedData/", "RELM_GriddedRegion.txt");
		FileOutputStream file_output;
		try {
			file_output = new FileOutputStream (file);
			DataOutputStream data_out = new DataOutputStream (file_output);
			data_out.writeChars("lat\tlon\n");
			for(int i=0; i<griddedRegion.getNumLocations(); i++) {
				Location loc = griddedRegion.getLocation(i);
				String lat = Float.toString((float)loc.getLatitude());
				String lon = Float.toString((float)loc.getLongitude());
				data_out.writeChars(lat+"\t"+lon+"\n");
			}
			file_output.close ();
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}


	}

}
