package org.scec.sha.gui.controls;

import java.awt.event.*;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import org.scec.param.DoubleParameter;
import org.scec.param.StringParameter;
import org.scec.param.editor.ConstrainedStringParameterEditor;
import org.scec.param.editor.ConstrainedDoubleParameterEditor;
import org.scec.param.DoubleConstraint;
import org.scec.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * <p>Title: PlotColorAndLineTypeSelectorControlPanel</p>
 * <p>Description: This class allows user to select different plotting
 * styles for curves. Here user can specify color, curve style and
 * it size. the default value for lines are 1.0f and and for shapes
 * it is 4.0f.
 * Currently supported Styles are:
 * SOLID_LINE
 * DOTTED_LINE
 * DASHED_LINE
 * DOT_DASH_LINE
 * X Symbols
 * CROSS_SYMBOLS
 * FILLED_CIRCLES
 * CIRCLES
 * FILLED_SQUARES
 * SQUARES
 * FILLED_TRIANGLES
 * TRIANGLES
 * FILLED_INV_TRIANGLES
 * INV_TRIANGLES
 * FILLED_DIAMONDS
 * DIAMONDS
 * LINE_AND_CIRCLES
 * LINE_AND_TRIANGLES</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
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
  public final static String CROSS_SYMBOLS = "+ Symbols";
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

  //parameter for tick label font size
  private  StringParameter tickFontSizeParam;
  public static final String tickFontSizeParamName = "Set tick label size";
  private ConstrainedStringParameterEditor tickFontSizeParamEditor;

  //parameter for axis label font size
  private StringParameter axisLabelsFontSizeParam;
  public static final String axislabelsFontSizeParamName = "Set axis label size";
  private ConstrainedStringParameterEditor axisLabelsFontSizeParamEditor;


  //Dynamic Gui elements array to show the dataset color coding and line plot scheme
  private JLabel[] datasetSelector;
  private JButton[] colorChooserButton;
  private JComboBox[] lineTypeSelector;
  //AttenuationRelationship parameters and list declaration
  private DoubleParameter[] lineWidthParameter;
  private ConstrainedDoubleParameterEditor[] lineWidthParameterEditor;

  private JButton applyButton = new JButton();
  private JButton cancelButton = new JButton();
  private BorderLayout borderLayout1 = new BorderLayout();

  //Curve characterstic array
  private ArrayList plottingFeatures;
  //default curve characterstics with values , when this control panel was called
  private ArrayList defaultPlottingFeatures;
  private JButton RevertButton = new JButton();
  //instance of application using this control panel
  private PlotColorAndLineTypeSelectorControlPanelAPI application;
  private JPanel curveFeaturePanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JButton doneButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  public PlotColorAndLineTypeSelectorControlPanel(PlotColorAndLineTypeSelectorControlPanelAPI api,
      ArrayList curveCharacterstics) {
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
    jLabel1.setFont(new java.awt.Font("Arial", 0, 18));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel1.setText("Plot Characterstics Settings Control Panel");
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
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 6, 0, 11), 248, 12));
    jPanel1.add(colorAndLineTypeSelectorPanel,  new GridBagConstraints(0, 1, 4, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 6, 0, 11), 557, 200));
    colorAndLineTypeSelectorPanel.getViewport().add(curveFeaturePanel, null);
    jPanel1.add(cancelButton,  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 22, 2, 108), 2, 5));
    jPanel1.add(RevertButton,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 21, 2, 0), 0, 5));
    jPanel1.add(doneButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 22, 2, 0), -6, 5));
    jPanel1.add(applyButton,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 99, 2, 0), -4, 5));
    jPanel1.setSize(600,300);
    //colorAndLineTypeSelectorPanel.setSize(500,250);
    setSize(600,300);
  }


  /**
   * creates the control panel with plotting characterstics for each curve in list.
   * This function shows plotting characterstics (curve style, color, and its width)
   * for each curve in list ,so creates these gui components dynamically based on
   * number of functions in list.
   */
  public void setPlotColorAndLineType(ArrayList curveCharacterstics){
    int numCurves = curveCharacterstics.size();
    plottingFeatures = curveCharacterstics;
    defaultPlottingFeatures = new ArrayList();

    //creating the defaultPlotting features with original color scheme.
    for(int i=0;i<numCurves;++i){
      PlotCurveCharacterstics curvePlotPref = (PlotCurveCharacterstics)plottingFeatures.get(i);
      defaultPlottingFeatures.add(new PlotCurveCharacterstics(curvePlotPref.getCurveType(),
          curvePlotPref.getCurveColor(),curvePlotPref.getCurveWidth()));
    }



    //creating list of supported font sizes
    ArrayList supportedFontSizes = new ArrayList();

    supportedFontSizes.add("8");
    supportedFontSizes.add("10");
    supportedFontSizes.add("12");
    supportedFontSizes.add("14");
    supportedFontSizes.add("16");
    supportedFontSizes.add("18");
    supportedFontSizes.add("20");
    supportedFontSizes.add("22");
    supportedFontSizes.add("24");

    //creating the font size parameters
    tickFontSizeParam = new StringParameter(tickFontSizeParamName,supportedFontSizes,(String)supportedFontSizes.get(1));
    axisLabelsFontSizeParam = new StringParameter(axislabelsFontSizeParamName,supportedFontSizes,(String)supportedFontSizes.get(2));

    //creating editors for these font size parameters
    try{
      tickFontSizeParamEditor = new ConstrainedStringParameterEditor(tickFontSizeParam);
      axisLabelsFontSizeParamEditor = new ConstrainedStringParameterEditor(axisLabelsFontSizeParam);
    }catch(Exception e){
      e.printStackTrace();
    }


    datasetSelector = new JLabel[numCurves];
    colorChooserButton = new  JButton[numCurves];
    lineTypeSelector = new JComboBox[numCurves];
    lineWidthParameter = new DoubleParameter[numCurves];
    lineWidthParameterEditor = new ConstrainedDoubleParameterEditor[numCurves];
    DoubleConstraint sizeConstraint = new DoubleConstraint(0,20);
    for(int i=0;i<numCurves;++i){
      PlotCurveCharacterstics curvePlotPref = (PlotCurveCharacterstics)plottingFeatures.get(i);
      //creating the dataset Labl with the color in which they are shown in plots.
      datasetSelector[i] = new JLabel(curvePlotPref.getCurveName());
      datasetSelector[i].setForeground(curvePlotPref.getCurveColor());
      colorChooserButton[i] = new JButton(colorChooserString);
      colorChooserButton[i].addActionListener(this);
      lineTypeSelector[i] = new JComboBox();
      //adding choices to line type selector
      lineTypeSelector[i].addItem(SOLID_LINE);
      lineTypeSelector[i].addItem(DOTTED_LINE);
      lineTypeSelector[i].addItem(DASHED_LINE);
      lineTypeSelector[i].addItem(DOT_DASH_LINE);
      lineTypeSelector[i].addItem(X);
      lineTypeSelector[i].addItem(CROSS_SYMBOLS);
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
      lineTypeSelector[i].setSelectedItem(curvePlotPref.getCurveType());
      lineTypeSelector[i].addActionListener(this);

      try{
        //creating double parameter for size of each curve.
        lineWidthParameter[i] = new DoubleParameter(lineWidthParamName+(i+1),sizeConstraint,
            new Double(curvePlotPref.getCurveWidth()));

        lineWidthParameterEditor[i] = new ConstrainedDoubleParameterEditor(lineWidthParameter[i]);
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    curveFeaturePanel.removeAll();
    //adding color chooser button,plot style and size to GUI.
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
    curveFeaturePanel.add(tickFontSizeParamEditor,new GridBagConstraints(1, numCurves+1, 1, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
    curveFeaturePanel.add(axisLabelsFontSizeParamEditor,new GridBagConstraints(2, numCurves+1, 1, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.WEST, new Insets(4, 3, 5, 5), 0, 0));
  }



  /**
   * This is a common function if any action is performed on the color chooser button
   * and plot line type selector
   * It checks what is the source of the action and depending on the source how will it
   * response to it.
   * @param e
   */
  public void actionPerformed(ActionEvent e){
    int numCurves = plottingFeatures.size();
    //checking if the source of the action was the button
    if(e.getSource() instanceof JButton){
      Object button = e.getSource();
      //if the source of the event was color button
      for(int i=0;i<numCurves;++i){
        PlotCurveCharacterstics curvePlotPref = (PlotCurveCharacterstics)plottingFeatures.get(i);
        if(button.equals(colorChooserButton[i])){
          Color color = JColorChooser.showDialog(this,"Select Color",curvePlotPref.getCurveColor());
          //chnage the default color only if user has selected a new color , else leave it the way it is
          if(color !=null){
            curvePlotPref.setCurveColor(color);
            datasetSelector[i].setForeground(color);
          }
        }
      }
    }
    else if(e.getSource() instanceof JComboBox){
      Object comboBox = e.getSource();
      //if the source of the event was color button
      int itemIndex=0;
      for(int i=0;i<numCurves;++i){
        PlotCurveCharacterstics curvePlotPref = (PlotCurveCharacterstics)plottingFeatures.get(i);
        if(comboBox.equals(lineTypeSelector[i])){
          curvePlotPref.setCurveType((String)lineTypeSelector[i].getSelectedItem());
          itemIndex= i;
          break;
        }
      }
      //method to set the default value for line and shapes
      setStyleSizeBasedOnSelectedShape(itemIndex,(String)lineTypeSelector[itemIndex].getSelectedItem());
    }
  }


  /**
   * Set the default size value based on the selected Style. For line it is 1.0f
   * and for shapes it is 4.0f.
   * @param index : Curve index
   * @param selectedStyle
   */
  private void setStyleSizeBasedOnSelectedShape(int index,String selectedStyle){

    if(selectedStyle.equals(this.SOLID_LINE) || selectedStyle.equals(this.DASHED_LINE) ||
       selectedStyle.equals(this.DOTTED_LINE) || selectedStyle.equals(this.DOT_DASH_LINE))
      lineWidthParameterEditor[index].setValue(new Double(1.0));
    else if(selectedStyle.equals(this.LINE_AND_CIRCLES) || selectedStyle.equals(this.LINE_AND_TRIANGLES))
      lineWidthParameterEditor[index].setValue(new Double(1.0));
    else
     lineWidthParameterEditor[index].setValue(new Double(4.0));
    lineWidthParameterEditor[index].refreshParamEditor();
  }


  /**
   * Apply changes to the Plot and keeps the control panel for user to view the results
   * @param e
   */
  void applyButton_actionPerformed(ActionEvent e) {
    applyChangesToPlot();
  }

  private void applyChangesToPlot(){
    int numCurves = plottingFeatures.size();
    //getting the line width parameter
    for(int i=0;i<numCurves;++i)
      ((PlotCurveCharacterstics)plottingFeatures.get(i)).setCurveWidth(((Double)lineWidthParameterEditor[i].getParameter().getValue()).doubleValue());
    application.plotGraphUsingPlotPreferences();
  }

  /**
   * reverts the plots to original values and close the window
   * @param e
   */
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
    int numCurves = defaultPlottingFeatures.size();
    for(int i=0;i<numCurves;++i){
      PlotCurveCharacterstics curveCharacterstics = (PlotCurveCharacterstics)defaultPlottingFeatures.get(i);
      datasetSelector[i].setForeground(curveCharacterstics.getCurveColor());
      ((PlotCurveCharacterstics)plottingFeatures.get(i)).setCurveColor(curveCharacterstics.getCurveColor());
      //setting the selected plot type to be one currently selected.
      lineTypeSelector[i].setSelectedItem(curveCharacterstics.getCurveType());
      ((PlotCurveCharacterstics)plottingFeatures.get(i)).setCurveType(curveCharacterstics.getCurveType());
      lineWidthParameterEditor[i].setValue(new Double(curveCharacterstics.getCurveWidth()));
      lineWidthParameterEditor[i].refreshParamEditor();
      ((PlotCurveCharacterstics)plottingFeatures.get(i)).setCurveWidth(curveCharacterstics.getCurveWidth());
      curveFeaturePanel.repaint();
      curveFeaturePanel.validate();
      application.plotGraphUsingPlotPreferences();
    }
  }

  /**
   *
   * @returns axis label font size
   */
  public int getAxisLabelFontSize(){
    return  Integer.parseInt((String)axisLabelsFontSizeParam.getValue());
  }

  /**
   *
   * @returns the tick label font size
   */
  public int getTickLabelFontSize(){
    return  Integer.parseInt((String)tickFontSizeParam.getValue());
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
