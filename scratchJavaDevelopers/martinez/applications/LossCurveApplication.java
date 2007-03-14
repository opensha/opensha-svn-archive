package scratchJavaDevelopers.martinez.applications;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.*;

import org.opensha.data.Site;
import org.opensha.data.TimeSpan;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
//import org.opensha.param.StringParameter;
//import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ERF_API;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.util.ImageUtils;

import scratchJavaDevelopers.martinez.LossCurveCalculator;
import scratchJavaDevelopers.martinez.VulnerabilityModels.VulnerabilityModel;
import scratchJavaDevelopers.martinez.beans.GraphPane;
import scratchJavaDevelopers.martinez.beans.GuiBeanAPI;
//import scratchJavaDevelopers.martinez.beans.ProgressBar;
import scratchJavaDevelopers.martinez.beans.VulnerabilityBean;

@SuppressWarnings("serial")
public class LossCurveApplication extends JFrame {
	/* Used for main content of application */
	protected JSplitPane appSplitPane = null;
	protected JSplitPane mainSplitPane = null;
	protected JPanel mainLeftContent = null;
	protected JPanel mainRightContent = null;
	protected JPanel mainBottomContent = null;
	
	/* Beans that provide input parameters */
	protected VulnerabilityBean vulnBean = null;
	protected Site_GuiBean siteBean = null;
	protected TimeSpanGuiBean timeBean = null;
	
	/* Other compenents */
	private JButton btnCalc = null;
	private JButton btnClear = null;
	private ArrayList<ArbitrarilyDiscretizedFunc> lossCurves = new ArrayList<ArbitrarilyDiscretizedFunc>();
	private static JFrame splashScreen = null;
	private static JPanel creditPanel = null;
	
	/* Static Parameters used for Calculation */
	private static ERF_API forecast = null;
	private static AttenuationRelationshipAPI imr = null;

	
	/**
	 * Instantiates a new </code>LossCurveApplication</code> object and
	 * shows it to the user.  This is the entry point for the
	 * application.
	 */
	public static void main(String[] args) {
		splashScreen = createSplashScreen();
		splashScreen.setVisible(true);
		LossCurveApplication app = new LossCurveApplication();
		app.prepare();
		splashScreen.setVisible(false);
		splashScreen.dispose();
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
		imr.setIntensityMeasure(AttenuationRelationship.SA_NAME);
		imr.setParamDefaults();
		
		// Dummy parameters for easy display only
		ArrayList<String> forecasts = new ArrayList<String>();
		forecasts.add(forecast.getName());
		ArrayList<String> imrs = new ArrayList<String>();
		imrs.add(imr.getName());
		
		// Create the left content information
		mainLeftContent = generateLeftContentPane(null);
		
		// Create the right content information
		mainRightContent = new JPanel(new GridBagLayout());
		vulnBean = new VulnerabilityBean();
		siteBean = new Site_GuiBean();
		forecast.getTimeSpan().setDuration(1.0);
		timeBean = new TimeSpanGuiBean(forecast.getTimeSpan());
		timeBean.getTimeSpan().setDuration(1.0);
		siteBean.addSiteParams(imr.getSiteParamsIterator());
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
		
		mainRightContent.add((Component) vulnBean.getVisualization(GuiBeanAPI.APPLICATION), new GridBagConstraints(
				0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 2, 2));
		mainRightContent.add(siteBean, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2)); 
		
