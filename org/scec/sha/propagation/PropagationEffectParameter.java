package org.scec.sha.propagation;

import org.scec.data.*;
import org.scec.exceptions.*;
import org.scec.param.*;
import org.scec.sha.earthquake.*;

/**
 * <b>Title:</b> PropagationEffectParameter<br>
 * <b>Description:</b> <br>
 *
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
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

    /* Debbuging variables */
    protected final static String C = "PropagationEffectParameter";
    protected final static boolean D = false;

    /** The Site used for calculating the PropagationEffect */
    protected Site site = null;

    /** The ProbEqkRupture used for calculating the PropagationEffect */
    protected ProbEqkRupture probEqkRupture = null;





    /**
     * this is called whenever either the Site or
     * ProbEqkRupture has been changed to
     * update the value stored in this parameter.
     */
    protected abstract void calcValueFromSiteAndPE();


    /* ***************************/
    /** @todo  Getters / Setters */
    /* ***************************/

    public Object getValue(ProbEqkRupture probEqkRupture, Site site){
        this.probEqkRupture = probEqkRupture;
        this.site = site;
        calcValueFromSiteAndPE();
        return super.getValue();
    }

    /** The ProbEqkRupture must have already been set */
    public Object getValue(Site site){
        this.site = site;
        calcValueFromSiteAndPE();
        return this.value;
    }

    /** The Site must have already been set */
    public Object getValue(ProbEqkRupture probEqkRupture){
        this.probEqkRupture = probEqkRupture;
        calcValueFromSiteAndPE();
        return this.value;
    }

    /** The ProbEqkRupture and Site must have already been set */
    public Object getValue(){ return this.value; }

    public void setSite(Site site){
        this.site = site;
        calcValueFromSiteAndPE();
    }
    public Site getSite(){ return site; }

    public void setProbEqkRupture(ProbEqkRupture probEqkRupture){
        this.probEqkRupture = probEqkRupture;
        calcValueFromSiteAndPE();
    }
    public ProbEqkRupture getProbEqkRupture(){ return probEqkRupture; }

    /** Special value setter that asks the constraint object if this value is allowed
     *  else it throws and exception
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

    public String getType() { return C; }



    /** Compares the values to see if they are the same */
    public abstract int compareTo(Object obj) throws ClassCastException;

    /** Compares value to see if equal */
    public boolean equals(Object obj) throws ClassCastException{
        if( compareTo(obj) == 0) return true;
        else return false;
    }





}
