package org.scec.sha.gui.beans;

import java.util.ListIterator;
import java.util.Vector;
import java.util.Iterator;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;


import org.scec.param.ParameterAPI;
import org.scec.param.StringParameter;
import org.scec.param.ParameterList;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.sha.magdist.gui.MagFreqDistParameterEditor;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import java.awt.event.*;


/**
 * <p>Title: Eqk Rupture Selector GuiBean</p>
 * <p>Description: This class will show ERF and its parameters. It will
 * also allow the user to select a particular rupture for scenario maps </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class EqkRupSelectorGuiBean extends JPanel implements ParameterChangeListener
{


  /**
   * Name of the class
   */
  protected final static String C = "EqkRupSelectorGuiBean";
  // for debug purpose
  protected final static boolean D = false;


  // ERF Editor stuff
  public final static String ERF_PARAM_NAME = "Eqk Rup Forecast";

  // Source Param Name
  public final static String SOURCE_PARAM_NAME = "Source Index";
  // Source Param Name
  public final static String RUPTURE_PARAM_NAME = "Rupture Index";

  // boolean needed to handle to handle the first case whenever each ERF is selected
  private boolean first = true;

  //ERFGuiBean Instance
  ERF_GuiBean erfGuiBean;
  private JButton erfAdjParamButton = new JButton();
  private JScrollPane sourceRupInfoScroll = new JScrollPane();
  private JTextPane sourceRupInfoText = new JTextPane();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  //ListEditor
  ParameterListEditor listEditor;

  //Instance of the JDialog to show all the adjuatble params for the forecast model
  JDialog frame;

  /**
  * Constructor : It accepts the classNames of the ERFs to be shown in the editor
  * @param erfClassNames
  */
 public EqkRupSelectorGuiBean(Vector erfClassNames) {

   try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
   // create the instance of ERFs
   erfGuiBean= new ERF_GuiBean(erfClassNames);
   setParamsInForecast(erfGuiBean.erfNamesVector.get(0).toString(),0);

 }


 /**
  * this function is called to add the paramters based on the forecast
  *  selected by the user
  * @param forecast
  */
 protected void setParamsInForecast(String selectedForecast, int sourceIndex) {


   // get the selected forecast
   EqkRupForecast erf = (EqkRupForecast)erfGuiBean.getSelectedERF_Instance();

   // also show the progress bar while the forecast is being updated
   CalcProgressBar progress = new CalcProgressBar("Forecast","Updating Forecast");
   progress.displayProgressBar();

   // update the Forecast to get the sources and ruptures
   erfGuiBean.updateMagDistParam();
   erf.updateForecast();


   // add the select forecast parameter
   ParameterAPI chooseERF_Param = erfGuiBean.getParameterList().getParameter(erfGuiBean.ERF_PARAM_NAME);
   ParameterList parameterList = new ParameterList();
   parameterList.addParameter(chooseERF_Param);


   int numSources = erf.getNumSources();
   IntegerParameter sourceParam = new IntegerParameter(SOURCE_PARAM_NAME,
       0,numSources-1,new Integer(0));


   sourceParam.addParameterChangeListener(this);
   parameterList.addParameter(sourceParam);

   //add parameter for selecting the rupture for selected source index
   int numRuptures = erf.getNumRuptures(((Integer)sourceParam.getValue()).intValue());
   IntegerParameter ruptureParam = new IntegerParameter(RUPTURE_PARAM_NAME,
       0,numRuptures-1,new Integer(0));
   parameterList.addParameter(ruptureParam);



   // make the parameters visible based on selected forecast
   //   while(it.hasNext()) parameterList.addParameter((ParameterAPI)it.next());

   listEditor= new ParameterListEditor(parameterList);
  // this.addParameters();
   // now make the editor based on the parameter list
   listEditor.setTitle( erfGuiBean.ERF_EDITOR_TITLE );

   // get the panel for increasing the font and border
   // this is hard coding for increasing the IMR font
   // the colors used here are from ParameterEditor
   JPanel panel = listEditor.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).getOuterPanel();
   TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(new Color( 80, 80, 140 ),3),"");
   titledBorder1.setTitleColor(new Color( 80, 80, 140 ));
   Font DEFAULT_LABEL_FONT = new Font( "SansSerif", Font.BOLD, 13 );
   titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
   titledBorder1.setTitle(erfGuiBean.ERF_PARAM_NAME);
   Border border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
   panel.setBorder(border1);

   this.add(listEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
   progress.dispose();
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
   if( name1.equals(erfGuiBean.ERF_PARAM_NAME) ){
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
     setParamsInForecast(erfGuiBean.getSelectedERF_Name(),Integer.parseInt(value));
     this.validate();
     this.repaint();
   }
 }

   private void jbInit() throws Exception {
    erfAdjParamButton.setText("Get All EqkRup Forecast Params");
    erfAdjParamButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        erfAdjParamButton_actionPerformed(e);
      }
    });
    erfAdjParamButton.setForeground(new Color(80,80,133));
    erfAdjParamButton.setBackground(new Color(200,200,230));
    this.setLayout(gridBagLayout1);
    sourceRupInfoText.setEditable(false);
    this.add(sourceRupInfoScroll,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    this.add(erfAdjParamButton,  new GridBagConstraints(0, 2, 1, 1, 0, 0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    sourceRupInfoScroll.getViewport().add(sourceRupInfoText, null);
  }

  void erfAdjParamButton_actionPerformed(ActionEvent e) {
    getAllERFAdjustableParams();
  }


  /**
   * This method gets the ERF adjustable Params for the selected ERF model
   * and the user has pressed the button to see adjust all the adjustable params
   */
  private void getAllERFAdjustableParams(){

    // get the selected forecast

    //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to make the update MagDist button invisible
    erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setVisible(false);
    MagFreqDistParameterEditor magDistEditor=erfGuiBean.getMagDistEditor();
    if(magDistEditor !=null)  magDistEditor.setUpdateButtonVisible(false);
    //Panel Parent
    Container parent = this;
    /*This loops over all the parent of this class until the parent is Frame(applet)
    this is required for the passing in the JDialog to keep the focus on the adjustable params
    frame*/

    while(!(parent instanceof JFrame) && parent != null)
      parent = parent.getParent();
    frame = new JDialog((JFrame)parent);
    frame.setModal(true);
    frame.setSize(300,600);
    frame.setTitle("ERF Adjustable Params");
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(erfGuiBean,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

    //Adding Button to update the forecast
    JButton button = new JButton();
    button.setText("Update Forecast");
    button.setForeground(new Color(80,80,133));
    button.setBackground(new Color(200,200,230));
    button.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       button_actionPerformed(e);
     }
    });
    frame.getContentPane().add(button,new GridBagConstraints(0, 2, 1, 1, 0.0,0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    frame.show();
    frame.pack();
  }

  void button_actionPerformed(ActionEvent e) {
    // get the selected forecast
   EqkRupForecast erf = (EqkRupForecast)erfGuiBean.getSelectedERF_Instance();
   erf.updateForecast();
   frame.dispose();
  }


  /**
   *
   * @param : Name of the Parameter
   * @returns the parameter with the name param
   */
  public ParameterAPI getParameter(String param){
    return listEditor.getParameterList().getParameter(param);
  }

  /**
   *
   * @returns the ERF_GuiBean Object
   */
  public ERF_GuiBean getERF_GuiObject(){
    return erfGuiBean;
  }




}