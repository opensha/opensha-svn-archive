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
	private JButton slipRateButton = new JButton("Plot Slip Rate for Segments");
	private JButton mriButton = new JButton("Plot Recurrence Intervals");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat SLIP_FORMAT = new DecimalFormat("0.000");
	private ArrayList<ArbitrarilyDiscretizedFunc>slipRatesList;
	private ArrayList<ArbitrarilyDiscretizedFunc>recurIntvList;
	private final static PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      new Color(255,0,0), 10); // RED Cross symbols
	private final static PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      new Color(0,0,0), 2); 
	private final static PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      new Color(0,255,0), 2); 
	private String xAxisLabel, yAxisLabel;
	private ArrayList<PlotCurveCharacterstics> plottingFeatures, slipRatePlottingFeatures, recurIntvPlottingFeatures;
	private ArrayList<ArbitrarilyDiscretizedFunc> plottingFuncList;
	
	public SegmentDataPanel() {
		setLayout(new GridBagLayout());
		createGUI();
		slipRateButton.addActionListener(this);
		mriButton.addActionListener(this);
		this.makePlottingFeaturesList();
	}
	
	private void makePlottingFeaturesList() {
		// slip rate plotting features
		slipRatePlottingFeatures = new ArrayList<PlotCurveCharacterstics>();;
		slipRatePlottingFeatures.add(this.PLOT_CHAR1);
		slipRatePlottingFeatures.add(this.PLOT_CHAR1);
		slipRatePlottingFeatures.add(this.PLOT_CHAR1);
		slipRatePlottingFeatures.add(this.PLOT_CHAR3);
		// recur Intv Plotting features
		recurIntvPlottingFeatures = new ArrayList<PlotCurveCharacterstics>();;
		recurIntvPlottingFeatures.add(this.PLOT_CHAR1);
		recurIntvPlottingFeatures.add(this.PLOT_CHAR1);
		recurIntvPlottingFeatures.add(this.PLOT_CHAR1);
		recurIntvPlottingFeatures.add(this.PLOT_CHAR2);
		recurIntvPlottingFeatures.add(this.PLOT_CHAR3);
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
		add(mriButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
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
		setMagAndSlipsString(faultSegmentData, isAseisReducesArea, magAreaRelationships);
		segmentTableModel.setSegmentedFaultData(faultSegmentData,  segmentedSource);
		segmentTableModel.fireTableDataChanged();
		if(faultSegmentData!=null) faultSectionTableModel.setFaultSectionData(faultSegmentData.getPrefFaultSectionDataList());
		else faultSectionTableModel.setFaultSectionData(null);
		faultSectionTableModel.fireTableDataChanged();
		
		if(segmentedSource==null) { // for unsegmented source
			this.mriButton.setVisible(false);
			this.slipRateButton.setVisible(false);
		} else { // Segmented source
			this.mriButton.setVisible(true);
			this.slipRateButton.setVisible(true);
			generateSlipRateFuncList(segmentedSource, faultSegmentData);
			generateMRI_FuncList(segmentedSource, faultSegmentData);
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
		} else {
			xAxisLabel = "Segment Index";
			yAxisLabel = "Recurrence Interval (years)";
			// plotting features
			plottingFeatures = recurIntvPlottingFeatures;
			plottingFuncList = this.recurIntvList;
		}
		GraphWindow graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(yAxisLabel);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.pack();
		graphWindow.setVisible(true);
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
		ArbitrarilyDiscretizedFunc minSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		minSlipRateFunc.setName("Min Slip Rate");
		ArbitrarilyDiscretizedFunc maxSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		maxSlipRateFunc.setName("Max Slip Rate");
		ArbitrarilyDiscretizedFunc finalSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		finalSlipRateFunc.setName("Final Slip Rate");
		double origSlipRate, slipStdDev, finalSlipRate;
		for(int seg=0; seg<faultSegmentData.getNumSegments(); ++seg) {
			origSlipRate = faultSegmentData.getSegmentSlipRate(seg);
			slipStdDev = faultSegmentData.getSegSlipStdDev(seg);
			finalSlipRate  = segmentedSource.getFinalSegSlipRate(seg);
			origSlipRateFunc.set((double)seg, origSlipRate*1e3);
			minSlipRateFunc.set((double)seg, (origSlipRate-2*slipStdDev)*1e3);
			maxSlipRateFunc.set((double)seg, (origSlipRate+2*slipStdDev)*1e3);
			finalSlipRateFunc.set((double)seg, finalSlipRate*1e3);
		 }
		slipRatesList = new ArrayList<ArbitrarilyDiscretizedFunc>();
		slipRatesList.add(origSlipRateFunc);
		slipRatesList.add(minSlipRateFunc);
		slipRatesList.add(maxSlipRateFunc);
		slipRatesList.add(finalSlipRateFunc);
	}
	
	/**
	 * Generate function list for recurrence intervals
	 * 
	 * @param segmentedSource
	 */
	private void generateMRI_FuncList(A_FaultSegmentedSource segmentedSource, 
			FaultSegmentData faultSegmentData) {
		ArbitrarilyDiscretizedFunc origRecurIntvFunc = new ArbitrarilyDiscretizedFunc();
		origRecurIntvFunc.setName("Data Recurrence Interval");
		ArbitrarilyDiscretizedFunc minRecurIntvFunc = new ArbitrarilyDiscretizedFunc();
		minRecurIntvFunc.setName("Min Recurrence Interval");
		ArbitrarilyDiscretizedFunc maxRecurIntvFunc = new ArbitrarilyDiscretizedFunc();
		maxRecurIntvFunc.setName("Max Recurrence Interval");
		ArbitrarilyDiscretizedFunc finalRecurIntvFunc = new ArbitrarilyDiscretizedFunc();
		finalRecurIntvFunc.setName("Final Recurrence Interval");
		ArbitrarilyDiscretizedFunc predRecurIntvFunc = new ArbitrarilyDiscretizedFunc();
		predRecurIntvFunc.setName("Predicted Recurrence Interval from Apriori Rupture Rates");
		double origRecurIntv, stdDevRecurIntv, predRecurIntv, finalRecurIntv;
		for(int seg=0; seg<faultSegmentData.getNumSegments(); ++seg) {
			origRecurIntv = faultSegmentData.getRecurInterval(seg);
			stdDevRecurIntv = faultSegmentData.getRecurIntervalSigma(seg);
			finalRecurIntv  = segmentedSource.getFinalSegRecurInt(seg);
			predRecurIntv = 1.0/segmentedSource.getSegRateFromAprioriRates(seg);
			origRecurIntvFunc.set((double)seg, origRecurIntv);
			minRecurIntvFunc.set((double)seg, origRecurIntv-2*stdDevRecurIntv);
			maxRecurIntvFunc.set((double)seg, origRecurIntv+2*stdDevRecurIntv);
			predRecurIntvFunc.set((double)seg, predRecurIntv);
			finalRecurIntvFunc.set((double)seg, finalRecurIntv);
		 }
		this.recurIntvList = new ArrayList<ArbitrarilyDiscretizedFunc>();
		recurIntvList.add(origRecurIntvFunc);
		recurIntvList.add(minRecurIntvFunc);
		recurIntvList.add(maxRecurIntvFunc);
		recurIntvList.add(predRecurIntvFunc);
		recurIntvList.add(finalRecurIntvFunc);
	}
	
	
	/**
	 * Set mag and slip
	 * @param segmetedFaultData
	 * @param isAseisReducesArea
	 * @param magAreaRelationships
	 */
	private void setMagAndSlipsString(FaultSegmentData segmetedFaultData, boolean isAseisReducesArea, ArrayList magAreaRelationships ) {
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
		magAreasTextArea.setText(getLegend()+"\n\n"+text+"\n\n"+getRateConstraints(segmetedFaultData)+"\n\n"+summaryString);
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
				rateConstraintStr+=(segIndex+1)+"\t"+(float)segConstraintList.get(i).getMean()+"\t\t"+(float)segConstraintList.get(i).getStdDevToMean()+"\n";
		}

		
		return rateConstraintStr;
	}
	
	
	private String getLegend() {
		String legend = "Orig SR \t- segment slip rate (mm/yr)\n";
		legend += "\t (possibly reduced by aseis factor, but not by fract ABC removed)\n";
		legend += "SR Sigma\t- standard deviation of Orig SR\n";
		legend += "Final SR\t- Post-inversion segment slip rate\n";
		legend += "\t (reduced by aseis factor & fract ABC removed)\n";
		legend += "Area\t- sq km\n";
		legend += "\t (possibly reduced by aseis factor, but not by fract ABC removed)\n";
		legend += "Length\t- km\n";
		legend += "Mo Rate\t- Moment Rate (Newton-Meters/yr)\n";
		legend += "\t (reduced by aseis factor, but not by fract ABC removed)\n";
		legend += "Data MRI\t- Ave Mean Recur Int (years) from Parsons/Dawson table\n";
		legend += "MRI Sigma\t- Standard deviation of Data MRI\n";
		legend += "Pred MRI\t- MRI predicated from A Priori Rates\n";
		legend += "Final MRI\t- Final MRI given MFDs\n";
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
		return true;	
		
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
		"Length", "Mo Rate", "Data MRI", "MRI Sigma", "Pred MRI", "Final MRI", /*"Char Slip",*/ 
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
				return SLIP_RATE_FORMAT.format(segFaultData.getSegSlipStdDev(rowIndex)*1e3);
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
				return ""+Math.round(segFaultData.getRecurInterval(rowIndex));
			case 9:
				return ""+Math.round(segFaultData.getRecurIntervalSigma(rowIndex));
			case 10:
				return ""+Math.round(1.0/segmentedSource.getSegRateFromAprioriRates(rowIndex));
			case 11:
				return ""+Math.round(segmentedSource.getFinalSegRecurInt(rowIndex));
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