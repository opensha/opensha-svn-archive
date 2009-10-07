package org.opensha.sha.calc.hazardMap.applet;


public class StatusCheckManager extends StepManager {
	
	private Step selectStep;
	private Step statusStep;
	
	public StatusCheckManager(HazardMapApplet parent) {
		super(parent, null, parent.getConsole());
		selectStep = parent.getSelector().getStep();
		
		StatusPanel statusPanel = new StatusPanel(parent);
		statusStep = new Step(statusPanel, "Calculation Status");
		statusStep.addStepActivatedListener(statusPanel);
		
		steps.add(selectStep);
		steps.add(statusStep);
		
		this.init();
		
//		this.
	}

}
