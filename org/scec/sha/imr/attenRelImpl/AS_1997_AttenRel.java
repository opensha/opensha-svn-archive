package org.scec.sha.imr.attenRelImpl;

import java.util.*;
import java.awt.Polygon;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.imr.*;
import org.scec.sha.propagation.*;
import org.scec.sha.surface.*;
import org.scec.calc.RelativeLocation;
import org.scec.util.*;

/**
 * <b>Title:</b> AS_1997_AttenRel<p>
 *
 * <b>Description:</b> This implements the classicIMR (attenuation relationship)
 * developed by Abrahmson and Silva (1997, Seismological Research Letters, vol
 * 68, num 1, pp 94-127) <p>
 *
 * Supported Intensity-Measure Parameters:<p>
 * <UL>
 * <LI>pgaParam - Peak Ground Acceleration
 * <LI>saParam - Response Spectral Acceleration
 * </UL><p>
 * Other Independent Parameters:<p>
 * <UL>
 * <LI>magParam - moment Magnitude
 * <LI>distanceRupParam - closest distance to surface projection of fault
 * <LI>siteTypeParam - "Rock/Shallow-Soil" versus "Deep-Soil"
 * <LI>fltTypeParam - Style of faulting
 * <LI>isOnHangingWallParam - tells if site is directly over the rupture surface
 * <LI>componentParam - Component of shaking (only one)
 * <LI>stdDevTypeParam - The type of standard deviation
 * </UL><p>
 *
 * @author     Edward H. Field
 * @created    April, 2002
 * @version    1.0
 */


