package org.opensha.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.opensha.param.editor.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.gui.servlets.CyberShakeHazardDataSelectorServlet;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;

/**
 * <p>Title: CyberShakeHazardDatasetPlotControlPanel </p>
 *
 * <p>Description: This allows to view the Cybershake Hazard Dataset and gives
 * the ability to user to add any site hazard dataset on top of the hazard calculated
 * using OpenSHA. This makes the comparison of the Hazrda Calculated using the
 * Empirical based Attenuations with the Waveform based AttenuationRelationship.
 * </p>
 *
 * @author Nitin Gupta
 * @since Feb 27,2006
 * @version 1.0
 */
public class CyberShakeHazardDatasetPlotControlPanel
    extends JFrame implements ParameterChangeListener{




  private static final boolean D = false;
  public static final String SITE_SELECTOR_PARAM = "CyberShake Site";
  public static final String SA_PERIOD_SELECTOR_PARAM = "SA Period";
  private HashMap siteSA_PeriodMap;
  JPanel guiPanel = new JPanel();
  JButton submitButton = new JButton();
  JLabel controlPanelLabel = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  //Site selectrion param
  private StringParameter siteSelectionParam;
  //SA Period selection param
  private StringParameter saPeriodParam;

  private ParameterListEditor listEditor;
  private ParameterList paramList;

  private CyberShakePlotControlPanelAPI application;

  public CyberShakeHazardDatasetPlotControlPanel(CyberShakePlotControlPanelAPI app) {
    application = app;
    try {
      siteSA_PeriodMap = getSiteAndSAPeriod();
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
    controlPanelLabel.setText("Cybershake Hazard Dataset selector Control");
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
        new Insets(4, 4, 4, 4), 53, 0));
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
    Set set = siteSA_PeriodMap.keySet();
    Iterator it = set.iterator();
    while(it.hasNext())
      siteList.add((String)it.next());


    siteSelectionParam = new StringParameter(SITE_SELECTOR_PARAM,
                                             siteList,(String)siteList.get(0));


    paramList = new ParameterList();
    initSA_PeriodParam();
    paramList.addParameter(siteSelectionParam);
    paramList.addParameter(saPeriodParam);
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
    ArrayList saPeriods = (ArrayList)siteSA_PeriodMap.get(siteName);
    saPeriodParam = new StringParameter(this.SA_PERIOD_SELECTOR_PARAM,
        saPeriods,(String)saPeriods.get(0));
  }


  /**
   * Updates the list editor when user changes the Cybershake site
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(siteSelectionParam.getName())){
      siteSA_PeriodMap = getSiteAndSAPeriod();
      initSA_PeriodParam();
      listEditor.replaceParameterForEditor(SA_PERIOD_SELECTOR_PARAM,saPeriodParam );
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
  private HashMap getSiteAndSAPeriod() throws RuntimeException{

    HashMap siteAndSA_PeriodList=null;
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
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.GET_CYBERSHAKE_INFO_PROB_CURVE);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream outputFromServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      siteAndSA_PeriodList = (HashMap)outputFromServlet.readObject();
      outputFromServlet.close();
    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return siteAndSA_PeriodList;
  }


  /**
   * Gets the hazard data from the Cybershake site for the given SA period.
   * @param cybershakeSite String Cybershake Site
   * @param saPeriod String SA period for which hazard file needs to be read.
   * @return ArrayList Hazard Data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getHazardData(String cybershakeSite, String saPeriod) throws RuntimeException{
    DiscretizedFuncAPI cyberShakeHazardData=null;
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


      //sending the input parameters to the servlet
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.GET_HAZARD_DATA);
      //sending the cybershake site
      outputToServlet.writeObject(cybershakeSite);
      //sending the sa period
      outputToServlet.writeObject(saPeriod);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      Object messageFromServlet = inputToServlet.readObject();
      inputToServlet.close();
      cyberShakeHazardData = (DiscretizedFuncAPI) messageFromServlet;
    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return cyberShakeHazardData;
  }

  public void submitButton_actionPerformed(ActionEvent actionEvent) {
    String cyberShakeSite = (String)siteSelectionParam.getValue();
    String saPeriod = (String)saPeriodParam.getValue();
    DiscretizedFuncAPI hazardData = getHazardData(cyberShakeSite,saPeriod);
    String name = "Cybershake hazard curve";
    String infoString = "Site = "+ (String)siteSelectionParam.getValue()+
        "; SA-Period = "+(String)saPeriodParam.getValue();
    hazardData.setName(name);
    hazardData.setInfo(infoString);
    application.addCybershakeCurveData(hazardData);
  }


}
