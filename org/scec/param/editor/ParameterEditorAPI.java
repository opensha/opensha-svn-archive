package org.scec.param.editor;

import java.beans.PropertyChangeListener;
import org.scec.param.ParameterAPI;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeFailEvent;
import org.scec.param.event.ParameterChangeFailListener;
import org.scec.param.event.ParameterChangeListener;

// Fix - Needs more comments

/**
 *  <b>Title:</b> ParameterEditorAPI<p>
 *
 *  <b>Description:</b> All Parameter Editors must implement these functions so
 *  that the Parameter Editor knows how to handle them without knowing the exact
 *  Editor class it is dealing with.<p>
 *
 * @author     Steven W. Rock
 * @created    April 17, 2002
 * @version    1.0
 */

public interface ParameterEditorAPI {

    /**
     *  Sets the value attribute of the ParameterEditorAPI object
     *
     * @param  object  The new value value
     */
    public void setValue( Object object );

    /**
     *  Gets the value attribute of the ParameterEditorAPI object
     *
     * @return    The value value
     */
    public Object getValue();

    /**
     *  Description of the Method
     *
     * @param  object  Description of the Parameter
     */
    public void unableToSetValue( Object object );

    /**
     *  Description of the Method
     */
    public void synchToModel();

    /**
     *  Gets the asText attribute of the ParameterEditorAPI object
     *
     * @return    The asText value
     */
    public String getAsText();

    /**
     *  Sets the asText attribute of the ParameterEditorAPI object
     *
     * @param  string                        The new asText value
     * @exception  IllegalArgumentException  Description of the Exception
     */
    public void setAsText( String string ) throws IllegalArgumentException;

    /**
     *  Gets the tags attribute of the ParameterEditorAPI object
     *
     * @return    The tags value
     */
    public String[] getTags();

    /**
     *  Gets the parameter attribute of the ParameterEditorAPI object
     *
     * @return    The parameter value
     */
    public ParameterAPI getParameter();

    /**
     *  Sets the parameter attribute of the ParameterEditorAPI object
     *
     * @param  model  The new parameter value
     */
    public void setParameter( ParameterAPI model );


    /**
     *  Sets the focusEnabled attribute of the ParameterEditorAPI object
     *
     * @param  newFocusEnabled  The new focusEnabled value
     */
    public void setFocusEnabled( boolean newFocusEnabled );

    /**
     *  Gets the focusEnabled attribute of the ParameterEditorAPI object
     *
     * @return    The focusEnabled value
     */
    public boolean isFocusEnabled();

}

