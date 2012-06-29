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
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;


/**
 * The reads Karen's MFD files for All Ca, No. Ca., So. Ca., LA box and SF box and provides 
 * target MFD_InversionConstraints for these regions.
 * 
 * To Do:
 * 
 * 1) solve the following (I sent email to Karen):
 * 
 * 			LongTermModelFull.txt is identical to LongTermModelNoCal.txt
 * 			DirectCountsNoCal.txt is identical to DirectCountsSoCal.txt
 * 
 * 2) add getters and setters for the various MFDs.
 * 3) more tests (beyond those in the main method here)?
 * 
 * @author field
 *
 */
public class UCERF3_Observed_MFD_Fetcher {
	
	final static boolean D=true;
	
	private final static String SUB_DIR_NAME = "mfdData";
	
	// discretization params for Karen's MFD files:
	final static double MIN_MAG=4.25;
	final static double MAX_MAG=7.75;
	final static int NUM_MAG=8;
	final static double DELTA_MAG = (MAX_MAG-MIN_MAG)/((double)NUM_MAG-1.0);
	
	final static double TARGET_MIN_MAG=5.05;
	final static int TARGET_NUM_MAG=35;
	final static double TARGET_DELTA_MAG=0.1;
	final static double TARGET_MAX_MAG=TARGET_MIN_MAG+TARGET_DELTA_MAG*(TARGET_NUM_MAG-1);

	final static double TARGET_B_VALUE = 1.0;	
	
	IncrementalMagFreqDist longTermModelFull, longTermModelLA, longTermModelNoCal, longTermModelSF, longTermModelSoCal;
	IncrementalMagFreqDist directCountsFull, directCountsLA, directCountsNoCal, directCountsSF, directCountsSoCal;
	IncrementalMagFreqDist directCountsFull_Lower95, directCountsLA_Lower95, directCountsNoCal_Lower95, directCountsSF_Lower95, directCountsSoCal_Lower95;
	IncrementalMagFreqDist directCountsFull_Upper95, directCountsLA_Upper95, directCountsNoCal_Upper95, directCountsSF_Upper95, directCountsSoCal_Upper95;

	
	public enum Area {

		ALL_CA,
		NO_CA,
		SO_CA,
		LA_BOX,
		SF_BOX
		
		}
	
	public UCERF3_Observed_MFD_Fetcher() {
		
		// DATA FILES
//		DirectCountsFull.txt
//		DirectCountsLA.txt
//		DirectCountsNoCal.txt
//		DirectCountsSF.txt
//		DirectCountsSoCal.txt
//		LongTermModelFull.txt
//		LongTermModelLA.txt
//		LongTermModelNoCal.txt
//		LongTermModelSF.txt
//		LongTermModelSoCal.txt
		
		// Read all the files
		ArrayList<IncrementalMagFreqDist> mfds;

		mfds = readMFD_DataFromFile("LongTermModelFull.txt");
		longTermModelFull = mfds.get(0);
		mfds = readMFD_DataFromFile("LongTermModelLA.txt");
		longTermModelLA = mfds.get(0);
		mfds = readMFD_DataFromFile("LongTermModelNoCal.txt");
		longTermModelNoCal = mfds.get(0); 
		mfds = readMFD_DataFromFile("LongTermModelSF.txt");
		longTermModelSF = mfds.get(0); 
		mfds = readMFD_DataFromFile("LongTermModelSoCal.txt");
		longTermModelSoCal = mfds.get(0);
		
		mfds = readMFD_DataFromFile("DirectCountsFull.txt");
		directCountsFull = mfds.get(0);
		directCountsFull_Lower95 = mfds.get(1);
		directCountsFull_Upper95 = mfds.get(2);
		mfds = readMFD_DataFromFile("DirectCountsLA.txt");
		directCountsLA = mfds.get(0);
		directCountsLA_Lower95 = mfds.get(1);
		directCountsLA_Upper95 = mfds.get(2);
		mfds = readMFD_DataFromFile("DirectCountsNoCal.txt");
		directCountsNoCal = mfds.get(0);
		directCountsNoCal_Lower95 = mfds.get(1);
		directCountsNoCal_Upper95 = mfds.get(2);
		mfds = readMFD_DataFromFile("DirectCountsSF.txt");
		directCountsSF = mfds.get(0);
		directCountsSF_Lower95 = mfds.get(1);
		directCountsSF_Upper95 = mfds.get(2);
		mfds = readMFD_DataFromFile("DirectCountsSoCal.txt");
		directCountsSoCal = mfds.get(0);
		directCountsSoCal_Lower95 = mfds.get(1);
		directCountsSoCal_Upper95 = mfds.get(2);

	}
	
