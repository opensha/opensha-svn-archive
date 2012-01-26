/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with the Southern California
 * Earthquake Center (SCEC, http://www.scec.org) at the University of Southern
 * California and the UnitedStates Geological Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.opensha.sha.gui.infoTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;

/**
 * General utility progress bar dialog.
 * 
 * This class used to serve the dual purpose of alert and progress dialog, with
 * the progress bar replacing the alert message if required. These two components
 * have now been separated; as such, there are instances of the dialogs use
 * where the progress bar is never used (e.g. when the ERF_GuiBean "Updates
 * Forecast...").

 * @author Nitin Gupta
 * @author Vipin Gupta
 * @version $Id$
 */

public class CalcProgressBar extends JDialog {

	private static final int DIALOG_WIDTH = 320;
	private static final int B = 20; // border width
	private static Font font;
	private static Font boldFont;

	private JProgressBar progress;
	private JLabel info;
	private JLabel alert;

	static {
		Font baseFont = UIManager.getFont("Label.font");
		font = baseFont.deriveFont(baseFont.getSize2D() - 2f);
		boldFont = font.deriveFont(Font.BOLD);
	}

	/**
	 * Creates a new progress dialog. Maintains existing implementations but
	 * constructor that takes an owner should be used.
	 * @param title
	 * @param message
	 */
	public CalcProgressBar(String title, String message) {
		this(null, title, message);
	}

	/**
	 * Creates a new progress dialog. The dialog is positioned relative to the
	 * <code>Window</code> containing its <code>owner</code>. If
	 * <code>owner</code> is null, the dialog will be centered on the screen.
	 * @param owner
	 * @param title
	 * @param info
	 */
	public CalcProgressBar(Component owner, String title, String info) {
		super(JOptionPane.getFrameForComponent(owner), title);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder(B - 4, B, 4, B));
		content.add(buildContent(info), BorderLayout.CENTER);
		getContentPane().add(content);
		pack();
		setLocationRelativeTo(JOptionPane.getFrameForComponent(owner));
		setVisible(true);
	}

	private JPanel buildContent(String s) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		s = StringUtils.isEmpty(s) ? " " : s;
		info = new JLabel(s);
		info.setFont(boldFont);
		info.setOpaque(false);
		info.setAlignmentX(0f);
		panel.add(info);

		panel.add(Box.createVerticalStrut(10));

		progress = new JProgressBar(0, 100);
		progress.setValue(0);
		progress.putClientProperty("JComponent.sizeVariant", "mini");
		progress.setAlignmentX(0f);
		panel.add(progress);

		panel.add(Box.createVerticalStrut(2));

		alert = new JLabel(" ");
		alert.setFont(font);
		panel.add(alert);

		JComponent c = (JComponent) Box.createHorizontalStrut(DIALOG_WIDTH);
		c.setAlignmentX(0f);
		panel.add(c);

		panel.add(Box.createVerticalGlue());
		return panel;
	}

	/**
	 * Updates the info message.
	 * @param s info <code>String</code>
	 */
	public void setProgressMessage(String s) {
		info.setText(StringUtils.isEmpty(s) ? " " : s);
		info.paintImmediately(info.getBounds());
	}

	/**
	 * Does nothing. This is an unecessary message as an info message is always
	 * displayed above the progress bar; one does not replace the other.
	 */
	@Deprecated
	public void displayProgressBar() {}

	/**
	 * Updates the dialog progress and info message using the supplied
	 * <code>count</code> and <code>total</code> values.
	 * @param count
	 * @param total
	 */
	public void updateProgress(int count, int total) {
		if (total != 0) {
			int value = (count * 100) / total;
			StringBuilder sb = new StringBuilder();
			sb.append(count).append(" of ").append(total).append(" Complete");
			info.setText(sb.toString());
			progress.setValue(value);
		}
	}

	/**
	 * Shows or hides the dialog.
	 * @param show
	 */
	public void showProgress(boolean show) {
		setVisible(show);
		if (!show) dispose();
	}

	/**
	 * Sets the alert message below progress bar.
	 * @param s alert <code>String</code>
	 */
	public void alert(String s) {
		alert.setText(StringUtils.isEmpty(s) ? " " : s);
		alert.paintImmediately(alert.getBounds());
	}

	public static void main(String[] args) {
		CalcProgressBar dialog = new CalcProgressBar(null,
			"Calculation Monitor",
			"Please wait while ruptures are being updated\u2026");
		// CalcProgressBar dialog = new CalcProgressBar(null,
		// "Calculation Monitor", "Starting\u2026");
		dialog.showProgress(true);
	}
}
