package org.scec.sha.calc.tests.relativeLocation;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * <b>Title:</b> DefaultEditor<p>
 *
 * <b>Description:</b> Base class for the Direction and Location editors
 * used in the Relative Location Applet. These editors are simple a label
 * and text field. This class defines some of the common parameters and
 * default setup functions.<p>
 *
 * This class is soley to avoid duplication of code between the
 * LocationEditor and the Direction Editor.<p>
 *
 * @author  Steven W. Rock
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
        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }
    }

    /** Sets the title of the titled border. Shows up in the GUI. */
    public void setName(String name){
        this.titledBorder.setTitle( name );
    }

    /**
     *  Toggles edit mode for the fields. If false, users cannot
     *  change the values in the GUI. Will be set to false for the
     *  output fields.
     */
    public void setEditMode( boolean newEditMode ) {
        editMode = newEditMode;
    }

    /**
     * Returns true if in edit mode, false if uneditable.
     */
    public boolean isEditMode() {
        return editMode;
    }


    /**
     * GUI initializer
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


    public void setTextColor(java.awt.Color newTextColor) { textColor = newTextColor; }
    public java.awt.Color getTextColor() { return textColor; }

}
