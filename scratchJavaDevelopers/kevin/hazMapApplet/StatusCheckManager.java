package scratchJavaDevelopers.kevin.hazMapApplet;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusCheckManager extends StepManager {
	
	private Step selectStep;
	private Step statusStep;
	
	public StatusCheckManager(HazardMapApplet parent) {
		super(parent, null, parent.getConsole());
		selectStep = DataSetSelector.createStep();
		
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(new JLabel("Status"));
		
		statusStep = new Step(statusPanel, "Calculation Status");
		
		steps.add(selectStep);
		steps.add(statusStep);
		
		this.init();
	}

}
