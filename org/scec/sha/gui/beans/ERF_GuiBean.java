package org.scec.sha.gui.beans;


import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;

import org.scec.sha.param.*;
import org.scec.sha.earthquake.ERF_EpistemicList;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.sha.param.editor.*;


/**
 * <p>Title: ERF_GuiBean </p>
 * <p>Description: It displays ERFs and parameters supported by them</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ERF_GuiBean extends ParameterListEditor implements ERF_GuiBeanAPI {
  //this vector saves the names of all the supported Eqk Rup Forecasts
  protected Vector erfNamesVector=new Vector();
  //this vector holds the full class names of all the supported Eqk Rup Forecasts
  private Vector erfClasses;

  // ERF Editor stuff
  public final static String ERF_PARAM_NAME = "Eqk Rup Forecast";
  // these are to store the list of independ params for chosen ERF
  public final static String ERF_EDITOR_TITLE =  "Set Forecast";
  // boolean for telling whether to show a progress bar
  boolean showProgressBar = true;

  //instance of the selected ERF
  EqkRupForecastAPI eqkRupForecast = null;

  /**
   * default constructor
   */
  public ERF_GuiBean() {
  }


  /**
   * Constructor : It accepts the classNames of the ERFs to be shown in the editor
   * @param erfClassNames
   */
  public ERF_GuiBean(Vector erfClassNames) throws InvocationTargetException{
    // save the classs names of ERFs to be shown
     this.erfClasses = erfClassNames;

    // create the instance of ERFs
    init_erf_IndParamListAndEditor(erfClasses);
    // forecast 1  is selected initially
    setParamsInForecast((String)erfNamesVector.get(0));
  }


  /**
    * Creates a class instance from a string of the full class name including packages.
    * This is how you dynamically make objects at runtime if you don't know which\
    * class beforehand.
    *
    */
   private Object createERFClassInstance( String className) throws InvocationTargetException{
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
     } catch ( IllegalAccessException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     } catch ( InstantiationException e ) {
       System.out.println(S + e.toString());
       throw new RuntimeException( S + e.toString() );
     }
   }

   /**
    * Gets the name of the ERF and show in the PickList for the ERF's
    * As the ERF_NAME is defined as the static variable inside the EqkRupForecast class
    * so it gets the vale for this clas field. Generate the object of the ERF only if the
    * user chooses it from the pick list.
    * @param className
    * @return
    */
   private String getERFName(String className) throws InvocationTargetException{
     Object obj = this.createERFClassInstance(className);
     return ((EqkRupForecastAPI)obj).getName();
   }

   /**
    * init erf_IndParamList. List of all available forecasts at this time
    */
   protected void init_erf_IndParamListAndEditor(Vector erfClassNames) throws InvocationTargetException{

     this.parameterList = new ParameterList();


     //Name of the first ERF class that is to be shown as the default ERF in the ERF Pick List
     String erfClassName = (String)erfClassNames.get(0);

     // make the ERF objects to get their adjustable parameters
     eqkRupForecast = (EqkRupForecastAPI ) createERFClassInstance(erfClassName);
     if(D)  System.out.println("Iterator Class:"+eqkRupForecast.getName());

     //gets the iterator for the class names of all the ERF's
     Iterator it = erfClasses.iterator();

     //adding the names of all the ERF's to the erfNamesVector- Pick List for the ERF's
     while(it.hasNext())
     erfNamesVector.add(this.getERFName((String)it.next()));

     Iterator it1 = eqkRupForecast.getAdjustableParamsIterator();

     // add the listener for the paramters in the forecast
     while(it1.hasNext()) {
       ParameterAPI param = (ParameterAPI)it1.next();
       param.addParameterChangeFailListener(this);
     }

     // make the forecast selection parameter
     StringParameter selectERF= new StringParameter(ERF_PARAM_NAME,
                                 erfNamesVector, (String)erfNamesVector.get(0));
     selectERF.addParameterChangeListener(this);
     parameterList.addParameter(selectERF);
  }


  /**
    * this function is called to add the paramters based on the forecast
    *  selected by the user
    * @param forecast
    */
   private void setParamsInForecast(String selectedForecast) throws InvocationTargetException{

     ParameterAPI chooseERF_Param = parameterList.getParameter(this.ERF_PARAM_NAME);
     parameterList = new ParameterList();
     parameterList.addParameter(chooseERF_Param);

     // get the selected forecast
     getSelectedERF_Instance();

     Iterator it = eqkRupForecast.getAdjustableParamsIterator();

    // make the parameters visible based on selected forecast
     while(it.hasNext()){
       ParameterAPI tempParam = (ParameterAPI)it.next();
       parameterList.addParameter(tempParam);
     }
     this.editorPanel.removeAll();
     this.addParameters();
     // now make the editor based on the paramter list
     setTitle( this.ERF_EDITOR_TITLE );

     // get the panel for increasing the font and border
     // this is hard coding for increasing the IMR font
     // the colors used here are from ParameterEditor
     JPanel panel = this.getParameterEditor(this.ERF_PARAM_NAME).getOuterPanel();
     TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(new Color( 80, 80, 140 ),3),"");
     titledBorder1.setTitleColor(new Color( 80, 80, 140 ));
     Font DEFAULT_LABEL_FONT = new Font( "SansSerif", Font.BOLD, 13 );
     titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
     titledBorder1.setTitle(ERF_PARAM_NAME);
     Border border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
     panel.setBorder(border1);

     //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
     //calls the method to make the update MagDist button invisible

     MagFreqDistParameterEditor magDistEditor=getMagDistEditor();
     if(magDistEditor !=null)  magDistEditor.setUpdateButtonVisible(false);

     //checks to see if the forecast include the SimpleFaultParameter
     SimpleFaultParameterEditor simpleFaultEditor = getSimpleFaultEditor();
     if(simpleFaultEditor !=null) simpleFaultEditor.setUpdateButtonVisible(false);

   }


   /**
    * gets the lists of all the parameters that exists in the ERF parameter Editor
    * then checks if the simpleFaultParameter exists inside it , if so then returns the Simple FaultEditor
    * else return null.  The only reason this is public is because at least one control panel
    * (for the PEER test cases) needs access.
    * @returns SimpleFaultParameterEditor
    */
   public SimpleFaultParameterEditor getSimpleFaultEditor(){

     ListIterator lit = parameterList.getParametersIterator();
     while(lit.hasNext()){
       ParameterAPI param=(ParameterAPI)lit.next();
       if(param instanceof SimpleFaultParameter){
         SimpleFaultParameterEditor simpleFaultEditor=((SimpleFaultParameterEditor)getParameterEditor(param.getName()));
         return simpleFaultEditor;
       }
     }
     return null;
   }


   /**
    * gets the lists of all the parameters that exists in the ERF parameter Editor
    * then checks if the magFreqDistParameter exists inside it , if so then returns the MagEditor
    * else return null.  The only reason this is public is because at least one control panel
    * (for the PEER test cases) needs access.
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
    * It returns the forecast without updating the forecast
    * @return
    */
   public EqkRupForecastAPI getSelectedERF_Instance() throws InvocationTargetException{
     // update the mag dist param
     if(this.getMagDistEditor()!=null)
       this.updateMagDistParam();
     //update teh simple Fault parameter
     if(this.getSimpleFaultEditor()!=null)
       this.updateSimpleFaultParam();
     return eqkRupForecast;
   }


   /**
    * get the selected ERF instance.
    * It returns the ERF after updating its forecast
    * @return
    */
   public EqkRupForecastAPI getSelectedERF() throws InvocationTargetException{
     getSelectedERF_Instance();
     CalcProgressBar progress= null;
     if(this.showProgressBar) {
       // also show the progress bar while the forecast is being updated
       progress = new CalcProgressBar("Forecast","Updating Forecast");
       //progress.displayProgressBar();
     }
     // update the forecast
     eqkRupForecast.updateForecast();
     if (this.showProgressBar) progress.dispose();
     return eqkRupForecast;

   }

   /**
    * It sees whether selected ERF is a Epistemic list.
    * @return : true if selected ERF is a epistemic list, else false
    */
   public boolean isEpistemicList() {
     try{
       EqkRupForecastAPI eqkRupForecast = getSelectedERF_Instance();
       if(eqkRupForecast instanceof ERF_EpistemicList)
         return true;
     }catch(Exception e){
       e.printStackTrace();
     }
     return false;
   }


   /**checks if the magFreqDistParameter exists inside it ,
    * if so then gets its Editor and calls the method to update the magDistParams.
    */
   protected void updateMagDistParam() {
     MagFreqDistParameterEditor magEditor=getMagDistEditor();
     if(magEditor!=null)  magEditor.setMagDistFromParams();
   }

   /**checks if the SimpleFaultParameter exists inside it ,
    * if so then gets its Editor and calls the method to update the simpleFault Parameter.
    */
   protected void updateSimpleFaultParam() {
     SimpleFaultParameterEditor simpleFaultEditor= getSimpleFaultEditor();
     if(simpleFaultEditor!=null)  simpleFaultEditor.setEvenlyGriddedSurfaceFromParams();
   }

   /**
    *  Shown when a Constraint error is thrown on a ParameterEditor
    *
    * @param  e  Description of the Parameter
    */
   public void parameterChangeFailed( ParameterChangeFailEvent e ) {

     String S = C + " : parameterChangeFailed(): ";
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
       int size = this.erfNamesVector.size();
       try{
         for(int i=0;i<size;++i){
           if(value.equalsIgnoreCase((String)erfNamesVector.get(i))){
             eqkRupForecast = (EqkRupForecastAPI)this.createERFClassInstance((String)erfClasses.get(i));
             break;
           }
         }
         setParamsInForecast(value);
       }catch(Exception e){
         e.printStackTrace();
       }
       this.validate();
       this.repaint();
       //       applet.updateChosenERF();
     }
   }

   /**
    * This allows tuning on or off the showing of a progress bar
    * @param show - set as true to show it, or false to not show it
    */
   public void showProgressBar(boolean show) {
     this.showProgressBar=show;
   }
}




