package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.scec.sha.gui.beans.*;
import org.scec.sha.gui.controls.GenerateHazusFilesConrolPanelAPI;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import java.awt.event.*;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.gui.ScenarioShakeMapMultipleAttenRelApp;

/**
 * <p>Title: GenerateHazusFilesControlPanel</p>
 * <p>Description: This class generates the ShapeFiles for the Hazus for the
 * selected Scenario.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GenerateHazusControlPanelForMultipleIMRs extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JTextPane infoPanel = new JTextPane();
  private BorderLayout borderLayout1 = new BorderLayout();


  //instance of the application calling this control panel.
  private ScenarioShakeMapMultipleAttenRelApp application;

  private final static String sa = AttenuationRelationship.SA_NAME;
  private final static String pga = AttenuationRelationship.PGA_NAME;
  private final static String pgv = AttenuationRelationship.PGV_NAME;


  //Stores the XYZ data set for the SA-0.3, SA-1.0, PGA and PGV
  private XYZ_DataSetAPI sa03_xyzdata;
  private XYZ_DataSetAPI sa10_xyzdata;
  private XYZ_DataSetAPI pga_xyzdata;
  private XYZ_DataSetAPI pgv_xyzdata;

  //metadata string for the different IMT required to generate the shapefiles for Hazus.
  private String metadata;
  private JButton generateHazusShapeFilesButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  //Object to get the handle to the IMT Parameter editor List.
  private ParameterListEditor imtParamEditor;
  //records if the user has pressed the button to generate the XYZ data to produce
  //the shapefiles for inout to Hazus
  boolean generatingXYZDataForShapeFiles= false;

  //progress bar
  CalcProgressBar calcProgress;

  /**
   * Class constructor.
   * This will generate the shapefiles for the input to the Hazus
   * @param parent : parent frame on which to show this control panel
   * @param editor : IMT Paramter List editor.
   */
  public GenerateHazusControlPanelForMultipleIMRs(Component parent,ParameterListEditor editor,
                                        ScenarioShakeMapMultipleAttenRelApp api) {
    imtParamEditor = editor;
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
      if(attenRel.isIntensityMeasureSupported(pgv))
        attenRelListSupportingPGV.add(attenRel);
      else
        attenRelListNotSupportingPGV.add(attenRel);
    }

    //Doing for PGV
    //if the AttenRels support PGV
    XYZ_DataSetAPI xyzDataSet_PGV =hazusCalcForPGV(attenRelListSupportingPGV,true);
    //if the AttenRels do not support PGV
    XYZ_DataSetAPI xyzDataSet = hazusCalcForPGV(attenRelListNotSupportingPGV,false);

    //ArrayLists containing the Z Values for the XYZ dataset.
    ArrayList pgvList = xyzDataSet_PGV.getZ_DataSet();
    ArrayList list = xyzDataSet.getZ_DataSet();

    size = list.size();

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


    //Doing for PGA
    hazusCalcForSA(selectedAttenRels);

    calcProgress.showProgress(false);
    calcProgress.dispose();
    imtParamEditor.refreshParamEditor();
  }

  /**
   * Hazus Calculation for PGA
   * @param selectedAttenRels: List of AttenuationRelation models
   */
  private void hazusCalcForPGA(ArrayList selectedAttenRels){
    imtParamEditor.getParameterList().getParameter(MultipleAttenuationRelationsGuiBean.IMT_PARAM_NAME).setValue(pga);
    pga_xyzdata = application.generateShakeMap(selectedAttenRels);
    metadata += imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";
  }


  /**
   * Hazus Calculation for SA at 1sec and 0.3 sec
   * @param selectedAttenRels: List of AttenuationRelation models
   */
  private void hazusCalcForSA(ArrayList selectedAttenRels){
    //Doing for SA
    imtParamEditor.getParameterList().getParameter(MultipleAttenuationRelationsGuiBean.IMT_PARAM_NAME).setValue(sa);
    //Doing for SA-0.3sec
    imtParamEditor.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME).setValue("0.3");
    sa03_xyzdata = application.generateShakeMap(selectedAttenRels);

    metadata = imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";

    //Doing for SA-1.0sec
    imtParamEditor.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME).setValue("1.0");
    sa10_xyzdata = application.generateShakeMap(selectedAttenRels);
    metadata += imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";
  }

  /**
   * Hazus Calculation for PGV
   * @param AttenRelList : List of AttenuationRelation models
   * @param pgvSupported : Checks if the list of the AttenRels support PGV
   * @return
   */
  private XYZ_DataSetAPI hazusCalcForPGV(ArrayList AttenRelList, boolean pgvSupported){
    //if the PGV is supportd by the AttenuationRelationships
    XYZ_DataSetAPI pgvDataSet = null;
    if(pgvSupported){
      imtParamEditor.getParameterList().getParameter(MultipleAttenuationRelationsGuiBean.IMT_PARAM_NAME).setValue(pgv);
      pgvDataSet = application.generateShakeMap(AttenRelList);
      metadata += imtParamEditor.getVisibleParameters().getParameterListMetadataString()+"<br>\n";
    }
    else{ //if the List of the attenRels does not support IMT then use SA at 1sec for PGV
      imtParamEditor.getParameterList().getParameter(MultipleAttenuationRelationsGuiBean.IMT_PARAM_NAME).setValue(sa);
      imtParamEditor.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME).setValue("1.0");
      pgvDataSet = application.generateShakeMap(AttenRelList);

      //if PGV is not supported by the attenuation then use the SA-1sec pd
      //and multiply the value by scaler 37.24*2.54
      ArrayList zVals = pgvDataSet.getZ_DataSet();
      int size = zVals.size();
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
    generatingXYZDataForShapeFiles = true;
  }

  /**
   * This function sets the Gridded region Sites and the type of plot user wants to see
   * IML@Prob or Prob@IML and it value.
   */
  public void getRegionAndMapType(){
    application.getGriddedSitesAndMapType();
  }

  /**
   *
   * @returns if the user has pressed the button to generate the xyz dataset
   * for prodcing the shapefiles for Hazus
   */
  public boolean isHazusShapeFilesButtonPressed(){
    return generatingXYZDataForShapeFiles;
  }

}