package org.scec.sha.calc.tests.relativeLocation;

import java.awt.*;
import java.math.*;

import javax.swing.*;

import org.scec.data.*;
import org.scec.param.editor.*;

/**
 * <b>Title:</b> DirectionEditor <p>
 * <b>Description:</b> This GUI widget provides an
 * editor for editing a Direction object. Therefore it has textfields for
 * editing the horDistanc, verticalDistance, azimuth amd backAzimuth, i.e. the variables
 * of the Direction object. This GUI lets a user fully specify a Direction
 * object.<p>
 *
 * The basic layout is that there is a label and a textfield within a panel
 * for each variable( i.e. 4).<p>
 * <p>
 *
 * @author  Steven W. Rock
 * @version 1.0
 */


public class DirectionEditor extends DefaultEditor {


    /** Model ( i.e. data ) for this GUI editor */
    protected Direction direction;


    // Horizontal distnance GUI elements
    JPanel horzPanel = new JPanel();
    JLabel horzLabel = new JLabel();
    NumericTextField horzTextField = new NumericTextField();

    // vertical distnance GUI elements
    JPanel vertPanel = new JPanel();
    JLabel vertLabel = new JLabel();
    NumericTextField vertTextField = new NumericTextField();

    // azimuth GUI elements
    JPanel azimuthPanel = new JPanel();
    JLabel azimuthLabel = new JLabel();
    NumericTextField azimuthTextField = new NumericTextField();

    // backAzimuth GUI elements
    JPanel backAzimuthPanel = new JPanel();
    JLabel backAzimuthLabel = new JLabel();
    NumericTextField backAzimuthTextField = new NumericTextField();




    /** Constructor  */
    public DirectionEditor() {
        super();
        try { jbInit2(); }
        catch ( Exception e ) { e.printStackTrace(); }
    }


    /**
     *  Sets all the fields and model of the gui widget by passing in a
     *  new Dirction object.
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





    /** Returns a Direction object based on all the text field values. */
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


    /** Initializes all the gui elements */
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


    /** Returns the model field values as a string. */
    public String toString() {
        return this.direction.toString();
    }

}
