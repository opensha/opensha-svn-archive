package org.scec.sha.gui.controls;

import java.util.*;

import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.sha.gui.beans.*;
import org.scec.sha.earthquake.rupForecastImpl.SimpleFaultRuptureERF;
import org.scec.calc.magScalingRelations.magScalingRelImpl.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.param.*;
import org.scec.sha.param.editor.gui.SimpleFaultParameterEditorPanel;
import org.scec.sha.param.editor.MagFreqDistParameterEditor;
import org.scec.sha.param.*;
import org.scec.sha.magdist.SingleMagFreqDist;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.data.Location;
import org.scec.data.Direction;
import org.scec.calc.RelativeLocation;
import org.scec.sha.fault.FaultTrace;

/**
 * <p>Title: PuenteHillsScenarioControlPanel</p>
 * <p>Description: Sets the param value to replicate the official scenario shakemap
 * for the Puente Hill Scenario (http://www.trinet.org/shake/Puente_Hills_se)</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class PuenteHillsScenarioControlPanel {

  //for debugging
  protected final static boolean D = true;


  private EqkRupSelectorGuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private SitesInGriddedRegionGuiBean regionGuiBean;
  private MapGuiBean mapGuiBean;
  private IMT_GuiBean imtGuiBean;

  private FaultTrace faultTrace;
  private double aveDipDir;

  //default magnitude.
  private double magnitude = 7.1;

  /**
   * no argument constructor only for the main() method for testinng
   */
  public PuenteHillsScenarioControlPanel() {
    mkFaultTrace();
  }


  /**
   * This make the faultTrace from the segment-data sent by Andreas Plesch via
   * email on 3/10/04:
   */
  private void mkFaultTrace() {

    /* Original Segment Data from Andreas Plesch via email on 3/10/04:

    LA_lon  LA_lat  LA_depth
    -118.12273      33.97087 -2979.44
    -118.33585      34.03440 -2363.39
    -118.25737      34.23723 -14965.3
    -118.04988      34.15721 -14532.7

    CH_lon  CH_lat  CH_depth
    -118.04441      33.89454 -3440.82
    -117.86819      33.89952 -2500
    -117.86678      34.13627 -15474.6
    -118.04490      34.09232 -14485

    SF_lon  SF_lat  SF_depth
    -118.01871      33.93282 -3000
    -118.13920      33.90885 -2750
    -118.14720      34.11061 -15068.3
    -118.01795      34.10093 -14479.3
    */

    Location loc1, loc2, loc3;
    Location finalLoc1, finalLoc2, finalLoc3, finalLoc4 , tempLoc1, tempLoc2, tempLoc3, tempLoc4;
    Direction dir1, dir2;
    double hDist,vDist, dip;
    aveDipDir = 0;

    // find the points at 5 and 17 km depths by projecting each edge down
    // also find the final fault trace (by averaging intermediate points)
    // and the ave-dip direction.

    // LA Segment:
    if (D) System.out.println("\nLA Segment:");
    if (D) System.out.println("LA_lon2  LA_lat2  LA_depth2");

    loc1 = new Location(33.97087, -118.12273, 2.97944);
    loc2 = new Location(34.15721, -118.04988, 14.5327);
    dir1 = RelativeLocation.getDirection(loc1, loc2);
    dip = Math.tan(dir1.getVertDistance()/dir1.getHorzDistance());
    vDist = loc1.getDepth()-5.0;
    hDist = vDist/Math.atan(dip);
    aveDipDir += dir1.getAzimuth();
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());
    tempLoc4 = loc3;
    vDist = loc1.getDepth()-17.0;
    hDist = vDist/Math.atan(dip);
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());

    loc1 = new Location(34.03440, -118.33585, 2.36339);
    loc2 = new Location(34.23723, -118.25737, 14.9653);
    dir1 = RelativeLocation.getDirection(loc1, loc2);
    dip = Math.tan(dir1.getVertDistance()/dir1.getHorzDistance());
    vDist = loc1.getDepth()-5.0;
    hDist = vDist/Math.atan(dip);
    aveDipDir += dir1.getAzimuth();
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());
    finalLoc4 = loc3;
    vDist = loc1.getDepth()-17.0;
    hDist = vDist/Math.atan(dip);
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());

    // CH Segment:
   if (D) System.out.println("\nCH Segment:");
   if (D) System.out.println("CH_lon2  CH_lat2  CH_depth2");

   loc1 = new Location(33.89952, -117.86819, 2.500);
   loc2 = new Location(34.13627, -117.86678, 15.4746);
   dir1 = RelativeLocation.getDirection(loc1, loc2);
   dip = Math.tan(dir1.getVertDistance()/dir1.getHorzDistance());
   vDist = loc1.getDepth()-5.0;
   hDist = vDist/Math.atan(dip);
   aveDipDir += dir1.getAzimuth();
   dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
   loc3 = RelativeLocation.getLocation(loc1,dir2);
   if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());
   finalLoc1 = loc3;
   vDist = loc1.getDepth()-17.0;
   hDist = vDist/Math.atan(dip);
   dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
   loc3 = RelativeLocation.getLocation(loc1,dir2);
   if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());

   loc1 = new Location(33.89454, -118.04441, 3.44082);
   loc2 = new Location(34.09232, -118.04490, 14.485);
   dir1 = RelativeLocation.getDirection(loc1, loc2);
   dip = Math.tan(dir1.getVertDistance()/dir1.getHorzDistance());
   vDist = loc1.getDepth()-5.0;
   hDist = vDist/Math.atan(dip);
   aveDipDir += dir1.getAzimuth();
   dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
   loc3 = RelativeLocation.getLocation(loc1,dir2);
   if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());
   tempLoc1 = loc3;
   vDist = loc1.getDepth()-17.0;
   hDist = vDist/Math.atan(dip);
   dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
   loc3 = RelativeLocation.getLocation(loc1,dir2);
   if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());




    // SF Segment:
    if (D) System.out.println("\nSF Segment:");
    if (D) System.out.println("SF_lon2  SF_lat2  SF_depth2");

    loc1 = new Location(33.93282, -118.01871, 3.000);
    loc2 = new Location(34.10093, -118.01795, 14.4793);
    dir1 = RelativeLocation.getDirection(loc1, loc2);
    dip = Math.tan(dir1.getVertDistance()/dir1.getHorzDistance());
    vDist = loc1.getDepth()-5.0;
    hDist = vDist/Math.atan(dip);
    aveDipDir += dir1.getAzimuth();
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());
    tempLoc2 = loc3;
    vDist = loc1.getDepth()-17.0;
    hDist = vDist/Math.atan(dip);
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());

    loc1 = new Location(33.90885, -118.13920, 2.750);
    loc2 = new Location(34.11061, -118.14720, 15.0683);
    dir1 = RelativeLocation.getDirection(loc1, loc2);
    dip = Math.tan(dir1.getVertDistance()/dir1.getHorzDistance());
    vDist = loc1.getDepth()-5.0;
    hDist = vDist/Math.atan(dip);
    aveDipDir += dir1.getAzimuth();
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());
    tempLoc3 = loc3;
    vDist = loc1.getDepth()-17.0;
    hDist = vDist/Math.atan(dip);
    dir2 = new Direction(vDist, hDist,dir1.getBackAzimuth(),dir1.getAzimuth());
    loc3 = RelativeLocation.getLocation(loc1,dir2);
    if (D) System.out.println((float)loc3.getLongitude()+" "+(float)loc3.getLatitude()+" "+(float)loc3.getDepth());

    finalLoc2 = new Location((tempLoc1.getLatitude()+tempLoc2.getLatitude())/2,
                             (tempLoc1.getLongitude()+tempLoc2.getLongitude())/2,
                             (tempLoc1.getDepth()+tempLoc2.getDepth())/2);
    finalLoc3 = new Location((tempLoc3.getLatitude()+tempLoc4.getLatitude())/2,
                             (tempLoc3.getLongitude()+tempLoc4.getLongitude())/2,
                             (tempLoc3.getDepth()+tempLoc4.getDepth())/2);

    if (D) System.out.println("\nFinal Fault Trace:");
    if (D) System.out.println("final_tr_lat final_tr_lon final_tr_depth");
    if (D) System.out.println((float)finalLoc1.getLatitude()+" "+(float)finalLoc1.getLongitude()+" "+(float)finalLoc1.getDepth());
    if (D) System.out.println((float)finalLoc2.getLatitude()+" "+(float)finalLoc2.getLongitude()+" "+(float)finalLoc2.getDepth());
    if (D) System.out.println((float)finalLoc3.getLatitude()+" "+(float)finalLoc3.getLongitude()+" "+(float)finalLoc3.getDepth());
    if (D) System.out.println((float)finalLoc4.getLatitude()+" "+(float)finalLoc4.getLongitude()+" "+(float)finalLoc4.getDepth());

    faultTrace = new FaultTrace("Puente Hills Fault Trace");
    faultTrace.addLocation(finalLoc1);
    faultTrace.addLocation(finalLoc2);
    faultTrace.addLocation(finalLoc3);
    faultTrace.addLocation(finalLoc4);

    aveDipDir /= 6;

    if (D) System.out.println("\nAveDipDir = "+aveDipDir);

    if (D) System.out.println("\n Trace Length = "+faultTrace.getTraceLength());

    FaultTrace tempTr = new FaultTrace("");
    tempTr.addLocation(finalLoc1);
    tempTr.addLocation(finalLoc2);
    if (D) System.out.println("\n new-merged CH seg Length = "+tempTr.getTraceLength());
    tempTr = new FaultTrace("");
    tempTr.addLocation(finalLoc2);
    tempTr.addLocation(finalLoc3);
    if (D) System.out.println("\n new-merged SF seg Length = "+tempTr.getTraceLength());
    tempTr = new FaultTrace("");
    tempTr.addLocation(finalLoc3);
    tempTr.addLocation(finalLoc4);
    if (D) System.out.println("\n new-merged LA seg Length = "+tempTr.getTraceLength());

    tempTr = new FaultTrace("");
    tempTr.addLocation(finalLoc1);
    tempTr.addLocation(tempLoc1);
    if (D) System.out.println("\n new CH seg Length = "+tempTr.getTraceLength());
    tempTr = new FaultTrace("");
    tempTr.addLocation(tempLoc2);
    tempTr.addLocation(tempLoc3);
    if (D) System.out.println("\n new SF seg Length = "+tempTr.getTraceLength());
    tempTr = new FaultTrace("");
    tempTr.addLocation(tempLoc4);
    tempTr.addLocation(finalLoc4);
    if (D) System.out.println("\n new LA seg Length = "+tempTr.getTraceLength());
  }



  //class default constructor
  /**
   * Accepts 3 params for the EqkRupSelectorGuiBean, IMR_GuiBean, SitesInGriddedRegionGuiBean
   * from the applet.
   * @param erfGuiBean
   * @param imrGuiBean
   * @param regionGuiBean
   * @param MapGuiBean
   * @param IMT_GuiBean
   */
  public PuenteHillsScenarioControlPanel(EqkRupSelectorGuiBean erfGuiBean, IMR_GuiBean imrGuiBean,
      SitesInGriddedRegionGuiBean regionGuiBean, MapGuiBean mapGuiBean, IMT_GuiBean imtGuiBean) {
    //getting the instance for variuos GuiBeans from the applet required to set the
    //default values for the Params for the Puente Hills Scenario.
    this.erfGuiBean = erfGuiBean;
    this.imrGuiBean = imrGuiBean;
    this.regionGuiBean = regionGuiBean;
    this.mapGuiBean = mapGuiBean;
    this.imtGuiBean = imtGuiBean;

    // make the fault trace data
    mkFaultTrace();
  }

  /**
   * Sets the default Parameters in the Application for the Puente Hill Scenario
   */
  public void setParamsForPuenteHillsScenario(){

    //making the ERF Gui Bean Adjustable Param not visible to the user, becuase
    //this control panel will set the values by itself.
    //This is done in the EqkRupSelectorGuiBean
    erfGuiBean.showAllParamsForForecast(false);

    //changing the ERF to SimpleFaultERF
    erfGuiBean.getParameterListEditor().getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setValue(SimpleFaultRuptureERF.NAME);
    erfGuiBean.getParameterListEditor().refreshParamEditor();

    //Getting the instance for the editor that holds all the adjustable params for the selcetd ERF
    ERF_GuiBean erfParamGuiBean =erfGuiBean.getERF_ParamEditor();

    // Set rake value to 90 degrees
    erfParamGuiBean.getParameterList().getParameter(SimpleFaultRuptureERF.RAKE_PARAM_NAME).setValue(new Double(90));


    double dip = 27;
    double depth1=5, depth2=17;

    //getting the instance for the SimpleFaultParameterEditorPanel from the GuiBean to adjust the fault Params
    SimpleFaultParameterEditorPanel faultPanel= erfParamGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
    //creating the Lat vector for the SimpleFaultParameter

    Vector lats = new Vector();
    Vector lons = new Vector();
    for(int i = 0; i<faultTrace.getNumLocations(); i++) {
      lats.add(new Double(faultTrace.getLocationAt(i).getLatitude()));
      lons.add(new Double(faultTrace.getLocationAt(i).getLongitude()));
    }

    //creating the dip vector for the SimpleFaultParameter
    Vector dips = new Vector();
    dips.add(new Double(dip));

    //creating the depth vector for the SimpleFaultParameter
    Vector depths = new Vector();
    depths.add(new Double(depth1));
    depths.add(new Double(depth2));

    //setting the FaultParameterEditor with the default values for Puente Hills Scenario
    faultPanel.setAll(((SimpleFaultParameter)faultPanel.getParameter()).DEFAULT_GRID_SPACING,lats,
                      lons,dips,depths,((SimpleFaultParameter)faultPanel.getParameter()).STIRLING);

    // set the average dip direction
    // use default which is perp to ave strike.
//    faultPanel.setDipDirection(aveDipDir);

    faultPanel.refreshParamEditor();

    //updaing the faultParameter to update the faultSurface
    faultPanel.setEvenlyGriddedSurfaceFromParams();

    //updating the magEditor with the values for the Puente Hills Scenario
    MagFreqDistParameterEditor magEditor = erfParamGuiBean.getMagDistEditor();
    magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
    magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
    magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(magnitude));
    erfParamGuiBean.refreshParamEditor();
    // now have the editor create the magFreqDist
    magEditor.setMagDistFromParams();

    //updating the EQK_RupSelectorGuiBean with the Source and Rupture Index respectively.
    erfGuiBean.setParamsInForecast(0,0);

    //Updating the IMR Gui Bean with the ShakeMap attenuation relationship.
    imrGuiBean.getParameterList().getParameter(imrGuiBean.IMR_PARAM_NAME).setValue(ShakeMap_2003_AttenRel.NAME);
    imrGuiBean.getSelectedIMR_Instance().getParameter(ShakeMap_2003_AttenRel.COMPONENT_NAME).setValue(ShakeMap_2003_AttenRel.COMPONENT_AVE_HORZ);
    imrGuiBean.refreshParamEditor();

    //Updating the SitesInGriddedRegionGuiBean with the Puente Hills resion setting
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LATITUDE).setValue(new Double(33.2));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LATITUDE).setValue(new Double(35.0));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LONGITUDE).setValue(new Double(-119.5));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LONGITUDE).setValue(new Double(-116.18));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.GRID_SPACING).setValue(new Double(.1));
//    regionGuiBean.getParameterList().getParameter(regionGuiBean.GRID_SPACING).setValue(new Double(.016667));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.SITE_PARAM_NAME).setValue(regionGuiBean.SET_SITE_USING_WILLS_SITE_TYPE);

    // Set the imt as PGA
    imtGuiBean.getParameterList().getParameter(imtGuiBean.IMT_PARAM_NAME).setValue(ShakeMap_2003_AttenRel.PGA_NAME);
    imtGuiBean.refreshParamEditor();

    // Set some of the mapping params:
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.GMT_WEBSERVICE_NAME).setValue(new Boolean(false));
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.LOG_PLOT_NAME).setValue(new Boolean(false));
    mapGuiBean.refreshParamEditor();
  }


  public static void main(String[] args) {
    PuenteHillsScenarioControlPanel ph = new PuenteHillsScenarioControlPanel();

  }
}


