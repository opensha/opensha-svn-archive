package org.scec.data.test;
import junit.framework.Assert;

import junit.framework.TestCase;
import org.scec.data.*;

/**
 *  <b>Title:</b> DataPoint2DTestCase<br>
 *  <b>Description:</b> Class used by the JUnit testing harness to test the
 *  DataPoint2D. This class was used to test using JUnit. For some reason
 *  testEquals() fails in JUnit, but the main() test (hand coded testing) shows
 *  that the DataPoint2D passes the tests. Need more exploring of JUnit. <P>
 *
 *  JUnit has gained many supporters, specifically used in ANT which is a java
 *  based tool that performs the same function as the make command in unix. ANT
 *  is developed under Apache.<p>
 *
 *  Any function that begins with test will be executed by JUnit
 *
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class DataPoint2DTestCase extends TestCase {

    /**
     *  First test DataPoint2D
     */
    public DataPoint2D d1;
    /**
     *  Second test DataPoint2D
     */
    public DataPoint2D d2;
    /**
     *  Third test DataPoint2D
     */
    public DataPoint2D d3;
    /**
     *  Fourth test DataPoint2D
     */
    public DataPoint2D d4 = null;


    /**
     *  Constructor for the DataPoint2DTestCase object
     *
     * @param  name  Description of the Parameter
     */
    public DataPoint2DTestCase( String name ) {
        super( name );
    }


    /**
     *  The JUnit setup method
     */
    protected void setUp() {
        d1 = new DataPoint2D( 12.2,  11.3  );
        d3 = new DataPoint2D( 120.2 ,  111.3  );
        d2 = new DataPoint2D( 12.2, 11.3  );

    }


    /**
     *  A unit test for JUnit
     */
    public void testEquals() {

        Assert.assertNotNull( d1 );
        Assert.assertNull( d4 );

        Assert.assertEquals( d1, d1 );
        Assert.assertEquals( d1, d2 );

        Assert.assertTrue( d1.equals( d3 ) );
    }


    /**
     * Command line test
     *
     * @param  args  Command line arguments
     */
    public static void main( String args[] ) {

        DataPoint2DTestCase test = new DataPoint2DTestCase( "OK" );

        test.setUp();

        if ( test.d1.equals( test.d1 ) ) {
            System.out.println( "Test 1 passed" );
        } else {
            System.out.println( "Test 1 failed" );
        }

        if ( test.d1.equals( test.d2 ) ) {
            System.out.println( "Test 2 passed" );
        } else {
            System.out.println( "Test 2 failed" );
        }

        if ( test.d1.equals( test.d3 ) ) {
            System.out.println( "Test 3 passed" );
        } else {
            System.out.println( "Test 3 failed" );
        }

        Assert.assertEquals( test.d1, test.d2 );

    }

}
