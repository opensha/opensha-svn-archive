package org.opensha.sha.calc.IM_EventSet.v03;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.PropagationEffect;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceJBParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;

public abstract class IM_EventSetCalc_v3_0 {
	
	public static final DecimalFormat meanSigmaFormat = new DecimalFormat("0.####");
	public static final DecimalFormat distFormat = new DecimalFormat("0.###");
	
	public static final float MIN_SOURCE_DIST = 200;
	
	private float sourceCutOffDistance = 0;
	private Site siteForSourceCutOff = null;

	private SiteTranslator siteTrans = new SiteTranslator();

	/**
	 * This should ONLY be accessed through the getter method as it may
	 * be uninitialized
	 */
	private ArrayList<Site> sites = null;

	private ArrayList<ArrayList<SiteDataValue<?>>> sitesData = null;

	public IM_EventSetCalc_v3_0() {

	}

	/*			ABSTRACT METHODS			*/

	/**
	 * Returns the number of sites for the calculation
	 * 
	 * @return
	 */
	public abstract int getNumSites();

	/**
	 * Returns the Location of the ith site.
	 * 
	 * @param i
	 * @return
	 */
	public abstract Location getSiteLocation(int i);

	/**
	 * Returns the ordered site data provider list, or null to not use site data providers
	 * 
	 * @return
	 */
	public abstract OrderedSiteDataProviderList getSiteDataProviders();

	/**
	 * Returns the user specified (in the input file) site data values for the site
	 * or null to try to use site data providers
	 * 
	 * @param i
	 * @return
	 */
	public abstract ArrayList<SiteDataValue<?>> getUserSiteDataValues(int i);

	/**
	 * Returns the output directory for all results
	 * 
	 * @return
	 */
	public abstract File getOutputDir();
	
	private float getSourceCutOffDistance() {
		if (sourceCutOffDistance == 0) {
			createSiteList();
		}
		return sourceCutOffDistance;
	}
	
	private Site getSiteForSourceCutOff() {
		if (siteForSourceCutOff == null) {
			createSiteList();
		}
		return siteForSourceCutOff;
	}
	
	/**
	 * This method finds the location at the middle of the region encompassing all of
	 * the sites and gets a cutoff distance such that all ruptures within 200 km of any
	 * site are included in the output.
	 */
	protected void createSiteList() {
		//gets the min lat, lon and max lat, lon from given set of locations.
		double minLon = Double.MAX_VALUE;
		double maxLon = Double.NEGATIVE_INFINITY;
		double minLat = Double.MAX_VALUE;
		double maxLat = Double.NEGATIVE_INFINITY;
		int numSites = getNumSites();
		for (int i = 0; i < numSites; ++i) {

			Location loc = getSiteLocation(i);
			double lon = loc.getLongitude();
			double lat = loc.getLatitude();
			if (lon > maxLon)
				maxLon = lon;
			if (lon < minLon)
				minLon = lon;
			if (lat > maxLat)
				maxLat = lat;
			if (lat < minLat)
				minLat = lat;
		}
		double middleLon = (minLon + maxLon) / 2;
		double middleLat = (minLat + maxLat) / 2;

		//getting the source-site cuttoff distance
		sourceCutOffDistance = (float)RelativeLocation.getHorzDistance(middleLat, middleLon,
				minLat, minLon) + MIN_SOURCE_DIST;
		siteForSourceCutOff = new Site(new Location(middleLat, middleLon));

		return;
	}
	
	/**
	 * This method checks if the source is within 200 KM of any site
	 * 
	 * @param source
	 * @return
	 */
	private boolean shouldIncludeSource(ProbEqkSource source) {
		float sourceCutOffDistance = getSourceCutOffDistance();
		Site siteForSourceCutOff = getSiteForSourceCutOff();
		
		double sourceDistFromSite = source.getMinDistance(siteForSourceCutOff);
		if (sourceDistFromSite > sourceCutOffDistance)
			return false;
		return true;
	}

	private ArrayList<Site> getSites() {
		if (sites == null) {
			sites = new ArrayList<Site>();
			for (int i=0; i<getNumSites(); i++) {
				Site site = new Site(getSiteLocation(i));
				sites.add(site);
			}
		}
		return sites;
	}

