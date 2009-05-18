package org.opensha.commons.data.region.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.region.RectangularGeographicRegion;
import org.opensha.data.region.*;
import org.opensha.data.*;
import org.opensha.exceptions.*;
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
 */

public class RectangularGeoRegionTests extends TestCase
{
    public RectangularGeoRegionTests(String name)
    {
        super( name );
    }

    protected void setUp()
    {

    }

    protected void tearDown()
    {

    }

   public void testRecGeoRegion()
   {
     double x = 33.0;
     double y = 34.0;
     double z = 120.0;
     double za = 122.0;

    RectangularGeographicRegion geoReg = null;
    try {
      geoReg = new RectangularGeographicRegion(x, y, z, za);
    }
    catch (RegionConstraintException ex) {
      ex.printStackTrace();
    }
     int numLocs = 4; // Each corner in the grid is a location.
     assertEquals("Unexpected Number of Locations",numLocs,geoReg.getNumRegionOutlineLocations());

     assertTrue("Unexpected Min Lat: ", x==geoReg.getMinLat());
     assertTrue("Unexpected Max Lat: ", y==geoReg.getMaxLat());
     assertTrue("Unexpected Min Lon: ", z==geoReg.getMinLon());
     assertTrue("Unexpected Max Lon: ", za==geoReg.getMaxLon());

     assertTrue("Should be inside",geoReg.isLocationInside(new Location(33.5,121)));
     assertTrue("Should be inside",geoReg.isLocationInside(new Location(33.001,120.001)));
     assertTrue("Should be inside",geoReg.isLocationInside(new Location(33,120)));
     assertTrue("Should be outside",!(geoReg.isLocationInside(new Location(33,122))));
     assertTrue("Should be outside",!(geoReg.isLocationInside(new Location(34,122))));
     assertTrue("Should be outside",!(geoReg.isLocationInside(new Location(34,120))));
     assertTrue("Should be inside",geoReg.isLocationInside(new Location(33,121)));
     assertTrue("Should be inside",geoReg.isLocationInside(new Location(33.5,120)));
    }
}
