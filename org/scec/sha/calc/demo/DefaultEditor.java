package org.scec.sha.calc.demo;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class DefaultEditor  extends JPanel {

    protected final static String C = "DirectionEditor";
    protected final static boolean D = false;
    protected boolean editMode = true;

    private TitledBorder titledBorder = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(178, 178, 178)),"Direction");

    protected final static Font SMALL_FONT = new java.awt.Font( "Dialog", 1, 10 );
    protected final static Font MEDIUM_FONT = new java.awt.Font( "Dialog", 1, 11 );
    protected final static Font LARGE_FONT = new java.awt.Font( "Dialog", 1, 12 );

    protected final static GridBagLayout GBL = new GridBagLayout();

    /**
     *  Description of the Field
     */
    protected final static Insets THREE_INSETS = new Insets( 3, 3, 3, 3 );
    protected final static Insets TEN_FIVE_INSETS = new Insets(0, 10, 0, 5);
    protected final static Insets ZERO_INSETS = new Insets(0, 0, 0, 0);


    protected Color textColor = Color.black;
    protected JPanel inputsPanel = new JPanel();

    protected static Color[] COLORS = new Color[]{

        new Color(180, 180, 235),
        new Color(235, 180, 235),
        new Color(180, 235, 180),
        new Color(225, 225, 160),
        new Color(180, 235, 235)
    };

    /**
     *  Constructor for the LocationEditor object
     */
    public DefaultEditor() {
        try {
            jbInit();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setName(String name){
        this.titledBorder.setTitle( name );
    }

    /**
     *  Sets the editMode attribute of the LocationEditor object
     *
     * @param  newEditMode  The new editMode value
     */
    public void setEditMode( boolean newEditMode ) {
        editMode = newEditMode;
    }

    /**
     *  Gets the editMode attribute of the LocationEditor object
     *
     * @return    The editMode value
     */
    public boolean isEditMode() {
        return editMode;
    }


    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    private void jbInit() throws Exception {

        titledBorder.setTitleFont(LARGE_FONT);
        titledBorder.setTitleColor(textColor);

        this.setBackground( Color.white );
        this.setLayout(GBL);


        inputsPanel.setBackground(Color.white);
        inputsPanel.setBorder(titledBorder);
        inputsPanel.setLayout(GBL);


        this.add(inputsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));

    }

    public void setTextColor(java.awt.Color newTextColor) {
        textColor = newTextColor;
    }
    public java.awt.Color getTextColor() {
        return textColor;
    }

}
