/* =======================================
 * JFreeChart : a Java Chart Class Library
 * =======================================
 *
 * Project Info:  http://www.object-refinery.com/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
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
 * ----------------------------
 * VerticalLogarithmicAxis.java
 * ----------------------------
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
 *
 * Original Author:  Michael Duffy;
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *                   Eric Thomas;
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
 *
 */

package com.jrefinery.chart;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.awt.font.FontRenderContext;
import com.jrefinery.chart.event.AxisChangeEvent;
import com.jrefinery.data.Range;
import com.jrefinery.ui.RefineryUtilities;

/**
 * A logartihmic value axis, for values displayed vertically.
 */




public class VerticalLogarithmicAxis extends NumberAxis implements VerticalAxis {

     /** A flag indicating whether or not the axis label is drawn vertically. */
    protected boolean verticalLabel;
    public static final double LOG10_VALUE = Math.log(10);

    private int counter=0;
    /**
     * Constructs a vertical number axis, using default values where necessary.
     */
    public VerticalLogarithmicAxis() { this(null); }

    /**
     * Constructs a vertical number axis, using default values where necessary.
     * @param label The axis label (null permitted).
     */
    public VerticalLogarithmicAxis(String label) {

	this(label,
             Axis.DEFAULT_AXIS_LABEL_FONT,
             ValueAxis.DEFAULT_MINIMUM_AXIS_VALUE,
             ValueAxis.DEFAULT_MAXIMUM_AXIS_VALUE);

        this.autoRange = true;

    }

    /**
     * Constructs a vertical number axis.
     * @param label The axis label (null permitted).
     * @param labelFont The font for displaying the axis label.
     * @param minimumAxisValue The lowest value shown on the axis.
     * @param maximumAxisValue The highest value shown on the axis.
     */
    public VerticalLogarithmicAxis(String label, Font labelFont,
			      double minimumAxisValue, double maximumAxisValue) {

	this(label,
             labelFont,
             Axis.DEFAULT_AXIS_LABEL_PAINT,
             Axis.DEFAULT_AXIS_LABEL_INSETS,
             true, // vertical axis label
             true, // tick labels visible
             Axis.DEFAULT_TICK_LABEL_FONT,
             Axis.DEFAULT_TICK_LABEL_PAINT,
             Axis.DEFAULT_TICK_LABEL_INSETS,
	     true, // tick marks visible
             Axis.DEFAULT_TICK_STROKE,
             true, // auto range
             NumberAxis.DEFAULT_AUTO_RANGE_INCLUDES_ZERO,
             NumberAxis.DEFAULT_AUTO_RANGE_STICKY_ZERO,
             NumberAxis.DEFAULT_MINIMUM_AUTO_RANGE,
	     minimumAxisValue,
             maximumAxisValue,
             false, // inverted
	     true, // auto tick unit selection
             NumberAxis.DEFAULT_TICK_UNIT,
	     true, // grid lines visible
             ValueAxis.DEFAULT_GRID_LINE_STROKE,
             ValueAxis.DEFAULT_GRID_LINE_PAINT,
             ValueAxis.DEFAULT_CROSSHAIR_VISIBLE,
             0.0,
             ValueAxis.DEFAULT_CROSSHAIR_STROKE,
             ValueAxis.DEFAULT_CROSSHAIR_PAINT);

    }

