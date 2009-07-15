package org.opensha.commons.data.siteData.servlet.impl;

import java.io.IOException;

import org.opensha.commons.data.siteData.impl.SRTM30PlusTopoSlope;
import org.opensha.commons.data.siteData.servlet.AbstractSiteDataServlet;

public class SRTM30PlusTopoSlopeServlet extends AbstractSiteDataServlet<Double> {
	
	public static final String FILE_NAME = "/export/opensha/data/siteData/wald_allen_vs30/srtm30_plus_v5.0_grad.bin";
	
	public SRTM30PlusTopoSlopeServlet() throws IOException {
		super(new SRTM30PlusTopoSlope(FILE_NAME));
	}
}
