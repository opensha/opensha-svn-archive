package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import java.awt.event.*;

/**
 * <p>Title: X_ValuesInCurveControlPanel</p>
 * <p>Description: Provides the user to input his own set of X-Values for the
 * HazardCurve</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class X_ValuesInCurveControlPanel extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JLabel xValuesLabel = new JLabel();
  private JScrollPane xValuesScrollPane = new JScrollPane();
  private JTextArea xValuesText = new JTextArea();
  private JButton xValuesButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();


  //function containing x,y values
  ArbitrarilyDiscretizedFunc function;

  public X_ValuesInCurveControlPanel(Component parent) {
    try {
      jbInit();
      // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.createFunction();
    this.setX_Values();
  }
  private void jbInit() throws Exception {
    xValuesButton.setBackground(new Color(200, 200, 230));
    xValuesButton.setForeground(new Color(80, 80, 133));
    xValuesButton.setText("Set Default");
    this.setTitle("X Values Control Panel");
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }
    });
    xValuesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        xValuesButton_actionPerformed(e);
      }
    });
    xValuesText.setBackground(new Color(200, 200, 230));
    xValuesText.setForeground(new Color(80, 80, 133));
    xValuesText.setLineWrap(false);
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    xValuesLabel.setBackground(new Color(200, 200, 230));
    xValuesLabel.setForeground(new Color(80, 80, 133));
    xValuesLabel.setText("Enter X-Values for Hazard Curve");
    jPanel1.add(xValuesLabel,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 53, 0, 14), 32, 12));
    jPanel1.add(xValuesScrollPane,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(12, 87, 12, 0), 0, 0));
    jPanel1.add(xValuesButton,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(303, 15, 19, 21), 3, 14));
    xValuesScrollPane.getViewport().add(xValuesText, null);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
  }

  /**
   * initialises the function with the x and y values
   * the y values are modified with the values entered by the user
   */
  private void createFunction(){
    function= new ArbitrarilyDiscretizedFunc();
    function.set(.001,1);
    function.set(.01,1);
    function.set(.05,1);
    function.set(.15,1);
    function.set(.1,1);
    function.set(.2,1);
    function.set(.25,1);
    function.set(.3,1);
    function.set(.4,1);
    function.set(.5,1);
    function.set(.6,1);
    function.set(.7,1);
    function.set(.8,1);
    function.set(.9,1);
    function.set(1.0,1);
    function.set(1.1,1);
    function.set(1.2,1);
    function.set(1.3,1);
    function.set(1.4,1);
    function.set(1.5,1);
  }

  /**
   * initialise the X values with the default X values in the textArea
   */
  private void setX_Values(){
    ListIterator lt=function.getXValuesIterator();
    String st =new String("");
    while(lt.hasNext())
      st += lt.next().toString().trim()+"\n";
    this.xValuesText.setText(st);
  }

  /**
   *
   * sets the  X values in ArbitrarilyDiscretizedFunc from the text area
   */
  private void getX_Values()
      throws NumberFormatException,RuntimeException{
    function = new ArbitrarilyDiscretizedFunc();
    String str = this.xValuesText.getText();

    StringTokenizer st = new StringTokenizer(str,"\n");
    while(st.hasMoreTokens()){
      double tempX_Val=0;
      double previousTempX_Val =tempX_Val;
      try{
        tempX_Val=(new Double(st.nextToken().trim())).doubleValue();
        if(tempX_Val < previousTempX_Val)
          throw new RuntimeException("X Values must be entered in increasing  order");
      }catch(NumberFormatException e){
        throw new NumberFormatException("X Values entered must be a valid number");
      }
      function.set(tempX_Val,1);
    }
  }

  /**
   * If the user wants to have the default set of X values
   * @param e
   */
  void xValuesButton_actionPerformed(ActionEvent e) {
    this.createFunction();
    this.setX_Values();
  }

  /**
   * Sets the ArbitrarilyDiscretizedFunc with the X values and provides all the
   * checks to see if the X values rae correctly entered
   * @param e
   */
  void  this_windowClosing(WindowEvent e) {
    System.out.println("Inside the window closing");
    int flag=0;
    try{
      //sets the X values in the ArbitrarilyDiscretizedFunc
      getX_Values();

      //if the user text area for the X values is empty
      if(xValuesText.getText().trim().equalsIgnoreCase("")){
        JOptionPane.showMessageDialog(this,"Must enter X values","Invalid Entry",
                                      JOptionPane.OK_OPTION);
        flag=1;
      }
    }catch(NumberFormatException ee){
      //if user has not entered a valid number in the textArea
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Invalid Entry",
                                    JOptionPane.OK_OPTION);
      flag=1;
    }catch(RuntimeException eee){
      //if the user has not entered the X values in increasing order
      JOptionPane.showMessageDialog(this,eee.getMessage(),"Invalid Entry",
                                    JOptionPane.OK_OPTION);
      flag=1;
    }
    if(flag!=0){
      //this.pack();
      this.show();
    }
  }


  /**
   *
   * @returns the ArbitrarilyDiscretizedFunction containing the X values that user entered
   */
  public ArbitrarilyDiscretizedFunc getX_ValuesFunctions(){
    return this.function;
  }

/*  void this_windowClosed(WindowEvent e) {
    //if the user text area for the X values is empty
    System.out.println("Function is null");
    if(function == null){
      JOptionPane.showMessageDialog(this,"Must enter X values","Invalid Entry",
                                    JOptionPane.OK_OPTION);
      return;
    }
    else
      super.dispose();
  }*/
}