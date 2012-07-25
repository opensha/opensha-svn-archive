package scratch.UCERF3.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dom4j.DocumentException;
import org.jfree.chart.plot.Plot;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.param.impl.IntegerParameter;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.collect.Lists;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM2pt1_Ruptures;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_FM3_Ruptures;
import scratch.UCERF3.utils.FindEquivUCERF2_Ruptures.FindEquivUCERF2_Ruptures;

public class RupRateConvergenceGUI extends JFrame implements ParameterChangeListener {
	
	private static final int DEFAULT_PLOT_WIDTH = 100;
	
	private static final String BROWSE_PARAM_NAME = "Browse";
	private FileParameter browseParam = new FileParameter(BROWSE_PARAM_NAME);
	
	private static final String UCERF2_PARAM_NAME = "Only UCERF2 Equiv Rups";
	private BooleanParameter ucerf2Param = new BooleanParameter(UCERF2_PARAM_NAME, false);
	
	private static final String SDOM_N_PARAM_NAME = "N (for SDOM)";
	private static final Integer SDOM_N_PARAM_MIN = 1;
	private static final Integer SDOM_N_PARAM_MAX = 10000;
	private IntegerParameter sdomNParam = new IntegerParameter(
			SDOM_N_PARAM_NAME, SDOM_N_PARAM_MIN, SDOM_N_PARAM_MAX);
	
	private static final String ZOOM_IN_PARAM_NAME = "Zoom In";
	private ButtonParameter zoomInParam = new ButtonParameter(ZOOM_IN_PARAM_NAME, "+");
	
	private static final String ZOOM_OUT_PARAM_NAME = "Zoom Out";
	private ButtonParameter zoomOutParam = new ButtonParameter(ZOOM_OUT_PARAM_NAME, "-");
	
	private static final String START_PARAM_NAME = "Start";
	private ButtonParameter startParam = new ButtonParameter(START_PARAM_NAME, "|<");
	
	private static final String PREV_PAGE_PARAM_NAME = "Prev Page";
	private ButtonParameter prevPageParam = new ButtonParameter(PREV_PAGE_PARAM_NAME, "<<<");
	
	private static final String PREV_HALF_PAGE_PARAM_NAME = "Prev 1/2 Page";
	private ButtonParameter prevHalfPageParam = new ButtonParameter(PREV_HALF_PAGE_PARAM_NAME, "<<");
	
	private static final String PREV_RUP_PARAM_NAME = "Prev Rup";
	private ButtonParameter prevRupParam = new ButtonParameter(PREV_RUP_PARAM_NAME, "<");
	
	private static final String END_PARAM_NAME = "End";
	private ButtonParameter endParam = new ButtonParameter(END_PARAM_NAME, ">|");
	
	private static final String NEXT_PAGE_PARAM_NAME = "Next Page";
	private ButtonParameter nextPageParam = new ButtonParameter(NEXT_PAGE_PARAM_NAME, ">>>");
	
	private static final String NEXT_HALF_PAGE_PARAM_NAME = "Next 1/2 Page";
	private ButtonParameter nextHalfPageParam = new ButtonParameter(NEXT_HALF_PAGE_PARAM_NAME, ">>");
	
	private static final String NEXT_RUP_PARAM_NAME = "Next Rup";
	private ButtonParameter nextRupParam = new ButtonParameter(NEXT_RUP_PARAM_NAME, ">");
	
	private HeadlessGraphPanel gp;
	private ArrayList<DiscretizedFunc> funcs;
	private ArrayList<PlotCurveCharacterstics> chars;
	private AverageFaultSystemSolution sol;
	private double[] stdDevs;
	private FindEquivUCERF2_Ruptures ucerf2Rups;
	private EvenlyDiscretizedFunc meanFunc;
	private EvenlyDiscretizedFunc minFunc;
	private EvenlyDiscretizedFunc maxFunc;
	private EvenlyDiscretizedFunc meanPlusStdDevFunc;
	private EvenlyDiscretizedFunc meanMinusStdDevFunc;
	private EvenlyDiscretizedFunc meanPlusStdDevOfMeanFunc;
	private EvenlyDiscretizedFunc meanMinusStdDevOfMeanFunc;
	
	private ParameterList controlParamList;
	
