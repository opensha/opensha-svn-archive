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

package org.opensha.sha.calc.params;

import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.impl.StringParameter;

/**
 * This parameter allows one to choose a type of  point source distance correction
 */
public class PtSrcDistanceCorrectionParam extends StringParameter {

	private static final long serialVersionUID = 1L;
	public final static String NAME = "Pt Src Dist Corr";
	public final static String INFO = "Type of distance correction for point sources";

	// Options for constraint:
	public final static String PT_SRC_DIST_CORR_NONE = "None";
	public final static String PT_SRC_DIST_CORR_FIELD = "Field (2004)";
	public final static String PT_SRC_DIST_CORR_NSHMP08 = "NSHMP (2008)";
	StringConstraint constraint = new StringConstraint();

	/**
	 * The parameter is set as non editable after creation
	 * @param options
	 * @param defaultValue
	 */
	public PtSrcDistanceCorrectionParam() {
		super(NAME);
		StringConstraint constraint = new StringConstraint();
		constraint.addString(PT_SRC_DIST_CORR_NONE);
		constraint.addString(PT_SRC_DIST_CORR_FIELD);
		constraint.addString(PT_SRC_DIST_CORR_NSHMP08);
		setConstraint(constraint);
	    setInfo(INFO);
	    setDefaultValue(PT_SRC_DIST_CORR_NONE);
	    setNonEditable();
	}
}
