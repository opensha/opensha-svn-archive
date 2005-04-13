package org.opensha.sha.fault.gui;

import java.awt.*;
import java.util.*;
import java.awt.geom.Ellipse2D;

import javax.swing.*;
import javax.swing.border.*;

import org.opensha.gui.*;
import org.opensha.gui.plot.jfreechart.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.tooltips.*;
import org.jfree.data.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.labels.StandardXYToolTipGenerator;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class GriddedFaultPlotter extends ArrayList{

    protected final static String C = "GriddedFaultPlotter";
    protected final static boolean D = false;

    private boolean lightweight;
    private java.awt.Color plotColor = Color.black;

    public final static String X_AXIS_LABEL = "Longitude (deg.)";
    public final static String Y_AXIS_LABEL = "Latitude (deg.)";

    //protected org.jfree.chart.NumberAxis xAxis =  new org.jfree.chart.HorizontalNumberAxis( X_AXIS_LABEL );
    //protected org.jfree.chart.NumberAxis yAxis =  new org.jfree.chart.VerticalNumberAxis( Y_AXIS_LABEL );

    protected ChartPanel singleChartPanel = null;
    protected ChartPanel multiChartPanel = null;

    protected GriddedSubsetXYItemRenderer SUB_SHAPE_RENDERER = new GriddedSubsetXYItemRenderer(
                org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
                new StandardXYToolTipGenerator()
    );

    protected AdjustableScaleXYItemRenderer SHAPE_RENDERER = new AdjustableScaleXYItemRenderer(
                org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES,
                new StandardXYToolTipGenerator()
    );

    protected AdjustableScaleXYItemRenderer SHAPES_AND_LINES_RENDERER = new AdjustableScaleXYItemRenderer(
                org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES_AND_LINES,
                new StandardXYToolTipGenerator()
    );

    protected AdjustableScaleXYItemRenderer LINE_RENDERER = new AdjustableScaleXYItemRenderer(
                org.jfree.chart.renderer.StandardXYItemRenderer.LINES,
                new StandardXYToolTipGenerator()
    );

    final public static int SHAPES = 1;
    final public static int LINES = 2;
    final public static int SHAPES_AND_LINES = 3;
    final public static int SUB_SHAPES = 4;
    final public static int SHAPES_LINES_AND_SHAPES=5;

    private int plotType = 1;

    // these are coordinates and size of the circles visible in the plot
    private final static double SIZE = 6.0;
    private final static double DELTA = SIZE / 2.0;

    public GriddedFaultPlotter(){
        // X - Axis
       // xAxis.setAutoRangeIncludesZero( false );
       // xAxis.setCrosshairLockedOnData( false );
       // xAxis.setCrosshairVisible( false );

        // Y axis
        //yAxis.setAutoRangeIncludesZero( false );
        //yAxis.setCrosshairLockedOnData( false );
        //yAxis.setCrosshairVisible( false );


        int blue = 255 - plotColor.getBlue();
        int red = 255 - plotColor.getRed();
        int green = 255 - plotColor.getGreen();
        SUB_SHAPE_RENDERER.setFillColor( new Color(red, green, blue)  );
    }

    protected void setRenderer(XYPlot plot){
        switch (plotType) {
            case SHAPES:
              // set the shape so that only circles are drawn
              int numSeries = ((XYDataset)plot.getDataset()).getSeriesCount();
              for(int i=0; i<numSeries; ++i)
                SHAPE_RENDERER.setSeriesShape(i,new Ellipse2D.Double(-DELTA, -DELTA, SIZE, SIZE));
              plot.setRenderer( SHAPE_RENDERER );
              break;
            case LINES:
              plot.setRenderer( LINE_RENDERER );
              break;
            case SHAPES_AND_LINES:
              plot.setRenderer( SHAPES_AND_LINES_RENDERER );
              break;
            case SUB_SHAPES:
              // set the shape so that only circles are drawn
              int numSeries1 = ((XYDataset)plot.getDataset()).getSeriesCount();
              for(int i=0; i<numSeries1; ++i)
                SUB_SHAPE_RENDERER.setSeriesShape(i,new Ellipse2D.Double(-DELTA, -DELTA, SIZE, SIZE));
              plot.setRenderer( SUB_SHAPE_RENDERER );
              break;
            default: plot.setRenderer( SHAPE_RENDERER ); break;
        }
    }

    protected void lazyInitSinglePanel(JFreeChart chart){
        //singleChartPanel = new MyJFreeChartPanel(chart, true, true, true, true, false);
        singleChartPanel = new ChartPanel(chart, true, true, true, true, false);
        singleChartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        singleChartPanel.setMouseZoomable(true);
        singleChartPanel.setDisplayToolTips(true);
        singleChartPanel.setHorizontalAxisTrace(false);
        singleChartPanel.setVerticalAxisTrace(false);
    }

    protected void lazyInitMultiPanel(JFreeChart chart){
        //multiChartPanel = new MyJFreeChartPanel(chart, true, true, true, true, true);
        multiChartPanel = new ChartPanel(chart, true, true, true, true, true);
        multiChartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        multiChartPanel.setMouseZoomable(true);
        multiChartPanel.setDisplayToolTips(true);
        multiChartPanel.setHorizontalAxisTrace(false);
        multiChartPanel.setVerticalAxisTrace(false);
    }


    public void setLightweight(boolean lightweight) { this.lightweight = lightweight; }
    public boolean isLightweight() { return lightweight; }

    public void setPlotColor(java.awt.Color plotColor) {
        this.plotColor = plotColor;
        int blue = 255 - plotColor.getBlue();
        int red = 255 - plotColor.getRed();
        int green = 255 - plotColor.getGreen();

        SUB_SHAPE_RENDERER.setFillColor( new Color(red, green, blue)  );

    }
    public java.awt.Color getPlotColor() { return plotColor; }

    public void setPlotType(int plotType) { this.plotType = plotType; }
    public int getPlotType() { return plotType; }

    /**
     *  Adds a feature to the GraphPanel attribute of the IMRTesterApplet object
     */
    protected ChartPanel addGraphPanel(String griddedSurfaceName,double gridSpacing) {

        // Starting
        String S = C + ": addGraphPanel(): ";
        if ( D ) System.out.println( S + "Starting: ");
        if( this.size() < 1 ) return null;
        else if ( this.size() == 1 ) return createChartWithSingleDataset(griddedSurfaceName,gridSpacing);
        else return createOverlaidChart(griddedSurfaceName, gridSpacing);
    }

    private ChartPanel createChartWithSingleDataset(String griddedSurfaceName,double gridSpacing ){

        org.jfree.chart.axis.NumberAxis xAxis =  new org.jfree.chart.axis.NumberAxis( X_AXIS_LABEL );
        org.jfree.chart.axis.NumberAxis yAxis =  new org.jfree.chart.axis.NumberAxis( Y_AXIS_LABEL );
        int type = org.jfree.chart.renderer.StandardXYItemRenderer.LINES;
        org.jfree.chart.renderer.StandardXYItemRenderer renderer =
            new org.jfree.chart.renderer.StandardXYItemRenderer(type, new StandardXYToolTipGenerator() );
       // X - Axis
        xAxis.setAutoRangeIncludesZero( false );

        // Y axis
        yAxis.setAutoRangeIncludesZero( false );


        // update axis
        xAxis.setAutoRange(true) ;
        yAxis.setAutoRange(true);
        // Get the data
        XYDataset functions = (XYDataset)this.get(0);
        if( functions == null ) return null;

        // build the plot
        GeoXYPlot plot = new GeoXYPlot(functions, xAxis, yAxis,renderer);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairLockedOnData(false);
        plot.setRangeCrosshairVisible(true);

        plot.setBackgroundPaint( plotColor );
        setRenderer(plot);
        int totalSeries=functions.getSeriesCount();
        boolean showLegend = false;


        /* To set the rainbow colors based on the depth of the fault, this also overrides the colors
           being generated  in the Plot.java class constructor*/
        if(functions instanceof GriddedSurfaceXYDataSet) { // change colors for gridded plot
          //Smooth Colors transition from Red to Blue

          int count = (int)(Math.ceil(255.0/totalSeries));
          for(int i=255,j=0;i>=0;i-=count,j++)
          plot.getRenderer().setSeriesPaint(j, new Color(i,0,255-i));
          showLegend = true;
        } else if(totalSeries == 1) showLegend = true;

        // build chart
        JFreeChart chart = new JFreeChart(griddedSurfaceName, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend );
        chart.setBackgroundPaint( GriddedFaultApplet.peach );

        // create panel
        if( singleChartPanel == null ) lazyInitSinglePanel(chart);
        singleChartPanel.setChart(chart);
        return singleChartPanel;

    }



    private ChartPanel createOverlaidChart( String griddedSurfaceName, double gridSpacing) {


        org.jfree.chart.axis.NumberAxis xAxis =  new org.jfree.chart.axis.NumberAxis( X_AXIS_LABEL );
        org.jfree.chart.axis.NumberAxis yAxis =  new org.jfree.chart.axis.NumberAxis( Y_AXIS_LABEL );
        int type = org.jfree.chart.renderer.StandardXYItemRenderer.LINES;
        org.jfree.chart.renderer.StandardXYItemRenderer renderer =
            new org.jfree.chart.renderer.StandardXYItemRenderer(type, new StandardXYToolTipGenerator() );

        // X - Axis
        xAxis.setAutoRangeIncludesZero( false );
        // Y axis
        yAxis.setAutoRangeIncludesZero( false );
        xAxis.setAutoRange(true) ;
        yAxis.setAutoRange(true);

        // Get the data
        XYDataset functions ;
        functions = (XYDataset)this.get(0);
        if( functions == null ) return null;
        // multi plot
        GeoXYPlot plot = new GeoXYPlot(functions, xAxis, yAxis, renderer);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairLockedOnData(false);
        plot.setRangeCrosshairVisible(false);
        int numSeries, count, j;
        XYDataset dataset1;
        switch(plotType) {
          case SHAPES_LINES_AND_SHAPES: /** for grid plot and fault trace */
            plot.setRenderer(this.SHAPES_AND_LINES_RENDERER);
            dataset1 = (XYDataset)this.get(1);
            numSeries = dataset1.getSeriesCount();
            count = (int)(Math.ceil(255.0/numSeries));
            j=0;
            for(int i=255;i>=0;i-=count,j++) this.SHAPE_RENDERER.setSeriesPaint(j, new Color(i,0,255-i));
            for(int i=0; i<numSeries; ++i)  SHAPE_RENDERER.setSeriesShape(i,new Ellipse2D.Double(-DELTA, -DELTA, SIZE, SIZE));
            plot.setSecondaryDataset(0, dataset1);
            plot.setSecondaryRenderer(0, SHAPE_RENDERER);
            break;
          case SUB_SHAPES: /** for grid plot and sub plot */
            plot.setRenderer(this.SHAPE_RENDERER);
            numSeries = functions.getSeriesCount();
            count = (int)(Math.ceil(255.0/numSeries));
            j=0;
            for(int i=255;i>=0;i-=count,j++) this.SHAPE_RENDERER.setSeriesPaint(j, new Color(i,0,255-i));
            for(int i=0; i<numSeries; ++i) {
              SHAPE_RENDERER.setSeriesShape(i,new Ellipse2D.Double(-DELTA, -DELTA, SIZE, SIZE));
              SUB_SHAPE_RENDERER.setSeriesShape(i,new Ellipse2D.Double(-DELTA, -DELTA, SIZE, SIZE));
            }
            dataset1 = (XYDataset)this.get(1);
            int numSeries1 = dataset1.getSeriesCount();
            for(int i=0; i<numSeries1; ++i) this.SUB_SHAPE_RENDERER.setSeriesPaint(i, SUB_SHAPE_RENDERER.getFillColor());
            plot.setSecondaryDataset(0, dataset1);
            plot.setSecondaryRenderer(0,SUB_SHAPE_RENDERER);
            break;
        }
        plot.setBackgroundPaint( plotColor );
        //System.out.println("Lower: "+yAxis.getRange().getLowerBound()+"Upper: "+yAxis.getRange().getUpperBound());

        // return a new chart containing the overlaid plot...
        JFreeChart chart = new JFreeChart(griddedSurfaceName, JFreeChart.DEFAULT_TITLE_FONT, plot, !lightweight);
        chart.setBackgroundPaint( GriddedFaultApplet.peach );
        // create panel
        if( multiChartPanel == null ) lazyInitMultiPanel(chart);
        multiChartPanel.setChart(chart);
        return multiChartPanel;
     }

}
