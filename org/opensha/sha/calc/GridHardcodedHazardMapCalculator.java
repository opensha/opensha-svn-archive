package org.opensha.sha.calc;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.data.region.EvenlyGriddedSoCalRegion;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.ParameterException;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.nshmp.sha.gui.infoTools.GraphWindow;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
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
public class GridHardcodedHazardMapCalculator implements ParameterChangeWarningListener, WindowListener {

	boolean xLogFlag = true;
	private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
	boolean timer = true;
	boolean showResult = true;
	boolean windowOpened = false;
	boolean loadERFFromFile = false;
	ArrayList<Long> curveTimes = new ArrayList<Long>();
	
	SitesInGriddedRegionAPI sites;
	int startIndex;
	int endIndex;
	boolean debug;
	// location to store output files if debugging
	static final String DEBUG_RESULT_FOLDER = "/home/kevin/OpenSHA/condor/test_results/";

	/**
	 * Sets variables for calculation of hazard curves in hazard map
	 * @param sites - sites in gridded region
	 * @param startIndex - index  to start at within sites
	 * @param endIndex - index to end at (the very last one is NOT computed)
	 * @param debug - flag to enable debugging mode. if true, the timer and graph window will be enabled
	 * 		if hard coded in.
	 */
	public GridHardcodedHazardMapCalculator(SitesInGriddedRegionAPI sites, int startIndex, int endIndex, boolean debug) {
		this.sites = sites;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.debug = debug;
		
		// show timing results if debug mode and timer is selected
		timer = timer & debug;
		// open graph window for first 10 plots if debug mode and showResult is selected
		showResult = showResult & debug;
	}
	
