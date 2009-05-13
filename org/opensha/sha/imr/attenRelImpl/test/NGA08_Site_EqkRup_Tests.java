package org.opensha.sha.imr.attenRelImpl.test;

import java.util.ListIterator;

import junit.framework.TestCase;

import org.opensha.data.Location;
import org.opensha.data.Site;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.param.*;


/**
 * This tests whether the 2008 NGA attenuation relationships get their Site-, EqkRupture-, and 
 * PropagationEffect-related parameters set properly when a Site and EqkRupture are passed in.  
 * All distance measures are checked to be withing 100 meters of the target.
 * @author field
 *
 */
public class NGA08_Site_EqkRup_Tests extends TestCase {
	
	private final static boolean D = false;
	
	double dip, rake, upperSeisDepth, faultDDW, faultLat1, faultLat2;
	
	// hard-coded test values (just make sure they aren't equal to defaults in atten relationships)
	double mag=6.345, vs30=551.2, depth2pt5=8.134, depth1pt0=1111.1;
	String vs30_type = AttenuationRelationship.VS30_TYPE_INFERRED;
	Boolean aftershock = new Boolean(false);
	
		
	double[] distX;
	double[][] distJB, distRup;
	
	public static final double DIST_THRESHOLD = 0.1;  // distances must be within 100 meters
	
	int[][] onHangingWall;
	double[] rakes = {0, 180, 90,-90};
	
	
	AS_2008_AttenRel as08 = new AS_2008_AttenRel(null);
	CB_2008_AttenRel cb08 = new CB_2008_AttenRel(null);
	CY_2008_AttenRel cy08 = new CY_2008_AttenRel(null);
	BA_2008_AttenRel ba08 = new BA_2008_AttenRel(null);
	
	EqkRupture eqkRup;
	Site site;
	
	// test site locations
	double kmToDeg = 360.0/40000.0;
	double[] lats = {100*kmToDeg, 25*kmToDeg, 0.0, -25*kmToDeg, -100*kmToDeg};
	double[] lons = {-20*kmToDeg, 2.5*kmToDeg, 20*kmToDeg};

	
	
	
	
	public NGA08_Site_EqkRup_Tests() {
		
		// Create the test Earthquake Rupture
		dip = 60;
		upperSeisDepth = 5;
		faultDDW = 10;
		double lowerSeisDepth = upperSeisDepth + faultDDW*Math.sin(Math.toRadians(dip)); // Down-dip width = 10km
		faultLat1 = -0.25;
		faultLat2 = 0.25;		
		Location faultLoc1 = new Location(faultLat1,0.0,5.0);
		Location faultLoc2 = new Location(faultLat2,0.0,5.0);
		FaultTrace trace = new FaultTrace("test trace");
		trace.addLocation(faultLoc1);
		trace.addLocation(faultLoc2);
		StirlingGriddedSurface surface = new StirlingGriddedSurface(trace, dip, upperSeisDepth, lowerSeisDepth, 0.1);
//		System.out.println(surface.toString());
		eqkRup = new EqkRupture();
		eqkRup.setRuptureSurface(surface);
		
		
		// Create the test Site object & add the site-related parameters to the Site
		site = new Site();
		ListIterator<ParameterAPI> it = as08.getSiteParamsIterator();
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			if(!site.containsParameter(param)) site.addParameter(param);
		}