    /**
     * Constructs a new VerticalNumberAxis.
     * @param label The axis label.
     * @param labelFont The font for displaying the axis label.
     * @param labelPaint The paint used to draw the axis label.
     * @param labelInsets Determines the amount of blank space around the label.
     * @param verticalLabel Flag indicating whether or not the label is drawn vertically.
     * @param tickLabelsVisible Flag indicating whether or not tick labels are visible.
     * @param tickLabelFont The font used to display tick labels.
     * @param tickLabelPaint The paint used to draw tick labels.
     * @param tickLabelInsets Determines the amount of blank space around tick labels.
     * @param tickMarksVisible Flag indicating whether or not tick marks are visible.
     * @param tickMarkStroke The stroke used to draw tick marks (if visible).
     * @param autoRange Flag indicating whether or not the axis is automatically scaled to fit the
     *                  data.
     * @param autoRangeIncludesZero A flag indicating whether or not zero *must* be displayed on
     *                              axis.
     * @param autoRangeMinimum The smallest automatic range allowed.
     * @param minimumAxisValue The lowest value shown on the axis.
     * @param maximumAxisValue The highest value shown on the axis.
     * @param inverted A flag indicating whether the axis is normal or inverted (inverted means
     *                 running from positive to negative).
     * @param autoTickUnitSelection A flag indicating whether or not the tick units are
     *                              selected automatically.
     * @param tickUnit The tick unit.
     * @param gridLinesVisible Flag indicating whether or not grid lines are visible for this axis.
     * @param gridStroke The pen/brush used to display grid lines (if visible).
     * @param gridPaint The color used to display grid lines (if visible).
     * @param crosshairValue The value at which to draw an optional crosshair (null permitted).
     * @param crosshairStroke The pen/brush used to draw the crosshair.
     * @param crosshairPaint The color used to draw the crosshair.
     */
    public VerticalLogarithmicAxis(String label,
                              Font labelFont, Paint labelPaint, Insets labelInsets,
			      boolean verticalLabel,
			      boolean tickLabelsVisible, Font tickLabelFont, Paint tickLabelPaint,
                              Insets tickLabelInsets,
			      boolean tickMarksVisible, Stroke tickMarkStroke,
			      boolean autoRange,
                              boolean autoRangeIncludesZero, boolean autoRangeStickyZero,
                              Number autoRangeMinimum,
			      double minimumAxisValue, double maximumAxisValue,
                              boolean inverted,
			      boolean autoTickUnitSelection,
                              NumberTickUnit tickUnit,
 			      boolean gridLinesVisible, Stroke gridStroke, Paint gridPaint,
                              boolean crosshairVisible, double crosshairValue,
                              Stroke crosshairStroke, Paint crosshairPaint) {

	super(label,
              labelFont, labelPaint, labelInsets,
              tickLabelsVisible,
              tickLabelFont, tickLabelPaint, tickLabelInsets,
              tickMarksVisible,
              tickMarkStroke,
	      autoRange,
              autoRangeIncludesZero, autoRangeStickyZero,
              autoRangeMinimum,
	      minimumAxisValue, maximumAxisValue,
              inverted,
              autoTickUnitSelection, tickUnit,
              gridLinesVisible, gridStroke, gridPaint,
              crosshairVisible, crosshairValue, crosshairStroke, crosshairPaint);

	this.verticalLabel = verticalLabel;

    }

    /**
     * Returns a flag indicating whether or not the axis label is drawn vertically.
     */
    public boolean isVerticalLabel() {
        return this.verticalLabel;
    }

    /**
     * Sets a flag indicating whether or not the axis label is drawn vertically.  If the setting
     * is changed, registered listeners are notified that the axis has changed.
     */
    public void setVerticalLabel(boolean flag) {

    	if (this.verticalLabel!=flag) {
	    this.verticalLabel = flag;
	    notifyListeners(new AxisChangeEvent(this));
	}

    }

    /**
     * Configures the axis to work with the specified plot.  If the axis has auto-scaling, then sets
     * the maximum and minimum values.
     */
    public void configure() {

	if (isAutoRange()) {
	    autoAdjustRange();
	}

    }

     /**
     * Translates a data value to the display coordinates (Java 2D User Space) of the chart.
     *
     * @param value The data value.
     * @param plotArea The plot area in Java 2D User Space.
     */
    public double translateValueToJava2D(double value, Rectangle2D plotArea) {

	double axisMin = range.getLowerBound();
	double axisMax = range.getUpperBound();

	double maxY = plotArea.getMaxY();
	double minY = plotArea.getMinY();

        if (inverted) {
           return minY + (((value - axisMin)/(axisMax - axisMin)) * (maxY - minY));

        }
        else {
            return maxY - (((value - axisMin)/(axisMax - axisMin)) * (maxY - minY));

        }

    }



