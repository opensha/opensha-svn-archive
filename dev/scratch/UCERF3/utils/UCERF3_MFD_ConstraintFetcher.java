package scratch.UCERF3.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;


/**
 * The reads Karen's MFD files for All Ca, No. Ca., and So. Ca., and provides 
 * target MFD_InversionConstraints for the specified time span (1850-2011 and 1984-2011) and region.
 * 
 * To Do:
 * 
 * 1) convert cum MFDs to incremental versions.
 * 2) add getters and setters for the various MFDs.
 * 3) more tests (beyond those in the main method here)?
 * 
 * @author field
 *
 */
public class UCERF3_MFD_ConstraintFetcher {
	
	final static boolean D=true;
	
	private final static String SUB_DIR_NAME = "mfdData";
	
	// discretization params for Karen's MFD files:
	final static double MIN_MAG=4.0;
	final static double MAX_MAG=7.5;
	final static int NUM_MAG=8;
	final static double DELTA_MAG = (MAX_MAG-MIN_MAG)/((double)NUM_MAG-1.0);
	
	final static double TARGET_MIN_MAG=5.05;
	final static int TARGET_NUM_MAG=35;
	final static double TARGET_DELTA_MAG=0.1;
	final static double TARGET_MAX_MAG=TARGET_MIN_MAG+TARGET_DELTA_MAG*(TARGET_NUM_MAG-1);

	final static double TARGET_B_VALUE = 1.0;	
	
	EvenlyDiscretizedFunc allCal1850_mean_cumMFD, allCal1984_mean_cumMFD, noCal1850_mean_cumMFD, noCal1984_mean_cumMFD, soCal1850_mean_cumMFD, soCal1984_mean_cumMFD;
	EvenlyDiscretizedFunc allCal1850_upper95_cumMFD, allCal1984_upper95_cumMFD, noCal1850_upper95_cumMFD, noCal1984_upper95_cumMFD, soCal1850_upper95_cumMFD, soCal1984_upper95_cumMFD;
	EvenlyDiscretizedFunc allCal1850_lower95_cumMFD, allCal1984_lower95_cumMFD, noCal1850_lower95_cumMFD, noCal1984_lower95_cumMFD, soCal1850_lower95_cumMFD, soCal1984_lower95_cumMFD;
	
	
	public enum TimeAndRegion {

		ALL_CA_1850,
		ALL_CA_1984,
		NO_CA_1850,
		NO_CA_1984,
		SO_CA_1850,
		SO_CA_1984,
		
		}
	
	public UCERF3_MFD_ConstraintFetcher() {
		
		// Read all the files
		ArrayList<EvenlyDiscretizedFunc> mfds;
		
		mfds = readMFD_DataFromFile("WholeRegion1850_2011_v1.txt");
		allCal1850_mean_cumMFD = mfds.get(0);
		allCal1850_lower95_cumMFD = mfds.get(1);
		allCal1850_upper95_cumMFD = mfds.get(2);

		mfds = readMFD_DataFromFile("WholeRegion1984_2011_v1.txt");
		allCal1984_mean_cumMFD = mfds.get(0);
		allCal1984_lower95_cumMFD = mfds.get(1);
		allCal1984_upper95_cumMFD = mfds.get(2);
		
		mfds = readMFD_DataFromFile("NoCal1850_2011_v1.txt");
		noCal1850_mean_cumMFD = mfds.get(0);
		noCal1850_lower95_cumMFD = mfds.get(1);
		noCal1850_upper95_cumMFD = mfds.get(2);

		mfds = readMFD_DataFromFile("NoCal1984_2011_v1.txt");
		noCal1984_mean_cumMFD = mfds.get(0);
		noCal1984_lower95_cumMFD = mfds.get(1);
		noCal1984_upper95_cumMFD = mfds.get(2);
		
		mfds = readMFD_DataFromFile("SoCal1850_2011_v1.txt");
		soCal1850_mean_cumMFD = mfds.get(0);
		soCal1850_lower95_cumMFD = mfds.get(1);
		soCal1850_upper95_cumMFD = mfds.get(2);

		mfds = readMFD_DataFromFile("SoCal1984_2011_v1.txt");
		soCal1984_mean_cumMFD = mfds.get(0);
		soCal1984_lower95_cumMFD = mfds.get(1);
		soCal1984_upper95_cumMFD = mfds.get(2);
		
	}
	
