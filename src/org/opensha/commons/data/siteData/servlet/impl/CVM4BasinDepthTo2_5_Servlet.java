package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.SiteDataAPI;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class CVM4BasinDepthTo2_5_Servlet extends
		AbstractSiteDataServlet<Double> {
	
	private static final String FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/data/siteData/CVM4/depth_2.5.bin";
	
	public CVM4BasinDepthTo2_5_Servlet() throws IOException {
		super(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5, FILE, false));
	}
}
