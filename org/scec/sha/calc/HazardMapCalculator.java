package org.scec.sha.calc;


import javax.swing.JFrame;
import javax.swing.JProgressBar;
import java.awt.Rectangle;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.io.*;
import java.util.Iterator;

import org.scec.data.function.*;
import org.scec.data.Site;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.gui.infoTools.*;
import org.scec.data.region.*;


/**
 * <p>Title: HazardMapCalculator </p>
 * <p>Description: This class calculates the Hazard curve based on the
 * input parameters imr, site and eqkRupforecast</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 28, 2002
 * @version 1.0
 */

public class HazardMapCalculator {

  protected final static String C = "HazardMapCalculator";
  protected final static boolean D = false;

  /* maximum permitted distance between fault and site to consider source in
  hazard analysis for that site; this default value is to allow all PEER test
  cases to pass through
  */
  protected double MAX_DISTANCE = 2500;

  private CalcProgressBar progressClass ;

  private DecimalFormat decimalFormat=new DecimalFormat("0.00##");



  /**
   * This sets the maximum distance of sources to be considered in the calculation
   * (as determined by the getMinDistance(Site) method of ProbEqkSource subclasses).
   * Sources more than this distance away are ignored.
   * Default value is 250 km.
   *
   * @param distance: the maximum distance in km
   */
  public void setMaxSourceDistance(double distance) {
    MAX_DISTANCE = distance;
  }

  /**
   * this function determines the hazard curve based on the parameters
   *
   * @param func : it has X values set and result will be returned in this function
   * @param griddedSites  : Sites in the Gridded Region
   * @param imr  :selected IMR object
   * @param eqkRupForecast  : selected Earthquake rup forecast
   * @return
   */
  public void getHazardMapCurves(DiscretizedFuncAPI hazFunction,
                                 SitesInGriddedRegion griddedSites, AttenuationRelationshipAPI imr, EqkRupForecast eqkRupForecast) {

    Site site;
    HazardCurveCalculator hazCurveCalc=new HazardCurveCalculator();
    //creating the directory that stores all the HazardCurves for that region
    boolean success = (new File("tempData")).mkdir();
    int numSites = griddedSites.getNumGridLocs();
    for(int j=0;j<numSites;++j){
      site = griddedSites.getSite(j);
      int numPoints=hazFunction.getNum();
      for(int i=0;i<numPoints;i++)
        hazFunction.set(i,1.0);
      hazCurveCalc.getHazardCurve(hazFunction,site,imr,eqkRupForecast);
      String lat = decimalFormat.format(site.getLocation().getLatitude());
      String lon = decimalFormat.format(site.getLocation().getLongitude());

      try{
        if(success){
          FileWriter fr = new FileWriter("tempData/"+lat+"_"+lon+".txt");
          for(int i=0;i<numPoints;++i) {
            //System.out.println(hazFunction.getY(i));
            //System.out.println(hazFunction.getX(i)+" "+new Double(hazFunction.getY(i))+"\n");
            fr.write(hazFunction.getX(i)+" "+hazFunction.getY(i)+"\n");
          }

          fr.close();
        }
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }

}
