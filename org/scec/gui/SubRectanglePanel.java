package org.scec.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.scec.exceptions.*;
import org.scec.gui.*;
import org.scec.param.*;
import org.scec.param.event.*;

import org.scec.param.editor.*;

// FIX - Needs more comments

/**
 * <p>Title: SubRectanglePanel</p>
 * <p>Description: GUI Widget that allows you to specify a window
 * into a 2D matrix </p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class SubRectanglePanel extends JPanel{

    int xStart, xEnd, yStart, yEnd;
    int xMin, xMax, yMin, yMax;

    GridBagLayout GBL = new GridBagLayout();
    Insets zero = new Insets(0,0,0,0);
    final static Dimension DIM = new Dimension(60, 18);
    final static Font FONT = new java.awt.Font("Dialog", 1, 11);
    JPanel yMaxPanel = new JPanel();
    JPanel yMinPanel = new JPanel();
    JPanel xMaxPanel = new JPanel();
    JPanel xMinPanel = new JPanel();


    JLabel xMinLabel = new JLabel();
    JTextField xMinTextField = new IntegerTextField();

    JLabel xMaxLabel = new JLabel();
    JTextField xMaxTextField = new IntegerTextField();

    JLabel yMinLabel = new JLabel();
    JTextField yMinTextField = new IntegerTextField();

    JLabel yMaxLabel = new JLabel();
    JTextField yMaxTextField = new IntegerTextField();
    JPanel controlPanel = new JPanel();
   // JButton jButton1 = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel xLabel = new JLabel();
    JLabel yLabel = new JLabel();


    public int getXMin(){ return (new Integer(xMinTextField.getText())).intValue();}
    public int getXMax(){return (new Integer(xMaxTextField.getText())).intValue();}
    public int getYMin(){return (new Integer(yMinTextField.getText())).intValue();}
    public int getYMax(){return (new Integer(yMaxTextField.getText())).intValue();}


    public void setFullRange(int xMin, int xMax, int yMin, int yMax){

        this.xMin = xMin;
        xStart = xMin;
        xMinTextField.setText("" + xMin);

        this.xMax = xMax;
        xEnd = xMax;
        xMaxTextField.setText("" + xMax);

        this.yMin = yMin;
        this.yStart = yMin;
        yMinTextField.setText("" + yMin);

        this.yMax = yMax;
        this.yEnd = yMax;
        yMaxTextField.setText("" + yMax);

        xLabel.setText("X Range (" + xStart + "-" + xEnd + ")");
        yLabel.setText("Y Range (" + yStart + "-" + yEnd + ')');

    }
    public SubRectanglePanel(int xMin, int xMax, int yMin, int yMax) {

    try { jbInit(); }
        catch(Exception e) { e.printStackTrace(); }
        setFullRange(xMin, xMax, yMin, yMax);


    }

    private void jbInit() throws Exception {

        this.setBackground(Color.white);
        this.setLayout(GBL);

        xMinPanel.setBackground(Color.white);
        xMinPanel.setLayout(GBL);

        xMaxPanel.setBackground(Color.white);
        xMaxPanel.setLayout(GBL);

        yMinPanel.setBackground(Color.white);
        yMinPanel.setLayout(GBL);

        yMaxPanel.setBackground(Color.white);
        yMaxPanel.setLayout(GBL);

        xMinLabel.setFont(FONT);
        xMinLabel.setForeground(SystemColor.activeCaption);
        xMinLabel.setToolTipText("");
        xMinLabel.setText("X Start:  ");

        xMinTextField.setBorder(BorderFactory.createLoweredBevelBorder());
        xMinTextField.setMinimumSize(DIM);
        xMinTextField.setPreferredSize(DIM);
        xMinTextField.setText("0");
        xMinTextField.setHorizontalAlignment(SwingConstants.TRAILING);


        xMaxLabel.setFont(FONT);
        xMaxLabel.setForeground(SystemColor.activeCaption);
        xMaxLabel.setToolTipText("");
        xMaxLabel.setText("X End:    ");

        xMaxTextField.setBorder(BorderFactory.createLoweredBevelBorder());
        xMaxTextField.setMinimumSize(DIM);
        xMaxTextField.setPreferredSize(DIM);
        xMaxTextField.setText("0");
        xMaxTextField.setHorizontalAlignment(SwingConstants.TRAILING);


        yMinLabel.setFont(FONT);
        yMinLabel.setForeground(SystemColor.activeCaption);
        yMinLabel.setToolTipText("");
        yMinLabel.setText("Y Start:  ");

        yMinTextField.setBorder(BorderFactory.createLoweredBevelBorder());
        yMinTextField.setMinimumSize(DIM);
        yMinTextField.setPreferredSize(DIM);
        yMinTextField.setText("0");
        yMinTextField.setHorizontalAlignment(SwingConstants.TRAILING);


        yMaxLabel.setFont(FONT);
        yMaxLabel.setForeground(SystemColor.activeCaption);
        yMaxLabel.setToolTipText("");
        yMaxLabel.setText("Y End:    ");

        yMaxTextField.setBorder(BorderFactory.createLoweredBevelBorder());
        yMaxTextField.setMinimumSize(DIM);
        yMaxTextField.setPreferredSize(DIM);
        yMaxTextField.setText("0");
        yMaxTextField.setHorizontalAlignment(SwingConstants.TRAILING);


        controlPanel.setBackground(Color.white);
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        controlPanel.setLayout(gridBagLayout1);

        /*jButton1.setBackground(Color.white);
        jButton1.setFont(FONT);
        jButton1.setForeground(SystemColor.activeCaption);
        jButton1.setBorder(BorderFactory.createRaisedBevelBorder());
        jButton1.setText("Defaults");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });*/


        xLabel.setFont(FONT);
        xLabel.setForeground(SystemColor.activeCaption);
        xLabel.setText("X Range (0-100)");

        yLabel.setFont(FONT);
        yLabel.setForeground(SystemColor.activeCaption);
        yLabel.setText("Y Range ( 0 - 100 )");


        this.add(xMinPanel,    new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this.add(xMaxPanel,      new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this.add(yMinPanel,      new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this.add(yMaxPanel,      new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


        this.add(controlPanel,  new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
       // controlPanel.add(jButton1,       new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        //    ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 4, 1, 2), 0, 0));
        controlPanel.add(xLabel,       new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));


        xMinPanel.add(xMinLabel,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 3), 0, 0));
        xMinPanel.add(xMinTextField,    new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));

        xMaxPanel.add(xMaxLabel,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 3), 0, 0));
        xMaxPanel.add(xMaxTextField,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));

        yMinPanel.add(yMinLabel,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
        yMinPanel.add(yMinTextField,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));

        yMaxPanel.add(yMaxLabel,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
        yMaxPanel.add(yMaxTextField,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
        controlPanel.add(yLabel,   new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));


    }

    /*void jButton1_actionPerformed(ActionEvent e) {

        this.xMin = xStart;
        xMinTextField.setText("" + xMin);

        this.xMax = xEnd;
        xMaxTextField.setText("" + xMax);

        this.yMin = yStart;
        yMinTextField.setText("" + yMin);

        this.yMax = yEnd;
        yMaxTextField.setText("" + yMax);

    }*/

    public boolean isEnabled(){
        return enabled;
    }
    protected boolean enabled = false;
    public void enable(){

        enabled = true;
        xMaxTextField.enable();
        xMinTextField.enable();

        yMaxTextField.enable();
        yMinTextField.enable();

    }
    public void disable(){
        enabled = false;

        xMaxTextField.disable();
        xMinTextField.disable();

        yMaxTextField.disable();
        yMinTextField.disable();

    }



}
