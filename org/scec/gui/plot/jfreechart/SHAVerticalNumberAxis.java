package org.scec.gui.plot.jfreechart;



import java.awt.Font;
import java.awt.Paint;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import com.jrefinery.data.Range;
import com.jrefinery.ui.RefineryUtilities;
import com.jrefinery.chart.event.AxisChangeEvent;
import com.jrefinery.chart.VerticalValuePlot;
import com.jrefinery.chart.VerticalNumberAxis;

/**
 * <p>Title: SHAVerticalNumberAxis</p>
 *
 * <p>Description: This class extends the class verticalNumberAxis of the JFreechart
 * package to make the ticks for the small values lower than 1E-7, Now all the
 * Tester Applets will  making use of this class to draw the vertical ticks</p>
 *
 * @author : Nitin Gupta   Date:Aug,17,2002
 * @version 1.0
 */

public class SHAVerticalNumberAxis extends VerticalNumberAxis {

    /**
     * Constructs a vertical number axis, using default values where necessary.
     */
    public SHAVerticalNumberAxis() {

        super();

    }

    /**
     * Constructs a vertical number axis, using default values where necessary.
     *
     * @param label The axis label (null permitted).
     */
    public SHAVerticalNumberAxis(String label) {

        super(label);

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
  protected void autoAdjustRange() {

      if (plot==null) return;  // no plot, no data

      if (plot instanceof VerticalValuePlot) {
          VerticalValuePlot vvp = (VerticalValuePlot)plot;

          Range r = vvp.getVerticalDataRange();
          if (r==null) r = new Range(DEFAULT_MINIMUM_AXIS_VALUE, DEFAULT_MAXIMUM_AXIS_VALUE);

          double lower = r.getLowerBound();
          double upper = r.getUpperBound();
          if(upper==lower) {
            this.range = new Range(lower-0.5,lower+0.5);
            return;
          }


          double range = upper-lower;

          // ensure the autorange is at least <minRange> in size...
          double minRange = this.autoRangeMinimumSize.doubleValue();
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
          }

          this.range = new Range(lower, upper);
      }


    }
}