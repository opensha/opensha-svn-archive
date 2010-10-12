package scratch.kevin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.calc.hazus.parallel.HardCodedTest;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;

public class HAZUSBasinTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Loading locs");
		LocationList locs = HardCodedTest.loadCSV(new File("/home/kevin/OpenSHA/hazus/05grid.csv"));
		CVM4BasinDepth cvm = new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0);
		
		System.out.println("Getting vals");
		ArrayList<Double> vals = cvm.getValues(locs);
		int cnt = 0;
		for (int i=0; i<locs.size(); i++) {
			Location loc = locs.get(i);
			double val = vals.get(i);
			if (val > DepthTo2pt5kmPerSecParam.MAX) {
				cnt++;
				System.out.println(loc.getLatitude() + ", " + loc.getLongitude() + ": " + val);
			}
		}
		System.out.println("Num above: " + cnt);
	}

}
