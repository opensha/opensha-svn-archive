package org.scec.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.Constructor;

import org.scec.data.region.*;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.imr.*;
import org.scec.sha.calc.ScenarioShakeMapCalculatorWithPropagationEffect;
import org.scec.sha.earthquake.*;
import org.scec.data.XYZ_DataSetAPI;
import org.scec.data.ArbDiscretizedXYZ_DataSet;
import org.scec.sha.gui.infoTools.IMT_Info;
import org.scec.util.FileUtils;

/**
 * <p>Title: ScenarioShakeMapCalcServlet  </p>
 * <p>Description: This servlet hosted at gravity.usc.edu, accepts parameters
 * to do the ScenarioShakeMap calculation on the server.
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ScenarioShakeMapCalcServlet  extends HttpServlet implements ParameterChangeWarningListener{


  //path on the server where all the object will be stored
  private final static String FILE_PATH= "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/MapCalculationSavedObjects/";
  private final static String XYZ_DATA_DIR = "xyzDataObject/" ;


  /**
   * method to get the XYZ data for the scenarioshakemap after doing the calculation.
   *
   * @param request
   * @param response
   * @throws IOException
   * @throws ServletException
   * @return the XYZ data representing the either the IML values or prob for the selected region.
   */
  public void doGet(HttpServletRequest request,  HttpServletResponse response)
                                  throws IOException, ServletException {


    try {

      //gets the current time in milliseconds to be the new file for the xyz data object file
      String xyzDataFileName ="";
      xyzDataFileName += System.currentTimeMillis()+".obj";
      //all the user gmt stuff will be stored in this directory
      File mainDir = new File(FILE_PATH+XYZ_DATA_DIR);
      //create the main directory if it does not exist already
      if(!mainDir.isDirectory()){
        boolean success = (new File(FILE_PATH+XYZ_DATA_DIR)).mkdir();
      }

      // get all the input stream from the applet
      ObjectInputStream inputFromApplication = new ObjectInputStream(request.getInputStream());
      //gets the inputs from the Application.

      //gets the selected AttenuationRelationships
      ArrayList selectedAttenRels = (ArrayList)inputFromApplication.readObject();

      //gets the selected AttenRel Absolute Wts
      ArrayList selectedAttenRelWts = (ArrayList)inputFromApplication.readObject();

      //gets the selected region object form the application
      String griddedRegionFile = (String)inputFromApplication.readObject();
      SitesInGriddedRegion griddedRegion = (SitesInGriddedRegion)FileUtils.loadObject(griddedRegionFile);

      //gets the selected EqkRupture object form the application
      EqkRupture rupture = (EqkRupture)inputFromApplication.readObject();

      //gets the boolean to if IML@Prob or Prob@IML
      boolean isProbAtIML = ((Boolean)inputFromApplication.readObject()).booleanValue();

      //the IML or Prob value to compute the map for
      double value = ((Double)inputFromApplication.readObject()).doubleValue();

      //gets the selected IMT
      String selectedIMT = (String)inputFromApplication.readObject();

      //close of the input from the application
      inputFromApplication.close();

      //adds the parameter change listener event to the parameters of the selected AttenRels
      getIMR_ParametersAndAddListeners(selectedAttenRels);

      //creating the object for the ScenarioShakeMapCalculator to compute the XYZ data for the selected region
      ScenarioShakeMapCalculatorWithPropagationEffect calc = new ScenarioShakeMapCalculatorWithPropagationEffect();
      //sending the output in the form of the arrayList back to the calling application.
      ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
      ArbDiscretizedXYZ_DataSet xyzData = null;
      if(!selectedIMT.equals(AttenuationRelationship.PGV_NAME)){
      //XYZ data for the scenarioshake as computed
        xyzData = (ArbDiscretizedXYZ_DataSet)calc.getScenarioShakeMapData(selectedAttenRels,selectedAttenRelWts,
        griddedRegion,rupture,isProbAtIML,value);
        convertIML_ValuesToExpo(xyzData,selectedIMT,isProbAtIML);
      }
      else
        xyzData = (ArbDiscretizedXYZ_DataSet)getXYZDataForPGV(selectedAttenRels,selectedAttenRelWts,griddedRegion,rupture,
                                   isProbAtIML,value,calc);

      //absolute path to the xyz data object file
      String xyzDataFileWithAbsolutePath = FILE_PATH+XYZ_DATA_DIR+xyzDataFileName;

      //writes the XYZ data object to the file
      createXYZDataObjectFile(xyzData,xyzDataFileWithAbsolutePath);

      //calculates the XYZ data for the ScenarioShakeMap and returns back
     //the path to the XYZ data object file, to the application.
      output.writeObject(xyzDataFileWithAbsolutePath);

      output.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This function converts the IML value to exponential value if IMT selected is
   * log normal supported, becuase IMR's return the IML values in the log space.
   * @param xyzData : XYZ data set
   * @param selectedIMT : choosen IMT in the application
   * @param isProbAtIML : if prob@IML is selected
   */
  private void convertIML_ValuesToExpo(ArbDiscretizedXYZ_DataSet xyzData,
                                       String selectedIMT,boolean isProbAtIML){
    //if the IMT is log supported then take the exponential of the Value if IML @ Prob
    if(IMT_Info.isIMT_LogNormalDist(selectedIMT) && !isProbAtIML){
      ArrayList zVals = xyzData.getZ_DataSet();
      int size = zVals.size();
      for(int i=0;i<size;++i){
        double tempVal = Math.exp(((Double)(zVals.get(i))).doubleValue());
        zVals.set(i,new Double(tempVal));
      }
    }
  }


  /**
   *
   * @param selectedIMRs
   * @param selectedWts
   * @param region
   * @param rupture
   * @param isProbAtIML
   * @param value
   * @return
   */
  private XYZ_DataSetAPI getXYZDataForPGV(ArrayList selectedIMRs,ArrayList selectedWts,
      SitesInGriddedRegion region,EqkRupture rupture, boolean isProbAtIML, double value,
    ScenarioShakeMapCalculatorWithPropagationEffect calc){

    //ArrayList for the Attenuations supporting and not supporting PGV
    ArrayList attenRelsSupportingPGV = new ArrayList();
    ArrayList attenRelsNotSupportingPGV = new ArrayList();

    //ArrayList for the Attenuations Wts supporting and not supporting PGV
    ArrayList attenRelsWtsSupportingPGV = new ArrayList();
    ArrayList attenRelsWtsNotSupportingPGV = new ArrayList();


    //gets the final PGV values after summing up the attenRels not supporting PGV
   // and one's supporting PGV.
    XYZ_DataSetAPI pgvDataSet = null;

    int size = selectedIMRs.size();
    for(int i=0;i<size;++i){
      AttenuationRelationshipAPI attenRel = (AttenuationRelationshipAPI)selectedIMRs.get(i);
      String imt = attenRel.getIntensityMeasure().getName();
      if(imt.equals(AttenuationRelationship.SA_NAME)){
        attenRelsNotSupportingPGV.add(attenRel);
        attenRelsWtsNotSupportingPGV.add(selectedWts.get(i));
      }
      else{
        attenRelsSupportingPGV.add(attenRel);
        attenRelsWtsSupportingPGV.add(selectedWts.get(i));
      }
    }

    int attenRelsNotSupportingPGV_size =  attenRelsNotSupportingPGV.size();
    int attenRelsSupportingPGV_size = attenRelsSupportingPGV.size();


    //XYZ data for the data set supporting the PGV
    XYZ_DataSetAPI xyzDataSetForPGV = null;
    //XYZ data for the data set not supporting PGV
    XYZ_DataSetAPI xyzDataSetForNotPGV = null;

    if(attenRelsNotSupportingPGV_size >0){ //if Attenuation Relations do not support the PGV
      xyzDataSetForNotPGV=calc.getScenarioShakeMapData(attenRelsNotSupportingPGV,attenRelsWtsNotSupportingPGV,
                                   region,rupture,isProbAtIML,value);
      convertIML_ValuesToExpo((ArbDiscretizedXYZ_DataSet)xyzDataSetForNotPGV,AttenuationRelationship.SA_NAME,isProbAtIML);
      //if PGV is not supported by the attenuation then use the SA-1sec pd
      //and multiply the value by scaler 37.24*2.54
      ArrayList zVals = xyzDataSetForNotPGV.getZ_DataSet();
      size = zVals.size();
      for(int i=0;i<size;++i){
        double val = ((Double)zVals.get(i)).doubleValue()*37.24*2.54;
        zVals.set(i,new Double(val));
      }
    }
    else{ //if Attenuations support PGV
      xyzDataSetForPGV=calc.getScenarioShakeMapData(attenRelsSupportingPGV,attenRelsWtsSupportingPGV,
                                   region,rupture,isProbAtIML,value);
      convertIML_ValuesToExpo((ArbDiscretizedXYZ_DataSet)xyzDataSetForPGV,AttenuationRelationship.PGV_NAME,isProbAtIML);
    }

    //if there are both AttenRels selected those that support PGV and those that don't.
    if(attenRelsNotSupportingPGV_size >0 && attenRelsSupportingPGV_size >0){
      //arrayList declaration for the Atten Rel not supporting PGV
      ArrayList list = null;
      //arrayList declaration for the Atten Rel supporting PGV
      ArrayList pgvList = null;
      list = xyzDataSetForNotPGV.getZ_DataSet();
      pgvList = xyzDataSetForPGV.getZ_DataSet();

      //ArrayList to store the combine( added) result(from Atten that support PGV
      //and that do not support PGV) of the Z Values for the PGV.
      ArrayList finalPGV_Vals = new ArrayList();
      //adding the values from both the above list for PGV( one calculated using PGV
      //and other calculated using the SA at 1sec and mutipling by the scalar 37.24*2.54).
      for(int i=0;i<size;++i)
        finalPGV_Vals.add(new Double(((Double)pgvList.get(i)).doubleValue()+((Double)list.get(i)).doubleValue()));
      //creating the final dataste for the PGV dataset.
      pgvDataSet = new ArbDiscretizedXYZ_DataSet(xyzDataSetForPGV.getX_DataSet(),
          xyzDataSetForPGV.getY_DataSet(),finalPGV_Vals);
    }
    else{ //if only one kind of AttenRels are selected those that support PGV or those that don't.
      //if XYZ dataset supporting PGV is null
      if(attenRelsSupportingPGV_size ==0)
        pgvDataSet = xyzDataSetForNotPGV;
      //if XYZ dataset not supporting PGV is null
      else if(attenRelsNotSupportingPGV_size ==0)
        pgvDataSet = xyzDataSetForPGV;
    }

    return pgvDataSet;
  }

  /**
   * Saves the XYZ object in file specified by the xyzDataFileWithAbsolutePath
   * @param griddedRegion
   * @param xyzDataFileWithAbsolutePath
   */
  private void createXYZDataObjectFile(ArbDiscretizedXYZ_DataSet xyzData,String xyzDataFileWithAbsolutePath){
    FileUtils.saveObjectInFile(xyzDataFileWithAbsolutePath,xyzData);
  }


  /**
   * This class replicates the selected AttenuationRelationships( and their parameter values)
   * and add the parameter change warninglistener to them for this class. Now
   * any parameter change waring event occurs it will happen at this class.
   * @param selectedAttenRels
   */
  private void getIMR_ParametersAndAddListeners(ArrayList selectedAttenRels){

    /**
     * Iterating over all the selected AttenRels
     */
    ListIterator it = selectedAttenRels.listIterator();
    while(it.hasNext()){
      AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)it.next();

      AttenuationRelationshipAPI imr_temp =
          (AttenuationRelationshipAPI)createIMRClassInstance(imr.getClass().getName(), this);

      // set other params
      ListIterator lt = imr.getOtherParamsIterator();
      while(lt.hasNext()){
        ParameterAPI tempParam=(ParameterAPI)lt.next();
        imr_temp.getParameter(tempParam.getName()).setValue(tempParam.getValue());
      }
      // set IM
      //imr_temp.setIntensityMeasure(imr.getIntensityMeasure().getName());
      //imr_temp.setIntensityMeasureLevel(imr.getIntensityMeasureLevel());
      imr_temp.setIntensityMeasure(imr.getIntensityMeasure());
      imr  = imr_temp;
    }
  }

  /**
   * Creates a class instance from a string of the full class name including packages.
   * This is how you dynamically make objects at runtime if you don't know which\
   * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
   * it the normal way:<P>
   *
   * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
   *
   * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
   * instead to create the same class by:<P>
   *
   * <code>BJF_1997_AttenRel imr =
   * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.scec.sha.imt.attenRelImpl.BJF_1997_AttenRel");
   * </code><p>
   *
   */
  private Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
    try {

      Class listenerClass = Class.forName( "org.scec.param.event.ParameterChangeWarningListener" );
      Object[] paramObjects = new Object[]{ listener };
      Class[] params = new Class[]{ listenerClass };
      Class imrClass = Class.forName( className );
      Constructor con = imrClass.getConstructor( params );
      Object obj = con.newInstance( paramObjects );
      return obj;
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }



  /**
   * If any parameter change warning occurs to the IMR parameters then this class will
   * handle it, rather than GuiBeans handling it becuase  guiBeans being the swing
   * component can't be seralized.
   * @param e : Warning event
   */
  public void parameterChangeWarning(ParameterChangeWarningEvent e) {
    e.getWarningParameter().setValueIgnoreWarning(e.getNewValue());
  }

  /**
   * This method just calls the doPost method
   *
   * @param request : Request Object
   * @param response : Response Object
   * @throws IOException : Throws IOException during read-write from connection stream
   * @throws ServletException
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request,response);
  }
}
