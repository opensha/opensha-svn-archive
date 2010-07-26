package org.opensha.sha.gui.util;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.opensha.commons.util.ServerPrefUtils;
import org.opensha.commons.util.ServerPrefs;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.gui.HazardCurveLocalModeApplication;
import org.opensha.sha.gui.HazardSpectrumLocalModeApplication;
import org.opensha.sha.gui.ScenarioShakeMapLocalModeCalcApp;

public class JNLPGen {
	
	private static final String jnlpDir = "ant" + File.separator + "jnlp";
	private static final String webRoot = "http://opensha.usc.edu/apps/opensha";
	
	private static final String vendor = "OpenSHA";
	private static final String homepage = "http://www.opensha.org";
	
	private Class<?> theClass;
	private String shortName;
	private String title;
	private int xmxMegs = 1024;
	private ServerPrefs prefs = ServerPrefUtils.SERVER_PREFS;
	private boolean startMenu = true;
	private boolean desktop = true;
	private boolean onlineOnly = false;
	
	public JNLPGen(Class<?> theClass, String shortName, String title, boolean onlineOnly) {
		System.out.println("Creating JNLP for: " + theClass.getName());
		this.theClass = theClass;
		this.shortName = shortName;
		this.title = title;
		this.onlineOnly = onlineOnly;
	}
	
	private String getDistType() {
		return prefs.getBuildType();
	}
	
	public void setOnlineOnly(boolean onlineOnly) {
		this.onlineOnly = onlineOnly;
	}
	
	public void writeJNLPFile() throws IOException {
		writeJNLPFile(jnlpDir);
	}
	
	public void writeJNLPFile(String dir) throws IOException {
		Document doc = createDocument();
		
		File dirFile = new File(dir);
		if (!dirFile.exists())
			dirFile.mkdir();
		
		String fileName = dir + File.separator + shortName + ".jnlp";
		System.out.println("Writing JNLP to: " + fileName);
		
		XMLUtils.writeDocumentToFile(fileName, doc);
	}
	
	public Document createDocument() {
		Document doc = DocumentHelper.createDocument();
		
		doc.addElement("jnlp");
		Element root = doc.getRootElement();
		
		// root attributes
		root.addAttribute("spec", "1.5+");
		String codeBaseURL = webRoot + "/" + shortName + "/" + getDistType();
		root.addAttribute("codebase", codeBaseURL);
		root.addAttribute("href", shortName + ".jnlp");
		
		// information
		Element infoEl = root.addElement("information");
		Element titleEl = infoEl.addElement("title");
		titleEl.addText(title);
		Element vendorEl = infoEl.addElement("vendor");
		vendorEl.addText(vendor);
		// shortcuts
		if (startMenu || desktop) {
			Element shortcutEl = infoEl.addElement("shortcut");
			shortcutEl.addAttribute("online", onlineOnly + "");
			if (desktop)
				shortcutEl.addElement("desktop");
			if (startMenu) {
				Element menuEl = shortcutEl.addElement("menu");
				menuEl.addAttribute("submenu", vendor);
			}
		}
		infoEl.addElement("homepage").addAttribute("href", homepage);
		if (!onlineOnly) {
			// offline-allowed
			infoEl.addElement("offline-allowed");
		}
		
		// resources
		Element resourcesEl = root.addElement("resources");
		Element j2seEl = resourcesEl.addElement("j2se");
		j2seEl.addAttribute("version", "1.6+");
		j2seEl.addAttribute("java-vm-args", "-Xmx"+xmxMegs+"M");
		j2seEl.addAttribute("href", "http://java.sun.com/products/autodl/j2se");
		Element jarEl = resourcesEl.addElement("jar");
		String jarName = shortName + ".jar";
		jarEl.addAttribute("href", jarName);
		jarEl.addAttribute("main", "true");
		
		// application-desc
		Element appDestEl = root.addElement("application-desc");
		appDestEl.addAttribute("name", title);
		appDestEl.addAttribute("main-class", theClass.getName());
		
		// update
		Element updateEl = root.addElement("update");
		updateEl.addAttribute("check", "timeout");
		
		// security
		Element securityEl = root.addElement("security");
		securityEl.addElement("all-permissions");
		
		return doc;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String outputDir = null;
		if (args.length == 0) {
			outputDir = JNLPGen.jnlpDir;
		} else if (args.length == 1) {
			outputDir = args[0];
		} else {
			System.err.println("USAGE: JNLPGen [outputDir]");
			System.exit(2);
		}
		new JNLPGen(HazardCurveLocalModeApplication.class,
					"HazardCurveLocal", "Hazard Curve Local Mode Application", false).writeJNLPFile(outputDir);
		new JNLPGen(HazardSpectrumLocalModeApplication.class,
				"HazardSpectrumLocal", "Hazard Spectrum Local Mode Application", false).writeJNLPFile(outputDir);
		new JNLPGen(ScenarioShakeMapLocalModeCalcApp.class,
				"ScenarioShakeMapLocal", "Scenario ShakeMap Local Mode Application", true).writeJNLPFile(outputDir);
	}

}
