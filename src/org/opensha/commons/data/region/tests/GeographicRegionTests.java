package org.opensha.commons.data.region.tests;
import junit.framework.Assert;

import junit.framework.TestCase;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.GeographicRegion;


/**
 * <b>Title:</b> GeographicRegionTests<p>
 *
 * <b>Description:</b> Class used by the JUnit testing harness to test the
 * GeographicalRegionTests. This class was used to test using JUnit.
 *
 * Note: Requires the JUnit classes to run<p>
 * Note: This class is not needed in production, only for testing.<p>
 *
 * Any function that begins with test will be executed by JUnit<p>
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 * TODO retire package
 */

public class GeographicRegionTests extends TestCase
{
    public GeographicRegionTests(String name)
    {
        super( name );
    }

    protected void setUp()
    {

    }

    protected void tearDown()
    {

    }

   public void testGeoRegion()
    {

      Location tempLoc = new Location(33,120);
      LocationList tempLocList = new LocationList();
      tempLocList.addLocation(tempLoc);
      tempLoc = new Location(33,122);
      tempLocList.addLocation(tempLoc);
      tempLoc = new Location(34,122);
      tempLocList.addLocation(tempLoc);
      tempLoc = new Location(34,120);
      tempLocList.addLocation(tempLoc);

//      GeographicRegion geoReg = new GeographicRegion(tempLocList);
//
//      int numLocs = 4;
//      assertEquals("locations in region unexpected", geoReg.getNumRegionOutlineLocations(),numLocs);
//      assertTrue(geoReg.isLocationInside(new Location(33.5,121)));
//      assertTrue(geoReg.isLocationInside(new Location(33.001,120.001)));
//      assertTrue(geoReg.isLocationInside(new Location(33,120)));
//      assertTrue(!(geoReg.isLocationInside(new Location(33,122))));
//      assertTrue(!(geoReg.isLocationInside(new Location(34,122))));
//      assertTrue(!(geoReg.isLocationInside(new Location(34,120))));
//      assertTrue(geoReg.isLocationInside(new Location(33,121)));
//      assertTrue(geoReg.isLocationInside(new Location(33.5,120)));
   }
}
