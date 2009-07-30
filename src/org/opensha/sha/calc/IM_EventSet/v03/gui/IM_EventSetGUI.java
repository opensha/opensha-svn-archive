package org.opensha.sha.calc.IM_EventSet.v03.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetCalculation;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetOutputWriter;
import org.opensha.sha.calc.IM_EventSet.v03.outputImpl.HAZ01Writer;
import org.opensha.sha.calc.IM_EventSet.v03.outputImpl.OriginalModWriter;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.gui.HazardCurveLocalModeApplication;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

public class IM_EventSetGUI extends JFrame implements ActionListener {
	
	private static File cwd = new File(System.getProperty("user.dir"));
	
	private SitesPanel sitesPanel = null;
	private ERF_GuiBean erfGuiBean = null;
	private IMR_ChooserPanel imrChooser = null;
	private IMT_ChooserPanel imtChooser = null;
	
	private JTabbedPane tabbedPane;
	
	private JPanel imPanel = new JPanel();
	private JPanel siteERFPanel = new JPanel();
	
	private JButton calcButton = new JButton("Start Calculation");
	private JButton saveButton = new JButton("Save Calculation Settings");
	
	private JFileChooser saveChooser;
	private JFileChooser outputChooser;
	
	private JComboBox outputWriterChooser;
	
//	private JProgressBar bar = new JProgressBar();
	
	public IM_EventSetGUI() {
		sitesPanel = new SitesPanel();
		erfGuiBean = createERF_GUI_Bean();
		imtChooser = new IMT_ChooserPanel();
		imrChooser = new IMR_ChooserPanel(imtChooser);
		
		imPanel.setLayout(new BoxLayout(imPanel, BoxLayout.X_AXIS));
		imPanel.add(imrChooser);
		imPanel.add(imtChooser);
		
		siteERFPanel.setLayout(new BoxLayout(siteERFPanel, BoxLayout.X_AXIS));
		siteERFPanel.add(sitesPanel);
		siteERFPanel.add(erfGuiBean);
		
		tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab("Sites/ERF", siteERFPanel);
		tabbedPane.addTab("IMRs/IMTs", imPanel);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		String writers[] = new String[2];
		writers[0] = OriginalModWriter.NAME;
		writers[1] = HAZ01Writer.NAME;
		outputWriterChooser = new JComboBox(writers);
		JPanel outputWriterChooserPanel = new JPanel();
		outputWriterChooserPanel.add(outputWriterChooser);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(outputWriterChooserPanel);
		bottomPanel.add(calcButton);
		bottomPanel.add(saveButton);
//		bottomPanel.add(bar);
		calcButton.addActionListener(this);
		saveButton.addActionListener(this);
		
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
	}
	
	private ERF_GuiBean createERF_GUI_Bean() {
		try {
			return new ERF_GuiBean(HazardCurveLocalModeApplication.getLocalERFClasses());
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public IM_EventSetCalculation getEventSetCalc() {
		EqkRupForecastAPI erf = null;
		try {
			erf = (EqkRupForecastAPI) this.erfGuiBean.getSelectedERF_Instance();
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		ArrayList<EqkRupForecastAPI> erfs = new ArrayList<EqkRupForecastAPI>();
		erfs.add(erf);
		
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = imrChooser.getSelectedIMRs();
		
		ArrayList<String> imts = imtChooser.getIMTStrings();
		ArrayList<Location> locs = sitesPanel.getLocs();
		ArrayList<ArrayList<SiteDataValue<?>>> vals = sitesPanel.getDataLists();
		
		return new IM_EventSetCalculation(locs, vals, erfs, imrs, imts);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(calcButton)) {
			// make sure we're ready to calculate first
			ArrayList<Location> locs = null;
			ArrayList<ArrayList<SiteDataValue<?>>> dataLists = null;
			EqkRupForecastAPI erf = null;
			ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = null;
			ArrayList<String> imts = null;
			try {
				locs = sitesPanel.getLocs();
				dataLists = sitesPanel.getDataLists();
				erf = (EqkRupForecastAPI)erfGuiBean.getSelectedERF();
				imrs = imrChooser.getSelectedIMRs();
				imts = imtChooser.getIMTStrings();
				
				if (locs.size() < 1) {
					JOptionPane.showMessageDialog(this, "You must add at least 1 site!", "No Sites Selected!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (locs.size() != dataLists.size()) {
					JOptionPane.showMessageDialog(this, "Internal error: Site data lists not same size as site list!",
							"Internal error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (erf == null) {
					JOptionPane.showMessageDialog(this, "Error instantiating ERF!", "Error with ERF!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (imrs.size() < 1) {
					JOptionPane.showMessageDialog(this, "You must add at least 1 IMR!", "No IMRs Selected!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (imts.size() < 1) {
					JOptionPane.showMessageDialog(this, "You must add at least 1 IMT!", "No IMTs Selected!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				JOptionPane.showMessageDialog(this, e2.getMessage(), "Exception Preparing Calculation",
						JOptionPane.ERROR_MESSAGE);
			}
			
			if (outputChooser == null) {
				outputChooser = new JFileChooser(cwd);
				outputChooser.setDialogTitle("Select Output Directory");
				outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			int returnVal = outputChooser.showOpenDialog(this);;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File outputDir = outputChooser.getSelectedFile();
				GUICalcAPI_Impl calc = new GUICalcAPI_Impl(locs, dataLists,
						outputDir, null);
				IM_EventSetOutputWriter writer;
				String writerName = (String) outputWriterChooser.getSelectedItem();
				if (writerName.equals(OriginalModWriter.NAME))
					writer = new OriginalModWriter(calc);
				else if (writerName.equals(HAZ01Writer.NAME))
					writer = new HAZ01Writer(calc);
				else
					throw new RuntimeException("Unknown writer: " + writerName);
				try {
//					bar.setIndeterminate(true);
//					bar.setString("Calculating...");
//					bar.setStringPainted(true);
					this.calcButton.setEnabled(false);
					this.validate();
					writer.writeFiles(erf, imrs, imts);
				} catch (Exception e1) {
//					bar.setIndeterminate(false);
//					bar.setStringPainted(false);
					this.calcButton.setEnabled(true);
					throw new RuntimeException(e1);
				}
				this.calcButton.setEnabled(true);
//				bar.setIndeterminate(false);
//				bar.setStringPainted(false);
			}
		} else if (e.getSource().equals(saveButton)) {
			if (saveChooser == null)
				saveChooser = new JFileChooser(cwd);
			int returnVal = saveChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				IM_EventSetCalculation calc = getEventSetCalc();
				File file = saveChooser.getSelectedFile();
				Document doc = XMLUtils.createDocumentWithRoot();
				Element root = doc.getRootElement();
				calc.toXMLMetadata(root);
				try {
					XMLUtils.writeDocumentToFile(file.getAbsolutePath(), doc);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IM_EventSetGUI gui = new IM_EventSetGUI();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(900, 700);
		
		gui.setVisible(true);
	}

}