	public void calculateCurves() {
		String overheadStr = "";
		long overhead = 0;
		long start = 0;
		if (timer) {
			start = System.currentTimeMillis();
		}
		int numSites = 0;
		try {
			// max cutoff distance for calculator
			double maxDistance =  200.0;
			
			// create IMR
			AttenuationRelationshipAPI imr = new CB_2008_AttenRel(this);
			// set the Intensity Measure Type
			imr.setIntensityMeasure(AttenuationRelationship.PGA_NAME);
			// set default parameters
			imr.setParamDefaults();
			
			// add parameters to sites from IMR
			sites.addSiteParams(imr.getSiteParamsIterator());

			// create the ERF
			System.out.println("Creating Forecast");
			EqkRupForecastAPI erf = null;
			if (loadERFFromFile) { // load the ERF from a file with a pre-updated forecast for less overhead
				long start_erf = 0;
				if (timer) {
					start_erf = System.currentTimeMillis();
				}
				erf = (EqkRupForecastAPI)FileUtils.loadObject("erf.obj");
				if (timer) {
					System.out.println("Took " + getTime(start_erf) + " seconds to load ERF.");
				}
			} else { // create a new forecast, but you have to update the forecast
				//erf = new Frankel96_AdjustableEqkRupForecast();
				//erf = new UCERF2();
				erf = new MeanUCERF2();
				System.out.println("Updating Forecast");
				long start_erf = 0;
				if (timer) {
					start_erf = System.currentTimeMillis();
				}
				
				// update the forecast
				erf.updateForecast();
				if (timer) {
					System.out.println("Took " + getTime(start_erf) + " seconds to update forecast.");
				}
			}
			

			// create the calculator object used for every curve
			HazardCurveCalculator calc = new HazardCurveCalculator();
			// set maximum source distance
			calc.setMaxSourceDistance(maxDistance);

			System.out.println("Setting up Hazard Function");
			IMT_Info imtInfo = new IMT_Info();
			// get the default function for the specified IMT
			ArbitrarilyDiscretizedFunc hazFunction = imtInfo.getDefaultHazardCurve(AttenuationRelationship.PGA_NAME);
			
			// total number of sites for the entire map
		    numSites = sites.getNumGridLocs();
		    // number of points on the hazard curve
			int numPoints = hazFunction.getNum();
			
			Site site;
			
			long start_curve = 0;
			if (timer) {
				overhead = System.currentTimeMillis() - start;
				overheadStr = getTime(start);
				System.out.println(overheadStr + " seconds total overhead before calculation");
				start_curve = System.currentTimeMillis();
			}
			
			System.out.println("Starting Curve Calculations");
			
			// loop through each site in this job's portion of the map
			for(int j = startIndex; j < numSites && j < endIndex; ++j){
				if (timer && j != startIndex) {
					curveTimes.add(System.currentTimeMillis() - start_curve);
					System.out.println("Took " + getTime(start_curve) + " seconds to calculate curve.");
					start_curve = System.currentTimeMillis();
				}
				
				System.out.println("Doing site " + (j - startIndex + 1) + " of " + (endIndex - startIndex) + " (index: " + j + " of " + numSites + " total)");
				try {
					// get the site at the given index. it should already have all parameters set
					site = sites.getSite(j);
				} catch (RegionConstraintException e) {
					System.out.println("No More Sites!");
					break;
				}

				// take the log of the hazard function and to send to the calculator
				ArbitrarilyDiscretizedFunc logHazFunction = getLogFunction(hazFunction);

				System.out.println("Calculating Hazard Curve");
				// actually calculate the curve from the log hazard function, site, IMR, and ERF
				calc.getHazardCurve(logHazFunction,site,imr,erf);
				System.out.println("Calculated a curve!");
				
				// get the location from the site for output file naming
				String lat = decimalFormat.format(site.getLocation().getLatitude());
				String lon = decimalFormat.format(site.getLocation().getLongitude());
				// convert the hazard function back from log values
				hazFunction = unLogFunction(hazFunction, logHazFunction);

				// write the result to the file
				System.out.println("Writing Results to File");
				String prefix = "";
				if (debug)
					prefix = GridHardcodedHazardMapCalculator.DEBUG_RESULT_FOLDER;
				FileWriter fr = new FileWriter(prefix + lat + "_" + lon + ".txt");
				for (int i = 0; i < numPoints; ++i)
					fr.write(hazFunction.getX(i) + " " + hazFunction.getY(i) + "\n");
				fr.close();

				// show the plot graphically for verification
				if (showResult && (j - startIndex) < 10) {
					ArrayList data = new ArrayList<ArbitrarilyDiscretizedFunc>();
					data.add(hazFunction);
					GraphWindow graph = new GraphWindow(data);
					graph.setVisible(true);
					graph.addWindowListener(this);
					windowOpened = true;
				}
			}
			if (timer) {
				curveTimes.add(System.currentTimeMillis() - start_curve);
				System.out.println("Took " + getTime(start_curve) + " seconds to calculate curve.");
				start_curve = System.currentTimeMillis();
			}

		} catch (ParameterException e) {
			// something bad happened, exit with code 1
			e.printStackTrace();
			System.exit(1);
		} catch (RemoteException e) {
			// something bad happened, exit with code 1
			e.printStackTrace();
			System.exit(1);
		} catch (FileNotFoundException e) {
			// something bad happened, exit with code 1
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// something bad happened, exit with code 1
			e.printStackTrace();
			System.exit(1);
		}
		
		if (timer) {
			long total = 0;
			for (Long time:curveTimes)
				total += time;
			double average = (double)total / (double)curveTimes.size();
			System.out.println("Average curve time: " + new DecimalFormat(	"###.##").format(average / 1000d));
			System.out.println("Total overhead: " + overheadStr);
			System.out.println("Total calculation time: " + getTime(start));
			System.out.println();
			
			// calculate an estimate
			int curvesPerJob = 100;
			double estimate = average * (double)numSites + (double)overhead * (numSites / curvesPerJob);
			String estimateHoursStr = new DecimalFormat(	"###.##").format(estimate / 3600000d);
			String estimateSecondsStr = new DecimalFormat(	"###.##").format(estimate / 1000d);
			System.out.println("Estimated Total CPU Time (current region, " + numSites + " sites): " + estimateHoursStr + " hours (" + estimateSecondsStr + " seconds)");
			
			numSites = 180000;
			estimate = average * (double)numSites + (double)overhead * (numSites / curvesPerJob);
			estimateHoursStr = new DecimalFormat(	"###.##").format(estimate / 3600000d);
			estimateSecondsStr = new DecimalFormat(	"###.##").format(estimate / 1000d);
			System.out.println("Estimated Total CPU Time (estimated region, " + numSites + " sites): " + estimateHoursStr + " hours (" + estimateSecondsStr + " seconds)");
		}
		System.out.println("***DONE***");
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

	/**
	 * Takes the log of the X-values of the given function
	 * @param arb
	 * @return A function with points (Log(x), 1)
	 */
	private ArbitrarilyDiscretizedFunc getLogFunction(DiscretizedFuncAPI arb) {
		ArbitrarilyDiscretizedFunc new_func = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < arb.getNum(); ++i)
				new_func.set(Math.log(arb.getX(i)), 1);
			return new_func;
		}
		else
			throw new RuntimeException("Unsupported IMT");
	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {}

