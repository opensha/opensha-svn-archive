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

package org.opensha.sha.gui.infoTools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.commons.lang3.SystemUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.ui.RectangleInsets;
import org.opensha.commons.data.function.AbstractXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.opensha.commons.gui.plot.jfreechart.JFreeLogarithmicAxis;
import org.opensha.commons.gui.plot.jfreechart.MyTickUnits;
import org.opensha.commons.util.CustomFileFilter;
import org.opensha.commons.util.FileUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;



/**
 * <p>Title: GraphPanel</p>
 * <p>Description: This class shows the JFreechart Panel in a window. It plot curves
 * using JFrechart package and if application supports allowing user to specify
 * different styles, colors and width of each curve the this application plots that
 * for the person.</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class GraphPanel extends JSplitPane {



	// mesage needed in case of show data if plot is not available
	private final static String NO_PLOT_MSG = "No Plot Data Available";
	
	private JFileChooser chooser;
	
	private DatasetRenderingOrder renderingOrder = DatasetRenderingOrder.FORWARD;

	/**
	 * default color scheme for plotting curves
	 */
	public static final Color[] defaultColor = {Color.red,Color.blue,Color.green,Color.darkGray,Color.magenta,Color.cyan,
			Color.orange,Color.pink,Color.yellow,Color.gray};


	private SimpleAttributeSet setLegend;

	// accessible components
	//private JSplitPane chartSplitPane;
	private JPanel chartPane;
	private JTextPane metadataText;
	private JScrollPane dataScrollPane;
	private JTextArea dataTextArea;
	private ChartPanel chartPanel;

	// these are coordinates and size of the circles visible in the plot
	private final static double SIZE = 2;
	private final static double DELTA = SIZE / 2.0;

	private static Dimension minPanelSize = new Dimension(320,120);

	//dataset to handover the data to JFreechart
	private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();
	//list containing Discretized function set
	private XY_DataSetList totalProbFuncs = new XY_DataSetList();

	//checks if weighted function exists in the list of functions
	private int weightedfuncListIndex;

	/**
	 * for Y-log, 0 values will be converted to this small value
	 */
	private double Y_MIN_VAL = 1e-16;


	private XYPlot plot;

	// Create the x-axis and y-axis - either normal or log
	//xaxis1 and yAxis1 replica of the x-Axis and y-Axis object, in case error occurs
	//and we have revert back the Axis
	ValueAxis xAxis, xAxis1 ;
	ValueAxis yAxis, yAxis1;
	
	private boolean xAxisInverted = false;
	private boolean yAxisInverted = false;

	// light blue color
	private Color backgroundColor = new Color( 200, 200, 230 );

	//Keeps track when to toggle between the data and chart.
	private boolean graphOn = false;


	//Instance of the application using this class
	GraphPanelAPI application;



	//List of PlotCurveCharacterstics for each curve
	//that we plot which include the line color,line width.
	private ArrayList<PlotCurveCharacterstics> curvePlottingCharacterstics = new ArrayList<PlotCurveCharacterstics>();

	//This ArrayList stores the legend for various
	private ArrayList<String> legendString;

	/**
	 * class constructor
	 * @param api : Application instance
	 */
	public GraphPanel(GraphPanelAPI api) {
		super(JSplitPane.VERTICAL_SPLIT, true);
		setResizeWeight(1);
		setBorder(null);

		data.setFunctions(this.totalProbFuncs);
		// for Y-log, convert 0 values in Y axis to this small value, it just sets the minimum
		//value
		data.setConvertZeroToMin(true,Y_MIN_VAL);
		//instance of application using this class.
		application = api;
		try {
			jbInit();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Function to add GUI component to Graphpanel class
	 * @throws Exception
	 */
	void jbInit() throws Exception {

		dataTextArea = new JTextArea(NO_PLOT_MSG);
		//dataTextArea.setBorder(BorderFactory.createEtchedBorder());
		dataTextArea.setLineWrap(true);

		dataScrollPane = new JScrollPane();
		//dataScrollPane.setBorder(BorderFactory.createEtchedBorder());
		dataScrollPane.getViewport().add(dataTextArea, null);

		chartPane = new JPanel(new BorderLayout());
		chartPane.setMinimumSize(minPanelSize);
		chartPane.setPreferredSize(minPanelSize);

		metadataText = new JTextPane();
		metadataText.setEditable(false);
		JScrollPane metadataScrollPane = new JScrollPane();
		metadataScrollPane.getViewport().add(metadataText);
		metadataScrollPane.setMinimumSize(minPanelSize);
		metadataScrollPane.setPreferredSize(minPanelSize);
		metadataScrollPane.setBorder(
				BorderFactory.createLineBorder(Color.gray,1));


		setTopComponent(chartPane);
		setBottomComponent(metadataScrollPane);

	}


	/**
	 * For each function in the list it sets the plotting characeterstics of the curve
	 * so that when that list is given to JFreechart , it creates it with these characterstics.
	 * @param lineType : Plotting style
	 * @param color : Plotting cure color
	 * @param curveWidth : size of each plot
	 * @param functionIndex : secondary datset index.
	 * This method creates a new renderer for each dataset based on user's selected
	 * plotting style.If index is zero then set primary renderer else set secondary renderer
	 */
	private void drawCurvesUsingPlottingFeatures(
			PlotLineType lineType, float lineWidth,
			PlotSymbol symbol, float symbolWidth,
			Color color, int functionIndex){
		XYItemRenderer renderer = PlotLineType.buildRenderer(lineType, symbol, lineWidth, symbolWidth);
		setRendererInPlot(color, functionIndex, renderer);
	}


	private void setRendererInPlot(Color color, int functionIndex,
			XYItemRenderer xyItemRenderer) {
		plot.setRenderer(functionIndex,xyItemRenderer);
		xyItemRenderer.setPaint(color);
	}

	private boolean isBlankCurve(PlotCurveCharacterstics chars) {
		return (chars.getLineType() == null || chars.getLineWidth() <= 0f)
				&& (chars.getSymbol() == null || chars.getSymbolWidth() <= 0f);
	}

	/**
	 * Draws curves
	 * @param xAxisName : X-Axis Label
	 * @param yAxisName : Y-Axis Label
	 * @param funcList  : ArrayList containing individual functions and weighted functionlist
	 * @param xLog      : boolean tell if xLog is selected
	 * @param yLog      : boolean tells if yLog is selected
	 * @param customAxis : boolean tells if graph needs to ne plotted using custom axis range
	 * @param title  : JFreechart window title
	 * @param buttonControlPanel : Instance of class which called this method.
	 */
	public void drawGraphPanel(String xAxisName, String yAxisName,ArrayList funcList,
			boolean xLog,boolean yLog,boolean customAxis, String title,
			PlotControllerAPI buttonControlPanel ) {

		// Starting
		String S = "drawGraphPanel(): ";


		createColorSchemeAndFunctionList(funcList);


		//flags to check if the exception was thrown on selection of the x-log or y-log.
		boolean logErrorFlag = false;

		//getting the axis font size
		int axisFontSize = buttonControlPanel.getAxisLabelFontSize();
		//getting the tick label font size
		int tickFontSize = buttonControlPanel.getTickLabelFontSize();

		//create the standard ticks so that smaller values too can plotted on the chart
		TickUnits units = MyTickUnits.createStandardTickUnits();

		try{

			/// check if x log is selected or not
			if (xLog) {
				JFreeLogarithmicAxis logAxis = new JFreeLogarithmicAxis(xAxisName);
				// this fixes the overlap issue with the bottom of the plot
				logAxis.setVerticalAnchorShift(4);
				xAxis = logAxis;
			}
			else xAxis = new NumberAxis( xAxisName );

			//if (!xLog)
			//  xAxis.setAutoRangeIncludesZero(true);
			// else
			if (xAxis instanceof NumberAxis)
				((NumberAxis)xAxis).setAutoRangeIncludesZero( false );
			xAxis.setStandardTickUnits(units);
			xAxis.setTickMarksVisible(false);
			xAxis.setTickLabelInsets(new RectangleInsets(3, 10, 3, 10));
			//Axis label font
			Font axisLabelFont = xAxis.getLabelFont();
			xAxis.setLabelFont(new Font(axisLabelFont.getFontName(),axisLabelFont.getStyle(),axisFontSize));

			//tick label font
			Font axisTickFont = xAxis.getTickLabelFont();
			xAxis.setTickLabelFont(new Font(axisTickFont.getFontName(),axisTickFont.getStyle(),tickFontSize));
			
			if (xAxisInverted)
				xAxis.setInverted(true);

			//added to have the minimum range within the Upper and Lower Bound of the Axis
			//xAxis.setAutoRangeMinimumSize(.1);

			/* to set the range of the axis on the input from the user if the range combo box is selected*/
			if(customAxis)
				xAxis.setRange(application.getUserMinX(),application.getUserMaxX());

		}catch(Exception e){
			//e.printStackTrace();
			JOptionPane.showMessageDialog(this,e.getMessage(),"X-Plot Error",JOptionPane.OK_OPTION);
			graphOn=false;
			xLog = false;
			buttonControlPanel.setXLog(xLog);
			xAxis = xAxis1;
			logErrorFlag = true;
		}

		try{
			/// check if y log is selected or not
			if(yLog) yAxis = new JFreeLogarithmicAxis(yAxisName);
			else yAxis = new NumberAxis( yAxisName );

			if (yAxis instanceof NumberAxis) {
				if (!yLog)
					((NumberAxis)yAxis).setAutoRangeIncludesZero(true);
				else
					((NumberAxis)yAxis).setAutoRangeIncludesZero( false );
			}

			yAxis.setStandardTickUnits(units);
			yAxis.setTickMarksVisible(false);

			//Axis label font
			Font axisLabelFont = yAxis.getLabelFont();
			yAxis.setLabelFont(new Font(axisLabelFont.getFontName(),axisLabelFont.getStyle(),axisFontSize));

			//tick label font
			Font axisTickFont = yAxis.getTickLabelFont();
			yAxis.setTickLabelFont(new Font(axisTickFont.getFontName(),axisTickFont.getStyle(),tickFontSize));
			//added to have the minimum range within the Upper and Lower Bound of the Axis
			//yAxis.setAutoRangeMinimumSize(.1);
			
			if (yAxisInverted)
				yAxis.setInverted(true);

			/* to set the range of the axis on the input from the user if the range combo box is selected*/
			if(customAxis)
				yAxis.setRange(application.getUserMinY(),application.getUserMaxY());

		}catch(Exception e){
			//e.printStackTrace();
			JOptionPane.showMessageDialog(this,e.getMessage(),"Y-Plot Error",JOptionPane.OK_OPTION);
			graphOn=false;
			yLog = false;
			buttonControlPanel.setYLog(yLog);
			yAxis = yAxis1;
			logErrorFlag = false;
		}


		plot = null;
		// build the plot
		plot = new XYPlot(null, xAxis, yAxis, null);

		//setting the plot properties
		plot.setDomainCrosshairLockedOnData(false);
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairLockedOnData(false);
		plot.setRangeCrosshairVisible(false);
		plot.setInsets(new RectangleInsets(10, 0, 0, tickFontSize+15));


		//total number of funtions that need to be plotted differently using different characterstics
		int numFuncs = curvePlottingCharacterstics.size();
		//index of dataset from total prob functionlist (list containing each curve as
		//individual discretized function).
		int datasetIndex = 0;

		//secondarydataset index keeps track where do we have to add the seconadary data set in plot
		for(int j=0,dataIndex=0; j < numFuncs; ++j,++dataIndex){
			PlotCurveCharacterstics curveCharaceterstic = curvePlottingCharacterstics.get(j);
			//getting the number of consecutive curves that have same plotting characterstics.
			int numCurves = curveCharaceterstic.getNumContinuousCurvesWithSameCharacterstics();
			if (isBlankCurve(curveCharaceterstic)) {
				//adding the number of consecutive curves with same plotting characterstics to dataset index.
				datasetIndex +=numCurves;
				//decrement the secondary dataset index so that we seconday dataset is added to correct place.
				--dataIndex;
				continue;
			}
			Color color = curveCharaceterstic.getColor();
			float lineWidth = curveCharaceterstic.getLineWidth();
			PlotLineType lineType = curveCharaceterstic.getLineType();
			float symbolWidth = curveCharaceterstic.getSymbolWidth();
			PlotSymbol symbol = curveCharaceterstic.getSymbol();
			
			//creating dataset for each curve and its consecutive curves which have same plotting
			//characterstics. Eg: can be weighted functions in weighted functionlist  have same
			//plotting characterstics, also fractiles in weighted function list share same
			//plotting characterstics. So creating dataset for each list of curves with
			//same plotting characterstics.
			XY_DataSetList dataFunctions = new XY_DataSetList();
			DiscretizedFunctionXYDataSet dataset = new DiscretizedFunctionXYDataSet();
			dataset.setXLog(xLog);
			dataset.setYLog(yLog);
			//converting the zero in Y-axis to some minimum value.
			dataset.setConvertZeroToMin(true,Y_MIN_VAL);
			dataset.setFunctions(dataFunctions);


			//creating the secondary dataset to show it in different color and shapes
			for(int i=datasetIndex;i<(datasetIndex+numCurves);++i){
				if (i >= totalProbFuncs.size())
					break;
				dataFunctions.add(totalProbFuncs.get(i));
			}
			datasetIndex +=numCurves;

			//adding the dataset to the plot
			plot.setDataset(dataIndex,dataset);

			//based on plotting characterstics for each curve sending configuring plot object
			//to be send to JFreechart for plotting.
			drawCurvesUsingPlottingFeatures(lineType, lineWidth, symbol, symbolWidth, color, dataIndex);
		}
		plot.setDatasetRenderingOrder(renderingOrder);

		plot.setBackgroundAlpha( .8f );

		//getting the tick label font size
		int plotLabelFontSize = buttonControlPanel.getPlotLabelFontSize();

		Font defaultPlotLabelFont = JFreeChart.DEFAULT_TITLE_FONT;
		Font newPlotLabelFont = new Font(defaultPlotLabelFont.getFontName(),defaultPlotLabelFont.getStyle(),plotLabelFontSize);

		//giving off all the data that needs to be plotted to JFreechart, which return backs
		//a panel fo curves,
		JFreeChart chart = new JFreeChart(title, newPlotLabelFont, plot, false );

		chart.setBackgroundPaint( backgroundColor );

		// Put into a panel
		chartPanel = new ChartPanel(chart, true, true, true, true, false);

		//chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) ); TODO clean
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.gray,1));
		chartPanel.setMouseZoomable(true);
		chartPanel.setDisplayToolTips(true);
		chartPanel.setHorizontalAxisTrace(false);
		chartPanel.setVerticalAxisTrace(false);

		// set the font of legend
		int numOfColors = plot.getSeriesCount();

		/**
		 * Adding the metadata text to the Window below the Chart
		 */
		metadataText.removeAll();
		metadataText.setEditable(false);
		setLegend =new SimpleAttributeSet();
		setLegend.addAttribute(StyleConstants.CharacterConstants.Bold,
				Boolean.TRUE);
		javax.swing.text.Document doc = metadataText.getStyledDocument();

		weightedfuncListIndex = -1;
		try {

			/**
			 * formatting the metadata to be added , according to the colors of the
			 * Curves. So now curves and metadata will be displayed in the same color.
			 */
			doc.remove(0,doc.getLength());
			//total number of elements in the list containing individual functions and
			//weighted function list.
			int totalNumofFunctions = funcList.size();
			legendString = new ArrayList();
			//getting the metadata associated with each function in the list
			for(int i=0,plotPrefIndex=0;i<totalNumofFunctions;++i){
				String legend=null;
				//setting the font style for the legend
				setLegend =new SimpleAttributeSet();
				StyleConstants.setFontSize(setLegend,12);
				//checking if element in the list is weighted function list object
				Object obj = funcList.get(i);
				String datasetName = "DATASET #"+(i+1);
				if(obj instanceof WeightedFuncListforPlotting){
					//getting the metadata for weighted functionlist
					WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;

					String listInfo = weightedList.getInfo();

					legend = new String(datasetName+"\n"+
							listInfo+SystemUtils.LINE_SEPARATOR);
					legendString.add(legend);
					StyleConstants.setForeground(setLegend,Color.black);
					doc.insertString(doc.getLength(),legend,setLegend);
					//index where the weighted function list exits if it does in the list of functions.
					weightedfuncListIndex = legendString.size()-1;
					//checking if individual curves need to be plotted
					if(weightedList.areIndividualCurvesToPlot()){
						(this.curvePlottingCharacterstics.get(plotPrefIndex)).setName(datasetName+" Curves");

						//getting the metadata for each individual curves and creating the legend string
						String listFunctionsInfo = weightedList.getFunctionTraceInfo();

						legend = new String(listFunctionsInfo+SystemUtils.LINE_SEPARATOR);
						legendString.add(legend);
						Color color = (this.curvePlottingCharacterstics.get(plotPrefIndex)).getColor();
						StyleConstants.setForeground(setLegend,color);
						doc.insertString(doc.getLength(),legend,setLegend);
						++plotPrefIndex;
					}
					//checking if fractiles need to be plotted
					if(weightedList.areFractilesToPlot()){
						(this.curvePlottingCharacterstics.get(plotPrefIndex)).setName(
								datasetName+" Fractiles");

						//getting the fractile info for the weighted function list and adding that to the legend
						String fractileListInfo = weightedList.getFractileInfo();

						legend = new String(fractileListInfo+SystemUtils.LINE_SEPARATOR);
						legendString.add(legend);
						Color color = (this.curvePlottingCharacterstics.get(plotPrefIndex)).getColor();
						StyleConstants.setForeground(setLegend,color);
						doc.insertString(doc.getLength(),legend,setLegend);
						++plotPrefIndex;
					}
					//checking if mean fractile need to be plotted
					if(weightedList.isMeanToPlot()){
						(this.curvePlottingCharacterstics.get(plotPrefIndex)).setName(
								datasetName+" Mean");
						//getting the fractileinfo and showing it as legend
						String meanInfo = weightedList.getMeanFunctionInfo();

						legend = new String(meanInfo+SystemUtils.LINE_SEPARATOR);
						legendString.add(legend);
						Color color = (this.curvePlottingCharacterstics.get(plotPrefIndex)).getColor();
						StyleConstants.setForeground(setLegend,color);
						doc.insertString(doc.getLength(),legend,setLegend);
						++plotPrefIndex;
					}
				}
				else{ //if element in the list are individual function then get their info and show as legend
					(this.curvePlottingCharacterstics.get(plotPrefIndex)).setName(
							datasetName);
					XY_DataSet func = (XY_DataSet)funcList.get(i);
					String functionInfo = func.getInfo();
					String name = func.getName();
					legend = new String(datasetName+"\n"+
							name+"  "+SystemUtils.LINE_SEPARATOR+
							functionInfo+SystemUtils.LINE_SEPARATOR);
					legendString.add(legend);
					Color color = (this.curvePlottingCharacterstics.get(plotPrefIndex)).getColor();
					StyleConstants.setForeground(setLegend,color);
					doc.insertString(doc.getLength(),legend,setLegend);
					++plotPrefIndex;
				}
			}
		} catch (BadLocationException e) {
			return;
		}
		graphOn=false;

		//Check to see if there is no log Error and only  xLog or yLog are selected
		if(!logErrorFlag && !xLog)
			xAxis1 = xAxis;
		if(!logErrorFlag && !yLog)
			yAxis1 = yAxis;

		//setting the info in the
		dataTextArea.setText(this.showDataInWindow(funcList,xAxisName,yAxisName));
		return ;
	}

	/**
	 *
	 * @param funcList
	 * @param xAxisName
	 * @param yAxisName
	 * @return data to be shown in the data window
	 */
	private String showDataInWindow(ArrayList funcList,String xAxisName,String yAxisName){

		int size = funcList.size();

		StringBuffer b = new StringBuffer();
		b.append("\n");
		b.append("X-Axis: " + xAxisName + '\n');
		b.append("Y-Axis: " + yAxisName + '\n');
		b.append("Number of Data Sets: " + size + "\n\n");


		for(int i=0;i<size;++i){
			Object obj = funcList.get(i);

			if(!(obj instanceof WeightedFuncListforPlotting)){ //showing data for the individual function
				XY_DataSet function = (XY_DataSet)obj;
				b.append("\nDATASET #" + (i+1) + "\n\n");
				b.append(function.toString()+ '\n');
			}
			else{ //showing data for weighted function list
				WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;
				b.append("\nDATASET #" + (i+1) + "   Weighted Function List"+'\n');
				b.append(weightedList.getInfo()+"\n\n");
				//checking if individual curves need to be plotted
				if(weightedList.areIndividualCurvesToPlot()){
					//getting the metadata for each individual curves and creating the legend string
					XY_DataSetList list = weightedList.getWeightedFunctionList();
					ArrayList wtList = weightedList.getRelativeWtList();
					int listSize = list.size();
					for(int j=0;j<listSize;++j){
						b.append("\nFunction #"+(j+1)+" of "+listSize+", from Dataset #"+(i+1)+
								", with relative wt = "+(Double)wtList.get(j)+"\n");
						DiscretizedFunc function = (DiscretizedFunc)list.get(j);
						b.append(function.getMetadataString()+ '\n');
					}
				}
				//checking if fractiles need to be plotted
				if(weightedList.areFractilesToPlot()){

					//getting the fractile info for the weighted function list and adding that to the legend
					XY_DataSetList list = weightedList.getFractileList();
					ArrayList fractileValueList = weightedList.getFractileValuesList();
					int listSize = list.size();
					for(int j=0;j<listSize;++j){
						b.append("\n"+(Double)fractileValueList.get(j)+" Fractile for Dataset #"+(i+1)+"\n");
						DiscretizedFunc function = (DiscretizedFunc)list.get(j);
						b.append(function.getMetadataString()+ '\n');
					}
				}

				//checking if mean fractile need to be plotted
				if(weightedList.isMeanToPlot()){
					//getting the fractileinfo and showing it as legend
					b.append("\nMean for Dataset #"+(i+1)+"\n");
					b.append(weightedList.getMean().getMetadataString()+"\n");
				}
			}
		}

		return b.toString();
	}


	/**
	 * Sets the metadata in the Data window
	 * @param metadata
	 */
	public void setMetadata(String metadata){
		dataTextArea.setText(metadata);
	}
	
	public void setBackgroundColor(Color background) {
		this.backgroundColor = background;
	}


	/**
	 * Clears the plot and the Metadata Window
	 */
	public void removeChartAndMetadata(){
		chartPane.removeAll();
		chartPanel = null;
		metadataText.setText("");
		dataTextArea.setText(this.NO_PLOT_MSG);
		curvePlottingCharacterstics.clear();
	}


	/**
	 *  Toggle between showing the graph and showing the actual data
	 */
	public void togglePlot(ButtonControlPanel buttonControlPanel) {

		chartPane.removeAll();
		//showing the data window
		if ( graphOn ) {
			if (buttonControlPanel != null)
				buttonControlPanel.setToggleButtonText( "Show Plot" );
			graphOn = false;

			chartPane.add(dataScrollPane, BorderLayout.CENTER);
			//      chartPane.add(dataScrollPane,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
			//          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
		}
		else {
			//showing the Plot window, if not null
			graphOn = true;
			if (buttonControlPanel != null)
				buttonControlPanel.setToggleButtonText("Show Data");
			// panel added here
			if(chartPanel !=null) {
				chartPane.add(chartPanel, BorderLayout.CENTER);
				//        chartPane.add(chartPanel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0 TODO clean
				//          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

			} else {
				chartPane.add(dataScrollPane, BorderLayout.CENTER);
				//    	  chartPane.add(dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
				//    	          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
			}

		}
		return ;
	}

	/**
	 * sets the backgound for the plot
	 * @param color
	 */
	public void setPlotBackgroundColor(Color color){
		if(plot !=null)
			plot.setBackgroundPaint(color);
	}
	/**
	 *
	 * @return the Range for the X-Axis
	 */
	public Range getX_AxisRange(){
		return xAxis.getRange();
	}

	/**
	 *
	 * @return the Range for the Y-Axis
	 */
	public Range getY_AxisRange(){
		return yAxis.getRange();
	}
	
	public ValueAxis getXAxis() {
		return xAxis;
	}
	
	public ValueAxis getYAxis() {
		return yAxis;
	}


	/**
	 *
	 * @return the list PlotCurveCharacterstics that contain the info about
	 * plotting the curve like plot line color , its width and line type.
	 */
	public ArrayList<PlotCurveCharacterstics> getCurvePlottingCharacterstic(){
		return curvePlottingCharacterstics;
	}


	/**
	 * This sets the plotting prefences for the curves. It takes in the
	 * list of PlotCurveCharacterstics and apply it to each curve in the list.
	 *
	 * @param plotPrefsList: List of PlotCurveCharacterstics for all curves.
	 */
	public void setCurvePlottingCharacterstic(ArrayList plotPrefsList){
		curvePlottingCharacterstics = plotPrefsList;
	}


	/**
	 * This method extracts all the functions from the ArrayList and add that
	 * to the DiscretizedFunction List. This method also creates the color scheme
	 * depending on the different types of DiscretizedFunc added to the list.
	 * @param functionList
	 */
	private void createColorSchemeAndFunctionList(ArrayList functionList){

		totalProbFuncs.clear();
		int numCurves  = functionList.size();
		ArrayList<Integer> numColorArray = new ArrayList<Integer>();


		for(int i=0;i<numCurves;++i){

			Object obj = functionList.get(i);
			if(obj instanceof WeightedFuncListforPlotting){
				WeightedFuncListforPlotting weightedList = (WeightedFuncListforPlotting)obj;
				if(weightedList.areIndividualCurvesToPlot()){
					XY_DataSetList list= weightedList.getWeightedFunctionList();
					//list.get(0).setInfo(weightedList.getInfo()+"\n"+"(a) "+list.getInfo());
					numColorArray.add(new Integer(list.size()));
					totalProbFuncs.addAll(list);
				}
				if(weightedList.areFractilesToPlot()){
					XY_DataSetList list= weightedList.getFractileList();
					// list.get(0).setInfo("(b) "+list.getInfo());
					totalProbFuncs.addAll(list);
					numColorArray.add(new Integer(list.size()));
				}
				if(weightedList.isMeanToPlot()){
					AbstractXY_DataSet meanFunc = weightedList.getMean();
					//String info = meanFunc.getInfo();
					//meanFunc.setInfo("(c) "+info);
					totalProbFuncs.add(meanFunc);
					numColorArray.add(new Integer(1));
				}
			}
			else{
				totalProbFuncs.add((XY_DataSet)obj);
				numColorArray.add(new Integer(1));
			}
		}


		//number of different curves with different plotting characterstics.
		int existingCurvesWithPlotPrefs = this.curvePlottingCharacterstics.size();

		int numDiffColors = numColorArray.size();

		//looping over all the default colors to add those to the color array
		for(int i=0,defaultColorIndex =0;i<numDiffColors;++i,++defaultColorIndex){
			//if the number of curves to be drawn are more in number then default colors then start from first again
			if(defaultColorIndex == defaultColor.length)
				defaultColorIndex = 0;
			int val = ((Integer)numColorArray.get(i)).intValue();
			//adding the new curves to the list for plot preferences.
			if(i>=existingCurvesWithPlotPrefs) {
				XY_DataSet func = totalProbFuncs.get(i);
				PlotLineType lineType;
				PlotSymbol symbol;
				if (func instanceof DiscretizedFunc) {
					lineType = PlotLineType.SOLID;
					symbol = null;
				} else {
					lineType = null;
					symbol = PlotSymbol.DIAMOND;
				}
				curvePlottingCharacterstics.add(new PlotCurveCharacterstics(lineType, 1f, symbol, 4f,
						defaultColor[defaultColorIndex],val));
			}
		}
	}

	/**
	 * Opens a file chooser and gives the user an opportunity to save the chart
	 * in PDF/PNG/TXT format.
	 *
	 * @throws IOException if there is an I/O error.
	 */
	public void save() throws IOException {
		if (chooser == null) {
			chooser = new JFileChooser();
			CustomFileFilter pdfFF = new CustomFileFilter("pdf", "PDF File");
			CustomFileFilter pngFF = new CustomFileFilter("png", "PNG File");
			CustomFileFilter txtFF = new CustomFileFilter("txt", "TXT File");
			
			chooser.addChoosableFileFilter(pdfFF);
			chooser.addChoosableFileFilter(pngFF);
			chooser.addChoosableFileFilter(txtFF);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(pdfFF);
		}
		int option = chooser.showSaveDialog(this);
		String fileName = null;
		if (option == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
			CustomFileFilter filter = (CustomFileFilter) chooser.getFileFilter();
			String ext = filter.getExtension();
			System.out.println(ext);
			if (!fileName.toLowerCase().endsWith(ext)) {
				fileName = fileName + ext;
			}
			if (ext.equals(".pdf")) {
				saveAsPDF(fileName);
			} else if (ext.equals(".png")) {
				saveAsPNG(fileName);
			} else if (ext.equals(".txt")) {
				saveAsTXT(fileName);
			} else {
				throw new RuntimeException("Unknown extension selected: "+ext);
			}
		}
	}

	/**
	 * Allows the user to save the chart as PNG.
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsPNG(String fileName) throws IOException {
		saveAsPNG(fileName, chartPanel.getWidth(), chartPanel.getHeight());
	}

	/**
	 * Allows the user to save the chart as TXT
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsTXT(String fileName) throws IOException {
		FileUtils.save(fileName, dataTextArea.getText());
	}
	
	/**
	 * Allows the user to save the chart as PNG.
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsPNG(String fileName, int width, int height) throws IOException {
		ChartUtilities.saveChartAsPNG(new File(fileName),chartPanel.getChart() , 
				width, height);
	}

	/**
	 * Allows the user to save the chart contents and metadata as PDF.
	 * This allows to preserve the color coding of the metadata.
	 * @throws IOException
	 */
	public void saveAsPDF(String fileName) throws IOException {
		int width = chartPanel.getWidth();
		int height = chartPanel.getHeight();
		this.saveAsPDF(fileName, width, height);
	}

	/**
	 * Allows the user to save the chart contents and metadata as PDF.
	 * This allows to preserve the color coding of the metadata.
	 * @throws IOException
	 */
	public void saveAsPDF(String fileName, int width, int height) throws IOException {
		int textLength = metadataText.getStyledDocument().getLength();
		int totalLength = textLength + height;
		// step 1
		Document metadataDocument = new Document(new com.lowagie.text.Rectangle(
				width, height));
		metadataDocument.addAuthor("OpenSHA");
		metadataDocument.addCreationDate();
		HeaderFooter footer = new HeaderFooter(new Phrase("Powered by OpenSHA"), true);
		metadataDocument.setFooter(footer);
		try {
			// step 2
			PdfWriter writer;

			writer = PdfWriter.getInstance(metadataDocument,
					new FileOutputStream(fileName));
			// step 3
			metadataDocument.open();
			// step 4
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2d = tp.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
			chartPanel.getChart().draw(g2d, r2d);
			g2d.dispose();
			cb.addTemplate(tp, 0, 0);
			//starts the metadata from the new page.
			metadataDocument.newPage();
			int size = legendString.size();
			for (int i = 0, legendColor = 0; i < size; ++i, ++legendColor) {
				com.lowagie.text.Paragraph para = new com.lowagie.text.Paragraph();
				//checks to see if the WeightFuncList exists in the list of functions
				//then plot it in black else plot in the same as the legend
				if (weightedfuncListIndex != -1 && weightedfuncListIndex == i) {
					para.add(new Phrase( (String) legendString.get(i),
							FontFactory.getFont(
									FontFactory.HELVETICA, 10, Font.PLAIN,
									Color.black)));
					--legendColor;
				}
				else {
					para.add(new Phrase( (String) legendString.get(i),
							FontFactory.getFont(
									FontFactory.HELVETICA, 10, Font.PLAIN,
									( 
											curvePlottingCharacterstics.get(legendColor)).
											getColor())));
				}
				metadataDocument.add(para);
			}
		}
		catch (DocumentException de) {
			de.printStackTrace();
		}
		// step 5
		metadataDocument.close();
	}


	/**
	 * Creates a print job for the chart if plot is being shown, else print
	 * the chart data if data window is visible.
	 * @param frame JFrame Instance of the Frame using this GraphPanel class
	 */
	public void print(JFrame frame){
		if(graphOn)
			chartPanel.createChartPrintJob();
		else{
			try {
				dataTextArea.print();
			} catch (PrinterException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Printing failed:\n"+e.getMessage(),
						"Printing Failed!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	/**
	 *
	 * @return the XAxis Label if not null
	 * else return null
	 */
	public String getXAxisLabel(){
		if(xAxis !=null)
			return xAxis.getLabel();
		return null;
	}

	/**
	 *
	 * @return the YAxis Label if not null
	 * else return null
	 */
	public String getYAxisLabel(){
		if(yAxis !=null)
			return yAxis.getLabel();
		return null;
	}

	/**
	 *
	 * @return the chart Title if not null
	 * else return null
	 */
	public String getPlotLabel(){
		if(chartPanel !=null)
			return chartPanel.getChart().getTitle().getText();
		return null;
	}


	public ChartPanel getCartPanel() {
		return this.chartPanel;
	}
	
	public DatasetRenderingOrder getRenderingOrder() {
		return renderingOrder;
	}

	public void setRenderingOrder(DatasetRenderingOrder renderingOrder) {
		this.renderingOrder = renderingOrder;
	}

	public boolean isxAxisInverted() {
		return xAxisInverted;
	}

	public void setxAxisInverted(boolean xAxisInverted) {
		this.xAxisInverted = xAxisInverted;
		if (xAxis  != null)
			xAxis.setInverted(xAxisInverted);
	}

	public boolean isyAxisInverted() {
		return yAxisInverted;
	}

	public void setyAxisInverted(boolean yAxisInverted) {
		this.yAxisInverted = yAxisInverted;
		if (yAxis  != null)
			yAxis.setInverted(yAxisInverted);
	}
}
