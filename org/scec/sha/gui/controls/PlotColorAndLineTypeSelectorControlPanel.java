package org.scec.sha.gui.controls;

import java.awt.event.*;

import java.awt.*;
import javax.swing.*;


import org.scec.param.DoubleParameter;
import org.scec.param.editor.DoubleParameterEditor;
import org.scec.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * <p>Title: PlotColorAndLineTypeSelectorControlPanel</p>
 * <p>Description: This class allows user to </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class PlotColorAndLineTypeSelectorControlPanel extends JFrame implements
    ActionListener{
  private JPanel jPanel1 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JScrollPane colorAndLineTypeSelectorPanel = new JScrollPane();


  //static String definitions
  private final static String colorChooserString = "Choose Color";
  private final static String lineTypeString = "Choose Line Type";
  //name of the attenuationrelationship weights parameter
  public static final String lineWidthParamName = "Size -";



  //static line types that allows user to select in combobox.
  public final static String SOLID_LINE = "Solid Line";
  public final static String DOTTED_LINE = "Dotted Line";
  public final static String DASHED_LINE = "Dash Line";
  public final static String DOT_DASH_LINE = "Dot and Dash Line";
  public final static String X = "X Symbols";
  public final static String FILLED_CIRCLES = "Filled Circles";
  public final static String CIRCLES = "Circles";
  public final static String FILLED_SQUARES = "Filled Squares";
  public final static String SQUARES = "Squares";
  public final static String FILLED_TRIANGLES = "Filled Triangles";
  public final static String TRIANGLES = "Triangles";
  public final static String FILLED_INV_TRIANGLES = "Filled Inv. Triangles";
  public final static String INV_TRIANGLES = "Inv. Triangles";
  public final static String FILLED_DIAMONDS = "Filled Diamond";
  public final static String DIAMONDS = "Diamond";
  public final static String LINE_AND_CIRCLES = "Line and Circles";
  public final static String LINE_AND_TRIANGLES = "Line and Triangles";


  //Dynamic Gui elements array to show the dataset color coding and line plot scheme
  private JLabel[] datasetSelector;
  private JButton[] colorChooserButton;
  private JComboBox[] lineTypeSelector;
  //AttenuationRelationship parameters and list declaration
  private DoubleParameter[] lineWidthParameter;
  private DoubleParameterEditor[] lineWidthParameterEditor;

  private JButton applyButton = new JButton();
  private JButton cancelButton = new JButton();
  private BorderLayout borderLayout1 = new BorderLayout();

  //Curve characterstic array
  private PlotCurveCharacterstics[] plottingFeatures;
  //default curve characterstics with values , when this control panel was called
  private PlotCurveCharacterstics[] defaultPlottingFeatures;
  private JButton RevertButton = new JButton();
  //instance of application using this control panel
  private PlotColorAndLineTypeSelectorControlPanelAPI application;
  private JPanel curveFeaturePanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JButton doneButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  public PlotColorAndLineTypeSelectorControlPanel(PlotColorAndLineTypeSelectorControlPanelAPI api,
      PlotCurveCharacterstics[] curveCharacterstics) {
    application = api;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    Component parent = (Component)api;
    // show the window at center of the parent component
     this.setLocation(parent.getX()+parent.getWidth()/2,
                      parent.getY());
    setPlotColorAndLineType(curveCharacterstics);
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText("Set Dataset color and plot line type");
    applyButton.setText("Apply");
    applyButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyButton_actionPerformed(e);
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    RevertButton.setText("Revert");
    RevertButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        RevertButton_actionPerformed(e);
      }
    });
    curveFeaturePanel.setLayout(gridBagLayout2);
    doneButton.setText("Done");
    doneButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doneButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 40, 0, 132), 107, 12));
    jPanel1.add(colorAndLineTypeSelectorPanel,  new GridBagConstraints(0, 1, 4, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 6, 0, 11), 457, 151));
    colorAndLineTypeSelectorPanel.getViewport().add(curveFeaturePanel, null);
    jPanel1.add(applyButton,  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 9, 5, 60), 6, 5));
    jPanel1.add(RevertButton,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 54, 5, 0), 6, 5));
    jPanel1.add(doneButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 9, 5, 0), 6, 5));
    jPanel1.add(cancelButton,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 8, 5, 0), 6, 5));
    jPanel1.setSize(600,300);
    //colorAndLineTypeSelectorPanel.setSize(500,250);
    setSize(600,300);
  }


  /**
   *
   */
  private void setPlotColorAndLineType(PlotCurveCharacterstics[] curveCharacterstics){
    int numCurves = curveCharacterstics.length;
    plottingFeatures = curveCharacterstics;
    defaultPlottingFeatures = new PlotCurveCharacterstics[numCurves];

    //creating the defaultPlotting features with original color scheme.
    for(int i=0;i<numCurves;++i)
      defaultPlottingFeatures[i] =new PlotCurveCharacterstics(curveCharacterstics[i].getCurveType(),
          curveCharacterstics[i].getCurveColor(),curveCharacterstics[i].getCurveWidth());


    datasetSelector = new JLabel[numCurves];
    colorChooserButton = new  JButton[numCurves];
    lineTypeSelector = new JComboBox[numCurves];
    lineWidthParameter = new DoubleParameter[numCurves];
    lineWidthParameterEditor = new DoubleParameterEditor[numCurves];
    for(int i=0;i<numCurves;++i){
      //creating the dataset Labl with the color in which they are shown in plots.
      datasetSelector[i] = new JLabel(plottingFeatures[i].getCurveName());
      datasetSelector[i].setForeground(plottingFeatures[i].getCurveColor());
      colorChooserButton[i] = new JButton(colorChooserString);
      colorChooserButton[i].addActionListener(this);
      lineTypeSelector[i] = new JComboBox();
      //adding choices to line type selector
      lineTypeSelector[i].addItem(SOLID_LINE);
      lineTypeSelector[i].addItem(DOTTED_LINE);
      lineTypeSelector[i].addItem(DASHED_LINE);
      lineTypeSelector[i].addItem(DOT_DASH_LINE);
      lineTypeSelector[i].addItem(X);
      lineTypeSelector[i].addItem(FILLED_CIRCLES);
      lineTypeSelector[i].addItem(CIRCLES);
      lineTypeSelector[i].addItem(FILLED_SQUARES);
      lineTypeSelector[i].addItem(SQUARES);
      lineTypeSelector[i].addItem(FILLED_TRIANGLES);
      lineTypeSelector[i].addItem(TRIANGLES);
      lineTypeSelector[i].addItem(FILLED_INV_TRIANGLES);
      lineTypeSelector[i].addItem(INV_TRIANGLES);
      lineTypeSelector[i].addItem(FILLED_DIAMONDS);
      lineTypeSelector[i].addItem(DIAMONDS);
      lineTypeSelector[i].addItem(LINE_AND_CIRCLES);
      lineTypeSelector[i].addItem(LINE_AND_TRIANGLES);
      //setting the selected plot type to be one currently selected.
      lineTypeSelector[i].setSelectedItem(plottingFeatures[i].getCurveType());
      lineTypeSelector[i].addActionListener(this);

      try{
        lineWidthParameter[i] = new DoubleParameter(lineWidthParamName+(i+1),0,10,
            new Double(plottingFeatures[i].getCurveWidth()));
        lineWidthParameterEditor[i] = new DoubleParameterEditor(lineWidthParameter[i]);
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    curveFeaturePanel.removeAll();
    for(int i=0;i<numCurves;++i){
      curveFeaturePanel.add(datasetSelector[i],new GridBagConstraints(0, i+1, 1, 1, 1.0, 1.0
      ,GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      curveFeaturePanel.add(colorChooserButton[i],new GridBagConstraints(1, i+1, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      curveFeaturePanel.add(lineTypeSelector[i],new GridBagConstraints(2, i+1, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
      curveFeaturePanel.add(lineWidthParameterEditor[i],new GridBagConstraints(3, i+1, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
    }
  }



  /**
   * This is a common function if any action is performed on the color chooser button
   * and plot line type selector
   * It checks what is the source of the action and depending on the source how will it
   * response to it.
   * @param e
   */
  public void actionPerformed(ActionEvent e){
    int numCurves = plottingFeatures.length;
    //checking if the source of the action was the button
    if(e.getSource() instanceof JButton){
      Object button = e.getSource();
      //if the source of the event was color button
      for(int i=0;i<numCurves;++i){
        if(button.equals(colorChooserButton[i])){
          Color color = JColorChooser.showDialog(this,"Select Color",plottingFeatures[i].getCurveColor());
          plottingFeatures[i].setCurveColor(color);
          datasetSelector[i].setForeground(color);
        }
      }
    }
    else if(e.getSource() instanceof JComboBox){
      Object comboBox = e.getSource();
      //if the source of the event was color button
      for(int i=0;i<numCurves;++i){
        if(comboBox.equals(lineTypeSelector[i]))
          plottingFeatures[i].setCurveType((String)lineTypeSelector[i].getSelectedItem());
      }
    }
  }




  /**
   * Apply changes to the Plot and keeps the control panel for user to view the results
   * @param e
   */
  void applyButton_actionPerformed(ActionEvent e) {
    applyChangesToPlot();
  }

  private void applyChangesToPlot(){
    int numCurves = plottingFeatures.length;
    //getting the line width parameter
    for(int i=0;i<numCurves;++i)
      plottingFeatures[i].setCurveWidth(((Double)lineWidthParameterEditor[i].getParameter().getValue()).doubleValue());
    application.drawGraph(plottingFeatures);
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    revertPlotToOriginal();
    this.dispose();
  }

  /**
   * Restoring the original values for plotting features
   * @param e
   */
  void RevertButton_actionPerformed(ActionEvent e) {
    int flag =JOptionPane.showConfirmDialog(this,"Restore Original Values","Reverting changes",JOptionPane.OK_CANCEL_OPTION);
    if(flag == JOptionPane.OK_OPTION){
      revertPlotToOriginal();
    }
  }

  private void revertPlotToOriginal(){
    int numCurves = defaultPlottingFeatures.length;
    for(int i=0;i<numCurves;++i){
      datasetSelector[i].setForeground(defaultPlottingFeatures[i].getCurveColor());
      plottingFeatures[i].setCurveColor(defaultPlottingFeatures[i].getCurveColor());
      //setting the selected plot type to be one currently selected.
      lineTypeSelector[i].setSelectedItem(defaultPlottingFeatures[i].getCurveType());
      plottingFeatures[i].setCurveType(defaultPlottingFeatures[i].getCurveType());
      lineWidthParameterEditor[i].setValue(new Double(defaultPlottingFeatures[i].getCurveWidth()));
      lineWidthParameterEditor[i].refreshParamEditor();
      plottingFeatures[i].setCurveWidth(defaultPlottingFeatures[i].getCurveWidth());
      curveFeaturePanel.repaint();
      curveFeaturePanel.validate();
      application.drawGraph(plottingFeatures);
    }
  }

  /**
   * Apply all changes to Plot and closes the control window
   * @param e
   */
  void doneButton_actionPerformed(ActionEvent e) {
    applyChangesToPlot();
    this.dispose();
  }
}
