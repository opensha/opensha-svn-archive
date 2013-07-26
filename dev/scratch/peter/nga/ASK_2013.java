/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package scratch.peter.nga;

import static java.lang.Math.*;
import static org.opensha.commons.geo.GeoTools.TO_RAD;
import static scratch.peter.nga.IMT.PGA;
import static scratch.peter.nga.FaultStyle.*;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * Preliminary implementation of the Abrahamson & Silva (2013) next generation 
 * attenuation relationship developed as part of NGA West II.
 * 
 * Component: RotD50
 * 
 * Implementation details:
 * 
 * Not thread safe -- create new instances as needed
 * 
 * @author Peter Powers
 * @created November 2012
 * @version $Id: CB_2008_AttenRel.java 9377 2012-09-05 19:04:42Z pmpowers $
 */
public class ASK_2013 {

	static final String NAME = "Abrahamson & Silva (2013)";
	
	private final Coeffs coeffs;
	
	// author declared constants
	private static final double N = 1.5;
	private static final double C = 1.8;

	// implementation constants
	private static final double A = pow(610, 4);
	private static final double B = pow(1360, 4) + A;
	
	private class Coeffs extends Coefficients {
		
		// TODO inline constance coeffs with final statics
		
		double a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14,
				a15, a17, a43, a44, a45, a46, b, c4, s1, s2, s3, s4, Vlin;
		
		Coeffs() {
			super("ASK13.csv");
			set(PGA);
		}
	}
	
	/**
	 * Constructs a new instance of this attenuation relationship.
	 */
	public ASK_2013() {
		coeffs = new Coeffs();
	}