    public double myTranslateValueToJava2D(double value, Rectangle2D plotArea) {

	double axisMin = range.getLowerBound();
	double axisMax = range.getUpperBound();

	double maxY = plotArea.getMaxY();
	double minY = plotArea.getMinY();


        if(axisMin==0.0)
          axisMin=this.tickUnit.getSize()/15;//this.tickUnit.getSize()/1.5;


         axisMin = Math.log(axisMin)/LOG10_VALUE;

         axisMax = Math.log(axisMax)/LOG10_VALUE;
       // }

        if (inverted) {
            if(axisMin==axisMax)
               return minY;
            else
               return minY + (((value - axisMin)/(axisMax - axisMin)) * (maxY - minY));

        }
        else {
            if(axisMin==axisMax)
              return maxY;
            else
              return maxY - (((value - axisMin)/(axisMax - axisMin)) * (maxY - minY));
        }

    }

    public double translateJava2DtoValue(float java2DValue, Rectangle2D plotArea) {

	double axisMin = range.getLowerBound();
	double axisMax = range.getUpperBound();

	double plotMinY = plotArea.getMinY();
	double plotMaxY = plotArea.getMaxY();

        // The Math.log() funtion is based on e not 10.
        if (axisMin != 0.0) {

            axisMin = Math.log(axisMin)/LOG10_VALUE;
        }

        if (axisMax != 0.0) {

            axisMax = Math.log(axisMax)/LOG10_VALUE;
        }

        if (inverted)
       {
         double logVal=(java2DValue-plotMinY)/(plotMaxY-plotMinY)*(axisMax-axisMin)+axisMin;
         return Math.pow(10,logVal);
       }
       else
       {
         double logVal=( plotMaxY-java2DValue)/(plotMaxY-plotMinY)*(axisMax-axisMin)+axisMin;
         return Math.pow(10,logVal);
       }
    }



    /**
     * Returns the smallest (closest to negative infinity) double value that is
     * not less than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param lower  a double value above which a ceiling will be calcualted.
     */
    private double computeLogCeil(double upper) {

        // The Math.log() funtion is based on e not 10.
        double logCeil = Math.log(upper)/LOG10_VALUE;

        logCeil = Math.ceil(logCeil);

        logCeil = Math.pow(10, logCeil);

        return logCeil;
    }

    /**
     * Returns the largest (closest to positive infinity) double value that is
     * not greater than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param lower  a double value below which a floor will be calcualted.
     */
    private double computeLogFloor(double lower) {

        // The Math.log() funtion is based on e not 10.
        double logFloor = Math.log(lower)/LOG10_VALUE;

        logFloor = Math.floor(logFloor);

        logFloor = Math.pow(10, logFloor);

        return logFloor;
    }

