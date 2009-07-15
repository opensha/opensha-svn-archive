package org.opensha.sha.calc.hazardMap.cron;

import java.io.File;
import java.net.MalformedURLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CronConfLoader {
	
	// XML schema statics
	public static final String CONF_MAIN_ELEMENT = "HazardMapCronJobConfiguration";
	public static final String CONF_DIR_ELEMENT = "Directories";
	public static final String CONF_INPUT_DIR = "input";
	public static final String CONF_PROCESSING_DIR = "processing";
	public static final String CONF_PROCESSED_DIR = "processed";
	public static final String CONF_FAILED_DIR = "failed";
	public static final String CONF_LOG_DIR = "log";
	public static final String CONF_HAZARD_MAP_DIR = "hazMaps";
	
	private String inDir;
	private String processingDir;
	private String processedDir;
	private String failedDir;
	private String logDir;
	private String hazMapDir;
	
	public CronConfLoader(String confFile) throws MalformedURLException, DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(confFile));
		
		// get the root element
		Element root = document.getRootElement();
		
		// get the main conf element
		Element cronEl = root.element(CONF_MAIN_ELEMENT);
		
		loadConf(cronEl);
	}
	
	public CronConfLoader(Element cronEl) {
		loadConf(cronEl);
	}
	
	private void loadConf(Element cronEl) {
		// get the directories element
		Element dirEl = cronEl.element(CONF_DIR_ELEMENT);
		
		inDir = loadPathAttribute(dirEl, CONF_INPUT_DIR);
		processingDir = loadPathAttribute(dirEl, CONF_PROCESSING_DIR);
		processedDir = loadPathAttribute(dirEl, CONF_PROCESSED_DIR);
		failedDir = loadPathAttribute(dirEl, CONF_FAILED_DIR);
		logDir = loadPathAttribute(dirEl, CONF_LOG_DIR);
		hazMapDir = loadPathAttribute(dirEl, CONF_HAZARD_MAP_DIR);
	}
	
	private static String loadPathAttribute(Element el, String attName) {
		String dir = el.attributeValue(attName);
		
		dir = dir.trim();
		dir = dir.replace('/', File.separatorChar);
		dir = dir.replace('\\', File.separatorChar);
		
		File dirFile = new File(dir);
		
		String path = dirFile.getAbsolutePath();
		
		if (!path.endsWith(File.separator))
			path += File.separator;
		
		return path;
	}

	public String getInDir() {
		return inDir;
	}

	public String getProcessingDir() {
		return processingDir;
	}

	public String getProcessedDir() {
		return processedDir;
	}

	public String getFailedDir() {
		return failedDir;
	}

	public String getLogDir() {
		return logDir;
	}
	
	public String getHazMapDir() {
		return hazMapDir;
	}

}
