package org.opensha.sha.calc;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RELM_TestingRegion;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel;
import org.opensha.util.FileUtils;


/**
 * GridHardcodedHazardMapCalculator
 * 
 * Class to calculate a set of hazard curves from a region as part of a grid hazard map
 * computation. All values except for the start and end indices should be hard coded into
 * this class before distributing to compute nodes.
 * @author kevin
 *
 */
public class GridMetadataHazardMapCalculator implements ParameterChangeWarningListener {

	boolean timer = true;
	boolean loadERFFromFile = false;
	boolean lessPrints = false;
	boolean skipPoints = false;
	int skipFactor = 10;

	int startIndex;
	int endIndex;
	boolean debug;
	// location to store output files if debugging
	static final String DEBUG_RESULT_FOLDER = "/home/kevin/OpenSHA/condor/test_results/";

	boolean useCVM = false;
	String cvmFileName = "";

	String outputDir = "";

	/**
	 * Sets variables for calculation of hazard curves in hazard map
	 * @param sites - sites in gridded region
	 * @param startIndex - index  to start at within sites
	 * @param endIndex - index to end at (the very last one is NOT computed)
	 * @param debug - flag to enable debugging mode. if true, the timer and graph window will be enabled
	 * 		if hard coded in.
	 */
	public GridMetadataHazardMapCalculator(int startIndex, int endIndex, boolean debug) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.debug = debug;

		// show timing results if debug mode and timer is selected
		timer = timer & debug;
	}

	public void calculateCurves(String metadataFileName) throws MalformedURLException, DocumentException, InvocationTargetException {
		File metadataFile = new File(metadataFileName);
		System.out.println("Loading metadata from " + metadataFile.getAbsolutePath());
		if (!metadataFile.exists())
			throw new RuntimeException("Metadata file doesn't exists!");
		
		long start = 0;
		if (timer) {
			start = System.currentTimeMillis();
		}

		SAXReader reader = new SAXReader();
		Document document = reader.read(metadataFile);
		Element root = document.getRootElement();

		// load the ERF
		long start_erf = 0;
		System.out.println("Creating Forecast");
		if (timer) {
			start_erf = System.currentTimeMillis();
		}
		Element erfElement = root.element(EqkRupForecast.XML_METADATA_NAME);
		Attribute className = erfElement.attribute("className");
		EqkRupForecast erf;
		if (className == null) { // load it from a file
			String erfFileName = erfElement.attribute("fileName").getValue();
			erf = (EqkRupForecast)FileUtils.loadObject(erfFileName);
		} else {
			erf = EqkRupForecast.fromXMLMetadata(erfElement);
			System.out.println("Updating Forecast");
			erf.updateForecast();
		}
		if (timer) {
			System.out.println("Took " + getTime(start_erf) + " seconds to load ERF.");
		}
		
		Element regionElement = root.element(EvenlyGriddedGeographicRegion.XML_METADATA_NAME);
		EvenlyGriddedGeographicRegion region = EvenlyGriddedGeographicRegion.fromXMLMetadata(regionElement);
		SitesInGriddedRegionAPI sites = new SitesInGriddedRegion(region.getRegionOutline(), region.getGridSpacing());

		// max cutoff distance for calculator
		Element calcParams = root.element("calculationParameters");
		double maxDistance =  Double.parseDouble(calcParams.attribute("maxSourceDistance").getValue());

		// intensity measure type
		String imt = calcParams.attribute("intesityMeasureType").getValue();

		// load IMR
		//AttenuationRelationshipAPI imr = new CB_2008_AttenRel(this);
		AttenuationRelationship imr = (AttenuationRelationship)AttenuationRelationship.fromXMLMetadata(root.element("IMR"), this);

		GridHazardMapPortionCalculator calculator = new GridHazardMapPortionCalculator(sites, erf, imr, maxDistance, outputDir);

		calculator.timer = this.timer;
		calculator.lessPrints = this.lessPrints;
		calculator.skipPoints = this.skipPoints;
		calculator.skipFactor = this.skipFactor;
		calculator.useCVM = this.useCVM;
		calculator.cvmFileName = this.cvmFileName;

		if (timer) {
			System.out.println(getTime(start) + " seconds total pre-calculator overhead");
		}

		calculator.calculateCurves(startIndex, endIndex);
	}

	/**
	 * Calculate and format the time from 'before'
	 * @param before - currentTimeMillis that should be counted from
	 * @return string in seconds of elapsed time
	 */
	public String getTime(long before) {
		double time = ((double)System.currentTimeMillis() - (double)before)/1000d; 
		return new DecimalFormat(	"###.##").format(time);
	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {}

	/**
	 * Main class to calculate hazard curves. If there are less than 2 arguments, it is considered to be
	 * a test and timing messages will be displayed along with a graph window for the first 10 curves.
	 * Otherwise, the first argument is the start index for the site and the sedond argument is the end
	 * index.
	 * @param args: startIndex endIndex metadataFileName (cvmFileName)
	 */
	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		if (args.length < 3) { // this is a debug run
			System.err.println("RUNNING FROM DEBUG MODE!");
			args = new String[3];
			args[0] = 0 + "";
			args[1] = 5 + "";
			args[2] = "output.xml";
		}
		// get start and end index of sites to do within region from command line
		int startIndex = Integer.parseInt(args[0]);
		int endIndex = Integer.parseInt(args[1]);
		try {
			// run the calculator with debugging disabled
			GridMetadataHazardMapCalculator calc = new GridMetadataHazardMapCalculator(startIndex, endIndex, false);
			String metadataFileName = args[2];
			if (args.length >=4 && args[3].toLowerCase().contains("cvm")) {
				calc.useCVM = true;
				calc.cvmFileName = args[3];
			}
			calc.skipPoints = false;
			calc.timer = true;
			calc.calculateCurves(metadataFileName);
			System.out.println("Total execution time: " + calc.getTime(start));
		} catch (Exception e) {
			// something bad happened, exit with code 1
			e.printStackTrace();
			System.exit(1);
		}
		// exit without error
		System.exit(0);
	}

}
