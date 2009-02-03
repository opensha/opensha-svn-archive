package org.opensha.sha.imr.attenRelImpl.test;

import java.util.*;

import org.opensha.exceptions.ConstraintException;
import org.opensha.exceptions.ParameterException;
import org.opensha.param.WarningDoubleParameter;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.util.FileUtils;
import org.opensha.sha.param.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.*;
import junit.framework.TestCase;

public class AS_2008_test extends NGATest {

	private AS_2008_AttenRel as_2008 = null;

	private static final String RESULT_SET_PATH = "org/opensha/sha/imr/attenRelImpl/AttenRelResultSet/NGA_ModelsTestFiles/AS08/";
	
	public static final boolean CONTINUE_ON_FAIL = false;

	//	private double[] period={0.010,0.020,0.030,0.050,0.075,0.10,0.15,0.20,0.25,0.30,0.40,0.50,0.75,1.0,1.5,2.0,3.0,4.0,5.0,7.5,10.0};

	private double maxDiscrepancy = 0;

	//Tolerance to check if the results fall within the range.
	private static double tolerence = 1; //default value for the tolerence

	private DecimalFormat format = new DecimalFormat("0.####");
	private ArrayList testDataLines;
	public static void main(String[] args) {
		junit.swingui.TestRunner.run(AS_2008_test.class);
	}

