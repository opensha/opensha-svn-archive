package org.opensha.sha.imr;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.opensha.commons.data.Site;

import org.opensha.exceptions.*;
import org.opensha.metadata.MetadataLoader;
import org.opensha.param.*;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.param.*;


/**
 *  <b>Title:</b> IntensityMeasureRelationship<p>
 *
 *  <b>Description:</b> Abstract base class for Intensity Measure Relationship (IMR).
 *  All IMRs compute the probability of exceeding a particular shaking level (specified
 *  by an intenisty-measure Parameter) given a Site and ProbEqkRupture object.
 *  Subclasses will implement specific types of IMRs (e.g., AttenuationRelationship).
 *  This abstract IMR class also contains seperate parameterList objects for the
 *  site, potential-earthquake, and propagation-effect related parameters, as well
 *  as a list of "other" parameters that don't fit into those three categories.
 *  This class also contains a list of supported intensity-measure parameters (which
 *  may have internal independent parameters). These five lists combined (siteParams,
 *  EqkRuptureParams, propagationEffectParams, supportedIMParams, and otherParams)
 *  constitutes the complete list of parameters that the exceedance probability depends
 *  upon.  The only other paramter is exceedProbParam, which is used to compute the
 *  IML at a particular probability in subclasses that support the getIML_AtExceedProb()
 *  method. <p>
 *
 * @author     Edward H. Field & Steven W. Rock
 * @created    February 21, 2002
 * @version    1.0
 * @see        IntensityMeasureRelationshipAPI
 */

