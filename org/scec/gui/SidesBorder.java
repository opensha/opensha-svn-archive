package org.scec.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *  <b>Title: </b> SidesBorder<p>
 *  Description: This class implements a Border where you can set any side color
 *  individually. You can also set on or off the drawing of any side. Especially
 *  useful to give the illusion of an underline. Setting the top and sides
 *  border to the same color as the background panel, users only see the bottom
 *  border.<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class SidesBorder implements Border {

    /**
     *  Top side of the border color
     */
    Color topColor = new Color( 120, 160, 100 );
    /**
     *  Bottom side of the border color
     */
    Color bottomColor = Color.yellow;
    /**
     *  Right side of the border color
     */
    Color leftColor = Color.red;
    /**
     *  Left side of the border color
     */
    Color rightColor = Color.blue;

    /**
     *  Description of the Field
     */
    private int height = 0;
    /**
     *  Description of the Field
     */
    private int width = 0;

    /**
     *  Boolean whether to draw the left of the border
     */
    private boolean drawLeft = true;
    /**
     *  Boolean whether to draw the right of the border
     */
    private boolean drawRight = true;
    /**
     *  Boolean whether to draw the top of the border
     */
    private boolean drawTop = true;
    /**
     *  Boolean whether to draw the bottom of the border
     */
    private boolean drawBottom = true;


    /**
     *  No-Arg constructor - all sides drawn, all sides different color
     */
    public SidesBorder() { }


    /**
     *  Constructor for the SidesBorder object
     *
     * @param  topColor     Color for the top side of the border
     * @param  bottomColor  Color for the bottom side of the border
     * @param  leftColor    Color for the left side of the border
     * @param  rightColor   Color for the right side of the border
     */
    public SidesBorder(
            Color topColor,
            Color bottomColor,
            Color leftColor,
            Color rightColor
             ) {

        this.topColor = topColor;
        this.bottomColor = bottomColor;
        this.leftColor = leftColor;
        this.rightColor = rightColor;
    }


    /**
     *  Sets the left Color attribute of the SidesBorder object
     *
     * @param  a  The new leftColor value
     */
    public void setLeftColor( Color a ) {
        leftColor = a;
    }


    /**
     *  Sets the right Color attribute of the SidesBorder object
     *
     * @param  a  The new rightColor value
     */
    public void setRightColor( Color a ) {
        rightColor = a;
    }


    /**
     *  Sets the top Color attribute of the SidesBorder object
     *
     * @param  a  The new topColor value
     */
    public void setTopColor( Color a ) {
        topColor = a;
    }


    /**
     *  Sets the bottom Color attribute of the SidesBorder object
     *
     * @param  a  The new bottomColor value
     */
    public void setBottomColor( Color a ) {
        bottomColor = a;
    }


    /**
     *  Sets the width attribute
     *
     * @param  a  The new width value
     */
    public void setWidth( int a ) {
        width = a;
    }


    /**
     *  Sets the height attribute
     *
     * @param  a  The new height value
     */
    public void setHeight( int a ) {
        height = a;
    }


    /**
     *  Sets the drawLeft attribute which determines if the left border is drawn
     *
     * @param  a  The new drawLeft value
     */
    public void setDrawLeft( boolean a ) {
        drawLeft = a;
    }


    /**
     *  Sets the drawRight attribute which determines if the right border is
     *  drawn
     *
     * @param  a  The new drawRight value
     */
    public void setDrawRight( boolean a ) {
        drawRight = a;
    }


    /**
     *  Sets the drawTop attribute which determines iif the top border is drawn
     *
     * @param  a  The new drawTop value
     */
    public void setDrawTop( boolean a ) {
        drawTop = a;
    }


    /**
     *  Sets the drawBottom attribute of the SidesBorder object
     *
     * @param  a  The new drawBottom value
     */
    public void setDrawBottom( boolean a ) {
        drawBottom = a;
    }



    /**
     *  Gets the borderInsets attribute of the SidesBorder object
     *
     * @param  c
     * @return    The borderInsets value
     */
    public Insets getBorderInsets( Component c ) {
        return new Insets( height, width, height, width );
    }


    /**
     *  Returns true if the border is opaque
     *
     * @return    The borderOpaque value
     */
    public boolean isBorderOpaque() {
        return true;
    }


    /**
     *  Gets the leftColor attribute of the SidesBorder object
     *
     * @return    The leftColor value
     */
    public Color getLeftColor() {
        return leftColor;
    }


    /**
     *  Gets the rightColor attribute of the SidesBorder object
     *
     * @return    The rightColor value
     */
    public Color getRightColor() {
        return rightColor;
    }


    /**
     *  Gets the topColor attribute of the SidesBorder object
     *
     * @return    The topColor value
     */
    public Color getTopColor() {
        return topColor;
    }


    /**
     *  Gets the bottomColor attribute of the SidesBorder object
     *
     * @return    The bottomColor value
     */
    public java.awt.Color getBottomColor() {
        return bottomColor;
    }


    /**
     *  Gets the width attribute of the SidesBorder object
     *
     * @return    The width value
     */
    public int getWidth() {
        return width;
    }


    /**
     *  Gets the height attribute of the SidesBorder object
     *
     * @return    The height value
     */
    public int getHeight() {
        return height;
    }


    /**
     *  Returns if the left border will be drawn or not
     *
     * @return    true if the left border is being drawn
     */
    public boolean isDrawLeft() {
        return drawLeft;
    }


    /**
     *  Returns if the right border will be drawn or not
     *
     * @return    true if the right border is being drawn
     */
    public boolean isDrawRight() {
        return drawRight;
    }


    /**
     *  Returns if the top border will be drawn or not
     *
     * @return    true if the top border is being drawn
     */
    public boolean isDrawTop() {
        return drawTop;
    }


    /**
     *  Returns if the bottom border will be drawn or not
     *
     * @return    true if the bottom border is being drawn
     */
    public boolean isDrawBottom() {
        return drawBottom;
    }


    /**
     *  Description of the Method
     *
     * @param  c  The component that the border is painted to
     * @param  g  Graphics being drawn to
     * @param  x
     * @param  y
     * @param  w
     * @param  h
     */
    public void paintBorder(
            Component c,
            Graphics g,
            int x, int y, int w, int h ) {
        w--;
        h--;

        if ( isDrawTop() ) {
            g.setColor( topColor );

            if ( width > 0 || height > 0 ) {
                g.drawArc( x, y, 2 * width, 2 * height, 180, -90 );
                g.drawArc( x + w - 2 * width, y, 2 * width, 2 * height, 90, -90 );
            }

            g.drawLine( x + width, y, x + w - width, y );
        }

        if ( isDrawLeft() ) {
            g.setColor( leftColor );
            g.drawLine( x, y + h - height, x, y + height );
        }

        if ( isDrawBottom() ) {
            g.setColor( bottomColor );
            if ( width > 0 || height > 0 ) {
                g.drawArc( x + w - 2 * width, y + h - 2 * height, 2 * width, 2 * height, 0, -90 );
                g.drawArc( x, y + h - 2 * height, 2 * width, 2 * height, -90, -90 );
            }
            g.drawLine( x + width, y + h, x + w - width, y + h );
        }

        if ( isDrawRight() ) {
            g.setColor( rightColor );
            g.drawLine( x + w, y + height, x + w, y + h - height );
        }
    }



    /**
     *  Tester function to see the border in action
     *
     * @param  args  The command line arguments
     */
    public static void main( String[] args ) {
        JFrame frame = new JFrame( "Custom Border: SideBorder" );
        JLabel label = new JLabel( "SideBorder" );
        ( ( JPanel ) frame.getContentPane() ).setBorder( new CompoundBorder(
                new EmptyBorder( 10, 10, 10, 10 ), new SidesBorder( Color.blue,
                Color.black, Color.red, Color.yellow ) ) );
        frame.getContentPane().add( label );
        frame.setBounds( 0, 0, 300, 150 );
        frame.setVisible( true );
    }


    /**
     *  Prints out state of the border variables
     *
     * @return    variable values
     */
    public String toString() {

        StringBuffer b = new StringBuffer( "SidesBorder:\n" );
        b.append( "\tDraw Top = " );
        b.append( drawTop );
        b.append( "\tDraw Bottom = " );
        b.append( drawBottom );
        b.append( "\tDraw Left = " );
        b.append( drawLeft );
        b.append( "\tDraw Right = " );
        b.append( drawRight );
        return b.toString();
    }
}
