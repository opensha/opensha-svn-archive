/**
 * 
 */
package org.opensha.refFaultParamDb.gui.view;

import java.awt.Container;
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

import org.opensha.calc.RelativeLocation;
import org.opensha.data.Location;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.fault.SimpleFaultData;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.surface.FrankelGriddedSurface;
import org.opensha.sha.surface.StirlingGriddedSurface;

import java.util.Iterator;

/**
 * @author vipingupta
 *
 */
public class FaultSectionsDistanceCalcGUI extends JPanel implements ActionListener {
	private final static double GRID_SPACING = 1.0;
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection); 
	private StringParameter faultSection1Param, faultSection2Param, faultModelParam;
	private final static String FAULT_SECTION1_PARAM_NAME = "Fault Section 1";
	private final static String FAULT_SECTION2_PARAM_NAME = "Fault Section 2";
	private final static String STIRLING = "Stirling's";
	private final static String FRANKEL = "Frankel's";
	private final static String FAULT_MODEL_PARAM_NAME = "Fault Model Name";
	private ConstrainedStringParameterEditor faultSection1ParamEditor, faultSection2ParamEditor, faultModelParamEditor;
	private JButton calcButton = new JButton("Calculate Distance");
	private DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	
	public FaultSectionsDistanceCalcGUI() {
		makeFaultSectionNamesParamAndEditor();
		makeFaultModelParamAndEditor();
		calcButton.addActionListener(this);
		createGUI();
	}
	
	
	/**
	 * Add GUI components
	 *
	 */
	private void createGUI() {
		setLayout(new GridBagLayout());
		int pos=0;
		add(faultSection1ParamEditor, new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		add(faultSection2ParamEditor, new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		add(faultModelParamEditor, new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		add(calcButton, new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
	}
	
	
	
	/**
	 * Fault section names param and editor
	 *
	 */
	private void makeFaultSectionNamesParamAndEditor() {
		ArrayList faultSectionsSummaryList = faultSectionDAO.getAllFaultSectionsSummary();
		ArrayList faultSectionsList = new ArrayList();
		for(int i=0; i<faultSectionsSummaryList.size(); ++i)
			faultSectionsList.add(((FaultSectionSummary)faultSectionsSummaryList.get(i)).getAsString());
		
		// fault section 1 param and editor
		faultSection1Param = new StringParameter(FAULT_SECTION1_PARAM_NAME, faultSectionsList, (String)faultSectionsList.get(0));
		faultSection1ParamEditor = new ConstrainedStringParameterEditor(faultSection1Param);
		
		// fault section 2 param and editor
		faultSection2Param = new StringParameter(FAULT_SECTION2_PARAM_NAME, faultSectionsList, (String)faultSectionsList.get(0));
		faultSection2ParamEditor = new ConstrainedStringParameterEditor(faultSection2Param);	
	}
	
	/**
	 * Make fault model name param and editor
	 * It specifies whether Frankel's is chosen or Stirling's is chosen
	 *
	 */
	private void makeFaultModelParamAndEditor() {
		ArrayList faultModels = new ArrayList();
		faultModels.add(FRANKEL);
		faultModels.add(STIRLING);
		faultModelParam = new StringParameter(FAULT_MODEL_PARAM_NAME, faultModels, (String)faultModels.get(0));
		faultModelParamEditor = new ConstrainedStringParameterEditor(faultModelParam);
	}
	
	/**
	 * Calculate minimum distance  on fault trace and full 3D distance
	 *
	 */
	private void calculateDistances() {
		// first fault section
		FaultSectionSummary faultSection1Summary = FaultSectionSummary.getFaultSectionSummary((String)faultSection1Param.getValue());
		FaultSectionPrefData faultSection1PrefData = faultSectionDAO.getFaultSection(faultSection1Summary.getSectionId()).getFaultSectionPrefData();
		FaultTrace faultTrace1 = faultSection1PrefData.getFaultTrace();
		// second fault section
		FaultSectionSummary faultSection2Summary = FaultSectionSummary.getFaultSectionSummary((String)faultSection2Param.getValue());
		FaultSectionPrefData faultSection2PrefData = faultSectionDAO.getFaultSection(faultSection2Summary.getSectionId()).getFaultSectionPrefData();
		FaultTrace faultTrace2 = faultSection2PrefData.getFaultTrace();
		
		// calculate the minimum fault trace distance
		double minFaultTraceDist = Double.POSITIVE_INFINITY;
		double dist;
		for(int i=0; i<faultTrace2.getNumLocations(); ++i) {
			dist = faultTrace1.getMinHorzDistToLine(faultTrace2.getLocationAt(i));
			if(dist<minFaultTraceDist) minFaultTraceDist = dist;
		}
		
		// calculate the minimum 3D distance
		EvenlyGriddedSurfaceAPI surface1 = getEvenlyGriddedSurface(faultSection1PrefData);
		EvenlyGriddedSurfaceAPI surface2 = getEvenlyGriddedSurface(faultSection2PrefData);
		double min3dDist = Double.POSITIVE_INFINITY;
		Iterator it1 = surface1.getLocationsIterator();
		while(it1.hasNext()) {
			Location loc1 = (Location)it1.next();
			Iterator it2 = surface2.getLocationsIterator();
			while(it2.hasNext()) {
				Location loc2 = (Location)it2.next();
				dist = RelativeLocation.getApproxHorzDistance(loc1, loc2);
				if(dist<min3dDist) min3dDist = dist;
			}
		}
		
		JOptionPane.showMessageDialog(this, "Minimum Fault Trace distance="+DECIMAL_FORMAT.format(minFaultTraceDist)+" km\n"+
				"Minimum 3D distance="+DECIMAL_FORMAT.format(min3dDist)+" km");
	}
	
	/**
	 * Get the surface from fault section data
	 * 
	 * @param faultSectionPrefData
	 * @return
	 */
	private EvenlyGriddedSurfaceAPI getEvenlyGriddedSurface(FaultSectionPrefData faultSectionPrefData) {
		SimpleFaultData simpleFaultData = new SimpleFaultData(faultSectionPrefData.getAveDip(),
				faultSectionPrefData.getAveLowerDepth(), faultSectionPrefData.getAveUpperDepth(), 
				faultSectionPrefData.getFaultTrace());
		String selectedFaultModel = (String)this.faultModelParam.getValue();
		// frankel and stirling surface
		if(selectedFaultModel.equalsIgnoreCase(FRANKEL)) {
			return new FrankelGriddedSurface(simpleFaultData, GRID_SPACING);
		} else {
			return new StirlingGriddedSurface(simpleFaultData, GRID_SPACING);
		}
	}

	/**
	 * This function is called when Calculate button is clicked
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if(source == this.calcButton) { // calculate distances
			this.calculateDistances();
		}	
	}
	
	public static void main(String[] args) {
		new FaultSectionsDistanceCalcGUI();
	}
}
