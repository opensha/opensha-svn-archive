package org.opensha.sha.calc.hazardMap;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.opensha.commons.gridComputing.GridJob;
import org.opensha.commons.gridComputing.GridResources;

public class HazardMapJob extends GridJob {

	public HazardMapJob(GridResources resources, HazardMapCalculationParameters calcParams, String jobID, String jobName,
			String email, String configFileName) {
		super(resources, calcParams, jobID, jobName, email, configFileName);
	}
	
	@Override
	public HazardMapCalculationParameters getCalcParams() {
		return (HazardMapCalculationParameters)calcParams;
	}

	public static HazardMapJob fromXMLMetadata(Element jobElem) {
		GridResources resources = GridResources.fromXMLMetadata(jobElem.element(GridResources.XML_METADATA_NAME));
		HazardMapCalculationParameters resourceProvider = new HazardMapCalculationParameters(jobElem);
		
		String jobID = jobElem.attributeValue("jobID");
		Attribute jobNameAtt = jobElem.attribute("jobName");
		String jobName;
		if (jobNameAtt == null)
			jobName = jobID;
		else
			jobName = jobNameAtt.getValue();
		String email = jobElem.attributeValue("email");
		String configFileName = jobElem.attributeValue("configFileName");
		
		return new HazardMapJob(resources, resourceProvider, jobID, jobName, email, configFileName);
	}
}
