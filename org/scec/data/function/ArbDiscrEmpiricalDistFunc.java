package org.scec.data.function;

import org.scec.data.DataPoint2D;
import org.scec.exceptions.DataPoint2DException;

import java.util.*;
import java.io.Serializable;
import org.scec.util.*;
import org.scec.exceptions.*;
import org.scec.param.ParameterList;
import org.scec.data.*;

/**
 * <b>Title:</b> ArbDiscrEmpiricalDistFunc<p>
 *
 * <b>Description:</b>  This class is similar to ArbitraryDiscretizedFunction,
 * except that rather than replacing a point that has the same x value (or within
 * tolerance, which is required to be zero here), the y values are added together.
 * This is useful for making an empirical distribution where the y-values represent
 * the frequency of occurrence of each x value.  In this context, a nonzero tolerance
 * would not make sense because a values might fall into different points depending
 * on what order they're added.  Due to the numerical precision of floating point
 * arithmetic, the tolerance is really about 1e-16.<p>
 *
 * The getNormalizedCumDist() method enables one to get a normalized cumulative distribution.<p>
 * The getFractile(fraction) method gets the x-axis value for the specified fraction (does so by
 * creating a NormalizedCumDist each time, so this is not efficient if several fractiles are
 * desired). <p>
  *
 * @author Edward H. Field
 * @version 1.0
 */

public class ArbDiscrEmpiricalDistFunc extends ArbitrarilyDiscretizedFunc
                                        implements Serializable {

    /* Class name Debbuging variables */
    protected final static String C = "ArbDiscrEmpiricalDistFunc";

    /* Boolean debugging variable to switch on and off debug printouts */
    protected final static boolean D = true;

    /**
     * No-Arg Constructor.
     */
    public ArbDiscrEmpiricalDistFunc() { super.points = new EmpiricalDistributionTreeMap(); }


    /**
     * This method is over ridded to throw an exception because tolerance cannot be
     * changed from zero for this class.
     */
    public void setTolerance(double newTolerance) throws InvalidRangeException {

      throw new InvalidRangeException("Cannot change the tolerance for " + C + " (it must be zero)");
    }


    /**
     * This function returns a new copy of this list, including copies
     * of all the points. A shallow clone would only create a new DiscretizedFunc
     * instance, but would maintain a reference to the original points. <p>
     *
     * Since this is a clone, you can modify it without changing the original.
     * @return
     */
    public DiscretizedFuncAPI deepClone(){

        ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc(  );

        Iterator it = this.getPointsIterator();
        if( it != null ) {
            while(it.hasNext()) {
                function.set( (DataPoint2D)((DataPoint2D)it.next()).clone() );
            }
        }

        return function;

    }


    /**
     * This returns the x-axis value where the normalized cumulative distribution
     * equals the specified fraction.
     * @param fraction - a value between 0 and 1.
     * @return
     */
    public double getFractile(double fraction) {

      if(fraction < 0 || fraction > 1)
        throw new InvalidRangeException("fraction value must be between 0 and 1");

      ArbitrarilyDiscretizedFunc tempCumDist = getNormalizedCumDist();

      if(fraction < tempCumDist.getMinX())
        throw new InvalidRangeException("The chosen fraction is below the minimum value of the cumulative distribution");

      return tempCumDist.getFirstInterpolatedX(fraction);
    }


    /**
     * This returns an ArbitrarilyDiscretizedFunc representing the cumulative
     * distribution normalized (so that the last value is equal to one)
     * @return
     */
    public ArbitrarilyDiscretizedFunc getNormalizedCumDist() {
      ArbitrarilyDiscretizedFunc cumDist = new ArbitrarilyDiscretizedFunc(0.0);
      DataPoint2D dp;

      // get the total sum
      double totSum = 0;
      Iterator it = getPointsIterator();
      while (it.hasNext()) { totSum += ((DataPoint2D) it.next()).getY(); }

      double sum = 0;
      it = getPointsIterator();
      while (it.hasNext()) {
        dp = (DataPoint2D) it.next();
        sum += dp.getY();
        DataPoint2D dpNew = new DataPoint2D(dp.getX(),sum/totSum);
        cumDist.set(dpNew);
      }
      return cumDist;
    }




/*  temp main method to test and to investige numerical precision issues */
public static void main( String[] args ) {

  ArbDiscrEmpiricalDistFunc func = new ArbDiscrEmpiricalDistFunc();
  func.set(0.0,0);
  func.set(1.0,0);
  func.set(1.0,1);
  func.set(2.0,1);
  func.set(2.0,1);
  func.set(3.0,1);
  func.set(3.0,1);
  func.set(3.0,1);
  func.set(4.0,5);
  func.set(4.0,-1);
  func.set(5.0,5.0);
  func.set(5.0+1e-15,6.0);
  func.set(5.0+1e-16,7.0);

  System.out.println("func:");
  Iterator it = func.getPointsIterator();
  DataPoint2D point;
  while( it.hasNext()) {
    point = (DataPoint2D) it.next();
    System.out.println(point.getX()+"  "+point.getY());
  }

  System.out.println("\ncumFunc:");
  ArbitrarilyDiscretizedFunc cumFunc = func.getNormalizedCumDist();
  it = cumFunc.getPointsIterator();
  while( it.hasNext()) {
    point = (DataPoint2D) it.next();
    System.out.println(point.getX()+"  "+point.getY());
  }
/* */
  System.out.println("\nFractiles from cumFunc:");
  System.out.println("0.25: " + cumFunc.getFirstInterpolatedX(0.25));
  System.out.println("0.5: " + cumFunc.getFirstInterpolatedX(0.5));
  System.out.println("0.75: " + cumFunc.getFirstInterpolatedX(0.75));

  System.out.println("\nFractiles from method:");
  System.out.println("0.25: " + func.getFractile(0.25));
  System.out.println("0.5: " + func.getFractile(0.5));
  System.out.println("0.75: " + func.getFractile(0.75));
  System.out.println("0.0: " + func.getFractile(0.0));

}



    /*
    public void rebuild(){

        // make temporary storage
        ArrayList points = new ArrayList();

        // get all points
        Iterator it = getPointsIterator();
        if( it != null ) while(it.hasNext()) { points.add( (DataPoint2D)it.next() ); }

        // get all non-log points if any
        it = getNonLogPointsIterator();
        if( it != null ) while(it.hasNext()) { points.add( (DataPoint2D)it.next() ); }

        // clear permanent storage
        points.clear();
        nonPositivepoints.clear();

        // rebuild permanent storage
        it = points.listIterator();
        if( it != null ) while(it.hasNext()) { set( (DataPoint2D)it.next() ); }

        if( D ) System.out.println("rebuild: " + toDebugString());
        points = null;
    }

    public boolean isYLog() { return yLog; }
    public void setYLog(boolean yLog) {

        if( yLog != this.yLog ) {
            this.yLog = yLog;
            rebuild();
        }
    }

    public boolean isXLog() { return xLog; }
    public void setXLog(boolean xLog) {
        if( xLog != this.xLog ) {
            this.xLog = xLog;
            rebuild();
        }
    }

    */


}
