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
 * ------------------
 * CrosshairInfo.java
 * ------------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jan-2002 : Version 1 (DG);
 * 05-Mar-2002 : Added Javadoc comments (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 19-Sep-2003 : Modified crosshair distance calculation (DG);
 *
 */

package org.jfree.chart;

/**
 * Maintains information about crosshairs on a plot.
 *
 * @author David Gilbert
 */
public class CrosshairInfo {

    /** A flag that controls whether the distance is calculated in data space or Java2D space. */
    private boolean calculateDistanceInDataSpace = false;

    /** The x-value for the anchor point. */
    private double anchorX;

    /** The y-value for the anchor point. */
    private double anchorY;
    
    /** The X anchor value in Java2D space. */
    private double anchorXView;
    
    /** The Y anchor value in Java2D space. */
    private double anchorYView;

    /** The x-value for the crosshair point. */
    private double crosshairX;

    /** The y-value for the crosshair point. */
    private double crosshairY;

    /** The smallest distance so far between the anchor point and a data point. */
    private double distance;

    /**
     * Default constructor.
     */
    public CrosshairInfo() {
    }

    /**
     * Creates a new info object.
     * 
     * @param calculateDistanceInDataSpace  a flag that controls whether the distance is calculated
     *                                      in data space or Java2D space.
     */
    public CrosshairInfo(boolean calculateDistanceInDataSpace) {
        this.calculateDistanceInDataSpace = calculateDistanceInDataSpace;
    }

    /**
     * Sets the distance.
     *
     * @param distance  the distance.
     */
    public void setCrosshairDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Evaluates a data point and if it is the closest to the anchor point it
     * becomes the new crosshair point.
     * <P>
     * To understand this method, you need to know the context in which it will
     * be called.  An instance of this class is passed to an XYItemRenderer as
     * each data point is plotted.  As the point is plotted, it is passed to
     * this method to see if it should be the new crosshair point.
     *
     * @param dataX  x position of candidate for the new crosshair point.
     * @param dataY  y position of candidate for the new crosshair point.
     * @param viewX  x in Java2D space.
     * @param viewY  y in Java2D space.
     */
    public void updateCrosshairPoint(double dataX, double dataY, double viewX, double viewY) {

        double d = 0.0;
        if (this.calculateDistanceInDataSpace) {
            d = (dataX - this.anchorX) * (dataX - this.anchorX)
                    + (dataY - this.anchorY) * (dataY - this.anchorY);
        }
        else {
            d = (viewX - this.anchorXView) * (viewX - this.anchorXView)
                     + (viewY - this.anchorYView) * (viewY - this.anchorYView);            
        }

        if (d < distance) {
            crosshairX = dataX;
            crosshairY = dataY;
            distance = d;
        }

    }

    /**
     * Evaluates an x-value and if it is the closest to the anchor point it
     * becomes the new crosshair point.
     * <P>
     * Used in cases where only the x-axis is numerical.
     *
     * @param candidateX  x position of the candidate for the new crosshair point.
     */
    public void updateCrosshairX(double candidateX) {

        double d = Math.abs(candidateX - anchorX);
        if (d < distance) {
            crosshairX = candidateX;
            distance = d;
        }

    }

    /**
     * Evaluates a y-value and if it is the closest to the anchor point it
     * becomes the new crosshair point.
     * <P>
     * Used in cases where only the y-axis is numerical.
     *
     * @param candidateY  y position of the candidate for the new crosshair point.
     */
    public void updateCrosshairY(double candidateY) {

        double d = Math.abs(candidateY - anchorY);
        if (d < distance) {
            crosshairY = candidateY;
            distance = d;
        }

    }

    /**
     * Set the x-value for the anchor point.
     *
     * @param x  the x position.
     */
    public void setAnchorX(double x) {
        this.anchorX = x;
        this.crosshairX = x;
    }

    /**
     * Set the y-value for the anchor point.
     *
     * @param y  the y position.
     */
    public void setAnchorY(double y) {
        this.anchorY = y;
        this.crosshairY = y;
    }

    /**
     * Set the x-value for the anchor point.
     *
     * @param x  the x position.
     */
    public void setAnchorXView(double x) {
        this.anchorXView = x;
    }

    /**
     * Set the y-value for the anchor point.
     *
     * @param y  the y position.
     */
    public void setAnchorYView(double y) {
        this.anchorYView = y;
    }

    /**
     * Get the x-value for the crosshair point.
     *
     * @return the x position of the crosshair point.
     */
    public double getCrosshairX() {
        return this.crosshairX;
    }

    /**
     * Get the y-value for the crosshair point.
     *
     * @return the y position of the crosshair point.
     */
    public double getCrosshairY() {
        return this.crosshairY;
    }

}
