package org.opensha.cybershake;

import java.io.*;
import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.data.*;
import org.opensha.sha.surface.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.region.CircularGeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.util.SystemPropertiesUtils;

/**
 * <p>Title: ERF2RuptureForSTF_Generator</p>
 *
 * <p>Description: This class returns the list of EqkRuptures either
 * as ArrayList or a XML String, which can be dumped in the file.</p>
 * @author Nitin Gupta and Ned Field
 * @version 1.0
 */


public class ERF2RuptureForSTF_Generator {

  private EqkRupForecastAPI eqkRupForecast;

  private Site site;
  private double distance;

  //max Depth of any location rupture in the volume
  private double maxDepth;

  //to see if the rupture is within the circular distance of the given Site.
  private CircularGeographicRegion region;



  public ERF2RuptureForSTF_Generator(EqkRupForecast eqkRupForecast, Site site,
                      double cuttOffDistance) {
    this.eqkRupForecast = eqkRupForecast;
    this.site = site;
    this.distance = cuttOffDistance;
    region = new
        CircularGeographicRegion(site.getLocation(), distance);
  }

  /**
   * Returns the list of the EqkRupture for a Site. Only those ruptures are
   * included which are with in the cut-off distance.
   * @return ArrayList List of ProbEqkRupRuptures
   */
  public ArrayList getEqkRupturesNearSite() {

    //initializing the list for containing the EqkRuptures
    ArrayList probEqkList = new ArrayList();
    int numSources = eqkRupForecast.getNumSources();

    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface().getGridCenteredSurface();


        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt locations and if any of those lies
        //within the provided distance range then include the rupture in the list.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          if (region.isLocationInside(ptLoc)) {
            probEqkList.add(rupture.clone());
            break;
          }
        }
      }
    }
    return probEqkList;
  }

  /**
   * Creates XML file with metadata about the selected ERF, Site and cut-off distance
   * as the Header info. It also writes out the Source index for the ERF and Ruptures
   * index and its info like prob,mag,rake,dip,strike and GriddedSurface in the same
   * XML file. Ruptures which are in the range of cut-off distance form the Site
   * are written to the XML file.
   *
   * @return FileWriter handle to the XML file
   */
  public void getEqkRupturesAsXMLStringNearSite(String directoryName) {

    //Location of the Site
    Location siteLoc = site.getLocation();


    FileWriter fw = null;
    try {
      File f = new File(directoryName);

      String directoryPath = f.getAbsolutePath();

      if(!directoryPath.endsWith(SystemPropertiesUtils.getSystemFileSeparator()))
      directoryPath += SystemPropertiesUtils.getSystemFileSeparator();
      if(!f.exists() || !f.isDirectory()){
        f.mkdir();

        fw = new FileWriter(directoryPath + "EqkRupForecast_Params.txt");

        String erfString = "EqkRupForecast_Class = " +
            eqkRupForecast.getClass().getName() + "\n";

        ListIterator it = eqkRupForecast.getAdjustableParamsIterator();
        while (it.hasNext()) {
          ParameterAPI param = (ParameterAPI) it.next();
          erfString += param.getName() + "=" + param.getValue() + "\n";
        }
        fw.write(erfString);
        fw.close();
      }


      fw = new FileWriter(directoryPath+(float)siteLoc.getLatitude()+"_"+(float)siteLoc.getLongitude()+".txt");
      String siteString ="";
      siteString += "Site-Latitude = "+(float)siteLoc.getLatitude() +"\n";
      siteString += "Site-Longitude = "+(float)siteLoc.getLongitude() +"\n";

      siteString += "Site-Depth = " + (float)siteLoc.getDepth() + "\n";
      siteString += "Cut-Off-Distance = " + distance +"\n";
      fw.write(siteString);
      fw.close();
      int numSources = eqkRupForecast.getNumSources();

      //Going over each and every source in the forecast
      for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {

        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
        int numRuptures = source.getNumRuptures();

        //going over all the ruptures in the source
        for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {

          ProbEqkRupture rupture = source.getRupture(rupIndex);
          GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface().
              getGridCenteredSurface();

          //getting the iterator for all points on the rupture
          ListIterator lit = rupSurface.getAllByRowsIterator();
          //looping over all the rupture pt locations and if any of those lies
          //within the provided distance range then include the rupture in the list.
          while (lit.hasNext()) {
            Location ptLoc = (Location) lit.next();
            if(region.isLocationInside(ptLoc)) {
              fw = new FileWriter(directoryPath+sourceIndex+"_"+rupIndex+".txt");
              String ruptureString = ruptureString(rupture);
              fw.write(ruptureString);
              fw.close();
              break;
            }
          }

        }

      }
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the Hashmap for the EqkRuptures with key being the SourceIndex
   * and values being the ArrayList of the rupture indicies.
   * @param eqkRupForecast EqkRupForecastAPI
   * @param site Site
   * @param distance double
   * @return HashMap
   */
  public HashMap getProbEqkRuptureIdentifiersNearSite() {
    //initializing the list for containing the EqkRuptures
    HashMap probRupIdentifierList = new HashMap();

    int numSources = eqkRupForecast.getNumSources();
    //creating a ArrayList to hold the rupture indices for a given site and eqkrupforecast.
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex)
      probRupIdentifierList.put(new Integer(sourceIndex), new ArrayList());


    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface().getGridCenteredSurface();

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt locations and if any of those lies
        //within the provided distance range then include the rupture in the list.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          if (region.isLocationInside(ptLoc)) {
            ArrayList rupIndicesList = (ArrayList) probRupIdentifierList.get(new
                Integer(sourceIndex));
            rupIndicesList.add(new Integer(rupIndex));
            break;
          }
        }
      }
    }
    return probRupIdentifierList;
  }



  /**
   * Returns the regional bounds of the largest rectangular region around the
   * site that include all the ruptures surface locations within the range of
   * provided distance.
   * @return RectangularGeographicRegion
   */
  public RectangularGeographicRegion getSiteRegionBounds() throws
      RegionConstraintException {
    int numSources = eqkRupForecast.getNumSources();

    double minLat = Double.POSITIVE_INFINITY;
    double maxLat = Double.NEGATIVE_INFINITY;
    double minLon = Double.POSITIVE_INFINITY;
    double maxLon = Double.NEGATIVE_INFINITY;


    //Going over each and every source in the forecast
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
      // get the ith source
      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
      int numRuptures = source.getNumRuptures();

      //going over all the ruptures in the source
      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
        ProbEqkRupture rupture = source.getRupture(rupIndex);

        GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();

        //getting the iterator for all points on the rupture
        ListIterator it = rupSurface.getAllByRowsIterator();
        //looping over all the rupture pt locations and if any of those lies
        //within the provided distance range then include the rupture in the list.
        while (it.hasNext()) {
          Location ptLoc = (Location) it.next();
          if (region.isLocationInside(ptLoc)) {
            double lat = ptLoc.getLatitude();
            double lon = ptLoc.getLongitude();
            double depth = ptLoc.getDepth();
            if (lat < minLat)
              minLat = lat;
            if (lat > maxLat)
              maxLat = lat;
            if (lon < minLon)
              minLon = lon;
            if (lon > maxLon)
              maxLon = lon;
            if (depth > maxDepth)
              maxDepth = depth;
          }
        }
      }
    }
    return new RectangularGeographicRegion(minLat, maxLat, minLon, maxLon);

  }

  /**
   * Returns the maximum depth for which any given rupture in a given geographic
   * region extends.
   * @return double
   */
  public double getMaxDepthForRuptureInRegionBounds() throws
      RegionConstraintException {
    if(maxDepth == 0)
      getSiteRegionBounds();
    return maxDepth;
  }

  /**
   *
   * @param args String[]
   */
  public static void main(String[] args) {

    Frankel02_AdjustableEqkRupForecast frankelForecast = null;

    frankelForecast = new
        Frankel02_AdjustableEqkRupForecast();

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
                                 BACK_SEIS_INCLUDE);

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME).
        setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);

    frankelForecast.getAdjustableParameterList().getParameter(
      Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_NAME).setValue(
        frankelForecast.FAULT_MODEL_STIRLING);
    frankelForecast.getAdjustableParameterList().getParameter(
      Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_NAME).setValue(
        new Double(5.0));

    frankelForecast.getTimeSpan().setDuration(50.0);
    frankelForecast.updateForecast();
    LocationList locList = new LocationList();

    locList.addLocation(new Location(34.01919, -118.28631));
    /*locList.addLocation(new Location(34.14844 , -118.17117));
    locList.addLocation(new Location(34.054985,-118.41201));
    locList.addLocation(new Location(34.052041, -118.25713));
    locList.addLocation(new Location(33.754962, -118.22300));
    locList.addLocation(new Location(33.754111, -117.86776));
    locList.addLocation(new Location(34.041832,-118.06528));
    locList.addLocation(new Location(34.063778, -117.29572));
    locList.addLocation(new Location(34.009092, -118.48937));
    locList.addLocation(new Location(34.199765, -118.35602));*/

    ListIterator it = locList.listIterator();
    /*FileWriter fw = null;
    try {
      fw = new FileWriter("SiteDistanceBounds.txt");
    }
    catch (IOException ex1) {
      ex1.printStackTrace();
    }*/
    while (it.hasNext()) {
      Location loc = (Location) it.next();
      //System.out.println("After creating the ERF");
      Site site = new Site(loc);
      ERF2RuptureForSTF_Generator calc = new ERF2RuptureForSTF_Generator(frankelForecast, site, 200.0);
      calc.getEqkRupturesAsXMLStringNearSite("Temp");
      /*RectangularGeographicRegion region = null;
      try {
        region = calc.getSiteRegionBounds();
        double maxDepth = calc.getMaxDepthForRuptureInRegionBounds();
        double minLat = region.getMinLat();
        double maxLat = region.getMaxLat();
        double minLon = region.getMinLon();
        double maxLon = region.getMaxLon();
        double distanceSWSE = RelativeLocation.getApproxHorzDistance(minLat,
            minLon, minLat, maxLon);
        double distanceNWSW = RelativeLocation.getApproxHorzDistance(minLat,
            minLon, maxLat, minLon);

        try {
          fw.write("Site : Lat =" + loc.getLatitude() + "   Lon =" +
                   loc.getLongitude() + "\n");
          fw.write("Site Region Bounds :\n");
          fw.write("MinLat = " + minLat + "  MaxLat = " + maxLat + "  MinLon = " +
                   minLon + "  MaxLon = " + maxLon +
                   "  MaxDepth = " + maxDepth + "\n");
          fw.write("Length of the box along longitude = "+distanceSWSE+"\n");
          fw.write("Length of the box along latitude = "+distanceNWSW+"\n");
        }
        catch (IOException ex2) {
          ex2.printStackTrace();
        }

      }
      catch (RegionConstraintException ex) {
        ex.printStackTrace();
      }
      try {
        fw.write("\n\n\n");
      }
      catch (IOException ex4) {
        ex4.printStackTrace() ;
      }*/
    }
   /* try {
      fw.close();
    }
    catch (IOException ex3) {
      ex3.printStackTrace();
    }*/
  }

  /**
   * Creates the XML representation for the Eqk Rupture Object
   * @return String
   */
  private String ruptureString(ProbEqkRupture rupture) {

    String rupInfo = "";
    rupInfo += "Probability = " + (float)rupture.getProbability() +"\n";
    rupInfo += "Magnitude = " + (float)rupture.getMag() +"\n";

    GriddedSurfaceAPI surface = rupture.getRuptureSurface();
    double gridSpacing = (float)this.getGridSpacing(surface);
    rupInfo += "GridSpacing = " + gridSpacing +"\n";
    ListIterator it = rupture.getAddedParametersIterator();
    if (it != null) {
      while (it.hasNext()) {
        ParameterAPI param = (ParameterAPI) it.next();
        rupInfo += param.getName() + "=" + param.getValue() + "\n";
      }
    }

    double rake = rupture.getAveRake();
    double dip = surface.getAveDip();

    //Local Strike for each grid centered location on the rupture
    double[] localStrikeList = this.getLocalStrikeList(surface);

    GriddedSurfaceAPI rupSurface = surface.getGridCenteredSurface();
    int numRows = rupSurface.getNumRows();
    int numCols = rupSurface.getNumCols();
    rupInfo += "NumRows = "+numRows+"\n";
    rupInfo += "NumCols = "+numCols+"\n";
    rupInfo +="# Lat  Lon  Depth  Rake  Dip  Strike\n";
    for(int i=0;i<numRows;++i){
      for (int j = 0; j < numCols; ++j) {
        Location loc = rupSurface.getLocation(i,j);
        rupInfo += (float)loc.getLatitude() + "    " + (float)loc.getLongitude() + "    " +
            (float)loc.getDepth() +"   "+(float)rake+"    "+(float)dip+"   "+(float)localStrikeList[j]+"\n";
      }
    }
    return rupInfo;
  }

  /**
   * Returns the local strike list for a given rupture
   * @param surface GriddedSurfaceAPI
   * @return double[]
   */
  private double[] getLocalStrikeList(GriddedSurfaceAPI surface){
    int numCols = surface.getNumCols();
    double[] localStrike = new double[numCols-1];
    for(int i=0;i<numCols-1;++i){
      Location loc1 = surface.getLocation(0,i);
      Location loc2 = surface.getLocation(0,i+1);
      double strike = RelativeLocation.getAzimuth(loc1.getLatitude(),loc1.getLongitude(),loc2.getLatitude(),loc2.getLongitude());
      localStrike[i] = strike;
    }
    return localStrike;
  }



  /**
   * Returns the gridspacing for the Latitudes
   * @return double
   */
  private double getGridSpacing(GriddedSurfaceAPI surface) {
    if(surface instanceof EvenlyGriddedSurface)
      return ((EvenlyGriddedSurface)surface).getGridSpacing();
    else{
      Location loc1 = surface.getLocation(0, 0);
      Location loc2 = surface.getLocation(1, 0);
      return Math.abs(loc2.getLatitude() - loc1.getLatitude());
    }
  }


}
