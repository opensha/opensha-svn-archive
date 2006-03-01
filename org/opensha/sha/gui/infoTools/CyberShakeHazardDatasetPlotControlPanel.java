package org.opensha.sha.gui.infoTools;

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




  private static final boolean D = true;
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



  public CyberShakeHazardDatasetPlotControlPanel() {
    try {
      siteSA_PeriodMap = getSiteAndSAPeriod();
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
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
        new Insets(197, 127, 35, 134), 53, 0));
    this.getContentPane().add(guiPanel, java.awt.BorderLayout.CENTER);
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submitButton_actionPerformed(e);
      }
    });
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
    initSA_PeriodParam();

    paramList = new ParameterList();
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
    String siteName = (String)paramList.getParameter(SITE_SELECTOR_PARAM).getValue();
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
      outputToServlet.writeObject(CyberShakeHazardDataSelectorServlet.GET_SITES_AND_SA_PERIODS);


      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "actual webaddress of all the gmt related files"
     // from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      Object messageFromServlet = inputToServlet.readObject();
      inputToServlet.close();
      if(messageFromServlet instanceof ArrayList)
        siteAndSA_PeriodList = (HashMap) messageFromServlet;
      else
        throw (RuntimeException)messageFromServlet;
    }catch(RuntimeException e){
      e.printStackTrace();
     throw new RuntimeException(e.getMessage());
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
  private ArrayList getHazardData(String cybershakeSite, String saPeriod) throws RuntimeException{
    ArrayList cyberShakeHazardData=null;
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
      if(messageFromServlet instanceof ArrayList)
        cyberShakeHazardData = (ArrayList) messageFromServlet;
      else
        throw (RuntimeException)messageFromServlet;
    }catch(RuntimeException e){
      e.printStackTrace();
     throw new RuntimeException(e.getMessage());
    }catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Server is down , please try again later");
    }
    return cyberShakeHazardData;
  }

  public void submitButton_actionPerformed(ActionEvent actionEvent) {
    String cyberShakeSite = (String)siteSelectionParam.getValue();
    String saPeriod = (String)saPeriodParam.getValue();
    ArrayList hazardData = getHazardData(cyberShakeSite,saPeriod);
  }

}
