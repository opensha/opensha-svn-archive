package scratch.UCERF3.analysis;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.math.stat.StatUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.impl.GriddedParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.PlotControllerAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;

public class InversionConvergenceGUI extends JFrame implements
ParameterChangeListener, GraphPanelAPI, PlotControllerAPI {
	
	private static final String BROWSE_PARAM_NAME = "CSV Dir/Zip File";
	private FileParameter browseParam;
	
	private static final String ENUM_PARAM_ANY_CHOICE = "(any)";
	private ParameterList enumParams;
	
	private static final String REFRESH_PARAM_NAME = "Plot";
	private static final String REFRESH_BUTTON_TEXT = "Reload Results";
	private ButtonParameter refreshButton;
	
	private static final String CARD_GRAPH = "Graph";
	private static final String CARD_BAR = "Bar";
	private static final String CARD_NONE = "None";
	
	private enum PlotType {
		ENERGY_VS_TIME("Energy vs Time", CARD_GRAPH),
		ENERGY_VS_ITERATIONS("Energy vs Iterations", CARD_GRAPH),
		FINAL_ENERGY_BREAKDOWN("Final Energy Breakdown", CARD_BAR),
		FINAL_NORMALIZED_PERTURBATION_BREAKDOWN("Final Norm. Perturb Breakdown", CARD_BAR),
		PERTURBATIONS_VS_ITERATIONS("Perturbations Vs Iterations", CARD_GRAPH),
		PERTURBATIONS_FRACTION("Perturbs/Iters Vs Time", CARD_GRAPH);
		private String name;
		private String card;
		private PlotType(String name, String card) {
			this.name = name;
			this.card = card;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private static final String PLOT_TYPE_PARAM_NAME = "Plot Type";
	private EnumParameter<PlotType> plotTypeParam;
	
	private GriddedParameterListEditor griddedEditor;
	
	private JPanel contentPane;
	
	private CardLayout cl;
	private JPanel chartPanel;
	
	private GraphPanel graphPanel;
	
	private JPanel barChartPanel;
	
	private Map<LogicTreeBranch, CSVFile<String>> resultFilesMap;
	private ArrayList<String> curNames;
	private ArrayList<LogicTreeBranch> curBranches;
	private ArrayList<ArbitrarilyDiscretizedFunc[]> curEnergyVsTimes;
	private ArrayList<ArbitrarilyDiscretizedFunc[]> curEnergyVsIters;
	private ArrayList<ArbitrarilyDiscretizedFunc> curPerturbsPerItersVsTimes;
	private ArrayList<ArbitrarilyDiscretizedFunc> curPerturbsVsIters;
	
	private static ArrayList<String> energyComponentNames =
		Lists.newArrayList("Total", "Equality", "Entropy", "Inequality");
	
	public InversionConvergenceGUI() {
		ParameterList params = new ParameterList();
		
		browseParam = new FileParameter(BROWSE_PARAM_NAME);
		browseParam.addParameterChangeListener(this);
		params.addParameter(browseParam);
		
		enumParams = new ParameterList();
		enumParams.addParameter(buildEnumParam(FaultModels.class, FaultModels.FM3_1));
		enumParams.addParameter(buildEnumParam(DeformationModels.class, null));
		enumParams.addParameter(buildEnumParam(MagAreaRelationships.class, MagAreaRelationships.AVE_UCERF2));
		enumParams.addParameter(buildEnumParam(SlipAlongRuptureModels.class, SlipAlongRuptureModels.TAPERED));
		enumParams.addParameter(buildEnumParam(AveSlipForRupModels.class, AveSlipForRupModels.AVE_UCERF2));
		enumParams.addParameter(buildEnumParam(InversionModels.class, InversionModels.CHAR));
		params.addParameterList(enumParams);
		
		refreshButton = new ButtonParameter(REFRESH_PARAM_NAME, REFRESH_BUTTON_TEXT);
		refreshButton.addParameterChangeListener(this);
		params.addParameter(refreshButton);
		
		plotTypeParam = new EnumParameter<InversionConvergenceGUI.PlotType>(
				PLOT_TYPE_PARAM_NAME, EnumSet.allOf(PlotType.class), PlotType.ENERGY_VS_ITERATIONS, null);
		plotTypeParam.addParameterChangeListener(this);
		params.addParameter(plotTypeParam);
		
		contentPane = new JPanel(new BorderLayout());
		griddedEditor = new GriddedParameterListEditor(params, 1, 0);
		contentPane.add(griddedEditor, BorderLayout.NORTH);
		
		cl = new CardLayout();
		chartPanel = new JPanel(cl);
		JPanel nonePanel = new JPanel();
		nonePanel.add(new JLabel("No results found/loaded"));
		chartPanel.add(nonePanel, CARD_NONE);
		graphPanel = new GraphPanel(this);
		chartPanel.add(graphPanel, CARD_GRAPH);
		barChartPanel = new JPanel(new BorderLayout());
		chartPanel.add(barChartPanel, CARD_BAR);
		contentPane.add(chartPanel, BorderLayout.CENTER);
		
		this.setContentPane(contentPane);
		this.setSize(1400, 1000);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private <E extends Enum<E>> EnumParameter<?> buildEnumParam(Class<E> e, E defaultValue) {
		String name = ClassUtils.getClassNameWithoutPackage(e);
		EnumParameter<E> param = new EnumParameter<E>(name, EnumSet.allOf(e),
				defaultValue, ENUM_PARAM_ANY_CHOICE);
		param.addParameterChangeListener(this);
		return param;
	}
	
	private static HashMap<LogicTreeBranch, CSVFile<String>> loadZipFile(File zipFile, LogicTreeBranch branch)
	throws IOException {
		HashMap<LogicTreeBranch, CSVFile<String>> map = Maps.newHashMap();
		ZipFile zip = new ZipFile(zipFile);
		
		Enumeration<? extends ZipEntry> e = zip.entries();
		
		while (e.hasMoreElements()) {
			ZipEntry entry = e.nextElement();
			String name = entry.getName();
			if (!name.endsWith(".csv"))
				continue;
			LogicTreeBranch candidate = LogicTreeBranch.parseFileName(name);
			if (!branch.matchesNonNulls(candidate)) {
				continue;
			}
			System.out.println("Loading: "+name);
//			System.out.println("Branch: "+candidate);
			map.put(candidate, CSVFile.readStream(zip.getInputStream(entry), true));
		}
		
		return map;
	}
	
	private static HashMap<LogicTreeBranch, CSVFile<String>> loadDir(File dir, LogicTreeBranch branch)
	throws IOException {
		HashMap<LogicTreeBranch, CSVFile<String>> map = Maps.newHashMap();
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				continue;
			String name = file.getName();
			if (!name.endsWith(".csv"))
				continue;
			LogicTreeBranch candidate = LogicTreeBranch.parseFileName(name);
			if (!branch.matchesNonNulls(candidate)) {
				continue;
			}
			map.put(candidate, CSVFile.readFile(file, true));
		}
		
		return map;
	}
	
	private void loadResultFiles(LogicTreeBranch branch) throws IOException {
		File file = browseParam.getValue();
		
		if (file == null) {
			resultFilesMap = null;
		} else if (file.getName().toLowerCase().endsWith(".zip")) {
			resultFilesMap = loadZipFile(file, branch);
		} else {
			resultFilesMap = loadDir(file, branch);
		}
	}
	
	private LogicTreeBranch getCurrentBranch() {
		FaultModels fm = enumParams.getParameter(FaultModels.class,
				ClassUtils.getClassNameWithoutPackage(FaultModels.class)).getValue();
		DeformationModels dm = enumParams.getParameter(DeformationModels.class,
				ClassUtils.getClassNameWithoutPackage(DeformationModels.class)).getValue();
		MagAreaRelationships ma = enumParams.getParameter(MagAreaRelationships.class,
				ClassUtils.getClassNameWithoutPackage(MagAreaRelationships.class)).getValue();
		AveSlipForRupModels as = enumParams.getParameter(AveSlipForRupModels.class,
				ClassUtils.getClassNameWithoutPackage(AveSlipForRupModels.class)).getValue();
		SlipAlongRuptureModels sal = enumParams.getParameter(SlipAlongRuptureModels.class,
				ClassUtils.getClassNameWithoutPackage(SlipAlongRuptureModels.class)).getValue();
		InversionModels im = enumParams.getParameter(InversionModels.class,
				ClassUtils.getClassNameWithoutPackage(InversionModels.class)).getValue();
		return new LogicTreeBranch(fm, dm, ma, as, sal, im);
	}
	
	private void buildFunctions(LogicTreeBranch branch) {
		curNames = new ArrayList<String>();
		curBranches = new ArrayList<LogicTreeBranch>();
		curEnergyVsTimes = new ArrayList<ArbitrarilyDiscretizedFunc[]>();
		curEnergyVsIters = new ArrayList<ArbitrarilyDiscretizedFunc[]>();
		curPerturbsPerItersVsTimes = new ArrayList<ArbitrarilyDiscretizedFunc>();
		curPerturbsVsIters = new ArrayList<ArbitrarilyDiscretizedFunc>();
		
		if (resultFilesMap == null)
			return;
		
		for (LogicTreeBranch candidate : resultFilesMap.keySet()) {
			ArrayList<String> diffNames = new ArrayList<String>();
			if (branch.getFaultModel() == null)
				diffNames.add("FM: "+candidate.getFaultModel().getShortName());
			if (branch.getDefModel() == null)
				diffNames.add("DM: "+candidate.getDefModel().getShortName());
			if (branch.getMagArea() == null)
				diffNames.add("MA: "+candidate.getMagArea().getShortName());
			if (branch.getAveSlip() == null)
				diffNames.add("Dr: "+candidate.getAveSlip().getShortName());
			if (branch.getSlipAlong() == null)
				diffNames.add("Dsr: "+candidate.getSlipAlong().getShortName());
			if (branch.getInvModel() == null)
				diffNames.add("Inv: "+candidate.getInvModel().getShortName());
			String name = Joiner.on(", ").join(diffNames);
			CSVFile<String> csv = resultFilesMap.get(candidate);
			ArbitrarilyDiscretizedFunc[] energyVsTime = new ArbitrarilyDiscretizedFunc[4];
			ArbitrarilyDiscretizedFunc[] energyVsIter = new ArbitrarilyDiscretizedFunc[4];
			for (int i=0; i<energyVsIter.length; i++) {
				energyVsTime[i] = new ArbitrarilyDiscretizedFunc();
				energyVsTime[i].setName(name);
				energyVsTime[i].setInfo(energyComponentNames.get(i)+" energy");
				energyVsIter[i] = new ArbitrarilyDiscretizedFunc();
				energyVsIter[i].setName(name);
				energyVsIter[i].setInfo(energyComponentNames.get(i)+" energy");
			}
			ArbitrarilyDiscretizedFunc perturbsPerItersVsTimes = new ArbitrarilyDiscretizedFunc();
			perturbsPerItersVsTimes.setName(name);
			ArbitrarilyDiscretizedFunc perturbsVsIters = new ArbitrarilyDiscretizedFunc();
			perturbsVsIters.setName(name);
			for (int row=1; row<csv.getNumRows(); row++) {
				double iter = Double.parseDouble(csv.get(row, 0));
				double mins = Double.parseDouble(csv.get(row, 1)) / 1000d / 60d;
				double totEnergy =  Double.parseDouble(csv.get(row, 2));
				double eqEnergy =  Double.parseDouble(csv.get(row, 3));
				double entropyEnergy =  Double.parseDouble(csv.get(row, 4));
				double ineqEnergy =  Double.parseDouble(csv.get(row, 5));
				double perturbs =  Double.parseDouble(csv.get(row, 6));
				double[] energies = { totEnergy, eqEnergy, entropyEnergy, ineqEnergy };
				for (int i=0; i<energies.length; i++) {
					energyVsTime[i].set(mins, energies[i]);
					energyVsIter[i].set(iter, energies[i]);
				}
				perturbsPerItersVsTimes.set(mins, perturbs/iter);
				perturbsVsIters.set(iter, perturbs);
			}
			// now find insertion point
			int ind = -1;
			for (int i=curNames.size()-1; i>=0; i--) {
				int cmp = name.compareTo(curNames.get(i));
				if (cmp <= 0)
					ind = i;
			}
			if (ind < 0)
				ind = curNames.size();
			curNames.add(ind, name);
			curBranches.add(ind, candidate);
			curEnergyVsTimes.add(ind, energyVsTime);
			curEnergyVsIters.add(ind, energyVsIter);
			curPerturbsPerItersVsTimes.add(ind, perturbsPerItersVsTimes);
			curPerturbsVsIters.add(ind, perturbsVsIters);
		}
		System.out.println("Loaded: "+Joiner.on(", ").join(curNames));
	}
	
	private void updatePlot() {
		if (curBranches.isEmpty()) {
			cl.show(chartPanel, CARD_NONE);
			return;
		}
		PlotType plot = plotTypeParam.getValue();
		if (plot.card == CARD_GRAPH) {
			ArrayList<ArbitrarilyDiscretizedFunc> funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
			String xAxisName;
			String yAxisName;
			String title;
			ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
			switch (plot) {
			case ENERGY_VS_ITERATIONS:
				xAxisName = "Iterations";
				yAxisName = "Energy";
				title = "Energy vs Iterations";
				if (curEnergyVsIters.size() == 1) {
					// single run case, show all component of energy
					for (ArbitrarilyDiscretizedFunc f : curEnergyVsIters.get(0))
						funcs.add(f);
					title += " (Single Run Components!)";
					chars = ThreadedSimulatedAnnealing.getEnergyBreakdownChars();
				} else {
					for (ArbitrarilyDiscretizedFunc[] f : curEnergyVsIters)
						funcs.add(f[0]);
				}
				break;
			case ENERGY_VS_TIME:
				xAxisName = "Time (minutes)";
				yAxisName = "Energy";
				title = "Energy vs Time";
				if (curEnergyVsTimes.size() == 1) {
					// single run case, show all component of energy
					for (ArbitrarilyDiscretizedFunc f : curEnergyVsTimes.get(0))
						funcs.add(f);
					title += " (Single Run Components!)";
					chars = ThreadedSimulatedAnnealing.getEnergyBreakdownChars();
				} else {
					for (ArbitrarilyDiscretizedFunc[] f : curEnergyVsTimes)
						funcs.add(f[0]);
				}
				break;
			case PERTURBATIONS_VS_ITERATIONS:
				xAxisName = "Iterations";
				yAxisName = "Perturbations";
				title = "Perturbations Vs Iterations";
				funcs.addAll(curPerturbsVsIters);
				break;
			case PERTURBATIONS_FRACTION:
				xAxisName = "Time (minutes)";
				yAxisName = "Perturbations/Iterations";
				title = "Perturbations/Iterations Vs Time";
				funcs.addAll(curPerturbsPerItersVsTimes);
				break;

			default:
				throw new RuntimeException("shouldn't get here...");
			}
			
			graphPanel.setCurvePlottingCharacterstic(chars);
			graphPanel.drawGraphPanel(xAxisName, yAxisName, funcs, false, false, false, title, this);
			graphPanel.togglePlot(null);
			graphPanel.validate();
			graphPanel.repaint();
		} else {
			// bar graph
			boolean perturb = plot == PlotType.FINAL_NORMALIZED_PERTURBATION_BREAKDOWN;
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			ArrayList<String> series;
			String rangeLabel;
			if (perturb) {
				series = Lists.newArrayList("% Perturbs Kept");
				rangeLabel = "Perturbations Kept (%)";
			} else {
				series = energyComponentNames;
				rangeLabel = "Energy";
			}
			
			for (int i=0; i<curBranches.size(); i++) {
				ArbitrarilyDiscretizedFunc[] energies = curEnergyVsIters.get(i);
				ArbitrarilyDiscretizedFunc perturbs = curPerturbsPerItersVsTimes.get(i);
				String category = curNames.get(i);
				
				int lastInd = perturbs.getNum()-1;
				if (perturb) {
					double norm = perturbs.get(lastInd).getY() * 100d;
					dataset.addValue(norm, series.get(0), category);
				} else {
					dataset.addValue(energies[0].getY(lastInd), series.get(0), category);
					dataset.addValue(energies[1].getY(lastInd), series.get(1), category);
					dataset.addValue(energies[2].getY(lastInd), series.get(2), category);
					dataset.addValue(energies[3].getY(lastInd), series.get(3), category);
				}
			}
			JFreeChart chart = ChartFactory.createBarChart(plot.toString(), "Branch", rangeLabel,
					dataset, PlotOrientation.VERTICAL, true, true, false);
			
			// set the background color for the chart...
	        chart.setBackgroundPaint(Color.white);

	        // get a reference to the plot for further customisation...
	        final CategoryPlot plt = chart.getCategoryPlot();
	        plt.setBackgroundPaint(Color.lightGray);
	        plt.setDomainGridlinePaint(Color.white);
	        plt.setRangeGridlinePaint(Color.white);

	        // set the range axis to display integers only...
	        if (!perturb) {
	        	final NumberAxis rangeAxis = (NumberAxis) plt.getRangeAxis();
		        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        }

	        // disable bar outlines...
	        final BarRenderer renderer = (BarRenderer) plt.getRenderer();
	        renderer.setDrawBarOutline(false);
			
			final CategoryAxis domainAxis = plt.getDomainAxis();
	        domainAxis.setCategoryLabelPositions(
	            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
	        );
	        
	        ChartPanel cp = new ChartPanel(chart);
	        barChartPanel.removeAll();
	        barChartPanel.add(cp, BorderLayout.CENTER);
	        barChartPanel.validate();
	        barChartPanel.repaint();
		}
		cl.show(chartPanel, plot.card);
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		Parameter<?> param = event.getParameter();
		if (param == plotTypeParam) {
			updatePlot();
		} else if (param == refreshButton) {
			try {
				LogicTreeBranch branch = getCurrentBranch();
				loadResultFiles(branch);
				buildFunctions(branch);
				updatePlot();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InversionConvergenceGUI ic = new InversionConvergenceGUI();
		File file = new File("D:\\Documents\\temp\\csvs.zip");
		if (file.exists())
			ic.browseParam.setValue(file);
	}

	@Override
	public double getUserMinX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUserMaxX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUserMinY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUserMaxY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAxisLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	@Override
	public int getTickLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	@Override
	public void setXLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setYLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPlotLabelFontSize() {
		// TODO Auto-generated method stub
		return 16;
	}

}

