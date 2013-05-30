/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.gui.plot;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.Range;
import org.opensha.sha.gui.infoTools.ButtonControlPanel;

/**
 * <p>Title: GraphWindow</p>
 * <p>Description: This window pops up when the user wants to see the plot curves
 * in a separate window ( peel the plot from the original window )</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class GraphWidget extends JSplitPane {

	protected final static int W = 670;
	protected final static int H = 700;
	protected JPanel chartPane = new JPanel();
	protected GridBagLayout gridBagLayout1 = new GridBagLayout();
	protected BorderLayout borderLayout1 = new BorderLayout();
	protected JPanel buttonPanel = new JPanel();
	protected FlowLayout flowLayout1 = new FlowLayout();

	//boolean parameters for the Axis to check for log
	protected boolean xLog = false;
	protected boolean yLog = false;
	
	private Range xRange;
	private Range yRange;

	protected static int windowNumber = 1;

	protected final static String TITLE = "Plot Window - ";
	
	private PlotPreferences plotPrefs;

	//instance for the ButtonControlPanel
	protected ButtonControlPanel buttonControlPanel;

	//instance of the GraphPanel class
	protected GraphPanel graphPanel;

	/**
	 * List of ArbitrarilyDiscretized functions and Weighted funstions
	 */
	protected PlotSpec plotSpec;

	/**
	 * for Y-log, 0 values will be converted to this small value
	 */
	protected double Y_MIN_VAL = 1e-16;

	// TODO move to GraphWindow
//	protected JMenuBar menuBar = new JMenuBar();
//	protected JMenu fileMenu = new JMenu();
//
//	protected JMenuItem fileExitMenu = new JMenuItem();
//	protected JMenuItem fileSaveMenu = new JMenuItem();
//	protected JMenuItem filePrintMenu = new JCheckBoxMenuItem();
//	protected JToolBar jToolBar = new JToolBar();
//
//	protected JButton closeButton = new JButton();
//	protected ImageIcon closeFileImage = new ImageIcon(FileUtils.loadImage("icons/closeFile.png"));
//
//	protected JButton printButton = new JButton();
//	protected ImageIcon printFileImage = new ImageIcon(FileUtils.loadImage("icons/printFile.jpg"));
//
//	protected JButton saveButton = new JButton();
//	protected ImageIcon saveFileImage = new ImageIcon(FileUtils.loadImage("icons/saveFile.jpg"));


