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

import static scratch.peter.nga.IMT.PGA;
import static java.lang.Math.*;
import static scratch.peter.nga.FaultStyle.*;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * Preliminary implementation of the Boore, Stewart, Seyhan, &amp; Atkinson
 * (2013) next generation attenuation relationship developed as part of NGA West
 * II.
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
public class BSSA_2013 {

	public static final String NAME = "Boore et al. (2013)";

	// implementation constants
	private static final double A = pow(570.94, 4);
	private static final double B = pow(1360, 4) + A;

	private final Coeffs coeffs;
	private final Coeffs coeffsPGA;

	private static class Coeffs extends Coefficients {

		double e0, e1, e2, e3, e4, e5, e6, Mh, c1, c2, c3, Mref, Rref, h,
				Dc3CaTw, Dc3CnTr, Dc3ItJp, c, Vc, Vref, f1, f3, f4, f5, f6, f7,
				R1, R2, dPhiR, dPhiV, V1, V2, phi1, phi2, tau1, tau2;

		Coeffs() {
			super("BSSA13.csv");
			set(PGA);
		}
	}

	/**
	 * Constructs a new instance of this attenuation relationship.
	 */
	public BSSA_2013() {
		coeffs = new Coeffs();
		coeffsPGA = new Coeffs();
	}

	// TODO limit supplied z1p0 to 0-3 km
	
	/**
	 * Returns the ground motion for the supplied arguments.
	 * @param imt intensity measure type
	 * @param Mw moment magnitude
	 * @param rJB Joyner-Boore distance to rupture (in km)
	 * @param vs30 average shear wave velocity in top 30 m (in m/sec)
	 * @param z1p0 depth to V<sub>s</sub>=1.0 km/sec (in km)
	 * @param style of faulting
	 * @return the ground motion
	 */
	public final ScalarGroundMotion calc(IMT imt, double Mw, double rJB,
			double vs30, double z1p0, FaultStyle style) {

		coeffs.set(imt);
		double pgaRock = calcPGArock(coeffsPGA, Mw, rJB, style);
		double mean = calcMean(coeffs, Mw, rJB, vs30, z1p0, style, pgaRock);
		double stdDev = calcStdDev(coeffs, Mw, rJB, vs30);

		return new DefaultGroundMotion(mean, stdDev);
	}

	// Mean ground motion model
	private double calcMean(Coeffs c, double Mw, double rJB, double vs30,
			double z1p0, FaultStyle style, double pgaRock) {

		// Source/Event Term -- Equation 3.5
		double Fe = calcSourceTerm(c, Mw, style);
		
		// Path Term
		double R = sqrt(rJB * rJB + c.h * c.h); // -- Equation 3.4
		// Base model -- Equation 3.3
		double Fpb = calcPathTerm(c, Mw, R);
		// Adjusted path term -- Equation 3.7
		double Fp = Fpb + c.Dc3CaTw * (R - c.Rref);

		// Site Linear Term
		double vsLin = (vs30 <= c.Vc) ? vs30 : c.Vc;
		double lnFlin = c.c * log(vsLin / c.Vref); // -- Equation 3.9
		
		// Site Nonlinear Term
		// -- Equation 3.11
		double f2 = c.f4 * (exp(c.f5 * (min(vs30, 760.0) - 360.0)) - 
				exp(c.f5 * (760.0 - 360.0)));
		// -- Equation 3.10
		double lnFnl = c.f1 + f2 * log((pgaRock + c.f3) / c.f3);
		// Base model -- Equation 3.8
		double Fsb = lnFlin + lnFnl;

		// Basin depth term -- Equations 4.9 and 4.10
		double DZ1 = calcDeltaZ1(z1p0, vs30);
		double Fz1 = 0.0;
		// ignore at short periods and PGV, PGD
		// (must test for PGA first and fall through to period check
		if (c.imt().equals(PGA) || 
				(c.imt().getPeriod() != null && c.imt().getPeriod() >= 0.65)) {
			// -- Equation 3.13
			Fz1 = (DZ1 <= c.f7 / c.f6) ? c.f6 * DZ1 : c.f7;
		}
		double Fs = Fsb + Fz1;

		// Total model
		return Fe + Fp + Fs;
	}
	
