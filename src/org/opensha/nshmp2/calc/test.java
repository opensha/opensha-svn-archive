package org.opensha.nshmp2.calc;

import static com.google.common.base.Charsets.US_ASCII;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.Properties;

import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

import scratch.peter.ucerf3.calc.UC3_CalcDriver;

import com.google.common.io.Files;
import com.google.common.io.Resources;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class test {

	// where is local storage for MPJ
	// how to consolidate result sets; KM's curent implementation stores
	// individual curves in lat or lon directories and then by lat or lon id
	
	// curve writer
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String pathToCurves = args[0];
//		File dir = new File(pathToCurves);
//		HazardCalcDriverMPJ.aggregateResults(dir, Period.GM0P20);
//		try {
//			InputStream is = test.class.getResourceAsStream("calc.properties");
//			Properties props = new Properties();
//			props.load(is);
//			
//			TestGrid grid = TestGrid.valueOf(props.getProperty("grid"));
//			Period period = Period.valueOf(props.getProperty("period"));
//			String name = props.getProperty("name");
//			
//			System.out.println(grid);
//			System.out.println(period);
//			System.out.println(name);
//			// set up HCM2 to process one location list/site set
//			
//			
//		} catch (IOException ioe) {
//			ioe.printStackTrace();
//		}
		
		// Eureka - orig converg runs (little salmon)
		// Eureka - mean
		// Eureka - faults only
		// Palmda - faults only
		
		
		
		try {
			String solSetPath = "/Users/pmpowers/projects/OpenSHA/tmp/invSols/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";
			String sitesPath = "/Users/pmpowers/projects/OpenSHA/tmp/curves/sites/SRPsites1.txt";
			String outPath = "/Users/pmpowers/projects/OpenSHA/tmp/SRPconvTest";
			Period[] periods = new Period[] { Period.GM0P00 };
			for (int i = 0; i < 100; i++) {
//				File out = new File("tmp/SRPconvTestHCtest.txt");
//				Files.write("", out, US_ASCII);

				UC3_CalcDriver cd = new UC3_CalcDriver(solSetPath, i,
					sitesPath, outPath, periods, false);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
