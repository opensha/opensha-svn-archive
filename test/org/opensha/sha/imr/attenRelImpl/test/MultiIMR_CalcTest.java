package org.opensha.sha.imr.attenRelImpl.test;

import static org.junit.Assert.*;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.CurveAverager;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.MultiIMR_Averaged_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

public class MultiIMR_CalcTest {

	private static EqkRupForecastAPI erf;
	private static Site site;
	private static HazardCurveCalculator hc;

	private static final double max_curve_pt_diff = 0.01;
	private static final double max_avg_curve_pt_diff = 0.02;
	private static final double max_val_diff = 0.01;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		erf = new Frankel02_AdjustableEqkRupForecast();
		erf.updateForecast();

		Vs30_Param vs30 = new Vs30_Param(760d);
		vs30.setValueAsDefault();
		DepthTo2pt5kmPerSecParam z25 = new DepthTo2pt5kmPerSecParam(1d);
		z25.setValueAsDefault();
		DepthTo1pt0kmPerSecParam z10 = new DepthTo1pt0kmPerSecParam(1d);
		z10.setValueAsDefault();
		Vs30_TypeParam vs30Type = new Vs30_TypeParam();
		vs30Type.setValueAsDefault();

		site = new Site(new Location(34, -120));
		site.addParameter(vs30);
		site.addParameter(z25);
		site.addParameter(z10);
		site.addParameter(vs30Type);

