package org.opensha.sha.calc.hazardMap;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.gridComputing.XMLPresetLoader;
import org.opensha.sha.calc.hazardMap.cron.CronConfLoader;
import org.opensha.util.FileNameComparator;
import org.opensha.util.XMLUtils;

public class ConfLoader {
	
	public static final String CONF_NOTIFY_ELEMENT = "Notify";
	public static final String CONF_REGIONS_PATH_ELEMENT = "GeographicRegionsPath";
	public static final String CONF_EMAIL = "email";
	
	XMLPresetLoader presets;
	String notifyEmail = "";
	CronConfLoader cronConf;
	String regionsPath;
	
	ArrayList<Document> regions = null;
	
	public ConfLoader(String confFile) throws MalformedURLException, DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new File(confFile));
		
		// get the root element
		Element root = document.getRootElement();
		
		// get the main conf element
		Element cronConfEl = root.element(CronConfLoader.CONF_MAIN_ELEMENT);
		
		Element notifyEl = root.element(CONF_NOTIFY_ELEMENT);
		if (notifyEl != null) {
			notifyEmail = notifyEl.attributeValue(CONF_EMAIL);
		}
		
		Element gridDefaultsEl = root.element(XMLPresetLoader.XML_METADATA_NAME);
		
		presets = XMLPresetLoader.fromXMLMetadata(gridDefaultsEl);
		cronConf = new CronConfLoader(cronConfEl);
		
		Element regionsEl = root.element(CONF_REGIONS_PATH_ELEMENT);
		regionsPath = regionsEl.attributeValue("value");
	}

	public XMLPresetLoader getPresets() {
		return presets;
	}

	public String getNotifyEmail() {
		return notifyEmail;
	}

	public CronConfLoader getCronConf() {
		return cronConf;
	}
	
	public ArrayList<Document> getGeographicRegionsDocs() {
		if (regions == null) {
			regions = new ArrayList<Document>();
			
			File dir = new File(regionsPath);
			
			if (dir.exists()) {
				File files[] = dir.listFiles();
				
				Arrays.sort(files, new FileNameComparator());
				
				for (File file : files) {
					if (!file.getName().toLowerCase().endsWith(".xml"))
						continue;
					try {
						Document doc = XMLUtils.loadDocument(file.getAbsolutePath());
						regions.add(doc);
						System.out.println("Loaded " + file.getName());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Error loading " + file.getName());
					}
				}
			}
		}
		
		return regions;
	}

}
