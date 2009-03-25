package org.opensha.sha.calc.hazardMap.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.SitesInGriddedRegionAPI;
import org.opensha.data.siteType.OrderedSiteDataProviderList;
import org.opensha.data.siteType.gui.beans.OrderedSiteDataGUIBean;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.gridComputing.GridResources;
import org.opensha.gridComputing.GridResourcesList;
import org.opensha.gridComputing.ResourceProvider;
import org.opensha.gridComputing.StorageHost;
import org.opensha.gridComputing.SubmitHost;
import org.opensha.gui.UserAuthDialog;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardMapCalculationParameters;
import org.opensha.sha.calc.hazardMap.HazardMapJob;
import org.opensha.sha.calc.hazardMap.NamedGeographicRegion;
import org.opensha.sha.calc.hazardMap.servlet.ManagementServletAccessor;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.gui.beans.GridParametersGuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.SitesInGriddedRegionGuiBean;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.IntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeEvent;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeListener;
import org.opensha.util.XMLUtils;
import org.opensha.util.http.HTTPAuthenticator;
import org.opensha.util.http.InstallSSLCert;
import org.opensha.util.http.StaticPasswordAuthenticator;

public class CreateDataManager extends StepManager implements AttenuationRelationshipChangeListener {
	
	boolean useSSL = false;
	
	private String username = "";
	private char[] password = null;
	
	private Step hazardStep;
	private Step regionStep;
	private Step gridStep;
	private Step siteDataStep;
	private Step submitStep;
	
	private SitesInGriddedRegionGuiBean sitesGuiBean;
	private GridParametersGuiBean gridGuiBean;
	private OrderedSiteDataGUIBean siteDataGuiBean;
	
	GridResourcesList resources;
	
	ManagementServletAccessor manager;
	
	HazardStep hazard;
	SubmitPanel submit;
	
	private IMT_Info imtInfo = new IMT_Info();
	
	HazardMapApplet applet;
	
	ArrayList<NamedGeographicRegion> regions;
	
	public CreateDataManager(HazardMapApplet parent) {
		super(parent, null, parent.getConsole());
		this.applet = parent;
		
		// SSL's not working from within an applet, at least for now. Just use it if it's an application.
//		if (!parent.isApplet())
//			useSSL = true;
		
		manager = new ManagementServletAccessor(ManagementServletAccessor.SERVLET_URL, false);
		resources = getResourcesList();
		regions = getRegionsList();
		
		this.createSteps();
		
		this.init();
	}
	
