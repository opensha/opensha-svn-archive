package org.opensha.sha.imr.attenRelImpl.test;

import java.util.ListIterator;

import org.opensha.data.Location;
import org.opensha.data.Site;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.surface.StirlingGriddedSurface;

import sun.text.CompactShortArray.Iterator;

public class NGA_Site_EqkRup_Tests {
	
	public NGA_Site_EqkRup_Tests() {
	}
	
	
	
	
	public void test() {
		// Earthquake Rupture
		double dip = 60;
		double upperSeisDepth = 5;
		double lowerSeisDepth = 5 + Math.sqrt(75); // Down-dip width = 10km
		Location faultLoc1 = new Location(-0.25,0.0);
		Location faultLoc2 = new Location(-0.25,0.0);
		FaultTrace trace = new FaultTrace("test trace");
		trace.addLocation(faultLoc1);
		trace.addLocation(faultLoc2);
		StirlingGriddedSurface surface = new StirlingGriddedSurface(trace, dip, upperSeisDepth, lowerSeisDepth, 0.1);
		EqkRupture eqkRup = new EqkRupture();
		eqkRup.setRuptureSurface(surface);
		double rake = 0;
		double mag = 6;
		eqkRup.setMag(mag);
		eqkRup.setAveRake(rake);
		
		// Sites
		double kmToDegreeConversion = 360/40000;
		double siteLat1 = -100.0*kmToDegreeConversion;
		double siteLat2 = -50.0*kmToDegreeConversion;
		double siteLat3 = 0.0;
		double siteLat4 = 50.0*kmToDegreeConversion;
		double siteLat5 = 100.0*kmToDegreeConversion;
		
		double siteLon1 = -20*kmToDegreeConversion;
		double siteLon2 = 2.5*kmToDegreeConversion;
		double siteLon3 = 20*kmToDegreeConversion;

		Site site = new Site();
		
		AS_2008_AttenRel as08 = new AS_2008_AttenRel(null);
		CB_2008_AttenRel cb08 = new CB_2008_AttenRel(null);
		CY_2008_AttenRel cy08 = new CY_2008_AttenRel(null);
		BA_2008_AttenRel ba08 = new BA_2008_AttenRel(null);
		
		ListIterator<ParameterAPI> it = as08.getSiteParamsIterator();
		while(it.hasNext()) site.addParameter(it.next());

		it = cb08.getSiteParamsIterator();
		while(it.hasNext()) site.addParameter(it.next());

		it = cy08.getSiteParamsIterator();
		while(it.hasNext()) site.addParameter(it.next());

		it = ba08.getSiteParamsIterator();
		while(it.hasNext()) site.addParameter(it.next());

		System.out.println("here");
		it = site.getParametersIterator();
		while(it.hasNext()) System.out.println(it.next().getName());

		
	}

	/**  
	 * This test whether the Site, EqkRupture, and PropagationEffect  relatedParameters are 
	 * set properly when these objects are passed into the NGA models.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		NGA_Site_EqkRup_Tests test = new NGA_Site_EqkRup_Tests();
		System.out.println("here too");
		test.test();

	}

}
