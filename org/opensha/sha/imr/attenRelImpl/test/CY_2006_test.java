package org.opensha.sha.imr.attenRelImpl.test;

import java.util.*;

import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.imr.attenRelImpl.CY_2006_AttenRel;
import org.opensha.util.FileUtils;
import org.opensha.sha.param.*;

import java.text.*;
import junit.framework.TestCase;

public class CY_2006_test extends TestCase implements ParameterChangeWarningListener{
	
	
	  private CY_2006_AttenRel cy_2006 = null;
	
	  private static final String RESULT_SET_PATH = "org/opensha/sha/imr/attenRelImpl/AttenRelResultSet/";
	  private static final String CY_2006_RESULTS = RESULT_SET_PATH +"CY2006_NGA.txt";
	
	  //Tolerence to check if the results fall within the range.
	  private static double tolerence = .01; //default value for the tolerence

	 private DecimalFormat format = new DecimalFormat("0.######");
	private ArrayList testDataLines;
	public static void main(String[] args) {
	  junit.swingui.TestRunner.run(CY_2006_test.class);
	}

	public CY_2006_test(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		//create the instance of the CY_2006
		cy_2006 = new CY_2006_AttenRel(this);
		cy_2006.setParamDefaults();
		testDataLines = FileUtils.loadFile(CY_2006_RESULTS);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.opensha.sha.imr.attenRelImpl.CY_2006_AttenRel.getMean(int, double, double, double, double, double, double)'
	public void testGetMean() {
		int numDataLines = testDataLines.size();
		for(int i=1;i<numDataLines;++i){
			cy_2006.setIntensityMeasure(cy_2006.SA_NAME);
			StringTokenizer st = new StringTokenizer((String)testDataLines.get(i));
			double period = Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.PERIOD_NAME).setValue(new Double(period));
			double mag = Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.MAG_NAME).setValue(new Double(mag));
			double rrup = Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(DistanceRupParameter.NAME).setValue(new Double(rrup));
			double vs30 = Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.VS30_NAME).setValue(new Double(vs30));
			double rjb = Double.parseDouble(st.nextToken().trim());
			double distRupMinusJB_OverRup = (rrup-rjb)/rrup;
			cy_2006.getParameter(DistRupMinusJB_OverRupParameter.NAME).setValue(new Double(distRupMinusJB_OverRup));
			double rupWidth =  Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.RUP_WIDTH_NAME).setValue(new Double(rupWidth));
			int frv =  Integer.parseInt(st.nextToken().trim());
			int fnm = Integer.parseInt(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.FLT_TYPE_NAME).setValue(cy_2006.FLT_TYPE_STRIKE_SLIP);
			if(frv ==1 && fnm==0)
			   cy_2006.getParameter(cy_2006.FLT_TYPE_NAME).setValue(cy_2006.FLT_TYPE_REVERSE);
			else if(frv ==0 && fnm ==1)
			   cy_2006.getParameter(cy_2006.FLT_TYPE_NAME).setValue(cy_2006.FLT_TYPE_NORMAL);
			
			double depthTop =  Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.RUP_TOP_NAME).setValue(new Double(depthTop));
			double dip =  Double.parseDouble(st.nextToken().trim());
			cy_2006.getParameter(cy_2006.DIP_NAME).setValue(new Double(dip));
			double meanVal = cy_2006.getMean();
			st.nextToken();
			double targetMedian = Double.parseDouble(st.nextToken().trim());
			double medianFromOpenSHA = Double.parseDouble(format.format(Math.exp(meanVal)));	
			//System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
			boolean results = compareResults(medianFromOpenSHA,targetMedian);
			//if the test was failure the add it to the test cases Vecotr that stores the values for  that failed
        	 	
               //If any test for the CY-2006 failed
              
			
            if(results == false){
            	 String failedResultMetadata = "Results failed for CY-2006 attenuation with the following parameter settings:"+
            	          "IMT ="+cy_2006.SA_NAME+" with Period ="+period+"\nMag ="+(float)mag+" rRup = "+rrup+
            	          "  vs30 = "+vs30+"  rjb = "+(float)rjb+"\n   rupWidth = "+rupWidth+"   Frv = "+frv+
            	          "   Fnm = "+fnm+
            	          "   depthTop = "+depthTop+"\n   dip = "+dip;
            	 //System.out.println("Test number= "+i+" failed for +"+failedResultMetadata);
            	 //System.out.println("OpenSHA Median = "+medianFromOpenSHA+"   Target Median = "+targetMedian);
              this.assertNull(failedResultMetadata,failedResultMetadata);
            }

            //if the all the succeeds and their is no fail for any test
            else {
              this.assertTrue("CY-2006 Test succeeded for all the test cases",results);
            }
		}
	}
*/
	
	public void testGetMean() {
		
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
