package org.scec.data.test;

import junit.framework.*;
import org.scec.data.*;

// FIX - Needs more comments

/**
 * <b>Title:</b> TestLocation<p>
 * <b>Description:>/b> JUnit tester for the Location object. Tests every
 * piece of functionality, included expected fail conditions.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class TestLocation extends TestCase {

    Location location = new Location();

  public TestLocation(String s) {
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

    Location loc1 =  new Location();  /** @todo fill in non-null value */;
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
    this.assertTrue( !booleanRet );

    loc1.setLatitude( 10 );
    loc1.setDepth( 20 );
    booleanRet = location.equalsLocation(loc1);
    this.assertTrue( !booleanRet );

    loc1.setDepth( 10 );
    booleanRet = location.equalsLocation(loc1);
    this.assertTrue( booleanRet );

  }

  public void testGetDepth() {
    double doubleRet = location.getDepth();
    assertTrue( doubleRet == 10.0 );
  }

  public void testGetLatitude() {
    double doubleRet = location.getLatitude();
    assertTrue( doubleRet == 10.0 );
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  }

  public void testGetLongitude() {
    double doubleRet = location.getLongitude();
    assertTrue( doubleRet == 10.0 );
  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
  }

  public void testSetDepth() {
    double depth1 =   15.0   /** @todo fill in non-null value */;
    location.setDepth(depth1);
    double doubleRet = location.getDepth();
    assertTrue( doubleRet == 15.0 );

    location.setDepth( 10.0 );
    doubleRet = location.getDepth();
    assertTrue( doubleRet == 10.0 );

  /** @todo:  Insert test code here.  Use assertEquals(), for example. */
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


    latitude1 = -95.1  /** @todo fill in non-null value */;
    try {
      location.setLatitude(latitude1);
    }
    catch(Exception e) {
        doubleRet = location.getLatitude();
        assertTrue( doubleRet == 10.0 );
        System.err.println("Successfully caught Exception thrown:  " + e);
    }



    latitude1= 95.1   /** @todo fill in non-null value */;
    try {
      location.setLatitude(latitude1);

    }
    catch(Exception e) {
        doubleRet = location.getLatitude();
        assertTrue( doubleRet == 10.0 );
        System.err.println("Successfully caught Exception thrown:  " + e);
    }



  }



  public void testSetLongitude() {
    double longitude1= 15.0 ;  /** @todo fill in non-null value */
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


    longitude1 = -181.1  /** @todo fill in non-null value */;
    try {
      location.setLongitude(longitude1);
    }
    catch(Exception e) {
        doubleRet = location.getLongitude();
        assertTrue( doubleRet == 10.0 );
        System.err.println("Successfully caught Exception thrown:  " + e);
    }



    longitude1 = 181.1   /** @todo fill in non-null value */;
    try {
      location.setLongitude(longitude1);

    }
    catch(Exception e) {
        doubleRet = location.getLongitude();
        assertTrue( doubleRet == 10.0 );
        System.err.println("Successfully caught Exception thrown:  " + e);
    }



  }


  public void testToString() {

    location.setDepth( 10 );
    location.setLatitude( 10 );
    location.setLongitude( 10 );
    String stringRet = location.toString();
    System.out.println( "Printing out toString(): " + stringRet );

    String testStr = "Location : latitude = 10.0 : longitude = 10.0 : depth = 10.0";
    this.assertEquals(stringRet, testStr);

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
    this.assertTrue( booleanRet );


    location1.setDepth( 11 );
    booleanRet = location.equals(location1);
    this.assertTrue( !booleanRet );

    location1.setDepth( 10 );
    booleanRet = location.equals(location1);
    this.assertTrue( booleanRet );



    location1.setLatitude( 11 );
    booleanRet = location.equals(location1);
    this.assertTrue( !booleanRet );

    location1.setLatitude( 10 );
    booleanRet = location.equals(location1);
    this.assertTrue( booleanRet );



    location1.setLongitude( 11 );
    booleanRet = location.equals(location1);
    this.assertTrue( !booleanRet );

    location1.setLongitude( 10 );
    booleanRet = location.equals(location1);
    this.assertTrue( booleanRet );

  }

  public void testClone() {

    Object objectRet = location.clone();
    boolean booleanRet = location.equals(objectRet);
    this.assertTrue( booleanRet );

    Location location1 = (Location)objectRet;


    location1.setDepth( 11 );
    booleanRet = location.equals(location1);
    this.assertTrue( !booleanRet );

    location1.setDepth( 10 );
    booleanRet = location.equals(location1);
    this.assertTrue( booleanRet );



    location1.setLatitude( 11 );
    booleanRet = location.equals(location1);
    this.assertTrue( !booleanRet );

    location1.setLatitude( 10 );
    booleanRet = location.equals(location1);
    this.assertTrue( booleanRet );



    location1.setLongitude( 11 );
    booleanRet = location.equals(location1);
    this.assertTrue( !booleanRet );

    location1.setLongitude( 10 );
    booleanRet = location.equals(location1);
    this.assertTrue( booleanRet );


  }

}
