package org.scec.sha.calc;


import java.io.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.data.Site;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;

import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.util.FileUtils;

/**
 *
 * <p>Title: ThreadHazardMapCalculator.java </p>
 * <p>Description: This class will accept 5
 *   THIS WILL BE USED TO RUN IN CONDOR POOL
       args array will have following order:
       // index 0 - start index for sites
       // index 1 - end index for sites
       // index 2 - GriddedRegion file name (region.dat)
      // index 3 - ERF File Name (stepForecast.dat)
      // index 4 - IMR File Name (shakemap_imr.dat) </p>
 * @author Ned Field, Nitin Gupta, Vipin Gupta
 * @date Mar 16, 2004
 * @version 1.0
 */

public class ThreadHazardMapCalculator
{
  private static boolean D = false;
  // make a array for saving the X values
  private static   double [] xValues = { .001, .01, .05, .1, .15, .2, .25, .3, .4, .5,
                             .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5 };
  private static int MAX_DISTANCE = 200;
  private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
  private boolean xLogFlag = true;

  private static int numPoints = xValues.length;
  // now run the hazard map calculations
  HazardCurveCalculator hazCurveCalc=new HazardCurveCalculator();

  public static void main(String[] args)
  {

  /** THIS WILL BE USED TO RUN IN CONDOR POOL
       args array will have following order:
       // index 1 - GriddedRegion file name (region.dat)
      // index 2 - ERF File Name (stepForecast.dat)
      // index 0 - IMR File Name (shakemap_imr.dat)
  */
  try {
    // make a array for saving the X values
    ThreadHazardMapCalculator calc = new ThreadHazardMapCalculator();
    calc.getHazardMapCurves(args);
  } catch (Exception ex) {ex.printStackTrace(); }


}



/**
 * this method generates the threads that run 100 sites at a time
 * and each thread will create the Hazard Curve for that site.
 * @param args :Command Line arguments for the ERF, IMR and Region
 */
public void getHazardMapCurves(String[] args) {
  try{

    SitesInGriddedRegion griddedSites = (SitesInGriddedRegion)FileUtils.loadObject(args[1]);
    hazCurveCalc.setMaxSourceDistance(this.MAX_DISTANCE);
    int numSites = griddedSites.getNumGridLocs();
    for(int j=0;j<numSites;j +=100){
      int endIndex = j+100;
      if(endIndex >=numSites)
        endIndex = numSites;
      Thread t = new Thread(new HazardCurvesGenerator(args,j,endIndex));
      t.start();
    }
  }catch(Exception e){
    e.printStackTrace();
  }
}




 /**
  * set x values in log space for Hazard Function to be passed to IMR
  * if the selected IMT are SA , PGA or PGV
  * It accepts 1 parameters
  *
  * @param originalFunc :  this is the function with X values set
  */
 private void initX_Values(DiscretizedFuncAPI arb, double[] xValues) {
   // take log only if it is PGA, PGV or SA
   if (this.xLogFlag) {
     for (int i = 0; i < xValues.length; ++i)
       arb.set(Math.log(xValues[i]), 1);
   }
   else
     throw new RuntimeException("Unsupported IMT");
 }

 /**
  * set x values back from the log space to the original linear values
  * for Hazard Function after completion of the Hazard Calculations
  * if the selected IMT are SA , PGA or PGV
  * It accepts 1 parameters
  *
  * @param hazFunction :  this is the function with X values set
  */
 private ArbitrarilyDiscretizedFunc toggleHazFuncLogValues(
     ArbitrarilyDiscretizedFunc hazFunc, double[] xValues) {
   int numPoints = hazFunc.getNum();
   DiscretizedFuncAPI tempFunc = hazFunc.deepClone();
   hazFunc = new ArbitrarilyDiscretizedFunc();
   // take log only if it is PGA, PGV or SA
   if (this.xLogFlag) {
     for (int i = 0; i < numPoints; ++i)
       hazFunc.set(xValues[i], tempFunc.getY(i));
     return hazFunc;
   }
   else
     throw new RuntimeException("Unsupported IMT");
 }

 private class HazardCurvesGenerator implements Runnable{

   private EqkRupForecast erfObj;
   private AttenuationRelationshipAPI imrObj;
   private SitesInGriddedRegion regionObj;
   private Site site;
   private int startIndex;
   private int endIndex;
   HazardCurvesGenerator(String[] args,int startIndex, int endIndex){
     // load the objects from the file
     regionObj = (SitesInGriddedRegion)FileUtils.loadObject(args[1]);
     erfObj = (EqkRupForecast)FileUtils.loadObject(args[2]);
     imrObj = (AttenuationRelationshipAPI)FileUtils.loadObject(args[0]);

     this.startIndex = startIndex;
     this.endIndex = endIndex;
   }

   public void run(){
     Site site = null;
     Calendar calendar = Calendar.getInstance();
     String datetime = new String(calendar.get(Calendar.YEAR) + "-" +
                                 (calendar.get(Calendar.MONTH) + 1) + "-" +
                                 calendar.get(Calendar.DAY_OF_MONTH) + "  " +
                                 calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                                 calendar.get(Calendar.MINUTE) + ":" +
                                 calendar.get(Calendar.SECOND));
     try{
       FileWriter fw = new FileWriter("ThreadTime.txt", true);
       fw.write("Thread for : "+startIndex+"-"+endIndex+" started at: "+datetime+"\n");
       for(int j=startIndex;j<endIndex;++j){
         site = regionObj.getSite(j);
         // make and initialize the haz function
         ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
         initX_Values(hazFunction,xValues);
         hazCurveCalc.getHazardCurve(hazFunction,site,imrObj,erfObj);
         String lat = decimalFormat.format(site.getLocation().getLatitude());
         String lon = decimalFormat.format(site.getLocation().getLongitude());
         hazFunction = toggleHazFuncLogValues(hazFunction, xValues);

         // write the result to the file
         FileWriter fr = new FileWriter(lat + "_" + lon + ".txt");
         for (int i = 0; i < numPoints; ++i)
           fr.write(hazFunction.getX(i) + " " + hazFunction.getY(i) + "\n");
         fr.close();
       }
       calendar = Calendar.getInstance();
       datetime = new String(calendar.get(Calendar.YEAR) + "-" +
                             (calendar.get(Calendar.MONTH) + 1) + "-" +
                             calendar.get(Calendar.DAY_OF_MONTH) + "  " +
                             calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                             calendar.get(Calendar.MINUTE) + ":" +
                             calendar.get(Calendar.SECOND));
       fw.write("Thread for : "+startIndex+"-"+endIndex+" finished at: "+datetime+"\n");
       fw.close();
     }catch(Exception e){
       e.printStackTrace();
     }
   }

 }

}