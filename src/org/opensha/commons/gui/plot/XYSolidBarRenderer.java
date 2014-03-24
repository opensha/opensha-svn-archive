package org.opensha.commons.gui.plot;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * Custom item renderer for drawing thick bars where the thickness is in actual y axis units.
 * Useful for things like tornado diagrams.
 * 
 * Limitations: gaps will be drawn in multi segment lines.
 * @author kevin
 *
 */
public class XYSolidBarRenderer extends AbstractXYItemRenderer {

	private float thicknessY;

	public XYSolidBarRenderer(float thicknessY) {
		this.thicknessY = thicknessY;
	}

	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {
		int size = dataset.getItemCount(series);

		if (size <= 1)
			// nothing to draw
			return;

		for (int i=1; i<size; i++) {
			double origX0 = dataset.getXValue(series, i-1);
			double origY0 = dataset.getYValue(series, i-1);
			double origX1 = dataset.getXValue(series, i);
			double origY1 = dataset.getYValue(series, i);
			
			// thickness Y is hypotenuse
			double angle = Math.atan((origY1 - origY0)/(origX1 - origX0));
//			System.out.println("Angle: "+Math.toDegrees(angle)+" deg");
			double xAdd = thicknessY * Math.sin(angle);
			double yAdd = thicknessY * Math.cos(angle);
//			System.out.println("xAdd = "+xAdd);
//			System.out.println("yAdd = "+yAdd);
			
			double topLeftX = origX0 - xAdd;
			double topLeftY = origY0 + yAdd;
			
			double botLeftX = origX0 + xAdd;
			double botLeftY = origY0 - yAdd;
			
			double topRightX = origX1 - xAdd;
			double topRightY = origY1 + yAdd;
			
			double botRightX = origX1 + xAdd;
			double botRightY = origY1 - yAdd;
			
			PlotOrientation orientation = plot.getOrientation();
			RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
					plot.getDomainAxisLocation(), orientation);
			RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
					plot.getRangeAxisLocation(), orientation);
			Polygon p = new Polygon();
			
			int pX0 = (int)domainAxis.valueToJava2D(topLeftX, dataArea, domainEdge);
			int pY0 = (int)rangeAxis.valueToJava2D(topLeftY, dataArea, rangeEdge);
			int pX1 = (int)domainAxis.valueToJava2D(topRightX, dataArea, domainEdge);
			int pY1 = (int)rangeAxis.valueToJava2D(topRightY, dataArea, rangeEdge);
			int pX2 = (int)domainAxis.valueToJava2D(botRightX, dataArea, domainEdge);
			int pY2 = (int)rangeAxis.valueToJava2D(botRightY, dataArea, rangeEdge);
			int pX3 = (int)domainAxis.valueToJava2D(botLeftX, dataArea, domainEdge);
			int pY3 = (int)rangeAxis.valueToJava2D(botLeftY, dataArea, rangeEdge);
			if (orientation == PlotOrientation.HORIZONTAL) {
				p.addPoint(pY0, pX0);
				p.addPoint(pY1, pX1);
				p.addPoint(pY2, pX2);
				p.addPoint(pY3, pX3);
			} else {
				p.addPoint(pX0, pY0);
				p.addPoint(pX1, pY1);
				p.addPoint(pX2, pY2);
				p.addPoint(pX3, pY3);
			}
			
			Paint paint = getItemPaint(series, i);
			if (paint != null) {
//				System.out.println("Paint: "+paint);
				g2.setPaint(paint);
				g2.fillPolygon(p);
			}

			// add an entity for the item...
			if (info != null) {
				EntityCollection entities = info.getOwner().getEntityCollection();
				if (entities != null) {
					String tip = null;
					XYToolTipGenerator generator = getToolTipGenerator(series, 
							item);
					if (generator != null) {
						tip = generator.generateToolTip(dataset, series, item);
					}
					String url = null;
					if (getURLGenerator() != null) {
						url = getURLGenerator().generateURL(dataset, series, item);
					}
					XYItemEntity entity = new XYItemEntity(p, dataset, series, 
							item, tip, url);
					entities.add(entity);
				}
			}
		}
	}

}
