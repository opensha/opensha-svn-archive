/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.SegRateConstraint;

/**
 * Panel to show the Segments and Fault sections data 
 * @author vipingupta
 *
 */
public class SegmentDataPanel extends JPanel {
	private SegmentDataTableModel segmentTableModel = new SegmentDataTableModel();
	private FaultSectionTableModel faultSectionTableModel = new FaultSectionTableModel();
	private final static String MSG_ASEIS_REDUCES_AREA = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of area (as requested) in the table above; this will also influence the segment slip rates for any segments composed of more than one section (because the slip rates are weight-averaged according to section areas)";
	private final static String MSG_ASEIS_REDUCES_SLIPRATE = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of slip rate (as requested); keep this in mind when interpreting the segment slip rates (which for any segments composed of more than one section are a weight average by section areas)";
	private JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private JTextArea magAreasTextArea = new JTextArea();
	
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat SLIP_FORMAT = new DecimalFormat("0.000");
	
	public SegmentDataPanel() {
		setLayout(new GridBagLayout());
		createGUI();
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
		String legend = "Orig SR - segment slip rate (mm/yr)\n";
		legend += "SR Sigma - standard deviation of Orig SR\n";
		legend += "Final SR - Post-inversion segment slip rate\n";
		legend += "Area - sq km\n";
		legend += "Length - km\n";
		legend += "Mo Rate - Moment Rate (Newton-Meters/yr)\n";
		legend += "Data MRI - Ave Mean Recur Int (years) from Parsons/Dawson table\n";
		legend += "MRI Sigma - Standard deviation of Data MRI\n";
		legend += "Pred MRI - MRI predicated from A Priori Rates\n";
		legend += "Final MRI - Final MRI given MFDs\n";
		return legend;
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