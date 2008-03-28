package org.opensha.sha.calc;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.opensha.data.Site;
import org.opensha.data.TimeSpan;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.EvenlyGriddedRELM_Region;
import org.opensha.data.region.EvenlyGriddedRELM_TestingRegion;
import org.opensha.data.region.EvenlyGriddedSoCalRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RELM_TestingRegion;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.region.SitesInGriddedRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.exceptions.ParameterException;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.nshmp.sha.gui.infoTools.GraphWindow;
import org.opensha.param.Parameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.util.SiteTranslator;
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
public class GridHazardMapPortionCalculator {

	boolean xLogFlag = true;
	private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
	boolean timer = true;
	boolean lessPrints = false;
	ArrayList<Long> curveTimes = new ArrayList<Long>();
	boolean skipPoints = false;
	int skipFactor = 10;
	
	SitesInGriddedRegionAPI sites;
	
	boolean useCVM = false;
	String cvmFileName = "";
	
	EqkRupForecastAPI erf;
	AttenuationRelationshipAPI imr;
	double maxDistance;
	String outputDir;

	/**
	 * Sets variables for calculation of hazard curves in hazard map
	 * @param sites - sites in gridded region for calculation
	 * @param erf - Earthquake Rupture Forecast (should already be updated) for calculation
	 * @param imr - Attenuation Relationship for calculation
	 * @param imt - Intensity Measure Type
	 * @param maxDistance - maximum source distance for calculation
	 * @param outputDir - directory to store results (or empty string for current working directory)
	 */
	public GridHazardMapPortionCalculator(SitesInGriddedRegionAPI sites, EqkRupForecastAPI erf, AttenuationRelationshipAPI imr, double maxDistance, String outputDir) {
		this.sites = sites;
		
		this.erf = erf;
		this.imr = imr;
		this.maxDistance = maxDistance;
		this.outputDir = outputDir;
		
		// show timing results if debug mode and timer is selected
		timer = true;
	}
	
