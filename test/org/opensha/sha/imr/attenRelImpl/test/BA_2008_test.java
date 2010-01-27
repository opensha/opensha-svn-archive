/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.imr.attenRelImpl.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.opensha.commons.param.WarningDoubleParameter;
import org.opensha.commons.param.event.ParameterChangeWarningEvent;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.param.EqkRuptureParams.FaultTypeParam;
import org.opensha.sha.imr.param.EqkRuptureParams.MagParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceJBParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.WarningDoublePropagationEffectParameter;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

public class BA_2008_test extends TestCase implements ParameterChangeWarningListener{

	private BA_2008_AttenRel ba_2008 = null;

	private static final String RESULT_SET_PATH = "src/org/opensha/sha/imr/attenRelImpl/test/AttenRelResultSetFiles/NGA_ModelsTestFiles/BA08/";

	private double[] period={0.010,0.020,0.030,0.050,0.075,0.10,0.15,0.20,0.25,0.30,0.40,0.50,0.75,1.0,1.5,2.0,3.0,4.0,5.0,7.5,10.0};

	private double maxDiscrepancy = 0;
	
	//Tolerance to check if the results fall within the range.
	private static double tolerence = 1; //default value for the tolerence

	private DecimalFormat format = new DecimalFormat("0.####");
	private ArrayList testDataLines;
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(BA_2008_test.class);
	}

	public BA_2008_test(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		//create the instance of the CB_2006
		ba_2008 = new BA_2008_AttenRel(this);
		ba_2008.setParamDefaults();
		//testDataLines = FileUtils.loadFile(CB_2006_RESULTS);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	

	/*
	 * Test method for 'org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel.getMean()'
	 * Also Test method for 'org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel.getStdDev()'
	 */
	public void testMeanAndStdDev() {	
		File f = new File(RESULT_SET_PATH);
		File[] fileList = f.listFiles();
		for(int i=0;i<fileList.length;++i) {

			String fileName = fileList[i].getName();
			
			if(fileName.contains("README") || !fileName.contains(".TXT")) continue; // skip the README file
			
			boolean isMedian = false;
			String testValString = "Std Dev";
			if(fileName.contains("MEDIAN"))  { // test mean
				isMedian = true; 
				testValString = "Mean";
			} else { // test Standard Deviation
				isMedian = false;
				/* set whether we are testing Std dev of geomteric mean or 
				 standard deviation of arbitrary horizontal component */
				if(fileName.contains("SIGTM")) {
					// Std Dev of arbitrary horizontal component
					ba_2008.getParameter(FaultTypeParam.NAME).setValue(BA_2008_AttenRel.FLT_TYPE_STRIKE_SLIP);
					testValString = "Std Dev of geometric mean for known faulting";
				} else {
					//Std dev of geomteric mean 
					ba_2008.getParameter(FaultTypeParam.NAME).setValue(BA_2008_AttenRel.FLT_TYPE_UNKNOWN);
					testValString = "Std dev of geomteric mean for unspecified faulting";
				}
			}
			int index1 = fileName.indexOf(".TXT");
			String fltType = fileName.substring(index1-2, index1);
			fltType.replaceAll("_", "");
			
			if(fileName.contains("SS.TXT") && !fileName.contains("SIGTU"))
				ba_2008.getParameter(FaultTypeParam.NAME).setValue(ba_2008.FLT_TYPE_STRIKE_SLIP);
			else if(fileName.contains("RV.TXT"))
				ba_2008.getParameter(FaultTypeParam.NAME).setValue(ba_2008.FLT_TYPE_REVERSE);
			else if(fileName.contains("NM.TXT"))
				ba_2008.getParameter(FaultTypeParam.NAME).setValue(ba_2008.FLT_TYPE_NORMAL);
			else 
				//throw new RuntimeException("Unknown Fault Type");
				ba_2008.getParameter(FaultTypeParam.NAME).setValue(ba_2008.FLT_TYPE_UNKNOWN);
			
				try {
				testDataLines = FileUtils.loadFile(fileList[i].getAbsolutePath());
				int numLines = testDataLines.size();
				for(int j=1;j<numLines;++j){
					System.out.println("Doing "+j+" of "+numLines);
					String fileLine = (String)testDataLines.get(j);
					StringTokenizer st = new StringTokenizer(fileLine);
					double mag = Double.parseDouble(st.nextToken().trim());
					((WarningDoubleParameter)ba_2008.getParameter(MagParam.NAME)).setValueIgnoreWarning(new Double(mag));
					
					//Rrup not used, skipping
					st.nextToken();
					//((WarningDoublePropagationEffectParameter)ba_2008.getParameter(DistanceRupParameter.NAME)).setValueIgnoreWarning(new Double(rrup));

					double rjb = Double.parseDouble(st.nextToken().trim());
					((WarningDoublePropagationEffectParameter)ba_2008.getParameter(DistanceJBParameter.NAME)).setValueIgnoreWarning(new Double(rjb));
					
					st.nextToken().trim(); // ignore R(x) ( Horizontal distance from top of rupture perpendicular to fault strike)
					
					st.nextToken(); // ignore dip
					//ba_2008.getParameter(ba_2008.DIP_NAME).setValue(new Double(dip));
					
					st.nextToken(); // ignore W, width of rup plane
					
					st.nextToken(); // ignore Ztor, depth of top

					double vs30 = Double.parseDouble(st.nextToken().trim());
					((WarningDoubleParameter)ba_2008.getParameter(Vs30_Param.NAME)).setValueIgnoreWarning(new Double(vs30));

					st.nextToken(); // ignore Zsed, sediment/basin depth
					
					ba_2008.setIntensityMeasure(SA_Param.NAME);
					int num= period.length;
					double openSHA_Val, tested_Val;
					boolean results;
					for(int k=0;k<num;++k){
						ba_2008.getParameter(PeriodParam.NAME).setValue(new Double(period[k]));
						if(isMedian) openSHA_Val = Math.exp(ba_2008.getMean());
						else openSHA_Val = ba_2008.getStdDev();
						tested_Val = Double.parseDouble(st.nextToken().trim());
						results = this.compareResults(openSHA_Val, tested_Val);
						if(results == false){
							String failedResultMetadata = "Results from file "+fileName+"failed for  calculation for " +
							"CB-2008 attenuation with the following parameter settings:"+
							"  SA at period = "+period[k]+"\nMag ="+(float)mag+
							"  vs30 = "+vs30+"  rjb = "+(float)rjb+"\n"+ " FaultType = "+fltType+
							"\n"+
							testValString+" from OpenSHA = "+openSHA_Val+"  should be = "+tested_Val;

							//System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
							//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
							this.assertNull(failedResultMetadata,failedResultMetadata);
						}
					}

					
					ba_2008.setIntensityMeasure(PGA_Param.NAME);
					if(isMedian) openSHA_Val = Math.exp(ba_2008.getMean());
					else openSHA_Val = ba_2008.getStdDev();
					tested_Val = Double.parseDouble(st.nextToken().trim());
					results = this.compareResults(openSHA_Val, tested_Val);
					if(results == false){
						String failedResultMetadata = "Results from file "+fileName+"failed for  calculation for " +
						"CB-2008 attenuation with the following parameter settings:"+
						"  PGA "+"\nMag ="+(float)mag+
						"  vs30 = "+vs30+"  rjb = "+(float)rjb+"\n"+ " FaultType = "+fltType+
						"\n"+
						testValString+" from OpenSHA = "+openSHA_Val+"  should be = "+tested_Val;

						//System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
						//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
						this.assertNull(failedResultMetadata,failedResultMetadata);
					}
					ba_2008.setIntensityMeasure(PGV_Param.NAME);
					if(isMedian) openSHA_Val = Math.exp(ba_2008.getMean());
					else openSHA_Val = ba_2008.getStdDev();
					tested_Val = Double.parseDouble(st.nextToken().trim());
					results = this.compareResults(openSHA_Val, tested_Val);
					if(results == false){
						String failedResultMetadata = "Results from file "+fileName+"failed for calculation for " +
						"CB-2008 attenuation with the following parameter settings:"+
						"  PGV "+"\nMag ="+(float)mag+
						"  vs30 = "+vs30+"  rjb = "+(float)rjb+"\n"+ " FaultType = "+fltType+
						"\n"+
						testValString+" from OpenSHA = "+openSHA_Val+"  should be = "+tested_Val;

						//System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
						//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
						this.assertNull(failedResultMetadata,failedResultMetadata);
					}
					
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Maximum Discrepancy: " + (float)maxDiscrepancy +"%");
	}

	/**
	 * This function compares the values we obtained after running the values for
	 * the IMR and the target Values( our benchmark)
	 * @param valFromSHA = values we got after running the OpenSHA code for the IMR
	 * @param targetVal = values we are comparing with to see if OpenSHA does correct calculation
	 * @return
	 */
	private boolean compareResults(double valFromSHA,
			double targetVal){
		//comparing each value we obtained after doing the IMR calc with the target result
		//and making sure that values lies with the .01% range of the target values.
		//comparing if the values lies within the actual tolerence range of the target result
		double result = 0;
		if(targetVal!=0)
			result =(StrictMath.abs(valFromSHA-targetVal)/targetVal)*100;
		
		if (result > maxDiscrepancy)
			maxDiscrepancy = result;

		//System.out.println("Result: "+ result);
		if(result < this.tolerence)
			return true;
		return false;
	}


	public void parameterChangeWarning(ParameterChangeWarningEvent e){
		return;
	}
}
