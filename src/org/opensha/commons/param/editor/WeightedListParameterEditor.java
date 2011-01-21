package org.opensha.commons.param.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.WeightedList;
import org.opensha.commons.gui.WeightedListGUI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WeightedListParameter;

public class WeightedListParameterEditor extends NewParameterEditor<WeightedList<? extends NamedObjectAPI>> {

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
	
	private static class TestClass implements NamedObjectAPI {

		private String name;
		public TestClass(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
	}
	
	public static void main(String[] args) {
		ArrayList<TestClass> objects = new ArrayList<WeightedListParameterEditor.TestClass>();
		ArrayList<Double> weights = new ArrayList<Double>();
		objects.add(new TestClass("item 1"));
		weights.add(0.25);
		objects.add(new TestClass("item 1"));
		weights.add(0.25);
		objects.add(new TestClass("item 1"));
		weights.add(0.25);
		objects.add(new TestClass("item 1"));
		weights.add(0.25);
		
		WeightedList<TestClass> list =
			new WeightedList<WeightedListParameterEditor.TestClass>(objects, weights);
		
		WeightedListParameter<TestClass> param =
			new WeightedListParameter<WeightedListParameterEditor.TestClass>("my param", list);
		
		JFrame frame = new JFrame();
		frame.setSize(400, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new WeightedListParameterEditor(param));
//		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public boolean isParameterSupported(
			ParameterAPI<WeightedList<? extends NamedObjectAPI>> param) {
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
	
	private WeightedList<? extends NamedObjectAPI> getList() {
		ParameterAPI<WeightedList<? extends NamedObjectAPI>> param = getParameter();
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
