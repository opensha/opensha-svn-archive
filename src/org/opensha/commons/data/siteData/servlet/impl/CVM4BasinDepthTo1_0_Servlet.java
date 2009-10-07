package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class CVM4BasinDepthTo1_0_Servlet extends
		AbstractSiteDataServlet<Double> {
	
	private static final String FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/data/siteData/CVM4/depth_1.0.bin";
	
	public CVM4BasinDepthTo1_0_Servlet() throws IOException {
		super(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0, FILE, false));
	}
}
