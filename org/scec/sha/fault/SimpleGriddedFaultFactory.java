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
    protected Double aveDip;
    protected Double upperSeismogenicDepth;
    protected Double lowerSeismogenicDepth;
    protected Double gridSpacing;


    private static final Double zero = new Double(0);
    private static final Double ninety = new Double(90);


    // **********************
    /** @todo  Constructors */
    // **********************

    public SimpleGriddedFaultFactory() { super(); }

    public SimpleGriddedFaultFactory(
        FaultTrace faultTrace,
        Double aveDip,
        Double upperSeismogenicDepth,
        Double lowerSeismogenicDepth,
        Double gridSpacing
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

    public void setAveDip(Double aveDip) throws FaultException {
        if ( aveDip.compareTo( zero ) < 0 || aveDip.compareTo( ninety ) > 0 ){
            throw new FaultException(C + " : setAveDip(): Input value must be between 0 and 90, inclusive");
        }
        this.aveDip = aveDip;
    }
    public Double getAveDip() { return aveDip; }

    public void setUpperSeismogenicDepth(Double upperSeismogenicDepth) throws FaultException {
        if ( upperSeismogenicDepth.compareTo( zero ) < 0  ){
            throw new FaultException(C + " : setUpperSeismogenicDepth(): Input value must be greater than or equal to 0.");
        }
        this.upperSeismogenicDepth = upperSeismogenicDepth;
    }
    public Double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public void setLowerSeismogenicDepth(Double lowerSeismogenicDepth) throws FaultException {
        if ( lowerSeismogenicDepth.compareTo( zero ) < 0  ){
            throw new FaultException(C + " : setLowerSeismogenicDepth(): Input value must be greater than or equal to 0.");
        }
        this.lowerSeismogenicDepth = lowerSeismogenicDepth;
    }
    public Double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }

    public void setGridSpacing(Double gridSpacing) throws FaultException {
        if ( gridSpacing.compareTo( zero ) <= 0  ){
            throw new FaultException(C + " : setGridSpacing(): Input value must be greater than 0.");
        }
        this.gridSpacing = gridSpacing;
    }
    public Double getGridSpacing() { return gridSpacing; }



}
