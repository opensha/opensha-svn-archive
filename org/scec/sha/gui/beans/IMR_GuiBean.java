package org.scec.sha.gui.beans;

import org.scec.param.editor.ParameterListEditor;
import org.scec.param.ParameterList;
import org.scec.sha.imr.*;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

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
  public final static String IMR_EDITOR_TITLE =  "Set IMR";
  //this vector saves the names of all the supported IMRs
  private ArrayList imrNamesVector=new ArrayList();
  //this vector holds the full class names of all the supported IMRs
  private ArrayList imrClasses = new ArrayList();
  //saves the IMR objects, to the parameters related to an IMR.
  private ArrayList imrObject = new ArrayList();
  // this flag is needed else messages are shown twice on focus lost
  private boolean inParameterChangeWarning = false;

  /**
   * constructor which accepts the class names of the imrs to be shown in pick list
   * @param classNames
   */
 public IMR_GuiBean(ArrayList classNames) {
   this.imrClasses = classNames;
   parameterList = new ParameterList();

   init_imrParamListAndEditor();
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
        imr.setParamDefaults();
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
    imr = this.getSelectedIMR_Instance();

    // getting the iterator of the Other Parameters for IMR
    /**
     * Instead of getting hard-coding for getting the selected Params Ned added
     * a method to get the OtherParams for the selected IMR, Otherwise earlier we
     * were getting the 3 params associated with IMR's by there name. But after
     * adding the method to get the otherParams, it can also handle params that
     * are customary to the selected IMR. So this automically adds the parameters
     * associated with the IMR, which are : SIGMA_TRUNC_TYPE_NAME, SIGMA_TRUNC_LEVEL_NAME,
     * STD_DEV_TYPE_NAME and any other param assoctade with the IMR.
     */
    ListIterator lt = imr.getOtherParamsIterator();
    while(lt.hasNext()){
      ParameterAPI tempParam=(ParameterAPI)lt.next();
      //adding the parameter to the parameterList.
      tempParam.addParameterChangeListener(this);
      parameterList.addParameter(tempParam);
    }

    this.editorPanel.removeAll();
    addParameters();
    setTitle(IMR_EDITOR_TITLE);

    // get the panel for increasing the font and border
    // this is hard coding for increasing the IMR font
    // the colors used here are from ParameterEditor
    JPanel panel = this.getParameterEditor(this.IMR_PARAM_NAME).getOuterPanel();
    TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(new Color( 80, 80, 140 ),3),"");
    titledBorder1.setTitleColor(new Color( 80, 80, 140 ));
    Font DEFAULT_LABEL_FONT = new Font( "SansSerif", Font.BOLD, 13 );
    titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
    titledBorder1.setTitle(IMR_PARAM_NAME);
    Border border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
    panel.setBorder(border1);

    // set the trunc level based on trunc type
    String value = (String)parameterList.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).getValue();
    toggleSigmaLevelBasedOnTypeValue(value);

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
     // if IMR parameter changes, then get the Gaussian truncation, etc from this selected IMR
     if(name1.equalsIgnoreCase(this.IMR_PARAM_NAME)) {
       init_imrParamListAndEditor();
     }


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
    *  Function that must be implemented by all Listeners for
    *  ParameterChangeWarnEvents.
    *
    * @param  event  The Event which triggered this function call
    */
   public void parameterChangeWarning( ParameterChangeWarningEvent e ){

     String S = C + " : parameterChangeWarning(): ";
     if(D) System.out.println(S + "Starting");
     WarningParameterAPI param = e.getWarningParameter();

     //check if this parameter exists in the site param list of this IMR
     // if it does not then set its value using ignore warning
     Iterator it = this.getSelectedIMR_Instance().getSiteParamsIterator();
     boolean found = false;
     while(it.hasNext() && !found)
       if(param.getName().equalsIgnoreCase(((ParameterAPI)it.next()).getName()))
          found = true;
     if(!found) {
       param.setValueIgnoreWarning(e.getNewValue());
       return;
     }


     // if it is already processing a warning, then return
     if(inParameterChangeWarning) return;
     inParameterChangeWarning = true;

     StringBuffer b = new StringBuffer();

     try{
       Double min = (Double)param.getWarningMin();
       Double max = (Double)param.getWarningMax();

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
     inParameterChangeWarning = false;
     if(D) System.out.println(S + "Ending");
   }

   /**
    * Whether to show the warning messages or not
    * In some cases, we may not want to show warning messages.
    * Presently it is being used in HazardCurveApplet
    * @param show
    */
   public void showWarningMessages(boolean show){
     inParameterChangeWarning = !show;
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

     // only show messages for visible site parameters
     AttenuationRelationshipAPI imr = getSelectedIMR_Instance();
     ListIterator it = imr.getSiteParamsIterator();
     boolean found = false;
     // see whether this parameter exists in site param list for this IMR
     while(it.hasNext() && !found)
       if(((ParameterAPI)it.next()).getName().equalsIgnoreCase(name))
         found = true;

     // if this parameter for which failure was issued does not exist in
     // site parameter list, then do not show the message box
     if(!found)  return;



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

   /**
    * this method will return the name of selected IMR
    * @return : Selected IMR name
    */
   public String getSelectedIMR_Name() {
     return parameterList.getValue(IMR_PARAM_NAME).toString();
   }

   /**
    * This method will return the instance of selected IMR
    * @return : Selected IMR instance
    */
   public AttenuationRelationshipAPI getSelectedIMR_Instance() {
     AttenuationRelationshipAPI imr = null;
     String selectedIMR = getSelectedIMR_Name();
     int size = imrObject.size();
     for(int i=0; i<size ; ++i) {
       imr = (AttenuationRelationshipAPI)imrObject.get(i);
       if(imr.getName().equalsIgnoreCase(selectedIMR))
         break;
     }
     return imr;
   }

   /**
    * return a list of imr instances shown in this gui bean
    *
    * @return
    */
   public ArrayList getIMR_Objects() {
     return this.imrObject;
   }

}