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

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
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
	//private String dirName=null; 
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
	  
	  
	 /* private String getDirectoryName() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose directory to save files");
		if(dirName!=null) fileChooser.setSelectedFile(new File(dirName));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { 
	    	dirName = fileChooser.getSelectedFile().getAbsolutePath();
	    } else dirName=null;
		return dirName;
	  }*/
	  
	
	/**
	 * When Calc button is clicked
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		CalcProgressBar progressBar = new CalcProgressBar("Calculating", "Please Wait  (Accessing database takes time) .....");
		progressBar.setLocationRelativeTo(this);
		eqkRateModelERF.updateForecast(); // update forecast
		// show the output
		EqkRateModel2_Output_Window outputWindow = new EqkRateModel2_Output_Window(eqkRateModelERF);
		outputWindow.setLocationRelativeTo(this);
		progressBar.showProgress(false);
	}
}