public abstract class IntensityMeasureRelationship
    implements IntensityMeasureRelationshipAPI {

  private final static String NAME = "Intensity Measure Relationship";
  
  protected String url_info_string;
  
  public final static String XML_METADATA_NAME = "IMR";
  public final static String XML_METADATA_IMT_NAME = "IntensityMeasure";
  public final static String XML_METADATA_SITE_PARAMETERS_NAME = "SiteParameters";

  /** Classname constant used for debugging statements */
  protected final static String C = "IntensityMeasureRelationship";

  /** Prints out debugging statements if true */
  protected final static boolean D = false;

  /**
   * Exceed Prob parameter, used only to store the exceedance probability to be
   * used by the getIML_AtExceedProb() method of subclasses (if a subclass supports
   * this method).  Note that calling the getExceedProbability() does not store the
   * value in this parameter.
   */
  protected DoubleParameter exceedProbParam = null;
  public final static String EXCEED_PROB_NAME = "Exceed. Prob.";
  protected final static Double EXCEED_PROB_DEFAULT = new Double(0.5);
  public final static String EXCEED_PROB_INFO = "Exceedance Probability";
  public final static Double EXCEED_PROB_MIN = new Double(1.0e-6);
  public final static Double EXCEED_PROB_MAX = new Double(1.0 - 1e-6);

  /** ParameterList of all Site parameters */
  protected ParameterList siteParams = new ParameterList();

  /** ParameterList of all eqkRupture parameters */
  protected ParameterList eqkRuptureParams = new ParameterList();

  /**
   * ParameterList of all Propagation-Effect parameters (this should perhaps
   * exist only in subclasses since not all IMRs will have these?)
   */
  protected ParameterList propagationEffectParams = new ParameterList();

  /** ParameterList of all supported Intensity Measure parameters */
  protected ParameterList supportedIMParams = new ParameterList();

  /**
   * ParameterList of other parameters that don't fit into above categories.
   * These are any parameters that the exceedance probability depends upon that is
   * not a supported IMT (or one of their independent parameters) and is not contained
   * in, or computed from, the site or eqkRutpure objects.  Note that this does not
   * include the exceedProbParam (which exceedance probability does not depend on).
   */
  protected ParameterList otherParams = new ParameterList();

  /** The current Site object (passing one in will set site-related parameters). */
  protected Site site;

  /** The current EqkRupture object (passing one in will set Earthquake-
   *  Rupture related parameters.
   */
  protected EqkRupture eqkRupture;

  protected PropagationEffect propEffect;

  /**
   *  Intensity Measure.  This is a specification of the type of shaking one
   *  is concered about.  Its representation as a Parameter makes the
   *  specification quite general and flexible.  IMRs compute the probability
   *  of exceeding the "value" field of this im Parameter.
   */
  protected ParameterAPI im;

  //this flag checks if intensity measure has been changed, if it is then make it
  //true else false. It is initialized to false
  protected boolean intensityMeasureChanged;

  /**
   *  No-Arg Constructor for the IntensityMeasureRelationship object. This only
   *  creates one parameter (exceedProbParam) used by some subclasses.
   */
  public IntensityMeasureRelationship() {

    exceedProbParam = new DoubleParameter(EXCEED_PROB_NAME, EXCEED_PROB_MIN,
                                          EXCEED_PROB_MAX, EXCEED_PROB_DEFAULT);
    exceedProbParam.setInfo(EXCEED_PROB_INFO);
    exceedProbParam.setNonEditable();

  }

  /**
   *  Returns name of the IntensityMeasureRelationship.
   *
   * @return    The name string
   */
  public String getName() {
    return NAME;
  }

  /**
   *  Sets the Site object as a reference to that passed in, and sets
   *  any internal site-related parameters that the IMR depends upon.
   *
   * @param  site  The new site object
   */
  public void setSite(Site site) {
    this.site = site;
  }

  /**
   * This sets the site and EqkRupture from the propEffect object passed in
   * @param propEffect
   */
  public void setPropagationEffect(PropagationEffect propEffect) {
    setSite(propEffect.getSite());
    setEqkRupture(propEffect.getEqkRupture());
  }

  /**
   *  Returns a reference to the current Site object of the IMR
   *
   * @return    The site object
   */
  public Site getSite() {
    return site;
  }

  /**
   *  Returns a reference to the current EqkRupture object in the IMR
   *
   * @return    The EqkRupture object
   */
  public EqkRupture getEqkRupture() {
    return eqkRupture;
  }

  /**
   *  Sets the EqkRupture object in the IMR as a reference
   *  to the one passed in, and sets any earthquake-rupture related
   *  parameters that the IMR depends upon.
   *
   * @param  eqkRupture  The new EqkRupture object
   */
  public void setEqkRupture(EqkRupture eqkRupture) {
    this.eqkRupture = eqkRupture;
  }

  /**
   *  Returns the "value" object of the currently chosen Intensity-Measure
   *  Parameter.
   *
   * @return    The value field of the currently chosen intensityMeasure
   */
  public Object getIntensityMeasureLevel() {
    return im.getValue();
  }

  /**
   *  Sets the value of the currently chosen Intensity-Measure Parameter.
   *  This is the value that the probability of exceedance is computed for.
   *
   * @param  iml  The new value for the intensityMeasure Parameter
   */
  public void setIntensityMeasureLevel(Object iml) throws ParameterException {

    if (this.im == null) {
      throw new ParameterException(C +
                                   ": setIntensityMeasureLevel(): " +
                                   "The Intensity Measure has not been set yet, unable to set the level."
          );
    }

    im.setValue(iml);

  }

  /**
   *  Gets a reference to the currently chosen Intensity-Measure Parameter
   *  from the IMR.
   *
   * @return    The intensityMeasure Parameter
   */
  public ParameterAPI getIntensityMeasure() {
    return im;
  }

  /**
   *  Sets the intensityMeasure parameter, not as a  pointer to that passed in,
   *  but by finding the internally held one with the same name and then setting
   *  its value (and the value of any of its independent parameters) to be equal
   *  to that passed in.
   *
   * @param  intensityMeasure  The new intensityMeasure Parameter
   */
  public void setIntensityMeasure(ParameterAPI intensityMeasure) throws
      ParameterException, ConstraintException {

    if (isIntensityMeasureSupported(intensityMeasure)) {
      setIntensityMeasure(intensityMeasure.getName());
      ListIterator it = ( (DependentParameterAPI) intensityMeasure).
          getIndependentParametersIterator();
      while (it.hasNext()) {
        ParameterAPI param = (ParameterAPI) it.next();
        getParameter(param.getName()).setValue(param.getValue());
      }
    }
    else {
      throw new ParameterException("This im is not supported, name = " +
                                   intensityMeasure.getName());
    }
  }

  /**
   *  This sets the intensityMeasure parameter as that which has the name
   *  passed in and sets the passed in period  is IMT is SA.
   *
   * @param  intensityMeasure  The new intensityMeasureParameter name
   */
  public void setIntensityMeasure(String intensityMeasure , double period) throws
      ParameterException, ConstraintException {

    if (isIntensityMeasureSupported(intensityMeasure,period)) {
        setIntensityMeasure(intensityMeasure);
        if(intensityMeasure.equals(AttenuationRelationship.SA_NAME))
           getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(period));
    }
    else {
      throw new ParameterException("This im is not supported, name = " +
                                   intensityMeasure);
    }
  }
  
  /**
   *  This sets the intensityMeasure parameter as that which has the name
   *  passed in; no value (level) is set, nor are any of the IM's independent
   *  parameters set (since it's only given the name).
   *
   * @param  intensityMeasure  The new intensityMeasureParameter name
   */
  public void setIntensityMeasure(String intensityMeasureName) throws
      ParameterException {

    im = supportedIMParams.getParameter(intensityMeasureName);
    intensityMeasureChanged = true;
  }

  /**
   * Sets the exceed probabality param value. This function is only for setting
   * the value of the Exceed Prob. Param , so that we can get the IML@ excced prob.
   *
   * @param prob : The value passed in to set the Exceed Prob Param is not what
   * is returned back from function getExceedProb(), becuase that returns the
   * computed exceedance probablity at the selected IML.
   *
   * @throws ParameterException
   */
  public void setExceedProb(double prob) throws ParameterException {
    //just sets the exccedProb param value
    exceedProbParam.setValue(prob);
  }

  /**
   *  Checks if the Parameter is a supported intensity-Measure (checking
   *  both the name and value, as well as any dependent parameters
   *  (names and values) of the IM).  PROBLEM: THE VALUE OF THE IM IS NOT CHECKED,
   *  AND THIS IMPLEMENTATION ASSUMES THAT ALL THE DEPENDENT PARAMETERS ARE DOUBLE
   *  PARAMETERS - WE NEED TO FIX THE FORMER AND RELAX THAT LATTER.
   *
   * @param  intensityMeasure  Description of the Parameter
   * @return                   True if this is a supported IMT
   */
  public boolean isIntensityMeasureSupported(ParameterAPI intensityMeasure) {

    if (supportedIMParams.containsParameter(intensityMeasure)) {
      //   ParameterAPI param = supportedIMParams.getParameter( intensityMeasure.getName() );
      ListIterator it = ( (DependentParameterAPI) intensityMeasure).
          getIndependentParametersIterator();
      while (it.hasNext()) {
        ParameterAPI param = (ParameterAPI) it.next();
        if (getParameter(param.getName()).isAllowed(param.getValue())) {
          continue;
        }
        else {
          return false;
        }
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Checks if the Parameter is a supported intensity-Measure (checking
   * only the name).
   * @param intensityMeasure Name of the intensity Measure parameter
   * @return
   */
  public boolean isIntensityMeasureSupported(String intensityMeasure) {
    if (supportedIMParams.containsParameter(intensityMeasure)) {
      return true;
    }
    return false;
  }
  
  /**
   * Checks if the Parameter is a supported intensity-Measure (checking
   * only the name and Period).
   * @param intensityMeasure Name of the intensity Measure parameter
   * @param period Period Param Name is intensity measure is SA
   * @return
   */
  public boolean isIntensityMeasureSupported(String intensityMeasure,double period){
	  if(isIntensityMeasureSupported(intensityMeasure)){
		ParameterAPI imParam = supportedIMParams.getParameter(intensityMeasure);
		if(imParam.getName().equals(AttenuationRelationship.SA_NAME)){
	        if (getParameter(AttenuationRelationship.PERIOD_NAME).isAllowed(period)) {
	          return true;
	        }
	        else {
	          return false;
	        }
		}
		return true;
	  }
	return false;
  }

  /**
   *  Sets the eqkRupture, site, and intensityMeasure objects
   *  simultaneously.<p>
   *
   *  SWR: Warning - this function doesn't provide full rollback in case of
   *  failure. There are 4 method calls that sets parameters with new values.
   *  If one function fails, the previous functions effects are not undone,
   *  i.e. the transaction is not handled gracefully.<p>
   *
   *  This will take alot of design and work so it is held off for now until
   *  it is decided that it is needed.<p>
   *
   *
   *
   * @param  eqkRupture           The new EqkRupture
   * @param  site                     The new Site
   * @param  intensityMeasure         The new intensityMeasure
   * @exception  ParameterException   Description of the Exception
   * @exception  IMRException         Description of the Exception
   * @exception  ConstraintException  Description of the Exception
   */
  public void setAll(
      EqkRupture eqkRupture,
      Site site,
      ParameterAPI intensityMeasure
      ) throws ParameterException, IMRException, ConstraintException {
    setSite(site);
    setEqkRupture(eqkRupture);
    setIntensityMeasure(intensityMeasure);
  }

  /**
   * Returns a pointer to a parameter if it exists in one of the parameter lists
   *
   * @param name                  Parameter key for lookup
   * @return                      The found parameter
   * @throws ParameterException   If parameter with that name doesn't exist
   */
  public ParameterAPI getParameter(String name) throws ParameterException {

    // check whether it's the exceedProbParam
    if (name.equals(EXCEED_PROB_NAME)) {
      return exceedProbParam;
    }

    try {
      return siteParams.getParameter(name);
    }
    catch (ParameterException e) {}

    try {
      return eqkRuptureParams.getParameter(name);
    }
    catch (ParameterException e) {}

    try {
      return propagationEffectParams.getParameter(name);
    }
    catch (ParameterException e) {}

    try {
      return supportedIMParams.getParameter(name);
    }
    catch (ParameterException e) {}

    ListIterator<ParameterAPI> it = supportedIMParams.getParametersIterator();
    while (it.hasNext()) {

      DependentParameterAPI param = (DependentParameterAPI) it.next();
      if (param.containsIndependentParameter(name)) {
        return param.getIndependentParameter(name);
      }

    }

    try {
      return otherParams.getParameter(name);
    }
    catch (ParameterException e) {}

    throw new ParameterException(C +
        ": getParameter(): Parameter doesn't exist named " + name);
  }

  /**
   *  Returns an iterator over all Site-related parameters.
   *
   * @return    The Site Parameters Iterator
   */
  public ListIterator<ParameterAPI> getSiteParamsIterator() {
    return siteParams.getParametersIterator();
  }

  /**
   *  Returns an iterator over all other parameters.  Other parameters are those
   *  that the exceedance probability depends upon, but that are not a
   *  supported IMT (or one of their independent parameters) and are not contained
   *  in, or computed from, the site or eqkRutpure objects.  Note that this does not
   *  include the exceedProbParam (which exceedance probability does not depend on).
   *
   * @return    Iterator for otherParameters
   */
  public ListIterator<ParameterAPI> getOtherParamsIterator() {
    return otherParams.getParametersIterator();
  }

  /**
   *  Returns an iterator over all EqkRupture related parameters.
   *
   * @return    The EqkRupture Parameters Iterator
   */
  public ListIterator<ParameterAPI> getEqkRuptureParamsIterator() {
    return eqkRuptureParams.getParametersIterator();
  }

  /**
   *  Returns the iterator over all Propagation-Effect related parameters
   * (perhaps this method should exist only in subclasses that have these types
   * of parameters).
   *
   * @return    The Propagation Effect Parameters Iterator
   */
  public ListIterator<ParameterAPI> getPropagationEffectParamsIterator() {
    return propagationEffectParams.getParametersIterator();
  }

  /**
   *  Returns the iterator over all supported Intensity-Measure
   *  Parameters.
   *
   * @return    The Supported Intensity-Measures Iterator
   */
  public ListIterator<ParameterAPI> getSupportedIntensityMeasuresIterator() {
    return supportedIMParams.getParametersIterator();
  }
  
  public Element toXMLMetadata(Element root) {
	  Element xml = root.addElement(IntensityMeasureRelationship.XML_METADATA_NAME);
	  xml.addAttribute("className", this.getClass().getName());
	  ListIterator paramIt = this.getOtherParamsIterator();
	  Element paramsElement = xml.addElement(Parameter.XML_GROUP_METADATA_NAME);
	  while (paramIt.hasNext()) {
		  Parameter param = (Parameter)paramIt.next();
		  paramsElement = param.toXMLMetadata(paramsElement);
	  }
	  paramIt = this.getSiteParamsIterator();
	  Element siteParamsElement = xml.addElement(IntensityMeasureRelationship.XML_METADATA_SITE_PARAMETERS_NAME);
	  while (paramIt.hasNext()) {
		  Parameter param = (Parameter)paramIt.next();
		  siteParamsElement = param.toXMLMetadata(siteParamsElement);
	  }
	  String imt = this.getIntensityMeasure().getName();
	  Element imtElem = xml.addElement(IntensityMeasureRelationship.XML_METADATA_IMT_NAME);
	  imtElem.addAttribute("Type", imt);
	  ParameterAPI period = this.getParameter(AttenuationRelationship.PERIOD_NAME);
	  if (period != null)
		  imtElem.addAttribute(AttenuationRelationship.PERIOD_NAME.replaceAll(" ", ""), period.getValue().toString());
	  ParameterAPI damping = this.getParameter(AttenuationRelationship.DAMPING_NAME);
	  if (damping != null)
		  imtElem.addAttribute(AttenuationRelationship.DAMPING_NAME.replaceAll(" ", ""), damping.getValue().toString());
	  return root;
  }
  
  public static IntensityMeasureRelationship fromXMLMetadata(Element root, ParameterChangeWarningListener listener) throws InvocationTargetException {
	  String className = root.attribute("className").getValue();
	  System.out.println("Loading IMR: " + className);
	  ArrayList<Object> args = new ArrayList<Object>();
	  ArrayList<String> argNames = new ArrayList<String>();
	  args.add(listener);
	  argNames.add(ParameterChangeWarningListener.class.getName());
	  IntensityMeasureRelationship imr = (IntensityMeasureRelationship)MetadataLoader.createClassInstance(className, args, argNames);
	  imr.setParamDefaults();
	  
	  // add params
	  System.out.println("Setting params...");
	  Element paramsElement = root.element(Parameter.XML_GROUP_METADATA_NAME);
	  ListIterator paramIt = imr.getOtherParamsIterator();
	  while (paramIt.hasNext()) {
		  Parameter param = (Parameter)paramIt.next();
		  System.out.println("Setting param " + param.getName());
		  Iterator<Element> it = paramsElement.elementIterator();
		  while (it.hasNext()) {
			  Element el = it.next();
			  if (param.getName().equals(el.attribute("name").getValue())) {
				  System.out.println("Found a match!");
				  if (param.setValueFromXMLMetadata(el)) {
					  System.out.println("Parameter set successfully!");
				  } else {
					  System.out.println("Parameter could not be set from XML!");
					  System.out.println("It is possible that the parameter type doesn't yet support loading from XML");
				  }
			  }
		  }
	  }
	  
	  System.out.println("Setting site params...");
	  Element siteParamsElement = root.element(IntensityMeasureRelationship.XML_METADATA_SITE_PARAMETERS_NAME);
	  if (siteParamsElement != null) {
		  paramIt = imr.getSiteParamsIterator();
		  while (paramIt.hasNext()) {
			  Parameter param = (Parameter)paramIt.next();
			  System.out.println("Setting param " + param.getName());
			  Iterator<Element> it = siteParamsElement.elementIterator();
			  while (it.hasNext()) {
				  Element el = it.next();
				  if (param.getName().equals(el.attribute("name").getValue())) {
					  System.out.println("Found a match!");
					  if (param.setValueFromXMLMetadata(el)) {
						  System.out.println("Parameter set successfully!");
					  } else {
						  System.out.println("Parameter could not be set from XML!");
						  System.out.println("It is possible that the parameter type doesn't yet support loading from XML");
					  }
				  }
			  }
		  }
	  }
	  
	  // set IMT
	  Element imtElem = root.element(IntensityMeasureRelationship.XML_METADATA_IMT_NAME);
	  if (imtElem != null) {
		  imr.setIntensityMeasure(imtElem.attribute("Type").getValue());
		  Attribute period = imtElem.attribute(AttenuationRelationship.PERIOD_NAME.replaceAll(" ", ""));
		  if (period != null) {
			  ParameterAPI periodParam = imr.getParameter(AttenuationRelationship.PERIOD_NAME);
			  if (periodParam != null)
				  periodParam.setValue(Double.parseDouble(period.getValue()));
		  }

		  Attribute damping = imtElem.attribute(AttenuationRelationship.DAMPING_NAME.replaceAll(" ", ""));
		  if (damping != null) {
			  ParameterAPI dampingParam = imr.getParameter(AttenuationRelationship.DAMPING_NAME);
			  if (dampingParam != null)
				  dampingParam.setValue(Double.parseDouble(damping.getValue()));
		  }
	  }
	  
	  return imr;
  }
  
  
  /**
   * This provides a URL where more info on this model can be obtained
   * @throws MalformedURLException if returned URL is not a valid URL.
   * @returns the URL to the AttenuationRelationship document on the Web.
   * 
   * This default implementation returns nothing
   */
  public URL getInfoURL() throws MalformedURLException{
	  return new URL(null);
  }



}
