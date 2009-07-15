package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.impl.WillsMap2000;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class WillsMap2000Servlet extends AbstractSiteDataServlet<String> {
	
	public static final String ABSOLUTE_FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/data/siteData/Wills2000/usgs_cgs_geology_60s_mod.txt";
	
	public WillsMap2000Servlet() throws IOException {
		super(new WillsMap2000(ABSOLUTE_FILE));
	}
	
}
