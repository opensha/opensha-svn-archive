package org.opensha.sra.vuln.models.curee.caltech;


/**
 * <strong>Title:</strong> CCTownhouseLimitedDrift<br />
 * <strong>Description</strong> A digital representation of the CUREE-Caltech Woodframe
 * Project townhouse limit drift Sagm(0.2s,5%) vulnerability function. (CWF-304-0205)
 * 
 * @see CureeCaltechWoodFrame
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 * @author Keith Porter
 *
 */
public class CCTownhouseLimitedDrift extends CureeCaltechWoodFrame {
	
	private static double [] DF = {
		0.000, 0.000, 0.000, 0.000, 0.000, 0.002, 0.005, 0.009, 0.014, 0.019,
		0.025, 0.030, 0.036, 0.040, 0.044, 0.048, 0.052, 0.056, 0.061, 0.065
	};
	
	private static double[] COVDF = {
		2.50, 2.50, 2.50, 2.50, 2.50, 1.59, 1.18, 0.96, 0.82, 0.74,
		0.68, 0.63, 0.60, 0.58, 0.56, 0.54, 0.53, 0.52, 0.50, 0.49
	};
	
	private static double[][] DEM = {
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.98E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.97E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.96E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.93E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.90E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.84E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.76E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.66E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.52E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.33E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 9.09E-001, 9.97E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 8.80E-001, 9.95E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 8.44E-001, 9.92E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 8.02E-001, 9.86E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 7.54E-001, 9.78E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 7.00E-001, 9.66E-001, 9.98E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 6.41E-001, 9.48E-001, 9.97E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 5.79E-001, 9.24E-001, 9.94E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 5.15E-001, 8.91E-001, 9.89E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 4.50E-001, 8.51E-001, 9.80E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 3.87E-001, 8.01E-001, 9.67E-001, 9.97E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 3.27E-001, 7.42E-001, 9.47E-001, 9.94E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.70E-001, 6.75E-001, 9.17E-001, 9.89E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.19E-001, 6.02E-001, 8.77E-001, 9.78E-001, 9.97E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.74E-001, 5.25E-001, 8.25E-001, 9.61E-001, 9.93E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.36E-001, 4.47E-001, 7.61E-001, 9.35E-001, 9.85E-001, 9.98E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.04E-001, 3.72E-001, 6.86E-001, 8.96E-001, 9.70E-001, 9.94E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 7.73E-002, 3.01E-001, 6.02E-001, 8.43E-001, 9.46E-001, 9.87E-001, 9.97E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 5.64E-002, 2.36E-001, 5.13E-001, 7.74E-001, 9.09E-001, 9.73E-001, 9.92E-001, 9.98E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 4.02E-002, 1.81E-001, 4.24E-001, 6.90E-001, 8.55E-001, 9.48E-001, 9.82E-001, 9.94E-001, 9.98E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.80E-002, 1.34E-001, 3.38E-001, 5.96E-001, 7.83E-001, 9.08E-001, 9.62E-001, 9.86E-001, 9.93E-001, 9.97E-001, 9.99E-001, 9.99E-001, 1.00E+000, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.91E-002, 9.62E-002, 2.60E-001, 4.95E-001, 6.94E-001, 8.50E-001, 9.28E-001, 9.70E-001, 9.84E-001, 9.92E-001, 9.96E-001, 9.98E-001, 9.99E-001, 1.00E+000, 1.00E+000},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.27E-002, 6.70E-002, 1.92E-001, 3.95E-001, 5.92E-001, 7.70E-001, 8.74E-001, 9.39E-001, 9.64E-001, 9.80E-001, 9.90E-001, 9.94E-001, 9.97E-001, 9.99E-001, 9.99E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 8.24E-003, 4.51E-002, 1.37E-001, 3.01E-001, 4.82E-001, 6.71E-001, 7.96E-001, 8.88E-001, 9.28E-001, 9.56E-001, 9.75E-001, 9.84E-001, 9.91E-001, 9.96E-001, 9.98E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 5.23E-003, 2.94E-002, 9.34E-002, 2.19E-001, 3.74E-001, 5.59E-001, 6.96E-001, 8.13E-001, 8.70E-001, 9.13E-001, 9.44E-001, 9.63E-001, 9.76E-001, 9.88E-001, 9.93E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 3.24E-003, 1.86E-002, 6.11E-002, 1.52E-001, 2.76E-001, 4.41E-001, 5.78E-001, 7.12E-001, 7.84E-001, 8.44E-001, 8.91E-001, 9.22E-001, 9.46E-001, 9.69E-001, 9.79E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.96E-003, 1.13E-002, 3.82E-002, 9.97E-002, 1.92E-001, 3.29E-001, 4.53E-001, 5.91E-001, 6.73E-001, 7.46E-001, 8.08E-001, 8.54E-001, 8.91E-001, 9.30E-001, 9.50E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.15E-003, 6.67E-003, 2.29E-002, 6.21E-002, 1.26E-001, 2.30E-001, 3.32E-001, 4.61E-001, 5.44E-001, 6.23E-001, 6.95E-001, 7.55E-001, 8.05E-001, 8.62E-001, 8.95E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 6.64E-004, 3.79E-003, 1.31E-002, 3.66E-002, 7.73E-002, 1.50E-001, 2.27E-001, 3.34E-001, 4.10E-001, 4.85E-001, 5.60E-001, 6.27E-001, 6.87E-001, 7.59E-001, 8.06E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 3.73E-004, 2.08E-003, 7.18E-003, 2.04E-002, 4.46E-002, 9.16E-002, 1.44E-001, 2.25E-001, 2.85E-001, 3.50E-001, 4.17E-001, 4.83E-001, 5.46E-001, 6.25E-001, 6.80E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.04E-004, 1.11E-003, 3.75E-003, 1.07E-002, 2.41E-002, 5.19E-002, 8.39E-002, 1.39E-001, 1.83E-001, 2.31E-001, 2.84E-001, 3.41E-001, 3.98E-001, 4.73E-001, 5.30E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.09E-004, 5.66E-004, 1.87E-003, 5.32E-003, 1.22E-002, 2.73E-002, 4.51E-002, 7.87E-002, 1.07E-001, 1.39E-001, 1.76E-001, 2.19E-001, 2.64E-001, 3.25E-001, 3.76E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 5.68E-005, 2.80E-004, 8.89E-004, 2.49E-003, 5.75E-003, 1.33E-002, 2.22E-002, 4.07E-002, 5.67E-002, 7.59E-002, 9.84E-002, 1.27E-001, 1.58E-001, 2.00E-001, 2.39E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.89E-005, 1.33E-004, 4.03E-004, 1.10E-003, 2.53E-003, 5.97E-003, 1.00E-002, 1.92E-002, 2.73E-002, 3.74E-002, 4.94E-002, 6.56E-002, 8.45E-002, 1.10E-001, 1.35E-001},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.43E-005, 6.12E-005, 1.74E-004, 4.54E-004, 1.04E-003, 2.48E-003, 4.14E-003, 8.19E-003, 1.19E-002, 1.65E-002, 2.21E-002, 3.03E-002, 4.02E-002, 5.35E-002, 6.73E-002},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 6.92E-006, 2.71E-005, 7.18E-005, 1.77E-004, 3.97E-004, 9.51E-004, 1.56E-003, 3.17E-003, 4.68E-003, 6.57E-003, 8.85E-003, 1.25E-002, 1.70E-002, 2.29E-002, 2.94E-002},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 3.26E-006, 1.16E-005, 2.82E-005, 6.52E-005, 1.41E-004, 3.36E-004, 5.35E-004, 1.11E-003, 1.66E-003, 2.33E-003, 3.14E-003, 4.54E-003, 6.33E-003, 8.56E-003, 1.12E-002},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.50E-006, 4.78E-006, 1.05E-005, 2.25E-005, 4.68E-005, 1.09E-004, 1.67E-004, 3.54E-004, 5.27E-004, 7.41E-004, 9.91E-004, 1.46E-003, 2.08E-003, 2.80E-003, 3.73E-003},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 6.70E-007, 1.90E-006, 3.76E-006, 7.31E-006, 1.44E-005, 3.27E-005, 4.77E-005, 1.01E-004, 1.51E-004, 2.10E-004, 2.77E-004, 4.16E-004, 6.00E-004, 8.00E-004, 1.08E-003},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.93E-007, 7.28E-007, 1.27E-006, 2.23E-006, 4.13E-006, 9.01E-006, 1.23E-005, 2.62E-005, 3.86E-005, 5.30E-005, 6.84E-005, 1.04E-004, 1.52E-004, 1.99E-004, 2.69E-004},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.25E-007, 2.69E-007, 4.12E-007, 6.40E-007, 1.10E-006, 2.28E-006, 2.91E-006, 6.12E-006, 8.88E-006, 1.19E-005, 1.50E-005, 2.31E-005, 3.39E-005, 4.29E-005, 5.80E-005},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 5.17E-008, 9.56E-008, 1.27E-007, 1.73E-007, 2.72E-007, 5.32E-007, 6.23E-007, 1.29E-006, 1.83E-006, 2.38E-006, 2.89E-006, 4.48E-006, 6.60E-006, 8.02E-006, 1.08E-005},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 2.09E-008, 3.28E-008, 3.71E-008, 4.37E-008, 6.24E-008, 1.14E-007, 1.21E-007, 2.44E-007, 3.37E-007, 4.24E-007, 4.91E-007, 7.65E-007, 1.13E-006, 1.30E-006, 1.74E-006},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 8.24E-009, 1.08E-008, 1.03E-008, 1.04E-008, 1.33E-008, 2.24E-008, 2.14E-008, 4.17E-008, 5.57E-008, 6.71E-008, 7.38E-008, 1.15E-007, 1.68E-007, 1.83E-007, 2.41E-007},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 3.16E-009, 3.45E-009, 2.74E-009, 2.32E-009, 2.64E-009, 4.04E-009, 3.43E-009, 6.41E-009, 8.23E-009, 9.44E-009, 9.78E-009, 1.51E-008, 2.19E-008, 2.23E-008, 2.87E-008},
		{0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 0.00E+000, 1.18E-009, 1.06E-009, 6.90E-010, 4.86E-010, 4.86E-010, 6.70E-010, 4.99E-010, 8.88E-010, 1.09E-009, 1.18E-009, 1.14E-009, 1.75E-009, 2.50E-009, 2.35E-009, 2.95E-009}
};
	
	public CCTownhouseLimitedDrift() {
		displayName = "CUREE-Caltech: Townhouse Limited Drift";
		setInitVars();
		ADF = 0.003;
		BDF = 0.218;
		NIML = IML.length;
		register(supportedTypes);
	}

	@Override
	/**
	 * See the general contract in CureeCaltechWoodFrame.
	 */
	protected double[] getDFArray() {
		return DF;
	}
	@Override
	/**
	 * See the general contract in CureeCaltechWoodFrame.
	 */
	protected double[] getCOVDFArray() {
		return COVDF;
	}
	public double[][] getDEMMatrix() {
		return DEM;
	}
}
