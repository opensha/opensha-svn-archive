package org.scec.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: ShowMessage</p>
 * <p>Description: This class handles all  the messages
 * that the GUI will throwing for any kind of runtime exception
 * for Log Plots</p>
 *
 * @author : Nitin Gupta Date :Aug,7,2002
 * @version 1.0
 */

public class ShowMessage extends JFrame {
  private JPanel jMessagePanel = new JPanel();
  private JLabel jMessageLabel = new JLabel();
  private JButton jMessageButton = new JButton();
  private String infoMessage;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  public ShowMessage(String s) {
    this.infoMessage=s;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    jMessagePanel.setLayout(gridBagLayout1);
    this.setResizable(false);
    this.setTitle("Log-Plot Information");
    this.getContentPane().setLayout(gridBagLayout2);
    jMessageButton.setBackground(new Color(200, 200, 230));
    jMessageButton.setForeground(new Color(80, 80, 133));
    jMessageButton.setText("OK");
    jMessageButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMessageButton_actionPerformed(e);
      }
    });
    jMessagePanel.setBackground(new Color(200, 200, 230));
    jMessagePanel.setForeground(new Color(80, 18, 133));
    jMessagePanel.setMaximumSize(new Dimension(370, 145));
    jMessagePanel.setMinimumSize(new Dimension(370, 145));
    jMessagePanel.setPreferredSize(new Dimension(370, 145));
    this.getContentPane().setBackground(new Color(200, 200, 230));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);
    jMessageLabel.setBackground(new Color(200, 200, 230));
    jMessageLabel.setFont(new java.awt.Font("Dialog", 1, 13));
    jMessageLabel.setForeground(new Color(80, 80, 133));
    jMessageLabel.setMaximumSize(new Dimension(350, 40));
    jMessageLabel.setMinimumSize(new Dimension(350, 40));
    jMessageLabel.setPreferredSize(new Dimension(350, 40));
    jMessageLabel.setHorizontalAlignment(SwingConstants.LEFT);
    jMessageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    this.getContentPane().add(jMessagePanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 11, 12), 0, 0));
    jMessagePanel.add(jMessageLabel,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 21, 0, 10), 0, 0));
    jMessagePanel.add(jMessageButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(11, 143, 12, 140), 38, 5));
    this.jMessageLabel.setText(this.infoMessage);
  }

  void jMessageButton_actionPerformed(ActionEvent e) {
   this.dispose();
  }
}
