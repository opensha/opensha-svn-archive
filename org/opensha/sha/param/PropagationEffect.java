package org.opensha.sha.param;

import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.earthquake.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.param.WarningDoublePropagationEffectParameter;


/**
 * <b>Title:</b> PropagationEffect<p>
 *
 * <b>Description:</b>
 *
 *
 * @author Ned Field
 * @version 1.0
 */
public class PropagationEffect implements java.io.Serializable, ParameterChangeListener{

    private final static String C = "PropagationEffect";
    private final static boolean D = false;

    private boolean APPROX_HORZ_DIST = true;
    private boolean POINT_SRC_CORR = true;

    // Approx Horz Dist Parameter
    public final static String APPROX_DIST_PARAM_NAME = "Use Approximate Distance";
    private final static String APPROX_DIST_PARAM_INFO = "Horz. dist. calculated as: 111 * ( (lat1-lat2)^2 + (cos(0.5*(lat1+lat2))*(lon1-lon2))^2 )^0.5";
    BooleanParameter approxDistParam;

    // Point source correction Parameter
    public final static String POINT_SRC_CORR_PARAM_NAME = "Point-Source Correction";
    private final static String POINT_SRC_CORR_PARAM_INFO = "Use median distance correction for point sources";
    BooleanParameter pointSrcCorrParam;

    protected ParameterList adjustableParams;

    /** The Site used for calculating the PropagationEffect parameter values. */
    protected Site site = null;

    /** The EqkRupture used for calculating the PropagationEffect parameter values.*/
    protected EqkRupture eqkRupture = null;

    /** this distance measure for the DistanceRupParameter */
    protected double distanceRup;

    /** this distance measure for the DistanceJBParameter */
    protected double distanceJB;

    /** this distance measure for the DistanceSeisParameter */
    protected double distanceSeis;

    // this tells whether values are out of date w/ respect to current Site and EqkRupture
    protected boolean STALE = true;

    /** No Argument consructor */
    public PropagationEffect() {

      approxDistParam = new BooleanParameter(APPROX_DIST_PARAM_NAME, new Boolean(APPROX_HORZ_DIST));
      approxDistParam.setInfo(APPROX_DIST_PARAM_INFO);
      approxDistParam.addParameterChangeListener(this);

      pointSrcCorrParam = new BooleanParameter(POINT_SRC_CORR_PARAM_NAME, new Boolean(POINT_SRC_CORR));
      pointSrcCorrParam.setInfo(POINT_SRC_CORR_PARAM_INFO);
      pointSrcCorrParam.addParameterChangeListener(this);

      adjustableParams = new ParameterList();
      adjustableParams.addParameter(approxDistParam);
      adjustableParams.addParameter(pointSrcCorrParam);

    }

    /** Constructor that is give Site and EqkRupture objects */
    public PropagationEffect( Site site, EqkRupture eqkRupture) {
      this();
      this.site = site;
      this.eqkRupture = eqkRupture;
    }

    /** Returns the Site object */
    public Site getSite() { return site; }

    /** Returns the EqkRupture object */
    public EqkRupture getEqkRupture() { return eqkRupture; }

    /** Sets the Site object */
    public void setSite(Site site) {
        this.site = site;
        STALE = true;
    }

    /** Sets the EqkRupture object */
    public void setEqkRupture(EqkRupture eqkRupture) {
        this.eqkRupture = eqkRupture;
        STALE = true;
    }

    /** Sets both the EqkRupture and Site object */
    public void setAll(EqkRupture eqkRupture, Site site) {
        this.eqkRupture = eqkRupture;
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

      if( ( this.site != null ) && ( this.eqkRupture != null ) ){

          Location loc1 = site.getLocation();
          distanceJB = Double.MAX_VALUE;
          distanceSeis = Double.MAX_VALUE;
          distanceRup = Double.MAX_VALUE;

          double horzDist, vertDist, rupDist, jbDist, seisDist;

          ListIterator it = eqkRupture.getRuptureSurface().getLocationsIterator();
          while( it.hasNext() ){

            Location loc2 = (Location) it.next();

            // get the vertical distance
            vertDist = RelativeLocation.getVertDistance(loc1, loc2);

            // get the horizontal dist depending on desired accuracy
            if(APPROX_HORZ_DIST)
              horzDist = RelativeLocation.getApproxHorzDistance(loc1, loc2);
            else
              horzDist = RelativeLocation.getHorzDistance(loc1,loc2);

            // make point source correction if desired
            if(eqkRupture.getRuptureSurface().getNumCols() == 1 &&
                 eqkRupture.getRuptureSurface().getNumRows() == 1) {
              if(POINT_SRC_CORR) {
                // Wells and Coppersmith L(M) for "all" focal mechanisms
                double halfRupLen =  0.5 * Math.pow(10.0,-3.22+0.69*eqkRupture.getMag());
                if(halfRupLen >= horzDist*0.7071)
                  horzDist *= 0.7071; // /= 2^-0.5
                else
                  horzDist *= Math.sqrt(1 + (halfRupLen/horzDist)*(halfRupLen/horzDist) - 1.4142*halfRupLen/horzDist);
              }
            }

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
        throw new RuntimeException ("Site or EqkRupture is null");

    }

    /**
     *  This is the method called by any parameter whose value has been changed
     *
     * @param  event
     */
    public void parameterChange( ParameterChangeEvent event ) {

      APPROX_HORZ_DIST = ((Boolean)approxDistParam.getValue()).booleanValue();
      POINT_SRC_CORR   = ((Boolean)pointSrcCorrParam.getValue()).booleanValue();

    }

    /**
     *
     * @returns the adjustable ParameterList
     */
    public ParameterList getAdjustableParameterList(){
      return this.adjustableParams;
    }

    /**
     * get the adjustable parameters
     *
     * @return
     */
    public ListIterator getAdjustableParamsIterator() {
      return adjustableParams.getParametersIterator();
    }


}