	/**
	 * This initializes the site data values for each site.
	 * 
	 * If there is user specified data for the specific site, that is given top
	 * priority. If there are also site data providers available, those will
	 * be used (but given lower priority than any user values).
	 * 
	 * @return
	 */
	private ArrayList<ArrayList<SiteDataValue<?>>> getSitesData() {
		if (sitesData == null) {
			sitesData = new ArrayList<ArrayList<SiteDataValue<?>>>();
			ArrayList<Site> sites = getSites();
			OrderedSiteDataProviderList providers = getSiteDataProviders();
			for (int i=0; i<sites.size(); i++) {
				Site site = sites.get(i);
				ArrayList<SiteDataValue<?>> dataVals = getUserSiteDataValues(i);
				if (dataVals == null) {
					dataVals = new ArrayList<SiteDataValue<?>>();
				}
				if (providers != null) {
					ArrayList<SiteDataValue<?>> provData = providers.getAllAvailableData(site.getLocation());
					if (provData != null)
						dataVals.addAll(provData);
				}
				sitesData.add(dataVals);
			}
		}

		return sitesData;
	}

	/**
	 * Gets all of the default site params from the attenuation relationship
	 * 
	 * @param attenRel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<ParameterAPI> getDefaultSiteParams(AttenuationRelationship attenRel) {
		ListIterator<ParameterAPI> siteParamsIt = attenRel.getSiteParamsIterator();
		ArrayList<ParameterAPI> defaultSiteParams = new ArrayList<ParameterAPI>();

		while (siteParamsIt.hasNext()) {
			defaultSiteParams.add((ParameterAPI) siteParamsIt.next().clone());
		}

		return defaultSiteParams;
	}

	/**
	 * Sets the site params for the given Attenuation Relationship to the value in the given params.
	 * @param attenRel
	 * @param defaultSiteParams
	 */
	@SuppressWarnings("unchecked")
	private void setSiteParams(AttenuationRelationship attenRel, ArrayList<ParameterAPI> defaultSiteParams) {
		for (ParameterAPI param : defaultSiteParams) {
			ParameterAPI attenParam = attenRel.getParameter(param.getName());
			attenParam.setValue(param.getValue());
		}
	}

	/**
	 * This goes through each site and makes sure that it has a parameter for each site
	 * param from the Attenuation Relationship. It then tries to set that parameter from
	 * its own data values, and if it can't, uses the attenuation relationship's default.
	 * 
	 * @param attenRel
	 * @return
	 */
	private ArrayList<Site> getInitializedSites(AttenuationRelationship attenRel) {
		// get the list of sites
		ArrayList<Site> sites = getSites();
		ArrayList<ArrayList<SiteDataValue<?>>> sitesData = getSitesData();

		// we need to make sure that the site has parameters for this atten rel
		ListIterator<ParameterAPI> siteParamsIt = attenRel.getSiteParamsIterator();
		while (siteParamsIt.hasNext()) {
			ParameterAPI attenParam = siteParamsIt.next();
			for (int i=0; i<sites.size(); i++) {
				Site site = sites.get(i);
				ArrayList<SiteDataValue<?>> siteData = sitesData.get(i);
				ParameterAPI siteParam;
				if (site.containsParameter(attenParam.getName())) {
					siteParam = site.getParameter(attenParam.getName());
				} else {
					siteParam = (ParameterAPI)attenParam.clone();
					site.addParameter(siteParam);
				}
				// now try to set this parameter from the site data
				boolean success = siteTrans.setParameterValue(siteParam, siteData);
				// if we couldn't set it from our data, use the atten rel's default
				if (!success)
					siteParam.setValue(attenParam.getValue());
			}
		}
//		for (int i=0; i<sites.size(); i++) {
//			Site site = sites.get(i);
//			ArrayList<SiteDataValue<?>> siteData = sitesData.get(i);
//			printSiteParams(site, siteData);
//		}
		return sites;
	}
	
