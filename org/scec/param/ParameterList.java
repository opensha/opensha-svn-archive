package org.scec.param;

import java.util.*;
import org.scec.exceptions.ConstraintException;
import org.scec.exceptions.ParameterException;

/**
 * <b>Title:</b> ParameterList<p>
 *
 * <b>Description:</b> List container for parameters. Specialized version of Hashtable.
 * The keys of the hashtable are the names of the parameters.
 * Can add specialized iterators so that it returns only specific types of paramters,
 * i.e. return all DoubleParameters<p>
 *
 * This class assumes that two parameters are equal if they have the same name.
 * This implies that parameters have unique names. This must be the case for
 * all functions below that take a String name as an argument would fail if
 * two or more parameters have the same name.<p>
 *
 * An additional complication is that Parameters can have a constraint with a different name.
 * To handle this a mapping has to be generated such that constraint name can be mapped back
 * to the original parameter name. This is accomplished via a hashtable. This is only performed
 * in the occasional case when the constraint name differs. In most cases the parameter name and
 * constraint name will be identical. Due to uniqueness of parameter names this implies that all
 * constraint names must be unique also, when differing from the constraint name.<P>
 *
 * 4/3/2002 SWR<BR>
 * WARNING - This class needs a little more work and a JUnit test case. I added constraint names
 * but didn't fully implement updating and removing parameters with differing constraint names,
 * only implemented addParameter fully. <p>
 *
 * <b>Copyright:</b> Copyright (c) 2001<p>
 * <b>Company:</b> <p>
 * @author Steven W. Rock
 * @version 1.0
 */

public class ParameterList {


    /* *******************/
    /** @todo  Variables */
    /* *******************/

    /* Debbuging variables */
    protected final static String C = "ParameterList";
    protected final static boolean D = false;

    /** Internal list of parameters - indexed by name */
    protected Hashtable params = new Hashtable();

    /** Internal list of constraint name mapped to parameter name */
    protected Hashtable constraintNameMap = new Hashtable();

    /* **********************/
    /** @todo  Constructors */
    /* **********************/

    /** no arg constructor */
    public ParameterList() {}



    /* ****************************/
    /** @todo  Accessors, Setters */
    /* ****************************/

    /**
     * Adds all parameters of the parameterlist to this one, if the
     * named parameter is not already in the list
     */
    public void addParameterList(ParameterList list2) throws ParameterException{

        ListIterator it = list2.getParametersIterator();
        while( it.hasNext() ){
            ParameterAPI param = (ParameterAPI)it.next();
            if( !this.containsParameter(param) ){ this.addParameter(param); }        }

    }

    /** adds the parameter if it doesn't exist, else throws exception */
    public void addParameter(ParameterAPI param) throws ParameterException{

        String S = C + ": addParameter(): ";

        String name = param.getName();
        String constraintName = param.getConstraintName();

        if( !params.containsKey(name) ) params.put(name, param);
        else throw new ParameterException(S + "A Parameter already exists named " + name);


        if( constraintName == null || constraintName.equals("") || constraintName.equals( name )) return;

        if( !constraintNameMap.containsKey(constraintName) ) {
            constraintNameMap.put(constraintName, name);
        }
        else{
            params.remove(name);
            throw new ParameterException(S + "A Parameter already exists with this constraint named " + constraintName);
        }



    }

    /**
     * Maps back the constraint name to parameter name if this is a constraint name
     * @param name      The value to check if it is a constraint
     * @return          Either the unmodified name if not constraint name, else the parameter
     * name from the mappings if this is a constraint name.
     */
    public String getParameterName( String name ){
        if( constraintNameMap.containsKey(name) ) return (String)constraintNameMap.get( name );
        return name;
    }

