package org.opensha.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * <p>Title: UserAuthorizationCheckWindow</p>
 * <p>Description: This class provide controlled access to the users who want to generate
 * the datasets for the Hazard Maps using Condor at University of Southern California.</p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class UserAuthDialog extends JDialog implements ActionListener {

	private static final boolean D= false;
	
	private boolean canceled = false;
	private boolean exitOnCancel = false;
	
	private static final String ACTION_KEY = "theAction";

	private JPanel passwordPanel = new JPanel();
	private JButton continueButton = new JButton();
	private JPasswordField passwordText = new JPasswordField();
	private JLabel jLabel5 = new JLabel();
	private JButton cancelButton = new JButton();
	private JLabel jLabel2 = new JLabel();
	JTextField usernameText = new JTextField();
	JLabel jLabel1 = new JLabel();
	BorderLayout borderLayout1 = new BorderLayout();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	
	Action actionListener = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				cancelAction();
			} else {
				continueAction();
			}
		}
	};
	
	public UserAuthDialog(Frame owner, boolean exitOnCancel) {
		super(owner, "Enter Username and Password", true);
		this.exitOnCancel = exitOnCancel;
		init();
		this.setLocationRelativeTo(owner);
	}
	
	private void addEnterAction(JComponent comp) {
		InputMap in = comp.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
		in.put(enter, UserAuthDialog.ACTION_KEY);
		ActionMap actionMap = comp.getActionMap();
		actionMap.put(UserAuthDialog.ACTION_KEY, actionListener);
	}


	public void init() {
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		//this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
		usernameText.setForeground(new Color(80, 80, 133));
		usernameText.setBackground(Color.white);
		passwordText.setBackground(Color.white);
		this.getContentPane().setLayout(borderLayout1);
		
		this.getContentPane().add(passwordPanel, java.awt.BorderLayout.CENTER);
		passwordPanel.setLayout(gridBagLayout1);
		continueButton.setFont(new java.awt.Font("Dialog", 1, 12));
		continueButton.setForeground(new Color(80, 80, 133));
		continueButton.setText("Continue");
		continueButton.addActionListener(this);
		passwordText.setBackground(Color.white);
		passwordText.setFont(new java.awt.Font("Dialog", 1, 12));
		passwordText.setForeground(new Color(80, 80, 133));
		jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
		jLabel5.setForeground(new Color(80, 80, 133));
		jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel5.setHorizontalTextPosition(SwingConstants.CENTER);
		jLabel5.setText("Authorizing User");
		cancelButton.setFont(new java.awt.Font("Dialog", 1, 12));
		cancelButton.setForeground(new Color(80, 80, 133));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(this);
		jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
		jLabel2.setForeground(new Color(80, 80, 133));
		jLabel2.setText("Enter Password:");
		jLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
		jLabel1.setForeground(new Color(80, 80, 133));
		jLabel1.setText("Enter Username:");
		passwordPanel.add(jLabel5, null);
		passwordPanel.add(jLabel5, new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(6, 2, 0, 4), 271, 13));
		passwordPanel.add(usernameText, new GridBagConstraints(2, 1, 3, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(24, 0, 0, 83), 186, 7));
		passwordPanel.add(passwordText, new GridBagConstraints(2, 2, 3, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(8, 0, 0, 83), 186, 9));
		passwordPanel.add(jLabel1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(25, 8, 0, 0), 20, 13));
		passwordPanel.add(jLabel2, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(10, 8, 0, 0), 20, 13));
		passwordPanel.add(cancelButton, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(24, 0, 24, 0), 9, 0));
		passwordPanel.add(continueButton,
				new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
						, GridBagConstraints.CENTER,
						GridBagConstraints.NONE,
						new Insets(24, 25, 24, 0), 5, 0));
		
		this.addEnterAction(this.passwordPanel);
		this.addEnterAction(this.passwordText);
		this.addEnterAction(this.usernameText);
		this.addEnterAction(this.continueButton);
		this.addEnterAction(this.cancelButton);

		pack();
		//this.setSize(370,200);
//		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
//		this.setLocation( (d.width - this.getSize().width) / 2,
//				(d.height - this.getSize().height) / 2);
	}
	
	public String getUsername() {
		return usernameText.getText();
	}
	
	public char[] getPassword() {
		return passwordText.getPassword();
	}
	
	public static void main(String args[]) {
		System.out.println("Start");
		UserAuthDialog box = new UserAuthDialog(null, true);
		System.out.println("Middle");
		box.setVisible(true);
		System.out.println("End");
		System.exit(0);
	}
	
	public void setVisible(boolean visible) {
		if (visible == true)
			canceled = false;
		super.setVisible(visible);
	}
	
	public void continueAction() {
		this.setVisible(false);
		canceled = false;
	}
	
	public void cancelAction() {
		canceled = true;
		if (exitOnCancel)
			System.exit(0);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == continueButton) {
			continueAction();
		} else if (e.getSource() == cancelButton) {
			cancelAction();
		}
	}

}
