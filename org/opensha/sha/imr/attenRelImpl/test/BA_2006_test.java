package org.opensha.sha.imr.attenRelImpl.test;

import java.util.*;

import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.imr.attenRelImpl.BA_2006_AttenRel;
import org.opensha.util.FileUtils;
import org.opensha.sha.param.*;

import java.text.*;

import junit.framework.TestCase;

public class BA_2006_test extends TestCase implements ParameterChangeWarningListener{
	
	
	  private BA_2006_AttenRel ba_2006 = null;
	
	  private static final String RESULT_SET_PATH = "org/opensha/sha/imr/attenRelImpl/AttenRelResultSet/";
	  private static final String BA_2006_RESULTS = RESULT_SET_PATH +"BA2006_NGA.txt";
	
	  //Tolerence to check if the results fall within the range.
	  private static double tolerence = .01; //default value for the tolerence

	  private DecimalFormat format = new DecimalFormat("0.####");
	private ArrayList testDataLines;
	public static void main(String[] args) {
	  junit.swingui.TestRunner.run(BA_2006_test.class);
	}

	public BA_2006_test(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		//create the instance of the CY_2006
		ba_2006 = new BA_2006_AttenRel(this);
		ba_2006.setParamDefaults();
		testDataLines = FileUtils.loadFile(BA_2006_RESULTS);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.opensha.sha.imr.attenRelImpl.CY_2006_AttenRel.getMean(int, double, double, double, double, double, double)'
	 */
	public void testGetMean() {
		int numDataLines = testDataLines.size();
		for(int i=1;i<numDataLines;++i){
			
			StringTokenizer st = new StringTokenizer((String)testDataLines.get(i));
			
			double period = Double.parseDouble(st.nextToken().trim());
			if(period == -1)
				ba_2006.setIntensityMeasure(ba_2006.PGV_NAME);
			else{
			  ba_2006.setIntensityMeasure(ba_2006.SA_NAME);
			  ba_2006.getParameter(ba_2006.PERIOD_NAME).setValue(new Double(period));
			}
			double mag = Double.parseDouble(st.nextToken().trim());
			ba_2006.getParameter(ba_2006.MAG_NAME).setValue(new Double(mag));
			
			double rjb = Double.parseDouble(st.nextToken().trim());
			ba_2006.getParameter(DistanceJBParameter.NAME).setValue(new Double(rjb));
			
			double vs30 = Double.parseDouble(st.nextToken().trim());
			ba_2006.getParameter(ba_2006.VS30_NAME).setValue(new Double(vs30));
			
			int imech = Integer.parseInt(st.nextToken().trim());
			String faultType;
			if(imech == 0)
				faultType = ba_2006.FLT_TYPE_STRIKE_SLIP;
			else if(imech == 2)
				faultType = ba_2006.FLT_TYPE_REVERSE;
			else if(imech == 1)
				faultType = ba_2006.FLT_TYPE_NORMAL;
			else
				faultType = ba_2006.FLT_TYPE_UNKNOWN;
			
			ba_2006.getParameter(ba_2006.FLT_TYPE_NAME).setValue(faultType);
			double meanVal = ba_2006.getMean();
			
			double targetMedian = Double.parseDouble(st.nextToken().trim());
			targetMedian = Double.parseDouble(format.format(targetMedian));
			double medianFromOpenSHA = Double.parseDouble(format.format(Math.exp(meanVal)));	
			//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
			boolean results = compareResults(medianFromOpenSHA,targetMedian);
			//if the test was failure the add it to the test cases Vecotr that stores the values for  that failed
        	 	/**
              * If any test for the BA-2006 failed
              */
			
            if(results == false){
            	 String failedResultMetadata = "Results failed for Median calculation for" +
            	 		" BA-2006 attenuation with the following parameter settings:"+
            	          "IMT ="+ba_2006.SA_NAME+" with Period ="+period+"\nMag ="+(float)mag+
            	          "  vs30 = "+vs30+"  rjb = "+(float)rjb+"   FaultType = "+faultType+"\n"+
            	          " Median is "+medianFromOpenSHA+"  where as it should be "+targetMedian;
            	          
            	 //System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
            	 //System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
              this.assertNull(failedResultMetadata,failedResultMetadata);
            }
           /* st.nextToken();
            double stdVal = Math.exp(ba_2006.getStdDev());
            double meanSig = (medianFromOpenSHA*981)/stdVal;
            meanSig = Double.parseDouble(format.format(meanSig));
            double targetMeanSig = Double.parseDouble(st.nextToken().trim());
            targetMeanSig = Double.parseDouble(format.format(targetMeanSig));
//          System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
			results = compareResults(meanSig,targetMeanSig);
			if(results == false){
           	 String failedResultMetadata = "Results failed for Mean/Sig calculation for" +
           	 		"BA-2006 attenuation with the following parameter settings:"+
           	          "IMT ="+ba_2006.SA_NAME+" with Period ="+period+"\nMag ="+(float)mag+
           	          "  vs30 = "+vs30+"  rjb = "+(float)rjb+"   FaultType = "+faultType+"\n"+
           	          " Median/Sig is "+meanSig+"  where as it should be "+targetMeanSig;
           	          
           	 //System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
           	 //System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
             this.assertNull(failedResultMetadata,failedResultMetadata);
           }*/
            //if the all the succeeds and their is no fail for any test
            else {
              this.assertTrue("CY-2006 Test succeeded for all the test cases",results);
            }
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
