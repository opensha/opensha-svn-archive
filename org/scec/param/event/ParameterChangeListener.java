package org.scec.param.event;

import java.util.EventListener;

/**
 *  <b>Title:</b> ParameterChangeListener<br>
 *  <b>Description:</b> The change listener receives change events whenever one
 *  of the Parameters have been edited in the ParameterEditor. The listener is
 *  typically the Main Application that wants to do something with the changed
 *  data, such as generate a new plot.<br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 */

public interface ParameterChangeListener extends EventListener {
    /**
     *  Function that must be implemented by all Listeners for
     *  ParameterChangeEvents.
     *
     * @param  event  The Event which triggered this function call
     */
    public void parameterChange( ParameterChangeEvent event );
}
