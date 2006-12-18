/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import org.opensha.calc.FaultMomentCalc;
import org.opensha.calc.MomentMagCalc;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.SegRateConstraint;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * Panel to show the Segments and Fault sections data 
 * @author vipingupta
 *
 */
public class SegmentDataPanel extends JPanel implements ActionListener, GraphWindowAPI {
	private SegmentDataTableModel segmentTableModel = new SegmentDataTableModel();
	private FaultSectionTableModel faultSectionTableModel = new FaultSectionTableModel();
	private final static String MSG_ASEIS_REDUCES_AREA = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of area (as requested) in the table above; this will also influence the segment slip rates for any segments composed of more than one section (because the slip rates are weight-averaged according to section areas)";
	private final static String MSG_ASEIS_REDUCES_SLIPRATE = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of slip rate (as requested); keep this in mind when interpreting the segment slip rates (which for any segments composed of more than one section are a weight average by section areas)";
	private JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private JTextArea magAreasTextArea = new JTextArea();
	private JButton slipRateButton = new JButton("Plot Segment Slip Rates");
	private JButton slipRateRatioButton = new JButton("Plot Normalized Slip-Rate Residuals - (Final_SR-Data_SR)/SR_Sigma");
	private JButton eventRateButton = new JButton("Plot Segment Event Rates");
	private JButton eventRateRatioButton = new JButton("Plot Normalized Event Rate Residuals - (Final_ER-Data_ER)/ER_Sigma");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat SLIP_FORMAT = new DecimalFormat("0.000");
	private ArrayList<ArbitrarilyDiscretizedFunc> slipRatesList, eventRatesList, eventRatesRatioList, slipRatesRatioList;
	
