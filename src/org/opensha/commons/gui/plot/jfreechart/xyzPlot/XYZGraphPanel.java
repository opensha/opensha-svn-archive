package org.opensha.commons.gui.plot.jfreechart.xyzPlot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.Range;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.gui.plot.GraphPanel;
import org.opensha.commons.gui.plot.PlotPreferences;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.jfreechart.JFreeLogarithmicAxis;
import org.opensha.commons.gui.plot.jfreechart.MyTickUnits;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * XYZ version of GraphPanel - this plots XYZ data in JFreeChart using CPT instances. A few
 * special things to note:<br>
 * <br>
 * Every block (colored rectangle plotted at a data point) is plotted at a global thickness for
 * that plot. If XYZPlotSpec's don't specify this thickness, it is determined from the XYZ_DataSet.
 * For EvenlyDiscrXYZ_DataSet instances, the grid spacing will be used. For other (arbitrarily discretized)
 * datasets, grid spacing is crudely estimated as the median distance between neighboring (by index) points
 * in the dataset.<br>
 * <br>
 * Multiple XYZ plots can be combined in a single plot (sharing 1 axis) as with GraphPanel, but the color
 * scales will all be placed together (at the top by default, but optionally at the bottom by setting the
 * legend location in the first XYZPlotSpec instance).
 * @author kevin
 *
 */
public class XYZGraphPanel extends JPanel {

	private boolean combinedYAxis = false;
	private boolean xAxisInverted = false;
	private boolean yAxisInverted = false;
	
	private ValueAxis xAxis, yAxis;
	private XYPlot plot;

	private PlotPreferences plotPrefs;
	
	private ChartPanel chartPanel;
	
	private static PlotPreferences getDefault() {
		PlotPreferences prefs = PlotPreferences.getDefault();
		prefs.setBackgroundColor(Color.WHITE);
		return prefs;
	}
	
	public XYZGraphPanel() {
		this(getDefault());
	}
	
	public XYZGraphPanel(PlotPreferences plotPrefs) {
		this.plotPrefs = plotPrefs;
	}

	public void drawPlot(XYZPlotSpec spec, boolean xLog, boolean yLog,
			Range xRange, Range yRange) {
		drawPlot(Lists.newArrayList(spec), xLog, yLog, Lists.newArrayList(xRange), Lists.newArrayList(yRange));
	}

	public void drawPlot(List<XYZPlotSpec> specs, boolean xLog, boolean yLog,
			List<Range> xRanges, List<Range> yRanges) {
		drawPlot(specs, xLog, yLog, xRanges, yRanges, null);
	}

