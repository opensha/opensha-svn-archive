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
	private double[] mags = { 5.0, 6.0, 6.5, 6.7, 7.0, 7.5, 8.0 };
	private String[] columns = { "Mags", "A-Faults", "B-Faults", "Non-CA B-Faults", "C-Zones", "Background", "Total"};
	private double data[][];
	
	public ProbsTableModel(UCERF2 ucerf2) {
		int numDataRows = mags.length;
		int numDataCols = columns.length-1;
		data = new double[numDataRows][numDataCols];
		for(int i=0; i<numDataRows; ++i) {
			double mag = mags[i];
			data[i][0] = ucerf2.getTotal_A_FaultsProb(mag);
			data[i][1] = ucerf2.getTotal_B_FaultsProb(mag);
			data[i][2] = ucerf2.getTotal_NonCA_B_FaultsProb(mag);
			data[i][3] = ucerf2.getTotal_C_ZoneProb(mag);
			data[i][4] = ucerf2.getTotal_BackgroundProb(mag);
			data[i][5] = ucerf2.getTotalProb(mag);
		}
		
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
			 default:
				return ""+data[rowIndex][columnIndex-1];
		}
	}
}
