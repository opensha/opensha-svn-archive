/* ======================================
* JFreeChart : a free Java chart library
* ======================================
*
* Project Info:  http://www.jfree.org/jfreechart/index.html
* Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
*
* (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
*
* This library is free software; you can redistribute it and/or modify it under the terms
* of the GNU Lesser General Public License as published by the Free Software Foundation;
* either version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
* Boston, MA 02111-1307, USA.
*
* --------------------
* LogarithmicAxis.java
* --------------------
* (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
*
* Original Author:  Michael Duffy / Eric Thomas;
* Contributor(s):   David Gilbert (for Object Refinery Limited);
*                   David M. O'Donnell;
*
* $Id$
*
* Changes
* -------
* 14-Mar-2002 : Version 1 contributed by Michael Duffy (DG);
* 19-Apr-2002 : drawVerticalString(...) is now drawRotatedString(...) in RefineryUtilities (DG);
* 23-Apr-2002 : Added a range property (DG);
* 15-May-2002 : Modified to be able to deal with negative and zero values (via new
*               'adjustedLog10()' method);  occurrences of "Math.log(10)" changed to "LOG10_VALUE";
*               changed 'intValue()' to 'longValue()' in 'refreshTicks()' to fix label-text value
*               out-of-range problem; removed 'draw()' method; added 'autoRangeMinimumSize' check;
*               added 'log10TickLabelsFlag' parameter flag and implementation (ET);
* 25-Jun-2002 : Removed redundant import (DG);
* 25-Jul-2002 : Changed order of parameters in ValueAxis constructor (DG);
* 16-Jul-2002 : Implemented support for plotting positive values arbitrarily
*               close to zero (added 'allowNegativesFlag' flag) (ET).
* 05-Sep-2002 : Updated constructor reflecting changes in the Axis class (DG);
* 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
* 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
* 22-Nov-2002 : Bug fixes from David M. O'Donnell (DG);
* 14-Jan-2003 : Changed autoRangeMinimumSize from Number --> double (DG);
* 20-Jan-2003 : Removed unnecessary constructors (DG);
* 26-Mar-2003 : Implemented Serializable (DG);
* 08-May-2003 : Fixed plotting of datasets with lower==upper bounds when
*               'minAutoRange' is very small; added 'strictValuesFlag'
*               and default functionality of throwing a runtime exception
*               if 'allowNegativesFlag' is false and any values are less
*               than or equal to zero; added 'expTickLabelsFlag' and
*               changed to use "1e#"-style tick labels by default
*               ("10^n"-style tick labels still supported via 'set'
*               method); improved generation of tick labels when range of
*               values is small; changed to use 'NumberFormat.getInstance()'
*               to create 'numberFormatterObj' (ET);
* 14-May-2003 : Merged HorizontalLogarithmicAxis and VerticalLogarithmicAxis (DG);
*
 */

package org.jfree.chart.axis;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.Font;
import java.util.Iterator;
import java.awt.geom.Line2D;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;


/**
 * A numerical axis that uses a logarithmic scale.
 *
 * @author Michael Duffy
 */
public class LogarithmicAxis extends NumberAxis {

  /** Useful constant for log(10). */
  public static final double LOG10_VALUE = Math.log(10.0);

  /** Smallest arbitrarily-close-to-zero value allowed. */
  public static final double SMALL_LOG_VALUE = 1e-100;

  /** Flag set true to allow negative values in data. */
  protected boolean allowNegativesFlag = false;

  /** Flag set true make axis throw exception if any values are
   * <= 0 and 'allowNegativesFlag' is false. */
  protected boolean strictValuesFlag = true;

  /** Number formatter for generating numeric strings. */
  protected final NumberFormat numberFormatterObj = NumberFormat.getInstance();

  /** Flag set true for "1e#"-style tick labels. */
  protected boolean expTickLabelsFlag = false;

  /** Flag set true for "10^n"-style tick labels. */
  protected boolean log10TickLabelsFlag = false;

  /** Flag set true for representing the tick labels as the super script of 10 */
  protected boolean log10TickLabelsInPowerFlag = true;

  /** Flag set true to show the  tick labels for minor axis */
  protected boolean minorAxisTickLabelFlag = true;

  /** Helper flag for log axis processing. */
  protected boolean smallLogFlag = false;

  /**
   * Creates a new axis.
   *
   * @param label  the axis label.
   */
  public LogarithmicAxis(String label) {
    super(label);
    setupNumberFmtObj();      //setup number formatter obj
  }

  /**
   * Sets the 'allowNegativesFlag' flag; true to allow negative values
   * in data, false to be able to plot positive values arbitrarily close to zero.
   *
   * @param flgVal  the new value of the flag.
   */
  public void setAllowNegativesFlag(boolean flgVal) {
    allowNegativesFlag = flgVal;
  }

  /**
   * Returns the 'allowNegativesFlag' flag; true to allow negative values
   * in data, false to be able to plot positive values arbitrarily close
   * to zero.
   *
   * @return the flag.
   */
  public boolean getAllowNegativesFlag() {
    return allowNegativesFlag;
  }

