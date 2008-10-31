package org.opensha.sha.calc.hazardMap;

import java.io.File;
import java.net.MalformedURLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.opensha.gridComputing.XMLPresetLoader;
import org.opensha.sha.calc.hazardMap.cron.CronConfLoader;

public class ConfLoader {
	
	public static final String CONF_NOTIFY_ELEMENT = "Notify";
	public static final String CONF_EMAIL = "email";
	
	XMLPresetLoader presets;
	String notifyEmail = "";
	CronConfLoader cronConf;
	
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

}
