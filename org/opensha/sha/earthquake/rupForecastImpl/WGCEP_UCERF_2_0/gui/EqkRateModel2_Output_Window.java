/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.EqkRateModel2_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.A_FaultSegmentedSource;
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
		setLocationRelativeTo(null);
		this.show();
	}
	
	
	private void createGUI() {
		tabbedPane.addTab("Total Model Summary", getTotalModelSummaryGUI());
		tabbedPane.addTab("A Fault Summary", getA_FaultSummaryGUI());
		tabbedPane.addTab("B Fault Summary", getB_FaultSummaryGUI());
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
		IncrementalMagFreqDist magFreqDist = this.eqkRateModelERF.getTotalMFD();
		textArea.append("Total Rate (M>=5) = "+(float)magFreqDist.getTotalIncrRate()+"\n");
		textArea.append("Total Moment Rate = "+(float)magFreqDist.getTotalMomentRate()+"\n\n");
		textArea.append("\tRate (M>=5)\tMoment Rate\n");
		textArea.append("---------------------------------------\n");
		textArea.append("A Faults\t"+(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_A_FaultsMFD().getTotalMomentRate()+"\n");
		textArea.append("B Char\t"+(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsCharMFD().getTotalMomentRate()+"\n");
		textArea.append("B GR\t"+(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_B_FaultsGR_MFD().getTotalMomentRate()+"\n");
		textArea.append("C Zone\t"+(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_C_ZoneMFD().getTotalMomentRate()+"\n");
		textArea.append("Background\t"+(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotal_BackgroundMFD().getTotalMomentRate()+"\n");
		textArea.append("Total\t"+(float)this.eqkRateModelERF.getTotalMFD().getTotalIncrRate()+"\t"+
				(float)this.eqkRateModelERF.getTotalMFD().getTotalMomentRate()+"\n\n");
		textArea.append("Adjustable Params Metadata:\n");
		textArea.append(eqkRateModelERF.getAdjustableParameterList().getParameterListMetadataString("\n"));
		panel.add(new JScrollPane(textArea),new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		panel.add(plotMFDsButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		textArea.setEditable(false);
		plotMFDsButton.addActionListener(this);
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
		if(rupModel.equalsIgnoreCase(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL)) this.isUnsegmented = true;
		else this.isUnsegmented = false;
		
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
			graphWindow.pack();
			graphWindow.setLocationRelativeTo(this);
			graphWindow.setVisible(true);
		}
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
		
		
		double rate = ((Double)eqkRateModelERF.getParameter(eqkRateModelERF.TOT_MAG_RATE_PARAM_NAME).getValue()).doubleValue();
		boolean includeAfterShocks;
		if(rate > 5.85) includeAfterShocks = true;
		else includeAfterShocks = false;
		// historical best fit cum dist
		funcs.add(this.eqkRateModelERF.getObsBestFitCumMFD(includeAfterShocks));
		
		// historical cum dist
		funcs.addAll(this.eqkRateModelERF.getObsCumMFD(includeAfterShocks));
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
