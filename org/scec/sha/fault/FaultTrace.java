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
     *  Description of the Field
     */
    private String faultName;
    private double upperSeismogenicDepth;
    private double lowerSeismogenicDepth;
    private double aveDip;


    public FaultTrace(String faultName){
        super();
        this.faultName = faultName;
    }


    public void setUpperSeismogenicDepth(double upperSeismogenicDepth) { this.upperSeismogenicDepth = upperSeismogenicDepth; }
    public double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public void setLowerSeismogenicDepth(double lowerSeismogenicDepth) { this.lowerSeismogenicDepth = lowerSeismogenicDepth; }
    public double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }

    public void setAveDip(double aveDip) { this.aveDip = aveDip; }
    public double getAveDip() { return aveDip; }


    public void setName( String faultName ) { this.faultName = faultName; }
    public String getName() { return faultName; }

    public int getNumLocations() { return size(); }

    /**
     * If dip is negative reverse all points and make dip positive
     */
    public void normalize(){
        if( aveDip > 0 ) return;
        aveDip *= -1;
        reverse();
    }

    private final static String TAB = "  ";
    public String toString(){

        StringBuffer b = new StringBuffer(C);
        b.append('\n');
        b.append(TAB + "Name = " + faultName);
        b.append(TAB + "Ave. Dip = " + aveDip);
        b.append(TAB + "Upper Seismogenic Depth = " + upperSeismogenicDepth);
        b.append(TAB + "Lower Seismogenic Depth = " + lowerSeismogenicDepth);

        b.append( super.toString() ) ;
        return b.toString();

    }


}
