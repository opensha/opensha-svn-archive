/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

/**
 * @author Vipin Gupta and Ned Field
 *
 */
public class FaultSegmentData {
	private ArrayList sectionToSegmentData;
	private boolean aseisReducesArea;
	private double totalArea, totalMoRate, totalMoRateIgnoringAseis, totalLength;
	private double[] segArea, segOrigArea, segLength, segMoRate, segMoRateIgnoringAseis, segSlipRate; 
	private String[] segName, sectionsInSegString;
	private String faultName;
	private double[] recurInterval;

	
	/**
  	 * Description: This class contains data for one or more contiguous fault segments, 
  	 * where each segment is composed of one or more contiguous fault sections 
  	 * (or FaultSectionPrefData objects).  This class also provides various derived data.
  	 * Note that areas (or slip rates if aseisReducesArea is false)  will be reduced by any 
  	 * non-zero fault-section aseimicity factors; the same is true for any values derived 
  	 * from these.  Segment slip rates represent a weight-average over the sections, where
  	 * the weights are section areas (and aseismicity factors are applied as specified).
  	 * All data provided in the get methods are in SI units, which generally differs from the
  	 * units in the input.
  	 * 
  	 * @param sectionToSegmentData - an ArrayList containing N ArrayLists (one for each segment), 
  	 * where the arrayList for each segment contains some number of FaultSectionPrefData objects.
  	 * It is assumed that these are in proper order such that concatenating the FaultTraces will produce
  	 * a total FaultTrace with locations in the proper order.
  	 * @param aseisReducesArea - if true apply asiesmicFactor as reduction of area, otherwise it reduces slip rate
  	 * @
  	 */
	public FaultSegmentData(ArrayList sectionToSegmentData, String[] segNames, boolean aseisReducesArea, String faultName, double[] recurInterval) {
		if(recurInterval!=null && (recurInterval.length!=sectionToSegmentData.size()))
				throw new RuntimeException ("Number of recurrence intervals should  equal  number of segments");
		this.recurInterval = recurInterval;
		this.faultName = faultName;
		this.sectionToSegmentData = sectionToSegmentData;	
		this.aseisReducesArea = aseisReducesArea;
		calcAll();
		if(segNames==null) this.segName = this.sectionsInSegString;
		else this.segName = segNames;
	}
	
	/**
	 * This returns the name of the fault.
	 * @return
	 */
	public String getFaultName() {
		return faultName;
	}
	
	/**
	 * Get the total area of all segments combined (note that this 
	 * is reduced by any non-zero aseismicity factors if aseisReducesArea is true)
	 * @return area in SI units (meters squared)
	 */
	public double getTotalArea() {
		return totalArea;
	}
	
	/**
	 * Get the number of segments in this model
	 * @return
	 */
	public int getNumSegments() {
		return sectionToSegmentData.size();
	}
	
	/**
	 * Get segment area by index - SI units (note that this is reduce by any 
	 * non-zero aseismicity factors if aseisReducesArea is true)
	 * @param index
	 * @return area in SI units (meters squared) 
	 */
	public double getSegmentArea(int index) {
		return segArea[index];
	}
	
	/**
	 * Get original segment area by index - SI units (note that this is NOT reduce by any 
	 * aseismicity factors)
	 * @param index
	 * @return area in SI units (meters squared) 
	 */
	public double getOrigSegmentArea(int index) {
		return segOrigArea[index];
	}
	
	
	
	/**
	 * Get original segment down-dip-width - SI units (note that this is NOT reduce by any 
	 * aseismicity factors)
	 * @param index
	 * @return down dip width in SI units (meters) 
	 */
	public double getOrigSegmentDownDipWidth(int index) {
		return segOrigArea[index]/segLength[index];
	}
	
	
	/**
	 * Get segment length by index.  Note that this is not reduced if aseisReducesArea.
	 * @param index
	 * @return length in SI units (meters)
	 */
	public double getSegmentLength(int index) {
		return this.segLength[index];
	}
	
	/**
	 * Get total length of all segments combined.    Note that this is not reduced if aseisReducesArea.
	 * 
	 * @return length in SI units (meters)
	 */
	public double getTotalLength() {
		return this.totalLength;
	}
	
	/**
	 * Get segment slip rate by index (note that this is reduce by any non-zero 
	 * aseismicity factors if aseisReducesArea is false)
	 * @param index
	 * @return slip rate in SI units (m/sec)
	 */
	public double getSegmentSlipRate(int index) {
		return segSlipRate[index];
	}
	
	/**
	 * Get total ave slip rate - wt averaged by area (note that this is reduce by  
	 * any non-zero aseismicity factors if aseisReducesArea is false)
	 * @param index
	 * @return slip rate in SI units (m/sec)
	 */
	public double getTotalAveSlipRate() {
		return totalMoRate/(totalArea*FaultMomentCalc.SHEAR_MODULUS);
	}
	
