package org.scec.sha.gui.beans;


import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.magdist.gui.MagFreqDistParameterEditor;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ERF_GuiBean extends ParameterListEditor implements
    ParameterChangeFailListener, ParameterChangeListener {
  //this vector saves the names of all the supported Eqk Rup Forecasts
  private Vector erfNamesVector=new Vector();
  //this vector holds the full class names of all the supported Eqk Rup Forecasts
  private Vector erfClasses;
  //saves the erf objects
  private Vector erfObject = new Vector();
  // ERF Editor stuff
  public final static String ERF_PARAM_NAME = "Eqk Rup Forecast";
  // these are to store the list of independ params for chosen ERF
  public final static String ERF_EDITOR_TITLE =  "Select Forecast";


  /**
   * Constructor : It accepts the classNames of the ERFs to be shown in the editor
   * @param erfClassNames
   */
  public ERF_GuiBean(Vector erfClassNames) {
    this.parameterList = new ParameterList();
    // save the classs names of ERFs to be shown
    this.erfClasses = erfClassNames;
    // search path needed for making editors
    searchPaths = new String[2];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = "org.scec.sha.magdist.gui" ;
    // create the instance of ERFs
    init_erf_IndParamListAndEditor();
  }


  /**
    * Creates a class instance from a string of the full class name including packages.
    * This is how you dynamically make objects at runtime if you don't know which\
    * class beforehand.
    *
    */
   public Object createERFClassInstance( String className){
     String S = C + ": createERFClassInstance(): ";
     try {
       Object[] paramObjects = new Object[]{};
       Class[] params = new Class[]{};
       Class erfClass = Class.forName( className );
       Constructor con = erfClass.getConstructor(params);
       Object obj = con.newInstance( paramObjects );
       return obj;
     } catch ( ClassCastException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( ClassNotFoundException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( NoSuchMethodException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( InvocationTargetException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( IllegalAccessException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( InstantiationException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     }

   }

   /**
   * init erf_IndParamList. List of all available forecasts at this time
   */
   private void init_erf_IndParamListAndEditor() {

     EqkRupForecastAPI erf;

     Iterator it= erfClasses.iterator();

     while(it.hasNext()){
       // make the ERF objects to get their adjustable parameters
       erf = (EqkRupForecastAPI ) createERFClassInstance((String)it.next());
       if(D)
         System.out.println("Iterator Class:"+erf.getName());
       erfObject.add(erf);
       erfNamesVector.add(erf.getName());
       Iterator it1 = erf.getAdjustableParamsList();

       // add the listener for the paramters in the forecast
       while(it1.hasNext()) {
         ParameterAPI param = (ParameterAPI)it1.next();
         param.addParameterChangeFailListener(this);
       }
     }

     // make the forecast selection parameter
     StringParameter selectSource= new StringParameter(ERF_PARAM_NAME,
                                 erfNamesVector, (String)erfNamesVector.get(0));
     selectSource.addParameterChangeListener(this);
     parameterList.addParameter(selectSource);


     // forecast 1  is selected initially
     setParamsInForecast((String)erfNamesVector.get(0));
  }

  /**
    * this function is called to add the paramters based on the forecast
    *  selected by the user
    * @param forecast
    */
   private void setParamsInForecast(String selectedForecast) {

     ParameterAPI chooseERF_Param = parameterList.getParameter(this.ERF_PARAM_NAME);
     parameterList = new ParameterList();
     parameterList.addParameter(chooseERF_Param);

     // get the selected forecast
     int size = this.erfNamesVector.size();
     String erfName;
     EqkRupForecastAPI erf = null;
     for(int i=0; i<size; ++i) {
       erfName = (String)erfNamesVector.get(i);
       if(selectedForecast.equalsIgnoreCase(erfName)) { // we found selected forecast in the lsit
         erf = (EqkRupForecastAPI)this.erfObject.get(i);
         break;
       }
     }

     Iterator it = erf.getAdjustableParamsList();

    // make the parameters visible based on selected forecast
     while(it.hasNext()) parameterList.addParameter((ParameterAPI)it.next());

     this.editorPanel.removeAll();
     this.addParameters();
     // now make the editor based on the paramter list
     setTitle( this.ERF_EDITOR_TITLE );


     //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
     //calls the method to make the update MagDist button invisible

     MagFreqDistParameterEditor magDistEditor=getMagDistEditor();
     if(magDistEditor !=null)  magDistEditor.setUpdateButtonVisible(false);

   }



   /**
    * gets the lists of all the parameters that exists in the ERF parameter Editor
    * then checks if the magFreqDistParameter exists inside it , if so then returns the MagEditor
    * else return null.
    * @returns MagFreDistParameterEditor
    */
   public MagFreqDistParameterEditor getMagDistEditor(){

     ListIterator lit = parameterList.getParametersIterator();
     while(lit.hasNext()){
       ParameterAPI param=(ParameterAPI)lit.next();
       if(param instanceof MagFreqDistParameter){
         MagFreqDistParameterEditor magDistEditor=((MagFreqDistParameterEditor)getParameterEditor(param.getName()));
         return magDistEditor;
       }
     }
     return null;
   }


   /**
    * returns the name of selected ERF
    * @return
    */
   public String getSelectedERF_Name() {
     return (String)parameterList.getValue(this.ERF_PARAM_NAME);
   }

   /**
    * get the selected ERF instance
    * @return
    */
   public EqkRupForecastAPI getSelectedERF_Instance() {
     EqkRupForecastAPI eqkRupForecast = null;
     // get the selected forecast model
     String selectedForecast = getSelectedERF_Name();

     // check which forecast has been selected by the user
     int size = this.erfNamesVector.size();
     String erfName;
     for(int i=0; i<size; ++i) {
       erfName = (String)erfNamesVector.get(i);
       if(selectedForecast.equalsIgnoreCase(erfName)) { // we found selected forecast in the list
         eqkRupForecast = (EqkRupForecastAPI)this.erfObject.get(i);
         break;
       }
     }
     return eqkRupForecast;
   }


   /**checks if the magFreqDistParameter exists inside it ,
    * if so then gets its Editor and calls the method to update the magDistParams.
    */
   public void updateMagDistParam() {
     MagFreqDistParameterEditor magEditor=getMagDistEditor();
     if(magEditor!=null)  magEditor.setMagDistFromParams();
   }


   /**
    *  Shown when a Constraint error is thrown on a ParameterEditor
    *
    * @param  e  Description of the Parameter
    */
   public void parameterChangeFailed( ParameterChangeFailEvent e ) {

     String S = C + " : parameterChangeWarning(): ";
     if(D) System.out.println(S + "Starting");


     StringBuffer b = new StringBuffer();

     ParameterAPI param = ( ParameterAPI ) e.getSource();


     ParameterConstraintAPI constraint = param.getConstraint();
     String oldValueStr = e.getOldValue().toString();
     String badValueStr = e.getBadValue().toString();
     String name = param.getName();

     b.append( "The value ");
     b.append( badValueStr );
     b.append( " is not permitted for '");
     b.append( name );
     b.append( "'.\n" );
     b.append( "Resetting to ");
     b.append( oldValueStr );
     b.append( ". The constraints are: \n");
     b.append( constraint.toString() );

     JOptionPane.showMessageDialog(
         this, b.toString(),
         "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
         );

     if(D) System.out.println(S + "Ending");

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
       setParamsInForecast(value);
       this.validate();
       this.repaint();
       //       applet.updateChosenERF();
     }
   }
}




