package org.opensha.sha.imr.attenRelImpl.test;

import java.util.*;

import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel;
import org.opensha.util.FileUtils;
import org.opensha.sha.param.*;

import java.text.*;
import junit.framework.TestCase;

public class CB_2006_test extends TestCase implements ParameterChangeWarningListener{
	
	
	  private CB_2006_AttenRel cb_2006 = null;
	
	  private static final String RESULT_SET_PATH = "org/opensha/sha/imr/attenRelImpl/AttenRelResultSet/";
	  private static final String CB_2006_RESULTS = RESULT_SET_PATH +"CB2006_NGA.txt";
	
	  //Tolerence to check if the results fall within the range.
	  private static double tolerence = .01; //default value for the tolerence

	 private DecimalFormat format = new DecimalFormat("0.####");
	private ArrayList testDataLines;
	public static void main(String[] args) {
	  junit.swingui.TestRunner.run(CB_2006_test.class);
	}

	public CB_2006_test(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		//create the instance of the CB_2006
		cb_2006 = new CB_2006_AttenRel(this);
		cb_2006.setParamDefaults();
		testDataLines = FileUtils.loadFile(CB_2006_RESULTS);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel.getMean()'
	 * Also Test method for 'org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel.getStdDev()'
	 */
	public void testMeanAndStdDev() {
		int numDataLines = testDataLines.size();
		for(int i=1;i<numDataLines;++i){
			
			StringTokenizer st = new StringTokenizer((String)testDataLines.get(i));
			double period = Double.parseDouble(st.nextToken().trim());
			if(period == -1)
				cb_2006.setIntensityMeasure(cb_2006.PGV_NAME);
			else{
				cb_2006.setIntensityMeasure(cb_2006.SA_NAME);
				cb_2006.getParameter(cb_2006.PERIOD_NAME).setValue(new Double(period));
			}
			double mag = Double.parseDouble(st.nextToken().trim());
			cb_2006.getParameter(cb_2006.MAG_NAME).setValue(new Double(mag));
			String fltType = st.nextToken().trim();
			if(fltType.equals("SS"))
			  cb_2006.getParameter(cb_2006.FLT_TYPE_NAME).setValue(cb_2006.FLT_TYPE_STRIKE_SLIP);
			else if(fltType.equals("RV"))
				  cb_2006.getParameter(cb_2006.FLT_TYPE_NAME).setValue(cb_2006.FLT_TYPE_REVERSE);
			else
				cb_2006.getParameter(cb_2006.FLT_TYPE_NAME).setValue(cb_2006.FLT_TYPE_NORMAL);
			
			double frv= Double.parseDouble(st.nextToken().trim());
			double fnm= Double.parseDouble(st.nextToken().trim());
			double depthTop = Double.parseDouble(st.nextToken().trim());
			cb_2006.getParameter(cb_2006.RUP_TOP_NAME).setValue(new Double(depthTop));
			double dip = Double.parseDouble(st.nextToken().trim());
			cb_2006.getParameter(cb_2006.DIP_NAME).setValue(new Double(dip));
			double vs30 = Double.parseDouble(st.nextToken().trim());
			cb_2006.getParameter(cb_2006.VS30_NAME).setValue(new Double(vs30));
			double depth25 = Double.parseDouble(st.nextToken().trim());
			cb_2006.getParameter(cb_2006.DEPTH_2pt5_NAME).setValue(new Double(depth25));
			
			
			double rrup = Double.parseDouble(st.nextToken().trim());
			cb_2006.getParameter(DistanceRupParameter.NAME).setValue(new Double(rrup));
			
			double rjb = Double.parseDouble(st.nextToken().trim());
			double distRupMinusJB_OverRup = (rrup-rjb)/rrup;
			cb_2006.getParameter(DistRupMinusJB_OverRupParameter.NAME).setValue(new Double(distRupMinusJB_OverRup));
			double meanVal = cb_2006.getMean();
			double targetMedian = Double.parseDouble(st.nextToken().trim());
			double medianFromOpenSHA = Double.parseDouble(format.format(Math.exp(meanVal)));	
			targetMedian = Double.parseDouble(format.format(targetMedian));
			//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
			boolean results = compareResults(medianFromOpenSHA,targetMedian);
			//if the test was failure the add it to the test cases Vecotr that stores the values for  that failed
        	 	/**
              * If any test for the CY-2006 failed
              */
			
            if(results == false){
            	 String failedResultMetadata = "Results failed for Median calculation for" +
            	 		  "CB-2006 attenuation with the following parameter settings:"+
            	          "  Period ="+period+"\nMag ="+(float)mag+" rRup = "+(float)rrup+
            	          "  vs30 = "+vs30+"  rjb = "+(float)rjb+"\n"+ " depth2pt5 ="+depth25+" FaultType = "+fltType+
            	          "   Frv = "+frv+
            	          "   Fnm = "+fnm+
            	          "   depthTop = "+depthTop+"   dip = "+dip+"\n"+
            	          "Median from OpenSHA = "+medianFromOpenSHA+"  should be = "+targetMedian;
            	 //System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
            	 //System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
              this.assertNull(failedResultMetadata,failedResultMetadata);
            }
            st.nextToken();
            st.nextToken();
            st.nextToken();
            cb_2006.getParameter(cb_2006.STD_DEV_TYPE_NAME).setValue(cb_2006.STD_DEV_TYPE_TOTAL);
            cb_2006.getParameter(cb_2006.COMPONENT_NAME).setValue(cb_2006.COMPONENT_RANDOM_HORZ);
            double stdVal = cb_2006.getStdDev();
			double targetStdDev = Double.parseDouble(st.nextToken().trim());
			double stdFromOpenSHA = Double.parseDouble(format.format(stdVal));	
			//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
			results = compareResults(stdFromOpenSHA,targetStdDev);
			//if the test was failure the add it to the test cases Vecotr that stores the values for  that failed
        	 	/**
              * If any test for the CY-2006 failed
              */
			
            /*if(results == false){
            	String failedResultMetadata = "Results failed for Std Dev calculation for " +
            			                        "CB-2006 attenuation with the following parameter settings:"+
            									"  Period ="+period+"\nMag ="+(float)mag+" rRup = "+(float)rrup+
            									"  vs30 = "+vs30+"  rjb = "+(float)rjb+"\n"+ " depth2pt5 ="+depth25+" FaultType = "+fltType+
            									"   Frv = "+frv+
            									"   Fnm = "+fnm+
            									"   depthTop = "+depthTop+"\n   dip = "+dip+"\n"+
            									"Std Dev from OpenSHA = "+stdFromOpenSHA+"  should be = "+targetStdDev;;
            	
            	 //System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
            	 //System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
              this.assertNull(failedResultMetadata,failedResultMetadata);
            }

            //if the all the succeeds and their is no fail for any test
            else {
              this.assertTrue("CY-2006 Test succeeded for all the test cases",results);
            }*/
		}
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

	    //System.out.println("Result: "+ result);
	    if(result < this.tolerence)
	      return true;
	    return false;
	  }
	
	
	  public void parameterChangeWarning(ParameterChangeWarningEvent e){
		    return;
	  }
}
