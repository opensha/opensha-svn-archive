package org.scec.sha.fault;

import java.util.*;
import org.scec.util.*;
import org.scec.data.*;


// Fix - Needs more comments


/**
 *  <b>Title:</b> SimpleFaultData<p>
 *  <b>Description:</b> This object contains all the fault-related information needed
 *  by a SimpleGriddedFaultFactory.  This does not check whether the values make sense
 *  (e.g., it doesn not check that 0²aveDip²90) because these will get checked in the
 *  classes that use this data (and we don't want duplicate checks) <p>
 *
 *
 * @author     Sid Hellman, Steven W. Rock, Ned Field
 * @created    February 26, 2002
 * @version    1.0
 */

public class SimpleFaultData {

    /**
     *  Description of the Field
     */
    private double upperSeismogenicDepth;
    private double lowerSeismogenicDepth;
    private double aveDip;
    private FaultTrace faultTrace;

    protected final static String C = "SimpleFaultData";


    public SimpleFaultData(){ }

    public SimpleFaultData(double aveDip,
                           double lowerSeisDepth,
                           double upperSeisDepth,
                           FaultTrace faultTrace) {

        this.aveDip = aveDip;
        this.lowerSeismogenicDepth = lowerSeisDepth;
        this.upperSeismogenicDepth = upperSeisDepth;
        this.faultTrace = faultTrace;

    }

    public void setUpperSeismogenicDepth(double upperSeismogenicDepth) { this.upperSeismogenicDepth = upperSeismogenicDepth; }
    public double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public void setLowerSeismogenicDepth(double lowerSeismogenicDepth) { this.lowerSeismogenicDepth = lowerSeismogenicDepth; }
    public double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }

    public void setAveDip(double aveDip) { this.aveDip = aveDip; }
    public double getAveDip() { return aveDip; }

    public void setFaultTrace(FaultTrace faultTrace) {
        this.faultTrace = faultTrace;
    }
    public FaultTrace getFaultTrace() { return faultTrace; }


    private final static String TAB = "  ";
    public String toString(){

        StringBuffer b = new StringBuffer(C);
        b.append('\n');
        b.append(TAB + "Ave. Dip = " + aveDip);
        b.append(TAB + "Upper Seismogenic Depth = " + upperSeismogenicDepth);
        b.append(TAB + "Lower Seismogenic Depth = " + lowerSeismogenicDepth);
        b.append(TAB + "Fault Trace = " + faultTrace.toString() ) ;
        return b.toString();

    }

}
