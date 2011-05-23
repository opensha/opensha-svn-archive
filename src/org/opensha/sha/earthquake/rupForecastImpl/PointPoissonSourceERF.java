/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.earthquake.rupForecastImpl;

import java.util.ArrayList;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.param.MagFreqDistParameter;


/**
 * <p>Title: PointPoissonSourceERF</p>
 * <p>Description: This ERF creates a single PointEqkSource
 * for the following user-defined parameters:  </p>
 * <UL>
 * <LI>mag-freq-dist
 * <LI>source location (lat, lon, depth params; should be one LocationParam)
 * <LI>rake - the rake (in degrees) assigned to all ruptures.
 * <LI>dip - the dip (in degrees) assigned to all rupture surfaces.
 * <LI>timeSpan - the duration of the forecast (in same units as in the magFreqDist)
 * </UL><p>
 * The source is Poissonain, and the timeSpan is in years.
 * @author Ned Field
 * Date : June , 2004
 * @version 1.0
 */

public class PointPoissonSourceERF extends EqkRupForecast{

  //for Debug purposes
  private static String  C = new String("PointPoissonSourceERF");
  private boolean D = true;

  //name for this classs
  public final static String  NAME = "Point Poisson Source ERF";

  // this is the source (only 1 for this ERF)
  private PointEqkSource source;

  //mag-freq dist parameter Name
  public final static String MAG_DIST_PARAM_NAME = "Mag Freq Dist";

  // rake parameter stuff
  public final static String RAKE_PARAM_NAME = "Rake";
  private final static String RAKE_PARAM_INFO = "The rake of the rupture (direction of slip)";
  private final static String RAKE_PARAM_UNITS = "degrees";
  private Double RAKE_PARAM_MIN = new Double(-180);
  private Double RAKE_PARAM_MAX = new Double(180);
  private Double RAKE_PARAM_DEFAULT = new Double(0.0);

  // dip parameter stuff
  public final static String DIP_PARAM_NAME = "Dip";
  private final static String DIP_PARAM_INFO = "The dip of the rupture surface";
  private final static String DIP_PARAM_UNITS = "degrees";
  private Double DIP_PARAM_MIN = new Double(0);
  private Double DIP_PARAM_MAX = new Double(90);
  private Double DIP_PARAM_DEFAULT = new Double(90);

  // the source-location parameters (this should be a location parameter)
  public final static String SRC_LAT_PARAM_NAME = "Source Latitude";
  private final static String SRC_LAT_PARAM_INFO = "Latitude of the point source";
  private final static String SRC_LAT_PARAM_UNITS = "Degrees";
  private Double SRC_LAT_PARAM_MIN = new Double (-90.0);
  private Double SRC_LAT_PARAM_MAX = new Double (90.0);
  private Double SRC_LAT_PARAM_DEFAULT = new Double (35.71);

  public final static String SRC_LON_PARAM_NAME = "Source Longitude";
  private final static String SRC_LON_PARAM_INFO = "Longitude of the point source";
  private final static String SRC_LON_PARAM_UNITS = "Degrees";
  private Double SRC_LON_PARAM_MIN = new Double (-360);
  private Double SRC_LON_PARAM_MAX = new Double (360);
  private Double SRC_LON_PARAM_DEFAULT = new Double (-121.1);

  public final static String SRC_DEPTH_PARAM_NAME = "Source Depth";
  private final static String SRC_DEPTH_PARAM_INFO = "Depth of the point source";
  private final static String SRC_DEPTH_PARAM_UNITS = "km";
  private Double SRC_DEPTH_PARAM_MIN = new Double (0);
  private Double SRC_DEPTH_PARAM_MAX = new Double (50);
  private Double SRC_DEPTH_PARAM_DEFAULT = new Double (7.6);


  // parameter declarations
  MagFreqDistParameter magDistParam;
  DoubleParameter dipParam;
  DoubleParameter rakeParam;
  DoubleParameter srcLatParam;
  DoubleParameter srcLonParam;
  DoubleParameter srcDepthParam;


