package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.ListIterator;
import java.util.EventObject;

import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.export.Exporter;
import java.rmi.*;

import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.UnknownEventException;

import org.opensha.data.TimeSpan;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterAPI;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.remote.*;
import org.opensha.data.Location;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import java.rmi.server.*;

/**
 *
 * <p>Title: RemoteERF_Client</p>
 *
 * <p>Description: </p>
 *
 * @author Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public class RemoteERF_Client extends EqkRupForecast implements RemoteEventListener,
    ParameterChangeListener,TimeSpanChangeListener{

  private RemoteEqkRupForecastAPI erfServer = null;


  //adds the listeners to this list
  private ArrayList listenerList = new ArrayList();

  //creates the EventObject to send to the listeners for parameter change
  //and timespan change
  private EventObject eventObj;

  //checks if within the notify function
  private boolean withinNotify = false;

  /**
   * Get the reference to the remote ERF
   */
  protected void getRemoteERF(String className,
                              String rmiRemoteRegistrationName) throws
      RemoteException {
    try {
      RemoteERF_FactoryAPI remoteERF_Factory = (RemoteERF_FactoryAPI) Naming.
          lookup(rmiRemoteRegistrationName);
      erfServer = remoteERF_Factory.getRemoteERF(className);
      adjustableParams = erfServer.getAdjustableParameterList();
      ListIterator it = adjustableParams.getParametersIterator();
      while (it.hasNext())
        ( (ParameterAPI) it.next()).addParameterChangeListener(this);
      //getting the timespan object and adding the parameterchange listener obejct to it
      //getting the timespan and adjustable params
      timeSpan = erfServer.getTimeSpan();
      addListenersToTimeSpan();
    }
    catch (NotBoundException n) {
      n.printStackTrace();
    }
    catch (MalformedURLException m) {
      m.printStackTrace();
    }
    catch (java.rmi.UnmarshalException u) {
      u.printStackTrace();
    }
    //Make a proxy of myself to pass to the server/filter
    Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                                              new BasicILFactory());
    try {
      RemoteEventListener proxy = (RemoteEventListener) exporter.export(this);
      erfServer.addParameterAndTimeSpanChangeListener(proxy);
    }
    catch (ExportException ex) {
      ex.printStackTrace();
    }

  }


  //add the listeners to the timespan parameters
  private void addListenersToTimeSpan() {
    //if timespan is not null then add the change listeners to its parameters.
    //we are again adding listeners here becuase they are transient and cannot be serialized.
    if (timeSpan != null) {
      timeSpan.addParameterChangeListener(this);
      ParameterList timeSpanParamList = timeSpan.getAdjustableParams();
      ListIterator it = timeSpanParamList.getParametersIterator();
      while (it.hasNext())
        ( (ParameterAPI) it.next()).addParameterChangeListener(timeSpan);
    }
  }

  /**
   * @param paramArrays: Object array of the arguments used to create the argument
   * constructor for the server based ERF's.
   * @param paramTypes
   * @param className
   * @throws RemoteException
   */
  protected void getRemoteERF(ArrayList paramArrays,ArrayList paramTypes,String className,String rmiRemoteRegistrationName) throws RemoteException{
    try {
      RemoteERF_FactoryAPI remoteERF_Factory= (RemoteERF_FactoryAPI) Naming.lookup(rmiRemoteRegistrationName);
      erfServer = remoteERF_Factory.getRemoteERF(paramArrays,paramTypes,className);
      adjustableParams = erfServer.getAdjustableParameterList();
      ListIterator it = adjustableParams.getParametersIterator();
      while(it.hasNext())
        ((ParameterAPI)it.next()).addParameterChangeListener(this);
    }
    catch (NotBoundException n) {
      n.printStackTrace();
    }
    catch (MalformedURLException m) {
      m.printStackTrace();
    }
    catch (java.rmi.UnmarshalException u) {
      u.printStackTrace();
    }
    //Make a proxy of myself to pass to the server/filter
    Exporter exporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),
                                              new BasicILFactory());
    try {
      RemoteEventListener proxy = (RemoteEventListener) exporter.export(this);
      erfServer.addParameterAndTimeSpanChangeListener(proxy);
    }
    catch (ExportException ex) {
      ex.printStackTrace();
    }

  }


   /**
    * This function returns the parameter with specified name from adjustable param list
    * @param paramName : Name of the parameter needed from adjustable param list
    * @return : ParamterAPI instance
    */
   public ParameterAPI getParameter(String paramName) {
     try {
       return erfServer.getParameter(paramName);
     }
     catch (Exception e) {
       e.printStackTrace();
     }
    return null;
   }



  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.EqkRupForecastAPI#getNumSources()
   */
  public int getNumSources() {
    try {
      return erfServer.getNumSources();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.EqkRupForecastAPI#getSource(int)
   */
  public ProbEqkSource getSource(int iSource) {
    // TODO Auto-generated method stub
    try {
      return erfServer.getSource(iSource);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.EqkRupForecastAPI#getSourceList()
   */
  public ArrayList getSourceList() {
    try {
      return erfServer.getSourceList();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.opensha.param.event.ParameterChangeListener#parameterChange(org.opensha.param.event.ParameterChangeEvent)
   */
  public void parameterChange(ParameterChangeEvent event) {
    try {

      erfServer.setParameter(event.getParameterName(), event.getNewValue());
    }
    catch (RemoteException ex) {
      ex.printStackTrace();
    }
  }

  /**
   *  Function that must be implemented by all Timespan Listeners for
   *  ParameterChangeEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void timeSpanChange(EventObject event) {
    try {
      ParameterChangeEvent chgEvent = ((ParameterChangeEvent)event);
      erfServer.setParameter(chgEvent.getParameterName(), chgEvent.getNewValue());
    }
    catch (RemoteException ex) {
      ex.printStackTrace();
    }
  }


  /**
   * This function finds whether a particular location lies in applicable
   * region of the forecast
   *
   * @param loc : location
   * @return: True if this location is within forecast's applicable region, else false
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    try {
      return erfServer.isLocWithinApplicableRegion(loc);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }


  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
   public GeographicRegion getApplicableRegion() {
     try {
       return erfServer.getApplicableRegion();
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     return null;
   }

   /**
    * Get number of ruptures for source at index iSource
    * This method iterates through the list of 3 vectors for charA , charB and grB
    * to find the the element in the vector to which the source corresponds
    * @param iSource index of source whose ruptures need to be found
    */
   public int getNumRuptures(int iSource){
     try {
       return erfServer.getNumRuptures(iSource);
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     return -1;
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
     try {
       return erfServer.getRupture(iSource,nRupture);
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     return null;
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
     try {
       return erfServer.getRuptureClone(iSource,nRupture);
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     return null;
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
     try {
       return erfServer.getSourceClone(iSource);
     }
     catch (Exception e) {
       e.printStackTrace();
     }
     return null;
   }


  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.EqkRupForecastAPI#updateForecast()
   */
  public void updateForecast() {
    try {
      if(parameterChangeFlag){
        erfServer.updateForecast();
        setParameterChangeFlag(false);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.EqkRupForecastAPI#updateForecast()
   */
  public String updateAndSaveForecast() {
    try {
      updateForecast();
      return erfServer.saveForecast();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.opensha.data.NamedObjectAPI#getName()
   */
  public String getName() {
    // TODO Auto-generated method stub
    try {
      return erfServer.getName();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * return the time span object
   *
   * @return : time span object is returned which contains start time and duration
   */
  public TimeSpan getTimeSpan() {
    try {
      return timeSpan;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }



  /* (non-Javadoc)
   * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParameterList()
   */
  public ParameterList getAdjustableParameterList() {
    // TODO Auto-generated method stub
    try {
      return adjustableParams;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * This method sets the time-span field.
   * @param time
   */
  public void setTimeSpan(TimeSpan time) {
    try{
      erfServer.setTimeSpan(time);
    }catch(RemoteException e){
      e.printStackTrace();
    }
  }


  /**
   *
   * @returns the instance to the remote ERF on the server
   */
  public RemoteEqkRupForecastAPI getERF_Server(){
    return this.erfServer;
  }


  /**
   *
   * @param remoteEqkRupForecastAPI RemoteEqkRupForecastAPI
   */
  public void setERF_Server(RemoteEqkRupForecastAPI remoteEqkRupForecastAPI){
    this.erfServer = remoteEqkRupForecastAPI;
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
    try {
      return erfServer.getTotalProbAbove(minMag, region);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return 0.0;
  }

  /**
   * This function returns the total Rate above a given magnitude ,
   * for the given geographic region.
   * Calcuated Rates depend on the ERF model instantiated by the user.
   * @param minMag double  : Amgnitude above which rate needs to be returned
   *
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */
  public double getTotalRateAbove(double minMag, GeographicRegion region) {
    try {
      return erfServer.getTotalRateAbove(minMag, region);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return 0.0;
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
    try {
      return erfServer.getMagRateDistForEachLocationInRegion(minMag, region);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
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
      EvenlyGriddedGeographicRegionAPI region){
    try {
      return erfServer.getTotalSeisRateAtEachLocationInRegion(minMag, region);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;

  }

  /**
   * adds the listener obj to list. When the change events come, all
   * listeners added to it are notified of it.
   * @param obj Object
   */
  public void addParameterAndTimeSpanChangeListener(ParameterAndTimeSpanChangeListener obj) {
    listenerList.add(obj);
  }

  /**
   * This method is called from the remote event is received from the Server by the client.
   * @param remoteEvent RemoteEvent
   * @throws UnknownEventException
   * @throws RemoteException
   */
  public void notify(RemoteEvent remoteEvent) throws UnknownEventException,
      RemoteException {
    withinNotify = true;
    Object obj = remoteEvent.getSource();
    eventObj = new EventObject(obj);
    if(obj instanceof ParameterAPI){
      ParameterAPI param = (ParameterAPI)obj;
      adjustableParams.getParameter(param.getName()).setValue(param.getValue());
    }
    else if(obj instanceof TimeSpan){
      timeSpan = (TimeSpan)obj;
      addListenersToTimeSpan();
    }

    int size = listenerList.size();
    for(int i=0;i<size;++i){
      ParameterAndTimeSpanChangeListener listener = (ParameterAndTimeSpanChangeListener)listenerList.get(i);
      listener.parameterOrTimeSpanChange(eventObj);
    }
    withinNotify = false;
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
      GeographicRegion region){
    try {
      return erfServer.getMagRateDistForRegion(minMag, region);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
