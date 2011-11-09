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

package org.opensha.sha.earthquake;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.dom4j.Element;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.geo.Region;
import org.opensha.commons.metadata.MetadataLoader;
import org.opensha.commons.metadata.XMLSaveable;
import org.opensha.commons.param.AbstractParameter;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.event.TimeSpanChangeListener;
import org.opensha.sha.util.TectonicRegionType;

/**
 * Class provides a basic implementation of an Earthquake RUpture FOrecast
 * (ERF).
 * 
 * @author Ned Field
 * @version $Id$
 */
public abstract class AbstractERF implements
		ERF,
		TimeSpanChangeListener,
		ParameterChangeListener,
		XMLSaveable {

	private static final long serialVersionUID = 1L;

	/** Adjustable params for the forecast. */
	protected ParameterList adjustableParams = new ParameterList();
	
	/** DUration of the forecast. */
	protected TimeSpan timeSpan;

	/** Flag indiacting whether any parameter has changed. */
	protected boolean parameterChangeFlag = true;

	/**
	 * get the adjustable parameters for this forecast
	 *
	 * @return
	 */
	public ListIterator<Parameter<?>> getAdjustableParamsIterator() {
		return adjustableParams.getParametersIterator();
	}

	/**
	 * Get the region for which this forecast is applicable
	 * @return : Geographic region object specifying the applicable region of forecast
	 */
	public Region getApplicableRegion() {
		return null;
	}

	/**
	 * This function returns the parameter with specified name from adjustable param list
	 * @param paramName : Name of the parameter needed from adjustable param list
	 * @return : ParamterAPI instance
	 */
	@SuppressWarnings("rawtypes")
	public Parameter getParameter(String paramName) {
		return adjustableParams.getParameter(paramName);
	}

	/**
	 * set the TimeSpan in the ERF
	 * @param timeSpan : TimeSpan object
	 */
	public void setTimeSpan(TimeSpan time) {
		// set the start time
		if (!time.getStartTimePrecision().equalsIgnoreCase(TimeSpan.NONE))
			this.timeSpan.setStartTime(time.getStartTimeCalendar());
		//set the duration as well
		this.timeSpan.setDuration(time.getDuration(), time.getDurationUnits());
	}

	/**
	 * return the time span object
	 *
	 * @return : time span object is returned which contains start time and duration
	 */
	public TimeSpan getTimeSpan() {
		return this.timeSpan;
	}




	/**
	 * Loops over all the adjustable parameters and set parameter with the given
	 * name to the given value.
	 * First checks if the parameter is contained within the ERF adjustable parameter
	 * list or TimeSpan adjustable parameters list. If not then return false.
	 * @param name String Name of the Adjustable Parameter
	 * @param value Object Parameeter Value
	 * @return boolean boolean to see if it was successful in setting the parameter
	 * value.
	 */
	@SuppressWarnings("unchecked")
	public boolean setParameter(String name, Object value){
		if(getAdjustableParameterList().containsParameter(name)){
			getAdjustableParameterList().getParameter(name).setValue(value);
			return true;
		}
		else if(timeSpan.getAdjustableParams().containsParameter(name)){
			timeSpan.getAdjustableParams().getParameter(name).setValue(value);
			return true;
		}
		return false;
	}

	/**
	 *  Function that must be implemented by all Timespan Listeners for
	 *  ParameterChangeEvents.
	 *
	 * @param  event  The Event which triggered this function call
	 */
	public void timeSpanChange(EventObject event) {
		parameterChangeFlag = true;
	}



	/**
	 *  This is the main function of this interface. Any time a control
	 *  paramater or independent paramater is changed by the user in a GUI this
	 *  function is called, and a paramater change event is passed in.
	 *
	 *  This sets the flag to indicate that the sources need to be updated
	 *
	 * @param  event
	 */
	public void parameterChange(ParameterChangeEvent event) {
		parameterChangeFlag = true;
	}

	/**
	 * Get number of ruptures for source at index iSource
	 * This method iterates through the list of 3 vectors for charA , charB and grB
	 * to find the the element in the vector to which the source corresponds
	 * @param iSource index of source whose ruptures need to be found
	 */
	public int getNumRuptures(int iSource) {
		return getSource(iSource).getNumRuptures();
	}

	/**
	 * Get the ith rupture of the source. this method DOES NOT return reference
	 * to the object. So, when you call this method again, result from previous
	 * method call is valid. This behavior is in contrast with
	 * getRupture(int source, int i) method
	 *
	 * @param source
	 * @param i
	 * @return
	 */
	public ProbEqkRupture getRuptureClone(int iSource, int nRupture) {
		return getSource(iSource).getRuptureClone(nRupture);
	}

	/**
	 * Get the ith rupture of the source. this method DOES NOT return reference
	 * to the object. So, when you call this method again, result from previous
	 * method call is valid. This behavior is in contrast with
	 * getRupture(int source, int i) method
	 *
	 * @param source
	 * @param i
	 * @return
	 */
	public ProbEqkRupture getRupture(int iSource, int nRupture) {
		return getSource(iSource).getRupture(nRupture);
	}

	/**
	 * Return the earthquake source at index i. This methos DOES NOT return the
	 * reference to the class variable. So, when you call this method again,
	 * result from previous method call is still valid. This behavior is in contrast
	 * with the behavior of method getSource(int i)
	 *
	 * @param iSource : index of the source needed
	 *
	 * @return Returns the ProbEqkSource at index i
	 *
	 * FIX:FIX :: This function has not been implemented yet. Have to give a thought on that
	 *
	 */
	public ProbEqkSource getSourceClone(int iSource) {
		return null;
	}

	/**
	 *
	 * @return the adjustable ParameterList for the ERF
	 */
	public ParameterList getAdjustableParameterList() {
		return this.adjustableParams;
	}

	/**
	 * sets the value for the parameter change flag
	 * @param flag
	 */
	public void setParameterChangeFlag(boolean flag) {
		this.parameterChangeFlag = flag;
	}

	/**
	 * Update the forecast and save it in serialized mode into a file
	 * @return
	 */
	public String updateAndSaveForecast() {
		throw new UnsupportedOperationException(
		"updateAndSaveForecast() not supported");
	}





	public static final String XML_METADATA_NAME = "ERF";
	
	protected static void baseERF_ToXML(BaseERF erf, Element erfEl) {
		erfEl.addAttribute("className", erf.getClass().getName());
		ListIterator<Parameter<?>> paramIt = erf.getAdjustableParameterList().getParametersIterator();
		Element paramsElement = erfEl.addElement(AbstractParameter.XML_GROUP_METADATA_NAME);
		while (paramIt.hasNext()) {
			Parameter<?> param = paramIt.next();
			paramsElement = param.toXMLMetadata(paramsElement);
		}
		erfEl = erf.getTimeSpan().toXMLMetadata(erfEl);
	}
	
	protected static BaseERF baseERF_FromXML(Element el) throws InvocationTargetException {
		String className = el.attribute("className").getValue();
//		System.out.println("Loading ERF: " + className);
		AbstractERF erf = (AbstractERF)MetadataLoader.createClassInstance(className);

		// add params
//		System.out.println("Setting params...");
		Element paramsElement = el.element(AbstractParameter.XML_GROUP_METADATA_NAME);
		ParameterList.setParamsInListFromXML(erf.getAdjustableParameterList(), paramsElement);

		erf.setTimeSpan(TimeSpan.fromXMLMetadata(el.element("TimeSpan")));
		
		return erf;
	}

	public Element toXMLMetadata(Element root) {
		Element xml = root.addElement(AbstractERF.XML_METADATA_NAME);
		
		baseERF_ToXML(this, xml);

		return root;
	}
	
	

	public static AbstractERF fromXMLMetadata(Element el) throws InvocationTargetException {
		return (AbstractERF)baseERF_FromXML(el);
	}

	/**
	 * This draws a random event set.  Non-poisson sources are not yet implemented
	 * @return
	 */
	public ArrayList<EqkRupture> drawRandomEventSet() {
		ArrayList<EqkRupture> rupList = new ArrayList<EqkRupture>();
		for(int s=0; s<this.getNumSources(); s++)
			rupList.addAll(getSource(s).drawRandomEqkRuptures());
		return rupList;
	}

	/**
	 * This specifies what types of Tectonic Regions are included in the ERF.
	 * This default implementation includes only ACTIVE_SHALLOW, so it should 
	 * be overridden in subclasses if other types are used
	 * @return : ArrayList<TectonicRegionType>
	 */
	public ArrayList<TectonicRegionType> getIncludedTectonicRegionTypes(){
		ArrayList<TectonicRegionType> list = new ArrayList<TectonicRegionType>();
		list.add(TectonicRegionType.ACTIVE_SHALLOW);
		return list;
	}

	@Override
	public int compareTo(BaseERF o) {
		return getName().compareToIgnoreCase(o.getName());
	}
	
	@Override
	public Iterator<ProbEqkSource> iterator() {
		return getSourceList().iterator();
	}

}
