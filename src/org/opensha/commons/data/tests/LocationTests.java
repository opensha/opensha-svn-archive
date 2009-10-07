package org.opensha.commons.data.tests;

import junit.framework.TestCase;

import org.opensha.commons.data.Location;


/**
 * <b>Title:</b> TestLocation<p>
 *
 * <b>Description:>/b> JUnit tester for the Location object. Tests every
 * piece of functionality, included expected fail conditions. If any
 * part of the test fails, the error code is indicated. Useful to ensure
 * the accuracy and weither the class is functioning as expect. Any
 * time in the future if the internal code is changed, this class will
 * verify that the class still works as prescribed. This is called
 * unit testing in software engineering. <p>
 *
 * Note: Requires the JUnit classes to run<p>
 * Note: This class is not needed in production, only for testing.<p>
 *
 * JUnit has gained many supporters, specifically used in ANT which is a java
 * based tool that performs the same function as the make command in unix. ANT
 * is developed under Apache.<p>
 *
 * Any function that begins with test will be executed by JUnit<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class LocationTests extends TestCase {

    Location location = new Location();

    public LocationTests(String s) {
    super(s);
    location.setDepth( 10 );
    location.setLatitude( 10 );
    location.setLongitude( 10 );
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testEqualsLocation() {

    Location loc1 =  new Location();
    loc1.setDepth( 10 );
    loc1.setLatitude( 10 );
    loc1.setLongitude( 10 );

    boolean booleanRet = location.equalsLocation(loc1);
    this.assertTrue( booleanRet );


    loc1.setLongitude( 20 );
    booleanRet = location.equalsLocation(loc1);
    this.assertTrue( !booleanRet );


    loc1.setLongitude( 10 );
    loc1.setLatitude( 20 );
    booleanRet = location.equalsLocation(loc1);
    assertTrue( !booleanRet );

    loc1.setLatitude( 10 );
    loc1.setDepth( 20 );
    booleanRet = location.equalsLocation(loc1);
    assertTrue( !booleanRet );

    loc1.setDepth( 10 );
    booleanRet = location.equalsLocation(loc1);
    assertTrue( booleanRet );

  }

  public void testGetDepth() {
    double doubleRet = location.getDepth();
    assertTrue( doubleRet == 10.0 );
  }

  public void testGetLatitude() {
    double doubleRet = location.getLatitude();
    assertTrue( doubleRet == 10.0 );
  }

  public void testGetLongitude() {
    double doubleRet = location.getLongitude();
    assertTrue( doubleRet == 10.0 );
  }

  public void testSetDepth() {
    double depth1 =   15.0;
    location.setDepth(depth1);
    double doubleRet = location.getDepth();
    assertTrue( doubleRet == 15.0 );

    location.setDepth( 10.0 );
    doubleRet = location.getDepth();
    assertTrue( doubleRet == 10.0 );
  }


  public void testSetLatitude() {
    double latitude1 =   15.0   /** @todo fill in non-null value */;
    double doubleRet;
    try {
      location.setLatitude(latitude1);
      doubleRet = location.getLatitude();
      assertTrue( doubleRet == 15.0 );

      location.setLatitude( 10.0 );
      doubleRet = location.getLatitude();
      assertTrue( doubleRet == 10.0 );
    }
    catch(Exception e) {
      System.err.println("Exception thrown:  "+e);
    }


    latitude1 = -95.1;
    try {
      location.setLatitude(latitude1);
    }
    catch(Exception e) {
        doubleRet = location.getLatitude();
        assertTrue( doubleRet == 10.0 );
        //System.err.println("Successfully caught Exception thrown:  " + e);
    }



    latitude1= 95.1;
    try {
      location.setLatitude(latitude1);

    }
    catch(Exception e) {
        doubleRet = location.getLatitude();
        assertTrue( doubleRet == 10.0 );
        //System.err.println("Successfully caught Exception thrown:  " + e);
    }
  }


  public void testSetLongitude() {
    double longitude1= 15.0 ;
    double doubleRet;
    try {
      location.setLongitude(longitude1);
      doubleRet = location.getLongitude();
      assertTrue( doubleRet == 15.0 );

      location.setLongitude( 10.0 );
      doubleRet = location.getLongitude();
      assertTrue( doubleRet == 10.0 );
    }
    catch(Exception e) {
      System.err.println("Exception thrown:  "+e);
    }


    longitude1 = -181.1;
    try {
      location.setLongitude(longitude1);
    }
    catch(Exception e) {
        doubleRet = location.getLongitude();
        assertTrue( doubleRet == 10.0 );
        //System.err.println("Successfully caught Exception thrown:  " + e);
    }

    longitude1 = 181.1;
    try {
      location.setLongitude(longitude1);
    }
    catch(Exception e) {
        doubleRet = location.getLongitude();
        assertTrue( doubleRet == 10.0 );
        //System.err.println("Successfully caught Exception thrown:  " + e);
    }
  }

  private final static char TAB = '\t';
  public void testToString() {

    location.setDepth( 10 );
    location.setLatitude( 10 );
    location.setLongitude( 10 );
    String stringRet = location.toString();
    //System.out.println( "Printing out toString(): " + stringRet );

    String testStr = new String();
    testStr = "" + location.getLatitude() + TAB + location.getLongitude() +
        TAB + location.getDepth();
    assertEquals(stringRet, testStr);

  }

  public void testEquals() {

    location.setDepth( 10 );
    location.setLatitude( 10 );
    location.setLongitude( 10 );

    Location location1 = new Location();
    location1.setDepth( 10 );
    location1.setLatitude( 10 );
    location1.setLongitude( 10 );

    boolean booleanRet = location.equals(location1);
    assertTrue( booleanRet );

    location1.setDepth( 11 );
    booleanRet = location.equals(location1);
    assertTrue( !booleanRet );

    location1.setDepth( 10 );
    booleanRet = location.equals(location1);
    assertTrue( booleanRet );

    location1.setLatitude( 11 );
    booleanRet = location.equals(location1);
    assertTrue( !booleanRet );

    location1.setLatitude( 10 );
    booleanRet = location.equals(location1);
    assertTrue( booleanRet );

    location1.setLongitude( 11 );
    booleanRet = location.equals(location1);
    assertTrue( !booleanRet );

    location1.setLongitude( 10 );
    booleanRet = location.equals(location1);
    assertTrue( booleanRet );
  }

  public void testClone() {

    Object objectRet = location.copy();
    boolean booleanRet = location.equals(objectRet);
    assertTrue( booleanRet );

    Location location1 = (Location)objectRet;

    location1.setDepth( 11 );
    booleanRet = location.equals(location1);
    assertTrue( !booleanRet );

    location1.setDepth( 10 );
    booleanRet = location.equals(location1);
    assertTrue( booleanRet );

    location1.setLatitude( 11 );
    booleanRet = location.equals(location1);
    assertTrue( !booleanRet );

    location1.setLatitude( 10 );
    booleanRet = location.equals(location1);
    assertTrue( booleanRet );

    location1.setLongitude( 11 );
    booleanRet = location.equals(location1);
    assertTrue( !booleanRet );

    location1.setLongitude( 10 );
    booleanRet = location.equals(location1);
    assertTrue( booleanRet );
  }
}
