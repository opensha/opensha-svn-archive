package org.opensha.param.event;

import java.util.*;

/**
 * <p>Title: ParameterAndTimeSpanChangeListener </p>
 *
 * <p>Description: All class that need to listen to the Parameter change and
 * Time span change must implement this interface to receive the change events.
 * </p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public interface ParameterAndTimeSpanChangeListener
    extends EventListener {

  public void parameterOrTimeSpanChange(EventObject obj);

}