  /**
   * Sets the 'strictValuesFlag' flag; if true and 'allowNegativesFlag'
   * is false then this axis will throw a runtime exception if any of its
   * values are less than or equal to zero; if false then the axis will
   * adjust for values less than or equal to zero as needed.
   *
   * @param flgVal true for strict enforcement.
   */
  public void setStrictValuesFlag(boolean flgVal) {
    strictValuesFlag = flgVal;
  }

  /**
   * Returns the 'strictValuesFlag' flag; if true and 'allowNegativesFlag'
   * is false then this axis will throw a runtime exception if any of its
   * values are less than or equal to zero; if false then the axis will
   * adjust for values less than or equal to zero as needed.
   *
   * @return true if strict enforcement is enabled.
   */
  public boolean getStrictValuesFlag() {
    return strictValuesFlag;
  }

  /**
   * Sets the 'expTickLabelsFlag' flag to true for for "1e#"-style tick labels,
   * false for "10^n" and false for representing the power of 10 in superScript.
   *
   */
  public void setExpTickLabelsFlag() {
    expTickLabelsFlag = true;
    log10TickLabelsFlag = false;
    log10TickLabelsInPowerFlag = false;
    setupNumberFmtObj();             //setup number formatter obj
  }

  /**
   * Returns the 'expTickLabelsFlag' flag.
   *
   * @return  true for "1e#"-style tick labels, false for log10 or
   * regular numeric tick labels.
   */
  public boolean getExpTickLabelsFlag() {
    return expTickLabelsFlag;
  }

  /**
   * Sets the 'log10TickLabelsFlag' flag to true for for "10^n"-style tick labels,
   * false for "1e#"-style tick labels and false for representing the power of 10 in superScript.
   *
   */
  public void setLog10TickLabelsFlag() {
    log10TickLabelsFlag = true;
    expTickLabelsFlag = false;
    log10TickLabelsInPowerFlag = false;
  }


  /**
   * Returns the 'log10TickLabelsFlag' flag.
   *
   * @return true for "10^n"-style tick labels, false for "1e#"-style
   * or regular numeric tick labels.
   */
  public boolean getLog10TickLabelsFlag() {
    return log10TickLabelsFlag;
  }


  /**
   * Sets the 'log10TickLabelsInPowerFlag' flag to true for representing the power of 10 in superScript ,
   * false for "1e#"-style tick labels and false for "10^n"-style tick labels .
   *
   */
  public void setLog10TickLabelsInPowerFlag() {
    log10TickLabelsInPowerFlag = true;
    this.smallLogFlag = true;
    expTickLabelsFlag = false;
    log10TickLabelsFlag = false;
  }


  /**
   * Returns the 'log10TickLabelsInPowerFlag' flag.
   *
   * @return true for representing the power of 10 in superScript , false for "1e#"-style
   * or "10^n"-style tick labels.
   */
  public boolean getLog10TickLabelsInPowerFlag() {
    return log10TickLabelsInPowerFlag;
  }


  /**
   * Sets the minorAxisTickLabelFlag to true for showing the tick labels for
   * minor axis, else sets it false.
   * @param flag : sets the minorAxisTickLabel flag to true or false
   */
  public void setMinorAxisTickLabelFlag(boolean flag) {
    minorAxisTickLabelFlag = flag;
  }

  /**
   *
   * @returns true if minorAxis tick labels are to shown, false if they
   * are not to be shown.
   */
  public boolean getMinorAxisTickLabelFlag() {
    return minorAxisTickLabelFlag;
  }

  /**
   * Overridden version that calls original and then sets up flag for
   * log axis processing.
   *
   * @param range  the new range.
   */
  public void setRange(Range range) {
    super.setRange(range);      // call parent method
    setupSmallLogFlag();        // setup flag based on bounds values
  }

  /**
   * Sets up flag for log axis processing.  Set true if negative values
   * not allowed and the lower bound is between 0 and 10.
   */
  protected void setupSmallLogFlag() {
    // set flag true if negative values not allowed and the
    // lower bound is between 0 and 10:
    final double lowerVal = getRange().getLowerBound();
    if(!log10TickLabelsInPowerFlag)
      smallLogFlag = (!allowNegativesFlag && lowerVal < 10.0 && lowerVal > 0.0);
    else
      smallLogFlag = true;
  }

  /**
   * Sets up the number formatter object according to the
   * 'expTickLabelsFlag' flag.
   */
  protected void setupNumberFmtObj() {
    if (numberFormatterObj instanceof DecimalFormat) {
      //setup for "1e#"-style tick labels or regular
      // numeric tick labels, depending on flag:
      ((DecimalFormat) numberFormatterObj).applyPattern(expTickLabelsFlag ? "0E0" : "0.###");
    }
  }

