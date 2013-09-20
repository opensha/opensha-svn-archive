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
import static scratch.peter.nga.IMT.PGA;
import static scratch.peter.nga.FaultStyle.REVERSE;

import java.util.Collection;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * Preliminary implementation of the Idriss (2013) next generation attenuation
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
public class Idriss_2013 {

	public static final String NAME = "Idriss (2013)";
	
	private final Coeffs coeffsLo;
	private final Coeffs coeffsHi;
	
	private static class Coeffs extends Coefficients {
		double a1, a2, a3, b1, b2, xi, gamma, phi;
		Coeffs(String cName) {
			super(cName);
		}
	}
	
	/**
	 * Constructs a new instance of this attenuation relationship.
	 */
	public Idriss_2013() {
		coeffsLo = new Coeffs("Idriss13loM.csv");
		coeffsLo.set(PGA);
		coeffsHi = new Coeffs("Idriss13hiM.csv");
		coeffsHi.set(PGA);
	}

	/**
	 * Returns the ground motion for the supplied arguments.
	 * @param imt intensity measure type
	 * @param Mw moment magnitude
	 * @param rRup 3D distance to rupture plane (in km)
	 * @param vs30 average shear wave velocity in top 30 m (in m/sec)
	 * @param style of faulting; only {@code REVERSE} is used
	 * @return the ground motion
	 */
	public final ScalarGroundMotion calc(IMT imt, double Mw,
			double rRup, double vs30, FaultStyle style) {

		coeffsHi.set(imt);
		coeffsLo.set(imt);
		
		Coeffs c = (Mw <= 6.75) ? coeffsLo : coeffsHi;
		
		double mean = calcMean(c, Mw, rRup, vs30, style);
		double stdDev = calcStdDev(c, Mw);

		return new DefaultGroundMotion(mean, stdDev);
	}
	
	// Mean ground motion model
	private double calcMean(Coeffs c, double Mw, double rRup, double vs30,
			FaultStyle style) {
		return c.a1 + c.a2 * Mw + c.a3 * (8.5 - Mw) * (8.5 - Mw) -
			(c.b1 + c.b2 * Mw) * log(rRup + 10.0) + c.xi * log(vs30) + c.gamma *
			rRup + (style == REVERSE ? c.phi : 0.0);
	}

	// Aleatory uncertainty model
	private double calcStdDev(Coeffs c, double Mw) {
		double s1 = 0.05;
		Double T = c.imt().getPeriod();
		s1 *= (T == null || T <= 0.05) ? log(0.05) : (T < 3.0) ? log(T)
			: log(3d);
		double s2 = 0.08;
		s2 *= (Mw <= 5.0) ? 5.0 : (Mw < 7.5) ? Mw : 7.5;
		return 1.28 + s1 - s2;
	}
	
	public Collection<IMT> getSupportedIMTs() {
		return coeffsLo.getSupportedIMTs();
	}
	
	public static void main(String[] args) {
		Idriss_2013 id = new Idriss_2013();
		
		System.out.println("PGA");
		ScalarGroundMotion sgm = id.calc(PGA, 6.80, 4.629, 760.0, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());
		System.out.println("5Hz");
		sgm = id.calc(IMT.SA0P2, 6.80, 4.629, 760.0, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());
		System.out.println("1Hz");
		sgm = id.calc(IMT.SA1P0, 6.80, 4.629, 760.0, FaultStyle.REVERSE);
		System.out.println(sgm.mean());
		System.out.println(sgm.stdDev());

//		ScalarGroundMotion sgm = id.calc(PGA, 7.06, 27.08, 760.0, FaultStyle.STRIKE_SLIP);
//		System.out.println(sgm.mean());
//		System.out.println(sgm.stdDev());
	}

}
