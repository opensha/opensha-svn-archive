package org.scec.sha.gui.beans;


import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.magdist.gui.MagFreqDistParameterEditor;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.earthquake.ERF_EpistemicList;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.*;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.data.TimeSpan;



/**
 * <p>Title: ERF_ServletModeGuiBean </p>
 * <p>Description: It displays ERFs and parameters supported by them</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created: June 11, 2003
 * @version 1.0
 */

public class ERF_ServletModeGuiBean extends ParameterListEditor
    implements ERF_GuiBeanAPI {
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

  // boolean for telling whether to show a progress bar
  boolean showProgressBar = true;

  //Hashtable where keys corresponds to being as the keys and ParamList as their Values
  private Hashtable paramListForAllERF=new Hashtable();

  //Hashtable where keys corresponds to being as the keys and timeSpan as their Values
  private Hashtable timespanListForAllERF = new Hashtable();

  /**
   * default constructor
   */
  public ERF_ServletModeGuiBean() {
  }


  /**
   * Constructor : It accepts the classNames of the ERFs to be shown in the editor
   * @param erfClassNames
   */
  public ERF_ServletModeGuiBean(Vector erfClassNames) {

    // forecast 1  is selected initially
    this.openConnectionToAllERF_Servlets();

    //gets the supported ERF List and initialise the selction of the ERF for the user
    initERF_List();

    //sets the ParamList and Editor the Selected ERF.
    setParamsInForecast();

  }



  /**
   * initERF_List. List of all available forecasts at this time
   */
  protected void initERF_List() {

    EqkRupForecastAPI erf;
    this.parameterList = new ParameterList();
    // search path needed for making editors
    searchPaths = new String[2];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = "org.scec.sha.magdist.gui" ;

    // gets the Names of all the ERF from the Hashtable and adds them to the vector
    //this list of ERF names act as the selection list for user to choose the ERF of his desire.
    Enumeration enum= this.paramListForAllERF.keys();
    this.erfNamesVector.removeAllElements();
    while(enum.hasMoreElements())
      this.erfNamesVector.add(enum.nextElement());

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

    //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to make the update MagDist button invisible

    MagFreqDistParameterEditor magDistEditor=getMagDistEditor();
    if(magDistEditor !=null)  magDistEditor.setUpdateButtonVisible(false);

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
   * get the selected ERF instance.
   * It returns the ERF after updating its forecast
   * @return
   */
  public ERF_API getSelectedERF() {

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
    ERF_API eqkRupForecast=null;
    //Based on the selected ERF model it connects to the srevlet for that ERF
    // and gets it ERF Object

    //if the selected forecast is PEER_Fault Forecast
    if(selectedForecast.equalsIgnoreCase(PEER_FaultForecast.NAME))
      eqkRupForecast=(ERF_API)this.openPEERFaultConnection(this.getERF_API);

    if (this.showProgressBar) progress.dispose();
    return eqkRupForecast;
  }

  /**
   * It sees whether selected ERF is a Epistemic list.
   * @return : true if selected ERF is a epistemic list, else false
   */
  /*public boolean isEpistemicList() {
    EqkRupForecastAPI eqkRupForecast = getSelectedERF_Instance();
    if(eqkRupForecast instanceof ERF_EpistemicList)
      return true;
    else return false;
  }*/


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
  }

  /**
   * This allows tuning on or off the showing of a progress bar
   * @param show - set as true to show it, or false to not show it
   */
  public void showProgressBar(boolean show) {
    this.showProgressBar=show;
  }

  private void openConnectionToAllERF_Servlets(){
    //open the connection with the PEER_FaultForecastServlet.
    String forecastName =(String)openPEERFaultConnection(getName);
    ParameterList paramList =(ParameterList)openPEERFaultConnection(getAdjParams);
    TimeSpan timeSpan =(TimeSpan)openPEERFaultConnection(getTimeSpan);
    paramListForAllERF.put(forecastName,paramList);
    timespanListForAllERF.put(forecastName,timeSpan);
  }


  /**
   * sets up the connection with the PEER Fault Forecast Servlet on the server (scec.usc.edu)
   */
  private  Object openPEERFaultConnection(String function) {

    Object outputFromServletFunction=null;
    try{
      URL peerFaultServlet = new
                             URL("http://scec.usc.edu:9999/examples/servlet/PEER_FaultForecastServlet");


      URLConnection servletConnection = peerFaultServlet.openConnection();
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
        System.out.println("MagDist Value:"+this.getAdjParamList().getParameter("Mag Dist").getValue().toString());
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

    }catch(FileNotFoundException ee){
      ee.printStackTrace();
    }
    catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
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



