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
import static scratch.peter.nga.IMT.*;
import static scratch.peter.nga.FaultStyle.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * Preliminary implementation of the Campbell & Bozorgnia (2013) next generation attenuation
 * relationship developed as part of NGA West II.
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
public class CB_2013 {

	public static final String NAME = "Campbell & Bozorgnia (2013)";
	
	private final Coeffs coeffs;
	private final Coeffs coeffsPGA;
	
	private static final Set<IMT> SHORT_PERIODS = EnumSet.range(SA0P01, SA0P25);
	
	private static class Coeffs extends Coefficients {
		
		// TODO inline constance coeffs with final statics
		
		double c0, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10,
				c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, a2, h1, h2,
				h3, h4, h5, h6, k1, k2, k3, c, n, phi_lo, phi_hi, tau_lo,
				tau_hi, phi_lnaf, sigma_c, rho;
		
		// fixed PGA values used by all periods when calculating stdDev
		double tau_hi_PGA, tau_lo_PGA, phi_hi_PGA, phi_lo_PGA, phi_lnaf_PGA;

		Coeffs() {
			super("CB13.csv");
			set(PGA);
			tau_hi_PGA = get(PGA, "tau_hi");
			tau_lo_PGA = get(PGA, "tau_lo");
			phi_hi_PGA = get(PGA, "phi_hi");
			phi_lo_PGA = get(PGA, "phi_lo");
			phi_lnaf_PGA = get(PGA, "phi_lnaf");
		}
	}
	
	/**
	 * Constructs a new instance of this attenuation relationship.
	 */
	public CB_2013() {
		coeffs = new Coeffs();
		coeffsPGA = new Coeffs();
	}

	// currently unexposed japan flag
	private final double JP = 0;
	
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
	 * @param zHyp hypocentral depth (in km)
	 * @param vs30 average shear wave velocity in top 30 m (in m/sec)
	 * @param z2p5 depth to V<sub>s</sub>=2.5 km/sec
	 * @param style of faulting
	 * @return the ground motion
	 */
	public final ScalarGroundMotion calc(IMT imt, double Mw, double rJB,
			double rRup, double rX, double dip, double width, double zTop,
			double zHyp, double vs30, double z2p5, FaultStyle style) {
		
		coeffs.set(imt);

		// calc pga rock reference value using CA vs30 z2p5 value: 0.398
		double pgaRock = (vs30 < coeffs.k1) ? exp(calcMean(coeffsPGA, Mw, rJB,
			rRup, rX, dip, width, zTop, zHyp, 1100.0, 0.398, style, 0.0))
			: 0.0;

		double mean = calcMean(coeffs, Mw, rJB, rRup, rX, dip, width, zTop,
			zHyp, vs30, z2p5, style, pgaRock);
		
		// prevent SA<PGA for short periods
		if (SHORT_PERIODS.contains(imt)) {
			double pgaMean = calcMean(coeffsPGA, Mw, rJB, rRup, rX, dip,
				width, zTop, zHyp, vs30, z2p5, style, pgaRock);
			mean = max(mean, pgaMean);
		}
		
		double stdDev = calcStdDev(coeffs, Mw, vs30, pgaRock);

		return new DefaultGroundMotion(mean, stdDev);
	}
	