		hc = new HazardCurveCalculator();
	}

	protected static ArrayList<ScalarIntensityMeasureRelationshipAPI> createNGAs(boolean setParamDefaults) {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = new ArrayList<ScalarIntensityMeasureRelationshipAPI>();

		imrs.add(new CB_2008_AttenRel(null));
		imrs.add(new BA_2008_AttenRel(null));
		imrs.add(new CY_2008_AttenRel(null));
		imrs.add(new AS_2008_AttenRel(null));

		if (setParamDefaults) {
			for (ScalarIntensityMeasureRelationshipAPI imr : imrs)
				imr.setParamDefaults();
		}

		return imrs;
	}

	@Test
	public void testSingleIMRs_PGA() throws RemoteException {
		doHC_NGA_Test(PGA_Param.NAME, -1.0);
	}

	@Test
	public void testSingleIMRs_PGV() throws RemoteException {
		doHC_NGA_Test(PGV_Param.NAME, -1.0);
	}

	@Test
	public void testSingleIMRs_SA01() throws RemoteException {
		doHC_NGA_Test(SA_Param.NAME, 0.1);
	}

	@Test
	public void testSingleIMRs_SA10() throws RemoteException {
		doHC_NGA_Test(SA_Param.NAME, 1.0);
	}

	@Test
	public void testSingleIMRs_SA20() throws RemoteException {
		doHC_NGA_Test(SA_Param.NAME, 2.0);
	}

	@Test
	public void testMultiIMRs_SA01() throws RemoteException {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> ngas1 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> ngas2 = createNGAs(true);
		testMultiIMRAverageCurve(new MultiIMR_Averaged_AttenRel(ngas1), ngas2, SA_Param.NAME, 1.0);
	}

	@Test
	public void testSingleIMRs_EPSILON_SA10() throws RemoteException {
		doVal_NGA_Test(SA_Param.NAME, 1.0, IMR_PROP.EPSILON);
	}

	@Test
	public void testSingleIMRs_MEAN_SA10() throws RemoteException {
		doVal_NGA_Test(SA_Param.NAME, 1.0, IMR_PROP.MEAN);
	}

	@Test
	public void testSingleIMRs_STD_DEV_SA10() throws RemoteException {
		doVal_NGA_Test(SA_Param.NAME, 1.0, IMR_PROP.STD_DEV);
	}

	@Test
	public void testSingleIMRs_EXCEED_PROB_SA10() throws RemoteException {
		doVal_NGA_Test(SA_Param.NAME, 1.0, IMR_PROP.EXCEED_PROB);
	}

	@Test
	public void testSingleIMRs_EPSILON_PGA() throws RemoteException {
		doVal_NGA_Test(PGA_Param.NAME, -1.0, IMR_PROP.EPSILON);
	}

	@Test
	public void testSingleIMRs_MEAN_PGA() throws RemoteException {
		doVal_NGA_Test(PGA_Param.NAME, -1.0, IMR_PROP.MEAN);
	}

	@Test
	public void testSingleIMRs_STD_DEV_PGA() throws RemoteException {
		doVal_NGA_Test(PGA_Param.NAME, -1.0, IMR_PROP.STD_DEV);
	}

	@Test
	public void testSingleIMRs_EXCEED_PROB_PGA() throws RemoteException {
		doVal_NGA_Test(PGA_Param.NAME, -1.0, IMR_PROP.EXCEED_PROB);
	}

	private void doHC_NGA_Test(String imt, double period) throws RemoteException {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs1 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs2 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs3 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs4 = createNGAs(true);
		for (int i=0; i<imrs1.size(); i++)
			testSingleIMRHazardCurve(imrs1.get(i),
					getMulti(imrs2.get(i)),
					getMulti(imrs3.get(i), imrs4.get(i)),
					imt, period);
	}

	private void doVal_NGA_Test(String imt, double period, IMR_PROP prop) throws RemoteException {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs1 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs2 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs3 = createNGAs(true);
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs4 = createNGAs(true);
		for (int i=0; i<imrs1.size(); i++)
			testSingleIMRIndVal(imrs1.get(i),
					getMulti(imrs2.get(i)),
					getMulti(imrs3.get(i), imrs4.get(i)),
					imt, period, prop);
	}

	private MultiIMR_Averaged_AttenRel getMulti(ScalarIntensityMeasureRelationshipAPI imr) {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs =
			new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		imrs.add(imr);
		MultiIMR_Averaged_AttenRel multi = new MultiIMR_Averaged_AttenRel(imrs);
		return multi;
	}

	private MultiIMR_Averaged_AttenRel getMulti(ScalarIntensityMeasureRelationshipAPI imr1,
			ScalarIntensityMeasureRelationshipAPI imr2) {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs =
			new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		imrs.add(imr1);
		imrs.add(imr2);
		MultiIMR_Averaged_AttenRel multi = new MultiIMR_Averaged_AttenRel(imrs);
		return multi;
	}

	protected enum IMR_PROP {
		EPSILON,
		MEAN,
		STD_DEV,
		EXCEED_PROB;
	}

	private void testSingleIMRIndVal(ScalarIntensityMeasureRelationshipAPI imr,
			MultiIMR_Averaged_AttenRel multi,
			MultiIMR_Averaged_AttenRel multis,
			String imt, double period, IMR_PROP prop) throws RemoteException {

		setIMT(imr, imt, period);
		setIMT(multi, imt, period);
		setIMT(multis, imt, period);

		imr.setSite(site);
		multi.setSite(site);
		multis.setSite(site);

		MinMaxAveTracker tracker1 = new MinMaxAveTracker();
		MinMaxAveTracker tracker2 = new MinMaxAveTracker();

		String meta = "("+prop+") IMR: " + imr.getShortName() + " IMT: " + imt;
		if (period >= 0)
			meta += " PERIOD: " + period;

		for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
			ProbEqkSource source = erf.getSource(sourceID);
			for (int rupID=0; rupID<source.getNumRuptures(); rupID++) {
				ProbEqkRupture rup = source.getRupture(rupID);
				imr.setEqkRupture(rup);
				multi.setEqkRupture(rup);
				multis.setEqkRupture(rup);

				double mean1 = imr.getMean();
				double mean2 = multi.getMean();
				double mean3 = multis.getMean();

				double val1 = 0;
				double val2 = 0;
				double val3 = 0;
				if (prop == IMR_PROP.EPSILON) {
					val1 = imr.getEpsilon();
					val2 = multi.getEpsilon();
					val3 = multis.getEpsilon();
				} else if (prop == IMR_PROP.MEAN) {
					val1 = mean1;
					val2 = mean2;
					val3 = mean3;
				} else if (prop == IMR_PROP.STD_DEV) {
					val1 = imr.getStdDev();
					val2 = multi.getStdDev();
					val3 = multis.getStdDev();
				} else if (prop == IMR_PROP.EXCEED_PROB) {
					val1 = imr.getExceedProbability();
					val2 = multi.getExceedProbability();
					val3 = multis.getExceedProbability();
				}

				double diff1 = DataUtils.getPercentDiff(val1, val2);
				double diff2 = DataUtils.getPercentDiff(val2, val3);
				tracker1.addValue(diff1);
				tracker2.addValue(diff2);
				String vals = val1 + ", " + val2 + ", PDIFF1: " + diff1 + ", " + val3 + ", PDIFF2: " + diff2;
				assertTrue(meta+" PDiff1 greater than "+max_val_diff+"\n"+vals, diff1 < max_val_diff);
				assertTrue(meta+" PDiff2 greater than "+max_val_diff+"\n"+vals, diff2 < max_val_diff);

			}
		}
		System.out.println("********* " + meta + " *********");
		System.out.println("compare single with multi(1):\t" + tracker1);
		System.out.println("compare multi(1) with multi(2):\t" + tracker2);
		System.out.println("********************************************************");
	}

	private void testMultiIMRAverageCurve(MultiIMR_Averaged_AttenRel multi,
			ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs,
			String imt, double period) throws RemoteException {

		IMT_Info imtInfo = new IMT_Info();
		ArrayList<DiscretizedFuncAPI> singleCurves = new ArrayList<DiscretizedFuncAPI>();
		for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
			setIMT(imr, imt, period);
			DiscretizedFuncAPI singleCurve = imtInfo.getDefaultHazardCurve(imt);
			hc.getHazardCurve(singleCurve, site, imr, erf);
			singleCurves.add(singleCurve);
		}
		DiscretizedFuncAPI averageCurve = CurveAverager.averageCurves(singleCurves);
		averageCurve.setName("Average value curve");
		setIMT(multi, imt, period);
		DiscretizedFuncAPI multiCurve = imtInfo.getDefaultHazardCurve(imt);
		multiCurve.setName("Curve calculated with MultiIMR_Averaged_AttenRel");
		hc.getHazardCurve(multiCurve, site, multi, erf);

		String meta = "(hazard curve) IMT: " + imt;
		if (period >= 0)
			meta += " PERIOD: " + period;

		MinMaxAveTracker tracker1 = new MinMaxAveTracker();

		int perfectMatches = 0;

		for (int j=0; j<multiCurve.getNum(); j++) {
			double x = multiCurve.getX(j);
			double yMulti = multiCurve.getY(j);
			double ySingleAvg = averageCurve.getY(j);

			if (ySingleAvg == yMulti && ySingleAvg != 0d)
				perfectMatches++;

			double diff = DataUtils.getPercentDiff(ySingleAvg, yMulti);
			String vals = ySingleAvg + ", " + yMulti + ", PDIFF: " + diff;
			assertTrue(meta+" PDiff1 greater than "+max_avg_curve_pt_diff+"\n"+vals, diff < max_avg_curve_pt_diff);

			tracker1.addValue(diff);
			String singleVals = null;
			for (DiscretizedFuncAPI singleCurve : singleCurves) {
				if (singleVals == null)
					singleVals = "";
				else
					singleVals += "\t";
				singleVals += "("+(float)singleCurve.getY(j)+")";
			}
			System.out.println(x+"\t"+(float)yMulti+"\t"+(float)ySingleAvg+"\t"+(float)diff+"\t"+singleVals);
		}
		//		System.out.println(multiCurve);
		//		System.out.println(averageCurve);
		System.out.println("********* " + meta + " *********");
		System.out.println("Percent differences between MultiIMR value and averaged single values " +
				"(for each hazard curve x value):\n" + tracker1);
		System.out.println(perfectMatches+"/"+multiCurve.getNum()+" non zero curve points match EXACTLY");
		System.out.println("********************************************************");

	}

	private void testSingleIMRHazardCurve(ScalarIntensityMeasureRelationshipAPI imr,
			MultiIMR_Averaged_AttenRel multi,
			MultiIMR_Averaged_AttenRel multis,
			String imt, double period) throws RemoteException {

		setIMT(imr, imt, period);
		setIMT(multi, imt, period);
		setIMT(multis, imt, period);

		IMT_Info imtInfo = new IMT_Info();

		DiscretizedFuncAPI curve1 = imtInfo.getDefaultHazardCurve(imt);
		DiscretizedFuncAPI curve2 = imtInfo.getDefaultHazardCurve(imt);
		DiscretizedFuncAPI curve3 = imtInfo.getDefaultHazardCurve(imt);
		hc.getHazardCurve(curve1, site, imr, erf);
		hc.getHazardCurve(curve2, site, multi, erf);
		hc.getHazardCurve(curve3, site, multis, erf);

		String meta = "(hazard curve) IMR: " + imr.getShortName() + " IMT: " + imt;
		if (period >= 0)
			meta += " PERIOD: " + period;

		MinMaxAveTracker tracker1 = new MinMaxAveTracker();
		MinMaxAveTracker tracker2 = new MinMaxAveTracker();

		for (int j=0; j<curve1.getNum(); j++) {

			double y1 = curve1.getY(j);
			double y2 = curve2.getY(j);
			double y3 = curve3.getY(j);

			double diff1 = DataUtils.getPercentDiff(y1, y2);
			double diff2 = DataUtils.getPercentDiff(y2, y3);
			tracker1.addValue(diff1);
			tracker2.addValue(diff2);
			String vals = y1 + ", " + y2 + ", PDIFF1: " + diff1 + ", " + y3 + ", PDIFF2: " + diff2;
			assertTrue(meta+" PDiff1 greater than "+max_curve_pt_diff+"\n"+vals, diff1 < max_curve_pt_diff);
			assertTrue(meta+" PDiff2 greater than "+max_curve_pt_diff+"\n"+vals, diff2 < max_curve_pt_diff);
		}
		System.out.println("********* " + meta + " *********");
		System.out.println("compare single with multi(1):\t" + tracker1);
		System.out.println("compare multi(1) with multi(2):\t" + tracker2);
		System.out.println("********************************************************");

	}

	protected static void setIMT(ScalarIntensityMeasureRelationshipAPI imr, String imt, double period) {
		imr.setIntensityMeasure(imt);
		if (period >= 0) {
			DependentParameterAPI<Double> imtParam = (DependentParameterAPI<Double>) imr.getIntensityMeasure();
			imtParam.getIndependentParameter(PeriodParam.NAME).setValue(period);
		}
	}

	@Test
	public void testCurveAverage_SA01() throws RemoteException {
		testCurveAverage(SA_Param.NAME, 0.1);
	}

	@Test
	public void testCurveAverage_SA10() throws RemoteException {
		testCurveAverage(SA_Param.NAME, 1.0);
	}

	@Test
	public void testCurveAverage_PGA() throws RemoteException {
		testCurveAverage(PGA_Param.NAME, -1.0);
	}

	private void testCurveAverage(String imt, double period) throws RemoteException {
		CB_2008_AttenRel cb08_master = new CB_2008_AttenRel(null);
		CB_2008_AttenRel cb08_multi = new CB_2008_AttenRel(null);
		BA_2008_AttenRel ba08_master = new BA_2008_AttenRel(null);
		BA_2008_AttenRel ba08_multi = new BA_2008_AttenRel(null);
		cb08_master.setParamDefaults();
		cb08_multi.setParamDefaults();
		ba08_master.setParamDefaults();
		ba08_multi.setParamDefaults();

		IMT_Info imtInfo = new IMT_Info();

		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		imrs.add(cb08_multi);
		imrs.add(ba08_multi);

		MultiIMR_Averaged_AttenRel multiIMR = new MultiIMR_Averaged_AttenRel(imrs);

		setIMT(multiIMR, imt, period);
		setIMT(cb08_master, imt, period);
		setIMT(ba08_master, imt, period);

		DiscretizedFuncAPI multiFunc = imtInfo.getDefaultHazardCurve(imt);
		DiscretizedFuncAPI cb08Func = imtInfo.getDefaultHazardCurve(imt);
		DiscretizedFuncAPI ba08Func = imtInfo.getDefaultHazardCurve(imt);

		hc.getHazardCurve(multiFunc, site, multiIMR, erf);
		hc.getHazardCurve(cb08Func, site, cb08_master, erf);
		hc.getHazardCurve(ba08Func, site, ba08_master, erf);

		int numVals = multiFunc.getNum();
		int numEqualCB = 0;
		int numEqualBA = 0;

		for (int i=0; i<numVals; i++) {
			double multiVal = multiFunc.getY(i);
			double cbVal = cb08Func.getY(i);
			double baVal = ba08Func.getY(i);

			if (multiVal != 0 && multiVal == cbVal)
				numEqualCB++;
			if (multiVal != 0 && multiVal == baVal)
				numEqualBA++;
			double avgVal = (cbVal + baVal) / 2d;
			assertEquals("average not within 0.01 of multi val!", avgVal, multiVal, 0.01);
		}
		assertTrue("averaged curve matches CB curve in " + numEqualCB + " places!", numEqualCB < 3);
		assertTrue("averaged curve matches BA curve in " + numEqualBA + " places!", numEqualBA < 3);
	}

}
