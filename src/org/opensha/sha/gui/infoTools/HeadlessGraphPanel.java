package org.opensha.sha.gui.infoTools;

import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.XY_DataSet;

import com.google.common.collect.Lists;

public class HeadlessGraphPanel extends GraphPanel implements GraphPanelAPI, PlotControllerAPI {
	
	private double userMinX=Double.NaN, userMaxX=Double.NaN, userMinY=Double.NaN, userMaxY=Double.NaN;
	private int tickLabelFontSize=12, axisLabelFontSize=12, plotLabelFontSize=14;
	private boolean xLog=false, yLog=false;
	
	public HeadlessGraphPanel() {
		super(null);
		super.application = this;
	}
	
	public void drawGraphPanel(String xAxisName, String yAxisName,
			ArrayList funcList, ArrayList plotChars, boolean customAxis, String title) {
		checkUserBounds(funcList);
		setCurvePlottingCharacterstic(plotChars);
		drawGraphPanel(xAxisName, yAxisName, funcList, customAxis, title);
	}
	
	public void drawGraphPanel(String xAxisName, String yAxisName,
			ArrayList funcList, boolean customAxis, String title) {
		checkUserBounds(funcList);
		super.drawGraphPanel(xAxisName, yAxisName, funcList, xLog, yLog, customAxis,
				title, this);
		
		this.setVisible(true);
		
		this.togglePlot(null);
		
		this.validate();
		this.repaint();
	}
	
	private void checkUserBounds(ArrayList funcList) {
		if(Double.isNaN(userMinX) || Double.isNaN(userMaxX))
			this.setUserX_BoundsFromFuncList(funcList);
		if(Double.isNaN(userMinY) || Double.isNaN(userMaxY))
			this.setUserY_BoundsFromFuncList(funcList);
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
	
	/**
	 * This sets the bound from the min and max in the list of functions (no buffer added)
	 * @param funcList
	 */
	public void setUserBoundsFromFuncList(ArrayList funcList) {
		setUserX_BoundsFromFuncList(funcList);
		setUserY_BoundsFromFuncList(funcList);
	}
	
	/**
	 * This sets the bound from the min and max in the list of functions (no buffer added)
	 * @param funcList
	 */
	public void setUserX_BoundsFromFuncList(ArrayList funcList) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		for(Object func: funcList) {
			if(minX > ((XY_DataSet)func).getMinX())
				minX = ((XY_DataSet)func).getMinX();
			if(maxX < ((XY_DataSet)func).getMaxX())
				maxX = ((XY_DataSet)func).getMaxX();
		}
		setUserMinX(minX);
		setUserMaxX(maxX);
	}
	
	/**
	 * This sets the bound from the min and max in the list of functions (no buffer added)
	 * @param funcList
	 */
	public void setUserY_BoundsFromFuncList(ArrayList funcList) {
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for(Object func: funcList) {
			if(minY > ((XY_DataSet)func).getMinY())
				minY = ((XY_DataSet)func).getMinY();
			if(maxY < ((XY_DataSet)func).getMaxY())
				maxY = ((XY_DataSet)func).getMaxY();
		}
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
