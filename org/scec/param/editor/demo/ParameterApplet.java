package org.scec.param.editor.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.*;

import org.scec.param.DoubleConstraint;
import org.scec.param.DoubleDiscreteConstraint;
import org.scec.param.DoubleDiscreteParameter;
import org.scec.param.DoubleParameter;
import org.scec.param.IntegerConstraint;
import org.scec.param.IntegerParameter;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterList;
import org.scec.param.StringConstraint;
import org.scec.param.StringParameter;
import org.scec.param.editor.ParameterListEditor;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeFailEvent;
import org.scec.param.event.ParameterChangeFailListener;
import org.scec.param.event.ParameterChangeListener;

// Fix - Needs more comments

/**
 *  <b>Title:</b> ParameterApplet<p>
 *  <b>Description:</b> Test applet to demonstrate the ParameterListEditor in
 *  action.<p>
 *
 * @author     Steven W. Rock
 * @created    April 17, 2002
 * @version    1.0
 */

public class ParameterApplet
         extends JApplet
         implements
        ParameterChangeListener,
        ParameterChangeFailListener {

    /**
     *  Description of the Field
     */
    protected final static String C = "ParameterApplet";
    /**
     *  Description of the Field
     */
    protected final static boolean D = true;

    /**
     *  Description of the Field
     */
    final static int NUM = 5;
    /**
     *  Description of the Field
     */
    static int paramCount = 0;

    /**
     *  Description of the Field
     */
    boolean isStandalone = false;

    /**
     *  Description of the Field
     */
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    /**
     *  Description of the Field
     */
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    /**
     *  Description of the Field
     */
    JLabel statusLabel = new JLabel();
    /**
     *  Description of the Field
     */
    JLabel mainTitleLabel = new JLabel();

    /**
     *  Description of the Field
     */
    JPanel jPanel1 = new JPanel();
    /**
     *  Description of the Field
     */
    JPanel jPanel2 = new JPanel();

    /**
     *  Gets the parameter attribute of the ParameterApplet object
     *
     * @param  key  Description of the Parameter
     * @param  def  Description of the Parameter
     * @return      The parameter value
     */
    public String getParameter( String key, String def ) {
        return ( isStandalone ? System.getProperty( key, def )
                 : this.getParameter( key ) != null ? this.getParameter( key )
                 : def );
    }

    /**
     *  Gets the appletInfo attribute of the ParameterApplet object
     *
     * @return    The appletInfo value
     */
    public String getAppletInfo() {
        return "Applet Information";
    }

    /**
     *  Gets the parameterInfo attribute of the ParameterApplet object
     *
     * @return    The parameterInfo value
     */
    public String[][] getParameterInfo() {
        return null;
    }

    /**
     *  Description of the Method
     */
    public void init() {
        try {
            jbInit();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    private void jbInit() throws Exception {
        this.getContentPane().setBackground( Color.white );
        this.setSize( new Dimension( 400, 300 ) );
        jPanel1.setBackground( Color.white );
        jPanel1.setBorder( BorderFactory.createEtchedBorder() );
        jPanel1.setMinimumSize( new Dimension( 100, 300 ) );
        jPanel1.setPreferredSize( new Dimension( 100, 300 ) );
        jPanel1.setLayout( gridBagLayout1 );
        mainTitleLabel.setBackground( Color.white );
        mainTitleLabel.setFont( new Font( "Dialog", 1, 14 ) );
        mainTitleLabel.setBorder( BorderFactory.createEtchedBorder() );
        mainTitleLabel.setHorizontalAlignment( 0 );
        mainTitleLabel.setText( "Parameter Editor Applet" );
        jPanel2.setBackground( Color.white );
        jPanel2.setFont( new Font( "Dialog", 0, 10 ) );
        jPanel2.setBorder( BorderFactory.createLoweredBevelBorder() );
        jPanel2.setLayout( gridBagLayout2 );
        statusLabel.setFont( new Font( "Dialog", 1, 10 ) );
        statusLabel.setForeground( Color.black );
        statusLabel.setText( "Status" );
        this.getContentPane().add( jPanel1, "Center" );
        jPanel1.add( mainTitleLabel,
                new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0, 10, 2,
                new Insets( 0, 0, 0, 0 ), 0, 0 ) );

        ParameterList list = makeParameterList( 5 );
        ParameterListEditor editor = new ParameterListEditor( list, this, this );

        jPanel1.add( editor,
                new GridBagConstraints( 1, 2, 1, 1, 1.0, 1.0, 10, 1,
                new Insets( 10, 10, 10, 10 ), 0, 0 ) );
        jPanel1.add( jPanel2,
                new GridBagConstraints( 1, 3, 1, 1, 1.0, 0.0, 10, 2,
                new Insets( 0, 0, 0, 0 ), 0, 0 ) );
        jPanel2.add( statusLabel,
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0, 17, 2,
                new Insets( 0, 8, 0, 0 ), 0, 0 ) );
    }

    /**
     *  Description of the Method
     *
     * @param  number  Description of the Parameter
     * @return         Description of the Return Value
     */
    private ParameterList makeParameterList( int number ) {
        Vector val = new Vector();
        val.add( "Steven" );
        val.add( "William" );
        val.add( "Michael" );
        StringConstraint constraint = new StringConstraint( val );
        ParameterList list = new ParameterList();
        list.addParameter( makeConstrainedStringParameter( constraint ) );
        list.addParameter( makeStringParameter() );
        list.addParameter( makeConstrainedStringParameter( constraint ) );
        list.addParameter( makeDoubleParameter() );
        list.addParameter( makeIntegerParameter() );
        list.addParameter( makeConstrainedIntegerParameter() );
        list.addParameter( makeConstrainedDoubleDiscreteParameter() );
        for ( int i = 3; i < number; i++ )
            list.addParameter( makeStringParameter() );
        return list;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    private ParameterAPI makeConstrainedDoubleDiscreteParameter() {
        String name = "Name " + paramCount;
        String value = "12.1";
        paramCount++;
        Vector val = new Vector();
        val.add( new Double( 11.1 ) );
        val.add( new Double( 12.1 ) );
        val.add( new Double( 13.1 ) );
        DoubleDiscreteConstraint constraint
                 = new DoubleDiscreteConstraint( val );
        DoubleDiscreteParameter param
                 = new DoubleDiscreteParameter( name, constraint, "sec.", new Double( 12.1 ) );
        return param;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    private ParameterAPI makeIntegerParameter() {
        String name = "Name " + paramCount;
        String value = "1" + paramCount;
        paramCount++;
        IntegerParameter param = new IntegerParameter( name, new Integer( value ) );
        return param;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    private ParameterAPI makeConstrainedIntegerParameter() {
        String name = "Name " + paramCount;
        String value = "1" + paramCount;
        paramCount++;
        IntegerConstraint constraint = new IntegerConstraint( -180, 180 );
        IntegerParameter param = new IntegerParameter( name, constraint, "degrees", new Integer( value ) );
        return param;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    private ParameterAPI makeDoubleParameter() {
        String name = "Name " + paramCount;
        String value = "12." + paramCount;
        paramCount++;
        DoubleConstraint constraint = new DoubleConstraint( 0.0, 20.0 );
        DoubleParameter param = new DoubleParameter( name, constraint, "acres", new Double( value ) );
        return param;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    private ParameterAPI makeStringParameter() {
        String name = "Name " + paramCount;
        String value = "Value " + paramCount;
        paramCount++;
        StringParameter param = new StringParameter( name, value );
        return param;
    }

    /**
     *  Description of the Method
     *
     * @param  constraint  Description of the Parameter
     * @return             Description of the Return Value
     */
    private ParameterAPI makeConstrainedStringParameter
            ( StringConstraint constraint ) {
        String name = "Name " + paramCount;
        String value = "Value " + paramCount;
        paramCount++;
        StringParameter param = new StringParameter( name, constraint, null, "William" );
        return param;
    }

    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void parameterChange( ParameterChangeEvent event ) {
        String S = "ParameterApplet: parameterChange(): ";
        System.out.println( S + "starting: " );
        String name1 = event.getParameterName();
        String old1 = event.getOldValue().toString();
        String str1 = event.getNewValue().toString();
        String msg = "Status: " + name1 + " changed from " + old1 + " to " + str1;
        System.out.println( msg );
        statusLabel.setText( msg );
    }

    /**
     *  Description of the Method
     */
    public void start() { }

    /**
     *  Description of the Method
     */
    public void stop() { }

    /**
     *  Description of the Method
     */
    public void destroy() { }

    /**
     *  The main program for the ParameterApplet class
     *
     * @param  args  The command line arguments
     */
    public static void main( String[] args ) {

        Double d1 = new Double(1);
        Double d2 = new Double( Double.NaN );
        Double d3 = null;

        //System.out.println("" + d1.compareTo(d3));




        ParameterApplet applet = new ParameterApplet();
        applet.isStandalone = true;
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( 3 );
        frame.setTitle( "Applet Frame" );
        frame.getContentPane().add( applet, "Center" );
        applet.init();
        applet.start();
        frame.setSize( 400, 320 );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation( ( d.width - frame.getSize().width ) / 2,
                ( d.height - frame.getSize().height ) / 2 );
        frame.setVisible( true );
    }

    /**
     *  Shown when a Constraint error is thrown on a ParameterEditor
     *
     * @param  e  Description of the Parameter
     */
    public void parameterChangeFailed( ParameterChangeFailEvent e ) {

        StringBuffer b = new StringBuffer();

        b.append( '"' );
        b.append( e.getParameterName() );
        b.append( '"' );
        b.append( " doesn't allow the value: " );
        b.append( e.getBadValue().toString() );
        b.append( ". \nChoose within constraints:\n" );
        b.append( ( ( ParameterAPI ) e.getSource() ).getConstraint().toString() );

        JOptionPane.showMessageDialog(
                this, b.toString(),
                "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
                 );

    }
}
