
package org.scec.sha.earthquake.rupForecastImpl.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.data.region.GeographicRegion;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterList;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.earthquake.ProbEqkSource;
import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_API;
import org.scec.sha.earthquake.EqkRupForecast;

/**
 *
 * <p>Title: RemoteERF_Impl.java </p>
 * <p>Description: This class wraps the ERFs for remote access </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */
public class RemoteERF_Impl
	extends UnicastRemoteObject
	implements RemoteERF_API {

   private EqkRupForecast eqkRupForecast = null;
   private static final boolean D = false;

   /**
    * creates the EqkRupForecast object based on received className
    *
    * @param className
    * @throws java.rmi.RemoteException
    * @throws IOException
    */
   public RemoteERF_Impl(String className)
       throws java.rmi.RemoteException, IOException {
     eqkRupForecast = (EqkRupForecast)org.scec.util.ClassUtils.createNoArgConstructorClassInstance(className);;
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
    */
   public void updateForecast(ParameterList list, TimeSpan timeSpan) throws
       RemoteException {
     eqkRupForecast.setParameterChangeFlag(true);
     Iterator it = list.getParametersIterator();
     while (it.hasNext()) {
       ParameterAPI param = (ParameterAPI) it.next();
       eqkRupForecast.getParameter(param.getName()).setValue(param.getValue());
       if(D) System.out.println("Param Name:"+param.getName()+",value="+param.getValue());
     }
     eqkRupForecast.setTimeSpan(timeSpan);
     eqkRupForecast.updateForecast();
     // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
    */
   public String updateAndSaveForecast(ParameterList list, TimeSpan timeSpan) throws
       RemoteException {
     this.updateForecast(list, timeSpan);
     String urlPrefix = "http://gravity.usc.edu/";
     String parentDir = "/opt/install/jakarta-tomcat-4.1.24/webapps/";
     String subDir = "OpenSHA/HazardMapDatasets/savedERFs/";
     String fileName = System.currentTimeMillis() + ".javaobject";
     org.scec.util.FileUtils.saveObjectInFile(parentDir + subDir + fileName,
                                              eqkRupForecast);
     return parentDir + subDir + fileName;
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getName()
    */
   public String getName() throws RemoteException {
     return eqkRupForecast.getName();
     // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#setTimeSpan(org.scec.data.TimeSpan)
    */
   public void setTimeSpan(TimeSpan time) throws RemoteException {
     eqkRupForecast.setTimeSpan(time);
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getTimeSpan()
    */
   public TimeSpan getTimeSpan() throws RemoteException {
     TimeSpan timeSpan = eqkRupForecast.getTimeSpan();
     // TODO Auto-generated method stub
     return timeSpan;
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParamsIterator()
    */
   public ListIterator getAdjustableParamsIterator() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getAdjustableParamsIterator();
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#isLocWithinApplicableRegion(org.scec.data.Location)
    */
   public boolean isLocWithinApplicableRegion(Location loc) throws
       RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.isLocWithinApplicableRegion(loc);
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getApplicableRegion()
    */
   public GeographicRegion getApplicableRegion() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getApplicableRegion();
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParameterList()
    */
   public ParameterList getAdjustableParameterList() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getAdjustableParameterList();
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getNumSources()
    */
   public int getNumSources() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getNumSources();
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getSourceList()
    */
   public ArrayList getSourceList() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getSourceList();
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getSource(int)
    */
   public ProbEqkSource getSource(int iSource) throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getSource(iSource);
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getNumRuptures(int)
    */
   public int getNumRuptures(int iSource) throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getNumRuptures(iSource);
   }

   /* (non-Javadoc)
    * @see org.scec.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getRupture(int, int)
    */
   public ProbEqkRupture getRupture(int iSource, int nRupture) throws
       RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getRupture(iSource, nRupture);
   }

}