	/**
	 * Get segment recur interval.  This simply gives back values given in the constructor.
	 * @param index
	 * @return recur int in years
	 */
	public double getRecurInterval(int index) {
		return recurInterval[index];
	}
	
	/**
	 * Get segment moment rate by index (note that this 
	 * is reduce by any non-zero aseismicity factors)
	 * @param index
	 * @return moment rate in SI units
	 */
	public double getSegmentMomentRate(int index) {
		return segMoRate[index];
	}
	
	/**
	 * Get total Moment rate for all segments combined (note that this 
	 * is reduce by any non-zero aseismicity factors)
	 * @return total moment rate in SI units
	 */
	public double getTotalMomentRate() {
		return totalMoRate;
	}
	
	/**
	 * Get the average aseismicity factor (computed as final total moment rate
	 * divided by original total moment rate)
	 * @return total moment rate in SI units
	 */
	public double getTotalAveAseismicityFactor() {
		double aseisFactor =  1- totalMoRate/totalMoRateIgnoringAseis;
		if(aseisFactor<0) aseisFactor=0;
		return aseisFactor;
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
	 * Get segment name as a concatenated String of section names by index
	 * @param index
	 * @return
	 */
	public String getSectionsInSeg(int index) {
		return this.sectionsInSegString[index];
	}
	
	/**
	 * Get an array of all segment names
	 * @return
	 */
	public String[] getSegmentNames() {
		return this.segName;
	}
	
	
	
	/**
	 * Get a list of FaultSectionPrefData for selected fault model 
	 * (Not sure if this is the best place for this because the info 
	 * can be accessed from a returned FaultSegmentData object)
	 * @param faultModel
	 * @param deformationModelId
	 * @return
	 */
	public ArrayList getPrefFaultSectionDataList() {
		ArrayList faultSectionList = new ArrayList();
		for(int i=0; i<sectionToSegmentData.size(); ++i) {
			ArrayList prefDataList = (ArrayList)sectionToSegmentData.get(i);
			faultSectionList.addAll(prefDataList);
		}
		return faultSectionList;
	}
	
	
	/**
	 * Calculate  Stuff
	 * @return
	 */
	private void calcAll() {
		totalArea=0;
		totalMoRate=0;
		totalMoRateIgnoringAseis=0;
		segArea = new double[sectionToSegmentData.size()];
		segOrigArea=new double[sectionToSegmentData.size()];
		segLength = new double[sectionToSegmentData.size()];
		segMoRate = new double[sectionToSegmentData.size()];
		segMoRateIgnoringAseis = new double[sectionToSegmentData.size()];
		segSlipRate = new double[sectionToSegmentData.size()];
		sectionsInSegString = new String[sectionToSegmentData.size()];
		
		// fill in segName, segArea and segMoRate
		for(int seg=0;seg<sectionToSegmentData.size();seg++) {
			segArea[seg]=0;
			segOrigArea[seg]=0;
			segLength[seg]=0;
			segMoRate[seg]=0;
			segMoRateIgnoringAseis[seg]=0;
			ArrayList segmentDatum = (ArrayList) sectionToSegmentData.get(seg);
			Iterator it = segmentDatum.iterator();
			sectionsInSegString[seg]="";
			while(it.hasNext()) {
				FaultSectionPrefData sectData = (FaultSectionPrefData) it.next();
				if(it.hasNext()) sectionsInSegString[seg]+=sectData.getSectionName()+" + ";
				else sectionsInSegString[seg]+=sectData.getSectionName();
				//set the area & moRate
				segLength[seg]+= sectData.getLength()*1e3;  // converted to meters
				double ddw = sectData.getDownDipWidth()*1e3; // converted to meters
				double area = ddw*sectData.getLength()*1e3; // converted to meters-squared
				double slipRate = sectData.getAveLongTermSlipRate()*1e-3;  // converted to m/sec
				double alpha = 1.0 - sectData.getAseismicSlipFactor();  // reduction factor
				segMoRateIgnoringAseis[seg] += FaultMomentCalc.getMoment(area,slipRate); // SI units
				segOrigArea[seg] +=  area;
				if(aseisReducesArea) {
					segArea[seg] += area*alpha;
					segMoRate[seg] += FaultMomentCalc.getMoment(area*alpha,slipRate); // SI units
				}
				else {
					segArea[seg] +=  area;// meters-squared
					segMoRate[seg] += FaultMomentCalc.getMoment(area,slipRate*alpha); // SI units
				}
				
			}
			// segment slip rate is an average weighted by the section areas
			segSlipRate[seg] = FaultMomentCalc.getSlip(segArea[seg], segMoRate[seg]);
			totalArea+=segArea[seg];
			totalMoRate+=segMoRate[seg];
			totalMoRateIgnoringAseis+=segMoRateIgnoringAseis[seg];
			totalLength+=segLength[seg];
		}
		return ;
	}
	
}
