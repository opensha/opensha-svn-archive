package org.opensha.nshmp2.calc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.opensha.nshmp2.tmp.TestGrid;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.SourceIMR;

/**
 * Wrapper for NSHMP calculation configuration.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class HazardCalcConfig {
	
	String name;
	TestGrid grid;
	double spacing;
	Period period;
	ERF_ID erfID;
	SourceIMR imr;
	boolean epiUnc;
	String outDir;
	boolean singleFile;

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
		
		name = props.getProperty("name");
		grid = TestGrid.valueOf(props.getProperty("grid"));
		spacing = Double.parseDouble(props.getProperty("spacing"));
		period = Period.valueOf(props.getProperty("period"));
		erfID = ERF_ID.valueOf(props.getProperty("erfID"));
		imr = SourceIMR.valueOf(props.getProperty("imr"));
		epiUnc = Boolean.valueOf(props.getProperty("epiUnc"));
		outDir = props.getProperty("outDir");
		singleFile = Boolean.valueOf(props.getProperty("singleFile"));
	}
}
