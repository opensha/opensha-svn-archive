package org.scec.sha.param;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.param.WarningDoublePropagationEffectParameter;


/**
 * <b>Title:</b> PropagationEffect<p>
 *
 * <b>Description:</b>
 *
 *
 * @author Ned Field
 * @version 1.0
 */
public class PropagationEffect {

    private final static String C = "PropagationEffect";
    private final static boolean D = false;


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

    // this tells whether values are out of date w/ respect to current Site and ProbEqkRupture
    protected boolean STALE = true;

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
        STALE = true;
    }

    /** Sets the ProbEqkRupture object */
    public void setProbEqkRupture(ProbEqkRupture probEqkRupture) {
        this.probEqkRupture = probEqkRupture;
        STALE = true;
    }

    /** Sets both the ProbEqkRupture and Site object */
    public void setAll(ProbEqkRupture probEqkRupture, Site site) {
        this.probEqkRupture = probEqkRupture;
        this.site = site;
        STALE = true;
    }


    /**
     * This returns the value for the parameter-name given
     */
    public Object getParamValue(String paramName) {

      if (D) System.out.println(C+": getting Param Value for "+paramName);

      if(STALE == true)
        computeParamValues();

      //QUESTION - IS CREATING A NEW DOUBLE OBJECT WITH EACH CALL INEFFICIENT/UNNECESSARY?
      if(paramName.equals(DistanceRupParameter.NAME))
    	return new Double(distanceRup);
      else if(paramName.equals(DistanceJBParameter.NAME))
        return new Double(distanceJB);
      else if(paramName.equals(DistanceSeisParameter.NAME))
        return new Double(distanceSeis);
      else
        throw new RuntimeException("Parameter not supported");
    }


    /**
     * This sets the value of the passed in parameter with that computed internally.
     * This ignores warnings exceptions.
     */
    public void setParamValue( ParameterAPI param ) {

      if(param instanceof WarningDoublePropagationEffectParameter)
       ((WarningDoublePropagationEffectParameter)param).setValueIgnoreWarning(getParamValue(param.getName()));
      else
        param.setValue(getParamValue(param.getName()));

    }


    /**
     *
     * @param paramName
     * @return
     */
    public boolean isParamSupported(String paramName) {
      if(paramName.equals(DistanceRupParameter.NAME))
        return true;
      else if(paramName.equals(DistanceJBParameter.NAME))
        return true;
      else if(paramName.equals(DistanceSeisParameter.NAME))
        return true;
      else
        return false;
    }


    /**
     *
     * @param param
     * @return
     */
    public boolean isParamSupported( ParameterAPI param ) {
      return isParamSupported(param.getName());
    }



    /**
     *
     */
    private void computeParamValues() {

      if( ( this.site != null ) && ( this.probEqkRupture != null ) ){

          Location loc1 = site.getLocation();
          distanceJB = Double.MAX_VALUE;
          distanceSeis = Double.MAX_VALUE;
          distanceRup = Double.MAX_VALUE;

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

          if(D) {
            System.out.println(C+": distanceRup = " + distanceRup);
            System.out.println(C+": distanceSeis = " + distanceSeis);
            System.out.println(C+": distanceJB = " + distanceJB);
          }

          STALE = false;
      }
      else
        throw new RuntimeException ("Site or ProbEqkRupture is null");

    }
}