	public static MFD_InversionConstraint getTargetMFDConstraint(Area area) {
		
		Region region=null;
		ArrayList<IncrementalMagFreqDist> mfds=null;
		switch(area) {
		case ALL_CA: 
			mfds = readMFD_DataFromFile("LongTermModelFull.txt");
			region = new CaliforniaRegions.RELM_TESTING();
			break;
		case NO_CA: 
			mfds = readMFD_DataFromFile("LongTermModelNoCal.txt");
			region = new CaliforniaRegions.RELM_NOCAL();
			break;
		case SO_CA: 
			mfds = readMFD_DataFromFile("LongTermModelSoCal.txt");
			region = new CaliforniaRegions.RELM_SOCAL();
			break;
		case SF_BOX:
			mfds = readMFD_DataFromFile("LongTermModelSF.txt");
			region = new CaliforniaRegions.SF_BOX_GRIDDED();
			break;
		case LA_BOX:
			mfds = readMFD_DataFromFile("LongTermModelLA.txt");
			region = new CaliforniaRegions.LA_BOX_GRIDDED();
			break;
		}
		
		double totalTargetRate = mfds.get(0).getCumRate(TARGET_MIN_MAG-TARGET_DELTA_MAG/2.0+DELTA_MAG/2);
		
		GutenbergRichterMagFreqDist targetMFD = new GutenbergRichterMagFreqDist(TARGET_B_VALUE,totalTargetRate,
				TARGET_MIN_MAG,TARGET_MAX_MAG,TARGET_NUM_MAG);
		
        if(D) {
//       	System.out.println("minTargetMagTest="+(TARGET_MIN_MAG-TARGET_DELTA_MAG/2.0+DELTA_MAG/2));
        	System.out.println(area+" totalTargetRate="+totalTargetRate+"\t"+(float)targetMFD.getTotCumRate());
//        	System.out.println("targetMFD=\n"+targetMFD);
        }
		
		return new MFD_InversionConstraint(targetMFD,region);
		
	}
	
	/**
	 * This reads the three cum MFDs from one of Karen's files
	 * @param fileName
	 * @return
	 */
	private static ArrayList<IncrementalMagFreqDist> readMFD_DataFromFile(String fileName) {
		IncrementalMagFreqDist mfdMean = new IncrementalMagFreqDist(MIN_MAG,MAX_MAG,NUM_MAG);
		IncrementalMagFreqDist mfdLower95Conf = new IncrementalMagFreqDist(MIN_MAG,MAX_MAG,NUM_MAG);
		IncrementalMagFreqDist mfdUpper95Conf = new IncrementalMagFreqDist(MIN_MAG,MAX_MAG,NUM_MAG);
		
		try {
			BufferedReader reader = new BufferedReader(UCERF3_DataUtils.getReader(SUB_DIR_NAME, fileName));
			int l=0;
			String line;
			while ((line = reader.readLine()) != null) {
				String[] st = StringUtils.split(line," ");
				double magTest = MIN_MAG+l*DELTA_MAG;
				double mag = (Double.valueOf(st[0])+Double.valueOf(st[1]))/2;
				if(mag >= magTest+0.001 || mag <= magTest-0.001)
					throw new RuntimeException("mags are unequal: "+mag+"\t"+magTest);
				mfdMean.set(mag, Double.valueOf(st[2]));
				if(st.length > 3) {
					mfdLower95Conf.set(mag, Double.valueOf(st[3]));
					mfdUpper95Conf.set(mag, Double.valueOf(st[4]));					
				}
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

		ArrayList<IncrementalMagFreqDist> mfds = new ArrayList<IncrementalMagFreqDist>();
		mfds.add(mfdMean);
		mfds.add(mfdLower95Conf);
		mfds.add(mfdUpper95Conf);
		
		return mfds;
	}
	
	
	
	/**
	 * This plots the computed MFDs
	 */
	public void plotMFDs() {
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GREEN));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		
		// No Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs2 = new ArrayList<EvenlyDiscretizedFunc>();
		funcs2.add(longTermModelNoCal);
		funcs2.add(directCountsNoCal);
		funcs2.add(directCountsNoCal_Lower95);
		funcs2.add(directCountsNoCal_Upper95);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "No Cal Mag-Freq Dists", plotChars); 
		graph2.setX_AxisLabel("Mag");
		graph2.setY_AxisLabel("Rate");
		graph2.setY_AxisRange(1e-3, 500);
		graph2.setX_AxisRange(3.5, 8.0);
		graph2.setYLog(true);

