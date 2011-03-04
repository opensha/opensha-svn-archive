package org.opensha.commons.param.editor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.opensha.commons.mapping.gmt.gui.CPTListCellRenderer;
import org.opensha.commons.param.ListBasedConstraint;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterConstraintAPI;
import org.opensha.commons.util.cpt.CPT;

public class ConstrainedCPTParameterEditor extends NewParameterEditor<CPT> implements ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox combo;
	
	public ConstrainedCPTParameterEditor(ParameterAPI<CPT> param) {
		super(param);
	}

	@Override
	public boolean isParameterSupported(ParameterAPI<CPT> param) {
		if (param == null)
			return false;
		
		if (param.getValue() == null)
			return false;
		
		if (!(param.getValue() instanceof CPT))
			return false;
		
		ParameterConstraintAPI<?> constraint = param.getConstraint();
		if (constraint == null)
			return false;
		
		if (!(constraint instanceof ListBasedConstraint))
			return false;
		ListBasedConstraint<CPT> lconst = (ListBasedConstraint)constraint;
		if (lconst.getAllowed().size() < 1)
			return false;
		
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		combo.setEnabled(enabled);
	}
	
	private ListBasedConstraint<CPT> getListConst() {
		return (ListBasedConstraint)getParameter().getConstraint();
	}

	@Override
	protected JComponent buildWidget() {
		ListBasedConstraint<CPT> lconst = getListConst();
//		System.out.println("Items: "+lconst.getAllowed().size());
		combo = new JComboBox(lconst.getAllowed().toArray());
		combo.setRenderer(new CPTListCellRenderer(combo));
		combo.setSelectedItem(getValue());
		combo.addItemListener(this);
		return combo;
	}

	@Override
	protected JComponent updateWidget() {
		ListBasedConstraint<CPT> lconst = getListConst();
		combo.removeItemListener(this);
		combo.setModel(new DefaultComboBoxModel(lconst.getAllowed().toArray()));
		combo.setSelectedItem(getValue());
		combo.addItemListener(this);
		return combo;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == combo) {
			CPT selection = (CPT)combo.getSelectedItem();
//			System.out.println("Setting CPT: "+selection.getName());
			setValue(selection);
		}
	}

}