  /**
   * Constructor for this source (no arguments)
   */
  public PointPoissonSourceERF() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // make the magFreqDistParameter
    ArrayList supportedMagDists=new ArrayList();
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);

    // create the rake param
    rakeParam = new DoubleParameter(RAKE_PARAM_NAME,RAKE_PARAM_MIN,
        RAKE_PARAM_MAX,RAKE_PARAM_UNITS,RAKE_PARAM_DEFAULT);
    rakeParam.setInfo(RAKE_PARAM_INFO);

    // create the rake param
    dipParam = new DoubleParameter(DIP_PARAM_NAME,DIP_PARAM_MIN,
        DIP_PARAM_MAX,DIP_PARAM_UNITS,DIP_PARAM_DEFAULT);
    dipParam.setInfo(DIP_PARAM_INFO);

    // create src lat, lon, & depth param
    srcLatParam = new DoubleParameter(SRC_LAT_PARAM_NAME,SRC_LAT_PARAM_MIN,
        SRC_LAT_PARAM_MAX,SRC_LAT_PARAM_UNITS,SRC_LAT_PARAM_DEFAULT);
    srcLatParam.setInfo(SRC_LAT_PARAM_INFO);
    srcLonParam = new DoubleParameter(SRC_LON_PARAM_NAME,SRC_LON_PARAM_MIN,
        SRC_LON_PARAM_MAX,SRC_LON_PARAM_UNITS,SRC_LON_PARAM_DEFAULT);
    srcLonParam.setInfo(SRC_LON_PARAM_INFO);
    srcDepthParam = new DoubleParameter(SRC_DEPTH_PARAM_NAME,SRC_DEPTH_PARAM_MIN,
        SRC_DEPTH_PARAM_MAX,SRC_DEPTH_PARAM_UNITS,SRC_DEPTH_PARAM_DEFAULT);
    srcDepthParam.setInfo(SRC_DEPTH_PARAM_INFO);

    // add the adjustable parameters to the list
    adjustableParams.addParameter(srcLatParam);
    adjustableParams.addParameter(srcLonParam);
    adjustableParams.addParameter(srcDepthParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(dipParam);
    adjustableParams.addParameter(magDistParam);

    // register the parameters that need to be listened to
    rakeParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
    srcLatParam.addParameterChangeListener(this);
    srcLonParam.addParameterChangeListener(this);
    srcDepthParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
  }



   /**
    * update the source based on the paramters (only if a parameter value has changed)
    */
   public void updateForecast(){
     String S = C + "updateForecast::";

     if(parameterChangeFlag) {

       Location loc = new Location( ((Double)srcLatParam.getValue()).doubleValue(),
                                    ((Double)srcLonParam.getValue()).doubleValue(),
                                    ((Double)srcDepthParam.getValue()).doubleValue());
       source = new PointEqkSource(loc,
                                          (IncrementalMagFreqDist) magDistParam.getValue(),
                                          timeSpan.getDuration(),
                                          ((Double)rakeParam.getValue()).doubleValue(),
                                          ((Double)dipParam.getValue()).doubleValue());
       parameterChangeFlag = false;
     }

     if(D) {
       System.out.println(C+" numSources="+getNumSources());
       System.out.println(C+" numRuptures(0th src)="+getSource(0).getNumRuptures());
       System.out.println(C+" isPoissonian(0th src)="+getSource(0).isSourcePoissonian());
       for(int n=0; n <getSource(0).getNumRuptures(); n++) {
         System.out.println(C+" "+n+"th rup prob="+ getSource(0).getRupture(n).getProbability());
         System.out.println(C+" "+n+"th rup mag="+getSource(0).getRupture(n).getMag());
       }
     }

   }


   /**
    * Return the earhthquake source at index i.   Note that this returns a
    * pointer to the source held internally, so that if any parameters
    * are changed, and this method is called again, the source obtained
    * by any previous call to this method will no longer be valid.
    *
    * @param iSource : index of the desired source (only "0" allowed here).
    *
    * @return Returns the ProbEqkSource at index i
    *
    */
   public ProbEqkSource getSource(int iSource) {

     // we have only one source
    if(iSource!=0)
      throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

    return source;
   }


   /**
    * Returns the number of earthquake sources (always "1" here)
    *
    * @return integer value specifying the number of earthquake sources
    */
   public int getNumSources(){
     return 1;
   }


    /**
     *  This returns a list of sources (contains only one here)
     *
     * @return ArrayList of Prob Earthquake sources
     */
    public ArrayList  getSourceList(){
      ArrayList v =new ArrayList();
      v.add(source);
      return v;
    }


  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
   public String getName(){
     return NAME;
   }
}
