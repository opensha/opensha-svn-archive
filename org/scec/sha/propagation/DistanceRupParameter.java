package org.scec.sha.propagation;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.calc.*;
import org.scec.calc.RelativeLocation;


/**
 * <b>Title:</b> DistanceRupParameter<p>
 *
 * <b>Description:</b> Special subclass of PropagationEffectParameter.
 * This finds the shortest distance to the fault surface. <p>
 *
 * @see DistanceJBParameter
 * @see DistanceSeisParameter
 * @author Steven W. Rock
 * @version 1.0
 */
public class DistanceRupParameter
     extends WarningDoublePropagationEffectParameter
     implements WarningParameterAPI
{


    /** Class name used in debug strings */
    protected final static String C = "DistanceRupParameter";
    /** If true debug statements are printed out */
    protected final static boolean D = false;


    /** Hardcoded name */
    private final static String NAME = "DistanceRup";
    /** Hardcoded units string */
    private final static String UNITS = "km";
    /** Hardcoded info string */
    private final static String INFO = "Rupture Distance (closest distance to fault surface)";
    /** Hardcoded min allowed value */
    private final static Double MIN = new Double(0.0);
    /** Hardcoded max allowed value */
    private final static Double MAX = new Double(Double.MAX_VALUE);


    /** No-Arg constructor that calls init(). No constraint so all values are allowed.  */
    public DistanceRupParameter() { init(); }


    /** Constructor that sets up constraints. This is a constrained parameter. */
    public DistanceRupParameter(ParameterConstraintAPI warningConstraint)
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

    /** Sets default fields on the Constraint,  such as info and units. */
    protected void init( DoubleConstraint warningConstraint){
        this.warningConstraint = warningConstraint;
        this.constraint = new DoubleConstraint(MIN, MAX );
        this.constraint.setNullAllowed(false);
        this.name = NAME;
        this.constraint.setName( this.name );
        this.units = UNITS;
        this.info = INFO;
        //setNonEditable();
    }

    /** Sets the warning constraint to null, then initializes the absolute constraint */
    protected void init(){ init( null ); }


    /**
     * SWR: Note - This function's performance could be increased by having
     * RelativeLocation return a double instead of a Direction for the function call
     * <code>Direction dir = RelativeLocation.getDirection(loc1, loc2)</code>
     */
    protected void calcValueFromSiteAndPE(){
        if( ( this.site != null ) && ( this.probEqkRupture != null ) ){

            Location loc1 = site.getLocation();
            double minDistance = 999999;
            double totalDist;

            ListIterator it = probEqkRupture.getRuptureSurface().getLocationsIterator();
            while( it.hasNext() ){

                Object obj = it.next();
                Location loc2 = (Location)obj;
                Direction dir = RelativeLocation.getDirection(loc1, loc2);
                totalDist = dir.getHorzDistance() * dir.getHorzDistance() + dir.getVertDistance() * dir.getVertDistance();
                if( totalDist < minDistance ) minDistance = totalDist;

            }
            // take square root before returning
            // Steve- is this effiecient?
            this.setValue( new Double( Math.pow ( minDistance , 0.5 ) ));

        }
        else this.value = null;


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

        DistanceRupParameter param = new DistanceRupParameter(  );
        param.value = val2;
        param.constraint =  c1;
        param.warningConstraint = c2;
        param.name = name;
        param.info = info;
        param.site = site;
        param.probEqkRupture = probEqkRupture;
        if( !this.editable ) param.setNonEditable();
        return param;
    }

}
