package org.opensha.sha.gui.beans;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;


import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.data.Site;
import org.opensha.data.Location;
import org.opensha.data.region.*;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.util.SiteTranslator;

import org.opensha.exceptions.RegionConstraintException;

import scratchJavaDevelopers.kevin.GridJobPreset;
import scratchJavaDevelopers.kevin.HazardMapJob;

/**
 * <p>Title:SitesInGriddedRectangularRegionGuiBean </p>
 * <p>Description: This creates the Gridded Region parameter Editor with Site Params
 * for the selected Attenuation Relationship in the Application.
 * </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date March 11, 2003
 * @version 1.0
 */



public class GridParametersGuiBean extends ParameterListEditor implements
ParameterChangeFailListener, ParameterChangeListener, Serializable {

	// for debug purposes
	protected final static String C = "GridParametersGuiBean";


	// title for site paramter panel
	public final static String GRIDDED_SITE_PARAMS = "Set Gridded Region Params";

	//Site Params ArrayList
	ArrayList siteParams ;

	//Static String for setting the site Params
	public final static String SET_ALL_SITES = "Apply same site parameter(s) to all locations";
	public final static String SET_SITE_USING_WILLS_SITE_TYPE = "Use the CGS Preliminary Site Conditions Map of CA (web service)";
	public final static String SET_SITES_USING_SCEC_CVM = "Use both CGS Map and SCEC Basin Depth (web services)";

	ArrayList<GridJobPreset> presetsList = new ArrayList<GridJobPreset>();
	
	//StringParameter to set site related params
	private StringParameter presets;
	private StringParameter rp_host = new StringParameter("Resource Provider Host-Name");
	private StringParameter rp_batchScheduler = new StringParameter("Resource Provider Batch Scheduler");
	private StringParameter rp_javaPath = new StringParameter("Resource Provider Java Path");
	private StringParameter rp_storagePath = new StringParameter("Resource Provider Storage Path");
	private StringParameter rp_globusrsl = new StringParameter("Globus RSL Params");
	private StringParameter repo_host = new StringParameter("Data Storage Host-Name");
	private StringParameter repo_storagePath = new StringParameter("Data Storage Path");
	private StringParameter submitHost = new StringParameter("Submit Host-Name");
	private StringParameter submitHostPath = new StringParameter("Submit Host Path");
	private StringParameter submitHostPathToDependencies = new StringParameter("Submit Host Dependencies Path");
	private IntegerParameter sitesPerJob = new IntegerParameter("Site Per Job", 0, Integer.MAX_VALUE);
	private BooleanParameter saveERF = new BooleanParameter("Save ERF to File?", true);

	//SiteTranslator
	SiteTranslator siteTrans = new SiteTranslator();

	//instance of class EvenlyGriddedRectangularGeographicRegion
	private SitesInGriddedRectangularRegion gridRectRegion;

	/**
	 * constuctor which builds up mapping between IMRs and their related sites
	 */
	public GridParametersGuiBean() {
		
		
		presetsList.add(HazardMapJob.HPC_PRESET);
		presetsList.add(HazardMapJob.DYNAMIC_PRESET);
		
		ArrayList<String> presetsStr = new ArrayList<String>();
		for (GridJobPreset preset : presetsList) {
			presetsStr.add(preset.name);
		}
		
		GridJobPreset firstPreset = presetsList.get(0);
		
		this.rp_host.setValue(firstPreset.rp_host);
		this.rp_batchScheduler.setValue(firstPreset.rp_batchScheduler);
		this.rp_javaPath.setValue(firstPreset.rp_javaPath);
		this.rp_storagePath.setValue(firstPreset.rp_storagePath);
		this.rp_globusrsl.setValue(firstPreset.rp_globusrsl);
		
		this.repo_host.setValue(HazardMapJob.DEFAULT_REPO_HOST);
		this.repo_storagePath.setValue(HazardMapJob.DEFAULT_REPO_STORAGE_PATH);
		this.sitesPerJob.setValue(100);
		
		submitHost.setValue(HazardMapJob.DEFAULT_SUBMIT_HOST);
		submitHostPath.setValue(HazardMapJob.DEFAULT_SUBMIT_HOST_PATH);
		submitHostPathToDependencies.setValue(HazardMapJob.DEFAULT_DEPENDENCY_PATH);
		
		presets = new StringParameter("Resource Provider Presets", presetsStr);
		presets.addParameterChangeListener(this);

		// add the longitude and latitude paramters
		parameterList = new ParameterList();
		parameterList.addParameter(presets);
		parameterList.addParameter(rp_host);
		parameterList.addParameter(rp_batchScheduler);
		parameterList.addParameter(rp_javaPath);
		parameterList.addParameter(rp_storagePath);
		parameterList.addParameter(rp_globusrsl);
		parameterList.addParameter(repo_host);
		parameterList.addParameter(repo_storagePath);
		parameterList.addParameter(submitHost);
		parameterList.addParameter(submitHostPath);
		parameterList.addParameter(submitHostPathToDependencies);
		parameterList.addParameter(sitesPerJob);
		parameterList.addParameter(saveERF);
		editorPanel.removeAll();
		addParameters();

		try {
			jbInit();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shown when a Constraint error is thrown on a ParameterEditor
	 *
	 * @param  e  Description of the Parameter
	 */
	public void parameterChangeFailed( ParameterChangeFailEvent e ) {


		String S = C + " : parameterChangeFailed(): ";



		StringBuffer b = new StringBuffer();

		ParameterAPI param = ( ParameterAPI ) e.getSource();


		ParameterConstraintAPI constraint = param.getConstraint();
		String oldValueStr = e.getOldValue().toString();
		String badValueStr = e.getBadValue().toString();
		String name = param.getName();

		b.append( "The value ");
		b.append( badValueStr );
		b.append( " is not permitted for '");
		b.append( name );
		b.append( "'.\n" );
		b.append( "Resetting to ");
		b.append( oldValueStr );
		b.append( ". The constraints are: \n");
		b.append( constraint.toString() );

		JOptionPane.showMessageDialog(
				this, b.toString(),
				"Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
		);
	}

	/**
	 * This function is called when value a parameter is changed
	 * @param e Description of the parameter
	 */
	public void parameterChange(ParameterChangeEvent e){
		ParameterAPI param = ( ParameterAPI ) e.getSource();

		if(param == presets) {
			for (GridJobPreset preset : presetsList) {
				String name = (String)presets.getValue();
				if (name.equals(preset.name)) {
					this.rp_host.setValue(preset.rp_host);
					this.rp_batchScheduler.setValue(preset.rp_batchScheduler);
					this.rp_javaPath.setValue(preset.rp_javaPath);
					this.rp_storagePath.setValue(preset.rp_storagePath);
					this.rp_globusrsl.setValue(preset.rp_globusrsl);
					editorPanel.removeAll();
				    addParameters();
					editorPanel.validate();
					editorPanel.repaint();
					break;
				}
			}
		}
	}
	
	public String get_rp_host() {
		return (String)this.rp_host.getValue();
	}
	
	public String get_rp_batchScheduler() {
		return (String)this.rp_batchScheduler.getValue();
	}
	
	public String get_rp_javaPath() {
		return (String)this.rp_javaPath.getValue();
	}
	
	public String get_rp_storagePath() {
		return (String)this.rp_storagePath.getValue();
	}
	
	public String get_rp_globusrsl() {
		return (String)this.rp_globusrsl.getValue();
	}
	
	public String get_repo_host() {
		return (String)this.repo_host.getValue();
	}
	
	public String get_repo_storagePath() {
		return (String)this.repo_storagePath.getValue();
	}
	
	public int get_sitesPerJob() {
		return (Integer)this.sitesPerJob.getValue();
	}
	
	public boolean get_saveERF() {
		return (Boolean)this.saveERF.getValue();
	}
	
	public String get_submitHost() {
		return (String)this.submitHost.getValue();
	}
	
	public String get_submitHostPath() {
		return (String)this.submitHostPath.getValue();
	}
	
	public String get_submitHostPathToDependencies() {
		return (String)this.submitHostPathToDependencies.getValue();
	}
}