public class AS_1997_AttenRel
         extends AttenuationRelationship
         implements
        AttenuationRelationshipAPI,
        NamedObjectAPI {

    // Debugging stuff
    private final static String C = "AS_1997_AttenRel";
    private final static boolean D = false;

    // Name of IMR
    public final static String NAME = "Abrahamson & Silva (1997)";

    // style of faulting options
    public final static String FLT_TYPE_REVERSE = "Reverse";
    public final static String FLT_TYPE_REV_OBL = "Reverse-Oblique";
    public final static String FLT_TYPE_OTHER = "Other";
    public final static String FLT_TYPE_DEFAULT = "Other";

    /**
     * Site Type Parameter ("Rock/Shallow-Soil" versus "Deep-Soil")
     */
     private StringParameter siteTypeParam = null;
     public final static String SITE_TYPE_NAME = "AS Site Type";
     // no units
     public final static String SITE_TYPE_INFO = "Geological conditions at the site";
     public final static String SITE_TYPE_ROCK =  "Rock/Shallow-Soil";
     public final static String SITE_TYPE_SOIL =  "Deep-Soil";
     public final static String SITE_TYPE_DEFAULT =  "Deep-Soil";

    /**
     * Specifies whether the site is directly over the rupture surface.
     * The "AS Defn." is part of the name to distinguish this from the hanging-wall
     * parameter of the CB_2003_AttenRel.<p>
     * This should really be a boolean sublcass of PropagationEffectParameter
     */
    private StringParameter isOnHangingWallParam = null;
    public final static String IS_ON_HANGING_WALL_NAME = "On Hanging Wall?";
    public final static String IS_ON_HANGING_WALL_INFO = "Is site directly over rupture?";
    public final static String IS_ON_HANGING_WALL_TRUE = "Yes";
    public final static String IS_ON_HANGING_WALL_FALSE = "No";
    public final static String IS_ON_HANGING_WALL_DEFAULT = "No";

    // these were given to Ned Field by Norm Abrahamson over the phone
    // (they're not given in their 1997 paper)
    protected final static Double MAG_WARN_MIN = new Double(4.5);
    protected final static Double MAG_WARN_MAX = new Double(8);
    protected final static Double DISTANCE_RUP_WARN_MIN = new Double(0.0);
    protected final static Double DISTANCE_RUP_WARN_MAX = new Double(200.0);


    /**
     * The DistanceRupParameter, closest distance to fault surface.
     */
    private DistanceRupParameter distanceRupParam = null;
    private final static Double DISTANCE_RUP_DEFAULT = new Double( 0 );

    /**
     * Joyner-Boore Distance parameter, used as a proxy for computing their
     * hanging-wall term from a site and probEqkRupture.
     */
    private DistanceJBParameter distanceJBParam = null;
    private final static Double DISTANCE_JB_DEFAULT = new Double( 0 );
    // No waring constraint needed for this


    /**
     * The current set of coefficients based on the selected intensityMeasure
     */
    private AS_1997_AttenRelCoefficients coeff = null;

    /**
     *  Hashtable of coefficients for the supported intensityMeasures
     */
    protected Hashtable horzCoeffs = new Hashtable();
    protected Hashtable vertCoeffs = new Hashtable();

    // coefficients that don't depend on period (but do depend on component):
    private double a2, a4, a13, c1, c5, n;

    // for issuing warnings:
    ParameterChangeWarningListener warningListener = null;




    /**
     *  No-Arg constructor. This initializes several ParameterList objects.
     */
    public AS_1997_AttenRel(ParameterChangeWarningListener warningListener) {

        super();

        this.warningListener = warningListener;

        initCoefficients( );  // This must be called before the next one
        initSupportedIntensityMeasureParams( );

        initProbEqkRuptureParams(  );
        initPropagationEffectParams( );
        initSiteParams();
        initOtherParams( );

        initIndependentParamLists(); // This must be called after the above

    }



    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam; since their paper does not quantify the
     * distinction, Norm advised as follows: Reverse if 67.5<rake<112.5;
     * Oblique-Reverse if 22.5<rake<67.5 or 112.5<rake<157.5; Other is rake
     * is something else (strike slip and normal faulting).
     *
     * @param rake                      Input determines the fault type
     * @return                          Fault Type, either "Reverse",
     * "Reverse-Oblique", or "Other" according to rake as described above.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultTypeFromRake( double rake )
        throws InvalidRangeException
    {
    /* */
        FaultUtils.assertValidRake( rake );
        if( rake >= 67.5 && rake <= 112.5 )       return FLT_TYPE_REVERSE;
        else if ( rake < 67.5 && rake >= 22.5 )   return FLT_TYPE_REV_OBL;
        else if ( rake <= 157.5 && rake > 112.5 ) return FLT_TYPE_REV_OBL;
        else                                      return FLT_TYPE_OTHER;
    }

    /**
     * Determines the style of faulting from the rake angle (which
     * comes from the probEqkRupture object) and fills in the
     * value of the fltTypeParam; since their paper does not quantify the
     * distinction, Norm advised as follows: Reverse if 67.5<rake<112.5;
     * Oblique-Reverse if 22.5<rake<67.5 or 112.5<rake<157.5; Other is rake
     * is something else (strike slip and normal faulting).
     *
     * @param rake                      Input determines the fault type
     * @return                          Fault Type, either "Reverse",
     * "Reverse-Oblique", or "Other" according to rake as described above.
     * @throws InvalidRangeException    If not valid rake angle
     */
    protected static String determineFaultTypeFromRake( Double rake )
        throws InvalidRangeException
    {
        return determineFaultTypeFromRake( rake.doubleValue() );
    }


    /**
     *  This sets the potential-earthquake related parameters (magParam
     *  and fltTypeParam) based on the probEqkRupture passed in.
     *  The internally held probEqkRupture object is also set as that
     *  passed in. Since this object updates more than one parameter, an
     *  attempt is made to rollback to the original parameter values in case
     *  there are any errors thrown in the process.
     *
     * @param  pe  The new probEqkRupture value
     */
    public void setProbEqkRupture( ProbEqkRupture probEqkRupture ) throws ConstraintException{


        Double magOld = (Double)magParam.getValue( );
        String fltOld = (String)fltTypeParam.getValue();


        try {
          // constraints get checked
          magParam.setValue( probEqkRupture.getMag() );
        } catch (WarningException e){
          if(D) System.out.println(C+"Warning Exception:"+e);
        }
        // If fail, rollback to all old values
        try{
            String fltTypeStr = determineFaultTypeFromRake( probEqkRupture.getAveRake() );
            fltTypeParam.setValue(fltTypeStr);
        }
        catch( ConstraintException e ){
            magParam.setValue( (Double)magOld );
            throw e;
        }

        // Set the PE
        this.probEqkRupture = probEqkRupture;

       /* Calculate the PropagationEffectParameters; this is
        * not efficient if both the site and probEqkRupture
        * are set before getting the mean, stdDev, or ExceedProbability
        */
        setPropagationEffectParams();
    }


    /**
     *  This sets the site-related parameter (siteTypeParam) based on what is in
     *  the Site object passed in (the Site object must have a parameter with
     *  the same name as that in siteTypeParam).  This also sets the internally held
     *  Site object as that passed in.
     *
     * @param  site             The new site object
     * @throws ParameterException Thrown if the Site object doesn't contain a
     * Vs30 parameter
     */
    public void setSite( Site site ) throws ParameterException, IMRException, ConstraintException {


        // This will throw a parameter exception if the parameter doesn't exist
        // in the Site object

        ParameterAPI siteType = site.getParameter( SITE_TYPE_NAME );

        // This may throw a constraint exception
         this.siteTypeParam.setValue( siteType.getValue() );

        // Now pass function up to super to set the site
        // Why not just say "this.Site = site" ?? (Ned)

        super.setSite( site );

        // Calculate the PropagationEffectParameters; this is
        // not efficient if both the site and probEqkRupture
        // are set before getting the mean, stdDev, or ExceedProbability
        setPropagationEffectParams();

    }

    /**
     * This sets the two propagation-effect parameters (distanceRupParam and
     * isOnHangingWallParam) based on the current site and probEqkRupture.  The
     * hanging-wall term is rake independent (i.e., it can apply to strike-slip or
     * normal faults as well as reverse and thrust).  However, it is turned off if
     * the dip is greater than 70 degrees.  It is also turned off for point sources
     * regardless of the dip.  These specifications were determined from a series of
     * discussions between Ned Field, Norm Abrahamson, and Ken Campbell.
     */
    protected void setPropagationEffectParams(){

        if( ( this.site != null ) && ( this.probEqkRupture != null ) ){
            try{
              distanceRupParam.setValue( probEqkRupture, site );
            }catch (WarningException e){
              if(D) System.out.println(C+"Warning Exception:"+e);
            }

        // here is the hanging wall term.  This should really be implemented as a
        // formal propagation-effect parameter.
            int numPts = probEqkRupture.getRuptureSurface().getNumCols();

            if(probEqkRupture.getRuptureSurface().getAveDip() < 70 && isOnHangingWall() && numPts > 1)
                isOnHangingWallParam.setValue(IS_ON_HANGING_WALL_TRUE);
            else
                isOnHangingWallParam.setValue(IS_ON_HANGING_WALL_FALSE);

            if (D) System.out.println("HW result: " + isOnHangingWall() + ";  rake & dip: "+
                                      probEqkRupture.getAveRake() + "; " +
                                      probEqkRupture.getRuptureSurface().getAveDip());

//System.out.println("AS_1997 hanging wall value: " + isOnHangingWallParam.getValue().toString());

/* OLD IMPLEMENTATION:

            /* The following is a bit of a hack. It assumes the fault grid spacing
               is less than 1 km, and that points off the bottem end of the fault
               don't have significant hanging-wall effects;p Norm said the latter
               is probably close enough (it's also what Frankel's code does).

               There is a problem that this term will apply to vertical strike-slip faults
               when on the fault trace.  The 70-degree dip threshold was the solution
               recommended to Ned Field by Ken Cambell and Norm Abrahamson (see email
               to Ned on 9-26-02).


            if(probEqkRupture.getRuptureSurface().getAveDip() < 70) {
                distanceJBParam.setValue( probEqkRupture, site );
                if (D) System.out.println("DistJB = " + distanceJBParam.getValue());
                if ( ( (Double)distanceJBParam.getValue() ).doubleValue() <= 1.0 )
                    isOnHangingWallParam.setValue(IS_ON_HANGING_WALL_TRUE);
                else
                    isOnHangingWallParam.setValue(IS_ON_HANGING_WALL_FALSE);
            }
            else // turn it off for vertically dipping faults
                isOnHangingWallParam.setValue(IS_ON_HANGING_WALL_FALSE);

            if (D) System.out.println("Old HW result: " + isOnHangingWallParam.getValue());

            if (D) System.out.println("New HW result: " + isOnHangingWall());
*/
        }
    }

    /**
     * This determines whether the rupture is on the hanging wall by creating a polygon
     * that is extended in the down-dip direction, and then checking whether the site is
     * inside.  This should really be implemented as a PropagationEffectParameter, but we
     * don't yet have a boolearn parameter implemented.
     * @return
     */
    protected boolean isOnHangingWall() {

          // this is used to scale lats and lons to large numbers that can
          // be converted to ints without losing info.
          double toIntFactor = 1.0e7;     // makes results accurate to ~cm.

          GriddedSurfaceAPI surface = this.probEqkRupture.getRuptureSurface();
          int numCols = surface.getNumCols();

          int[] xVals = new int[numCols+2];
          int[] yVals = new int[numCols+2];

          Location loc, loc2, loc3;
          Direction dir;

          for(int c=0; c < numCols; c++) {
              loc = surface.getLocation(0,c);
              xVals[c] = (int) (loc.getLongitude() * toIntFactor);
              yVals[c] = (int) (loc.getLatitude() * toIntFactor);
          }

          // now get the locations projected way down dip
          loc = surface.getLocation(0,numCols-1);
          loc2 = surface.getLocation(surface.getNumRows()-1,numCols-1);
          dir = RelativeLocation.getDirection(loc, loc2);
          dir.setHorzDistance(100.0);    // anything that makes rup dist > 25 km
          loc3 = RelativeLocation.getLocation(loc, dir);
          xVals[numCols] = (int) (loc3.getLongitude() * toIntFactor);
          yVals[numCols] = (int) (loc3.getLatitude() * toIntFactor);

          loc = surface.getLocation(0,0);
          loc2 = surface.getLocation(surface.getNumRows()-1,0);
          dir = RelativeLocation.getDirection(loc, loc2);
          dir.setHorzDistance(100.0);    // anything that makes rup dist > 25 km
          loc3 = RelativeLocation.getLocation(loc, dir);
          xVals[numCols+1] = (int) (loc3.getLongitude() * toIntFactor);
          yVals[numCols+1] = (int) (loc3.getLatitude() * toIntFactor);

          Polygon polygon = new Polygon(xVals,yVals,numCols+2);

          Location siteLoc = this.site.getLocation();

          int siteX = (int)(siteLoc.getLongitude()*toIntFactor);
          int siteY = (int)(siteLoc.getLatitude()*toIntFactor);

          return polygon.contains(siteX, siteY);

    }




    /**
     * This function determines which set of coefficients in the HashMap
     * are to be used given the current intensityMeasure (im) Parameter. The
     * lookup is done keyed on the name of the im, plus the period value if
     * im.getName() == "SA" (seperated by "/").
     *
     * SWR: I choose the name <code>update</code> instead of set, because set is so common
     * to java bean fields, i.e. getters and setters, that set() usually implies
     * passing in a new value to the java bean field. I prefer update or refresh
     * to functions that change internal values internally
     */
    protected void updateCoefficients() throws ParameterException {

        // Check that parameter exists
        if( im == null ) throw new ParameterException( C +
            ": updateCoefficients(): " +
            "The Intensity Measusre Parameter has not been set yet, unable to process."
        );

        StringBuffer key = new StringBuffer( im.getName() );
        if( im.getName() == SA_NAME ) key.append( "/" + periodParam.getValue() );

        // Get component-dependent coefficients
        if ( componentParam.getValue().toString().equals( COMPONENT_AVE_HORZ ) ) {
            // these are the same for all periods
            a2 = 0.512; a4 = -0.144; a13 = 0.17; c1 = 6.4; c5 = 0.03; n = 2;

            // now get the period dependent coeffs
            coeff = ( AS_1997_AttenRelCoefficients )horzCoeffs.get( key.toString() );

// Above replaces the following because I don't think the test is needed (can never be violated)
//            if( horzCoeffs.containsKey( key.toString() ) ) coeff = ( AS_1997_AttenRelCoefficients )horzCoeffs.get( key.toString() );
//            else throw new ParameterException( C + ": setIntensityMeasureType(): " + "Unable to locate coefficients with key = " + key );
        }
        else {
            // these are the same for all periods
            a2 = 0.909; a4 = 0.275; a13 = 0.06; c1 = 6.4; c5 = 0.3; n = 3;

            // now get the period dependent coeffs
            coeff = ( AS_1997_AttenRelCoefficients )vertCoeffs.get( key.toString() );
        }

    }



    /**
     * Calculates the mean of the exceedence probability distribution. <p>
     * @return    The mean value
     */
    public double getMean() throws IMRException{

        double mag, dist, mean;
        String fltType, isHW, siteType, component;

        try{
            mag = ((Double)magParam.getValue()).doubleValue();
            dist = ((Double)distanceRupParam.getValue()).doubleValue();
            fltType = fltTypeParam.getValue().toString();
            siteType = siteTypeParam.getValue().toString();
            isHW = isOnHangingWallParam.getValue().toString();
            component = componentParam.getValue().toString();
        }
        catch(NullPointerException e){
            throw new IMRException(C + ": getMean(): " + ERR);
        }

        double F, f5, rockMeanPGA, rockMean;
        int HW;

        if ( fltType.equals( FLT_TYPE_REVERSE ) )      F = 1.0;
        else if ( fltType.equals( FLT_TYPE_REV_OBL ) ) F = 0.5;
        else                                           F = 0.0;

        if ( isHW.equals( IS_ON_HANGING_WALL_TRUE ) ) HW = 1;
        else HW = 0;

        // Get PGA coefficients
        if ( componentParam.getValue().toString().equals( COMPONENT_AVE_HORZ ) ) {
            coeff = ( AS_1997_AttenRelCoefficients ) horzCoeffs.get( PGA_NAME );
            a2 = 0.512; a4 = -0.144; a13 = 0.17; c1 = 6.4; c5 = 0.03; n = 2;
        }
        else {
            coeff = ( AS_1997_AttenRelCoefficients ) vertCoeffs.get( PGA_NAME );
            a2 = 0.909; a4 = 0.275; a13 = 0.06; c1 = 6.4; c5 = 0.3; n = 3;
        }

        // Get mean rock PGA
        rockMeanPGA = calcRockMean(mag, dist, F, HW);

        // now set coefficients for the current im and component (inefficent if im=PGA)
        updateCoefficients();

        rockMean = calcRockMean(mag, dist, F, HW);

        // Compute f5, the site response term
        if(siteType.equals( SITE_TYPE_SOIL )) {
            f5 = coeff.a10 + coeff.a11*Math.log(Math.exp(rockMeanPGA) + c5);

            mean = rockMean + f5;        // Norm's S=1
        }
        else mean = rockMean;            // Norm's S=0

       // return the result
        return mean;
    }


    /**
     * This calculates the mean (natural log) for a rock site
     * @param mag magnidue
     * @param dist distanceRup
     * @param F    style of faulting factor (0, 0.5, or 1.0)
     * @param HW  1 if on hanging wall; 0 otherwise
     * @return  Mean for a rock site
     */
    private double calcRockMean(double mag, double dist, double F, int HW) {

        //Norm's sub-equation terms (all but f5):
        double f1, f3, f4, fHWM, fHWRrup;

        double R = Math.sqrt( dist*dist + coeff.c4*coeff.c4 );

        // Compute f1
        if(mag<=c1)
            f1 = coeff.a1 + a2*(mag-c1) + coeff.a12*Math.pow(8.5 - mag, n) +
              Math.log(R)*(coeff.a3 + a13*(mag-c1));
        else
            f1 = coeff.a1 + a4*(mag-c1) + coeff.a12*Math.pow(8.5 - mag, n) +
              Math.log(R)*(coeff.a3 + a13*(mag-c1));
/*
System.out.println( coeff.a1 );
System.out.println( a4 );
System.out.println( c1 );
System.out.println( coeff.a12 );
System.out.println( n );
System.out.println( coeff.a3 );
System.out.println( a13 );
*/
        // Compute f3, the style of faulting factor
        if(mag <=5.8)
            f3 = coeff.a5;
        else if (mag>5.8 && mag<c1)
            f3 = coeff.a5 + (coeff.a6-coeff.a5)*(mag-5.8)/(c1-5.8);
        else
            f3= coeff.a6;


// Compute f4, compute the hanging wall effect

        // only do these calculations if it's not going to be zeroed out
        if( HW == (int) 1 ) {
            if (mag<=5.5)                 fHWM=0.0;
            else if (mag>5.5 && mag<6.5)  fHWM=mag-5.5;
            else                          fHWM=1.0;

            if(dist<=4.0)		  fHWRrup = 0;
            else if (dist>4 && dist<=8)   fHWRrup = coeff.a9*(dist-4)/4;
            else if (dist>8 && dist<=18)  fHWRrup = coeff.a9;
            else if (dist>18 && dist<=25) fHWRrup = coeff.a9*(1 - (dist-18)/7);
            else 			  fHWRrup = 0;

//          f4 = fHWM*fHWRrup;
            return f1 + F*f3 + fHWM*fHWRrup;
        }
        else
            return f1 + F*f3;
//            f4 = 0;   	// set it to anything since HW = 0

//        return f1 + F*f3 + HW*f4;
    }

    /**
     * @return    The stdDev value
     */
    public double getStdDev() throws IMRException {


        if ( stdDevTypeParam.getValue().equals( STD_DEV_TYPE_NONE ) )
            return 0;
        else {

            // this is inefficient if the im has not been changed in any way
            updateCoefficients();

            // only horz case implemented
            double mag = ((Double)magParam.getValue()).doubleValue();
            if(mag <= 5.0)
                return  coeff.b5 ;
            else if (mag > 5.0 && mag < 7.0)
                return (coeff.b5 - coeff.b6*(mag-5.0));
            else
                return (coeff.b5-2*coeff.b6);
        }
    }


    public void setParamDefaults(){

        siteTypeParam.setValue( SITE_TYPE_DEFAULT );
        magParam.setValue( MAG_DEFAULT );
        fltTypeParam.setValue( FLT_TYPE_DEFAULT );
        distanceRupParam.setValue( DISTANCE_RUP_DEFAULT );
        saParam.setValue( SA_DEFAULT );
        periodParam.setValue( PERIOD_DEFAULT );
        dampingParam.setValue(DAMPING_DEFAULT);
        pgaParam.setValue( PGA_DEFAULT );
        componentParam.setValue( COMPONENT_DEFAULT );
        stdDevTypeParam.setValue( STD_DEV_TYPE_DEFAULT );
        isOnHangingWallParam.setValue( IS_ON_HANGING_WALL_DEFAULT );

    }


    /**
     * This creates the lists of independent parameters that the various dependent
     * parameters (mean, standard deviation, exceedance probability, and IML at
     * exceedance probability) depend upon. NOTE: these lists do not include anything
     * about the intensity-measure parameters or any of thier internal
     * independentParamaters.
     */
    protected void initIndependentParamLists() {

        // params that the mean depends upon
        meanIndependentParams.clear();
        meanIndependentParams.addParameter( distanceRupParam );
        meanIndependentParams.addParameter( siteTypeParam );
        meanIndependentParams.addParameter( magParam );
        meanIndependentParams.addParameter( fltTypeParam );
        meanIndependentParams.addParameter( isOnHangingWallParam );
        meanIndependentParams.addParameter( componentParam );

        // params that the stdDev depends upon
        stdDevIndependentParams.clear();
        stdDevIndependentParams.addParameter( stdDevTypeParam );
        stdDevIndependentParams.addParameter( magParam );
        stdDevIndependentParams.addParameter( componentParam );

        // params that the exceed. prob. depends upon
        exceedProbIndependentParams.clear();
        exceedProbIndependentParams.addParameter( distanceRupParam );
        exceedProbIndependentParams.addParameter( siteTypeParam );
        exceedProbIndependentParams.addParameter( magParam );
        exceedProbIndependentParams.addParameter( fltTypeParam );
        exceedProbIndependentParams.addParameter( isOnHangingWallParam );
        exceedProbIndependentParams.addParameter( componentParam );
        exceedProbIndependentParams.addParameter( stdDevTypeParam );
        exceedProbIndependentParams.addParameter( this.sigmaTruncTypeParam );
        exceedProbIndependentParams.addParameter( this.sigmaTruncLevelParam );

        // params that the IML at exceed. prob. depends upon
        imlAtExceedProbIndependentParams.addParameterList(exceedProbIndependentParams);
        imlAtExceedProbIndependentParams.addParameter(exceedProbParam);
    }



    /**
     *  Creates the Site-Type parameter and adds it to the siteParams list.
     *  Makes the parameters noneditable.
     */
    protected void initSiteParams( ) {

        StringConstraint siteConstraint = new StringConstraint();
        siteConstraint.addString( SITE_TYPE_ROCK );
        siteConstraint.addString( SITE_TYPE_SOIL );
        siteConstraint.setNonEditable();
        siteTypeParam = new StringParameter( SITE_TYPE_NAME, siteConstraint, null);
        siteTypeParam.setInfo( SITE_TYPE_INFO );
        siteTypeParam.setNonEditable();

        siteParams.clear();
        siteParams.addParameter( siteTypeParam );

    }


    /**
     *  Creates the two Potential Earthquake parameters (magParam and
     *  fltTypeParam) and adds them to the probEqkRuptureParams
     *  list. Makes the parameters noneditable.
     */
    protected void initProbEqkRuptureParams(  ) {

        // Create magParam
        super.initProbEqkRuptureParams();

       //  Create and add warning constraint to magParam:
        DoubleConstraint warn = new DoubleConstraint(MAG_WARN_MIN, MAG_WARN_MAX);
        warn.setNonEditable();
        magParam.setWarningConstraint(warn);
        magParam.addParameterChangeWarningListener( warningListener );
        magParam.setNonEditable();

        // Fault type parameter
        StringConstraint constraint = new StringConstraint();
        constraint.addString( FLT_TYPE_REVERSE );
        constraint.addString( FLT_TYPE_REV_OBL );
        constraint.addString( FLT_TYPE_OTHER );
        constraint.setNonEditable();
        fltTypeParam = new StringParameter( FLT_TYPE_NAME, constraint, null);
        fltTypeParam.setInfo( FLT_TYPE_INFO );
        fltTypeParam.setNonEditable();

        probEqkRuptureParams.clear();
        probEqkRuptureParams.addParameter( magParam );
        probEqkRuptureParams.addParameter( fltTypeParam );
    }

    /**
     *  Creates the Propagation Effect parameters and adds them to the
     *  propagationEffectParams list. Makes the parameters noneditable.
     */
    protected void initPropagationEffectParams( ) {

        distanceRupParam = new DistanceRupParameter();
        distanceRupParam.addParameterChangeWarningListener( warningListener );
        DoubleConstraint warn = new DoubleConstraint(DISTANCE_RUP_WARN_MIN, DISTANCE_RUP_WARN_MAX);
        warn.setNonEditable();
        distanceRupParam.setWarningConstraint(warn);
        distanceRupParam.setNonEditable();

        // create hanging wall parameter
        StringConstraint HW_Constraint = new StringConstraint();
        HW_Constraint.addString( IS_ON_HANGING_WALL_TRUE );
        HW_Constraint.addString( IS_ON_HANGING_WALL_FALSE );
        HW_Constraint.setNonEditable();
        isOnHangingWallParam = new StringParameter( IS_ON_HANGING_WALL_NAME, HW_Constraint, IS_ON_HANGING_WALL_DEFAULT);
        isOnHangingWallParam.setInfo( IS_ON_HANGING_WALL_INFO );
        isOnHangingWallParam.setNonEditable();

        propagationEffectParams.addParameter( distanceRupParam );
        propagationEffectParams.addParameter( isOnHangingWallParam );

        // This is needed to compute the isOn HangingWallParam; it does not
        // need to be added to any Param List
        distanceJBParam = new DistanceJBParameter();
    }


    /**
     *  Creates the two supported IM parameters (PGA and SA), as well as the
     *  independenParameters of SA (periodParam and dampingParam) and adds
     *  them to the supportedIMParams list. Makes the parameters noneditable.
     */
    protected void initSupportedIntensityMeasureParams( ) {

        // Create saParam (& its dampingParam) and pgaParam:
        super.initSupportedIntensityMeasureParams();

        // Create saParam's "Period" independent parameter:
        DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint();
        TreeSet set = new TreeSet();
        Enumeration keys = horzCoeffs.keys(); // same as for vertCoeffs
        while ( keys.hasMoreElements() ) {
            AS_1997_AttenRelCoefficients coeff = ( AS_1997_AttenRelCoefficients ) horzCoeffs.get( keys.nextElement() );
            if ( coeff.period >= 0 )  set.add( new Double( coeff.period ) );
        }
        Iterator it = set.iterator();
        while ( it.hasNext() ) periodConstraint.addDouble( ( Double ) it.next() );
        periodConstraint.setNonEditable();
        periodParam = new DoubleDiscreteParameter( PERIOD_NAME, periodConstraint, PERIOD_UNITS, null );
        periodParam.setInfo( PERIOD_INFO );
        periodParam.setNonEditable();

        // Set damping constraint as non editable since no other options exist
        dampingConstraint.setNonEditable();

        // Add SA's independent parameters:
        saParam.addIndependentParameter(dampingParam);
        saParam.addIndependentParameter(periodParam);

        // Now Make the parameter noneditable:
        saParam.setNonEditable();

        // Add the warning listeners:
        saParam.addParameterChangeWarningListener( warningListener );
        pgaParam.addParameterChangeWarningListener( warningListener );

        // Put parameters in the supportedIMParams list:
        supportedIMParams.clear();
        supportedIMParams.addParameter( saParam );
        supportedIMParams.addParameter( pgaParam );

    }

    /**
     *  Creates other Parameters that the mean or stdDev depends upon,
     *  such as the Component or StdDevType parameters.
     */
    protected void initOtherParams( ) {

        // init other params defined in parent class
        super.initOtherParams();

        // the Component Parameter
        StringConstraint constraint = new StringConstraint();
        constraint.addString( COMPONENT_AVE_HORZ );
        constraint.addString( COMPONENT_VERT );
        constraint.setNonEditable();
        componentParam = new StringParameter( COMPONENT_NAME, constraint, COMPONENT_DEFAULT );
        componentParam.setInfo( COMPONENT_INFO );
        componentParam.setNonEditable();

        // the stdDevType Parameter
        StringConstraint stdDevTypeConstraint = new StringConstraint();
        stdDevTypeConstraint.addString( STD_DEV_TYPE_TOTAL );
        stdDevTypeConstraint.addString( STD_DEV_TYPE_NONE );
        stdDevTypeConstraint.setNonEditable();
        stdDevTypeParam = new StringParameter( STD_DEV_TYPE_NAME, stdDevTypeConstraint, STD_DEV_TYPE_DEFAULT );
        stdDevTypeParam.setInfo( STD_DEV_TYPE_INFO );
        stdDevTypeParam.setNonEditable();

        // add these to the list
        otherParams.addParameter( componentParam );
        otherParams.addParameter( stdDevTypeParam );

    }


    /**
     *  This creates the hashtable of coefficients for the supported
     *  intensityMeasures (im).  The key is the im parameter name, plus the
     *  period value for SA (separated by "/").  For example, the key for SA
     *  at 1.00 second period is "SA/1.00".
     */
    protected void initCoefficients(  ) {

        String S = C + ": initCoefficients():";
        if ( D ) System.out.println( S + "Starting" );

        horzCoeffs.clear();

        // PGA
        AS_1997_AttenRelCoefficients coeff = new AS_1997_AttenRelCoefficients(PGA_NAME,
            0, 5.6, 1.64, -1.145, 0.61, 0.26, 0.37, -0.417, -0.23, 0, 0.7, 0.135 );

        // SA/5.00
        AS_1997_AttenRelCoefficients coeff0 = new AS_1997_AttenRelCoefficients( SA_NAME + '/' +( new Double( "5.00" ) ).doubleValue() ,
            5.00, 3.5, -1.46, -0.725, 0.4, -0.2, 0, 0.664, 0.04, -0.215, 0.89, 0.087 );
        // SA/4.00
        AS_1997_AttenRelCoefficients coeff1 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "4.00" ) ).doubleValue() ,
            4.00, 3.5, -1.13, -0.725, 0.4, -0.2, 0.039, 0.64, 0.04, -0.1956, 0.88, 0.092 );
        // SA/3.00
        AS_1997_AttenRelCoefficients coeff2 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "3.00" ) ).doubleValue() ,
            3.00, 3.5, -0.69, -0.725, 0.4, -0.156, 0.089, 0.63, 0.04, -0.1726, 0.87, 0.097 );
        // SA/2.00
        AS_1997_AttenRelCoefficients coeff3 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "2.00" ) ).doubleValue() ,
            2.00, 3.5, -0.15, -0.725, 0.4, -0.094, 0.16, 0.61, 0.04, -0.14, 0.85, 0.105 );
        // SA/1.50
        AS_1997_AttenRelCoefficients coeff4 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "1.50" ) ).doubleValue() ,
            1.50, 3.55, 0.26, -0.7721, 0.438, -0.049, 0.21, 0.6, 0.04, -0.12, 0.84, 0.11 );
        // SA/1.00
        AS_1997_AttenRelCoefficients coeff5 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "1.00" ) ).doubleValue() ,
            1.00, 3.7, 0.828, -0.8383, 0.49, 0.013, 0.281, 0.423, 0, -0.102, 0.83, 0.118 );
        // SA/0.85
        AS_1997_AttenRelCoefficients coeff6 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.85" ) ).doubleValue() ,
            0.85, 3.81, 1.02, -0.8648, 0.512, 0.038, 0.309, 0.37, -0.028, -0.0927, 0.82, 0.121 );
        // SA/0.75
        AS_1997_AttenRelCoefficients coeff7 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.75" ) ).doubleValue() ,
            0.75, 3.9, 1.16, -0.8852, 0.528, 0.057, 0.331, 0.32, -0.05, -0.0862, 0.81, 0.123 );
        // SA/0.60
        AS_1997_AttenRelCoefficients coeff8 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.60" ) ).doubleValue() ,
            0.60, 4.12, 1.428, -0.9218, 0.557, 0.091, 0.37, 0.194, -0.089, -0.074, 0.81, 0.127 );
        // SA/0.50
        AS_1997_AttenRelCoefficients coeff9 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.50" ) ).doubleValue() ,
            0.50, 4.3, 1.615, -0.9515, 0.581, 0.119, 0.37, 0.085, -0.121, -0.0635, 0.8, 0.13 );
        // SA/0.46
        AS_1997_AttenRelCoefficients coeff10 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.46" ) ).doubleValue() ,
            0.46, 4.38, 1.717, -0.9652, 0.592, 0.132, 0.37, 0.02, -0.136, -0.0594, 0.8, 0.132 );
        // SA/0.40
        AS_1997_AttenRelCoefficients coeff11 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.40" ) ).doubleValue() ,
            0.40, 4.52, 1.86, -0.988, 0.61, 0.154, 0.37, -0.065, -0.16, -0.0518, 0.79, 0.135 );
        // SA/0.36
        AS_1997_AttenRelCoefficients coeff12 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.36" ) ).doubleValue() ,
            0.36, 4.62, 1.955, -1.0052, 0.61, 0.17, 0.37, -0.123, -0.173, -0.046, 0.79, 0.135 );
        // SA/0.30
        AS_1997_AttenRelCoefficients coeff13 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.30" ) ).doubleValue() ,
            0.30, 4.8, 2.114, -1.035, 0.61, 0.198, 0.37, -0.219, -0.195, -0.036, 0.78, 0.135 );
        // SA/0.24
        AS_1997_AttenRelCoefficients coeff14 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.24" ) ).doubleValue() ,
            0.24, 4.97, 2.293, -1.079, 0.61, 0.232, 0.37, -0.35, -0.223, -0.0238, 0.77, 0.135 );
        // SA/0.20
        AS_1997_AttenRelCoefficients coeff15 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.20" ) ).doubleValue() ,
            0.20, 5.1, 2.406, -1.115, 0.61, 0.26, 0.37, -0.445, -0.245, -0.0138, 0.77, 0.135 );
        // SA/0.17
        AS_1997_AttenRelCoefficients coeff16 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.17" ) ).doubleValue() ,
            0.17, 5.19, 2.43, -1.135, 0.61, 0.26, 0.37, -0.522, -0.265, -0.004, 0.76, 0.135 );
        // SA/0.15
        AS_1997_AttenRelCoefficients coeff17 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.15" ) ).doubleValue() ,
            0.15, 5.27, 2.407, -1.145, 0.61, 0.26, 0.37, -0.577, -0.28, 0.005, 0.75, 0.135 );
        // SA/0.12
        AS_1997_AttenRelCoefficients coeff18 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.12" ) ).doubleValue() ,
            0.12, 5.39, 2.272, -1.145, 0.61, 0.26, 0.37, -0.591, -0.28, 0.018, 0.75, 0.135 );
        // SA/0.10
        AS_1997_AttenRelCoefficients coeff19 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.10" ) ).doubleValue() ,
            0.10, 5.5, 2.16, -1.145, 0.61, 0.26, 0.37, -0.598, -0.28, 0.028, 0.74, 0.135 );
        // SA/0.09
        AS_1997_AttenRelCoefficients coeff20 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.09" ) ).doubleValue() ,
            0.09, 5.54, 2.1, -1.145, 0.61, 0.26, 0.37, -0.609, -0.28, 0.03, 0.74, 0.135 );
        // SA/0.075
        AS_1997_AttenRelCoefficients coeff21 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.075" ) ).doubleValue() ,
            0.075, 5.58, 2.037, -1.145, 0.61, 0.26, 0.37, -0.628, -0.28, 0.03, 0.73, 0.135 );
        // SA/0.06
        AS_1997_AttenRelCoefficients coeff22 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.06" ) ).doubleValue() ,
            0.06, 5.6, 1.94, -1.145, 0.61, 0.26, 0.37, -0.665, -0.28, 0.03, 0.72, 0.135 );
        // SA/0.05
        AS_1997_AttenRelCoefficients coeff23 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.05" ) ).doubleValue() ,
            0.05, 5.6, 1.87, -1.145, 0.61, 0.26, 0.37, -0.62, -0.267, 0.028, 0.71, 0.135 );
        // SA/0.04
        AS_1997_AttenRelCoefficients coeff24 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.04" ) ).doubleValue() ,
            0.04, 5.6, 1.78, -1.145, 0.61, 0.26, 0.37, -0.555, -0.251, 0.0245, 0.71, 0.135 );
        // SA/0.03
        AS_1997_AttenRelCoefficients coeff25 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.03" ) ).doubleValue() ,
            0.03, 5.6, 1.69, -1.145, 0.61, 0.26, 0.37, -0.47, -0.23, 0.0143, 0.7, 0.135 );
        // SA/0.02
        AS_1997_AttenRelCoefficients coeff26 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.02" ) ).doubleValue() ,
            0.02, 5.6, 1.64, -1.145, 0.61, 0.26, 0.37, -0.417, -0.23, 0, 0.7, 0.135 );
        // SA/0.01
        AS_1997_AttenRelCoefficients coeff27 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.01" ) ).doubleValue() ,
            0.01, 5.6, 1.64, -1.145, 0.61, 0.26, 0.37, -0.417, -0.23, 0, 0.7, 0.135 );
        // SA/0.0 -- same as 0.01
        AS_1997_AttenRelCoefficients coeff28 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.0" ) ).doubleValue() ,
            0.00, 5.6, 1.64, -1.145, 0.61, 0.26, 0.37, -0.417, -0.23, 0, 0.7, 0.135 );

        horzCoeffs.put( coeff.getName(), coeff );
        horzCoeffs.put( coeff0.getName(), coeff0 );
        horzCoeffs.put( coeff1.getName(), coeff1 );
        horzCoeffs.put( coeff2.getName(), coeff2 );
        horzCoeffs.put( coeff3.getName(), coeff3 );
        horzCoeffs.put( coeff4.getName(), coeff4 );
        horzCoeffs.put( coeff5.getName(), coeff5 );
        horzCoeffs.put( coeff6.getName(), coeff6 );
        horzCoeffs.put( coeff7.getName(), coeff7 );
        horzCoeffs.put( coeff8.getName(), coeff8 );
        horzCoeffs.put( coeff9.getName(), coeff9 );

        horzCoeffs.put( coeff10.getName(), coeff10 );
        horzCoeffs.put( coeff11.getName(), coeff11 );
        horzCoeffs.put( coeff12.getName(), coeff12 );
        horzCoeffs.put( coeff13.getName(), coeff13 );
        horzCoeffs.put( coeff14.getName(), coeff14 );
        horzCoeffs.put( coeff15.getName(), coeff15 );
        horzCoeffs.put( coeff16.getName(), coeff16 );
        horzCoeffs.put( coeff17.getName(), coeff17 );
        horzCoeffs.put( coeff18.getName(), coeff18 );
        horzCoeffs.put( coeff19.getName(), coeff19 );

        horzCoeffs.put( coeff20.getName(), coeff20 );
        horzCoeffs.put( coeff21.getName(), coeff21 );
        horzCoeffs.put( coeff22.getName(), coeff22 );
        horzCoeffs.put( coeff23.getName(), coeff23 );
        horzCoeffs.put( coeff24.getName(), coeff24 );
        horzCoeffs.put( coeff25.getName(), coeff25 );
        horzCoeffs.put( coeff26.getName(), coeff26 );
        horzCoeffs.put( coeff27.getName(), coeff27 );
        horzCoeffs.put( coeff28.getName(), coeff28 );




        vertCoeffs.clear();

        // PGA
        coeff = new AS_1997_AttenRelCoefficients(PGA_NAME,
            0, 6.00, 1.642, -1.2520, 0.390, -0.050, 0.630, -0.140, -0.220, -0.0000, 0.76, 0.085 );

        // SA/5.00
        coeff0 = new AS_1997_AttenRelCoefficients( SA_NAME + '/' +( new Double( "5.00" ) ).doubleValue() ,
            5.00, 2.50, -2.053, -0.7200, 0.260, -0.100, 0.240, 0.040, -0.220, -0.0670, 0.78, 0.050 );
        // SA/4.00
        coeff1 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "4.00" ) ).doubleValue() ,
            4.00, 2.50, -1.857, -0.7200, 0.260, -0.100, 0.240, 0.040, -0.220, -0.0565, 0.75, 0.050 );
        // SA/3.00
        coeff2 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "3.00" ) ).doubleValue() ,
            3.00, 2.50, -1.581, -0.7200, 0.260, -0.100, 0.240, 0.040, -0.220, -0.0431, 0.72, 0.050 );
        // SA/2.00
        coeff3 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "2.00" ) ).doubleValue() ,
            2.00, 2.50, -1.224, -0.7200, 0.260, -0.008, 0.240, 0.040, -0.220, -0.0240, 0.69, 0.050 );
        // SA/1.50
        coeff4 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "1.50" ) ).doubleValue() ,
            1.50, 2.50, -0.966, -0.7285, 0.260, 0.058, 0.240, 0.025, -0.220, -0.0180, 0.69, 0.050 );
        // SA/1.00
        coeff5 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "1.00" ) ).doubleValue() ,
            1.00, 2.50, -0.602, -0.7404, 0.260, 0.150, 0.240, 0.004, -0.220, -0.0115, 0.69, 0.050 );
        // SA/0.85
        coeff6 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.85" ) ).doubleValue() ,
            0.85, 2.50, -0.469, -0.7451, 0.309, 0.150, 0.273, -0.004, -0.220, -0.0097, 0.69, 0.050 );
        // SA/0.75
        coeff7 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.75" ) ).doubleValue() ,
            0.75, 2.50, -0.344, -0.7488, 0.348, 0.150, 0.299, -0.010, -0.220, -0.0083, 0.69, 0.050 );
        // SA/0.60
        coeff8 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.60" ) ).doubleValue() ,
            0.60, 2.85, -0.087, -0.7896, 0.416, 0.150, 0.345, -0.022, -0.220, -0.0068, 0.69, 0.050 );
        // SA/0.50
        coeff9 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.50" ) ).doubleValue() ,
            0.50, 3.26, 0.145, -0.8291, 0.471, 0.150, 0.383, -0.031, -0.220, -0.0060, 0.69, 0.050 );
        // SA/0.46

        coeff10 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.46" ) ).doubleValue() ,
            0.46, 3.45, 0.271, -0.8472, 0.497, 0.150, 0.400, -0.035, -0.220, -0.0056, 0.69, 0.050 );
        // SA/0.40

        coeff11 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.40" ) ).doubleValue() ,
            0.40, 3.77, 0.478, -0.8776, 0.539, 0.150, 0.428, -0.043, -0.220, -0.0050, 0.69, 0.050 );
        // SA/0.36

        coeff12 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.36" ) ).doubleValue() ,
            0.36, 4.01, 0.617, -0.9004, 0.571, 0.150, 0.450, -0.048, -0.220, -0.0047, 0.69, 0.050 );
        // SA/0.30

        coeff13 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.30" ) ).doubleValue() ,
            0.30, 4.42, 0.878, -0.9400, 0.580, 0.150, 0.488, -0.057, -0.220, -0.0042, 0.69, 0.050 );
        // SA/0.24

        coeff14 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.24" ) ).doubleValue() ,
            0.24, 4.93, 1.312, -1.0274, 0.580, 0.109, 0.533, -0.069, -0.220, -0.0035, 0.69, 0.050 );
        // SA/0.20

        coeff15 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.20" ) ).doubleValue() ,
            0.20, 5.35, 1.648, -1.0987, 0.580, 0.076, 0.571, -0.078, -0.220, -0.0030, 0.69, 0.050 );
        // SA/0.17

        coeff16 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.17" ) ).doubleValue() ,
            0.17, 5.72, 1.960, -1.1623, 0.580, 0.047, 0.604, -0.087, -0.220, -0.0025, 0.70, 0.056 );
        // SA/0.15

        coeff17 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.15" ) ).doubleValue() ,
            0.15, 6.00, 2.170, -1.2113, 0.580, 0.024, 0.630, -0.093, -0.220, -0.0022, 0.72, 0.063 );
        // SA/0.12

        coeff18 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.12" ) ).doubleValue() ,
            0.12, 6.00, 2.480, -1.2986, 0.580, -0.017, 0.630, -0.104, -0.220, -0.0015, 0.74, 0.075 );
        // SA/0.10

        coeff19 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.10" ) ).doubleValue() ,
            0.10, 6.00, 2.700, -1.3700, 0.580, -0.050, 0.630, -0.114, -0.220, -0.0010, 0.76, 0.085 );
        // SA/0.09

        coeff20 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.09" ) ).doubleValue() ,
            0.09, 6.00, 2.730, -1.3700, 0.567, -0.050, 0.630, -0.119, -0.220, -0.0009, 0.76, 0.085 );
        // SA/0.075

        coeff21 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.075" ) ).doubleValue() ,
            0.075, 6.00, 2.750, -1.3700, 0.545, -0.050, 0.630, -0.129, -0.220, -0.0007, 0.76, 0.085 );
        // SA/0.06

        coeff22 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.06" ) ).doubleValue() ,
            0.06, 6.00, 2.710, -1.3700, 0.518, -0.050, 0.630, -0.140, -0.220, -0.0004, 0.76, 0.085 );
        // SA/0.05

        coeff23 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.05" ) ).doubleValue() ,
            0.05, 6.00, 2.620, -1.3700, 0.496, -0.050, 0.630, -0.140, -0.220, -0.0002, 0.76, 0.085 );
        // SA/0.04

        coeff24 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.04" ) ).doubleValue() ,
            0.04, 6.00, 2.420, -1.3700, 0.469, -0.050, 0.630, -0.140, -0.220, -0.0000, 0.76, 0.085 );
        // SA/0.03

        coeff25 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.03" ) ).doubleValue() ,
            0.03, 6.00, 2.100, -1.3168, 0.432, -0.050, 0.630, -0.140, -0.220, -0.0000, 0.76, 0.085 );
        // SA/0.02

        coeff26 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.02" ) ).doubleValue() ,
            0.02, 6.00, 1.642, -1.2520, 0.390, -0.050, 0.630, -0.140, -0.220, -0.0000, 0.76, 0.085 );
        // SA/0.01

        coeff27 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.01" ) ).doubleValue() ,
            0.01, 6.00, 1.642, -1.2520, 0.390, -0.050, 0.630, -0.140, -0.220, -0.0000, 0.76, 0.085 );
        // SA/0.0 -- same as 0.01
        coeff28 = new AS_1997_AttenRelCoefficients( "SA/" +( new Double( "0.0" ) ).doubleValue() ,
            0.00, 6.00, 1.642, -1.2520, 0.390, -0.050, 0.630, -0.140, -0.220, -0.0000, 0.76, 0.085 );

        vertCoeffs.put( coeff.getName(), coeff );
        vertCoeffs.put( coeff0.getName(), coeff0 );
        vertCoeffs.put( coeff1.getName(), coeff1 );
        vertCoeffs.put( coeff2.getName(), coeff2 );
        vertCoeffs.put( coeff3.getName(), coeff3 );
        vertCoeffs.put( coeff4.getName(), coeff4 );
        vertCoeffs.put( coeff5.getName(), coeff5 );
        vertCoeffs.put( coeff6.getName(), coeff6 );
        vertCoeffs.put( coeff7.getName(), coeff7 );
        vertCoeffs.put( coeff8.getName(), coeff8 );
        vertCoeffs.put( coeff9.getName(), coeff9 );

        vertCoeffs.put( coeff10.getName(), coeff10 );
        vertCoeffs.put( coeff11.getName(), coeff11 );
        vertCoeffs.put( coeff12.getName(), coeff12 );
        vertCoeffs.put( coeff13.getName(), coeff13 );
        vertCoeffs.put( coeff14.getName(), coeff14 );
        vertCoeffs.put( coeff15.getName(), coeff15 );
        vertCoeffs.put( coeff16.getName(), coeff16 );
        vertCoeffs.put( coeff17.getName(), coeff17 );
        vertCoeffs.put( coeff18.getName(), coeff18 );
        vertCoeffs.put( coeff19.getName(), coeff19 );

        vertCoeffs.put( coeff20.getName(), coeff20 );
        vertCoeffs.put( coeff21.getName(), coeff21 );
        vertCoeffs.put( coeff22.getName(), coeff22 );
        vertCoeffs.put( coeff23.getName(), coeff23 );
        vertCoeffs.put( coeff24.getName(), coeff24 );
        vertCoeffs.put( coeff25.getName(), coeff25 );
        vertCoeffs.put( coeff26.getName(), coeff26 );
        vertCoeffs.put( coeff27.getName(), coeff27 );
        vertCoeffs.put( coeff28.getName(), coeff28 );

    }

    /**
        * get the name of this IMR
        *
        * @returns the name of this IMR
        */
    public String getName() {
         return NAME;
    }


    /**
     *  <b>Title:</b> AS_1997_AttenRelCoefficients<br>
     *  <b>Description:</b> This class encapsulates all the
     *  coefficients needed to calculate the Mean and StdDev for
     *  the AS_1997_AttenRel.  One instance of this class holds the set of
     *  coefficients for each period (one row of their tables 3/4 (horz) or 5/6 (vert)).<br>
     *  <b>Copyright:</b> Copyright (c) 2001 <br>
     *  <b>Company:</b> <br>
     *
     *
     * @author     Steven W Rock
     * @created    February 27, 2002
     * @version    1.0
     */

    class AS_1997_AttenRelCoefficients
             implements NamedObjectAPI {


        protected final static String C = "AS_1997_AttenRelCoefficients";
        protected final static boolean D = true;

        protected String name;
        protected double period = -1;
        protected double c4;
        protected double a1;
        protected double a3;
        protected double a5;
        protected double a6;
        protected double a9;
        protected double a10;
        protected double a11;
        protected double a12;
        protected double b5;
        protected double b6;

        /**
         *  Constructor for the AS_1997_AttenRelCoefficients object
         *
         * @param  name  Description of the Parameter
         */
        public AS_1997_AttenRelCoefficients( String name ) { this.name = name; }

        /**
         *  Constructor for the AS_1997_AttenRelCoefficients object that sets all values at once
         *
         * @param  name  Description of the Parameter
         */
        public AS_1997_AttenRelCoefficients( String name,  double period,
            double c4,  double a1,  double a3, double a5,  double a6,
            double a9,  double a10, double a11, double a12,
            double b5, double b6 )
       {
            this.name = name;
            this.period = period;   this.c4 = c4;
            this.a1 = a1;           this.a3 = a3;
            this.a5 = a5;           this.a6 = a6;
            this.a9 = a9;           this.a10 = a10;
            this.a11 = a11;         this.a12 = a12;
            this.b5 = b5;           this.b6 = b6;
        }

        /**
         *  Gets the name attribute of the AS_1997_AttenRelCoefficients object
         *
         * @return    The name value
         */
        public String getName() {
            return name;
        }

        /**
         *  Debugging - prints out all cefficient names and values
         *
         * @return    Description of the Return Value
         */
        public String toString() {

            StringBuffer b = new StringBuffer();
            b.append( C );
            b.append( "\n  Period = " + period );   b.append( "\n  c4 = " + c4 );
            b.append( "\n  a1 = " + a1 );           b.append( "\n  a2 = " + a2 );
            b.append( "\n  a3 = " + a3 );           b.append( "\n  a4 = " + a4 );
            b.append( "\n  a5 = " + a5 );           b.append( "\n  a6 = " + a6 );
            b.append( "\n  a9 = " + a9 );           b.append( "\n  a10 = " + a10 );
            b.append( "\n  a11 = " + a11 );         b.append( "\n  a12 = " + a12 );
            b.append( "\n  a13 = " + a13 );         b.append( "\n  c1 = " + c1 );
            b.append( "\n  c5 = " + c5 );           b.append( "\n  n = " + n );
            b.append( "\n  b5 = " + b5 );           b.append( "\n  b6 = " + b6 );
            return b.toString();
        }
    }

}