     /**
     * Sets the axis minimum and maximum values so that all the data is visible.
     * <P>
     * You can control the range calculation in several ways.  First, you can define upper and
     * lower margins as a percentage of the data range (the default is a 5% margin for each).
     * Second, you can set a flag that forces the range to include zero.  Finally, you can set
     * another flag, the 'sticky zero' flag, that only affects the range when zero falls within the
     * axis margins.  When this happens, the margin is truncated so that zero is the upper or lower
     * limit for the axis.
     */
    public void autoAdjustRange() {


    if (plot==null) return;  // no plot, no data

        if (plot instanceof VerticalValuePlot) {
            VerticalValuePlot vvp = (VerticalValuePlot)plot;

            Range r = vvp.getVerticalDataRange();
            if (r==null) r = new Range(DEFAULT_MINIMUM_AXIS_VALUE, DEFAULT_MAXIMUM_AXIS_VALUE);

            double lower = r.getLowerBound();
            double upper = r.getUpperBound();
            //double range = upper-lower;
           // System.out.println("vertical Logaxis:sutoadjustrange:lower::"+lower+",upper::"+upper);
            // ensure the autorange is at least <minRange> in size...
           /* double minRange = this.autoRangeMinimumSize.doubleValue();
            if (range<minRange) {
                upper = (upper+lower+minRange)/2;
                lower = (upper+lower-minRange)/2;
            }

            if (this.autoRangeIncludesZero) {
                if (this.autoRangeStickyZero) {
                    if (upper<=0.0) {
                        upper = 0.0;
                    }
                    else {
                        upper = upper+upperMargin*range;
                    }
                    if (lower>=0.0) {
                        lower = 0.0;
                    }
                    else {
                        lower = lower-lowerMargin*range;
                    }
                }
                else {
                    upper = Math.max(0.0, upper+upperMargin*range);
                    lower = Math.min(0.0, lower-lowerMargin*range);
                }
            }
            else {
                if (this.autoRangeStickyZero) {
                    if (upper<=0.0) {
                        upper = Math.min(0.0, upper+upperMargin*range);
                    }
                    else {
                        upper = upper+upperMargin*range;
                    }
                    if (lower>=0.0) {
                        lower = Math.max(0.0, lower-lowerMargin*range);
                    }
                    else {
                        lower = lower-lowerMargin*range;
                    }
                }
                else {
                    upper = upper+upperMargin*range;
                    lower = lower-lowerMargin*range;
                }
            }*/

            this.range = new Range(lower, upper);
        }


    }


    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
     * @param g2 The graphics device;
     * @param drawArea The area within which the chart should be drawn.
     * @param plotArea The area within which the plot should be drawn (a subset of the drawArea).
     */
    public void draw(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea) {

        if (!visible) return;

        //System.out.println("VerticalNumberAxis:draw method called");

        // draw the axis label
        if (this.label!=null) {
            g2.setFont(labelFont);
            g2.setPaint(labelPaint);

            Rectangle2D labelBounds = labelFont.getStringBounds(label, g2.getFontRenderContext());
            if (verticalLabel) {
                double xx = drawArea.getX()+labelInsets.left+labelBounds.getHeight();
                double yy = plotArea.getY()+plotArea.getHeight()/2+(labelBounds.getWidth()/2);
                //old: drawVerticalString(label, g2, (float)xx, (float)yy);
                RefineryUtilities.drawRotatedString(label, g2, (float)xx, (float)yy, -Math.PI/2);
            }
            else {
                double xx = drawArea.getX()+labelInsets.left;
                double yy = drawArea.getY()+drawArea.getHeight()/2-labelBounds.getHeight()/2;
                g2.drawString(label, (float)xx, (float)yy);
            }
        }

        // draw the tick labels and marks and gridlines
        this.refreshTicks(g2, drawArea, plotArea);
        double xx = plotArea.getX();
        g2.setFont(tickLabelFont);

        Iterator iterator = ticks.iterator();
        while (iterator.hasNext()) {
            Tick tick = (Tick)iterator.next();
            //float yy = (float)this.translateValueToJava2D(tick.getNumericalValue(), plotArea);
            float yy=(float)tick.getY() ;
            if(tick.getNumericalValue()==range.getLowerBound())
              yy=(float)plotArea.getMaxY();
            else {
              double logval=Math.log(tick.getNumericalValue())/LOG10_VALUE;
              yy = (float)this.myTranslateValueToJava2D(logval, plotArea);
            }
            if (tickLabelsVisible) {
               g2.setPaint(this.tickLabelPaint);
               g2.drawString(tick.getText(), tick.getX(), tick.getY());
            }
            if (tickMarksVisible) {
               g2.setStroke(this.getTickMarkStroke());
               Line2D mark = new Line2D.Double(plotArea.getX()-2, yy,
                             plotArea.getX()+2, yy);
               g2.draw(mark);
            }
            if (gridLinesVisible) {
               g2.setStroke(gridStroke);
               g2.setPaint(gridPaint);
               Line2D gridline = new Line2D.Double(xx, yy,
                                plotArea.getMaxX(), yy);
               g2.draw(gridline);

            }
        }

    }

