package org.opensha.sha.calc;

import java.util.*;
import java.net.*;
import java.io.*;

import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.data.Location;
import org.opensha.data.Site;
import org.opensha.sha.imr.*;
import org.opensha.sha.earthquake.*;
import org.opensha.data.XYZ_DataSetAPI;
import org.opensha.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.exceptions.ParameterException;
import org.opensha.sha.param.PropagationEffect;
import org.opensha.param.ParameterList;

import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: ScenarioShakeMapCalculator</p>
 * <p>Description: This class calculates the Scenario Shake Map Data using the
 * based on the PropagationEffectParam input parameters imr, site and eqkRupforecast</p>
 * @author : Nitin Gupta
 * @created May 19,2004
 * @version 1.0
 */

public class ScenarioShakeMapCalculator {

  protected final static String C = "ScenarioShakeMapCalculator";
  protected final static boolean D = false;

  //stores the number of sites
  private int numSites;
  //gets the current site being processed
  private int currentSiteBeingProcessed;

  //the propagation effect object
  private PropagationEffect propagationEffect;

  /**
   * class constructor that receives the propagation effect from the outside source and sets
   * as its own propagation.
   * Currently used in the server side so as to set the Propagation from the application to the
   * server , where we create a new instance of the propagationEffect.
   * @param propEffect: PropagationEffect Object.
   */
  public ScenarioShakeMapCalculator(PropagationEffect propEffect) {
    propagationEffect = propEffect;
  }


  //class default constructor
  public ScenarioShakeMapCalculator() {
    propagationEffect = new PropagationEffect();
  }


  /**
   * Does the ScenarioShakeMap data calculation on the user's local system.
   * This function computes a Scenario ShakeMap Data for the given Region, IMR, and ERF.
   * The computed  data in the form of X, Y and Z is place XYZ_DataSetAPI object.
   * @param selectedAttenRels : ArrayList for the selected AttenuationRelationships
   * @param attenRelWts : Relative Wts for all the selected AttenuationRelationship models.
   * @param griddedRegionSites : Gridded Region Object
   * @param rupture : selected EarthquakeRupture Object.
   * @param isProbAtIML : if true the prob at the specified IML value (next param) will
   * be computed; if false the IML at the specified Prob value (next param) will be computed.
   * @param value : the IML or Prob to compute the map for.
   * @returns the XYZ_DataSetAPI  : ArbDiscretized XYZ dataset
   */
  public XYZ_DataSetAPI getScenarioShakeMapData(ArrayList selectedAttenRels, ArrayList attenRelWts,
      SitesInGriddedRectangularRegion griddedRegionSites,EqkRupture rupture,
      boolean isProbAtIML,double value) throws ParameterException, RegionConstraintException {

    numSites = griddedRegionSites.getNumGridLocs();

    //instance of the XYZ dataSet.
    XYZ_DataSetAPI xyzDataSet =null;

    //setting the rupture inside the propagationeffect.
    propagationEffect.setEqkRupture(rupture);

    // get the selected attenuationRelation array size.
    int size = selectedAttenRels.size();

    /**
     * Based on the selected IML@prob or Prob@IML the corresponding value is
     * set in the selected IMR's
     */
    for(int i=0;i<size;++i){ //iterate over all the selected AttenuationRelationships
      AttenuationRelationship attenRel = (AttenuationRelationship)selectedAttenRels.get(i);
      if(isProbAtIML) //if Prob@IML set the Intensity Measure Level
        attenRel.setIntensityMeasureLevel(new Double(value));
      else{
        try{ //if IML@Prob set the Exceed Prob param for the Attenuation.
          attenRel.setExceedProb(value);
        }catch(ParameterException e){
          throw new ParameterException(e.getMessage());
        }
      }
    }

    ArrayList zVals;
    ArrayList sumZVals = new ArrayList();
    //store the sum of the averaged value of all the selected AttenRel
    double attenRelsAvgValForSite = 0.0;
    //iterating over all the sites and averaging the values for all AttenRels
    for(int k=0;k<numSites;++k){
      //saves the number of the current site being processed
      currentSiteBeingProcessed = k+1;
      //for each site initializing it to 0.0
      attenRelsAvgValForSite = 0.0;
      //getting one site at a time
      Site site = griddedRegionSites.getSite(k);

      //setting the site in the PropagationEffect
      propagationEffect.setSite(site);
/*
      // write out useful info
      System.out.print((float)site.getLocation().getLatitude()+"\t"+
                       (float)site.getLocation().getLongitude()+"\t"+
                       ((Double)site.getParameter(AttenuationRelationship.VS30_NAME).getValue()).floatValue()+"\t"+
                       site.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME).getValue()+"\t"+
                       site.getParameter(SCEMY_1997_AttenRel.SITE_TYPE_NAME).getValue()+"\t"+
                       site.getParameter(CB_2003_AttenRel.SITE_TYPE_NAME).getValue()+"\t"+
                       site.getParameter(ShakeMap_2003_AttenRel.WILLS_SITE_NAME).getValue()+"\t"+
                       ((Double)site.getParameter(Field_2000_AttenRel.BASIN_DEPTH_NAME).getValue()).floatValue()+"\t"+
                       propagationEffect.getParamValue(DistanceRupParameter.NAME)+"\t"+
                       propagationEffect.getParamValue(DistanceSeisParameter.NAME)+"\t"+
                       propagationEffect.getParamValue(DistanceJBParameter.NAME)+"\n");
*/
      //iterating overe all the selected attenautionRelationShips and getting the XYZ data for them
      for(int i=0;i<size;++i){
        AttenuationRelationship attenRel = (AttenuationRelationship)selectedAttenRels.get(i);
        //getting the calculated value for the scenarioshakemap for the i-th attenRel and for this site.
        double val= scenarioShakeMapDataCalc(propagationEffect,attenRel,isProbAtIML);

        //multiplying the value for the attenuation with the relative normalised wt for it
        val *= ((Double)attenRelWts.get(i)).doubleValue();
        //adding up all the values obtained from different AttenRel for the site.
        attenRelsAvgValForSite +=val;
      }
      sumZVals.add(new Double(attenRelsAvgValForSite));
    }

    //updating the Z Values for the XYZ data after averaging the values for all selected attenuations.
    xyzDataSet = new ArbDiscretizedXYZ_DataSet(getSitesLat(griddedRegionSites),
        getSitesLon(griddedRegionSites),sumZVals);
    return xyzDataSet;
  }



