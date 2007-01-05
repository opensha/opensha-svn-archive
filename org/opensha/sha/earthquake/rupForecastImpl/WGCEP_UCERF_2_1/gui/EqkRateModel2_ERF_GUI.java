package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;


import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterListParameter;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.EqkRateModel2_ERF;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;


/**
 * This class allows the user the user to adjust various parameters in EqkRateModel2_ERF and
 * the resulting Mag freq Distributions
 * @author vipingupta
 *
 */
public class EqkRateModel2_ERF_GUI extends JFrame implements ActionListener, ParameterChangeListener{
	
	private EqkRateModel2_ERF eqkRateModelERF= new EqkRateModel2_ERF(); // erf to get the adjustable params
	private ParameterListEditor editor; // editor
	private final static String TITLE = "Eqk Rate Model2 Params";
	private JButton calcButton = new JButton("Calculate");
	private JButton analysisFiguresButton = new JButton("Generate Figures for Analysis");
	//private ArbitrarilyDiscretizedFunc historicalMFD;
	private final static int W = 300;
	private final static int H = 800;
	//private  JMenuBar menuBar = new JMenuBar();
	//private JMenu analysisMenu = new JMenu("Further Analysis");
	// rupture rates
	//private JMenuItem rupRatesMenu = new JMenuItem("A-Fault Rup Rates");
	// Segment recurrence interval
	//private JMenu recurIntvMenu = new JMenu("Recurrence Intervals");
	//private JMenuItem segRecurIntvMenu = new JMenuItem("A-Fault Segment Recur Interval");
	//private JMenuItem ratioRecurIntvMenu = new JMenuItem("A-Fault Ratio of Segment Recur Intervals");
	//private JMenuItem segRatioRecurIntvMenu = new JMenuItem("Ratio of Segment Recur Intervals for a Particular Segment");
	// Segment Slip Rates
//	private JMenu slipRateMenu = new JMenu("Slip Rates");
//	private JMenuItem segSlipRateMenu = new JMenuItem("A-Fault Segment Slip Rates");
//	private JMenuItem ratioSlipRateMenu = new JMenuItem("A-Fault Ratio of Segment Slip Rates");
//	private JMenuItem segRatioSlipRateMenu = new JMenuItem("Ratio of Segment Slip Rates for a Particular Segment");

	
	//private final static String A_FAULT_RUP_RATES_FILENAME = "A_FaultRupRates_2_1.xls";
	//private final static String A_FAULT_SEG_RECUR_INTV_FILENAME = "A_FaultSegRecurIntv_2_1.xls";
	//private final static String A_FAULT_SEG_SLIP_RATE_FILENAME = "A_FaultSegSlipRate_2_1.xls";
	private String dirName=null; 
	private JScrollPane scrollPane = new JScrollPane();
	
	public static void main(String[] args) {
		new EqkRateModel2_ERF_GUI();
	}
	