    /**
     * Returns the width required to draw the axis in the specified draw area.
     * @param g2 The graphics device;
     * @param plot A reference to the plot;
     * @param drawArea The area within which the plot should be drawn.
     */
    public double reserveWidth(Graphics2D g2, Plot plot, Rectangle2D drawArea) {

	// calculate the width of the axis label...
	double labelWidth = 0.0;
	if (label!=null) {
	    Rectangle2D labelBounds = labelFont.getStringBounds(label, g2.getFontRenderContext());
	    labelWidth = labelInsets.left+labelInsets.right;
	    if (this.verticalLabel) {
		labelWidth = labelWidth + labelBounds.getHeight();  // assume width == height before rotation
	    }
	    else {
		labelWidth = labelWidth + labelBounds.getWidth();
	    }
	}

	// calculate the width required for the tick labels (if visible);
	double tickLabelWidth = tickLabelInsets.left+tickLabelInsets.right;
	if (tickLabelsVisible) {
	    this.refreshTicks(g2, drawArea, drawArea);
	    tickLabelWidth = tickLabelWidth+getMaxTickLabelWidth(g2, drawArea);
	}
	return labelWidth+tickLabelWidth;

    }

    /**
     * Returns area in which the axis will be displayed.
     * @param g2 The graphics device;
     * @param plot A reference to the plot;
     * @param drawArea The area in which the plot and axes should be drawn;
     * @param reservedHeight The height reserved for the horizontal axis;
     */
    public Rectangle2D reserveAxisArea(Graphics2D g2, Plot plot, Rectangle2D drawArea,
				       double reservedHeight) {

	// calculate the width of the axis label...
	double labelWidth = 0.0;
	if (label!=null) {
	    Rectangle2D labelBounds = labelFont.getStringBounds(label, g2.getFontRenderContext());
	    labelWidth = labelInsets.left+labelInsets.right;
	    if (this.verticalLabel) {
		labelWidth = labelWidth + labelBounds.getHeight();  // assume width == height before rotation
	    }
	    else {
		labelWidth = labelWidth + labelBounds.getWidth();
	    }
	}

	// calculate the width of the tick labels
	double tickLabelWidth = tickLabelInsets.left+tickLabelInsets.right;
	if (tickLabelsVisible) {
	    Rectangle2D approximatePlotArea = new Rectangle2D.Double(drawArea.getX(), drawArea.getY(),
								     drawArea.getWidth(),
								     drawArea.getHeight()-reservedHeight);
	    this.refreshTicks(g2, drawArea, approximatePlotArea);
	    tickLabelWidth = tickLabelWidth+getMaxTickLabelWidth(g2, approximatePlotArea);
	}

	return new Rectangle2D.Double(drawArea.getX(), drawArea.getY(), labelWidth+tickLabelWidth,
				      drawArea.getHeight()-reservedHeight);

    }