//	/**
//	 * Protected class constructor , that can be only be called from the class
//	 * inherting this class.
//	 */
//	protected GraphWindow(){}

	/**
	 *
	 * @param api : Instance of this application using this object.
	 */
	public GraphWidget(PlotSpec plotSpec) {
		this(plotSpec, PlotPreferences.getDefault(), false, false, null, null);
	}

	/**
	 * 
	 * @param plotSpec
	 * @param plotPrefs
	 * @param xLog
	 * @param yLog
	 * @param xRange
	 * @param yRange
	 */
	public GraphWidget(PlotSpec plotSpec, PlotPreferences plotPrefs, boolean xLog, boolean yLog, Range xRange, Range yRange) {
		this.plotPrefs = plotPrefs;
		this.plotSpec = plotSpec;
		graphPanel = new GraphPanel(plotPrefs);
		this.xLog = xLog;
		this.yLog = yLog;
		this.xRange = xRange;
		this.yRange = yRange;
		
		try {
			jbInit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		//increasing the window number corresponding to the new window.
		++windowNumber;
		//Recreating the chart with all the default settings that existed in the main application.
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
		this.setLayout(borderLayout1);
		// TODO move to GraphWindow
//		fileMenu.setText("File");
//		fileExitMenu.setText("Exit");
//		fileSaveMenu.setText("Save");
//		filePrintMenu.setText("Print");
//
//		fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				fileExitMenu_actionPerformed(e);
//			}
//		});
//
//		fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				fileSaveMenu_actionPerformed(e);
//			}
//		});
//
//		filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				filePrintMenu_actionPerformed(e);
//			}
//		});
//
//		closeButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent actionEvent) {
//				closeButton_actionPerformed(actionEvent);
//			}
//		});
//		printButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent actionEvent) {
//				printButton_actionPerformed(actionEvent);
//			}
//		});
//		saveButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent actionEvent) {
//				saveButton_actionPerformed(actionEvent);
//			}
//		});
//
//		menuBar.add(fileMenu);
//		fileMenu.add(fileSaveMenu);
//		fileMenu.add(filePrintMenu);
//		fileMenu.add(fileExitMenu);
//
//		setJMenuBar(menuBar);
//		closeButton.setIcon(closeFileImage);
//		closeButton.setToolTipText("Close Window");
//		Dimension d = closeButton.getSize();
//		jToolBar.add(closeButton);
//		printButton.setIcon(printFileImage);
//		printButton.setToolTipText("Print Graph");
//		printButton.setSize(d);
//		jToolBar.add(printButton);
//		saveButton.setIcon(saveFileImage);
//		saveButton.setToolTipText("Save Graph as image");
//		saveButton.setSize(d);
//		jToolBar.add(saveButton);
//		jToolBar.setFloatable(false);
//
//		this.getContentPane().add(jToolBar, BorderLayout.NORTH);

		this.setOrientation(JSplitPane.VERTICAL_SPLIT);
		chartPane.setLayout(gridBagLayout1);
		buttonPanel.setLayout(flowLayout1);
//		this.getContentPane().add(chartSplitPane, BorderLayout.CENTER);
		this.add(chartPane, JSplitPane.TOP);
		this.add(buttonPanel, JSplitPane.BOTTOM);
		this.setDividerLocation(560);
		//object for the ButtonControl Panel
		buttonControlPanel = new ButtonControlPanel(this, plotPrefs);
		buttonPanel.add(buttonControlPanel, null);
		togglePlot();
		// TODO move to GraphWindow
//		this.setTitle(TITLE + windowNumber);
	}

//	/**
//	 * File | Exit action performed.
//	 *
//	 * @param actionEvent ActionEvent
//	 */
//	protected  void fileExitMenu_actionPerformed(ActionEvent actionEvent) {
//		this.dispose();
//	}
//
//	/**
//	 * File | Exit action performed.
//	 *
//	 * @param actionEvent ActionEvent
//	 */
//	protected  void fileSaveMenu_actionPerformed(ActionEvent actionEvent) {
//		try {
//			save();
//		}
//		catch (IOException e) {
//			JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
//					JOptionPane.OK_OPTION);
//			return;
//		}
//	}
//
//	/**
//	 * File | Exit action performed.
//	 *
//	 * @param actionEvent ActionEvent
//	 */
//	protected  void filePrintMenu_actionPerformed(ActionEvent actionEvent) {
//		print();
//	}
//
//	protected  void closeButton_actionPerformed(ActionEvent actionEvent) {
//		this.dispose();
//	}
//
//	protected  void printButton_actionPerformed(ActionEvent actionEvent) {
//		print();
//	}
//
//	protected  void saveButton_actionPerformed(ActionEvent actionEvent) {
//		try {
//			save();
//		}
//		catch (IOException e) {
//			JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
//					JOptionPane.OK_OPTION);
//			return;
//		}
//	}
	
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Opens a file chooser and gives the user an opportunity to save the chart
	 * in PNG format.
	 *
	 * @throws IOException if there is an I/O error.
	 */
	public void save() throws IOException {
		graphPanel.save();
	}

	/**
	 * Save the chart in pdf format
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsPDF(String fileName) throws IOException {
		graphPanel.saveAsPDF(fileName);
	}

	/**
	 * Allows the user to save the chart as PNG.
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsPNG(String fileName) throws IOException {
		graphPanel.saveAsPNG(fileName);
	}

	/**
	 * Creates a print job for the chart.
	 */
	public void print() {
		graphPanel.print();
	}

	/**
	 *
	 * @return the custom Range for the X-Axis
	 */
	public Range getUserX_AxisRange() {
		return xRange;
	}

	/**
	 *
	 * @return the custom Range for the Y-Axis
	 */
	public Range getUserY_AxisRange() {
		return yRange;
	}

	/**
	 *
	 * @return the current Range for the X-Axis
	 */
	public Range getX_AxisRange() {
		return graphPanel.getX_AxisRange();
	}

	/**
	 *
	 * @return the current Range for the Y-Axis
	 */
	public Range getY_AxisRange() {
		return graphPanel.getY_AxisRange();
	}

	/**
	 * tells the application if the xLog is selected
	 * @param xLog : boolean
	 */
	public void setX_Log(boolean xLog) {
		this.xLog = xLog;
		drawGraph();
	}

	/**
	 * tells the application if the yLog is selected
	 * @param yLog : boolean
	 */
	public void setY_Log(boolean yLog) {
		this.yLog = yLog;
		drawGraph();
	}

	/**
	 * sets the range for X and Y axis
	 * @param xMin : minimum value for X-axis
	 * @param xMax : maximum value for X-axis
	 * @param yMin : minimum value for Y-axis
	 * @param yMax : maximum value for Y-axis
	 *
	 */
	public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
		setAxisRange(new Range(xMin, xMax), new Range(yMin, yMax));
	}
	
	public void setAxisRange(Range xRange, Range yRange) {
		this.xRange = xRange;
		this.yRange = yRange;
		drawGraph();
	}

	/**
	 * set the auto range for the axis. This function is called
	 * from the AxisLimitControlPanel
	 */
	public void setAutoRange() {
		this.xRange = null;
		this.yRange = null;
		drawGraph();
	}

	/**
	 * to draw the graph
	 */
	public void drawGraph() {
		graphPanel.drawGraphPanel(plotSpec, xLog, yLog, xRange, yRange);
		togglePlot();
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public void setPlottingFeatures(List<PlotCurveCharacterstics> curveCharacteristics) {
		plotSpec.setChars(curveCharacteristics);
		drawGraph();
	}

	//checks if the user has plot the data window or plot window
	public void togglePlot() {
		chartPane.removeAll();
		graphPanel.togglePlot();
		// TODO update button control panel
		chartPane.add(graphPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		chartPane.validate();
		chartPane.repaint();
	}

	/**
	 * 
	 * @return custom X-Axis range if supplied, else null for auto scale
	 */
	public Range getXRange() {
		return xRange;
	}

	/**
	 * 
	 * @return custom Y-Axis range if supplied, else null for auto scale
	 */
	public Range getYRange() {
		return yRange;
	}

	/**
	 *
	 * @return the plotting feature like width, color and shape type of each
	 * curve in list.
	 */
	public List<PlotCurveCharacterstics> getPlottingFeatures() {
		return plotSpec.getChars();
	}

	/**
	 *
	 * @return the X Axis Label
	 */
	public String getXAxisLabel() {
		return plotSpec.getXAxisLabel();
	}

	/**
	 *
	 * @return Y Axis Label
	 */
	public String getYAxisLabel() {
		return plotSpec.getYAxisLabel();
	}

	/**
	 *
	 * @return plot Title
	 */
	public String getPlotLabel() {
		return plotSpec.getTitle();
	}

	/**
	 *
	 * sets  X Axis Label
	 */
	public void setXAxisLabel(String xAxisLabel) {
		plotSpec.setXAxisLabel(xAxisLabel);
		this.drawGraph();
	}

	/**
	 *
	 * sets Y Axis Label
	 */
	public void setYAxisLabel(String yAxisLabel) {
		plotSpec.setYAxisLabel(yAxisLabel);
		this.drawGraph();
	}

	/**
	 *
	 * sets plot Title
	 */
	public void setPlotLabel(String plotTitle) {
		plotSpec.setTitle(plotTitle);
		this.drawGraph();
	}

	/**
	 * Set plot label font size
	 * 
	 * @param fontSize
	 */
	public void setPlotLabelFontSize(int fontSize) {
		this.plotPrefs.setPlotLabelFontSize(fontSize);
		this.drawGraph();
	}



	/**
	 * Set the tick label font size
	 * 
	 * @param fontSize
	 */
	public void setTickLabelFontSize(int fontSize) {
		this.plotPrefs.setTickLabelFontSize(fontSize);
		this.drawGraph();
	}

	/**
	 * Set the axis label font size
	 * 
	 * @param fontSize
	 */
	public void setAxisLabelFontSize(int fontSize) {
		this.plotPrefs.setAxisLabelFontSize(fontSize);
		this.drawGraph();
	}


	/**
	 *
	 * @return the tick label font
	 * Default is 10
	 */
	public int getTickLabelFontSize(){
		return plotPrefs.getTickLabelFontSize();
	}


	/**
	 *
	 * @return the axis label font size
	 * Default is 12
	 */
	public int getPlotLabelFontSize(){
		return plotPrefs.getPlotLabelFontSize();
	}
	
	public PlotPreferences getPlotPrefs() {
		return plotPrefs;
	}

	public void setPlottingOrder(DatasetRenderingOrder order) {
		graphPanel.setRenderingOrder(order);
	}
	
	public DatasetRenderingOrder getPlottingOrder() {
		return graphPanel.getRenderingOrder();
	}
	
	public void setDividerLocation(int location) {
		this.setDividerLocation(location);
	}
	
	public void setDividerLocation(double proportionalLocation) {
		this.setDividerLocation(proportionalLocation);
	}
}
