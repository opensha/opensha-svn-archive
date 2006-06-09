/**
 * 
 */
package org.opensha.sha.gui.infoTools;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults.A_FaultSource;

/**
 *This class generaates a window to show the ouput of the Rupture Model App
 *
 * @author vipingupta
 *
 */
public class RuptureModelOuput extends JFrame {
	private JTabbedPane outputTabbedPane = new JTabbedPane();
	private final static int W = 800;
	private final static int H = 700;
	
	/**
	 * Accepts A_FaultSource object and creates output to show segment data, rupture data,
	 * floating rupture data
	 * 
	 * @param aFaultSource
	 * @param inputParameters
	 */
	public RuptureModelOuput(A_FaultSource aFaultSource, String inputParameters) {
		this.getContentPane().setLayout(new GridBagLayout());
		// show data related to each segment
		createSegmentOutput(aFaultSource);
		// show data related to ruptures
		createRupturesOutput(aFaultSource);
		// show data related to Floater ruptures
		createFloaterOutput(aFaultSource);
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
	private void createSegmentOutput(A_FaultSource aFaultSource) {
		JTextArea segmentOutput = new JTextArea();
		segmentOutput.setEnabled(false);
		this.outputTabbedPane.addTab("Segment Info", new JScrollPane(segmentOutput));
		// list index, name, area, rate and recurrence interval(1/rate) for each segment
		int numSegments = aFaultSource.getNumSegments();
		segmentOutput.append("Index\tSegment Area\tSegment Rate\tRecurrence Interval\tSegment Name\n\n");
		for(int i=0; i<numSegments; ++i) {
			segmentOutput.append(i+"\t"+
					(float)aFaultSource.getSegmentArea(i)+"\t"+
					+(float)aFaultSource.getSegmentRate(i)+"\t"+
					(float)(aFaultSource.getSegmentRecurrenceInterval(i))+"\t"+
					aFaultSource.getSegmentName(i)+"\n");
		}
		
		// now list cumulative slip distribution for each segment
		segmentOutput.append("Cumulative Slip distribution for each segment:\n\n");
		for(int i=0; i<numSegments; ++i) {
			segmentOutput.append("Segment "+i+":\n"+aFaultSource.getSegmentSlipDist(i).getCumDist().toString()+"\n");
		}
		segmentOutput.setCaretPosition(0);
		
	}

	
	/**
	 * Create Ruptures output
	 * @param aFaultSource
	 */
	private void createRupturesOutput(A_FaultSource aFaultSource) {
		JTextArea rupOutput = new JTextArea();
		rupOutput.setEnabled(false);
		this.outputTabbedPane.addTab("Rupture Info", new JScrollPane(rupOutput));
		// list index, meanMag, total Rate, name for each rupture
		int numRuptures = aFaultSource.getNumRuptures();
		rupOutput.append("Rup Index\tMean Mag\tTotal Rate\tRup Name\n\n");
		for(int i=0; i<numRuptures; ++i) {
			rupOutput.append(i+"\t"+(float)aFaultSource.getRupMeanMag(i)+"\t"+
					(float)aFaultSource.getRupMagFreqDist(i).getTotalIncrRate()+"\t"+
					aFaultSource.getRupName(i)+"\n");
		}
		rupOutput.setCaretPosition(0);
	}
	
	/**
	 * Output for floater ruptures
	 * 
	 * @param aFaultSource
	 */
	private void createFloaterOutput(A_FaultSource aFaultSource) {
		JTextArea floaterOutput = new JTextArea();
		floaterOutput.setEnabled(false);
		this.outputTabbedPane.addTab("Floater Ruptures", new JScrollPane(floaterOutput));
		floaterOutput.append(aFaultSource.getFloaterMagFreqDist().toString());
		floaterOutput.setCaretPosition(0);
	}
	
	/**
	 * Show input parameters as metadata
	 * 
	 * @param metadata
	 */
	private void showInputParameters(String metadata) {
		JTextArea metadataTextArea = new JTextArea();
		metadataTextArea.setEnabled(false);
		this.outputTabbedPane.addTab("Metadata", new JScrollPane(metadataTextArea));
		metadataTextArea.append(metadata);
		metadataTextArea.setCaretPosition(0);
	}
}