	public AS_2008_test(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		//create the instance of the CB_2006
		as_2008 = new AS_2008_AttenRel(this);
		as_2008.setParamDefaults();
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

			if(fileName.contains("README") || !fileName.contains(".OUT")) continue; // skip the README file

			System.out.println("Testing file " + fileName);
			
			boolean skipAfterShocks = false;
			
			if (skipAfterShocks && fileName.contains("_AS_")) {
				System.out.println("Skipping aftershock file: " + fileName);
				continue;
			}

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
					as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(AS_2008_AttenRel.FLT_TYPE_STRIKE_SLIP);
					testValString = "Std Dev of geometric mean for known faulting";
				} else {
					//Std dev of geomteric mean
					as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(AS_2008_AttenRel.FLT_TYPE_DEFAULT);
					//					as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(AS_2008_AttenRel.FLT_TYPE_UNKNOWN);
					testValString = "Std dev of geomteric mean for unspecified faulting";
				}
			}
			int index1 = fileName.indexOf(".OUT");
			String fltType = fileName.substring(index1-2, index1);
			fltType.replaceAll("_", "");

			if(fileName.contains("SS.OUT") && !fileName.contains("SIGTU"))
				as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(as_2008.FLT_TYPE_STRIKE_SLIP);
			else if(fileName.contains("RV.OUT"))
				as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(as_2008.FLT_TYPE_REVERSE);
			else if(fileName.contains("NM.OUT"))
				as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(as_2008.FLT_TYPE_NORMAL);
			else 
				//throw new RuntimeException("Unknown Fault Type");
				//				as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(as_2008.FLT_TYPE_UNKNOWN);
				as_2008.getParameter(as_2008.FLT_TYPE_NAME).setValue(AS_2008_AttenRel.FLT_TYPE_DEFAULT);
			
			boolean estVs30 = false;
			if (fileName.contains("SIGEST"))
				estVs30 = true;

			try {
				testDataLines = FileUtils.loadFile(fileList[i].getAbsolutePath());
				int numLines = testDataLines.size();
				double period[] = this.loadPeriods((String)testDataLines.get(0));
				for(int j=1;j<numLines;++j){
					StringTokenizer st;
					double mag;
					//((WarningDoublePropagationEffectParameter)as_2008.getParameter(DistanceRupParameter.NAME)).setValueIgnoreWarning(new Double(rrup));
					double dist_jb;
					double vs30;
					try {
						System.out.println("Doing "+j+" of "+numLines);
						String fileLine = (String)testDataLines.get(j);
						st = new StringTokenizer(fileLine);
						mag = Double.parseDouble(st.nextToken().trim());
						((WarningDoubleParameter)as_2008.getParameter(as_2008.MAG_NAME)).setValueIgnoreWarning(new Double(mag));

						//Rrup is used for this one
						double rRup = Double.parseDouble(st.nextToken().trim());
						dist_jb = Double.parseDouble(st.nextToken().trim());
//						System.out.println("rRup: " + rRup + " dist_jb: " + dist_jb);
						as_2008.getParameter(DistanceRupParameter.NAME).setValue(rRup);
//						as_2008.getParameter(DistanceRupParameter.NAME).setValue(dist_jb);
						//					System.out.println("Before rjb get");
						DistRupMinusJB_OverRupParameter param = (DistRupMinusJB_OverRupParameter)as_2008.getParameter(DistRupMinusJB_OverRupParameter.NAME);
						//					System.out.println("rjb param retreived");
						double rupMinusJB = (rRup-dist_jb)/rRup;
						if (rRup != 0 && rupMinusJB != Double.NaN && !(new String(rupMinusJB + "").contains("NaN"))) {
							//						System.out.println("setting to " + rupMinusJB + " (" + rRup + ", " + dist_jb + ")");
							//						param.setValueIgnoreWarning(new Double(rupMinusJB));
							param.setValueIgnoreWarning(new Double(0.1));
//							System.out.println("New rupMinusJB: " + as_2008.getParameter(DistRupMinusJB_OverRupParameter.NAME).getValue());
							//						System.out.println("rjb is set");
						} else {
							//						System.out.println("It's NaN!!!! Setting to default I guess..");
							param.setValueIgnoreWarning(AS_2008_AttenRel.DISTANCE_RUP_MINUS_DEFAULT);
//							System.out.println("New rupMinusJB (default): " + as_2008.getParameter(DistRupMinusJB_OverRupParameter.NAME).getValue());
						}
						double rx = Double.parseDouble(st.nextToken()); // R(x) ( Horizontal distance from top of rupture perpendicular to fault strike)
						as_2008.getParameter(DistanceX_Parameter.NAME).setValue(new Double(rx));

						double dip = Double.parseDouble(st.nextToken()); // dip
						as_2008.getParameter(as_2008.DIP_NAME).setValue(new Double(dip));

						double w = Double.parseDouble(st.nextToken()); // W, width of rup plane
						// not sure what i should do here....
						if (w < AS_2008_AttenRel.RUP_WIDTH_MIN)
							as_2008.getParameter(AS_2008_AttenRel.RUP_WIDTH_NAME).setValue(new Double(AS_2008_AttenRel.RUP_WIDTH_MIN));
						else if (w > AS_2008_AttenRel.RUP_WIDTH_MAX)
							as_2008.getParameter(AS_2008_AttenRel.RUP_WIDTH_NAME).setValue(new Double(AS_2008_AttenRel.RUP_WIDTH_MAX));
						else
							as_2008.getParameter(AS_2008_AttenRel.RUP_WIDTH_NAME).setValue(new Double(w));
//						as_2008.getParameter(AS_2008_AttenRel.RUP_WIDTH_NAME).setValue(new Double(AS_2008_AttenRel.RUP_WIDTH_DEFAULT));
						

						double ztor = Double.parseDouble(st.nextToken()); // Ztor, depth of top
						as_2008.getParameter(AS_2008_AttenRel.RUP_TOP_NAME).setValue(new Double(ztor));

						vs30 = Double.parseDouble(st.nextToken().trim());
						((WarningDoubleParameter)as_2008.getParameter(as_2008.VS30_NAME)).setValueIgnoreWarning(new Double(vs30));
						
						if (estVs30)	// vs30 is estimated
							as_2008.getParameter(AS_2008_AttenRel.VS_FLAG_NAME).setValue(AS_2008_AttenRel.VS_FLAG_E);
						else			// vs30 is measured
							as_2008.getParameter(AS_2008_AttenRel.VS_FLAG_NAME).setValue(AS_2008_AttenRel.VS_FLAG_M);

						double zsed = Double.parseDouble(st.nextToken()); // Zsed, sediment/basin depth
						as_2008.getParameter(AS_2008_AttenRel.DEPTH_1pt0_NAME).setValue(new Double(zsed));


						as_2008.setIntensityMeasure(as_2008.SA_NAME);
						int num= period.length;
						double openSHA_Val, tested_Val;
						boolean results;
						boolean skipTest = false;
						for(int k=0;k<num;++k){
							as_2008.getParameter(as_2008.PERIOD_NAME).setValue(new Double(period[k]));
							if(isMedian) openSHA_Val = Math.exp(as_2008.getMean());
							else openSHA_Val = as_2008.getStdDev();
							tested_Val = Double.parseDouble(st.nextToken().trim());
							results = this.compareResults(openSHA_Val, tested_Val);
							if(results == false){
								String failedResultMetadata = "Results from file "+fileName+" failed for  calculation for " +
								"AS-2008 attenuation with the following parameter settings:\n"+
								"  \tSA at period = "+period[k]+"\n\tMag = "+(float)mag+
								"  rrup = "+(float)rRup+"  rjb = "+(float)dist_jb+"\n\t"+ "FaultType = "+fltType+
								"  rx = "+(float)rx+"  dip = "+(float)dip+"\n\t"+ "w = "+(float)w+
								"  ztor = "+(float)ztor+"  vs30 = "+(float)vs30+"\n\t"+ "zsed = "+(float)zsed+
//								"\n\tSet distRupMinusJB_OverRupParam = " + as_2008.getParameter(DistRupMinusJB_OverRupParameter.NAME).getValue() + 
								"\n"+
								testValString+" from OpenSHA = "+openSHA_Val+"  should be = "+tested_Val;

								System.out.println("Test number= "+i+"("+j+"/"+numLines+")"+" failed for "+failedResultMetadata);
								//							System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
								printOpenSHAParams(as_2008);
								
								if (!CONTINUE_ON_FAIL) {
									this.assertNull(failedResultMetadata,failedResultMetadata);
								}
							}
						}

						as_2008.setIntensityMeasure(as_2008.PGA_NAME);
						if(isMedian) openSHA_Val = Math.exp(as_2008.getMean());
						else openSHA_Val = as_2008.getStdDev();
						tested_Val = Double.parseDouble(st.nextToken().trim());
						results = this.compareResults(openSHA_Val, tested_Val);
						if(results == false){
							String failedResultMetadata = "Results from "+fileName+"failed for " +
							"AS-2008 attenuation with the following parameter settings:"+
							"  PGA "+"\nMag ="+(float)mag+
							"  rrup = "+(float)rRup+"  rjb = "+(float)dist_jb+"\n\t"+ "FaultType = "+fltType+
							"  rx = "+(float)rx+"  dip = "+(float)dip+"\n\t"+ "w = "+(float)w+
							"  ztor = "+(float)ztor+"  vs30 = "+(float)vs30+"\n\t"+ "zsed = "+(float)zsed+
							"\n\tSet distRupMinusJB_OverRupParam = " + as_2008.getParameter(DistRupMinusJB_OverRupParameter.NAME).getValue() + 
							"\n"+
							testValString+" from OpenSHA = "+openSHA_Val+"  should be = "+tested_Val;

							System.out.println("Test number= "+i+"("+j+"/"+numLines+")"+" failed for +"+failedResultMetadata);
							printOpenSHAParams(as_2008);
//							System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
							if (!CONTINUE_ON_FAIL)
								this.assertNull(failedResultMetadata,failedResultMetadata);
						}
						boolean doPGV = true;
						if (doPGV) {
							as_2008.setIntensityMeasure(as_2008.PGV_NAME);
							if(isMedian) openSHA_Val = Math.exp(as_2008.getMean());
							else openSHA_Val = as_2008.getStdDev();
							tested_Val = Double.parseDouble(st.nextToken().trim());
							results = this.compareResults(openSHA_Val, tested_Val);
							if(results == false){
								String failedResultMetadata = "Results from file "+fileName+"failed for calculation for " +
								"AS-2008 attenuation with the following parameter settings:"+
								"  PGV "+"\nMag ="+(float)mag+
								"  rrup = "+(float)rRup+"  rjb = "+(float)dist_jb+"\n\t"+ "FaultType = "+fltType+
								"  rx = "+(float)rx+"  dip = "+(float)dip+"\n\t"+ "w = "+(float)w+
								"  ztor = "+(float)ztor+"  vs30 = "+(float)vs30+"\n\t"+ "zsed = "+(float)zsed+
								"\n\tSet distRupMinusJB_OverRupParam = " + as_2008.getParameter(DistRupMinusJB_OverRupParameter.NAME).getValue() + 
								"\n"+
								testValString+" from OpenSHA = "+openSHA_Val+"  should be = "+tested_Val;

								System.out.println("Test number= "+i+"("+j+"/"+numLines+")"+" failed for +"+failedResultMetadata);
								printOpenSHAParams(as_2008);
								//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
								if (CONTINUE_ON_FAIL)
									continue;
								this.assertNull(failedResultMetadata,failedResultMetadata);
							}
						}
						
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						assertFalse(true);
					} catch (ConstraintException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						assertFalse(true);
					} catch (ParameterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						assertFalse(true);
					}

				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				assertFalse(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Maximum Discrepancy: " + maxDiscrepancy);
	}
	
	private void printOpenSHAParams(AttenuationRelationship attenRel) {
		System.out.println("\nOpenSHA params:");
		if (attenRel.getIntensityMeasure().getName().equals(AttenuationRelationship.SA_NAME))
			System.out.println("SA period = " + attenRel.getParameter(AS_2008_AttenRel.PERIOD_NAME).getValue());
		else
			System.out.println("IM Type = " + attenRel.getIntensityMeasure().getName());
		System.out.print("Mag = " + attenRel.getParameter(AS_2008_AttenRel.MAG_NAME).getValue());
		System.out.print("\tRrup = " + attenRel.getParameter(DistanceRupParameter.NAME).getValue());
		System.out.println("\t(Rrup-Rjb)/Rrup = " + attenRel.getParameter(DistRupMinusJB_OverRupParameter.NAME).getValue());
		System.out.print("Fault Type = " + attenRel.getParameter(AS_2008_AttenRel.FLT_TYPE_NAME).getValue());
		System.out.print("\tDistanceX = " + attenRel.getParameter(DistanceX_Parameter.NAME).getValue());
		System.out.println("\tDip = " + attenRel.getParameter(AS_2008_AttenRel.DIP_NAME).getValue());
		System.out.print("DDWidth = " + attenRel.getParameter(AS_2008_AttenRel.RUP_WIDTH_NAME).getValue());
		System.out.print("\tzTor = " + attenRel.getParameter(AS_2008_AttenRel.RUP_TOP_NAME).getValue());
		System.out.print("\tVs30 = " + attenRel.getParameter(AS_2008_AttenRel.VS30_NAME).getValue());
		System.out.println ("\tVs30 flag = " + attenRel.getParameter(AS_2008_AttenRel.VS_FLAG_NAME).getValue());
		System.out.println("Depthto1km/sec = " + attenRel.getParameter(AS_2008_AttenRel.DEPTH_1pt0_NAME).getValue());
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
}
