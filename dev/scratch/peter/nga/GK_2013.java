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

import static java.lang.Double.NaN;
import static java.lang.Math.*;
import static scratch.peter.nga.IMT.*;
import static scratch.peter.nga.FaultStyle.REVERSE;
import static scratch.peter.nga.FaultStyle.UNKNOWN;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.opensha.sha.util.TectonicRegionType;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * Preliminary implementation of the Graizer &amp; Kalkan (2013) GMPE.
 * 
 * Component: two component random hoizontal 
 * 
 * Implementation details:
 * 
 * Not thread safe -- create new instances as needed
 * 
 * @author Peter Powers
 */
public class GK_2013 implements NGAW2_GMM {

	public static final String NAME = "Graizer & Kalkan (2013)";
	public static final String SHORT_NAME = "GK2013";
	
	static final Set<IMT> IMTS = EnumSet.complementOf(EnumSet.of(PGV, PGD));
		
	/**
	 * Constructs a new instance of this attenuation relationship.
	 */
	public GK_2013() {}

	private IMT imt = null;
	private double Mw = NaN;
	private double rRup = NaN;
	private double vs30 = NaN;
	private FaultStyle style = UNKNOWN;

	@Override
	public ScalarGroundMotion calc() {
		return calc(imt, Mw, rRup, vs30, style);
	}

	@Override public String getName() { return NAME; }

	@Override public void set_IMT(IMT imt) { this.imt = imt; }

	@Override public void set_Mw(double Mw) { this.Mw = Mw; }
	
	@Override public void set_rJB(double rJB) {} // not used
	@Override public void set_rRup(double rRup) { this.rRup = rRup; }
	@Override public void set_rX(double rX) {} // not used
	
	@Override public void set_dip(double dip) {} // not used
	@Override public void set_width(double width) {} // not used
	@Override public void set_zTop(double zTop) {} // not used
	@Override public void set_zHyp(double zHyp) {} // not used
	
	@Override public void set_vs30(double vs30) { this.vs30 = vs30; }
	@Override public void set_vsInf(boolean vsInf) {} // not used
	@Override public void set_z2p5(double z2p5) {} // not used
	@Override public void set_z1p0(double z1p0) {} // not used

	@Override public void set_fault(FaultStyle style) { this.style = style; }

	@Override
	public TectonicRegionType get_TRT() {
		return TectonicRegionType.ACTIVE_SHALLOW;
	}

	@Override
	public Collection<IMT> getSupportedIMTs() {
		return IMTS;
	}

	
	/**
	 * Returns the ground motion for the supplied arguments.
	 * @param imt intensity measure type
	 * @param Mw moment magnitude
	 * @param rRup 3D distance to rupture plane (in km)
	 * @param style of faulting; only {@code REVERSE} is used
	 * @param vs30 average shear wave velocity in top 30 m (in m/sec)
	 * @return the ground motion
	 */
	public final ScalarGroundMotion calc(IMT imt, double Mw, double rRup,
			double vs30, FaultStyle style) {
		double per = imt.equals(PGA) ? 0.01 : imt.getPeriod();
		double F = (style == REVERSE) ? 1.28 : 1.0;

		// fixed at 150m for now; generic CA value per email from Vladimir
		double dBasin = 0.150;

		double pgaRef = calcLnPGA(Mw, rRup, vs30, dBasin, F);
		double sa = calcSpectralShape(per, Mw, rRup, vs30, dBasin);
		double mean = log(sa) + pgaRef;
		double std = calcStdDev(per);
		
		return new DefaultGroundMotion(mean, std);
	}
	
	private static final double m1 = -0.0012;
	private static final double m2 = -0.38;
	private static final double m3 = 0.0006;
	private static final double m4 = 3.9;
	
	private static final double a1 = 0.01686;
	private static final double a2 = 1.2695;
	private static final double a3 = 0.0001;

	private static final double s1 = 0.000;
	private static final double s2 = 0.077;
	private static final double s3 = 0.3251;

	private static final double t1 = 0.001;
	private static final double t2 = 0.59;
	private static final double t3 = -0.0005;
	private static final double t4 = -2.3;

	private static final double calcSpectralShape(double per, double Mw,
			double rRup, double vs30, double dBasin) {
		double mu = m1 * rRup + m2 * Mw + m3 * vs30 + m4;
		double A = (a1 * Mw + a2) * exp(a3 * rRup);
		double si = s1 * rRup - (s2 * Mw + s3);
		double T1 = abs(t1 * rRup + t2 * Mw + t3 * vs30 + t4);
		double To = max(0.3, T1);

		double slope = 1.763 - 0.25 * atan(1.4 * (dBasin - 1.0));
		double F1A = (log(per) + mu) / si;
		double F1 = A * exp(-0.5 * F1A * F1A);
		double F2A = pow(per / To, slope);
		double F2 = 1.0 / sqrt((1.0 - F2A) * (1.0 - F2A) + 2.25 * F2A);
        return F1 + F2;   
	}
	
	private static final double calcStdDev(double per) {
		double Sigma1 = 0.5522 + 0.0047 * log(per);
		double Sigma2 = 0.646 + 0.0497 * log(per);
		return max(Sigma1, Sigma2);
	}
	
	private static final double c1 = 0.140;
	private static final double c2 = -6.250;
	private static final double c3 = 0.370;
	private static final double c4 = 2.237;
	private static final double c5 = -7.542;
	private static final double c6 = -0.125;
	private static final double c7 = 1.190;
	private static final double c8 = -6.150;
	private static final double c9 = 0.600;
	private static final double bv = -0.240;
	private static final double VA = 484.5;
	private static final double c11 = 0.345;
	private static final double Q = 150.0; // California specific (is 156.6 in SH code)
	// TODO Q, above, needs to be updated to 205 outside CA

	private static final double calcLnPGA(double Mw, double rRup, double vs30,
			double dBasin, double F) {

		// Attenuation Equations
		double F1 = log((c1 * atan(Mw + c2) + c3) * F / 1.12);
		double Ro = c4 * Mw + c5;
		double Do = c6 * cos(c7 * (Mw + c8)) + c9;

		double rRo1 = rRup / Ro;
		double rRo2 = 1.0 - rRo1;
		double F2 = -0.5 * log(rRo2 * rRo2 + 4 * (Do * Do) * rRo1);

		// New Anelastic Eq.
		double F3 = -c11 * rRup / Q;
		double F4 = bv * log(vs30 / VA);

		// New Basin Eq.
		double bd1 = (1.5 / (dBasin + 0.1)) * (1.5 / (dBasin + 0.1));
		double Bas_Depth = 1.4 / sqrt((1.0 - bd1) * (1.0 - bd1) + 1.96 * bd1);

		double bd2 = (40.0 / (rRup + 0.1)) * (40.0 / (rRup + 0.1));
		double Bas_Dist = 1.0 / sqrt((1.0 - bd2) * (1.0 - bd2) + 1.96 * bd2);
		double Bas_Cor = Bas_Depth * Bas_Dist;
		double F5 = log(1 + Bas_Cor / 1.3);

		// Final Eq.
		return F1 + F2 + F3 + F4 + F5;
	}
	
}
