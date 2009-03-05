package org.opensha.data.siteType.servlet.impl;

import java.io.File;
import java.io.IOException;

import org.opensha.data.siteType.impl.WillsMap2006;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class WillsMap2006Servlet extends AbstractSiteDataServlet<Double> {
	
	public WillsMap2006Servlet() throws IOException {
		super(new WillsMap2006(WillsMap2006.SERVER_BIN_FILE));
	}
	
}