  /**
   * Returns the log10 value, depending on if values between 0 and
   * 1 are being plotted.  If negative values are not allowed and
   * the lower bound is between 0 and 10 then a normal log is
   * returned; otherwise the returned value is adjusted if the
   * given value is less than 10.
   *
   * @param val the value.
   *
   * @return log<sub>10</sub>(val).
   */
  protected double switchedLog10(double val) {
    return smallLogFlag ? Math.log(val) / LOG10_VALUE : adjustedLog10(val);
  }

  /**
   * Returns an adjusted log10 value for graphing purposes.  The first
   * adjustment is that negative values are changed to positive during
   * the calculations, and then the answer is negated at the end.  The
   * second is that, for values less than 10, an increasingly large
   * (0 to 1) scaling factor is added such that at 0 the value is
   * adjusted to 1, resulting in a returned result of 0.
   *
   * @param val  value for which log10 should be calculated.
   *
   * @return an adjusted log<sub>10</sub>(val).
   */
  public double adjustedLog10(double val) {
    final boolean negFlag;
    if (negFlag = (val < 0.0)) {
      val = -val;                  // if negative then set flag and make positive
    }
    if (val < 10.0) {                // if < 10 then
      val += (10.0 - val) / 10;    //increase so 0 translates to 0
    }
    //return value; negate if original value was negative:
    return negFlag ? -(Math.log(val) / LOG10_VALUE) : (Math.log(val) / LOG10_VALUE);
  }

  /**
   * Returns the largest (closest to positive infinity) double value that is
   * not greater than the argument, is equal to a mathematical integer and
   * satisfying the condition that log base 10 of the value is an integer
   * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
   *
   * @param lower a double value below which a floor will be calcualted.
   *
   * @return 10<sup>N</sup> with N .. { 1 ... }
   */
  protected double computeLogFloor(double lower) {

    double logFloor;
    if (allowNegativesFlag) {
      //negative values are allowed
      if (lower > 10.0) {   //parameter value is > 10
        // The Math.log() function is based on e not 10.
        logFloor = Math.log(lower) / LOG10_VALUE;
        logFloor = Math.floor(logFloor);
        logFloor = Math.pow(10, logFloor);
      }
      else if (lower < -10.0) {   //parameter value is < -10
        //calculate log using positive value:
        logFloor = Math.log(-lower) / LOG10_VALUE;
        //calculate floor using negative value:
        logFloor = Math.floor(-logFloor);
        //calculate power using positive value; then negate
        logFloor = -Math.pow(10, -logFloor);
      }
      else {
        //parameter value is -10 > val < 10
        logFloor = Math.floor(lower);   //use as-is
      }
    }
    else {
      //negative values not allowed
      if (lower > 0.0) {   //parameter value is > 0
        // The Math.log() function is based on e not 10.
        logFloor = Math.log(lower) / LOG10_VALUE;
        logFloor = Math.floor(logFloor);
        logFloor = Math.pow(10, logFloor);
      }
      else {
        //parameter value is <= 0
        logFloor = Math.floor(lower);   //use as-is
      }
    }
    return logFloor;
  }

  /**
   * Returns the smallest (closest to negative infinity) double value that is
   * not less than the argument, is equal to a mathematical integer and
   * satisfying the condition that log base 10 of the value is an integer
   * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
   *
   * @param upper a double value above which a ceiling will be calcualted.
   *
   * @return 10<sup>N</sup> with N .. { 1 ... }
   */
  protected double computeLogCeil(double upper) {

    double logCeil;
    if (allowNegativesFlag) {
      //negative values are allowed
      if (upper > 10.0) {
        //parameter value is > 10
        // The Math.log() function is based on e not 10.
        logCeil = Math.log(upper) / LOG10_VALUE;
        logCeil = Math.ceil(logCeil);
        logCeil = Math.pow(10, logCeil);
      }
      else if (upper < -10.0) {
        //parameter value is < -10
        //calculate log using positive value:
        logCeil = Math.log(-upper) / LOG10_VALUE;
        //calculate ceil using negative value:
        logCeil = Math.ceil(-logCeil);
        //calculate power using positive value; then negate
        logCeil = -Math.pow(10, -logCeil);
      }
      else {
        //parameter value is -10 > val < 10
        logCeil = Math.ceil(upper);     //use as-is
      }
    }
    else {
      //negative values not allowed
      if (upper > 0.0) {
        //parameter value is > 0
        // The Math.log() function is based on e not 10.
        logCeil = Math.log(upper) / LOG10_VALUE;
        logCeil = Math.ceil(logCeil);
        logCeil = Math.pow(10, logCeil);
      }
      else {
        //parameter value is <= 0
        logCeil = Math.ceil(upper);     //use as-is
      }
    }
    return logCeil;
  }