    /** returns parameter if exist else throws exception */
    public ParameterAPI getParameter(String name) throws ParameterException {

        name = getParameterName( name );
        if( params.containsKey(name) ) {
            ParameterAPI param = (ParameterAPI)params.get(name);
            return param;
        }
        else{
            String S = C + ": getParameter(): ";
            throw new ParameterException(S + "No parameter exists named " + name);
        }

    }


    /** returns parameter contained value object, if exist else throws exception */
    public Object getValue(String name) throws ParameterException{

        name = getParameterName( name );

        if( params.containsKey(name) ) {
            ParameterAPI param = (ParameterAPI)params.get(name);
            Object obj = param.getValue();
            return obj;
        }
        else{
            String S = C + ": getValue(): ";
            throw new ParameterException(S + "No parameter exists named " + name);
        }
    }

    /** set's a new value to a Parameter in the list, if it exists, else throws exception */
    public void setValue(String name, Object value) throws ParameterException, ConstraintException {

        String S = C + ": setValue(): ";
        if(D) System.out.println(S + "Starting");

        name = getParameterName( name );

        if( params.containsKey(name) ) {
            ParameterAPI param = (ParameterAPI)params.get(name);
            param.setValue(value);
        }
        else{

            throw new ParameterException(S + "No parameter exists named " + name);
        }

        if(D) System.out.println(S + "Ending");

    }

    /** returns parameter type of named parameter in list, if not exist throws exception */
    public String getType(String name) throws ParameterException {
        name = getParameterName( name );

        if( params.containsKey(name) ) {
            ParameterAPI param = (ParameterAPI)params.get(name);
            String str = param.getType();
            return str;
        }
        else{
            String S = C + ": getType(): ";
            throw new ParameterException(S + "No parameter exists named " + name);
        }
    }


    /** checks if the parameter exists in the list */
    public boolean containsParameter(ParameterAPI param){

        String name = param.getName();
        if( params.containsKey(name) ) { return true; }
        else{ return false; }

    }

    /** checks if the parameter exists in the list */
    public boolean containsParameter(String paramName){
        if( params.containsKey(paramName) ) { return true; }
        else{ return false; }

    }


    /** removes parameter if it exists, else
     *  throws exception
     */
    public void removeParameter(ParameterAPI param) throws ParameterException {
        String name = param.getName();
        if( params.containsKey(name) ) {
            if( params.contains( param ) ) params.remove(param);
            else{
                String S = C + ": removeParameter(): ";
                throw new ParameterException(S + "The Parameter with the name " + name +
                    " in the list is not the same as the passed in Parameter, unable to remove");
            }
        }
        else{
            String S = C + ": removeParameter(): ";
            throw new ParameterException(S + "No Parameter exist named " + name + ", unable to remove");
        }
    }

    /** removes parameter if it exists, else
     *  throws exception
     */
    public void removeParameter(String name) throws ParameterException {

        if( params.containsKey(name) ) { params.remove(name); }
        else{
            String S = C + ": removeParameter(): ";
            throw new ParameterException(S + "No Parameter exist named " + name + ", unable to remove");
        }
    }


    /**
     * updates an existing parameter with the new value,
     * throws exception if parameter doesn't exist
     */
    public void updateParameter(ParameterAPI param) throws ParameterException {
        String name = param.getName();
        removeParameter(name);
        addParameter(param);
    }

    /** returns an iterator of all parameters in the list */
    public ListIterator getParametersIterator(){

        Vector v = new Vector();
        Enumeration enum = params.elements();

        while(enum.hasMoreElements()){
            Object obj = enum.nextElement();
            v.add(obj);
        }

        return v.listIterator();
    }

    /** returns an iterator of all parameters in the list */
    public ListIterator getParameterNamesIterator(){
        Vector v = new Vector();
        Enumeration enum = params.keys();

        while(enum.hasMoreElements()){
            Object obj = enum.nextElement();
            v.add(obj);
        }

        java.util.Collections.sort(v);
        return v.listIterator();
    }

    /** removes all parameters from the list, making it empty, ready for
     *  new parameters
     */
    public void clear(){ params.clear(); }

