/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeListener;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.earthquake.ProbEqkSource;

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ERFFrankel02Client extends EqkRupForecast implements ParameterChangeListener {

  private ERFFrankel02Server erfServer = null;

  String remoteName = "ERFFrankel02Server";
  public ERFFrankel02Client() throws Exception {
    String dnsString = "rmi://gravity.usc.edu:1099/" + remoteName;
    System.out.println("Constructor of ERFClient");
    try {
      //System.setSecurityManager(new java.rmi.RMISecurityManager());
      erfServer = (ERFFrankel02Server) Naming.lookup(dnsString);
      System.out.println("erfserver:"+erfServer);
      System.out.println("ERFSErver lookup successful");
    }
    catch (NotBoundException n) {
      System.out.println("Constructor of ERFClient Not Bound");
      throw new Exception(n.getMessage());
    }
    catch (MalformedURLException m) {
      System.out.println("Constructor of ERFClient Malformed");
      throw new Exception(m.getMessage());
    }
    catch (java.rmi.UnmarshalException u) {
      System.out.println("Constructor of ERFClient unmarshalExeption");
      throw new Exception(u.getMessage());
    }
    catch (RemoteException r) {
      System.out.println("Constructor of ERFClient remoteExeption");
      throw new Exception(r.getMessage());
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
  public void parameterChange(ParameterChangeEvent event) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.scec.sha.earthquake.EqkRupForecastAPI#updateForecast()
   */
  public void updateForecast() {
    // TODO Auto-generated method stub
    try {
      erfServer.updateForecast(adjustableParams, timeSpan);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.scec.sha.earthquake.EqkRupForecastAPI#updateForecast()
   */
  public String updateAndSaveForecast() {
    // TODO Auto-generated method stub
    try {
      return erfServer.updateAndSaveForecast(adjustableParams,
                                             timeSpan);
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
      this.timeSpan = erfServer.getTimeSpan();
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
      adjustableParams = erfServer.getAdjustableParameterList();
      return adjustableParams;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}