  /**
   * Rescales the axis to ensure that all data is visible.
   */
  public void autoAdjustRange() {

    Plot plot = getPlot();
    if (plot == null) {
      return;  // no plot, no data.
    }

    if (plot instanceof ValueAxisPlot) {
      ValueAxisPlot vap = (ValueAxisPlot) plot;

      double lower;
      Range r = vap.getDataRange(this);
      if (r == null) {
        //no real data present
        r = new Range(DEFAULT_LOWER_BOUND, DEFAULT_UPPER_BOUND);
        lower = r.getLowerBound();    //get lower bound value
      }
      else {
        //actual data is present
        lower = r.getLowerBound();    //get lower bound value
        if (strictValuesFlag && !allowNegativesFlag && lower <= 0.0)
            { //strict flag set, allow-negatives not set and values <= 0
          throw new RuntimeException("Values less than or equal to "
                                     + "zero not allowed with logarithmic axis");
        }
      }

      //change to log version of lowest value to make range
      // begin at a 10^n value:
      lower = computeLogFloor(lower);

      if (!allowNegativesFlag && lower >= 0.0 && lower < SMALL_LOG_VALUE) {
        //negatives not allowed and lower range bound is zero
        lower = r.getLowerBound();    //use data range bound instead
      }

      double upper = r.getUpperBound();
      if (!allowNegativesFlag && upper < 1.0 && upper > 0.0 && lower > 0.0) {
        //negatives not allowed and upper bound between 0 & 1
        //round up to nearest significant digit for bound:
        //get negative exponent:
        double expVal = Math.log(upper) / LOG10_VALUE;
        expVal = Math.ceil(-expVal + 0.001); //get positive exponent
        expVal = Math.pow(10, expVal);      //create multiplier value
        //multiply, round up, and divide for bound value:
        upper = (expVal > 0.0) ? Math.ceil(upper * expVal) / expVal : Math.ceil(upper);
      }
      else {
        //negatives allowed or upper bound not between 0 & 1
        upper = computeLogCeil(upper);  //use nearest log value
      }
      // ensure the autorange is at least <minRange> in size...
      double minRange = getAutoRangeMinimumSize();
      if (upper - lower < minRange) {
        upper = (upper + lower + minRange) / 2;
        lower = (upper + lower - minRange) / 2;
        //if autorange still below minimum then adjust by 1%
        // (can be needed when minRange is very small):
        if (upper - lower < minRange) {
          final double absUpper = Math.abs(upper);
          //need to account for case where upper==0.0
          final double adjVal = (absUpper > SMALL_LOG_VALUE) ? absUpper / 100.0 : 0.01;
          upper = (upper + lower + adjVal) / 2;
          lower = (upper + lower - adjVal) / 2;
        }
      }

      setRange(new Range(lower, upper), false, false);

      setupSmallLogFlag();       //setup flag based on bounds values
    }
  }

  /**
   * Converts a data value to a coordinate in Java2D space, assuming that
   * the axis runs along one edge of the specified plotArea.
   * Note that it is possible for the coordinate to fall outside the
   * plotArea.
   *
   * @param value  the data value.
   * @param plotArea  the area for plotting the data.
   * @param edge  the axis location.
   *
   * @return the Java2D coordinate.
   */
  public double translateValueToJava2D(double value, Rectangle2D plotArea,
                                       RectangleEdge edge) {

    Range range = getRange();
    double axisMin = switchedLog10(range.getLowerBound());
    double axisMax = switchedLog10(range.getUpperBound());

    double min = 0.0;
    double max = 0.0;
    if (RectangleEdge.isTopOrBottom(edge)) {
      min = plotArea.getMinX();
      max = plotArea.getMaxX();
    }
    else if (RectangleEdge.isLeftOrRight(edge)) {
      min = plotArea.getMaxY();
      max = plotArea.getMinY();
    }

    value = switchedLog10(value);

    if (isInverted()) {
      return max - (((value - axisMin) / (axisMax - axisMin)) * (max - min));
    }
    else {
      return min + (((value - axisMin) / (axisMax - axisMin)) * (max - min));
    }

  }

  /**
   * Converts a coordinate in Java2D space to the corresponding data
   * value, assuming that the axis runs along one edge of the specified plotArea.
   *
   * @param java2DValue  the coordinate in Java2D space.
   * @param plotArea  the area in which the data is plotted.
   * @param edge  the axis location.
   *
   * @return the data value.
   */
  public double translateJava2DtoValue(float java2DValue, Rectangle2D plotArea,
                                       RectangleEdge edge) {

    Range range = getRange();
    double axisMin = switchedLog10(range.getLowerBound());
    double axisMax = switchedLog10(range.getUpperBound());

    double plotMin = 0.0;
    double plotMax = 0.0;
    if (RectangleEdge.isTopOrBottom(edge)) {
      plotMin = plotArea.getX();
      plotMax = plotArea.getMaxX();
    }
    else if (RectangleEdge.isLeftOrRight(edge)) {
      plotMin = plotArea.getMaxY();
      plotMax = plotArea.getMinY();
    }

    if (isInverted()) {
      return Math.pow(10, axisMax
                      - ((java2DValue - plotMin) / (plotMax - plotMin)) * (axisMax - axisMin));
    }
    else {
      return Math.pow(10, axisMin
                      + ((java2DValue - plotMin) / (plotMax - plotMin)) * (axisMax - axisMin));
    }
  }

