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

package scratch.UCERF3.erf.ETAS.ETAS_Params;

import org.opensha.commons.param.impl.DoubleParameter;

/**
 * This k_ProductivityParam is used for setting the productivity 
 * parameter of the ETAS model.
 * The definition and values are based on Hardebeck 
 * (2013; http://pubs.usgs.gov/of/2013/1165/pdf/ofr2013-1165_appendixS.pdf).
 */
public class ETAS_ProductivityParam_k extends DoubleParameter {
	
	private static final long serialVersionUID = 1L;
	
	public final static String NAME = "k - ETAS Productivity";
	public final static String INFO = "The ETAS productivity parameter";
	private static final String UNITS = "(years)^(p-1)";
	protected final static Double MIN = new Double(3.79E-4);
	protected final static Double MAX = new Double(4.97E-3);
	

	/**
	 * This sets the default value as given.
	 */
	public ETAS_ProductivityParam_k(double defaultValue) {
		super(NAME, MIN, MAX, UNITS);
		getConstraint().setNonEditable();
	    setInfo(INFO);
	    setDefaultValue(defaultValue);
	    setValueAsDefault();
	}

	/**
	 * This sets the default value as 0.
	 */
	public ETAS_ProductivityParam_k() { this(2.84E-03);}
	
	
}
