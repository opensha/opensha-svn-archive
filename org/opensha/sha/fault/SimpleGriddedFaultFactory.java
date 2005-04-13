package org.opensha.sha.fault;

import java.util.ListIterator;
import java.io.*;

import org.opensha.sha.surface.GriddedSurfaceFactoryAPI;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.data.Location;
import org.opensha.exceptions.*;
import org.opensha.util.FaultUtils;


// Fix - Needs more comments

/**
 * <b>Title:</b> SimpleGriddedFaultFactory.  This is the abstract class of any
 * object that creates a gridded surface (actually an EvenlyGriddedSurface)
 * using SimpleFaultData (plus a grid spacing)<p>
 * <b>Description:</b> <p>
 *
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


    public SimpleGriddedFaultFactory( SimpleFaultData simpleFaultData,
                                        double gridSpacing)
                                        throws FaultException {

        this(simpleFaultData.getFaultTrace(),
              simpleFaultData.getAveDip(),
              simpleFaultData.getUpperSeismogenicDepth(),
              simpleFaultData.getLowerSeismogenicDepth(),
              gridSpacing);
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


    public void setAll(FaultTrace faultTrace,double aveDip,double upperSeismogenicDepth,
                       double lowerSeismogenicDepth, double gridSpacing ) {

      setFaultTrace(faultTrace);
      setAveDip(aveDip);
      setUpperSeismogenicDepth(upperSeismogenicDepth);
      setLowerSeismogenicDepth(lowerSeismogenicDepth);
      setGridSpacing(gridSpacing);
    }

    public void setFaultTrace(FaultTrace faultTrace) { this.faultTrace = faultTrace; }
    public FaultTrace getFaultTrace() { return faultTrace; }

    public void setAveDip(double aveDip) throws FaultException {
        FaultUtils.assertValidDip(aveDip);
        this.aveDip = aveDip;
    }
    public double getAveDip() { return aveDip; }

    public void setUpperSeismogenicDepth(double upperSeismogenicDepth) throws FaultException {
        FaultUtils.assertValidDepth(upperSeismogenicDepth);
        this.upperSeismogenicDepth = upperSeismogenicDepth;
    }
    public double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public void setLowerSeismogenicDepth(double lowerSeismogenicDepth) throws FaultException {
        FaultUtils.assertValidDepth(lowerSeismogenicDepth);
        this.lowerSeismogenicDepth = lowerSeismogenicDepth;
    }
    public double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }

    public void setGridSpacing(double gridSpacing) throws FaultException {
        if ( gridSpacing <= 0.0  ){
            throw new FaultException(C + " : setGridSpacing(): Input value must be greater than 0.");
        }
        this.gridSpacing = gridSpacing;
    }
    public double getGridSpacing() { return gridSpacing; }

    /**
     * This method checks the simple-fault data to make sure it's all OK.
     * @throws FaultException
     */
    protected void assertValidData() throws FaultException {

        if( faultTrace == null ) throw new FaultException(C + "Fault Trace is null");

        FaultUtils.assertValidDip(aveDip);
        FaultUtils.assertValidSeisUpperAndLower(upperSeismogenicDepth, lowerSeismogenicDepth);

        if( gridSpacing == Double.NaN ) throw new FaultException(C + "invalid gridSpacing");

        double depth = faultTrace.getLocationAt(0).getDepth();
        if(depth > upperSeismogenicDepth)
                throw new FaultException(C + "depth on faultTrace locations must be < upperSeisDepth");

        ListIterator it=faultTrace.listIterator();
        while(it.hasNext()) {
          if(((Location)it.next()).getDepth() !=depth){
            throw new FaultException(C + "All depth on faultTrace locations must be equal");
          }
        }
    }


}
