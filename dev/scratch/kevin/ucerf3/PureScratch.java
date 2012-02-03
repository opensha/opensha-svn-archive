package scratch.kevin.ucerf3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.nshmp.imr.AB2006_140_AttenRel;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;

public class PureScratch {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		double a1 = 8.62552e32;
		double a2 = 1.67242e34;
		double a3 = 1.77448e20;
		double a4 = 9.05759e20;
		double c = 8.0021909e37;
		double me = 5.9742e24;
		double re = 6371000;
		
		double p1 = (c - a1*me/a3) / (a2-a1*a4/a3);
		double p2 = (me - a4*p1)/a3;
		
		System.out.println("p1: "+p1);
		System.out.println("p2: "+p2);
		
		System.exit(0);
		
		
		CB_2008_AttenRel imr = new CB_2008_AttenRel(null);
		imr.setParamDefaults();
		MeanUCERF2 ucerf = new MeanUCERF2();
		ucerf.updateForecast();
		ProbEqkSource src = ucerf.getSource(3337);
		System.out.println(src.getName());
		ProbEqkRupture theRup = src.getRupture(77);
		System.out.println("Rup 77 pts: "+theRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size());
		imr.setEqkRupture(theRup);
		imr.setIntensityMeasure(PGA_Param.NAME);
		Site site = new Site(new Location(34.10688, -118.22060));
		site.addParameterList(imr.getSiteParams());
		imr.setAll(theRup, site, imr.getIntensityMeasure());
		imr.getMean();
		System.exit(0);
		
		UCERF2_TimeIndependentEpistemicList ti_ep = new UCERF2_TimeIndependentEpistemicList();
		UCERF2_TimeDependentEpistemicList td_ep = new UCERF2_TimeDependentEpistemicList();
//		ep.updateForecast();
		System.out.println(ti_ep.getNumERFs());
		System.out.println(td_ep.getNumERFs());
		
		System.exit(0);
		
		
		File zipFile = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/" +
				"InversionSolutions/NCAL_Model1.zip");
		SimpleFaultSystemSolution.fromZipFile(zipFile).toXMLFile(
				new File(zipFile.getAbsolutePath().replaceAll(".zip", ".xml")));
		System.exit(0);
		
		
		// this get's the DB accessor (version 3)
		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();

		PrefFaultSectionDataDB_DAO faultSectionDB_DAO = new PrefFaultSectionDataDB_DAO(db);

		List<FaultSectionPrefData> sections = faultSectionDB_DAO.getAllFaultSectionPrefData(); 
		for (FaultSectionPrefData data : sections)
			System.out.println(data);
		System.exit(0);
		
		
		double minX = -9d;
		double maxX = 0d;
		int num = 200;
		EvenlyDiscretizedFunc ucerf2Func = new EvenlyDiscretizedFunc(minX, maxX, num);
		double delta = ucerf2Func.getDelta();
		ucerf2Func.setName("UCERF2");
		
		boolean doUCERF2 = true;
		
		if (doUCERF2) {
			System.out.println("Creating UCERF2");
			ERF erf = new MeanUCERF2();
			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
			erf.getTimeSpan().setDuration(1, TimeSpan.YEARS);
			erf.updateForecast();
			
			System.out.println("Setting UCERF2 rates");
			for (ProbEqkSource source : erf) {
				for (ProbEqkRupture rup : source) {
					if (Math.random() > 0.2d)
						continue;
					double prob = rup.getProbability();
					double log10prob = Math.log10(prob);
					if (log10prob < minX || log10prob > maxX) {
						System.out.println("Prob outside of bounds: "+prob + " (log10: "+log10prob+")");
					}
					int ind = (int)Math.round((log10prob-minX)/delta);
					ucerf2Func.set(ind, ucerf2Func.getY(ind)+1);
				}
			}
		}
		
		File dir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_09_08-morgan-CS_fixed");
		File binFile = new File(dir, "run1.mat");
		
		double[] rupRateSolution = MatrixIO.doubleArrayFromFile(binFile);
		int numNonZero = 0;
		for (double rate : rupRateSolution)
			if (rate > 0)
				 numNonZero++;
		double[] nonZeros = new double[numNonZero];
		int cnt = 0;
		for (double rate : rupRateSolution) {
			if (rate > 0)
				nonZeros[cnt++] = rate;
		}
		EvenlyDiscretizedFunc inversionFunc = new EvenlyDiscretizedFunc(minX, maxX, num);
		inversionFunc.setName("UCERF3 Inversion");
		
		
		System.out.println("Setting inversion rates");
		for (int i=0; i<nonZeros.length; i++) {
			double log10rate = Math.log10(nonZeros[i]);
			if (log10rate < minX || log10rate > maxX) {
				System.out.println("Prob outside of bounds: "+nonZeros[i] + " (log10: "+log10rate+")");
			}
			int ind = (int)Math.round((log10rate-minX)/delta);
			inversionFunc.set(ind, inversionFunc.getY(ind)+1);
		}
		
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		funcs.add(ucerf2Func);
		funcs.add(inversionFunc);
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
		
		System.out.println("Displaying graph!");
		
		new GraphiWindowAPI_Impl(funcs, "Rupture Rates", chars);
		System.out.println("DONE");
	}

}
