package org.scec.sha.earthquake.rupForecastImpl.PuenteHillsERF;

import java.util.Vector;

import org.scec.data.TimeSpan;

import org.scec.param.*;
import org.scec.sha.surface.*;
import org.scec.sha.param.*;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.calc.magScalingRelations.magScalingRelImpl.*;
import org.scec.calc.magScalingRelations.*;
import org.scec.sha.param.SimpleFaultParameter;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.SimpleFaultRuptureSource;


/**
 * <p>Title: PuenteHillsFaultERF</p>
 * <p>Description: This is   </p>
 *
 * @author Ned Field
 * Date : Oct 24 , 2002
 * @version 1.0
 */

public class PuenteHillsFaultERF extends EqkRupForecast
    implements ParameterChangeListener {

  //for Debug purposes
  private static String  C = new String("Char Poisson Fault ERF");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;

  // this is the source (only 1 for this ERF)
  private SimpleFaultRuptureSource source;


  /**
   * Constructor for this source (no arguments)
   */
  public PuenteHillsFaultERF() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

  }


  /**
    *  This is the method called by any parameter whose value has been changed
    *
    * @param  event
    */
   public void parameterChange( ParameterChangeEvent event ) {
      parameterChangeFlag=true;
   }


   /**
    * update the source based on the paramters (only if a parameter value has changed)
    */
   public void updateForecast(){
     String S = C + "updateForecast::";

     if(parameterChangeFlag) {

       // get the mag & prob
       double mag = 7.1;
       double prob = 1.0;
       double rake = 90;
       if (D) System.out.println(S+":  mag="+mag+"; prob="+prob+"; rake="+rake);

       double aveDipDir = 0; // dipping to the north

       EvenlyGriddedSurface surface = null;

       source = new SimpleFaultRuptureSource(mag, surface,rake, prob);
       parameterChangeFlag = false;
     }

   }


   /**
    * Return the earhthquake source at index i.   Note that this returns a
    * pointer to the source held internally, so that if any parameters
    * are changed, and this method is called again, the source obtained
    * by any previous call to this method will no longer be valid.
    *
    * @param iSource : index of the desired source (only "0" allowed here).
    *
    * @return Returns the ProbEqkSource at index i
    *
    */
   public ProbEqkSource getSource(int iSource) {

     // we have only one source
    if(iSource!=0)
      throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

    return source;
   }


   /**
    * Returns the number of earthquake sources (always "1" here)
    *
    * @return integer value specifying the number of earthquake sources
    */
   public int getNumSources(){
     return 1;
   }


    /**
     *  This returns a list of sources (contains only one here)
     *
     * @return Vector of Prob Earthquake sources
     */
    public Vector  getSourceList(){
      Vector v =new Vector();
      v.add(source);
      return v;
    }


  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
   public String getName(){
     return NAME;
   }
}
