package org.scec.sha.gui.beans;

import org.scec.param.event.*;
import org.scec.param.ParameterList;
import org.scec.sha.param.editor.*;

/**
 * <p>Title: ERF_GuiBeanAPI</p>
 * <p>Description: This interface is implemented by the GUI beans for the ERF Applets</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field , Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public interface ERF_GuiBeanAPI extends
    ParameterChangeFailListener, ParameterChangeListener {


  /**
   * gets the lists of all the parameters that exists in the ERF parameter Editor
   * then checks if the magFreqDistParameter exists inside it , if so then returns the MagEditor
   * else return null.  The only reason this is public is because at least one control panel
   * (for the PEER test cases) needs access.
   * @returns MagFreDistParameterEditor
   */
   public MagFreqDistParameterEditor getMagDistEditor();

   /**
    * gets the lists of all the parameters that exists in the ERF parameter Editor
    * then checks if the simpleFaultParameter exists inside it , if so then returns the
    * SimpleFaultParameterEditor else return null.  The only reason this is public is
    * because at least one control panel (for the PEER test cases) needs access.
    * @returns SimpleFaultParameterEditor
    */
   public SimpleFaultParameterEditor getSimpleFaultParamEditor();


   /**
    * returns the name of selected ERF
    * @return
    */
   public String getSelectedERF_Name();


   /**
    * This allows tuning on or off the showing of a progress bar
    * @param show - set as true to show it, or false to not show it
    */
   public void showProgressBar(boolean show);

   /** gets the parameterList. Simple javabean method */
    public ParameterList getParameterList();


    /**
     * Proxy to each parameter editor. THe lsit of editors is iterated over, calling the
     * same function. <p>
     *
     * Updates the paramter editor with the parameter value. Used when
     * the parameter is set for the first time, or changed by a background
     * process independently of the GUI. This could occur with a ParameterChangeFail
     * event.
     */
    public void synchToModel() ;

}