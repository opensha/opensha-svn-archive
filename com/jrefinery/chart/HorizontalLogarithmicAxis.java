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
 * ------------------------------
 * HorizontalLogarithmicAxis.java
 * ------------------------------
 * (C) Copyright 2002, by Eric Thomas and Contributors.
 *
 * Original Author:  Nitin Gupta & Vipin gupta
 * 16-May-2002 : Version 1, based on existing HorizontalNumberAxis and VerticalLogarithmicAxis
 *               classes (ET).
 *
 */

package com.jrefinery.chart;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import com.jrefinery.data.Range;
import java.util.Iterator;
import com.jrefinery.ui.RefineryUtilities;

/**
 * A logartihmic value axis, for values displayed horizontally.  Display of
 * negative values is supported (if 'allowNegativesFlag' flag set), as well
 * as positive values arbitrarily close to zero.
 */
public class HorizontalLogarithmicAxis extends HorizontalNumberAxis  {

    public static final double LOG10_VALUE = Math.log(10);
    protected final boolean allowNegativesFlag;
    protected boolean smallLogFlag = false;
    protected final DecimalFormat numberFormatterObj =
                                               new DecimalFormat("0.00000");

    private int counter=0;
    private int lowest=-30; // lowest power of ten allowed
    /**
     * Constructs a horizontal logarithmic axis, using default values where necessary.
     */
    public HorizontalLogarithmicAxis() {

        this(null);

    }

    /**
     * Constructs a horizontal logarithmic axis, using default values where necessary.
     *
     * @param label The axis label (null permitted).
     */
    public HorizontalLogarithmicAxis(String label) {

        // Set the default min/max axis values for a logaritmic scale.
        this(label,
             Axis.DEFAULT_AXIS_LABEL_FONT,
             1,
             10);

        this.setAutoRange(true);

    }

    /**
     * Constructs a horizontal logarithmic axis.
     *
     * @param label The axis label (null permitted).
     * @param labelFont The font for displaying the axis label.
     * @param minimumAxisValue The lowest value shown on the axis.
     * @param maximumAxisValue The highest value shown on the axis.
     */
    public HorizontalLogarithmicAxis(String label,
                                   Font labelFont,
                                   double minimumAxisValue,
                                   double maximumAxisValue) {

        this(label,
             labelFont,
             Axis.DEFAULT_AXIS_LABEL_PAINT,
             Axis.DEFAULT_AXIS_LABEL_INSETS,
             true, // tick labels visible
             Axis.DEFAULT_TICK_LABEL_FONT,
             Axis.DEFAULT_TICK_LABEL_PAINT,
             Axis.DEFAULT_TICK_LABEL_INSETS,
             false,  // tick labels drawn vertically
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
             ValueAxis.DEFAULT_CROSSHAIR_PAINT,
             false);  //'allowNegativesFlag' set false for no values < 0
    }

    /**
     * Constructs a horizontal number axis.
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
     * @param allowNegativesFlag true to allow plotting of negative values;
     * false if all positive values, thus allowing values less than 1.0 and
     * arbitrarily close to zero to be plotted correctly.
     */
    public HorizontalLogarithmicAxis(String label,
                                   Font labelFont, Paint labelPaint, Insets labelInsets,
                                   boolean tickLabelsVisible, Font tickLabelFont, Paint tickLabelPaint,
                                   Insets tickLabelInsets,
                                   boolean verticalTickLabels,
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
                                   Stroke crosshairStroke, Paint crosshairPaint,
                                   boolean allowNegativesFlag) {

        super(label,
              labelFont, labelPaint, labelInsets,
              tickLabelsVisible,
              tickLabelFont, tickLabelPaint, tickLabelInsets,
              verticalTickLabels,
              tickMarksVisible,
              tickMarkStroke,tickMarkPaint,
              autoRange, autoRangeMinimumSize,autoRangeIncludesZero, autoRangeStickyZero,
              minimumAxisValue, maximumAxisValue,
              inverted,
              autoTickUnitSelection, tickUnit,
              gridLinesVisible, gridStroke, gridPaint,anchorValue,
              crosshairVisible, crosshairValue, crosshairStroke, crosshairPaint);

      this.allowNegativesFlag = allowNegativesFlag;        //save flag
      if(!autoRange)              //if not auto-ranging then
        setupSmallLogFlag();      //setup flag based on bounds values
    }

    /**
     * Overridden version that calls original and then sets up flag for
     * log axis processing.
     */
    public void setRange(Range range)
    {
      super.setRange(range);      //call parent mathod
      setupSmallLogFlag();        //setup flag based on bounds values
    }

