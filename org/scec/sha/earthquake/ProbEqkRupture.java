package org.scec.sha.earthquake;



import org.scec.data.*;
import org.scec.exceptions.InvalidRangeException;
import org.scec.sha.surface.GriddedSurfaceAPI;


/**
 * <p>Title:ProbEqkRupture </p>
 * <p>Description: Probabilistic Earthquake Rupture</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
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

}