	public static MFD_InversionConstraint getTargetMFDConstraint(TimeAndRegion timeAndRegion) {
		
		Region region=null;
		ArrayList<EvenlyDiscretizedFunc> mfds=null;
		switch(timeAndRegion) {
		case ALL_CA_1850: 
			mfds = readMFD_DataFromFile("WholeRegion1850_2011_v1.txt");
			region = new CaliforniaRegions.RELM_TESTING();
			break;
		case ALL_CA_1984:
			mfds = readMFD_DataFromFile("WholeRegion1984_2011_v1.txt");
			region = new CaliforniaRegions.RELM_TESTING();			
			break;
		case NO_CA_1850: 
			mfds = readMFD_DataFromFile("NoCal1850_2011_v1.txt");
			region = new CaliforniaRegions.RELM_NOCAL();
;
			break;
		case NO_CA_1984:
			mfds = readMFD_DataFromFile("NoCal1984_2011_v1.txt");
			region = new CaliforniaRegions.RELM_NOCAL();	
			break;
		case SO_CA_1850: 
			mfds = readMFD_DataFromFile("SoCal1850_2011_v1.txt");
			region = new CaliforniaRegions.RELM_SOCAL();
			break;
		case SO_CA_1984:
			mfds = readMFD_DataFromFile("SoCal1984_2011_v1.txt");
			region = new CaliforniaRegions.RELM_SOCAL();
			break;
		}
		
		double totalTargetRate = mfds.get(0).getY(TARGET_MIN_MAG-TARGET_DELTA_MAG/2.0);
		
		GutenbergRichterMagFreqDist targetMFD = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,totalTargetRate,
				TARGET_MIN_MAG,TARGET_MAX_MAG,TARGET_NUM_MAG);
		
        if(D) {
 //       	System.out.println("minTargetMagTest="+(TARGET_MIN_MAG-TARGET_DELTA_MAG/2.0));
        	System.out.println(timeAndRegion+" totalTargetRate="+totalTargetRate+"\t"+(float)targetMFD.getTotCumRate());
//        	System.out.println("targetMFD=\n"+targetMFD);
        }
		
