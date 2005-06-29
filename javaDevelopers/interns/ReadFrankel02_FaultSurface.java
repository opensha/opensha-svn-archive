package javaDevelopers.interns;

import java.rmi.RemoteException;
import java.util.*;

import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.sha.earthquake.*;

/**
 * <p>Title: ReadFrankel02_FaultSurface</p>
 *
 * <p>Description: This class creates the instance of the Frankel-02 and
 * then loop over all the sources for the ERF and return the gridded surface of these
 * sources to be plotted.</p>
 *
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class ReadFrankel02_FaultSurface {


  private Frankel02_AdjustableEqkRupForecastClient frankelForecast;

  /**
   * Creating the instance of the Frankel02 forecast
   */
  private void createFrankel02Forecast() throws RemoteException {
    frankelForecast = new Frankel02_AdjustableEqkRupForecastClient();
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
                                 BACK_SEIS_EXCLUDE);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(10.0));
    frankelForecast.getTimeSpan().setDuration(50.0);
    frankelForecast.updateForecast();
  }


  /**
   * ArrayList containing GridddedSurfaceAPI object.
   * Using these object user can iterate over the getLocationIterator method
   * and extract the location to be plotted.
   * @return ArrayList
   */
  public ArrayList getSourceList(){
    // get total number of sources
    int numSources = frankelForecast.getNumSources();
    ArrayList sourceSurfaceList = new ArrayList();

    /**
     * Loops over all the Sources and EqkRuptures for Frankel-02 and add their
     * surface as GriddedSurfaceAPI object to the ArrayList.
     */
    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex){
      ProbEqkSource eqkSource= frankelForecast.getSource(sourceIndex);
      int numRuptures = eqkSource.getNumRuptures();
      for(int i=0;i<numRuptures;++i){
        ProbEqkRupture rupture = eqkSource.getRupture(i);
        sourceSurfaceList.add(rupture.getRuptureSurface());
      }
    }

    return sourceSurfaceList;
  }




}
