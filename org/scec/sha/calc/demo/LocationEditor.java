package org.scec.sha.calc.demo;

import java.awt.*;
import java.math.*;

import javax.swing.*;

import org.scec.data.*;
import org.scec.param.editor.*;

/**
 * <b>Title:</b> LocationEditor <p>
 *
 * <b>Description:</b> This GUI widget provides an
 * editor for editing a Location object. Therefore it has textfields for
 * editing the latitude, longitude, and depth, i.e. the variables
 * of the Location object. This GUI lets a user fully specify a Location
 * object.<p>
 *
 * The basic layout is that there is a label and a textfield within a panel
 * for each variable( i.e. 3 fields so 3 lables and 3 text fields ).<p>
 *
 * @author  Steven W. Rock
 * @version 1.0
 */

public class LocationEditor extends DefaultEditor {

    /** Class name used for debugging */
    protected final static String C = "ParameterEditor";
    protected final static boolean D = false;

    /** Model ( i.e. data ) for this GUI editor */
    protected Location location;


    // Latitude gui components
    JPanel latPanel = new JPanel();
    JLabel latLabel = new JLabel();
    NumericTextField latTextField = new NumericTextField();

    // longitude gui components
    JPanel lonPanel = new JPanel();
    JLabel lonLabel = new JLabel();
    NumericTextField lonTextField = new NumericTextField();


    // depth gui components
    JPanel depthPanel = new JPanel();
    JLabel depthLabel = new JLabel();
    NumericTextField depthTextField = new NumericTextField();



    /** Constructor for the LocationEditor object */
    public LocationEditor() {
        super();
        try { jbInit2(); }
        catch ( Exception e ) { e.printStackTrace(); }
    }


    /**
     *  Sets the location of the LocationEditor object. Updates
     *  all text fields with the new values.
     *
     * @param  newLocation  The new location value
     */
    public void setTheLocation( Location newLocation ) {

        location = newLocation;

        BigDecimal bdX = new BigDecimal( location.getLatitude() );
        BigDecimal bdY = new BigDecimal( location.getLongitude() );
        BigDecimal bdD = new BigDecimal( location.getDepth() );

        bdX = bdX.setScale(4,BigDecimal.ROUND_UP);
        bdY = bdY.setScale(4,BigDecimal.ROUND_UP);
        bdD = bdD.setScale(4,BigDecimal.ROUND_UP);

        this.latTextField.setText( "" + bdX.toString() );
        this.lonTextField.setText( "" + bdY.toString() );
        this.depthTextField.setText( "" + bdD.toString() );

    }

    /**
     *  Gets the location of the LocationEditor object
     *  from the text field values.
     *
     * @return    The location value
     */
    public Location getTheLocation() {

        String latStr = this.latTextField.getText();
        String lonStr = this.lonTextField.getText();
        String depthStr = this.depthTextField.getText();


        location.setLatitude( Double.parseDouble(latStr ) );
        location.setLongitude( Double.parseDouble(lonStr));
        location.setDepth(Double.parseDouble(depthStr ) );

        return location;
    }


    /**
     * GUI Initializer
     */
    private void jbInit2() throws Exception {


        latPanel.setBackground( COLORS[0] );
        latPanel.setLayout(GBL);

        latLabel.setFont( MEDIUM_FONT );
        latLabel.setText("Latitude:" );
        latLabel.setForeground( this.textColor );

        latTextField.setFont( SMALL_FONT );
        latTextField.setText( "20.1" );
        latTextField.setBackground( COLORS[0] );

        lonPanel.setBackground( COLORS[1] );
        lonPanel.setLayout(GBL);

        lonLabel.setFont( MEDIUM_FONT );
        lonLabel.setText("Longitude:" );
        lonLabel.setForeground( this.textColor );

        lonTextField.setFont( SMALL_FONT );
        lonTextField.setText( "20.1" );
        lonTextField.setBackground( COLORS[1] );

        depthPanel.setBackground(COLORS[2] );
        depthPanel.setLayout(GBL);

        depthLabel.setFont( MEDIUM_FONT );
        depthLabel.setText("Depth:" );
        depthLabel.setForeground( this.textColor );

        depthTextField.setFont( SMALL_FONT );
        depthTextField.setText( "20.1" );
        depthTextField.setBackground( COLORS[2] );


        inputsPanel.add( latPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );
        latPanel.add( latLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        latPanel.add( latTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );


        inputsPanel.add( lonPanel, new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );
        lonPanel.add( lonLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        lonPanel.add( lonTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );


        inputsPanel.add( depthPanel, new GridBagConstraints(4, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );
        depthPanel.add( depthLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        depthPanel.add( depthTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );

    }


    /**
     * Returns the location variables values as a string. Useful for debugging.
     */
    public String toString() {
        return this.location.toString();
    }


}