  /**
   * Calculates the positions of the tick labels for the axis, storing the results in the
   * tick label list (ready for drawing).
   *
   * @param g2  the graphics device.
   * @param cursor  the cursor.
   * @param drawArea  the area in which the plot and the axes should be drawn.
   * @param dataArea  the area in which the plot should be drawn.
   * @param edge  the location of the axis.
   */
  public void refreshTicks(Graphics2D g2, double cursor,
                           Rectangle2D drawArea, Rectangle2D dataArea,
                           RectangleEdge edge) {

    if (RectangleEdge.isTopOrBottom(edge)) {
      refreshTicksHorizontal(g2, drawArea, dataArea, edge);
    }
    else if (RectangleEdge.isLeftOrRight(edge)) {
      refreshTicksVertical(g2, drawArea, dataArea, edge);
    }

  }

  /**
   * Calculates the positions of the tick labels for the axis, storing the results in the
   * tick label list (ready for drawing).
   *
   * @param g2  the graphics device.
   * @param drawArea  the area in which the plot and the axes should be drawn.
   * @param dataArea  the area in which the plot should be drawn.
   * @param edge  the location of the axis.
   */
  public void refreshTicksHorizontal(Graphics2D g2,
                                     Rectangle2D plotArea, Rectangle2D dataArea,
                                     RectangleEdge edge) {

    getTicks().clear();
    double x0 =  0;
    //get lower bound value:
    double lowerBoundVal = getRange().getLowerBound();
    //if small log values and lower bound value too small
    // then set to a small value (don't allow <= 0):
    if (smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
      lowerBoundVal = SMALL_LOG_VALUE;
    }
    //get upper bound value
    final double upperBoundVal = getRange().getUpperBound();

    //get log10 version of lower bound and round to integer:
    int iBegCount = (int) StrictMath.floor(switchedLog10(lowerBoundVal));
    //get log10 version of upper bound and round to integer:
    final int iEndCount = (int) StrictMath.ceil(switchedLog10(upperBoundVal));

    if (iBegCount == iEndCount && iBegCount > 0 && Math.pow(10, iBegCount) > lowerBoundVal) {
      //only 1 power of 10 value, it's > 0 and its resulting
      // tick value will be larger than lower bound of data
      --iBegCount;       //decrement to generate more ticks
    }

    double tickVal;
    String tickLabel;
    boolean zeroTickFlag = false;

    //setRange(Math.pow(10,iBegCount),Math.pow(10,iEndCount));
    boolean superscript=false;

    // whether you want to show in superscript form or not

    if(iEndCount - iBegCount >= 4)
      superscript=true;
    if(getRange().getLowerBound()<0.001 || getRange().getUpperBound()>1000.0)
      superscript=true;


    for (int i = iBegCount; i <= iEndCount; i++) {
      //for each tick with a label to be displayed
      int jEndCount = 9;
      if (i == iEndCount) {
        jEndCount = 1;
      }

      for (int j = 0; j < jEndCount; j++) {
        //for each tick to be displayed
        if (smallLogFlag) {
          //small log values in use
          tickVal = Double.parseDouble("1e"+i) * (1 + j);
          //tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
          if (j == 0) {
            //checks to if tick Labels to be represented in the form of superscript of 10.
            if(log10TickLabelsInPowerFlag){
              if(superscript)
                //if flag is true
                tickLabel ="10E" +i; //create a "10E" type label, "E" would be trimmed from the tick
                                    //label to represent if the form of superscript of 10.

              else //value does not require the superscript representation
                tickLabel = ""+Double.parseDouble((j+1)+"e"+i);
            }
            else if (log10TickLabelsFlag) {
              //if flag then
              tickLabel = "10^" + i;      //create "log10"-type label
            }
            else {    //not "log10"-type label
              if (expTickLabelsFlag) {
                //if flag then
                tickLabel = "1e" + i;   //create "1e#"-type label
              }
              else {    //not "1e#"-type label
                if (i >= 0) {   //if positive exponent then make integer
                  tickLabel =  Long.toString((long) Math.rint(tickVal));
                }
                else {
                  //negative exponent; create fractional value
                  //set exact number of fractional digits to be shown:
                  numberFormatterObj.setMaximumFractionDigits(-i);
                  //create tick label:
                  tickLabel = numberFormatterObj.format(tickVal);
                }
              }
            }
          }
          else {   //not first tick to be displayed
            if(log10TickLabelsInPowerFlag)
              tickLabel = ""+(j+1);     //no tick label
            else
              tickLabel = "";
          }
        }
        else { //not small log values in use; allow for values <= 0
          if (zeroTickFlag) {      //if did zero tick last iter then
            --j;
          }               //decrement to do 1.0 tick now
          tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                    : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
          if (j == 0) {  //first tick of group
            if (!zeroTickFlag) {     //did not do zero tick last iteration
              if (i > iBegCount && i < iEndCount
                  && Math.abs(tickVal - 1.0) < 0.0001) {
                //not first or last tick on graph and value is 1.0
                tickVal = 0.0;        //change value to 0.0
                zeroTickFlag = true;  //indicate zero tick
                tickLabel = "0";      //create label for tick
              }
              else {
                //first or last tick on graph or value is 1.0
                //create label for tick:
                if (log10TickLabelsFlag) {
                  //create "log10"-type label
                  tickLabel = (((i < 0) ? "-" : "") + "10^" + Math.abs(i));
                }
                else {
                  if (expTickLabelsFlag) {
                    //create "1e#"-type label
                    tickLabel = (((i < 0) ? "-" : "") + "1e" + Math.abs(i));
                  }
                  else {
                    //create regular numeric label
                    tickLabel = Long.toString((long) Math.rint(tickVal));
                  }
                }
              }
            }
            else {     // did zero tick last iteration
              tickLabel = "";         //no label
              zeroTickFlag = false;   //clear flag
            }
          }
          else {       // not first tick of group
            tickLabel = "";           //no label
            zeroTickFlag = false;     //make sure flag cleared
          }
        }

        if (tickVal > upperBoundVal) {
          return;     //if past highest data value then exit method
        }

        if (tickVal >= lowerBoundVal - SMALL_LOG_VALUE) {
          //tick value not below lowest data value
          double xx = translateValueToJava2D(tickVal, dataArea, edge);
          Rectangle2D tickLabelBounds = getTickLabelFont().getStringBounds(
              tickLabel, g2.getFontRenderContext());
          float x = 0.0f;
          float y = 0.0f;
          Insets tickLabelInsets = getTickLabelInsets();
          if (isVerticalTickLabels()) {
            x = (float) (xx + tickLabelBounds.getHeight() / 2);
            if (edge == RectangleEdge.TOP) {
              y = (float) (dataArea.getMinY() - tickLabelInsets.bottom
                           - tickLabelBounds.getWidth());
            }
            else {
              y = (float) (dataArea.getMaxY() + tickLabelInsets.top
                           + tickLabelBounds.getWidth());
            }
          }
          else {
            x = (float) (xx - tickLabelBounds.getWidth() / 2);
            if (edge == RectangleEdge.TOP) {
              y = (float) (dataArea.getMinY() - tickLabelInsets.bottom);
            }
            else {
              y = (float) (dataArea.getMaxY() + tickLabelInsets.top
                           + tickLabelBounds.getHeight());
            }
          }
          if(this.log10TickLabelsInPowerFlag){
            //removing the minor labelling, if the ticks overlap.
            /* also if the difference in the powers of the smallest major axis
             * and largest major axis is larger than 3 then don't label the minor axis
             **/
            if((x<x0 || (iEndCount-iBegCount>3)) && j!=0)
              tickLabel="";
            else{
              //removing the previous minor tickLabels if the major axis overlaps any tickLabels
              if(j==0){
                int size = getTicks().size();
                --size;
                while(x<=x0 && size>0){
                  //only remove the previous ticklabel if that has been labelled.
                  Tick tempTick = ((Tick)getTicks().get(size));
                  if(!tempTick.getText().equals(""))
                    removePreviousTick();
                  --size;
                  x0 =  tempTick.getX()+3;
                }
              }
              x0 = x + tickLabelBounds.getWidth() +3;
            }
          }
          Tick tick = new Tick(new Double(tickVal), tickLabel, x, y);
          getTicks().add(tick);
        }
      }
    }
  }

