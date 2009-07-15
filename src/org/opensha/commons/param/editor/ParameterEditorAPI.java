package org.opensha.commons.param.editor;

import java.awt.Component;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeFailEvent;
import org.opensha.commons.param.event.ParameterChangeFailListener;
import org.opensha.commons.param.event.ParameterChangeListener;

/**
 * <b>Title:</b> ParameterEditorAPI<p>
 *
 * <b>Description:</b> Common interface functions that all implementing
 * ParameterEditors must implement so that they can be plugged transparently
 * into GUI frameworks. <p>
 *
 * This allows classes that use the ParameterEditors to deal with any
 * Editor type equally. Using this interface they all look the same. This
 * permits new editors to be added to the framework without changing the
 * using classes. <p>
 *
 * Note that all editors edit a Parameter. Internally they maintain a reference
 * to the particular parameter type they know how to handle. <p>
 *
 * @author     Steven W. Rock
 * @created    April 17, 2002
 * @version    1.0
 */

public interface ParameterEditorAPI {

    /** Set the value of the Parameter this editor is editing. */
    public void setValue( Object object );

    /** Returns the value of the parameter object.  */
    public Object getValue();

    /**
     * Needs to be called by subclasses when editable widget field change fails
     * due to constraint problems. Allows rollback to the previous good value.
     */
    public void unableToSetValue( Object object );

    /**
     * Called when the parameter has changed independently from
     * the editor. This function needs to be called to to update
     * the GUI component ( text field, picklsit, etc. ) with
     * the new parameter value.
     */
    public void refreshParamEditor();

    /** Returns the value of the parameer as a String, regardless of it's true data type */
    public String getAsText();

    /**
     * Set the value of the parameer as a String, regardless of it's true data type .
     * Internally the string is converted to the correct data type if possible.
     */
    public void setAsText( String string ) throws IllegalArgumentException;

    /** Not sure what this is used for. */
    public String[] getTags();

    /** Sets the parameter that is stored internally for this GUI widget to edit */
    public ParameterAPI getParameter();

    /** Returns the parameter that is stored internally that this GUI widget is editing */
    public void setParameter( ParameterAPI model );


    /** Sets the focusEnabled boolean indicating this is the GUI componet with the current focus */
    public void setFocusEnabled( boolean newFocusEnabled );

    /** Returns the focusEnabled boolean indicating this is the GUI componet with the current focus */
    public boolean isFocusEnabled();

}

