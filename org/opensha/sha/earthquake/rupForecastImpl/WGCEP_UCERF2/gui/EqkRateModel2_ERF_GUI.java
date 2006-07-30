package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.gui;


import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.param.ParameterList;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.EqkRateModel2_ERF;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;


/**
 * This class allows the user the user to adjust various parameters in EqkRateModel2_ERF and
 * the resulting Mag freq Distributions
 * @author vipingupta
 *
 */
public class EqkRateModel2_ERF_GUI extends JFrame implements ActionListener, GraphWindowAPI{
	
	private EqkRateModel2_ERF eqkRateModelERF= new EqkRateModel2_ERF(); // erf to get the adjustable params
	private ParameterListEditor editor; // editor
	private final static String TITLE = "Eqk Rate Model2 Params";
	private JButton calcButton = new JButton("Calculate");
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String Y_AXIS_LABEL = "Rate";
	private final static String PLOT_LABEL = "Eqk Rates";
	private ArrayList funcs;
	private ArbitrarilyDiscretizedFunc historicalMFD;
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.DARK_GRAY, 2);
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.YELLOW, 2);
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 5);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	
	public static void main(String[] args) {
		new EqkRateModel2_ERF_GUI();
	}
	
	public EqkRateModel2_ERF_GUI() {
		// make parameter list editor
		ParameterList paramList = eqkRateModelERF.getAdjustableParameterList();
		editor = new ParameterListEditor(paramList);
		editor.setTitle(TITLE);
		createHistoricalMFD();
		createGUI();
		calcButton.addActionListener(this);
		pack();
		this.setLocationRelativeTo(null);
		show();
	}
	
	private void createHistoricalMFD() {
		historicalMFD = new ArbitrarilyDiscretizedFunc();
		double fractionInRELM_Region=0.75;
		historicalMFD.set(5.0, 11.5*fractionInRELM_Region);
		historicalMFD.set(5.5, 4.7*fractionInRELM_Region);
		historicalMFD.set(6.0, 1.07*fractionInRELM_Region);
		historicalMFD.set(6.5, 0.36*fractionInRELM_Region);
		historicalMFD.set(7.0, 0.089*fractionInRELM_Region);
		historicalMFD.set(7.5, 0.019*fractionInRELM_Region);
		historicalMFD.setInfo("Observed CUMULATIVE distribution (w/ model cum dist. prediction)");
	}
	
	/**
	 * Create GUI
	 *
	 */
	private void createGUI() {
		Container container = this.getContentPane();
		container.setLayout(new GridBagLayout());
		container.add(this.editor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		container.add(this.calcButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));

	}
	
	/**
	 * When Calc button is clicked
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		this.createFunctionList();
		GraphWindow graphWindow= new GraphWindow(this);
	    graphWindow.setPlotLabel(PLOT_LABEL);
	    graphWindow.plotGraphUsingPlotPreferences();
	    //graphWindow.pack();
	    graphWindow.setVisible(true);
	}
	
	/**
	 * Create Function List
	 *
	 */
	private void createFunctionList() {
		this.eqkRateModelERF.updateForecast();
		funcs = new ArrayList();
		funcs.add(eqkRateModelERF.getTotal_A_FaultsMFD());
		funcs.add(eqkRateModelERF.getTotal_B_FaultsCharMFD());
		funcs.add(eqkRateModelERF.getTotal_B_FaultsGR_MFD());
		funcs.add(eqkRateModelERF.getTotal_BackgroundMFD());
		funcs.add(eqkRateModelERF.getTotal_C_ZoneMFD());
		funcs.add(eqkRateModelERF.getTotalMFD());
		funcs.add(historicalMFD);
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
		return false;
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
		 return list;
	}
	

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
}
