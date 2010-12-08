package org.opensha.sha.imr.attenRelImpl.test;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.junit.Before;
import org.junit.Test;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.DoubleDiscreteParameter;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.WarningDoubleParameter;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.SimpleFaultData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.ZhaoEtAl_2006_AttenRel;
import org.opensha.sha.imr.param.EqkRuptureParams.FaultTypeParam;
import org.opensha.sha.imr.param.EqkRuptureParams.MagParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;
import org.opensha.sha.imr.param.PropagationEffectParams.WarningDoublePropagationEffectParameter;
import org.opensha.sha.util.TectonicRegionType;

public class ZhaoEtAl_2006_test {

	private static final double THRESH = 0.05;

	private static final boolean failWhenAbove = true;

	ZhaoEtAl_2006_AttenRel imr;
	StirlingGriddedSurface fltSurf;
	double mLa, mLo;
	WC1994_MagLengthRelationship magLenRel;
	double rake = 90.0;

	@Before
	public void setUp() {
		imr = new ZhaoEtAl_2006_AttenRel(null);
		imr.setParamDefaults();

		double aveDip = 90.0;
		double lowerSeisDepth = 5.0;
		double upperSeisDept = 10.0;

		// Create fault trace 
		FaultTrace ftrace = new FaultTrace("test");
		ftrace.add(new Location(45.00,10.00));
		ftrace.add(new Location(46.00,10.00));

		// Calculate magnitude from the fault trace 
		magLenRel = new WC1994_MagLengthRelationship();

		// Create fault surface 
		SimpleFaultData fltDat =  new SimpleFaultData(aveDip,upperSeisDept,lowerSeisDepth,ftrace);
		fltSurf = new StirlingGriddedSurface(fltDat,10.0);

		// Find hypocenter
		mLo = 0.0;
		mLa = 0.0;
		LocationList locl = fltSurf.getLocationList();
		Iterator<Location> iter = locl.iterator();
		double cnt = 0.0;
		while (iter.hasNext()){
			cnt++;
			mLo += iter.next().getLongitude();
			mLa += iter.next().getLatitude();
		}

		// Create the hypocenter location
		mLo = mLo/cnt;
		mLa = mLa/cnt;
	}

	private EqkRupture getRup(double mag, double depth) {
		Location hypo = new Location(mLa,mLo,depth);
		return new EqkRupture(mag,rake,fltSurf,hypo);
	}

	private void doTest(double mag, double distance, double depth, String siteType, String trt, String fochMech, String fName) {
		// Set the rupture
		EqkRupture rup = getRup(mag, depth);
		imr.setEqkRupture(rup);
		// Set site conditions 
		imr.getParameter(ZhaoEtAl_2006_AttenRel.SITE_TYPE_NAME).setValue(siteType);
		// Set tectonic region
		imr.getParameter(TectonicRegionTypeParam.NAME).setValue(trt);
		if (fochMech != null)
			imr.getParameter(FaultTypeParam.NAME).setValue(fochMech);
		// Magnitude
		((WarningDoubleParameter)imr.getParameter(MagParam.NAME)).setValueIgnoreWarning(new Double(mag));
		// Distance 
		((WarningDoublePropagationEffectParameter)imr.getParameter(DistanceRupParameter.NAME))
		.setValueIgnoreWarning(new Double(distance));

		System.out.println("Testing: "+fName);
		URL url = this.getClass().getResource("AttenRelResultSetFiles/ZhaoEtAl_2006/"+fName);

		checkResults(imr, readTable(url));
	}

	private static void checkResults(AttenuationRelationship imr, ArrayList<Double[]> dat) {

		ArrayList<Double> per = getPeriods(imr);

		// Maximum absolute difference
		double maxPDiffGM = 0;
		double maxPDiffSigma = 0;

		// Looping on the spectral ordinates
		for (int i = 0; i < per.size(); i++){	 
			double tmp = per.get(i);
			if (tmp == 0.0){
				imr.setIntensityMeasure(PGA_Param.NAME);
			} else {
				imr.setIntensityMeasure(SA_Param.NAME);
				imr.getParameter(PeriodParam.NAME).setValue(tmp);
			}
			double gmOpenSHA = Math.exp(imr.getMean());
			double sigmaOpenSHA = imr.getStdDev();
			double gmZhao = dat.get(i)[1];
			double sigmaZhao = dat.get(i)[3];

			double pDiffGM = DataUtils.getPercentDiff(gmOpenSHA, gmZhao);
			if (pDiffGM > maxPDiffGM)
				maxPDiffGM = pDiffGM;
			double pDiffSigma = DataUtils.getPercentDiff(sigmaOpenSHA, sigmaZhao);
			if (pDiffSigma > maxPDiffSigma)
				maxPDiffSigma = pDiffSigma;

			String gmDiffStr = "GM differs above thresh: gmOpenSHA="+gmOpenSHA+", gmZhao="+gmZhao
			+", pDiff="+pDiffGM;
			if (pDiffGM > THRESH) {
				if (failWhenAbove)
					fail(gmDiffStr);
			}

			String sigmaDiffStr = "Sigma differs above thresh: sigmaOpenSHA="+sigmaOpenSHA+", sigmaZhao="+sigmaZhao
			+", pDiff="+pDiffSigma;
			if (pDiffSigma > THRESH) {
				if (failWhenAbove)
					fail(sigmaDiffStr);
			}
		}

		System.out.println("max gm pdiff: " + maxPDiffGM);
		System.out.println("max sigma pdiff: " + maxPDiffSigma);
	}

