package org.opensha.util;

import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeFailEvent;
import org.opensha.param.event.ParameterChangeFailListener;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;

public class FakeParameterListener implements ParameterChangeFailListener,
		ParameterChangeListener, ParameterChangeWarningListener {

	public void parameterChangeFailed(ParameterChangeFailEvent event) {
		// do nothing

	}

	public void parameterChange(ParameterChangeEvent event) {
		// do nothingb

	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {
		// do nothing
	}

}
