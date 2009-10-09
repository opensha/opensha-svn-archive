package org.opensha.commons.data.siteData;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.siteData.impl.CVM2BasinDepth;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.USGSBayAreaBasinDepth;
import org.opensha.commons.data.siteData.impl.WillsMap2000TranslatedVs30;
import org.opensha.commons.data.siteData.impl.WillsMap2006;

import com.sun.xml.internal.ws.protocol.soap.ServerMUTube;

import junit.framework.TestCase;

public class SiteDataProvidersTest extends TestCase {
	
	private static final boolean D = true;
	
	private Location loc1 = new Location(34d, -118d);
	private Location loc2 = new Location(34d, -120d);
	private Location loc3 = new Location(36d, -120d);
	private Location loc4 = new Location(38d, -123d);
	private Location loc5 = new Location(0d, 0d);
	
	private LocationList locs = new LocationList();
	
	public SiteDataProvidersTest() {
		super();
		
		locs.addLocation(loc1);
		locs.addLocation(loc2);
		locs.addLocation(loc3);
		locs.addLocation(loc4);
		locs.addLocation(loc5);
	}
	
	private void testProv(SiteDataAPI<?> prov, ArrayList<?> expectedVals) throws IOException {
		ArrayList<?> vals = prov.getValues(locs);
		
		for (int i=0; i<expectedVals.size(); i++) {
			Object expectedVal = expectedVals.get(i);
			Object serverGroupVal = vals.get(i);
			
			// just to make sure that the server gives the save values individually as it does in a list
			Object serverSingleVal = prov.getValue(locs.getLocationAt(i));
			
			if (D) System.out.println(prov.getShortName() + " " + i + ", exp: " + expectedVal
					+ ", single: " + serverSingleVal + ", group: " + serverGroupVal);
			
			assertEquals(expectedVal, serverGroupVal);
			assertEquals(expectedVal, serverSingleVal);
		}
	}
	
	public void testCVM2() throws IOException {
		CVM2BasinDepth prov = new CVM2BasinDepth();
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(4.7753623046875);
		vals.add(0d);
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
	
	public void testCVM4_2_5() throws IOException {
		CVM4BasinDepth prov = new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5);
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(2.147396484375);
		vals.add(0d);
		vals.add(0d);
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
	
	public void testCVM4_1_0() throws IOException {
		CVM4BasinDepth prov = new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0);
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(0.3040733642578125);
		vals.add(0d);
		vals.add(0d);
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
	
	public void testUSGSBayArea_2_5() throws IOException {
		USGSBayAreaBasinDepth prov = new USGSBayAreaBasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5);
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		vals.add(0.712);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
	
	public void testUSGSBayArea_1_0() throws IOException {
		USGSBayAreaBasinDepth prov = new USGSBayAreaBasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0);
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		vals.add(Double.NaN);
		vals.add(0.21681817626953126);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
	
	public void testWills2006() throws IOException {
		WillsMap2006 prov = new WillsMap2006();
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(390d);
		vals.add(Double.NaN);
		vals.add(390d);
		vals.add(390d);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
	
	public void testWills2000() throws IOException {
		WillsMap2000TranslatedVs30 prov = new WillsMap2000TranslatedVs30();
		
		ArrayList<Double> vals = new ArrayList<Double>();
		
		vals.add(360d);
		vals.add(Double.NaN);
		vals.add(360d);
		vals.add(1000d);
		vals.add(Double.NaN);
		
		testProv(prov, vals);
	}
}
