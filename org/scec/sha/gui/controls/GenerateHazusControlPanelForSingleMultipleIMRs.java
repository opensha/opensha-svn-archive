package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;


import org.scec.sha.gui.beans.*;
import org.scec.sha.gui.controls.GenerateHazusFilesConrolPanelAPI;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.param.*;
import org.scec.sha.gui.ScenarioShakeMapAttenRelApp_Temp;

/**
 * <p>Title: GenerateHazusControlPanelForSingleMultipleIMRs</p>
 * <p>Description: This class generates the ShapeFiles for the Hazus for the
 * selected Scenario.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GenerateHazusControlPanelForSingleMultipleIMRs extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JTextPane infoPanel = new JTextPane();
  private BorderLayout borderLayout1 = new BorderLayout();


  //instance of the application calling this control panel.
  private ScenarioShakeMapAttenRelApp_Temp application;


  //Stores the XYZ data set for the SA-0.3, SA-1.0, PGA and PGV
  private XYZ_DataSetAPI sa03_xyzdata;
  private XYZ_DataSetAPI sa10_xyzdata;
  private XYZ_DataSetAPI pga_xyzdata;
  private XYZ_DataSetAPI pgv_xyzdata;

  //metadata string for the different IMT required to generate the shapefiles for Hazus.
  private String metadata;
  private JButton generateHazusShapeFilesButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  //records if the user has pressed the button to generate the XYZ data to produce
  //the shapefiles for inout to Hazus
  boolean generatingXYZDataForShapeFiles= false;

  //progress bar
  CalcProgressBar calcProgress;

  /**
   * Class constructor.
   * This will generate the shapefiles for the input to the Hazus
   * @param parent : parent frame on which to show this control panel
   * @param api : Instance of the application using this control panel
   */
  public GenerateHazusControlPanelForSingleMultipleIMRs(Component parent,
                                        ScenarioShakeMapAttenRelApp_Temp api) {


    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
                     parent.getY()+parent.getHeight()/2);
    //save the instance of the application
    this.application = api;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    this.setTitle("Hazus Shapefiles Control");
    infoPanel.setBackground(SystemColor.menu);
    infoPanel.setEnabled(false);
    String info = new String("This generates the Hazus shapefiles (sa-0.3sec,"+
                             " sa-1.0sec, pga and pgv) for the selected scenario.");
    infoPanel.setPreferredSize(new Dimension(812, 16));
    infoPanel.setEditable(false);
    infoPanel.setText(info);
    jPanel1.setMinimumSize(new Dimension(350, 70));
    jPanel1.setPreferredSize(new Dimension(350, 125));
    generateHazusShapeFilesButton.setText("Generate Hazus Shape Files");
    generateHazusShapeFilesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        generateHazusShapeFilesButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(infoPanel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 42, 19, 41), 0, 0));
    jPanel1.add(generateHazusShapeFilesButton,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(14, 49, 6, 54), 87, 8));
  }


  /**
   * Generate the dataset to make shapefiles that goes as input to Hazus.
   * For that it iterates over all the following IMT(SA-1sec, SA-0.3sec, PGA and PGV) to
   * create the dataset for them.
   * @param selectedAttenRels : List of the selected AttenuationRelationships selected
   */
  private void generateHazusFiles(ArrayList selectedAttenRels){

    //doing for SA
    hazusCalcForSA(selectedAttenRels);

    //creating the 2 seperate list for the attenRels selected, for one suuporting
    //the PGV and results calculated using PGV and other not supporting PGV and result
    //calculated using the SA at 1sec and multiplying by 37.24*2.54.
    ArrayList attenRelListSupportingPGV = new ArrayList();

    ArrayList attenRelListNotSupportingPGV = new ArrayList();
    int size = selectedAttenRels.size();
    for(int i=0;i<size;++i){
      AttenuationRelationship attenRel = (AttenuationRelationship)selectedAttenRels.get(i);
      if(attenRel.isIntensityMeasureSupported(AttenuationRelationship.PGV_NAME))
        attenRelListSupportingPGV.add(attenRel);
      else
        attenRelListNotSupportingPGV.add(attenRel);
    }

    //Doing for PGV
    //arrayList declaration for the Atten Rel not supporting PGV
    ArrayList list = null;
    //arrayList declaration for the Atten Rel supporting PGV
    ArrayList pgvList = null;
    //XYZ data set supporting the PGV
    XYZ_DataSetAPI xyzDataSet_PGV = null;
    //XYZ data set not supporting the PGV
    XYZ_DataSetAPI xyzDataSet = null;

    if(attenRelListSupportingPGV.size() >0){
      //if the AttenRels support PGV
      xyzDataSet_PGV =hazusCalcForPGV(attenRelListSupportingPGV,true);
      //ArrayLists containing the Z Values for the XYZ dataset.
      pgvList = xyzDataSet_PGV.getZ_DataSet();
      size = pgvList.size();
    }

    if(attenRelListNotSupportingPGV.size()>0){
      //if the AttenRels do not support PGV
      xyzDataSet = hazusCalcForPGV(attenRelListNotSupportingPGV,false);
      //ArrayLists containing the Z Values for the XYZ dataset for attenRel not supporting PGV.
      list = xyzDataSet.getZ_DataSet();
      size = list.size();
    }

    if(xyzDataSet_PGV != null && xyzDataSet!=null){
      //ArrayList to store the combine( added) result(from Atten that support PGV
      //and that do not support PGV) of the Z Values for the PGV.
      ArrayList finalPGV_Vals = new ArrayList();
      //adding the values from both the above list for PGV( one calculated using PGV
      //and other calculated using the SA at 1sec and mutipling by the scalar 37.24*2.54).
      for(int i=0;i<size;++i)
        finalPGV_Vals.add(new Double(((Double)pgvList.get(i)).doubleValue()+((Double)list.get(i)).doubleValue()));
      //creating the final dataste for the PGV dataset.
      pgv_xyzdata = new ArbDiscretizedXYZ_DataSet(xyzDataSet_PGV.getX_DataSet(),
          xyzDataSet_PGV.getY_DataSet(),finalPGV_Vals);
    }
    else{
      //if XYZ dataset supporting PGV is null
      if(xyzDataSet_PGV ==null)
        pgv_xyzdata = xyzDataSet;
      //if XYZ dataset not supporting PGV is null
      else if(xyzDataSet ==null)
        pgv_xyzdata = xyzDataSet_PGV;

    }

    //Doing for PGA
    hazusCalcForPGA(selectedAttenRels);

    calcProgress.showProgress(false);
    calcProgress.dispose();
    //imtParamEditor.refreshParamEditor();
  }

  /**
   * Hazus Calculation for PGA
   * @param selectedAttenRels: List of AttenuationRelation models
   */
  private void hazusCalcForPGA(ArrayList selectedAttenRels){

    int size = selectedAttenRels.size();
    for(int i=0;i<size;++i)
      ((AttenuationRelationshipAPI)selectedAttenRels.get(i)).setIntensityMeasure(AttenuationRelationship.PGA_NAME);

    pga_xyzdata = application.generateShakeMap(selectedAttenRels);
    //metadata += imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";
  }


  /**
   * Hazus Calculation for SA at 1sec and 0.3 sec
   * @param selectedAttenRels: List of AttenuationRelation models
   */
  private void hazusCalcForSA(ArrayList selectedAttenRels){
    //Doing for SA
    int size = selectedAttenRels.size();
    for(int i=0;i<size;++i)
      ((AttenuationRelationshipAPI)selectedAttenRels.get(i)).setIntensityMeasure(AttenuationRelationship.SA_NAME);

    //Doing for SA-0.3sec
    setSA_PeriodForSelectedIMRs(selectedAttenRels,0.3);
    sa03_xyzdata = application.generateShakeMap(selectedAttenRels);
    //metadata = imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";

    //Doing for SA-1.0sec
    setSA_PeriodForSelectedIMRs(selectedAttenRels,1.0);
    sa10_xyzdata = application.generateShakeMap(selectedAttenRels);
    //metadata += imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";
  }

  /**
   * Hazus Calculation for PGV
   * @param AttenRelList : List of AttenuationRelation models
   * @param pgvSupported : Checks if the list of the AttenRels support PGV
   * @return
   */
  private XYZ_DataSetAPI hazusCalcForPGV(ArrayList attenRelList, boolean pgvSupported){
    //if the PGV is supportd by the AttenuationRelationships
    XYZ_DataSetAPI pgvDataSet = null;
    int size = attenRelList.size();
    if(pgvSupported){
      for(int i=0;i<size;++i)
        ((AttenuationRelationshipAPI)attenRelList.get(i)).setIntensityMeasure(AttenuationRelationship.PGV_NAME);

      pgvDataSet = application.generateShakeMap(attenRelList);
      //metadata += imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";
    }
    else{ //if the List of the attenRels does not support IMT then use SA at 1sec for PGV
      for(int i=0;i<size;++i)
        ((AttenuationRelationshipAPI)attenRelList.get(i)).setIntensityMeasure(AttenuationRelationship.SA_NAME);
      this.setSA_PeriodForSelectedIMRs(attenRelList,1.0);

      pgvDataSet = application.generateShakeMap(attenRelList);

      //if PGV is not supported by the attenuation then use the SA-1sec pd
      //and multiply the value by scaler 37.24*2.54
      ArrayList zVals = pgvDataSet.getZ_DataSet();
      size = zVals.size();
      ArrayList newZVals = new ArrayList();
      for(int i=0;i<size;++i){
        double val = ((Double)zVals.get(i)).doubleValue()*37.24*2.54;
        newZVals.add(new Double(val));
      }
      pgvDataSet = new ArbDiscretizedXYZ_DataSet(pgvDataSet.getX_DataSet(),
          pgvDataSet.getY_DataSet(), newZVals);
      metadata += "IMT: PGV"+"<br>\n";
    }
    return pgvDataSet;
  }


  /**
   * sets the SA Period in selected IMR's with the argument period
   */
  private void setSA_PeriodForSelectedIMRs(ArrayList selectedAttenRels, double period) {
    int size = selectedAttenRels.size();
    for(int i=0;i<size;++i)
      ((AttenuationRelationshipAPI)selectedAttenRels.get(i)).getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(period));
  }


  /**
   *
   * @returns the metadata for the IMT GUI if this control panel is selected
   */
  public String getIMT_Metadata(){
    return metadata;
  }


  /**
   *
   * @returns the XYZ data set for the SA-0.3sec
   */
  public XYZ_DataSetAPI getXYZ_DataForSA_03(){
    return sa03_xyzdata;
  }


  /**
   *
   * @return the XYZ data set for the SA-1.0sec
   */
  public XYZ_DataSetAPI getXYZ_DataForSA_10(){
    return sa10_xyzdata;
  }

  /**
   *
   * @return the XYZ data set for the PGA
   */
  public XYZ_DataSetAPI getXYZ_DataForPGA(){
    return pga_xyzdata;
  }

  /**
   *
   * @return the XYZ data set for the PGV
   */
  public XYZ_DataSetAPI getXYZ_DataForPGV(){
    return pgv_xyzdata;
  }

  void generateHazusShapeFilesButton_actionPerformed(ActionEvent e) {
    getRegionAndMapType();
    generateShapeFilesForHazus();
  }

  /**
   * Creates the dataset to generate the shape files that goes as input to Hazus.
   */
  public void generateShapeFilesForHazus(){
    calcProgress = new CalcProgressBar("Hazus Shape file data","Starting Calculation...");
    calcProgress.setProgressMessage("Doing Calculation for the Hazus ShapeFile Data...");
    generateHazusFiles(application.getSelectedAttenuationRelationships());
    //keeps tracks if the user has pressed the button to generate the xyz dataset
    //for prodcing the shapefiles for Hazus.
    setGenerateShapeFilesForHazus(true);
  }

  /**
   * Function accepts true if user wants to generate the hazus shapefiles. On setting
   * to false it does not generate the hazus shape files. User has to explicitly
   * set to false if he does not want to generate the shapefiles for hazus once he
   * has pressed button to generate the shape files for hazus which sets this
   * generateHazusShapeFiles to true. This function has to be explicitly have to be
   * called with false in order not to generate the shape files.
   * @param generateHazusShapeFiles
   */
  public void setGenerateShapeFilesForHazus(boolean generateHazusShapeFiles){
    generatingXYZDataForShapeFiles = generateHazusShapeFiles;
  }

  /**
   * This function sets the Gridded region Sites and the type of plot user wants to see
   * IML@Prob or Prob@IML and it value.
   */
  public void getRegionAndMapType(){
    application.getGriddedSitesMapTypeAndSelectedAttenRels();
  }

  /**
   *
   * @returns if the generate shape files for Hazus being done.
   * If returns then files for hazus will be generated else if returns
   * false then files are not being generated.
   */
  public boolean isGenerateShapeFilesForHazus(){
    return generatingXYZDataForShapeFiles;
  }

}