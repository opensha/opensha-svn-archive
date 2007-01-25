package org.opensha.sha.gui.controls;


import java.util.*;

import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.sha.gui.beans.*;
import org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.*;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.sha.param.editor.gui.SimpleFaultParameterEditorPanel;
import org.opensha.sha.param.editor.SimpleFaultParameterEditor;
import org.opensha.sha.param.editor.MagFreqDistParameterEditor;
import org.opensha.sha.param.*;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.data.Location;
import org.opensha.data.Direction;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.fault.*;


/**
 * <p>Title: PuenteHillsScenarioControlPanelUsingEqkRuptureCreation</p>
 * <p>Description: Sets the param value to replicate the official scenario shakemap
 * for the Puente Hill Scenario (http://www.trinet.org/shake/Puente_Hills_se)</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class SanAndreasScenarioControlPanel {

  //for debugging
  protected final static boolean D = false;


  private EqkRupSelectorGuiBean erfGuiBean;
  private AttenuationRelationshipGuiBean imrGuiBean;
  private SitesInGriddedRectangularRegionGuiBean regionGuiBean;
  private MapGuiBean mapGuiBean;

  private SimpleFaultData sanAndreasFaultData;
  private double aveDipDir;

  //default magnitude.
  private double magnitude = 7.9;

  /**
   * Accepts 3 params for the EqkRupSelectorGuiBean, AttenuationRelationshipGuiBean, SitesInGriddedRectangularRegionGuiBean
   * from the applet.
   * @param erfGuiBean
   * @param imrGuiBean
   * @param regionGuiBean
   * @param MapGuiBean
   */
  public SanAndreasScenarioControlPanel(EqkRupSelectorGuiBean erfGuiBean,
      AttenuationRelationshipGuiBean imrGuiBean, SitesInGriddedRectangularRegionGuiBean regionGuiBean,
      MapGuiBean mapGuiBean) {
    //getting the instance for variuos GuiBeans from the applet required to set the
    //default values for the Params for the Puente Hills Scenario.
    this.erfGuiBean = erfGuiBean;
    this.imrGuiBean = imrGuiBean;
    this.regionGuiBean = regionGuiBean;
    this.mapGuiBean = mapGuiBean;
    mkFaultTrace();
  }

  /**
   * This make the faultTrace from the fault section database that is being maintained by UCERF project
   * via email by vipin on 01/20/07:
   * Here are the fault sections that need to be combined:

	San Andreas (Mojave S)
	-118.508948,34.698495
	-118.103936,34.547849
	-117.753579,34.402927
	-117.549,34.3163
	upper depth = 0;
	lower depth = 13.1
	dip = 90
	rake = 180
	
	San Andreas (San Bernardino N)
	-117.549,34.3163
	-117.451,34.2709
	-117.388692,34.232843
	-117.274161,34.173137
	-117.222023,34.150027
	upper depth = 0;
	lower depth = 12.8
	dip = 90
	rake = 180
	
	
	San Andreas (San Bernardino S)
	-117.222023,34.150027
	-117.067674,34.092795
	-117.0139,34.073768
	-116.90235,34.033837
	-116.873541,34.011347
	-116.819795,33.959114
	upper depth = 0;
	lower depth = 12.8
	dip = 90
	rake = 180
	
	San Andreas (San Gorgonio Pass-Garnet HIll)
	-116.24629,33.78825
	-116.383007,33.848518
	-116.426527,33.848123
	-116.516889,33.884664
	-116.584856,33.907018
	-116.623871,33.917569
	-116.685809,33.944163
	-116.778598,33.937411
	-116.801391,33.953154
	upper depth = 0;
	lower depth = 12.8
	dip = 58
	rake = NA
	
	San Andreas (Coachella) rev
	-116.24629,33.78825
	-115.71192,33.35009
	upper depth = 0;
	lower depth = 11.1
	dip = 90
	rake = 180

   */
  private void mkFaultTrace() {
	FaultTrace faultTrace1 =  new FaultTrace("San Andreas Fault Trace(Mojave S)");
    //San Andreas (Mojave S)
	faultTrace1.addLocation(new Location(34.698495,-118.508948));
	faultTrace1.addLocation(new Location(34.547849,-118.103936));
	faultTrace1.addLocation(new Location(34.402927,-117.753579));
	faultTrace1.addLocation(new Location(34.3163,-117.549));
	SimpleFaultData faultData1 = new SimpleFaultData(90,0,13.1,faultTrace1);
    
    //San Andreas (San Bernardino N)
    FaultTrace faultTrace2 =  new FaultTrace("San Andreas (San Bernardino N)");
    faultTrace2.addLocation(new Location(34.3163,-117.549));
    faultTrace2.addLocation(new Location(34.2709,-117.451));
    faultTrace2.addLocation(new Location(34.232843,-117.388692));
    faultTrace2.addLocation(new Location(34.173137,-117.274161));
    faultTrace2.addLocation(new Location(34.150027,-117.222023));
    SimpleFaultData faultData2 = new SimpleFaultData(90,0,12.8,faultTrace2);
    
	//San Andreas (San Bernardino S)
    FaultTrace faultTrace3 =  new FaultTrace("San Andreas (San Bernardino S)");
    faultTrace3.addLocation(new Location(34.150027,-117.222023));
    faultTrace3.addLocation(new Location(34.092795,-117.067674));
    faultTrace3.addLocation(new Location(34.073768,-117.0139));
    faultTrace3.addLocation(new Location(34.033837,-116.90235));
    faultTrace3.addLocation(new Location(34.011347,-116.873541));
    faultTrace3.addLocation(new Location(33.959114,-116.819795));
    SimpleFaultData faultData3 = new SimpleFaultData(90,0,12.8,faultTrace3);
	
	//San Andreas (San Gorgonio Pass-Garnet HIll)
    FaultTrace faultTrace4 =  new FaultTrace("San Andreas (San Gorgonio Pass-Garnet HIll)");
    faultTrace4.addLocation(new Location(33.78825,-116.24629));
    faultTrace4.addLocation(new Location(33.848518,-116.383007));
    faultTrace4.addLocation(new Location(33.848123,-116.426527));
    faultTrace4.addLocation(new Location(33.884664,-116.516889));
    faultTrace4.addLocation(new Location(33.907018,-116.584856));
    faultTrace4.addLocation(new Location(33.917569,-116.623871));
    faultTrace4.addLocation(new Location(33.944163,-116.685809));
    faultTrace4.addLocation(new Location(33.937411,-116.778598));
    faultTrace4.addLocation(new Location(33.953154,-116.801391));
    SimpleFaultData faultData4 = new SimpleFaultData(58,0,12.8,faultTrace4);
	
	//San Andreas (Coachella) rev
    FaultTrace faultTrace5 =  new FaultTrace("San Andreas (Coachella) rev");
    faultTrace5.addLocation(new Location(33.78825,-116.24629));
    faultTrace5.addLocation(new Location(33.35009,-115.71192));
    SimpleFaultData faultData5 = new SimpleFaultData(90,0,11.1,faultTrace5);
	
    ArrayList<SimpleFaultData> faultList = new ArrayList<SimpleFaultData>();
    faultList.add(faultData1);
    faultList.add(faultData2);
    faultList.add(faultData3);
    faultList.add(faultData4);
    faultList.add(faultData5);
    sanAndreasFaultData = SimpleFaultData.getCombinedSimpleFaultData(faultList);
  }




  /**
   * Sets the default Parameters in the Application for the Puente Hill Scenario
   */
  public void setParamsForSanAndreasScenario(){
    //making the ERF Gui Bean Adjustable Param not visible to the user, becuase
    //this control panel will set the values by itself.
    //This is done in the EqkRupSelectorGuiBean
    ParameterEditor paramEditor = erfGuiBean.getParameterEditor(erfGuiBean.RUPTURE_SELECTOR_PARAM_NAME);
    paramEditor.setValue(erfGuiBean.CREATE_RUPTURE);
    paramEditor.refreshParamEditor();
    EqkRuptureCreationPanel erfPanel= (EqkRuptureCreationPanel)erfGuiBean.getEqkRuptureSelectorPanel();

    //changing the ERF to SimpleFaultERF
    paramEditor = erfPanel.getParameterEditor(erfPanel.SRC_TYP_PARAM_NAME);
    paramEditor.setValue(erfPanel.FINITE_SRC_NAME);
    paramEditor.refreshParamEditor();


    // Set rake value to 90 degrees
    erfPanel.getParameter(erfPanel.RAKE_PARAM_NAME).setValue(new Double(180));


    //getting the instance for the SimpleFaultParameterEditorPanel from the GuiBean to adjust the fault Params
    SimpleFaultParameterEditorPanel faultPanel= ((SimpleFaultParameterEditor)erfPanel.getParameterEditor(erfPanel.FAULT_PARAM_NAME)).getParameterEditorPanel();
    //creating the Lat vector for the SimpleFaultParameter

    ArrayList lats = new ArrayList();
    ArrayList lons = new ArrayList();
    FaultTrace faultTrace = sanAndreasFaultData.getFaultTrace();
    for(int i = 0; i<faultTrace.getNumLocations(); i++) {
      lats.add(new Double(faultTrace.getLocationAt(i).getLatitude()));
      lons.add(new Double(faultTrace.getLocationAt(i).getLongitude()));
    }

    //creating the dip vector for the SimpleFaultParameter
    ArrayList dips = new ArrayList();
    dips.add(new Double(sanAndreasFaultData.getAveDip()));
    

    //creating the depth vector for the SimpleFaultParameter
    ArrayList depths = new ArrayList();
    depths.add(new Double(sanAndreasFaultData.getUpperSeismogenicDepth()));
    depths.add(new Double(sanAndreasFaultData.getLowerSeismogenicDepth()));

    //setting the FaultParameterEditor with the default values for Puente Hills Scenario
    faultPanel.setAll(((SimpleFaultParameter)faultPanel.getParameter()).DEFAULT_GRID_SPACING,lats,
                      lons,dips,depths,((SimpleFaultParameter)faultPanel.getParameter()).STIRLING);

    // set the average dip direction
    // use default which is perp to ave strike.
//    faultPanel.setDipDirection(aveDipDir);

    faultPanel.refreshParamEditor();

    //updaing the faultParameter to update the faultSurface
    faultPanel.setEvenlyGriddedSurfaceFromParams();


    erfPanel.getParameter(erfPanel.MAG_PARAM_NAME).setValue(new Double(magnitude));
    erfPanel.getParameterListEditor().refreshParamEditor();


    //checking if the single AttenRel is selected
    boolean isSingleAttenRelSelected =imrGuiBean.isSingleAttenRelTypeSelected();
    //if single attenRel gui is not selected then toggle to the single attenRel gui Panel
    if(!isSingleAttenRelSelected)
      imrGuiBean.toggleBetweenSingleAndMultipleAttenRelGuiSelection();
    // Set the imt as PGA
    ParameterListEditor editor = imrGuiBean.getIntensityMeasureParamEditor();
    editor.getParameterList().getParameter(imrGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
    editor.refreshParamEditor();
    //Updating the IMR Gui Bean with the ShakeMap attenuation relationship
    imrGuiBean.setIMR_Selected(ShakeMap_2003_AttenRel.NAME);
    imrGuiBean.getSelectedIMR_Instance().getParameter(ShakeMap_2003_AttenRel.COMPONENT_NAME).setValue(ShakeMap_2003_AttenRel.COMPONENT_AVE_HORZ);
    imrGuiBean.getSingleAttenRelParamListEditor().refreshParamEditor();

    //Updating the SitesInGriddedRectangularRegionGuiBean with the Puente Hills resion setting
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LATITUDE).setValue(new Double(33.5));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LATITUDE).setValue(new Double(35.0));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LONGITUDE).setValue(new Double(-118.7));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LONGITUDE).setValue(new Double(-116.00));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.GRID_SPACING).setValue(new Double(.016667));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.SITE_PARAM_NAME).setValue(regionGuiBean.SET_SITES_USING_SCEC_CVM);


    // Set some of the mapping params:
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.GMT_WEBSERVICE_NAME).setValue(new Boolean(true));
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.LOG_PLOT_NAME).setValue(new Boolean(false));
    mapGuiBean.refreshParamEditor();
  }
}
