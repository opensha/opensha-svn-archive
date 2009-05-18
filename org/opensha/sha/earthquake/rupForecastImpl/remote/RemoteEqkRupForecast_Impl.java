
package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteEqkRupForecastAPI;
import org.opensha.sha.earthquake.EqkRupForecast;


/**
 *
 * <p>Title: RemoteEqkRupForecast_Impl </p>
 * <p>Description: This class wraps the ERFs for remote access </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta, Vipin Gupta
 * @version 1.0
 */
public class RemoteEqkRupForecast_Impl
	extends UnicastRemoteObject
	implements RemoteEqkRupForecastAPI{

   private EqkRupForecast eqkRupForecast = null;
   private static final boolean D = false;


   /**
    * creates the EqkRupForecast object based on received className
    *
    * @param className
    * @throws java.rmi.RemoteException
    * @throws IOException
    */
   public RemoteEqkRupForecast_Impl(String className)
       throws java.rmi.RemoteException, IOException {
     eqkRupForecast = (EqkRupForecast)org.opensha.util.ClassUtils.createNoArgConstructorClassInstance(className);
   }

   /**
    * Creates the EqkRupForecast object with arguments
    * @param params : object array to create the constructor for the ERF
    * @param className : ERF object class name.
    * @throws java.rmi.RemoteException
    * @throws IOException
    */
   public RemoteEqkRupForecast_Impl(ArrayList params,ArrayList paramTypes,String className)
       throws java.rmi.RemoteException, IOException {
     eqkRupForecast = (EqkRupForecast)org.opensha.util.ClassUtils.createNoArgConstructorClassInstance(params,paramTypes,className);;
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
    */
   public void updateForecast() throws
       RemoteException {
     eqkRupForecast.updateForecast();
     // TODO Auto-generated method stub
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
    public boolean setParameter(String name, Object value) throws
       RemoteException{
      return eqkRupForecast.setParameter(name,value);
    }




   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#updateForecast()
    */
   public String saveForecast() throws
       RemoteException {
     String urlPrefix = "http://gravity.usc.edu/";
     String parentDir = "/opt/install/apache-tomcat-5.5.20/webapps/";
     String subDir = "OpenSHA/HazardMapDatasets/savedERFs/";
     String fileName = System.currentTimeMillis() + ".javaobject";
     org.opensha.util.FileUtils.saveObjectInFile(parentDir + subDir + fileName,
                                              eqkRupForecast);
     return parentDir + subDir + fileName;
   }




   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getName()
    */
   public String getName() throws RemoteException {
     return eqkRupForecast.getName();
     // TODO Auto-generated method stub
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#setTimeSpan(org.opensha.data.TimeSpan)
    */
   public void setTimeSpan(TimeSpan time) throws RemoteException {
     eqkRupForecast.setTimeSpan(time);
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getTimeSpan()
    */
   public TimeSpan getTimeSpan() throws RemoteException {
     TimeSpan timeSpan = eqkRupForecast.getTimeSpan();
     // TODO Auto-generated method stub
     return timeSpan;
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParamsIterator()
    */
   public ListIterator getAdjustableParamsIterator() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getAdjustableParamsIterator();
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#isLocWithinApplicableRegion(org.opensha.data.Location)
    */
   public boolean isLocWithinApplicableRegion(Location loc) throws
       RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.isLocWithinApplicableRegion(loc);
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getApplicableRegion()
    */
   public GeographicRegion getApplicableRegion() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getApplicableRegion();
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getAdjustableParameterList()
    */
   public ParameterList getAdjustableParameterList() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getAdjustableParameterList();
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getNumSources()
    */
   public int getNumSources() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getNumSources();
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getSourceList()
    */
   public ArrayList getSourceList() throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getSourceList();
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getSource(int)
    */
   public ProbEqkSource getSource(int iSource) throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getSource(iSource);
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
   public ProbEqkSource getSourceClone(int iSource) throws RemoteException{
     // TODO Auto-generated method stub
     return eqkRupForecast.getSourceClone(iSource);
   }



   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getNumRuptures(int)
    */
   public int getNumRuptures(int iSource) throws RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getNumRuptures(iSource);
   }

   /* (non-Javadoc)
    * @see org.opensha.sha.earthquake.rupForecastImpl.Frankel02.ERFFrankel02Server#getRupture(int, int)
    */
   public ProbEqkRupture getRupture(int iSource, int nRupture) throws
       RemoteException {
     // TODO Auto-generated method stub
     return eqkRupForecast.getRupture(iSource, nRupture);
   }


   /**
    *
    * @param paramName
    * @returns the Parameter from the parameter list with param name.
    */
   public ParameterAPI getParameter(String paramName) throws RemoteException{
     // TODO Auto-generated method stub
     return eqkRupForecast.getParameter(paramName);
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
   public ProbEqkRupture getRuptureClone(int iSource, int nRupture) throws RemoteException{
     return eqkRupForecast.getRuptureClone(iSource,nRupture);
   }


}