		return new MFD_InversionConstraint(targetMFD,region);
		
	}
	
	/**
	 * This reads the three cum MFDs from one of Karen's files
	 * @param fileName
	 * @return
	 */
	private static ArrayList<EvenlyDiscretizedFunc> readMFD_DataFromFile(String fileName) {
		EvenlyDiscretizedFunc mfdMean = new EvenlyDiscretizedFunc(MIN_MAG,MAX_MAG,NUM_MAG);
		EvenlyDiscretizedFunc mfdLower95Conf = new EvenlyDiscretizedFunc(MIN_MAG,MAX_MAG,NUM_MAG);
		EvenlyDiscretizedFunc mfdUpper95Conf = new EvenlyDiscretizedFunc(MIN_MAG,MAX_MAG,NUM_MAG);
		
		try {
			BufferedReader reader = new BufferedReader(
					UCERF3_DataUtils.getReader(SUB_DIR_NAME, fileName));
			int l=0;
			String line;
			while ((line = reader.readLine()) != null) {
				String[] st = StringUtils.split(line," ");
				double magTest = MIN_MAG+l*DELTA_MAG;
				double mag = Double.valueOf(st[0]);
				if(mag != magTest)
					throw new RuntimeException("mags are unequal: "+mag+"\t"+magTest);
				mfdMean.set(mag, Double.valueOf(st[1]));
				mfdLower95Conf.set(mag, Double.valueOf(st[2]));
				mfdUpper95Conf.set(mag, Double.valueOf(st[3]));
				l+=1;
			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		mfdMean.setName("Mean MFD from "+fileName);
		mfdLower95Conf.setName("Lower 95th Conf MFD from "+fileName);
		mfdUpper95Conf.setName("Upper 95th Conf MFD from "+fileName);
		mfdMean.setInfo(" ");
		mfdLower95Conf.setInfo(" ");
		mfdUpper95Conf.setInfo(" ");

		ArrayList<EvenlyDiscretizedFunc> mfds = new ArrayList<EvenlyDiscretizedFunc>();
		mfds.add(mfdMean);
		mfds.add(mfdLower95Conf);
		mfds.add(mfdUpper95Conf);
		
		return mfds;
	}
	
	public static void plotGR_MFDsForVariousMmax() {
		
		double totCumM4_Rate = readMFD_DataFromFile("WholeRegion1850_2011_v1.txt").get(0).getY(4.0);
		System.out.println(totCumM4_Rate);
		
		GutenbergRichterMagFreqDist targetMFD = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,1.0,0.05,9.95,100);
		targetMFD.scaleToCumRate(4.05, totCumM4_Rate);
//		System.out.println(targetMFD.getCumRateDist());
		
		GutenbergRichterMagFreqDist targetMmax815 = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,1.0,0.05,8.15,82);
		targetMmax815.scaleToCumRate(4.05, totCumM4_Rate);
		targetMmax815.setName("GR with Mmax=8.15");
//		System.out.println(targetMmax815);
		
		GutenbergRichterMagFreqDist targetMmax835 = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,1.0,0.05,8.35,84);
		targetMmax835.scaleToCumRate(4.05, totCumM4_Rate);
		targetMmax835.setName("GR with Mmax=8.35");
		
		GutenbergRichterMagFreqDist targetMmax855 = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,1.0,0.05,8.55,86);
		targetMmax855.scaleToCumRate(4.05, totCumM4_Rate);
		targetMmax855.setName("GR with Mmax=8.55");
		
		GutenbergRichterMagFreqDist targetMmax875 = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,1.0,0.05,8.75,88);
		targetMmax875.scaleToCumRate(4.05, totCumM4_Rate);
		targetMmax875.setName("GR with Mmax=8.75");
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();	
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GREEN));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.MAGENTA));
		
		// No Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(targetMFD.getCumRateDistWithOffset());
		funcs.add(targetMmax815.getCumRateDistWithOffset());
		funcs.add(targetMmax835.getCumRateDistWithOffset());
		funcs.add(targetMmax855.getCumRateDistWithOffset());
		funcs.add(targetMmax875.getCumRateDistWithOffset());
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Mag-Freq Dists", plotChars); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setY_AxisRange(1e-4, 10);
		graph.setX_AxisRange(5.0, 9.0);
		graph.setYLog(true);

		
	}
	
	
	/**
	 * This plots the computed MFDs
	 */
	public void plotCumMFDs() {
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();	
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLACK));
		
		// No Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs2 = new ArrayList<EvenlyDiscretizedFunc>();
		funcs2.add(noCal1850_mean_cumMFD);
		funcs2.add(noCal1850_lower95_cumMFD);
		funcs2.add(noCal1850_upper95_cumMFD);
		funcs2.add(noCal1984_mean_cumMFD);
		funcs2.add(noCal1984_lower95_cumMFD);
		funcs2.add(noCal1984_upper95_cumMFD);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "No Cal Cum Mag-Freq Dists", plotChars); 
		graph2.setX_AxisLabel("Mag");
		graph2.setY_AxisLabel("Rate");
		graph2.setY_AxisRange(1e-3, 500);
		graph2.setX_AxisRange(3.5, 8.0);
		graph2.setYLog(true);

		// So Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs3 = new ArrayList<EvenlyDiscretizedFunc>();
		funcs3.add(soCal1850_mean_cumMFD);
		funcs3.add(soCal1850_lower95_cumMFD);
		funcs3.add(soCal1850_upper95_cumMFD);
		funcs3.add(soCal1984_mean_cumMFD);
		funcs3.add(soCal1984_lower95_cumMFD);
		funcs3.add(soCal1984_upper95_cumMFD);
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(funcs3, "So Cal Cum Mag-Freq Dists", plotChars); 
		graph3.setX_AxisLabel("Mag");
		graph3.setY_AxisLabel("Rate");
		graph3.setY_AxisRange(1e-3, 500);
		graph3.setX_AxisRange(3.5, 8.0);
		graph3.setYLog(true);

		
		// All Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(allCal1850_mean_cumMFD);
		funcs.add(allCal1850_lower95_cumMFD);
		funcs.add(allCal1850_upper95_cumMFD);
		funcs.add(allCal1984_mean_cumMFD);
		funcs.add(allCal1984_lower95_cumMFD);
		funcs.add(allCal1984_upper95_cumMFD);
		
		// add UCERF2 obs MFDs
		funcs.addAll(UCERF2.getObsCumMFD(true));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
