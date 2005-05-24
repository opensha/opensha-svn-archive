package org.opensha.nshmp.sha.nico;

import java.rmi.RemoteException;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import org.opensha.data.Site;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.util.FileUtils;
import java.util.StringTokenizer;
import org.opensha.calc.RelativeLocation;

/**
 * <p>Title: MeanSigmaCalcWithSiteEffectsProvided</p>
 *
 * <p>Description: This class calculates the Mean and Sigma (in Natural Log)
 * for the provided census track. This class is different from the MeanSigmaCalc
 * becuase rather then getting the Site effects for each location dynamically
 * using webservice, site effects are provided to us for each location in this
 * census track.</p>
 * @author Ned Field, Nitin Gupta
 * @version 1.0
 */
public class MeanSigmaCalcWithSiteEffectsProvided extends MeanSigmaCalc{

  public MeanSigmaCalcWithSiteEffectsProvided(String inpFile,String outDir) {
   super(inpFile,outDir);
  }


  /**
   * Creates the locationlist from the file for Nico.
   * Creates a location using the given locations to find source cut-off disance.
   * @return
   */
  protected void createSiteList() {
    locList = new LocationList();
    willsClass = new ArrayList();
    try {
      ArrayList fileLines = FileUtils.loadFile(inputFileName);

      //gets the min lat, lon and max lat, lon from given set of locations.
      double minLon = Double.MAX_VALUE;
      double maxLon = Double.NEGATIVE_INFINITY;
      double minLat = Double.MAX_VALUE;
      double maxLat = Double.NEGATIVE_INFINITY;
      int numSites= fileLines.size();
      for (int i = 0; i < numSites; ++i) {
        String firstLine = (String) fileLines.get(i);
        StringTokenizer st = new StringTokenizer(firstLine);
        double lat = Double.parseDouble(st.nextToken());
        double lon = Double.parseDouble(st.nextToken());
        String siteClass = st.nextToken();
        locList.addLocation(new Location(lat,lon));
        willsClass.add(siteClass);
        if(lon > maxLon)
          maxLon = lon;
        if(lon < minLon)
          minLon = lon;
        if(lat > maxLat)
          maxLat = lat;
        if(lat < minLat)
          minLat = lat;
      }
      double middleLon = (minLon + maxLon)/2;
      double middleLat = (minLat + maxLat)/2;

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



  /**
   * Main program to run the application
   * @param args String[]
   */
  public static void main(String[] args) {
    if(args.length != 2){
      System.out.println("Usage :\n\t"+"java -jar [jarfileName] [inputFileName] [output directory name]\n\n");
      System.out.println("jarfileName : Name of the executable jar file, by default it is MeanSigmaCalc.jar");
      System.out.println("inputFileName :Name of the input file, this input file should contain only 3 columns"+
                         " \"Lat Lon SiteClassValue\", For eg: see \"CensusTrackwithSiteInfo.txt\". ");
      System.out.println("output directory name : Name of the output directory where all the output files will be generated");
      System.exit(0);
    }

    MeanSigmaCalcWithSiteEffectsProvided calc = new MeanSigmaCalcWithSiteEffectsProvided(args[0],args[1]);
    calc.createSiteList();

    try {
      calc.createFrankel02Forecast();
    }
    catch (RemoteException ex) {
      ex.printStackTrace();
    }
    calc.createAttenuationRelationObjects();
    calc.getMeanSigma();
  }

}
