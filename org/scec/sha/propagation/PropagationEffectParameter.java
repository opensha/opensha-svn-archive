package org.scec.sha.propagation;

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
 * values from having a Site and ProbEqkRupture set. <p>
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

    /** The ProbEqkRupture used for calculating the PropagationEffect */
    protected ProbEqkRupture probEqkRupture = null;


    /**
     * This is called whenever either the Site or
     * ProbEqkRupture has been changed to
     * update the value stored in this parameter. <p>
     *
     * Subclasses implement this in their own way. This is what
     * differentiates different subclasses.
     */
    protected abstract void calcValueFromSiteAndPE();


    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/
    /* ***************************/
    /** @todo  PropagationEffectParameterAPI Interface */
    /* ***************************/



    /** Sets the independent variables (Site and ProbEqkRupture) then calculates and returns the value */
    public Object getValue(ProbEqkRupture probEqkRupture, Site site){
        this.probEqkRupture = probEqkRupture;
        this.site = site;
        calcValueFromSiteAndPE();
        return super.getValue();
    }

    /** Sets the site and recalculates the value. The ProbEqkRupture must have already been set */
    public Object getValue(Site site){
        this.site = site;
        calcValueFromSiteAndPE();
        return this.value;
    }

    /** Sets the ProbEqkRupture and recalculates the value. The Site must have already been set */
    public Object getValue(ProbEqkRupture probEqkRupture){
        this.probEqkRupture = probEqkRupture;
        calcValueFromSiteAndPE();
        return this.value;
    }

    /** Sets the independent variables (Site and ProbEqkRupture) then calculates the value */
    public void setValue(ProbEqkRupture probEqkRupture, Site site){
        this.probEqkRupture = probEqkRupture;
        this.site = site;
        calcValueFromSiteAndPE();
    }

    /** The ProbEqkRupture and Site must have already been set */
    public Object getValue(){ return this.value; }

    /** Sets the Site and the value is recalculated */
    public void setSite(Site site){
        this.site = site;
        calcValueFromSiteAndPE();
    }
    /** Returns the Site associated with this Parameter */
    public Site getSite(){ return site; }

    /** Sets the ProbEqkRupture associated with this Parameter */
    public void setProbEqkRupture(ProbEqkRupture probEqkRupture){
        this.probEqkRupture = probEqkRupture;
        calcValueFromSiteAndPE();
    }
    /** Returns the ProbEqkRupture associated with this Parameter */
    public ProbEqkRupture getProbEqkRupture(){ return probEqkRupture; }

    /**
     * Overides the calculation. Let's the programmer set the value as in the
     * standard way as any other parameter. Site and ProbEqkRupture values are
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



}