    /** returns number of parameters in the list */
    public int size(){ return params.size(); }

    /**
     * Returns true if all the parameters have the same names and values.
     * One use will be to determine if two DisctetizedFunctions
     * are the same, i.e. set up with the same independent parameters
     */
    public boolean equals(ParameterList list){

        // Not same size, can't be equal
        if( this.size() != list.size() ) return false;

        // Check each individual Parameter
        ListIterator it = this.getParametersIterator();
        while(it.hasNext()){

            // This list's parameter
            ParameterAPI param1 = (ParameterAPI)it.next();

            // List may not contain parameter with this list's parameter name
            if ( !list.containsParameter(param1.getName()) ) return false;

            // Found two parameters with same name, check equals, actually redundent,
            // because that is what equals does
            ParameterAPI param2 = (ParameterAPI)list.getParameter(param1.getName());
            if( !param1.equals(param2) ) return false;

            // Now try compare to to see if value the same, can fail if two values
            // are different, or if the value object types are different
            try{ if( param1.compareTo( param2 ) != 0 ) return false; }
            catch(ClassCastException ee) { return false; }

        }

        // Passed all tests - return true
        return true;

    }

    /**
     * Returns true if all the parameters have the same names.
     * One use will be to determine if two DisctetizedFunctions
     * are the same, i.e. set up with the same independent parameters
     */
    public boolean equalNames(ParameterList list){

        // Not same size, can't be equal
        if( this.size() != list.size() ) return false;

        // Check each individual Parameter
        ListIterator it = this.getParametersIterator();
        while(it.hasNext()){

            // This list's parameter
            ParameterAPI param1 = (ParameterAPI)it.next();

            // List may not contain parameter with this list's parameter name
            if ( !list.containsParameter(param1.getName()) ) return false;

        }

        // Passed all tests - return true
        return true;

    }

    /**
     * Returns a copy of this list, therefore any changes to the copy
     * cannot affect this original list.
     */
    public Object clone(){

        String S = C + ": clone(): ";
        ParameterList list = new ParameterList();
        if( this.size() < 1 ) return list;

        Enumeration enum = params.keys();
        while(enum.hasMoreElements()){


            String key = (String)enum.nextElement();
            if(D) System.out.println(S + "Next Parameter Key = " + key);


            ParameterAPI param = (ParameterAPI)params.get(key);
            if(D) System.out.println(S + param.toString());

            list.addParameter( (ParameterAPI)param.clone() );
        }

        return list;

    }


    public String toString(){

        String S = C + ": toString():";

        StringBuffer b = new StringBuffer();
        boolean first = true;
        Enumeration enum = params.keys();

        TreeMap map = new TreeMap();
        while(enum.hasMoreElements()){

            String key = (String)enum.nextElement();
            ParameterAPI param = (ParameterAPI)params.get(key);

            map.put(key, param);

        }


        Iterator it = map.keySet().iterator();
        int counter = 0;
        while(it.hasNext()){


            String key = (String)it.next();
            if(D) System.out.println(S + "Next Parameter Key = " + key);
            counter++;

            ParameterAPI param = (ParameterAPI)params.get(key);
            ParameterConstraintAPI constraint = param.getConstraint();

            boolean ok = true;
            if(constraint instanceof DiscreteParameterConstraintAPI){

                int size = ((DiscreteParameterConstraintAPI)constraint).size();
                if( size < 2) ok = false;

            }

            if( ok ){

                String val = "N/A";
                Object obj = param.getValue();
                if( obj != null) val = obj.toString();

                if(D) System.out.println(S + val);
                if(first){
                    first = false;
                    b.append( key + " = " + val );
                }
                else {
                    b.append( ", " + key + " = " + val );
                    if(counter > 5){
                        b.append('\n');
                        counter = 0;
                    }
                }
            }
        }

        return b.toString();

    }



}
