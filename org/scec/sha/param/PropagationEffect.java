package org.scec.sha.param;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;
import org.scec.calc.RelativeLocation;


/**
 * <b>Title:</b> PropagationEffect<p>
 *
 * <b>WARNING:</b> SWR: I noticed alot of incomplete functions in this class. Is
 * this class even being used??? <p>
 *
 * <b>Description:</b> This is a ParameterList of PropagationEffectParameters that also maintains
 * a reference to the Site and probEqkRupture objects that are common
 * to all the parameters. Recall from the PropagationEffectParameter documentation
 * these two parameters can be set in the PropagationEffectParameter to
 * uniquly determine the parameters's value, bypassing the normal useage
 * of setValue() to update the parameter's value. <p>
 *
 * The parameter options are held internally as a ParamterList
 * of PropagationEffectParameter objects which extend Paramter. These parameters
 * can be access by name, and the value can also be returned. <p>
 *
 * Since this class is a ParameterList one can create and add a new, arbitrary
 * PropagationEffectCalculator() to the vector of options. More importantly
 * this class also maintains a list of pre-defined parameters. This class
 * recognizes (and checks for first) the following
 * common propagation-effect parameter names (used in existing
 * IntensityMeasureRelationships) and performs some of the calculations
 * simultaneously to increase efficiency (e.g., it's faster to compute
 * Rrup, Rjb,and Rseis simultaneously, for the same Site and
 * ProbEqkRupture, rather than in series):<p>
 *
 * This can be accomplished by spawning new threads to return the desired
 * requested result first. These threads should be set at low priority.<p>
 *
 * <br><br>
 * <br>     Rrup	\
 * <br>     Rjb	 > (km; these are three common distance
 * <br>                 measures used by the Rseis	/
 * <br>                 various IntensityMeasureRelationships)
 * <br>     AS_1997_HangingWall	(int 0 or 1)
 * <br>     Abrahamson_2000_X   (fraction of fault length that ruptures toward
 * <br>                          the site; a directivity parameter)
 * <br>     Abrahamson_2000_Theta 	(angle between strike and
 * <br>                               epicentral azimuth; a directivity parameter)
 * <p>
 *
 * FIX *** FIX *** SWR: Many functions not implemented. Is this class still needed???
 *
 * @author Steven W. Rock
 * @version 1.0
 */
public class PropagationEffect {


    /** The Site used for calculating the PropagationEffect parameter values. */
    protected Site site = null;

    /** The ProbEqkRupture used for calculating the PropagationEffect parameter values.*/
    protected ProbEqkRupture probEqkRupture = null;


    /** this distance measure for the DistanceRupParameter */
   	protected double distanceRup;

    /** this distance measure for the DistanceJBParameter */
   	protected double distanceJB;

    /** this distance measure for the DistanceSeisParameter */
   	protected double distanceSeis;


    /** No Argument consructor */
    public PropagationEffect() { }

    /** Constructor that is give Site and ProbEqkRupture objects */
    public PropagationEffect( Site site, ProbEqkRupture pe) {
      this.site = site;
      this.probEqkRupture = probEqkRupture;
    }

    /** Returns the Site object */
    public Site getSite() { return site; }

    /** Returns the ProbEqkRupture object */
    public ProbEqkRupture getProbEqkRupture() { return probEqkRupture; }

    /** Sets the Site object */
    public void setSite(Site site) {
        this.site = site;
    }

    /** Sets the ProbEqkRupture object */
    public void setProbEqkRupture(ProbEqkRupture probEqkRupture) {
        this.probEqkRupture = probEqkRupture;
    }

    /** Sets both the ProbEqkRupture and Site object */
    public void setAll(ProbEqkRupture probEqkRupture, Site site) {
        this.probEqkRupture = probEqkRupture;
        this.site = site;
    }


    /**
     */
    public Object getParamValue(String paramName) {
      if(paramName.equals(DistanceSeisParameter.)
    	return null;
    }

    /**
     */
    public void setParamValue( ParameterAPI param ) {
    	return null;
    }

    /**
     *
     */
    private void computeParamValues() {

      if( ( this.site != null ) && ( this.probEqkRupture != null ) ){

          Location loc1 = site.getLocation();
          distanceJB = 9999999;
          distanceSeis = 9999999;
          distanceRup = 9999999;

          double horzDist, vertDist, rupDist, jbDist, seisDist;

          ListIterator it = probEqkRupture.getRuptureSurface().getLocationsIterator();
          while( it.hasNext() ){

            Location loc2 = (Location) it.next();

            horzDist = RelativeLocation.getHorzDistance(loc1, loc2);
            vertDist = RelativeLocation.getVertDistance(loc1, loc2);

            if(horzDist < distanceJB) distanceJB = horzDist;

            rupDist = horzDist * horzDist + vertDist * vertDist;
            if(rupDist < distanceRup) distanceRup = rupDist;

            if (loc2.getDepth() >= DistanceSeisParameter.seisDepth)
              if(rupDist < distanceSeis)
                distanceSeis = rupDist;
          }

          distanceRup = Math.pow(distanceRup,0.5);
          distanceSeis = Math.pow(distanceSeis,0.5);

      }

      throw new RuntimeException ("Site or ProbEqkRupture is null");

    }
}
