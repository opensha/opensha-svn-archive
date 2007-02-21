package scratchJavaDevelopers.martinez.applications;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.*;

import org.jfree.data.Range;
import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ERF_API;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.infoTools.ButtonControlPanel;
import org.opensha.sha.gui.infoTools.ButtonControlPanelAPI;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;

import scratchJavaDevelopers.martinez.LossCurveCalculator;
import scratchJavaDevelopers.martinez.VulnerabilityModels.VulnerabilityModel;
import scratchJavaDevelopers.martinez.beans.GuiBeanAPI;
import scratchJavaDevelopers.martinez.beans.VulnerabilityBean;

@SuppressWarnings("serial")
public class LossCurveApplication extends JFrame
		implements GraphPanelAPI, ButtonControlPanelAPI {
	/* Used for main content of application */
	protected JSplitPane mainSplitPane = null;
	protected JTabbedPane mainLeftContent = null;
	protected JPanel mainRightContent = null;
	
	/* Beans that provide input parameters */
	protected VulnerabilityBean vulnBean = null;
	protected Site_GuiBean siteBean = null;
	
	/* Output components */
	private GraphPanel graphOut = null;
	private JPanel textOut = null;
	
	/* Other compenents */
	private JButton btnCalc = null;
	private JButton btnClear = null;
	private ButtonControlPanel bcPanel =  null;
	private ArrayList<ArbitrarilyDiscretizedFunc> lossCurves = null;
	private boolean xLog = false;
	private boolean yLog = false;
	private String plotLabel = "Loss Curve";
	private String xAxisLabel = "Intensity Measure Level";
	private String yAxisLabel = "Percent Loss";
	private double maxXValue = 10.0;
	private double minXValue = 0.0;
	private double maxYValue = 10E+10;
	private double minYValue = 0.0;
	private Range xAxisRange = new Range(minXValue, maxXValue);
	private Range yAxisRange = new Range(minYValue, maxYValue);
	private boolean customAxis = false;
	
	/* Static Parameters used for Calculation */
	private static ERF_API forecast = null;
	private static AttenuationRelationshipAPI imr = null;

	
	/**
	 * Instantiates a new </code>LossCurveApplication</code> object and
	 * shows it to the user.  This is the entry point for the
	 * application.
	 */
	public static void main(String[] args) {
		LossCurveApplication app = new LossCurveApplication();
		app.prepare();
		app.setVisible(true);
	}

	public LossCurveApplication() {
		// Create the calculation utilities
		forecast = new Frankel02_AdjustableEqkRupForecast();
		imr = new USGS_Combined_2004_AttenRel(new ParameterChangeWarningListener() {
			public void parameterChangeWarning(ParameterChangeWarningEvent event) {
				System.err.println("A warining occurred while changing the value of " + event.getWarningParameter() +
						" to " + event.getNewValue() + "!");
			}
		});
		
		// Dummy parameters for easy display only
		ArrayList<String> forecasts = new ArrayList<String>();
		forecasts.add(forecast.getName());
		ArrayList<String> imrs = new ArrayList<String>();
		imrs.add(imr.getName());
		
		// Create the left content information
		mainLeftContent = generateLeftContentPane();
		
		// Create the right content information
		mainRightContent = new JPanel(new GridBagLayout());
		vulnBean = new VulnerabilityBean();
		siteBean = new Site_GuiBean();
		btnCalc = new JButton("Calculate");
		btnCalc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				btnCalc_actionPerformed(event);
			}
		});
		btnClear = new JButton("Clear Output");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				btnClear_actionPerformed(event);
			}
		});
		bcPanel = new ButtonControlPanel(this);
		
		mainRightContent.add((Component) vulnBean.getVisualization(GuiBeanAPI.APPLICATION), new GridBagConstraints(
				0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 2, 2));
		mainRightContent.add(siteBean, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2)); 
		
		try {
			mainRightContent.add(new ConstrainedStringParameterEditor(new StringParameter("Forecast Model", forecasts)), new GridBagConstraints(
					0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2));
			mainRightContent.add(new ConstrainedStringParameterEditor(new StringParameter("Intensity Measure Relationship", imrs)), new GridBagConstraints(
					0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mainRightContent.add(btnCalc, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		mainRightContent.add(btnClear, new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		
		mainRightContent.setPreferredSize(new Dimension(300, 500));
		mainRightContent.setSize(mainRightContent.getPreferredSize());
		
		// Put it all together
		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mainLeftContent, mainRightContent);
		mainSplitPane.setDividerLocation(0.50);
		add(mainSplitPane);
		
	}
	
	protected void btnCalc_actionPerformed(ActionEvent event) {
		LossCurveCalculator lCalc = new LossCurveCalculator();
		ArbitrarilyDiscretizedFunc hazFunc = getHazardCurve();
		ArbitrarilyDiscretizedFunc lossFunc = lCalc.getLossCurve(hazFunc, vulnBean.getCurrentModel());
		lossFunc.setInfo(getParameterInfoString());
		
		// Do this to swap the order, so curves stay the same color...
		lossCurves.add(0, lossFunc);
	}
	
	protected void btnClear_actionPerformed(ActionEvent event) {
		lossCurves.clear();
	}
	
	private void prepare() {
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    setLocation(
	    		(dim.width - getSize().width) / 2, 
	    		(dim.height - getSize().height) / 2
	    	);
	    setTitle("Risk Curve Calculator");
	}
	
	private JTabbedPane generateLeftContentPane() {
		JTabbedPane newLeftContent = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		graphOut = new GraphPanel(this);
		
		textOut = new JPanel(new GridBagLayout());
		newLeftContent.addTab("Graphical Output", null, graphOut, "View Graph");
		newLeftContent.addTab("Raw Data Output", null, textOut, "View Data");
		newLeftContent.setPreferredSize(new Dimension(500, 500));
		newLeftContent.setSize(newLeftContent.getPreferredSize());
		return newLeftContent;
	}
	
	private ArbitrarilyDiscretizedFunc getHazardCurve() {
		ArbitrarilyDiscretizedFunc hazFunc = new ArbitrarilyDiscretizedFunc();
		try {
			HazardCurveCalculator hCalc = new HazardCurveCalculator();
			
			VulnerabilityModel curVulnModel = vulnBean.getCurrentModel();
			ArrayList<Double> imls = curVulnModel.getIMLVals();
			Site site = siteBean.getSite();
			
			// We are currently only doing SA, so use log
			for(int i = 0; i < imls.size(); ++i)
				hazFunc.set(Math.log(imls.get(i)), 0.0);
			hazFunc = (ArbitrarilyDiscretizedFunc) hCalc.getHazardCurve(hazFunc, site, imr, (EqkRupForecastAPI) forecast);
			ArbitrarilyDiscretizedFunc tmpFunc = (ArbitrarilyDiscretizedFunc) hazFunc.deepClone();
			hazFunc.clear();
			for(int i = 0; i < imls.size(); ++i)
				hazFunc.set(imls.get(i), tmpFunc.getY(i));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return hazFunc;
	}
	
	private String getParameterInfoString() {
		return "";
	}

	private void drawGraph() {
		graphOut.drawGraphPanel(xAxisLabel, yAxisLabel, lossCurves, xLog, yLog, customAxis, plotLabel, bcPanel);
	}
	
	/* Functions to implement the GraphPanelAPI */
	public double getMaxX() {return maxXValue;}
	public double getMaxY() {return maxYValue;}
	public double getMinX() {return minXValue;}
	public double getMinY() {return minYValue;}

	/* Functions to implement the ButtonControlPanelAPI */
	public String getPlotLabel() {return plotLabel;}
	public ArrayList getPlottingFeatures() {
		return graphOut.getCurvePlottingCharacterstic();
	}
	
	public String getXAxisLabel() {return xAxisLabel;}
	public String getYAxisLabel() {return yAxisLabel;}
	public Range getX_AxisRange() {return xAxisRange;}
	public Range getY_AxisRange() {return yAxisRange;}

	public void plotGraphUsingPlotPreferences() {drawGraph();}
	public void setPlotLabel(String plotTitle) {this.plotLabel = plotTitle;}
	
	public void setXAxisLabel(String xAxisLabel) {this.xAxisLabel = xAxisLabel;}
	public void setYAxisLabel(String yAxisLabel) {this.yAxisLabel = yAxisLabel;}
	public void setX_Log(boolean xLog) {this.xLog = xLog;}
	public void setY_Log(boolean yLog) {this.yLog = yLog;}

	public void setAutoRange() {this.customAxis = false; drawGraph();}
	public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
		this.minXValue = xMin; this.maxXValue = xMax;
		this.minYValue = yMin; this.maxYValue = yMax;
		this.customAxis = true; drawGraph();
	}

	public void togglePlot() {
		// TODO Auto-generated method stub
		
	}
}
