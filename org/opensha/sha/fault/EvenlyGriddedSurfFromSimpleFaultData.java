package org.opensha.sha.fault;

import org.opensha.exceptions.FaultException;
import java.util.ListIterator;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import org.opensha.util.FaultUtils;
import org.opensha.data.Location;
import java.io.IOException;
import org.opensha.sha.surface.EvenlyGriddedSurface;

/**
 * <p>Title:  EvenlyGriddedSurfFromSimpleFaultData </p>
 *
 * <p>Description: This creates and EvenlyGriddedSurface from SimpleFaultData</p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public abstract class EvenlyGriddedSurfFromSimpleFaultData
    extends EvenlyGriddedSurface{

    // *********************
    /** @todo  Variables */
    // *********************

    /* Debbuging variables */
    protected final static String C = "EvenlyGriddedSurfFromSimpleFaultData";
    protected final static boolean D = false;

    protected FaultTrace faultTrace;
    protected double upperSeismogenicDepth = Double.NaN;
    protected double lowerSeismogenicDepth = Double.NaN;



    protected EvenlyGriddedSurfFromSimpleFaultData(SimpleFaultData simpleFaultData,
                                                double gridSpacing) throws
        FaultException {
      this(simpleFaultData.getFaultTrace(),
           simpleFaultData.getAveDip(),
           simpleFaultData.getUpperSeismogenicDepth(),
           simpleFaultData.getLowerSeismogenicDepth(),
           gridSpacing);

    }


    protected EvenlyGriddedSurfFromSimpleFaultData(FaultTrace faultTrace,
                                                double aveDip,
                                                double upperSeismogenicDepth,
                                                double lowerSeismogenicDepth,
                                                double gridSpacing) throws
        FaultException {
      this.faultTrace =faultTrace;
      this.aveDip =aveDip;
      this.upperSeismogenicDepth = upperSeismogenicDepth;
      this.lowerSeismogenicDepth =lowerSeismogenicDepth;
      this.gridSpacing = gridSpacing;
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


    public FaultTrace getFaultTrace() { return faultTrace; }


    public double getUpperSeismogenicDepth() { return upperSeismogenicDepth; }

    public double getLowerSeismogenicDepth() { return lowerSeismogenicDepth; }


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