		it = cb08.getSiteParamsIterator();
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			if(!site.containsParameter(param)) site.addParameter(param);
		}

		it = cy08.getSiteParamsIterator();
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			if(!site.containsParameter(param)) site.addParameter(param);
		}

		it = ba08.getSiteParamsIterator();
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			if(!site.containsParameter(param)) site.addParameter(param);
		}

		if(D) {
			it = site.getParametersIterator();
			while(it.hasNext()) System.out.println(it.next().getName());			
		}
		if(D) {
			it = site.getParametersIterator();
			while(it.hasNext()) System.out.println(it.next().getValue());
		}

		mkDistMatrices();
	}

	/**
	 * This creates the target distances for distX, distJB, and DistRup
	 */
	private void mkDistMatrices() {
		
		// Indices for the test site locations
		/*
		 * 	lon0	lon1	lon2
		 * 	00		01		02	lat0
		 * 	10		11		12	lat1
		 *	20		21		22	lat2
		 * 	30		31		32	lat3
		 * 	40		41		42	lat4
		 * 
		 * 
		 */
		
		// Now I need to create and fill in the target (correct) distance measures
		double distX_lon0 = lons[0]/kmToDeg;
		double distX_lon1 = lons[1]/kmToDeg;
		double distX_lon2 = lons[2]/kmToDeg;
		
		// these are what the distX values should be for each lon (independent of latitude)
		double[] temp_distX = {distX_lon0, distX_lon1, distX_lon2};
		distX=temp_distX;
		
		double distJB_01 = (lats[0]-faultLat2)/kmToDeg;
		double distJB_00 = Math.sqrt(Math.pow(distJB_01, 2) + Math.pow(distX_lon0, 2));
		double distJB_02 = Math.sqrt(Math.pow(distJB_01, 2) + Math.pow(distX_lon2-5.0, 2));

		double[][] temp_distJB = {
							{distJB_00,		distJB_01,	distJB_02},
							{-distX_lon0, 	0.0, 		distX_lon2-5.0},
							{-distX_lon0, 	0.0, 		distX_lon2-5.0},
							{-distX_lon0, 	0.0, 		distX_lon2-5.0},
							{distJB_00,		distJB_01,	distJB_02},
							};
		distJB=temp_distJB;
		
		double distRup_11 = Math.sqrt(lons[1]/kmToDeg*lons[1]/kmToDeg+upperSeisDepth*upperSeisDepth);
		DistanceRupParameter distRupCalc = new DistanceRupParameter();
		site.setLocation(new Location(lats[1],lons[2]));
		double distRup_12 = ((Double)distRupCalc.getValue(eqkRup, site)).doubleValue();
		site.setLocation(new Location(lats[0],lons[1]));
		double distRup_01 = ((Double)distRupCalc.getValue(eqkRup, site)).doubleValue();
		site.setLocation(new Location(lats[0],lons[2]));
		double distRup_02 = ((Double)distRupCalc.getValue(eqkRup, site)).doubleValue();
		double distRup00 = Math.sqrt(distJB_00*distJB_00+upperSeisDepth*upperSeisDepth);
		double distRup10 = Math.sqrt(distX_lon0*distX_lon0+upperSeisDepth*upperSeisDepth);
		
		double[][] temp_distRup = {
				{distRup00,		distRup_01,	distRup_02},
				{distRup10, 	distRup_11, distRup_12},
				{distRup10, 	distRup_11, distRup_12},
				{distRup10, 	distRup_11, distRup_12},
				{distRup00,		distRup_01,	distRup_02},
				};
		distRup=temp_distRup;

		int[][] temp_onHangingWall = {{0,1,1},{0,1,1},{0,1,1},{0,1,1},{0,1,1}};
		onHangingWall = temp_onHangingWall;
			
		if(D) {
			System.out.print("distX:\t"+distX[0]+"\t"+distX[1]+"\t"+distX[2]);

			System.out.print("\n\ndistJB:");
			for(int lat=0; lat<lats.length; lat++) {
				System.out.print("\n");
				for(int lon=0; lon<lons.length;lon++)
					System.out.print((float)distJB[lat][lon]+"\t");
			}

			System.out.print("\n\ndistRup:");
			for(int lat=0; lat<lats.length; lat++) {
				System.out.print("\n");
				for(int lon=0; lon<lons.length;lon++)
					System.out.print((float)distRup[lat][lon]+"\t");
			}

			System.out.print("\n\nonHangingWall:");
			for(int lat=0; lat<lats.length; lat++) {
				System.out.print("\n");
				for(int lon=0; lon<lons.length;lon++)
					System.out.print(onHangingWall[lat][lon]+"\t");
			}
			System.out.print("\n\n");
		}
	}
	
	/**
	 * This is the actual test
	 */
	private boolean doTest(AttenuationRelationship attenRel) {
		System.out.println("Testing " + attenRel.getName());
		// set hard-coded eqk rupture stuff
		eqkRup.setMag(mag);
		
		// set site parameters
		site.getParameter(AttenuationRelationship.VS30_NAME).setValue(new Double(vs30));
		site.getParameter(AttenuationRelationship.VS30_TYPE_NAME).setValue(vs30_type);
		site.getParameter(AttenuationRelationship.DEPTH_2pt5_NAME).setValue(new Double(depth2pt5));
		site.getParameter(AttenuationRelationship.DEPTH_1pt0_NAME).setValue(new Double(depth1pt0));

		int counter=0;
		
		boolean success = true;

		for(int irake=0;irake<rakes.length;irake++) {
			rake = rakes[irake];
			eqkRup.setAveRake(rake);

			// Loop over the site locations
			for(int lat=0; lat<lats.length; lat++) {
				for(int lon=0; lon<lons.length;lon++) {
					site.setLocation(new Location(lats[lat],lons[lon]));


					attenRel.setSite(site);
					cb08.setSite(site);
					cy08.setSite(site);
					ba08.setSite(site);

					attenRel.setEqkRupture(eqkRup);
					cb08.setEqkRupture(eqkRup);
					cy08.setEqkRupture(eqkRup);
					ba08.setEqkRupture(eqkRup);

					boolean testResult = checkAttenRel(lat, lon, attenRel);
					if (!testResult) {
						success = false;
					}
				}
			}
			counter +=1;
			if (D) System.out.println("DONE WITH RAKE "+counter);
		}
		if (success) {
			System.out.println("Success!");
			return true;
		} else {
			System.out.println("FAILURE!");
			return false;
		}
	}
	
	/**
	 * This tests AS 2008 as a JUnit test. It will be run by JUnit because the method starts with 'test'
	 */
	public void testAS08() {
		assertTrue(doTest(as08));
	}
	
	/**
	 * This tests CB 2008 as a JUnit test. It will be run by JUnit because the method starts with 'test'
	 */
	public void testCB08() {
		assertTrue(doTest(cb08));
	}
	
	/**
	 * This tests BA 2008 as a JUnit test. It will be run by JUnit because the method starts with 'test'
	 */
	public void testBA08() {
		assertTrue(doTest(ba08));
	}
	
	/**
	 * This tests CY 2008 as a JUnit test. It will be run by JUnit because the method starts with 'test'
	 */
	public void testCY08() {
		assertTrue(doTest(cy08));
	}
	
	/**
	 * This runs all tests without using JUnit
	 */
	public void runAllTests() {
		doTest(as08);
		doTest(cb08);
		doTest(ba08);
		doTest(cy08);
	}
	
	
	/**
	 * This checks the passed-in attenuation relationship
	 * @param lat
	 * @param lon
	 * @param attenRel
	 * @return
	 */
	private boolean checkAttenRel(int lat, int lon, AttenuationRelationship attenRel) {
		
		// Check the Earthquake Rupture Parameters
		ListIterator<ParameterAPI> it = attenRel.getEqkRuptureParamsIterator();
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			
			if(param.getName().equals(AttenuationRelationship.AFTERSHOCK_NAME)) {
				Boolean ashock = (Boolean)param.getValue();
				if(!aftershock.equals(ashock)){
					if (D) System.out.println(param.getName()+"\t"+ashock+"\t"+aftershock);
					return false;
				}
			}
			else if(param.getName().equals(AttenuationRelationship.DIP_NAME)) {
				double testDip = ((Double)param.getValue()).doubleValue();
				if(dip != testDip){
					if (D) System.out.println(param.getName()+"\t"+dip+"\t"+testDip);
					return false;
				}
			}
			else if(param.getName().equals(AttenuationRelationship.MAG_NAME)) {
				double testMag = ((Double)param.getValue()).doubleValue();
				if(mag != testMag){
					if (D) System.out.println(param.getName()+"\t"+mag+"\t"+testMag);
					return false;				
				}
			}
			else if(param.getName().equals(AttenuationRelationship.RUP_TOP_NAME)) {
				double testRupTop = ((Double)param.getValue()).doubleValue();
				if(upperSeisDepth != testRupTop){
					if (D) System.out.println(param.getName()+"\t"+upperSeisDepth+"\t"+testRupTop);
					return false;
				}
			}
			else if(param.getName().equals(AttenuationRelationship.RUP_WIDTH_NAME)) {
				double testWidth = ((Double)param.getValue()).doubleValue();
				if(faultDDW != testWidth){
					if (D) System.out.println(param.getName()+"\t"+faultDDW+"\t"+testWidth);
					return false;				
				}
			}
			else if(param.getName().equals(AttenuationRelationship.FLT_TYPE_NAME)) {
				String actual;
				if(rake==0 || rake==180) {
					actual = (String)param.getValue(); 
					if(!actual.equals(AS_2008_AttenRel.FLT_TYPE_STRIKE_SLIP)) {
						if (D) System.out.println(param.getName()+"\t"+actual+"\t"+rake);
						return false;
					}
				}
				if(rake==-90) {
					actual = (String)param.getValue(); 
					if(!actual.equals(AS_2008_AttenRel.FLT_TYPE_NORMAL)){
						if (D) System.out.println(param.getName()+"\t"+actual+"\t"+rake);
						return false;
					}
				}
				if(rake==90) {
					actual = (String)param.getValue(); 
					if(!(actual.equals(AS_2008_AttenRel.FLT_TYPE_REVERSE) || actual.equals(BA_2008_AttenRel.FLT_TYPE_REVERSE))){
						if (D) System.out.println(param.getName()+"\t"+actual+"\t"+rake);
						return false;
					}
				}
			}
			else
				throw new RuntimeException("Paramter not found");

		}

		
		// Check the Site Parameters
		it = attenRel.getSiteParamsIterator();
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			
			if(param.getName().equals(AttenuationRelationship.VS30_TYPE_NAME)) {
				String vs30_type_value = (String)param.getValue();
				if(!vs30_type_value.equals(vs30_type)){
					if (D) System.out.println(param.getName()+"\t"+vs30_type_value+"\t"+vs30_type);
					return false;
				}
			}
			else if(param.getName().equals(AttenuationRelationship.VS30_NAME)) {
				double testVs30 = ((Double)param.getValue()).doubleValue();
				if(vs30 != testVs30){
					if (D) System.out.println(param.getName()+"\t"+vs30+"\t"+testVs30);
					return false;
				}
			}
			else if(param.getName().equals(AttenuationRelationship.DEPTH_2pt5_NAME)) {
				double testDepth2pt5 = ((Double)param.getValue()).doubleValue();
				if(depth2pt5 != testDepth2pt5){
					if (D) System.out.println(param.getName()+"\t"+depth2pt5+"\t"+testDepth2pt5);
					return false;				
				}
			}
			else if(param.getName().equals(AttenuationRelationship.DEPTH_1pt0_NAME)) {
				double testDepth1pt0 = ((Double)param.getValue()).doubleValue();
				if(depth1pt0 != testDepth1pt0){
					if (D) System.out.println(param.getName()+"\t"+depth1pt0+"\t"+testDepth1pt0);
					return false;
				}
			}
			else
				throw new RuntimeException("Paramter not found");

		}


		// Check the Propagation Effect Parameters
		it = attenRel.getPropagationEffectParamsIterator();
		
		double dRup = distRup[lat][lon];
		double dJB  = distJB[lat][lon];
		double dX   = distX[lon];
		double dRupMinusJB = (dRup-dJB)/dRup;
		double dRupMinusDX;
		if(dX<0) dRupMinusDX = (dRup+dX)/dRup;
		else dRupMinusDX = (dRup-dX)/dRup;
		
		while(it.hasNext()) {
			ParameterAPI param = it.next();
			
			if(param.getName().equals(AttenuationRelationship.HANGING_WALL_FLAG_NAME)) {
				int value;
				if((Boolean)param.getValue())
					value = 1;
				else
					value = 0;
				if(value != onHangingWall[lat][lon]) {
					if (D) System.out.println(param.getName()+"\t"+value+"\t"+onHangingWall[lat][lon]);
					return false;
				}
			}
			else if(param.getName().equals(DistanceRupParameter.NAME)) {
				double dist = ((Double)param.getValue()).doubleValue();
				if(Math.abs(dRup-dist) > DIST_THRESHOLD) {
					if (D) System.out.println(param.getName()+"\t"+dRup+"\t"+dist+"\tlat,lon indices: "+lat+","+lon);
					return false;				
				}
			}
			else if(param.getName().equals(DistanceJBParameter.NAME)) {
				double dist = ((Double)param.getValue()).doubleValue();
				if(Math.abs(dJB-dist) > DIST_THRESHOLD) {
					if (D) System.out.println(param.getName()+"\t"+dJB+"\t"+dist+"\tlat,lon indices: "+lat+","+lon);
					return false;
				}
			}
			else if(param.getName().equals(DistRupMinusJB_OverRupParameter.NAME)) {
				double val = ((Double)param.getValue()).doubleValue();
				double pred_distJB = dRup*(1-val);
				if(Math.abs(dJB-pred_distJB) > DIST_THRESHOLD) {
					if (D) System.out.println(param.getName()+"\t"+dRupMinusJB+"\t"+val+"\tlat,lon indices: "+lat+","+lon);
					return false;
				}
			}
			else if(param.getName().equals(AttenuationRelationship.DIST_RUP_MINUS_DIST_X_NAME)) {
				double val = ((Double)param.getValue()).doubleValue();
				double pred_distX = dRup*(1-val);
				if(onHangingWall[lat][lon] != 1) pred_distX *= -1;  // change sign if on hanging wall
				if(Math.abs(dX-pred_distX) > DIST_THRESHOLD) {
					if (D) System.out.println(param.getName()+"\t"+dRupMinusDX+"\t"+val+"\tlat,lon indices: "+lat+","+lon);
					if (D) System.out.println("pred distX="+pred_distX+"\tdX="+dX);
					return false;
				}
			}
			else
				throw new RuntimeException("Paramter not found");

		}
		return true;
	}

	/**  
	 * This test whether the Site, EqkRupture, and PropagationEffect  relatedParameters are 
	 * set properly when these objects are passed into the NGA models.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		NGA08_Site_EqkRup_Tests test = new NGA08_Site_EqkRup_Tests();
		test.runAllTests();
	}

}
