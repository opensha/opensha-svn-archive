package org.scec.sha.propagation;

import java.util.*;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.calc.*;

/**
 * <p>Title: DistanceSeisParameter</p>
 * <p>Description: This computes the closest distance to the seimogenic part of the fault;
 * that is, the closest distance to the part of the fault that is below the seimogenic
 * thickness (seisDepth); this depth is currently hardwired at 3 km, but we can add
 * setSeisDepth() and getSeisDepth() methods if desired (the setter will have to create
 * a new constraint with seisDepth as the lower bound, which can be done even if the
 * parameter has been set as non editable). </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class DistanceSeisParameter
     extends WarningDoublePropagationEffectParameter
     implements WarningParameterAPI
{


    /* Debbuging variables */
    protected final static String C = "DistanceSeisParameter";
    protected final static boolean D = false;


    private final static String NAME = "DistanceSeis";
    private final static String UNITS = "km";
    private final static String INFO = "Seismogenic Distance (closest distance to seismogenic part of fault surface)";
    // Min value depends on chosen seismogenic thickness
    private final static Double MAX = new Double(Double.MAX_VALUE);

    // set default seismogenic depth
    // actually hard-wired for now.
    private double seisDepth = 3.0;


    /**  */
    public DistanceSeisParameter() { init(); }


    /**  */
    public DistanceSeisParameter(ParameterConstraintAPI warningConstraint)
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
        this.constraint = new DoubleConstraint(seisDepth, Double.MAX_VALUE );
        this.name = NAME;
        this.constraint.setName( this.name );
        this.constraint.setNullAllowed(false);
        this.units = UNITS;
        this.info = INFO;
        //setNonEditable();
    }

    protected void init(){ init( null ); }

    protected void calcValueFromSiteAndPE(){
        if( ( this.site != null ) && ( this.probEqkRupture != null ) ){

            Location loc1 = site.getLocation();
            double minDistance = 999999;
            double totalDist;

            ListIterator it = probEqkRupture.getRuptureSurface().getLocationsIterator();
            while( it.hasNext() ){

                Object obj = it.next();
                Location loc2 = (Location)obj;
                // ignore locations with depth less than siesDepth:
                if (loc2.getDepth() >= seisDepth) {
                    Direction dir = RelativeLocation.getDirection(loc1, loc2);
                    totalDist = dir.getHorzDistance() * dir.getHorzDistance() +
                                dir.getVertDistance() * dir.getVertDistance();
                    if( totalDist < minDistance )  minDistance = totalDist;
                }
            }
            // take square root before returning
            // Steve- is this effiecient?
            this.setValue( new Double( Math.pow ( minDistance , 0.5 ) ));

        }
        else this.setValue(null);

    }


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

        DistanceSeisParameter param = new DistanceSeisParameter(  );
        param.info = info;
        param.value = val2;
        param.constraint = c1;
        param.warningConstraint = c2;
        param.name = name;
        param.info = info;
        param.site = site;
        param.probEqkRupture = probEqkRupture;
        if( !this.editable ) param.setNonEditable();

        return param;

    }

}
