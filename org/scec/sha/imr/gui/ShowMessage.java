package org.scec.sha.imr.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: ShowMessage</p>
 * <p>Description: This class handles all  the messages that the GUI will throwing for any kind of runtime exception for Log Plots</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin & Vipin
 * @version 1.0
 */

public class ShowMessage extends JFrame {
  private JPanel jMessagePanel = new JPanel();
  private JLabel jMessageLabel = new JLabel();
  private JButton jMessageButton = new JButton();
  private IMRTesterApplet imrTesterApplet;
  private String infoMessage;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  public ShowMessage(IMRTesterApplet imr,String s) {
    imrTesterApplet=imr;
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
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);
    jMessageLabel.setBackground(new Color(200, 200, 230));
    jMessageLabel.setFont(new java.awt.Font("Dialog", 1, 13));
    jMessageLabel.setForeground(new Color(80, 80, 133));
    jMessageLabel.setHorizontalAlignment(SwingConstants.LEFT);
    jMessageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    this.getContentPane().add(jMessagePanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 80, 18));
    jMessagePanel.add(jMessageButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(40, 166, 29, 198), 33, 13));
    jMessagePanel.add(jMessageLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 12, 0, 13), 425, 41));
    this.jMessageLabel.setText(this.infoMessage);
  }

  void jMessageButton_actionPerformed(ActionEvent e) {
   this.dispose();
   imrTesterApplet.addGraphPanel();
  }



}