  /**
   * Calculates the positions of the tick labels for the axis, storing the
   * results in the tick label list (ready for drawing).
   *
   * @param g2  the graphics device.
   * @param plotArea  the area in which the plot and the axes should be drawn.
   * @param dataArea  the area in which the plot should be drawn.
   * @param edge  the axis location.
   */
  public void refreshTicksVertical(Graphics2D g2,
                                   Rectangle2D plotArea, Rectangle2D dataArea,
                                   RectangleEdge edge) {

    getTicks().clear();
    double y0 =0;
    //get lower bound value:
    double lowerBoundVal = getRange().getLowerBound();
    //if small log values and lower bound value too small
    // then set to a small value (don't allow <= 0):
    if (smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
      lowerBoundVal = SMALL_LOG_VALUE;
    }
    //get upper bound value
    final double upperBoundVal = getRange().getUpperBound();

    //get log10 version of lower bound and round to integer:
    int iBegCount = (int) StrictMath.floor(switchedLog10(lowerBoundVal));
    //get log10 version of upper bound and round to integer:
    final int iEndCount = (int) StrictMath.ceil(switchedLog10(upperBoundVal));

    if (iBegCount == iEndCount && iBegCount > 0 && Math.pow(10, iBegCount) > lowerBoundVal) {
      //only 1 power of 10 value, it's > 0 and its resulting
      // tick value will be larger than lower bound of data
      --iBegCount;       //decrement to generate more ticks
    }

    double tickVal;
    String tickLabel;
    boolean zeroTickFlag = false;
    //setRange(Math.pow(10,iBegCount),Math.pow(10,iEndCount));

    boolean superscript=false;

    // whether you want to show in superscript form or not

    if(iEndCount - iBegCount >= 4)
      superscript=true;
    if(getRange().getLowerBound()<0.001 || getRange().getUpperBound()>1000.0)
      superscript=true;

    for (int i = iBegCount; i <= iEndCount; i++) {
      //for each tick with a label to be displayed
      int jEndCount = 9;
      if (i == iEndCount) {
        jEndCount = 1;
      }

      for (int j = 0; j < jEndCount; j++) {
        //for each tick to be displayed
        if (smallLogFlag) {
          //small log values in use
          tickVal = Double.parseDouble("1e"+i) * (1 + j);
          //tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
          if (j == 0) {

            //checks to if tick Labels to be represented in the form of superscript of 10.
            if(log10TickLabelsInPowerFlag){
              if(superscript)
                //if flag is true
                tickLabel ="10E" +i; //create a "10E" type label, "E" would be trimmed from the tick
              //label to represent if the form of superscript of 10.

              else //value does not require the superscript representation
                tickLabel = ""+Double.parseDouble((j+1)+"e"+i);
            }
            //first tick of group; create label text
            else if (log10TickLabelsFlag) {
              //if flag then
              tickLabel = "10^" + i;      //create "log10"-type label
            }
            else {    //not "log10"-type label
              if (expTickLabelsFlag) {
                //if flag then
                tickLabel = "1e" + i;   //create "1e#"-type label
              }
              else {    //not "1e#"-type label
                if (i >= 0) {   //if positive exponent then make integer
                  tickLabel =  Long.toString((long) Math.rint(tickVal));
                }
                else {
                  //negative exponent; create fractional value
                  //set exact number of fractional digits to be shown:
                  numberFormatterObj.setMaximumFractionDigits(-i);
                  //create tick label:
                  tickLabel = numberFormatterObj.format(tickVal);
                }
              }
            }
          }
          else {   //not first tick to be displayed
            if(log10TickLabelsInPowerFlag)
              tickLabel = ""+(j+1);     //no tick label
            else
              tickLabel = "";
          }
        }
        else { //not small log values in use; allow for values <= 0
          if (zeroTickFlag) {      //if did zero tick last iter then
            --j;
          }               //decrement to do 1.0 tick now
          System.out.println("Not Small Log Flag");
          tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                    : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
          if (j == 0) {  //first tick of group
            if (!zeroTickFlag) {     //did not do zero tick last iteration
              if (i > iBegCount && i < iEndCount
                  && Math.abs(tickVal - 1.0) < 0.0001) {
                //not first or last tick on graph and value is 1.0
                tickVal = 0.0;        //change value to 0.0
                zeroTickFlag = true;  //indicate zero tick
                tickLabel = "0";      //create label for tick
              }
              else {
                //first or last tick on graph or value is 1.0
                //create label for tick:
                if (log10TickLabelsFlag) {
                  //create "log10"-type label
                  tickLabel = (((i < 0) ? "-" : "") + "10^" + Math.abs(i));
                }
                else {
                  if (expTickLabelsFlag) {
                    //create "1e#"-type label
                    tickLabel = (((i < 0) ? "-" : "") + "1e" + Math.abs(i));
                  }
                  else {
                    //create regular numeric label
                    tickLabel = Long.toString((long) Math.rint(tickVal));
                  }
                }
              }
            }
            else {     // did zero tick last iteration
              tickLabel = "";         //no label
              zeroTickFlag = false;   //clear flag
            }
          }
          else {       // not first tick of group
            tickLabel = "";           //no label
            zeroTickFlag = false;     //make sure flag cleared
          }
        }

        if (tickVal > upperBoundVal) {
          return;     //if past highest data value then exit method
        }

        if (tickVal >= lowerBoundVal - SMALL_LOG_VALUE) {
          //tick value not below lowest data value
          //get Y-position for tick:
          double yy = translateValueToJava2D(tickVal, dataArea, edge);
          //get bounds for tick label:
          Rectangle2D tickLabelBounds
          = getTickLabelFont().getStringBounds(tickLabel, g2.getFontRenderContext());
          //get X-position for tick label:
          float x;
          if (edge == RectangleEdge.LEFT) {
            x = (float) (dataArea.getX()
                         - tickLabelBounds.getWidth() - getTickLabelInsets().right);
          }
          else {
            x = (float) (dataArea.getMaxX() + getTickLabelInsets().left);
          }

          //get Y-position for tick label:
          float y = (float) (yy + (tickLabelBounds.getHeight() / 3));
          if(this.log10TickLabelsInPowerFlag){
            //removing the minor labelling, if the ticks overlap.
            /* also if the difference in the powers of the smallest major axis
            * and largest major axis is larger than 3 then don't label the minor axis
            **/
            if((y>y0 || (iEndCount-iBegCount>3)) && j!=0)
              tickLabel="";
            else{
              //removing the previous minor tickLabels if the major axis overlaps any tickLabels
              if(j==0){
                int size = getTicks().size();
                --size;
                while(y>=y0 && size>0){
                  //only remove the previous ticklabel if that has been labelled.
                  Tick tempTick = ((Tick)getTicks().get(size));
                  if(!tempTick.getText().equals(""))
                    removePreviousTick();
                  --size;
                  y0 =  tempTick.getY()-3;
                }
              }
              y0 = y - tickLabelBounds.getWidth() -3;
            }
          }
          //create tick object and add to list:
          getTicks().add(new Tick(new Double(tickVal), tickLabel, x, y));
        }
      }
    }
  }



