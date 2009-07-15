package org.opensha.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: RunAll_PEER_TestCasesControlPanel</p>
 * <p>Description: This class runs all the PEER tst cases and output the results in a file</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class RunAll_PEER_TestCasesControlPanel extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JCheckBox runPEERcheck = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  public RunAll_PEER_TestCasesControlPanel(Component parent) {
    try {
      jbInit();
      // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY()+parent.getHeight()/2);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    runPEERcheck.setText("Click  to run PEER Test Cases (this will take a long time!)");
    this.setTitle("Run All PEER Test Cases Control Panel");
    jPanel1.setPreferredSize(new Dimension(350,70));
    this.setSize(350,70);
    this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(runPEERcheck, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(76, 65, 95, 96), 36, 14));

  }


  /**
   *
   * @returns true if we have to run all the PEER test cases
   */
  public boolean runAllPEER_TestCases(){
    if(this.runPEERcheck.isSelected())
      return true;
    else
      return false;
  }

}