		mainRightContent.add(timeBean, new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2));
		
		/* Removed at Nico's request to make room for other parameter/logos.  E.M. 2/25/2007 */
		/*try {
			mainRightContent.add(new ConstrainedStringParameterEditor(new StringParameter("Forecast Model", forecasts)), new GridBagConstraints(
					0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2));
			mainRightContent.add(new ConstrainedStringParameterEditor(new StringParameter("Intensity Measure Relationship", imrs)), new GridBagConstraints(
					0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2));
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		/*mainRightContent.add(btnCalc, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		mainRightContent.add(btnClear, new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		
		mainRightContent.add(creditPanel, new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		*/
		mainRightContent.setPreferredSize(new Dimension(300, 500));
		mainRightContent.setSize(mainRightContent.getPreferredSize());
		
		// Put it all together
		mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mainLeftContent, mainRightContent);
		mainSplitPane.setDividerLocation(0.50);
		
		mainBottomContent = new JPanel(new FlowLayout());
		mainBottomContent.add(btnCalc, 0);
		mainBottomContent.add(btnClear, 1);
		mainBottomContent.add(new JLabel(new ImageIcon(
				ImageUtils.loadImage("PoweredByOpenSHA_Agua.jpg")
			)), 2);
		mainBottomContent.add(new JLabel(new ImageIcon(
				ImageUtils.loadImage("AgoraOpenRisk.jpg")
			)), 3);
		mainBottomContent.add(new JLabel(new ImageIcon(
				ImageUtils.loadImage("usgs_resrisk.gif")
			)), 4);
		
		appSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, mainSplitPane, mainBottomContent);
		add(appSplitPane);
		
	}
	
	protected void btnCalc_actionPerformed(ActionEvent event) {
		//ProgressBar progress = new ProgressBar((Component) this, "Computing", "Computing", creditPanel, 0);
		//progress.run();
		LossCurveCalculator lCalc = new LossCurveCalculator();
		forecast.updateForecast();
		ArbitrarilyDiscretizedFunc hazFunc = getHazardCurve();
		ArbitrarilyDiscretizedFunc lossFunc = lCalc.getLossCurve(hazFunc, vulnBean.getCurrentModel());
		lossFunc.setInfo(getParameterInfoString());
		lossFunc.setName(vulnBean.getCurrentModel().getDisplayName());
		lossFunc.setXAxisName("Damage Factor");
		lossFunc.setYAxisName("Probability of Exceedance");
		lossCurves.add(lossFunc);
		
		mainSplitPane.remove(mainLeftContent);
		mainLeftContent = generateLeftContentPane(lossCurves);
		mainSplitPane.add(mainLeftContent, JSplitPane.LEFT);
		//progress.dispose();
		mainSplitPane.repaint();
	}
	
	protected void btnClear_actionPerformed(ActionEvent event) {
		lossCurves.clear();
		mainSplitPane.remove(mainLeftContent );
		mainLeftContent = generateLeftContentPane(lossCurves);
		mainSplitPane.add(mainLeftContent, JSplitPane.LEFT);
		repaint();
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
	
	private JPanel generateLeftContentPane(ArrayList<ArbitrarilyDiscretizedFunc> funcList) {
		JPanel newLeftContent;
		if(funcList == null || funcList.size() == 0) {
			newLeftContent = new JPanel(new GridBagLayout());
			newLeftContent.add(new JLabel("No Data To Display", SwingConstants.CENTER), new GridBagConstraints(
					0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(5, 5, 5, 5), 2, 2)
			);
		} else {
			newLeftContent = new GraphPane(lossCurves);
			((GraphPane) newLeftContent).setLogSpace(true, true);
		}
		
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
		String TAB = "     ";
		String NEWLINE = System.getProperty("line.separator") + TAB;
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(NEWLINE);
		strBuf.append("Forecast Model:      " + TAB + forecast.getName() + NEWLINE);
		strBuf.append("Duration:               " + TAB + forecast.getTimeSpan().getDuration() + " years" + NEWLINE);
		strBuf.append("Vulnerability Model:" + TAB + vulnBean.getCurrentModel().getDisplayName() + NEWLINE);
		strBuf.append("Vs30 Value:             " + TAB + 
				siteBean.getParameterListEditor().getParameterList().getParameter(
				AttenuationRelationship.VS30_NAME).getValue() + " m/sec" + NEWLINE);
		strBuf.append("Latitude:" + TAB + siteBean.getSite().getLocation().getLatitude() +
				TAB + "Longitude:" + TAB + siteBean.getSite().getLocation().getLongitude() + NEWLINE);
		return strBuf.toString();
	}
	
	private static JFrame createSplashScreen() {
		JFrame splash = new JFrame();
		creditPanel = new JPanel(new FlowLayout());
		/*JLabel openshaImgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("PoweredBy.gif")));
		JLabel usgsImgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("usgslogo.JPG")));
		JLabel riskAgoraImgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("risk-agora_logo.jpg")));*/
		JLabel openshaImgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("PoweredByOpenSHA_Agua.jpg")));
		JLabel usgsImgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("usgs_resrisk.gif")));
		JLabel riskAgoraImgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("AgoraOpenRisk.jpg")));
		/*creditPanel.add(usgsImgLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		creditPanel.add(openshaImgLabel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		creditPanel.add(riskAgoraImgLabel, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		*/
		creditPanel.add(openshaImgLabel);
		creditPanel.add(usgsImgLabel);
		creditPanel.add(riskAgoraImgLabel);
		creditPanel.setPreferredSize(new Dimension(250, 160));
				
		splash.setTitle("A Joint Effort");
		splash.add(creditPanel);
		
		splash.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		splash.setLocation(
	    		(dim.width - splash.getSize().width) / 2, 
	    		(dim.height - splash.getSize().height) / 2
	    	);
	    return splash;
	}

}