	public RupRateConvergenceGUI() {
//		super(new BorderLayout());
		
		ParameterList paramList = new ParameterList();
		paramList.addParameter(browseParam);
		paramList.addParameter(ucerf2Param);
		sdomNParam.setValue(SDOM_N_PARAM_MIN);
		paramList.addParameter(sdomNParam);
		
		controlParamList = new ParameterList();
		controlParamList.addParameter(zoomOutParam);
		controlParamList.addParameter(zoomInParam);
		controlParamList.addParameter(startParam);
		controlParamList.addParameter(prevPageParam);
		controlParamList.addParameter(prevHalfPageParam);
		controlParamList.addParameter(prevRupParam);
		controlParamList.addParameter(nextRupParam);
		controlParamList.addParameter(nextHalfPageParam);
		controlParamList.addParameter(nextPageParam);
		controlParamList.addParameter(endParam);
		
		for (Parameter<?> param : paramList)
			param.addParameterChangeListener(this);
		
		for (Parameter<?> param : controlParamList)
			param.addParameterChangeListener(this);
		
		GriddedParameterListEditor paramEdit = new GriddedParameterListEditor(paramList, 1, 0);
		GriddedParameterListEditor controlParamEdit = new GriddedParameterListEditor(controlParamList, 1, 0);
		
		enableButtons();
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		this.setContentPane(mainPanel);
		
		gp = new HeadlessGraphPanel();
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(paramEdit, BorderLayout.CENTER);
		topPanel.add(controlParamEdit, BorderLayout.EAST);
		
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(gp, BorderLayout.CENTER);
		
		setSize(1400, 800);
	}
	
	private void setSol(AverageFaultSystemSolution sol) {
		this.sol = sol;
		ucerf2Rups = null;
		if (sol == null) {
			meanFunc = null;
			minFunc = null;
			maxFunc = null;
			meanPlusStdDevFunc = null;
			meanMinusStdDevFunc = null;
			meanPlusStdDevOfMeanFunc = null;
			meanMinusStdDevOfMeanFunc = null;
			stdDevs = null;
		} else {
			int numRups = sol.getNumRuptures();
			stdDevs = new double[numRups];
			meanFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			meanFunc.setName("Mean Rate");
			minFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			minFunc.setName("Min Rate");
			maxFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			maxFunc.setName("Max Rate");
			meanPlusStdDevFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			meanPlusStdDevFunc.setName("Mean + Std Dev");
			meanMinusStdDevFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			meanMinusStdDevFunc.setName("Mean - Std Dev");
			meanPlusStdDevOfMeanFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			meanPlusStdDevOfMeanFunc.setName("Mean + Std Dev Of Mean");
			meanMinusStdDevOfMeanFunc = new EvenlyDiscretizedFunc(0, numRups, 1d);
			meanMinusStdDevOfMeanFunc.setName("Mean - Std Dev Of Mean");
			
			sdomNParam.setValue(sol.getNumSolutions());
			sdomNParam.getEditor().refreshParamEditor();
			
			for (int r=0; r<numRups; r++) {
				double mean = sol.getRateForRup(r);
				double stdDev = sol.getRateStdDev(r);
				stdDevs[r] = stdDev;
				double min = sol.getRateMin(r);
				double max = sol.getRateMax(r);
				meanFunc.set(r, mean);
				minFunc.set(r, min);
				maxFunc.set(r, max);
				meanPlusStdDevFunc.set(r, mean + stdDev);
				meanMinusStdDevFunc.set(r, mean - stdDev);
			}
			updateSDOM();
		}
		
		enableButtons();
	}
	
	private void updateSDOM() {
		if (sol == null)
			return;
		int n = sdomNParam.getValue();
		for (int i=0; i<sol.getNumRuptures(); i++) {
			double stdDev = stdDevs[i];
			double mean = meanFunc.getY(i);
			double sdom = stdDev / Math.sqrt(n);
			meanPlusStdDevOfMeanFunc.set(i, mean + sdom);
			meanMinusStdDevOfMeanFunc.set(i, mean - sdom);
		}
	}
	
	private void enableButtons() {
		boolean enable = sol != null && funcs != null;
		
		for (Parameter<?> param : controlParamList) {
			param.getEditor().setEnabled(enable);
			param.getEditor().refreshParamEditor();
		}
	}
	
