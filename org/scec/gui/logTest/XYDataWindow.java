package org.scec.gui.logTest;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.jfree.data.*;
import java.awt.event.*;


/**
 * <p>Title:XYDataWindow </p>
 * <p>Description: This class allows the user to enter the XY data</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class XYDataWindow extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JScrollPane dataScroll = new JScrollPane();
  private JTextArea xyText = new JTextArea();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  //stores the XY DataSet
  private XYSeriesCollection dataset =null;

  public XYDataWindow() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 16));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setText("Enter XY DataSet");
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }
    });
    jPanel1.setPreferredSize(new Dimension(250, 650));
    dataScroll.setPreferredSize(new Dimension(250, 650));
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(dataScroll,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 8, 36), 0, 0));
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(36, 20, 0, 52), 0, 0));
    xyText.setBackground(new Color(200, 200, 230));
    xyText.setForeground(new Color(80, 80, 133));
    xyText.setLineWrap(false);
    dataScroll.getViewport().add(xyText, null);
    this.setSize(250,650);
    setTitle("XY Data Entry Window");
    this.validate();
    this.repaint();
  }


  /**
   *
   * @returns the XY Data set that user entered.
   */
  private void getDataSet() throws NumberFormatException, NullPointerException{
    dataset = new XYSeriesCollection();
    XYSeries series = new XYSeries("Random Data");
    try{
      String data = xyText.getText();
      StringTokenizer st = new StringTokenizer(data,"\n");
      while(st.hasMoreTokens()){
        StringTokenizer st1 = new StringTokenizer(st.nextToken());
        series.add(new Double(st1.nextToken()).doubleValue(),new Double(st1.nextToken()).doubleValue());
        dataset.addSeries(series);
      }
    }catch(NumberFormatException e){
      throw new RuntimeException("Must enter valid number");
    }catch(NullPointerException ee){
      throw new RuntimeException("Must enter data in X Y format");
    }

    //return (XYDataset)this.dataset;
  }

  public XYDataset getXYDataSet() {
    return (XYDataset)dataset;
  }

  void this_windowClosing(WindowEvent e) {

    int flag=0;
    try{

      //if the user text area for the X values is empty
      if(xyText.getText().trim().equalsIgnoreCase("")){
        JOptionPane.showMessageDialog(this,"Must enter X values","Invalid Entry",
                                      JOptionPane.OK_OPTION);
        flag=1;
      }
      //sets the X values in the ArbitrarilyDiscretizedFunc
      getDataSet();
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
    else
      return;

  }

}