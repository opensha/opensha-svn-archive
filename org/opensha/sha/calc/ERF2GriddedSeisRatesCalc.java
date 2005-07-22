package org.opensha.sha.calc;

import java.util.*;
import java.text.*;

import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.data.region.*;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.Location;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.
    Frankel02_AdjustableEqkRupForecastClient;
import java.rmi.*;
import org.opensha.exceptions.*;
import java.io.FileWriter;
import java.io.*;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.
    Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.DataObject2D;

/**
 * <p>Title: ERF2GriddedSeisRatesCalc</p>
 *
 * <p>Description: This class calculates the rates of the Ekq Rupture for the given magnitude.
 * </p>
 * <p>
 * This class provides the functionality of computing Mag-Rate distribution or
 * total seismic rates using the Earthquake Ruptures from the provided Earthquake
 * Rupture Forecast with a given Geographic Region.
 * </p>
 * @author Nitin Gupta , Vipin Gupta
 * @version 1.0
 */
public class ERF2GriddedSeisRatesCalc {



  /**
   * This function computes the rates above the given Magnitude for each rupture
   * location. Once computed , magnitude-rate distribution is stored for each
   * location on all ruptures in Eqk Rupture forecast model, if that lies within the
   * provided EvenlyGriddedGeographicRegion.
   * Once all Mag-Rate distribution has been computed for each location within the
   * ERF, this function returns list of DataObject2D object that constitutes of
   * 2 objects, with X object being Location and Y object being
   * ArbitrarilyDiscretizedFunc. This ArbitrarilyDiscretizedFunc for each location
   * is the Mag-Rate distribution with X values being Mag and Y values being Rate.
   * @param mag double : Magnitude above which Mag-Rate distribution is to be computed.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Ruptureforecast model
   * @param region EvenlyGriddedGeographicRegionAPI Region within which ruptures
   * are to be considered.
   * @return ArrayList of DataObject2D with X object being Location and Y object
   * being ArbitrarilyDiscretizedFunc
   * @see ArbitrarilyDiscretizedFunc, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion
   */
  public ArrayList getMagRateDistForEachLocationInRegion(double mag, EqkRupForecastAPI eqkRupForecast,
                               EvenlyGriddedGeographicRegionAPI region) {
      return eqkRupForecast.getMagRateDistForEachLocationInRegion(mag,region);
  }


  /**
   * This function computes the total SiesRate for each location on all the ruptures,
   * if they are within the provided Geographical Region.
   * It returns a list of DataObject2D that store location as X-Object and its
   * corresponding total seis rate(Double Object) as Y-Object.
   * @param mag double : Only those ruptures above this magnitude are considered
   * for calculation of the total seis rates in the region.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture forecast model
   * @param region EvenlyGriddedGeographicRegionAPI
   * @return ArrayList of DataObject2D with X object being Location and Y object
   * being Double Object respresenating the TotalSeisRate
   * @see Double, Location, EvenlyGriddedGeographicRegion,
   * EvenlyGriddedGeographicRegionAPI, EvenlyGriddedRectangularGeographicRegion

   */
  public ArrayList getTotalSeisRateAtEachLocationInRegion(double mag, EqkRupForecastAPI eqkRupForecast,
                              EvenlyGriddedGeographicRegionAPI region) {
    return eqkRupForecast.getTotalSeisRateAtEachLocationInRegion(mag,region);
 }



  /**
   * This function returns the total Rate above a given magnitude ,
   * for the given geographic region.
   * Calcuated Rates depend on the ERF model instantiated by the user.
   * @param magnitude double  : A magnitude above which rate needs to be returned
   * @param eqkRupForecast Earthquake Rupture Forecast Model
   * @param region GeographicRegion : Region whose rates need to be returned
   * @return double : Total Rate for the region
   */

  public double getTotalSeisRateInRegion(double mag, EqkRupForecastAPI eqkRupForecast,
                                  GeographicRegion region) {

    return eqkRupForecast.getTotalRateAbove(mag, region);
  }


  /**
   * This function returns the ArbDiscrEmpirical object that holds the
   * Mag-Rate of the entire region.
   * @param magnitude double  Ruptures above this magnitude will be the ones that
   * will considered within the provided region  for computing the Mag-Rate Dist.
   * @param eqkRupForecast EqkRupForecastAPI Earthquake Rupture Forecast from which
   * ruptures will computed.
   * @param region GeographicRegion Region for which mag-rate distribution has to be
   * computed.
   * @return ArbDiscrEmpiricalDistFunc : Distribution function that holds X values
   * as the magnitude and Y values as the sies rate for corresponding magnitude within
   * the region.
   */
  public ArbDiscrEmpiricalDistFunc getMagRateDistForRegion(double magnitude, EqkRupForecastAPI eqkRupForecast,
                                  GeographicRegion region) {
          return eqkRupForecast.getMagRateDistForRegion(magnitude,region);
  }


  /**
   *
   * @param args String[]
   */
  public static void main(String[] args) {
    ERF2GriddedSeisRatesCalc erf2griddedseisratescalc = new
        ERF2GriddedSeisRatesCalc();

    Frankel02_AdjustableEqkRupForecast frankelForecast = null;

    //try {
      frankelForecast = new
          Frankel02_AdjustableEqkRupForecast();
    //}
    //catch (RemoteException ex) {
     // ex.printStackTrace();
    //}

    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
                                 BACK_SEIS_INCLUDE);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_NAME).
        setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_RUP_FINITE);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(10.0));
    frankelForecast.getTimeSpan().setDuration(50.0);
    frankelForecast.updateForecast();

    try {
      EvenlyGriddedRectangularGeographicRegion region =
          new EvenlyGriddedRectangularGeographicRegion(26.1, 50.0, -125,
          -101.4, 0.1);
      /*HashMap map = erf2griddedseisratescalc.getRatesAbove(5.0, frankelForecast,
          region);
      Set set = map.keySet();
      Iterator it = set.iterator();
      try {
        FileWriter fw = new FileWriter("magRates.txt");
        while (it.hasNext()) {
          Location loc = (Location) it.next();
          Double rate = (Double) map.get(loc);
          fw.write(loc.toString() + "\t" + rate.doubleValue() + "" + "\n");
        }
        fw.close();
      }
      catch (IOException ex2) {
      }*/
  ArbDiscrEmpiricalDistFunc func = erf2griddedseisratescalc.getMagRateDistForRegion(5.0, frankelForecast,
          region);
  try {
          FileWriter fw = new FileWriter("magRates.txt");
            fw.write(func.toString()+ "\n");
          fw.close();
  } catch (IOException ex2) {
      }
    }
    catch (RegionConstraintException ex1) {
    }
  }
}
