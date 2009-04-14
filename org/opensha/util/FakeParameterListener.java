package org.opensha.util;

import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeFailEvent;
import org.opensha.param.event.ParameterChangeFailListener;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;

/**
 * This is simply a class that implements all of the ParameterChangeListeners, without 
 * actually doing anything. It is most useful when instantiating AttenuationRelationship
 * objects when you don't care about about ParameterChange events, especially for quick
 * tests.
 * 
 * @author kevin
 *
 */
public class FakeParameterListener implements ParameterChangeFailListener,
		ParameterChangeListener, ParameterChangeWarningListener {

	public void parameterChangeFailed(ParameterChangeFailEvent event) {
		// do nothing

	}

	public void parameterChange(ParameterChangeEvent event) {
		// do nothing

	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {
		// do nothing
	}

}
