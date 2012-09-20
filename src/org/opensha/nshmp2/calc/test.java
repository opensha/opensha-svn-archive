package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.Properties;

import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

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
		String pathToCurves = args[0];
		File dir = new File(pathToCurves);
		HazardCalcDriverMPJ.aggregateResults(dir, Period.GM0P00);
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
	}

}
