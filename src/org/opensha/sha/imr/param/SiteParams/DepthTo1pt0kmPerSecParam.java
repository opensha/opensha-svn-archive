/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.imr.param.SiteParams;

import org.opensha.commons.param.constraint.impl.DoubleConstraint;
import org.opensha.commons.param.impl.WarningDoubleParameter;

/**
 * Depth 1.0 km/sec Parameter, reserved for representing the depth to where
 * shear-wave velocity = 1.0 km/sec.
 * See constructors for info on editability and default values.
 */
public class DepthTo1pt0kmPerSecParam extends WarningDoubleParameter {


	public final static String NAME = "Depth 1.0 km/sec";
	public final static String UNITS = "m";
	public final static String INFO = "The depth to where shear-wave velocity = 1.0 km/sec";
	public final static Double MIN = new Double(0.0);
	public final static Double MAX = new Double(30000.0);


	/**
	 * This constructor sets the default as given, and leaves the param editable 
	 * so the warning constraint can be added later. Sets the primary constraint
	 * to allow <code>null</code>, or not.
	 * @param defaultDepth
	 * @param allowsNull 
	 */
	public DepthTo1pt0kmPerSecParam(double defaultDepth, boolean allowsNull) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNullAllowed(allowsNull);
		getConstraint().setNonEditable();
		setInfo(INFO);
		setDefaultValue(defaultDepth);
	}

	/**
	 * This constructor sets the default as 100, and leaves the param editable 
	 * so the warning constraint can be added later. Parameter configured via
	 * this constructor does allows <code>null</code> values.
	 */
	public DepthTo1pt0kmPerSecParam() {this(100.0, true);}

	/**
	 * This uses the given default and warning-constraint limits, and sets 
	 * everything as non-editable.
	 * @param defaultDepth
	 * @param warnMin
	 * @param warnMax
	 * @param allowsNull 
	 */
	public DepthTo1pt0kmPerSecParam(
			double defaultDepth, double warnMin, double warnMax, boolean allowsNull) {
		super(NAME, new DoubleConstraint(MIN, MAX), UNITS);
		getConstraint().setNullAllowed(allowsNull);
		getConstraint().setNonEditable();
		setInfo(INFO);
		setDefaultValue(defaultDepth);
		DoubleConstraint warn = new DoubleConstraint(warnMin, warnMax);
		setWarningConstraint(warn);
		warn.setNonEditable();
		setNonEditable();
	}
	
	/**
	 * This sets default as 100, uses the given warning-constraint limits, and sets 
	 * everything as non-editable. Parameter configured via
	 * this constructor allows <code>null</code> values.
	 * @param warnMin
	 * @param warnMax
	 */
	public DepthTo1pt0kmPerSecParam(double warnMin, double warnMax) {
		this(100.0,warnMin,warnMax, true);
	}
}
