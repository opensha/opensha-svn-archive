package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.impl.USGSBayAreaBasinDepth;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class USGSBayAreaBasinDepthTo1_0_Servlet extends
		AbstractSiteDataServlet<Double> {
	
	private static final String FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/" + USGSBayAreaBasinDepth.DEPTH_1_0_FILE;
	
	public USGSBayAreaBasinDepthTo1_0_Servlet() throws IOException {
		super(new USGSBayAreaBasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0, FILE, false));
	}
}
