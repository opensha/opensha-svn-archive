package org.opensha.sha.calc;

import java.io.*;
import java.util.*;

import org.opensha.sha.earthquake.*;
import org.opensha.data.*;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.calc.RelativeLocation;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.region.CircularGeographicRegion;
import org.opensha.data.region.RectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: ERF2XML_Calc</p>
 *
 * <p>Description: This class returns the list of EqkRuptures either
 * as ArrayList or a XML String, which can be dumped in the file.</p>
 * @author Nitin Gupta and Ned Field
 * @version 1.0
 */


public class ERF2XML_Calc {

  private EqkRupForecastAPI eqkRupForecast;

  private Site site;
  private double distance;

  //max Depth of any location rupture in the volume
  private double maxDepth;

  //to see if the rupture is within the circular distance of the given Site.
  private CircularGeographicRegion region;

  private static int xmlFileCounter =1;
  private static int erfSourceIndexXML_FileCounter=1;


  public ERF2XML_Calc(EqkRupForecast eqkRupForecast, Site site,
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
   *
   * @param eqkRupForecast EqkRupForecastAPI
   * @param site Site
   * @param distance double
   * @return String
   */
  public FileWriter getEqkRupturesAsXMLStringNearSite() {

    //Location of the Site
    Location siteLoc = site.getLocation();

    FileWriter fw = null;
    try {
      fw = new FileWriter("Site"+xmlFileCounter+".xml");

      String rupturesXMLString = "<Earthquake>\n";
      rupturesXMLString += "<EqkRupForecast>\n";
      rupturesXMLString += "<EqkRupForecast-Class>" +
          eqkRupForecast.getClass().getName() + "</EqkRupForecast-Class>\n";
      rupturesXMLString += "<EqkRupForecast-Params>\n";
      ListIterator it = eqkRupForecast.getAdjustableParamsIterator();
      while (it.hasNext()) {
        ParameterAPI param = (ParameterAPI) it.next();
        rupturesXMLString +=  param.getName() +"="+ param.getValue() + "\n";
      }
      rupturesXMLString += "</EqkRupForecast-Params>\n";
      rupturesXMLString += "</EqkRupForecast>\n";
      rupturesXMLString += "<Site>\n";
      rupturesXMLString += "<Site-Location>\n";
      rupturesXMLString += "<Latitude>" + siteLoc.getLatitude() +
          "</Latitude>\n";
      rupturesXMLString += "<Longitude>" + siteLoc.getLongitude() +
          "</Longitude>\n";
      rupturesXMLString += "<Depth>" + siteLoc.getDepth() + "</Depth>\n";
      rupturesXMLString += "</Site-Location>\n";
      rupturesXMLString += "</Site>\n";
      rupturesXMLString += "<Cut-Off-Distance>" + distance +
          "</Cut-Off-Distance>\n";
      rupturesXMLString += "<ProbEqkRupList>\n";
      int numSources = eqkRupForecast.getNumSources();
      fw.write(rupturesXMLString);
      rupturesXMLString = "";
      //Going over each and every source in the forecast
      for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {

        // get the ith source
        ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
        int numRuptures = source.getNumRuptures();
        //System.out.println("Going over source :" + sourceIndex +
          //                 " with numRups :" + numRuptures);
        rupturesXMLString += "<FaultSource>\n";
        rupturesXMLString += "SourceIndex = " + sourceIndex +"\n";
        //going over all the ruptures in the source
        for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {

          ProbEqkRupture rupture = source.getRupture(rupIndex);

          GriddedSurfaceAPI rupSurface = rupture.getRuptureSurface().getGridCenteredSurface();

          //getting the iterator for all points on the rupture
          ListIterator lit = rupSurface.getAllByRowsIterator();
          //looping over all the rupture pt locations and if any of those lies
          //within the provided distance range then include the rupture in the list.
          while (lit.hasNext()) {
            Location ptLoc = (Location) lit.next();
            if (region.isLocationInside(ptLoc)) {
              rupturesXMLString += "RuptureIndex =" + rupIndex +"\n";
              rupturesXMLString += rupture.ruptureXML_String();
              break;
            }
          }
          fw.write(rupturesXMLString);
          rupturesXMLString = "";
        }
        rupturesXMLString += "</FaultSource>\n";
        fw.write(rupturesXMLString);
        rupturesXMLString = "";
      }

      rupturesXMLString += "</ProbEqkRupList>\n";
      rupturesXMLString += "</Earthquake>\n";
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    ++xmlFileCounter;
    return fw;
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
    //creating a arrayList to hold the rupture indices for a given site and eqkrupforecast.
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
   *
   * @param eqkRupForecast EqkRupForecastAPI
   * @param site Site
   * @param distance double
   * @return String
   */
  public FileWriter getProbEqkRuptureIdentifiersXMLFileNearSite() {

    //Location of the Site
    Location siteLoc = site.getLocation();

    FileWriter fw = null;
    try {
      fw = new FileWriter("ERF_Identifier"+erfSourceIndexXML_FileCounter+".xml");

      String rupturesXMLString = "<Earthquake>\n";
      rupturesXMLString += "<EqkRupForecast>\n";
      rupturesXMLString += "<EqkRupForecast-Class>" +
          eqkRupForecast.getClass().getName() + "</EqkRupForecast-Class>\n";
      rupturesXMLString += "<EqkRupForecast-Params>\n";
      ListIterator it = eqkRupForecast.getAdjustableParamsIterator();
      while (it.hasNext()) {
        ParameterAPI param = (ParameterAPI) it.next();
        rupturesXMLString +=  param.getName() +"="+ param.getValue() + "\n";
      }
      rupturesXMLString += "</EqkRupForecast-Params>\n";
      rupturesXMLString += "</EqkRupForecast>\n";
      rupturesXMLString += "<Site>\n";
      rupturesXMLString += "<Site-Location>\n";
      rupturesXMLString += "<Latitude>" + siteLoc.getLatitude() +
          "</Latitude>\n";
      rupturesXMLString += "<Longitude>" + siteLoc.getLongitude() +
          "</Longitude>\n";
      rupturesXMLString += "<Depth>" + siteLoc.getDepth() + "</Depth>\n";
      rupturesXMLString += "</Site-Location>\n";
      rupturesXMLString += "</Site>\n";
      rupturesXMLString += "<Cut-Off-Distance>" + distance +
          "</Cut-Off-Distance>\n";
      rupturesXMLString += "<ProbEqkRupList>\n";
      fw.write(rupturesXMLString);
      rupturesXMLString = "";

      HashMap sourceRupList = getProbEqkRuptureIdentifiersNearSite();
      Set set = sourceRupList.keySet();
      Iterator lit = set.iterator();
      while(lit.hasNext()){
        int sourceIndex = ((Integer)lit.next()).intValue();
        ArrayList rupList = (ArrayList)sourceRupList.get(new Integer(sourceIndex));
        int rupListSize = rupList.size();
        if(rupListSize >0){
          rupturesXMLString += "<FaultSource>\n";
          rupturesXMLString += "SourceIndex = " + sourceIndex + "\n";
          for(int i=0;i<rupListSize;++i){
            rupturesXMLString += "RuptureIndex =" + ((Integer)rupList.get(i)).intValue() + "\n";
          }
          rupturesXMLString += "</FaultSource>\n";
          fw.write(rupturesXMLString);
          rupturesXMLString = "";
        }
      }
      rupturesXMLString += "</ProbEqkRupList>\n";
      rupturesXMLString += "</Earthquake>\n";
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    ++xmlFileCounter;
    return fw;
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
    frankelForecast.getTimeSpan().setDuration(50.0);
    frankelForecast.updateForecast();
    LocationList locList = new LocationList();

    locList.addLocation(new Location(34.01919, -118.28631));
    locList.addLocation(new Location(34.14844 , -118.17117));
    locList.addLocation(new Location(34.054985,-118.41201));
    locList.addLocation(new Location(34.052041, -118.25713));
    locList.addLocation(new Location(33.754962, -118.22300));
    locList.addLocation(new Location(33.754111, -117.86776));
    locList.addLocation(new Location(34.041832,-118.06528));
    locList.addLocation(new Location(34.063778, -117.29572));
    locList.addLocation(new Location(34.009092, -118.48937));
    locList.addLocation(new Location(34.199765, -118.35602));

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
      ERF2XML_Calc calc = new ERF2XML_Calc(frankelForecast, site, 200.0);
      calc.getEqkRupturesAsXMLStringNearSite();
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
}
