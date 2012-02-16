/**
 * 
 */
package scratch.UCERF3.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2009_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

/**
 * @author field
 *
 */
public enum AveSlipForRupModel {
		
	
	AVE_UCERF2 {
		public double getAveSlip(double area) {
			double areaKm = area/1e6;
			double mag = (ellB_magArea.getMedianMag(areaKm) + hb_magArea.getMedianMag(areaKm))/2;
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
	},
	
	SHAW_2009_MOD {
		public double getAveSlip(double area) {
			// the term "- Shaw_2009_MagAreaRel.cZero + 4.2" is the modification described in the sliphazned.pdf file sent on Feb. 15, 2012.
			double mag = sh09_magArea.getMedianMag(area/1e6) - Shaw_2009_MagAreaRel.cZero + 4.02;
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);		}
	},
	
	ELLSWORTH_B {
		public double getAveSlip(double area) {
			double mag = ellB_magArea.getMedianMag(area/1e6);
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
	},
		
	SHAW12_SQRT_LENGTH {
		public double getAveSlip(double length) {
			// c4 = 5.69e-5
			// W = 15 km = 15e3 m
			return 5.69e-5*Math.sqrt(length*15e3);
		}
	},

	SHAW_12_CONST_STRESS_DROP {
		public double getAveSlip(double length) {
			// stressDrop = 4.54 MPa
			// W = 15 km = 15e3 m
			double temp = 1.0/(7.0/(3.0*length) + 1.0/(2.0*15e3))*1e6;
			return 4.54*temp/FaultMomentCalc.SHEAR_MODULUS;
		}
	};
	
	Ellsworth_B_WG02_MagAreaRel ellB_magArea;
	HanksBakun2002_MagAreaRel hb_magArea;
	Shaw_2009_MagAreaRel sh09_magArea;

	
	private AveSlipForRupModel() {
		ellB_magArea = new Ellsworth_B_WG02_MagAreaRel();
		hb_magArea =new HanksBakun2002_MagAreaRel();
		sh09_magArea = new Shaw_2009_MagAreaRel();
	}
	
	public static void makePlot() {
		
		ArbitrarilyDiscretizedFunc u2_func = new ArbitrarilyDiscretizedFunc();
		u2_func.setName("AVE_UCERF2");
		ArbitrarilyDiscretizedFunc sh09_func = new ArbitrarilyDiscretizedFunc();
		sh09_func.setName("SHAW_2009_MOD");
		ArbitrarilyDiscretizedFunc ellB_func = new ArbitrarilyDiscretizedFunc();
		ellB_func.setName("ELLSWORTH_B)");
		ArbitrarilyDiscretizedFunc sh12_sqrtL_func = new ArbitrarilyDiscretizedFunc();
		sh12_sqrtL_func.setName("SHAW12_SQRT_LENGTH");
		ArbitrarilyDiscretizedFunc sh12_csd_func = new ArbitrarilyDiscretizedFunc();
		sh12_csd_func.setName("SHAW_12_CONST_STRESS_DROP");
		
		
		AveSlipForRupModel u2 = AveSlipForRupModel.AVE_UCERF2;
		AveSlipForRupModel sh09 = AveSlipForRupModel.SHAW_2009_MOD;
		AveSlipForRupModel ellB = AveSlipForRupModel.ELLSWORTH_B;
		AveSlipForRupModel sh12_sqrtL = AveSlipForRupModel.SHAW12_SQRT_LENGTH;
		AveSlipForRupModel sh12_csd = AveSlipForRupModel.SHAW_12_CONST_STRESS_DROP;
		
		
		// log10 area from 1 to 5
    	for(int i=1; i<=450; i++) {
    		double lengthKm = (double)i;
    		double length = lengthKm*1e3;
    		double area = length*15e3;
    		u2_func.set(lengthKm,u2.getAveSlip(area));
    		sh09_func.set(lengthKm,sh09.getAveSlip(area));
    		ellB_func.set(lengthKm,ellB.getAveSlip(area));
    		sh12_sqrtL_func.set(lengthKm,sh12_sqrtL.getAveSlip(length));
    		sh12_csd_func.set(lengthKm,sh12_csd.getAveSlip(length));
    	}
    	
    	ArrayList<ArbitrarilyDiscretizedFunc> funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
    	funcs.add(u2_func);
    	funcs.add(sh09_func);
    	funcs.add(ellB_func);
    	funcs.add(sh12_sqrtL_func);
    	funcs.add(sh12_csd_func);
    	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Mag Area Relationships"); 
		graph.setX_AxisLabel("Length");
		graph.setY_AxisLabel("Slip");
//		graph.setXLog(true);
//		graph.setX_AxisRange(0, 1e5);
//		graph.setY_AxisRange(4, 9);
	}
	
	
	//public 
	public static void main(String[] args) throws IOException {
		AveSlipForRupModel.makePlot();
	}
	
	
	/**
	 * This returns the slip (m) for the given rupture area (m-sq) or rupture length (m)
	 * @param areaOrLength
	 * @return
	 */
	 public abstract double getAveSlip(double areaOrLength);


}