	private void printSiteParams(Site site, ArrayList<SiteDataValue<?>> siteData) {
		System.out.println("*** Data ***");
		if (siteData != null) {
			for (SiteDataValue<?> val : siteData) {
				System.out.println(val.getDataType() + ": " + val.getValue());
			}
		}
		System.out.println("*** Params ***");
		ListIterator<ParameterAPI> it = site.getParametersIterator();
		while (it.hasNext()) {
			ParameterAPI param = it.next();
			System.out.println(param.getName() + ": " + param.getValue());
		}
	}
	
	/**
	 * Sets the IMT from the string specification
	 * 
	 * @param imtLine
	 * @param attenRel
	 */
	public static void setIMTFromString(String imtStr, AttenuationRelationship attenRel) {
		String imt = imtStr.trim();
		if ((imt.startsWith("SA") || imt.startsWith("SD"))) {
			// this is SA/SD
			double period = Double.parseDouble(imt.substring(3));
			attenRel.setIntensityMeasure(imt.substring(0, 2));
			DependentParameterAPI imtParam = (DependentParameterAPI)attenRel.getIntensityMeasure();
			imtParam.getIndependentParameter(PeriodParam.NAME).setValue(period);
		} else {
			attenRel.setIntensityMeasure(imt);
		}
	}

