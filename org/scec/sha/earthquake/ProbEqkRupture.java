package org.scec.sha.earthquake;



import org.scec.data.*;
import org.scec.exceptions.InvalidRangeException;
import org.scec.sha.surface.GriddedSurfaceAPI;


/**
 * <p>Title:ProbEqkRupture </p>
 * <p>Description: Probabilistic Earthquake Rupture</p>
 *
 * @author Nitin Gupta & Vipin Gupta
 * @date aug 27, 2002
 * @version 1.0
 */

public class ProbEqkRupture extends EqkRupture{

  protected double probability;

  /** Represents a start time and duration => has end time */
  protected TimeSpan timespan = null;



  /* **********************/
    /** @todo  Constructors */
    /* **********************/

  public ProbEqkRupture() {
    super();
  }

  public ProbEqkRupture(double mag,
                        double aveRake,
                        double probability,
                        GriddedSurfaceAPI ruptureSurface,
                        Location hypocenterLocation)
                            throws InvalidRangeException {

        super(mag, aveRake, ruptureSurface, hypocenterLocation);
        this.probability = probability;

   }


    /**
     * Get and set methods for Time Span and Probability
     *
     * @return
     */
  public TimeSpan getTimeSpan() { return timespan; }

  public void setTimeSpan(TimeSpan timespan) { this.timespan = timespan; }

  public double getProbability() { return probability; }
  public void setProbability(double p) { probability = p; }


  /**
   * This is a function of probability and timespan
   */
  public double getMeanAnnualRate(){
       return 0;
   }

   /**
    * Clones the eqk rupture and returns the new cloned object
    * @return
    */
  public Object clone() {
    ProbEqkRupture eqkRuptureClone=new ProbEqkRupture();
    eqkRuptureClone.setAveRake(this.aveRake);
    eqkRuptureClone.setMag(this.mag);
    eqkRuptureClone.setRuptureSurface(this.ruptureSurface);
    eqkRuptureClone.setHypocenterLocation(this.hypocenterLocation);
    eqkRuptureClone.setProbability(this.probability);
    eqkRuptureClone.setTimeSpan(this.timespan);
    return eqkRuptureClone;
  }

}