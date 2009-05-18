package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;


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
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterListParameter;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.EqkRateModel2_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.UCERF1MfdReader;
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
	private JMenuItem genReportFigMenu = new JMenuItem("Generate Report Figures");
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
		 setJMenuBar(menuBar);	
		 // when segment recur Interval menu is selected
		 genReportFigMenu.addActionListener(new java.awt.event.ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 genReportFigMenu_actionPerformed(e);
			 }
		 });
		 
		
	}
	
	  /**
	   * Generate Figures for Report
	   * 
	   * @param actionEvent
	   */
	  private void genReportFigMenu_actionPerformed(ActionEvent actionEvent) {
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
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.AFTERSHOCK_FRACTION_PARAM_NAME).setValue(0.0);
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME).setValue(1.0);
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.ABC_MO_RATE_REDUCTION_PARAM_NAME).setValue(0.0);
		 eqkRateModelERF.updateForecast();
		 makeMFDsPlot("plot"+fig);
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.AFTERSHOCK_FRACTION_PARAM_NAME)).createCell((short)fig).setCellValue(0);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME)).createCell((short)fig).setCellValue(1);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.ABC_MO_RATE_REDUCTION_PARAM_NAME)).createCell((short)fig).setCellValue(0);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));

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
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue("Minimum Rate");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		
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
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue("Minimum Rate");
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.REL_SEG_RATE_WT_PARAM_NAME)).createCell((short)fig).setCellValue(0);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
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
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue("Maximum Rate");
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.REL_SEG_RATE_WT_PARAM_NAME)).createCell((short)fig).setCellValue(0);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 6
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).setValue(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 7
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.CHAR_SLIP_MODEL);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue(A_FaultSegmentedSource.CHAR_SLIP_MODEL);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 8
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.UNIFORM_SLIP_MODEL);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue(A_FaultSegmentedSource.UNIFORM_SLIP_MODEL);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 9
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME).setValue(A_FaultSegmentedSource.WG02_SLIP_MODEL);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue(A_FaultSegmentedSource.WG02_SLIP_MODEL);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 10
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(Ellsworth_A_WG02_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME)).createCell((short)fig).setCellValue(Ellsworth_A_WG02_MagAreaRel.NAME);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 11
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(HanksBakun2002_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME)).createCell((short)fig).setCellValue(HanksBakun2002_MagAreaRel.NAME);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 12
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(Somerville_2006_MagAreaRel.NAME);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME)).createCell((short)fig).setCellValue(Somerville_2006_MagAreaRel.NAME);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 13
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME).setValue(new Double(100));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME)).createCell((short)fig).setCellValue(100);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 14
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.B_FAULTS_MIN_MAG).setValue(new Double(5.0));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.B_FAULTS_MIN_MAG)).createCell((short)fig).setCellValue(5.0);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 15
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME).setValue(false);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME)).createCell((short)fig).setCellValue("false");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 16
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.INCLUDE_C_ZONES).setValue(false);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.INCLUDE_C_ZONES)).createCell((short)fig).setCellValue("false");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 17
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.MEAN_MAG_CORRECTION).setValue(new Double(0.1));
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.MEAN_MAG_CORRECTION)).createCell((short)fig).setCellValue(0.1);
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 18
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.2");
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME)).createCell((short)fig).setCellValue("D2.2");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 19
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.3");
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME)).createCell((short)fig).setCellValue("D2.3");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
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
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME)).createCell((short)fig).setCellValue(0.67);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue("Minimum Rate");
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME)).createCell((short)fig).setCellValue(A_FaultSegmentedSource.WG02_SLIP_MODEL);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.REL_SEG_RATE_WT_PARAM_NAME)).createCell((short)fig).setCellValue(0);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME)).createCell((short)fig).setCellValue(100);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.INCLUDE_C_ZONES)).createCell((short)fig).setCellValue("false");
		 sheet.getRow(paramNames.size()+1).createCell((short)fig).setCellValue((eqkRateModelERF.getTotalMFD().getCumRate(6.5)/obsVal));
		 makeMFDsPlot("plot"+fig);
		 
		 //		 figure 21
		 ++fig;
		 this.eqkRateModelERF.setParamDefaults();
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME).setValue(new Double(0.75));
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME).setValue(new Double(85));
		 eqkRateModelERF.getParameter(EqkRateModel2_ERF.INCLUDE_C_ZONES).setValue(false);
		 eqkRateModelERF.updateForecast();
		 sheet.getRow(0).createCell((short)fig).setCellValue("Figure "+fig);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.COUPLING_COEFF_PARAM_NAME)).createCell((short)fig).setCellValue(0.75);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.CHAR_VS_GR_PARAM_NAME)).createCell((short)fig).setCellValue(85);
		 sheet.getRow(1+paramNames.indexOf(EqkRateModel2_ERF.INCLUDE_C_ZONES)).createCell((short)fig).setCellValue("false");
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
		 EqkRateModel2_MFDsPlotter mfdsPlotter = new EqkRateModel2_MFDsPlotter(this.eqkRateModelERF);
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
		 ArrayList aFaultSources = eqkRateModelERF.get_A_FaultSources();
		 
		 for(int i=0; i<aFaultSources.size(); ++i) {
			 IncrementalMagFreqDist magFreqDist;
			 ArrayList funcs = new ArrayList();
			 String faultName;
			 Object obj = aFaultSources.get(i);
			 if(obj instanceof A_FaultSegmentedSource) {
				 // segmented source
				 magFreqDist =( (A_FaultSegmentedSource)obj).getTotalRupMFD();
				 faultName = ( (A_FaultSegmentedSource)obj).getFaultSegmentData().getFaultName();
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
		} 
	}
}
