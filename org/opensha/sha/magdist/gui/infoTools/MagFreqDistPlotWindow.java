package org.opensha.sha.magdist.gui.infoTools;

import org.opensha.sha.gui.infoTools.*;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import javax.swing.JTabbedPane;

/**
 * <p>Title:MagFreqDistPlotWindow </p>
 *
 * <p>Description: Shows the Mag Dist plot in a pop up window.
 * This class was created so that if Plot Preferences are changed for
 * any of the Dist types then other plots are changed automatically.
 * For eg: If plot preferences are changed for Incremental Mag Freq. Dist then
 * they should be reflected in Cum. Mag Freq Dist plots or moment-rate plots.</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class MagFreqDistPlotWindow
    extends GraphWindow {


  private String plotTitle = "Mag.Freq.Dist.";
  private MagFreqDistPlotWindowAPI application;
  private JTabbedPane tabPane;



    /**
     *
     * @param api : Instance of this application using this object.
     */
   public MagFreqDistPlotWindow(MagFreqDistPlotWindowAPI api,GraphPanel graphPanel) {
     application = api;
     this.graphPanel = graphPanel;
     this.functionList = api.getCurveFunctionList();
     xAxisName = api.getXAxisLabel();
     yAxisName = api.getYAxisLabel();

     try {
        jbInit();
      }
      catch (Exception e) {
        e.printStackTrace();
      }

     //increasing the window number corresponding to the new window.
     ++windowNumber;
     /**
      * Recreating the chart with all the default settings that existed in the main application.
      */
     xLog = api.getXLog();
     yLog = api.getYLog();
     customAxis = api.isCustomAxis();
     if (customAxis)
       buttonControlPanel.setAxisRange(api.getMinX(), api.getMaxX(), api.getMinY(),
                                       api.getMaxY());
     if (xLog)
       buttonControlPanel.setXLog(xLog);
     if (yLog)
       buttonControlPanel.setYLog(yLog);
     if (!xLog && !yLog)
       drawGraph();
 }


 //function to create the GUI component.
 protected void jbInit() throws Exception {
   this.setSize(W, H);
   this.getContentPane().setLayout(borderLayout1);
   fileMenu.setText("File");
   fileExitMenu.setText("Exit");
   fileSaveMenu.setText("Save");
   filePrintMenu.setText("Print");

   fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       fileExitMenu_actionPerformed(e);
     }
   });

   fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       fileSaveMenu_actionPerformed(e);
     }
   });

   filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       filePrintMenu_actionPerformed(e);
     }
   });

   closeButton.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent actionEvent) {
       closeButton_actionPerformed(actionEvent);
     }
   });
   printButton.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent actionEvent) {
       printButton_actionPerformed(actionEvent);
     }
   });
   saveButton.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent actionEvent) {
       saveButton_actionPerformed(actionEvent);
     }
   });

   menuBar.add(fileMenu);
   fileMenu.add(fileSaveMenu);
   fileMenu.add(filePrintMenu);
   fileMenu.add(fileExitMenu);

   setJMenuBar(menuBar);
   closeButton.setIcon(closeFileImage);
   closeButton.setToolTipText("Close Window");
   Dimension d = closeButton.getSize();
   jToolBar.add(closeButton);
   printButton.setIcon(printFileImage);
   printButton.setToolTipText("Print Graph");
   printButton.setSize(d);
   jToolBar.add(printButton);
   saveButton.setIcon(saveFileImage);
   saveButton.setToolTipText("Save Graph as image");
   saveButton.setSize(d);
   jToolBar.add(saveButton);
   jToolBar.setFloatable(false);

   this.getContentPane().add(jToolBar, BorderLayout.NORTH);

   chartSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
   chartPane.setLayout(gridBagLayout1);
   buttonPanel.setLayout(flowLayout1);
   this.getContentPane().add(chartSplitPane, BorderLayout.CENTER);
   chartSplitPane.add(chartPane, JSplitPane.TOP);
   chartSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
   chartSplitPane.setDividerLocation(580);
   //object for the ButtonControl Panel
   buttonControlPanel = new ButtonControlPanel(this);
   buttonPanel.add(buttonControlPanel, null);
   togglePlot();
   this.setTitle(TITLE + windowNumber);
 }


 /**
  * Creates the graph. This method is different from  plotGraphUsingPlotPreferences
  * as it does not send the event back to the application that Plot Prefs have changed.
  */
  public void drawGraph() {
    super.drawGraph();
  }

 /**
  * plots the curves with defined color,line width and shape.
  * @param plotFeatures
  */
 public void plotGraphUsingPlotPreferences() {
   drawGraph();
   application.setPlotPreferencesChanged(true);
 }


}
