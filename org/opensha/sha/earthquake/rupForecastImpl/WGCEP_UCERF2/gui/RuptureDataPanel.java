/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.gui.WG02_RuptureModelsGraphWindowAPI_Impl;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * Show the rupture data in the window
 * 
 * @author vipingupta
 *
 */
public class RuptureDataPanel extends JPanel implements ActionListener, GraphWindowAPI {
	private RuptureTableModel rupTableModel = new RuptureTableModel();
	private JButton mfdButton = new JButton("Selected A Fault MFD");
	private JButton magAreaPlotButton = new JButton("Mag Area Plot");
	private A_FaultSegmentedSource source;
	
	//	Filled Circles for rupture from each plot
	public final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.BLUE, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.RED, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.GREEN, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.BLACK, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.MAGENTA, 2);	
	protected final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.ORANGE, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.PINK, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.YELLOW, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.CYAN, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR10 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.DARK_GRAY, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR11 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.LIGHT_GRAY, 2);
	protected final PlotCurveCharacterstics PLOT_CHAR12 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.FILLED_CIRCLES,
		      Color.GRAY, 2);
	
	// solid lines for Mag Area rel
	public final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	protected final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	protected final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	protected final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 2);
	protected final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);	
	protected final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.ORANGE, 2);
	protected final PlotCurveCharacterstics MAG_AREA_PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.PINK, 2);

	private final static double MIN_AREA = 100; // sq km
	private final static double MAX_AREA = 10000; // sq km
	
	private ArrayList plottingFeatures;
	private ArrayList magAreaFuncs;

	
	
	public RuptureDataPanel() {
		this.setLayout(new GridBagLayout());
		
		add(new JScrollPane(new JTable(this.rupTableModel)),new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		add(mfdButton,new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		add(magAreaPlotButton,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
	      	      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		mfdButton.addActionListener(this);
		magAreaPlotButton.addActionListener(this);
		mfdButton.setEnabled(false);
		magAreaPlotButton.setEnabled(false);
	}
	
	/**
	 * Set source list and mag area relationships for Mag Area plot
	 * 
	 * @param aFaultSegmentedSourceList
	 */
	public void setSourcesForMagAreaPlot(ArrayList aFaultSegmentedSourceList, ArrayList magAreaRels) {
		plottingFeatures = new ArrayList();
		magAreaFuncs = new ArrayList();
		magAreaPlotButton.setEnabled(true);
		int numFaults = aFaultSegmentedSourceList.size();
		int numMagAreaRels = magAreaRels.size();
		
		// create function list for all faults
		for(int i=0; i<numFaults; ++i) {
			A_FaultSegmentedSource aFaultSegmentedSource = (A_FaultSegmentedSource) aFaultSegmentedSourceList.get(i);
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			for(int j=0; j<aFaultSegmentedSource.getNumRuptures(); ++j) {
				//func.set(x, y)
			}
		}
		
		// create function list for mag area relationships
		double min = Math.log10(MIN_AREA);
		double max = Math.log10(MAX_AREA);
		int numPoints =101;
		double delta = (min-max)/(numPoints-1);
		double area;
		for(int i=0; i<numMagAreaRels; ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRels.get(i);
			ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
			for(int j=0; j<=numPoints; ++j) {
				area = Math.pow(10, min+j*delta);
				func.set(area, magAreaRel.getMedianMag(area));
			}
			magAreaFuncs.add(func);
		}
		
		
		// plotting features for rupture area and mag
		if(numFaults>0) plottingFeatures.add(this.PLOT_CHAR1);
		if(numFaults>1) plottingFeatures.add(this.PLOT_CHAR2);
		if(numFaults>2) plottingFeatures.add(this.PLOT_CHAR3);
		if(numFaults>3) plottingFeatures.add(this.PLOT_CHAR4);
		if(numFaults>4) plottingFeatures.add(this.PLOT_CHAR5);
		if(numFaults>5) plottingFeatures.add(this.PLOT_CHAR6);
		if(numFaults>6) plottingFeatures.add(this.PLOT_CHAR7);
		if(numFaults>7) plottingFeatures.add(this.PLOT_CHAR8);
		if(numFaults>8) plottingFeatures.add(this.PLOT_CHAR9);
		if(numFaults>9) plottingFeatures.add(this.PLOT_CHAR10);
		if(numFaults>10) plottingFeatures.add(this.PLOT_CHAR11);
		if(numFaults>11) plottingFeatures.add(this.PLOT_CHAR12);
		
		// plotting features for mag area rels
		if(numMagAreaRels>0) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR1);
		if(numMagAreaRels>1) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR2);
		if(numMagAreaRels>2) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR3);
		if(numMagAreaRels>3) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR4);
		if(numMagAreaRels>4) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR5);
		if(numMagAreaRels>5) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR6);
		if(numMagAreaRels>6) plottingFeatures.add(this.MAG_AREA_PLOT_CHAR7);
	}
	
	public void actionPerformed(ActionEvent event) {
		Object eventSource = event.getSource();
		if(eventSource == mfdButton) { // MFD for selected A Fault
			ArrayList funcs = new ArrayList();
			funcs.add(source.getTotalRupMFD());
			new WG02_RuptureModelsGraphWindowAPI_Impl(funcs, "Mag", "Rate", "Mag Rate");
		} else if(eventSource == this.magAreaPlotButton) {
			GraphWindow graphWindow= new GraphWindow(this);
		    graphWindow.setPlotLabel("Mag Area Plot");
		    graphWindow.plotGraphUsingPlotPreferences();
		    //graphWindow.pack();
		    graphWindow.setVisible(true);;
		}
	}
	
	/**
	 * Set the source to update the rupture info
	 * 
	 * @param aFaultSegmentedSource
	 */
	public void setSource(A_FaultSegmentedSource aFaultSegmentedSource) {
		this.source = aFaultSegmentedSource;
		if(source!=null) mfdButton.setEnabled(true);
		else mfdButton.setEnabled(false);
		rupTableModel.setFaultSegmentedSource(aFaultSegmentedSource);
		rupTableModel.fireTableDataChanged();
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return this.magAreaFuncs;
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
		return "Area";
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		return "Mag";
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		return this.plottingFeatures;
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



/**
* Fault Section Table Model
* 
* @author vipingupta
*
*/
class RuptureTableModel extends AbstractTableModel {
//	 column names
	private final static String[] columnNames = { "Rup Index", "Area (sq km)", "Mean Mag", 
		"Final Rate", "A Priori Rate", "Moment Rate",  "Short Name", "Long Name"};
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat RATE_FORMAT = new DecimalFormat("0.00000");
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.#####E0");
	private A_FaultSegmentedSource aFaultSegmentedSource;
	
	/**
	 * default constructor
	 *
	 */
	public RuptureTableModel() {
		this(null);
	}
	
	/**
	 *  Preferred Fault section data
	 *  
	 * @param faultSectionsPrefDataList  ArrayList of PrefFaultSedctionData
	 */
	public RuptureTableModel(A_FaultSegmentedSource aFaultSegmentedSource) {
		setFaultSegmentedSource(aFaultSegmentedSource);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setFaultSegmentedSource(A_FaultSegmentedSource aFaultSegmentedSource) {
		this.aFaultSegmentedSource =   aFaultSegmentedSource;
	}
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if(aFaultSegmentedSource==null) return 0;
		return (aFaultSegmentedSource.getNumRuptures()+1); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
			
		if(aFaultSegmentedSource==null) return "";
		if(rowIndex == aFaultSegmentedSource.getNumRuptures()) return getTotal(columnIndex);
		switch(columnIndex) {
			case 0:
				return ""+(rowIndex+1);
			case 1: 
				return AREA_LENGTH_FORMAT.format(aFaultSegmentedSource.getRupArea(rowIndex)/1e6);
			case 2:
				return MAG_FORMAT.format(aFaultSegmentedSource.getRupMeanMag(rowIndex));
			case 3:
				return RATE_FORMAT.format(aFaultSegmentedSource.getRupRate(rowIndex));
			case 4:
				return RATE_FORMAT.format(aFaultSegmentedSource.getAPrioriRupRate(rowIndex));
			case 5:
				return MOMENT_FORMAT.format(aFaultSegmentedSource.getRupMoRate(rowIndex));
			case 6:
				return aFaultSegmentedSource.getShortRupName(rowIndex);
			case 7:
				return aFaultSegmentedSource.getLongRupName(rowIndex);
		}
		return "";
	}
	
	/**
	 * 
	 * @param colIndex
	 * @return
	 */
	private String getTotal(int colIndex) {
		switch(colIndex) {
		case 0:
			return "Total";
		case 5:
			if(aFaultSegmentedSource!=null)
				return MOMENT_FORMAT.format(aFaultSegmentedSource.getTotalMoRateFromRups());
		
		}
		return "";
	}
}

