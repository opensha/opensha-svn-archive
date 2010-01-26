package org.opensha.sra.vuln.models.curee.caltech;

import java.util.ArrayList;

import org.opensha.sra.vuln.AbstractVulnerability;

/**
 * <strong>Title:</strong> CureeCaltechWoodFrame<br />
 * <strong>Description:</strong> A template used to describe how the CUREE-Caltech Wood Frame
 * Project Vulnerability Functions will be implemented.  The basic idea is of these vulnerability
 * functions is that V(IML) = DF.  CUREE-Caltech models implemented herein all have a predefined
 * descritized set of values for IML and corresponding DF and COVDF.
 * 
 * @see scratch.martinez.VulnerabilityModels.VulnerabilityModel
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 * @author Keith Porter
 */
public abstract class CureeCaltechWoodFrame extends AbstractVulnerability {
	protected String displayName = "";
	protected static double[] IML = {
			0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
			1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0

	};

	protected static double[] DEM_DFs = {
		1.00E-006, 1.20E-006, 1.44E-006, 1.73E-006, 2.07E-006, 2.49E-006, 2.99E-006, 3.58E-006,
		4.30E-006, 5.16E-006, 6.19E-006, 7.43E-006, 8.92E-006, 1.07E-005, 1.28E-005, 1.54E-005,
		1.85E-005, 2.22E-005, 2.66E-005, 3.19E-005, 3.83E-005, 4.60E-005, 5.52E-005, 6.62E-005,
		7.95E-005, 9.54E-005, 1.14E-004, 1.37E-004, 1.65E-004, 1.98E-004, 2.37E-004, 2.85E-004,
		3.42E-004, 4.10E-004, 4.92E-004, 5.91E-004, 7.09E-004, 8.51E-004, 1.02E-003, 1.22E-003,
		1.47E-003, 1.76E-003, 2.12E-003, 2.54E-003, 3.05E-003, 3.66E-003, 4.39E-003, 5.27E-003,
		6.32E-003, 7.58E-003, 9.10E-003, 1.09E-002, 1.31E-002, 1.57E-002, 1.89E-002, 2.26E-002,
		2.72E-002, 3.26E-002, 3.91E-002, 4.70E-002, 5.63E-002, 6.76E-002, 8.11E-002, 9.74E-002,
		1.17E-001, 1.40E-001, 1.68E-001, 2.02E-001, 2.42E-001, 2.91E-001, 3.49E-001, 4.19E-001,
		5.02E-001, 6.03E-001, 7.23E-001, 8.68E-001
	};
	
	protected void setInitVars() {
		supportedTypes = new ArrayList<String>();
		//supportedTypes.add("scratchJavaDevelopers.martinez.WoodFrame");
		setPeriod(0.2);
	}
	
	@Override
	/**
	 * See the general contract in VulnerabilityFunction
	 */
	public ArrayList<double[]> getDFTable() {
		ArrayList<double[]> rtn = new ArrayList<double[]>();
		double[] DF = getDFArray();
		double[] COVDF = getCOVDFArray();
		
		for(int i = 0; i < NIML; ++i) {
			double[] tmp = {IML[i], DF[i], COVDF[i]};
			rtn.add(tmp);
		}
		return rtn;
	}
	@Override
	/**
	 * See the general contract in VulnerabilityModel
	 * (Not implemented)
	 */
	public double getDF(double IML) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	/**
	 * See the general contract in VulnerabilityModel
	 */
	public String getIMT() {
		return AbstractVulnerability.SA;
	}
	@Override
	/**
	 * See the general contract in VulnerabilityModel
	 */
	public String getDisplayName() {
		return displayName;
	}
	public double[] getDEMDFVals() {
		return DEM_DFs;
	}
	
	protected abstract double[] getDFArray();
	protected abstract double[] getCOVDFArray();
}