	/**
	 * This writes the mean and lagarithmic standard deviation values to a file following the
	 * original IM Event Set calculator format, with the only change being the addition of
	 * a column for inter event std dev (at Erdem's request).
	 * 
	 * @param erf
	 * @param attenRel
	 * @throws IOException
	 */
	public void writeOriginalMeanSigmaFiles(EqkRupForecastAPI erf, AttenuationRelationship attenRel) throws IOException {
		ArrayList<ParameterAPI> defaultSiteParams = getDefaultSiteParams(attenRel);

		ArrayList<Site> sites = getInitializedSites(attenRel);
		
		StdDevTypeParam stdDevParam = (StdDevTypeParam)attenRel.getParameter(StdDevTypeParam.NAME);
		boolean hasInterIntra = stdDevParam.isAllowed(StdDevTypeParam.STD_DEV_TYPE_INTER) &&
									stdDevParam.isAllowed(StdDevTypeParam.STD_DEV_TYPE_INTRA);
		
		ParameterAPI<?> im = attenRel.getIntensityMeasure();
		String fname = attenRel.getShortName() + "_" + im.getName();
		if (im instanceof SA_Param) {
			SA_Param sa = (SA_Param)im;
			String period = "" + sa.getIndependentParameter(PeriodParam.NAME).getValue();
			fname += "_" + period.replace('.', '_');
		}
		fname += ".txt";
		
		FileWriter fw = new FileWriter(getOutputDir().getAbsolutePath() + File.separator + fname);

		erf.updateForecast();
		
		int numSources = erf.getNumSources();
		
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			if (!shouldIncludeSource(source))
				continue;
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				attenRel.setEqkRupture(rup);
				String line = sourceID + " " + rupID;
				for (Site site : sites) {
					attenRel.setSite(site);
					double mean = attenRel.getMean();
					stdDevParam.setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
					double total = attenRel.getStdDev();
					double inter = -1;
					if (hasInterIntra) {
						stdDevParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTER);
						inter = attenRel.getStdDev();
					}
					line += " " + meanSigmaFormat.format(mean) + " " + meanSigmaFormat.format(total)
									+ " " + meanSigmaFormat.format(inter);
				}
				fw.write(line + "\n");
			}
		}
		fw.close();

		// restore the default site params for the atten rel
		setSiteParams(attenRel, defaultSiteParams);
	}
	
	/**
	 * This writes the rupture distance files following the format of the original IM Event Set Calculator.
	 * The file 'rup_dist_info.txt' is equivelant to the old files, and 'rup_dist_jb_info.txt' is similar
	 * but with JB distances (at Erdem's request).
	 * 
	 * @param erf
	 * @throws IOException
	 */
	public void writeOriginalRupDistFile(EqkRupForecastAPI erf) throws IOException {
		String fname = "rup_dist_info.txt";
		String fname_jb = "rup_dist_jb_info.txt";
		FileWriter fw = new FileWriter(getOutputDir().getAbsolutePath() + File.separator + fname);
		FileWriter fw_jb = new FileWriter(getOutputDir().getAbsolutePath() + File.separator + fname_jb);
		
		ArrayList<Site> sites = getSites();
		
		erf.updateForecast();
		
		int numSources = erf.getNumSources();
		
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			if (!shouldIncludeSource(source))
				continue;
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				String line = sourceID + " " + rupID;
				String lineJB = line;
				for (Site site : sites) {
					PropagationEffect propEffect = new PropagationEffect(site,rup);
					double rupDist = ((Double)propEffect.getParamValue(DistanceRupParameter.NAME)).doubleValue();
					double distJB = ((Double)propEffect.getParamValue(DistanceJBParameter.NAME)).doubleValue();
					line += " " + distFormat.format(rupDist);
					lineJB += " " + distFormat.format(distJB);
				}
				fw.write(line + "\n");
				fw_jb.write(lineJB + "\n");
			}
		}
		fw.close();
		fw_jb.close();
	}
	
	/**
	 * This writes source/rupture metadate to the file 'src_rup_metadata.txt'
	 * 
	 * @param erf
	 * @throws IOException
	 */
	public void writeOriginalSrcRupMetaFile(EqkRupForecastAPI erf) throws IOException {
		String fname = "src_rup_metadata.txt";
		FileWriter fw = new FileWriter(getOutputDir().getAbsolutePath() + File.separator + fname);
		
		ArrayList<Site> sites = getSites();
		
		erf.updateForecast();
		
		int numSources = erf.getNumSources();
		
		double duration = ((TimeSpan)erf.getTimeSpan()).getDuration();
		
		for (int sourceID=0; sourceID<numSources; sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			if (!shouldIncludeSource(source))
				continue;
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				double rate = rup.getMeanAnnualRate(duration);
				fw.write(sourceID + "  " + rupID + " " + (float)rate + "  "
						+ (float)rup.getMag() + "  " + source.getName() + "\n");
			}
		}
		fw.close();
	}
	
	public void writeOriginalFileSet(EqkRupForecastAPI erf, ArrayList<AttenuationRelationship> attenRels,
			ArrayList<String> imts) throws IOException {
		this.writeOriginalSrcRupMetaFile(erf);
		this.writeOriginalRupDistFile(erf);
		int numIMTs = imts.size();
		for (int i = 0; i < attenRels.size(); ++i) {
			AttenuationRelationship attenRel = attenRels.get(i);
			for (int j = 0; j < numIMTs; ++j) {
				String imtLine = (String) imts.get(j);
				setIMTFromString(imtLine, attenRel);
				this.writeOriginalMeanSigmaFiles(erf, attenRel);
			}
		}
	}
	
	public void writeHAZ01A(EqkRupForecastAPI erf, ArrayList<AttenuationRelationship> attenRels,
			ArrayList<String> imts) throws IOException {
		ArrayList<EqkRupForecastAPI> erfs = new ArrayList<EqkRupForecastAPI>();
		erfs.add(erf);
		writeHAZ01(erfs, attenRels, imts);
	}
	
	public void writeHAZ01(ArrayList<EqkRupForecastAPI> erfs, ArrayList<AttenuationRelationship> attenRels,
			ArrayList<String> imts) throws IOException {
		FileWriter fwA = new FileWriter(getOutputDir().getAbsolutePath() + File.separator + "haz01a.txt");
		FileWriter fwB = new FileWriter(getOutputDir().getAbsolutePath() + File.separator + "haz01b.txt");
		
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
		
		fwA.write("\"OpenSHA IM Event Set Calculator Output (HAZ01A): " + formatter.format(now) + "\"\n");
		fwB.write("\"OpenSHA IM Event Set Calculator Output (HAZ01B): " + formatter.format(now) + "\"\n");
		fwA.write("ID,ERF,Source,Rupture,GMPE,Site,VS30,Dist,IMT,Median,LSDT,LSDE\n");
		fwB.write("ID,ERF,Source,Rupture,Rate,Mag,SourceName\n");
		
		int lineIDA = 0;
		int lineIDB = 0;
		
		for (int erfID=0; erfID<erfs.size(); erfID++) {
			String erfName = "erf" + (erfID + 1);
			for (AttenuationRelationship attenRel : attenRels) {
				for (String imt : imts) {
					fwA.flush();
					lineIDA = writeHAZ01A_Part(fwA, lineIDA, imt, erfName, erfs.get(erfID), attenRel);
				}
			}
			fwB.flush();
			lineIDB = writeHAZ01B_Part(fwB, lineIDB, erfName, erfs.get(erfID));
		}
		fwA.close();
		fwB.close();
	}
	
	private int writeHAZ01A_Part(FileWriter fw, int lineID, String imt, String erfName,
				EqkRupForecastAPI erf, AttenuationRelationship attenRel) throws IOException {
//		System.out.println("Writing portion of file for erf: " +  erf.getName() +
//				", imr: " + attenRel.getShortName() + ", imt: " + imt);
		setIMTFromString(imt, attenRel);
		ArrayList<ParameterAPI> defaultSiteParams = getDefaultSiteParams(attenRel);
		
		ArrayList<Site> sites = getInitializedSites(attenRel);
		
		StdDevTypeParam stdDevParam = (StdDevTypeParam)attenRel.getParameter(StdDevTypeParam.NAME);
		boolean hasInterIntra = stdDevParam.isAllowed(StdDevTypeParam.STD_DEV_TYPE_INTER) &&
									stdDevParam.isAllowed(StdDevTypeParam.STD_DEV_TYPE_INTRA);

		erf.updateForecast();
		
		int numSources = erf.getNumSources();
		
		String gmpe = attenRel.getShortName();
		
		for (int siteID=0; siteID<sites.size(); siteID++) {
			Site site = sites.get(siteID);
			
			float vs30 = -1;
			try {
				vs30 = (float)(double)((Double)attenRel.getParameter(Vs30_Param.NAME).getValue());
			} catch (ParameterException e) {
			}
			for (int sourceID=0; sourceID<numSources; sourceID++) {
				ProbEqkSource source = erf.getSource(sourceID);
				if (!shouldIncludeSource(source))
					continue;
				for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
					lineID++;
					ProbEqkRupture rup = source.getRupture(rupID);
					attenRel.setEqkRupture(rup);
					String line = lineID + "," + erfName + "," + sourceID + "," + rupID + "," + gmpe;
					
					PropagationEffect propEffect = new PropagationEffect(site,rup);
					double rupDist = ((Double)propEffect.getParamValue(DistanceRupParameter.NAME)).doubleValue();
					
					
					line += "," + siteID + "," + vs30 + "," + rupDist + "," + imt;
					
					attenRel.setSite(site);
					double mean = attenRel.getMean();
					stdDevParam.setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
					double total = attenRel.getStdDev();
					double inter = -1;
					if (hasInterIntra) {
						stdDevParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTER);
						inter = attenRel.getStdDev();
					}
					line += "," + meanSigmaFormat.format(mean) + "," + meanSigmaFormat.format(total)
									+ "," + meanSigmaFormat.format(inter);
					fw.write(line + "\n");
				}
			}
		}

		// restore the default site params for the atten rel
		setSiteParams(attenRel, defaultSiteParams);
		return lineID;
	}
	
	private int writeHAZ01B_Part(FileWriter fw, int lineID, String erfName,
			EqkRupForecastAPI erf) throws IOException {
		//	System.out.println("Writing portion of file for erf: " +  erf.getName() +
		//			", imr: " + attenRel.getShortName() + ", imt: " + imt);

		ArrayList<Site> sites = getSites();

		erf.updateForecast();

		int numSources = erf.getNumSources();
		
		double duration = ((TimeSpan)erf.getTimeSpan()).getDuration();

		for (int siteID=0; siteID<sites.size(); siteID++) {
			Site site = sites.get(siteID);
			for (int sourceID=0; sourceID<numSources; sourceID++) {
				ProbEqkSource source = erf.getSource(sourceID);
				if (!shouldIncludeSource(source))
					continue;
				for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
					lineID++;
					ProbEqkRupture rup = source.getRupture(rupID);
					double rate = rup.getMeanAnnualRate(duration);
					String line = lineID + "," + erfName + "," + sourceID + "," + rupID + ","
								+ rate + ","+ (float)rup.getMag() + "," + source.getName();
					fw.write(line + "\n");
				}
			}

		}
		return lineID;
	}

}