  /**
   * removes the previous nine so that powers of 10 can be displayed
   * It sees whether there is 9 at previous position which is overlapping
   * If that is so, then set the text of previous text to be ""
   */

  private void removePreviousTick() {
    int size = this.getTicks().size();
    for(int i=size-1;i>0;--i) {
      Tick tick = (Tick)this.getTicks().get(i);
      if(tick.getText().trim().equalsIgnoreCase("")) continue;
      //System.out.println("Removing tickVal:"+tick.getNumericalValue());
      this.getTicks().remove(i);
      this.getTicks().add(new Tick(new Double(tick.getNumericalValue()),"",tick.getX(),tick.getY()));
      return;
    }
  }


  /**
   * Draws the axis line, tick marks and tick mark labels.
   *
   * @param g2  the graphics device.
   * @param cursor  the cursor.
   * @param plotArea  the plot area.
   * @param dataArea  the data area.
   * @param edge  the edge that the axis is aligned with.
   *
   * @return The width or height used to draw the axis.
   */
  protected double drawTickMarksAndLabels(Graphics2D g2, double cursor,
      Rectangle2D plotArea,
      Rectangle2D dataArea, RectangleEdge edge) {

    if (isAxisLineVisible()) {
      drawAxisLine(g2, cursor, dataArea, edge);
    }
    double ol = getTickMarkOutsideLength();
    double il = getTickMarkInsideLength();

    refreshTicks(g2, cursor, plotArea, dataArea, edge);


    g2.setFont(getTickLabelFont());
    Iterator iterator = getTicks().iterator();
    while (iterator.hasNext()) {
      Tick tick = (Tick) iterator.next();
      float xx = (float) translateValueToJava2D(tick.getNumericalValue(), dataArea, edge);

      double val=1;
      int eIndex =-1;
      if(this.log10TickLabelsInPowerFlag)
        eIndex =tick.getText().indexOf("E");
      // check whether this is minor axis. for minor axis we save,2-9 in label

      if(!tick.getText().trim().equalsIgnoreCase("") && eIndex==-1)
        val = Double.parseDouble(tick.getText());
      //double logval=Math.log(tick.getNumericalValue())/LOG10_VALUE;
      //xx = (float)this.translateValueToJava2D(logval, plotArea);
      if(eIndex!=-1) // for major axis
        g2.setFont(this.getTickLabelFont());
      else  // show minor axis in smaller font
        g2.setFont(new Font(this.getTickLabelFont().getName(),this.getTickLabelFont().getStyle(),this.getTickLabelFont().getSize()-1));

      if (isTickLabelsVisible()) {
        g2.setPaint(getTickLabelPaint());
        if (isVerticalTickLabels()) {
          RefineryUtilities.drawRotatedString(tick.getText(), g2,
              tick.getX(), tick.getY(), -Math.PI / 2);
        }
        else {
          if(eIndex==-1)
            g2.drawString(tick.getText(), tick.getX(), tick.getY());
          else {
            g2.drawString("10", tick.getX(), tick.getY());
            g2.setFont(new Font(this.getTickLabelFont().getName(),this.getTickLabelFont().getStyle(),this.getTickLabelFont().getSize()-1));
            g2.drawString(tick.getText().substring(eIndex+1),tick.getX()+16,tick.getY()-6);
          }
        }
      }

      if (isTickMarksVisible()) {
        Line2D mark = null;
        g2.setStroke(getTickMarkStroke());
        g2.setPaint(getTickMarkPaint());
        if (edge == RectangleEdge.LEFT) {
          mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
        }
        else if (edge == RectangleEdge.RIGHT) {
          mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
        }
        else if (edge == RectangleEdge.TOP) {
          mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
        }
        else if (edge == RectangleEdge.BOTTOM) {
          mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
        }
        g2.draw(mark);
      }
    }
    return this.reservedForTickLabels;

  }





  /**
   * Converts the given value to a tick label string.
   *
   * @param val the value to convert.
   * @param forceFmtFlag true to force the number-formatter object
   * to be used.
   *
   * @return The tick label string.
   */
  protected String makeTickLabel(double val, boolean forceFmtFlag) {
    if (expTickLabelsFlag || forceFmtFlag) {
      //using exponents or force-formatter flag is set
      // (convert 'E' to lower-case 'e'):
      return numberFormatterObj.format(val).toLowerCase();
    }
    return getTickUnit().valueToString(val);
  }

  /**
   * Converts the given value to a tick label string.
   * @param val the value to convert.
   *
   * @return The tick label string.
   */
  protected String makeTickLabel(double val) {
    return makeTickLabel(val, false);
  }

}