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
  // directory where all the hazard map data sets will be saved
  private static final String DATASETS_PATH = "HazardMapDataSets";



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
     * @param imtLogFlag: Checks if the selected IMT is SA, PGA pr PGV, so that we can revert the
     * the Log X values of the Hazard func values back to the original values, before writing to the file
     * for each site.
     * @param hazFunction : it has X values set and result will be returned in this function
     * @param site  : site parameter
     * @param imr  :selected IMR object
     * @param eqkRupForecast  : selected Earthquake rup forecast
     * @param mapParametersInfo  : Parameters in String form used to generate the map
     * @return
   */
  public void getHazardMapCurves(boolean imtLogFlag,DiscretizedFuncAPI hazFunction,
                                 SitesInGriddedRegion griddedSites,
                                 AttenuationRelationshipAPI imr,
                                 EqkRupForecast eqkRupForecast,
                                 String mapParametersInfo) {

    Site site;
    String newDir;
    HazardCurveCalculator hazCurveCalc=new HazardCurveCalculator();
    // get the number of data sets presently in directory
    File mainDir = new File(this.DATASETS_PATH);

    if(!mainDir.isDirectory()) { // if main directory does not exist
      boolean success = (new File(DATASETS_PATH)).mkdir();
      newDir=  DATASETS_PATH+"/1";
    }
    else {
      if(mainDir.list()!=null) { // if there are various data sets in directory
        int numDataSets = mainDir.list().length;
        newDir=  DATASETS_PATH+"/"+(numDataSets+1);
      } else {// if main directory is there but it is empty
        newDir=  DATASETS_PATH+"/1";
      }
    }

    //creating a new directory that stores all the HazardCurves for that region
    boolean success = (new File(newDir)).mkdir();
    int numSites = griddedSites.getNumGridLocs();
    for(int j=0;j<numSites;++j){
      site = griddedSites.getSite(j);
      int numPoints=hazFunction.getNum();

     /*set x values back from the log space to the original linear values
       for Hazard Function after completion of the Hazard Calculations
       if the selected IMT are SA , PGA or PGV*/
      for(int i=0;i<numPoints;i++)
        hazFunction.set(i,1.0);
      hazCurveCalc.getHazardCurve(hazFunction,site,imr,eqkRupForecast);
      String lat = decimalFormat.format(site.getLocation().getLatitude());
      String lon = decimalFormat.format(site.getLocation().getLongitude());
      if(imtLogFlag)
        for(int i=0;i<hazFunction.getNum();++i)
          Math.exp(hazFunction.getX(i));
      try{
        if(success){
          FileWriter fr = new FileWriter(newDir+"/"+lat+"_"+lon+".txt");
          for(int i=0;i<numPoints;++i)
            fr.write(hazFunction.getX(i)+" "+hazFunction.getY(i)+"\n");
          fr.close();
        }
      }catch(IOException e){
        e.printStackTrace();
      }
    }

   // make the metadata.data and sites.data files
    try{
      FileWriter fr = new FileWriter(newDir+"/metadata.dat");
      fr.write(mapParametersInfo+"\n");
      fr.close();
      fr=new FileWriter(newDir+"/sites.dat");
      fr.write(griddedSites.getMinLat()+" "+griddedSites.getMaxLat()+" "+
               griddedSites.getGridSpacing()+"\n"+griddedSites.getMinLon()+" "+
               griddedSites.getMaxLon()+" "+ griddedSites.getGridSpacing()+"\n");
      fr.close();
    }catch(IOException ee){
      ee.printStackTrace();
    }

  }

}
