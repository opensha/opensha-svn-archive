package org.scec.sha.calc.demo;

import java.awt.*;
import java.math.*;

import javax.swing.*;

import org.scec.data.*;
import org.scec.param.editor.*;

// Fix - Needs more comments


/**
 * <b>Title:</b> DirectionEditor <p>
 * <b>Description:</b>
 * <p>
 *
 * @author  Steven W. Rock
 * @version 1.0
 */


public class DirectionEditor extends DefaultEditor {


    /**
     *  Description of the Field
     */
    protected Direction direction;




    /**
     *  Description of the Field
     */
    /**
     *  Description of the Field
     */
    JPanel horzPanel = new JPanel();
    /**
     *  Description of the Field
     */
    JPanel vertPanel = new JPanel();
    /**
     *  Description of the Field
     */
    JPanel azimuthPanel = new JPanel();
    JPanel backAzimuthPanel = new JPanel();

    /**
     *  Description of the Field
     */
    /**
     *  Description of the Field
     */
    JLabel horzLabel = new JLabel();
    /**
     *  Description of the Field
     */
    JLabel vertLabel = new JLabel();
    /**
     *  Description of the Field
     */
    JLabel azimuthLabel = new JLabel();
    JLabel backAzimuthLabel = new JLabel();

    /**
     *  Description of the Field
     */
    NumericTextField horzTextField = new NumericTextField();
    /**
     *  Description of the Field
     */
    NumericTextField vertTextField = new NumericTextField();
    /**
     *  Description of the Field
     */
    NumericTextField azimuthTextField = new NumericTextField();

    NumericTextField backAzimuthTextField = new NumericTextField();



    /**
     *  Constructor for the LocationEditor object
     */
    public DirectionEditor() {
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
    public void setTheDirection( Direction newDirection ) {
        direction = newDirection;

        BigDecimal bdH = new BigDecimal( direction.getHorzDistance() );
        BigDecimal bdV = new BigDecimal( direction.getVertDistance() );
        BigDecimal bdA = new BigDecimal( direction.getAzimuth() );
        BigDecimal bdB = new BigDecimal( direction.getBackAzimuth() );


        bdH = bdH.setScale(4,BigDecimal.ROUND_UP);
        bdV = bdV.setScale(4,BigDecimal.ROUND_UP);
        bdA = bdA.setScale(4,BigDecimal.ROUND_UP);
        bdB = bdB.setScale(4,BigDecimal.ROUND_UP);


        this.horzTextField.setText( bdH.toString() );
        this.vertTextField.setText( bdV.toString() );
        this.azimuthTextField.setText( bdA.toString() );
        this.backAzimuthTextField.setText( bdB.toString() );

    }





    /**
     *  Gets the location attribute of the LocationEditor object
     *
     * @return    The location value
     */
    public Direction getTheDirection() {

        String horzStr = this.horzTextField.getText();
        String vertStr = this.vertTextField.getText();
        String azimuthStr = this.azimuthTextField.getText();
        String backAzimuthStr = this.backAzimuthTextField.getText();

        direction.setHorzDistance( ( new Double( horzStr ) ).doubleValue() );
        direction.setVertDistance( ( new Double( vertStr ) ).doubleValue() );
        direction.setAzimuth( ( new Double( azimuthStr ) ).doubleValue() );
        direction.setBackAzimuth( ( new Double( backAzimuthStr ) ).doubleValue() );

        return direction;
    }


    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    private void jbInit2() throws Exception {





        horzPanel.setBackground( COLORS[0] );
        horzPanel.setLayout(GBL);

        horzLabel.setFont( MEDIUM_FONT );
        horzLabel.setText("Horizontal Dist.:" );
        horzLabel.setForeground( this.textColor );

        horzTextField.setFont( SMALL_FONT );
        horzTextField.setText( "20.1" );
        horzTextField.setBackground( COLORS[0] );


        vertPanel.setBackground( COLORS[1] );
        vertPanel.setLayout(GBL);

        vertLabel.setFont( MEDIUM_FONT );
        vertLabel.setText("Vertical Dist.:" );
        vertLabel.setForeground( this.textColor );

        vertTextField.setFont( SMALL_FONT );
        vertTextField.setText( "-20.1" );
        vertTextField.setBackground( COLORS[1] );

        azimuthPanel.setBackground( COLORS[2] );
        azimuthPanel.setLayout(GBL);

        azimuthLabel.setFont( MEDIUM_FONT );
        azimuthLabel.setText("Azimuth:" );
        azimuthLabel.setForeground( this.textColor );

        azimuthTextField.setFont( SMALL_FONT );
        azimuthTextField.setText( "1" );
        azimuthTextField.setBackground( COLORS[2] );

        backAzimuthPanel.setBackground( COLORS[3] );
        backAzimuthPanel.setLayout(GBL);

        backAzimuthLabel.setFont( MEDIUM_FONT );
        backAzimuthLabel.setText("Back Azimuth:" );
        backAzimuthLabel.setForeground( this.textColor );

        backAzimuthTextField.setFont( SMALL_FONT );
        backAzimuthTextField.setText( "1" );
        backAzimuthTextField.setBackground( COLORS[3] );



        inputsPanel.add( horzPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );
        horzPanel.add( horzLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        horzPanel.add( horzTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );

        inputsPanel.add( vertPanel, new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );

        vertPanel.add( vertLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        vertPanel.add( vertTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );

        inputsPanel.add( azimuthPanel, new GridBagConstraints(3, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );
        azimuthPanel.add( azimuthLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        azimuthPanel.add( azimuthTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );

        inputsPanel.add( backAzimuthPanel, new GridBagConstraints(4, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, THREE_INSETS, 0, 0) );
        backAzimuthPanel.add( backAzimuthLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, TEN_FIVE_INSETS, 0, 0) );
        backAzimuthPanel.add( backAzimuthTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, ZERO_INSETS, 0, 0) );


    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString() {
        return this.direction.toString();
    }

}
