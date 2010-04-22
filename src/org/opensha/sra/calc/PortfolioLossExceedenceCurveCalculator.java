package org.opensha.sra.calc;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetOutputWriter;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.imr.IntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sra.asset.Asset;
import org.opensha.sra.asset.MonetaryHighLowValue;
import org.opensha.sra.asset.MonetaryValue;
import org.opensha.sra.asset.Portfolio;
import org.opensha.sra.asset.Value;
import org.opensha.sra.vulnerability.Vulnerability;

/**
 * Portfolio loss calculator as described in:
 * 
 * "Portfolio Scenario Loss and Loss Exceedance Curve Calculator using Moment Matching"
 * by Keith Porter, et al. 2010
 * 
 * and discussed by Keith Porter, Ned Field, Peter Powers, and Kevin Milner in Golden, CO
 * April 2010.
 *
 * 
 * @author Peter Powers, Kevin Milner
 * @version $Id:$
 */
public class PortfolioLossExceedenceCurveCalculator {
	
	// TODO allow user to specify?
	private static double valueCoefficientOfVariation = 0.15;
	
	// TODO allow user to specify?
	private static double interEventFactor = 0.25;

	// TODO TectonicRegionType support?
	
	private ArbitrarilyDiscretizedFunc[][] calculateCurve(
			ScalarIntensityMeasureRelationshipAPI imr,
			EqkRupForecastAPI erf,
			Portfolio portfolio,
			DiscretizedFuncAPI function) {
		// TODO actually use the function that's passed in
		
		// data arrays
		int n = portfolio.size();
		// mean value
		double[] mValue = new double[n]; // v sub j bar
		// high value
		double[] hValue = new double[n]; // v sub j+
		// low value
		double[] lValue = new double[n]; // v sub j-
		// mean damage for mean IML
		double[] mDamage_mIML = new double[n]; // y sub j bar
		// high damage for mean IML
		double[] hDamage_mIML = new double[n]; // y sub j+
		// low damage for mean IML
		double[] lDamage_mIML = new double[n]; // y sub j-
		// mean damage ...
		double[] mShaking = new double[n]; // s sub j bar
		double[] mDamage_hInter = new double[n]; // s sub +t
		double[] mDamage_lInter = new double[n]; // s sub -t
		double[] mDamage_hIntra = new double[n]; // s sub +p
		double[] mDamage_lIntra = new double[n]; // s sub -p
		
		// Equation 5
		double w0 = 1d - (6d + 4d*portfolio.size())/6d;
		double wi = 1d / 6d;
		
		double sqrt3 = Math.sqrt(3d);
		
		// loop over assets
		for (int i=0; i<portfolio.size(); i++) {
			Asset asset = portfolio.get(i);
			Value value = asset.getValue();
			if (value instanceof MonetaryValue) {
				MonetaryValue mvalue = (MonetaryValue)asset.getValue();
				
				double meanValue = mvalue.getValue();
				double highValue, lowValue;
				if (mvalue instanceof MonetaryHighLowValue) {
					MonetaryHighLowValue hlmValue = (MonetaryHighLowValue) mvalue;
					highValue = hlmValue.getHighValue();
					lowValue = hlmValue.getLowValue();
				} else {
					// if high/low value isn't given, we need to calculate it from mean and COV
					// Equation 11
					double medianValue = meanValue / Math.sqrt(1d + valueCoefficientOfVariation*valueCoefficientOfVariation);
					// Equation 12
					double coeffSqr3 = valueCoefficientOfVariation * sqrt3;
					highValue = medianValue * Math.exp(1d + coeffSqr3);
					lowValue = medianValue * Math.exp(1d - coeffSqr3);
				}
				
				// set mean low and high value arrays
				mValue[i] = meanValue; // v sub j bar
				hValue[i] = highValue; // v sub j+
				lValue[i] = lowValue;  // v sub j-
			} else {
				throw new RuntimeException("Value must be of type MonetaryValue");
			}
		}
		// ---
		
		// std dev tests
		
		// loop over sources
		
		ArbitrarilyDiscretizedFunc[][] exceedanceProbs = new ArbitrarilyDiscretizedFunc[erf.getNumSources()][];
		
		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource src = erf.getSource(sourceID);
			
			// TODO skip sources not within cutoff distance of any asset?
			
			exceedanceProbs[sourceID] = new ArbitrarilyDiscretizedFunc[src.getNumRuptures()];
			
			for (int rupID=0; rupID<src.getNumRuptures(); rupID++) {
				
				for (int k=0; k<portfolio.size(); k++) {
					
					Asset asset = portfolio.get(k);
					Vulnerability vuln = asset.getVulnerability();
					
					// TODO: deal with setting period for SA in a better way
					String imt = vuln.getIMT();
					IM_EventSetOutputWriter.setIMTFromString(imt, imr);
					imr.setSite(asset.getSite());
					imr.setEqkRupture(src.getRupture(rupID));
					
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
					} else {
						interStd = interEventFactor*std; // Equation 6
						intraStd = Math.sqrt(std*std-interStd*interStd); // Equation 7
					}
					
					mDamage_mIML[k] = vuln.getMeanLoss(mLnIML); // y sub j bar
					
					// TODO K. Porter explain 11th and 89th
					// e^(mIML + 0.5 * std * std)
					double mIML = Math.exp(mLnIML + 0.5 * std * std); // Equation 9, mean shaking real domain
					mShaking[k] = mIML; // s sub j bar
					
					hDamage_mIML[k] = vuln.getLossAtExceedProb(mIML, 0.11); // y sub j+
					lDamage_mIML[k] = vuln.getLossAtExceedProb(mIML, 0.89); // y sub j-
					
					// TODO doublecheck log-space consistency for vulnerability
					// vuln not log space so what is mean?
					
					// Equation 8
					// e^(mIML+1.732*interStd)  96th %ile
					//
					// sqrt(3) = 1.732
					double interVal = sqrt3 * interStd;
					double imlHighInter = Math.exp(mLnIML + interVal);
					double imlLowInter = Math.exp(mLnIML - interVal);
					mDamage_hInter[k] = vuln.getMeanLoss(imlHighInter); // s sub +t
					mDamage_lInter[k] = vuln.getMeanLoss(imlLowInter);  // s sub -t
					
					double intraVal = sqrt3 * intraStd;
					double imlHighIntra = Math.exp(mLnIML + intraVal);
					double imlLowIntra = Math.exp(mLnIML - intraVal);
					mDamage_hIntra[k] = vuln.getMeanLoss(imlHighIntra); // s sub +p
					mDamage_lIntra[k] = vuln.getMeanLoss(imlLowIntra);  // s sub -p

					
					
				}
//				int numSamples = 8 + 4*portfolio.size();
				int numSamples = 7;
				double[] l = new double[numSamples];
				double[] lSquared = new double[numSamples];
				// init arrays to 0
				for (int i=0; i<numSamples; i++) {
					l[i] = 0;
					lSquared[i] = 0;
				}
				// now we combine everything
				for (int i=0; i<portfolio.size(); i++) {
					Asset asset = portfolio.get(i);
					
					double val;
					
					// Equation 20
					val = mValue[i] * mDamage_mIML[i] * mShaking[i];
					l[0] += val;
					lSquared[0] += val * val;
					
					// Equation 21
					val = mValue[i] * mDamage_mIML[i] * mDamage_hInter[i];
					l[1] += val;
					lSquared[1] += val * val;
					
					// Equation 22
					val = mValue[i] * mDamage_mIML[i] * mDamage_lInter[i];
					l[2] += val;
					lSquared[2] += val * val;
					
					// Equation 23
					val = hValue[i] * mDamage_mIML[i] * mShaking[i];
					l[3] += val;
					lSquared[2] += val * val;
					
					// Equation 24
					val = lValue[i] * mDamage_mIML[i] * mShaking[i];
					l[4] += val;
					lSquared[2] += val * val;
					
					// Equation 25
					val = mValue[i] * hDamage_mIML[i] * mShaking[i];
					l[5] += val;
					lSquared[0] += val * val;
					
					// Equation 26
					val = mValue[i] * lDamage_mIML[i] * mShaking[i];
					l[6] += val;
					lSquared[0] += val * val;
				}
				
				// all this is for Equation 33
				double sumReg = 0;
				double sumSquares = 0;
				for (int i=0; i<portfolio.size(); i++) {
					Asset asset = portfolio.get(i);
					
					// vBar ( yBar ( s sub +p ) + yBar ( s sub -p))
					sumReg += mValue[i] * ( mDamage_mIML[i] * mDamage_hIntra[i] + mDamage_mIML[i] * mDamage_lIntra[i] );
					sumSquares += Math.pow(mValue[i] * mDamage_mIML[i] * mDamage_hIntra[i], 2)
										+ Math.pow(mValue[i] * mDamage_mIML[i] * mDamage_lIntra[i], 2);
				}
				double e_LgivenS = w0 * l[0] + wi * (l[1] + l[2] + l[3] + l[4] + 2*l[5] + 2*l[6]
									+ (4*portfolio.size() - 4)*l[0] + sumReg);
				double e_LSuqaredGivenS = w0 * lSquared[0] + wi * (lSquared[1] + lSquared[2] + lSquared[3] + lSquared[4]
									+ 2*lSquared[5] + 2*lSquared[6] + (4*portfolio.size() - 4)*lSquared[0] + sumSquares);
				
				// Equation 34
				double varLgivenS = e_LSuqaredGivenS - e_LgivenS * e_LgivenS;
				
				// Eqaution 18
				double deltaSquaredSubLgivenS = varLgivenS / (e_LgivenS * e_LgivenS);
				
				// Equation 17
				double thetaSubLgivenS = e_LgivenS / Math.sqrt(1d + deltaSquaredSubLgivenS);
				
				// Equation 19
				double betaSubLgivenS = Math.sqrt(Math.log(1d + deltaSquaredSubLgivenS));
				
				ArbDiscrEmpiricalDistFunc distFunc = new ArbDiscrEmpiricalDistFunc();
				for (int k=0; k<51; k++) {
					double x = Math.pow(10d, -5d + 0.1 * k);
					double inside = (Math.log(x) / thetaSubLgivenS) / betaSubLgivenS;
					distFunc.set(x, inside);
				}
				ArbitrarilyDiscretizedFunc normCumDist = distFunc.getNormalizedCumDist();
				for (int k=0; k<51; k++) {
					double x = normCumDist.getX(k);
					double y = normCumDist.getY(k);
					normCumDist.set(x, 1-y);
				}
				
				exceedanceProbs[sourceID][rupID] = normCumDist;
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
		
		return exceedanceProbs;
	}
	
