package org.scec.param.editor;
import java.awt.*;

import java.util.*;
import javax.swing.*;
import org.scec.gui.*;
import org.scec.param.ParameterAPI;

import org.scec.param.ParameterList;
import org.scec.param.event.ParameterChangeFailListener;
import org.scec.param.event.ParameterChangeListener;

/**
 *  <b>Title:</b> ParameterListEditor<br>
 *  <b>Description:</b> The main Parameter Editor Panel that takes in a
 *  ParameterList, and using the ParameterEditorFactory to build all individual
 *  Parameter Editors for each editor in the Parameter List. The list is
 *  presented in a Scroll Pane so all parameters are accessable, no matter the
 *  size of the containing application<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    April 17, 2002
 * @version    1.0
 */

public class ParameterListEditor extends LabeledBoxPanel {



    protected final static String C = "ParameterListEditor";
    protected final static boolean D = false;

    private ParameterList parameterList;

    private HashMap parameterEditors = new HashMap();
    ParameterChangeListener changeListener;
    ParameterChangeFailListener failListener;

    String[] searchPaths;


    public ParameterListEditor() {
        super();
        this.setLayout( new GridBagLayout());
    }

    /**
     *  Constructor for the ParameterListEditor object
     *
     * @param  paramList       Description of the Parameter
     * @param  changeListener  Description of the Parameter
     * @param  failListener    Description of the Parameter
     */
    public ParameterListEditor(
            ParameterList paramList,
            ParameterChangeListener changeListener,
            ParameterChangeFailListener failListener
             ) {

        super();
        parameterList = paramList;

        this.changeListener = changeListener;
        this.failListener = failListener;


        // Build package names search path
        searchPaths = new String[1];
        searchPaths[0] = ParameterListEditor.getDefaultSearchPath();

        this.setLayout( new GridBagLayout());

        addParameters();

    }

    /**
     *  Constructor for the ParameterListEditor object
     *
     * @param  paramList       Description of the Parameter
     * @param  changeListener  Description of the Parameter
     * @param  failListener    Description of the Parameter
     * @param  searchPaths     Description of the Parameter
     */
    public ParameterListEditor(
            ParameterList paramList,
            ParameterChangeListener changeListener,
            ParameterChangeFailListener failListener,
            String[] searchPaths
             ) {

        super();

        parameterList = paramList;
        this.changeListener = changeListener;
        this.failListener = failListener;
        this.searchPaths = searchPaths;

        this.setLayout( new GridBagLayout());

        addParameters();

    }

    /**
     *  Sets the parameterList attribute of the ParameterListEditor object
     *
     * @param  paramList  The new parameterList value
     */
    public void setParameterList( ParameterList paramList ) {
        parameterList = paramList;
    }


    /**
     *  Hides or shows one of the ParameterEditors in the ParameterList. setting
     *  the boolean parameter to true hides the panel, setting it to false shows
     *  the panel
     *
     * @param  parameterName  The new parameterInvisible value
     * @param  invisible      The new parameterInvisible value
     */
    public void setParameterInvisible( String parameterName, boolean invisible ) {

        parameterName = this.parameterList.getParameterName( parameterName );
        if ( parameterEditors.containsKey( parameterName ) ) {

            ParameterEditor editor = ( ParameterEditor ) parameterEditors.get( parameterName );
            editor.setVisible( invisible );
        }

    }


    public ParameterList getParameterList() {
        return parameterList;
    }

    /**
     *  Gets the defaultSearchPath attribute of the ParameterListEditor class
     *
     * @return    The defaultSearchPath value
     */
    public static String getDefaultSearchPath() {
        return ParameterEditorFactory.DEFAULT_PATH;
    }

    /**
     *  Gets the visibleParametersCloned attribute of the ParameterListEditor
     *  object
     *
     * @return    The visibleParametersCloned value
     */
    public ParameterList getVisibleParametersCloned() {

        ParameterList visibles = new ParameterList();

        Set keys = parameterEditors.keySet();
        Iterator it = keys.iterator();
        while ( it.hasNext() ) {

            Object key = it.next();
            ParameterEditor editor = ( ParameterEditor ) parameterEditors.get( key );
            if ( editor.isVisible() ) {
                ParameterAPI param = ( ParameterAPI ) editor.getParameter().clone();
                visibles.addParameter( param );
            }

        }
        return visibles;
    }


    /**
     *  Gets the parameterEditor attribute of the ParameterListEditor object
     *
     * @param  parameterName               Description of the Parameter
     * @return                             The parameterEditor value
     * @exception  NoSuchElementException  Description of the Exception
     */
    public ParameterEditor getParameterEditor( String parameterName ) throws NoSuchElementException {

        parameterName = this.parameterList.getParameterName( parameterName );
        if ( parameterEditors.containsKey( parameterName ) ) {

            ParameterEditor editor = ( ParameterEditor ) parameterEditors.get( parameterName );
            return editor;
        }
        else
            throw new NoSuchElementException( "No ParameterEditor exist named " + parameterName );

    }


    /**
     *  Description of the Method
     */
    public void synchToModel() {

        Set keys = parameterEditors.keySet();
        Iterator it = keys.iterator();
        while ( it.hasNext() ) {
            Object key = it.next();
            ParameterEditorAPI editor = ( ParameterEditorAPI ) parameterEditors.get( key );
            editor.synchToModel();
        }
    }

    /**
     *  Description of the Method
     *
     * @param  parameterName  Description of the Parameter
     * @param  param          Description of the Parameter
     */
    public void replaceParameterForEditor( String parameterName, ParameterAPI param ) {

        parameterName = this.parameterList.getParameterName( parameterName );
        if ( parameterEditors.containsKey( parameterName ) ) {

            ParameterEditor editor = ( ParameterEditor ) parameterEditors.get( parameterName );
            editor.setParameter( param );
            editor.addParameterChangeListener( changeListener );
            editor.addParameterChangeFailListener( failListener );

            parameterList.removeParameter( parameterName );
            parameterList.addParameter( param );

        }

    }


    /**
     *  Adds a feature to the Parameters attribute of the ParameterListEditor
     *  object
     */
    private void addParameters() {

        if ( parameterList == null )
            return;

        ListIterator it = parameterList.getParameterNamesIterator();
        int counter = 0;
        //boolean first = true;

        // Set additional search paths for non-standard editors
        if ( ( searchPaths != null ) || ( searchPaths.length > 0 ) )
            ParameterEditorFactory.setSearchPaths( this.searchPaths );

        parameterEditors.clear();
        while ( it.hasNext() ) {

            Object obj1 = it.next();
            String name = ( String ) obj1;

            ParameterAPI param = parameterList.getParameter( name );

            // if(obj instanceof ParameterAPI){
            //ParameterAPI param = (ParameterAPI)obj;
            ParameterEditor panel = ParameterEditorFactory.getEditor( param );
            panel.addParameterChangeListener( changeListener );
            panel.addParameterChangeFailListener( failListener );

            parameterEditors.put( param.getName(), panel );

            // if ( first ) {
                // first = false;
                // SidesBorder border = new SidesBorder( Color.white, Color.white, Color.white, Color.white );
                // panel.setBorder( border );
            // }
            editorPanel.add( panel, new GridBagConstraints( 0, counter, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
            counter++;
            //}
        }

    }


}
