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
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WeightedListParameter;

public class WeightedListParameterEditor extends ParameterEditor implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final DecimalFormat df = new DecimalFormat("0.0000");
	
	private ArrayList<JSlider> sliders;
	private ArrayList<JTextField> textFields;
	
	private boolean ignoreUpdates = false;
	
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
		WeightedList<?> list = (WeightedList<?>) model.getValue();
		
		if (list == null)
			return;
		
		sliders = new ArrayList<JSlider>();
		textFields = new ArrayList<JTextField>();
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel editorsPanel = new JPanel();
		editorsPanel.setLayout(new BoxLayout(editorsPanel, BoxLayout.Y_AXIS));
		
		for (int i=0; i<list.size(); i++) {
			Object obj = list.get(i);
			
			JSlider slide = new JSlider(0, 100);
			JTextField field = new JTextField(7);
			
			sliders.add(slide);
			textFields.add(field);
			
			JPanel editorPanel = new JPanel();
			editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
			
			String name;
			if (obj instanceof NamedObjectAPI)
				name = ((NamedObjectAPI)obj).getName();
			else
				name = obj.toString();
			
			editorPanel.add(new JLabel(name));
			editorPanel.add(slide);
			editorPanel.add(field);
			
			editorsPanel.add(editorPanel);
		}
		
		updateSliders();
		
		for (JSlider slide : sliders)
			slide.addChangeListener(this);
		
		mainPanel.add(editorsPanel, BorderLayout.CENTER);
		
		mainPanel.setPreferredSize(new Dimension(400, 600));
		
		widgetPanel.add(mainPanel);
		widgetPanel.setPreferredSize(new Dimension(400, 600));
	}
	
	private void updateSliders() {
		WeightedList<?> list = (WeightedList<?>) model.getValue();
		
		ignoreUpdates = true;
		for (int i=0; i<list.size(); i++) {
			double weight = list.getWeight(i);
			int pos = weightToPos(weight);
			
			sliders.get(i).setValue(pos);
			textFields.get(i).setText(df.format(weight));
		}
		ignoreUpdates = false;
	}
	
	private int weightToPos(double weight) {
		return (int)(weight * 100d + 0.5);
	}
	
	private double posToWeight(int pos) {
		return (double)pos / 100d;
	}

	@Override
	protected void removeWidget() {
		widgetPanel.removeAll();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub
		super.setEnabled(isEnabled);
	}
	
	private void updateWeight(int i, double newWeight) {
		if (ignoreUpdates)
			return;
		WeightedList<?> list = (WeightedList<?>) model.getValue();
		
		double[] newWeights = new double[list.size()];
		for (int j=0; j<list.size(); j++)
			newWeights[j] = list.getWeight(j);
		
		double diff = newWeight - list.getWeight(i);
		
		double diffPer = diff / (double)(list.size()-1);
		
		// now adjust for sliders that are going to hit zero or one
		for (int j=0; j<list.size(); j++) {
			double weight = list.getWeight(j) - diffPer;
			
		}
		
		double sum = 0;
		for (int j=0; j<list.size(); j++) {
			double weight;
			if (i == j) {
				weight = newWeight;
			} else {
				weight = list.getWeight(j) - diffPer;
				if (weight < 0)
					weight = 0;
				if (weight > 1)
					weight = 1;
			}
			sum += weight;
//			newWeights.add(weight);
		}
		
		System.out.println("setting for new sum: " + sum);
//		list.setWeights(newWeights);
		updateSliders();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSlider) {
			for (int i=0; i<sliders.size(); i++) {
				JSlider slide = sliders.get(i);
				if (e.getSource() == slide) {
					double weight = posToWeight(slide.getValue());
					updateWeight(i, weight);
				}
			}
		}
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
