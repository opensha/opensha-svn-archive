package org.scec.sha.param;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;

/**
 * <b>Title:</b> PropagationEffectParameter<p>
 *
 * <b>Description:</b> Propagation Effectg Paraemters
 * deal with special subclass of Parameters that are associated with
 * earthquakes, and know how to calculate their own
 * values from having a Site and EqkRupture set. <p>
 *
 * These values are generally self calculated as opposed
 * t normal Parameters where the values are specifically
 * set calling setValue().<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class PropagationEffectParameter
    extends DependentParameter
    implements
        PropagationEffectParameterAPI,
        DependentParameterAPI,
        ParameterAPI
{


    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Class name used for debug strings. */
    protected final static String C = "PropagationEffectParameter";
    /* If true prints out debbuging statements */
    protected final static boolean D = false;

    /** The Site used for calculating the PropagationEffect */
    protected Site site = null;

    /** The EqkRupture used for calculating the PropagationEffect */
    protected EqkRupture eqkRupture = null;


    /**
     * This is called whenever either the Site or
     * EqkRupture has been changed to
     * update the value stored in this parameter. <p>
     *
     * Subclasses implement this in their own way. This is what
     * differentiates different subclasses.
     */
    protected abstract void calcValueFromSiteAndEqkRup();


    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/
    /* ***************************/
    /** @todo  PropagationEffectParameterAPI Interface */
    /* ***************************/



    /** Sets the independent variables (Site and EqkRupture) then calculates and returns the value */
    public Object getValue(EqkRupture eqkRupture, Site site){
        this.eqkRupture = eqkRupture;
        this.site = site;
        calcValueFromSiteAndEqkRup();
        return super.getValue();
    }

    /** Sets the site and recalculates the value. The EqkRupture must have already been set */
    public Object getValue(Site site){
        this.site = site;
        calcValueFromSiteAndEqkRup();
        return this.value;
    }

    /** Sets the EqkRupture and recalculates the value. The Site must have already been set */
    public Object getValue(EqkRupture eqkRupture){
        this.eqkRupture = eqkRupture;
        calcValueFromSiteAndEqkRup();
        return this.value;
    }

    /** Sets the independent variables (Site and EqkRupture) then calculates the value */
    public void setValue(EqkRupture eqkRupture, Site site){
        this.eqkRupture = eqkRupture;
        this.site = site;
        calcValueFromSiteAndEqkRup();
    }

    /** The EqkRupture and Site must have already been set */
    public Object getValue(){ return this.value; }

    /** Sets the Site and the value is recalculated */
    public void setSite(Site site){
        this.site = site;
        calcValueFromSiteAndEqkRup();
    }
    /** Returns the Site associated with this Parameter */
    public Site getSite(){ return site; }

    /** Sets the EqkRupture associated with this Parameter */
    public void setEqkRupture(EqkRupture eqkRupture){
        this.eqkRupture = eqkRupture;
        calcValueFromSiteAndEqkRup();
    }
    /** Returns the EqkRupture associated with this Parameter */
    public EqkRupture getEqkRupture(){ return eqkRupture; }

    /**
     * Overides the calculation. Let's the programmer set the value as in the
     * standard way as any other parameter. Site and EqkRupture values are
     * ignored. Of course the constraints are still inquiried if the value is
     * allowed. An ConstraintException excpetion is thrown if the value exceedes
     * the constraint.
     */
	public void setValue(Object value) throws ConstraintException, ParameterException{
        String S = C + ": setValue(): ";
        if( !isAllowed(value) ) {
            String err = S + "Value is not allowed: ";
            if( value != null ) err += value.toString();
            else err += "null value";
            throw new ConstraintException(err);
        }
        this.value = value;
    }

     /** function used to determine which GUI widget to use for editing this parameter in an Applet */
    public String getType() { return C; }



    /** Compares the values to see if they are the same, greater than or less than. */
    public abstract int compareTo(Object obj) throws ClassCastException;

    /** Compares value to see if equal */
    public boolean equals(Object obj) throws ClassCastException{
        if( compareTo(obj) == 0) return true;
        else return false;
    }

    /**
     * Standard Java function. Creates a copy of this class instance
     * so originaly can not be modified
     */
    public abstract Object clone();

}
