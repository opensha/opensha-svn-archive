
package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.data.Location;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.GeographicRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteEqkRupForecastAPI;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;


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
     String parentDir = "/opt/install/jakarta-tomcat-4.1.24/webapps/";
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

   /**
    * This function returns the total probability of events above a given magnitude
    * within the given geographic region.  The calcuated Rates depend on the  ERF
    * subclass.  Note that it is assumed that the forecast has been updated.
    * @param minMag double  : magnitude above which rate needs to be returned
    *
    * @param region GeographicRegion : Region whose rates need to be returned
    * @return double : Total Rate for the region
    */
   public double getTotalProbAbove(double minMag, GeographicRegion region) throws RemoteException{
     return eqkRupForecast.getTotalProbAbove(minMag,region);
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
   public double getTotalRateAbove(double minMag, GeographicRegion region) throws
       RemoteException {
     return eqkRupForecast.getTotalRateAbove(minMag, region);
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
       EvenlyGriddedGeographicRegionAPI region) throws RemoteException {
     return eqkRupForecast.getMagRateDistForEachLocationInRegion(minMag, region);
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
       EvenlyGriddedGeographicRegionAPI region) throws RemoteException {
     return eqkRupForecast.getTotalSeisRateAtEachLocationInRegion(minMag, region);
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
       GeographicRegion region) throws RemoteException {
     return eqkRupForecast.getMagRateDistForRegion(minMag, region);
   }

}

