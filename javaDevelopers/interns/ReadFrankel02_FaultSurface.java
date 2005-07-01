package javaDevelopers.interns;

import java.util.*;
import java.io.*;

import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.Location;


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


  private Frankel02_AdjustableEqkRupForecast frankelForecast;

  /**
   * Creating the instance of the Frankel02 forecast
   */
  private void createFrankel02Forecast(){
    frankelForecast = new Frankel02_AdjustableEqkRupForecast();
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
  public void createNSHMP_CharSourcesFile() throws IOException {

    ArrayList sourceSurfaceList = frankelForecast.getAllCharFaultSources();

    int size = sourceSurfaceList.size();
    FileWriter fw = new FileWriter("NSHMP_CharSourceGridFile.txt");

    fw.write("# Total number of Characterstic fault Sources\n");
    fw.write(""+size+"\n");

    /**
     * Loop over all the Char Sources  for Frankel-02 and add their
     * surface as GriddedSurfaceAPI object to the ArrayList.
     */
    for (int sourceIndex = 0; sourceIndex < size; ++sourceIndex){
      FaultRuptureSource source = (FaultRuptureSource)sourceSurfaceList.get(sourceIndex);
      GriddedSurfaceAPI surface = source.getSourceSurface();
      fw.write(source.getName()+"\n");
      fw.write(""+surface.getNumRows()*surface.getNumCols()+"\n");
      ListIterator it = surface.getLocationsIterator();
      while(it.hasNext())
        fw.write(((Location)it.next()).toString()+"\n");
      fw.write("\n");
    }
    fw.close();
  }


  public static void main(String[] args) {
    ReadFrankel02_FaultSurface nshmpSources = new ReadFrankel02_FaultSurface();
    nshmpSources.createFrankel02Forecast();
    try {
      nshmpSources.createNSHMP_CharSourcesFile();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

}
