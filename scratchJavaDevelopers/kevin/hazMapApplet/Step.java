package scratchJavaDevelopers.kevin.hazMapApplet;

import javax.swing.JPanel;

public class Step {
	
	private JPanel panel;
	private String title;
	
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

}