	/**
	 *  Un-log the function, keeping the y values from the log function, but matching
	 *  them with the x values of the original (not log) function
	 * @param oldHazFunc - original hazard function
	 * @param logHazFunction - calculated hazard curve with log x values
	 * @return
	 */
	private ArbitrarilyDiscretizedFunc unLogFunction(
			ArbitrarilyDiscretizedFunc oldHazFunc, ArbitrarilyDiscretizedFunc logHazFunction) {
		int numPoints = oldHazFunc.getNum();
		ArbitrarilyDiscretizedFunc hazFunc = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < numPoints; ++i) {
				hazFunc.set(oldHazFunc.getX(i), logHazFunction.getY(i));
			}
			return hazFunc;
		}
		else
			throw new RuntimeException("Unsupported IMT");
	}
	
	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}

	public void windowClosing(WindowEvent e) {}

	public void windowDeactivated(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	public void windowOpened(WindowEvent e) {}
	
	public boolean isWindowOpened() {
		return windowOpened;
	}
	
	/**
	 * Main class to calculate hazard curves. If there are less than 2 arguments, it is considered to be
	 * a test and timing messages will be displayed along with a graph window for the first 10 curves.
	 * Otherwise, the first argument is the start index for the site and the sedond argument is the end
	 * index.
	 * @param args: startIndex endIndex
	 */
	public static void main(String[] args) {
		// create site object
		SitesInGriddedRegionAPI sites = new SitesInGriddedRegion(new EvenlyGriddedRELM_Region().getGridLocationsList(), 1);
		
		if (args.length >= 2) { // this is from the command line and is real
			// get start and end index of sites to do within region from command line
			int startIndex = Integer.parseInt(args[0]);
			int endIndex = Integer.parseInt(args[1]);
			try {
				// run the calculator with debugging disabled
				GridHardcodedHazardMapCalculator calc = new GridHardcodedHazardMapCalculator(sites, startIndex, endIndex, false);
				calc.calculateCurves();
			} catch (RuntimeException e) {
				// something bad happened, exit with code 1
				e.printStackTrace();
				System.exit(1);
			}
			// exit without error
			System.exit(0);
		} else { // this is just a test
			// hard coded indices
			int startIndex = 0;
			int endIndex = 3000;
			System.out.println("Doing sites " + startIndex + " to " + endIndex + " of " + sites.getNumGridLocs());
			try {
				//sites = new SitesInGriddedRectangularRegion(34.0, 35.0, -118.0, -117.0, .5);
				// run the calculator with debugging enabled
				GridHardcodedHazardMapCalculator calc = new GridHardcodedHazardMapCalculator(sites, startIndex, endIndex, true);
				calc.showResult = false;
				calc.calculateCurves();
				// if nothing was calculated, just exit
				if (!calc.isWindowOpened())
					System.exit(0);
			} catch (Exception e) {
				// something bad happened, exit with code 1
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
