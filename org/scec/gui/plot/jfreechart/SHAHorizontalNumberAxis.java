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
 * <p>Description: This class extends the class HorizontalNumberAxis of the JFreechart
 * package to make the ticks for the small values lower than 1E-7, Now all the
 * Tester Applets will  making use of this class to draw the horizontal ticks</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
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
            double range = upper-lower;
            if(lower==upper) {
              lower=Math.floor(lower);
              upper=Math.ceil(upper);
            }
            this.range=new Range(lower, upper);
        }
    }

}