package scratch.kevin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;

public class SandarshStationListCreate {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		CSVFile<String> file = CSVFile.readFile(new File("/tmp/CS_Gridded_Sites.txt"), true, 3);
		
		LocationList locs = new LocationList();
		
		WillsMap2006 wills = new WillsMap2006();
		
		for (int i=0; i<file.getNumRows(); i++) {
			List<String> line = file.getLine(i);
			double lat = Double.parseDouble(line.get(1));
			double lon = Double.parseDouble(line.get(2));
			
			locs.add(new Location(lat, lon));
		}
		
		ArrayList<Double> vals = wills.getValues(locs);
		
		CSVFile<String> out = new CSVFile<String>(true);
		
		for (int i=0; i<file.getNumRows(); i++) {
			List<String> line = file.getLine(i);
			line.add(vals.get(i)+"");
			out.addLine(line);
		}
		
		out.writeToFile(new File("/tmp/CS_Gridded_Sites_withVs30.txt"));
	}

}
