package org.scec.sha.calc;

import java.util.*;
import java.net.*;
import java.io.*;

import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Location;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.sha.earthquake.ERF_API;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.exceptions.ParameterException;
import org.scec.sha.param.PropagationEffect;

/**
 * <p>Title: ScenarioShakeMapCalculatorWithPropagationEffect</p>
 * <p>Description: This class calculates the Scenario Shake Map Data using the
 * based on the PropagationEffectParam input parameters imr, site and eqkRupforecast</p>
 * @author : Nitin Gupta
 * @created May 19,2004
 * @version 1.0
 */

public class ScenarioShakeMapCalculatorWithPropagationEffect {

  protected final static String C = "ScenarioShakeMapCalculatorWithPropagationEffect";
  protected final static boolean D = false;

  //stores the number of sites
  private int numSites;
  //gets the current site being processed
  private int currentSiteBeingProcessed;


  public ScenarioShakeMapCalculatorWithPropagationEffect() {
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
      SitesInGriddedRegion griddedRegionSites,EqkRupture rupture,
      boolean isProbAtIML,double value) throws ParameterException {

    numSites = griddedRegionSites.getNumGridLocs();

    //instance of the XYZ dataSet.
    XYZ_DataSetAPI xyzDataSet =null;
    //creating the propagation effect object
    PropagationEffect propagationEffect = new PropagationEffect();

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
      //iterating overe all the selected attenautionRelationShips and getting the XYZ data for them
      for(int i=0;i<size;++i){
        AttenuationRelationship attenRel = (AttenuationRelationship)selectedAttenRels.get(i);
        double val= scenarioShakeMapDataCalc(propagationEffect,attenRel,isProbAtIML);

        //multiplying the value for the attenuation with the relative normalised wt for it
        if(!isProbAtIML)//it is IML at Prob then we don't need to take the log before averaging the values
          val *= ((Double)attenRelWts.get(i)).doubleValue();
        else{ //If Prob@IML then take the log before we average the values out
            val = Math.log(val);
            val *= ((Double)attenRelWts.get(i)).doubleValue();
          }
       attenRelsAvgValForSite +=val;
      }
      sumZVals.add(new Double(attenRelsAvgValForSite));
    }

    if(isProbAtIML){ //if Prob@IML then do Math.exp for the Prob's
      int size1 = sumZVals.size();
      for (int j = 0; j < size1; ++j){
        double tempVal = Math.exp(((Double)sumZVals.get(j)).doubleValue());
        sumZVals.set(j,new Double(tempVal));
      }
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
   * @returns the XYZ_DataSetAPI  : ArbDiscretized XYZ dataset
   */
  public XYZ_DataSetAPI getScenarioShakeMapDataUsingServer(ArrayList selectedAttenRels, ArrayList attenRelWts,
      SitesInGriddedRegion griddedRegionSites,EqkRupture rupture,
      boolean isProbAtIML,double value) throws ParameterException {

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

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending the ArrayList of the selected AttenuationRelationships
      outputToServlet.writeObject(selectedAttenRels);


      //sending the Absolute weights of the selected AttenRel to the servlet
      outputToServlet.writeObject(attenRelWts);

      //sending the gridded region object( containing the info. about the sites).
      outputToServlet.writeObject(griddedRegionSites);

      //sending the EqkRupture object ( rupture info).
      outputToServlet.writeObject(rupture);

      //sending the Map type ot the servlet, is it Prob@IML or IML@Prob
      outputToServlet.writeObject(new Boolean(isProbAtIML));


      //sending the value of the iml or prob whichever other needs to be computed
      //based on the selection of the IML@prob or Prob@IML
      outputToServlet.writeObject(new Double(value));

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      //XYZ data for the scenarioshake as computed using the servlet
      XYZ_DataSetAPI xyzData =(XYZ_DataSetAPI)inputToServlet.readObject();
      //if(D) System.out.println("Receiving the Input from the Servlet:"+webaddr);
      inputToServlet.close();
      return xyzData;
    }catch (Exception e) {
      throw new RuntimeException("Server is down , please try again later");
    }
  }








  /**
   * Gives the ArrayList of Latitudes from the gridded region
   * @param griddedRegionSites
   * @return
   */
  private ArrayList getSitesLat(SitesInGriddedRegion griddedRegionSites){
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
  private ArrayList getSitesLon(SitesInGriddedRegion griddedRegionSites){
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

}