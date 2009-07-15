package org.opensha.sha.calc.hazardMap.applet;

import java.util.ArrayList;

import javax.swing.JPanel;

public class MapManager implements OptionPanelListener {
	
	private DataSetSelector selector;
	private Step datasetStep = null;
	private Step chooseMapDownloadStep = null;
	private Step finalStep = null;
	
	private StepsPanel stepPanel;
	
	private OptionPanel options = null;
	private HazardMapApplet applet;
	
	public static final String PLOT_OPTION = "Plot Data";
	public static final String DOWNLOAD_OPTION = "Download Data";
	
	private PlotPanel plotPanel = null;
	
	public MapManager(HazardMapApplet applet) {
		this.applet = applet;
		ArrayList<String> choices = new ArrayList<String>();
		choices.add(PLOT_OPTION);
		choices.add(DOWNLOAD_OPTION);
		options = new OptionPanel("What would you like to do?", choices, this, applet.getConsole());
		
		selector = new DataSetSelector(true);
		datasetStep = selector.getStep();
		
		chooseMapDownloadStep = new Step(options.getButtonPanel(), "What would you like to do?");
		
		finalStep = new Step(new JPanel(), "");
		
		ArrayList<Step> steps = new ArrayList<Step>();
		steps.add(datasetStep);
		steps.add(chooseMapDownloadStep);
		steps.add(finalStep);
		
		stepPanel = new StepsPanel(steps, applet, null, applet.getConsole());
	}
	
	public JPanel getPanel() {
		return stepPanel;
	}

	public void optionSelected(String option) {
		if (option.equals(HazardMapApplet.BACK_OPTION)) {
			applet.loadStep();
		} else if (option.equals(PLOT_OPTION)) {
			if (plotPanel == null)
				plotPanel = new PlotPanel();
			plotPanel.setDatasetID(selector.getSelectedID().getID());
			plotPanel.setRegion(selector.getSelectedDatasetRegion());
			JPanel finalPanel = finalStep.getPanel();
			finalPanel.removeAll();
			finalPanel.add(plotPanel);
			finalPanel.invalidate();
			finalStep.setTitle("Plot Dataset");
			stepPanel.loadNextStep();
		} else if (option.equals(DOWNLOAD_OPTION)) {
			
		}
	}

}
