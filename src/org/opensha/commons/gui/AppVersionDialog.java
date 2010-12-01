package org.opensha.commons.gui;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.opensha.commons.util.ApplicationVersion;

public class AppVersionDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextArea textArea;
	private JScrollPane textScroll;

	public AppVersionDialog(String appName, ApplicationVersion appVersion) {
		this.setLayout(new BorderLayout());
		
		String license = DisclaimerDialog.getLicense();
		
		textArea = new JTextArea(license);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textScroll = new JScrollPane(textArea);
		textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		this.add(textScroll, BorderLayout.CENTER);
		String versionText = "<html><b>Application:</b> "+appName;
		versionText += "<br><b>Version:</b> " + appVersion+"</html>";
		
		JLabel versionLabel = new JLabel(versionText);
		versionLabel.setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));
		
		this.add(versionLabel, BorderLayout.SOUTH);
		
		this.setSize(600, 500);
	}

}
