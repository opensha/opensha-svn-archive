/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.gui;

import javax.swing.table.AbstractTableModel;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;

/**
 * @author vipingupta
 * 
 * Table Mode to generate table of contribution of various types of sources at various magnitudes
 *
 */
public class ProbsTableModel extends AbstractTableModel {
	private double[] mags = { 5.0, 6.0, 6.7, 7.0, 7.5, 8.0 };
	private String[] columns = { "Mags", "A-Faults", "B-Faults", "Non-CA B-Faults", "C-Zones", "Background", "Total"};
	private UCERF2 ucerf2;
	
	public ProbsTableModel(UCERF2 ucerf2) {
		this.ucerf2 = ucerf2;
	}
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columns.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columns[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return mags.length;
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		double mag = mags[rowIndex];	
		switch(columnIndex) {
			case 0:
				return ""+mags[rowIndex];
			case 1: // A-Faults
				return ""+(float)ucerf2.getTotal_A_FaultsProb(mag);
			case 2: //B-Faults
				return ""+(float)ucerf2.getTotal_B_FaultsProb(mag);
			case 3: // Non-CA B-Faults
				return ""+(float)ucerf2.getTotal_NonCA_B_FaultsProb(mag);
			case 4: // C-Zones
				return ""+(float)ucerf2.getTotal_C_ZoneProb(mag);
			case 5: // Background
				return ""+(float)ucerf2.getTotal_BackgroundProb(mag);
			case 6: // total
				return ""+(float)ucerf2.getTotalProb(mag);
		}
		return "";
	}
}
