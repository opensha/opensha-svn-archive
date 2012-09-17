package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

/**
 * Wrapper for NSHMP calculation configuration.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcConfig {
	
	TestGrid grid;
	Period period;
	String name;
	String out;

	/**
	 * Creates a new config instance from the supplied {@code File}.
	 * @param propfile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public HazardCalcConfig(File propfile) throws FileNotFoundException,
			IOException {
		Properties props = new Properties();
		InputStream is = new FileInputStream(propfile);
		props.load(is);			
		
		TestGrid grid = TestGrid.valueOf(props.getProperty("grid"));
		Period period = Period.valueOf(props.getProperty("period"));
		String name = props.getProperty("name");
		String out = props.getProperty("out");
	}
}
