package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.impl.CVM2BasinDepth;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class CVM2BasinDepthServlet extends AbstractSiteDataServlet<Double> {
	
	private static final String FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/" + CVM2BasinDepth.FILE_NAME;
	
	public CVM2BasinDepthServlet() throws IOException {
		super(new CVM2BasinDepth(FILE));
	}
}
