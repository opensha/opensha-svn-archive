package org.opensha.data.siteType.servlet.impl;

import java.io.IOException;

import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.impl.CVM4BasinDepth;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class CVM4BasinDepthTo1_0_Servlet extends
		AbstractSiteDataServlet<Double> {
	
	public CVM4BasinDepthTo1_0_Servlet() throws IOException {
		super(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0, false));
	}
}
