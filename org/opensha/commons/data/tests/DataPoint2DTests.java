package org.opensha.commons.data.tests;
import junit.framework.Assert;

import junit.framework.TestCase;

import org.opensha.commons.data.DataPoint2D;
import org.opensha.data.*;

/**
 * <b>Title:</b> DataPoint2DTestCase<p>
 *
 * <b>Description:</b> Class used by the JUnit testing harness to test the
 * DataPoint2D. This class was used to test using JUnit. For some reason
 * testEquals() fails in JUnit, but the main() test (hand coded testing) shows
 * that the DataPoint2D passes the tests. Need more exploring of JUnit. <P>
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
 * @author     Steven W. Rock
 * @created    February 20, 2002
 * @version    1.0
 */

public class DataPoint2DTests extends TestCase {

    /** First test DataPoint2D */
    public DataPoint2D d1;

    /** Second test DataPoint2D */
    public DataPoint2D d2;

    /** Third test DataPoint2D */
    public DataPoint2D d3;

    /** Fourth test DataPoint2D */
    public DataPoint2D d4 = null;


    /**
     *  Constructor for the DataPoint2DTestCase object.
     */
    public DataPoint2DTests(String name) {
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
    public void testEquals()
    {
        assertNotNull( d1 );
        assertNull( d4 );
        assertEquals( d1, d1 );
        assertEquals( d1, d2 );
        assertTrue( !(d1.equals( d3 )) );
        assertTrue(d1.equals(d2));
    }

}