    /**
     * Sets up flag for log axis processing.
     */
    protected void setupSmallLogFlag()
    {
              //set flag true if negative values not allowed and the
              // lower bound is between 0 and 10:
      final double lowerVal = getRange().getLowerBound();
      smallLogFlag = (!allowNegativesFlag && lowerVal < 10.0 &&
                                                            lowerVal > 0.0);
    }


    /**
     * Converts a data value to a coordinate in Java2D space, assuming that
     * the axis runs along one edge of the specified plotArea.
     * Note that it is possible for the coordinate to fall outside the
     * plotArea.
     * @param dataValue the data value.
     * @param plotArea the area for plotting the data.
     * @return The Java2D coordinate.
     */
    public double translateValueToJava2D(double value, Rectangle2D plotArea) {



        double axisMin = getRange().getLowerBound();
        double axisMax = getRange().getUpperBound();


        double maxX = plotArea.getMaxX();
        double minX = plotArea.getMinX();


        if (isInverted())
        {
          return maxX - (((value - axisMin)/(axisMax - axisMin)) *
                                                             (maxX - minX));
        }
        else
        {
          return minX + (((value - axisMin)/(axisMax - axisMin)) *
                                                             (maxX - minX));
        }

    }

    /**
    * Function added for the Log-Log plotting
    */

   public double myTranslateValueToJava2D(double value, Rectangle2D plotArea) {

	double axisMin = getRange().getLowerBound();
	double axisMax = getRange().getUpperBound();

	double maxX = plotArea.getMaxX();
	double minX = plotArea.getMinX();


        if(axisMin==0.0)
           axisMin=this.getTickUnit().getSize()/15;//.01;//this.tickUnit.getSize()/3.5;

         if(axisMin!=0)
             axisMin = Math.log(axisMin)/LOG10_VALUE;
         axisMax = Math.log(axisMax)/LOG10_VALUE;


         if (isInverted()) {
           if(axisMin==axisMax)
               return maxX;
           else
               return maxX - ((value - axisMin)/(axisMax - axisMin)) * (maxX - minX);
        }
        else {
          if(axisMin==axisMax)
               return minX;
          else
               return minX + ((value - axisMin)/(axisMax - axisMin)) * (maxX - minX);
        }
     }


    /**
     * Converts a coordinate in Java2D space to the corresponding data
     * value, assuming that the axis runs along one edge of the specified
     * plotArea.
     * @param java2DValue the coordinate in Java2D space.
     * @param plotArea the area in which the data is plotted.
     * @return The data value.
     */
    public double translateJava2DtoValue(float java2DValue, Rectangle2D plotArea) {

        double axisMin = Math.log(getRange().getLowerBound())/LOG10_VALUE;
        double axisMax = Math.log(getRange().getUpperBound())/LOG10_VALUE;

        double plotMinX = plotArea.getMinX();
        double plotMaxX = plotArea.getMaxX();

        if (isInverted())
        {
          double logVal=(plotMaxX - java2DValue)/(plotMaxX-plotMinX)*(axisMax-axisMin)+axisMin;
          return Math.pow(10,logVal);
        }
        else
        {
          double logVal=( java2DValue - plotMinX)/(plotMaxX-plotMinX)*(axisMax-axisMin)+axisMin;
          return Math.pow(10,logVal);
        }
    }