	// Median PGA for ref rock (Vs30=760m/s); always called with PGA coeffs
	private double calcPGArock(Coeffs c, double Mw, double rJB, FaultStyle style) {
		
		// Source/Event Term
		double FePGA = calcSourceTerm(c, Mw, style);
		
		// Path Term
		double R = sqrt(rJB * rJB + c.h * c.h); // -- Equation 3.4
		// Base model -- Equation 3.3
		double FpbPGA = calcPathTerm(c, Mw, R);

		// -- Equation 3.9
		double Vs30rk = 760; // TODO make constant
		double vsPGArk = (Vs30rk <= c.Vc) ? Vs30rk : c.Vc;
		double FsPGA = c.c*log(vsPGArk/c.Vref);

		// Total model -- Equations 3.1 & 3.6
		return exp(FePGA + FpbPGA + FsPGA);
	}

	// Source/Event Term
	private double calcSourceTerm(Coeffs c, double Mw, FaultStyle style) {
		double Fe = (style == STRIKE_SLIP) ? c.e1 :
					(style == REVERSE) ? c.e3 :
					(style == NORMAL) ? c.e2 : c.e0; // else UNKNOWN
		double MwMh = Mw - c.Mh;
		// -- Equation 3.5a : Equation 3.5b
		Fe += (Mw <= c.Mh) ? c.e4 * MwMh + c.e5 * MwMh * MwMh : c.e6 * MwMh;
		return Fe;
	}
	
	// Path Term, base model -- Equation 3.3
	private double calcPathTerm(Coeffs c, double Mw, double R) {
		return (c.c1 + c.c2 * (Mw - c.Mref)) * log(R / c.Rref) + c.c3 *
			(R - c.Rref);
	}
	
	// Calculate delta Z1 in km as a  function of vs30 and using the default 
	// model of CY_2013
	private static double calcDeltaZ1(double z1p0, double vs30) {
		if (Double.isNaN(z1p0)) return 0.0;
		double vsPow4 = vs30 * vs30 * vs30 * vs30;
		//  -- Equations 4.9a and 4.10
		return z1p0 - exp(-7.15 / 4 * log((vsPow4 + A) / B)) / 1000.0;
	}

	// Aleatory uncertainty model
	private double calcStdDev(Coeffs c, double Mw, double rJB, double vs30) {

		// Inter-event Term -- Equation 4.11
		// (reordered, most Mw will be > 5.5)
		double tau = (Mw >= 5.5) ? c.tau2 : (Mw <= 4.5) ? c.tau1 : c.tau1 +
			(c.tau2 - c.tau1) * (Mw - 4.5);
		
		// Intra-event Term
		
		//  -- Equation 4.12
		double phiM = (Mw >= 5.5) ? c.phi2 : (Mw <= 4.5) ? c.phi1
			: c.phi1 + (c.phi2 - c.phi1) * (Mw - 4.5);
		
		//  -- Equation 4.13
		double phiMR = phiM;
		if (rJB > c.R2) {
			phiMR += c.dPhiR;
		} else if (rJB > c.R2) {
			phiMR += c.dPhiR * (log(rJB / c.R1) / log(c.R2 / c.R1));
		}
		
		//  -- Equation 4.14
		double V1 = 225; // TODO make constant
		double V2 = 300;
		double phiMRV = 0.0;
		if (vs30 >= V2) {
		    phiMRV = phiMR;
		} else if (vs30 >= V1) {
			phiMRV = phiMR - c.dPhiV * (log(V2 / vs30) / log(V2 / V1));
		} else {
		    phiMRV = phiMR - c.dPhiV;
		}

		// Total model -- Equation 3.2
		return sqrt(phiMRV * phiMRV + tau * tau);
	}	

	public static void main(String[] args) {
		BSSA_2013 bssa = new BSSA_2013();
		
		ScalarGroundMotion sgm = bssa.calc(PGA, 6.80, 0.0, 760.0, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());

//		ScalarGroundMotion sgm = bssa.calc(IMT.SA0P2, 6.06, 27.08, 760.0, Double.NaN, FaultStyle.REVERSE);
//		System.out.println(sgm.mean());
//		System.out.println(sgm.stdDev());
		
//		BSSA2013May(0,7.06,27.08,0,0,1,0,760,-999)
	}

}