	// Mean ground motion model
	private double calcMean(Coeffs c, double Mw, double rJB, double rRup,
			double rX, double dip, double width, double zTop, double zHyp,
			double vs30, double z2p5, FaultStyle style, double pgaRock) {
		
		// @formatter:off

		double Fmag, Fr, Fflt, Fhw, Fhyp, Fdip, Fsed, Fatn, Fsite;
		
		// Magnitude term -- Equation 2
		if (Mw <= 4.5) {
			Fmag = c.c0 + c.c1 * Mw;
		} else if (Mw <= 5.5) {
			Fmag = c.c0 + c.c1 * Mw + c.c2 * (Mw - 4.5);
		} else if (Mw <= 6.5) {
			Fmag = c.c0 + c.c1 * Mw + c.c2 * (Mw - 4.5) + c.c3 * (Mw - 5.5);
		} else {
			Fmag = c.c0 + c.c1 * Mw + c.c2 * (Mw - 4.5) + c.c3 * (Mw - 5.5) + c.c4 * (Mw - 6.5);
		}

		// Distance term -- Equation 3
		double r = sqrt(rRup * rRup + c.c7 * c.c7);
		Fr = (c.c5 + c.c6 * Mw) * log(r);
	    
		// Style-of-faulting term -- Equations 5 & 6
		double Ff_F = (style == REVERSE) ? c.c8 : (style == NORMAL) ? c.c9 : 0.0;
		if (Mw <= 4.5) {
			Fflt = 0;
		} else if (Mw <= 5.5) {
			Fflt = (Mw - 4.5) * Ff_F;
		} else {
			Fflt = Ff_F;
		}
		
		// Hanging-wall term
		// Jennifer Donahue's HW Model plus CB08 distance taper
		//  -- Equations 9, 10, 11 & 12
		double r1 = width * cos(dip * TO_RAD);
		double r2 = 62.0 * Mw - 350.0;
		double rXr1 = rX / r1;
		double rXr2r1 = (rX - r1) / (r2 - r1);
		double f1_Rx = c.h1 + c.h2 * rXr1 + c.h3 * (rXr1 * rXr1);
		double f2_Rx = c.h4 + c.h5 * (rXr2r1) + c.h6 * rXr2r1 * rXr2r1;
		double Fhw_rRup, Fhw_r, Fhw_m, Fhw_z, Fhw_d;
		
		// CB08 distance taper -- Equation 13
		Fhw_rRup = (rRup == 0.0) ? 1.0 : (rRup - rJB) / rRup;

		// .....distance -- Equation 8
		if (rX < 0.0) {
			Fhw_r = 0.0;
		} else if (rX < r1) {
			Fhw_r = f1_Rx;
		} else {
			Fhw_r = max(f2_Rx, 0.0);
		}
		
		// .....magnitude -- Equation 14
		if (Mw <= 5.5) {
			Fhw_m = 0.0;
		} else if (Mw <= 6.5) {
			Fhw_m = (Mw - 5.5) * (1 + c.a2 * (Mw - 6.5));
		} else {
			Fhw_m = 1.0 + c.a2 * (Mw - 6.5);
		}

		// .....rupture depth -- Equation 15
		Fhw_z = (zTop > 16.66) ? 0.0 : 1.0 - 0.06 * zTop;

		// .....dip -- Equation 16
		Fhw_d = (90.0 - dip) / 45.0;

		// .....total -- Equation 7
		Fhw = c.c10 * Fhw_rRup * Fhw_r * Fhw_m * Fhw_z * Fhw_d;

		
		// update z2p5 with CA model if not supplied -- Eqn 6.9
		if (Double.isNaN(z2p5)) z2p5 = exp(7.089 - 1.144 * log(vs30));
		// Shallow sediment depth and 3-D basin term -- Equation 20
		if (z2p5 <= 1.0) {
			Fsed = (c.c14 + c.c15 * JP) * (z2p5 - 1.0);
		} else if (z2p5 <= 3.0) {
			Fsed = 0.0 * c.c16;
		} else {
			Fsed = c.c16 * c.k3 * exp(-0.75) * (1.0 - exp(-0.25 * (z2p5 - 3.0)));
		}

		// Hypo depth term -- Equations 21, 22 & 23
		Fhyp = (zHyp <= 7.0) ? 0.0 : (zHyp <= 20.0) ? zHyp - 7.0 : 13.0;

		if (Mw <= 5.5) {
			Fhyp *= c.c17;
		} else if (Mw <= 6.5) {
			Fhyp *= (c.c17 + (c.c18 - c.c17) * (Mw - 5.5));
		} else {
			Fhyp *= c.c18;
		}

		// Dip term -- Equation 24
		if (Mw <= 4.5) {
			Fdip = c.c19 * dip;
		} else if (Mw <= 5.5) {
			Fdip = c.c19 * (5.5 - Mw) * dip;
		} else {
			Fdip = 0.0;
		}

		// Anelastic attenuation term -- Equation 25
		Fatn = (rRup > 80.0) ? c.c20 * (rRup-80.0) : 0.0;
		
		// Shallow site response - pgaRock term is computed through an initial
		// call to this method with vs30=1100; 1100 is higher than any k1 value
		// so else condition always prevails -- Equation 18
		double vsk1 = vs30 / c.k1;
		if (vs30 <= c.k1) {
			Fsite = c.c11 * log(vsk1) +
				c.k2 * (log(pgaRock + c.c * pow(vsk1, c.n)) - 
						log(pgaRock + c.c));
		} else {
			Fsite = (c.c11 + c.k2 * c.n) * log(vsk1);
		}
		
		// NOTE Japan ignored  -- Equation 19

//		System.out.println(pgaRock);
//		System.out.println(" Fmag: " + Fmag);
//		System.out.println("   Fr: " + Fr);
//		System.out.println(" Fflt: " + Fflt);
//		System.out.println("  Fhw: " + Fhw);
//		System.out.println("Fsite: " + Fsite);
//		System.out.println(" Fsed: " + Fsed);
//		System.out.println(" Fhyp: " + Fhyp);
//		System.out.println(" Fdip: " + Fdip);
//		System.out.println(" Fatn: " + Fatn);
//
		// total model -- Equation 1
		return Fmag + Fr + Fflt + Fhw + Fsite + Fsed + Fhyp + Fdip + Fatn;
		// @formatter:on
	}