	private void rebuildPlot() {
		funcs = Lists.newArrayList();
		chars = Lists.newArrayList();
		
		if (sol != null) {
			float normalWidth = 4f;
			float meanWidth = 10f;
			
			funcs.add(meanFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, meanWidth, Color.BLACK));
			funcs.add(minFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, normalWidth, Color.GREEN));
			funcs.add(maxFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, normalWidth, Color.GREEN));
			funcs.add(meanPlusStdDevFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, normalWidth, Color.RED));
			funcs.add(meanMinusStdDevFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, normalWidth, Color.RED));
			funcs.add(meanPlusStdDevOfMeanFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, normalWidth, Color.BLUE));
			funcs.add(meanMinusStdDevOfMeanFunc);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, normalWidth, Color.BLUE));
			
			if (sol != null && ucerf2Param.getValue()) {
				if (ucerf2Rups == null) {
					if (sol.getFaultModel() == FaultModels.FM2_1)
						ucerf2Rups = new FindEquivUCERF2_FM2pt1_Ruptures(
								sol, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
					else
						ucerf2Rups = new FindEquivUCERF2_FM3_Ruptures(
								sol, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, sol.getFaultModel());
					
					List<Integer> rups = Lists.newArrayList();
					for (int r=0; r<ucerf2Rups.getNumUCERF2_Ruptures(); r++) {
						int ind = ucerf2Rups.getEquivFaultSystemRupIndexForUCERF2_Rupture(r);
						if (ind >= 0)
							rups.add(ind);
					}
					
					ArrayList<DiscretizedFunc> newFuncs = Lists.newArrayList();
					for (DiscretizedFunc func : funcs) {
						EvenlyDiscretizedFunc newFunc = new EvenlyDiscretizedFunc(0, rups.size(), 1d);
						newFunc.setName(func.getName());
						
						for (int i=0; i<rups.size(); i++) {
							newFunc.set(i, func.getY(rups.get(i)));
						}
					}
					funcs = newFuncs;
				}
			}
			
			int minX = 0;
			int maxX = DEFAULT_PLOT_WIDTH-1;
			double[] yBounds = getYBounds(minX, maxX);
			gp.setUserBounds(minX, maxX, yBounds[0], yBounds[1]);
		}
		
		drawGraph();
		
		enableButtons();
	}
	
	private void drawGraph() {
		gp.drawGraphPanel("Rupture Index", "Rate", funcs, chars, true, "Rup Rate Convergence");
	}
	
	private double[] getYBounds(int minX, int maxX) {
		double minY = Double.POSITIVE_INFINITY;
		double maxY = 0;
		
		for (DiscretizedFunc func : funcs) {
			for (int x=minX; x<=maxX; x++) {
				double y = func.getY(x);
				if (y < minY)
					minY = y;
				if (y > maxY)
					maxY = y;
			}
		}
		
		double[] ret = {minY, maxY};
		return ret;
	}
	
	private void updatePlotRange(int min, int max) {
		double[] yBounds = getYBounds(min, max);
		double minY = yBounds[0]*0.9d;
		double maxY = yBounds[1]*1.1d;
		gp.setUserBounds(min, max, minY, maxY);
		gp.getXAxis().setRange(min, max);
		gp.getYAxis().setRange(minY, maxY);
//		drawGraph();
//		gp.validate();
//		gp.repaint();
	}
	
	private int[] getCurrentBounds() {
		int min;
		int max;
		try {
			min = (int)gp.getX_AxisRange().getLowerBound();
			max = (int)gp.getX_AxisRange().getUpperBound();
		} catch (Exception e) {
			min = 0;
			max = 0;
		}
		
		int[] ret = {min, max};
		return ret;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		
		int[] range = getCurrentBounds();
		int min = range[0];
		int max = range[1];
		int num = max - min;
		
		if (param == browseParam) {
			AverageFaultSystemSolution sol = null;
			try {
				sol = AverageFaultSystemSolution.fromZipFile(browseParam.getValue());
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error loading average solution:\n"+e.getMessage(),
						"Error Loading Average Solution", JOptionPane.ERROR_MESSAGE);
			}
			setSol(sol);
			rebuildPlot();
		} else if (param == ucerf2Param) {
			rebuildPlot();
		} else if (param == sdomNParam) {
			updateSDOM();
			rebuildPlot();
		} else {
			int totSize = funcs.get(0).getNum();
			
			// move/zoom buttons
			if (param == startParam) {
				min = 0;
				max = num;
			} else if (param == prevPageParam) {
				min -= num;
				max -= num;
			} else if (param == prevHalfPageParam) {
				min -= (num / 2);
				max -= (num / 2);
			} else if (param == prevRupParam) {
				min--;
				max--;
			} else if (param == endParam) {
				max = totSize-1;
				min = max - num;
			} else if (param == nextPageParam) {
				min += num;
				max += num;
			} else if (param == nextHalfPageParam) {
				min += (num / 2);
				max += (num / 2);
			} else if (param == nextRupParam) {
				min++;
				max++;
			} else if (param == zoomInParam) {
				max = (int)((double)max * 2d / 3d);
			} else if (param == zoomOutParam) {
				max = (int)((double)max * 1.5d);
			}
			
			if (min < 0) {
				int below = 0 - min;
				min += below;
				max += below;
			} else if (max >= totSize) {
				int above = max - totSize;
				max -= above;
				min -= above;
			}
			updatePlotRange(min, max);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RupRateConvergenceGUI gui = new RupRateConvergenceGUI();
		
		gui.setVisible(true);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
