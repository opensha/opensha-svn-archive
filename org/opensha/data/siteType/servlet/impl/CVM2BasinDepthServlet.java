package org.opensha.data.siteType.servlet.impl;

import java.io.IOException;

import org.opensha.data.siteType.impl.CVM2BasinDepth;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class CVM2BasinDepthServlet extends AbstractSiteDataServlet<Double> {
	
	private static final String FILE = "/usr/local/tomcat/default/webapps/OpenSHA/WEB-INF/" + CVM2BasinDepth.FILE_NAME;
	
	public CVM2BasinDepthServlet() throws IOException {
		super(new CVM2BasinDepth(FILE));
	}
}
