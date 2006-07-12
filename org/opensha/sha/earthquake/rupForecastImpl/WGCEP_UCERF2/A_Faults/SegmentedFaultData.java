/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.calc.FaultMomentCalc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

/**
 * @author field
 *
 */
public class SegmentedFaultData {
	private ArrayList segmentData;
	private boolean aseisReducesArea;
	private double totalArea, totalMoRate;
	private double[] segArea, segLength, segMoRate, segSlipRate; 
	private String[] segName;
	private final static double KM_TO_METERS_CONVERT=1e6;

	
	/**
  	 * Description:
  	 * 
  	 * @param segmentData - an ArrayList containing N ArrayLists (one for each segment), 
  	 * where the arrayList for each segment contains some number of FaultSectionPrefData objects.
  	 * It is assumed that these are in proper order such that concatenating the FaultTraces will produce
  	 * a total FaultTrace with locations in the proper order.
  	 * @param aseisReducesArea - if true apply asiesmicFactor as reduction of area, otherwise as reduction of slip rate
  	 * @
  	 */
	public SegmentedFaultData(ArrayList segmentData, boolean aseisReducesArea) {
		this.segmentData = segmentData;	
		this.aseisReducesArea = aseisReducesArea;
		calcAll();
	}
	
	/**
	 * Get the total area of all segments combined in sq km 
	 * @return
	 */
	public double getTotalArea() {
		return totalArea;
	}
	
	/**
	 * Get the number of segments in this model
	 * @return
	 */
	public int getNumSegments() {
		return segmentData.size();
	}
	
	/**
	 * Get segment area by index
	 * @param index
	 * @return
	 */
	public double getSegmentArea(int index) {
		return segArea[index];
	}
	
	/**
	 * Get segment length by index
	 * @param index
	 * @return
	 */
	public double getSegmentLength(int index) {
		return this.segLength[index];
	}
	
	/**
	 * Get segment slip rate by index
	 * @param index
	 * @return
	 */
	public double getSegmentSlipRate(int index) {
		return segSlipRate[index];
	}
	
	/**
	 * Get segment moment rate by index
	 * @param index
	 * @return
	 */
	public double getSegmentMomentRate(int index) {
		return segMoRate[index];
	}
	
	/**
	 * Get total Moment rate for all segments combined
	 * @return
	 */
	public double getTotalMomentRate() {
		return totalMoRate;
	}
	
	/**
	 * Get segment name by index
	 * @param index
	 * @return
	 */
	public String getSegmentName(int index) {
		return this.segName[index];
	}
	
	/**
	 * Calculate  Stuff
	 * @return
	 */
	private void calcAll() {
		totalArea=0;
		totalMoRate=0;
		segArea = new double[segmentData.size()];
		segLength = new double[segmentData.size()];
		segMoRate = new double[segmentData.size()];
		segSlipRate = new double[segmentData.size()];
		// fill in segName, segArea and segMoRate
		for(int seg=0;seg<segmentData.size();seg++) {
			segArea[seg]=0;
			segLength[seg]=0;
			segMoRate[seg]=0;
			ArrayList segmentDatum = (ArrayList) segmentData.get(seg);
			Iterator it = segmentDatum.iterator();
			segName[seg]="";
			while(it.hasNext()) {
				FaultSectionPrefData sectData = (FaultSectionPrefData) it.next();
				if(it.hasNext()) segName[seg]+=sectData.getSectionName()+" + ";
				else segName[seg]+=sectData.getSectionName();
				//set the area & moRate
				double length = sectData.getLength(); // km
				segLength[seg]+=length;
				double ddw = sectData.getDownDipWidth(); //km
				if(aseisReducesArea) {
					double area = length*ddw*(1-sectData.getAseismicSlipFactor())*KM_TO_METERS_CONVERT; // meters-squared
					segArea[seg] += area;
					segMoRate[seg] += FaultMomentCalc.getMoment(area, 
							sectData.getAveLongTermSlipRate()*1e-3); // SI units
				}
				else {
					double area  = length*ddw*KM_TO_METERS_CONVERT;
					segArea[seg] +=  area;// meters-squared
					segMoRate[seg] += FaultMomentCalc.getMoment(area, 
							sectData.getAveLongTermSlipRate()*1e-3*(1-sectData.getAseismicSlipFactor())); // SI units
				}
				
			}
			segSlipRate[seg] = FaultMomentCalc.getSlip(segArea[seg], segMoRate[seg]);
			totalArea+=segArea[seg];
			totalMoRate+=segMoRate[seg];
		}
		return ;
	}
	
}
