package org.scec.data;

import java.util.*;

import org.scec.param.*;
import org.scec.util.*;


/**
 *  <b>Title:</b> Site<p>
 *
 *  <b>Description:</b> This class hold the information about a site. Each
 *  Parameter object represents a site parameter used by an
 *  IntensityMeasureRelationship. The constructor will create some default
 *  Parameters for previously published relationships (e.g., AS_1997,
 *  Campbell_1997, Sadigh_1997, Field_1997, and Abrahamson_2000), but a method
 *  will also be provided so that one can add others if so desired. <p>
 *
 *  An IntensityMeasureRalationship object will grab whatever Parameter values
 *  it needs from the Site object that is passed to it. <p>
 *
 *  Provide methods for setting all site parameters from, for example, Vs30
 *  (authors must approve how this is done)? <p>
 *
 *  <b>Copyright:</b> Copyright (c)<br>2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Sid Hellman
 * @created    February 26, 2002
 * @version    1.0
 */

public class Site extends ParameterList implements NamedObjectAPI {

    /**
     *  Class name - used for debugging
     */
    protected final static String C = "Site";

    /**
     *  Boolean when set prints out debugging statements
     */
    protected final static boolean D = false;

    /**
     *  Name of the site.
     */
    public String name;

    /**
     *  Site location.
     */
    protected Location location;


    /**
     *  No-Arg Constructor for the Site object. Currenlty does nothing.
     */
    public Site() { }


    /**
     *  Constructor for the Site object that sets the location.
     *
     * @param  location  The site location
     */
    public Site( Location location ) { this.location = location; }


    /**
     *  Constructor for the Site object that sets the site location
     *  and the name.
     *
     * @param  location  Site location
     * @param  name      Site name
     */
    public Site( Location location, String name ) {
        this.location = location;
        this.name = name;
    }


    /**
     *  Sets the name of the Site.
     *
     * @param  name  The new name
     */
    public void setName( String name ) { this.name = name; }


    /**
     *  Sets the location attribute of the Site object.
     *
     * @param  location  The new location value
     */
    public void setLocation( Location location ) {
        this.location = location;
    }


    /**
     *  Returns the name of the Site.
     *
     * @return    The name
     */
    public String getName() { return name; }


    /**
     *  Returns the location of the Site.
     *
     * @return    The site location
     */
    public Location getLocation() { return location; }



    /**
     * Represents the current state of the Site parameters and variables as a
     * String. Useful for debugging.
     *
     * @return name, location and all parameters as string
     */
    public String toString(){


        StringBuffer b = new StringBuffer();
        b.append(C);
        //b.append('\n');
        b.append(" : ");


        b.append("Name = ");
        b.append(name);
        //b.append('\n');
        b.append(" : ");

        b.append("Location = ");
        b.append(location.toString());
        //b.append('\n');
        b.append(" : ");

        b.append("Parameters = ");
        b.append( super.toString() );

        return b.toString();

    }

    public boolean equalsSite(Site site){

        if( !name.equals( site.name ) ) return false;
        if( !location.equalsLocation( site.location ) ) return false;

        // Not same size, can't be equal
        if( this.size() != site.size() ) return false;

        // Check each individual Parameter
        ListIterator it = this.getParametersIterator();
        while(it.hasNext()){

            // This list's parameter
            ParameterAPI param1 = (ParameterAPI)it.next();

            // List may not contain parameter with this list's parameter name
            if ( !site.containsParameter(param1.getName()) ) return false;

            // Found two parameters with same name, check equals, actually redundent,
            // because that is what equals does
            ParameterAPI param2 = (ParameterAPI)site.getParameter(param1.getName());
            if( !param1.equals(param2) ) return false;

            // Now try compare to to see if value the same, can fail if two values
            // are different, or if the value object types are different
            try{ if( param1.compareTo( param2 ) != 0 ) return false; }
            catch(ClassCastException ee) { return false; }

        }

        return true;
    }

    public boolean equals(Object obj){
        if(obj instanceof Site) return equalsSite( (Site)obj );
        else return false;
    }


    /**
     * Returns a copy of this list, therefore any changes to the copy
     * cannot affect this original list.
     */
    public Object clone(){


        String S = C + ": clone(): ";

        Site site = new Site();
        site.setName( this.getName() );
        site.setLocation( (Location)this.location.clone() );

        if( this.size() < 1 ) return site;

        Enumeration enum = params.keys();
        while(enum.hasMoreElements()){


            String key = (String)enum.nextElement();
            if(D) System.out.println(S + "Next Parameter Key = " + key);


            ParameterAPI param = (ParameterAPI)params.get(key);
            if(D) System.out.println(S + param.toString());

            site.addParameter( (ParameterAPI)param.clone() );
        }

        return site;

    }

}
