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
 * Original Author:  Nitin Gupta and Vipin Gupta
 * Contributor(s):   David Gilbert (for Simba Management Limited);
 *                   Eric Thomas;
 *
 * $Id$
 *
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




public class VerticalLogarithmicAxis extends VerticalNumberAxis{

     /** A flag indicating whether or not the axis label is drawn vertically. */
    protected boolean verticalLabel;
    public static final double LOG10_VALUE = Math.log(10);
    private int lowest=-30; // lowest power of ten allowed
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
             ValueAxis.DEFAULT_LOWER_BOUND,
             ValueAxis.DEFAULT_UPPER_BOUND);

        setAutoRange(true);

    }

       /**
        * Constructs a vertical logarithmic axis.
        *
        * @param label The axis label (null permitted).
        * @param labelFont The font for displaying the axis label.
        * @param minimumAxisValue The lowest value shown on the axis.
        * @param maximumAxisValue The highest value shown on the axis.
        */
       public VerticalLogarithmicAxis(String label,
                                      Font labelFont,
                                      double minimumAxisValue,
                                      double maximumAxisValue) {

           this(label,
                labelFont,
                Axis.DEFAULT_AXIS_LABEL_PAINT,
                Axis.DEFAULT_AXIS_LABEL_INSETS,
                true, // tick labels visible
                true,  // tick labels drawn vertically
                Axis.DEFAULT_TICK_LABEL_FONT,
                Axis.DEFAULT_TICK_LABEL_PAINT,
                Axis.DEFAULT_TICK_LABEL_INSETS,
                false, // tick marks visible
                Axis.DEFAULT_TICK_STROKE,
                Axis.DEFAULT_TICK_PAINT,
                false, // no auto range selection, since the caller specified a range in the arguments
                NumberAxis.DEFAULT_AUTO_RANGE_MINIMUM_SIZE,
                NumberAxis.DEFAULT_AUTO_RANGE_INCLUDES_ZERO,
                NumberAxis.DEFAULT_AUTO_RANGE_STICKY_ZERO,
                minimumAxisValue,
                maximumAxisValue,
                false, // inverted
                true, // auto tick unit selection
                NumberAxis.DEFAULT_TICK_UNIT,
                true, // grid lines visible
                ValueAxis.DEFAULT_GRID_LINE_STROKE,
                ValueAxis.DEFAULT_GRID_LINE_PAINT,
                0.0, //anchorValue
                ValueAxis.DEFAULT_CROSSHAIR_VISIBLE,
                0.0,
                ValueAxis.DEFAULT_CROSSHAIR_STROKE,
                ValueAxis.DEFAULT_CROSSHAIR_PAINT);

       }

       /**
        * Constructs a vertical number axis.
        *
        * @param label The axis label.
        * @param labelFont The font for displaying the axis label.
        * @param labelPaint The paint used to draw the axis label.
        * @param labelInsets Determines the amount of blank space around the label.
        * @param tickMarksVisible Flag indicating whether or not tick labels are visible.
        * @param tickLabelFont The font used to display tick labels.
        * @param tickLabelPaint The paint used to draw tick labels.
        * @param tickLabelInsets Determines the amount of blank space around tick labels.
        * @param showTickMarks Flag indicating whether or not tick marks are visible.
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
        * @param showGridLines Flag indicating whether or not grid lines are visible for this axis.
        * @param gridStroke The pen/brush used to display grid lines (if visible).
        * @param gridPaint The color used to display grid lines (if visible).
        * @param crosshairValue The value at which to draw an optional crosshair (null permitted).
        * @param crosshairStroke The pen/brush used to draw the crosshair.
        * @param crosshairPaint The color used to draw the crosshair.
        */
       public VerticalLogarithmicAxis(String label,
                                      Font labelFont, Paint labelPaint, Insets labelInsets,
                                      boolean tickLabelsVisible,boolean verticalTickLabels,
                                      Font tickLabelFont, Paint tickLabelPaint,
                                      Insets tickLabelInsets,
                                      boolean tickMarksVisible, Stroke tickMarkStroke,Paint tickMarkPaint,
                                      boolean autoRange,Number autoRangeMinimumSize,
                                      boolean autoRangeIncludesZero,
                                      boolean autoRangeStickyZero,
                                      double minimumAxisValue, double maximumAxisValue,
                                      boolean inverted,
                                      boolean autoTickUnitSelection,
                                      NumberTickUnit tickUnit,
                                      boolean gridLinesVisible, Stroke gridStroke, Paint gridPaint,
                                      double anchorValue, boolean crosshairVisible, double crosshairValue,
                                      Stroke crosshairStroke, Paint crosshairPaint) {

           super(label,
                 labelFont, labelPaint, labelInsets,
                 tickLabelsVisible,verticalTickLabels,
                 tickLabelFont, tickLabelPaint, tickLabelInsets,
                 tickMarksVisible,
                 tickMarkStroke,tickMarkPaint,
                 autoRange, autoRangeMinimumSize,autoRangeIncludesZero, autoRangeStickyZero,
                 minimumAxisValue, maximumAxisValue,
                 inverted,
                 autoTickUnitSelection, tickUnit,
                 gridLinesVisible, gridStroke, gridPaint,anchorValue,
                 crosshairVisible, crosshairValue, crosshairStroke, crosshairPaint);
            this.verticalLabel = verticalTickLabels;


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

	double axisMin = getRange().getLowerBound();
	double axisMax = getRange().getUpperBound();

	double maxY = plotArea.getMaxY();
	double minY = plotArea.getMinY();

        if (isInverted()) {
           return minY + (((value - axisMin)/(axisMax - axisMin)) * (maxY - minY));

        }
        else {
            return maxY - (((value - axisMin)/(axisMax - axisMin)) * (maxY - minY));

        }

    }



    public double myTranslateValueToJava2D(double value, Rectangle2D plotArea) {

	double axisMin = getRange().getLowerBound();
	double axisMax = getRange().getUpperBound();

	double maxY = plotArea.getMaxY();
	double minY = plotArea.getMinY();


        if(axisMin==0.0)
          axisMin=this.getTickUnit().getSize()/15;//this.tickUnit.getSize()/1.5;


         axisMin = Math.log(axisMin)/LOG10_VALUE;

         axisMax = Math.log(axisMax)/LOG10_VALUE;
       // }

        if (isInverted()) {
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

	double axisMin = getRange().getLowerBound();
	double axisMax = getRange().getUpperBound();

	double plotMinY = plotArea.getMinY();
	double plotMaxY = plotArea.getMaxY();

        // The Math.log() funtion is based on e not 10.
        if (axisMin != 0.0) {

            axisMin = Math.log(axisMin)/LOG10_VALUE;
        }

        if (axisMax != 0.0) {

            axisMax = Math.log(axisMax)/LOG10_VALUE;
        }

        if (isInverted())
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
            if (r==null) r = new Range(DEFAULT_LOWER_BOUND, DEFAULT_UPPER_BOUND);

            double lower = r.getLowerBound();
            double upper = r.getUpperBound();

            // make the upper a little higher so that it does not overlap with  axis lines
            boolean found = false;
            double val;
            for(int i = lowest;!found;++i) {
              val = Math.pow(10,i);
              for(int j=1;j<10;++j)
                 if((j*val)>upper){
                   upper = Math.pow(10,i+1);
                   found = true;
                   break;
                 }
            }

            // set the range
            setRange(new Range(lower, upper));
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
           double val=1;
           int eIndex =tick.getText().indexOf("E");
           // check whether this is minor axis. for minor axis we save,2-9 in label
           if(!tick.getText().trim().equalsIgnoreCase("") && eIndex==-1)
              val=Double.parseDouble(tick.getText());
           double logval=Math.log(tick.getNumericalValue())/LOG10_VALUE;
           yy = (float)this.myTranslateValueToJava2D(logval, plotArea);

           if(!isPowerOfTen(val)) // for major axis
             g2.setFont(tickLabelFont);
           else  // show minor axis in smaller font
             g2.setFont(new Font(tickLabelFont.getName(),tickLabelFont.getStyle(),tickLabelFont.getSize()+3));

           if (tickLabelsVisible) {
              g2.setPaint(this.tickLabelPaint);
              if(eIndex==-1)
                g2.drawString(tick.getText(), tick.getX(), tick.getY());
              else { // show in superscript form
                g2.drawString("10", tick.getX(), tick.getY());
                g2.setFont(new Font(tickLabelFont.getName(),tickLabelFont.getStyle(),tickLabelFont.getSize()-2));
                g2.drawString(tick.getText().substring(eIndex+1),tick.getX()+16,tick.getY()-6);
              }
           }
           if (tickMarksVisible) {
              g2.setStroke(this.getTickMarkStroke());
              Line2D mark = new Line2D.Double(plotArea.getX()-2, yy,
                            plotArea.getX()+2, yy);
              g2.draw(mark);
           }
           if (this.isGridLinesVisible()) {
              g2.setStroke(this.getGridStroke());
              g2.setPaint(getGridPaint());
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
    public void selectAutoTickUnit(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea) {

        // calculate the tick label height...
        FontRenderContext frc = g2.getFontRenderContext();
        double tickLabelHeight = tickLabelFont.getLineMetrics("123", frc).getHeight()
                                 +this.tickLabelInsets.top+this.tickLabelInsets.bottom;

        // now find the smallest tick unit that will accommodate the labels...
	double zero = this.translateValueToJava2D(0.0, plotArea);

        // start with the current tick unit...
        NumberTickUnit candidate1
                         = (NumberTickUnit)this.getStandardTickUnits().getCeilingTickUnit(getTickUnit());
        double y = this.translateValueToJava2D(candidate1.getSize(), plotArea);
        double unitHeight = Math.abs(y-zero);

        // then extrapolate...
        double bestguess = (tickLabelHeight/unitHeight) * candidate1.getSize();
        NumberTickUnit guess = new NumberTickUnit(bestguess, null);
        // NumberTickUnit candidate2 = (NumberTickUnit)this.standardTickUnits.getNearestTickUnit(guess);

        NumberTickUnit candidate2 = (NumberTickUnit)this.getStandardTickUnits().getCeilingTickUnit(guess);

        this.setTickUnit(candidate2);

    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the results in the
     * tick label list (ready for drawing).
     * @param g2 The graphics device.
     * @param drawArea The area in which the plot and the axes should be drawn.
     * @param plotArea The area in which the plot should be drawn.
     */
    public void refreshTicks(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea) {

	this.ticks.clear();
        ++counter;
	g2.setFont(tickLabelFont);

	if (isAutoTickUnitSelection()) {
	    selectAutoTickUnit(g2, drawArea, plotArea);
	}

	double size = getTickUnit().getSize();
	int count = this.calculateVisibleTickCount();

	double y0=plotArea.getMaxY();
        double sum=0.0;

        /*if(counter==2)
           getTickUnit().formatter.setMaximumFractionDigits(3);*/

        boolean superscript=false;

        int upperBound=powerOf10(getRange().getUpperBound());
        int lowerBound=powerOf10(getRange().getLowerBound());

        // whether you want to show in superscript form or not
        if((upperBound-lowerBound)>= 4)
           superscript=true;
        if(getRange().getLowerBound()<0.001 || getRange().getUpperBound()>1000.0)
          superscript=true;

        // see whther there exists any major axis in data
        double lower = getRange().getLowerBound();
        double upper = getRange().getUpperBound();
        if(lower==0.0 || upper==0.0)
               throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist for Y-Log Plot");
        for(int i=lowest;;++i) {
          double val1=Double.parseDouble("1e"+i);
          double val2=Double.parseDouble("1e"+(i+1));
          if(lower==val1 || upper==val1)
            break;
          if(lower > val1 && lower< val2 && upper > val1 && upper<val2) {
            // no major axis exixts in dat so you have to add the major axis
            this.setRange(val1,val2);
            break;
          }
          if(lower < val2 && upper > val2) // we have found 1 major axis
            break;
        }

        /**
         * For Loop - Drawing the ticks which corresponds to the  power of 10
         */
        for (int i=lowest; ; i++){
	    for(int j=0;j<10;++j) {
              sum =Double.parseDouble(j+"e"+i);
              if(sum<getRange().getLowerBound())
                continue;
              if(sum>getRange().getUpperBound())
                return;
              double currentTickValue = sum;
              double val=currentTickValue;
              double logval;
              double yy;


              logval=Math.log(val)/LOG10_VALUE;
              yy = this.myTranslateValueToJava2D(logval, plotArea);

              if(sum<=0.0)
                throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist for Y-Log Plot");

              String tickLabel = new String(""+currentTickValue);

              if(j!=1) // for minor axis, just display 2 to 9
                 tickLabel= ""+j;
              else if(superscript) // whether you want to show in superscript format
                tickLabel=new String("10E"+i);


              /**
               * to remove the extra zeros
               */
              if(tickLabel.startsWith("0")) // remove the starting ZERO
                  tickLabel=tickLabel.substring(1);
                int ticklength= tickLabel.length();

                if(tickLabel.lastIndexOf("0")==ticklength-1) {
                    for(int k= ticklength-1; tickLabel.indexOf(".")!=-1 ;){
                      tickLabel=tickLabel.substring(0,k);
                      --k;
                      if(k<0)
                        break;
                      if(tickLabel.charAt(k)=='0' || tickLabel.charAt(k)=='.')
                        continue;
                      else
                        break;
                    }
                  }



              Rectangle2D tickLabelBounds = tickLabelFont.getStringBounds(tickLabel,
                                                                        g2.getFontRenderContext());
              float x = (float)(plotArea.getX()
                              -tickLabelBounds.getWidth()
                              -tickLabelInsets.left-tickLabelInsets.right);
              float y = (float)(yy+(tickLabelBounds.getHeight()/2));

              if(sum==getRange().getLowerBound())
               y=(float)plotArea.getMaxY();


            /**
             * Code added to remove the overlapping of the tickLabels
             */
             if((y>y0 || (upperBound-lowerBound>3)) && j!=1)
                tickLabel="";
              else {
                if(j==1 && y>y0 )
                 removePreviousTick();
                 y0=y-tickLabelBounds.getHeight()-0.25;
              }
             // System.out.println("VerticalLogarithmicAxis:currenttickValue->"+currentTickValue+";TickLabel->"+tickLabel);
              Tick tick = new Tick(new Double(currentTickValue), tickLabel, x, y);
	      ticks.add(tick);
	}
       }

    }

    /**
     * removes the prevois nine so that powers of 10 can be displayed
     * It sees whether there is 9 at previous position which is overlapping
     * If that is so, then set the text of previous text to be ""
     */
    private void removePreviousTick() {
      int size = ticks.size();
      if(size==0)
         return;
      for(int i=size-1;i>0;--i) {
        Tick tick = (Tick)ticks.get(i);
        if(tick.getText().trim().equalsIgnoreCase(""))
          continue;
        ticks.remove(i);
        ticks.add(new Tick(new Double(tick.getNumericalValue()),"",tick.getX(),tick.getY()));
        return;
      }
  }



  /**
   * checks to see whether num is a power of a ten or not
   * returns true if number is a power of ten else returns false
   * @param num
   */
  private boolean isPowerOfTen(double num) {
    if(num>=2 && num<=9)
      return false;
    return true;
 }

  /**
    * this function is used to find the nearest power of 10 for any number passed as the parameter
    * this function is used for computing the difference in the power of 10
    * for the lowerBound and UpperBounds of the range, to enable the superscript labeing of the ticks
    * @param num
    * @return
    */
   private int powerOf10(double num) {

       int i=lowest;
       while(num >= Math.pow(10,i)){
          if(num>=Math.pow(10,i) && num<Math.pow(10,i+1))
             return i;
          ++i;
       }
      return 0;
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
