package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import java.util.StringTokenizer;

/**
 * <p>Title: XY_ValuesControlPanel</p>
 *
 * <p>Description: This class allows user to enter X and Y values.
 * Each line represents one XY value and each XY value should be space seperated.</p>
 *
 * @author :Nitin Gupta
 * @version 1.0
 */
public class XY_ValuesControlPanel
    extends JFrame {

  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JScrollPane xyDatasetScrollPane = new JScrollPane();
  JLabel jLabel1 = new JLabel();
  JTextArea xyDatasetText = new JTextArea();
  JTextArea metadataText = new JTextArea();
  JLabel jLabel2 = new JLabel();
  JScrollPane metadataScrollPane = new JScrollPane();
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();
  //instance of the aplication using this control panel.
  XY_ValuesControlPanelAPI application;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public XY_ValuesControlPanel(Component parent,XY_ValuesControlPanelAPI api) {
    application = api;
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY());

  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    cancelButton.addActionListener(new
        XY_ValuesControlPanel_cancelButton_actionAdapter(this));
    okButton.addActionListener(new XY_ValuesControlPanel_okButton_actionAdapter(this));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
    jLabel1.setFont(new java.awt.Font("Arial", Font.BOLD, 16));
    jLabel1.setText("Enter XY Dataset:");
    jLabel2.setFont(new java.awt.Font("Arial", Font.BOLD, 16));
    jLabel2.setText("Enter Metadata:");
    okButton.setText("OK");
    cancelButton.setText("Cancel");
    xyDatasetScrollPane.getViewport().add(xyDatasetText);
    metadataScrollPane.getViewport().add(metadataText);
    jPanel1.setLayout(gridBagLayout1);
    jPanel1.add(xyDatasetScrollPane,  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 38, 0, 105), 185, 331));
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(28, 38, 0, 107), 66, 16));
    jPanel1.add(jLabel2,  new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 38, 0, 120), 68, 19));
    jPanel1.add(metadataScrollPane,  new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 38, 0, 20), 270, 136));
    jPanel1.add(cancelButton,  new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 34, 18, 31), 22, -1));
    jPanel1.add(okButton,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 64, 18, 0), 19, 1));
    this.setSize(new Dimension(338, 695));
    this.setTitle("New Dataset Control Panel");
  }

  public void cancelButton_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
  }

  public void okButton_actionPerformed(ActionEvent actionEvent) {
    application.setArbitraryDiscretizedFuncInList(getX_Values());
    this.dispose();
  }


  /**
   *
   * sets the  XY dataset values in ArbitrarilyDiscretizedFunc from the text area
   */
  private ArbitrarilyDiscretizedFunc getX_Values()
      throws NumberFormatException,RuntimeException{
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    String str = xyDatasetText.getText();
    StringTokenizer st = new StringTokenizer(str,"\n");
    while(st.hasMoreTokens()){
      StringTokenizer st1 = new StringTokenizer(st.nextToken());
      double tempX_Val=0;
      double tempY_Val=0;
      try{
        tempX_Val = Double.parseDouble(st1.nextToken());
        tempY_Val = Double.parseDouble(st1.nextToken());
      }catch(NumberFormatException e){
        throw new NumberFormatException("X Values entered must be a valid number");
      }
      function.set(tempX_Val,tempY_Val);
    }

    function.setInfo(metadataText.getText());
    return function;
  }
}

class XY_ValuesControlPanel_okButton_actionAdapter
    implements ActionListener {
  private XY_ValuesControlPanel adaptee;
  XY_ValuesControlPanel_okButton_actionAdapter(XY_ValuesControlPanel adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    adaptee.okButton_actionPerformed(actionEvent);
  }
}

class XY_ValuesControlPanel_cancelButton_actionAdapter
    implements ActionListener {
  private XY_ValuesControlPanel adaptee;
  XY_ValuesControlPanel_cancelButton_actionAdapter(XY_ValuesControlPanel
      adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    adaptee.cancelButton_actionPerformed(actionEvent);
  }
}