	private final static PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      new Color(255,0,0), 10); // RED Cross symbols
	private final static PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      new Color(0,0,0), 2); 
	private final static PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      new Color(0,255,0), 2); 
	private final static PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      new Color(0,0,0), 10); // BLACK Cross symbols
	private String xAxisLabel, yAxisLabel;
	private ArrayList<PlotCurveCharacterstics> plottingFeatures, slipRatePlottingFeatures, eventRatesPlottingFeatures, eventRateRatioPlotFeatures, slipRateRatioPlotFeatures;
	private ArrayList<ArbitrarilyDiscretizedFunc> plottingFuncList;
	private boolean yLog = true ;
	
	public SegmentDataPanel() {
		setLayout(new GridBagLayout());
		createGUI();
		slipRateButton.addActionListener(this);
		eventRateButton.addActionListener(this);
		eventRateRatioButton.addActionListener(this);
		slipRateRatioButton.addActionListener(this);
		this.makePlottingFeaturesList();
	}
	
	private void makePlottingFeaturesList() {
		// slip rate plotting features
		slipRatePlottingFeatures = new ArrayList<PlotCurveCharacterstics>();;
		slipRatePlottingFeatures.add(this.PLOT_CHAR1);
		slipRatePlottingFeatures.add(this.PLOT_CHAR1);
		slipRatePlottingFeatures.add(this.PLOT_CHAR1);
		slipRatePlottingFeatures.add(this.PLOT_CHAR4);
		slipRatePlottingFeatures.add(this.PLOT_CHAR4);
		slipRatePlottingFeatures.add(this.PLOT_CHAR4);
		slipRatePlottingFeatures.add(this.PLOT_CHAR3);
		// event rates Plotting features
		eventRatesPlottingFeatures = new ArrayList<PlotCurveCharacterstics>();;
		eventRatesPlottingFeatures.add(this.PLOT_CHAR1);
		eventRatesPlottingFeatures.add(this.PLOT_CHAR1);
		eventRatesPlottingFeatures.add(this.PLOT_CHAR1);
		eventRatesPlottingFeatures.add(this.PLOT_CHAR2);
		eventRatesPlottingFeatures.add(this.PLOT_CHAR3);
		// normalized event rates residuals
		eventRateRatioPlotFeatures = new ArrayList<PlotCurveCharacterstics>();
		eventRateRatioPlotFeatures.add(PLOT_CHAR1);
		// normalized slip rate residuals
		slipRateRatioPlotFeatures = new ArrayList<PlotCurveCharacterstics>();
		slipRateRatioPlotFeatures.add(PLOT_CHAR1);
	}
	
	private void createGUI() {
		magAreasTextArea.setEditable(false);
		magAreasTextArea.setLineWrap(true);
		magAreasTextArea.setWrapStyleWord(true);
		JTable sectionDataTable = new JTable(faultSectionTableModel);
		sectionDataTable.setColumnSelectionAllowed(true);
		
		JSplitPane sectionDataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sectionDataSplitPane.add(new JScrollPane(sectionDataTable),JSplitPane.BOTTOM);
		sectionDataSplitPane.add(new JScrollPane(this.magAreasTextArea),JSplitPane.TOP);
		JTable segmentTable = new JTable(this.segmentTableModel);
		segmentTable.setColumnSelectionAllowed(true);
		rightSplitPane.add(new JScrollPane(segmentTable), JSplitPane.TOP);
		rightSplitPane.add(sectionDataSplitPane, JSplitPane.BOTTOM);
		rightSplitPane.setDividerLocation(150);
		sectionDataSplitPane.setDividerLocation(200);
		add(rightSplitPane,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		add(slipRateButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		add(this.slipRateRatioButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		add(this.eventRateButton,new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		add(this.eventRateRatioButton,new GridBagConstraints( 0, 4, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
	}
	
	
	/**
	 * Update the data in the tables with the selected fault 
	 * 
	 * @param faultSegmentData
	 * @param isAseisReducesArea
	 */
	public void setFaultSegmentData(A_FaultSegmentedSource segmentedSource, boolean isAseisReducesArea, ArrayList magAreaRelationships) {
		FaultSegmentData faultSegmentData = segmentedSource.getFaultSegmentData();
		// update the segment table model
		updateSegTableModel(isAseisReducesArea, magAreaRelationships, faultSegmentData, segmentedSource);
	}
	
	
	/**
	 * Update the data in the tables with the selected fault 
	 * 
	 * @param faultSegmentData
	 * @param isAseisReducesArea
	 */
	public void setFaultSegmentData(UnsegmentedSource unsegmentedSource, boolean isAseisReducesArea, ArrayList magAreaRelationships) {
		FaultSegmentData faultSegmentData = unsegmentedSource.getFaultSegmentData();
		// update the segment table model
		updateSegTableModel(isAseisReducesArea, magAreaRelationships, faultSegmentData, null);
	}


	/**
	 * Update the segment table model
	 * @param isAseisReducesArea
	 * @param magAreaRelationships
	 * @param faultSegmentData
	 * @param predMRI
	 * @param finalMRI
	 */
	private void updateSegTableModel(boolean isAseisReducesArea, ArrayList magAreaRelationships, FaultSegmentData faultSegmentData, 
			A_FaultSegmentedSource segmentedSource) {
		setMagAndSlipsString(faultSegmentData, isAseisReducesArea, magAreaRelationships, segmentedSource);
		segmentTableModel.setSegmentedFaultData(faultSegmentData,  segmentedSource);
		segmentTableModel.fireTableDataChanged();
		if(faultSegmentData!=null) faultSectionTableModel.setFaultSectionData(faultSegmentData.getPrefFaultSectionDataList());
		else faultSectionTableModel.setFaultSectionData(null);
		faultSectionTableModel.fireTableDataChanged();
		
		if(segmentedSource==null) { // for unsegmented source
			this.eventRateButton.setVisible(false);
			this.slipRateButton.setVisible(false);
		} else { // Segmented source
			this.eventRateButton.setVisible(true);
			this.slipRateButton.setVisible(true);
			generateSlipRateFuncList(segmentedSource, faultSegmentData);
			generateSlipRateRatioFuncList(segmentedSource);
			generateEventRateFuncList(segmentedSource, faultSegmentData);
			generateEventRateRatioFuncList(segmentedSource);
		}
		
	}
	
	/**
	 * When a plotting button is clicked
	 */
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if(src==this.slipRateButton) {
			xAxisLabel = "Segment Index";
			yAxisLabel = "Slip Rate (mm/yr)";
			// plotting features
			plottingFeatures = slipRatePlottingFeatures;
			// plotting Func List
			plottingFuncList = this.slipRatesList;
			yLog = true;
		} else if(src==this.eventRateButton){
			xAxisLabel = "Segment Index";
			yAxisLabel = "Event Rates (1/years)";
			// plotting features
			plottingFeatures = this.eventRatesPlottingFeatures;
			plottingFuncList = this.eventRatesList;
			yLog = true;
		} else if(src == this.eventRateRatioButton) {
			xAxisLabel = "Segment Index";
			yAxisLabel = " Normalized Event Rate Residuals - (Final_ER-Data_ER)/ER_Sigma";
			// plotting features
			plottingFeatures = this.eventRateRatioPlotFeatures;
			plottingFuncList = this.eventRatesRatioList;
			yLog = false;
		} else if(src == this.slipRateRatioButton) {
			xAxisLabel = "Segment Index";
			yAxisLabel = " Normalized Slip-Rate Residuals - (Final_SR-Data_SR)/SR_Sigma";
			// plotting features
			plottingFeatures = this.slipRateRatioPlotFeatures;
			plottingFuncList = this.slipRatesRatioList;
			yLog = false;
		}
		GraphWindow graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(yAxisLabel);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(true);
	}
	
	
	/**
	 * Normalized Slip-Rate Residuals - (Final_SR-Data_SR)/SR_Sigma
	 * 
	 * @param segmentedSource
	 */
	private void generateSlipRateRatioFuncList(A_FaultSegmentedSource segmentedSource) {
		ArbitrarilyDiscretizedFunc slipRateRatioFunc = new ArbitrarilyDiscretizedFunc();
		slipRateRatioFunc.setName("Normalized Slip-Rate Residuals - (Final_SR-Data_SR)/SR_Sigma");
		double normModResids[] = segmentedSource.getNormModSlipRateResids();
		for(int seg=0; seg<normModResids.length; ++seg) {
			slipRateRatioFunc.set((double)seg+1, normModResids[seg]);
		 }
		this.slipRatesRatioList = new ArrayList<ArbitrarilyDiscretizedFunc>();
		slipRatesRatioList.add(slipRateRatioFunc);
	}
	
	/**
	 * Generate function list for slip rates
	 * 
	 * @param segmentedSource
	 */
	private void generateSlipRateFuncList(A_FaultSegmentedSource segmentedSource, 
			FaultSegmentData faultSegmentData) {
		ArbitrarilyDiscretizedFunc origSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		origSlipRateFunc.setName("Orig Slip Rate");
		ArbitrarilyDiscretizedFunc origMinSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		origMinSlipRateFunc.setName("Min Orig Slip Rate");
		ArbitrarilyDiscretizedFunc origMaxSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		origMaxSlipRateFunc.setName("Max Orig Slip Rate");
		ArbitrarilyDiscretizedFunc modSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		modSlipRateFunc.setName("Modified Slip Rate");
		ArbitrarilyDiscretizedFunc modMinSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		modMinSlipRateFunc.setName("Min Modified Slip Rate");
		ArbitrarilyDiscretizedFunc modMaxSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		modMaxSlipRateFunc.setName("Max Modified Slip Rate");
		ArbitrarilyDiscretizedFunc finalSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		finalSlipRateFunc.setName("Final Slip Rate");
		double origSlipRate, origSlipRateStdDev, finalSlipRate, fraction;
		for(int seg=0; seg<faultSegmentData.getNumSegments(); ++seg) {
			 // Original Slip Rates
			origSlipRate = faultSegmentData.getSegmentSlipRate(seg)*1e3;
			origSlipRateStdDev = faultSegmentData.getSegSlipRateStdDev(seg)*1e3;
			origSlipRateFunc.set((double)seg+1, origSlipRate);
			origMinSlipRateFunc.set((double)seg+1, origSlipRate-2*origSlipRateStdDev);
			origMaxSlipRateFunc.set((double)seg+1, origSlipRate+2*origSlipRateStdDev);
			// Modified Slip Rates
			fraction = 1-segmentedSource.getMoRateReduction();
			modSlipRateFunc.set((double)seg+1, origSlipRate*fraction);
			modMinSlipRateFunc.set((double)seg+1, (origSlipRate-2*origSlipRateStdDev)*fraction);
			modMaxSlipRateFunc.set((double)seg+1, (origSlipRate+2*origSlipRateStdDev)*fraction);
			// Final slip Rate
			finalSlipRate  = segmentedSource.getFinalSegSlipRate(seg);
			finalSlipRateFunc.set((double)seg+1, finalSlipRate*1e3);
		 }
		slipRatesList = new ArrayList<ArbitrarilyDiscretizedFunc>();
		slipRatesList.add(origSlipRateFunc);
		slipRatesList.add(origMinSlipRateFunc);
		slipRatesList.add(origMaxSlipRateFunc);
		slipRatesList.add(modSlipRateFunc);
		slipRatesList.add(modMinSlipRateFunc);
		slipRatesList.add(modMaxSlipRateFunc);
		slipRatesList.add(finalSlipRateFunc);
	}
	
	/**
	 * Generate function list for event rates
	 * 
	 * @param segmentedSource
	 */
	private void generateEventRateFuncList(A_FaultSegmentedSource segmentedSource, 
			FaultSegmentData faultSegmentData) {
		ArbitrarilyDiscretizedFunc origEventRateFunc = new ArbitrarilyDiscretizedFunc();
		origEventRateFunc.setName("Data Event Rate");
		ArbitrarilyDiscretizedFunc minEventRateFunc = new ArbitrarilyDiscretizedFunc();
		minEventRateFunc.setName("Min Event Rate");
		ArbitrarilyDiscretizedFunc maxEventRateFunc = new ArbitrarilyDiscretizedFunc();
		maxEventRateFunc.setName("Max Event Rate");
		ArbitrarilyDiscretizedFunc finalEventRateFunc = new ArbitrarilyDiscretizedFunc();
		finalEventRateFunc.setName("Final (Post-Inversion) Event Rate");
		ArbitrarilyDiscretizedFunc predEventRateFunc = new ArbitrarilyDiscretizedFunc();
		predEventRateFunc.setName("Predicted Event Rate from Apriori Rupture Rates");
		double origEventRate, stdDevEventRate, predEventRate, finalEventRate;
		for(int seg=0; seg<faultSegmentData.getNumSegments(); ++seg) {
			origEventRate = faultSegmentData.getSegRateMean(seg);
			stdDevEventRate = faultSegmentData.getSegRateStdDevOfMean(seg);
			finalEventRate  = segmentedSource.getFinalSegmentRate(seg);
			predEventRate = segmentedSource.getSegRateFromAprioriRates(seg);
			origEventRateFunc.set((double)seg+1, origEventRate);
			minEventRateFunc.set((double)seg+1, origEventRate-2*stdDevEventRate);
			maxEventRateFunc.set((double)seg+1, origEventRate+2*stdDevEventRate);
			predEventRateFunc.set((double)seg+1, predEventRate);
			finalEventRateFunc.set((double)seg+1, finalEventRate);
		 }
		this.eventRatesList = new ArrayList<ArbitrarilyDiscretizedFunc>();
		eventRatesList.add(origEventRateFunc);
		eventRatesList.add(minEventRateFunc);
		eventRatesList.add(maxEventRateFunc);
		eventRatesList.add(predEventRateFunc);
		eventRatesList.add(finalEventRateFunc);
	}
	
	/**
	 * Generate function list for recurrence intervals
	 * 
	 * @param segmentedSource
	 */
	private void generateEventRateRatioFuncList(A_FaultSegmentedSource segmentedSource) {
		ArbitrarilyDiscretizedFunc eventRateRatioFunc = new ArbitrarilyDiscretizedFunc();
		eventRateRatioFunc.setName("Normalized Event Rate Residuals - (Final_ER-Data_ER)/ER_Sigma");
		double normResids[] = segmentedSource.getNormDataER_Resids();
		for(int seg=0; seg<normResids.length; ++seg) {
			if(Double.isNaN(normResids[seg])) continue;
			eventRateRatioFunc.set((double)seg+1, normResids[seg]);
		 }
		this.eventRatesRatioList = new ArrayList<ArbitrarilyDiscretizedFunc>();
		eventRatesRatioList.add(eventRateRatioFunc);
	}
	
	
	/**
	 * Set mag and slip
	 * @param segmetedFaultData
	 * @param isAseisReducesArea
	 * @param magAreaRelationships
	 */
	private void setMagAndSlipsString(FaultSegmentData segmetedFaultData, boolean isAseisReducesArea, ArrayList magAreaRelationships, A_FaultSegmentedSource segmentedSource ) {
		magAreasTextArea.setText("");
		if(segmetedFaultData==null) return ;
		int numSegs = segmetedFaultData.getNumSegments();
		String summaryString = "MAGS & AVE SLIPS IMPLIED BY M(A) RELATIONS\n"+
								"------------------------------------------\n\n";
		for(int i=0; i<magAreaRelationships.size(); ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRelationships.get(i);
			summaryString+="Segment  Mag       Ave-slip (m) for  ("+magAreaRel.getName()+")\n";
			for(int j=0; j<numSegs; ++j) {
				double mag = magAreaRel.getMedianMag(segmetedFaultData.getSegmentArea(j)/1e6);
				double moment = MomentMagCalc.getMoment(mag);
				summaryString+=(j+1)+"              "+MAG_FORMAT.format(mag)+"      "+SLIP_FORMAT.format(FaultMomentCalc.getSlip(segmetedFaultData.getSegmentArea(j), moment))+"\n";
			}
			double mag = magAreaRel.getMedianMag(segmetedFaultData.getTotalArea()/1e6);
			double moment = MomentMagCalc.getMoment(mag);
			summaryString+="All            "+MAG_FORMAT.format(mag)+"      "+SLIP_FORMAT.format(FaultMomentCalc.getSlip(segmetedFaultData.getTotalArea(), moment))+"\n\n";		
		}
		String text = MSG_ASEIS_REDUCES_SLIPRATE;
		if(isAseisReducesArea) text = MSG_ASEIS_REDUCES_AREA;
		String predError = "Gen. Pred. Error = "+(float)segmentedSource.getGeneralizedPredictionError()+"\n\n";
		magAreasTextArea.setText(predError+getLegend()+"\n\n"+text+"\n\n"+getRateConstraints(segmetedFaultData)+"\n\n"+summaryString);
		magAreasTextArea.setCaretPosition(0);
	}
	
	/**
	 * Get rate constraints for the segments
	 * 
	 * @param segmetedFaultData
	 */
	private String getRateConstraints(FaultSegmentData segmetedFaultData) {
		String rateConstraintStr = "Rate Constraints for the Segments \n"+
									"---------------------------------\n\n";
		rateConstraintStr+="Seg\tRate\t\tSigma\n";
		int numSegs = segmetedFaultData.getNumSegments();
		for(int segIndex=0; segIndex<numSegs; ++segIndex) {
			ArrayList<SegRateConstraint> segConstraintList = segmetedFaultData.getSegRateConstraints(segIndex);
			for(int i=0; i<segConstraintList.size(); ++i)
				rateConstraintStr+=(segIndex+1)+"\t"+(float)segConstraintList.get(i).getMean()+"\t\t"+(float)segConstraintList.get(i).getStdDevOfMean()+"\n";
		}

		
		return rateConstraintStr;
	}
	
	
	private String getLegend() {
		String legend = "Legend:";
		legend+="-------";
		legend+="Orig SR \t- segment slip rate (mm/yr)\n";
		legend += "\t (possibly reduced by aseis factor, but not by fract ABC removed)\n";
		legend += "SR Sigma\t- standard deviation of Orig SR\n";
		legend += "Final SR\t- Post-inversion segment slip rate\n";
		legend += "\t (reduced by aseis factor & fract ABC removed)\n";
		legend += "Area\t- sq km\n";
		legend += "\t (possibly reduced by aseis factor, but not by fract ABC removed)\n";
		legend += "Length\t- km\n";
		legend += "Mo Rate\t- Moment Rate (Newton-Meters/yr)\n";
		legend += "\t (reduced by aseis factor, but not by fract ABC removed)\n";
		legend += "Data ER\t- Mean Event Rate for segment (1/years) from Parsons/Dawson table\n";
		legend += "ER Sigma\t- Standard deviation of Mean Event Rate for segment (1/years) from Parsons/Dawson table\n";
		legend += "Pred ER\t- Segment mean Event Rate predicted from A Priori Rates\n";
		legend += "Final ER\t- Final (post inversion) segment mean Event Rate\n";
		return legend;
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return plottingFuncList;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public boolean getXLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return yLog;	
		
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return xAxisLabel;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		return yAxisLabel;
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		return plottingFeatures;
	}
	

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		//return 5.0;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		//return 9.255;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		//return 1e-4;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		//return 10;
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
}


/**
 * Fault Section Table Model
 * 
 * @author vipingupta
 *
 */
class FaultSectionTableModel extends AbstractTableModel {
//	 column names
	private final static String[] columnNames = { "Section Name", "Slip Rate (mm/yr)", "Slip Std Dev",
		"Aseismic Factor","Length (km)","Down Dip Width (km)", "Area (sq-km)",
		"Upper Depth (km)", "Lower Depth (km)", "Ave Dip (degrees)"};
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat ASEISMSIC__FORMAT = new DecimalFormat("0.00");
	private ArrayList faultSectionsPrefDataList = new ArrayList();
	
	/**
	 * default constructor
	 *
	 */
	public FaultSectionTableModel() {
		this(null);
	}
	
	/**
	 *  Preferred Fault section data
	 *  
	 * @param faultSectionsPrefDataList  ArrayList of PrefFaultSedctionData
	 */
	public FaultSectionTableModel(ArrayList faultSectionsPrefDataList) {
		setFaultSectionData(faultSectionsPrefDataList);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setFaultSectionData(ArrayList faultSectionsPrefDataList) {
		this.faultSectionsPrefDataList =   faultSectionsPrefDataList;
	}
	
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if(faultSectionsPrefDataList==null) return 0;
		return (faultSectionsPrefDataList.size()); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		if(faultSectionsPrefDataList==null) return "";
		FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData) faultSectionsPrefDataList.get(rowIndex);
		
		//"Name", "Slip Rate (cm/yr)", 
			//"Aseismic Factor","Length (km)","Down Dip Width (km)", "Area (sq-km)",
			//"Upper Depth (km)", "Lower Depth (km)", "Ave Dip (degrees)"};
		
		switch(columnIndex) {
			case 0:
				return faultSectionPrefData.getSectionName();
			case 1: // convert to mm/yr
				return SLIP_RATE_FORMAT.format(faultSectionPrefData.getAveLongTermSlipRate());
			case 2: 
				return SLIP_RATE_FORMAT.format(faultSectionPrefData.getSlipRateStdDev());
			case 3:
				return ASEISMSIC__FORMAT.format(faultSectionPrefData.getAseismicSlipFactor());
			case 4:
				// km
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getLength());
			case 5:
				// convert to km
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getDownDipWidth());
			case 6:
				// sq km
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getDownDipWidth() *
						faultSectionPrefData.getLength());
			case 7:
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getAveUpperDepth());
			case 8:
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getAveLowerDepth());
			case 9:
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getAveDip());
		}
		return "";
	}
}


