package org.opensha.commons.geo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.calc.RelativeLocation;

public class LocationListTest {

	private static LocationList ll1, ll2;
	private static Location p1,p2,p3,p4,p5,p6,p7,p8,p9;
	
	private double result_p3p4_p8 = 78.62078721818267;
	private double result_p4_p8 = 111.19505230826488;
	private double result_p6p7_p9 = 78.62078721818267;
	private double result_p6_p9 = 111.04265949308352;
	
	@BeforeClass
	public static void setUp() {
		p1 = new Location(-5,0);
		p2 = new Location(-3,-2);
		p3 = new Location(-2,-2);
		p4 = new Location(0,0);
		p5 = new Location(2,2);
		p6 = new Location(3,2);
		p7 = new Location(5,0);
		
		p8 = new Location(-1,0);
		p9 = new Location(3,1);
		
		ll1 = new LocationList();
		ll1.add(p1);
		ll1.add(p2);
		ll1.add(p3);
		ll1.add(p4);
		ll1.add(p5);
		ll1.add(p6);
		ll1.add(p7);

		ll2 = new LocationList();
		ll2.add(p1);
		ll2.add(p3);
		ll2.add(p2);
		ll2.add(p4);
		ll2.add(p6);
		ll2.add(p5);
		ll2.add(p7);
	}

	@Test
	public void testHashCode() {
		LocationList ll_clone = ll1.clone();
		assertTrue(ll_clone.hashCode() == ll1.hashCode());
		assertTrue(ll1.hashCode() != ll2.hashCode());
	}

	@Test
	public void testReverse() {
		LocationList ll_copy = ll1.clone();
		ll_copy.reverse(); 
		assertTrue(ll1.get(0).equals(ll_copy.get(6)));
		assertTrue(ll1.get(1).equals(ll_copy.get(5)));
		assertTrue(ll1.get(2).equals(ll_copy.get(4)));
		assertTrue(ll1.get(4).equals(ll_copy.get(2)));
		assertTrue(ll1.get(5).equals(ll_copy.get(1)));
		assertTrue(ll1.get(6).equals(ll_copy.get(0)));
	}

	@Test
	public void testSplit() {
		List<LocationList> lists = ll1.split(2);
		assertTrue(lists.size() == 4);
		assertTrue(lists.get(0).get(1).equals(p2));
		assertTrue(lists.get(1).get(0).equals(p3));
		assertTrue(lists.get(2).get(1).equals(p6));
		assertTrue(lists.get(3).get(0).equals(p7));
		assertTrue(lists.get(3).size() == 1);
	}

	@Test
	public void testMinDistToLocation() {
		assertTrue(ll1.minDistToLocation(p8) == result_p4_p8);
		assertTrue(ll1.minDistToLocation(p9) == result_p6_p9);
	}

	@Test
	public void testMinDistToLine() {
		assertTrue(ll1.minDistToLine(p8) == result_p3p4_p8);
		assertTrue(ll1.minDistToLine(p8) == result_p6p7_p9);
	}

	@Test
	public void testClone() {
		LocationList clone = ll1.clone();
		assertTrue(clone.get(0).equals(ll1.get(0)));
		assertTrue(clone.get(2).equals(ll1.get(2)));
		assertTrue(clone.get(4).equals(ll1.get(4)));
		assertTrue(clone.get(6).equals(ll1.get(6)));
	}

	@Test
	public void testEqualsObject() {
		LocationList eqTest = new LocationList();
		eqTest.add(p1);
		eqTest.add(p2);
		eqTest.add(p3);
		eqTest.add(p4);
		eqTest.add(p5);
		eqTest.add(p6);
		eqTest.add(p7);
		assertTrue(ll1.equals(eqTest));
		assertTrue(!ll1.equals(ll2));
	}

	@Test
	public void testToString() {
		StringBuffer b = new StringBuffer();
		b.append("LocationList size: " + ll1.size() + "\n");
		b.append("LocationList data: ");
		for (Location loc : ll1) {
			b.append(loc + " ");
		}
		assertTrue(b.toString().equals(ll1.toString()));
	}

}
