package org.scec.sha.fault;

import org.scec.sha.surface.GriddedSurfaceFactoryAPI;
import org.scec.sha.surface.GriddedSurfaceAPI;
import org.scec.sha.fault.FaultTrace;
import java.io.*;
import org.scec.exceptions.*;


/**
 * <b>Title:</b> SimpleGriddedFaultFactory<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Steven W. Rock
 * @version 1.0
 */

public abstract class SimpleGriddedFaultFactory extends GriddedFaultFactory implements Serializable {

    // *********************
    /** @todo  Variables */
    // *********************

    /* Debbuging variables */
    protected final static String C = "SimpleGriddedFaultFactory";
    protected final static boolean D = false;

    protected FaultTrace faultTrace;
    protected double aveDip = Double.NaN;
    protected double upperSeismogenicDepth = Double.NaN;
    protected double lowerSeismogenicDepth = Double.NaN;
    protected double gridSpacing = Double.NaN;


    private static final double zero = 0;
    private static final double ninety = 90;


    // **********************
    /** @todo  Constructors */
    // **********************

    public SimpleGriddedFaultFactory() { super(); }

    public SimpleGriddedFaultFactory(
        FaultTrace faultTrace,
        double aveDip,
        double upperSeismogenicDepth,
        double lowerSeismogenicDepth,
        double gridSpacing
    )
        throws FaultException
    {
        setFaultTrace(faultTrace);
        setAveDip(aveDip);
        setUpperSeismogenicDepth(upperSeismogenicDepth);
        setLowerSeismogenicDepth(lowerSeismogenicDepth);
        setGridSpacing(gridSpacing);
    }



    // ***************************************************************
    /** @todo  Serializing Helpers - overide to increase performance */
    // ***************************************************************

    protected void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }
    protected void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }



    // **********************************
    /** @todo  Data Accessors / Setters */
    // **********************************

    public void setFaultTrace(FaultTrace faultTrace) { this.faultTrace = faultTrace; }
    public FaultTrace getFaultTrace() { return faultTrace; }

    public void setAveDip(double aveDip) throws FaultException {
        if ( aveDip < zero || aveDip > ninety){
            throw new FaultException(C + " : setAveDip(): Input value must be between 0 and 90, inclusive");
        }
        this.aveDip = aveDip;
    }
    public double getAveDip() { return aveDip; }

    public void setUpperSeismogenicDepth(double upperSeismogenicDepth) throws FaultException {
        if ( upperSeismogenicDepth < zero ){
            throw new FaultException(C + " : setUpperSeismogenicDepth(): Input value must be greater than or equal to 0.");
        }
        this.upperSeismogenicDepth = upperSeismogenicDepth;
    }
    public double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public void setLowerSeismogenicDepth(double lowerSeismogenicDepth) throws FaultException {
        if ( lowerSeismogenicDepth < zero  ){
            throw new FaultException(C + " : setLowerSeismogenicDepth(): Input value must be greater than or equal to 0.");
        }
        this.lowerSeismogenicDepth = lowerSeismogenicDepth;
    }
    public double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }

    public void setGridSpacing(double gridSpacing) throws FaultException {
        if ( gridSpacing <= zero  ){
            throw new FaultException(C + " : setGridSpacing(): Input value must be greater than 0.");
        }
        this.gridSpacing = gridSpacing;
    }
    public double getGridSpacing() { return gridSpacing; }



}