   /**
     * Rescales the axis to ensure that all data is visible.
     */
   /* public void autoAdjustRange() {

        if (plot instanceof HorizontalValuePlot)
        {
            HorizontalValuePlot hvp = (HorizontalValuePlot)plot;

            Range r = hvp.getHorizontalDataRange();
            if (r == null)
            {
              r = new Range(DEFAULT_MINIMUM_AXIS_VALUE,
                                                DEFAULT_MAXIMUM_AXIS_VALUE);
            }

            double lower = computeLogFloor(r.getLowerBound());
            if(!allowNegativesFlag && lower >= 0.0 && lower < 1e-25)
            {      //negatives not allowed and lower range bound is zero
              lower = r.getLowerBound();    //use data range bound instead
            }

//            double upper = computeLogCeil(r.getUpperBound());
            double upper = r.getUpperBound();

            if(!allowNegativesFlag && upper < 1.0 && upper > 0.0 &&
                                                                lower > 0.0)
            {      //negatives not allowed and upper bound between 0 & 1
                        //round up to nearest significant digit for bound:
                                                 //get negative exponent:
              double expVal = Math.log(upper) / LOG10_VALUE;
              expVal = Math.ceil(-expVal+0.001); //get positive exponent
              expVal = Math.pow(10,expVal);      //create multiplier value
                        //multiply, round up, and divide for bound value:
              upper = (expVal > 0.0) ? Math.ceil(upper*expVal)/expVal :
                                                           Math.ceil(upper);
            }
            else   //negatives allowed or upper bound not between 0 & 1
              upper = Math.ceil(upper);     //use nearest integer value

            // ensure the autorange is at least <minRange> in size...
            double minRange = this.autoRangeMinimumSize.doubleValue();
            if (upper-lower < minRange)
            {
              upper = (upper+lower+minRange) / 2;
              lower = (upper+lower-minRange) / 2;
            }

            this.range = new Range(lower, upper);
        }

        setupSmallLogFlag();      //setup flag based on bounds values
    }*/

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    protected void autoAdjustRange() {

        if (plot==null) return;  // no plot, no data

        if (plot instanceof HorizontalValuePlot) {
            HorizontalValuePlot hvp = (HorizontalValuePlot)plot;

            Range r = hvp.getHorizontalDataRange();
            if (r==null) r = new Range(this.DEFAULT_LOWER_BOUND,this.DEFAULT_UPPER_BOUND);
            double upper = r.getUpperBound();
            double lower = r.getLowerBound();
            this.setRange(new Range(lower, upper));
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

        double logCeil;
        if(upper > 10.0)
        {     //parameter value is > 10
          // The Math.log() function is based on e not 10.
          logCeil = Math.log(upper) / LOG10_VALUE;
          logCeil = Math.ceil(logCeil);
          logCeil = Math.pow(10, logCeil);
        }
        else if(upper < -10.0)
        {     //parameter value is < -10
                   //calculate log using positive value:
          logCeil = Math.log(-upper) / LOG10_VALUE;
                   //calculate ceil using negative value:
          logCeil = Math.ceil(-logCeil);
                   //calculate power using positive value; then negate
          logCeil = -Math.pow(10, -logCeil);
        }
        else       //parameter value is -10 > val < 10
          logCeil = Math.ceil(upper);       //use as-is
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

        double logFloor;
        if(lower > 10.0)
        {     //parameter value is > 10
          // The Math.log() function is based on e not 10.
          logFloor = Math.log(lower) / LOG10_VALUE;
          logFloor = Math.floor(logFloor);
          logFloor = Math.pow(10, logFloor);
        }
        else if(lower < -10.0)
        {     //parameter value is < -10
                   //calculate log using positive value:
          logFloor = Math.log(-lower) / LOG10_VALUE;
                   //calculate floor using negative value:
          logFloor = Math.floor(-logFloor);
                   //calculate power using positive value; then negate
          logFloor = -Math.pow(10, -logFloor);
        }
        else       //parameter value is -10 > val < 10
          logFloor = Math.floor(lower);     //use as-is
        return logFloor;          //return zero
    }





    /**
     * Calculates the positions of the tick labels for the axis, storing the results in the
     * tick label list (ready for drawing).
     *
     * @param g2 The graphics device.
     * @param drawArea The area in which the plot and the axes should be drawn.
     * @param plotArea The area in which the plot should be drawn.
     */
    public void refreshTicks(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea)throws java.lang.ArithmeticException {


       ++counter;
      	this.ticks.clear();

        g2.setFont(tickLabelFont);

	if (this.isAutoTickUnitSelection()) {
	    selectAutoTickUnit(g2, drawArea, plotArea);
	}
        double size = this.getTickUnit().getSize();
	int count = this.calculateVisibleTickCount();
	double x0=0.0;
        double sum=0.0f;
        int i=lowest;



        // see whther there exists any major axis in data
        double lower = getRange().getLowerBound();
        double upper = getRange().getUpperBound();
        if(lower==0.0 || upper==0.0)
               throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist for X-Log Plot");
        for( i=lowest;;++i) {
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

         // check whether we want to show in superscript form
        int upperBound =powerOf10(getRange().getUpperBound());
        int lowerBound=powerOf10(getRange().getLowerBound());
        boolean superscript=false;

        // whether you want to show in superscript form or not
        if((upperBound-lowerBound) >= 4)
          superscript=true;

        if(getRange().getLowerBound()<0.001 || getRange().getUpperBound()>1000.0)
          superscript=true;


         /**
          * For Loop - Drawing the ticks which corresponds to the  power of 10
          */

        for (i=lowest;;i++) {
          for(int j=0;j<10;++j){
              sum =Double.parseDouble(j+"e"+i);
              if(sum<getRange().getLowerBound())
                 continue;
              if(sum>getRange().getUpperBound())
                 return;
               double currentTickValue = sum ;
               double val=currentTickValue;
               double logval;
               double xx;


               if(sum==getRange().getLowerBound())
                 xx = plotArea.getMinX();
               else if(sum==getRange().getUpperBound())
                 xx=plotArea.getMaxX();
               else {
                logval=Math.log(val)/LOG10_VALUE;
	        xx = this.myTranslateValueToJava2D(logval, plotArea);
                }


                if(sum<=0.0)
                   throw new java.lang.ArithmeticException("Log Value of the negative values and 0 does not exist for X-Log Plot");



	        String tickLabel = new String(""+currentTickValue);
                if(j!=1) // for minor axis, just display 2 to 9
                  tickLabel=""+j;
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
	    float x = 0.0f;
	    float y = 0.0f;
	    if (this.getVerticalTickLabels()) {
		x = (float)(xx+tickLabelBounds.getHeight()/2);
		y = (float)(plotArea.getMaxY()+tickLabelInsets.top+tickLabelBounds.getWidth());
	    }
	    else {
              x = (float)(xx-tickLabelBounds.getWidth()/2);
              y = (float)(plotArea.getMaxY()+tickLabelInsets.top+tickLabelBounds.getHeight());
	    }

            if(currentTickValue==getRange().getUpperBound())
               x=x-7;

            /**
            * Code added to prevent overlapping of the Tick Labels.
            */
             if((x<x0 || (upperBound-lowerBound>3)) && j!=1){
                tickLabel="";
             }
             else {
               if(j==1 && x<x0 )
                 removePreviousTick();
               x0=x+tickLabelBounds.getWidth()+3;
             }
             // System.out.println("HorizontalLogarithmicAxis:refreshTicks:tickLabel:="+tickLabel);
             Tick tick = new Tick(new Double(currentTickValue), tickLabel, x, y);
             ticks.add(tick);
	}
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
     * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
     *
     * @param g2 The graphics device.
     * @param drawArea The area within which the chart should be drawn.
     * @param plotArea The area within which the plot should be drawn (a subset of the drawArea).
     */
    public void draw(Graphics2D g2, Rectangle2D drawArea, Rectangle2D plotArea) {

        if (!visible) return;

        // draw the axis label...
        if (label!=null) {
            g2.setFont(labelFont);
            g2.setPaint(labelPaint);
            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D labelBounds = labelFont.getStringBounds(label, frc);
            LineMetrics lm = labelFont.getLineMetrics(label, frc);
            float labelx = (float)(plotArea.getX()+plotArea.getWidth()/2-labelBounds.getWidth()/2);
            float labely = (float)(drawArea.getMaxY()-labelInsets.bottom
                                                     -lm.getDescent()
                                                     -lm.getLeading());
            g2.drawString(label, labelx, labely);
        }

        // draw the tick labels and marks
        this.refreshTicks(g2, drawArea, plotArea);

        float maxY = (float)plotArea.getMaxY();
        g2.setFont(getTickLabelFont());
        Iterator iterator = ticks.iterator();
         while (iterator.hasNext()) {
             Tick tick = (Tick)iterator.next();
             float xx = (float)tick.getX();
             double val=1;
             int eIndex =tick.getText().indexOf("E");

             // check whether this is minor axis. for minor axis we save,2-9 in label
             if(!tick.getText().trim().equalsIgnoreCase("") && eIndex==-1)
               val = Double.parseDouble(tick.getText());

             double logval=Math.log(tick.getNumericalValue())/LOG10_VALUE;
             xx = (float)this.myTranslateValueToJava2D(logval, plotArea);
             if(!isPowerOfTen(val)) // for major axis
               g2.setFont(tickLabelFont);
             else  // show minor axis in smaller font
               g2.setFont(new Font(tickLabelFont.getName(),tickLabelFont.getStyle(),tickLabelFont.getSize()+3));
             if (tickLabelsVisible) {
                 g2.setPaint(this.tickLabelPaint);
                 if (this.getVerticalTickLabels()) {
                     RefineryUtilities.drawRotatedString(tick.getText(), g2,
                                                         tick.getX(), tick.getY(), -Math.PI/2);

                 }
                 else {
                   if(eIndex==-1)
                     g2.drawString(tick.getText(), tick.getX(), tick.getY());
                   else { // show in superscript form
                             g2.drawString("10", tick.getX(), tick.getY());
                             g2.setFont(new Font(tickLabelFont.getName(),tickLabelFont.getStyle(),tickLabelFont.getSize()-2));
                             g2.drawString(tick.getText().substring(eIndex+1),tick.getX()+16,tick.getY()-6);
                   }
                 }
             }



             if (tickMarksVisible) {
                 g2.setStroke(this.getTickMarkStroke());
                 Line2D mark = new Line2D.Float(xx, maxY-2, xx, maxY+2);
                 g2.draw(mark);
             }
             if (isGridLinesVisible()) {
                 g2.setStroke(getGridStroke());
                 g2.setPaint(getGridPaint());
                 Line2D gridline = new Line2D.Float(xx, (float)plotArea.getMaxY(), xx,
                                                    (float)plotArea.getMinY());
                 g2.draw(gridline);
             }

         }
     }


    /**
     * Returns the log10 value, depending on if values between 0 and
     * 1 are being plotted.
     */
    protected double switchedLog10(double val)
    {
      return smallLogFlag ? Math.log(val)/LOG10_VALUE : adjustedLog10(val);
    }

    /**
     * Returns an adjusted log10 value for graphing purposes.  The first
     * adjustment is that negative values are changed to positive during
     * the calculations, and then the answer is negated at the end.  The
     * second is that, for values less than 10, an increasingly large
     * (0 to 1) scaling factor is added such that at 0 the value is
     * adjusted to 1, resulting in a returned result of 0.
     */
    public double adjustedLog10(double val)
    {
      final boolean negFlag;
      if(negFlag = (val < 0.0))
        val = -val;          //if negative then set flag and make positive
      if(val < 10.0)                   //if < 10 then
        val += (10.0-val) / 10;        //increase so 0 translates to 0
              //return value; negate if original value was negative:
      return negFlag ? -(Math.log(val) / LOG10_VALUE) :
                                              (Math.log(val) / LOG10_VALUE);
    }

    /**
     * Selects an appropriate tick value for the axis.  The strategy is to display as many ticks as
     * possible (selected from an array of 'standard' tick units) without the labels overlapping.
     *
     * @param g2 The graphics device.
     * @param drawArea The area in which the plot and axes should be drawn.
     * @param dataArea The area defined by the axes.
     */
   protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D drawArea, Rectangle2D dataArea) {

        double tickLabelWidth = estimateMaximumTickLabelWidth(g2, getTickUnit());

        double zero = this.translateValueToJava2D(0.0, dataArea);

        // start with the current tick unit...
        TickUnit unit1 = this.getStandardTickUnits().getCeilingTickUnit(getTickUnit());
        double x1 = this.translateValueToJava2D(unit1.getSize(), dataArea);
        double unit1Width = Math.abs(x1-zero);

        // then extrapolate...
        double guess = (tickLabelWidth/unit1Width) * unit1.getSize();

        NumberTickUnit unit2 = (NumberTickUnit)getStandardTickUnits().getCeilingTickUnit(guess);
        double x2 = this.translateValueToJava2D(unit2.getSize(), dataArea);
        double unit2Width = Math.abs(x2-zero);

        tickLabelWidth = estimateMaximumTickLabelWidth(g2, unit2);
        if (tickLabelWidth>unit2Width) {
            unit2 = (NumberTickUnit)getStandardTickUnits().getLargerTickUnit(unit2);
        }

        this.setTickUnit(unit2);

    }

    /**
     * Estimates the maximum width of the tick labels, assuming the specified tick unit is used.
     */
    private double estimateMaximumTickLabelWidth(Graphics2D g2, TickUnit tickUnit) {

        double result = this.tickLabelInsets.left+this.tickLabelInsets.right;

        FontRenderContext frc = g2.getFontRenderContext();
        if (getVerticalTickLabels()) {
            result += tickLabelFont.getStringBounds("0", frc).getHeight();
        }
        else {

            // rather than look at the width of every tick, just consider the width of the lower
            // and upper bounds on the axis...these will usually be representative...
            double lower = getRange().getLowerBound();
            double upper = getRange().getUpperBound();
            String lowerStr = tickUnit.valueToString(lower);
            String upperStr = tickUnit.valueToString(upper);
            double w1 = tickLabelFont.getStringBounds(lowerStr, frc).getWidth();
            double w2 = tickLabelFont.getStringBounds(upperStr, frc).getWidth();
            result += Math.max(w1, w2);

        }

        return result;

    }




}
