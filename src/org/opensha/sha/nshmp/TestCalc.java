package org.opensha.sha.nshmp;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.nshmp.NEHRP_TestCity;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08.NSHMP08_CEUS_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.CEUS_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.source.GridERF;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.nshmp.imr.NSHMP08_CEUS;
import org.opensha.sha.util.NSHMP_Util;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class TestCalc {

	private static HazardCurveCalculator hazardCurveCalculator;
	private static ERF erf;
	private static ScalarIMR imr;
	private static Period per = Period.GM0P00;
//	private static Location loc = NEHRP_TestCity.MEMPHIS.location();
	private static Location loc = new Location(35, -90);
	private static Location loc2 = new Location(loc.getLatitude()+0.05, loc.getLongitude()+0.05);
	
	public static void main(String[] args) {
		initERF();
		initIMR();
		System.out.println("do calc");
		doCalc();
		System.exit(0);
	}
	
	static void doCalc() {
		Vs30_Param vs30param = new Vs30_Param(760);
		vs30param.setValueAsDefault();
		SiteTypeParam siteTypeParam = new SiteTypeParam();

		try {

			// init
//			String imt = (period == 0) ? "PGA" : "SA";
//			String imtString = imt;
//			imtString += (period != 0) ? 
//				"_" + periodFormat.format(period) + "sec" : "";
			

			imr.setIntensityMeasure((per == Period.GM0P00)? "PGA" : "SA");
			imr.getParameter(PeriodParam.NAME).setValue(per.getValue());
			
			hazardCurveCalculator = new HazardCurveCalculator();
			hazardCurveCalculator.getAdjustableParams().getParameter(MaxDistanceParam.NAME).setValue(1000.0);
//			File outputDir = new File(outDir);
//			outputDir.mkdirs();

			// Do for First Lat
//			double twoPercentProb, tenPercentProb;
//			int colIndex=1;
//			for(double lon=minLon; lon<=maxLon; lon+=GRID_SPACING, ++colIndex) {
//				System.out.println("Doing Site :" + latLonFormat.format(lat) + 
//					"," + latLonFormat.format(lon) + " " + imtString);
				
//				// ensure that DEPTH_2_5KM_PARAM value iis set from default
//				DEPTH_2_5KM_PARAM.setValueAsDefault();
//				// set DEPTH_1_0KM_PARAM based on vs30; this could conceivably
//				if (vs30 == 760.0) {
//					DEPTH_1_0KM_PARAM.setValue(40.0);
//				} else if (vs30 == 259.0) {
//					DEPTH_1_0KM_PARAM.setValue(330.0);
//				}
				Site site = new Site(loc);
				site.addParameter(vs30param);
				site.addParameter(siteTypeParam);
//				site.addParameter(DEPTH_2_5KM_PARAM); // used by CB2008
//				site.addParameter(DEPTH_1_0KM_PARAM); // used by CY2008
//				site.addParameter(VS_30_TYPE_PARAM);  
				
				// do log of X axis values
				DiscretizedFunc hazFunc = per.getFunction();
//				for(int i=0; i<numX_Vals; ++i)
//					hazFunc.set(Math.log(function.getX(i)), 1);
				
				// Note here that hazardCurveCalculator accepts the Log of X-Values
				hazardCurveCalculator.getHazardCurve(hazFunc, site, imr, erf);
				
				// convert to annual rate
				for (Point2D p : hazFunc) {
					hazFunc.set(p.getX(), -Math.log(1-p.getY()));
				}
				System.out.println(hazFunc);
				// Unlog the X-Values before doing interpolation. The Y Values we get from hazardCurveCalculator are unmodified
//				DiscretizedFunc newFunc = new ArbitrarilyDiscretizedFunc();
//				for(int i=0; i<numX_Vals; ++i)
//					newFunc.set(function.getX(i), hazFunc.getY(i));
//				
//				try {
//					twoPercentProb = newFunc.getFirstInterpolatedX_inLogXLogYDomain(0.02);
//				} catch (InvalidRangeException ire) {
//					twoPercentProb = 0.0;
//				}
//				try {
//					tenPercentProb = newFunc.getFirstInterpolatedX_inLogXLogYDomain(0.1);
//				} catch (InvalidRangeException ire) {
//					tenPercentProb = 0.0;
//				}
//				sheet.getRow(0).createCell((short)colIndex).setCellValue(latLonFormat.format(lon));
//				for(int i=0; i<numX_Vals; ++i)
//					sheet.createRow(i+1).createCell((short)colIndex).setCellValue(newFunc.getY(i));
//
//				sheet.createRow(twoPercentProbRoIndex).createCell((short)colIndex).setCellValue(twoPercentProb);
//				sheet.createRow(tenPercentProbRoIndex).createCell((short)colIndex).setCellValue(tenPercentProb);
//				
//			}
//			FileOutputStream fileOut = new FileOutputStream(outputFileName);
//			wb.write(fileOut);
//			fileOut.close();
		}catch(RemoteException e) {
			e.printStackTrace();
		}
//		System.exit(0);

	}
	
	// init any IMR params
	private static void initIMR() {
		System.out.println("init imr");
		imr = new NSHMP08_CEUS(null);
		imr.setParamDefaults();
		imr.getParameter("Source").setValue(CEUS_ERF.SourceCEUS.GRID);
//		imr.getParameter(name)
//		if (id == "BA") {
//			imr = new BA_2008_AttenRel(this);
//		} else if (id == "CB") {
//			imr = new CB_2008_AttenRel(this);
//		} else if (id == "CY"){
//			imr = new CY_2008_AttenRel(this);
//		}
//		imr.setParamDefaults();
//		imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//		imr.getParameter(SigmaTruncLevelParam.NAME).setValue(3.0);
	}

	// init any ERF params
	private static void initERF() {
		System.out.println("init erf");
		erf = new CEUS_ERF();
//		erf = GridERF.getTestGrid();
		erf.updateForecast();
//		meanUCERF2 = new MeanUCERF2();
//		meanUCERF2.setParameter(MeanUCERF2.RUP_OFFSET_PARAM_NAME, new Double(5.0));
//		meanUCERF2.setParameter(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME, false);
//		meanUCERF2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
//		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_CROSSHAIR);
//		meanUCERF2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.CENTERED_DOWNDIP_FLOATER);
//		meanUCERF2.getTimeSpan().setDuration(50.0);
//		meanUCERF2.updateForecast();
	}

}