	@Test
	public void testInterfaceRock() {
		double mag = 6.5;
		double distance = 22.3;
		double depth = 20.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_ROCK;
		String trt = TectonicRegionType.SUBDUCTION_INTERFACE.toString();
		String fochMech = null;

		String fName = "zhao_r22.3_m6.5_dep20.0_interf_site1.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);	
	}

	@Test
	public void testInterfaceHard() {
		double mag = 6.5;
		double distance = 22.3;
		double depth = 20.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_HARD_SOIL;
		String trt = TectonicRegionType.SUBDUCTION_INTERFACE.toString();
		String fochMech = null;

		String fName = "zhao_r22.3_m6.5_dep20.0_interf_site2.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);	
	}

	@Test
	public void testInterfaceMedium() {
		double mag = 6.5;
		double distance = 22.3;
		double depth = 20.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_MEDIUM_SOIL;
		String trt = TectonicRegionType.SUBDUCTION_INTERFACE.toString();
		String fochMech = null;

		String fName = "zhao_r22.3_m6.5_dep20.0_interf_site3.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);	
	}

	@Test
	public void testInterfaceSoft() {
		double mag = 6.5;
		double distance = 22.3;
		double depth = 20.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_SOFT_SOIL;
		String trt = TectonicRegionType.SUBDUCTION_INTERFACE.toString();
		String fochMech = null;

		String fName = "zhao_r22.3_m6.5_dep20.0_interf_site4.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);	
	}

	@Test
	public void testActiveRockReverse() {
		double mag = 6.5;
		double distance = 20.0;
		double depth = 10.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_ROCK;
		String trt = TectonicRegionType.ACTIVE_SHALLOW.toString();
		String fochMech = ZhaoEtAl_2006_AttenRel.FLT_FOC_MECH_REVERSE;

		String fName = "zhao_r20.0_m6.5_dep10.0_shallow_reverse_site1.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);	
	}

	@Test
	public void testActiveRockNormal() {
		double mag = 6.5;
		double distance = 20.0;
		double depth = 10.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_ROCK;
		String trt = TectonicRegionType.ACTIVE_SHALLOW.toString();
		String fochMech = ZhaoEtAl_2006_AttenRel.FLT_FOC_MECH_NORMAL;

		String fName = "zhao_r20.0_m6.5_dep10.0_shallow_normal_site1.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);
	}

	@Test
	public void testSlabRock() {
		double mag = 6.5;
		double distance = 22.3;
		double depth = 20.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_ROCK;
		String trt = TectonicRegionType.SUBDUCTION_SLAB.toString();
		String fochMech = null;

		String fName = "zhao_r22.3_m6.5_dep20.0_slab_site1.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);
	}

	@Test
	public void testInterfaceRockDep30() {
		double mag = 5.0;
		double distance = 30.0;
		double depth = 30.0;
		String siteType = ZhaoEtAl_2006_AttenRel.SITE_TYPE_ROCK;
		String trt = TectonicRegionType.SUBDUCTION_INTERFACE.toString();
		String fochMech = null;

		String fName = "zhao_r30.0_m5.0_dep30_interf_site1.dat";
		doTest(mag, distance, depth, siteType, trt, fochMech, fName);
	}

	/**
	 * 
	 * @param flepath
	 * @return
	 */
	public static ArrayList<Double[]> readTable(URL filepath){
		ArrayList<Double[]> dat = new ArrayList<Double[]>();
		String line;
		String[] strArr;
		int cnt = 0;

		// Try to read 'flepath'
		try {
			// Read lines
			for (String currentLine : FileUtils.loadFile(filepath)) {
				cnt++;
				//    				if (cnt != 1) {
				// Split string after cleaning
				line = currentLine.trim(); strArr = line.split("\\s+");
				Double[] lineDat = new Double[strArr.length];
				for (int i = 0; i < strArr.length; i++){
					lineDat[i] = Double.valueOf(strArr[i]).doubleValue();
				}
				dat.add(lineDat);
				//    				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Return the final array list 
		return dat;
	}

	private static ArrayList<Double> getPeriods(ScalarIntensityMeasureRelationshipAPI imr) {
		// Get the list of periods available for the selected IMR
		ArrayList<Double> per = new ArrayList<Double>();
		ListIterator<ParameterAPI<?>> it = imr.getSupportedIntensityMeasuresIterator();
		while(it.hasNext()){
			DependentParameterAPI tempParam = (DependentParameterAPI)it.next();
			if (tempParam.getName().equalsIgnoreCase(SA_Param.NAME)){
				ListIterator it1 = tempParam.getIndependentParametersIterator();
				while(it1.hasNext()){
					ParameterAPI independentParam = (ParameterAPI)it1.next();
					if (independentParam.getName().equalsIgnoreCase(PeriodParam.NAME)){
						ArrayList<Double> saPeriodVector = ((DoubleDiscreteParameter)independentParam).getAllowedDoubles();

						for (int h=0; h<saPeriodVector.size(); h++){
							if (h == 0 && saPeriodVector.get(h)>0.0){
								per.add(0.0);
							}
							per.add(saPeriodVector.get(h));
						}

					}
				}
			}
		}
		return per;
	}

}
