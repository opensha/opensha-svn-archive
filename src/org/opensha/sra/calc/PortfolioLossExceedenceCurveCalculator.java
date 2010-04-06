package org.opensha.sra.calc;

import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.IntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sra.asset.Asset;
import org.opensha.sra.asset.Portfolio;
import org.opensha.sra.vulnerability.Vulnerability;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class PortfolioLossExceedenceCurveCalculator {

	// TODO TectonicRegionType support?
	
	public DiscretizedFuncAPI calculateCurve(
			ScalarIntensityMeasureRelationshipAPI imr,
			EqkRupForecastAPI erf,
			Portfolio portfolio,
			DiscretizedFuncAPI function) {
		
		
		// data arrays
		int n = portfolio.size();
		// mean damage for mean IML
		double[] mDamage_mIML = new double[n];
		// high damage for mean IML
		double[] hDamage_mIML = new double[n];
		// low damage for mean IML
		double[] lDamage_mIML = new double[n];
		// mean damage ...
		double[] mDamage_hInter = new double[n];
		double[] mDamage_lInter = new double[n];
		double[] mDamage_hIntra = new double[n];
		double[] mDamage_lIntra = new double[n];
		
		// loop over assets
			// set mean low and high value array
		// ---
		
		// std dev tests
		
		// loop over sources
		
		for (int i=0; i<erf.getNumSources(); i++) {
			ProbEqkSource src = erf.getSource(i);
			
			for (int j=0; j<src.getNumRuptures(); j++) {
				
				for (int k=0; k<portfolio.size(); k++) {
					
					Asset asset = portfolio.get(k);
					Vulnerability vuln = asset.getVulnerability();
						
					imr.setIntensityMeasure(vuln.getIMT());
					imr.setSite(asset.getSite());
					imr.setEqkRupture(src.getRupture(j));
					
					double intraStd, interStd;
					
					double mLnIML = imr.getMean();
					ParameterAPI<String> stdParam = imr.getParameter(StdDevTypeParam.NAME);
					stdParam.setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
					double std = imr.getStdDev();
					
					if (stdParam.isAllowed(StdDevTypeParam.STD_DEV_TYPE_INTRA)) {
						stdParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTRA);
						intraStd = imr.getStdDev();
						stdParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTER);
						interStd = imr.getStdDev();
					}
					else {
						interStd = 0.25*std; // TODO make 0.25 adj. Param
						intraStd = Math.sqrt(std*std-interStd*interStd);
					}
					
					
					mDamage_mIML[k] = vuln.getMeanLoss(mLnIML);
					
					// TODO K. Porter explain 11th and 89th
					// e^(mIML + 0.5 * std * std)
					double mIML = Math.exp(mLnIML + 0.5 * std * std);
					hDamage_mIML[k] = vuln.getLossAtExceedProb(mIML, 0.11); 
					lDamage_mIML[k] = vuln.getLossAtExceedProb(mIML, 0.89);
					
					// TODO doublecheck log-space consistency for vulnerability
					// vuln not log space so what is mean?
					
					// e^(mIML+1.732*interStd)  96th %ile
					//
					// sqrt(3) = 1.732
					double interVal = 1.732 * interStd;
					double imlHighInter = Math.exp(mLnIML + interVal);
					double imlLowInter = Math.exp(mLnIML - interVal);
					mDamage_hInter[k] = vuln.getMeanLoss(imlHighInter);
					mDamage_lInter[k] = vuln.getMeanLoss(imlLowInter);
					
					double intraVal = 1.732 * intraStd;
					double imlHighIntra = Math.exp(mLnIML + intraVal);
					double imlLowIntra = Math.exp(mLnIML - intraVal);
					mDamage_hIntra[k] = vuln.getMeanLoss(imlHighIntra);
					mDamage_lIntra[k] = vuln.getMeanLoss(imlLowIntra);

					
					
				}
			}
		}
			// loop over ruptures
				// loop over Assets
					
					// compute mean, intra-, and inter-event
					// std dev from IMR
		
					// compute damage factor arrays
					//   - mDamage_mIML
					//   - hDamage_mIML
					//   - lDamage_mIML
					//   - mDamage_hInter
					//   - mDamage_lInter
					//   - mDamage_hIntra
					//   - mDamage_lIntra
					
					// do simulations
					// store 
		
		
					
					
		
		
		return null;
	}
}
