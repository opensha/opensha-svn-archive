package org.scec.sha.calc;

import java.util.*;

import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Location;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.exceptions.ParameterException;


/**
 * <p>Title: ScenarioShakeMapCalculator_Old </p>
 * <p>Description: This class calculates the Scenario Shake Map Data based on the
 * input parameters imr, site and eqkRupforecast</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field & Nitin Gupta & Vipin Gupta
 * @date Dec 22, 2003
 * @version 1.0
 */

public class ScenarioShakeMapCalculator_Old {

  protected final static String C = "ScenarioShakeMapCalculator_Old";
  protected final static boolean D = true;



  /**
   * This function computes a Scenario ShakeMap Data for the given Region, IMR, and ERF.
   * The computed  data in the form of X, Y and Z is place XYZ_DataSetAPI object.
   * @param griddedRegionSites : Gridded Region Object
   * @param imr : selected IMR object
   * @param rupture : selected EarthquakeRupture Object.
   * @param isProbAtIML : if true the prob at the specified IML value (next param) will
   * be computed; if false the IML at the specified Prob value (next param) will be computed.
   * @param value : the IML or Prob to compute the map for.
   * @returns the XYZ_DataSetAPI  : ArbDiscretized XYZ dataset
   */
  public XYZ_DataSetAPI getScenarioShakeMapData(SitesInGriddedRegion griddedRegionSites,
                                      AttenuationRelationship imr, EqkRupture rupture,
                                      boolean isProbAtIML,double value) throws ParameterException {

    //get the list of the Latitudes
    ArrayList sitesLat = getSitesLat(griddedRegionSites);
    //get the list of the Longitudes
    ArrayList sitesLon = getSitesLon(griddedRegionSites);


    return scenarioShakeMapDataCalc(griddedRegionSites,imr,rupture,isProbAtIML, value,
                                    sitesLat,sitesLon);

  }

  /**
   * Gives the ArrayList of Latitudes from the gridded region
   * @param griddedRegionSites
   * @return
   */
  protected ArrayList getSitesLat(SitesInGriddedRegion griddedRegionSites){
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
  protected ArrayList getSitesLon(SitesInGriddedRegion griddedRegionSites){
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


    //get the list of the Latitudes
    ArrayList sitesLat = getSitesLat(griddedRegionSites);
    //get the list of the Longitudes
    ArrayList sitesLon = getSitesLon(griddedRegionSites);
    //instance of the XYZ dataSet.
    XYZ_DataSetAPI xyzDataSet =null;

    // get the selected IMR
    int size = selectedAttenRels.size();
    ArrayList zVals;
    ArrayList sumZVals = null;
    //iterating overe all the selected attenautionRelationShips and getting the XYZ data for them
    for(int i=0;i<size;++i){
      xyzDataSet = scenarioShakeMapDataCalc(griddedRegionSites,(AttenuationRelationship)selectedAttenRels.get(i),
      rupture,isProbAtIML,value, sitesLat,sitesLon);
      //getting the Z Value from the XYZ data set
      zVals = xyzDataSet.getZ_DataSet();
      int size1 = zVals.size();
      //multiplying the zValue for the attenuation with the relative normalised wt for it
      if(!isProbAtIML){//it is IML at Prob then we don't need to take the log before averaging the values
      for(int j=0;j<size1;++j)
        zVals.set(j,new Double(((Double)zVals.get(j)).doubleValue()*((Double)attenRelWts.get(i)).doubleValue()));
      }
      else{ //If Prob@IML then take the log before we average the values out
        for(int j=0;j<size1;++j){
          double tempVal = Math.log(((Double)zVals.get(j)).doubleValue());
          zVals.set(j,new Double(tempVal*((Double)attenRelWts.get(i)).doubleValue()));
        }
      }
      //adding the Z Values for all the Attenuation Relationships together.
      if(sumZVals == null)
        sumZVals =zVals;
      else {
        size1 = sumZVals.size();
        for(int j=0;j<size1;++j)
          sumZVals.set(j,new Double(((Double)sumZVals.get(j)).doubleValue() + ((Double)zVals.get(j)).doubleValue()));
      }
    }
    if(isProbAtIML){ //if Prob@IML then do Math.exp for the Prob's
      int size1 = sumZVals.size();
      for (int j = 0; j < size1; ++j){
        double tempVal = Math.exp(((Double)sumZVals.get(j)).doubleValue());
        sumZVals.set(j,new Double(tempVal));
      }
    }
    //updating the Z Values for the XYZ data after averaging the values for all selected attenuations.
    xyzDataSet.setXYZ_DataSet(xyzDataSet.getX_DataSet(),xyzDataSet.getY_DataSet(),sumZVals);
    return xyzDataSet;
  }




  /**
   *
   * This function computes a Scenario ShakeMap Data for the given Region, IMR, and ERF.
   * The computed  data in the form of X, Y and Z is place XYZ_DataSetAPI object.
   * @param griddedRegionSites : Gridded Region Object
   * @param imr : selected IMR object
   * @param rupture : selected EarthquakeRupture Object.
   * @param isProbAtIML : if true the prob at the specified IML value (next param) will
   * be computed; if false the IML at the specified Prob value (next param) will be computed.
   * @param value : the IML or Prob to compute the map for.
   * @param sitesLat : ArrayList containing  gridded Locations Latitudes Values
   * @param sitesLon : ArrayList containing  gridded Locations Longitudes Values
   * @returns the XYZ_DataSetAPI  : ArbDiscretized XYZ dataset
   * @throws ParameterException
   */
  private XYZ_DataSetAPI scenarioShakeMapDataCalc(SitesInGriddedRegion griddedRegionSites,
                                      AttenuationRelationship imr, EqkRupture rupture,
                                      boolean isProbAtIML,double value,ArrayList sitesLat,
    ArrayList sitesLon) throws ParameterException {


    XYZ_DataSetAPI xyzDataSet ;
    Site site;
    int numSites = griddedRegionSites.getNumGridLocs();
    //Declaring the arrayList instance to store the site value for each site
    //ArrayList containing IML or Prob value for each site
    ArrayList sitesValue = new ArrayList();

    // set the ProbEQkRup in the IMR
    try {
      imr.setEqkRupture(rupture);
    } catch (ParameterException ex) {
      throw new ParameterException("Rupture not allowed for the chosen IMR: "+ex.getMessage());
    }

    for(int i=0;i<numSites;++i) {
      site = griddedRegionSites.getSite(i);
      imr.setSite(site);
      if(isProbAtIML)
        sitesValue.add(new Double(imr.getExceedProbability(value)));
      else{
        try{
          //if IML@Prob then Prob value should be between 0 and 1.
          //if(value<0 || value >1)
            //throw new ParameterException("Probability can only between 0 and 1");
          //imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(value));
          sitesValue.add(new Double(imr.getIML_AtExceedProb(value)));
        }catch(ParameterException e){
          throw new ParameterException(e.getMessage());
        }
      }
    }
    xyzDataSet = new ArbDiscretizedXYZ_DataSet(sitesLat,sitesLon,sitesValue);
    return xyzDataSet;
  }

}



