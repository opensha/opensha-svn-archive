package org.scec.data.test;

import junit.framework.*;
import org.scec.data.*;

// FIX - Needs more comments

public class TestDataPoint2DTreeMap extends TestCase {

    DataPoint2DTreeMap map = new DataPoint2DTreeMap();
    double tolerance = 0.001;


    public TestDataPoint2DTreeMap(String s) { super(s); }

    protected void setUp() {
        map = new DataPoint2DTreeMap();
        map.setTolerance( tolerance );
    }

    protected void tearDown() {}

    public void testClear() {

        map.clear();
        map.put( new DataPoint2D( 1.0, 2.0));
        this.assertTrue( map.size() > 0 );

        map.clear();
        /** @todo:  Insert test code here.  Use assertEquals(), for example. */
        this.assertTrue( map.size() == 0 );
    }

    public void testPut() {
        DataPoint2D key1 =  new DataPoint2D( 1.0, 2.0 );
        map.put(key1);
        /** @todo:  Insert test code here.  Use assertEquals(), for example. */
        //this.assertTrue( objectRet instanceof DataPoint2D );
        //this.assertTrue(key1.equals( (DataPoint2D)objectRet ));
    }

    public void testGetTolerance() {
        double doubleRet = map.getTolerance();
        /** @todo:  Insert test code here.  Use assertEquals(), for example. */
        assertTrue( tolerance == doubleRet);
    }


    public void testGet() {

        map.clear();
        int i = 0, j = 0;
        for(  ;i< 20;  ){

            DataPoint2D point = new DataPoint2D( i, j );
            map.put( point );

            i++;
            j++;

        }

        i = 0;
        for(  ;i < 20; i++ ){

            DataPoint2D point = map.get(i);
            System.out.println("Point: " + point.toString());
            assertTrue( point.getX() == i );
            assertTrue( point.getY() == i );

        }

    }

    public void testGetMaxY() {
        double doubleRet = map.getMaxY();
        /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }

    public void testGetMinY() {
        double doubleRet = map.getMinY();
        /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }


    // public void testSetComparator() {
        // DataPoint2DComparatorAPI comparator1 =  null  /** @todo fill in non-null value */;
        // map = map.setComparator(comparator1);
        /** @todo:  Insert test code here.  Use assertEquals(), for example. */
    // }

    public void testSetTolerance() {
        double newTolerance1 =  tolerance  /** @todo fill in non-null value */;
        try {
            map.setTolerance(newTolerance1);
            /** @todo:  Insert test code here.  Use assertEquals(), for example. */
        }
        catch(Exception e) {
            System.err.println("Exception thrown:  "+e);
        }
    }
}
