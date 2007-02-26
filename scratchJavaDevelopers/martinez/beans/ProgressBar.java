package scratchJavaDevelopers.martinez.beans;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.*;

@SuppressWarnings("serial")
public class ProgressBar extends JFrame implements Runnable {
	private Component parent = null;
	private JLabel textLabel = null;
	private JPanel otherContent = null;
	private JProgressBar progress = null;
	private int maxProgress = 0;
	private String windowTitle = "";
	private JPanel mainContent = null;
	
	public ProgressBar(Component parent, String textLabel, String title,
			JPanel otherContent, int maxProgress) {
		
		this.parent = parent;
		this.textLabel = new JLabel(textLabel, SwingConstants.CENTER);
		this.otherContent = otherContent;
		this.maxProgress = maxProgress;
		this.windowTitle = title;
		initialize();
	}
	
	private void initialize() {
		mainContent = new JPanel(new GridBagLayout());
		if(maxProgress == 0) {
			progress = new JProgressBar();
			progress.setIndeterminate(true);
		} else {
			progress = new JProgressBar(0, maxProgress);
		}
		
		int ypos = 0;
		// Add the text label if we should
		if(textLabel != null && textLabel.getText().length() > 0) {
			mainContent.add(textLabel, new GridBagConstraints(0, ypos, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
			++ypos;
		}
		// Add the content label if we should
		if(otherContent != null) {
			mainContent.add(otherContent, new GridBagConstraints(0, ypos, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
			++ypos;
		}
		mainContent.add(progress, new GridBagConstraints(0, ypos, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		
		add(mainContent);
		pack();
		double xloc = 0.0; double yloc = 0.0;
		if(parent != null) {
			xloc = parent.getX() + (parent.getWidth() / 2);
			yloc = parent.getY() + (parent.getHeight() / 2);
		} else {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		    xloc = (dim.width - getSize().width) / 2;
		    yloc = (dim.height - getSize().height) / 2;
		}
		
		setLocation((int) xloc, (int) yloc);
		setTitle(windowTitle);
	}

	public void run() {
		setVisible(true);
	}
}
