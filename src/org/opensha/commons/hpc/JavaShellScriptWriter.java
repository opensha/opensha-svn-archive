package org.opensha.commons.hpc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.opensha.commons.metadata.XMLSaveable;

import com.google.common.base.Preconditions;

public class JavaShellScriptWriter implements XMLSaveable {
	
	public static final String XML_METADATA_NAME = "JavaShellScriptWriter";
	
	private File javaBin;
	private int heapSizeMB;
	private Collection<File> classpath;
	
	public JavaShellScriptWriter(File javaBin, int heapSizeMB, Collection<File> classpath) {
		setJavaBin(javaBin);
		this.heapSizeMB = heapSizeMB;
		this.classpath = classpath;
	}
	
	public File getJavaBin() {
		return javaBin;
	}

	public void setJavaBin(File javaBin) {
		Preconditions.checkNotNull(javaBin, "java binary path cannot be null");
		this.javaBin = javaBin;
	}

	public int getHeapSizeMB() {
		return heapSizeMB;
	}

	public void setHeapSizeMB(int heapSizeMB) {
		this.heapSizeMB = heapSizeMB;
	}

	public Collection<File> getClasspath() {
		return classpath;
	}

	public void setClasspath(Collection<File> classpath) {
		this.classpath = classpath;
	}

	protected String getJVMArgs(String className) {
		Preconditions.checkNotNull(className, "class name cannot be null or empty");
		Preconditions.checkArgument(!className.isEmpty(), "class name cannot be null or empty");
		String cp = "";
		if (classpath != null && !classpath.isEmpty()) {
			cp = " -cp ";
			boolean first = true;
			for (File el : classpath) {
				if (first)
					first = false;
				else
					cp += ":";
				cp += el.getAbsolutePath();
			}
		}
		
		String heap = "";
		if (heapSizeMB > 0)
			heap = " -Xmx"+heapSizeMB+"M";
		
		String args = getFormattedArgs(heap+cp);
		
		return args+" "+className;
	}
	
	private String buildCommand(String className, String args) {
		String command = javaBin.getAbsolutePath()+getJVMArgs(className);
		
		command += getFormattedArgs(args);
		
		return command;
	}
	
	/**
	 * @param args
	 * @return arguments with a space in front, or empty string if null/empty
	 */
	protected static String getFormattedArgs(String args) {
		if (args != null && !args.isEmpty()) {
			if (!args.startsWith(" "))
				args = " "+args;
			return args;
		}
		return "";
	}
	
	public List<String> buildScript(String className, String args) {
		ArrayList<String> script = new ArrayList<String>();
		
		script.add("#!/bin/bash");
		script.add("");
		script.add(buildCommand(className, args));
		script.add("exit $?");
		
		return script;
	}
	
	public static void writeScript(File file, List<String> script) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		for (String line: script)
			fw.write(line + "\n");
		
		fw.close();
	}

	@Override
	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		el.addAttribute("javaBin", javaBin.getAbsolutePath());
		el.addAttribute("heapSizeMB", heapSizeMB+"");
		
		if (classpath != null && !classpath.isEmpty()) {
			Element cpEl = el.addElement("Classpath");
			for (File cp : classpath) {
				Element cpElEl = cpEl.addElement("element");
				cpElEl.addAttribute("path", cp.getAbsolutePath());
			}
		}
		
		return root;
	}
	
	public static JavaShellScriptWriter fromXMLMetadata(Element javaEl) {
		File javaBin = new File(javaEl.attributeValue("javaBin"));
		int heapSizeMB = Integer.parseInt(javaEl.attributeValue("heapSizeMB"));
		
		Element cpEl = javaEl.element("Classpath");
		ArrayList<File> classpath = null;
		if (cpEl != null) {
			Iterator<Element> it = cpEl.elementIterator("element");
			classpath = new ArrayList<File>();
			while (it.hasNext()) {
				Element cpElEl = it.next();
				File cp = new File(cpElEl.attributeValue("path"));
				classpath.add(cp);
			}
		}
		
		return new JavaShellScriptWriter(javaBin, heapSizeMB, classpath);
	}

}
