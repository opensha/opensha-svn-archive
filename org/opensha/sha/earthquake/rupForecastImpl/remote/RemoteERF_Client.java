package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.EventObject;

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

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RemoteERF_Client extends EqkRupForecast implements
    ParameterChangeListener,TimeSpanChangeListener {

  private RemoteEqkRupForecastAPI erfServer = null;


  /**
   * Get the reference to the remote ERF
   */
  protected void getRemoteERF(String className) throws RemoteException{
    try {
      RemoteERF_FactoryAPI remoteERF_Factory= (RemoteERF_FactoryAPI) Naming.lookup(RegisterRemoteERF_Factory.registrationName);
      erfServer = remoteERF_Factory.getRemoteERF(className);
      adjustableParams = erfServer.getAdjustableParameterList();
      ListIterator it = adjustableParams.getParametersIterator();
      while(it.hasNext())
        ((ParameterAPI)it.next()).addParameterChangeListener(this);
      //getting the timespan and adjustable params
      timeSpan =erfServer.getTimeSpan();
      //if timespan is not null then add the change listeners to its parameters.
      //we are again adding listeners here becuase they are transient and cannot be serialized.
      if(timeSpan !=null){
        timeSpan.addParameterChangeListener(this);
        ParameterList timeSpanParamList = timeSpan.getAdjustableParams();
        it = timeSpanParamList.getParametersIterator();
        while(it.hasNext())
          ((ParameterAPI)it.next()).addParameterChangeListener(timeSpan);
      }
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
  }


  /**
   * @param paramArrays: Object array of the arguments used to create the argument
   * constructor for the server based ERF's.
   * @param paramTypes
   * @param className
   * @throws RemoteException
   */
  protected void getRemoteERF(ArrayList paramArrays,ArrayList paramTypes,String className) throws RemoteException{
    try {
      RemoteERF_FactoryAPI remoteERF_Factory= (RemoteERF_FactoryAPI) Naming.lookup(RegisterRemoteERF_Factory.registrationName);
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
  public void parameterChange(EventObject event) {
    setParameterChangeFlag(true);
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
        erfServer.updateForecast(adjustableParams, timeSpan);
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



  public void setERF_Server(RemoteEqkRupForecastAPI remoteEqkRupForecastAPI){
    this.erfServer = remoteEqkRupForecastAPI;
  }

}
