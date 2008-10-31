package org.opensha.sha.calc.hazardMap.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

public class SubmitPanel extends JPanel implements ActionListener {
	
	private JLabel nameLabel = new JLabel("Dateset Name (Optional)");
	private JTextField nameField = new JTextField();
	private JLabel emailLabel = new JLabel("Email address(s)");
	private JTextField emailField = new JTextField();
	
	private JButton submitButton = new JButton("Submit Calculation");
	
	CreateDataManager manager;
	
	JPanel panel = new JPanel();
	
	public SubmitPanel(CreateDataManager manager) {
		super(new BorderLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		this.manager = manager;
		
		panel.add(leftRightBox(nameLabel, nameField));
		nameField.setColumns(30);
		panel.add(leftRightBox(emailLabel, emailField));
		emailField.setColumns(30);
		
		panel.add(submitButton);
		
		panel.setMaximumSize(new Dimension(300, 200));
		
		JPanel panel2 = new JPanel();
		panel2.add(panel);
		
		submitButton.addActionListener(this);
		
		this.add(panel2, BorderLayout.CENTER);
	}
	
	private JPanel leftRightBox(JComponent left, JComponent right) {
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		panel.add(left);
		panel.add(new JSeparator());
		panel.add(right);
		
		JPanel panel2 = new JPanel();
		
		panel2.add(panel);
		
		return panel2;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == submitButton) {
			manager.submit(nameField.getText(), emailField.getText());
		}
	}

}
