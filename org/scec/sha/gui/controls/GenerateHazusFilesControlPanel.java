package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.scec.sha.gui.beans.*;
import org.scec.sha.gui.controls.GenerateHazusFilesConrolPanelAPI;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;


/**
 * <p>Title: GenerateHazusFilesControlPanel</p>
 * <p>Description: This class generates the ShapeFiles for the Hazus for the
 * selected Scenario.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GenerateHazusFilesControlPanel extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JCheckBox hazusFilesCheck = new JCheckBox();
  private JTextPane infoPanel = new JTextPane();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();


  //instance of the application calling this control panel.
  private GenerateHazusFilesConrolPanelAPI application;

  //Stores the XYZ data set for the SA-0.3, SA-1.0, PGA and PGV
  private XYZ_DataSetAPI sa03_xyzdata;
  private XYZ_DataSetAPI sa10_xyzdata;
  private XYZ_DataSetAPI pga_xyzdata;
  private XYZ_DataSetAPI pgv_xyzdata;

  //metadata string for the different IMT required to generate the shapefiles for Hazus.
  private String metadata;

  /**
   * Class constructor.
   * This will generate the shapefiles for the input to the Hazus
   * @param parent : parent frame on which to show this control panel
   * @param imrGuiBean :object of IMT_GuiBean to set the imt.
   */
  public GenerateHazusFilesControlPanel(Component parent,
                                        GenerateHazusFilesConrolPanelAPI api) {
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
    hazusFilesCheck.setText("Generate ShapeFiles for Hazus");
    infoPanel.setBackground(SystemColor.menu);
    infoPanel.setEnabled(false);
    String info = new String("This generates the Hazus shapefiles (sa-0.3sec,"+
                             " sa-1.0sec, pga and pgv) for the selected scenario.");
    infoPanel.setPreferredSize(new Dimension(812, 16));
    infoPanel.setEditable(false);
    infoPanel.setText(info);
    jPanel1.setMinimumSize(new Dimension(350, 70));
    jPanel1.setPreferredSize(new Dimension(350, 125));
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(infoPanel,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 42, 19, 41), -557, 3));
    jPanel1.add(hazusFilesCheck,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(18, 42, 0, 83), 52, 5));
  }


  /**
   *
   * @param imtGuiBean : instance of the selected IMT
   * @param imr : instance of the selected IMR
   */
  public void generateHazusFiles(IMT_GuiBean imtGuiBean,AttenuationRelationship imr){

      String sa = AttenuationRelationship.SA_NAME;
      String pga = AttenuationRelationship.PGA_NAME;
      String pgv = AttenuationRelationship.PGV_NAME;

      //Doing for SA
      imtGuiBean.getParameterList().getParameter(imtGuiBean.IMT_PARAM_NAME).setValue(sa);
      //Doing for SA-0.3sec
      imtGuiBean.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME).setValue("0.3");
      sa03_xyzdata = application.generateShakeMap();

      metadata = imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+"<br>\n";

      //Doing for SA-1.0sec
      imtGuiBean.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME).setValue("1.0");
      sa10_xyzdata = application.generateShakeMap();
      metadata += imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+"<br>\n";

      //Doing for PGV
      if(imr.isIntensityMeasureSupported(pgv)){
        //if the PGV is supportd by the AttenuationRelationship
        imtGuiBean.getParameterList().getParameter(imtGuiBean.IMT_PARAM_NAME).setValue(pgv);
        pgv_xyzdata = application.generateShakeMap();
        metadata += imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+"<br>\n";
      }
      else{
        //if PGV is not supported by the attenuation then use the SA-1sec pd
        //and multiply the value by scaler 37.24*2.54
        Vector zVals = sa10_xyzdata.getZ_DataSet();
        int size = zVals.size();
        Vector newZVals = new Vector();
        for(int i=0;i<size;++i){
          double val = ((Double)zVals.get(i)).doubleValue()*37.24*2.54;
          newZVals.add(new Double(val));
        }
        pgv_xyzdata = new ArbDiscretizedXYZ_DataSet(sa10_xyzdata.getX_DataSet(),
                      sa10_xyzdata.getY_DataSet(), newZVals);
        metadata += "IMT: PGV"+"<br>\n";
      }
      //Doing for PGA
      imtGuiBean.getParameterList().getParameter(imtGuiBean.IMT_PARAM_NAME).setValue(pga);
      pga_xyzdata = application.generateShakeMap();
      metadata += imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+"<br>\n";
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
   * @returns if the ShapeFiles for Hazus have to be generated
   */
  public boolean isHazusShapeFilesControlSelected(){
    return hazusFilesCheck.isSelected();
  }


  /**
   * Sets the value for checkbox for shape files for Hazus
   * @param flag
   */
  public void setHazusShapeFilesControlSelected(boolean flag){
    hazusFilesCheck.setSelected(flag);
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

}