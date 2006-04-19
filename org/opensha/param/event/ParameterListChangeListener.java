package org.opensha.param.event;

import java.util.EventObject;

/**
 *  <b>Title:</b> ParameterListChangeListener <p>
 *
 *  <b>Description:</b> The parameterlist change listener receives
 *  events whenever any parameter changes leads to addtion/deletion
 *  of parameters in the parameterlist. The listener is
 *  typically an ERF that wants to do something with the changed
 *  paramterlist. <p>
 *
 * @author
 * @created
 * @version
 */

public interface ParameterListChangeListener {

  /**
   *  Function that must be implemented by all ParameterList Listeners for
   *  parameterListChangeEvents.
   *
   * @param  event  The Event which triggered this function call
   */
    public void parameterListChange( EventObject event );
}
