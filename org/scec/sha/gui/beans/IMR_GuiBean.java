package org.scec.sha.gui.beans;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.ParameterList;
import org.scec.sha.imr.*;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;

import org.scec.param.*;
import org.scec.param.event.*;

/**
 * <p>Title: IMR Gui Bean</p>
 * <p>Description: This is the IMR Gui Bean. This bean can be instantiated to be
 * added to the applets.
 * It displays the following :
 *  1. a pick list to choose a IMR.
 *  2. a pick list to choose Gaussian truncation type
 *  3. a pick list to choose std dev type
 *  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class IMR_GuiBean extends ParameterListEditor
    implements ParameterChangeListener,
    ParameterChangeWarningListener, ParameterChangeFailListener {

  // IMR GUI Editor & Parameter names
  public final static String IMR_PARAM_NAME = "IMR";
  public final static String IMR_EDITOR_TITLE =  "Select IMR";
  //this vector saves the names of all the supported IMRs
  private Vector imrNamesVector=new Vector();
  //this vector holds the full class names of all the supported IMRs
  private Vector imrClasses = new Vector();
  //saves the IMR objects, to the parameters related to an IMR.
  private Vector imrObject = new Vector();

  /**
   * constructor which accepts the class names of the imrs to be shown in pick list
   * @param classNames
   */
 public IMR_GuiBean(Vector classNames) {
   this.imrClasses = classNames;
   parameterList = new ParameterList();
   init_imrParamListAndEditor();
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
   public Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
       String S = C + ": createIMRClassInstance(): ";
       try {

           Class listenerClass = Class.forName( "org.scec.param.event.ParameterChangeWarningListener" );
           Object[] paramObjects = new Object[]{ listener };
           Class[] params = new Class[]{ listenerClass };
           Class imrClass = Class.forName( className );
           Constructor con = imrClass.getConstructor( params );
           Object obj = con.newInstance( paramObjects );
           return obj;
       } catch ( ClassCastException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( ClassNotFoundException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( NoSuchMethodException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( InvocationTargetException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( IllegalAccessException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       } catch ( InstantiationException e ) {
           System.out.println(S + e.toString());
           throw new RuntimeException( S + e.toString() );
       }

   }


   /**
    *  Create a list of all the IMRs
    */
   private void init_imrParamListAndEditor() {


     // if we are entering this function for the first time, then make imr objects
     if(!parameterList.containsParameter(IMR_PARAM_NAME)) {
       parameterList = new ParameterList();
       Iterator it= imrClasses.iterator();
       while(it.hasNext()){
         // make the IMR objects as needed to get the site params later
         AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI ) createIMRClassInstance((String)it.next(),this);
         imrObject.add(imr);
         imrNamesVector.add(imr.getName());
         Iterator it1 = imr.getSiteParamsIterator();
         // add change fail listener to the site parameters for this IMR
         while(it1.hasNext()) {
           ParameterAPI param = (ParameterAPI)it1.next();
           param.addParameterChangeFailListener(this);
         }
       }

       // make the IMR selection paramter
       StringParameter selectIMR = new StringParameter(IMR_PARAM_NAME,
           imrNamesVector,(String)imrNamesVector.get(0));
       // listen to IMR paramter to change site params when it changes
       selectIMR.addParameterChangeListener(this);
       parameterList.addParameter(selectIMR);
     }

     // remove all the parameters except the IMR parameter
     ListIterator it = parameterList.getParameterNamesIterator();
     while(it.hasNext()) {
       String paramName = (String)it.next();
       if(!paramName.equalsIgnoreCase(IMR_PARAM_NAME))
         parameterList.removeParameter(paramName);
     }


     // now find the selceted IMR and add the parameters related to it

     // initalize imr
     AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)imrObject.get(0);

     // find & set the selectedIMR
     String selectedIMR = parameterList.getValue(IMR_PARAM_NAME).toString();
     int size = imrObject.size();
     for(int i=0; i<size ; ++i) {
       imr = (AttenuationRelationshipAPI)imrObject.get(i);
       if(imr.getName().equalsIgnoreCase(selectedIMR))
         break;
     }


     ParameterAPI typeParam = imr.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME);
     parameterList.addParameter(typeParam);
     typeParam.addParameterChangeListener(this);


     // add trunc level
     ParameterAPI levelParam = imr.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME);
     parameterList.addParameter(levelParam);

     //add the sigma param for IMR
     ParameterAPI sigmaParam = imr.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME);
     sigmaParam.setValue(((StringParameter)sigmaParam).getAllowedStrings().get(0));
     parameterList.addParameter(sigmaParam);
     this.editorPanel.removeAll();
     addParameters();
     setTitle(IMR_EDITOR_TITLE);
     // set the trunc level based on trunc type
     String value = (String)parameterList.getValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME);
     toggleSigmaLevelBasedOnTypeValue(value);

   }


   /**
    * sigma level is visible or not
    * @param value
    */
   protected void toggleSigmaLevelBasedOnTypeValue(String value){

     if( value.equalsIgnoreCase("none") ) {
       if(D) System.out.println("Value = " + value + ", need to set value param off.");
       setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, false );
     }
     else{
      if(D) System.out.println("Value = " + value + ", need to set value param on.");
      setParameterVisible( AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME, true );
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
        if ( D ) System.out.println( "\n" + S + "starting: " );

        String name1 = event.getParameterName();

        // if Truncation type changes
        if( name1.equals(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME) ){  // special case hardcoded. Not the best way to do it, but need framework to handle it.
          String value = event.getNewValue().toString();
          toggleSigmaLevelBasedOnTypeValue(value);
        }

    }

    /**
   *  Function that must be implemented by all Listeners for
   *  ParameterChangeWarnEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChangeWarning( ParameterChangeWarningEvent e ){

    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");


    // only display messages if paramters are set at back
    StringBuffer b = new StringBuffer();

    WarningParameterAPI param = e.getWarningParameter();


    try{
      Double min = param.getWarningMin();
      Double max = param.getWarningMax();

      String name = param.getName();

      b.append( "You have exceeded the recommended range for ");
      b.append( name );
      b.append( ": (" );
      b.append( min.toString() );

      b.append( " to " );
      b.append( max.toString() );
      b.append( ")\n" );
      b.append( "Click Yes to accept the new value: " );
      b.append( e.getNewValue().toString() );
    }
    catch( Exception ee){

      String name = param.getName();

      b.append( "You have exceeded the recommended range for: \n");
      b.append( name + '\n' );
      b.append( "Click Yes to accept the new value: " );
      b.append( e.getNewValue().toString() );
      b.append( name );


    }
    if(D) System.out.println(S + b.toString());

    int result = 0;

    if(D) System.out.println(S + "Showing Dialog");

    result = JOptionPane.showConfirmDialog( this, b.toString(),
        "Exceeded Recommended Values", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

    if(D) System.out.println(S + "You choose" + result);

    switch (result) {
      case JOptionPane.YES_OPTION:
        if(D) System.out.println(S + "You choose yes, changing value to " + e.getNewValue().toString() );
        param.setValueIgnoreWarning( e.getNewValue());
        break;
      case JOptionPane.NO_OPTION:
        if(D) System.out.println(S + "You choose no, keeping value = " + e.getOldValue().toString() );
        param.setValueIgnoreWarning( e.getOldValue() );
        break;
      default:
        param.setValueIgnoreWarning( e.getOldValue() );
      if(D) System.out.println(S + "Not sure what you choose, not changing value.");
      break;
    }

    if(D) System.out.println(S + "Ending");
  }


 /**
   * Shown when a Constraint error is thrown on a ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {

    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");


    StringBuffer b = new StringBuffer();

    ParameterAPI param = ( ParameterAPI ) e.getSource();


    ParameterConstraintAPI constraint = param.getConstraint();
    String oldValueStr = e.getOldValue().toString();
    String badValueStr = e.getBadValue().toString();
    String name = param.getName();

    b.append( "The value ");
    b.append( badValueStr );
    b.append( " is not permitted for '");
    b.append( name );
    b.append( "'.\n" );
    b.append( "Resetting to ");
    b.append( oldValueStr );
    b.append( ". The constraints are: \n");
    b.append( constraint.toString() );

    JOptionPane.showMessageDialog(
        this, b.toString(),
        "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
        );

    if(D) System.out.println(S + "Ending");

  }


}