	private ArrayList<NamedGeographicRegion> getRegionsList() {
		try {
			return manager.getGeographicRegiongs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parent.loadStep();
		return null;
	}
	
	private GridResourcesList getResourcesList() {
		HTTPAuthenticator auth = new HTTPAuthenticator();
		Authenticator.setDefault(auth);
		
		while (true) {
			try {
				GridResourcesList list = manager.getGridResourcesList();
				
				return list;
			} catch (java.net.ProtocolException e) {
				if (auth.getDialog().isCanceled()) {
					this.parent.loadStep();
					return null;
				}
				
				System.out.println("Your password is incorrect!");
				continue;
			} catch (Exception e) {
				if (auth.getDialog().isCanceled()) {
					this.parent.loadStep();
					return null;
				}
				e.printStackTrace();
				if (retry("ERROR: " + e.getMessage()))
					continue;
				else {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public boolean retry(String message) {
		message += "\nRetry?";
		int response = JOptionPane.showConfirmDialog(this.panel, message, "Error...", JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION)
			return true;
		return false;
	}
	
	private void createSteps() {
		hazardStep = this.createHazardStep();
		regionStep = this.createRegionStep();
		gridStep = this.createGridStep();
		siteDataStep = this.createSiteDataStep();
		submitStep = this.createSubmitStep();
		
		steps.add(hazardStep);
		steps.add(regionStep);
		steps.add(siteDataStep);
		steps.add(gridStep);
		steps.add(submitStep);
	}
	
	private Step createHazardStep() {
		hazard = new HazardStep(sitesGuiBean);
		
		return new Step(hazard, "Hazard Calculation Settings");
	}
	
	private Step createRegionStep() {
		try {
			sitesGuiBean = this.hazard.createSitesGUIBean(regions);
		} catch (RegionConstraintException e) {
			throw new RuntimeException(e);
		}
//		JPanel regionPanel = new JPanel();
//		regionPanel.setLayout(new BorderLayout());
//		regionPanel.add(new JLabel("Region Settings"), BorderLayout.CENTER);
		
		return new Step(sitesGuiBean, "Region Settings");
	}
	
	private Step createGridStep() {
		gridGuiBean = new GridParametersGuiBean(resources);
		
		gridGuiBean.setSubmitHostsVisible(false);
		
		return new Step(gridGuiBean, "Grid Computing Settings");
	}
	
	private Step createSiteDataStep() {
		siteDataGuiBean = new OrderedSiteDataGUIBean(OrderedSiteDataProviderList.createSiteDataProviderDefaults(),
							hazard.getIMR());
		
		IMR_GuiBean imrGuiBean = hazard.getIMRGuiBean();
		imrGuiBean.addAttenuationRelationshipChangeListener(this);
		
		return new Step(siteDataGuiBean, "Site Data Providers");
	}
	
	private Step createSubmitStep() {
		submit = new SubmitPanel(this);
		
		return new Step(submit, "Submit");
	}
	
	public static String intensityMD5 = "2a 74 46 db b4 47 64 db f2 26 9c 95 67 2a cb 57";
	
	private boolean authenticate() throws IOException {
		File keystore = null;
		URL url = null;
		if (useSSL) {
			InstallSSLCert installCert = new InstallSSLCert(intensityMD5, "intensity.usc.edu");
			keystore = installCert.getKeyStore();
			
			if (keystore == null)
				return false;
			
			System.out.println("Loading keystore from: " + keystore.getAbsolutePath());
			System.setProperty("javax.net.ssl.trustStore", keystore.getAbsolutePath());
			
			url = new URL("https://intensity.usc.edu/trac/opensha/wiki/");
		} else {
			url = new URL("http://intensity.usc.edu/trac/opensha/wiki/");
		}
		HTTPAuthenticator auth = new HTTPAuthenticator();
		UserAuthDialog dialog = auth.getDialog();
		Authenticator.setDefault(auth);
//		URL url = new URL("https://intensity.usc.edu/");
		boolean success = false;
		while (true) {
			try {
				InputStream ins = url.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
				String str;
				System.out.println("Authentication successful!");
				success = true;
				this.username = dialog.getUsername();
				this.password = dialog.getPassword();
				// lets use this good password from now on!
				Authenticator.setDefault(new StaticPasswordAuthenticator(this.username, this.password));
				break;
			} catch (java.net.ProtocolException e) {
				if (auth.getDialog().isCanceled()) {
					success = false;
					break;
				}
				System.out.println("Your password is incorrect!");
				continue;
			}
		}
		if (keystore != null)
			keystore.delete();
		return success;
	}

	public static void main(String args[]) {
		CreateDataManager creator = new CreateDataManager(null);
		
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(creator.getPanel(), BorderLayout.CENTER);
		frame.setContentPane(panel);
		frame.setSize(new Dimension(700, 550));
		frame.setVisible(true);
	}
	
	public Document getSubmitDoc(String name, String email) throws InvocationTargetException, RuntimeException, RegionConstraintException {
		String id = System.currentTimeMillis() + "";
		
		Document document = XMLUtils.createDocumentWithRoot();
		Element root = document.getRootElement();
		
		// ***** ERF
		System.out.println("Saving ERF");
		EqkRupForecast erf = hazard.getERF();
		
		root = erf.toXMLMetadata(root);
		
		// ***** IMR/IMT
		System.out.println("Saving IMR/IMT");
		IntensityMeasureRelationshipAPI imr = hazard.getIMR();
		
		IMT_GuiBean imtGuiBean = hazard.getIMTGuiBean();
		
		String imt = (String)(imtGuiBean.getIntensityMeasure().getName());
		if (imt == null)
			System.out.println("NULL IMT!!!");
		imr.setIntensityMeasure(imt);
		ParameterAPI dampingParam = imtGuiBean.getParameterList().getParameter(AttenuationRelationship.DAMPING_NAME);
		if (dampingParam != null) {
			double damping = (Double)dampingParam.getValue();
			imr.getParameter(AttenuationRelationship.DAMPING_NAME).setValue(damping);
		}
		ParameterAPI periodParam = imtGuiBean.getParameterList().getParameter(AttenuationRelationship.PERIOD_NAME);
		if (periodParam != null) {
			double period = (Double)periodParam.getValue();
			imr.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(period);
		}
		ArrayList<ParameterAPI> siteParams = sitesGuiBean.getSiteParams();
		Iterator<ParameterAPI> imrParams = imr.getSiteParamsIterator();
		while (imrParams.hasNext()) {
			ParameterAPI imrParam = imrParams.next();
			for (ParameterAPI param : siteParams) {
				String siteParamName = param.getName();
				if (siteParamName.endsWith(imrParam.getName())) {
					System.out.println("Setting IMR param: " + imrParam.getName() + " to: " + param.getValue());
					imrParam.setValue(param.getValue());
				}
			}
		}
		
		root = imr.toXMLMetadata(root);
		
		// ***** Region
		System.out.println("Saving Site/Region info");
		SitesInGriddedRegionAPI griddedRegionSites = sitesGuiBean.getGriddedRegionSite();

		root = griddedRegionSites.toXMLMetadata(root);
		
		// ***** Site Data
		if (sitesGuiBean.isUseSiteData()) {
			System.out.println("Saving Site Data info");
			OrderedSiteDataProviderList dataList = (OrderedSiteDataProviderList)siteDataGuiBean.getProviderList().clone();
			// there's no reason to store info about or ever instantiate the disabled providers
			dataList.removeDisabledProviders();
			
			root = dataList.toXMLMetadata(root);
		}
		
		// ***** Function
		System.out.println("Saving Hazard Function");
//		if (!useCustomX_Values) {
		// TODO: add custom x vals
		ArbitrarilyDiscretizedFunc function = imtInfo.getDefaultHazardCurve(imtGuiBean.getSelectedIMT());
//		}
		
		root = function.toXMLMetadata(root);
		
		// ***** Grid Params
		System.out.println("Saving Grid Params");
		int sitesPerJob = this.gridGuiBean.get_sitesPerJob();
		int maxWallTime = this.gridGuiBean.get_maxWallTime();
		double maxSourceDistance;
		boolean useCVM = sitesGuiBean.isUseSiteData();
		boolean saveERF = this.gridGuiBean.get_saveERF();
		
//		if(distanceControlPanel == null )
		// TODO: add custom max source distance
		maxSourceDistance = new Double(HazardCurveCalculator.MAX_DISTANCE_DEFAULT);
//		else maxSourceDistance = new Double(distanceControlPanel.getDistance());
		
		String metadataFileName = id + ".xml";
		
		ResourceProvider rp = this.gridGuiBean.get_resourceProvider();
		SubmitHost submit = this.gridGuiBean.get_submitHost();
		StorageHost storage = this.resources.getStorageHosts().get(0);
		
		GridResources resources = new GridResources(submit, rp, storage);
		HazardMapCalculationParameters calcParams = new HazardMapCalculationParameters(maxWallTime, sitesPerJob, maxSourceDistance, useCVM, saveERF);
		
		HazardMapJob job = new HazardMapJob(resources, calcParams, id, name, email, metadataFileName);

		root = job.toXMLMetadata(root);
		
		return document;
	}
	
	protected void submit(String name, String email) {
		try {
			Document document = this.getSubmitDoc(name, email);
			
			System.out.println("Submitting Job!");
			ManagementServletAccessor manage = new ManagementServletAccessor(ManagementServletAccessor.SERVLET_URL, false);
			
			manage.submit(document);
			
			// show status area
			this.applet.loadStatusOption();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegionConstraintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void attenuationRelationshipChange(
			AttenuationRelationshipChangeEvent event) {
		this.siteDataGuiBean.setAttenuationRelationship(event.getNewAttenRel());
	}
}
