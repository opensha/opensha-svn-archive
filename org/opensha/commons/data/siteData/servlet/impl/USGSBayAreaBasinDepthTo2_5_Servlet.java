package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.impl.USGSBayAreaBasinDepth;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class USGSBayAreaBasinDepthTo2_5_Servlet extends
		AbstractSiteDataServlet<Double> {
	
	private static final String FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/" + USGSBayAreaBasinDepth.DEPTH_2_5_FILE;
	
	public USGSBayAreaBasinDepthTo2_5_Servlet() throws IOException {
		super(new USGSBayAreaBasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5, FILE, false));
	}
}
