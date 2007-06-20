package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.gui;


import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.EqkRateModel2_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data.UCERF1MfdReader;
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
	private final static int W = 300;
	private final static int H = 800;
	private  JMenuBar menuBar = new JMenuBar();
	private JMenu analysisMenu = new JMenu("Further Analysis");
	private JMenuItem genReportFigMenu = new JMenuItem("Generate MFD Figs for Report");
	private JMenuItem bulgeAnalysisMenu = new JMenuItem("Make Bulge Analysis Plots");
	private JMenuItem logicTreeCumMFDplotMenu = new JMenuItem("Logic tree Cumulative plots");
	private JMenuItem logicTreeIncrMFDplotMenu = new JMenuItem("Logic tree Incremental plots");
	
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
		
		 menuBar.add(analysisMenu);
		 analysisMenu.add(genReportFigMenu);
		 analysisMenu.add(bulgeAnalysisMenu);
		 analysisMenu.add(logicTreeCumMFDplotMenu);
		 analysisMenu.add(logicTreeIncrMFDplotMenu);
		 setJMenuBar(menuBar);	
		 
		 genReportFigMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 genReportFigMenu_actionPerformed(e);
			 }
		 });
		 bulgeAnalysisMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 bulgeAnalysisMenu_actionPerformed(e);
			 }
		 });
		 logicTreeCumMFDplotMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 logicTreeCumMFDplotMenu_actionPerformed(e);
			 }
		 });
		 logicTreeIncrMFDplotMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 logicTreeIncrMFDplotMenu_actionPerformed(e);
			 }
		 });
		
	}
	
	  /**
	   * Generate Figures for Report
	   * 
	   * @param actionEvent
	   */
	  private void genReportFigMenu_actionPerformed(ActionEvent actionEvent) {
		  //String dirName = getDirectoryName();
		  //if(dirName==null) return;
		  //generateAnalysisFigures(dirName);
		  this.eqkRateModelERF.plotFaultMFDs_forReport();
	  }
	  
	  /**
	   * Generate Cumulative MFD Plots for various logic tree branches
	   * 
	   * @param actionEvent
	   */
	  private void logicTreeCumMFDplotMenu_actionPerformed(ActionEvent actionEvent) {
		  LogicTreeMFDsPlotter  logicTreeMFDsPlotter = new LogicTreeMFDsPlotter(false, true);
		  logicTreeMFDsPlotter.plotMFDs();
	  }
	  
	  
	  /**
	   * Generate Incremental MFD Plots for various logic tree branches
	   * 
	   * @param actionEvent
	   */
	  private void logicTreeIncrMFDplotMenu_actionPerformed(ActionEvent actionEvent) {
		  LogicTreeMFDsPlotter  logicTreeMFDsPlotter = new LogicTreeMFDsPlotter(false, false);
		  logicTreeMFDsPlotter.plotMFDs();
	  }
	  
	  /**
	   * Generate Bulge analysis Plots
	   * 
	   * @param actionEvent
	   */
	  private void bulgeAnalysisMenu_actionPerformed(ActionEvent actionEvent) {
		  String dirName = getDirectoryName();
		  if(dirName==null) return;
		  generateAnalysisFigures(dirName);
	  }
	  
	  
	
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
		
		 
		 ArrayList<String> paramNames = new ArrayList<String>();
		 // remove parameter listeners
		 ParameterList paramList = eqkRateModelERF.getAdjustableParameterList();
		 Iterator it = paramList.getParametersIterator();
		 while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 param.removeParameterChangeListener(this);
			 paramNames.add(param.getName());
		 }
		 
		 HSSFWorkbook wb  = new HSSFWorkbook();
		 HSSFSheet sheet = wb.createSheet();
		 HSSFRow row;
		 // add row for each parameter name. Also add a initial blank row for writing figure names
		 for(int i=0; i<=paramNames.size(); ++i) {
			 row = sheet.createRow(i); 
			 if(i>0) row.createCell((short)0).setCellValue(paramNames.get(i-1));
		 }
		 // add a row for predicted and observed ratio
		 sheet.createRow(paramNames.size()+1).createCell((short)0).setCellValue("M 6.5 pred/obs");
		 
		 
		 this.dirName=dirName;
		 this.eqkRateModelERF.setParamDefaults();
		 
		 // figure 1 with defaults
		 int fig=1;
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot("plot"+fig);
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 for(int i=0; i<paramNames.size(); ++i) {
			 if(paramNames.get(i).equalsIgnoreCase(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME))
				 sheet.getRow(i+1).createCell((short)fig).setCellValue("Geological Insight");
			 else	 sheet.getRow(i+1).createCell((short)fig).setCellValue(paramList.getValue(paramNames.get(i)).toString());			 
		 }
		 double obsVal = this.eqkRateModelERF.getObsCumMFD(eqkRateModelERF.areAfterShocksIncluded()).get(0).getInterpolatedY(6.5);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));

		 // figure 2
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.4");
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot("plot"+fig);
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME)).createCell((short)fig).setCellValue("D2.4");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));

		 //		 figure 3
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).setValue(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		
		 //		 figure 4
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.REL_A_PRIORI_WT_PARAM_NAME).setValue(new Double(1e7));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.REL_A_PRIORI_WT_PARAM_NAME)).createCell((short)fig).setCellValue("1e7");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 5
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(HanksBakun2002_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME)).createCell((short)fig).setCellValue(HanksBakun2002_MagAreaRel.NAME);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 6
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MEAN_MAG_CORRECTION).setValue(new Double(-0.1));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MEAN_MAG_CORRECTION)).createCell((short)fig).setCellValue(-0.1);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 7
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MEAN_MAG_CORRECTION).setValue(new Double(0.1));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MEAN_MAG_CORRECTION)).createCell((short)fig).setCellValue(0.1);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 8
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME).setValue(new Boolean(false));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME)).createCell((short)fig).setCellValue("False");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 9
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.C_ZONE_WT_PARAM_NAME).setValue(new Double(0.0));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.C_ZONE_WT_PARAM_NAME)).createCell((short)fig).setCellValue(0.0);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 10
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.C_ZONE_WT_PARAM_NAME).setValue(new Double(1.0));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.C_ZONE_WT_PARAM_NAME)).createCell((short)fig).setCellValue(1.0);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 
		 // wrap cell style
		 HSSFCellStyle wrapCellStyle = wb.createCellStyle();
		 wrapCellStyle.setWrapText(true);
		 for(int rowCount=0; rowCount<=sheet.getLastRowNum(); ++rowCount) {
			 for(int colCount=0; colCount<=fig; ++colCount) {
				 HSSFCell cell = sheet.getRow(rowCount).getCell((short)colCount);
				 if(cell==null) continue;
				 cell.setCellStyle(wrapCellStyle);
			 }
		 }
		 
		 eqkRateModelERF.setParamDefaults();
		 
		 // add back parameter listeners
		 paramList = eqkRateModelERF.getAdjustableParameterList();
		 it = paramList.getParametersIterator();
		 while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 param.addParameterChangeListener(this);
		 }
		 
		 try {
				FileOutputStream fileOut = new FileOutputStream(dirName+"/Table_For_Figures.xls");
				wb.write(fileOut);
				fileOut.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
	 }
	 
	 /**
	  * Plot MFDs
	  * @param fileName
	  */
	 private void makeMFDsPlot(String fileName) {
		 EqkRateModel2_MFDsPlotter mfdsPlotter = new EqkRateModel2_MFDsPlotter(this.eqkRateModelERF, true);
		 GraphWindow graphWindow= new GraphWindow(mfdsPlotter);
		 graphWindow.setPlotLabel("Cum Mag Freq Dist");
		 graphWindow.plotGraphUsingPlotPreferences();
		 graphWindow.setVisible(true);
		 try {
			 graphWindow.saveAsPNG(dirName+"/"+fileName+".png");
		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		 
		 // 
		/* ArrayList aFaultSourceGenerators = eqkRateModelERF.get_A_FaultSourceGenerators();
		 
		 for(int i=0; i<aFaultSourceGenerators.size(); ++i) {
			 IncrementalMagFreqDist magFreqDist;
			 ArrayList funcs = new ArrayList();
			 String faultName;
			 Object obj = aFaultSourceGenerators.get(i);
			 if(obj instanceof A_FaultSegmentedSourceGenerator) {
				 // segmented source
				 magFreqDist =( (A_FaultSegmentedSourceGenerator)obj).getTotalRupMFD();
				 faultName = ( (A_FaultSegmentedSourceGenerator)obj).getFaultSegmentData().getFaultName();
			 } else {
				 // unsegmented source
				 magFreqDist =( (UnsegmentedSource)obj).getMagFreqDist();
				 faultName = ( (UnsegmentedSource)obj).getFaultSegmentData().getFaultName();
			 }
			 
			 magFreqDist.setName(faultName+" Mag Freq Dist");
			 EvenlyDiscretizedFunc cumRateDist = magFreqDist.getCumRateDist();
			 cumRateDist.setInfo(faultName+" Cumulative Mag Freq Dist");
			 funcs.add(magFreqDist);
			 funcs.add(cumRateDist);
			 ArbitrarilyDiscretizedFunc ucerf1Rate = UCERF1MfdReader.getUCERF1IncrementalMFD(faultName);
			 ArbitrarilyDiscretizedFunc ucerf1CumRate = UCERF1MfdReader.getUCERF1CumMFD(faultName);
			 funcs.add(ucerf1Rate);
			 funcs.add(ucerf1CumRate);
			 GraphWindowAPI_Impl faultGraphWindow = new GraphWindowAPI_Impl(funcs, "Mag", "Rate", faultName+" MFD");
			 faultGraphWindow.saveAsPNG(dirName+"/"+fileName+"_"+faultName+".png");
			 faultGraphWindow.destroy();
		 }*/
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
		} 
	}
}
