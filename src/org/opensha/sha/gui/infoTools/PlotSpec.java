package org.opensha.sha.gui.infoTools;

import java.util.ArrayList;

import org.opensha.commons.data.function.DiscretizedFunc;

/**
 * Useful if you need to describe a plot that will be used both in headless and interactive plotting.
 * 
 * @author kevin
 *
 */
public class PlotSpec {
	private ArrayList<? extends DiscretizedFunc> funcs;
	private ArrayList<PlotCurveCharacterstics> chars;
	private String title, xAxisLabel, yAxisLabel;
	
	public PlotSpec(ArrayList<? extends DiscretizedFunc> funcs,
			ArrayList<PlotCurveCharacterstics> chars, String title, String xAxisLabel, String yAxisLabel) {
		this.funcs = funcs;
		this.chars = chars;
		this.title = title;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
	}

	public ArrayList<? extends DiscretizedFunc> getFuncs() {
		return funcs;
	}

	public ArrayList<PlotCurveCharacterstics> getChars() {
		return chars;
	}

	public String getTitle() {
		return title;
	}

	public String getxAxisLabel() {
		return xAxisLabel;
	}

	public String getyAxisLabel() {
		return yAxisLabel;
	}
}
