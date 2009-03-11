package org.opensha.data.siteType.servlet.impl;

import java.io.IOException;

import org.opensha.data.siteType.impl.SRTM30Topography;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class SRTM30TopographyServlet extends AbstractSiteDataServlet<Double> {
	
	public static final String FILE_NAME = "/export/opensha/data/siteData/srtm30_v2.0";
	
	public SRTM30TopographyServlet() throws IOException {
		super(new SRTM30Topography(FILE_NAME));
	}
}
