package org.opensha.sra.calc;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.sha.calc.IM_EventSet.v03.IM_EventSetOutputWriter;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
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
 * Revisions made to match new version of document from 10/21/2010.
 *
 * 
 * @author Peter Powers, Kevin Milner
 * @version $Id:$
 */
public class PortfolioLossExceedenceCurveCalculator {
	
	public static final boolean D = true;
	
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
		
		// Equation 9
		double w0 = 1d - (6d + 4d*portfolio.size())/6d;
		if (D) System.out.println("w0: " + w0 + " (eqn 9)");
		double wi = 1d / 6d;
		if (D) System.out.println("wi: " + wi + " (eqn 9)");
		if (D) System.out.println("");
		
		double sqrt3 = Math.sqrt(3d);
		
		// loop over assets
		for (int i=0; i<portfolio.size(); i++) {
			Asset asset = portfolio.get(i);
			Value value = asset.getValue();
			if (D) System.out.println("Asset " + i);
			if (value instanceof MonetaryValue) {
				MonetaryValue mvalue = (MonetaryValue)asset.getValue();
				
				double meanValue = mvalue.getValue();
				if (D) System.out.println("meanValue: " + meanValue + " (v sub jbar)");
				double highValue, lowValue;
				if (mvalue instanceof MonetaryHighLowValue) {
					if (D) System.out.println("Asset already has high/low vals");
					MonetaryHighLowValue hlmValue = (MonetaryHighLowValue) mvalue;
					highValue = hlmValue.getHighValue();
					lowValue = hlmValue.getLowValue();
					if (D) {
						System.out.println("highValue: " + highValue + " (v sub j+) (from asset)");
						System.out.println("lowValue: " + lowValue + " (v sub j-) (from asset)");
					}
				} else {
					if (D) System.out.println("calculating high/low vals");
					// if high/low value isn't given, we need to calculate it from mean and COV
					// Equation 15
					double betaVJ = Math.sqrt(Math.log(1d+valueCoefficientOfVariation*valueCoefficientOfVariation));
					double medianValue = meanValue / Math.sqrt(Math.exp(betaVJ*betaVJ));
					// Equation 16
					highValue = medianValue * Math.exp(betaVJ*sqrt3);
					lowValue = medianValue * Math.exp(-betaVJ*sqrt3);
					if (D) {
						System.out.println("highValue: " + highValue + " (v sub j+) (eqn 16)");
						System.out.println("lowValue: " + lowValue + " (v sub j-) (eqn 16)");
					}
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
				
				if (D) System.out.println("");
				if (D) System.out.println("src: " + sourceID + " rup: " + rupID
						+ " prob: " + erf.getRupture(sourceID, rupID).getProbability());
				
				for (int k=0; k<portfolio.size(); k++) {
					
					if (D) System.out.println("Asset " + k);
					
					Asset asset = portfolio.get(k);
					Vulnerability vuln = asset.getVulnerability();
					
					// TODO: deal with setting period for SA in a better way
					String imt = vuln.getIMT();
					IM_EventSetOutputWriter.setIMTFromString(imt, imr);
					imr.setSite(asset.getSite());
					imr.setEqkRupture(src.getRupture(rupID));
					
					double intraStd, interStd;
					
					double mLnIML = imr.getMean();
					if (D) System.out.println("mLnIML: " + mLnIML);
					ParameterAPI<String> stdParam = imr.getParameter(StdDevTypeParam.NAME);
					stdParam.setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
					double std = imr.getStdDev();
					if (D) System.out.println("ln STD: " + std);
					
					if (stdParam.isAllowed(StdDevTypeParam.STD_DEV_TYPE_INTRA)) {
						stdParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTRA);
						intraStd = imr.getStdDev();
						stdParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTER);
						interStd = imr.getStdDev();
						if (D) System.out.println("interStd: " + interStd + " (from IMR)");
						if (D) System.out.println("intraStd: " + intraStd + " (from IMR)");
					} else {
						if (D) System.out.println("IMR doesn't support inter/intra std, we hae to calculate");
						if (D) System.out.println("interEventFactor: " + interEventFactor);
						interStd = interEventFactor*std; // Equation 10
						intraStd = Math.sqrt(std*std-interStd*interStd); // Equation 11
						if (D) System.out.println("interStd: " + interStd + " (eqn 10)");
						if (D) System.out.println("intraStd: " + intraStd + " (eqn 11)");
					}
					
					// TODO K. Porter explain 11th and 89th
					// e^(mIML + 0.5 * std * std)
					double mIML = Math.exp(mLnIML + 0.5 * std * std); // Equation 13, mean shaking real domain
					if (D) System.out.println("mIML: " + mIML + " (s sub j bar) (eqn 13)");
					
					mDamage_mIML[k] = vuln.getMeanDamageFactor(mIML); // y sub j bar
					if (D) System.out.println("mDamage_mIML: " + mDamage_mIML[k] + " (y sub j bar)");
					
					mShaking[k] = mIML; // s sub j bar
					
					hDamage_mIML[k] = vuln.getMeanDamageAtExceedProb(mIML, 0.11); // y sub j+
					if (D) System.out.println("hDamage_mIML: " + hDamage_mIML[k] + " (y sub j+)");
					lDamage_mIML[k] = vuln.getMeanDamageAtExceedProb(mIML, 0.89); // y sub j-
					if (D) System.out.println("lDamage_mIML: " + lDamage_mIML[k] + " (y sub j+)");
					
					// TODO doublecheck log-space consistency for vulnerability
					// vuln not log space so what is mean?
					
					// Equation 12
					// e^(mIML+1.732*interStd)  96th %ile
					//
					// sqrt(3) = 1.732
					double interVal = sqrt3 * interStd;
					double imlHighInter = Math.exp(interVal);
					if (D) System.out.println("imlHighInter: " + imlHighInter);
					double imlLowInter = Math.exp(-1d*interVal);
					if (D) System.out.println("imlLowInter: " + imlLowInter);
					mDamage_hInter[k] = vuln.getMeanDamageFactor(imlHighInter); // s sub +t
					if (D) System.out.println("mDamage_hInter: " + mDamage_hInter[k] + " (s sub +t) (eqn 12)");
					mDamage_lInter[k] = vuln.getMeanDamageFactor(imlLowInter);  // s sub -t
					if (D) System.out.println("mDamage_lInter: " + mDamage_lInter[k] + " (s sub -t) (eqn 12)");
					
					double intraVal = sqrt3 * intraStd;
					double imlHighIntra = Math.exp(intraVal);
					double imlLowIntra = Math.exp(-1d*intraVal);
					mDamage_hIntra[k] = vuln.getMeanDamageFactor(imlHighIntra); // s sub +p
					if (D) System.out.println("mDamage_hIntra: " + mDamage_hIntra[k] + " (s sub +p) (eqn 12)");
					mDamage_lIntra[k] = vuln.getMeanDamageFactor(imlLowIntra);  // s sub -p
					if (D) System.out.println("mDamage_lIntra: " + mDamage_lIntra[k] + " (s sub +p) (eqn 12)");

					
					
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
					double tempVal;
					
					if (D) System.out.println("Asset " + i + " (showing intermediate sums for L's)");
					
					// Equation 24
					tempVal = mValue[i] * mDamage_mIML[i] * mShaking[i];
					l[0] += tempVal;
					lSquared[0] += tempVal * tempVal;
					if (D) System.out.println("L[0]: " + l[0] + " (eqn 24)");
					if (D) System.out.println("L^2[0]: " + lSquared[0] + " (eqn 24)");
					
					// Equation 25
					tempVal = mValue[i] * mDamage_mIML[i] * mDamage_hInter[i];
					l[1] += tempVal;
					lSquared[1] += tempVal * tempVal;
					if (D) System.out.println("L[1]: " + l[1] + " (eqn 25)");
					if (D) System.out.println("L^2[1]: " + lSquared[1] + " (eqn 25)");
					
					// Equation 26
					tempVal = mValue[i] * mDamage_mIML[i] * mDamage_lInter[i];
					l[2] += tempVal;
					lSquared[2] += tempVal * tempVal;
					if (D) System.out.println("L[2]: " + l[2] + " (eqn 26)");
					if (D) System.out.println("L^2[2]: " + lSquared[2] + " (eqn 26)");
					
					// Equation 27
					tempVal = hValue[i] * mDamage_mIML[i] * mShaking[i];
					l[3] += tempVal;
					lSquared[3] += tempVal * tempVal;
					if (D) System.out.println("L[3]: " + l[3] + " (eqn 27)");
					if (D) System.out.println("L^2[3]: " + lSquared[3] + " (eqn 27)");
					
					// Equation 28
					tempVal = lValue[i] * mDamage_mIML[i] * mShaking[i];
					l[4] += tempVal;
					lSquared[4] += tempVal * tempVal;
					if (D) System.out.println("L[4]: " + l[4] + " (eqn 28)");
					if (D) System.out.println("L^2[4]: " + lSquared[4] + " (eqn 28)");
					
					// Equation 29
					tempVal = mValue[i] * hDamage_mIML[i] * mShaking[i];
					l[5] += tempVal;
					lSquared[5] += tempVal * tempVal;
					if (D) System.out.println("L[5]: " + l[5] + " (eqn 29)");
					if (D) System.out.println("L^2[5]: " + lSquared[5] + " (eqn 29)");
					
					// Equation 30
					tempVal = mValue[i] * lDamage_mIML[i] * mShaking[i];
					l[6] += tempVal;
					lSquared[6] += tempVal * tempVal;
					if (D) System.out.println("L[6]: " + l[6] + " (eqn 30)");
					if (D) System.out.println("L^2[6]: " + lSquared[6] + " (eqn 30)");
				}
				
				// all this is for Equation 37
				double sumReg = 0;
				double sumSquares = 0;
				for (int i=0; i<portfolio.size(); i++) {
					// vBar ( yBar ( s sub +p ) + yBar ( s sub -p))
					sumReg += mValue[i] * ( mDamage_mIML[i] * mDamage_hIntra[i] + mDamage_mIML[i] * mDamage_lIntra[i] );
					sumSquares += Math.pow(mValue[i] * mDamage_mIML[i] * mDamage_hIntra[i], 2)
										+ Math.pow(mValue[i] * mDamage_mIML[i] * mDamage_lIntra[i], 2);
				}
				double e_LgivenS = w0 * l[0] + wi * (l[1] + l[2] + l[3] + l[4] + 2*l[5] + 2*l[6]
									+ (4*portfolio.size() - 4)*l[0] + sumReg);
				double e_LSuqaredGivenS = w0 * lSquared[0] + wi * (lSquared[1] + lSquared[2] + lSquared[3] + lSquared[4]
									+ 2*lSquared[5] + 2*lSquared[6] + (4*portfolio.size() - 4)*lSquared[0] + sumSquares);
				if (D) System.out.println("e_LgivenS: " + e_LgivenS + " (eqn 37)");
				if (D) System.out.println("e_LSuqaredGivenS: " + e_LSuqaredGivenS + " (eqn 37)");
				
				// Equation 38
				double varLgivenS = e_LSuqaredGivenS - e_LgivenS * e_LgivenS;
				if (D) System.out.println("varLgivenS: " + varLgivenS + " (eqn 38)");
				
				// Eqaution 22
				double deltaSquaredSubLgivenS = varLgivenS / (e_LgivenS * e_LgivenS);
				if (D) System.out.println("deltaSquaredSubLgivenS: " + deltaSquaredSubLgivenS + " (eqn 22)");
				
				// Equation 21
				double thetaSubLgivenS = e_LgivenS / Math.sqrt(1d + deltaSquaredSubLgivenS);
				if (D) System.out.println("thetaSubLgivenS: " + thetaSubLgivenS + " (eqn 21)");
				
				// Equation 23
				double betaSubLgivenS = Math.sqrt(Math.log(1d + deltaSquaredSubLgivenS));
				if (D) System.out.println("betaSubLgivenS: " + betaSubLgivenS + " (eqn 23)");
				
				// Equation 40
				double sumMeanValues = 0;
				for (int i=0; i<portfolio.size(); i++) {
					sumMeanValues += mValue[i];
				}
				if (D) System.out.println("sumMeanValues: " + sumMeanValues + " (eqn 40)");
				
				// Equation 39
				ArbDiscrEmpiricalDistFunc distFunc = new ArbDiscrEmpiricalDistFunc();
				for (int k=0; k<51; k++) {
					double x = Math.pow(10d, -5d + 0.1 * k);
					double inside = (Math.log(x) * sumMeanValues / thetaSubLgivenS) / betaSubLgivenS;
					distFunc.set(x, inside);
				}
				ArbitrarilyDiscretizedFunc normCumDist = distFunc.getNormalizedCumDist();
				for (int k=0; k<51; k++) {
					double x = normCumDist.getX(k);
					double y = normCumDist.getY(k);
					normCumDist.set(x, 1-y);
				}
				
				if (D) System.out.println("normCumDist: (eqn 39)\n" + normCumDist);
				
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
		
		// init the curve x values
		for (int i=0; i<exceedanceProbs[0][0].getNum(); i++) {
			ArbitrarilyDiscretizedFunc normCumDist = exceedanceProbs[0][0];
			curve.set(normCumDist.getX(i), 0.0);
		}
		
		if (D) System.out.println("Creating final curve");
		for (int k=0; k<curve.getNum(); k++) {
			double x = curve.getX(k);
			if (D) System.out.println("iml: " + x);
			
			double product = 1;
			for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
				ProbEqkSource src = erf.getSource(sourceID);
				for (int rupID=0; rupID<src.getNumRuptures(); rupID++) {
					ProbEqkRupture rup = src.getRupture(rupID);
					
					ArbitrarilyDiscretizedFunc normCumDist = exceedanceProbs[sourceID][rupID];
					
					double rupProb = rup.getProbability();
					
					if (D) System.out.println("src: " + sourceID + " rup: " + rupID + " prob: " + rupProb);
					
					double normCumDistVal = normCumDist.getY(k);
					if (D) System.out.println("normCumDist[iml]: " + normCumDistVal);
					if (Double.isNaN(normCumDistVal)) {
						if (D) System.out.println("it's NaN, skipping");
						continue;
					}
					
					// part of equation 42 
					product *= 1 - rupProb * normCumDist.getY(k);
				}
			}
			// 1 - product (part of eqn 42)
			curve.set(x, 1-product);
		}
		
		if (D) System.out.println("*** final prob curve:\n" + curve);
		
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