	public void drawPlot(List<XYZPlotSpec> specs, boolean xLog, boolean yLog,
			List<Range> xRanges, List<Range> yRanges, List<XYPlot> extraPlots) {
		
		this.removeAll();

		// make sure ranges aren't specified, or only one has multiple (for combined plots)
		Preconditions.checkArgument(xRanges == null || yRanges == null
				|| xRanges.size() <= 1 || yRanges.size() <= 1);

		if (specs.size() > 1) {
			if (xRanges != null && xRanges.size() > 1)
				combinedYAxis = true;
			else if (yRanges != null && yRanges.size() > 1)
				combinedYAxis = false;
		}

		//getting the axis font size
		int axisFontSize = plotPrefs.getAxisLabelFontSize();
		//getting the tick label font size
		int tickFontSize = plotPrefs.getTickLabelFontSize();

		//create the standard ticks so that smaller values too can plotted on the chart
		TickUnits units = MyTickUnits.createStandardTickUnits();

		List<ValueAxis> subXAxis = Lists.newArrayList();
		List<ValueAxis> subYAxis = Lists.newArrayList();

		for (int i=0; i<specs.size(); i++) {
			XYZPlotSpec spec = specs.get(i);

			ValueAxis xAxis, yAxis;
			try{

				/// check if x log is selected or not
				if (xLog) {
					JFreeLogarithmicAxis logAxis = new JFreeLogarithmicAxis(spec.getXAxisLabel());
					// this fixes the overlap issue with the bottom of the plot
					logAxis.setVerticalAnchorShift(4);
					xAxis = logAxis;
				}
				else xAxis = new NumberAxis( spec.getXAxisLabel() );

				//if (!xLog)
				//  xAxis.setAutoRangeIncludesZero(true);
				// else
				if (xAxis instanceof NumberAxis)
					((NumberAxis)xAxis).setAutoRangeIncludesZero( false );
				xAxis.setStandardTickUnits(units);
				xAxis.setTickMarksVisible(false);
				xAxis.setTickLabelInsets(new RectangleInsets(3, 10, 3, 10));
				//Axis label font
				Font axisLabelFont = xAxis.getLabelFont();
				xAxis.setLabelFont(new Font(axisLabelFont.getFontName(),axisLabelFont.getStyle(),axisFontSize));

				//tick label font
				Font axisTickFont = xAxis.getTickLabelFont();
				xAxis.setTickLabelFont(new Font(axisTickFont.getFontName(),axisTickFont.getStyle(),tickFontSize));

				if (xAxisInverted)
					xAxis.setInverted(true);

				//added to have the minimum range within the Upper and Lower Bound of the Axis
				//xAxis.setAutoRangeMinimumSize(.1);

				/* to set the range of the axis on the input from the user if the range combo box is selected*/
				if (xRanges != null && xRanges.size() > i && xRanges.get(i) != null)
					xAxis.setRange(xRanges.get(i));

			}catch(Exception e){
				//e.printStackTrace();
//				JOptionPane.showMessageDialog(this,e.getMessage(),"X-Plot Error",JOptionPane.OK_OPTION);
//				graphOn=false;
//				xLog = false;
//				xAxis = prevXAxis;
//				logErrorFlag = true;
				throw ExceptionUtils.asRuntimeException(e);
			}

			try{
				/// check if y log is selected or not
				if(yLog) yAxis = new JFreeLogarithmicAxis(spec.getYAxisLabel());
				else yAxis = new NumberAxis( spec.getYAxisLabel() );

				if (yAxis instanceof NumberAxis) {
					if (!yLog)
						((NumberAxis)yAxis).setAutoRangeIncludesZero(true);
					else
						((NumberAxis)yAxis).setAutoRangeIncludesZero( false );
				}

				yAxis.setStandardTickUnits(units);
				yAxis.setTickMarksVisible(false);

				//Axis label font
				Font axisLabelFont = yAxis.getLabelFont();
				yAxis.setLabelFont(new Font(axisLabelFont.getFontName(),axisLabelFont.getStyle(),axisFontSize));

				//tick label font
				Font axisTickFont = yAxis.getTickLabelFont();
				yAxis.setTickLabelFont(new Font(axisTickFont.getFontName(),axisTickFont.getStyle(),tickFontSize));
				//added to have the minimum range within the Upper and Lower Bound of the Axis
				//yAxis.setAutoRangeMinimumSize(.1);

				if (yAxisInverted)
					yAxis.setInverted(true);

				/* to set the range of the axis on the input from the user if the range combo box is selected*/
				if (yRanges != null && yRanges.size() > i && yRanges.get(i) != null)
					yAxis.setRange(yRanges.get(i));

			}catch(Exception e){
				//e.printStackTrace();
//				JOptionPane.showMessageDialog(this,e.getMessage(),"Y-Plot Error",JOptionPane.OK_OPTION);
//				graphOn=false;
//				yLog = false;
//				yAxis = prevYAxis;
//				logErrorFlag = false;
				throw ExceptionUtils.asRuntimeException(e);
			}

			subXAxis.add(xAxis);
			subYAxis.add(yAxis);

			if (i == 0) {
				this.xAxis = xAxis;
				this.yAxis = yAxis;
			}
		}

		plot = null;
		// build the plot
		if (specs.size() == 1) {
			plot = new XYPlot(null, xAxis, yAxis, null);
		} else if (combinedYAxis) {
			plot = new CombinedRangeXYPlot(yAxis);
			((CombinedRangeXYPlot)plot).setGap(30);
		} else {
			plot = new CombinedDomainXYPlot(xAxis);
			((CombinedDomainXYPlot)plot).setGap(30);
		}
		
		GraphPanel.setupPlot(plot, tickFontSize);
		
		List<PaintScaleLegend> legends = Lists.newArrayList();
		
		for (int p=0; p<specs.size(); p++) {
			XYZPlotSpec spec = specs.get(p);
			XYPlot subPlot;
			if (specs.size()>1) {
				ValueAxis myXAxis, myYAxis;
				// this is a subPlot
				if (combinedYAxis) {
					// need a new X axis
					myYAxis = yAxis;
					myXAxis = subXAxis.get(p);
				} else {
					// need a new Y axis
					myXAxis = xAxis;
					myYAxis = subYAxis.get(p);
				}
				
				subPlot = new XYPlot(null, myXAxis, myYAxis, null);
				GraphPanel.setupPlot(subPlot, tickFontSize);
			} else {
				subPlot = plot;
			}
			
			XYZ_DataSet xyz = spec.getXYZ_Data();
			Double thickness = spec.getThickness();
			if (thickness == null) {
				if (xyz instanceof EvenlyDiscrXYZ_DataSet) {
					thickness = ((EvenlyDiscrXYZ_DataSet)xyz).getGridSpacing();
				} else {
					// detect from data - use median of differences
					int numToCheck = xyz.size();
					if (numToCheck > 1000)
						numToCheck = 1000;
					if (numToCheck > 0) {
						double[] diffs = new double[numToCheck];
						Point2D prevPt = xyz.getPoint(0);
						for (int i=1; i<numToCheck; i++) {
							Point2D pt = xyz.getPoint(i);
							
							double diffX = Math.abs(pt.getX() - prevPt.getX());
							double diffY = Math.abs(pt.getY() - prevPt.getY());
							
							if (diffX > diffY)
								diffs[i] = diffX;
							else
								diffs[i] = diffY;
							
							prevPt = pt;
						}
						thickness = DataUtils.median(diffs);
					}
				}
			}
			XYZDatasetWrapper dataset = new XYZDatasetWrapper(spec);
			XYBlockRenderer renderer = new XYBlockRenderer();
			renderer.setBlockHeight(thickness);
			renderer.setBlockWidth(thickness);
			PaintScaleWrapper scale = new PaintScaleWrapper(spec.getCPT());
	        renderer.setPaintScale(scale);
			subPlot.setRenderer(renderer);
			subPlot.setDataset(dataset);
			
			ValueAxis fakeZAxis = new NumberAxis();
			fakeZAxis.setLowerBound(scale.getLowerBound());
			fakeZAxis.setUpperBound(scale.getUpperBound());
			fakeZAxis.setLabel(spec.getZAxisLabel());
			Font axisLabelFont = fakeZAxis.getLabelFont();
			fakeZAxis.setLabelFont(new Font(axisLabelFont.getFontName(),axisLabelFont.getStyle(),axisFontSize));
			PaintScaleLegend legend = new PaintScaleLegend(scale, fakeZAxis);
			if (spec.getLegendPosition() != null)
				legend.setPosition(spec.getLegendPosition());
			legend.setPadding(5d, 15d, 5d, 15d);
			legends.add(legend);
			
			// now add any annotations
			if (spec.getPlotAnnotations() != null)
				for (XYAnnotation a : spec.getPlotAnnotations())
					subPlot.addAnnotation(a);
			
			// multiple plots
			if (plot instanceof CombinedRangeXYPlot)
				((CombinedRangeXYPlot)plot).add(subPlot);
			else if (plot instanceof CombinedDomainXYPlot)
				((CombinedDomainXYPlot)plot).add(subPlot);
			
//			plot.add
		}
		
		if (extraPlots != null) {
			for (XYPlot subPlot : extraPlots) {
				if (plot instanceof CombinedRangeXYPlot)
					((CombinedRangeXYPlot)plot).add(subPlot);
				else if (plot instanceof CombinedDomainXYPlot)
					((CombinedDomainXYPlot)plot).add(subPlot);
			}
		}
		
		//getting the tick label font size
		int plotLabelFontSize = plotPrefs.getPlotLabelFontSize();

		Font defaultPlotLabelFont = JFreeChart.DEFAULT_TITLE_FONT;
		Font newPlotLabelFont = new Font(defaultPlotLabelFont.getFontName(),defaultPlotLabelFont.getStyle(),plotLabelFontSize);

		//giving off all the data that needs to be plotted to JFreechart, which return backs
		//a panel of curves,
		JFreeChart chart = new JFreeChart(specs.get(0).getTitle(), newPlotLabelFont, plot, false );
		for (int i=0; i<legends.size(); i++)
			chart.addSubtitle(i, legends.get(i));

		chart.setBackgroundPaint( plotPrefs.getBackgroundColor() );

		// Put into a panel
		chartPanel = new ChartPanel(chart, true, true, true, true, false);

		//chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) ); TODO clean
		chartPanel.setBorder(BorderFactory.createLineBorder(Color.gray,1));
		chartPanel.setMouseZoomable(true);
		chartPanel.setDisplayToolTips(true);
		chartPanel.setHorizontalAxisTrace(false);
		chartPanel.setVerticalAxisTrace(false);
		
		this.add(chartPanel);
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	/**
	 * Allows the user to save the chart as PNG.
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsPNG(String fileName) throws IOException {
		saveAsPNG(fileName, chartPanel.getWidth(), chartPanel.getHeight());
	}
	
	/**
	 * Allows the user to save the chart as PNG.
	 * @param fileName
	 * @throws IOException
	 */
	public void saveAsPNG(String fileName, int width, int height) throws IOException {
		ChartUtilities.saveChartAsPNG(new File(fileName),chartPanel.getChart() , 
				width, height);
	}

	/**
	 * Allows the user to save the chart contents and metadata as PDF.
	 * This allows to preserve the color coding of the metadata.
	 * @throws IOException
	 */
	public void saveAsPDF(String fileName) throws IOException {
		int width = chartPanel.getWidth();
		int height = chartPanel.getHeight();
		this.saveAsPDF(fileName, width, height);
	}

	/**
	 * Allows the user to save the chart contents and metadata as PDF.
	 * This allows to preserve the color coding of the metadata.
	 * @throws IOException
	 */
	public void saveAsPDF(String fileName, int width, int height) throws IOException {
		// step 1
		Document metadataDocument = new Document(new com.lowagie.text.Rectangle(
				width, height));
		metadataDocument.addAuthor("OpenSHA");
		metadataDocument.addCreationDate();
		HeaderFooter footer = new HeaderFooter(new Phrase("Powered by OpenSHA"), true);
		metadataDocument.setFooter(footer);
		try {
			// step 2
			PdfWriter writer;

			writer = PdfWriter.getInstance(metadataDocument,
					new FileOutputStream(fileName));
			// step 3
			metadataDocument.open();
			// step 4
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2d = tp.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
			chartPanel.getChart().draw(g2d, r2d);
			g2d.dispose();
			cb.addTemplate(tp, 0, 0);
		}
		catch (DocumentException de) {
			de.printStackTrace();
		}
		// step 5
		metadataDocument.close();
	}
	
	private class XYZDatasetWrapper extends AbstractXYZDataset {
		
		private XYZ_DataSet xyz;
		private String zLabel;
		
		public XYZDatasetWrapper(XYZPlotSpec spec) {
			this(spec.getXYZ_Data(), spec.getZAxisLabel());
		}
		
		public XYZDatasetWrapper(XYZ_DataSet xyz, String zLabel) {
			this.xyz = xyz;
			this.zLabel = zLabel;
		}

		@Override
		public Number getZ(int series, int item) {
			return xyz.get(item);
		}

		@Override
		public int getItemCount(int series) {
			return xyz.size();
		}

		@Override
		public Number getX(int series, int item) {
			return xyz.getPoint(item).getX();
		}

		@Override
		public Number getY(int series, int item) {
			return xyz.getPoint(item).getY();
		}

		@Override
		public int getSeriesCount() {
			return 1;
		}

		@Override
		public Comparable getSeriesKey(int arg0) {
			return zLabel;
		}
		
	}
	
	private class PaintScaleWrapper implements PaintScale {
		
		private CPT cpt;
		
		public PaintScaleWrapper(CPT cpt) {
			this.cpt = cpt;
		}

		@Override
		public double getLowerBound() {
			return cpt.getMinValue();
		}

		@Override
		public Paint getPaint(double value) {
			return cpt.getColor((float)value);
		}

		@Override
		public double getUpperBound() {
			return cpt.getMaxValue();
		}
		
	}

}
