package org.scec.gui.plot.jfreechart;


import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.util.Iterator;
import com.jrefinery.data.Range;
import com.jrefinery.ui.RefineryUtilities;
import com.jrefinery.chart.HorizontalNumberAxis;
import com.jrefinery.chart.HorizontalValuePlot;

/**
 * <p>Title: SHAHorizontalNumberAxis</p>
 *
 * <p>Description: This class extends the class HorizontalNumberAxis of the JFreechart
 * package to make the ticks for the small values lower than 1E-7, Now all the
 * Tester Applets will  making use of this class to draw the horizontal ticks</p>
 *
 * @author : Nitin Gupta   Date:Aug,17,2002
 * @version 1.0
 */

public class SHAHorizontalNumberAxis extends HorizontalNumberAxis {

   /**
     * Constructs a horizontal number axis, using default values where necessary.
     *
     * @param label The axis label.
     */
    public SHAHorizontalNumberAxis(String label) {
        super(label);
    }


   /**
     * Rescales the axis to ensure that all data is visible.
     */
    protected void autoAdjustRange() {

      if (plot==null) return;  // no plot, no data

      if (plot instanceof HorizontalValuePlot) {
          HorizontalValuePlot hvp = (HorizontalValuePlot)plot;

          Range r = hvp.getHorizontalDataRange();
          if (r==null) r = new Range(DEFAULT_MINIMUM_AXIS_VALUE, DEFAULT_MAXIMUM_AXIS_VALUE);
          double upper = r.getUpperBound();
          double lower = r.getLowerBound();
          if(upper==lower)
            {
            this.range=new Range(lower-0.5, upper+0.5);
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

          this.range=new Range(lower, upper);
      }

    }

}