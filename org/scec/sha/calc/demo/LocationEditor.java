package org.scec.sha.calc.demo;

import java.awt.*;
import java.math.*;

import javax.swing.*;

import org.scec.data.*;
import org.scec.param.editor.*;

// Fix - Needs more comments

/**
 * <b>Title:</b> LocationEditor <p>
 * <b>Description:</b>
 * <p>
 *
 * @author  Steven W. Rock
 * @version 1.0
 */

public class LocationEditor
         extends DefaultEditor {

    /*
     *  Debbuging variables
     */
    protected final static String C = "ParameterEditor";
    /**
     *  Description of the Field
     */
    protected final static boolean D = false;

    /**
     *  Description of the Field
     */
    protected Location location;




    /**
     *  Description of the Field
     */
    JPanel latPanel = new JPanel();
    /**
     *  Description of the Field
     */
    JPanel lonPanel = new JPanel();
    /**
     *  Description of the Field
     */
    JPanel depthPanel = new JPanel();


    /**
     *  Description of the Field
     */
    JLabel latLabel = new JLabel();
    /**
     *  Description of the Field
     */
    JLabel lonLabel = new JLabel();
    /**
     *  Description of the Field
     */
    JLabel depthLabel = new JLabel();

    /**
     *  Description of the Field
     */
    NumericTextField latTextField = new NumericTextField();
    /**
     *  Description of the Field
     */
    NumericTextField lonTextField = new NumericTextField();
    /**
     *  Description of the Field
     */
    NumericTextField depthTextField = new NumericTextField();



    /**
     *  Constructor for the LocationEditor object
     */
    public LocationEditor() {
        super();
        try {
            jbInit2();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    /**
     *  Sets the location attribute of the LocationEditor object
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
     *  Gets the location attribute of the LocationEditor object
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
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
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
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString() {
        return this.location.toString();
    }


}