	/**
	 * Returns the ground motion for the supplied arguments.
	 * @param imt intensity measure type
	 * @param Mw moment magnitude
	 * @param rJB Joyner-Boore distance to rupture (in km)
	 * @param rRup 3D distance to rupture plane (in km)
	 * @param rX distance X (in km)
	 * @param dip of rupture (in degrees)
	 * @param width down-dip rupture width (in km)
	 * @param zTop depth to the top of the rupture (in km)
	 * @param vs30 average shear wave velocity in top 30 m (in m/sec)
	 * @param z1p0 depth to V<sub>s</sub>=1.0 km/sec (in km)
	 * @param style of faulting
	 * @return the ground motion
	 */
	public final ScalarGroundMotion calc(IMT imt, double Mw, double rJB,
			double rRup, double rX, double dip, double width, double zTop, 
			double vs30, double z1p0, FaultStyle style) {

		coeffs.set(imt);
		return calcValues(coeffs, Mw, rJB, rRup, rX, dip, width, zTop,
			vs30, z1p0, style);
	}
	

	
	private DefaultGroundMotion calcValues(Coeffs c, double Mw, double rJB,
			double rRup, double rX, double dip, double width, double zTop,
			double vs30, double z1p0, FaultStyle style) {

		// @formatter:off

		//  ****** Mean ground motion and standard deviation model ****** 
		
		// Base Model (magnitude and distance dependence for strike-slip eq)
		
		// Magnitude dependent taper -- Equation 4.4
		double c4mag = (Mw >= 5) ? c.c4 : 
					   (Mw > 4) ? c.c4 - (c.c4 - 1.0) * (5.0 - Mw) : 1.0;
					   
		// -- Equation 3
		double R = sqrt(rRup * rRup + c4mag * c4mag);
				
		// -- Equation 2
		double M1 = c.a9;
		double M2 = 5.0;
		double M2M1 = M2 - M1;
		double MaxM2 = 8.5 - M2;
		double MaxMw = 8.5 - Mw;
		double MwM2 = Mw - M2;
		double MwM1 = Mw - M1;

		double f1 = c.a1 + c.a17 * R;
		if (Mw > M1) {
			f1 += c.a5 * MwM1 + c.a8 * MaxMw * MaxMw + (c.a2 + c.a3 * MwM1) * log(R);
		} else if (Mw >= M2) {
			f1 += c.a4 * MwM1 + c.a8 * MaxMw * MaxMw + (c.a2 + c.a3 * MwM1) * log(R);
		} else {
			f1 += c.a4 * M2M1 + c.a8 * MaxM2 * MaxM2 + c.a6 * MwM2 + c.a7 * MwM2 * MwM2 + (c.a2 + c.a3 * M2M1) * log(R);
		}

		
		// Aftershock Model (Class1 = mainshock; Class2 = afershock)
		// not currently used as rJBc (the rJB from the centroid of the parent
		// Class1 event) is not defined; requires event type flag -- Equation 7
		// double f11 = 0.0 * c.a14;
		//if (rJBc < 5) {
		//	f11 = c.a14;
		//} else if (rJBc <= 15) {
		//	f11 = c.a14 * (1 - (rJBc - 5.0) / 10.0);
		//}

		// Hanging Wall Model
		double f4 = 0.0;
		if (rX >= 0.0) {
			double T1 = 0.0, T2 = 0.0, T3 = 0.0, T4 = 0.0, T5 = 0.0;
			
			// ....dip taper -- Equation 9
			T1 = (dip > 30.0) ? (90.0 - dip) / 45 : 1.33333333; // 60/45
			
			// ....mag taper -- Equation 10
			double hw_a2 = 0.2; // TODO make constant
			double dM = Mw - 6.5;
			if (Mw >= 6.5) {
				T2 = 1 + hw_a2 * dM;
			} else if (Mw > 5.5) {
				T2 = 1 + hw_a2 * dM + (1 - hw_a2) * dM * dM;
			}
			
			// ....rX taper -- Equation 11
			double r1 = width * cos(dip * TO_RAD);
			double r2 = 3 * r1;
//			System.out.println("r1: " + r1);
//			System.out.println("r2: " + r2);
			double h1 = 0.25, h2 = 1.5, h3 = -0.75; // TODO make constant
			if (rX <= r1) {
				double rXr1 = rX / r1; 
				T3 = h1 + h2 * rXr1 + h3 * rXr1 * rXr1;
			} else if (rX <= r2) {
				T3 = 1-(rX-r1)/(r2-r1);
			}
			
			// ....zTop taper -- Equation 12
			if (zTop <= 10) T4 = 1 - (zTop * zTop) / 100.0;
			
			// ....rX, rY0 taper -- Equation 13 (not currently used)
			//double rY1 = rX * tan(20 * TO_RAD);
			//if (rY0 < rY1) {
			//	T5 = 1;
			//} else if (rY0 < (5 + rY1)) {
			//	T5 = 1 - (rY0 - rY1) / 5;
			//}
			
			// ....rX, non-rY0 taper -- Equation 13a
			if (rJB == 0.0) {
				T5 = 1;
			} else if (rJB < 30.0) {
				T5 = 1 - rJB / 30.0;
			}
			
			// total -- Equation 8
			f4 = c.a13 * T1 * T2 * T3 * T4 * T5;
//			System.out.println(T1 + " " +T2 + " " +T3+ " " +T4 + " " +T5);
		}
		
		// Depth to Rupture Top Model -- Equation 14
		double f6 = c.a15;
		if (zTop < 20.0) f6 *= zTop / 20.0;

		// Style-of-Faulting Model -- Equations 15 & 16
		// (note that f7 always resolves to 0 as a11 are all 0s)
		double f78 = 0.0; // fault-style terms combined
		if (Mw > 5.0) {
			f78 = (style == REVERSE) ? c.a11 : 
				  (style == NORMAL) ? c.a12 : 0.0;
		} else if (Mw >= 4.0) {
			f78 = (style == REVERSE) ? c.a11 * (Mw - 4) : 
				  (style == NORMAL) ? c.a12 * (Mw - 4) : 0.0;
		}
	
		// Soil Depth Model -- Equation 17
		double f10 = calcSoilTerm(c, vs30, z1p0);
		
		// Site Response Model
		double f5 = 0.0;
		double v1 = getV1(c.imt()); // -- Equation 6
		double vs30s = (vs30 < v1) ? vs30 : v1; // -- Equation 5

		// -- Equation 4
		double saRock = 0.0; // calc saRock here if necessary
		if (vs30 < c.Vlin) {
			// soil term (f10) is zero (alternative would be to have supplied
			// rock value (0.0046) which would result in term close to 0
			double f5rk = (c.a10 + c.b * N) * log(vs30s / c.Vlin);
			saRock = f1 + f78 + f5rk + f4 + f6;
			f5 = c.a10 * log(vs30s / c.Vlin) - c.b * log(saRock + C) + c.b *
				log(saRock + C * pow(vs30s / c.Vlin, N));
		} else {
			// saRock loop will end up here because Vlin is always < 1100
			f5 = (c.a10 + c.b * N) * log(vs30s / c.Vlin);
		}

		// Constant Displacement Model (TBD)

		// total model (no aftershock f11)
		double mean = f1 + f78 + f5 + f4 + f6 + f10; 
		
//		System.out.println("f1 " + f1);
//		System.out.println("f78 " + f78);
//		System.out.println("f5 " + f5);
//		System.out.println("f4 " + f4);
//		System.out.println("f6 " + f6);
//		System.out.println("f10 " + f10);
		
		
		// ****** Aleatory uncertainty model ******
		
		// the code below removes unnecessary square-sqrt pairs
		
		// Intra-event term -- Equation 27
		double phiAsq = getPhiA(Mw, c.s1, c.s2);
		phiAsq *= phiAsq;
		
		// Inter-event term -- Equation 28
		double tauBsq = getTauA(Mw, c.s3, c.s4);
		tauBsq *= tauBsq;
		
		// Intra-event term with site amp variability removed -- Equation 23
		// sigAmp^2 = 0.16
		double sigAmpSq = 0.16; // TODO make constant
		double phiBsq = phiAsq - sigAmpSq;
		
		// Parital deriv. of ln(soil amp) w.r.t. ln(SA1100) -- Equation 26
		// saRock subject to same vs30 < Vlin test as in mean model
		double dAmp = get_dAmp(c.b, c.Vlin, vs30, saRock);
		
		// phi squared, with non-linear effects -- Equation 24
		double phiSq = phiBsq * (1.0 + 2.0 * dAmp) + sigAmpSq;

		// tau squared, with non-linear effects -- Equation 25
		double tauSq = tauBsq * (1.0 + 2.0 * dAmp);
		
		// total std dev
		double stdDev = sqrt(phiSq + tauSq);
		
		return new DefaultGroundMotion(mean, stdDev);

	}
	
