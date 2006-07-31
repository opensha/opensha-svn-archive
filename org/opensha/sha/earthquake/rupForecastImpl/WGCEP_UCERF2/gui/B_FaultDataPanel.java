/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.gui.WG02_RuptureModelsGraphWindowAPI_Impl;


/**
 * 
 * Show the B faults info in the table
 *  
 * @author vipingupta
 *
 */
public class B_FaultDataPanel extends JPanel {
	private B_FaultDataTableModel bFaultTableModel = new B_FaultDataTableModel();
	
	public B_FaultDataPanel() {
		this.setLayout(new GridBagLayout());
		add(new JScrollPane(new B_Faults_Table(this.bFaultTableModel)),new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
	}
	
	public void setB_FaultSources(ArrayList bFaultSources) {
		bFaultTableModel.setUnsegmentedSourceList(bFaultSources);
		bFaultTableModel.fireTableDataChanged();
	}
}


/**
 * Segment Table Model
 * 
 * @author vipingupta
 *
 */
class B_FaultDataTableModel extends AbstractTableModel {
	// column names
	private final static String[] columnNames = { "Name", "Mag", "Tot Rate","Slip Rate (mm/yr)", "Area (sq-km)",
		"Length (km)", "Moment Rate", "Ave Aseismicity", "Mag Freq Dist"};
	private ArrayList unsegmentedSourceList;
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.#####E0");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	
	
	/**
	 * default constructor
	 *
	 */
	public B_FaultDataTableModel() {
		this(null);
	}
	
	/**
	 * B-fault Fault data
	 * @param segFaultData
	 */
	public B_FaultDataTableModel( ArrayList unsegmentedSourceList) {
		setUnsegmentedSourceList(unsegmentedSourceList);
	}
	
	/**
	 * Set the B-fault fault data
	 * @param segFaultData
	 */
	public void setUnsegmentedSourceList(ArrayList unsegmentedSourceList) {
		this.unsegmentedSourceList =   unsegmentedSourceList;
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
		if(this.unsegmentedSourceList==null) return 0;
		return this.unsegmentedSourceList.size(); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		if(this.unsegmentedSourceList==null) return "";
		UnsegmentedSource source = (UnsegmentedSource)unsegmentedSourceList.get(rowIndex);
		FaultSegmentData faultSegmentData = source.getFaultSegmentData();
		//{ "Name", "Mag", "Tot Rate","Slip Rate (mm/yr)", "Area (sq-km)",
		//	"Length (km)", "Moment Rate", "Ave Aseismicity"};
		switch(columnIndex) {
			case 0:
				return faultSegmentData.getFaultName();
			case 1:
				return MAG_FORMAT.format(source.getSourceMag());
			case 2:
				return ""+(float)source.getMagFreqDist().getTotalIncrRate();
			case 3: 
				// convert to mm/yr
				return SLIP_RATE_FORMAT.format(faultSegmentData.getTotalAveSlipRate()*1e3);
			case 4:
				// convert to sq km
				return AREA_LENGTH_FORMAT.format(faultSegmentData.getTotalArea()/1e6);
			case 5:
				// convert to km
				return AREA_LENGTH_FORMAT.format(faultSegmentData.getTotalLength()/1e3);
			case 6:
				return MOMENT_FORMAT.format(faultSegmentData.getTotalMomentRate());
			case 7:
				return ""+faultSegmentData.getTotalAveAseismicityFactor();
			case 8:
				ArrayList funcs = new ArrayList();
				funcs.add(source.getMagFreqDist());
				funcs.add(source.getVisibleSourceMagFreqDist());
				return funcs;
		}
		return "";
	}
}




/**
* @author vipingupta
*
*/
class B_Faults_Table extends JTable {
	private final static String MFD = "Mag Freq Dist";
	/**
	 * @param dm
	 */
	public B_Faults_Table(TableModel dm) {
		super(dm);
		getTableHeader().setReorderingAllowed(false);
		getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer(MFD));
		addMouseListener(new MouseListener(this));
		// set width of first column 
		TableColumn col1 = getColumnModel().getColumn(1);
		col1.setPreferredWidth(125);
       //col1.setMinWidth(26);
       //col1.setMaxWidth(125);
       // set width of second column
       TableColumn col2 = getColumnModel().getColumn(2);
		col2.setPreferredWidth(125);
       //col2.setMinWidth(26);
       //col2.setMaxWidth(125);
	}	
}



/**
 * It handles the clicking whenever user clicks on JTable
 * 
 * @author vipingupta
 *
 */
class MouseListener extends MouseAdapter {
	private JTable table;
	
	public MouseListener(JTable table) {
		this.table = table;
	}
	
	public void mouseClicked(MouseEvent event) {
		//System.out.println("Mouse clicked");
		Point p = event.getPoint();
        int row = table.rowAtPoint(p);
        int column = table.columnAtPoint(p); // This is the view column!
        TableModel tableModel = table.getModel();
        if(column==8) { // edit slip rate
        	ArrayList funcs = (ArrayList)tableModel.getValueAt(row, column);
        	new WG02_RuptureModelsGraphWindowAPI_Impl(funcs, "Mag", "Rate", "Mag Rate");
        }
	}

}	

class ButtonRenderer extends JButton implements TableCellRenderer {

	  public ButtonRenderer(String text) {
	    setText(text);
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
	      boolean isSelected, boolean hasFocus, int row, int column) {
	    return this;
	  }
	}


