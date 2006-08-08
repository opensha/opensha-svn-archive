package org.opensha.sha.calc;

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

import org.opensha.data.function.*;
import org.opensha.data.Site;
import org.opensha.param.DoubleDiscreteParameter;
import org.opensha.sha.imr.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.gui.infoTools.*;
import org.opensha.data.region.*;


/**
 * <p>Title: HazardMapCalculator </p>
 * <p>Description: This class calculates the Hazus data Set curves based on the
 * input parameters imr, site and eqkRupforecast</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 28, 2002
 * @version 1.0
 */

public class HazusMapCalculator {

  protected final static String C = "HazusMapCalculator";
  protected final static boolean D = false;

  /* maximum permitted distance between fault and site to consider source in
  hazard analysis for that site; this default value is to allow all PEER test
  cases to pass through
  */
  protected double MAX_DISTANCE = 2500;

  // boolean for telling whether to show a progress bar
  boolean showProgressBar = true;

  private CalcProgressBar progressClass ;
  private static final double hazusTimePeriod =50;
  private DecimalFormat decimalFormat=new DecimalFormat("0.000000##");
  // directory where all the hazard map data sets will be saved
  public static final String DATASETS_PATH = "HazusMapDataSets/";
  // flag to indicate whether this IMT requires X values to be in log
  boolean xLogFlag = true;
  // name of the new directory for this data set
  private String newDir;


  private double[] returnPd = {100, 250,500,750,1000,1500,2000,2500};
 
  
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
  public void getHazardMapCurves(SitesInGriddedRectangularRegion griddedSites,
                                 AttenuationRelationshipAPI imr,
                                 EqkRupForecast eqkRupForecast,
                                 String mapParametersInfo) {

    // get the number of data sets presently in directory
    File mainDir = new File(this.DATASETS_PATH);

    if(!mainDir.isDirectory()) { // if main directory does not exist
      boolean success = (new File(DATASETS_PATH)).mkdir();
      newDir=  DATASETS_PATH+"Set-1";
    }
    else {
      if(mainDir.list()!=null) { // if there are various data sets in directory
        int numDataSets = mainDir.list().length;
        newDir=  DATASETS_PATH+"Set-"+(numDataSets+1);
      } else {// if main directory is there but it is empty
        newDir=  DATASETS_PATH+"Set-1";
      }
    }
    //creating a new directory that stores all the HazardCurves for that region
    boolean success = (new File(newDir)).mkdir();
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
    calculate(griddedSites, imr, eqkRupForecast);
  }

  /**
   * this function determines the hazard curve based on the parameters
   *
   * @param dirName : Directory name for this new data set
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
  public void getHazardMapCurves(String dirName,
                                 SitesInGriddedRectangularRegion griddedSites,
                                 AttenuationRelationshipAPI imr,
                                 EqkRupForecast eqkRupForecast,
                                 String mapParametersInfo) {

    File mainDir = new File(this.DATASETS_PATH);
    if(!mainDir.isDirectory()) { // if main directory does not exist
      boolean success = (new File(DATASETS_PATH)).mkdir();
      // make the metadata.data and sites.data files
      try{
        FileWriter fr = new FileWriter(mainDir+"/metadata.dat");
        fr.write(mapParametersInfo+"\n");
        fr.close();
        fr=new FileWriter(mainDir+"/sites.dat");
        fr.write(griddedSites.getMinLat()+" "+griddedSites.getMaxLat()+" "+
                 griddedSites.getGridSpacing()+"\n"+griddedSites.getMinLon()+" "+
                 griddedSites.getMaxLon()+" "+ griddedSites.getGridSpacing()+"\n");
        fr.close();
      }catch(IOException ee){
        ee.printStackTrace();
      }
    }
    newDir = DATASETS_PATH+dirName;
    boolean success = (new File(DATASETS_PATH+dirName)).mkdir();

    calculate(griddedSites, imr, eqkRupForecast);
  }



  /**
   * function to compute hazard curves and make the lat/lon files
   * @param imtLogFlag
   * @param xValues
   * @param griddedSites
   * @param imr
   * @param eqkRupForecast
   * @param mapParametersInfo
   */
  private void calculate( SitesInGriddedRectangularRegion griddedSites,
                          AttenuationRelationshipAPI imr,
                          EqkRupForecast eqkRupForecast) {
    
    try{
      for(int retPdIndex =0;retPdIndex<returnPd.length;++retPdIndex){
    	    FileWriter fw = new FileWriter(this.DATASETS_PATH+newDir+"/"+returnPd[retPdIndex]+".txt");
    	    fw.write("# Column Info: Lat,Lon,PGA,PGV,SA-0.3,SA-1\n");
    	    int numSites = griddedSites.getNumGridLocs();
    	    double prob = 1-Math.exp(-(1/returnPd[retPdIndex])*eqkRupForecast.getTimeSpan().getDuration());
    	    for(int i=0;i<numSites;++i){
    	    	    Site site = griddedSites.getSite(i);
    	     	String lat = decimalFormat.format(site.getLocation().getLatitude());
    	        String lon = decimalFormat.format(site.getLocation().getLongitude());
    	    		//Going over all IMT's calculates IML value for each site.
    	    		imr.	setIntensityMeasure(AttenuationRelationship.PGA_NAME);
    	    		double pga_iml = imr.getIML_AtExceedProb(prob);
    	    		imr.setIntensityMeasure(AttenuationRelationship.SA_NAME);
    	    	    ((DoubleDiscreteParameter)imr.getParameter(AttenuationRelationship.PERIOD_NAME)).setValue(new Double(0.3));
    	    	    double sa_03_iml = imr.getIML_AtExceedProb(prob);
    	    	    ((DoubleDiscreteParameter)imr.getParameter(AttenuationRelationship.PERIOD_NAME)).setValue(new Double(1.0));
    	    	    double sa_1_iml = imr.getIML_AtExceedProb(prob);
    	    	    imr.setIntensityMeasure(AttenuationRelationship.PGV_NAME);
    	    	    double pgv_iml = imr.getIML_AtExceedProb(prob);
    	    	    fw.write(lat+" , "+lon+" , "+(float)pga_iml+" , "+
    	    	    		(float)sa_03_iml+" , "+(float)sa_1_iml+" , "+(float)pgv_iml+"\n");
    	    }
      }

    }catch(Exception e){
      e.printStackTrace();
    }
    if(this.showProgressBar) progressClass.dispose();
  }



  /**
  * This allows tuning on or off the showing of a progress bar
  * @param show - set as true to show it, or false to not show it
  */
  public void showProgressBar(boolean show) {
    this.showProgressBar=show;
  }

}

