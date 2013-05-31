package org.opensha.commons.gui.plot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.DiscretizedFunc;

import com.google.common.collect.Lists;

/**
 * Useful if you need to describe a plot that will be used both in headless and interactive plotting.
 * 
 * @author kevin
 *
 */
public class PlotSpec implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<? extends PlotElement> elems;
	private List<PlotCurveCharacterstics> chars;
	private String title, xAxisLabel, yAxisLabel;
	
	public PlotSpec(List<? extends PlotElement> elems,
			List<PlotCurveCharacterstics> chars, String title, String xAxisLabel, String yAxisLabel) {
		this.elems = elems;
		this.chars = chars;
		this.title = title;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
	}

	public List<? extends PlotElement> getPlotElems() {
		return elems;
	}
	
	/**
	 * 
	 * @return list of all plot elements that are also Discretized Functions
	 */
	public List<DiscretizedFunc> getPlotFunctionsOnly() {
		List<DiscretizedFunc> funcs = Lists.newArrayList();
		for (PlotElement e : elems)
			if (e instanceof DiscretizedFunc)
				funcs.add((DiscretizedFunc)e);
		return funcs;
	}

	public void setPlotElems(List<? extends PlotElement> elems) {
		this.elems = elems;
	}

	public List<PlotCurveCharacterstics> getChars() {
		return chars;
	}
	
	public void setChars(List<PlotCurveCharacterstics> chars) {
		this.chars = chars;
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
}