	public EqkRateModel2_ERF_GUI() {
		
		// listen to all parameters
		ParameterList paramList = eqkRateModelERF.getAdjustableParameterList();
		Iterator it = paramList.getParametersIterator();
		while(it.hasNext()) {
			ParameterAPI param = (ParameterAPI)it.next();
			param.addParameterChangeListener(this);
		}
		
		//createHistoricalMFD();
		createGUI();
		calcButton.addActionListener(this);
		analysisFiguresButton.addActionListener(this);
		pack();
		Container container = this.getContentPane();
		container.add(this.scrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		container.validate();
		container.repaint();
		setSize(W, H);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		show();
	}
	
	public void parameterChange(ParameterChangeEvent event) {
		addParameterListEditor();
	}
	
	private void addParameterListEditor() {
		int val = scrollPane.getVerticalScrollBar().getValue();
		editor = new ParameterListEditor(eqkRateModelERF.getAdjustableParameterList());
		editor.setTitle(TITLE);
		scrollPane.setViewportView(editor);
		scrollPane.getVerticalScrollBar().setValue(val);
	}
	
	/*private void createHistoricalMFD() {
		historicalMFD = new ArbitrarilyDiscretizedFunc();
		double fractionInRELM_Region=0.75;
		historicalMFD.set(5.0, 11.5*fractionInRELM_Region);
		historicalMFD.set(5.5, 4.7*fractionInRELM_Region);
		historicalMFD.set(6.0, 1.07*fractionInRELM_Region);
		historicalMFD.set(6.5, 0.36*fractionInRELM_Region);
		historicalMFD.set(7.0, 0.089*fractionInRELM_Region);
		historicalMFD.set(7.5, 0.019*fractionInRELM_Region);
		historicalMFD.setInfo("Observed CUMULATIVE distribution (w/ model cum dist. prediction)");
	}*/
	/**
	 * Create GUI
	 *
	 */
	private void createGUI() {
		Container container = this.getContentPane();
		container.setLayout(new GridBagLayout());
		this.addParameterListEditor();
		container.add(this.calcButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		container.add(this.analysisFiguresButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));

		/* menuBar.add(analysisMenu);
		 analysisMenu.add(rupRatesMenu);
		 analysisMenu.add(recurIntvMenu);
		 analysisMenu.add(slipRateMenu);
		 // recurrence interval
		 recurIntvMenu.add(segRecurIntvMenu);
		 recurIntvMenu.add(ratioRecurIntvMenu);
		 recurIntvMenu.add(segRatioRecurIntvMenu);
		 // slip rate
		 slipRateMenu.add(segSlipRateMenu);
		 slipRateMenu.add(ratioSlipRateMenu);
		 slipRateMenu.add(this.segRatioSlipRateMenu);
		 
		 setJMenuBar(menuBar);	
		 // when Rup rates menu is selected
		 rupRatesMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 rupRatesMenu_actionPerformed(e);
			 }
		 });
		 // when segment recur Interval menu is selected
		 segRecurIntvMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 segRecurIntvMenu_actionPerformed(e);
			 }
		 });
		 // when ratio of segment recurrence intervals is selected
		 ratioRecurIntvMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 ratioSegRecurIntvMenu_actionPerformed(e);
			 }
		 });
		 // ratio of recurrence intervals for a specified segment
		 segRatioRecurIntvMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 segRatioRecurIntvMenu_actionPerformed(e);
			 }
		 });
		 
//		 ratio of recurrence intervals for a specified segment
		 segSlipRateMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 segSlipRateMenu_actionPerformed(e);
			 }
		 });
		 
//		 when ratio of segment recurrence intervals is selected
		 ratioSlipRateMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 ratioSegSlipRateMenu_actionPerformed(e);
			 }
		 });
		 // ratio of recurrence intervals for a specified segment
		 segRatioSlipRateMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 segRatioSlipRateMenu_actionPerformed(e);
			 }
		 });*/
	}
	
	
	/**
	   * Segment recurrence intervals ratio for a specific segment
	   *
	   * @param actionEvent ActionEvent
	   */
	 /* private  void segRatioRecurIntvMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/temp.xls";
		  this.eqkRateModelERF.generateExcelSheetForSegRecurIntv(excelSheetName);
		  FaultSegmentSelector faultSegmentSelector = new FaultSegmentSelector(this.eqkRateModelERF.get_A_FaultSources(), dirName, excelSheetName, true);
		  faultSegmentSelector.deleteExcelSheet(true);
	  }*/
	  
	  /**
	   * Segment slip rate ratio  for a specific segment
	   *
	   * @param actionEvent ActionEvent
	   */
	  /*private  void segRatioSlipRateMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/temp.xls";
		  this.eqkRateModelERF.generateExcelSheetForSegSlipRate(excelSheetName);
		  FaultSegmentSelector faultSegmentSelector = new FaultSegmentSelector(this.eqkRateModelERF.get_A_FaultSources(), dirName, excelSheetName, false);
		  faultSegmentSelector.deleteExcelSheet(true);
	  }*/
	  
	  
	 /**
	   * Segment recurrence intervals ratio for all segments
	   *
	   * @param actionEvent ActionEvent
	   */
	  /*private  void ratioSegRecurIntvMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/temp.xls";
		  this.eqkRateModelERF.generateExcelSheetForSegRecurIntv(excelSheetName);
		  CreateHistogramsFromSegRecurIntvFile.createHistogramPlots(dirName, excelSheetName);
		  new File(excelSheetName).delete();
	  }*/
	  
	  /**
	   * Segment slip Rate ratio for all segments
	   *
	   * @param actionEvent ActionEvent
	   */
	  /*private  void ratioSegSlipRateMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/temp.xls";
		  this.eqkRateModelERF.generateExcelSheetForSegSlipRate(excelSheetName);
		  CreateHistogramsFromSegSlipRateFile.createHistogramPlots(dirName, excelSheetName);
		  new File(excelSheetName).delete();
	  }*/
	
	 /**
	   * Rup Rates for A-Faults
	   *
	   * @param actionEvent ActionEvent
	   */
	  /*private  void rupRatesMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/"+A_FAULT_RUP_RATES_FILENAME;
		  this.eqkRateModelERF.generateExcelSheetsForRupMagRates(excelSheetName);
		  CreatePlotFromMagRateFile.createPlots(dirName, excelSheetName);
	  }*/
	  
	  /**
	   * Segment recurrece interval for A-Faults
	   * 
	   * @param actionEvent
	   */
	  /*private void segRecurIntvMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/"+A_FAULT_SEG_RECUR_INTV_FILENAME;
		  this.eqkRateModelERF.generateExcelSheetForSegRecurIntv(excelSheetName);
		  CreatePlotFromSegRecurIntvFile.createPlots(dirName, excelSheetName);
	  }*/
	  
	  /**
	   * Slip Rates for A-Faults
	   * 
	   * @param actionEvent
	   */
	  /*private void segSlipRateMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  String excelSheetName = dirName+"/"+A_FAULT_SEG_SLIP_RATE_FILENAME;
		  this.eqkRateModelERF.generateExcelSheetForSegSlipRate(excelSheetName);
		  CreatePlotFromSegSlipRateFile.createPlots(dirName, excelSheetName);
	  }*/
	  
	  
	 private String getDirectoryName() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose directory to save files");
		if(dirName!=null) fileChooser.setSelectedFile(new File(dirName));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { 
	    	dirName = fileChooser.getSelectedFile().getAbsolutePath();
	    } else dirName=null;
		return dirName;
	  }
	  
