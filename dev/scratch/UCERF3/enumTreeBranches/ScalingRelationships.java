/**
 * 
 */
package scratch.UCERF3.enumTreeBranches;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelDepthDep;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2009_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2009_ModifiedMagAreaRel;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;

/**
 * @author field
 *
 */
public enum ScalingRelationships implements LogicTreeBranchNode<ScalingRelationships> {
		
	
	AVE_UCERF2("Average UCERF2", "AveU2") {
		
		public double getAveSlip(double area, double length) {
			double areaKm = area/1e6;
			double mag = (ellB_magArea.getMedianMag(areaKm) + hb_magArea.getMedianMag(areaKm))/2;
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
		
		public double getMag(double area, double width) {
			double areaKm = area/1e6;
			return (ellB_magArea.getMedianMag(areaKm) + hb_magArea.getMedianMag(areaKm))/2;
		}		
		
		@Override
		public double getRelativeWeight() {
			return 0d; // TODO
		}
		
	},
	
	
	SHAW_2009_MOD("Shaw (2009) Modified", "Shaw09Mod") {
		
		public double getAveSlip(double area, double length) {
			double areaKm = area/1e6;
			double lengthKm = length/1e3;
			double mag = sh09_ModMagArea.getWidthDepMedianMag(areaKm, areaKm/lengthKm);
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
		
		public double getMag(double area, double width) {
			return sh09_ModMagArea.getWidthDepMedianMag(area*1e-6, width*1e-3);
		}		
		
		@Override
		public double getRelativeWeight() {
			return 0d; // TODO
		}
	},

	HANKS_BAKUN_08("Hanks & Bakun (2008)", "HB08") {
		
		public double getAveSlip(double area, double length) {
			double mag = hb_magArea.getMedianMag(area/1e6);
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
		
		public double getMag(double area, double width) {
			return hb_magArea.getMedianMag(area*1e-6);
		}		
		
		@Override
		public double getRelativeWeight() {
			return 0d; // TODO
		}
	},
	
	

	ELLSWORTH_B("Ellsworth B", "EllB") {
		
		public double getAveSlip(double area, double length) {
			double mag = ellB_magArea.getMedianMag(area/1e6);
			double moment = MagUtils.magToMoment(mag);
			return FaultMomentCalc.getSlip(area, moment);
		}
		
		public double getMag(double area, double width) {
			return ellB_magArea.getMedianMag(area*1e-6);
		}		
		
		@Override
		public double getRelativeWeight() {
			return 0d; // TODO
		}
	},
	
	
	ELLB_SQRT_LENGTH("EllB M(A) & Shaw12 Sqrt Length D(L)", "EllBsqrtLen") {
		public double getAveSlip(double area, double length) {
			double c6 = 5.69e-5;
			double xi = 1.25;
//			double w = 15e3;  // units of m
			double w = xi*area/length;  // units of m
			return c6*Math.sqrt(length*w);
		}
		
		public double getMag(double area, double width) {
			return ellB_magArea.getMedianMag(area*1e-6);
		}		
		
		@Override
		public double getRelativeWeight() {
			return 0d; // TODO
		}
	},

	SHAW_CONST_STRESS_DROP("Shaw09 M(A) & Shaw12 Const Stress Drop D(L)", "ShConStrDrp") {
		public double getAveSlip(double area, double length) {
			double stressDrop = 4.54;  // MPa
			double xi = 1.25;
//			double w = 15e3; // unit of meters
			double w = xi*area/length; // unit of meters
			double temp = 1.0/(7.0/(3.0*length) + 1.0/(2.0*w))*1e6;
			return stressDrop*temp/FaultMomentCalc.SHEAR_MODULUS;
		}
		
		public double getMag(double area, double width) {
			return sh09_ModMagArea.getWidthDepMedianMag(area*1e-6, width*1e3);
		}		
		
		@Override
		public double getRelativeWeight() {
			return 0d; // TODO
		}
	};
	
	Ellsworth_B_WG02_MagAreaRel ellB_magArea;
	HanksBakun2002_MagAreaRel hb_magArea;
	Shaw_2009_ModifiedMagAreaRel sh09_ModMagArea;

	private String name, shortName;
	
	private ScalingRelationships(String name, String shortName) {
		ellB_magArea = new Ellsworth_B_WG02_MagAreaRel();
		hb_magArea =new HanksBakun2002_MagAreaRel();
		sh09_ModMagArea = new Shaw_2009_ModifiedMagAreaRel();
		this.name = name;
		this.shortName = shortName;
	}
	
	
	
	/**
	 * This returns the slip (m) for the given rupture area (m-sq) or rupture length (m)
	 * @param area (m)
	 * @param length (m)
	 * @return
	 */
	 public abstract double getAveSlip(double area, double length);
	 
	 /**
	  * This returns the slip (m) for the given rupture area (m-sq) (and width (m)
	  * for the Shaw09 Mod relationship).
	  * @param area (m)
	  * @param width (m)
	  * @return
	  */
	 public abstract double getMag(double area, double width);

	
	@Override
	public String encodeChoiceString() {
		return "Dr"+getShortName();
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
	
	public static void makeSlipLengthPlot(double downDipWidth, int maxLength) {
		
		ArbitrarilyDiscretizedFunc u2_func = new ArbitrarilyDiscretizedFunc();
		u2_func.setName("AVE_UCERF2");
		ArbitrarilyDiscretizedFunc sh09_funcMod = new ArbitrarilyDiscretizedFunc();
		sh09_funcMod.setName("SHAW_2009_MOD");
		ArbitrarilyDiscretizedFunc ellB_func = new ArbitrarilyDiscretizedFunc();
		ellB_func.setName("ELLSWORTH_B");
		ArbitrarilyDiscretizedFunc hb_func = new ArbitrarilyDiscretizedFunc();
		hb_func.setName("HANKS_BAKUN_08");
		ArbitrarilyDiscretizedFunc sh12_sqrtL_func = new ArbitrarilyDiscretizedFunc();
		sh12_sqrtL_func.setName("ELLB_SQRT_LENGTH");
		ArbitrarilyDiscretizedFunc sh12_csd_func = new ArbitrarilyDiscretizedFunc();
		sh12_csd_func.setName("SHAW_CONST_STRESS_DROP");
		
		
		ScalingRelationships u2 = ScalingRelationships.AVE_UCERF2;
		ScalingRelationships sh09_Mod = ScalingRelationships.SHAW_2009_MOD;
		ScalingRelationships ellB = ScalingRelationships.ELLSWORTH_B;
		ScalingRelationships hb = ScalingRelationships.HANKS_BAKUN_08;
		ScalingRelationships sh12_sqrtL = ScalingRelationships.ELLB_SQRT_LENGTH;
		ScalingRelationships sh12_csd = ScalingRelationships.SHAW_CONST_STRESS_DROP;
		
		
		// log10 area from 1 to 5
    	for(int i=1; i<=maxLength; i++) {
    		double lengthKm = (double)i;
    		double length = lengthKm*1e3;
    		double area = length*downDipWidth*1e3;
    		u2_func.set(lengthKm,u2.getAveSlip(area, length));
    		sh09_funcMod.set(lengthKm,sh09_Mod.getAveSlip(area, length));
    		ellB_func.set(lengthKm,ellB.getAveSlip(area, length));
    		hb_func.set(lengthKm,hb.getAveSlip(area, length));
    		sh12_sqrtL_func.set(lengthKm,sh12_sqrtL.getAveSlip(area, length));
    		sh12_csd_func.set(lengthKm,sh12_csd.getAveSlip(area, length));
    	}
    	
    	ArrayList<ArbitrarilyDiscretizedFunc> funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
    	funcs.add(sh09_funcMod);
    	funcs.add(ellB_func);
    	funcs.add(hb_func);
    	funcs.add(sh12_sqrtL_func);
    	funcs.add(sh12_csd_func);
    	
    	ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.GREEN));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.MAGENTA));

    	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Slip-Length Relationships; DDW="+downDipWidth+" km", plotChars); 
		graph.setX_AxisLabel("Length (km)");
		graph.setY_AxisLabel("Slip (m)");
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(18);
		graph.setTickLabelFontSize(16);
	}
	
	
	/**
	 * This tests the magnitudes and implied slip amounts for creeping-section faults
	 * assuming a length and DDW
	 */
	public static void testCreepingSectionSlips() {
		double lengthKm = 150;
		double widthKm = 1.2;
		double areaKm = lengthKm*widthKm;
		
		ArrayList<MagAreaRelationships> magAreaList = new ArrayList<MagAreaRelationships>();
		magAreaList.add(MagAreaRelationships.ELL_B);
		magAreaList.add(MagAreaRelationships.HB_08);
		magAreaList.add(MagAreaRelationships.SHAW_09_MOD);
		
		ArrayList<ScalingRelationships> aveSlipForRupModelsList= new ArrayList<ScalingRelationships>();
		aveSlipForRupModelsList.add(ScalingRelationships.ELLSWORTH_B);
		aveSlipForRupModelsList.add(ScalingRelationships.HANKS_BAKUN_08);
		aveSlipForRupModelsList.add(ScalingRelationships.SHAW_2009_MOD);
		aveSlipForRupModelsList.add(ScalingRelationships.ELLB_SQRT_LENGTH);
		aveSlipForRupModelsList.add(ScalingRelationships.SHAW_CONST_STRESS_DROP);
		
		
		FaultModels fm = FaultModels.FM3_1;
		DeformationModels dm = DeformationModels.GEOLOGIC;
		
		String result = "CREEPING SECTION Mag and AveSlip (assuming length=150 and DDW=1.2 km):\n";
		
		for(MagAreaRelationships ma : magAreaList) {
			for(ScalingRelationships asm : aveSlipForRupModelsList) {
				double mag = ma.getMagAreaRelationships().get(0).getMedianMag(areaKm);
				double slip = asm.getAveSlip(areaKm*1e6, lengthKm*1e3);
				mag = Math.round(mag*100)/100.;
				slip = Math.round(slip*100)/100.;
				result += (float)mag+"\t"+(float)slip+"\tfor\t"+ma.getShortName()+"\t"+asm.getShortName()+"\n";
			}
		}
		
		System.out.println(result);

	}
	
	
	public static void makeMagAreaPlot() {
		
		double downDipWidth=11;
		
		ArbitrarilyDiscretizedFunc sh09mod_func = new ArbitrarilyDiscretizedFunc();
		sh09mod_func.setName("SHAW_2009_Mod; downDipWidth="+downDipWidth);
		ArbitrarilyDiscretizedFunc ellB_func = new ArbitrarilyDiscretizedFunc();
		ellB_func.setName("ELLSWORTH_B");
		ArbitrarilyDiscretizedFunc hb_func = new ArbitrarilyDiscretizedFunc();
		hb_func.setName("HANKS_BAKUN_08");
		
		ScalingRelationships sh09mod = ScalingRelationships.SHAW_2009_MOD;
		ScalingRelationships ellB = ScalingRelationships.ELLSWORTH_B;
		ScalingRelationships hb = ScalingRelationships.HANKS_BAKUN_08;
		
		// log10 area from 1 to 5
    	for(int i=50; i<=20000; i+=10) {
    		double area = (double)i;
     		sh09mod_func.set(area,sh09mod.getMag(area*1e6,downDipWidth*1e3));
    		ellB_func.set(area,ellB.getMag(area*1e6,downDipWidth*1e3));
    		hb_func.set(area,hb.getMag(area*1e6,downDipWidth*1e3));
    	}
    	
    	ArrayList<ArbitrarilyDiscretizedFunc> funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
    	funcs.add(sh09mod_func);
    	funcs.add(ellB_func);
    	funcs.add(hb_func);
    	
    	ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, null, 1f, Color.GREEN));

    	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Mag-Area Relationships",plotChars); 
		graph.setX_AxisLabel("Area (km-sq)");
		graph.setY_AxisLabel("Magnitude");
		graph.setXLog(true);
		graph.setX_AxisRange(50, 2e4);
		graph.setY_AxisRange(5, 9);
		graph.setPlotLabelFontSize(18);
		graph.setAxisLabelFontSize(18);
		graph.setTickLabelFontSize(16);
	}


	
	//public 
	public static void main(String[] args) throws IOException {

		 makeSlipLengthPlot(11, 1000);
		 makeMagAreaPlot();
		
	//	testCreepingSectionSlips();
		
		
//		// the following code tested the revised shaw09 mod implementation (he verified the numbers produced)
//		MagAreaRelationship sh09_Mod = MagAreaRelationships.SHAW_09_MOD.getMagAreaRelationships().get(0);
//		System.out.println("length\twidth\tarea\tmag\tmoment\tslip\timplWidth\timplWidth/width");
//		for(double width = 5; width<16; width += 5) {
//			for(double length = 10; length<200; length += 30) {
//				double area = length*width;
//				double mag = ((MagAreaRelDepthDep)sh09_Mod).getWidthDepMedianMag(area, width);			
//				double moment = MagUtils.magToMoment(mag);
//				double slip = AveSlipForRupModels.SHAW_2009_MOD.getAveSlip(area*1e6, length*1e3);
//				double implWidth = 1e-3*moment/(slip*length*1e3*FaultMomentCalc.SHEAR_MODULUS);			
//				double ratio = implWidth/width;			
//				System.out.println((float)length+
//						"\t"+(float)width+
//						"\t"+(float)area+
//						"\t"+(float)mag+
//						"\t"+(float)moment+
//						"\t"+(float)slip+
//						"\t"+(float)implWidth+
//						"\t"+(float)ratio);
//			}
//		}
	}


}
