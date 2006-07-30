/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.A_FaultSegmentedSource;

/**
 * Show the rupture data in the window
 * 
 * @author vipingupta
 *
 */
public class RuptureDataPanel extends JPanel {
	private RuptureTableModel rupTableModel = new RuptureTableModel();
	
	public RuptureDataPanel() {
		this.setLayout(new GridBagLayout());
		
		add(new JScrollPane(new JTable(this.rupTableModel)),new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		
	}
	
	/**
	 * Set the source to update the rupture info
	 * 
	 * @param aFaultSegmentedSource
	 */
	public void setSource(A_FaultSegmentedSource aFaultSegmentedSource) {
		rupTableModel.setFaultSegmentedSource(aFaultSegmentedSource);
		rupTableModel.fireTableDataChanged();
	}
}



/**
* Fault Section Table Model
* 
* @author vipingupta
*
*/
class RuptureTableModel extends AbstractTableModel {
//	 column names
	private final static String[] columnNames = { "Rup Index", "Area (sq km)", "Mean Mag", 
		"Final Rate", "A Priori Rate", "Short Name", "Long Name"};
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat RATE_FORMAT = new DecimalFormat("0.00000");
	private A_FaultSegmentedSource aFaultSegmentedSource;
	
	/**
	 * default constructor
	 *
	 */
	public RuptureTableModel() {
		this(null);
	}
	
	/**
	 *  Preferred Fault section data
	 *  
	 * @param faultSectionsPrefDataList  ArrayList of PrefFaultSedctionData
	 */
	public RuptureTableModel(A_FaultSegmentedSource aFaultSegmentedSource) {
		setFaultSegmentedSource(aFaultSegmentedSource);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setFaultSegmentedSource(A_FaultSegmentedSource aFaultSegmentedSource) {
		this.aFaultSegmentedSource =   aFaultSegmentedSource;
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
		if(aFaultSegmentedSource==null) return 0;
		return (aFaultSegmentedSource.getNumRuptures()); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		
		//{ "Rup Index", "Rup Area", "Rup Mag", 
			//"Short Name", "Long Name"};
		
		if(aFaultSegmentedSource==null) return "";
		
		switch(columnIndex) {
			case 0:
				return ""+(rowIndex+1);
			case 1: 
				return AREA_LENGTH_FORMAT.format(aFaultSegmentedSource.getRupArea(rowIndex)/1e6);
			case 2:
				return MAG_FORMAT.format(aFaultSegmentedSource.getRupMeanMag(rowIndex));
			case 3:
				return RATE_FORMAT.format(aFaultSegmentedSource.getRupRate(rowIndex));
			case 4:
				return RATE_FORMAT.format(aFaultSegmentedSource.getAPrioriRupRate(rowIndex));
			case 5:
				return aFaultSegmentedSource.getShortRupName(rowIndex);
			case 6:
				return aFaultSegmentedSource.getLongRupName(rowIndex);
		}
		return "";
	}
}

