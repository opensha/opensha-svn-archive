package org.opensha.sha.calc.hazardMap.applet;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.opensha.commons.gui.ConsoleWindow;

public class StepManager {

	protected StepsPanel panel;
	
	protected Loadable parent, next;
	
	protected ArrayList<Step> steps = new ArrayList<Step>();
	
	ConsoleWindow console;
	
	public StepManager(Loadable parent, Loadable next, ConsoleWindow console) {
		this.parent = parent;
		this.next = next;
		this.console = console;
	}
	
	public void init() {
		panel = new StepsPanel(steps, parent, next, console);
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
}
