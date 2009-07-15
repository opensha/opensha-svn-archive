/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.gui;

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
import javax.swing.JTextArea;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.WG_02FaultSource;

/**
 *This class generaates a window to show the ouput of the Rupture Model App
 *
 * @author vipingupta
 *
 */
public class WG02_RuptureModelOuput extends JFrame implements ActionListener{
	private JTabbedPane outputTabbedPane = new JTabbedPane();
	private final static int W = 800;
	private final static int H = 600;
	private JButton segmentSlipButton = new JButton("Cum Slip Dist Plot");
	private JButton totalRupMFDButton = new JButton("Total Mag Freq Dist Plot");
	private JButton floaterDistButton = new JButton("Floater Mag Freq Dist Plot");
	private ArrayList cumSlipFuncList;
	private ArrayList totalRupMFD;
	private ArrayList floaterRupMFD;
	
	
	/**
	 * Accepts A_FaultSource object and creates output to show segment data, rupture data,
	 * floating rupture data
	 * 
	 * @param aFaultSource
	 * @param inputParameters
	 */
	public WG02_RuptureModelOuput(WG_02FaultSource aFaultSource, String inputParameters) {
		this.getContentPane().setLayout(new GridBagLayout());
		// show data related to each segment
		createSegmentOutput(aFaultSource);
		// show data related to ruptures
		createRupturesOutput(aFaultSource);
		// show data related to Floater ruptures
		//createFloaterOutput(aFaultSource);
		// show the input parameters as well
		showInputParameters(inputParameters);
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
	private void createSegmentOutput(WG_02FaultSource aFaultSource) {
		JPanel panel = new JPanel(new GridBagLayout());
		JTextArea segmentOutput = new JTextArea();
		segmentOutput.setEditable(false);
		panel.add(new JScrollPane(segmentOutput), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
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
		int numSegments = aFaultSource.getNumSegments();
		cumSlipFuncList = new ArrayList();
		segmentOutput.append("Index\tSegment Area(sq. m)\tSegment Rate\t\tRecurrence Int\tSegment Name\n\n");
		String initialSlipRateStr = "\nInitial Ave Seg Slip Rate:\nIndex\tSlip Rate(mm/yr)\n";
		String finalSlipRateStr = "\nFinal Seg Slip Rate:\nIndex\tSlip Rate(mm/yr)\n";
		for(int i=0; i<numSegments; ++i) {
			segmentOutput.append((i+1)+"\t"+
					(float)aFaultSource.getSegmentArea(i)+"\t\t"+
					+(float)aFaultSource.getSegmentRate(i)+"\t\t"+
					Math.round(aFaultSource.getSegmentRecurrenceInterval(i))+"\t"+
					aFaultSource.getSegmentName(i)+"\n");
			initialSlipRateStr+=(i+1)+"\t"+(float)aFaultSource.getSegAveSlipRate(i)*1000+"\n"; // mm/yr
			finalSlipRateStr+=(i+1)+"\t"+(float)aFaultSource.getFinalAveSegSlipRate(i)*1000+"\n";// mm/yr
			ArbitrarilyDiscretizedFunc func = aFaultSource.getSegmentSlipDist(i).getCumDist();
			func.setInfo("Cumulative Slip distribution for segment "+(i+1));
			cumSlipFuncList.add(func);
		}
		
		// now list cumulative slip distribution for each segment
		//segmentOutput.append("Cumulative Slip distribution for each segment:\n\n");
		segmentOutput.append(initialSlipRateStr);
		segmentOutput.append(finalSlipRateStr);
		segmentOutput.append("\nTotal Moment Rate from segments ="+(float)aFaultSource.getTotalMoRateFromSegs()+"\n");
		segmentOutput.append("Total Moment Rate from ruptures ="+(float)aFaultSource.getTotalMoRateFromRups()+"\n");
		segmentOutput.append("Total Moment Rate from summed dist ="+(float)aFaultSource.getTotalMoRateFromSummedMFD()+"\n");
		segmentOutput.setCaretPosition(0);
		
	}

	
	/**
	 * Create Ruptures output
	 * @param aFaultSource
	 */
	private void createRupturesOutput(WG_02FaultSource aFaultSource) {
		JPanel panel = new JPanel(new GridBagLayout());
		JTextArea rupOutput = new JTextArea();
		rupOutput.setEditable(false);
		panel.add(new JScrollPane(rupOutput), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
		panel.add(this.totalRupMFDButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
		totalRupMFDButton.addActionListener(this);
		panel.add(this.floaterDistButton, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
		floaterDistButton.addActionListener(this);
		this.outputTabbedPane.addTab("Rupture Info", panel);
		// list index, meanMag, total Rate, name for each rupture
		int numRuptures = aFaultSource.getNumRuptures();
		rupOutput.append("Rup Index\tMean Mag\tTotal Rate\t\tRup Name\n\n");
		for(int i=0; i<numRuptures; ++i) {
			rupOutput.append((i+1)+"\t"+(float)aFaultSource.getRupMeanMag(i)+"\t"+
					(float)aFaultSource.getRupRate(i)+"\t\t"+
					aFaultSource.getRupName(i)+"\n");
		}
		rupOutput.setCaretPosition(0);
		if(aFaultSource.getFloaterMagFreqDist()!=null) {
			this.floaterRupMFD = new ArrayList();
			floaterRupMFD.add(aFaultSource.getFloaterMagFreqDist());
		} else floaterRupMFD = null;
		this.totalRupMFD  = new ArrayList();
		totalRupMFD.add(aFaultSource.getTotalRupMFD());
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
		else if(source == this.totalRupMFDButton)
			new WG02_RuptureModelsGraphWindowAPI_Impl(this.totalRupMFD, "Mag", "Rate", "Total Mag Freq Dist for Rups");
		else if(source == this.floaterDistButton) {
			if(floaterRupMFD==null){
				 JOptionPane.showMessageDialog(this, "Floater MFD not available");
				 return;
			}
			new WG02_RuptureModelsGraphWindowAPI_Impl(this.floaterRupMFD, "Mag", "Rate", "Mag Freq Dist for floaters");
		}
	}
}
