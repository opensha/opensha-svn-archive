package org.scec.sha.calc;



import java.io.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.data.Site;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import java.text.DecimalFormat;
import org.scec.data.function.DiscretizedFuncAPI;

/**
 *
 * <p>Title: CondorHazardMapCalculator.java </p>
 * <p>Description: This class will accept 5
 *   THIS WILL BE USED TO RUN IN CONDOR POOL
       args array will have following order:
       // index 0 - start index for sites
       // index 1 - end index for sites
       // index 2 - GriddedRegion file name (region.dat)
      // index 3 - ERF File Name (stepForecast.dat)
      // index 4 - IMR File Name (shakemap_imr.dat) </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field, Nitin Gupta, Vipin Gupta
 * @date Mar 16, 2004
 * @version 1.0
 */

public class GridHazardMapCalculator
{
  private static boolean D = false;
  // make a array for saving the X values
  private  double [] xValues = { .001, .01, .05, .1, .15, .2, .25, .3, .4, .5,
                             .6, .7, .8, .9, 1, 1.1, 1.2, 1.3, 1.4, 1.5 };
  private static int MAX_DISTANCE = 200;
  private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
  private boolean xLogFlag = true;



  public static void main(String[] args)
  {

  /** THIS WILL BE USED TO RUN IN CONDOR POOL
       args array will have following order:
       // index 0 - start index for sites
       // index 1 - end index for sites
       // index 2 - GriddedRegion file name (region.dat)
      // index 3 - ERF File Name (stepForecast.dat)
      // index 4 - IMR File Name (shakemap_imr.dat)
  */
  try {
    // make a array for saving the X values
    GridHazardMapCalculator calc = new GridHazardMapCalculator();
    calc.getHazardMapCurves(args, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
  } catch (Exception ex) {ex.printStackTrace(); }


}


/**
 * this function generates a set of curves for a region
 *
 * @param urls  addresses to IMR/ Forecast/griddedregion metadat files
 * @param mapParametersInfo Parameters need to regenerate the map
 */
public void getHazardMapCurves(String[] args, int startSiteIndex,
                                int endSiteIndex) {
   try{
     // load the objects from the file
     SitesInGriddedRegion griddedSites = (SitesInGriddedRegion)loadObject(args[2]);
     EqkRupForecast eqkRupForecast = (EqkRupForecast)loadObject(args[3]);
     AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)loadObject(args[4]);

     // now run the hazard map calculations
     HazardCurveCalculator hazCurveCalc=new HazardCurveCalculator();
     hazCurveCalc.setMaxSourceDistance(this.MAX_DISTANCE);
     int numSites = griddedSites.getNumGridLocs();
     int numPoints = xValues.length;
     Site site;
     for(int j=startSiteIndex;j<numSites && j<endSiteIndex;++j){
       site = griddedSites.getSite(j);
       // make and initialize the haz function
       ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
       initX_Values(hazFunction,xValues);
       hazCurveCalc.getHazardCurve(hazFunction,site,imr,eqkRupForecast);
       String lat = decimalFormat.format(site.getLocation().getLatitude());
       String lon = decimalFormat.format(site.getLocation().getLongitude());
       hazFunction = toggleHazFuncLogValues(hazFunction, xValues);

       // write the result to the file
       FileWriter fr = new FileWriter(lat + "_" + lon + ".txt");
       for (int i = 0; i < numPoints; ++i)
         fr.write(hazFunction.getX(i) + " " + hazFunction.getY(i) + "\n");
       fr.close();
     }
   }catch(Exception e){
     e.printStackTrace();
   }
 }


 /**
  * @param fileName File from where object needs to be read
  * @return Object object read from the file
  */
 public Object loadObject(String fileName)
 {
   if(D) System.out.println("fileName="+fileName);
   try {
     FileInputStream fin = new FileInputStream(fileName);
     ObjectInputStream tis = new ObjectInputStream( fin);
     Object obj =  tis.readObject();
     tis.close();
     return obj;
   }catch(Exception e) { e.printStackTrace(); }
   return null;
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


}
