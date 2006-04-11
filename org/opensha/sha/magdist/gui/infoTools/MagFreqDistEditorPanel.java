package org.opensha.sha.magdist.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import org.opensha.sha.param.editor.MagFreqDistParameterEditor;
import java.awt.event.ActionEvent;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import java.util.ArrayList;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.data.function.EvenlyDiscretizedFunc;

/**
 * <p>Title: MagFreqDistEditorWindow</p>
 *
 * <p>Description: This class shows the Mag Freq. Dist Editor in a window.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class MagFreqDistEditorPanel
    extends JDialog implements MagFreqDistGraphPlotterAPI{
  private MagFreqDistParameterEditor magDistEditor;
  private JPanel magDistPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  //instance for the
  protected JComboBox magDistPlotListSelector;
  JButton updateButton;

  private static final String SELECT_INCREMENTAL_DIST = "Incremental Dist.";
  private static final String SELECT_MOMENT_RATE_DIST = "Moment-Rate Dist";
  private static final String SELECT_CUMULATIVE_DIST = "Cumulative Dist";

  private boolean isIncrementalPlotted,isCumulativePlotted,isMomentRatePlotted;

  private MagFreqDistGraphPlotter incrPlotWindow,cumPlotWindow,
      momentRateWindow;


  public MagFreqDistEditorPanel(MagFreqDistParameterEditor magDistEditor) {
    this.setModal(true);
    this.magDistEditor = magDistEditor;
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setTitle("Set " + magDistEditor.getParameter().getName());

    magDistPanel.setLayout(gridBagLayout1);
    this.getContentPane().add(magDistPanel, java.awt.BorderLayout.CENTER);
    magDistPanel.add(magDistEditor.getMagFreDistParameterEditor(),
                     new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
                                            , GridBagConstraints.CENTER,
                                            GridBagConstraints.BOTH,
                                            new Insets(4, 4, 4, 4), 0, 0));

    //Adding Button to update the forecast
    updateButton = new JButton();
    updateButton.setText("Done");
    updateButton.setForeground(new Color(80, 80, 133));
    //updateButton.setBackground(new Color(200, 200, 230));
    updateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateButton_actionPerformed(e);
      }
    });

    magDistPlotListSelector = new JComboBox();
    magDistPlotListSelector.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        magDistPlotListSelector_actionPerformed(e);
      }
    });


    magDistPlotListSelector.addItem("Plot");
    magDistPlotListSelector.addItem(SELECT_INCREMENTAL_DIST);
    magDistPlotListSelector.addItem(SELECT_CUMULATIVE_DIST);
    magDistPlotListSelector.addItem(SELECT_MOMENT_RATE_DIST);
    magDistPanel.add(updateButton,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    magDistPanel.add(magDistPlotListSelector,
                               new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));

    this.setSize(300, 400);
  }

  /**
   * Returns the Panel thats shows the MagFreqDist Editor.
   * @return JPanel
   */
  public JPanel getMagFreqDistEditorPanel(){
    return magDistPanel;
  }

  /**
   * This function when update Mag dist is called
   *
   * @param ae
   */
  public void updateButton_actionPerformed(ActionEvent e) {
    try{
      magDistEditor.setMagDistFromParams();
      dispose();
    }catch(RuntimeException ee){
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Incorrect Values",JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Allows the user to choose to which kind of Dist. to plot.
   * @param e ItemEvent
   */
  public void magDistPlotListSelector_actionPerformed(ActionEvent e) {

    String selectedText = (String) magDistPlotListSelector.getSelectedItem();
    magDistEditor.setMagDistFromParams();
    IncrementalMagFreqDist incrFunc = (IncrementalMagFreqDist) magDistEditor.
        getParameter().getValue();
    String magDistMetadata = "";
        magDistEditor.getMagFreDistParameterEditor().getVisibleParametersCloned().getParameterListMetadataString();

    if (selectedText.equals(SELECT_INCREMENTAL_DIST)){
      ArrayList functionList = new ArrayList();
      incrFunc.setInfo(magDistMetadata);
      incrFunc.setName("Mag - Incremental Rate Dist.");
      functionList.add(incrFunc);
      getIncrementalMagFreqDistPlot(functionList);
      incrPlotWindow.showPlotWindow();
    }
    else if (selectedText.equals(SELECT_CUMULATIVE_DIST)){
      ArrayList functionList = new ArrayList();
      EvenlyDiscretizedFunc function = incrFunc.getCumRateDist();
      function.setInfo(magDistMetadata);
      function.setName("Mag - Cumulative Rate Dist.");
      functionList.add(function);
      getCumulativeRateDistPlot(functionList);
      cumPlotWindow.showPlotWindow();
    }
    else if (selectedText.equals(SELECT_MOMENT_RATE_DIST)){
      ArrayList functionList = new ArrayList();
      EvenlyDiscretizedFunc function = incrFunc.getMomentRateDist();
      function.setInfo(magDistMetadata);
      function.setName("Mag - Moment Rate Dist.");
      functionList.add(function);
      getMomentRateDistPlot(functionList);
      momentRateWindow.showPlotWindow();
    }
    magDistPlotListSelector.setSelectedIndex(0);
  }


  /**
   * Makes the button that update Mag FreqDist , visible
   * or invisible.
   * @param showButton boolean
   */
  public void showMagDistUpdateButton(boolean showButton){
    updateButton.setVisible(showButton);
  }

  /**
   * Makes the selection list for plotting various Mag FreqDist plots, visible
   * or invisible.
   * @param showListSelection boolean
   */
  public void showMagDistPlotList(boolean showListSelection){
    magDistPlotListSelector.setVisible(showListSelection);
  }

  /**
   * Allows user to plot incremental MagFreqDist plot
   * @param functionList ArrayList
   * @return GraphPanel
   */
  public GraphPanel getIncrementalMagFreqDistPlot(ArrayList functionList) {

    isIncrementalPlotted = true;

    if (isCumulativePlotted)
      incrPlotWindow = new
          MagFreqDistGraphPlotter(this,functionList,
                                  cumPlotWindow.getPlottingFeatures());
    else if (isMomentRatePlotted)
      incrPlotWindow = new
          MagFreqDistGraphPlotter(this,functionList,
                                  momentRateWindow.getPlottingFeatures());
    else
      incrPlotWindow = new
          MagFreqDistGraphPlotter(this,functionList, null);
    incrPlotWindow.setYAxisLabel("Incr. Rate");
    return incrPlotWindow.drawGraph();
  }


  /**
   * Allows user to create the moment rate dist plot
   * @param functionList ArrayList
   * @return GraphPanel
   */
  public GraphPanel getMomentRateDistPlot(ArrayList functionList) {
    isMomentRatePlotted = true;
    if (isIncrementalPlotted)
      momentRateWindow = new
          MagFreqDistGraphPlotter(this,functionList,
                                  incrPlotWindow.getPlottingFeatures());
    else if (isCumulativePlotted)
      momentRateWindow = new
          MagFreqDistGraphPlotter(this,functionList,
                                  cumPlotWindow.getPlottingFeatures());
    else
      momentRateWindow = new
          MagFreqDistGraphPlotter(this,functionList, null);
    momentRateWindow.setYAxisLabel("Moment Rate");
    return momentRateWindow.drawGraph();
  }


  /**
   * Allows user to create the cumulative rate dist. plot
   * @param functionList ArrayList
   * @return GraphPanel
   */
  private GraphPanel getCumulativeRateDistPlot(ArrayList functionList) {

    isCumulativePlotted = true;
    if (isIncrementalPlotted)
      cumPlotWindow = new
          MagFreqDistGraphPlotter(this,functionList,
                                  incrPlotWindow.getPlottingFeatures());
    else if (isMomentRatePlotted)
      cumPlotWindow = new
          MagFreqDistGraphPlotter(this,functionList,
                                  momentRateWindow.getPlottingFeatures());
    else
      cumPlotWindow = new
          MagFreqDistGraphPlotter(this,functionList, null);
    cumPlotWindow.setYAxisLabel("Cum. Rate");
    return cumPlotWindow.drawGraph();
  }

  /**
   * Updates all the windows with the modified plot prefs.
   * @param toUpdate boolean : if plot prefs updated
   */
  public void updateMagFreqDistPlotPrefs(boolean toUpdate){
    if(isIncrementalPlotted){
      incrPlotWindow.drawGraph();
      MagFreqDistPlotWindow window= incrPlotWindow.getPlotWindow();
      if(window !=null)
        window.drawGraph();
    }
    if(isMomentRatePlotted){
      momentRateWindow.drawGraph();
      MagFreqDistPlotWindow window= momentRateWindow.getPlotWindow();
      if(window !=null)
        window.drawGraph();
    }
    if(isCumulativePlotted){
      cumPlotWindow.drawGraph();
      MagFreqDistPlotWindow window= cumPlotWindow.getPlotWindow();
      if(window !=null)
        window.drawGraph();

    }
  }


  /*public static void main(String[] args) {
    MagFreqDistEditorWindow magfreqdisteditorwindow = new
        MagFreqDistEditorWindow();
     }*/
}
