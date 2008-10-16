package scratchJavaDevelopers.kevin.hazMapApplet;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class DataSetSelector extends JPanel {
	
	public DataSetSelector() {
		super(new BorderLayout());
		
		this.add(new JLabel("Select a dataset here..."));
	}
	
	public static Step createStep() {
		return new Step(new DataSetSelector(), "Select Data Set");
	}
}
