package org.opensha.sha.calc.hazardMap.cron;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CronOperation {
	
	// *************
	// OPERATIONS
	// *************
	public static final String OP_SUBMIT = "submit";
	public static final String OP_CANCEL = "cancel";
	public static final String OP_DELETE = "delete";
	public static final String OP_RESTART = "restart";
	
	public static final String XML_ELEMENT_NAME = "CronOperation";
	public static final String XML_OPERATION_ATTRIBUTE_NAME = "operation";
	public static final String XML_ID_ATTRIBUTE_NAME = "id";
	
	Logger logger = HazardMapCronJob.logger;
	
	private String fileName;
	private Document document;
	
	private String operation;
	private String id = null;
	
	public CronOperation(String fileName) throws MalformedURLException, DocumentException {
		this.fileName = fileName;
		
		logger.info("Loading " + fileName);
		
		SAXReader reader = new SAXReader();
		document = reader.read(new File(fileName));
		
		// get the root element
		Element root = document.getRootElement();
		
		Element cronEl = root.element(XML_ELEMENT_NAME);
		
		operation = cronEl.attributeValue(XML_OPERATION_ATTRIBUTE_NAME);
		operation = operation.trim();
		
		if (!operation.equals(OP_SUBMIT)) {
			// if it's not the submit option, then they needed to supply an id
			id = cronEl.attributeValue(XML_ID_ATTRIBUTE_NAME);
		}
	}
	
	public String getFileName() {
		return fileName;
	}

	public Document getDocument() {
		return document;
	}

	public String getOperation() {
		return operation;
	}

	public String getID() {
		return id;
	}

}
