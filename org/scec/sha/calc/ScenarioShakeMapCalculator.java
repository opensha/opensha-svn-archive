package org.scec.sha.calc;

import javax.swing.JOptionPane;
import java.util.*;

import org.scec.data.region.SitesInGriddedRegion;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.sha.earthquake.ERF_API;
import org.scec.sha.imr.AttenuationRelationship;
/**
 * <p>Title: ScenarioShakeMapCalculator </p>
 * <p>Description: This class calculates the Scenario Shake Map Data based on the
 * input parameters imr, site and eqkRupforecast</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field & Nitin Gupta & Vipin Gupta
 * @date Dec 22, 2003
 * @version 1.0
 */

public class ScenarioShakeMapCalculator {

  protected final static String C = "ScenarioShakeMapCalculator";
  protected final static boolean D = false;



  private int currRuptures = -1;
  private int totRuptures=0;
  private int numForecasts=0;



  /**
   * This function computes a Scenario ShakeMap Data for the given Region, IMR, and ERF.
   * The computed  data in the form of X, Y and Z is place XYZ_DataSetAPI object.
   * @param xyzData : In this Object we store the vector for x, y and z values to
   * generate the shakeMap.
   * @param sourceIndex : Selected Source Number of the selected ERF
   * @param ruptureIndex : Selected Rupture Number for the selected source
   * @param griddedRegionSites : Gridded Region Object
   * @param imr : selected IMR object
   * @param eqkRupForecast :selected Earthquake rup forecast
   * @param imlProbToggle : Its value can be true or false, based on selction made by the user.
   * If the user has selected Prob@IML then its value is true, else if the user
   * has selected IML@Prob then its value is false, based on which we generate the required Map.
   * @param imlProbValue: It is either IML or Prob value based on what user has selected.
   * If it is IML@Prob then it is Prob. value else if it is Prob@IML then it is IML value.
   */
  public void getScenarioShakeMapData(XYZ_DataSetAPI xyzData,
                                      int sourceIndex, int ruptureIndex,
                                      SitesInGriddedRegion griddedRegionSites,
                                      AttenuationRelationship imr, ERF_API eqkRupForecast,
                                      boolean imlProbToggle,double imlProbValue) {

    Site site;
    Vector siteLat= new Vector();
    Vector siteLon= new Vector();
    Vector siteValue = new Vector();
    int numSites = griddedRegionSites.getNumGridLocs();
    for(int i=0;i<numSites;++i) {
      site = griddedRegionSites.getSite(i);

      siteLat.add(new Double(site.getLocation().getLatitude()));
      siteLon.add(new Double(site.getLocation().getLongitude()));
      imr.setSite(site);
      // set the ProbEQkRup in the IMR
      try {
        imr.setProbEqkRupture(eqkRupForecast.getRupture(sourceIndex,ruptureIndex));
      } catch (Exception ex) {
        throw new RuntimeException("Rupture not allowed for the chosen IMR: "+ex.getMessage());
      }
      if(imlProbToggle)
        siteValue.add( new Double(imr.getExceedProbability(Math.log(imlProbValue))));
      else{
        imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(imlProbValue));
        try{
          siteValue.add(new Double(StrictMath.exp(imr.getIML_AtExceedProb())));
        }catch(RuntimeException e){
          throw new RuntimeException(e.getMessage());
        }
      }
    }
    xyzData.setXYZ_DataSet(siteLat,siteLon,siteValue);
  }


  public int getCurrRuptures() {
    return this.currRuptures;
  }

  public int getTotRuptures() {
    return this.totRuptures;
  }

  public boolean done() {
    return (currRuptures==totRuptures && (numForecasts==0));
  }

}



