package org.opensha.sha.calc.hazardMap.applet;

import java.util.ArrayList;

import javax.swing.JPanel;

public class Step {
	
	private JPanel panel;
	private String title;
	
	private StepsPanel stepsPanel;
	
	ArrayList<StepActivatedListener> listeners = new ArrayList<StepActivatedListener>();
	
	public Step(JPanel panel, String title) {
		this.panel = panel;
		this.title = title;
	}

	public JPanel getPanel() {
		return panel;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setActivated() {
		for (StepActivatedListener listener : listeners) {
			listener.stepActivated(this);
		}
	}
	
	public void addStepActivatedListener(StepActivatedListener listener) {
		listeners.add(listener);
	}
	
	public void removeStepActivatedListener(StepActivatedListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllStepActivatedListeners() {
		listeners.clear();
	}

	public StepsPanel getStepsPanel() {
		return stepsPanel;
	}

	public void setStepsPanel(StepsPanel stepsPanel) {
		this.stepsPanel = stepsPanel;
	}
}
