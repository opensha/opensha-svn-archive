package gov.usgs.sha.data;

import gov.usgs.sha.data.api.DataGeneratorAPI_NEHRP;
import java.util.ArrayList;
import gov.usgs.exceptions.ZipCodeErrorException;
import org.scec.data.function.DiscretizedFuncList;
import gov.usgs.util.GlobalConstants;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: DataGenerator_NEHRP</p>
 *
 * <p>Description: </p>
 * @author Ned Field, Nitin Gupta , E.V.Leyendecker
 * @version 1.0
 */
public class DataGenerator_NEHRP
    implements DataGeneratorAPI_NEHRP {

  //gets the selected region
  private String geographicRegion;
  //gets the selected edition
  private String dataEdition;

  private ArbitrarilyDiscretizedFunc saFunction;

  private DiscretizedFuncList mapSpectrumFunctions;
  private DiscretizedFuncList smSpectrumFunctions;
  private DiscretizedFuncList sdSpectrumFunctions;
  private float faVal;
  private float fvVal;
  private String siteClass;

  //holds all the data and its info in a String format.
  private String dataInfo = "";

  /**
   * Default class constructor
   */
  public DataGenerator_NEHRP() {}



  /**
   * Removes all the calculated data.
   */
  public void clearData() {
    dataInfo = "";
  }

  /**
   * Returns the Data and all the metadata associated with it in a String.
   * @return String
   */
  public String getDataInfo() {
    return dataInfo;
  }


  private void addDataInfo(String data){
    dataInfo += geographicRegion + "\n";
    dataInfo += dataEdition + "\n";
    dataInfo +=data+"\n\n";
  }

  /**
   * Gets the data for SsS1 in case Territory.
   * Territory is when user is not allowed to enter any zip code or Lat-Lon
   * for the location or if it is GAUM and TAUTILLA.
   */
  public void calculateSsS1() {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion);
    addDataInfo(function.getInfo());
    saFunction =function;

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies Lat-Lon for the location.
   */
  public void calculateSsS1(double lat, double lon) {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                 lat, lon);
    addDataInfo(function.getInfo());
    saFunction = function;

  }

  /**
   * Gets the data for SsS1 in case region specified is not a Territory and user
   * specifies zip code for the location.
   */
  public void calculateSsS1(String zipCode) throws ZipCodeErrorException {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSsS1(geographicRegion, dataEdition,
                                                 zipCode);
    addDataInfo(function.getInfo());
    saFunction = function;
  }


  /**
   *
   */
  public void calculateSMSsS1() {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSMSsS1(saFunction,faVal,fvVal,siteClass);
    addDataInfo(function.getInfo());
  }


  /**
   *
   *
   */
  public void calculatedSDSsS1() {

    HazardDataMiner miner = new HazardDataMiner();
    ArbitrarilyDiscretizedFunc function = miner.getSDSsS1(saFunction, faVal,
        fvVal,siteClass);
    addDataInfo(function.getInfo());
  }


  /**
   *
   *
   */
  public void calculateMapSpectrum(){
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getMapSpectrum(saFunction);
    addDataInfo(functions.getInfo());
    mapSpectrumFunctions= functions;
  }



  /**
   *
   * @return DiscretizedFuncList
   */
  public ArrayList getFunctionsForMapSpectrum(){
    DiscretizedFuncList saTfunctions = new DiscretizedFuncList();
    DiscretizedFuncList sasdFunctions = new DiscretizedFuncList();
    ArbitrarilyDiscretizedFunc saTFunction=null;
    ArbitrarilyDiscretizedFunc sasdFunction = new ArbitrarilyDiscretizedFunc();
    int numFunctions = mapSpectrumFunctions.size();
    int i=0;
    for(;i<numFunctions;++i){
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)mapSpectrumFunctions.get(i);
      if(tempFunction.getName().equals(GlobalConstants.MCE_SPECTRUM_SA_Vs_T_GRAPH)){
        saTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction =(ArbitrarilyDiscretizedFunc)mapSpectrumFunctions.get(1-i);

    int numPoints = tempSDFunction.getNum();
    for(int j=0;j<numPoints;++j)
      sasdFunction.set(saTFunction.getY(j),tempSDFunction.getY(j));

    sasdFunctions.setName(GlobalConstants.MCE_SPECTRUM_SA_Vs_SD_GRAPH);
    saTfunctions.setName(GlobalConstants.MCE_SPECTRUM_SA_Vs_T_GRAPH);
    sasdFunctions.add(sasdFunction);
    saTfunctions.add(saTFunction);
    ArrayList functions = new ArrayList();
    functions.add(saTfunctions);
    functions.add(sasdFunctions);
    return functions;
  }


  /**
   *
   * @return DiscretizedFuncList
   */
  public ArrayList getFunctionsForSMSpectrum(){
    DiscretizedFuncList saTFunctions = new DiscretizedFuncList();
    DiscretizedFuncList sasdFunctions = new DiscretizedFuncList();
    ArbitrarilyDiscretizedFunc saTFunction=null;
    ArbitrarilyDiscretizedFunc sasdFunction = new ArbitrarilyDiscretizedFunc();
    int numFunctions = smSpectrumFunctions.size();
    int i=0;
    for(;i<numFunctions;++i){
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)smSpectrumFunctions.get(i);
      if(tempFunction.getName().equals(GlobalConstants.SITE_MODIFIED_SA_Vs_T_GRAPH)){
        saTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction =(ArbitrarilyDiscretizedFunc)smSpectrumFunctions.get(1-i);

    int numPoints = tempSDFunction.getNum();

    for(int j=0;j<numPoints;++j)
      sasdFunction.set(saTFunction.getY(j),tempSDFunction.getY(j));

    sasdFunctions.setName(GlobalConstants.SITE_MODIFIED_SA_Vs_SD_GRAPH);
    saTFunctions.setName(GlobalConstants.SITE_MODIFIED_SA_Vs_T_GRAPH);
    sasdFunctions.add(sasdFunction);
    saTFunctions.add(saTFunction);

    ArrayList functions = new ArrayList();

    functions.add(sasdFunctions);
    functions.add(saTFunctions);

    return functions;
  }

  /**
   *
   * @return DiscretizedFuncList
   */
  public ArrayList getFunctionsForSDSpectrum(){
    DiscretizedFuncList sasdFunctions = new DiscretizedFuncList();
    DiscretizedFuncList saTFunctions = new DiscretizedFuncList();
    ArbitrarilyDiscretizedFunc saTFunction=null;
    ArbitrarilyDiscretizedFunc sasdFunction = new ArbitrarilyDiscretizedFunc();
    int numFunctions = sdSpectrumFunctions.size();
    int i=0;
    for(;i<numFunctions;++i){
      ArbitrarilyDiscretizedFunc tempFunction = (ArbitrarilyDiscretizedFunc)sdSpectrumFunctions.get(i);
      if(tempFunction.getName().equals(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_T_GRAPH)){
        saTFunction = tempFunction;
        break;
      }
    }

    ArbitrarilyDiscretizedFunc tempSDFunction =(ArbitrarilyDiscretizedFunc)smSpectrumFunctions.get(1-i);

    int numPoints = tempSDFunction.getNum();

    for(int j=0;j<numPoints;++j)
      sasdFunction.set(saTFunction.getY(j),tempSDFunction.getY(j));

    sasdFunctions.setName(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_SD_GRAPH);
    saTFunctions.setName(GlobalConstants.DESIGN_SPECTRUM_SA_Vs_T_GRAPH);

    sasdFunctions.add(sasdFunction);
    saTFunctions.add(saTFunction);


    ArrayList functions = new ArrayList();
    functions.add(sasdFunction);
    functions.add(saTFunction);
    return functions;
  }



  /**
   *
   *
   */
  public void calculateSMSpectrum(){
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSMSpectrum(saFunction, faVal,
        fvVal,siteClass);
    addDataInfo(functions.getInfo());
    smSpectrumFunctions = functions;
  }

  /**
   *
   */
  public void calculateSDSpectrum(){
    HazardDataMiner miner = new HazardDataMiner();
    DiscretizedFuncList functions = miner.getSDSpectrum(saFunction, faVal,
        fvVal,siteClass);
    addDataInfo(functions.getInfo());
    sdSpectrumFunctions = functions;
  }

  /**
   * Sets the selected geographic region.
   * @param region String
   */
  public void setRegion(String region) {
    geographicRegion = region;
  }

  /**
   * Sets the selected data edition.
   * @param edition String
   */
  public void setEdition(String edition) {
    dataEdition = edition;
  }

  /**
   * Sets the Fa value.
   * @param fa double
   */
  public void setFa(float fa) {
    faVal = fa;
  }

  /**
   * Sets the Fv value.
   * @param fv double
   */
  public void setFv(float fv) {
    fvVal = fv;
  }


  /**
   * Sets the selected site class
   * @param siteClass String
   */
  public void setSiteClass(String siteClass){
    this.siteClass = siteClass;
  }
}