/**
 * Segment Table Model
 * 
 * @author vipingupta
 *
 */
class SegmentDataTableModel extends AbstractTableModel {
	// column names
	private final static String[] columnNames = { "Seg Name", "Num", "Orig SR", "SR Sigma",
		"Final SR","Area",
		"Length", "Mo Rate", "Data ER", "ER Sigma", "Pred ER", "Final ER", /*"Char Slip",*/ 
		 "Sections In Segment"};
	private FaultSegmentData segFaultData;
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat CHAR_SLIP_RATE_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.0");
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.000E0");
	private A_FaultSegmentedSource segmentedSource;
	
	
	/**
	 * default constructor
	 *
	 */
	public SegmentDataTableModel() {
		this(null, null);
	}
	
	/**
	 * Segmented Fault data
	 * @param segFaultData
	 */
	public SegmentDataTableModel( FaultSegmentData segFaultData, A_FaultSegmentedSource segmentedSource) {
		setSegmentedFaultData(segFaultData, segmentedSource);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setSegmentedFaultData(FaultSegmentData segFaultData, A_FaultSegmentedSource segmentedSource) {
		this.segFaultData =   segFaultData;
		this.segmentedSource = segmentedSource;
	}
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if(segFaultData==null) return 0;
		return (segFaultData.getNumSegments()+1); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		if(segFaultData==null) return "";
		if(rowIndex == segFaultData.getNumSegments()) return getTotalValues(columnIndex);
		
		
		//"Seg Name", "Num", "Slip Rate (mm/yr)", "Area (sq-km)",
		//"Length (km)", "Moment Rate", "Orig MRI", "Pred MRI", "final MRI", "Char Slip", "Sections In Segment"
		
		switch(columnIndex) {
			case 0:
				return segFaultData.getSegmentName(rowIndex);
			case 1:
				return ""+(rowIndex+1);
			case 2: 
				// convert to mm/yr
				return SLIP_RATE_FORMAT.format(segFaultData.getSegmentSlipRate(rowIndex)*1e3);
			case 3: 
				// convert to mm/yr
				return SLIP_RATE_FORMAT.format(segFaultData.getSegSlipRateStdDev(rowIndex)*1e3);
			case 4:
				return SLIP_RATE_FORMAT.format(segmentedSource.getFinalSegSlipRate(rowIndex)*1e3);
			case 5:
				// convert to sq km
				return AREA_LENGTH_FORMAT.format(segFaultData.getSegmentArea(rowIndex)/1e6);
			case 6:
				// convert to km
				return AREA_LENGTH_FORMAT.format(segFaultData.getSegmentLength(rowIndex)/1e3);
			case 7:
				return MOMENT_FORMAT.format(segFaultData.getSegmentMomentRate(rowIndex));
			case 8:
				return SLIP_RATE_FORMAT.format(segFaultData.getSegRateMean(rowIndex));
			case 9:
				return SLIP_RATE_FORMAT.format(segFaultData.getSegRateStdDevOfMean(rowIndex));
			case 10:
				return SLIP_RATE_FORMAT.format(segmentedSource.getSegRateFromAprioriRates(rowIndex));
			case 11:
				return SLIP_RATE_FORMAT.format(segmentedSource.getFinalSegmentRate(rowIndex));
			/*case 12:	
				//System.out.println(this.predMRI[rowIndex]+","+segFaultData.getSegmentSlipRate(rowIndex));
				//return this.predMRI[rowIndex]*segFaultData.getSegmentSlipRate(rowIndex);
				return ""+ CHAR_SLIP_RATE_FORMAT.format(getCharSlip(rowIndex));
			case 11: // FOR STRESS DROP
				double ddw = segFaultData.getOrigSegmentDownDipWidth(rowIndex)/1e3; // ddw in km 
				double charSlip = getCharSlip(rowIndex)*100; // char slip in cm
				double segStressDrop = 2*charSlip*3e11*1e-11/(Math.PI *ddw); 
				return ""+(float)segStressDrop;*/
			case 12:
				return ""+segFaultData.getSectionsInSeg(rowIndex);
		}
		return "";
	}

	/**
	 * Get Char slip in meter
	 * @param rowIndex
	 * @return
	 */
	private double getCharSlip(int rowIndex) {
		return (1.0/segmentedSource.getSegRateFromAprioriRates(rowIndex))*segFaultData.getSegmentSlipRate(rowIndex);
	}
	
	
	private Object getTotalValues(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return "Total";
		case 1: 
			// convert to mm/yr
			return "";
		case 5:
			// convert to sq km
			return AREA_LENGTH_FORMAT.format(segFaultData.getTotalArea()/1e6);
		case 6:
			// convert to km
			return AREA_LENGTH_FORMAT.format(segFaultData.getTotalLength()/1000);
		case 7:
			return MOMENT_FORMAT.format(segFaultData.getTotalMomentRate());
		}
	return "";
	}
}