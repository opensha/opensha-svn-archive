package org.opensha.commons.util.bugReports;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.opensha.commons.util.ApplicationVersion;

public class BugReportDialog extends JDialog implements ActionListener, HyperlinkListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BugReport bug;
	private static String message = "Oops...something went wrong!";
	
	private static Color topTextColor = new Color(0, 0, 180);
	private static Color topBottomColor = new Color(150, 150, 220);
	private static Color mainColor = Color.WHITE;
	
	private JButton quitButton = new JButton("Exit Application");
	private JButton continueButton = new JButton("Continue Using Application");
	private JButton submitBugButton = new JButton("<html><center><b><font size=+2>Submit Bug Report</font></b><br>" +
			"<font size=-1>(will open in web browser)</font></center></html>");
	private JButton technicalButton = new JButton("View Techical Details");
	private JTextField emailField = new JTextField("", 100);
	
	private boolean fatal;

	public BugReportDialog(Component parent, BugReport bug, boolean fatal) {
		if (bug == null) {
			bug = new BugReport();
		}
		this.fatal = fatal;
		this.bug = bug;
		init();
		setLocationRelativeTo(parent);
	}

	private class ImagePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		BufferedImage image;
		public ImagePanel(BufferedImage image) {
			this.image = image;
			this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		}

		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(image, 0, 0, null);
		}
	}

	private void init() {
		this.setTitle(message);

		this.setLayout(new BorderLayout());
		this.setSize(650, 500);
		this.setResizable(false);

		setModal(true);
		if (fatal)
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		else
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.setBackground(mainColor);
			
		this.add(getTopPanel(), BorderLayout.NORTH);
		
		this.add(getCenterPanel(), BorderLayout.CENTER);
		
		this.add(getBottomPanel(), BorderLayout.SOUTH);
	}
	
	private static JPanel wrapInPanel(Component comp, Color backgroundColor) {
		JPanel panel = new JPanel();
		panel.add(comp);
		if (backgroundColor != null)
			panel.setBackground(backgroundColor);
		return panel;
	}
	
	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JLabel messageLabel = new JLabel(message + "   ");
		messageLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		messageLabel.setForeground(topTextColor);
		messageLabel.setBackground(topBottomColor);
		try {
			BufferedImage cautionImage = ImageIO.read(
					this.getClass().getResource("/resources/images/icons/software_bug.png"));
			ImagePanel imagePanel = new ImagePanel(cautionImage);
			imagePanel.setBackground(topBottomColor);
			topPanel.add(imagePanel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		topPanel.add(messageLabel);
		topPanel.setBackground(topBottomColor);
		topPanel.setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));
		
		return topPanel;
	}
	
	public JPanel getCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setBackground(mainColor);
		
		String text = "Sorry for the inconvenience, but an error has occurred.";
		
		if (fatal)
			text += "<br><br>Unfortunately this error is fatal and the application will now exit.";
		else
			text += "<br><br><b>It may be possible to continue to use the application, but errors " +
				"may persist and the application may produce unexpected results.</b>";
		
		text += "<br><br>You can help to improve OpenSHA by submitting a bug report to our " +
				"<a href=\""+BugReport.TRAC_URL+"\">Trac Site</a>. Click the button below which " +
				"will launch your web browser, allowing you to submit the bug. Information on " +
				"the bug will automatically be included. To view that information, click " +
				"\"View Technical Details\". Note that this requires an internet connection.";
		
		JTextPane mainText = new JTextPane();
		mainText.setContentType("text/html");
		mainText.setText(text);
		mainText.setEditable(false);
		mainText.setPreferredSize(new Dimension(this.getWidth()-6, 100));
		mainText.setBackground(mainColor);
		mainText.addHyperlinkListener(this);
		
		centerPanel.add(mainText);
		
		JPanel bottomCenter = new JPanel();
		bottomCenter.setLayout(new BoxLayout(bottomCenter, BoxLayout.Y_AXIS));
		
		JLabel emailLabel = new JLabel("Your E-mail Address: ");
		JPanel emailPanel = new JPanel();
		emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.X_AXIS));
		emailPanel.add(emailLabel);
		emailPanel.add(emailField);
		emailPanel.setPreferredSize(new Dimension(300, 20));
		emailPanel.setBackground(mainColor);
		emailPanel.setMaximumSize(new Dimension(300, 20));
		
		bottomCenter.add(wrapInPanel(emailPanel, mainColor));
		
		bottomCenter.add(wrapInPanel(submitBugButton, mainColor));
		bottomCenter.setBackground(mainColor);
		submitBugButton.addActionListener(this);
		
		centerPanel.add(wrapInPanel(bottomCenter, mainColor));
		
		centerPanel.setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));
		
		return centerPanel;
	}
	
	public JPanel getBottomPanel() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(quitButton);
		Dimension spacerSize = new Dimension(3, 3);
		bottomPanel.add(new Box.Filler(spacerSize, spacerSize, spacerSize));
		quitButton.addActionListener(this);
		bottomPanel.add(continueButton);
		bottomPanel.add(new Box.Filler(spacerSize, spacerSize, spacerSize));
		continueButton.setEnabled(!fatal);
		continueButton.addActionListener(this);
		bottomPanel.add(technicalButton);
		technicalButton.setEnabled(bug.getDescription() != null);
		technicalButton.addActionListener(this);
		bottomPanel.setBackground(topBottomColor);
		bottomPanel.setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));
		
		return wrapInPanel(bottomPanel, topBottomColor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == quitButton) {
			System.exit(1);
		} else if (e.getSource() == continueButton) {
			this.setVisible(false);
			this.dispose();
		} else if (e.getSource() == submitBugButton) {
			String email = emailField.getText();
			if (email.length() > 2)
				bug.setReporter(email);
			URL url = null;
			try {
				url = bug.buildTracURL();
			} catch (MalformedURLException e2) {
				e2.printStackTrace();
				String text = "We couldn't automatically generate a bug report. Please manually submit " +
						"one at " + BugReport.TRAC_NEW_TICKET_URL;
				JOptionPane.showMessageDialog(this, text, "Could geneate bug report", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				Desktop.getDesktop().browse(url.toURI());
			} catch (Exception e1) {
				String text = "Java couldn't open the bug report URL in your web browser. " +
						"Please copy/paste this entire link manually into your web browser to " +
						"submit the bug. Thanks!\n\n"+url.toString();
				JTextArea ta = new JTextArea(10, 50);
				ta.setText(text);
				ta.setLineWrap(true);
				ta.setEditable(false);
				ta.setWrapStyleWord(false);
				ta.setPreferredSize(new Dimension(300, 200));
				JScrollPane scroll = new JScrollPane(ta);
				scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				JOptionPane.showMessageDialog(this, scroll,
						"Could not launch browser!", JOptionPane.ERROR_MESSAGE);
			}
			
		} else if (e.getSource() == technicalButton) {
			JTextArea ta = new JTextArea(20, 50);
			ta.setEditable(false);
			ta.setText(bug.getDescription());
			JScrollPane scroll = new JScrollPane(ta);
			JOptionPane.showMessageDialog(this, scroll, "Technical Information", JOptionPane.PLAIN_MESSAGE);
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == EventType.ACTIVATED) {
			try {
				Desktop.getDesktop().browse(e.getURL().toURI());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) throws IOException {
		BugReport bug = new BugReport(new RuntimeException("this is my exceptoin!"),
				"Metadata is here\nmore stuff\nand done", "BugReportDialog",
				ApplicationVersion.loadBuildVersion(), null);
		
		BugReportDialog dialog = new BugReportDialog(null, bug, true);
		dialog.setVisible(true);
	}

}