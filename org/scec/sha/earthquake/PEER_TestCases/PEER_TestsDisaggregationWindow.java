package org.scec.sha.earthquake.PEER_TestCases;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

/**
 * <p>Title: PEER_TestsDisaggregationWindow</p>
 * <p>Description: This class shows the Disaggregation Result in a seperate window</p>
 * @author : Nitin Gupta & Vipin Gupta
 * Date :Feb,19,2003
 * @version 1.0
 */

class PEER_TestsDisaggregationWindow extends JFrame {
  private JPanel jMessagePanel = new JPanel();
  private JButton jMessageButton = new JButton();
  private String infoMessage;
  private JTextPane jMessagePane = new JTextPane();
  private SimpleAttributeSet setMessage;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  public PEER_TestsDisaggregationWindow(String s) {
    this.infoMessage=s;
    try {
      jbInit();
      jMessagePane.setEditable(false);
      Document doc = jMessagePane.getStyledDocument();
      doc.remove(0,doc.getLength());
      setMessage =new SimpleAttributeSet();
      StyleConstants.setFontSize(setMessage,13);
      StyleConstants.setForeground(setMessage,Color.black);
      doc.insertString(doc.getLength(),infoMessage,setMessage);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    jMessagePanel.setLayout(gridBagLayout1);
    this.setTitle("Disaggregation Result Window");
    this.getContentPane().setLayout(borderLayout1);
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
    this.setResizable(false);
    this.getContentPane().setBackground(new Color(200, 200, 230));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    jMessagePane.setBorder(null);
    jMessagePane.setToolTipText("");
    jMessagePane.setEditable(false);
    jMessagePanel.add(jMessageButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 83, 3, 79), 9, 12));
    jMessagePanel.add(jMessagePane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 1, 0, 2), 251, 223));
    this.getContentPane().add(jMessagePanel, BorderLayout.CENTER);
  }

  void jMessageButton_actionPerformed(ActionEvent e) {
   this.dispose();
  }
}