	 /**
	  * Generate figures for analysis
	  * @param dirName
	  */
	 private void generateAnalysisFigures(String dirName) {
		 
		 // remove parameter listeners
		 ParameterList paramList = eqkRateModelERF.getAdjustableParameterList();
		 Iterator it = paramList.getParametersIterator();
		 while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 param.removeParameterChangeListener(this);
		 }
		 
		 this.dirName=dirName;
		 this.eqkRateModelERF.setParamDefaults();
		 // figure 1 with defaults
		 int fig=1;
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 // figure 2
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.AFTERSHOCK_FRACTION_PARAM_NAME).setValue(0.0);
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME).setValue(1.0);
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.ABC_MO_RATE_REDUCTION_PARAM_NAME).setValue(0.0);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 3
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		  it = ((ParameterListParameter)eqkRateModelERF.getParameter(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).getParametersIterator();
		 while(it.hasNext()) { // set the specfiied rup model in each A fault
			 StringParameter param = (StringParameter)it.next();
			 ArrayList<String> allowedVals = param.getAllowedStrings();
			 param.setValue(allowedVals.get(1));
		 }
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 4
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 it = ((ParameterListParameter)eqkRateModelERF.getParameter(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).getParametersIterator();
		 while(it.hasNext()) { // set the specfiied rup model in each A fault
			 StringParameter param = (StringParameter)it.next();
			 ArrayList<String> allowedVals = param.getAllowedStrings();
			 param.setValue(allowedVals.get(1));
		 }
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.REL_SEG_RATE_WT_PARAM_NAME).setValue(new Double(0));
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 5
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 it = ((ParameterListParameter)eqkRateModelERF.getParameter(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).getParametersIterator();
		 while(it.hasNext()) { // set the specfiied rup model in each A fault
			 StringParameter param = (StringParameter)it.next();
			 ArrayList<String> allowedVals = param.getAllowedStrings();
			 param.setValue(allowedVals.get(2));
		 }
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.REL_SEG_RATE_WT_PARAM_NAME).setValue(new Double(0));
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 6
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).setValue(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 7
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.CHAR_SLIP_MODEL);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 8
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.UNIFORM_SLIP_MODEL);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 9
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.WG02_SLIP_MODEL);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 10
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(Ellsworth_A_WG02_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 11
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(HanksBakun2002_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 12
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(Somerville_2006_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 13
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME).setValue(new Double(100));
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 14
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.B_FAULTS_MIN_MAG).setValue(new Double(5.0));
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 15
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME).setValue(false);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 16
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.INCLUDE_C_ZONES).setValue(false);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 17
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MEAN_MAG_CORRECTION).setValue(new Double(0.1));
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 18
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.2");
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 19
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.3");
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 20
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME).setValue(new Double(0.67));
		 it = ((ParameterListParameter)eqkRateModelERF.getParameter(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).getParametersIterator();
		 while(it.hasNext()) { // set the specfiied rup model in each A fault
			 StringParameter param = (StringParameter)it.next();
			 ArrayList<String> allowedVals = param.getAllowedStrings();
			 param.setValue(allowedVals.get(1));
		 }
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.WG02_SLIP_MODEL);
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.REL_SEG_RATE_WT_PARAM_NAME).setValue(new Double(0));
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME).setValue(new Double(100));
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.INCLUDE_C_ZONES).setValue(false);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 //		 figure 21
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME).setValue(new Double(0.75));
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME).setValue(new Double(85));
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.INCLUDE_C_ZONES).setValue(false);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot(""+fig);
		 
		 eqkRateModelERF.setParamDefaults();
		 
		 // add back parameter listeners
		 paramList = eqkRateModelERF.getAdjustableParameterList();
		 it = paramList.getParametersIterator();
		 while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 param.addParameterChangeListener(this);
		 }
		 
		 
	 }
	 
	 /**
	  * Plot MFDs
	  * @param fileName
	  */
	 private void makeMFDsPlot(String fileName) {
		 EqkRateModel2_MFDsPlotter mfdsPlotter = new EqkRateModel2_MFDsPlotter(this.eqkRateModelERF);
		 GraphWindow graphWindow= new GraphWindow(mfdsPlotter);
		 graphWindow.setPlotLabel("Eqk Rates");
		 graphWindow.plotGraphUsingPlotPreferences();
		 graphWindow.setVisible(true);
		 try {
			 graphWindow.saveAsPNG(dirName+"/"+fileName+".png");
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
	 }
	
	/**
	 * When Calc button is clicked
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		Object source  = event.getSource();
		if(source==this.calcButton) {
			CalcProgressBar progressBar = new CalcProgressBar("Calculating", "Please Wait  (Accessing database takes time) .....");
			progressBar.setLocationRelativeTo(this);
			eqkRateModelERF.updateForecast(); // update forecast
			// show the output
			EqkRateModel2_Output_Window outputWindow = new EqkRateModel2_Output_Window(eqkRateModelERF);
			outputWindow.setLocationRelativeTo(this);
			progressBar.showProgress(false);
		} else if(source==this.analysisFiguresButton){
			String dirName = getDirectoryName();
			if(dirName==null) return;
			generateAnalysisFigures(dirName);
		}
	}
}
