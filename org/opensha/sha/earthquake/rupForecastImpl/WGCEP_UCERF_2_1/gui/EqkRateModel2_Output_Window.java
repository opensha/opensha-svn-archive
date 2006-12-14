/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.EqkRateModel2_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This shows the output for the EqkRateModel_ERF_GUI
 * 
 * @author vipingupta
 *
 */
public class EqkRateModel2_Output_Window extends JFrame implements GraphWindowAPI, ActionListener, ParameterChangeListener{
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String Y_AXIS_LABEL = "Rate";
	private final static String PLOT_LABEL = "Eqk Rates";
	private ArrayList funcs;
	
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.DARK_GRAY, 2);
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.PINK, 2);
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 5);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	private final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      Color.RED, 5);
	//private final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		//      Color.RED, 5);
	//private final PlotCurveCharacterstics PLOT_CHAR10 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		//      Color.RED, 5);
	private JButton plotMFDsButton = new JButton("Plot Mag Freq Dist");
	private JButton origSlipRateButton = new JButton("Plot the ratio of Final to Orig Slip Rates");
	private JButton modSlipRateButton = new JButton("Plot the ratio of Final to Modified Slip Rates");
	private JButton dataMRIButton = new JButton("Plot the ratio of Final to Data MRI");
	private JButton predMRIButton = new JButton("Plot the ratio of Final to Pred MRI");
	private JButton rupRatesRatioButton = new JButton("Plot the ratio of Final rup Rates to A-Priori Rates");
	private EqkRateModel2_ERF eqkRateModelERF;
	//private ArbitrarilyDiscretizedFunc historicalMFD;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private HashMap aFaultSourceMap;
	private SegmentDataPanel segmentDataPanel;
	private RuptureDataPanel ruptureDataPanel;
	private final static int W = 800;
	private final static int H = 800;
	private StringParameter aFaultParam;
	private final static String A_FAULT_PARAM_NAME = "A Fault";
	private boolean isUnsegmented;
	
	/**
	 * 
	 * @param eqkRateModelERF
	 * @param historicalMFD
	 */
	public EqkRateModel2_Output_Window(EqkRateModel2_ERF eqkRateModelERF) {
		this.eqkRateModelERF = eqkRateModelERF;
		//this.historicalMFD = historicalMFD;
		createGUI();
		this.pack();
		setSize(W,H);
		this.show();
	}
	
	
	private void createGUI() {
		tabbedPane.addTab("Total Model Summary", getTotalModelSummaryGUI());
		tabbedPane.addTab("A Fault Summary", getA_FaultSummaryGUI());
		tabbedPane.addTab("B Fault Summary", getB_FaultSummaryGUI());
		tabbedPane.addTab("C Zones Summary", getC_ZonesSummaryGUI());
		Container container = this.getContentPane();
		container.setLayout(new GridBagLayout());
		container.add(tabbedPane,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
	}
	
	
	/**
	 * Get Total Model Summary
	 * 
	 * @return
	 */
	private JPanel getTotalModelSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		this.createFunctionList();
		JTextArea textArea = new JTextArea();
		textArea.setText("");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		IncrementalMagFreqDist totalMFD = this.eqkRateModelERF.getTotalMFD();
		textArea.append("Total Rate (M>=5) = "+(float)totalMFD.getTotalIncrRate()+"\n");
		boolean includeAfterShocks = this.areAfterShocksIncluded();
		textArea.append("Predicted 6.5 rate over observed = "+(totalMFD.getCumRate(6.5)/this.eqkRateModelERF.getObsCumMFD(includeAfterShocks).get(0).getInterpolatedY(6.5))+"\n");
		textArea.append("Total Moment Rate = "+(float)totalMFD.getTotalMomentRate()+"\n\n");
		textArea.append("\tRate (M>=5)\tRate (M>=6.5)\tMoment Rate\n");
		
		
		textArea.append("------------------------------------------------\n");
		textArea.append("A Faults\t"+(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getTotalMomentRate()+"\n");
		textArea.append("B Char\t"+(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getTotalMomentRate()+"\n");
		textArea.append("B GR\t"+(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getTotalMomentRate()+"\n");
		textArea.append("C Zone\t"+(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getTotalMomentRate()+"\n");
		textArea.append("Background\t"+(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getCumRate(6.5)+"\t"+
				(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getTotalMomentRate()+"\n");
		textArea.append("Total\t"+(float)totalMFD.getTotalIncrRate()+"\t"+
				(float)totalMFD.getCumRate(6.5)+"\t"+
				(float)totalMFD.getTotalMomentRate()+"\n\n");
		textArea.append("Adjustable Params Metadata:\n");
		textArea.append(eqkRateModelERF.getAdjustableParameterList().getParameterListMetadataString("\n"));
		panel.add(new JScrollPane(textArea),new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(plotMFDsButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(origSlipRateButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(this.modSlipRateButton,new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(predMRIButton,new GridBagConstraints( 0, 4, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(dataMRIButton,new GridBagConstraints( 0, 5, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(rupRatesRatioButton,new GridBagConstraints( 0, 6, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		textArea.setEditable(false);
		plotMFDsButton.addActionListener(this);
		this.origSlipRateButton.addActionListener(this);
		this.modSlipRateButton.addActionListener(this);
		this.predMRIButton.addActionListener(this);
		this.dataMRIButton.addActionListener(this);
		rupRatesRatioButton.addActionListener(this);
		return panel;
	}
	
	/**
	 * A Fault summary GUI
	 * 
	 * @return
	 */
	private JPanel getA_FaultSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		aFaultSourceMap = new HashMap();
		ArrayList aFaultSources = this.eqkRateModelERF.get_A_FaultSources();
		
		// whether this is segmented or unsegmented
		String rupModel = (String)eqkRateModelERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).getValue();
		if(rupModel.equalsIgnoreCase(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL)) {
			this.isUnsegmented = true;
			this.origSlipRateButton.setVisible(false);
			this.predMRIButton.setVisible(false);
			this.dataMRIButton.setVisible(false);
		}
		else {
			this.isUnsegmented = false;
			this.origSlipRateButton.setVisible(true);
			this.predMRIButton.setVisible(true);
			this.dataMRIButton.setVisible(true);
		}
		
		if(aFaultSources==null) return panel;
		segmentDataPanel = new SegmentDataPanel();
		ArrayList faultNames = new ArrayList();
		for(int i=0; i<aFaultSources.size(); ++i) {
			Object source = aFaultSources.get(i);
			FaultSegmentData faultSegmentData = getFaultSegmentData(source);
			faultNames.add(faultSegmentData.getFaultName());
			aFaultSourceMap.put(faultSegmentData.getFaultName(), source);
		}
		this.aFaultParam = new StringParameter(A_FAULT_PARAM_NAME, faultNames, (String)faultNames.get(0));
		aFaultParam.addParameterChangeListener(this);
		ConstrainedStringParameterEditor paramEditor = new ConstrainedStringParameterEditor(aFaultParam);
		panel.add(paramEditor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		JTabbedPane segmentInfoTabbedPane = new JTabbedPane();
		segmentInfoTabbedPane.addTab("Segment Info", segmentDataPanel);
		if(this.isUnsegmented) {
			B_FaultDataPanel bFaultDataPanel = new B_FaultDataPanel();
			bFaultDataPanel.setB_FaultSources(aFaultSources);
			segmentInfoTabbedPane.addTab("Rupture Info", bFaultDataPanel);
		} else {
			ruptureDataPanel = new RuptureDataPanel();
			segmentInfoTabbedPane.addTab("Rupture Info", ruptureDataPanel);
			ruptureDataPanel.setSourcesForMagAreaPlot(aFaultSources, this.eqkRateModelERF.getMagAreaRelationships());
		}
		panel.add(segmentInfoTabbedPane,new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		
		updateA_FaultTableData();
		return panel;
	}
	
	/**
	 * Get fault segment data based on A fault source type (whether it is segmented or unsegmented)
	 * @param source
	 * @return
	 */
	private FaultSegmentData getFaultSegmentData(Object source) {
		if(this.isUnsegmented) return ((UnsegmentedSource)source).getFaultSegmentData();
		else return ((A_FaultSegmentedSource)source).getFaultSegmentData();
	}
	
	/**
	 * B faults sources
	 * @return
	 */
	private JPanel getB_FaultSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		ArrayList bFaultSources = this.eqkRateModelERF.get_B_FaultSources();
		B_FaultDataPanel bFaultDataPanel = new B_FaultDataPanel();
		bFaultDataPanel.setB_FaultSources(bFaultSources);
		panel.add(bFaultDataPanel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		return panel;
	}
	
	
	/**
	 * C Zones
	 * @return
	 */
	private JPanel getC_ZonesSummaryGUI() {
		JPanel panel = new JPanel(new GridBagLayout());
		ArrayList<IncrementalMagFreqDist> cZonesMFDs = this.eqkRateModelERF.getC_ZoneMFD_List();
		C_ZoneDataPanel cZonesDataPanel = new C_ZoneDataPanel();
		cZonesDataPanel.setC_ZonesMFD_List(cZonesMFDs);
		panel.add(cZonesDataPanel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		return panel;
	}
	
	/**
	 * 
	 * @param event
	 */
	public void parameterChange(ParameterChangeEvent event) {
		updateA_FaultTableData();
	}
	
	
	/**
	 * update the A fault table data based on the selected A fault
	 *
	 */
	private void updateA_FaultTableData() {
		String selectedFault = (String)aFaultParam.getValue();
		Object source =   aFaultSourceMap.get(selectedFault);
		boolean isAseisReducesArea = ((Boolean)this.eqkRateModelERF.getParameter(EqkRateModel2_ERF.ASEIS_INTER_PARAM_NAME).getValue()).booleanValue();
		if(!this.isUnsegmented)  {
			ruptureDataPanel.setSource((A_FaultSegmentedSource)source);
			this.segmentDataPanel.setFaultSegmentData((A_FaultSegmentedSource)source, isAseisReducesArea, this.eqkRateModelERF.getMagAreaRelationships());
		} else {
			this.segmentDataPanel.setFaultSegmentData((UnsegmentedSource)source, isAseisReducesArea, this.eqkRateModelERF.getMagAreaRelationships());
		}
	} 


	/**
	 * When Calc button is clicked
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if(src == this.plotMFDsButton) {
			GraphWindow graphWindow= new GraphWindow(this);
			graphWindow.setPlotLabel(PLOT_LABEL);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		} else if(src == this.modSlipRateButton) { // ratio of modified slip rates
			plotModSlipRatesRatio();
		}
		else if(src == this.origSlipRateButton) { // ratio of original slip rates
			plotOrigSlipRatesRatio();
		}else if(src == this.dataMRIButton) { // ratio of final MRIs and data MRI
			plotDataMRIRatio();
		}else if(src == this.predMRIButton) { // ratio of final MRI and pred MRI
			plotPredMRIRatio();
		} else if(src == this.rupRatesRatioButton) { // ratio of final rates to A-Priori Rates
			plotRupRatesRatio();
		}
	}

	/**
	 * Plot the ratio of slip rates
	 *
	 */
	private void plotRupRatesRatio() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		ArrayList<Double> ratioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numRuptures = source.getNumRuptures();
			// iterate over all ruptures
			for(int rupIndex = 0; rupIndex<numRuptures; ++rupIndex) 
				ratioList.add(source.getRupRate(rupIndex)/source.getAPrioriRupRate(rupIndex));
		}
		String plotLabel = "Rupture Rates Ratio";
		showHistograms(ratioList, plotLabel, "Ratio of final Rup Rates to A-Priori Rates");
	}
	
	/**
	 * Plot the ratio of slip rates
	 *
	 */
	private void plotOrigSlipRatesRatio() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		ArrayList<Double> ratioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numSegments = source.getFaultSegmentData().getNumSegments();
			// iterate over all segments
			for(int segIndex = 0; segIndex<numSegments; ++segIndex) 
				ratioList.add(source.getFinalSegSlipRate(segIndex)/source.getFaultSegmentData().getSegmentSlipRate(segIndex));
		}
		String plotLabel = "Original Slip Rates Ratio";
		showHistograms(ratioList, plotLabel, "Ratio of final Slip Rates to Original Slip Rates");
	}
	
	/**
	 * Plot  ratio of modified slip rates
	 *
	 */
	private void plotModSlipRatesRatio() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		double  reduction;
		ArrayList<Double> ratioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numSegments = source.getFaultSegmentData().getNumSegments();
			// iterate over all segments
			reduction = 1-source.getMoRateReduction();
			for(int segIndex = 0; segIndex<numSegments; ++segIndex) 
				ratioList.add(source.getFinalSegSlipRate(segIndex)/(source.getFaultSegmentData().getSegmentSlipRate(segIndex)*reduction));
		}
		String plotLabel = "Modified Slip Rates Ratio";
		showHistograms(ratioList, plotLabel, "Ratio of final Slip Rates to Modified Slip Rates");
	}
	
	/**
	 * Plot the ratio of MRIs
	 *
	 */
	private void plotDataMRIRatio() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		ArrayList<Double> ratioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numSegments = source.getFaultSegmentData().getNumSegments();
			// iterate over all segments
			for(int segIndex = 0; segIndex<numSegments; ++segIndex) {
				if(Double.isNaN(source.getFaultSegmentData().getRecurInterval(segIndex))) continue;
				ratioList.add(source.getFinalSegRecurInt(segIndex)/source.getFaultSegmentData().getRecurInterval(segIndex));
				}
		}
		String plotLabel = "Final and Data MRI Ratio";
		showHistograms(ratioList, plotLabel, "Ratio of final MRI to Data MRI");
	}
	
	/**
	 * Plot the ratio of MRIs
	 *
	 */
	private void plotPredMRIRatio() {
		ArrayList<A_FaultSegmentedSource> sourceList = this.eqkRateModelERF.get_A_FaultSources();
		ArrayList<Double> ratioList = new ArrayList<Double>();
		// iterate over all sources
		for(int i=0; i<sourceList.size(); ++i) {
			A_FaultSegmentedSource source = sourceList.get(i);
			int numSegments = source.getFaultSegmentData().getNumSegments();
			// iterate over all segments
			for(int segIndex = 0; segIndex<numSegments; ++segIndex) 
				ratioList.add(source.getFinalSegRecurInt(segIndex)*source.getSegRateFromAprioriRates(segIndex));
		}
		String plotLabel = "Final and Pred MRI Ratio";
		showHistograms(ratioList, plotLabel, "Ratio of final MRI to Pred MRI");
	}

	/**
	 * Show histograms
	 * @param func
	 * @param plotLabel
	 */
	private void showHistograms(ArrayList<Double> ratioList, String plotLabel, String funcName) {
		double min = Math.floor(Collections.min(ratioList));
		double max = Math.ceil(Collections.max(ratioList));
		double delta = 0.1;
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(min, (int)Math.round((max-min)/delta)+1, delta);
		func.setTolerance(func.getDelta());
		int xIndex;
		for(int i=0; i<ratioList.size(); ++i) {
			xIndex = func.getXIndex(ratioList.get(i));
			func.add(xIndex, 1.0);
		}
		ArrayList funcs = new ArrayList();
		funcs.add(func);
		String yAxisLabel = "Count";
		GraphWindow graphWindow= new GraphWindow(new CreateHistogramsFromSegSlipRateFile(funcs, plotLabel, yAxisLabel));
		graphWindow.setPlotLabel(plotLabel);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(true);
	}
	
	/**
	 * Create Function List
	 *
	 */
	private void createFunctionList() {
		funcs = new ArrayList();
		
		// Type A faults cum Dist
		EvenlyDiscretizedFunc cumDist = eqkRateModelERF.getTotal_A_FaultsMFD().getCumRateDist();
		cumDist.setInfo("Type A-Faults Total Mag Freq Dist");
		funcs.add(cumDist);
		 // Type B faults Char cum Dist
		cumDist = eqkRateModelERF.getTotal_B_FaultsCharMFD().getCumRateDist();
		cumDist.setInfo("Type B-Faults Total Char Mag Freq Dist");
		funcs.add(cumDist);
		//	Type B faults GR cum Dist
		cumDist = eqkRateModelERF.getTotal_B_FaultsGR_MFD().getCumRateDist();
		cumDist.setInfo("Type B-Faults Total GR Mag Freq Dist");
		funcs.add(cumDist);
		//	Background cum Dist
		cumDist = eqkRateModelERF.getTotal_BackgroundMFD().getCumRateDist();
		cumDist.setInfo("BackGround Total  Mag Freq Dist");
		funcs.add(cumDist);
		//	C zone cum Dist
		cumDist = eqkRateModelERF.getTotal_C_ZoneMFD().getCumRateDist();
		cumDist.setInfo("C Zone Total  Mag Freq Dist");
		funcs.add(cumDist);
		//	Total cum Dist
		cumDist = eqkRateModelERF.getTotalMFD().getCumRateDist();
		cumDist.setInfo("Total  Mag Freq Dist");
		funcs.add(cumDist);
		
		
		boolean includeAfterShocks = areAfterShocksIncluded();
		// historical best fit cum dist
		funcs.add(this.eqkRateModelERF.getObsBestFitCumMFD(includeAfterShocks));
		
		// historical cum dist
		funcs.addAll(this.eqkRateModelERF.getObsCumMFD(includeAfterShocks));
	}

	/**
	 * Whether to include the aftershocks
	 * 
	 * @return
	 */
	private boolean areAfterShocksIncluded() {
		double rate = ((Double)eqkRateModelERF.getParameter(eqkRateModelERF.TOT_MAG_RATE_PARAM_NAME).getValue()).doubleValue();
		boolean includeAfterShocks;
		if(rate > 5.85) includeAfterShocks = true;
		else includeAfterShocks = false;
		return includeAfterShocks;
	}
	
	

	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return funcs;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public boolean getXLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return X_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		return Y_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		 ArrayList list = new ArrayList();
		 list.add(this.PLOT_CHAR1);
		 list.add(this.PLOT_CHAR2);
		 list.add(this.PLOT_CHAR3);
		 list.add(this.PLOT_CHAR4);
		 list.add(this.PLOT_CHAR5);
		 list.add(this.PLOT_CHAR6);
		 list.add(this.PLOT_CHAR7);
		 list.add(this.PLOT_CHAR8);
		 list.add(this.PLOT_CHAR8);
		 list.add(this.PLOT_CHAR8);
		 return list;
	}
	

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		return 5.0;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		return 9.255;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		return 1e-4;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		return 10;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	

}