    /**
     * Selects an appropriate tick value for the axis.  The strategy is to display as many ticks as
     * possible (selected from an array of 'standard' tick units) without the labels overlapping.
     * @param g2 The graphics device;
     * @param drawArea The area in which the plot and axes should be drawn;
     * @param plotArea The area in which the plot should be drawn;
     */
    private void selectAutoTickUnit(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea) {

        // calculate the tick label height...
        FontRenderContext frc = g2.getFontRenderContext();
        double tickLabelHeight = tickLabelFont.getLineMetrics("123", frc).getHeight()
                                 +this.tickLabelInsets.top+this.tickLabelInsets.bottom;

        // now find the smallest tick unit that will accommodate the labels...
	double zero = this.translateValueToJava2D(0.0, plotArea);

        // start with the current tick unit...
        NumberTickUnit candidate1
                         = (NumberTickUnit)this.standardTickUnits.getCeilingTickUnit(this.tickUnit);
        double y = this.translateValueToJava2D(candidate1.getSize(), plotArea);
        double unitHeight = Math.abs(y-zero);

        // then extrapolate...
        double bestguess = (tickLabelHeight/unitHeight) * candidate1.getSize();
        NumberTickUnit guess = new NumberTickUnit(bestguess, null);
        // NumberTickUnit candidate2 = (NumberTickUnit)this.standardTickUnits.getNearestTickUnit(guess);

        NumberTickUnit candidate2 = (NumberTickUnit)this.standardTickUnits.getCeilingTickUnit(guess);

        this.tickUnit = candidate2;

    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the results in the
     * tick label list (ready for drawing).
     * @param g2 The graphics device.
     * @param drawArea The area in which the plot and the axes should be drawn.
     * @param plotArea The area in which the plot should be drawn.
     */
    public void refreshTicks(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea)throws java.lang.ArithmeticException {

	this.ticks.clear();
        ++counter;
	g2.setFont(tickLabelFont);

	if (this.autoTickUnitSelection) {
	    selectAutoTickUnit(g2, drawArea, plotArea);
	}

	double size = this.tickUnit.getSize();
	int count = this.calculateVisibleTickCount();

	double y0=plotArea.getMaxY();
        float sum=0.1f;
        int i=-20;
        if(counter==2)
         this.tickUnit.formatter.setMaximumFractionDigits(4);



        /**
         * For Loop - Drawing the ticks which corresponds to the  power of 10
         */
        for (i=-20; ; i++) {
	    for(int j=0;j<10;++j) {
              sum =j*(float)Math.pow(10,i);
              if(sum<range.getLowerBound())
                 continue;
              if(sum>range.getUpperBound())
                return;
             double currentTickValue = sum;
              double val=currentTickValue;
              double logval;
              double yy;
            if(sum==range.getLowerBound())
               yy=plotArea.getMaxY();
            else {
               logval=Math.log(val)/LOG10_VALUE;
                yy = this.myTranslateValueToJava2D(logval, plotArea);
            }

            if(sum<=0.0)
               throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist");
//             currentTickValue=this.tickUnit.getSize()/15;
            String tickLabel = this.tickUnit.valueToString(currentTickValue);

            /**
             * to remove the extra zeros
             */
	    if(tickLabel.startsWith("0"))
              tickLabel=tickLabel.substring(1);
              int ticklength= tickLabel.length();
            if(tickLabel.indexOf(".")!=-1) {
            if(tickLabel.lastIndexOf("0")==ticklength-1)
              for(int k= ticklength-1;;){
                  tickLabel=tickLabel.substring(0,k);
                  --k;
                  if(k<0)
                    break;
                  if(tickLabel.charAt(k)=='0' || tickLabel.charAt(k)=='.')
                    continue;
                  else break;
              }
            }

	    Rectangle2D tickLabelBounds = tickLabelFont.getStringBounds(tickLabel,
                                                                        g2.getFontRenderContext());
	    float x = (float)(plotArea.getX()
                              -tickLabelBounds.getWidth()
                              -tickLabelInsets.left-tickLabelInsets.right);
	    float y = (float)(yy+(tickLabelBounds.getHeight()/2));

           /**
             * Code added to remove the overlapping of the tickLabels
             */
            if(sum==range.getLowerBound())
               y=(float)yy;
            if(yy>plotArea.getMaxY())
              continue;
            if(yy<plotArea.getMinY())
              return;
            if(y>y0 && j!=1)
              tickLabel="";

            else {
              if(y>y0 && j==1)
                 removePreviousNine(i);
              y0=y-tickLabelBounds.getHeight()-0.25;
            }
              Tick tick = new Tick(new Double(currentTickValue), tickLabel, x, y);
	      ticks.add(tick);
	}
       }

    }

  /**
   * removes the prevois nine so that powers of 10 can be displayed
   */
  private void removePreviousNine(int power) {
    double value=9*(float)Math.pow(10,power-1);
    Iterator iterator = ticks.iterator();
    while (iterator.hasNext()) {
      Tick tick = (Tick)iterator.next();
      if(tick.getNumericalValue()==value) {

        tick.text="";
      }
    }
  }

    /**
     * Returns true if the specified plot is compatible with the axis, and false otherwise.
     * <P>
     * This class (VerticalNumberAxis) requires that the plot implements the VerticalValuePlot
     * interface.
     * @param plot The plot.
     * @return True if the specified plot is compatible with the axis, and false otherwise.
     */
    protected boolean isCompatiblePlot(Plot plot) {

        if (plot instanceof VerticalValuePlot) return true;
        else return false;

    }

}