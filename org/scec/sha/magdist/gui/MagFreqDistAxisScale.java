package org.scec.sha.magdist.gui;



import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * <p>Title: MagFreDistAxisScale</p>
 * <p>Description:  This Class pop up window when custom scale is selecetd for the combo box that enables the
 * user to customise the X and Y Axis scale</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta   Date: Aug,21,2002
 * @version 1.0
 */

public class MagFreqDistAxisScale extends JFrame{

  private MagFreqDistTesterApplet magFreqDistTesterApplet;
  private JPanel jPanel1 = new JPanel();
  private Border border1;
  private TitledBorder titledBorder1;
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JTextField jTextIncrMinX = new JTextField();
  private JTextField jTextIncrMinY = new JTextField();
  private JLabel jLabel3 = new JLabel();
  private JTextField jTextIncrMaxX = new JTextField();
  private JTextField jTextIncrMaxY = new JTextField();
  private JLabel jLabel4 = new JLabel();
  private JPanel jPanel2 = new JPanel();
  private JTextField jTextCumMaxX = new JTextField();
  private JTextField jTextCumMaxY = new JTextField();
  private JLabel jLabel5 = new JLabel();
  private JTextField jTextCumMinX = new JTextField();
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel7 = new JLabel();
  private JTextField jTextCumMinY = new JTextField();
  private JLabel jLabel8 = new JLabel();
  private JTextField jTextMoMaxY = new JTextField();
  private JLabel jLabel9 = new JLabel();
  private JTextField jTextMoMaxX = new JTextField();
  private JLabel jLabel10 = new JLabel();
  private JLabel jLabel11 = new JLabel();
  private JLabel jLabel12 = new JLabel();
  private JTextField jTextMoMinY = new JTextField();
  private JTextField jTextMoMinX = new JTextField();
  private JPanel jPanel3 = new JPanel();
  private JButton jButtonOk = new JButton();
  private JButton jButtonCancel = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();


  public MagFreqDistAxisScale(MagFreqDistTesterApplet magFreqDist) {
    this.magFreqDistTesterApplet= magFreqDist;
    try{
      jbInit();
    }catch(Exception e){
      System.out.println("Error Occured while running range combo box: "+e);
    }
  }
  void jbInit() throws Exception {
    jPanel2.setLayout(gridBagLayout2);
    jPanel2.setBackground(new Color(200, 200, 230));
    jPanel2.setBorder(titledBorder1);

    jLabel5.setText("Max Y:");
    jLabel6.setText("Max X:");
    jLabel7.setText("Min Y:");
    jLabel8.setText("Min X:");

    jLabel9.setText("Min X:");
    jLabel10.setText("Min Y:");
    jLabel11.setText("Max X:");
    jLabel12.setText("Max Y:");
    jPanel3.setBackground(new Color(200, 200, 230));
    jPanel3.setBorder(titledBorder1);
    jPanel3.setLayout(gridBagLayout1);
    jButtonOk.setBackground(new Color(200, 200, 230));
    jButtonOk.setForeground(new Color(80, 80, 133));
    jButtonOk.setText("OK");
    jButtonCancel.setBackground(new Color(200, 200, 230));
    jButtonCancel.setForeground(new Color(80, 80, 133));
    jButtonCancel.setText("Cancel");
    jPanel1.setBackground(new Color(200, 200, 230));
    this.getContentPane().setBackground(new Color(200, 200, 230));
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 9, 44, 0), 9, 8));
    jPanel1.add(jLabel2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(17, 9, 14, 0), 5, 1));
    jPanel1.add(jTextIncrMinY,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 14, 0), 61, 3));
    jPanel1.add(jTextIncrMinX,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 0, 0), 61, 3));
    jPanel1.add(jLabel3,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 34, 0, 0), 8, 3));
    jPanel1.add(jLabel4,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(18, 34, 14, 0), 6, 1));
    jPanel1.add(jTextIncrMaxX,  new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 0, 11), 61, 3));
    jPanel1.add(jTextIncrMaxY,  new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(13, 0, 14, 11), 61, 3));
    jPanel3.add(jLabel9,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(-2, 8, 0, 0), 11, 8));
    jPanel3.add(jLabel10,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 8, 10, 0), 10, 1));
    jPanel3.add(jTextMoMinY,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(7, 0, 10, 0), 54, 3));
    jPanel3.add(jTextMoMinX,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(-2, 0, 0, 0), 54, 3));
    jPanel3.add(jLabel11,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(-2, 37, 6, 0), 8, 3));
    jPanel3.add(jLabel12,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 37, 10, 0), 3, 1));
    jPanel3.add(jTextMoMaxX,  new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(-2, 0, 0, 11), 60, 3));
    jPanel3.add(jTextMoMaxY,  new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 10, 11), 60, 3));
    this.getContentPane().add(jPanel2,  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 10, 0, 12), 5, 2));
    jPanel2.add(jLabel8,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(-1, 9, 0, 0), 11, 8));
    jPanel2.add(jLabel7,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 9, 11, 0), 10, 1));
    jPanel2.add(jTextCumMinY,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(7, 0, 11, 0), 54, 3));
    jPanel2.add(jTextCumMinX,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(-1, 0, 0, 0), 54, 3));
    jPanel2.add(jLabel6,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(-1, 37, 6, 0), 8, 3));
    jPanel2.add(jLabel5,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 37, 11, 0), 3, 1));
    jPanel2.add(jTextCumMaxX,  new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(-1, 0, 0, 12), 60, 3));
    jPanel2.add(jTextCumMaxY,  new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 11, 12), 60, 3));
    this.getContentPane().add(jPanel3,  new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 10, 0, 12), 5, 2));
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText,1),"Moment Rate Graph Scale");
    this.getContentPane().setLayout(gridBagLayout4);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);
    jPanel1.setBorder(titledBorder1);
    jPanel1.setLayout(gridBagLayout3);
    jLabel1.setText("Min X:");
    jLabel2.setText("Min Y:");
    jLabel3.setText("Max X:");

    jLabel4.setText("Max Y:");
    this.getContentPane().add(jPanel1,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 10, 0, 12), 3, -3));
    jPanel2.add(jLabel8, null);
    jPanel2.add(jLabel7, null);
    jPanel2.add(jTextCumMinX, null);
    jPanel2.add(jTextCumMinY, null);
    jPanel2.add(jLabel6, null);
    jPanel2.add(jLabel5, null);
    jPanel2.add(jTextCumMaxX, null);
    jPanel2.add(jTextCumMaxY, null);
    jPanel3.add(jLabel9, null);
    jPanel3.add(jLabel10, null);
    jPanel3.add(jTextMoMinX, null);
    jPanel3.add(jTextMoMinY, null);
    jPanel3.add(jLabel11, null);
    jPanel3.add(jLabel12, null);
    jPanel3.add(jTextMoMaxX, null);
    jPanel3.add(jTextMoMaxY, null);
    this.getContentPane().add(jButtonCancel,  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 7, 12, 12), 3, -1));
    this.getContentPane().add(jButtonOk,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 142, 12, 0), 20, -1));
  }

}