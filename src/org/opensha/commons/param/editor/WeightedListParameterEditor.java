package org.opensha.commons.param.editor;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.opensha.commons.data.WeightedList;
import org.opensha.commons.gui.WeightedListGUI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WeightedListParameter;

public class WeightedListParameterEditor extends NewParameterEditor<WeightedList<?>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private WeightedListGUI gui;
	
	public WeightedListParameterEditor() {
		super();
	}
	
	public WeightedListParameterEditor(ParameterAPI model) {
		super(model);
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		gui.setEnabled(isEnabled);
	}
	
	public static void main(String[] args) {
		ArrayList<String> objects = new ArrayList<String>();
		ArrayList<Double> weights = new ArrayList<Double>();
		objects.add("item 1");
		weights.add(0.25);
		objects.add("item 2");
		weights.add(0.25);
		objects.add("item 3");
		weights.add(0.25);
		objects.add("item 4");
		weights.add(0.25);
		
		WeightedList<String> list =
			new WeightedList<String>(objects, weights);
		
		WeightedListParameter<String> param =
			new WeightedListParameter<String>("my param", list);
		
		JFrame frame = new JFrame();
		frame.setSize(400, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new WeightedListParameterEditor(param));
//		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public boolean isParameterSupported(ParameterAPI<WeightedList<?>> param) {
		return true;
	}

	@Override
	protected JComponent buildWidget() {
		if (gui == null) {
			gui = new WeightedListGUI(getList());
		}
//		gui.setPreferredSize(new Dimension(400, 600));
		return gui;
	}
	
	private WeightedList<?> getList() {
		ParameterAPI<WeightedList<?>> param = getParameter();
		if (param == null)
			return null;
		else
			return param.getValue();
	}

	@Override
	protected JComponent updateWidget() {
		gui.setList(getList());
		return gui;
	}

}
