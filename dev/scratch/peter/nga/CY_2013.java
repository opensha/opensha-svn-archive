/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with the Southern California
 * Earthquake Center (SCEC, http://www.scec.org) at the University of Southern
 * California and the UnitedStates Geological Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package scratch.peter.nga;

import static java.lang.Math.*;
import static org.opensha.commons.geo.GeoTools.TO_RAD;
import static scratch.peter.nga.IMT.PGA;
import static scratch.peter.nga.IMT.PGD;
import static scratch.peter.nga.IMT.PGV;
import static scratch.peter.nga.FaultStyle.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * Preliminary implementation of the Chiou & Youngs (2013) next generation
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
public class CY_2013 {

	public static final String NAME = "Chiou & Youngs (2013)";

	// implementation constants
	private static final double A = pow(571, 4);
	private static final double B = pow(1360, 4) + A;

	// author declared constants
	private static final double C2 = 1.06;
	private static final double C4 = -2.1;
	private static final double C4A = -0.5;
	private static final double dC4 = C4A - C4;
	private static final double CRB = 50.0;
	private static final double CRBsq = CRB * CRB;
	private static final double C11 = 0.0;

	public static final String SHORT_NAME = "CY2013";

	private final Coeffs coeffs;

	private class Coeffs extends Coefficients {

		// TODO inline constance coeffs with final statics

		double c1, c1a, c1b, c1c, c1d, c3, c5, c6, c7, c7b, c8b, c9, c9a, c9b,
				c11b, cn, cM, cHM, cgamma1, cgamma2, cgamma3, phi1, phi2, phi3,
				phi4, phi5, phi6, tau1, tau2, sigma1, sigma2, sigma3;

		Coeffs() {
			super("CY13.csv");
			set(PGA);
		}
	}

	/**
	 * Constructs a new instance of this attenuation relationship.
	 */
	public CY_2013() {
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
	 * @param zTop depth to the top of the rupture (in km)
	 * @param vs30 average shear wave velocity in top 30 m (in m/sec)
	 * @param vsInferred whether vs30 is an inferred or measured value
	 * @param z1p0 depth to V<sub>s</sub>=1.0 km/sec (in km)
	 * @param style of faulting
	 * @return the ground motion
	 */
	public final ScalarGroundMotion calc(IMT imt, double Mw, double rJB,
			double rRup, double rX, double dip, double zTop, double vs30,
			boolean vsInferred, double z1p0, FaultStyle style) {

		coeffs.set(imt);
		
		// terms used by both mean and stdDev
		double lnSAref = calcLnSAref(coeffs, Mw, rJB, rRup, rX, dip, zTop, style);
		double soilNonLin = calcSoilNonLin(coeffs, vs30);
		
		double mean = calcMean(coeffs, vs30, z1p0, soilNonLin, lnSAref);
		double stdDev = calcStdDev(coeffs, Mw, vsInferred, soilNonLin, lnSAref);

		return new DefaultGroundMotion(mean, stdDev);
	}

	// Seismic Source Scaling -- Equation 3.7
	private double calcLnSAref(Coeffs c, double Mw, double rJB, double rRup,
			double rX, double dip, double zTop, FaultStyle style) {
		
		// Magnitude scaling
		double r1 = c.c1 + C2 * (Mw - 6.0) + ((C2 - c.c3) / c.cn) *
			log(1.0 + exp(c.cn * (c.cM - Mw)));

		// Near-field magnitude and distance scaling
		double r2 = C4 * log(rRup + c.c5 * cosh(c.c6 * max(Mw - c.cHM, 0.0)));

		// Far-field distance scaling
		double gamma = (c.cgamma1 + c.cgamma2 / cosh(max(Mw - c.cgamma3, 0.0)));
		double r3 = dC4 * log(sqrt(rRup * rRup + CRBsq)) + rRup * gamma;

		// Scaling with other source variables
		double coshM = cosh(2 * max(Mw - 4.5, 0));
		double cosDelta = cos(dip * TO_RAD);
		// Center zTop on the zTop-M relation in Eqns (2.4) & (2.5)
		double deltaZtop = zTop - calcMwZtop(style, Mw);
		double r4 = (c.c7 + c.c7b / coshM) * deltaZtop + 
				    (C11 + c.c11b / coshM) * cosDelta * cosDelta;
		r4 += (style == REVERSE) ? (c.c1a + c.c1c / coshM) : 
			  (style == NORMAL) ? (c.c1b + c.c1d / coshM) : 0.0; 

		// Hanging-wall effect
		double r5 = 0.0;
		if (rX >= 0.0) {
			r5 = c.c9 * cos(dip * TO_RAD) *
				(c.c9a + (1.0 - c.c9a) * tanh(rX / c.c9b)) *
				(1 - sqrt(rJB * rJB + zTop * zTop) / (rRup + 1.0));
		}

		// Directivity effect (not implemented)
		// cDPP = centered DPP (direct point directivity parameter)
		//double c8 = 2.154;
		//double c8a = 0.2695;
		//double Mc8 = Mw-c.c8b;
		//double r6 = c8 * exp(-c8a * Mc8 * Mc8) *
		//	max(0.0, 1.0 - max(0, rRup - 40.0) / 30.0) *
		//	min(max(0, Mw - 5.5) / 0.8, 1.0) * cDPP;

		return r1 + r2 + r3 + r4 + r5;
	}
	
	private double calcSoilNonLin(Coeffs c, double vs30) {
		double exp1 = exp(c.phi3 * (min(vs30, 1130.0) - 360.0));
		double exp2 = exp(c.phi3 * (1130.0 - 360.0));
		return c.phi2 * (exp1 - exp2);
	}

	// Mean ground motion model -- Equation 3.8
	private double calcMean(Coeffs c, double vs30, double z1p0, double snl, 
			double lnSAref) {

		// Soil effect: linear response
		double sl = c.phi1 * min(log(vs30 / 1130.0), 0.0);

		// Soil effect: nonlinear response (base passed in)
		snl *= log((exp(lnSAref) + c.phi4) / c.phi4);

		// Soil effect: sediment thickness
		double dZ1 = calcDeltaZ1(z1p0, vs30);
		double rkdepth = c.phi5 * (1.0 - exp(-dZ1 / c.phi6));

		// total model
		return lnSAref + sl + snl + rkdepth;
	}

	// Center zTop on the zTop-M relation in Eqns (2.4) & (2.5)
	private static double calcMwZtop(FaultStyle style, double Mw) {
		double mzTop = 0.0;
		if (style == REVERSE) {
			mzTop = (Mw <= 5.849) ? 2.704 : max(2.704 - 1.226 * (Mw - 5.849), 0);
		} else {
			mzTop = (Mw <= 4.970) ? 2.673 : max(2.673 - 1.136 * (Mw - 4.970), 0);
		}
		return mzTop * mzTop;
	}
	
	private static double calcDeltaZ1(double z1p0, double vs30) {
		if (Double.isNaN(z1p0)) return 0.0;
		double vsPow4 = vs30 * vs30 * vs30 * vs30;
		return z1p0 * 1000.0 - exp(-7.15 / 4 * log((vsPow4 + A) / B));
	}

	// Aleatory uncertainty model -- Equation 3.9
	private double calcStdDev(Coeffs c, double Mw, boolean vsInferred,
			double snl, double lnSAref) {

		double SAref = exp(lnSAref);

		// Response Term - linear vs. non-linear
		double NL0 = snl * SAref / (SAref + c.phi4);

		// Magnitude thresholds
		double mTest = min(max(Mw, 5.0), 6.5) - 5.0;

		// Inter-event Term
		double tau = c.tau1 + (c.tau2 - c.tau1) / 1.5 * mTest;

		// Intra-event term
		double sigmaNL0 = c.sigma1 + (c.sigma2 - c.sigma1) / 1.5 * mTest;
		double vsTerm = vsInferred ? c.sigma3 : 0.7;
		double NL0sq = (1 + NL0) * (1 + NL0);
		sigmaNL0 *= sqrt(vsTerm + NL0sq);

		return sqrt(tau * tau * NL0sq + sigmaNL0 * sigmaNL0);
	}
	
	public Collection<IMT> getSupportedIMTs() {
		return coeffs.getSupportedIMTs();
	}

	public static void main(String[] args) {
		CY_2013 cy = new CY_2013();

		System.out.println("PGA");
		ScalarGroundMotion sgm = cy.calc(PGA, 6.80, 0.0, 4.629, 5.963, 27.0, 2.1, 760.0, true, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());
		System.out.println("5Hz");
		sgm = cy.calc(IMT.SA0P2, 6.80, 0.0, 4.629, 5.963, 27.0, 2.1, 760.0, true, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());
		System.out.println("1Hz");
		sgm = cy.calc(IMT.SA1P0, 6.80, 0.0, 4.629, 5.963, 27.0, 2.1, 760.0, true, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());

//		Set<IMT> IMTs = EnumSet.complementOf(EnumSet.of(PGV, PGD, IMT.SA0P01)); 
//		for (IMT imt : IMTs) {
////			ScalarGroundMotion sgm = cy.calc(imt, 7.06, 27.08, 27.08, 27.08, 90.0, 0.0, 760.0, true, Double.NaN, FaultStyle.STRIKE_SLIP);
//			ScalarGroundMotion sgm = cy.calc(imt, 7.5, 8.5, 10, 10, 70.0, 0.0, 760.0, true, Double.NaN, FaultStyle.STRIKE_SLIP);
//			System.out.println(String.format("%s\t%.4f\t%.4f", imt, sgm.mean(), sgm.stdDev()));
//		}
		
	}

}
