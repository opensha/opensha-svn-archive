package org.opensha.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * <b>Title:</b> LabeledBoxPanel<p>
 *
 * <b>Description:</b> GUI Widget that contains a Header panel with
 * a title lable and an editor panel withing a scroll pane. Is the
 * base class for the ParameterListEditor. This is a generic
 * component so it was useful to pull this functionality out of
 * the ParameterListEditor and make that a subclass. <p>
 *
 * The main use is to add any component to the editor panel, and
 * the results will be scrollable. See the add() functions below.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class LabeledBoxPanel extends JPanel{

    protected final static String C = "LabeledBoxPanel";
    protected final static boolean D = false;

    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JPanel editorPanel = new JPanel();
    protected JPanel headerPanel = new JPanel();
    protected JLabel headerLabel = new JLabel();

    protected SidesBorder border = null;
    protected SidesBorder border2 = null;

    protected static GridBagLayout GBL = new GridBagLayout();

    protected String title;
    protected Color borderColor = new Color( 80, 80, 133 );
    protected Color headerPanelBackgroundColor = new Color( 200, 200, 255 );

    protected boolean addDefault = true;


    /**
     * Creates a new JPanel with the specified layout manager and buffering
     * strategy.
     *
     * @param layout  the LayoutManager to use
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free
     *        updates
     */
    public LabeledBoxPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }
        if( editorPanel != null ) editorPanel.setLayout(layout);
    }

    /**
     * Create a new buffered JPanel with the specified layout manager
     *
     * @param layout  the LayoutManager to use
     */
    public LabeledBoxPanel(LayoutManager layout) {
        super(layout);
        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }
        if( editorPanel != null ) editorPanel.setLayout(layout);
    }

    /**
     * Creates a new <code>JPanel</code> with <code>FlowLayout</code>
     * and the specified buffering strategy.
     * If <code>isDoubleBuffered</code> is true, the <code>JPanel</code>
     * will use a double buffer.
     *
     * @param layout  the LayoutManager to use
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free
     *        updates
     */
    public LabeledBoxPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }
        if( editorPanel != null ) editorPanel.setLayout(new FlowLayout());
    }

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public LabeledBoxPanel() {
        super();
        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }

        if( editorPanel != null ) editorPanel.setLayout(new FlowLayout());
    }

    /**
     * Sets the layout manager for this container.
     * @param mgr the specified layout manager
     * @see #doLayout
     * @see #getLayout
     */
    public void setLayout(LayoutManager mgr) {
        if( addDefault ) super.setLayout(mgr);
        else if( editorPanel != null ) editorPanel.setLayout(mgr);

    }

    /**
     *  Sets the title in this boxPanel
     *
     * @param  newTitle  The new title value
     */
    public void setTitle( String newTitle ) {
        title = newTitle;
        headerLabel.setText( title );
    }

    /**
     *  Sets the borderColor for the borders in this boxPanel
     *
     * @param  newBorderColor  The new borderColor value
     */
    public void setBorderColor( Color newBorderColor ) {
        borderColor = newBorderColor;
        if( border != null ) {
            border.setBottomColor( borderColor );
            border.setLeftColor( borderColor );
            border.setRightColor( borderColor );
            border.setTopColor( borderColor );
        }
        if( border2 != null ) {
            border2.setBottomColor( borderColor );
            border2.setLeftColor( borderColor );
            border2.setRightColor( borderColor );
        }
    }

    public void setHeaderPanelBackgroundColor( Color background ){
        headerPanelBackgroundColor = background;
        if ( headerPanel != null ) headerPanel.setBackground( headerPanelBackgroundColor );
    }

    public Color getHeaderPanelBackgroundColor(  ){
        return headerPanelBackgroundColor;
    }

    /**
     *  Gets the title in this boxPanel
     *
     * @return    The title value
     */
    public String getTitle() { return title; }

    /**
     *  Gets the borderColor of this boxPanel
     *
     * @return    The borderColor value
     */
    public Color getBorderColor() { return borderColor; }


    /**
     * Initializes the GUI components and layout
     * @throws Exception
     */
    protected void jbInit() throws Exception {

        addDefault = true;

        border = new SidesBorder( borderColor, borderColor, borderColor, borderColor );
        border2 = new SidesBorder( Color.white, borderColor, borderColor, borderColor );


        //this.setBackground( Color.white );
        this.setBorder( border2 );
        this.setLayout( GBL );

        editorPanel.setLayout( GBL );

        headerPanel.setLayout( GBL );
        headerLabel.setFont( new java.awt.Font( "SansSerif", 1, 12 ) );
        headerLabel.setForeground( borderColor );
        headerLabel.setText( "Title" );
        headerPanel.setBackground( headerPanelBackgroundColor );
        headerPanel.setBorder( border );

        //editorPanel.setBackground( Color.white );
        jScrollPane1.setBorder( null );

        add( jScrollPane1, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets( 5, 7, 3, 8 ), 0, 0 ) );
        add( headerPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

        headerPanel.add( headerLabel,
                new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, 10, 0,
                new Insets( 1, 1, 0, 1 ), 0, 0 ) );

        jScrollPane1.getViewport().add( editorPanel, ( Object ) null );
        addDefault = false;
    }


    public void addPanel(JPanel panel){

        // editorPanel.add( panel, new GridBagConstraints( 0, counter, 1, 1, 1.0, 0.0
//                    /, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    }


    /**
     * Appends the specified component to the end of this container.
     * This is a convenience method for {@link #addImpl}.
     *
     * @param     comp   the component to be added
     * @see #addImpl
     * @return    the component argument
     */
    public Component add(Component comp) {
        if( addDefault ) return super.add(comp);
        else return editorPanel.add(comp);
    }

    /**
     * Adds the specified component to this container.
     * This is a convenience method for {@link #addImpl}.
     * <p>
     * This method is obsolete as of 1.1.  Please use the
     * method <code>add(Component, Object)</code> instead.
     */
    public Component add(String name, Component comp) {
        if( addDefault ) return super.add(name, comp);
        else return editorPanel.add(name, comp);
    }

    /**
     * Adds the specified component to this container at the given
     * position.
     * This is a convenience method for {@link #addImpl}.
     *
     * @param     comp   the component to be added
     * @param     index    the position at which to insert the component,
     *                   or <code>-1</code> to append the component to the end
     * @return    the component <code>comp</code>
     * @see #addImpl
     * @see	  #remove
     */
    public Component add(Component comp, int index) {
        if( addDefault ) return super.add(comp, index);
        else return editorPanel.add(comp, index);
    }


    /**
     * Adds the specified component to the end of this container.
     * Also notifies the layout manager to add the component to
     * this container's layout using the specified constraints object.
     * This is a convenience method for {@link #addImpl}.
     *
     * @param     comp the component to be added
     * @param     constraints an object expressing
     *                  layout contraints for this component
     * @see #addImpl
     * @see       LayoutManager
     * @since     JDK1.1
     */
    public void add(Component comp, Object constraints) {
        if( addDefault ) super.add(comp, constraints);
        else editorPanel.add(comp, constraints); }


    /**
     * Adds the specified component to this container with the specified
     * constraints at the specified index.  Also notifies the layout
     * manager to add the component to the this container's layout using
     * the specified constraints object.
     * This is a convenience method for {@link #addImpl}.
     *
     * @param comp the component to be added
     * @param constraints an object expressing layout contraints for this
     * @param index the position in the container's list at which to insert
     * the component. -1 means insert at the end.
     * component
     * @see #addImpl
     * @see #remove
     * @see LayoutManager
     */
    public void add(Component comp, Object constraints, int index) {
        if( addDefault ) super.add(comp, constraints, index);
        else editorPanel.add(comp, constraints, index);
    }


}
