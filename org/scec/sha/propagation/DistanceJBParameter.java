package org.scec.sha.propagation;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.calc.*;

/**
 * Title:
 * Description:  This finds the shortest distance to the surface projection of the
 * fault.
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class DistanceJBParameter
     extends WarningDoublePropagationEffectParameter
     implements WarningParameterAPI
{


    /* Debbuging variables */
    protected final static String C = "DistanceJBParameter";
    protected final static boolean D = false;


    private final static String NAME = "DistanceJB";
    private final static String UNITS = "km";
    private final static String INFO = "Joyner-Boore Distance (closest distance to surface projection of fault)";
    private final static Double MIN = new Double(0.0);
    private final static Double MAX = new Double(Double.MAX_VALUE);


    /**  */
    public DistanceJBParameter() { init(); }


    /**  */
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

    protected void init(){ init( null ); }

    /**
     * SWR: Note - This function's performance could be increased by having
     * RelativeLocation return a double instead of a Direction for the function call
     * <code>Direction dir = RelativeLocation.getDirection(loc1, loc2)</code>
     */
    protected void calcValueFromSiteAndPE(){
        if( ( this.site != null ) && ( this.potentialEarthquake != null ) ){

            Location loc1 = site.getLocation();
            double minDistance = 999999;
            double currentDistance;

            ListIterator it = potentialEarthquake.getRuptureSurface().getLocationsIterator();
            while( it.hasNext() ){

                Object obj = it.next();
                Location loc2 = (Location)obj;
                Direction dir = RelativeLocation.getDirection(loc1, loc2);
                currentDistance = dir.getHorzDistance();
                if( currentDistance < minDistance ) minDistance = currentDistance;

            }
            this.setValue( new Double( minDistance ) );
        }
        else this.setValue(null);
    }


    /**
     * This is used to determine what editor to use
     * @return
     */
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
        param.potentialEarthquake = potentialEarthquake;
        if( !this.editable ) param.setNonEditable();

        return param;

    }

}
