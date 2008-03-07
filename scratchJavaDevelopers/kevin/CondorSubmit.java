package scratchJavaDevelopers.kevin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.calc.SubmitJobForGridComputation;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.util.FileUtils;

public class CondorSubmit implements ParameterChangeWarningListener {

	public final static boolean D =false;

	// parent directory where each new calculation will have its own subdirectory
	public static final String PARENT_DIR = "/opt/install/apache-tomcat-5.5.20/webapps/OpenSHA/HazardMapDatasets/";
	// filenames for IMR, ERF, Region, metadata
	private static final String IMR_FILE_NAME = "imr.obj";
	private static final String ERF_FILE_NAME = "erf.obj";
	private static final String REGION_FILE_NAME = "region.obj";
	private static final String X_VALUES_FILE_NAME = "xValues.obj";
	public  static final String METADATA_FILE_NAME = "metadata.txt";
	public  static final String SITES_FILE_NAME = "sites.txt";

	public CondorSubmit() {

		//get the sites for which this needs to be calculated
		SitesInGriddedRectangularRegion sites = null;
		try {
			sites = new SitesInGriddedRectangularRegion(33.0, 34.0, -119.0, -118.0, .5);
		} catch (RegionConstraintException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//get the selected IMR
		AttenuationRelationshipAPI imr = new BA_2008_AttenRel(this);
		imr.setIntensityMeasure(BA_2008_AttenRel.PGV_NAME);
		imr.setParamDefaults();
		
		//get the selected EqkRupForecast

		EqkRupForecastAPI erf = new Frankel96_AdjustableEqkRupForecast();
		erf.updateForecast();

		IMT_Info imtInfo = new IMT_Info();
		ArbitrarilyDiscretizedFunc function = imtInfo.getDefaultHazardCurve(AttenuationRelationship.PGA_NAME);
		
		ArrayList xValuesList = new ArrayList();
	     for(int i = 0; i<function.getNum(); ++i) xValuesList.add(new String(""+function.getX(i)));
		// get the MAX_SOURCE distance
		double maxDistance =  200.0;

		//get the email address from the applet
		String emailAddr = "kmilner@usc.edu";
		//get the parameter values in String form needed to reproduce this
		//String mapParametersInfo = (String) inputFromApplet.readObject();

		//getting the dataset id, in which we want to put all his hazardmap dataset results
		String datasetId = "condor_test_1";
		//new directory for Hazard map dataset
		String datasetDir = "";
		//checking if datasetId gievn by is null
		if(datasetId!=null && !datasetId.trim().equals(""))
			datasetDir = datasetId.trim();

		else{
			datasetDir = System.currentTimeMillis()+"";
		}
		//creating the dataset directory.
		File hazardMapDataset = new File(PARENT_DIR+datasetDir);
		hazardMapDataset.mkdir();

		System.out.println("Generated Dataset with name :"+ datasetDir);

		String newDir = PARENT_DIR+datasetDir+"/";
		// write X values to a file
		try {
			FileWriter fwX_Values = new FileWriter(newDir+this.X_VALUES_FILE_NAME);
			for (int i=0; i<xValuesList.size(); ++i)
				fwX_Values.write(xValuesList.get(i)+"\n");
			fwX_Values.close();
			submit(sites, imr, erf, maxDistance, newDir, datasetDir, emailAddr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// write the metadata to the metadata file
//		FileWriter fwMetadata = new FileWriter(newDir+this.METADATA_FILE_NAME);
//		fwMetadata.write(mapParametersInfo);
//		fwMetadata.close();

		
	}

	public void submit(SitesInGriddedRectangularRegion sites, AttenuationRelationshipAPI imr, EqkRupForecastAPI erf,
			double maxDistance, String newDir, String datasetDir, String emailAddr) {
		try {
			// write site information in sites file
			FileWriter fwSites = new FileWriter(newDir+this.SITES_FILE_NAME);
			fwSites.write(sites.getMinLat()+" "+sites.getMaxLat()+" "+sites.getGridSpacing()+"\n");
			fwSites.write(sites.getMinLon()+" "+sites.getMaxLon()+" "+sites.getGridSpacing()+"\n");
			fwSites.close();


			FileUtils.saveObjectInFile(newDir+this.REGION_FILE_NAME, sites);
			FileUtils.saveObjectInFile(newDir+this.IMR_FILE_NAME, imr);
			FileUtils.saveObjectInFile(newDir+this.ERF_FILE_NAME, erf);

//		if(D) System.out.println("ERF URL="+eqkRupForecastLocation);
//		//EqkRupForecast eqkRupForecast = (EqkRupForecast)FileUtils.loadObjectFromURL(eqkRupForecastLocation);
//		//FileUtils.saveObjectInFile(newDir+this.ERF_FILE_NAME, eqkRupForecast);
//		String getERF_FileName = "getERF.sh";
//		FileWriter fw = new FileWriter(newDir+getERF_FileName);
//		fw.write("#!/bin/csh\n");
//		fw.write("cd "+newDir+"\n");
//		fw.write("cp  "+eqkRupForecastLocation+" "+newDir+this.ERF_FILE_NAME+"\n");
//		fw.close();
//		org.opensha.util.RunScript.runScript(new String[]{"sh", "-c", "sh "+newDir+getERF_FileName});
//		org.opensha.util.RunScript.runScript(new String[]{"sh", "-c", "rm "+newDir+getERF_FileName});
			if(D) System.out.println("after wget");

			// now run the calculation on grid
			SubmitJobForGridComputation computation = null;
//			computation =  new SubmitJobForGridComputation(IMR_FILE_NAME, ERF_FILE_NAME,
//					REGION_FILE_NAME, X_VALUES_FILE_NAME,maxDistance, newDir, datasetDir, sites,
//					emailAddr, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {
		// TODO Auto-generated method stub

	}
}
