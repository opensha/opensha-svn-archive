package org.opensha.sha.fault;

import org.opensha.exceptions.FaultException;

import java.util.ArrayList;
import java.util.ListIterator;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import org.opensha.util.FaultUtils;
import org.opensha.calc.RelativeLocation;
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
    	set(faultTrace, aveDip, upperSeismogenicDepth, lowerSeismogenicDepth, gridSpacing);
    }
    
    private void set(FaultTrace faultTrace,
    		double aveDip,
    		double upperSeismogenicDepth,
    		double lowerSeismogenicDepth,
    		double gridSpacing)	{
    	this.faultTrace =faultTrace;
    	this.aveDip =aveDip;
    	this.upperSeismogenicDepth = upperSeismogenicDepth;
    	this.lowerSeismogenicDepth =lowerSeismogenicDepth;
    	this.gridSpacing = gridSpacing;
    }
    
    
    /**
     * Stitch Together the fault sections. It assumes:
     * 1. Sections are in correct order (in how they are to be stitched together)
     * 2. Distance between adjacent points on neighboring sections (in correct order) 
     * is less than distance to opposite ends of the sections.  In other words no sections
     * overlap by more than half the section length.
     * Each of the following are average over the sections (weight averaged by area): 
     * upper and lower seismogenic depth, slip.  Total area of surface is maintained, 
     * plus an addition area implied by gaps between neighboring sections.
     * 
     * @param simpleFaultData
     * @param gridSpacing
     * @throws FaultException
     */
    protected EvenlyGriddedSurfFromSimpleFaultData(ArrayList<SimpleFaultData> simpleFaultData, double gridSpacing) {
    	if(simpleFaultData.size()==1) {
    		SimpleFaultData faultData = simpleFaultData.get(0);
    		set(faultData.getFaultTrace(),
    				faultData.getAveDip(),
    				faultData.getUpperSeismogenicDepth(),
    				faultData.getLowerSeismogenicDepth(),
    				gridSpacing);
    		return;
    	}
    	// correctly order the first fault section
    	FaultTrace faultTrace1 = simpleFaultData.get(0).getFaultTrace();
    	FaultTrace faultTrace2 =  simpleFaultData.get(1).getFaultTrace();
    	double minDist = Double.MAX_VALUE, distance;
    	boolean reverse = false;
    	distance = RelativeLocation.getHorzDistance(faultTrace1.getLocationAt(0), faultTrace2.getLocationAt(0));
    	if(distance<minDist) {
    		minDist = distance;
    		reverse=true;
    	}
    	distance = RelativeLocation.getHorzDistance(faultTrace1.getLocationAt(0), faultTrace2.getLocationAt(faultTrace2.getNumLocations()-1));
    	if(distance<minDist) {
    		minDist = distance;
    		reverse=true;  
    	}
    	distance = RelativeLocation.getHorzDistance(faultTrace1.getLocationAt(faultTrace1.getNumLocations()-1), faultTrace2.getLocationAt(0));
    	if(distance<minDist) {
    		minDist = distance;
    		reverse=false;
    	}
    	distance = RelativeLocation.getHorzDistance(faultTrace1.getLocationAt(faultTrace1.getNumLocations()-1), faultTrace2.getLocationAt(faultTrace2.getNumLocations()-1));
    	if(distance<minDist) {
    		minDist = distance;
    		reverse=false;
    	}
    	if(reverse) {
    		faultTrace1.reverse();
    		if( simpleFaultData.get(0).getAveDip()!=90)  simpleFaultData.get(0).setAveDip(- simpleFaultData.get(0).getAveDip());
    	}
    	
    	// Calculate Upper Seis Depth, Lower Seis Depth and Dip
    	double combinedDip=0, combinedUpperSeisDepth=0, totArea=0, totLength=0;
    	FaultTrace combinedFaultTrace = new FaultTrace("Combined Fault Sections");
    	int num = simpleFaultData.size();
    	for(int i=0; i<num; ++i) {
    		FaultTrace faultTrace = simpleFaultData.get(i).getFaultTrace();
    		if(i>0) { // check the ordering of point in this fault trace
    			FaultTrace prevFaultTrace = simpleFaultData.get(i-1).getFaultTrace();
    			Location lastLoc = prevFaultTrace.getLocationAt(prevFaultTrace.getNumLocations()-1);
    			double distance1 = RelativeLocation.getHorzDistance(lastLoc, faultTrace.getLocationAt(0));
    			double distance2 = RelativeLocation.getHorzDistance(lastLoc, faultTrace.getLocationAt(faultTrace.getNumLocations()-1));
    			if(distance2<distance1) { // reverse this fault trace
    				faultTrace.reverse();
    				if(simpleFaultData.get(i).getAveDip()!=90) simpleFaultData.get(i).setAveDip(-simpleFaultData.get(i).getAveDip());
    			}
    		}
    		double length = faultTrace.getTraceLength();
    		double dip = simpleFaultData.get(i).getAveDip();
    		double area = Math.abs(length*(simpleFaultData.get(i).getLowerSeismogenicDepth()-simpleFaultData.get(i).getUpperSeismogenicDepth())/Math.sin(dip*Math.PI/ 180));
    		totLength+=length;
    		totArea+=area;
    		combinedUpperSeisDepth+=(area*simpleFaultData.get(i).getUpperSeismogenicDepth());
    		combinedDip+=(area*dip);
    		
    		int numLocations = faultTrace.getNumLocations();
    		//System.out.println(i+":"+dip+","+area+","+combinedDip);
    		// add the fault Trace locations to combined trace
    		for(int locIndex=0; locIndex<numLocations; ++locIndex) combinedFaultTrace.addLocation(faultTrace.getLocationAt(locIndex));
    			
    	}
//    	 if Dip<0, reverse the trace points to follow Aki and Richards convention
    	if(combinedDip<0) {
    		combinedDip=-combinedDip;
    		combinedFaultTrace.reverse();
    	}
    	
    	upperSeismogenicDepth = combinedUpperSeisDepth/totArea;
    	this.aveDip = combinedDip/totArea;
    	
    	this.lowerSeismogenicDepth  = (totArea/totLength)*Math.sin(aveDip*Math.PI/180)+upperSeismogenicDepth;
    	this.faultTrace = combinedFaultTrace;
    	this.gridSpacing = gridSpacing;
    	//System.out.println(faultTrace.toString());
    	//System.out.println(simpleFaultData[0].getFaultTrace().getName()+","+ aveDip);
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
