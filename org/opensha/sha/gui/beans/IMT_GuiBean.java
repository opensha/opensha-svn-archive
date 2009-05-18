package org.opensha.sha.gui.beans;

import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;

import org.opensha.sha.imr.*;



import java.util.*;

/**
 * <p>Title: IMT_GuiBean </p>
 * <p>Description: this dispalys the various IMTs supported by the selected IMR</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class IMT_GuiBean extends ParameterListEditor implements ParameterChangeListener{

  // IMT GUI Editor & Parameter names
  public final static String IMT_PARAM_NAME =  "IMT";
  // IMT Panel title
  public final static String IMT_EDITOR_TITLE =  "Set IMT";

  //stores the IMT Params for the choosen IMR
  private ArrayList imtParam;

  // imr for which IMT is to be displayed
  private AttenuationRelationshipAPI imr;

  /**
   * constructor: accepts AttenuationRelationshipAPI and supprtedIntensity Measure type
   * @param imr Choosen AttenuationRelationship
   * @param supportedIntensityMeasureIt Supported Intensity Measure Iterator
   */
 public IMT_GuiBean(AttenuationRelationshipAPI imr,Iterator supportedIntensityMeasureIt) {
   setIM(imr,supportedIntensityMeasureIt );
 }
 
 /**
  * 
  * This function accepts AttenuationRelationshipAPI and supprtedIntensity Measure type
  *
  * @param imr Choosen AttenuationRelationship
  * @param supportedIntensityMeasureIt Supported Intensity Measure Iterator
  */
 public void setIM(AttenuationRelationshipAPI imr,Iterator supportedIntensityMeasureIt){
	 this.imr = imr;
	 init_imtParamListAndEditor(supportedIntensityMeasureIt);
 }


  /**
   *  Create a list of all the IMTs
   */
  private void init_imtParamListAndEditor(Iterator it) {

    parameterList = new ParameterList();

    //vector to store all the IMT's supported by an IMR
    ArrayList imt=new ArrayList();
    imtParam = new ArrayList();


    

    //loop over each IMT and get their independent parameters
    while ( it.hasNext() ) {
      DependentParameterAPI param = ( DependentParameterAPI ) it.next();
      DoubleDiscreteParameter param1=new DoubleDiscreteParameter(param.getName());

      // add all the independent parameters related to this IMT
      // NOTE: this will only work for DoubleDiscrete independent parameters; it's not general!
      // this also converts these DoubleDiscreteParameters to StringParameters
      ListIterator it2 = param.getIndependentParametersIterator();
      if(D) System.out.println("IMT is:"+param.getName());
      while ( it2.hasNext() ) {

        ParameterAPI param2 = (ParameterAPI ) it2.next();
        DoubleDiscreteConstraint values = ( DoubleDiscreteConstraint )param2.getConstraint();
        ListIterator it3 = values.listIterator();
         // add all the periods relating to the SA
        DoubleDiscreteParameter independentParam = new DoubleDiscreteParameter(param2.getName(),
            values, (Double)values.getAllowedValues().get(0));

        // added by Ned so the default period is 1.0 sec (this is a hack).
        if( ((String) independentParam.getName()).equals("SA Period") ) {
        	if (independentParam.isAllowed(new Double(1.0)))
        		independentParam.setValue(new Double(1.0));
        }

        param1.addIndependentParameter(independentParam);
      }
      imtParam.add(param1);
      imt.add(param.getName());
    }

    // add the IMT paramter
    StringParameter imtParameter = new StringParameter (IMT_PARAM_NAME,imt,
        (String)imt.get(0));
    imtParameter.addParameterChangeListener(this);
    parameterList.addParameter(imtParameter);

    /* gets the iterator for each supported IMT and iterates over all its indepenedent
    * parameters to add them to the common ArrayList to display in the IMT Panel
    **/

    it=imtParam.iterator();
    while(it.hasNext()){
      Iterator it1=((DependentParameterAPI)it.next()).getIndependentParametersIterator();
      while(it1.hasNext())
        parameterList.addParameter((ParameterAPI)it1.next());
    }
    this.editorPanel.removeAll();
    // now make the editor based on the paramter list
    addParameters();
    setTitle( this.IMT_EDITOR_TITLE );
    // update the current IMT
    updateIMT((String)imt.get(0));
  }

  /**
   * This function updates the IMTeditor with the independent parameters for the selected
    * IMT, by making only those visible to the user.
    * @param imtName : It is the name of the selected IMT, based on which we make
    * its independentParameters visible.
    */

   private void updateIMT(String imtName) {
     Iterator it= parameterList.getParametersIterator();

     //making all the IMT parameters invisible
     while(it.hasNext())
       setParameterVisible(((ParameterAPI)it.next()).getName(),false);

     //making the choose IMT parameter visible
     setParameterVisible(IMT_PARAM_NAME,true);

     it=imtParam.iterator();
     //for the selected IMT making its independent parameters visible
     while(it.hasNext()){
       DependentParameterAPI param=(DependentParameterAPI)it.next();
       if(param.getName().equalsIgnoreCase(imtName)){
         Iterator it1=param.getIndependentParametersIterator();
         while(it1.hasNext())
           setParameterVisible(((ParameterAPI)it1.next()).getName(),true);
       }
     }
   }

   /**
  *  This is the main function of this interface. Any time a control
  *  paramater or independent paramater is changed by the user in a GUI this
  *  function is called, and a paramater change event is passed in. This
  *  function then determines what to do with the information ie. show some
  *  paramaters, set some as invisible, basically control the paramater
  *  lists.
  *
  * @param  event
  */
 public void parameterChange( ParameterChangeEvent event ) {

     String S = C + ": parameterChange(): ";
     if ( D )
         System.out.println( "\n" + S + "starting: " );

     String name1 = event.getParameterName();

     // if IMT selection then update
     if (name1.equalsIgnoreCase(this.IMT_PARAM_NAME)) {
       updateIMT((String)event.getNewValue());
     }

 }

 /**
  * It will retunr the IMT selected by the user
  * @return : IMT selected by the user
  */
 public String getSelectedIMT() {
   return this.parameterList.getValue(this.IMT_PARAM_NAME).toString();
 }

 /**
  * set the IMT parameter in IMR
  */
 public void setIMT() {
   ParameterAPI param = getIntensityMeasure();
   imr.setIntensityMeasure(param);
 }


 /**
  * gets the selected Intensity Measure Parameter and its dependent Parameter
  * @return
  */
 public ParameterAPI getIntensityMeasure(){
   String selectedImt = parameterList.getValue(this.IMT_PARAM_NAME).toString();
   //set all the  parameters related to this IMT
   Iterator it= imtParam.iterator();
   while(it.hasNext()){
     DependentParameterAPI param=(DependentParameterAPI)it.next();
     if(param.getName().equalsIgnoreCase(selectedImt))
       return param;
   }
   return null;
 }



 /**
  *
  * @returns the Metadata string for the IMT Gui Bean
  */
public String getParameterListMetadataString(){
  String metadata=null;
  ListIterator it = getVisibleParameters().getParametersIterator();
  int paramSize = getVisibleParameters().size();
  while(it.hasNext()){
    //iterates over all the visible parameters
    ParameterAPI tempParam = (ParameterAPI)it.next();
    //if the param name is IMT Param then it is the Dependent param
    if(tempParam.getName().equals(this.IMT_PARAM_NAME)){
      metadata = tempParam.getName()+" = "+(String)tempParam.getValue();
      if(paramSize>1)
        metadata +="[ ";
    }
    else{ //rest all are the independent params
      metadata += tempParam.getName()+" = "+((Double)tempParam.getValue()).doubleValue()+" ; ";
    }
  }
  if(paramSize>1)
    metadata = metadata.substring(0,metadata.length()-3);
  if(paramSize >1)
  metadata +=" ] ";
  return metadata;
}

}
