/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.EqkRateModel2_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This shows the output for the EqkRateModel_ERF_GUI
 * 
 * @author vipingupta
 *
 */
public class EqkRateModel2_Output_Window extends JFrame implements ActionListener, ParameterChangeListener{
	private final static String PLOT_LABEL = "Eqk Rates";
	private JButton plotMFDsButton = new JButton("Plot Mag Freq Dist");
	private JButton modSlipRateButton = new JButton("Plot Histogram of Normalized Slip-Rate Residuals ((Final_SR-Orig_SR)/SR_Sigma)");
	private JButton dataERButton = new JButton("Plot Histogram of Normalized Segment Event-Rate Residuals - (Final_ER-Data_ER)/ER_Sigma");
	private JButton predERButton = new JButton("Plot the ratio of Final to Pred Segment Event Rate");
	private JButton rupRatesRatioButton = new JButton("Plot Histogram of (FinalRate-A_PrioriRate)/A_PrioriRate");
	private JButton aFaultsSegDataButton = new JButton("Table of all A-Faults Segment Data");
	private JButton aFaultsRupDataButton = new JButton("Table of all A-Faults Rupture Data");
	private EqkRateModel2_ERF eqkRateModelERF;
	//private ArbitrarilyDiscretizedFunc historicalMFD;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private HashMap aFaultSourceMap;
	private SegmentDataPanel segmentDataPanel;
	private RuptureDataPanel ruptureDataPanel;
	private final static int W = 800;
	private final static int H = 800;
	private StringParameter aFaultParam;
	private final static String A_FAULT_PARAM_NAME = "A Fault";
	private boolean isUnsegmented;
	private ArrayList<Double> normModlSlipRateRatioList;
	private ArrayList<Double> normDataER_RatioList;
	private ArrayList<Double> predER_RatioList;
	private ArrayList<Double> normRupRatesRatioList;
	private  boolean isAseisReducesArea;
	private JTable aFaultsSegData, aFaultsRupData;
	private EqkRateModel2_MFDsPlotter mfdsPlotter;
	/**
	 * 
	 * @param eqkRateModelERF
	 * @param historicalMFD
	 */
	public EqkRateModel2_Output_Window(EqkRateModel2_ERF eqkRateModelERF) {
		this.eqkRateModelERF = eqkRateModelERF;
		//this.historicalMFD = historicalMFD;
		createGUI();
		this.pack();
		setSize(W,H);
		this.show();
	}
	
	
	private void createGUI() {
		isAseisReducesArea = ((Boolean)this.eqkRateModelERF.getParameter(EqkRateModel2_ERF.ASEIS_INTER_PARAM_NAME).getValue()).booleanValue();
		JPanel aFaultSummaryPanel = getA_FaultSummaryGUI();
		tabbedPane.addTab("Total Model Summary", getTotalModelSummaryGUI());
		tabbedPane.addTab("A Fault Summary", aFaultSummaryPanel);
		tabbedPane.addTab("B Fault Summary", getB_FaultSummaryGUI());
		tabbedPane.addTab("C Zones Summary", getC_ZonesSummaryGUI());
		if(!this.isUnsegmented) {
			calcNormModSlipRateResids();
			calcNormDataER_Resids();
			calcPredERRatio();
			calcNormRupRatesDiff();
			calcA_FaultRupData();
			calcA_FaultSegData();
		} else {
			this.modSlipRateButton.setVisible(false);
			this.predERButton.setVisible(false);
			this.dataERButton.setVisible(false);
			rupRatesRatioButton.setVisible(false);
			this.aFaultsRupDataButton.setVisible(false);
			this.aFaultsSegDataButton.setVisible(false);
		}
		Container container = this.getContentPane();
		container.setLayout(new GridBagLayout());
		container.add(tabbedPane,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
	}
	
	
	/**
	 * Get Total Model Summary
	 * 
	 * @return
	 */
	private JPanel getTotalModelSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		mfdsPlotter = new EqkRateModel2_MFDsPlotter(this.eqkRateModelERF);
		JTextArea textArea = new JTextArea();
		textArea.setText("");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		IncrementalMagFreqDist totalMFD = this.eqkRateModelERF.getTotalMFD();
		textArea.append("Total Rate (M>=5) = "+(float)totalMFD.getTotalIncrRate()+"\n");
		boolean includeAfterShocks = eqkRateModelERF.areAfterShocksIncluded();
		textArea.append("Predicted 6.5 rate over observed = "+(totalMFD.getCumRate(6.5)/this.eqkRateModelERF.getObsCumMFD(includeAfterShocks).get(0).getInterpolatedY(6.5))+"\n");
		textArea.append("Total Moment Rate = "+(float)totalMFD.getTotalMomentRate()+"\n");
		
		// Display the general prediction error in case of Segmented A-Faults
		if(!this.isUnsegmented) { // for segmented faults, get the general prediction error
			double genPredErr = 0,  modSlipRateError=0, dataER_Error=0, aPrioriRateError=0, nonNorm_aPrioriRateError=0;
			Iterator it = aFaultSourceMap.values().iterator();
			while(it.hasNext()) {
				A_FaultSegmentedSource source = (A_FaultSegmentedSource)it.next();
				genPredErr += source.getGeneralizedPredictionError();
				modSlipRateError+=source.getNormModSlipRateError();
				dataER_Error+=source.getNormDataER_Error();
				aPrioriRateError+=source.getA_PrioriModelError();
				nonNorm_aPrioriRateError+=source.getNonNormA_PrioriModelError();
			}
			textArea.append("\nTotal A-Fault Gen Pred Error = "+(float)genPredErr+"\n");
			textArea.append("A-Fault Mod Slip Rate Error = "+(float)modSlipRateError+"\n");
			textArea.append("A-Fault Data Event Rate Error = "+(float)dataER_Error+"\n");
			textArea.append("A-Fault A-Priori Rates Error = "+(float)aPrioriRateError+"\t");
			textArea.append("(non-normalized = "+(float)nonNorm_aPrioriRateError+")\n\n");
		}
		
		
		textArea.append("\tRate (M>=5)\tRate (M>=6.5)\tMoment Rate\n");
		textArea.append("------------------------------------------------\n");
		textArea.append("A Faults\t"+(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getTotalMomentRate()+"\n");
		textArea.append("B Char\t"+(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getTotalMomentRate()+"\n");
		textArea.append("B GR\t"+(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getTotalMomentRate()+"\n");
		textArea.append("C Zone\t"+(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getTotalMomentRate()+"\n");
		textArea.append("Background\t"+(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getTotalMomentRate()+"\n");
		textArea.append("Total\t"+(float)totalMFD.getTotalIncrRate()+"\t"+
				(float)totalMFD.getCumRate(6.5)+"\t"+
				(float)totalMFD.getTotalMomentRate()+"\n\n");
		textArea.append("Adjustable Params Metadata:\n");
		textArea.append(eqkRateModelERF.getAdjustableParameterList().getParameterListMetadataString("\n"));
		panel.add(new JScrollPane(textArea),new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(plotMFDsButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(this.modSlipRateButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(predERButton,new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(dataERButton,new GridBagConstraints( 0, 4, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(rupRatesRatioButton,new GridBagConstraints( 0, 5, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(aFaultsRupDataButton,new GridBagConstraints( 0, 6, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(aFaultsSegDataButton,new GridBagConstraints( 0, 7, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));

		textArea.setEditable(false);
		plotMFDsButton.addActionListener(this);
		this.modSlipRateButton.addActionListener(this);
		this.predERButton.addActionListener(this);
		this.dataERButton.addActionListener(this);
		rupRatesRatioButton.addActionListener(this);
		this.aFaultsRupDataButton.addActionListener(this);
		this.aFaultsSegDataButton.addActionListener(this);
		return panel;
	}
	
	/**
	 * Calculate A Fault Segment Data
	 *
	 */
	private void calcA_FaultSegData() {
		ArrayList<String> faultNames  = aFaultParam.getAllowedStrings();
		int totalRows=faultNames.size();
		for(int srcIndex=0; srcIndex<faultNames.size(); ++srcIndex) {
			A_FaultSegmentedSource source =  (A_FaultSegmentedSource) aFaultSourceMap.get(faultNames.get(srcIndex));
			totalRows+=source.getFaultSegmentData().getNumSegments()+1;// include totals
		} 
		int totalCols = SegmentDataTableModel.columnNames.length;
		Object[][] rowData = new Object[totalRows][totalCols];
		int rowIndex=0;
		SegmentDataTableModel segTableModel = new SegmentDataTableModel();
		for(int srcIndex=0; srcIndex<faultNames.size(); ++srcIndex) {
			A_FaultSegmentedSource source =  (A_FaultSegmentedSource) aFaultSourceMap.get(faultNames.get(srcIndex));
			FaultSegmentData faultSegmentData = source.getFaultSegmentData();
			rowData[rowIndex][0]=faultSegmentData.getFaultName();
			for(int colIndex=1; colIndex<totalCols;++colIndex) rowData[rowIndex][colIndex]="";
			++rowIndex;
			segTableModel.setFaultData(faultSegmentData, source, null);
			for(int segIndex=0; segIndex<=faultSegmentData.getNumSegments(); ++segIndex, ++rowIndex) {
				for(int colIndex=0; colIndex<totalCols;++colIndex)
					rowData[rowIndex][colIndex]=segTableModel.getValueAt(segIndex, colIndex);
			}
		} 
		 aFaultsSegData = new JTable(rowData, SegmentDataTableModel.columnNames);
		 aFaultsSegData.setColumnSelectionAllowed(true);
	}
	
	/**
	 * Calculate Rupture Data forall A-Faults
	 *
	 */
	private void calcA_FaultRupData() {
		ArrayList<String> faultNames  = aFaultParam.getAllowedStrings();
		int totalRows=faultNames.size();
		for(int srcIndex=0; srcIndex<faultNames.size(); ++srcIndex) {
			A_FaultSegmentedSource source =  (A_FaultSegmentedSource) aFaultSourceMap.get(faultNames.get(srcIndex));
			totalRows+=source.getNumRuptures()+1; // also include totals
		} 
		int totalCols = RuptureTableModel.columnNames.length;
		Object[][] rowData = new Object[totalRows][totalCols];
		int rowIndex=0;
		RuptureTableModel rupTableModel = new RuptureTableModel();
		for(int srcIndex=0; srcIndex<faultNames.size(); ++srcIndex) {
			A_FaultSegmentedSource source =  (A_FaultSegmentedSource) aFaultSourceMap.get(faultNames.get(srcIndex));
			FaultSegmentData faultSegmentData = source.getFaultSegmentData();
			rowData[rowIndex][0]=faultSegmentData.getFaultName();
			for(int colIndex=1; colIndex<totalCols;++colIndex) rowData[rowIndex][colIndex]="";
			++rowIndex;
			rupTableModel.setFaultSegmentedSource(source);
			for(int rupIndex=0; rupIndex<=source.getNumRuptures(); ++rupIndex, ++rowIndex) {
				for(int colIndex=0; colIndex<totalCols;++colIndex)
					rowData[rowIndex][colIndex]=rupTableModel.getValueAt(rupIndex, colIndex);
			}
		} 
		 aFaultsRupData = new JTable(rowData, RuptureTableModel.columnNames);
		 aFaultsRupData.setColumnSelectionAllowed(true);
	}
	
	/**
	 * A Fault summary GUI
	 * 
	 * @return
	 */
	private JPanel getA_FaultSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		aFaultSourceMap = new HashMap();
		ArrayList aFaultSources = this.eqkRateModelERF.get_A_FaultSources();
		
		// whether this is segmented or unsegmented
		String rupModel = (String)eqkRateModelERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).getValue();
		if(rupModel.equalsIgnoreCase(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL)) {
			this.isUnsegmented = true;
			this.predERButton.setVisible(false);
			this.dataERButton.setVisible(false);
		}
		else {
			this.isUnsegmented = false;
			this.predERButton.setVisible(true);
			this.dataERButton.setVisible(true);
		}
		
		if(aFaultSources==null) return panel;
		segmentDataPanel = new SegmentDataPanel();
		ArrayList faultNames = new ArrayList();
		for(int i=0; i<aFaultSources.size(); ++i) {
			Object source = aFaultSources.get(i);
			FaultSegmentData faultSegmentData = getFaultSegmentData(source);
			faultNames.add(faultSegmentData.getFaultName());
			aFaultSourceMap.put(faultSegmentData.getFaultName(), source);
		}
		this.aFaultParam = new StringParameter(A_FAULT_PARAM_NAME, faultNames, (String)faultNames.get(0));
		aFaultParam.addParameterChangeListener(this);
		ConstrainedStringParameterEditor paramEditor = new ConstrainedStringParameterEditor(aFaultParam);
		panel.add(paramEditor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		JTabbedPane segmentInfoTabbedPane = new JTabbedPane();
		segmentInfoTabbedPane.addTab("Segment Info", segmentDataPanel);
		if(this.isUnsegmented) {
			B_FaultDataPanel bFaultDataPanel = new B_FaultDataPanel();
			bFaultDataPanel.setB_FaultSources(aFaultSources);
			segmentInfoTabbedPane.addTab("Rupture Info", bFaultDataPanel);
		} else {
			ruptureDataPanel = new RuptureDataPanel();
			segmentInfoTabbedPane.addTab("Rupture Info", ruptureDataPanel);
			ruptureDataPanel.setSourcesForMagAreaPlot(aFaultSources, this.eqkRateModelERF.getMagAreaRelationships());
		}
		panel.add(segmentInfoTabbedPane,new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		
		updateA_FaultTableData();
		return panel;
	}
	
	/**
	 * Get fault segment data based on A fault source type (whether it is segmented or unsegmented)
	 * @param source
	 * @return
	 */
	private FaultSegmentData getFaultSegmentData(Object source) {
		if(this.isUnsegmented) return ((UnsegmentedSource)source).getFaultSegmentData();
		else return ((A_FaultSegmentedSource)source).getFaultSegmentData();
	}
	
	/**
	 * B faults sources
	 * @return
	 */
	private JPanel getB_FaultSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		ArrayList bFaultSources = this.eqkRateModelERF.get_B_FaultSources();
		B_FaultDataPanel bFaultDataPanel = new B_FaultDataPanel();
		bFaultDataPanel.setB_FaultSources(bFaultSources);
		panel.add(bFaultDataPanel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		return panel;
	}
	
	
	/**
	 * C Zones
	 * @return
	 */
	private JPanel getC_ZonesSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		ArrayList<IncrementalMagFreqDist> cZonesMFDs = this.eqkRateModelERF.getC_ZoneMFD_List();
		C_ZoneDataPanel cZonesDataPanel = new C_ZoneDataPanel();
		cZonesDataPanel.setC_ZonesMFD_List(cZonesMFDs);
		panel.add(cZonesDataPanel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		return panel;
	}
	
	/**
	 * 
	 * @param event
	 */
	public void parameterChange(ParameterChangeEvent event) {
		updateA_FaultTableData();
	}
	
	
	/**
	 * update the A fault table data based on the selected A fault
	 *
	 */
	private void updateA_FaultTableData() {
		String selectedFault = (String)aFaultParam.getValue();
		Object source =   aFaultSourceMap.get(selectedFault);
		if(!this.isUnsegmented)  {
			ruptureDataPanel.setSource((A_FaultSegmentedSource)source);
			this.segmentDataPanel.setFaultSegmentData((A_FaultSegmentedSource)source, null, isAseisReducesArea, this.eqkRateModelERF.getMagAreaRelationships());
		} else {
			segmentDataPanel.setEventRatesList(this.eqkRateModelERF.getA_FaultsFetcher().getEventRatesList());
			this.segmentDataPanel.setFaultSegmentData(null, (UnsegmentedSource)source, isAseisReducesArea, this.eqkRateModelERF.getMagAreaRelationships());
		}
	} 


	/**
	 * When Calc button is clicked
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if(src == this.plotMFDsButton) {
			GraphWindow graphWindow= new GraphWindow(mfdsPlotter);
			graphWindow.setPlotLabel(PLOT_LABEL);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		} else if(src == this.modSlipRateButton) { // ratio of modified slip rates
			String plotLabel = "Normalized Segment Slip-Rate Residuals\n((Final_SR-Orig_SR)/SR_Sigma)";
			showHistograms(normModlSlipRateRatioList, plotLabel, "Normalized Segment Slip-Rate Residuals");
		}else if(src == this.dataERButton) { // ratio of final Event rate and data Event rate
			String plotLabel = "Normalized Segment Event-Rate Residuals\n((Final_ER-Data_ER)/ER_Sigma)";
			showHistograms(normDataER_RatioList, plotLabel, "Normalized Segment Event-Rate Residuals");
		}else if(src == this.predERButton) { // ratio of final event rate and pred Event rate
			String plotLabel = "Final vs Pred Segment Event Rate Ratios";
			showHistograms(predER_RatioList, plotLabel, "Ratio of final Event Ratio to Pred Event Ratio");
		} else if(src == this.rupRatesRatioButton) { // ratio of final rates to A-Priori Rates
			String plotLabel = "Histogram of (FinalRate-A_PrioriRate)/A_PrioriRate";
			showHistograms(normRupRatesRatioList, plotLabel, "Histogram of (FinalRate-A_PrioriRate)/A_PrioriRate");
		} else if(src==this.aFaultsSegDataButton) {
			JFrame frame = new JFrame();
			frame.getContentPane().setLayout(new  GridBagLayout());
			frame.getContentPane().add(new JScrollPane(this.aFaultsSegData), new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
		      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
			frame.pack();
			frame.show();
		} else if(src == this.aFaultsRupDataButton) {
			JFrame frame = new JFrame();
			frame.getContentPane().setLayout(new  GridBagLayout());
			frame.getContentPane().add(new JScrollPane(this.aFaultsRupData), new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
		      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
			frame.pack();
			frame.show();
		}
	}

	/**
	 * Plot Histogram of (FinalRate-A_PrioriRate)/A_PrioriRate
	 *
	 */
	private void calcNormRupRatesDiff() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		normRupRatesRatioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numRuptures = source.getNumRuptures();
			// iterate over all ruptures
			for(int rupIndex = 0; rupIndex<numRuptures; ++rupIndex) {
				double normResid = (source.getRupRate(rupIndex)-source.getAPrioriRupRate(rupIndex))/source.getAPrioriRupRate(rupIndex);
				normRupRatesRatioList.add(normResid);
			}
		}
	}
	

	
	/**
	 * Plot Normalized Segment Slip-Rate Residuals (where orig slip-rate and stddev are reduces by the fraction of moment rate removed)
	 *
	 */
	private void calcNormModSlipRateResids() {
		ArrayList<A_FaultSegmentedSource> sourceList = eqkRateModelERF.get_A_FaultSources();
		normModlSlipRateRatioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			double normModResids[] = source.getNormModSlipRateResids();
			for(int segIndex = 0; segIndex<normModResids.length; ++segIndex) normModlSlipRateRatioList.add(new Double(normModResids[segIndex]));
		}
	}
	
	/**
	 * Plot Normalized Event-Rate Residuals
	 *
	 */
	private void calcNormDataER_Resids() {
		ArrayList<A_FaultSegmentedSource> sourceList = eqkRateModelERF.get_A_FaultSources();
		normDataER_RatioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			double normDataER_Resids[] = source.getNormDataER_Resids();
			for(int segIndex = 0; segIndex<normDataER_Resids.length; ++segIndex) 
				if(!Double.isNaN(normDataER_Resids[segIndex])) normDataER_RatioList.add(new Double(normDataER_Resids[segIndex]));
		}
	}
	
	/**
	 * Plot the ratio of Event Rates
	 *
	 */
	private void calcPredERRatio() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		predER_RatioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numSegments = source.getFaultSegmentData().getNumSegments();
			// iterate over all segments
			for(int segIndex = 0; segIndex<numSegments; ++segIndex) 
				predER_RatioList.add(source.getFinalSegmentRate(segIndex)/source.getSegRateFromAprioriRates(segIndex));
		}
	}

	/**
	 * Show histograms
	 * @param func
	 * @param plotLabel
	 */
	private void showHistograms(ArrayList<Double> ratioList, String plotLabel, String funcName) {
		double min = Math.floor(Collections.min(ratioList));
		double max = Math.ceil(Collections.max(ratioList));
		double delta = 0.2;
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(min, (int)Math.round((max-min)/delta)+1, delta);
		func.setTolerance(func.getDelta());
		int xIndex;
		for(int i=0; i<ratioList.size(); ++i) {
			xIndex = func.getXIndex(ratioList.get(i));
			func.add(xIndex, 1.0);
		}
		ArrayList funcs = new ArrayList();
		funcs.add(func);
		String yAxisLabel = "Count";
		GraphWindow graphWindow= new GraphWindow(new CreateHistogramsFromSegSlipRateFile(funcs, plotLabel, yAxisLabel));
		graphWindow.setPlotLabel(plotLabel);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(true);
	}

}
