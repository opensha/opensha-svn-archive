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

/**
 * <p>Title: ScenarioShakeMapCalcServlet  </p>
 * <p>Description: This servlet hosted at gravity.usc.edu, accepts parameters
 * to do the ScenarioShakeMap calculation on the server.
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ScenarioShakeMapCalcServlet  extends HttpServlet implements ParameterChangeWarningListener{


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

    //Vectors for computing the lat and lons for the given gridded region
    ArrayList locationVector= new ArrayList();
    try {
      // get all the input stream from the applet
      ObjectInputStream inputFromApplication = new ObjectInputStream(request.getInputStream());
      //gets the inputs from the Application.

      //gets the selected AttenuationRelationships
      ArrayList selectedAttenRels = (ArrayList)inputFromApplication.readObject();

      //gets the selected AttenRel Absolute Wts
      ArrayList selectedAttenRelWts = (ArrayList)inputFromApplication.readObject();

      //gets the selected region object form the application
      SitesInGriddedRegion griddedRegion = (SitesInGriddedRegion)inputFromApplication.readObject();

      //gets the selected EqkRupture object form the application
      EqkRupture rupture = (EqkRupture)inputFromApplication.readObject();

      //gets the boolean to if IML@Prob or Prob@IML
      boolean isProbAtIML = ((Boolean)inputFromApplication.readObject()).booleanValue();

      //the IML or Prob value to compute the map for
      double value = ((Double)inputFromApplication.readObject()).doubleValue();

      //close of the input from the application
      inputFromApplication.close();

      //adds the parameter change listener event to the parameters of the selected AttenRels
      getIMR_ParametersAndAddListeners(selectedAttenRels);

      //creating the object for the ScenarioShakeMapCalculator to compute the XYZ data for the selected region
      ScenarioShakeMapCalculatorWithPropagationEffect calc = new ScenarioShakeMapCalculatorWithPropagationEffect();
      //sending the output in the form of the arrayList back to the calling application.
      ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());

      //XYZ data for the scenarioshake as computed
      XYZ_DataSetAPI xyzData = calc.getScenarioShakeMapData(selectedAttenRels,selectedAttenRelWts,
          griddedRegion,rupture,isProbAtIML,value);
      //calculates the XYZ data for the ScenarioShakeMap and returns it back to the application.
      output.writeObject(xyzData);

      output.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
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
