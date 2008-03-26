package scratchJavaDevelopers.kevin;

import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.opensha.data.TimeSpan;
import org.opensha.data.region.EvenlyGriddedGeographicRegion;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.region.RELM_TestingRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel;

public class MetadataSaver implements ParameterChangeWarningListener {

	public MetadataSaver() {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement( "OpenSHA" );


		EqkRupForecast erf = new MeanUCERF2();
//		EqkRupForecast erf = new Frankel02_AdjustableEqkRupForecast();
		erf.getAdjustableParameterList().getParameter(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_INCLUDE);
//		TimeSpan span = new TimeSpan(TimeSpan.YEARS, TimeSpan.YEARS);
//		span.setDuration(30);
//		span.setStartTime(2017);
//		erf.setTimeSpan(span);

		AttenuationRelationship imr = new BJF_1997_AttenRel(this);
		// set the Intensity Measure Type
		imr.setIntensityMeasure(AttenuationRelationship.PGA_NAME);
		// set default parameters
		imr.setParamDefaults();
		
//		GeographicRegion region = new RELM_TestingRegion();
//		EvenlyGriddedGeographicRegion gridded = new EvenlyGriddedGeographicRegion(region.getRegionOutline(), 0.1);
		
		GeographicRegion gridded = null;
		try {
			gridded = new EvenlyGriddedRectangularGeographicRegion(33.5, 34.8, -120.0, -116.0, 0.02);
		} catch (RegionConstraintException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String jobName = "verify_UCERF";
		String rp_host = HazardMapJob.HPC_PRESET.rp_host;
		String rp_batchScheduler = HazardMapJob.HPC_PRESET.rp_batchScheduler;
		String rp_javaPath = HazardMapJob.HPC_PRESET.rp_javaPath;
		String rp_storagePath = HazardMapJob.HPC_PRESET.rp_storagePath + "verify_0.02_noCVM_UCERF";
		String repo_host = "host.com";
		String repo_storagePath = "/path/to/repo";
		int sitesPerJob = 100;
		boolean useCVM = false;
		boolean saveERF = true;
		String metadataFileName = jobName + ".xml";
		HazardMapJob job = new HazardMapJob(jobName, rp_host, rp_batchScheduler, rp_javaPath, rp_storagePath, repo_host, repo_storagePath, sitesPerJob, useCVM, saveERF, metadataFileName);

		root = erf.toXMLMetadata(root);
		root = imr.toXMLMetadata(root);
		root = gridded.toXMLMetadata(root);
		root = job.toXMLMetadata(root);
		root = this.writeCalculationParams(root);
		

		XMLWriter writer;


		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			writer = new XMLWriter(System.out, format);
			writer.write(document);
			writer.close();

			writer = new XMLWriter(new FileWriter("output.xml"), format);
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public Element writeCalculationParams(Element root) {
		Element xml = root.addElement("calculationParameters");
		xml.addAttribute("maxSourceDistance", "200");
		xml.addAttribute("intesityMeasureType", AttenuationRelationship.PGA_NAME);
		
		return root;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MetadataSaver();
	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {
		// TODO Auto-generated method stub

	}

}