	private static double getV1(IMT imt) {
		if (imt.equals(PGA)) return 1500.0;
		// no PGV, PGD handling yet
		double T = imt.getPeriod();
		if (T >= 3.0) return 800.0;
		if (T > 0.5) return exp( -0.351 * log(T / 0.5) + log(1500.0));
		return 1500.0;
	}
	
	// Soil depth model adapted from CY13 form
	private static double calcSoilTerm(Coeffs c, double vs30, double z1p0) {
		if (Double.isNaN(z1p0)) return 0.0;
		double vsPow4 = vs30 * vs30 * vs30 * vs30;
		double z1ref = exp(-7.67 / 4.0 * log((vsPow4 + A) / B)) / 1000.0; // km
		double z1c = (vs30 > 500.0) ? c.a46 :
					 (vs30 > 300.0) ? c.a45 :
					 (vs30 > 200.0) ? c.a44 : c.a43;
		return z1c * log((z1p0 + 0.01) / (z1ref + 0.01));
	}
			
	// Aleatory uncertainty model
	private double calcStdDev(Coeffs c, double Mw, double vs30, double saRock) {
		// the code below removes unnecessary square-sqrt pairs
		
		// Intra-event term -- Equation 27
		double phiAsq = getPhiA(Mw, c.s1, c.s2);
		phiAsq *= phiAsq;
		
		// Inter-event term -- Equation 28
		double tauBsq = getTauA(Mw, c.s3, c.s4);
		tauBsq *= tauBsq;
		
		// Intra-event term with site amp variability removed -- Equation 23
		// sigAmp^2 = 0.16
		double sigAmpSq = 0.16; // TODO make constant
		double phiBsq = phiAsq - sigAmpSq;
		
		// Parital deriv. of ln(soil amp) w.r.t. ln(SA1100) -- Equation 26
		double dAmp = get_dAmp(c.b, c.Vlin, vs30, saRock);
		
		// phi squared, with non-linear effects -- Equation 24
		double phiSq = phiBsq * (1.0 + 2.0 * dAmp) + sigAmpSq;

		// tau squared, with non-linear effects -- Equation 25
		double tauSq = tauBsq * (1.0 + 2.0 * dAmp);
		
		// total std dev
		return sqrt(phiSq + tauSq);
	}
	
	private static double getPhiA(double Mw, double s1, double s2) {
		if (Mw < 4.0) return s1;
		if (Mw > 6.0) return s2;
		return s1 + ((s2 - s1) / 2) * (Mw - 4.0);
	}
	
	private static double getTauA(double Mw, double s3, double s4) {
		if (Mw < 5.0) return s3;
		if (Mw > 7.0) return s4;
		return s3 + ((s4 - s3) / 2) * (Mw - 5.0);
	}

	private double get_dAmp(double b, double vLin, double vs30, double saRock) {
		if (vs30 >= vLin) return 0.0;
		return (-b * saRock) / (saRock + C) +
			    (b * saRock) / (saRock + C * pow(vs30 / vLin, N));
	}	
	
	// @formatter:on
	
	
	public static void main(String[] args) {
		ASK_2013 as = new ASK_2013();
		
		ScalarGroundMotion sgm = as.calc(PGA, 6.80, 0.0, 4.6543,5.9626, 27.0, 28.0, 2.1, 760.0, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());


//		ScalarGroundMotion sgm = as.calc(PGA, 7.06, 27.08, 18, 27.08, 45.0, 14.0, 0.0, 760.0, Double.NaN, FaultStyle.NORMAL);
//		ScalarGroundMotion sgm = as.calc(PGA,6.85, 59.75, 59.758, 0.0, 90.0, 6.0, 1.0, 760.0, Double.NaN, FaultStyle.STRIKE_SLIP);
//		System.out.println(sgm.mean());
//		System.out.println(sgm.stdDev());
		
		// PGA,6.85,59.75,59.75836761492067,0.0,90.0,0.0,1.0,1.0,760.0,true,NaN,NaN,Strike-Slip
		
	}

}
