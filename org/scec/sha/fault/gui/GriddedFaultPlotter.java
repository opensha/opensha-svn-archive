package org.scec.sha.fault.gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.scec.gui.*;
import org.scec.gui.plot.jfreechart.*;
import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;


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
    private java.awt.Color plotColor = Color.white;

    public final static String X_AXIS_LABEL = "Longitude (deg.)";
    public final static String Y_AXIS_LABEL = "Latitude (deg.)";

    //protected com.jrefinery.chart.NumberAxis xAxis =  new com.jrefinery.chart.HorizontalNumberAxis( X_AXIS_LABEL );
    //protected com.jrefinery.chart.NumberAxis yAxis =  new com.jrefinery.chart.VerticalNumberAxis( Y_AXIS_LABEL );

    protected ChartPanel singleChartPanel = null;
    protected ChartPanel multiChartPanel = null;

    protected GriddedSubsetXYItemRenderer SUB_SHAPE_RENDERER = new GriddedSubsetXYItemRenderer(
                com.jrefinery.chart.renderer.StandardXYItemRenderer.SHAPES,
                new StandardXYToolTipGenerator()
    );

    protected AdjustableScaleXYItemRenderer SHAPE_RENDERER = new AdjustableScaleXYItemRenderer(
                com.jrefinery.chart.renderer.StandardXYItemRenderer.SHAPES,
                new StandardXYToolTipGenerator()
    );

    protected AdjustableScaleXYItemRenderer SHAPES_AND_LINES_RENDERER = new AdjustableScaleXYItemRenderer(
                com.jrefinery.chart.renderer.StandardXYItemRenderer.SHAPES_AND_LINES,
                new StandardXYToolTipGenerator()
    );

    protected AdjustableScaleXYItemRenderer LINE_RENDERER = new AdjustableScaleXYItemRenderer(
                com.jrefinery.chart.renderer.StandardXYItemRenderer.LINES,
                new StandardXYToolTipGenerator()
    );

    final public static int SHAPES = 1;
    final public static int LINES = 2;
    final public static int SHAPES_AND_LINES = 3;
    final public static int SUB_SHAPES = 4;
    final public static int SHAPES_LINES_AND_SHAPES=5;

    private int plotType = 1;

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

    protected void setRenderer(GeoXYPlot plot){
        switch (plotType) {
            case SHAPES: plot.setRenderer( SHAPE_RENDERER ); break;
            case LINES: plot.setRenderer( LINE_RENDERER ); break;
            case SHAPES_AND_LINES: plot.setRenderer( SHAPES_AND_LINES_RENDERER ); break;
            case SUB_SHAPES: plot.setRenderer( SUB_SHAPE_RENDERER ); break;
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

        com.jrefinery.chart.axis.NumberAxis xAxis =  new com.jrefinery.chart.axis.HorizontalNumberAxis( X_AXIS_LABEL );
        com.jrefinery.chart.axis.NumberAxis yAxis =  new com.jrefinery.chart.axis.VerticalNumberAxis( Y_AXIS_LABEL );
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
        GeoXYPlot plot = new GeoXYPlot(functions, xAxis, yAxis);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairLockedOnData(false);
        plot.setRangeCrosshairVisible(true);

        /* To set the rainbow colors based on the depth of the fault, this also overrides the colors
           being generated  in the Plot.java class constructor*/
        //Smooth Colors transition from Red to Blue
        int totalSeries=functions.getSeriesCount();
        Paint[] seriesPaint = new Paint[totalSeries+2];
        int count = (int)(Math.ceil(255.0/totalSeries));
        for(int i=255,j=0;i>=0;i-=count,j++) {
            seriesPaint[j]=new Color(i,0,255-i);
        }
        plot.setBackgroundPaint( plotColor );
        setRenderer(plot);
        // build chart
        JFreeChart chart = new JFreeChart(griddedSurfaceName, JFreeChart.DEFAULT_TITLE_FONT, plot, true );
        chart.setBackgroundPaint( GriddedFaultApplet.peach );

        // create panel
        if( singleChartPanel == null ) lazyInitSinglePanel(chart);
        singleChartPanel.setChart(chart);
        return singleChartPanel;

    }



    private ChartPanel createOverlaidChart( String griddedSurfaceName, double gridSpacing) {


        com.jrefinery.chart.axis.NumberAxis xAxis =  new com.jrefinery.chart.axis.HorizontalNumberAxis( X_AXIS_LABEL );
        com.jrefinery.chart.axis.NumberAxis yAxis =  new com.jrefinery.chart.axis.VerticalNumberAxis( Y_AXIS_LABEL );
      // X - Axis
        xAxis.setAutoRangeIncludesZero( false );

      // Y axis
        yAxis.setAutoRangeIncludesZero( false );


        // axis

        xAxis.setAutoRange(true) ;
        yAxis.setAutoRange(true);
        // multi plot
        OverlaidGeoXYPlot plot = new OverlaidGeoXYPlot(xAxis, yAxis);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairLockedOnData(false);
        plot.setRangeCrosshairVisible(false);

        // Get the data
        XYDataset functions ;
        if(this.plotType == this.SHAPES_LINES_AND_SHAPES)
            functions = (XYDataset)this.get(1);
        else
            functions = (XYDataset)this.get(0);

        if( functions == null ) return null;

        /* To set the rainbow colors based on the depth of the fault, this also overrides the colors
           being generated  in the Plot.java class constructor*/
        //Smooth Colors transition from Red to Blue
        int totalSeries=functions.getSeriesCount();
        Paint[] seriesPaint = new Paint[totalSeries+2];
        int count = (int)(Math.ceil(255.0/totalSeries));
        int j=0;
        for(int i=255;i>=0;i-=count,j++) {
            seriesPaint[j]=new Color(i,0,255-i);
         }
         int numSeries = j;
         for(int i=0; i < numSeries; ++i) plot.getRenderer().setSeriesPaint(i,seriesPaint[i]);
         plot.setBackgroundPaint( plotColor );


        // Add all subplots
        int last = this.size();
        int counter = 0;
        ListIterator it = this.listIterator();
        while(it.hasNext()){

            counter++;
            XYDataset dataSet = (XYDataset)it.next();
            GeoXYPlot plot1 = new GeoXYPlot(dataSet, null, null);
            for(int i=0; i < numSeries; ++i) plot1.getRenderer().setSeriesPaint(i,seriesPaint[i]);
            plot1.setBackgroundPaint( plotColor );

            if( plotType == SUB_SHAPES){
              if( counter == last ) {
                plot1.setRenderer( SUB_SHAPE_RENDERER );
              }
              else plot1.setRenderer( SHAPE_RENDERER );
            }else if(plotType==SHAPES_LINES_AND_SHAPES) {
              if( counter == last )
                plot1.setRenderer(SHAPE_RENDERER);
              else
                plot1.setRenderer(SHAPES_AND_LINES_RENDERER);
            }
            else setRenderer(plot1);

            plot.add(plot1);

     }

        // return a new chart containing the overlaid plot...
        JFreeChart chart = new JFreeChart(griddedSurfaceName, JFreeChart.DEFAULT_TITLE_FONT, plot, !lightweight);
        chart.setBackgroundPaint( GriddedFaultApplet.peach );

        // create panel
        if( multiChartPanel == null ) lazyInitMultiPanel(chart);
        multiChartPanel.setChart(chart);
        return multiChartPanel;

 }



}
