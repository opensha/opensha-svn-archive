package org.opensha.commons.gui.plot.jfreechart.xyzPlot;

import java.util.List;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.ui.RectangleEdge;
import org.opensha.commons.data.xyz.XYZ_DataSet;
import org.opensha.commons.util.cpt.CPT;

public class XYZPlotSpec {
	
	private XYZ_DataSet xyzData;
	private CPT cpt;
	private String title, xAxisLabel, yAxisLabel, zAxisLabel;
	private Double thickness = null;
	private List<? extends XYAnnotation> annotations;
	private RectangleEdge legendPosition = RectangleEdge.TOP;
	
	public XYZPlotSpec(XYZ_DataSet xyzData, CPT cpt, String title,
			String xAxisLabel, String yAxisLabel, String zAxisLabel) {
		super();
		this.xyzData = xyzData;
		this.cpt = cpt;
		this.title = title;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		this.zAxisLabel = zAxisLabel;
	}

	public XYZ_DataSet getXYZ_Data() {
		return xyzData;
	}

	public void setXYZ_Data(XYZ_DataSet xyzData) {
		this.xyzData = xyzData;
	}

	public CPT getCPT() {
		return cpt;
	}

	public void setCPT(CPT cpt) {
		this.cpt = cpt;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getXAxisLabel() {
		return xAxisLabel;
	}

	public void setXAxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
	}

	public String getYAxisLabel() {
		return yAxisLabel;
	}

	public void setYAxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
	}

	public String getZAxisLabel() {
		return zAxisLabel;
	}

	public void setZAxisLabel(String zAxisLabel) {
		this.zAxisLabel = zAxisLabel;
	}

	public Double getThickness() {
		return thickness;
	}

	public void setThickness(Double thickness) {
		this.thickness = thickness;
	}
	
	/**
	 * Set the list of plot annotations (or null for no annotations). Note that any line annotations
	 * will use default rendering (black 1pt line).
	 * 
	 * @param annotations
	 */
	public void setPlotAnnotations(List<? extends XYAnnotation> annotations) {
		this.annotations = annotations;
	}
	
	public List<? extends XYAnnotation> getPlotAnnotations() {
		return annotations;
	}

	public RectangleEdge getLegendPosition() {
		return legendPosition;
	}

	public void setLegendPosition(RectangleEdge legendPosition) {
		this.legendPosition = legendPosition;
	}

}
