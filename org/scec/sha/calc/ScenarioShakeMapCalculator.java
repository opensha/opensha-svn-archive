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
   * @param griddedRegionSites : Gridded Region Object
   * @param imr : selected IMR object
   * @param rupture : selected EarthquakeRupture Object.
   * @param isProbAtIML : if true the prob at the specified IML value (next param) will
   * be computed; if false the IML at the specified Prob value (next param) will be computed.
   * @param value : the IML or Prob to compute the map for.
   * @returns the Vector  of doubles with the probablity value for each site.
   */
  public Vector getScenarioShakeMapData(SitesInGriddedRegion griddedRegionSites,
                                      AttenuationRelationship imr, EqkRupture rupture,
                                      boolean isProbAtIML,double value) {

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
        imr.setProbEqkRupture((ProbEqkRupture)rupture);
      } catch (Exception ex) {
        throw new RuntimeException("Rupture not allowed for the chosen IMR: "+ex.getMessage());
      }
      if(isProbAtIML)
        siteValue.add( new Double(imr.getExceedProbability(Math.log(value))));
      else{
        try{
          //if IML@Prob then Prob value should be between 0 and 1.
          if(value<0 || value >1)
            throw new RuntimeException("Probability can only between 0 and 1");
          imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(value));

          siteValue.add(new Double(StrictMath.exp(imr.getIML_AtExceedProb())));
        }catch(RuntimeException e){
          throw new RuntimeException(e.getMessage());
        }
      }
    }
    return siteValue;
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



