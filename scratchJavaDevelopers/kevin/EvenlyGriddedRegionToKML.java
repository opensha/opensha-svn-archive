package scratchJavaDevelopers.kevin;

import java.io.FileWriter;
import java.io.IOException;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegion;

public class EvenlyGriddedRegionToKML {
	
	public EvenlyGriddedRegionToKML(EvenlyGriddedGeographicRegion region, String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		
		fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n");
		fw.write("<kml xmlns=\"http://earth.google.com/kml/2.2\">" + "\n");
		fw.write("  <Folder>" + "\n");
		fw.write("    <name>OpenSHA Gridded Region</name>" + "\n");
		fw.write("    <description>Open Seismic Hazard Analysis Evenly Gridded Region</description>" + "\n");
		
		int numLocs = region.getNumGridLocs();
		for (int i=0; i<numLocs; i++) {
			Location loc = region.getGridLocation(i);
			
			fw.write("    <Placemark>" + "\n");
			
			fw.write("      <Point id=\"Loc" + i + "\">" + "\n");
			fw.write("        <coordinates>" + loc.getLongitude() + "," + loc.getLatitude() + "," + (-loc.getDepth()) + "</coordinates>" + "\n");
//			fw.write("        <latitude>" + loc.getLatitude() + "</latitude>" + "\n");
//			fw.write("        <altitude>" + (-loc.getDepth()) + "</altitude>" + "\n");
			fw.write("      </Point>" + "\n");
			
			fw.write("    </Placemark>" + "\n");
		}
		
		fw.write("  </Folder>" + "\n");
		fw.write("</kml>" + "\n");
		fw.flush();
		fw.close();
		
		System.out.println("Wrote " + numLocs + " into " + fileName);
	}
	
	public static void main(String args[]) {
		
//		LocationList corners = new LocationList();
//		corners.addLocation(new Location(34.19, -116.60));
//		corners.addLocation(new Location(35.33, -118.75));
//		corners.addLocation(new Location(34.13, -119.63));
//		corners.addLocation(new Location(33.00, -117.50));
//		EvenlyGriddedGeographicRegion region = new EvenlyGriddedGeographicRegion(RegionSaver.createCyberShakeRegion().getRegionOutline(), 0.108);
		EvenlyGriddedGeographicRegion region = new EvenlyGriddedGeographicRegion(RegionSaver.createCyberShakeRegion().getRegionOutline(), 0.216);
		
		String fileName = "/var/www/kml/locs_20.kml";
		
		try {
			EvenlyGriddedRegionToKML grid2kml = new EvenlyGriddedRegionToKML(region, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
