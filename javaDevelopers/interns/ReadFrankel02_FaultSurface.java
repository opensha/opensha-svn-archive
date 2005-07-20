package javaDevelopers.interns;

import java.util.*;
import java.io.*;

import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.Location;
import org.opensha.param.ParameterConstraintAPI;
import org.opensha.param.StringConstraint;

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


  private Frankel02_AdjustableEqkRupForecast frankelForecast = new Frankel02_AdjustableEqkRupForecast();

  public ReadFrankel02_FaultSurface(){
    createFrankel02Forecast();
  }

  /**
   * Creating the instance of the Frankel02 forecast
   */
  private void createFrankel02Forecast(){
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.
                                 BACK_SEIS_EXCLUDE);
    frankelForecast.getAdjustableParameterList().getParameter(
        Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(10.0));
    frankelForecast.getTimeSpan().setDuration(50.0);
    setFaultModelInFrankelForecast(frankelForecast.FAULT_MODEL_FRANKEL);
    frankelForecast.updateForecast();
  }


  /**
   * This functions returns the different types of fault models supported.
   * This allows user to supported Fault models and change them accordingly in the
   * Frankel forecast model.
   * @return String[]
   */
  public String[] getSupportedFaultModes() {
    //ArrayList faultModelsList = (ArrayList)
    ParameterConstraintAPI constraint = frankelForecast.getAdjustableParameterList().getParameter(
      Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_NAME).getConstraint();
    ArrayList faultModelsList = ((StringConstraint)constraint).getAllowedStrings();
    String[] faultModels = new String[faultModelsList.size()];
    for(int i=0;i<faultModels.length;++i)
      faultModels[i] = (String)faultModelsList.get(i);

    return faultModels;
  }


  /**
   * Sets the Fault model name in the Frankel Forecast model
   * @param faultModel String : Fault model Name
   * User must call getSupportedFaultModes() to know
   * which fault model are supported.
   * Fault models returned from the getSupportedFaultModes() can be provided
   * as the arguments to this function.
   */
  public void setFaultModelInFrankelForecast(String faultModel){
    frankelForecast.getAdjustableParameterList().getParameter(
      Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_NAME).setValue(faultModel);
  }


  /**
   * Returns the list of Fault Rupture Source
   * @return FaultRuptureSource[]
   */
  public FaultRuptureSource[] getNSHMP_CharSourceFile(){

   ArrayList faultsources =  frankelForecast.getAllCharFaultSources();

   int size = faultsources.size();
   FaultRuptureSource[] faultruptureSources = new FaultRuptureSource[size];
   for(int i=0;i<size;++i)
     faultruptureSources[i] = (FaultRuptureSource)faultsources.get(i);

   return faultruptureSources;
  }




  /**
   * ArrayList containing GridddedSurfaceAPI object.
   * Using these object user can iterate over the getLocationIterator method
   * and extract the location to be plotted.
   * @return ArrayList
   */
  /*public void createNSHMP_CharSourcesFile() throws IOException {

    ArrayList sourceSurfaceList = frankelForecast.getAllCharFaultSources();

    int size = sourceSurfaceList.size();
    FileWriter fw = new FileWriter("NSHMP_CharSourceGridFile.txt");

    fw.write("# Total number of Characterstic fault Sources\n");
    fw.write(""+size+"\n");*/

    /**
     * Loop over all the Char Sources  for Frankel-02 and add their
     * surface as GriddedSurfaceAPI object to the ArrayList.
     */
    /*for (int sourceIndex = 0; sourceIndex < size; ++sourceIndex){
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
  }*/


  /*public static void main(String[] args) {
    ReadFrankel02_FaultSurface nshmpSources = new ReadFrankel02_FaultSurface();
    nshmpSources.createFrankel02Forecast();
    try {
      nshmpSources.createNSHMP_CharSourcesFile();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }*/

}
