package org.opensha.data.siteType.servlet.impl;

import java.io.IOException;

import org.opensha.data.siteType.impl.SRTM30PlusTopography;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class SRTM30PlusTopographyServlet extends AbstractSiteDataServlet<Double> {
	
	public static final String FILE_NAME = "/export/opensha/data/siteData/srtm30_plus_v5.0";
	
	public SRTM30PlusTopographyServlet() throws IOException {
		super(new SRTM30PlusTopography(FILE_NAME));
	}
}
