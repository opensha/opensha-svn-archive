package org.opensha.data.siteType.servlet.impl;

import java.io.IOException;

import org.opensha.data.siteType.impl.SRTM30TopoSlope;
import org.opensha.data.siteType.servlet.AbstractSiteDataServlet;

public class SRTM30TopoSlopeServlet extends AbstractSiteDataServlet<Double> {
	
	public static final String FILE_NAME = "/export/opensha/data/siteData/wald_allen_vs30/srtm30_v2.0_grad.bin";
	
	public SRTM30TopoSlopeServlet() throws IOException {
		super(new SRTM30TopoSlope(FILE_NAME));
	}
}
