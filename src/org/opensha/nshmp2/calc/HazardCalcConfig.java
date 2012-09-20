package org.opensha.nshmp2.calc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;

import com.google.common.io.Resources;

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
	boolean mpj;

	/**
	 * Creates a new config instance from the supplied {@code File}.
	 * @param propfile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public HazardCalcConfig(URL propfile) throws FileNotFoundException,
			IOException {
		Properties props = new Properties();
		InputStream is = propfile.openStream();
		props.load(is);		
		is.close();
		
		grid = TestGrid.valueOf(props.getProperty("grid"));
		period = Period.valueOf(props.getProperty("period"));
		name = props.getProperty("name");
		out = props.getProperty("out");
		mpj = Boolean.valueOf(props.getProperty("mpj"));
	}
}
