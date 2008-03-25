package org.opensha.sha.earthquake;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.ListIterator;

import org.dom4j.Element;
import org.opensha.data.Location;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.GeographicRegion;
import org.opensha.metadata.MetadataLoader;
import org.opensha.metadata.XMLSaveable;
import org.opensha.param.Parameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.param.event.TimeSpanChangeListener;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import java.util.HashMap;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import org.opensha.sha.calc.ERF2GriddedSeisRatesCalc;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.param.StringParameter;
import org.opensha.param.BooleanParameter;



/**
 * <p>Title: EqkRupForecast/p>
 * <p>Description: Abstract class that provides the  basic implementation for the EqkRupForecast objects.</p>
 * @author unascribed
 * @version 1.0
 */

public abstract class EqkRupForecast implements EqkRupForecastAPI,
    TimeSpanChangeListener,ParameterChangeListener, XMLSaveable{

  // adjustable params for each forecast
  protected ParameterList adjustableParams = new ParameterList();
  // timespan object for each forecast
  protected TimeSpan timeSpan;

  // it is flag which indiactes whether any parameter have changed.
  // if it is true it means that forecast needs to be updated
  protected boolean parameterChangeFlag = true;



  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
  public ListIterator getAdjustableParamsIterator() {
    return adjustableParams.getParametersIterator();
  }

  /**
   * This function finds whether a particular location lies in applicable
   * region of the forecast
   *
   * @param loc : location
   * @return: True if this location is within forecast's applicable region, else false
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    return true;
  }

  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    return null;
  }

  /**
   * This function returns the parameter with specified name from adjustable param list
   * @param paramName : Name of the parameter needed from adjustable param list
   * @return : ParamterAPI instance
   */
  public ParameterAPI getParameter(String paramName) {
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
   * Get the number of earthquake sources
   *
   * @return integer value spcifying the number of earthquake sources
   */
  public abstract int getNumSources();

  /**
   * Return the earhthquake source at index i. This methos returns the reference to
   * the class variable. So, when you call this method again, result from previous
   * method call is no longer valid.
   * this is secret, fast but dangerous method
   *
   * @param i : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public abstract ProbEqkSource getSource(int iSource);

  /**
   * Get the list of all earthquake sources. Clone is returned.
   * So, list can be save in ArrayList and this object subsequently destroyed
   *
   * @return ArrayList of Prob Earthquake sources
   */
  public abstract ArrayList getSourceList();

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
   * Return  iterator over all the earthquake sources
   *
   * @return Iterator over all earhtquake sources
   */
  public Iterator getSourcesIterator() {
    Iterator i = getSourceList().iterator();
    return i;
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
   * @returns the adjustable ParameterList for the ERF
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



  /**
   * This function returns the total probability of events above a given magnitude
   * within the given geographic region.  The calcuated Rates depend on the  ERF
   * subclass.  Note that it is assumed that the forecast has been updated.
   * @param minMag double  : magnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalProbAbove(double minMag, GeographicRegion region) {

    ERF2GriddedSeisRatesCalc seisRates = new ERF2GriddedSeisRatesCalc();
    return seisRates.getTotalProbAbove(this,minMag,region);
  }



  /**
   * This function returns the total equivalent Poissonian rate above a given magnitude
   * for the given geographic region.  The result should be exact for ERFs where all
   * sources are Poissonian, or for ERFs with non-Poisson sources but low probabilities.
   *  The calcuated Rates depend on the  ERF subclass.
   * @param minMag double  : magnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalRateAbove(double minMag, GeographicRegion region) {

    ERF2GriddedSeisRatesCalc seisRates = new ERF2GriddedSeisRatesCalc();
    return seisRates.getTotalSeisRateInRegion(minMag,this,region);
  }


  /**
   * This function computes the rates above the given Magnitude for each rupture
   * location. Once computed , magnitude-rate distribution is stored for each
   * location on all ruptures in Eqk Rupture forecast model, if that lies within the
   * provided EvenlyGriddedGeographicRegion.
   * Once all Mag-Rate distribution has been computed for each location within the
   * ERF, this function returns ArrayList that constitutes of
   * ArbitrarilyDiscretizedFunc object. This ArbitrarilyDiscretizedFunc for each location
   * is the Mag-Rate distribution with X values being Mag and Y values being Rate.
   * @param minMag double : Magnitude above which Mag-Rate distribution is to be computed.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Ruptureforecast model
   * @param region EvenlyGriddedGeographicRegionAPI Region within which ruptures
   * are to be considered.
   * @return ArrayList with values being ArbitrarilyDiscretizedFunc
   * @see ArbitrarilyDiscretizedFunc, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public ArrayList getMagRateDistForEachLocationInRegion(double minMag,
      EvenlyGriddedGeographicRegionAPI region) {

    ERF2GriddedSeisRatesCalc seisRates = new ERF2GriddedSeisRatesCalc();
    return seisRates.calcCumMFD_ForGriddedRegion(minMag,this,region);
  }


  /**
   * This function computes the total SiesRate for each location on all the ruptures,
   * if they are within the provided Geographical Region.
   * It returns a double[] value being total seis rate for each location in region.
   * @param minMag double : Only those ruptures above this magnitude are considered
   * for calculation of the total seis rates in the region.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture forecast model
   * @param region EvenlyGriddedGeographicRegionAPI
   * @return double[] with each element in the array being totalSeisRate for each
   * location in the region.
   * @see Double, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public double[] getTotalSeisRateAtEachLocationInRegion(double minMag,
      EvenlyGriddedGeographicRegionAPI region) {

    ERF2GriddedSeisRatesCalc seisRates = new ERF2GriddedSeisRatesCalc();
    return seisRates.getTotalSeisRateAtEachLocationInRegion(minMag,this,region);
  }


  /**
   * This function returns the ArbDiscrEmpirical object that holds the
   * Mag-Rate of the entire region.
   * @param minMag double  Ruptures above this magnitude will be the ones that
   * will considered within the provided region  for computing the Mag-Rate Dist.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture Forecast from which
   * ruptures will computed.
   * @param region GeographicRegion Region for which mag-rate distribution has to be
   * computed.
   * @return ArbDiscrEmpiricalDistFunc : Distribution function that holds X values
   * as the magnitude and Y values as the sies rate for corresponding magnitude within
   * the region.
   */
  public ArbDiscrEmpiricalDistFunc getMagRateDistForRegion(double minMag,
      GeographicRegion region) {
    ERF2GriddedSeisRatesCalc seisRates = new ERF2GriddedSeisRatesCalc();
    return seisRates.getMagRateDistForRegion(minMag,this,region);
  }
  
  public static final String XML_METADATA_NAME = "ERF";
  
  public Element toXMLMetadata(Element root) {
	  Element xml = root.addElement(EqkRupForecast.XML_METADATA_NAME);
	  xml.addAttribute("className", this.getClass().getName());
	  ListIterator paramIt = this.getAdjustableParameterList().getParametersIterator();
	  Element paramsElement = xml.addElement(Parameter.XML_GROUP_METADATA_NAME);
	  while (paramIt.hasNext()) {
		  Parameter param = (Parameter)paramIt.next();
		  paramsElement = param.toXMLMetadata(paramsElement);
	  }
	  xml = timeSpan.toXMLMetadata(xml);
	  
	  return root;
  }
  
  public static EqkRupForecast fromXMLMetadata(Element root) throws InvocationTargetException {
	  String className = root.attribute("className").getValue();
	  System.out.println("Loading ERF: " + className);
	  EqkRupForecast erf = (EqkRupForecast)MetadataLoader.createClassInstance(className);
	  
	  // add params
	  System.out.println("Setting params...");
	  Element paramsElement = root.element(Parameter.XML_GROUP_METADATA_NAME);
	  ListIterator paramIt = erf.getAdjustableParameterList().getParametersIterator();
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
	  
	  erf.setTimeSpan(TimeSpan.fromXMLMetadata(root.element("TimeSpan")));
	  
	  return erf;
  }
}
