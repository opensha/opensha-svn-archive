package org.scec.param.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.scec.exceptions.*;
import org.scec.gui.*;
import org.scec.param.*;
import org.scec.param.event.*;

// Fix - Needs more comments

/**
 *  <b>Title:</b> ParameterEditor<p>
 *
 *  <b>Description:</b> This is the base Editor class that all Editors extend
 *  from This sets up a JPanel to have a Label on the left, cooresponding to the
 *  name of the Parameter, and a widget on the right. What the widget is depends
 *  on the subclass. It can be as simple as a JTextField, or a JComboList of
 *  selectable values cooresponding to the Parameter Constraints. All common
 *  functionality of the Editors are in this class. The only thing that
 *  subclasses have to deal with is setting the specific widget for the
 *  subclass, and how to handle key and focus events on this widget.<p>
 *
 * @author     Steven W. Rock
 * @created    April 17, 2002
 * @version    1.0
 */

public class ParameterEditor
         extends JPanel
         implements
        ParameterEditorAPI,
        FocusListener,
        KeyListener {

    /** Class name for debugging. */
    protected final static String C = "ParameterEditor";
    /** If true print out debug statements. */
    protected final static boolean D = false;


    protected final static String DATA_TEXT = "Enter data here";
    protected final static String LABEL_TEXT = "This is the Label";
    protected final static String EMPTY = "";
    protected static Color BACK_COLOR = Color.white;
    protected static Color FORE_COLOR = new Color( 80, 80, 140 );
    protected static Color STRING_BACK_COLOR = Color.lightGray;
    protected final static Dimension LABEL_DIM = new Dimension( 100, 14 );
    protected final static Dimension LABEL_PANEL_DIM = new Dimension( 100, 15 );
    protected final static Dimension WIGET_PANEL_DIM = new Dimension( 100, 19 );
    protected final static Dimension JCOMBO_DIM = new Dimension( 100, 18 );
    protected final static GridBagLayout GBL = new GridBagLayout();
    protected final static Insets ZERO_INSETS = new Insets( 0, 0, 0, 0 );
    protected final static Insets FIVE_INSETS = new Insets( 0, 5, 0, 0 );
    protected final static Insets FIVE_FIVE_INSETS = new Insets( 0, 5, 0, 5 );
    protected static Font JCOMBO_FONT = new Font( "SansSerif", 0, 10 );
    public static Font DEFAULT_LABEL_FONT = new Font( "SansSerif", Font.BOLD, 11 );
    public static Font DEFAULT_FONT = new Font( "SansSerif", Font.PLAIN, 10 );
    protected final static Border BORDER = new SidesBorder( BACK_COLOR, BACK_COLOR, BACK_COLOR, BACK_COLOR );
    protected final static Border CONST_BORDER = BorderFactory.createLineBorder( Color.blue, 1 );
    protected final static Border FOCUS_BORDER = BorderFactory.createLineBorder( Color.orange, 1 );
    protected final static Border ETCHED = BorderFactory.createEtchedBorder();

    protected final static GridBagConstraints NAME_LABEL_GBC = new GridBagConstraints(
            0, 0, 1, 1, 1.0, 0.0, 10, 2, new Insets( 1, 5, 0, 0 ), 0, 0 );

    protected final static GridBagConstraints LABLE_PANEL_GBC = new GridBagConstraints(
            0, 0, 1, 1, 0.0, 0.0, 10, 2, ZERO_INSETS, 0, 0 );

    protected final static GridBagConstraints OUTER_PANEL_GBC = new GridBagConstraints(
            0, 0, 1, 1, 1.0, 1.0, 10, 1, new Insets( 1, 0, 0, 0 ), 0, 0 );

    protected final static GridBagConstraints WIDGET_PANEL_GBC = new GridBagConstraints(
            0, 1, 1, 1, 1.0, 0.0, 10, 2, ZERO_INSETS, 0, 0 );

    protected final static GridBagConstraints WIDGET_GBC = new GridBagConstraints(
            0, 0, 1, 1, 1.0, 0.0, 10, 2, new Insets( 1, 3, 0, 1 ), 0, 0 );

    protected ParameterAPI model;

    protected Vector changeListeners = new Vector();
    protected Vector failListeners = new Vector();

    protected JPanel outerPanel = new JPanel();
    protected JPanel labelPanel = new JPanel();
    protected JPanel widgetPanel = new JPanel();
    protected JLabel nameLabel = new JLabel();
    protected JComponent valueEditor = null;
    protected boolean focusEnabled = true;
    TitledBorder titledBorder1;
    Border border1;


    /**
     * Flag whether to catch errors when constraint error thrown. Resets
     * value to last value before setting with new value
     */
    boolean catchConstraint = false;


    /**
     * Flag to indicate this widget is processing a key typed event
     */
    protected boolean keyTypeProcessing = false;

    /**
     * Flag to indicate that this widget is processing a focus lost event
     */
    protected boolean focusLostProcessing = false;


    /**
     *  Constructor for the ParameterEditor object
     */
    public ParameterEditor() {

        String S = C + ": Constructor(): ";
        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }

    }

    /**
     *  Constructor for the ParameterEditor object
     *
     * @param  model  Description of the Parameter
     */
    public ParameterEditor( ParameterAPI model ) {

        String S = C + ": Constructor(model): ";
        if ( model == null ) throw new NullPointerException( S + "Input Parameter data cannot be null" );

        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }

        setParameter( model );

    }

    /**
     *  Needs to be called by subclasses when editable widget field changed
     *
     * @param  value                    The new value value
     * @exception  ConstraintException  Description of the Exception
     */
    public void setValue( Object value ) throws ConstraintException {

        String S = C + ": setValue():";
        if(D) System.out.println(S + "Starting");

        if ( ( model != null ) && ( value == null ) ) {
            if( model.isNullAllowed() ){
                try{
                    model.setValue( value );

                    org.scec.param.event.ParameterChangeEvent event = new org.scec.param.event.ParameterChangeEvent(
                        model, model.getName(),
                        model.getValue(), value
                    );

                    firePropertyChange( event );
                }
                catch(ParameterException ee){

                    System.out.println(S + ee.toString());

                }
            }
            else return;
        }
        else if ( model != null  ) {

            Object obj = model.getValue();
            if ( D ) System.out.println( S + "Old Value = " + obj.toString() );
            if ( D ) System.out.println( S + "Model's New Value = " + value.toString() );


            if (  ( obj == null )  ||   ( !obj.toString().equals( value.toString() ) )  ) {


                try{
                    model.setValue( value );

                    org.scec.param.event.ParameterChangeEvent event = new org.scec.param.event.ParameterChangeEvent(
                        model, model.getName(),
                        model.getValue(), value
                    );

                    firePropertyChange( event );
                }
                catch(ParameterException ee){

                    System.out.println(S + ee.toString());

                }
            }
        }

        if(D) System.out.println(S + "Ending");

    }


    /**
     *  This function sets the name and the value of this editor. It attempts to
     *  use the constraint name if found, else uses the parameter name
     *
     * @param  model
     */
    public void setParameter( ParameterAPI model ) {

        String S = C + ": setParameter(): ";
        if ( model == null )
            throw new NullPointerException( S + "Input Parameter data cannot be null" );
        else
            this.model = model;

        String name = "";
        name = model.getName();
        Object value = model.getValue();

        removeWidget();
        addWidget();

        setWidgetObject( name, value );

    }

    /**
     *  Sets the asText attribute of the ParameterEditor object
     *
     * @param  string                        The new asText value
     * @exception  IllegalArgumentException  Description of the Exception
     */
    public void setAsText( String string ) throws IllegalArgumentException { }

    /**
     *  Sets the widgetObject attribute of the ParameterEditor object
     *
     * @param  name  The new widgetObject value
     * @param  obj   The new widgetObject value
     */
    protected void setWidgetObject( String name, Object obj ) {
        updateNameLabel( name );
        setNameLabelToolTip( model.getInfo() );
    }

    /**
     *  Sets the nameLabelToolTip attribute of the ParameterEditor object
     *
     * @param  str  The new nameLabelToolTip value
     */
    protected void setNameLabelToolTip( String str ) {

        if ( ( str != null ) && !( str.equals( EMPTY ) ) ) {
            if( nameLabel != null) nameLabel.setToolTipText( str );
            this.setToolTipText(str);
        }
        else {
            if( nameLabel != null) nameLabel.setToolTipText( null );
            this.setToolTipText(null);
        }
    }

    /**
     *  Sets the focusEnabled attribute of the ParameterEditor object
     *
     * @param  newFocusEnabled  The new focusEnabled value
     */
    public void setFocusEnabled( boolean newFocusEnabled ) {
        focusEnabled = newFocusEnabled;
    }



    /**
     *  Gets the value attribute of the ParameterEditor object
     *
     * @return    The value value
     */
    public Object getValue() {
        return model.getValue();
    }

    /**
     *  Gets the parameter attribute of the ParameterEditor object
     *
     * @return    The parameter value
     */
    public ParameterAPI getParameter() {
        return model;
    }

    /**
     *  Gets the tags attribute of the ParameterEditor object
     *
     * @return    The tags value
     */
    public String[] getTags() {
        return null;
    }

    /**
     *  Gets the asText attribute of the ParameterEditor object
     *
     * @return    The asText value
     */
    public String getAsText() {
        return getValue().toString();
    }

    /**
     *  Gets the focusEnabled attribute of the ParameterEditor object
     *
     * @return    The focusEnabled value
     */
    public boolean isFocusEnabled() {
        return focusEnabled;
    }


    /**
     * Implemented in subclasses. Called if the model changes seperatly from the
     * GUI, such as with the ParameterWarningListener.e
     */
    public void synchToModel() { }

    /**
     *  Needs to be called by subclasses when editable widget field change fails
     *  due to constraint problems
     *
     * @param  value                    Description of the Parameter
     * @exception  ConstraintException  Description of the Exception
     */
    public void unableToSetValue( Object value ) throws ConstraintException {

        String S = C + ": unableToSetValue():";
        // if(D) System.out.println(S + "New Value = " + value.toString());

        if ( ( value != null ) && ( model != null ) ) {
            Object obj = model.getValue();
            if ( D ) System.out.println( S + "Old Value = " + obj.toString() );

            if ( !obj.toString().equals( value.toString() ) ) {
                org.scec.param.event.ParameterChangeFailEvent event = new org.scec.param.event.ParameterChangeFailEvent(
                        model,
                        model.getName(),
                        model.getValue(),
                        value
                         );

                firePropertyChangeFailed( event );
            }
        }
    }

    /**
     *  Adds a feature to the ParameterChangeFailListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the
     *      ParameterChangeFailListener attribute
     */
    public synchronized void addParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener ) {
        if ( failListeners == null ) failListeners = new Vector();
        if ( !failListeners.contains( listener ) ) failListeners.addElement( listener );
    }

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeFailListener( org.scec.param.event.ParameterChangeFailListener listener ) {
        if ( failListeners != null && failListeners.contains( listener ) )
            failListeners.removeElement( listener );
    }

    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void firePropertyChangeFailed( org.scec.param.event.ParameterChangeFailEvent event ) {

        String S = C + ": firePropertyChange(): ";
        if ( D ) System.out.println( S + "Firing failed change event for parameter = " + event.getParameterName() );
        if ( D ) System.out.println( S + "Old Value = " + event.getOldValue() );
        if ( D ) System.out.println( S + "Bad Value = " + event.getBadValue() );
        if ( D ) System.out.println( S + "Model Value = " + event.getSource().toString() );

        Vector vector;
        synchronized ( this ) {
            if ( failListeners == null ) return;
            vector = ( Vector ) failListeners.clone();
        }

        for ( int i = 0; i < vector.size(); i++ ) {
            org.scec.param.event.ParameterChangeFailListener listener = ( org.scec.param.event.ParameterChangeFailListener ) vector.elementAt( i );
            listener.parameterChangeFailed( event );
        }

    }

    /**
     *  Description of the Method
     *
     * @param  label  Description of the Parameter
     * @return        Description of the Return Value
     */
    public static JLabel makeConstantEditor( String label ) {

        JLabel l = new JLabel();
        l.setPreferredSize( LABEL_DIM );
        l.setMinimumSize( LABEL_DIM );
        l.setFont( JCOMBO_FONT );
        l.setForeground( Color.blue );
        l.setBorder( CONST_BORDER );
        l.setText( label );
        return l;
    }

    /**
     *  Adds a feature to the ParameterChangeListener attribute of the
     *  ParameterEditor object
     *
     * @param  listener  The feature to be added to the ParameterChangeListener
     *      attribute
     */
    public synchronized void addParameterChangeListener( org.scec.param.event.ParameterChangeListener listener ) {
        if ( changeListeners == null ) changeListeners = new Vector();
        if ( !changeListeners.contains( listener ) ) changeListeners.addElement( listener );
    }

    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public synchronized void removeParameterChangeListener( org.scec.param.event.ParameterChangeListener listener ) {
        if ( changeListeners != null && changeListeners.contains( listener ) )
            changeListeners.removeElement( listener );
    }

    /**
     *  Description of the Method
     *
     * @param  event  Description of the Parameter
     */
    public void firePropertyChange( ParameterChangeEvent event ) {

        String S = C + ": firePropertyChange(): ";
        if ( D ) System.out.println( S + "Firing change event for parameter = " + event.getParameterName() );
        if ( D ) System.out.println( S + "Old Value = " + event.getOldValue() );
        if ( D ) System.out.println( S + "New Value = " + event.getNewValue() );
        if ( D ) System.out.println( S + "Model Value = " + event.getSource().toString() );

        Vector vector;
        synchronized ( this ) {
            if ( changeListeners == null ) return;
            vector = ( Vector ) changeListeners.clone();
        }

        for ( int i = 0; i < vector.size(); i++ ) {
            org.scec.param.event.ParameterChangeListener listener = ( org.scec.param.event.ParameterChangeListener ) vector.elementAt( i );
            listener.parameterChange( event );
        }

    }

    /**
     *  Description of the Method
     *
     * @exception  Exception  Description of the Exception
     */
    protected void jbInit() throws Exception {

        // Main component
        titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(FORE_COLOR,1),"");
        titledBorder1.setTitleColor(FORE_COLOR);
        titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
        titledBorder1.setTitle("Parameter Name");
        //titledBorder1.setTitleFont(DEFAULT_FONT);
        border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));


        this.setBorder( BORDER );
        this.setBackground( BACK_COLOR );
        this.setLayout( GBL );
        // this.setMaximumSize(new Dimension(2147483647, 51));

        // Outermost panel
        outerPanel.setLayout( GBL );
        outerPanel.setBackground( BACK_COLOR );
        outerPanel.setBorder(border1);

        // widgetPanel panel init
        widgetPanel.setBackground( BACK_COLOR );
        widgetPanel.setLayout( GBL );
        widgetPanel.setMinimumSize( WIGET_PANEL_DIM );
        widgetPanel.setPreferredSize( WIGET_PANEL_DIM );

        // lable panel init
        // labelPanel.setBackground( BACK_COLOR );
        // labelPanel.setLayout( GBL );


        // labelPanel.setPreferredSize( WIGET_PANEL_DIM );
        // labelPanel.setMinimumSize( WIGET_PANEL_DIM );

        //labelPanel.setPreferredSize( LABEL_PANEL_DIM );
        //labelPanel.setMinimumSize( LABEL_PANEL_DIM );
        //labelPanel.setMaximumSize(new Dimension(12320981, 16));

        // nameLabel panel init
        nameLabel.setBackground( BACK_COLOR );
        nameLabel.setMaximumSize( LABEL_DIM );
        nameLabel.setMinimumSize( LABEL_DIM );
        nameLabel.setPreferredSize( LABEL_DIM );
        nameLabel.setHorizontalAlignment( SwingConstants.LEFT );
        nameLabel.setHorizontalTextPosition( SwingConstants.LEFT );
        nameLabel.setText( LABEL_TEXT );
        nameLabel.setFont( DEFAULT_LABEL_FONT );

        if ( FORE_COLOR != null )
            nameLabel.setForeground( FORE_COLOR );

        // Build gui layout
        //labelPanel.add( nameLabel, NAME_LABEL_GBC );
        //outerPanel.add( labelPanel, LABLE_PANEL_GBC );
        outerPanel.add( widgetPanel, WIDGET_PANEL_GBC );


        this.add( outerPanel, OUTER_PANEL_GBC );

    }

    /**
     *  Description of the Method
     */
    protected void removeWidget() {
        String S = C + ": addWidget(): ";
        if ( widgetPanel != null && valueEditor != null )
            widgetPanel.remove( valueEditor );
        valueEditor = null;
    }

    /**
     *  Adds a feature to the Widget attribute of the ParameterEditor object
     */
    protected void addWidget() {

        valueEditor = new JTextField();
        valueEditor.setBackground( Color.white );
        valueEditor.setMinimumSize( LABEL_DIM );
        valueEditor.setPreferredSize( LABEL_DIM );

        ( ( JTextField ) valueEditor ).setText( DATA_TEXT );
        widgetPanel.add( valueEditor, WIDGET_GBC );

    }

    public void setWidgetBorder(Border b){
        //((JTextField)valueEditor).setBorder(b);
    }

    /**
     *  Description of the Method
     *
     * @param  label  Description of the Parameter
     */
    protected void updateNameLabel( String label ) {

        String units = model.getUnits();

        if ( ( units != null ) && !( units.equals( "" ) ) ){
            label += " (" + units + "):";
            titledBorder1.setTitle(label);
            //nameLabel.setText( label );
        }
        else{
            label += ':';
            titledBorder1.setTitle(label);
            //nameLabel.setText( label );
        }
    }

    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void focusGained( FocusEvent e ) {
    }

    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void focusLost( FocusEvent e ) {
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void keyTyped( KeyEvent e ) { }

    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void keyPressed( KeyEvent e ) { }

    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void keyReleased( KeyEvent e ) { }

}
