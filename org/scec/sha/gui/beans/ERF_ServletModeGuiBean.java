package org.scec.sha.gui.beans;


import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;

import org.scec.sha.param.SimpleFaultParameter;
import org.scec.sha.param.editor.SimpleFaultParameterEditor;
import org.scec.param.editor.ParameterListEditor;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.param.editor.MagFreqDistParameterEditor;
import org.scec.sha.param.MagFreqDistParameter;
import org.scec.sha.earthquake.ERF_EpistemicList;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.*;
import org.scec.sha.earthquake.rupForecastImpl.WG02.*;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.sha.gui.servlets.erf.*;
import org.scec.sha.earthquake.*;
import org.scec.data.TimeSpan;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.*;
import org.scec.sha.earthquake.rupForecastImpl.step.*;
import org.scec.sha.earthquake.rupForecastImpl.*;

/**
 * <p>Title: ERF_ServletModeGuiBean </p>
 * <p>Description: It displays ERFs and parameters supported by them</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created: June 11, 2003
 * @version 1.0
 */

public class ERF_ServletModeGuiBean extends ParameterListEditor
    implements ERF_GuiBeanAPI,TimeSpanChangeListener {
  //this vector saves the names of all the supported Eqk Rup Forecasts
  protected Vector erfNamesVector=new Vector();

   // ERF Editor stuff
  public final static String ERF_PARAM_NAME = "Eqk Rup Forecast";
  // these are to store the list of independ params for chosen ERF
  public final static String ERF_EDITOR_TITLE =  "Set Forecast";

  //static strings to make the function calls in the Forecast servlets
  public final static String getName="Forecast Name";
  public final static String getAdjParams="Forecast Adj Params";
  public final static String getTimeSpan ="TimeSpan";
  public final static String getERF_API= "EqkRupForecast Object";
  public final static String getERF_ListAPI= "EqkRupForecast List Object";

  // boolean for telling whether to show a progress bar
  boolean showProgressBar = true;

  //Hashtable where keys corresponds to being as the keys and ParamList as their Values
  private Hashtable paramListForAllERF=new Hashtable();

  //Hashtable where keys corresponds to being as the keys and timeSpan as their Values
  private Hashtable timespanListForAllERF = new Hashtable();

  //Hashtable where keys corresponds to the name of the forecast and value tell if any parameter
  //for that forecast has been changed
  //private Hashtable parameterChangeFlagsForAllERF = new Hashtable();



  /**
   * default constructor
   * @param erfClassNames
   */
  public ERF_ServletModeGuiBean() throws RuntimeException{

    //gets the supported ERF List and initialise the selction of the ERF for the user
    try{
      initERF_List();
    }
    catch(Exception ee){
      throw new RuntimeException("Connection to ERF servlets failed");
    }
    //sets the ParamList and Editor the Selected ERF.
    setParamsInForecast();

  }



  /**
   * initERF_List. List of all available forecasts at this time
   */
  protected void initERF_List() throws Exception{

    EqkRupForecastAPI erf;
    this.parameterList = new ParameterList();

    //open the connections to all the ERF servlets to get their paramList and timspan
    //It also initialises all Vector with names of all the ERF's
    this.openConnectionToAllERF_Servlets();

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
  private void setParamsInForecast() {

    ParameterAPI chooseERF_Param = parameterList.getParameter(this.ERF_PARAM_NAME);
    parameterList = new ParameterList();
    parameterList.addParameter(chooseERF_Param);

    //get the iterator for adjustable params for the selected forecast
    ListIterator it = this.getAdjParamList().getParametersIterator();

    // add the parameterchangeListener to adjustable params of the Forecast
    //And add the parameter to the parameterList
    while(it.hasNext()) {
      ParameterAPI tempParam=(ParameterAPI)it.next();
      tempParam.addParameterChangeListener(this);
      tempParam.addParameterChangeFailListener(this);
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
   * gets the lists of all the parameters that exists in the ERF parameter Editor
   * then checks if the simpleFaultParameter exists inside it , if so then returns the
   * SimpleFaultParameterEditor else return null.  The only reason this is public is
   * because at least one control panel (for the PEER test cases) needs access.
   * @returns SimpleFaultParameterEditor
   */
  public SimpleFaultParameterEditor getSimpleFaultParamEditor(){

    ListIterator lit = parameterList.getParametersIterator();
    while(lit.hasNext()){
      ParameterAPI param=(ParameterAPI)lit.next();
      if(param instanceof SimpleFaultParameter){
        SimpleFaultParameterEditor simpleFaultEditor = ((SimpleFaultParameterEditor)getParameterEditor(param.getName()));
        return simpleFaultEditor;
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
   * get the selected ERF instance.
   * It returns the ERF after updating its forecast
   * @return
   */
  public ForecastAPI getSelectedERF() {

    // update the mag dist param
    if(this.getMagDistEditor()!=null)
      this.updateMagDistParam();
    //System.out.println("MagDist Value:"+this.getMagDistEditor().getParameter("Mag Dist").getValue().toString());

    CalcProgressBar progress= null;

    if(this.showProgressBar) {
      // also show the progress bar while the forecast is being updated
      progress = new CalcProgressBar("Forecast","Updating Forecast");
      //progress.displayProgressBar();
    }
    // get the selected forecast model
    String selectedForecast = getSelectedERF_Name();

    //Reference to the EqkRupForecast Objects returned from the ERF servlets
    ForecastAPI eqkRupForecast=null;

    //checks if any parameter for the forecast has been changed only then get the
    //new forecast object from the server
    //if(((Boolean)parameterChangeFlagsForAllERF.get(selectedForecast)).booleanValue()){

    //Based on the selected ERF model it connects to the servlet for that ERF
    //and gets it ERF Object.
    //if the selected forecast is SimplePoisson Fault Forecast
    if(selectedForecast.equalsIgnoreCase(SimplePoissonFaultERF.NAME)){
      try{
      eqkRupForecast=(ForecastAPI)this.openSimplePoissonFaultConnection(this.getERF_API);
      }catch(Exception e){
        throw new RuntimeException("Connection to SimpleFault ERF servlet failed");
      }
    }
    //if the selected ERF is the USGS/CGS frankel ERF
    else if(selectedForecast.equalsIgnoreCase(Frankel96_AdjustableEqkRupForecast.NAME)){
      try{
      eqkRupForecast=(ForecastAPI)this.openUSGS_CGS_1996FaultConnection(this.getERF_API);
      }catch(Exception e){
        throw new RuntimeException("Connection to USGS/CGS(1996) ERF servlet failed");
      }
    }

    //if the selected ERF is the STEP ERF
    else if(selectedForecast.equalsIgnoreCase(STEP_EqkRupForecast.NAME)){
      try{
      eqkRupForecast=(ForecastAPI)this.openSTEP_ERFConnection(this.getERF_API);
      }catch(Exception e){
        throw new RuntimeException("Connection to STEP ERF servlet failed");
      }
    }

    //if the selected ERF is the STEP ERF
    else if(selectedForecast.equalsIgnoreCase(STEP_AlaskanPipeForecast.NAME)){
      try{
      eqkRupForecast=(ForecastAPI)this.openSTEP_ERF_AlaskanPipeConnection(this.getERF_API);
      }catch(Exception e){
        throw new RuntimeException("Connection to STEP ERF for Alaskan Pipe servlet failed");
      }
    }


    //if the selected forecast is WG02_List Forecast
    else if(selectedForecast.equalsIgnoreCase(WG02_FortranWrappedERF_EpistemicList.NAME)){
      try{
        eqkRupForecast=(ForecastAPI)this.openWG02_ERFConnection(this.getERF_ListAPI);
      }catch(Exception e){
        System.out.println("*************servlet failure*********");
        throw new RuntimeException("Connection to WG-02 ERF servlet failed");
      }
    }
    //changing the paramterChangeFlag for the selected forecast to false
    //parameterChangeFlagsForAllERF.put(selectedForecast,new Boolean(false));
    //}

    if (this.showProgressBar) progress.dispose();
    return eqkRupForecast;
  }

  /**
   * It sees whether selected ERF is a Epistemic list.
   * @return : true if selected ERF is a epistemic list, else false
   */
  public boolean isEpistemicList() {
    //This needed to fix , to remove the hard codeing
    //it is being used to pop up the control panel for the Epistemic List
    if(this.getSelectedERF_Name().equalsIgnoreCase(WG02_FortranWrappedERF_EpistemicList.NAME))
      return true;
    else return false;
  }


  /**checks if the magFreqDistParameter exists inside it ,
   * if so then gets its Editor and calls the method to update the magDistParams.
   */
  protected void updateMagDistParam() {
    System.out.println("MagDist Param is not null");
    MagFreqDistParameterEditor magEditor=getMagDistEditor();
    if(magEditor!=null)  magEditor.setMagDistFromParams();
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
      //String value = event.getNewValue().toString();
      setParamsInForecast();
      this.validate();
      this.repaint();
      //       applet.updateChosenERF();
    }
    //make the parameter change flag to be true
    //this.parameterChangeFlagsForAllERF.put(this.getSelectedERF_Name(),new Boolean(true));
  }

  /**
   *  Function that must be implemented by all Timespan Listeners for
   *  ParameterChangeEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChange( EventObject event ) {
    //make the parameter change flag to be true
    //this.parameterChangeFlagsForAllERF.put(this.getSelectedERF_Name(),new Boolean(true));
   }

  /**
   * This allows tuning on or off the showing of a progress bar
   * @param show - set as true to show it, or false to not show it
   */
  public void showProgressBar(boolean show) {
    this.showProgressBar=show;
  }

  /**
   * This function initially makes connection to all the supported Forecast models
   * get their parameterList and timespan and stores them in a Hashtable with the key
   * being thename of the Forecast for all the forecast model
   * This is done to improve the efficency of the Applet, so that it does not need
   * to make the connection with the servlet to get timeSpan and paramList
   */
  private void openConnectionToAllERF_Servlets() throws Exception{
    //open the connection with the SimplePoisson_FaultForecastServlet.
    String spForecastName =(String)openSimplePoissonFaultConnection(getName);
    ParameterList spParamList =(ParameterList)openSimplePoissonFaultConnection(getAdjParams);
    TimeSpan spTimeSpan =(TimeSpan)openSimplePoissonFaultConnection(getTimeSpan);
    spTimeSpan.addParameterChangeListener(this);
    paramListForAllERF.put(spForecastName,spParamList);
    timespanListForAllERF.put(spForecastName,spTimeSpan);
    //parameterChangeFlagsForAllERF.put(peerForecastName,new Boolean(true));

    //open the connection for the WG-02 ERF EpistemicList
    String wg02ForecastName =(String)openWG02_ERFConnection(getName);
    ParameterList wg02ParamList =(ParameterList)openWG02_ERFConnection(getAdjParams);
    TimeSpan wg02TimeSpan =(TimeSpan)openWG02_ERFConnection(getTimeSpan);
    wg02TimeSpan.addParameterChangeListener(this);
    paramListForAllERF.put(wg02ForecastName,wg02ParamList);
    timespanListForAllERF.put(wg02ForecastName,wg02TimeSpan);
    //parameterChangeFlagsForAllERF.put(wg02ForecastName,new Boolean(true));

    //open the connection with the USGS/CGS(1996) Forecast model
    String frankelForecastName =(String)openUSGS_CGS_1996FaultConnection(getName);
    ParameterList frankelParamList =(ParameterList)openUSGS_CGS_1996FaultConnection(getAdjParams);
    TimeSpan frankelTimeSpan =(TimeSpan)openUSGS_CGS_1996FaultConnection(getTimeSpan);
    frankelTimeSpan.addParameterChangeListener(this);
    paramListForAllERF.put(frankelForecastName,frankelParamList);
    timespanListForAllERF.put(frankelForecastName,frankelTimeSpan);
    //parameterChangeFlagsForAllERF.put(frankelForecastName,new Boolean(true));


    //open the connection with the STEP Forecast model
    String stepForecastName =(String)openSTEP_ERFConnection(getName);
    ParameterList stepParamList =(ParameterList)openSTEP_ERFConnection(getAdjParams);
    TimeSpan stepTimeSpan =(TimeSpan)openSTEP_ERFConnection(getTimeSpan);
    stepTimeSpan.addParameterChangeListener(this);
    paramListForAllERF.put(stepForecastName,stepParamList);
    timespanListForAllERF.put(stepForecastName,stepTimeSpan);
    //parameterChangeFlagsForAllERF.put(stepForecastName,new Boolean(true));


    //open the connection with the STEP Forecast  for Alaskan Pipe model
    String stepAP_ForecastName =(String)openSTEP_ERF_AlaskanPipeConnection(getName);
    ParameterList stepAP_ParamList =(ParameterList)openSTEP_ERF_AlaskanPipeConnection(getAdjParams);
    TimeSpan stepAP_TimeSpan =(TimeSpan)openSTEP_ERF_AlaskanPipeConnection(getTimeSpan);
    stepAP_TimeSpan.addParameterChangeListener(this);
    paramListForAllERF.put(stepAP_ForecastName,stepAP_ParamList);
    timespanListForAllERF.put(stepAP_ForecastName,stepAP_TimeSpan);
    //parameterChangeFlagsForAllERF.put(stepAP_ForecastName,new Boolean(true));





    // gets the Names of all the ERF from the Hashtable and adds them to the vector
    //this list of ERF names act as the selection list for user to choose the ERF of his desire.
    this.erfNamesVector.add(spForecastName);
    this.erfNamesVector.add(wg02ForecastName);
    this.erfNamesVector.add(frankelForecastName);
    this.erfNamesVector.add(stepForecastName);
    this.erfNamesVector.add(stepAP_ForecastName);
  }


  /**
   * sets up the connection with the STEP Forecast Servlet on the server (gravity.usc.edu)
   */
  private  Object openSTEP_ERFConnection(String function) throws Exception{

    Object outputFromServletFunction=null;

    URL stepERF_Servlet = new
                           URL("http://gravity.usc.edu/OpenSHA/servlet/STEP_EqkRupForecastServlet");


    URLConnection servletConnection = stepERF_Servlet.openConnection();
    System.out.println("connection established:"+servletConnection.toString());

    // inform the connection that we will send output and accept input
    servletConnection.setDoInput(true);
    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);
    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

    ObjectOutputStream outputToServlet = new
        ObjectOutputStream(servletConnection.getOutputStream());

    System.out.println("Calling the function:"+function);

    //tells the servlet which function to call
    outputToServlet.writeObject(function);

    /**
     * if the function to be called is getERF_API
     * then we need to passs the TimeSpan and Adjustable ParamList to the
     * servlet.
     */
    if(function.equalsIgnoreCase(this.getERF_API)){
      //gives the Adjustable Params  object to the Servelet
      outputToServlet.writeObject(this.getAdjParamList());
      System.out.println(this.getAdjParamList().toString());
      //gives the timeSpan object to the servlet
      outputToServlet.writeObject(this.getTimeSpan());
    }

    outputToServlet.flush();
    outputToServlet.close();

    // Receive the "object" from the servlet after it has received all the data
    ObjectInputStream inputToServlet = new
                                       ObjectInputStream(servletConnection.getInputStream());

    outputFromServletFunction=inputToServlet.readObject();
    System.out.println("Received the input from the servlet");
    inputToServlet.close();
    return outputFromServletFunction;
  }


  /**
   * sets up the connection with the Alaskan Pipe STEP Forecast Servlet on the server (gravity.usc.edu)
   */
  private  Object openSTEP_ERF_AlaskanPipeConnection(String function) throws Exception{

    Object outputFromServletFunction=null;

    URL stepERF_Servlet = new
                           URL("http://gravity.usc.edu/OpenSHA/servlet/STEP_AlaskanPipeEqkRupForecastServlet");


    URLConnection servletConnection = stepERF_Servlet.openConnection();
    System.out.println("connection established:"+servletConnection.toString());

    // inform the connection that we will send output and accept input
    servletConnection.setDoInput(true);
    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);
    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

    ObjectOutputStream outputToServlet = new
        ObjectOutputStream(servletConnection.getOutputStream());

    System.out.println("Calling the function:"+function);

    //tells the servlet which function to call
    outputToServlet.writeObject(function);

    /**
     * if the function to be called is getERF_API
     * then we need to passs the TimeSpan and Adjustable ParamList to the
     * servlet.
     */
    if(function.equalsIgnoreCase(this.getERF_API)){
      //gives the Adjustable Params  object to the Servelet
      outputToServlet.writeObject(this.getAdjParamList());
      System.out.println(this.getAdjParamList().toString());
      //gives the timeSpan object to the servlet
      outputToServlet.writeObject(this.getTimeSpan());
    }

    outputToServlet.flush();
    outputToServlet.close();

    // Receive the "object" from the servlet after it has received all the data
    ObjectInputStream inputToServlet = new
                                       ObjectInputStream(servletConnection.getInputStream());

    outputFromServletFunction=inputToServlet.readObject();
    System.out.println("Received the input from the servlet");
    inputToServlet.close();
    return outputFromServletFunction;
  }




  /**
   * sets up the connection with the USGS/CGS(1996) Fault Forecast Servlet on the server (gravity.usc.edu)
   */
  private  Object openUSGS_CGS_1996FaultConnection(String function) throws Exception{

    Object outputFromServletFunction=null;

    URL frankelFaultServlet = new
                           URL("http://gravity.usc.edu/OpenSHA/servlet/USGS_CGS_1996_ForecastServlet");


    URLConnection servletConnection = frankelFaultServlet.openConnection();
    System.out.println("connection established:"+servletConnection.toString());

    // inform the connection that we will send output and accept input
    servletConnection.setDoInput(true);
    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);
    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

    ObjectOutputStream outputToServlet = new
        ObjectOutputStream(servletConnection.getOutputStream());

    System.out.println("Calling the function:"+function);

    //tells the servlet which function to call
    outputToServlet.writeObject(function);

    /**
     * if the function to be called is getERF_API
     * then we need to passs the TimeSpan and Adjustable ParamList to the
     * servlet.
     */
    if(function.equalsIgnoreCase(this.getERF_API)){
      //gives the Adjustable Params  object to the Servelet
      outputToServlet.writeObject(this.getAdjParamList());
      System.out.println(this.getAdjParamList().toString());
      //gives the timeSpan object to the servlet
      outputToServlet.writeObject(this.getTimeSpan());
    }

    outputToServlet.flush();
    outputToServlet.close();

    // Receive the "object" from the servlet after it has received all the data
    ObjectInputStream inputToServlet = new
                                       ObjectInputStream(servletConnection.getInputStream());

    outputFromServletFunction=inputToServlet.readObject();
    System.out.println("Received the input from the servlet");
    inputToServlet.close();
    return outputFromServletFunction;
  }

  /**
   * sets up the connection with the WG-02 Forecast Servlet on the server (gravity.usc.edu)
   */
  private  Object openWG02_ERFConnection(String function) throws Exception{

    Object outputFromServletFunction=null;
    URL wg02_Servlet = new
                       URL("http://gravity.usc.edu/OpenSHA/servlet/WG02_EqkRupForecastServlet");

    URLConnection servletConnection = wg02_Servlet.openConnection();
    System.out.println("connection established:"+servletConnection.toString());

    // inform the connection that we will send output and accept input
    servletConnection.setDoInput(true);
    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);
    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

    ObjectOutputStream outputToServlet = new
        ObjectOutputStream(servletConnection.getOutputStream());

    System.out.println("Calling the function:"+function);

    //tells the servlet which function to call
    outputToServlet.writeObject(function);

    /**
     * if the function to be called is getERF_API
     * then we need to passs the TimeSpan and Adjustable ParamList to the
     * servlet.
     */
    if(function.equalsIgnoreCase(this.getERF_ListAPI)){
      //gives the Adjustable Params  object to the Servelet
      outputToServlet.writeObject(this.getAdjParamList());

      //gives the timeSpan object to the servlet
      outputToServlet.writeObject(this.getTimeSpan());
    }

    outputToServlet.flush();
    outputToServlet.close();

    // Receive the "object" from the servlet after it has received all the data
    ObjectInputStream inputToServlet = new
                                       ObjectInputStream(servletConnection.getInputStream());

    outputFromServletFunction=inputToServlet.readObject();
    System.out.println("Received the input from the servlet");
    inputToServlet.close();

    return outputFromServletFunction;
  }

  /**
   * sets up the connection with the SimplePoisson Fault Forecast Servlet on the server (gravity.usc.edu)
   */
  private  Object openSimplePoissonFaultConnection(String function) throws Exception{

    Object outputFromServletFunction=null;

    URL simplePoissonFaultServlet = new
                           URL("http://gravity.usc.edu/OpenSHA/servlet/SimplePoissonFault_ForecastServlet");


    URLConnection servletConnection = simplePoissonFaultServlet.openConnection();
    System.out.println("connection established:"+servletConnection.toString());

    // inform the connection that we will send output and accept input
    servletConnection.setDoInput(true);
    servletConnection.setDoOutput(true);

    // Don't use a cached version of URL connection.
    servletConnection.setUseCaches (false);
    servletConnection.setDefaultUseCaches (false);
    // Specify the content type that we will send binary data
    servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

    ObjectOutputStream outputToServlet = new
        ObjectOutputStream(servletConnection.getOutputStream());

    System.out.println("Calling the function:"+function);

    //tells the servlet which function to call
    outputToServlet.writeObject(function);

    /**
     * if the function to be called is getERF_API
     * then we need to passs the TimeSpan and Adjustable ParamList to the
     * servlet.
     */
    if(function.equalsIgnoreCase(this.getERF_API)){
      //gives the Adjustable Params  object to the Servelet
      outputToServlet.writeObject(this.getAdjParamList());
      //gives the timeSpan object to the servlet
      outputToServlet.writeObject(this.getTimeSpan());
    }

    outputToServlet.flush();
    outputToServlet.close();

    // Receive the "object" from the servlet after it has received all the data
    ObjectInputStream inputToServlet = new
                                       ObjectInputStream(servletConnection.getInputStream());

    outputFromServletFunction=inputToServlet.readObject();
    System.out.println("Received the input from the servlet");
    inputToServlet.close();
    return outputFromServletFunction;
  }



  /**
   *
   * @returns the TimeSpan for the Selected ERF
   */
  public TimeSpan getTimeSpan(){
    String selectedERF = this.getSelectedERF_Name();
    return (TimeSpan)this.timespanListForAllERF.get(selectedERF);
  }


  /**
   *
   * @returns the Adjustable ParamList for the selected ERF
   */
  public ParameterList getAdjParamList(){
    String selectedERF = this.getSelectedERF_Name();
    return (ParameterList)this.paramListForAllERF.get(selectedERF);
  }
}