//		// Add target we've been working with
//		Region region = new CaliforniaRegions.RELM_GRIDDED();
//		UCERF2_MFD_ConstraintFetcher fetcher = new UCERF2_MFD_ConstraintFetcher(region);
//		funcs.add(fetcher.getTargetMFDConstraint().getMagFreqDist().getCumRateDistWithOffset());
//		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.MAGENTA));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "All Cal Cum Mag-Freq Dists", plotChars); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setY_AxisRange(1e-3, 500);
		graph.setX_AxisRange(3.5, 8.0);
		graph.setYLog(true);

	}
	
	
	/**
	 * This returns the fraction of aftershocks as a function of magnitude 
	 * implied by Table 21 of UCERF2 Appendix I (where cumulative distributions were
	 * first converted to incremental).
	 * @return
	 */
	private static EvenlyDiscretizedFunc getGarderKnoppoffFractAftershocksMDF() {
		EvenlyDiscretizedFunc withAftCum = UCERF2.getObsCumMFD(true).get(0);
		EvenlyDiscretizedFunc noAftCum = UCERF2.getObsCumMFD(false).get(0);
		double min = noAftCum.getX(0)+noAftCum.getDelta()/2.0;
		double max = noAftCum.getX(noAftCum.getNum()-1)-noAftCum.getDelta()/2.0;
		EvenlyDiscretizedFunc fractFunc = new EvenlyDiscretizedFunc(min, max, noAftCum.getNum()-1);
		for(int i=0;i<withAftCum.getNum()-1;i++) {
			double mag = (withAftCum.getX(i)+withAftCum.getX(i+1))/2;
			double with = withAftCum.getY(i)-withAftCum.getY(i+1);
			double wOut = noAftCum.getY(i)-noAftCum.getY(i+1);
			double frac = (with-wOut)/with;
			if(frac<0) frac=0;
//			System.out.println(mag+"\t"+frac);
			fractFunc.set(i,frac);
		}

//		System.out.println(fractFunc);
		
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(fractFunc, "Fract aftershocks"); 
		
		return fractFunc;

		
	}

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		plotGR_MFDsForVariousMmax();
		
//		System.out.println(getGarderKnoppoffFractAftershocksMDF());
		
//		MFD_InversionConstraint invConstr = UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850);
//		UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1984);
//		UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.NO_CA_1850);
//		UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.NO_CA_1984);
//		UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.SO_CA_1850);
//		UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.SO_CA_1984);
	
//		UCERF3_MFD_ConstraintFetcher test = new UCERF3_MFD_ConstraintFetcher();		
//		test.plotCumMFDs();
	}

	
}