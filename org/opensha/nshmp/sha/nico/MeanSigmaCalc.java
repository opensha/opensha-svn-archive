package org.opensha.nshmp.sha.nico;

import org.opensha.data.Location;
import org.opensha.data.LocationList;

import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.imr.*;
import org.opensha.sha.imr.attenRelImpl.*;

import org.opensha.param.ParameterAPI;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.param.WarningParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.data.Site;
import java.util.*;
import java.io.*;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.util.*;
import java.rmi.RemoteException;
import org.opensha.calc.RelativeLocation;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MeanSigmaCalc
    implements ParameterChangeWarningListener {

  private String[] willsClass;
  private LocationList locList;
  private ArrayList locNameList;

  private Frankel02_AdjustableEqkRupForecastClient frankelForecast;

  //supported Attenuations
  private ArrayList supportedAttenuationsList;

  private final static String MEAN = "mean";
  private final static String SIGMA = "sigma";

  //some static IMT names

  private final static String PGA ="PGA";
  private final static String SA_10 = "SA_10";
  private final static String SA_03 = "SA_03";


  private double sourceCutOffDistance;
  private final static double MIN_DIST = 200;
  private Site siteForSourceCutOff;

  // site translator
  private SiteTranslator siteTranslator = new SiteTranslator();

  public MeanSigmaCalc() {
  }

  /**
   * Creating the instance of the Frankel02 forecast
   */
  public void createFrankel02Forecast() throws RemoteException{
    frankelForecast = new Frankel02_AdjustableEqkRupForecastClient();
    frankelForecast.getAdjustableParameterList().getParameter(Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
    frankelForecast.getAdjustableParameterList().getParameter(Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_RUP_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
    frankelForecast.getAdjustableParameterList().getParameter(Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(10.0));
    frankelForecast.getTimeSpan().setDuration(1.0);
    frankelForecast.updateForecast();
  }

  /**
   * Creating the instances of the Attenuation Relationhships
   */
  public void createAttenuationRelationObjects() {
    AS_1997_AttenRel as1997 = new AS_1997_AttenRel(this);
    CB_2003_AttenRel cb2003 = new CB_2003_AttenRel(this);
    SCEMY_1997_AttenRel scemy1997 = new SCEMY_1997_AttenRel(this);
    BJF_1997_AttenRel bjf1997 = new BJF_1997_AttenRel(this);
    supportedAttenuationsList = new ArrayList();
    supportedAttenuationsList.add(as1997);
    supportedAttenuationsList.add(cb2003);
    supportedAttenuationsList.add(scemy1997);
    supportedAttenuationsList.add(bjf1997);
  }

  /**
   * Gets the wills  site class for the given sites
   */

  public void getSiteParamsForRegion() {
    int numSites = locList.size();
    // get the vs 30 and basin depth from cvm
    willsClass = new String[numSites];
    try {

      for (int i = 0; i < numSites; ++i) {
        double lat = ( (Location) locList.getLocationAt(i)).getLatitude();
        double lon = ( (Location) locList.getLocationAt(i)).getLongitude();
        willsClass[i] = (String) (ConnectToCVM.getWillsSiteTypeFromCVM(lon,
            lon, lat, lat,
            0.5)).get(0);
      }
    }
    catch (Exception ee) {
      ee.printStackTrace();
      return;
    }
  }

  /**
   * Starting with the Mean and Sigma calculation.
   * Creates the directory to put the mean and sigma files.
   */
  private void getMeanSigma() {

    int numIMRs = supportedAttenuationsList.size();

    String dirName = "MeanSigma";
    File file = new File(dirName);
    file.mkdir();
    this.generateRuptureFile(frankelForecast,
                             dirName +
                             SystemPropertiesUtils.getSystemFileSeparator() +
                             "Rupture_Prob.txt");

    for (int i = 0; i < numIMRs; ++i) {
      AttenuationRelationshipAPI attenRel = (AttenuationRelationshipAPI)
          supportedAttenuationsList.get(i);
      attenRel.setParamDefaults();
      generateMeanAndSigmaFile(attenRel,dirName+SystemPropertiesUtils.getSystemFileSeparator());
    }
  }


  /**
   * set the site params in IMR according to basin Depth and vs 30
   * @param imr
   * @param lon
   * @param lat
   * @param willsClass
   * @param basinDepth
   */
  private void setSiteParamsInIMR(AttenuationRelationshipAPI imr,
                                  String willsClass) {

    Iterator it = imr.getSiteParamsIterator(); // get site params for this IMR
    while (it.hasNext()) {
      ParameterAPI tempParam = (ParameterAPI) it.next();
      //adding the site Params from the CVM, if site is out the range of CVM then it
      //sets the site with whatever site Parameter Value user has choosen in the application
      boolean flag = siteTranslator.setParameterValue(tempParam, willsClass,
          Double.NaN);
      if (!flag) {
        String message = "cannot set the site parameter \"" + tempParam.getName() +
            "\" from Wills class \"" + willsClass + "\"" +
            "\n (no known, sanctioned translation - please set by hand)";
      }
    }
  }

  /**
   * Creates the locationlist from the file for Nico.
   * Creates a location using the given locations to find source cut-off disance.
   * @return
   */
  private void createSiteList() {
    locList = new LocationList();
    locNameList = new ArrayList();
    try {
      ArrayList fileLines = FileUtils.loadFile("trackSiteInfo.txt");

      //gets the min lat, lon and max lat, lon from given set of locations.
      double minLon = Double.MAX_VALUE;
      double maxLon = Double.MIN_VALUE;
      double minLat = Double.MAX_VALUE;
      double maxLat = Double.MIN_VALUE;
      int numSites= fileLines.size();
      for (int i = 0; i < numSites; ++i) {
        String firstLine = (String) fileLines.get(i);
        StringTokenizer st = new StringTokenizer(firstLine);
        String trackNumber = st.nextToken();
        double lon = Double.parseDouble(st.nextToken());
        double lat = Double.parseDouble(st.nextToken());
        locList.addLocation(new Location(lat,lon));
        locNameList.add(trackNumber);
        if(lon > maxLon)
          maxLon = lon;
        if(lon < minLon)
          minLon = lon;
        if(lat > maxLat)
          maxLat = lat;
        if(lat < minLat)
          minLat = lat;
      }
      double middleLat = (minLon + maxLon)/2;
      double middleLon = (minLat + maxLat)/2;

      //getting the source-site cuttoff distance
      sourceCutOffDistance = RelativeLocation.getHorzDistance(middleLat,middleLon,minLat,minLon) + MIN_DIST;
      siteForSourceCutOff = new Site(new Location(middleLat,middleLon));
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    return;
  }



  public void generateMeanAndSigmaFile(AttenuationRelationshipAPI imr,String dirName) {

    // get total number of sources
    int numSources = frankelForecast.getNumSources();
    // init the current rupture number
    int currRuptures = 0;
    String fileNamePrefixCommon = dirName +
    SystemPropertiesUtils.getSystemFileSeparator() + imr.getName();
    // set the Site in IMR
    try {
      // opens the files for writing
      FileWriter fwPGAMean = new FileWriter(fileNamePrefixCommon +"_"+PGA+ "_"+MEAN+".txt");
      FileWriter fwPGASigma = new FileWriter(fileNamePrefixCommon +"_"+PGA+ "_"+SIGMA+".txt");

      FileWriter fwSA_03_Mean = new FileWriter(fileNamePrefixCommon +"_"+SA_03+ "_"+MEAN+".txt");
      FileWriter fwSA_03_Sigma = new FileWriter(fileNamePrefixCommon +"_"+SA_03+ "_"+SIGMA+".txt");

      FileWriter fwSA_10_Mean = new FileWriter(fileNamePrefixCommon +"_"+SA_10+ "_"+MEAN+".txt");
      FileWriter fwSA_10_Sigma = new FileWriter(fileNamePrefixCommon +"_"+SA_10+ "_"+SIGMA+".txt");

      // loop over sources
      for (int sourceIndex = 0; sourceIndex < numSources; sourceIndex++) {

        // get the ith source
        ProbEqkSource source = frankelForecast.getSource(sourceIndex);

        if(source.getMinDistance(siteForSourceCutOff) > sourceCutOffDistance)
          continue;

        // get the number of ruptures for the current source
        int numRuptures = source.getNumRuptures();



        // loop over these ruptures
        for (int n = 0; n < numRuptures; n++, ++currRuptures) {

          EqkRupture rupture = source.getRupture(n);
          // set the EqkRup in the IMR
          imr.setEqkRupture(rupture);

          fwPGAMean.write(currRuptures + " ");
          fwPGASigma.write(currRuptures + " ");

          fwSA_03_Mean.write(currRuptures + " ");
          fwSA_03_Sigma.write(currRuptures + " ");

          fwSA_10_Mean.write(currRuptures + " ");
          fwSA_10_Sigma.write(currRuptures + " ");

          int numSites = locList.size();
          //looping over all the sites for the selected Attenuation Relationship
          for (int j = 0; j < numSites; ++j) {
            setSiteParamsInIMR(imr, willsClass[j]);
            Site site = new Site(locList.getLocationAt(j));
            Iterator it = imr.getSiteParamsIterator();
            while (it.hasNext())
              site.addParameter( (ParameterAPI) it.next());
            imr.setSite(site);
            //setting different intensity measures for each site and writing those to the file.
            imr.setIntensityMeasure(AttenuationRelationship.PGA_NAME);

            fwPGAMean.write(imr.getMean() + " ");
            fwPGASigma.write(imr.getStdDev() + " ");

            imr.setIntensityMeasure(AttenuationRelationship.SA_NAME);
            imr.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new
                Double(1.0));

            fwSA_10_Mean.write(imr.getMean() + " ");
            fwSA_10_Sigma.write(imr.getStdDev() + " ");

            imr.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new
                Double(0.3));

            fwSA_03_Mean.write(imr.getMean() + " ");
            fwSA_03_Sigma.write(imr.getStdDev() + " ");

          }

          fwPGAMean.write("\n");
          fwPGASigma.write("\n");

          fwSA_03_Mean.write("\n");
          fwSA_03_Sigma.write("\n");

          fwSA_10_Mean.write("\n");
          fwSA_10_Sigma.write("\n");


        }
      }
      fwPGAMean.close();
      fwPGASigma.close();

      fwSA_03_Mean.close();
      fwSA_03_Sigma.close();

      fwSA_10_Mean.close();
      fwSA_10_Sigma.close();

      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

  public void generateRuptureFile(EqkRupForecastAPI eqkRupForecast,
                                  String outFileName) {
    // get total number of sources
    int numSources = eqkRupForecast.getNumSources();
    // init the current rupture number
    int currRuptures = 0;
    try {
      // opens the files for writing
      FileWriter fwRup = new FileWriter(outFileName);

      // loop over sources
      for (int sourceIndex = 0; sourceIndex < numSources; sourceIndex++) {

        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);


        if(source.getMinDistance(siteForSourceCutOff) > sourceCutOffDistance)
          continue;
        // get the number of ruptures for the current source
        int numRuptures = source.getNumRuptures();

        // loop over these ruptures
        for (int n = 0; n < numRuptures; n++, ++currRuptures) {

          ProbEqkRupture rupture = (ProbEqkRupture) source.getRupture(n);
          fwRup.write(currRuptures + " " + rupture.getProbability() + "\n");
        }
      }
      fwRup.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   *  Function that must be implemented by all Listeners for
   *  ParameterChangeWarnEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChangeWarning(ParameterChangeWarningEvent e) {

    String S = " : parameterChangeWarning(): ";

    WarningParameterAPI param = e.getWarningParameter();

    //System.out.println(b);
    param.setValueIgnoreWarning(e.getNewValue());

  }

  public static void main(String[] args) {
    MeanSigmaCalc calc = new MeanSigmaCalc();
    calc.createSiteList();

    try {
      calc.createFrankel02Forecast();
    }
    catch (RemoteException ex) {
      ex.printStackTrace();
    }
    calc.createAttenuationRelationObjects();
    calc.getSiteParamsForRegion();
    calc.getMeanSigma();
  }
}
