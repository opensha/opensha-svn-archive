package org.scec.sha.gui.infoTools;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.data.LineFunction2D;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.data.statistics.Statistics;

import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;

/** Creates scatter plots from data values and returns statistics
 *
 * @author Vijesh Mehta
 */
public class ScatterPlot {

        /** Values for x Axis */
        List xAxisValues;
        /** Values for y Axis */
        List yAxisValues;
        /** holds the series of functions to plot */
        private DiscretizedFuncList functions = new DiscretizedFuncList();
        private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

        /**
         * Constructs a Scatter Plot Object using a set of values for each Axis
         *
         * @param xAxis the set of values for the x axis
         * @param yAxis the set of values for the y axis
         */
        public ScatterPlot(List xAxis, List yAxis) {
          data.setFunctions(functions);
                setXAxisValues(xAxis);
                setYAxisValues(yAxis);
        }

        /**
         * gets the list of X values
         * @return the list
         */
        public List getXAxisValues() {
                return xAxisValues;
        }

        /**
         * gets the list of y values
         * @return the list
         */
        public List getYAxisValues() {
                return yAxisValues;
        }

        /**
         * sets the x values
         * @param Xvalues
         */
        public void setXAxisValues(List Xvalues) {
                this.xAxisValues = Xvalues;
        }

        /**
         * sets the y values
         * @param Yvalues
         */
        public void setYAxisValues(List Yvalues) {
                this.yAxisValues = Yvalues;
        }

        /**
         * gets the correlation between the x and y axis values
         * @return a double with the correlation
         */
        public double getCorrelation() {

                Number [] x = new Number[xAxisValues.size()],y = new Number[yAxisValues.size()];

                for ( int i = 0; i < xAxisValues.size() && i < yAxisValues.size(); i++ ) {
                        x[i] = (Number)((Double)xAxisValues.get(i));
                        y[i] = (Number)((Double)yAxisValues.get(i));
                }

                return Statistics.getCorrelation(x,y);
        }

        /**
         * creates the Linear Fit of the x and y values
         * @return a LineFunction2D with the linear fit line for JFree
         */
        public LineFunction2D Linearfit() {

                if ( xAxisValues.size() != yAxisValues.size() ) {
                        System.out.println("Array Sizes not the same!");
                }

                Number [] x = new Number[xAxisValues.size()],y = new Number[yAxisValues.size()];

                for ( int i = 0; i < xAxisValues.size() && i < yAxisValues.size(); i++ ) {
                        x[i] = (Number)((Double)xAxisValues.get(i));
                        y[i] = (Number)((Double)yAxisValues.get(i));
                }

                double [] result = Statistics.getLinearFit(x,y);
                LineFunction2D func = new LineFunction2D(result[0],result[1]);

                return func;
        }

        /**
         * Creates an overlaid chart.
         *
         * @return The chart.
         */
        public JFreeChart createOverlaidChart(String xLabel, String yLabel) {

                LineFunction2D average = Linearfit();

            // create plot ...
            StandardXYItemRenderer renderer1 = new StandardXYItemRenderer(org.jfree.chart.renderer.StandardXYItemRenderer.SHAPES, new StandardXYToolTipGenerator() );
            NumberAxis domainAxis = new NumberAxis(xLabel);
            NumberAxis rangeAxis = new NumberAxis(yLabel);//"Predicted data - " + imrGuiBean.getSelectedIMR_Name() + " " + imtGuiBean.getParameterListMetadataString());

            // Scatter Plot Created
            XYPlot plot = new XYPlot(data, domainAxis, rangeAxis, renderer1);

            // add a second dataset and renderer...for the Average Line
            XYSeries AverageXY = new XYSeries("Average Line");
            AverageXY.add(0, average.getValue(0));
            AverageXY.add(domainAxis.getUpperBound(), average.getValue(domainAxis.getUpperBound()));

            XYSeriesCollection seriesCollection = new XYSeriesCollection();
            seriesCollection.addSeries(AverageXY);
            XYItemRenderer renderer2 = new StandardXYItemRenderer(org.jfree.chart.renderer.StandardXYItemRenderer.LINES, new StandardXYToolTipGenerator() );
            seriesCollection.addSeries(AverageXY);
            plot.setSecondaryDataset(0, seriesCollection);
            plot.setSecondaryRenderer(0, renderer2);

            double maxXRange = domainAxis.getRange().getUpperBound();
            double maxYRange = rangeAxis.getRange().getUpperBound();

            if(maxXRange > maxYRange)
              rangeAxis.setRange(domainAxis.getRange());
            else
              domainAxis.setRange(rangeAxis.getRange());

            //rangeAxis.setRange(domainAxis.getRange());
            // return a new chart containing the overlaid plot...
            return new JFreeChart("Scatter Plot - Correlation: " + getCorrelation(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        }

        /**
         * this function sets the initial X and Y values for which a plot has to be generated.
         * Call this function first before generating a chart
         *
         * @param function : XYSeries Object
         */
        public void fillValues() {

                functions.removeAll(functions); // removes all the series in the functions
                ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
                for ( int i = 0; i < xAxisValues.size() && i < yAxisValues.size(); i++ ){
                        function.set(((Double)xAxisValues.get(i)).doubleValue(), ((Double)yAxisValues.get(i)).doubleValue());
                }

                functions.add(function); // adds new series to the functions
        }

        /** Main function - tests class methods */
        public static void main(String [] args) {
                ArrayList x = new ArrayList();
                ArrayList y = new ArrayList();
                x.add(new Double(2));
                x.add(new Double(3));
                y.add(new Double(3));
                y.add(new Double(4));
                ScatterPlot t = new ScatterPlot(x,y);

                t.getCorrelation();
        }

        /**
         *
         * @returns the info for the selected plot
         */
        public String getInfoForPlot(){
          functions.setXAxisName("Observed Rupture Data");
          functions.setYAxisName("Predicted Rupture Data");
          return functions.toString();
        }



}
