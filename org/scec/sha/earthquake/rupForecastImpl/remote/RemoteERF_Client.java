/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.remote;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.EventObject;

import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;
import org.scec.param.ParameterAPI;
import org.scec.param.event.*;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.earthquake.ProbEqkSource;
import org.scec.sha.earthquake.rupForecastImpl.remote.*;

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RemoteERF_Client extends EqkRupForecast implements
    ParameterChangeListener,TimeSpanChangeListener {

  private RemoteERF_API erfServer = null;


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
      ListIterator it = erfServer.getAdjustableParamsIterator();
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



  /* (non-Javadoc)
   * @see org.scec.sha.earthquake.ERF_API#getNumSources()
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
   * @see org.scec.sha.earthquake.ERF_API#getSource(int)
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
   * @see org.scec.sha.earthquake.ERF_API#getSourceList()
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
   * @see org.scec.param.event.ParameterChangeListener#parameterChange(org.scec.param.event.ParameterChangeEvent)
   */
  public void parameterChange(EventObject event) {
    setParameterChangeFlag(true);
  }

  /* (non-Javadoc)
   * @see org.scec.sha.earthquake.EqkRupForecastAPI#updateForecast()
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
   * @see org.scec.sha.earthquake.EqkRupForecastAPI#updateForecast()
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
   * @see org.scec.data.NamedObjectAPI#getName()
   */
  public String getName() {
    // TODO Auto-generated method stub
    try {
      return null;
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
      timeSpan = erfServer.getTimeSpan();
      return timeSpan;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParamsIterator()
   */
  public ListIterator getAdjustableParamsIterator() {
    try {
      // TODO Auto-generated method stub
      return getAdjustableParameterList().getParametersIterator();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParameterList()
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
   *
   * @returns the instance to the remote ERF on the server
   */
  public RemoteERF_API getERF_Server(){
    return this.erfServer;
  }


  public void setERF_Server(RemoteERF_API remoteERF_API){
    this.erfServer = remoteERF_API;
  }

}