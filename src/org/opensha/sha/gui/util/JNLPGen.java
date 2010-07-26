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
	
	private Class<?> theClass;
	private String shortName;
	private String title;
	private int xmxMegs = 1024;
	private ServerPrefs prefs = ServerPrefUtils.SERVER_PREFS;
	
	public JNLPGen(Class<?> theClass, String shortName, String title) {
		System.out.println(theClass.getName());
		this.theClass = theClass;
		this.shortName = shortName;
		this.title = title;
	}
	
	private String getDistType() {
		return prefs.getBuildType();
	}
	
	public void writeJNLPFile() throws IOException {
		writeJNLPFile(jnlpDir);
	}
	
	public void writeJNLPFile(String dir) throws IOException {
		Document doc = createDocument();
		
		XMLUtils.writeDocumentToFile(dir + File.separator + shortName + ".jnlp", doc);
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
		
		// offline-allowed
		root.addElement("offline-allowed");
		
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
					"HazardCurveLocal", "Hazard Curve Local Mode Application").writeJNLPFile(outputDir);
		new JNLPGen(HazardSpectrumLocalModeApplication.class,
				"HazardSpectrumLocal", "Hazard Spectrum Local Mode Application").writeJNLPFile(outputDir);
		new JNLPGen(ScenarioShakeMapLocalModeCalcApp.class,
				"ScenarioShakeMapLocal", "Scenario ShakeMap Local Mode Application").writeJNLPFile(outputDir);
	}

}
