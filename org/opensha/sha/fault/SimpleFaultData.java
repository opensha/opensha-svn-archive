package org.opensha.sha.fault;

import java.util.*;
import org.opensha.util.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.data.*;


// Fix - Needs more comments


/**
 *  <b>Title:</b> SimpleFaultData<p>
 *  <b>Description:</b> This object contains all the fault-related information needed
 *  by a SimpleGriddedFaultFactory.  This does not check whether the values make sense
 *  (e.g., it doesn not check that 0²aveDip²90) because these will get checked in the
 *  classes that use this data (and we don't want duplicate these checks). <p>
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
    
    /**
     * Get a single combined simpleFaultData from multiple SimpleFaultData
     * @param simpleFaultDataList
     * @return
     */
    public static SimpleFaultData getCombinedSimpleFaultData(ArrayList<SimpleFaultData> simpleFaultDataList) {
    	if(simpleFaultDataList.size()==1) {
    		return simpleFaultDataList.get(0);
    	}
    	// correctly order the first fault section
    	FaultTrace faultTrace1 = simpleFaultDataList.get(0).getFaultTrace();
    	FaultTrace faultTrace2 =  simpleFaultDataList.get(1).getFaultTrace();
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
    		if( simpleFaultDataList.get(0).getAveDip()!=90)  simpleFaultDataList.get(0).setAveDip(- simpleFaultDataList.get(0).getAveDip());
    	}
    	
    	// Calculate Upper Seis Depth, Lower Seis Depth and Dip
    	double combinedDip=0, combinedUpperSeisDepth=0, totArea=0, totLength=0;
    	FaultTrace combinedFaultTrace = new FaultTrace("Combined Fault Sections");
    	int num = simpleFaultDataList.size();
    	for(int i=0; i<num; ++i) {
    		FaultTrace faultTrace = simpleFaultDataList.get(i).getFaultTrace();
    		if(i>0) { // check the ordering of point in this fault trace
    			FaultTrace prevFaultTrace = simpleFaultDataList.get(i-1).getFaultTrace();
    			Location lastLoc = prevFaultTrace.getLocationAt(prevFaultTrace.getNumLocations()-1);
    			double distance1 = RelativeLocation.getHorzDistance(lastLoc, faultTrace.getLocationAt(0));
    			double distance2 = RelativeLocation.getHorzDistance(lastLoc, faultTrace.getLocationAt(faultTrace.getNumLocations()-1));
    			if(distance2<distance1) { // reverse this fault trace
    				faultTrace.reverse();
    				if(simpleFaultDataList.get(i).getAveDip()!=90) simpleFaultDataList.get(i).setAveDip(-simpleFaultDataList.get(i).getAveDip());
    			}
    		}
    		double length = faultTrace.getTraceLength();
    		double dip = simpleFaultDataList.get(i).getAveDip();
    		double area = Math.abs(length*(simpleFaultDataList.get(i).getLowerSeismogenicDepth()-simpleFaultDataList.get(i).getUpperSeismogenicDepth())/Math.sin(dip*Math.PI/ 180));
    		totLength+=length;
    		totArea+=area;
    		combinedUpperSeisDepth+=(area*simpleFaultDataList.get(i).getUpperSeismogenicDepth());
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
    	
    	SimpleFaultData simpleFaultData = new SimpleFaultData();
    	double upperSeismogenicDepth = combinedUpperSeisDepth/totArea;
    	simpleFaultData.setUpperSeismogenicDepth(upperSeismogenicDepth);
    	for(int locIndex=0; locIndex<combinedFaultTrace.getNumLocations(); ++locIndex)
    		combinedFaultTrace.getLocationAt(locIndex).setDepth(simpleFaultData.getUpperSeismogenicDepth());
    	double dip = combinedDip/totArea;
    	double tolerance = 1e-6;
		if(dip-90 < tolerance) dip=90;
    	simpleFaultData.setAveDip(dip);
    	simpleFaultData.setLowerSeismogenicDepth((totArea/totLength)*Math.sin(dip*Math.PI/180)+upperSeismogenicDepth);
    	simpleFaultData.setFaultTrace(combinedFaultTrace);
    	return simpleFaultData;
 
    }
    

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