	/**
	 * Calculate the curves from the given start index to end index - 1.
	 * 
	 * @param startIndex - index  to start at within sites
	 * @param endIndex - index to end at (the very last one is NOT computed)
	 */
	public void calculateCurves(int startIndex, int endIndex) {
		String overheadStr = "";
		long overhead = 0;
		long start = 0;
		if (timer) {
			start = System.currentTimeMillis();
			System.out.println("Start Time Stamp: " + start);
		}
		int numSites = 0;
		try {
			// add parameters to sites from IMR
			sites.addSiteParams(imr.getSiteParamsIterator());
			//sites.setSiteParamsForRegionFromServlet(true);
			
			System.out.println("Selected ERF: " + erf.getName());
			System.out.println("ERF Params:");
			Iterator erfIt = erf.getAdjustableParamsIterator();
			while (erfIt.hasNext()) {
				ParameterAPI param = (ParameterAPI)erfIt.next();
				System.out.println(param.getName() + ": " + param.getValue());
			}
			try {
				System.out.println("Time Span: " + erf.getTimeSpan().getDuration() + " " + erf.getTimeSpan().getDurationUnits() + " from " + erf.getTimeSpan().getStartTimeYear());
			} catch (RuntimeException e1) {
			}
			System.out.println("Selected IMR: " + imr.getName());
			System.out.println("IMT: " + imr.getIntensityMeasure().getName());

			// create the calculator object used for every curve
			HazardCurveCalculator calc = new HazardCurveCalculator();
			// set maximum source distance
			calc.setMaxSourceDistance(maxDistance);

			System.out.println("Setting up Hazard Function");
			IMT_Info imtInfo = new IMT_Info();
			// get the default function for the specified IMT
			ArbitrarilyDiscretizedFunc hazFunction = imtInfo.getDefaultHazardCurve(imr.getIntensityMeasure().getName());
			
			// total number of sites for the entire map
		    numSites = sites.getNumGridLocs();
		    // number of points on the hazard curve
			int numPoints = hazFunction.getNum();
			
			Site site;
			
			long start_curve = 0;
			if (timer) {
				overhead = System.currentTimeMillis() - start;
				overheadStr = getTime(start);
				System.out.println(overheadStr + " seconds total calculator overhead");
				start_curve = System.currentTimeMillis();
			}
			
			// use the CVM
			ArrayList<String> cvmStr = null;
			SiteTranslator siteTranslator = new SiteTranslator();
			ArrayList<ParameterAPI> defaultSiteParams = null;
			if (useCVM) {
				System.out.println("Loading CVM from " + cvmFileName);
				cvmStr = FileUtils.loadFile(cvmFileName);
				
				Iterator it = imr.getSiteParamsIterator();
				
				defaultSiteParams = new ArrayList<ParameterAPI>();
				while (it.hasNext()) {
					ParameterAPI param = (ParameterAPI)it.next();
					defaultSiteParams.add((ParameterAPI)param.clone());
				}
			}
			
			System.out.println("Starting Curve Calculations");
			
			// loop through each site in this job's portion of the map
			int j = 0;
			
			for(j = startIndex; j < numSites && j < endIndex; ++j){
				// if we're skipping some of them, then check if this should be skipped
				if (skipPoints && j % skipFactor != 0)
					continue;
				boolean print = true;
				if (lessPrints && j % 100 != 0)
					print = false;
				if (print && timer && j != startIndex) {
					curveTimes.add(System.currentTimeMillis() - start_curve);
					System.out.println("Took " + getTime(start_curve) + " seconds to calculate curve.");
					start_curve = System.currentTimeMillis();
				}
				
				if (print)
					System.out.println("Doing site " + (j - startIndex + 1) + " of " + (endIndex - startIndex) + " (index: " + j + " of " + numSites + " total) ");
				try {
					// get the site at the given index. it should already have all parameters set.
					// it will read the sites along latitude lines, starting with the southernmost
					// latitude in the region, and going west to east along that latitude line.
					site = sites.getSite(j);
					if (useCVM) {
						if ((j - startIndex) >= cvmStr.size()) {
							System.err.println("WARNING: CVM index out of bounds! (index: " + j + ")");
							System.err.println("Location: " + site.getLocation().getLatitude() + ", " + site.getLocation().getLongitude());
						} else {
							String cvm = cvmStr.get(j - startIndex);
							StringTokenizer tok = new StringTokenizer(cvm);
							double lat = Double.parseDouble(tok.nextToken());
							double lon = Double.parseDouble(tok.nextToken());
							String type = tok.nextToken();
							double depth = Double.parseDouble(tok.nextToken());

							if (Math.abs(lat - site.getLocation().getLatitude()) >= sites.getGridSpacing()) {
								if (Math.abs(lon - site.getLocation().getLongitude()) >= sites.getGridSpacing()) {
									System.err.println("WARNING: CVM data is for the WRONG LOCATION! (index: " + j + ")");
									System.err.println("CVM Location: " + lat + ", " + lon + " REAL Location: " + site.getLocation().getLatitude() + ", " + site.getLocation().getLongitude());
								}
							}

							Iterator it = site.getParametersIterator();
							while(it.hasNext()){
								ParameterAPI tempParam = (ParameterAPI)it.next();

								//Setting the value of each site Parameter from the CVM and translating them into the Attenuation related site
								boolean flag = siteTranslator.setParameterValue(tempParam,type,depth);
								if (!flag) {
									for (ParameterAPI param : defaultSiteParams) {
										if (tempParam.getName().equals(param.getName())) {
											tempParam.setValue(param.getValue());
										}
									}
								}
							}
						}
					}
				} catch (RegionConstraintException e) {
					System.out.println("No More Sites!");
					break;
				}
				Iterator it = site.getParametersIterator();
				System.out.println("Site Parameters:");
				while (it.hasNext()) {
					ParameterAPI param = (ParameterAPI)it.next();
					System.out.println(param.getName() + ": " + param.getValue());
				}

				// take the log of the hazard function and to send to the calculator
				ArbitrarilyDiscretizedFunc logHazFunction = getLogFunction(hazFunction);

				if (print)
					System.out.println("Calculating Hazard Curve");
				// actually calculate the curve from the log hazard function, site, IMR, and ERF
				calc.getHazardCurve(logHazFunction,site,imr,erf);
				if (print)
					System.out.println("Calculated a curve!");
				
				// get the location from the site for output file naming
				String lat = decimalFormat.format(site.getLocation().getLatitude());
				String lon = decimalFormat.format(site.getLocation().getLongitude());
				// convert the hazard function back from log values
				hazFunction = unLogFunction(hazFunction, logHazFunction);
				
				// see if it's empty\
				if (hazFunction.getY(0) == 0 && hazFunction.getY(3) == 0) {
					System.err.println("WARNING: Empty hazard curve!");
					System.err.println("Site index: " + j);
					System.err.println("Site Location: " + site.getLocation().getLatitude() + " " + site.getLocation().getLongitude());
					Iterator it2 = site.getParametersIterator();
					System.err.println("Site Parameters:");
					while (it2.hasNext()) {
						ParameterAPI param = (ParameterAPI)it2.next();
						System.err.println(param.getName() + ": " + param.getValue());
					}
//					System.err.println("SKIPPING!!!");
//					continue;
				}

				// write the result to the file
				String prefix = "";
				String jobDir = lat + "/";
				prefix += outputDir;
				prefix += jobDir;
				File dir = new File(prefix);
				if (!dir.exists()) {
					dir.mkdir();
//					chmod(dir.getAbsolutePath());
				}
				String outFileName = prefix + lat + "_" + lon + ".txt";
				if (print)
					System.out.println("Writing Results to File: " + outFileName);
				File outFile = new File(outFileName);
				FileWriter fr = new FileWriter(outFile);
				for (int i = 0; i < numPoints; ++i)
					fr.write(hazFunction.getX(i) + " " + hazFunction.getY(i) + "\n");
				fr.close();
//				chmod(outFile.getAbsolutePath());
			}
			if ((lessPrints && j % 100 != 0 || !lessPrints) && timer) {
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
		System.out.println("End Time Stamp: " + System.currentTimeMillis());
	}
	
	public void chmod(String name) {
		System.out.println("CMODDING: " + name);
		String[] cmd = {"/bin/sh", " /bin/chmod ", " o+rw ", name};
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		try {
			String line;
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			System.out.println("MODDED!");
			BufferedReader input =
				new BufferedReader
				(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.err.println(line);
			}
			input.close();
		}
		catch (Exception err) {
			err.printStackTrace();
		}

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
}
