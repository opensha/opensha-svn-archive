package org.scec.sha.gui.beans;

import java.util.Vector;
import java.util.Iterator;

import org.scec.param.ParameterAPI;
import org.scec.param.StringParameter;
import org.scec.param.ParameterList;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.sha.magdist.gui.MagFreqDistParameterEditor;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;


/**
 * <p>Title: Eqk Rupture Selector GuiBean</p>
 * <p>Description: This class will show ERF and its parameters. It will
 * also allow the user to select a particular rupture for scenario maps </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class EqkRupSelectorGuiBean extends ERF_GuiBean  {

  // Source Param Name
  public final static String SOURCE_PARAM_NAME = "Source Index";
  // Source Param Name
  public final static String RUPTURE_PARAM_NAME = "Rupture Index";

  // boolean needed to handle to handle the first case whenever each ERF is selected
  private boolean first = true;


  /**
  * Constructor : It accepts the classNames of the ERFs to be shown in the editor
  * @param erfClassNames
  */
 public EqkRupSelectorGuiBean(Vector erfClassNames) {
   // create the instance of ERFs
   init_erf_IndParamListAndEditor(erfClassNames);
   // forecast 1  is selected initially, also source index for first time is 0
    setParamsInForecast((String)erfNamesVector.get(0), 0);
 }


 /**
  * this function is called to add the paramters based on the forecast
  *  selected by the user
  * @param forecast
  */
 protected void setParamsInForecast(String selectedForecast, int sourceIndex) {


   // get the selected forecast
   EqkRupForecast erf = (EqkRupForecast)this.getSelectedERF_Instance();

   if(!first) {
     // update the Forecast to get the sources and ruptures
     this.updateMagDistParam();
     erf.updateForecast();
   }

   // add the select forecast parameter
   ParameterAPI chooseERF_Param = parameterList.getParameter(this.ERF_PARAM_NAME);
   parameterList = new ParameterList();
   parameterList.addParameter(chooseERF_Param);


   if(!first) {
     // add another parameter for selecting the source
     Vector sourceVector = new Vector();
     int numSources = erf.getNumSources();
     for(int i=0; i<numSources; ++i)
       sourceVector.add(""+i);
     StringParameter selectSource= new StringParameter(SOURCE_PARAM_NAME,
         sourceVector, ""+sourceIndex);
     selectSource.addParameterChangeListener(this);
     parameterList.addParameter(selectSource);

     //add parameter for selecting the rupture for selected source index
     Vector ruptureVector = new Vector();
     int numRuptures = erf.getNumRuptures(sourceIndex);
     for(int i=0; i<numRuptures; ++i)
       ruptureVector.add(""+i);
     StringParameter selectRupture= new StringParameter(RUPTURE_PARAM_NAME,
         sourceVector, ""+sourceIndex);
     parameterList.addParameter(selectRupture);
   }


   Iterator it = erf.getAdjustableParamsIterator();
   // make the parameters visible based on selected forecast
   while(it.hasNext()) parameterList.addParameter((ParameterAPI)it.next());

   this.editorPanel.removeAll();
   this.addParameters();
   // now make the editor based on the parameter list
   setTitle( this.ERF_EDITOR_TITLE );

   //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
   //calls the method to make the update MagDist button invisible
   MagFreqDistParameterEditor magDistEditor=getMagDistEditor();
   if(magDistEditor !=null)  magDistEditor.setUpdateButtonVisible(false);

   if(first) {
     first = false;
     setParamsInForecast(selectedForecast,sourceIndex);
   }

 }

 /**
  *  This is the main function of this interface. Any time a control
  *  paramater or independent paramater is changed by the user in a GUI this
  *  function is called, and a paramater change event is passed in. This
  *  function then determines what to do with the information ie. show some
  *  paramaters, set some as invisible, basically control the paramater
  *  lists.
  *
  * @param  event
  */
 public void parameterChange( ParameterChangeEvent event ) {

   String S = C + ": parameterChange(): ";
   if ( D )
     System.out.println( "\n" + S + "starting: " );

   String name1 = event.getParameterName();

   // if ERF selected by the user  changes
   if( name1.equals(this.ERF_PARAM_NAME) ){
     String value = event.getNewValue().toString();
     // set the new forecast parameters.
     //Also selected source index is 0 for newly selected forecast
     first = true;
     setParamsInForecast(value,0);
     this.validate();
     this.repaint();
   }

   // if source selected by the user  changes
   if( name1.equals(this.SOURCE_PARAM_NAME) ){
     String value = event.getNewValue().toString();
     // set the new forecast parameters. Also change the number of ruptures in this source
     setParamsInForecast(this.getSelectedERF_Name(),Integer.parseInt(value));
     this.validate();
     this.repaint();
   }
 }




}