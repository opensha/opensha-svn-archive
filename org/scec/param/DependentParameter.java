package org.scec.param;

import java.util.*;
import org.scec.exceptions.*;

// Fix - Needs more comments

/**
 * <b>Title:</b> DependentParameter<p>
 * <b>Description:</b> Implementation of the DependentParameterAPI.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class DependentParameter extends Parameter implements
    DependentParameterAPI
{

    /** Internal list of parameters - indexed by name */
    protected TreeMap independentParameters = new TreeMap();


    /**
     *  Empty no-arg constructor.
     */
    public DependentParameter() { super(); }

    /**
     *  This is the main constructor. All subclass constructors call this one.
     *  Constraints must be set first, because the value may not be an allowed
     *  one. Null values are always allowed in the constructor.
     *
     * @param  name                     Name of this parameter
     * @param  constraint               Constraints for this Parameter. May be
     *      set to null
     * @param  units                    The units for this parameter
     * @param  value                    The value object of this parameter.
     * @exception  ConstraintException  Description of the Exception
     * @throws  ConstraintException     This is thrown if the passes in
     *      parameter is not allowed
     */
    public DependentParameter(
        String name,
        ParameterConstraintAPI constraint,
        String units,
        Object value )
            throws ConstraintException
    {
        super(name, constraint, units, value);
        //if( (constraint != null) && (constraint.getName() == null) )
            //constraint.setName( name );

    }



     /**
      * Returns an iterator of all parameters in the list.<p>
      *
      * Note: SWR - I choose to implement the iterator an easy way
      * but not the best way. I create a vector, add all parameters from
      * the Hashtable. Quick and dirty. May want to improve in the future.
      */
    public ListIterator getIndependentParametersIterator(){

        Set set = independentParameters.keySet();
        Iterator it = set.iterator();
        Vector v = new Vector();

        while( it.hasNext() ){
            ParameterAPI param = (ParameterAPI)independentParameters.get( it.next() );
            v.add( param );
        }

        return v.listIterator();
    }




    /** returns parameter if exist else throws exception */
    public ParameterAPI getIndependentParameter(String name) throws ParameterException {

        if( independentParameters.containsKey(name) ) {
            ParameterAPI param = (ParameterAPI)independentParameters.get(name);
            return param;
        }
        else{
            String S = C + ": getParameter(): ";
            throw new ParameterException(S + "No parameter exists named " + name);
        }

    }


    /**
     * Since the storage of the independent parameters are a TreeMap, the parameters are
     * sorted by their names. This guarentees that this function always returns the
     * same value if more than one independent parameter. A normal iterator doesn't guarentee
     * order of returned elements in a Hashtable or HashMap
     * @return
     */
    public String getIndependentParametersKey(){

        // This provides a key for coefficient lookup in hashtable
        StringBuffer key = new StringBuffer( name );

        Set set = independentParameters.keySet();
        Iterator it = set.iterator();

        while( it.hasNext() ){

            Object obj = independentParameters.get( it.next().toString() );
            Object value = ( ( ParameterAPI )obj ).getValue();
            if( value != null ){
                key.append( '/' );
                key.append( value.toString() );
            }
        }
        //System.out.println(C + ": setIntensityMeasureType(): " + key);

        // Update the currently selected coefficient
        return key.toString();

    }

    /** Checks if the parameter exists in the list, returns
     *  true only if it finds a name match. No other comparision is done.
     *  We may want to increase the comparision in the future, i.e. returns
     *  true if has same independent parameters, etc.
     */
    public boolean containsIndependentParameter(String paramName){
        if( independentParameters.containsKey(paramName) ) { return true; }
        else{ return false; }

    }

    /**
     * Clears out any exising parameters, then adds all parameters of the
     * input parameterlist to this object
     */
    public void setIndependentParameters(ParameterList list)throws ParameterException, EditableException{

        String S = C + ": setIndependentParameters(): ";
        checkEditable(S);

        ListIterator it = list.getParametersIterator();
        while( it.hasNext() ){
            ParameterAPI param = (ParameterAPI)it.next();
            if( !containsIndependentParameter( param.getName() ) )
                addIndependentParameter(param);
        }

    }


    /** Adds the parameter if it doesn't exist, else throws exception */
    public void addIndependentParameter(ParameterAPI parameter) throws ParameterException, EditableException{

        String S = C + ": addIndependentParameter(): ";
        checkEditable(S);

        String name = parameter.getName();
        if( !independentParameters.containsKey(name) ) independentParameters.put(name, parameter);
        else throw new ParameterException(S + "A Parameter already exists named " + name);

    }


    /** removes parameter if it exists, else
     *  throws exception
     */
    public void removeIndependentParameter(String name) throws ParameterException, EditableException {

        String S = C + ": removeIndependentParameter(): ";
        checkEditable(S);

        if( independentParameters.containsKey(name) ) { independentParameters.remove(name); }
        else throw new ParameterException(S + "No Parameter exist named " + name + ", unable to remove");

    }





}
