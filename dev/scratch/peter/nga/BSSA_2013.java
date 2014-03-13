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
import static scratch.peter.nga.IMT.PGV;
import static java.lang.Math.*;
import static scratch.peter.nga.FaultStyle.*;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

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

	public static final String NAME = "Boore, Stewart, Seyhan \u0026 Atkinson (2014)";

	// implementation constants
	private static final double A = pow(570.94, 4);
	private static final double B = pow(1360, 4) + A;
	private static final double M_REF = 4.5;
	private static final double R_REF = 1.0;
	private static final double DC3_CA_TW = 0.0;
	private static final double V_REF = 760.0;
	private static final double F1 = 0.0;
	private static final double F3 = 0.1;
	private static final double V1 = 225;
	private static final double V2 = 300;

	public static final String SHORT_NAME = "BSSA2013";

	private final Coeffs coeffs;
	private final Coeffs coeffsPGA;

	private static class Coeffs extends Coefficients {

		double e0, e1, e2, e3, e4, e5, e6, Mh, c1, c2, c3, h, c, Vc, f4, f5,
		f6, f7, R1, R2, dPhiR, dPhiV, phi1, phi2, tau1, tau2;

		// same for all periods; replaced with constant
		double Mref, Rref, Dc3CaTw, Vref, f1, f3, v1, v2;
		// unused regional coeffs
		double Dc3CnTr, Dc3ItJp;

		Coeffs() {
			super("BSSA14.csv");
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
	private static final double calcMean(Coeffs c, double Mw, double rJB,
			double vs30, double z1p0, FaultStyle style, double pgaRock) {

		// Source/Event Term -- Equation 2
		double Fe = calcSourceTerm(c, Mw, style);
		
		// Path Term -- Equations 3, 4
		double R = sqrt(rJB * rJB + c.h * c.h);
		double Fp = calcPathTerm(c, Mw, R);

		// Site Linear Term -- Equation 6
		double vsLin = (vs30 <= c.Vc) ? vs30 : c.Vc;
		double lnFlin = c.c * log(vsLin / V_REF);
		
		// Site Nonlinear Term -- Equations 7, 8
		double f2 = c.f4 * (exp(c.f5 * (min(vs30, 760.0) - 360.0)) - 
				exp(c.f5 * (760.0 - 360.0)));
		double lnFnl = F1 + f2 * log((pgaRock + F3) / F3);

		// Basin depth term -- Equations 9, 10 , 11
		double DZ1 = calcDeltaZ1(z1p0, vs30);
		double Fdz1 = (c.imt().isSA() && c.imt().getPeriod() >= 0.65) ?
			(DZ1 <= c.f7 / c.f6) ? c.f6 * DZ1 : c.f7
				: 0.0;
		
		// Total site term -- Equation 5
		double Fs = lnFlin + lnFnl + Fdz1;

		// Total model -- Equation 1
		return Fe + Fp + Fs;
	}
	
	// Median PGA for ref rock (Vs30=760m/s); always called with PGA coeffs
	private static final double calcPGArock(Coeffs c, double Mw, double rJB,
			FaultStyle style) {
		
		// Source/Event Term -- Equation 2
		double FePGA = calcSourceTerm(c, Mw, style);
		
		// Path Term -- Equation 3
		double R = sqrt(rJB * rJB + c.h * c.h);
		double FpPGA = calcPathTerm(c, Mw, R);

		// No Site term -- [Vs30rk==760] < [Vc(PGA)=1500] && 
		// ln(Vs30rk / V_REF) = ln(760/760) = 0

		// Total PGA model -- Equation 1
		return exp(FePGA + FpPGA);
	}

	// Source/Event Term -- Equation 2
	private static final double calcSourceTerm(Coeffs c, double Mw,
			FaultStyle style) {
		double Fe = (style == STRIKE_SLIP) ? c.e1 :
					(style == REVERSE) ? c.e3 :
					(style == NORMAL) ? c.e2 : c.e0; // else UNKNOWN
		double MwMh = Mw - c.Mh;
		Fe += (Mw <= c.Mh) ? c.e4 * MwMh + c.e5 * MwMh * MwMh : c.e6 * MwMh;
		return Fe;
	}
	
	// Path Term, base model -- Equation 3
	private static final double calcPathTerm(Coeffs c, double Mw, double R) {
		return (c.c1 + c.c2 * (Mw - M_REF)) * log(R / R_REF) +
			(c.c3 + DC3_CA_TW) * (R - R_REF);
	}
	
	// Calculate delta Z1 in km as a  function of vs30 and using the default 
	// model of ChiouYoungs_2013 -- Equations 10, 11
	private static final double calcDeltaZ1(double z1p0, double vs30) {
		if (Double.isNaN(z1p0)) return 0.0;
		double vsPow4 = vs30 * vs30 * vs30 * vs30;
		return z1p0 - exp(-7.15 / 4.0 * log((vsPow4 + A) / B)) / 1000.0;
	}

	// Aleatory uncertainty model
	private static final double calcStdDev(Coeffs c, double Mw, double rJB,
			double vs30) {

		// Inter-event Term -- Equation 14
		double tau = (Mw >= 5.5) ? c.tau2 : (Mw <= 4.5) ? c.tau1 : c.tau1 +
			(c.tau2 - c.tau1) * (Mw - 4.5);
		
		// Intra-event Term -- Equations 15, 16, 17
		double phiM = (Mw >= 5.5) ? c.phi2 : (Mw <= 4.5) ? c.phi1
			: c.phi1 + (c.phi2 - c.phi1) * (Mw - 4.5);
		
		double phiMR = phiM;
		if (rJB > c.R2) {
			phiMR += c.dPhiR;
		} else if (rJB > c.R1) {
			phiMR += c.dPhiR * (log(rJB / c.R1) / log(c.R2 / c.R1));
		}
		
		double phiMRV = phiMR;
		if (vs30 <= V1) {
			phiMRV -= c.dPhiV;
		} else if (vs30 < V2) {
			phiMRV -= c.dPhiV * (log(V2 / vs30) / log(V2 / V1));
		}

		// Total model -- Equation 13
		return sqrt(phiMRV * phiMRV + tau * tau);
	}
	
	public Collection<IMT> getSupportedIMTs() {
		List<IMT> imts = Lists.newArrayList();
		imts.addAll(coeffs.getSupportedIMTs());
		imts.addAll(coeffsPGA.getSupportedIMTs());
		return imts;
	}

	public static void main(String[] args) {
		BSSA_2013 bssa = new BSSA_2013();
		
//		GMM_Input in = GMM_Input.create(7.0, 5.0, 5.1, 5.0, 90.0, 15.0, 6.0, 8.0, 0.0, 270, false, 1.983, 0.479);
//		ScalarGroundMotion sgm;

		System.out.println("PGA");
		ScalarGroundMotion sgm = bssa.calc(PGA, 7.0, 5.0, 270.0, 0.479, FaultStyle.STRIKE_SLIP);
		System.out.println(Math.exp(sgm.mean()));
		System.out.println(sgm.stdDev());
		System.out.println("5Hz");
		sgm = bssa.calc(IMT.SA0P2, 7.0, 5.0, 270.0, 0.479, FaultStyle.STRIKE_SLIP);
		System.out.println(Math.exp(sgm.mean()));
		System.out.println(sgm.stdDev());
		System.out.println("1Hz");
		sgm = bssa.calc(IMT.SA1P0, 7.0, 5.0, 270.0, 0.479, FaultStyle.STRIKE_SLIP);
		System.out.println(Math.exp(sgm.mean()));
		System.out.println(sgm.stdDev());

//		ScalarGroundMotion sgm = bssa.calc(IMT.SA0P2, 6.06, 27.08, 760.0, Double.NaN, FaultStyle.REVERSE);
//		System.out.println(sgm.mean());
//		System.out.println(sgm.stdDev());
		
//		BSSA2013May(0,7.06,27.08,0,0,1,0,760,-999)
	}

}