	public ArbitrarilyDiscretizedFunc calcProbabilityOfExceedanceCurve(
			ScalarIntensityMeasureRelationshipAPI imr,
			EqkRupForecastAPI erf,
			Portfolio portfolio,
			DiscretizedFuncAPI function) {
		
		ArbitrarilyDiscretizedFunc[][] exceedanceProbs = calculateCurve(imr, erf, portfolio, function);
		
		ArbitrarilyDiscretizedFunc curve = new ArbitrarilyDiscretizedFunc();
		
		// init the curve
		for (int i=0; i<exceedanceProbs[0][0].getNum(); i++) {
			ArbitrarilyDiscretizedFunc normCumDist = exceedanceProbs[0][0];
			curve.set(normCumDist.getX(i), 0.0);
		}
		
		for (int k=0; k<curve.getNum(); k++) {
			for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
				ProbEqkSource src = erf.getSource(sourceID);
				for (int rupID=0; rupID<src.getNumRuptures(); rupID++) {
					ProbEqkRupture rup = src.getRupture(rupID);
					
					ArbitrarilyDiscretizedFunc normCumDist = exceedanceProbs[sourceID][rupID];
					
					double rupProb = rup.getProbability();
					
					double x = normCumDist.getX(k);
					
					double y = curve.getY(k);
					
					y +=  rupProb * normCumDist.getY(k);
					
					curve.set(x, y);
				}
			}
		}
		
		return curve;
	}
	
	public ArbitrarilyDiscretizedFunc calcFrequencyOfExceedanceCurve(
			ScalarIntensityMeasureRelationshipAPI imr,
			EqkRupForecastAPI erf,
			Portfolio portfolio,
			DiscretizedFuncAPI function) {
		// TODO implement frequency
		return null;
	}
}