  /**
   * Does the ScenarioShakeMap data calculation on the server.
   * This function computes a Scenario ShakeMap Data for the given Region, IMR, and ERF.
   * The computed  data in the form of X, Y and Z is place XYZ_DataSetAPI object.
   * The computation is performed by the server to save the processor memory.
   * It opens the connection with the servlet hosted on gravity.usc.edu , which does the
   * calculation  for the it and return back the result to it.
   * @param selectedAttenRels : ArrayList for the selected AttenuationRelationships
   * @param attenRelWts : Relative Wts for all the selected AttenuationRelationship models.
   * @param griddedRegionSites : Gridded Region Object
   * @param rupture : selected EarthquakeRupture Object.
   * @param isProbAtIML : if true the prob at the specified IML value (next param) will
   * be computed; if false the IML at the specified Prob value (next param) will be computed.
   * @param value : the IML or Prob to compute the map for.
   * @returns the String  : Absolute path to ArbDiscretized XYZ dataset file on the server
   */
  public String getScenarioShakeMapDataUsingServer(ArrayList selectedAttenRels, ArrayList attenRelWts,
      String griddedRegionSitesFile,EqkRupture rupture,
      boolean isProbAtIML,double value, String selectedIMT) throws ParameterException {
    ObjectOutputStream outputToServlet = null;
    ObjectInputStream inputToServlet = null;
    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL scenarioshakeMapCalcServlet = new
                             URL("http://gravity.usc.edu/OpenSHA/servlet/ScenarioShakeMapCalcServlet");


      URLConnection servletConnection = scenarioshakeMapCalcServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the ArrayList of the selected AttenuationRelationships
      outputToServlet.writeObject(selectedAttenRels);


      //sending the Absolute weights of the selected AttenRel to the servlet
      outputToServlet.writeObject(attenRelWts);

      //sending the full path to the griddedregion file to the server
      outputToServlet.writeObject(griddedRegionSitesFile);

      //sending the EqkRupture object ( rupture info).
      outputToServlet.writeObject(rupture);

      //sending the Map type ot the servlet, is it Prob@IML or IML@Prob
      outputToServlet.writeObject(new Boolean(isProbAtIML));


      //sending the value of the iml or prob whichever other needs to be computed
      //based on the selection of the IML@prob or Prob@IML
      outputToServlet.writeObject(new Double(value));

      //sending the selected IMT to the server
      outputToServlet.writeObject(selectedIMT);

      //sending the Propagation effect parameter to the servlet
      outputToServlet.writeObject(propagationEffect);


      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      //Absolute path to the XYZ data for the scenarioshake as computed using the servlet
      String xyzDataFile =(String)inputToServlet.readObject();
      if(xyzDataFile.startsWith("Error"))
        throw new RuntimeException(xyzDataFile.substring(6));
      //if(D) System.out.println("Receiving the Input from the Servlet:"+webaddr);
      inputToServlet.close();
      return xyzDataFile;
    }catch(RuntimeException e){
      throw new RuntimeException(e.getMessage());
    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }finally{
      try{
        if(outputToServlet !=null){
          outputToServlet.flush();
          outputToServlet.close();
        }
        if(inputToServlet !=null){
          inputToServlet.close();
        }
      }catch(IOException e){
        e.printStackTrace();
        throw new RuntimeException("Server is down , please try again later");
      }
    }
  }








  /**
   * Gives the ArrayList of Latitudes from the gridded region
   * @param griddedRegionSites
   * @return
   */
  private ArrayList getSitesLat(SitesInGriddedRectangularRegion griddedRegionSites){
    //getting the gridded Locations list iterator
    ListIterator it= griddedRegionSites.getGridLocationsIterator();

    //Adding the Latitudes to the ArrayLists for lats
    ArrayList sitesLat = new ArrayList();
    while(it.hasNext())
      sitesLat.add(new Double(((Location)it.next()).getLatitude()));
   return  sitesLat;
  }

  /**
   * Gives the ArrayList of Longitudes from the gridded region
   * @param griddedRegionSites
   * @return
   */
  private ArrayList getSitesLon(SitesInGriddedRectangularRegion griddedRegionSites){
    //getting the gridded Locations list iterator
     //iterating over the locations iterator in the reverse order to get the Longitudes.
    ListIterator it= griddedRegionSites.getGridLocationsIterator();
    //Adding the Longitudes to the ArrayLists for lons
    ArrayList sitesLon = new ArrayList();
    while(it.hasNext())
      sitesLon.add(new Double(((Location)it.next()).getLongitude()));
    return sitesLon;
  }




  /**
   *
   * @param propagationEffect : Propagation Effect comtaining the site and rupture information.
   * @param imr selected IMR object.
   * @param isProbAtIML : if true the prob at the specified IML value (next param) will
   * be computed; if false the IML at the specified Prob value will be computed.
   * @returns computed value for the exceed Prob or IML based on above argument.
   * @throws ParameterException
   */
  private double scenarioShakeMapDataCalc(PropagationEffect propagationEffect,
      AttenuationRelationship imr,boolean isProbAtIML) throws ParameterException {

    imr.setPropagationEffect(propagationEffect);
    if(D) {
      System.out.println("Selected Site : " +imr.getSite().getLocation().toString());
      System.out.println("--------------");
      System.out.println(imr.getName()+" Params:\n"+imr.getAllParamMetadata().replaceAll(";","\n"));
      System.out.println("--------------\n");
    }
    if(isProbAtIML)
      return imr.getExceedProbability();
    else{
      try{
        return imr.getIML_AtExceedProb();
      }catch(ParameterException e){
        throw new ParameterException(e.getMessage());
      }
    }
  }




  /**
   *
   * @returns the total number of sites in the region
   */
  public int getNumSites(){
    return numSites;
  }


  /**
   *
   * @returns the current site being processed
   */
  public int getCurrentSite(){
    return currentSiteBeingProcessed ;
  }

  /**
   *
   * @returns true if calculations for all the sites are done
   * else return false.
   */
  public boolean done(){
   if(currentSiteBeingProcessed == numSites)
     return true;
   return false;
  }

  /**
   *
   * @returns the ParameterList for the Propagation Effect.
   * But later we will be creating a adjustable parameterlist for the calculator.
   */
  public ParameterList getAdjustableParams(){
    return propagationEffect.getAdjustableParameterList();
  }

}
