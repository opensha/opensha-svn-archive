package org.scec.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *  <b>Title:</b> OvalBorder<br>
 *  <b>Description:</b> This class implements a Border that has rounded edges.
 *  Gives a GUI a sofgter feel, instead of typical rectangular components<p>
 *
 *  <b>Note: <.b>This class has a few problems with aliasing for single
 *  thickness lines with small rounded corners. May need to be fixed at some
 *  point in the future. For our app we use the double thick line so not a
 *  problem<P>
 *
 *  @ see Border
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class OvalBorder implements Border {

    /**
     *  Width in pixels of the rounded corners, the higher the number, the
     *  larger the rounded corner is drawn
     */
    protected int width = 6;
    /**
     *  Height in pixels of the rounded corners, the higher the number, the
     *  larger the rounded corner is drawn
     */
    protected int height = 6;

    /**
     *  The color of the border in the top and left sides, defaults to white
     */
    protected Color topColor = Color.white;
    /**
     *  The color of the border in the bottom and right sides, defaults to gray
     */
    protected Color bottomColor = Color.gray;

    /**
     *  Can draw line as either single thickness or double. Set this value to
     *  true for double. Defaults to double
     */
    protected boolean thickLine = true;


    /**
     *  No-Arg constructor sets width and height = 6 for the rounded corners
     */
    public OvalBorder() {
        width = 6;
        height = 6;
    }


    /**
     *  Constructor where you can set the rounded corners width and height
     *
     * @param  w  Description of the Parameter
     * @param  h  Description of the Parameter
     */
    public OvalBorder( int w, int h ) {
        width = w;
        height = h;
    }


    /**
     *  Constructor where you can set the rounded corners width and height, and
     *  the colors of the top-left and bottom-right halves
     *
     * @param  w            Description of the Parameter
     * @param  h            Description of the Parameter
     * @param  topColor     Description of the Parameter
     * @param  bottomColor  Description of the Parameter
     */
    public OvalBorder( int w, int h, Color topColor, Color bottomColor ) {
        width = w;
        height = h;
        topColor = topColor;
        bottomColor = bottomColor;
    }


    /**
     *  Sets the topColor attribute of the OvalBorder object
     *
     * @param  newTopColor  The new topColor value
     */
    public void setTopColor( java.awt.Color newTopColor ) {
        topColor = newTopColor;
    }


    /**
     *  Sets the bottomColor attribute of the OvalBorder object
     *
     * @param  newBottomColor  The new bottomColor value
     */
    public void setBottomColor( java.awt.Color newBottomColor ) {
        bottomColor = newBottomColor;
    }


    /**
     *  Sets the width attribute of the OvalBorder object
     *
     * @param  newWidth  The new width value
     */
    public void setWidth( int newWidth ) {
        width = newWidth;
    }


    /**
     *  Sets the height attribute of the OvalBorder object
     *
     * @param  newHeight  The new height value
     */
    public void setHeight( int newHeight ) {
        height = newHeight;
    }


    /**
     *  Sets the thickLine attribute of the OvalBorder object
     *
     * @param  newThickLine  The new thickLine value
     */
    public void setThickLine( boolean newThickLine ) {
        thickLine = newThickLine;
    }


    /**
     *  Gets the borderInsets attribute of the OvalBorder object
     *
     * @param  c  Description of the Parameter
     * @return    The borderInsets value
     */
    public Insets getBorderInsets( Component c ) {
        return new Insets( height, width, height, width );
    }


    /**
     *  Gets the borderOpaque attribute of the OvalBorder object
     *
     * @return    The borderOpaque value
     */
    public boolean isBorderOpaque() {
        return true;
    }


    /**
     *  Gets the topColor attribute of the OvalBorder object
     *
     * @return    The topColor value
     */
    public java.awt.Color getTopColor() {
        return topColor;
    }


    /**
     *  Gets the bottomColor attribute of the OvalBorder object
     *
     * @return    The bottomColor value
     */
    public java.awt.Color getBottomColor() {
        return bottomColor;
    }


    /**
     *  Gets the width attribute of the OvalBorder object
     *
     * @return    The width value
     */
    public int getWidth() {
        return width;
    }


    /**
     *  Gets the height attribute of the OvalBorder object
     *
     * @return    The height value
     */
    public int getHeight() {
        return height;
    }


    /**
     *  Gets the thickLine attribute of the OvalBorder object
     *
     * @return    The thickLine value
     */
    public boolean isThickLine() {
        return thickLine;
    }


    /**
     *  Description of the Method
     *
     * @param  c  Description of the Parameter
     * @param  g  Description of the Parameter
     * @param  x  Description of the Parameter
     * @param  y  Description of the Parameter
     * @param  w  Description of the Parameter
     * @param  h  Description of the Parameter
     */
    public void paintBorder(
            Component c,
            Graphics g,
            int x, int y, int w, int h ) {
        w--;
        h--;
        g.setColor( topColor );

        g.drawLine( x, y + h - height, x, y + height );

        g.drawArc( x, y, 2 * width, 2 * height, 180, -90 );
        //g.drawArc(x+1, y+1, 2*width, 2*height, 180, -90);
        g.drawArc( x + 1, y, 2 * width, 2 * height, 180, -90 );
        //g.drawArc(x, y+1, 2*width, 2*height, 180, -90);

        g.drawLine( x + width, y, x + w - width, y );
        //g.drawLine(x+width, y+1, x+w-width, y+1);

        g.drawArc( x + w - 2 * width, y, 2 * width, 2 * height, 90, -90 );
        //g.drawArc(x+w-2*width - 1, y +1, 2*width, 2*height, 90, -90);
        g.drawArc( x + w - 2 * width - 1, y, 2 * width, 2 * height, 90, -90 );
        //g.drawArc(x+w-2*width, y +1, 2*width, 2*height, 90, -90);

        g.setColor( bottomColor );

        g.drawLine( x + w, y + height, x + w, y + h - height );
        //g.drawLine(x+w - 1, y+height, x+w - 1, y+h-height);

        g.drawArc( x + w - 2 * width, y + h - 2 * height, 2 * width, 2 * height, 0, -90 );
        //g.drawArc(x+w-2*width - 1, y+h-2*height -1, 2*width, 2*height, 0, -90);
        g.drawArc( x + w - 2 * width, y + h - 2 * height - 1, 2 * width, 2 * height, 0, -90 );
        //g.drawArc(x+w-2*width - 1, y+h-2*height , 2*width, 2*height, 0, -90);

        g.drawLine( x + width, y + h, x + w - width, y + h );
        //g.drawLine(x+width, y+h -1, x+w-width, y+h - 1);

        g.drawArc( x, y + h - 2 * height, 2 * width, 2 * height, -90, -90 );
        //g.drawArc(x + 1, y+h-2*height - 1, 2*width, 2*height, -90, -90);
        g.drawArc( x, y + h - 2 * height - 1, 2 * width, 2 * height, -90, -90 );
        //g.drawArc(x + 1, y+h-2*height, 2*width, 2*height, -90, -90);


        if ( thickLine ) {

            g.drawLine( x + 1, y + h - height, x + 1, y + height );

            g.drawArc( x + 1, y + 1, 2 * width, 2 * height, 180, -90 );
            g.drawArc( x, y + 1, 2 * width, 2 * height, 180, -90 );

            g.drawLine( x + width, y + 1, x + w - width, y + 1 );

            g.drawArc( x + w - 2 * width - 1, y + 1, 2 * width, 2 * height, 90, -90 );
            g.drawArc( x + w - 2 * width, y + 1, 2 * width, 2 * height, 90, -90 );

            g.setColor( bottomColor );

            g.drawLine( x + w - 1, y + height, x + w - 1, y + h - height );

            g.drawArc( x + w - 2 * width - 1, y + h - 2 * height - 1, 2 * width, 2 * height, 0, -90 );
            g.drawArc( x + w - 2 * width - 1, y + h - 2 * height, 2 * width, 2 * height, 0, -90 );

            g.drawLine( x + width, y + h - 1, x + w - width, y + h - 1 );

            g.drawArc( x + 1, y + h - 2 * height - 1, 2 * width, 2 * height, -90, -90 );
            g.drawArc( x + 1, y + h - 2 * height, 2 * width, 2 * height, -90, -90 );

        }
    }


    /**
     *  Tester function that makes a frame and draws a OvalBorder on it
     *
     * @param  args  The command line arguments
     */
    public static void main( String[] args ) {
        JFrame frame = new JFrame( "Custom Border: OvalBorder" );
        JLabel label = new JLabel( "OvalBorder" );
        ( ( JPanel ) frame.getContentPane() ).setBorder( new CompoundBorder(
                new EmptyBorder( 10, 10, 10, 10 ), new OvalBorder( 20, 20, Color.blue, Color.black ) ) );
        frame.getContentPane().add( label );
        frame.setBounds( 0, 0, 300, 150 );
        frame.setVisible( true );
    }

}
