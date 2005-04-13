package org.opensha.sha.param;

import java.util.*;

import org.opensha.data.*;
import org.opensha.exceptions.*;
import org.opensha.param.*;
import org.opensha.sha.calc.*;
import org.opensha.calc.RelativeLocation;

/**
 * <b>Title:</b> DistanceJBParameter<p>
 *
 * <b>Description:</b> Special subclass of PropagationEffectParameter.
 * This finds the shortest distance to the surface projection of the fault.
 * <p>
 *
 * @see DistanceRupParameter
 * @see DistanceSeisParameter
 * @author Steven W. Rock
 * @version 1.0
 */
public class DistanceJBParameter
     extends WarningDoublePropagationEffectParameter
     implements WarningParameterAPI
{



    /** Class name used in debug strings */
    protected final static String C = "DistanceJBParameter";
    /** If true debug statements are printed out */
    protected final static boolean D = false;


    /** Hardcoded name */
    public final static String NAME = "DistanceJB";
    /** Hardcoded units string */
    private final static String UNITS = "km";
    /** Hardcoded info string */
    private final static String INFO = "Joyner-Boore Distance (closest distance to surface projection of fault)";
    /** Hardcoded min allowed value */
    private final static Double MIN = new Double(0.0);
    /** Hardcoded max allowed value */
    private final static Double MAX = new Double(Double.MAX_VALUE);


    /**
     * No-Arg constructor that just calls init() with null constraints.
     * All value are allowed.
     */
    public DistanceJBParameter() { init(); }


    /** Constructor that sets up constraints. This is a constrained parameter. */
    public DistanceJBParameter(ParameterConstraintAPI warningConstraint)
        throws ConstraintException
    {
        if( ( warningConstraint != null ) && !( warningConstraint instanceof DoubleConstraint) ){
            throw new ConstraintException(
                C + " : Constructor(): " +
                "Input constraint must be a DoubleConstraint"
            );
        }
        init( (DoubleConstraint)warningConstraint );
    }

    /** Initializes the constraints, name, etc. for this parameter */
    protected void init( DoubleConstraint warningConstraint){
        this.warningConstraint = warningConstraint;
        this.constraint = new DoubleConstraint(MIN,MAX);
        this.constraint.setNullAllowed(false);
        this.name = NAME;
        this.constraint.setName( this.name );
        this.units = UNITS;
        this.info = INFO;
        //setNonEditable();
    }

    /** Initializes the constraints, name, etc. for this parameter */
    protected void init(){ init( null ); }

    /**
     * Note that this does not throw a warning
     */
    protected void calcValueFromSiteAndEqkRup(){
        if( ( this.site != null ) && ( this.eqkRupture != null ) ){

            Location loc1 = site.getLocation();
            double minDistance = 999999;
            double currentDistance;

            ListIterator it = eqkRupture.getRuptureSurface().getLocationsIterator();
            while( it.hasNext() ){

                Location loc2 = (Location) it.next();
                currentDistance = RelativeLocation.getHorzDistance(loc1, loc2);
                if( currentDistance < minDistance ) minDistance = currentDistance;

            }
            this.setValueIgnoreWarning( new Double( minDistance ) );
        }
        else this.setValue(null);
    }


    /** This is used to determine what widget editor to use in GUI Applets.  */
    public String getType() {
        String type = "DoubleParameter";
        // Modify if constrained
        ParameterConstraintAPI constraint = this.constraint;
        if (constraint != null) type = "Constrained" + type;
        return type;
    }


    /**
     *  Returns a copy so you can't edit or damage the origial.<P>
     *
     * Note: this is not a true clone. I did not clone Site or ProbEqkRupture.
     * PE could potentially have a million points, way to expensive to clone. Should
     * not be a problem though because once the PE and Site are set, they can not
     * be modified by this class. The clone has null Site and PE parameters.<p>
     *
     * This will probably have to be changed in the future once the use of a clone is
     * needed and we see the best way to implement this.
     *
     * @return    Exact copy of this object's state
     */
    public Object clone() {

        DoubleConstraint c1 = null;
        DoubleConstraint c2 = null;

        if( constraint != null ) c1 = ( DoubleConstraint ) constraint.clone();
        if( warningConstraint != null ) c2 = ( DoubleConstraint ) warningConstraint.clone();

        Double val = null, val2 = null;
        if( value != null ) {
            val = ( Double ) this.value;
            val2 = new Double( val.doubleValue() );
        }

        DistanceJBParameter param = new DistanceJBParameter(  );
        param.info = info;
        param.value = val2;
        param.constraint = c1;
        param.warningConstraint = c2;
        param.name = name;
        param.info = info;
        param.site = site;
        param.eqkRupture = eqkRupture;
        if( !this.editable ) param.setNonEditable();

        return param;

    }

}
