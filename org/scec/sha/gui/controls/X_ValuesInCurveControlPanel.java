package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.sha.gui.infoTools.DefaultHazardCurveForIMTs;


/**
 * <p>Title: X_ValuesInCurveControlPanel</p>
 * <p>Description: Provides the user to input his own set of X-Values for the
 * HazardCurve</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class X_ValuesInCurveControlPanel extends JFrame {

  //static Strings for the Different X Vlaues that the user can choose from.
  private final static String PEER_X_VALUES = "PEER Test-Case Values";
  private final static String CUSTOM_VALUES = "Custom Values";
  private final static String DEFAULT = "DEFAULT";
  private final static String MIN_MAX_NUM = "Enter Min, Max and Num";


  private JPanel jPanel1 = new JPanel();
  private JLabel xValuesLabel = new JLabel();
  private JScrollPane xValuesScrollPane = new JScrollPane();
  private JTextArea xValuesText = new JTextArea();

  //function containing x,y values
  ArbitrarilyDiscretizedFunc function;
  private JButton doneButton = new JButton();
  private JComboBox xValuesSelectionCombo = new JComboBox();
  private JLabel minLabel = new JLabel();
  private JLabel maxLabel = new JLabel();
  private JLabel numLabel = new JLabel();
  private JTextField minText = new JTextField();
  private JTextField maxText = new JTextField();
  private JTextField numText = new JTextField();
  private JButton setButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();


  private DecimalFormat format = new DecimalFormat("0.000000##");

  //Stores the imt selected by the user
  private String imt;

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
    format.setMaximumFractionDigits(6);
    //initialise the function with the PEER values
    generateXValues();
  }
  private void jbInit() throws Exception {
    xValuesLabel.setHorizontalAlignment(SwingConstants.CENTER);
    xValuesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    doneButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doneButton_actionPerformed(e);
      }
    });

    //jPanel1.setPreferredSize(new Dimension(300, 500));
    minLabel.setForeground(new Color(80, 80, 133));
    minLabel.setText("Min :");
    maxLabel.setForeground(new Color(80, 80, 133));
    maxLabel.setText("Max :");
    numLabel.setForeground(new Color(80, 80, 133));
    numLabel.setText("Num :");
    setButton.setText("Set Values");
    setButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setButton_actionPerformed(e);
      }
    });
    xValuesSelectionCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        xValuesSelectionCombo_actionPerformed(e);
      }
    });
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setMinimumSize(new Dimension(300, 350));
    jPanel1.setPreferredSize(new Dimension(340, 430));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.setLayout(gridBagLayout1);
    jPanel1.add(xValuesLabel,  new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 15, 0, 68), 7, 0));
    jPanel1.add(xValuesScrollPane,  new GridBagConstraints(0, 2, 1, 5, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(17, 28, 15, 0), 87, 400));
    jPanel1.add(xValuesSelectionCombo,  new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(9, 15, 0, 74), 94, 0));
    xValuesScrollPane.getViewport().add(xValuesText, null);

    jPanel1.add(numLabel,  new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(9, 35, 0, 0), 10, 9));
    jPanel1.add(minText,  new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(54, 0, 0, 26), 84, 9));
    jPanel1.add(maxText,  new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 0, 26), 84, 9));
    jPanel1.add(numText,  new GridBagConstraints(2, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 0, 26), 84, 9));
    jPanel1.add(doneButton,     new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(100, 30, 20, 68), 12, 20));
    jPanel1.add(maxLabel,  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 35, 0, 0), 13, 9));
    jPanel1.add(setButton,  new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(23, 70, 0, 26), 2, 4));
    jPanel1.add(minLabel,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(56, 35, 0, 0), 13, 9));

    this.setTitle("X Values Control Panel");
    xValuesText.setBackground(new Color(200, 200, 230));
    xValuesText.setForeground(new Color(80, 80, 133));
    xValuesText.setLineWrap(false);
    xValuesLabel.setBackground(new Color(200, 200, 230));
    xValuesLabel.setForeground(new Color(80, 80, 133));
    xValuesLabel.setText("X-axis (IML) Values for Hazard Curves");
    doneButton.setForeground(new Color(80, 80, 133));
    doneButton.setText("Done");
    this.setSize(new Dimension(400, 400));
    //adding the variuos choices to the Combo Selection for the X Values
    xValuesSelectionCombo.addItem(PEER_X_VALUES);
    xValuesSelectionCombo.addItem(CUSTOM_VALUES);
    xValuesSelectionCombo.addItem(DEFAULT);
    xValuesSelectionCombo.addItem(MIN_MAX_NUM);
    xValuesSelectionCombo.setSelectedItem(PEER_X_VALUES);
    maxLabel.setVisible(false);
    minLabel.setVisible(false);
    numLabel.setVisible(false);
    maxText.setVisible(false);
    minText.setVisible(false);
    numText.setVisible(false);
    setButton.setVisible(false);
  }

  /**
   * initialises the function with the x and y values if the user has chosen the PEER X Vals
   * the y values are modified with the values entered by the user
   */
  private void createPEER_Function(){
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
   * initialises the function with the x and y values if the user has chosen the Min Max Num Vals.
   * The user enters the min, max and num and using that we create the ArbitrarilyDiscretizedFunc
   * using the log space.
   */
  private void createFunctionFromMinMaxNum(){
    function  = new ArbitrarilyDiscretizedFunc();
    try{
      //get the min,  max and num values enter by the user.
      int numIMT_Vals = Integer.parseInt(numText.getText().trim());
      double minIMT_Val = Double.parseDouble(minText.getText().trim());
      double maxIMT_Val = Double.parseDouble(maxText.getText().trim());
      if(minIMT_Val >= maxIMT_Val){
        JOptionPane.showMessageDialog(this,"Min Val should be less than Max Val",
                                      "Incorrect Input",JOptionPane.ERROR_MESSAGE);
        return;
      }
      double discretizationIMT = (Math.log(maxIMT_Val) - Math.log(minIMT_Val))/(numIMT_Vals-1);
      for(int i=0; i < numIMT_Vals ;++i){
        double xVal =Double.parseDouble(format.format(Math.exp(Math.log(minIMT_Val)+i*discretizationIMT)));
        function.set(xVal,1.0);
      }


    }catch(NumberFormatException e){
      JOptionPane.showMessageDialog(this,"Must enter a Valid Number",
                                      "Incorrect Input",JOptionPane.ERROR_MESSAGE);
      return;

    }catch(NullPointerException e){
      JOptionPane.showMessageDialog(this,"Null not allowed, must enter a valid number",
                                "Incorrect Input",JOptionPane.ERROR_MESSAGE);
      return;
    }

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



  private void closeWindow(){

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
    //if there is no exception occured and user properly entered the X values
    if(flag==0)
      this.dispose();
  }

  /**
   *
   * @returns the ArbitrarilyDiscretizedFunction containing the X values that user entered
   */
  public ArbitrarilyDiscretizedFunc getX_ValuesFunctions(){
    return this.function;
  }

  /**
   * Closes the window when the user is done with entering the x values.
   * @param e
   */
  void doneButton_actionPerformed(ActionEvent e) {
    closeWindow();
  }

  //sets the imt selected by the user in the application
  public  void setIMT(String imt){
    this.imt = imt;
    generateXValues();
    repaint();
    validate();
  }

  //making the GUI visible or invisible based on the selection of "Type of X-Values"
  void xValuesSelectionCombo_actionPerformed(ActionEvent e) {
    String selectedItem = (String)xValuesSelectionCombo.getSelectedItem();
    if(selectedItem.equals(this.PEER_X_VALUES) || selectedItem.equals(this.CUSTOM_VALUES)){
      maxLabel.setVisible(false);
      minLabel.setVisible(false);
      numLabel.setVisible(false);
      maxText.setVisible(false);
      minText.setVisible(false);
      numText.setVisible(false);
      setButton.setVisible(false);
      xValuesText.setEditable(false);
      if(selectedItem.equals(this.CUSTOM_VALUES))
        xValuesText.setEditable(true);
    }
    else if(selectedItem.equals(this.DEFAULT) || selectedItem.equals(this.MIN_MAX_NUM)){
      maxLabel.setVisible(true);
      minLabel.setVisible(true);
      numLabel.setVisible(true);
      maxText.setVisible(true);
      minText.setVisible(true);
      numText.setVisible(true);
      setButton.setVisible(true);
      this.xValuesText.setEditable(false);
      if(selectedItem.equals(this.DEFAULT)){
        setButton.setVisible(false);
        minText.setEditable(false);
        maxText.setEditable(false);
        numText.setEditable(false);
      }
      if(selectedItem.equals(this.MIN_MAX_NUM)){
        minText.setEditable(true);
        maxText.setEditable(true);
        numText.setEditable(true);
      }
    }
    generateXValues();
  }

  /**
   * This function initialises the ArbitrarilyDiscretizedFunction with the X Values
   * and Y Values based on the selection made by the user to choose the X Values.
   */
  private void generateXValues(){
    String selectedItem = (String)xValuesSelectionCombo.getSelectedItem();
    if(selectedItem.equals(this.PEER_X_VALUES)){
      this.createPEER_Function();
      setX_Values();
    }
    else if(selectedItem.equals(this.DEFAULT)){
      minText.setText(""+DefaultHazardCurveForIMTs.getMinIMT_Val(imt));
      maxText.setText(""+DefaultHazardCurveForIMTs.getMaxIMT_Val(imt));
      numText.setText(""+DefaultHazardCurveForIMTs.getNumIMT_Val(imt));
      DefaultHazardCurveForIMTs defaultX_Vals = new DefaultHazardCurveForIMTs();
      function = defaultX_Vals.getHazardCurve(imt);
      setX_Values();
    }
    else if(selectedItem.equals(this.MIN_MAX_NUM)){
      minText.setText("");
      maxText.setText("");
      numText.setText("");
      xValuesText.setText("");
    }
    else if(selectedItem.equals(this.CUSTOM_VALUES)){
      xValuesText.setText("");
    }
  }

  void setButton_actionPerformed(ActionEvent e) {
    createFunctionFromMinMaxNum();
    setX_Values();
  }

}