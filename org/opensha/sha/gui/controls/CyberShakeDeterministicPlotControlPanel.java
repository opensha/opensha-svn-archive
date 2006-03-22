package org.opensha.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.util.*;

import org.opensha.param.editor.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.gui.servlets.CyberShakeHazardDataSelectorServlet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opensha.data.function.DiscretizedFuncAPI;

/**
 * <p>Title: CyberShakeDeterministicPlotControlPanel </p>
 *
 * <p>Description: This allows to view the Deterministic Cybershake Curves with
 * that of OpenSHA using teh Empirical based AttenuationRelationships.
 * </p>
 *
 * @author Nitin Gupta
 * @since March 7,2006
 * @version 1.0
 */
public class CyberShakeDeterministicPlotControlPanel
    extends JFrame implements ParameterChangeListener{


  private static final boolean D = false;
  public static final String SITE_SELECTOR_PARAM = "CyberShake Site";
  public static final String SA_PERIOD_SELECTOR_PARAM = "SA Period";
  public static final String SRC_INDEX_PARAM = "Source Index";
  public static final String RUP_INDEX_PARAM = "Rupture Index";

  JPanel guiPanel = new JPanel();
  JButton submitButton = new JButton();
  JLabel controlPanelLabel = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  //Site selection param
  private StringParameter siteSelectionParam;
  //SA Period selection param
  private StringParameter saPeriodParam;

  //source index parameter
  private StringParameter srcIndexParam;

  //rupture index parameter
  private IntegerParameter rupIndexParam;

  //Editor to show the parameters in the panel
  private ParameterListEditor listEditor;
  //list to show the parameters
  private ParameterList paramList;

  //handle to the application using this control panel
  private CyberShakePlotControlPanelAPI application;

  //list for getting the SA Period for the selected Site
  private HashMap siteAndSA_PeriodList=null;
  //list for getting the sources for the selected site
  private HashMap siteAndSrcListMap = null;



  public CyberShakeDeterministicPlotControlPanel(CyberShakePlotControlPanelAPI app) {
    application = app;
    try {
      getSiteInfoForDeterministicCalc();
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    this.pack();
    Component parent = (Component)app;
    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
                     parent.getY());

  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    guiPanel.setLayout(gridBagLayout1);
    submitButton.setText("OK");
    controlPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
    controlPanelLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    controlPanelLabel.setText("Cybershake Deterministic Curve Control");
    guiPanel.add(controlPanelLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(6, 10, 0, 12), 367, 23));
    //creating the Site and SA Period selection for the Cybershake control panel
    initCyberShakeControlPanel();
    guiPanel.add(listEditor, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 2, 2), 0, 0));
    guiPanel.add(submitButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 53, 0));
    this.getContentPane().add(guiPanel, java.awt.BorderLayout.CENTER);
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submitButton_actionPerformed(e);
      }
    });
    this.setSize(100,200);
  }


  /**
   * Creates the Cybershake site and SA Period GUI elements.
   * Allows the user to select the Site and SA period value for which
   * hazard curve needs to be plotted.
   */
  private void initCyberShakeControlPanel(){
    ArrayList siteList  = new ArrayList();
    Set set = siteAndSA_PeriodList.keySet();
    Iterator it = set.iterator();
    while(it.hasNext())
      siteList.add((String)it.next());



    paramList = new ParameterList();

    siteSelectionParam = new StringParameter(SITE_SELECTOR_PARAM,
                                             siteList,(String)siteList.get(0));
    initSA_PeriodParam();
    initSrcIndexParam();
    rupIndexParam = new IntegerParameter(RUP_INDEX_PARAM);

    paramList.addParameter(siteSelectionParam);
    paramList.addParameter(saPeriodParam);
    paramList.addParameter(srcIndexParam);
    paramList.addParameter(rupIndexParam);
    siteSelectionParam.addParameterChangeListener(this);
    listEditor = new ParameterListEditor(paramList);
    listEditor.setTitle("Cybershake Site and SA Period Selector");
  }


  /**
   * Creates the SA Period Parameter which allows the user to select the
   * SA Period for a given site for which hazard data needs to be plotted.
   */
  private void initSA_PeriodParam(){
    String siteName = (String)siteSelectionParam.getValue();
    ArrayList saPeriods = (ArrayList)siteAndSA_PeriodList.get(siteName);
    saPeriodParam = new StringParameter(this.SA_PERIOD_SELECTOR_PARAM,
        saPeriods,(String)saPeriods.get(0));
  }


  /**
   * Creates the parameters displaying all the src index for a given Cybershake
   * site for which deterministic calculations can be done.
   */
  private void initSrcIndexParam(){
    String siteName = (String)siteSelectionParam.getValue();
    TreeSet srcIndexSet = (TreeSet)siteAndSrcListMap.get(siteName);
    Iterator it =srcIndexSet.iterator();
    ArrayList srcIndexList = new ArrayList();
    while(it.hasNext())
      srcIndexList.add(((Integer)it.next()).toString());
    srcIndexParam = new StringParameter(SRC_INDEX_PARAM,srcIndexList,(String)srcIndexList.get(0));
  }


  /**
   * Updates the list editor when user changes the Cybershake site
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(siteSelectionParam.getName())){
      getSiteInfoForDeterministicCalc();
      initSA_PeriodParam();
      initSrcIndexParam();
      listEditor.replaceParameterForEditor(SA_PERIOD_SELECTOR_PARAM,saPeriodParam );
      listEditor.replaceParameterForEditor(SRC_INDEX_PARAM,srcIndexParam);
      listEditor.refreshParamEditor();
    }
  }


  /**
   *
   * @return HashMap Returns the Hashmap of Cybershake sites and SA period values
   * for each site with keys being the Site name and values being the Arraylist
   * of SA periods for which hazard has been computed.
   * @throws RuntimeException
   */
  private void getSiteInfoForDeterministicCalc() throws RuntimeException{

    try{

      if(D) System.out.println("starting to make connection with servlet");
      URL cybershakeDataServlet = new
                             URL("http://gravity.usc.edu/OpenSHA/servlet/CyberShakeHazardDataSelectorServlet");


      URLConnection servletConnection = cybershakeDataServlet.openConnection();
      if(D) System.out.println("connection established");

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


      //sending the ArrayList of the gmt Script Lines
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.GET_CYBERSHAKE_INFO_DETER_CURVE);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream outputFromServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      siteAndSA_PeriodList = (HashMap)outputFromServlet.readObject();
      siteAndSrcListMap  = (HashMap)outputFromServlet.readObject();
      outputFromServlet.close();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
  }


  /**
   * Gets the hazard data from the Cybershake site for the given SA period.
   * @param cybershakeSite String Cybershake Site
   * @param saPeriod String SA period for which hazard file needs to be read.
   * @return ArbitrarilyDiscretizedFunc Determinitic curve data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getDeterministicData(String cybershakeSite,
                                                  String saPeriod,
                                                  String srcIndex,
                                                  Integer rupIndex,
                                                  ArrayList imlVals) throws
      RuntimeException {
    DiscretizedFuncAPI cyberShakeDeterminicticHazardCurve = null;
    try {

      if (D) System.out.println("starting to make connection with servlet");
      URL cybershakeDataServlet = new
          URL(
          "http://gravity.usc.edu/OpenSHA/servlet/CyberShakeHazardDataSelectorServlet");

      URLConnection servletConnection = cybershakeDataServlet.openConnection();
      if (D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches(false);
      servletConnection.setDefaultUseCaches(false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty("Content-Type",
                                           "application/octet-stream");

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());

      //sending the input parameters to the servlet
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.
                                  GET_DETERMINISTIC_DATA);
      //sending the cybershake site
      outputToServlet.writeObject(cybershakeSite);
      //sending the sa period
      outputToServlet.writeObject(saPeriod);

      //sending the src Index
      outputToServlet.writeObject(srcIndex);

      //sending the rupture index
      outputToServlet.writeObject(rupIndex);

      //sending the rupture index
      outputToServlet.writeObject(imlVals);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
      // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      cyberShakeDeterminicticHazardCurve = (DiscretizedFuncAPI) inputToServlet.
          readObject();
      inputToServlet.close();
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return cyberShakeDeterminicticHazardCurve;
  }

  public void submitButton_actionPerformed(ActionEvent actionEvent) {
    String cyberShakeSite = (String)siteSelectionParam.getValue();
    String saPeriod = (String)saPeriodParam.getValue();
    String srcIndex = (String)srcIndexParam.getValue();
    Integer rupIndex = (Integer)rupIndexParam.getValue();
    ArrayList imlVals = application.getIML_Values();
    DiscretizedFuncAPI deterministicData = getDeterministicData(cyberShakeSite,
        saPeriod, srcIndex, rupIndex,
        imlVals);
    String name = "Cybershake deterministic curve";
    String infoString = "Site = "+ (String)siteSelectionParam.getValue()+
        "; SA-Period = "+(String)saPeriodParam.getValue()+"; SourceIndex = "+(String)srcIndexParam.getValue()+
        "; RuptureIndex = "+((Integer)rupIndexParam.getValue()).intValue();
    deterministicData.setName(name);
    deterministicData.setInfo(infoString);
    application.addCybershakeCurveData(deterministicData);
  }

}
