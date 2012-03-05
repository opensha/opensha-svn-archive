/**
 * 
 */
package scratch.UCERF3.enumTreeBranches;

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
public enum AveSlipForRupModels {
		
	
	AVE_UCERF2("Average UCERF2 M(A)", "AveU2") {
		public double getAveSlip(double area, double length) {
			double areaKm = area/1e6;
			double mag = (ellB_magArea.getMedianMag(areaKm) + hb_magArea.getMedianMag(areaKm))/2;
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
	},
	
	SHAW_2009_MOD("Shaw (2009) Modified for D(L)", "Shaw09Mod") {
		public double getAveSlip(double area, double length) {
			// From Table 2b of UCERF2_task2_p2.pdf (& using his equation 7)
			double c3 = 3.72e-5;
			double wBeta = 95.0*1e3;
			if(length<wBeta)
				return c3*length;
			else
				return 2*c3/(1/length + 1/wBeta);  }
	},

	HANKS_BAKUN_08("Hanks & Bakun (2008) M(A)", "HB08") {
		public double getAveSlip(double area, double length) {
			double mag = hb_magArea.getMedianMag(area/1e6);
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
	},
	
	

	ELLSWORTH_B("Ellsworth B M(A)", "EllB") {
		public double getAveSlip(double area, double length) {
			double mag = ellB_magArea.getMedianMag(area/1e6);
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
	},
	
	
	ELLSWORTH_B_MOD("Ellsworth B  Modified for D(L)", "EllB_Mod") {
		// From Table 2b of UCERF2_task2_p2.pdf (& using his equation 5)
		public double getAveSlip(double area, double length) {
			// c1 = 7.58e-5
			// W = 15 km --> 15000 m
			return 7.58e-5*Math.sqrt(length*15000d);
		}
	},

		
	SHAW12_SQRT_LENGTH("Sqrt Length D(L) (Shaw 2012)", "SqrtLen") {
		public double getAveSlip(double area, double length) {
			// c4 = 5.69e-5
			// W = 15 km = 15e3 m
			return 5.69e-5*Math.sqrt(length*15e3);
		}
	},

	SHAW_12_CONST_STRESS_DROP("Constant Stress Drop D(L) (Shaw 2012)", "CostStressDrop") {
		public double getAveSlip(double area, double length) {
			// stressDrop = 4.54 MPa
			// W = 15 km = 15e3 m
			double temp = 1.0/(7.0/(3.0*length) + 1.0/(2.0*15e3))*1e6;
			return 4.54*temp/FaultMomentCalc.SHEAR_MODULUS;
		}
	};
	
	Ellsworth_B_WG02_MagAreaRel ellB_magArea;
	HanksBakun2002_MagAreaRel hb_magArea;
	Shaw_2009_MagAreaRel sh09_magArea;

	private String name, shortName;
	
	private AveSlipForRupModels(String name, String shortName) {
		ellB_magArea = new Ellsworth_B_WG02_MagAreaRel();
		hb_magArea =new HanksBakun2002_MagAreaRel();
		sh09_magArea = new Shaw_2009_MagAreaRel();
		this.name = name;
		this.shortName = shortName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static void makePlot() {
		
		ArbitrarilyDiscretizedFunc u2_func = new ArbitrarilyDiscretizedFunc();
		u2_func.setName("AVE_UCERF2");
		ArbitrarilyDiscretizedFunc sh09_func = new ArbitrarilyDiscretizedFunc();
		sh09_func.setName("SHAW_2009_MOD");
		ArbitrarilyDiscretizedFunc ellB_Mod_func = new ArbitrarilyDiscretizedFunc();
		ellB_Mod_func.setName("ELLSWORTH_B_MOD");
		ArbitrarilyDiscretizedFunc ellB_func = new ArbitrarilyDiscretizedFunc();
		ellB_func.setName("ELLSWORTH_B");
		ArbitrarilyDiscretizedFunc hb_func = new ArbitrarilyDiscretizedFunc();
		hb_func.setName("HANKS_BAKUN_08");
		ArbitrarilyDiscretizedFunc sh12_sqrtL_func = new ArbitrarilyDiscretizedFunc();
		sh12_sqrtL_func.setName("SHAW12_SQRT_LENGTH");
		ArbitrarilyDiscretizedFunc sh12_csd_func = new ArbitrarilyDiscretizedFunc();
		sh12_csd_func.setName("SHAW_12_CONST_STRESS_DROP");
		
		
		AveSlipForRupModels u2 = AveSlipForRupModels.AVE_UCERF2;
		AveSlipForRupModels sh09 = AveSlipForRupModels.SHAW_2009_MOD;
		AveSlipForRupModels ellB_Mod = AveSlipForRupModels.ELLSWORTH_B_MOD;
		AveSlipForRupModels ellB = AveSlipForRupModels.ELLSWORTH_B;
		AveSlipForRupModels hb = AveSlipForRupModels.HANKS_BAKUN_08;
		AveSlipForRupModels sh12_sqrtL = AveSlipForRupModels.SHAW12_SQRT_LENGTH;
		AveSlipForRupModels sh12_csd = AveSlipForRupModels.SHAW_12_CONST_STRESS_DROP;
		
		
		// log10 area from 1 to 5
    	for(int i=1; i<=450; i++) {
    		double lengthKm = (double)i;
    		double length = lengthKm*1e3;
    		double area = length*15e3;
    		u2_func.set(lengthKm,u2.getAveSlip(area, length));
    		sh09_func.set(lengthKm,sh09.getAveSlip(area, length));
    		ellB_Mod_func.set(lengthKm,ellB_Mod.getAveSlip(area, length));
    		ellB_func.set(lengthKm,ellB.getAveSlip(area, length));
    		hb_func.set(lengthKm,hb.getAveSlip(area, length));
    		sh12_sqrtL_func.set(lengthKm,sh12_sqrtL.getAveSlip(area, length));
    		sh12_csd_func.set(lengthKm,sh12_csd.getAveSlip(area, length));
    	}
    	
    	ArrayList<ArbitrarilyDiscretizedFunc> funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
    	funcs.add(u2_func);
    	funcs.add(sh09_func);
    	funcs.add(ellB_Mod_func);
    	funcs.add(ellB_func);
    	funcs.add(hb_func);
    	funcs.add(sh12_sqrtL_func);
    	funcs.add(sh12_csd_func);
    	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Slip-Length Relationships"); 
		graph.setX_AxisLabel("Length");
		graph.setY_AxisLabel("Slip");
//		graph.setXLog(true);
//		graph.setX_AxisRange(0, 1e5);
//		graph.setY_AxisRange(4, 9);
	}
	
	
	//public 
	public static void main(String[] args) throws IOException {
		AveSlipForRupModels.makePlot();
	}
	
	
	/**
	 * This returns the slip (m) for the given rupture area (m-sq) or rupture length (m)
	 * @param areaOrLength
	 * @return
	 */
	 public abstract double getAveSlip(double area, double length);


}
