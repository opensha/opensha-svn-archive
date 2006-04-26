package org.opensha.refFaultParamDb.gui.addEdit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.opensha.refFaultParamDb.gui.view.ViewFaultSection;


/**
 * 
 *  this class makes the JTable to view Fault sections within a FaultModel
 * @author vipingupta
 *
 */
public class FaultModelTable extends JTable{
	/**
	 * @param dm
	 */
	public FaultModelTable(TableModel dm) {
		super(dm);
		getTableHeader().setReorderingAllowed(false);
		getColumnModel().getColumn(1).setCellRenderer(new ButtonRenderer());
		addMouseListener(new MouseListener(this));
		// set width of first column 
		TableColumn col1 = getColumnModel().getColumn(0);
		col1.setPreferredWidth(65);
        //col1.setMinWidth(26);
        col1.setMaxWidth(65);
        // set width of second column
        TableColumn col2 = getColumnModel().getColumn(1);
		col2.setPreferredWidth(65);
        //col2.setMinWidth(26);
        col2.setMaxWidth(65);
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
	private ViewFaultSection viewFaultSection ;
	
	public MouseListener(JTable table) {
		this.table = table;
	}
	
	public void mouseClicked(MouseEvent event) {
		//System.out.println("Mouse clicked");
		Point p = event.getPoint();
        int row = table.rowAtPoint(p);
        int column = table.columnAtPoint(p); // This is the view column!
        if(column!=1) return;
        if(viewFaultSection==null) viewFaultSection = new ViewFaultSection();
        viewFaultSection.setSelectedFaultSectionNameId((String)table.getModel().getValueAt(row, column));
	}

}	

class ButtonRenderer extends JButton implements TableCellRenderer {

	  public ButtonRenderer() {
	    setOpaque(true);
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
	      boolean isSelected, boolean hasFocus, int row, int column) {
	    setText("Info");
	    return this;
	  }
	}

