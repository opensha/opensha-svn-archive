package org.opensha.sha.gui.infoTools;

import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.XY_DataSet;

import com.google.common.collect.Lists;

public class HeadlessGraphPanel extends GraphPanel implements GraphPanelAPI, PlotControllerAPI {
	
	private double userMinX, userMaxX, userMinY, userMaxY;
	private int tickLabelFontSize=12, axisLabelFontSize=12, plotLabelFontSize=14;
	private boolean xLog=false, yLog=false;
	
	public HeadlessGraphPanel() {
		super(null);
		super.application = this;
	}
	
	public void drawGraphPanel(String xAxisName, String yAxisName,
			ArrayList funcList, ArrayList plotChars, boolean customAxis, String title) {
		setCurvePlottingCharacterstic(plotChars);
		drawGraphPanel(xAxisName, yAxisName, funcList, customAxis, title);
	}
	
	public void drawGraphPanel(String xAxisName, String yAxisName,
			ArrayList funcList, boolean customAxis, String title) {
		// TODO Auto-generated method stub
		super.drawGraphPanel(xAxisName, yAxisName, funcList, xLog, yLog, customAxis,
				title, this);
		
		this.setVisible(true);
		
		this.togglePlot(null);
		
		this.validate();
		this.repaint();
	}

	@Override
	public double getUserMinX() {
		return userMinX;
	}

	@Override
	public double getUserMaxX() {
		return userMaxX;
	}

	@Override
	public double getUserMinY() {
		return userMinY;
	}

	@Override
	public double getUserMaxY() {
		return userMaxY;
	}

	public void setUserMinX(double userMinX) {
		this.userMinX = userMinX;
	}

	public void setUserMaxX(double userMaxX) {
		this.userMaxX = userMaxX;
	}

	public void setUserMinY(double userMinY) {
		this.userMinY = userMinY;
	}

	public void setUserMaxY(double userMaxY) {
		this.userMaxY = userMaxY;
	}
	
	public void setUserBounds(double minX, double maxX, double minY, double maxY) {
		setUserMinX(minX);
		setUserMaxX(maxX);
		setUserMinY(minY);
		setUserMaxY(maxY);
	}

	public int getTickLabelFontSize() {
		return tickLabelFontSize;
	}

	public void setTickLabelFontSize(int tickLabelFontSize) {
		this.tickLabelFontSize = tickLabelFontSize;
	}

	public int getAxisLabelFontSize() {
		return axisLabelFontSize;
	}

	public void setAxisLabelFontSize(int axisLabelFontSize) {
		this.axisLabelFontSize = axisLabelFontSize;
	}

	public int getPlotLabelFontSize() {
		return plotLabelFontSize;
	}

	public void setPlotLabelFontSize(int plotLabelFontSize) {
		this.plotLabelFontSize = plotLabelFontSize;
	}

	public boolean getXLog() {
		return xLog;
	}

	public void setXLog(boolean xLog) {
		this.xLog = xLog;
	}

	public boolean getYLog() {
		return yLog;
	}

	public void setYLog(boolean yLog) {
		this.yLog = yLog;
	}
}
