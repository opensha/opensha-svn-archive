package org.scec.gui;

import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;

/**
 * <p>Title: AdjustableScaleXYItemRenderer</p>
 * <p>Description: JFreeChart subclass. Used by FaultDemo Applet. </p>
 *
 * @author unascribed
 * @version 1.0
 */

public class AdjustableScaleXYItemRenderer extends StandardXYItemRenderer implements XYItemRenderer{

    /**
     * Constructs a new renderer.
     */
    public AdjustableScaleXYItemRenderer() { super(); }

    /**
     * Constructs a new renderer.
     * <p>
     * To specify the type of renderer, use one of the constants: SHAPES, LINES or SHAPES_AND_LINES.
     *
     * @param type The type of renderer.
     * @param toolTipGenerator The tooltip generator.
     */
    public AdjustableScaleXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
        super(type, toolTipGenerator);
    }


    double scale = 4.0;

    /**
     * Returns the shape scale of a single data item.
     *
     * @param plot The plot (can be used to obtain standard color information etc).
     * @param series The series index
     * @param item The item index
     * @param x The x value of the item
     * @param y The y value of the item
     *
     * @ return The scale used to draw the shape used for the data item
     */
    protected double getShapeScale(Plot plot,int series,int item, double x, double y) {
      return scale;
    }

    /**
     * Is used to determine if a shape is filled when drawn or not
     *
     * @param plot The plot (can be used to obtain standard color information etc).
     * @param series The series index
     * @param item The item index
     * @param x The x value of the item
     * @param y The y value of the item
     *
     * @return True if the shape used to draw the data item should be filled, false otherwise.
     */
    protected boolean isShapeFilled(Plot plot, int series, int item, double x, double y) {
      return false;
    }

}
