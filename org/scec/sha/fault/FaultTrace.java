package org.scec.sha.fault;

import java.util.*;
import org.scec.util.*;
import org.scec.data.*;


/**
 *  <b>Title:</b> FaultTrace<br>
 *  <b>Description:</b> This simply contains a vector (or array) of Location
 *  objects representing the top trace of a fault (with non-zero depth if it
 *  buried). <br>
 *  <b>Copyright:</b> Copyright (c) 2001<br>
 *  <b>Company:</b> <br>
 *
 *
 * @author     Sid Hellman
 * @created    February 26, 2002
 * @version    1.0
 */

public class FaultTrace extends LocationList implements NamedObjectAPI {

    /**
     *  Fault name field
     */
    private String faultName;


    public FaultTrace(String faultName){
        super();
        this.faultName = faultName;
    }

    public void setName( String faultName ) { this.faultName = faultName; }
    public String getName() { return faultName; }

    public int getNumLocations() { return size(); }


    private final static String TAB = "  ";
    public String toString(){

        StringBuffer b = new StringBuffer(C);
        b.append('\n');
        b.append(TAB + "Name = " + faultName);

        b.append( super.toString() ) ;
        return b.toString();

    }


}