		// No Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs3 = new ArrayList<EvenlyDiscretizedFunc>();
		funcs3.add(longTermModelSoCal);
		funcs3.add(directCountsSoCal);
		funcs3.add(directCountsSoCal_Lower95);
		funcs3.add(directCountsSoCal_Upper95);
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(funcs3, "So Cal Mag-Freq Dists", plotChars); 
		graph3.setX_AxisLabel("Mag");
		graph3.setY_AxisLabel("Rate");
		graph3.setY_AxisRange(1e-3, 500);
		graph3.setX_AxisRange(3.5, 8.0);
		graph3.setYLog(true);

		// All Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(longTermModelFull);
		funcs.add(directCountsFull);
		funcs.add(directCountsFull_Lower95);
		funcs.add(directCountsFull_Upper95);
		
		// add UCERF2 obs MFDs
		funcs.addAll(UCERF2.getObsCumMFD(true));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "All Cal Cum Mag-Freq Dists", plotChars); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setY_AxisRange(1e-3, 500);
		graph.setX_AxisRange(3.5, 8.0);
		graph.setYLog(true);
		

	}
	
	/**
	 * This plots the computed MFDs
	 */
	public void plotCumMFDs() {
		
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.GREEN));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.BLUE));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		// No Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs2 = new ArrayList<EvenlyDiscretizedFunc>();
		funcs2.add(longTermModelNoCal.getCumRateDistWithOffset());
		funcs2.add(directCountsNoCal.getCumRateDistWithOffset());
		funcs2.add(directCountsNoCal_Lower95.getCumRateDistWithOffset());
		funcs2.add(directCountsNoCal_Upper95.getCumRateDistWithOffset());
		funcs2.add(getTargetMFDConstraint(Area.NO_CA).getMagFreqDist().getCumRateDistWithOffset());
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "No Cal Mag-Freq Dists", plotChars); 
		graph2.setX_AxisLabel("Mag");
		graph2.setY_AxisLabel("Rate");
		graph2.setY_AxisRange(1e-3, 500);
		graph2.setX_AxisRange(3.5, 8.0);
		graph2.setYLog(true);

		// No Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs3 = new ArrayList<EvenlyDiscretizedFunc>();
		funcs3.add(longTermModelSoCal.getCumRateDistWithOffset());
		funcs3.add(directCountsSoCal.getCumRateDistWithOffset());
		funcs3.add(directCountsSoCal_Lower95.getCumRateDistWithOffset());
		funcs3.add(directCountsSoCal_Upper95.getCumRateDistWithOffset());
		funcs3.add(getTargetMFDConstraint(Area.SO_CA).getMagFreqDist().getCumRateDistWithOffset());
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(funcs3, "So Cal Mag-Freq Dists", plotChars); 
		graph3.setX_AxisLabel("Mag");
		graph3.setY_AxisLabel("Rate");
		graph3.setY_AxisRange(1e-3, 500);
		graph3.setX_AxisRange(3.5, 8.0);
		graph3.setYLog(true);

		// All Cal Plot
		ArrayList<EvenlyDiscretizedFunc> funcs = new ArrayList<EvenlyDiscretizedFunc>();
		funcs.add(longTermModelFull.getCumRateDistWithOffset());
		funcs.add(directCountsFull.getCumRateDistWithOffset());
		funcs.add(directCountsFull_Lower95.getCumRateDistWithOffset());
		funcs.add(directCountsFull_Upper95.getCumRateDistWithOffset());
		funcs.add(getTargetMFDConstraint(Area.ALL_CA).getMagFreqDist().getCumRateDistWithOffset());
		
		// add UCERF2 obs MFDs
		funcs.addAll(UCERF2.getObsCumMFD(true));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
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
				
//		System.out.println(getGarderKnoppoffFractAftershocksMDF());
	
		UCERF3_Observed_MFD_Fetcher test = new UCERF3_Observed_MFD_Fetcher();		
		test.plotCumMFDs();
	}

	
}
