package org.scec.sha.gui.infoTools;

import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * <p>Title: CalcProgressBar</p>
 * <p>Description:Shows the Progrss Bar for the calculations</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @created: March 10, 2003
 * @version 1.0
 */

public class CalcProgressBar extends JProgressBar {

  private JFrame frame;
  private JLabel label;
  // frame height and width
  private int FRAME_WIDTH = 250;
  private int FRAME_HEIGHT = 60;

  // start x and y for frame
  private int FRAME_STARTX = 400;
  private int FRAME_STARTY = 200;

  private String frameMessage=  new String();
  private String labelMsg=  new String();
  /**
   * class constructor
   */
  public CalcProgressBar(String frameMsg,String labelMsg){
    super(0,100);

    //progress frame title
    frameMessage=frameMsg;
    this.labelMsg=labelMsg;
     // initiliaze the progress bar frame in which to show progress bar
    initProgressFrame();
  }

  /**
   * initialize the progress bar
   * Display "Updating forecast" initially
   */
  public void initProgressFrame() {
    // make the progress bar
    frame = new JFrame(frameMessage);
    frame.setLocation(this.FRAME_STARTX, this.FRAME_STARTY);
    frame.setSize(this.FRAME_WIDTH, this.FRAME_HEIGHT);
    this.setStringPainted(true); // display the percentage completed also
    this.setSize(FRAME_WIDTH-10, FRAME_HEIGHT-10);
    label = new JLabel(labelMsg);
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 2, 1, 2), 110, 10));
    frame.show();
    label.paintImmediately(label.getBounds());
  }

  /**
   * remove the "Updating forecast Label" and display the progress bar
   */
  public void displayProgressBar() {
    // now add the  progress bar
    label.setVisible(false);
    frame.getContentPane().remove(label);
    frame.getContentPane().add(this, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 2, 1, 2), 110, 10));
    frame.getContentPane().remove(label);
    frame.getContentPane().validate();
    frame.getContentPane().repaint();
  }

  /**
   * update the progress bar with this new value and string
   *
   * @param val : Value of progress bar
   * @param str  : string to be displayed in progress bar
   */
  public void updateProgressBar(int val, String str) {
    this.setString(str);
    this.setValue(val);
    Rectangle rect = this.getBounds();
    this.paintImmediately(rect);
  }


  /**
   * update the calculation progress
   * @param num:    the current number
   * @param totNum: the total number
   */
  public void updateProgress(int num, int totNum) {

    int val=0;
    boolean update = false;

    // find if we're at a point to update
    if(num == (int) (totNum*0.9)) { // 90% complete
      val = 90;
      update = true;
    } else if(num == (int) (totNum*0.8)) { // 80% complete
      val = 80;
      update = true;
    } else if(num == (int) (totNum*0.7)) { // 70% complete
      val = 70;
      update = true;
    } else if(num == (int) (totNum*0.6)) { // 60% complete
      val = 60;
      update = true;
    } else if(num == (int) (totNum*0.5)) { // 50% complete
      val = 50;
      update = true;
    } else if(num == (int) (totNum*0.4)) { // 40% complete
      val = 40;
      update = true;
    } else if(num == (int) (totNum*0.3)) { // 30% complete
      val = 30;
      update = true;
    } else if(num == (int) (totNum*0.2)) { // 20% complete
      val = 20;
      update = true;
    } else if(num == (int) (totNum*0.1)) { // 10% complete
      val = 10;
      update = true;
    }
    // update the progress bar
    if(update == true)
      updateProgressBar(val,Integer.toString((int) (totNum*val/100)) + "  of  " + Integer.toString(totNum) + "  Done");
  }

  public void showProgress(boolean flag){
    this.setVisible(flag);
    if(flag==false)
      this.frame.dispose();
  }


  /**
   * dispose the frame
   */
  public void dispose() {
    frame.dispose();
  }
}