	// Aleatory uncertainty model
	private double calcStdDev(Coeffs c, double Mw, double vs30, double pgaRock) {

		// @formatter:off

		//  -- Equation 31
		double vsk1 = vs30 / c.k1;
		double alpha = 0.0;
		if (vs30 < c.k1) {
			alpha = c.k2 * pgaRock * (1 / (pgaRock + c.c * pow(vsk1, c.n))
					- 1 / (pgaRock + c.c));
		}
		
		// Magnitude dependence -- Equations 29 & 30
		double tau_lnYB, tau_lnPGAB, phi_lnY, phi_lnPGAB;
		if (Mw <= 4.5) {
			tau_lnYB = c.tau_lo;
			phi_lnY = c.phi_lo;
			tau_lnPGAB = c.tau_lo_PGA;
			phi_lnPGAB = c.phi_lo_PGA;
		} else if (Mw < 5.5) {
			tau_lnYB = stdMagDep(c.tau_lo, c.tau_hi, Mw);
			phi_lnY = stdMagDep(c.phi_lo, c.phi_hi, Mw);
			tau_lnPGAB = stdMagDep(c.tau_lo_PGA, c.tau_hi_PGA, Mw);
			phi_lnPGAB = stdMagDep(c.phi_lo_PGA, c.phi_hi_PGA, Mw);
		} else {
			tau_lnYB = c.tau_hi;
			phi_lnY = c.phi_hi;
			tau_lnPGAB = c.tau_hi_PGA;
			phi_lnPGAB = c.phi_hi_PGA;
		}
		
		// intra-event std dev -- Equation 27
		double alphaTau = alpha * tau_lnPGAB;
		double tau = sqrt(tau_lnYB * tau_lnYB + alphaTau * alphaTau + 
			2.0 * alpha * c.rho * tau_lnYB * tau_lnPGAB);
		
		// inter-event std dev -- Equation 28
		double phi_lnYB = sqrt(phi_lnY * phi_lnY - c.phi_lnaf * c.phi_lnaf);
		phi_lnPGAB = sqrt(phi_lnPGAB * phi_lnPGAB - c.phi_lnaf_PGA * c.phi_lnaf_PGA);
		double alphaPhi = alpha * phi_lnPGAB;

		double phi = sqrt(phi_lnY * phi_lnY + alphaPhi * alphaPhi +
			2.0 * alpha * c.rho * phi_lnYB * phi_lnPGAB);
		
		// total model -- Equation 32
		return sqrt(phi * phi + tau * tau);
		// @formatter:on
	}

	private static double stdMagDep(double lo, double hi, double Mw) {
		return hi + (lo - hi) * (5.5 - Mw);
	}
	
	public Collection<IMT> getSupportedIMTs() {
		List<IMT> imts = Lists.newArrayList();
		imts.addAll(coeffs.getSupportedIMTs());
		imts.addAll(coeffsPGA.getSupportedIMTs());
		return imts;
	}
	
	public static void main(String[] args) {

		CB_2013 cb = new CB_2013();
//		ScalarGroundMotion sgm = cb.calc(PGA, 7.06, 27.08, 22, 27.08, 60.0, 14.0, 0.0, 5, 760.0, Double.NaN, FaultStyle.NORMAL);
//		System.out.println(sgm.mean());
//		System.out.println(sgm.stdDev());
		//6.5600004       0.0000000       4.6288643       5.9628415       27.000000       28.000000       2.0999999       10.100000       760.00000      0.60682398       1.0000000       0.0
		
		// FORT params 6.80	0.000	4.629	5.963	27.0	28.0	2.100	10.100	760.0	0.6068	1	0
		ScalarGroundMotion sgm = cb.calc(PGA, 6.80, 0.0, 4.629, 5.963, 27.0, 28.0, 2.1, 10.1, 760.0, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());
		
		sgm = cb.calc(PGA, 6.56, 0.0, 4.6288643, 5.9628415, 27.0, 28.0, 2.1, 8.455866996353654, 760.0, Double.NaN, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
//		System.out.println(sgm.stdDev());
		
//		         T0,M,Rrup,Rjb,Rx,FRV,FNM,ZTOR,Hhyp,W,dip,Vs30,Z25,SJ,iSpec)
//		CB2013May(0,6.56,4.6288643,0.0,5.9628415,1,0,2.1,28,27,760,0.60682398,0,0)

	}

}
