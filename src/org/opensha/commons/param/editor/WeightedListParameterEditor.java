package org.opensha.commons.param.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.WeightedList;
import org.opensha.commons.gui.WeightedListGUI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WeightedListParameter;

public class WeightedListParameterEditor extends ParameterEditor {

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
	public void setParameter(ParameterAPI model) {
		if (!(model instanceof WeightedListParameter))
			throw new IllegalArgumentException("parameter must be a WeightedListParameter");
		super.setParameter(model);
	}

	@Override
	protected void addWidget() {
		WeightedList<? extends NamedObjectAPI> list = (WeightedList<? extends NamedObjectAPI>) model.getValue();
		
		if (gui == null) {
			gui = new WeightedListGUI(list);
			gui.setPreferredSize(new Dimension(400, 600));
//			gui.setMinimumSize(new Dimension(100, 100));
			widgetPanel.setLayout(new BorderLayout());
		} else {
			gui.setList(list);
		}
		
		gui.invalidate();
		widgetPanel.add(gui, BorderLayout.CENTER);
	}

	@Override
	protected void removeWidget() {
		widgetPanel.removeAll();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		gui.setEnabled(isEnabled);
		super.setEnabled(isEnabled);
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

}
