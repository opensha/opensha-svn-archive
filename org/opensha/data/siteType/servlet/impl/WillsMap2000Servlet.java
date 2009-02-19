package org.opensha.data.siteType.servlet.impl;

import java.io.IOException;

import org.opensha.data.siteType.impl.WillsMap2000;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class WillsMap2000Servlet extends AbstractSiteDataServlet<String> {
	
	public WillsMap2000Servlet() throws IOException {
		super(new WillsMap2000());
	}
	
}
