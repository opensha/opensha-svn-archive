/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.A_FaultFloatingSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.SegmentedFaultData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.WG_02FaultSource;

/**
 * Output for A floating source
 * @author vipingupta
 *
 */
public class A_FloatingSourceOuput extends JFrame implements ActionListener{
	private JTabbedPane outputTabbedPane = new JTabbedPane();
	private final static int W = 800;
	private final static int H = 600;
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.#####E0");
	private JButton segmentSlipButton = new JButton("Cum Slip Dist Plot");
	private JButton visibleFloaterMFDButton = new JButton("Visible Floater Mag Freq Dist Plot");
	private JButton floaterDistButton = new JButton("Floater Mag Freq Dist Plot");
	private ArrayList cumSlipFuncList;
	private ArrayList visibleFloaterMFD;
	private ArrayList floaterMFD;
	private final static String TITLE = "A Floating Source Output";
	
	
	/**
	 * Accepts A_FaultSource object and creates output to show segment data, rupture data,
	 * floating rupture data
	 * 
	 * @param aFaultSource
	 * @param inputParameters
	 */
	public A_FloatingSourceOuput(A_FaultFloatingSource aFaultFloatingSource,
			SegmentedFaultData segmentedFaultData, 
			String inputParameters) {
		this.getContentPane().setLayout(new GridBagLayout());
		// show data related to each segment
		createSegmentOutput(aFaultFloatingSource, segmentedFaultData);
		// show data related to ruptures
		createRupturesOutput(aFaultFloatingSource, segmentedFaultData);
		// show data related to Floater ruptures
		//createFloaterOutput(aFaultSource);
		// show the input parameters as well
		showInputParameters(inputParameters);
		setTitle(TITLE);
		getContentPane().add(this.outputTabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
		setSize(W,H);
		show();
	}
	
	/**
	 * Create Segment output data
	 * 
	 * @param aFaultSource
	 */
	private void createSegmentOutput(A_FaultFloatingSource aFaultFloatingSource,
			SegmentedFaultData segmentedFaultData) {
		JPanel panel = new JPanel(new GridBagLayout());
		SegmentOutputTableModel segmentOutputModel = 
			new SegmentOutputTableModel(aFaultFloatingSource, segmentedFaultData);
		JTable segmentOutputTable = new JTable(segmentOutputModel);
		panel.add(new JScrollPane(segmentOutputTable), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
		panel.add(this.segmentSlipButton, new GridBagConstraints(0, 1, 1, 0, 1.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
		segmentSlipButton.addActionListener(this);
		this.outputTabbedPane.addTab("Segment Info", panel);
		// list index, name, area, rate and recurrence interval(1/rate) for each segment
		int numSegments = segmentedFaultData.getNumSegments();
		cumSlipFuncList = new ArrayList();
		for(int i=0; i<numSegments; ++i) {
			ArbitrarilyDiscretizedFunc func = aFaultFloatingSource.getSegmentSlipDist(i).getCumDist();
			func.setInfo("Cumulative Slip distribution for segment "+(i+1));
			cumSlipFuncList.add(func);
		}		
	}

	
	/**
	 * Create Ruptures output
	 * @param aFaultSource
	 */
	private void createRupturesOutput(A_FaultFloatingSource aFaultFloatingSource,
			SegmentedFaultData segmentedFaultData) {
		JPanel panel = new JPanel(new GridBagLayout());
		JTextArea rupOutput = new JTextArea();
		rupOutput.setEditable(false);
		panel.add(new JScrollPane(rupOutput), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
		panel.add(this.visibleFloaterMFDButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
		visibleFloaterMFDButton.addActionListener(this);
		panel.add(this.floaterDistButton, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
		floaterDistButton.addActionListener(this);
		this.outputTabbedPane.addTab("Rupture Info", panel);
		// list index, meanMag, total Rate, name for each rupture
		rupOutput.append("Total Original Moment Rate (from segments)="+MOMENT_FORMAT.format(segmentedFaultData.getTotalMomentRate())+"\n");
		rupOutput.append("Total Final Moment Rate (from MFD)="+MOMENT_FORMAT.format(aFaultFloatingSource.getFloaterMagFreqDist().getTotalMomentRate())+"\n");
		rupOutput.setCaretPosition(0);
		if(aFaultFloatingSource.getFloaterMagFreqDist()!=null) {
			this.floaterMFD = new ArrayList();
			floaterMFD.add(aFaultFloatingSource.getFloaterMagFreqDist());
		} else floaterMFD = null;
		this.visibleFloaterMFD  = new ArrayList();
		visibleFloaterMFD.add(aFaultFloatingSource.getVisibleFloaterMagFreqDist());
	}
	
	/**
	 * Output for floater ruptures
	 * 
	 * @param aFaultSource
	 */
	/*private void createFloaterOutput(A_FaultSource aFaultSource) {
		JTextArea floaterOutput = new JTextArea();
		floaterOutput.setEditable(false);
		this.outputTabbedPane.addTab("Floater Ruptures", new JScrollPane(floaterOutput));
		floaterOutput.append(aFaultSource.getFloaterMagFreqDist().toString());
		floaterOutput.setCaretPosition(0);
	}*/
	
	/**
	 * Show input parameters as metadata
	 * 
	 * @param metadata
	 */
	private void showInputParameters(String metadata) {
		JTextArea metadataTextArea = new JTextArea();
		metadataTextArea.setEditable(false);
		metadataTextArea.setWrapStyleWord(true);
		metadataTextArea.setLineWrap(true);
		this.outputTabbedPane.addTab("Metadata", new JScrollPane(metadataTextArea));
		metadataTextArea.append(metadata);
		metadataTextArea.setCaretPosition(0);
	}
	
	/**
	 * This function is called when a buton is clicked
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if(source == this.segmentSlipButton)
			new WG02_RuptureModelsGraphWindowAPI_Impl(cumSlipFuncList, "Slip (meters)", "Rate", "Cum Slip Dist for Segments");
		else if(source == this.visibleFloaterMFDButton)
			new WG02_RuptureModelsGraphWindowAPI_Impl(this.visibleFloaterMFD, "Mag", "Rate", "Visible Mag Freq Dist");
		else if(source == this.floaterDistButton) {
			if(floaterMFD==null){
				 JOptionPane.showMessageDialog(this, "Floater MFD not available");
				 return;
			}
			new WG02_RuptureModelsGraphWindowAPI_Impl(this.floaterMFD, "Mag", "Rate", "Mag Freq Dist");
		}
	}
}


/**
 * Segment output Table Model
 * 
 * @author vipingupta
 *
 */
class SegmentOutputTableModel extends AbstractTableModel {
//	 column names
	private final static String[] columnNames = { "Index", "Name", 
		"Rate","Recur Intv","Visible Rate", "Visible Recur Intv",
		"Orig Slip Rate(mm/yr)", "Implied Slip Rate"};
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private A_FaultFloatingSource floatingSource;
	private SegmentedFaultData segmentedFaultData;
	
	/**
	 * default constructor
	 *
	 */
	public SegmentOutputTableModel(A_FaultFloatingSource floatingSource, 
			SegmentedFaultData segmentedFaultData) {
		this.segmentedFaultData = segmentedFaultData;
		this.floatingSource = floatingSource;
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
		if(segmentedFaultData==null) return 0;
		return (segmentedFaultData.getNumSegments()); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		if(segmentedFaultData==null) return "";
		//"Index", "Name", 
		//"Rate","Recur Intv","Visible Rate", "Visible Recur Intv",
		//"Orig Slip Rate", "Implied Slip Rate"
		
		switch(columnIndex) {
			case 0:
				return ""+(rowIndex+1);
			case 1: 
				return segmentedFaultData.getSegmentName(rowIndex);
			case 2:
				return ""+this.floatingSource.getSegmentRate(rowIndex);
			case 3:
				return ""+ (int)floatingSource.getSegmentRecurrenceInterval(rowIndex);
			case 4:
				return ""+this.floatingSource.getSegmentVisibleRate(rowIndex);
			case 5:
				return ""+ (int)floatingSource.getSegmentVisibleRecurrenceInterval(rowIndex);
			case 6:
				// convert to mm/yr
				return SLIP_RATE_FORMAT.format(segmentedFaultData.getSegmentSlipRate(rowIndex)*1000);
			case 7:
				return SLIP_RATE_FORMAT.format(floatingSource.getFinalAveSegSlipRate(rowIndex));
		}
		return "";
	}
}

