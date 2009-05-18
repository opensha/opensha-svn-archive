package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.analysis;

import java.util.ArrayList;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data.A_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data.UCERF1MfdReader;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.gui.A_FaultsMFD_Plotter;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

/**
 * It plots the MFDs from UCERF2.2 and compares them with MFDs from UCERF1
 * @author vipingupta
 *
 */
public class UCERF1ComparisonPlotter {
	private UCERF2 ucerf2;
	
	public UCERF1ComparisonPlotter(UCERF2 ucerf2) {
		this.ucerf2 = ucerf2;
	}
	
	public UCERF1ComparisonPlotter() {
		ucerf2 = new UCERF2();
	}
	
	
	//	plot all MFDs in one chart, but diff chart for diff faults
	public void plotA_FaultMFDs_forReport() {
		// FOR SEGMENTED MODEL

		// Default parameters
		ucerf2.setParamDefaults();
		ucerf2.updateForecast();
		ArrayList aFaultSourceGenerators = ucerf2.get_A_FaultSourceGenerators();
		A_FaultsFetcher aFaultsFetcher = ucerf2.getA_FaultsFetcher();
		int numA_Faults = aFaultSourceGenerators.size();
		ArrayList<String> faultNames = aFaultsFetcher.getAllFaultNames();
		// It holds incr rates for each A-Fault
		ArrayList<DiscretizedFuncList> aFaultIncrRateFuncList = new ArrayList<DiscretizedFuncList>();
		// It holds Cum Rates for each A-Fault
		ArrayList<DiscretizedFuncList> aFaultCumRateFuncList = new ArrayList<DiscretizedFuncList>();

		for(int i=0; i<numA_Faults; ++i) {
			aFaultIncrRateFuncList.add(new DiscretizedFuncList());
			aFaultCumRateFuncList.add(new DiscretizedFuncList());
		}
		String name = "Default Parameters";
		addToFuncListForReportPlots(aFaultIncrRateFuncList, aFaultCumRateFuncList, name);

		// Def Params w/ change Mag Area to Hanks Bakun
		ucerf2.setParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME, HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		name = "Def Params w/ change Mag Area to Hanks Bakun";
		addToFuncListForReportPlots(aFaultIncrRateFuncList, aFaultCumRateFuncList, name);

		// Def. params with High apriori model weight
		ucerf2.setParamDefaults();
		ucerf2.setParameter(UCERF2.REL_A_PRIORI_WT_PARAM_NAME, new Double(1e10));
		ucerf2.setParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME, new Double(0));
		ucerf2.setParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME, new Double(0));
		ucerf2.updateForecast();
		name = "Def. params with High apriori model weight";
		addToFuncListForReportPlots(aFaultIncrRateFuncList, aFaultCumRateFuncList, name);

		// Def. params with High apriori model weight & change Mag Area to Hanks Bakun
		ucerf2.setParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME, HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		name = "Def. params with High apriori model weight & change Mag Area to Hanks Bakun";
		addToFuncListForReportPlots(aFaultIncrRateFuncList, aFaultCumRateFuncList, name);


		// Def. Params with unegmented
		ucerf2.setParamDefaults();
		ucerf2.setParameter(UCERF2.RUP_MODEL_TYPE_NAME, UCERF2.UNSEGMENTED_A_FAULT_MODEL);
		ucerf2.updateForecast();
		name = "Def. Params with unegmented";
		addToFuncListForReportPlots(aFaultIncrRateFuncList, aFaultCumRateFuncList, name);

		// Def. Params with unegmented & change Mag Area to Hans Bakun
		ucerf2.setParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME, HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		name = "Def. Params with unegmented & change Mag Area to Hans Bakun";
		addToFuncListForReportPlots(aFaultIncrRateFuncList, aFaultCumRateFuncList, name);

		/* wt-ave MFD WT PROPOSED BY OTHER EXCOM MEMBERS FOLLOWING CONFERENCE CALL
		 aPriori_EllB 	0.225
		 aPriori_HB 		0.225
		 MoBal_EllB 		0.225
		 MoBal_HB 		0.225
		 Unseg_EllB 	0.05
		 Unseg_HB	0.05
		 */
		name  = "Wt Avg MFD";
		for(int i=0; i<aFaultIncrRateFuncList.size(); ++i) {
			DiscretizedFuncList funcList = aFaultIncrRateFuncList.get(i);
			IncrementalMagFreqDist wtAveMFD = (IncrementalMagFreqDist) ((IncrementalMagFreqDist)funcList.get(0)).deepClone();
			DiscretizedFuncAPI func = funcList.get(0);
			/*			
			for(int imag=0; imag<func.getNum(); ++imag) 
				wtAveMFD.set(func.getX(imag), 
						0.33*funcList.get(0).getY(imag) + 0.33*funcList.get(1).getY(imag) + 
						0.12*funcList.get(2).getY(imag)+ 0.12*funcList.get(3).getY(imag) + 
						0.05*funcList.get(4).getY(imag) + 0.05*funcList.get(5).getY(imag));
			 */
			for(int imag=0; imag<func.getNum(); ++imag) 
				wtAveMFD.set(func.getX(imag), 
						0.225*funcList.get(0).getY(imag) + 0.225*funcList.get(1).getY(imag) + 
						0.225*funcList.get(2).getY(imag) + 0.225*funcList.get(3).getY(imag) + 
						0.050*funcList.get(4).getY(imag) + 0.050*funcList.get(5).getY(imag));


			wtAveMFD.setName(name);
			aFaultIncrRateFuncList.get(i).add(wtAveMFD);
			EvenlyDiscretizedFunc cumMFD = wtAveMFD.getCumRateDist();
			cumMFD.setName(name);
			aFaultCumRateFuncList.get(i).add(cumMFD);
		}

		//UCERF1 MFD
		name = "UCERF1 MFD";
		for(int i=0; i<aFaultIncrRateFuncList.size(); ++i) {
			String faultName = faultNames.get(i);
			ArbitrarilyDiscretizedFunc ucerf1Rate = UCERF1MfdReader.getUCERF1IncrementalMFD(faultName);
			if(ucerf1Rate.getNum()==0) ucerf1Rate.set(0.0, 0.0);
			aFaultIncrRateFuncList.get(i).add(ucerf1Rate);
			ucerf1Rate.setName(name);
			ArbitrarilyDiscretizedFunc cumMFD = UCERF1MfdReader.getUCERF1CumMFD(faultName);
			if(cumMFD.getNum()==0) cumMFD.set(0.0, 0.0);
			cumMFD.setName(name);
			aFaultCumRateFuncList.get(i).add(cumMFD);
		}

		// PLOT INCR RATES
		for(int i=0; i<aFaultIncrRateFuncList.size(); ++i) {
			DiscretizedFuncList funcList = aFaultIncrRateFuncList.get(i);
			String faultName = faultNames.get(i);
			ArrayList funcArrayList = new ArrayList();
			funcArrayList.add(funcList.get(funcList.size()-1));
			funcArrayList.add(funcList.get(funcList.size()-2));
			for(int j=0; j<funcList.size()-2; ++j) funcArrayList.add(funcList.get(j));
			GraphWindow graphWindow= new GraphWindow(new A_FaultsMFD_Plotter(funcArrayList, false));
			graphWindow.setPlotLabel(faultName);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		}

		// PLOT CUM RATES
		for(int i=0; i<aFaultCumRateFuncList.size(); ++i) {
			DiscretizedFuncList funcList = aFaultCumRateFuncList.get(i);
			String faultName = faultNames.get(i);
			ArrayList funcArrayList = new ArrayList();
			funcArrayList.add(funcList.get(funcList.size()-1));
			funcArrayList.add(funcList.get(funcList.size()-2));
			for(int j=0; j<funcList.size()-2; ++j) funcArrayList.add(funcList.get(j));
			GraphWindow graphWindow= new GraphWindow(new A_FaultsMFD_Plotter(funcArrayList, true));
			graphWindow.setPlotLabel(faultName);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		}
	}
	
	
	/**
	 * Plot MFDs for San Gregorio, Greenville, Concord-Green Valley and Mt. Diablo
	 */
	public void plotB_FaultMFDs_forReport() {

		// Default parameters
		ucerf2.setParamDefaults();
		ucerf2.updateForecast();
		String []bFaultNames = { "San Gregorio Connected", "Greenville Connected", "Green Valley Connected", "Mount Diablo Thrust"};  
		int[] b_FaultIndices = new int[bFaultNames.length];
		ArrayList<UnsegmentedSource> bFaultSources = ucerf2.get_B_FaultSources();
		//find indices of B-Faults in the B-Fault sources list
		for(int i=0; i<bFaultNames.length; ++i) {
			String faultName = bFaultNames[i];
			for(int j=0; j<bFaultSources.size(); ++j) {
				if(bFaultSources.get(j).getFaultSegmentData().getFaultName().equalsIgnoreCase(faultName)) {
					b_FaultIndices[i] = j;
					break;
				}
			}
		}
		
		int numB_Faults = bFaultNames.length;
		
		// It holds incr rates for each B-Faults
		ArrayList<DiscretizedFuncList> bFaultIncrRateFuncList = new ArrayList<DiscretizedFuncList>();
		// It holds Cum Rates for each A-Fault
		ArrayList<DiscretizedFuncList> bFaultCumRateFuncList = new ArrayList<DiscretizedFuncList>();

		for(int i=0; i<numB_Faults; ++i) {
			bFaultIncrRateFuncList.add(new DiscretizedFuncList());
			bFaultCumRateFuncList.add(new DiscretizedFuncList());
		}
		// Default Parameters
		String name = "Default Parameters";
		addToB_FaultsPlottingList(b_FaultIndices, numB_Faults, bFaultIncrRateFuncList, bFaultCumRateFuncList, name);
		
		// Def Params w/ change Mag Area to Hanks Bakun
		ucerf2.setParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME, HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		name = "Def Params w/ change Mag Area to Hanks Bakun";
		addToB_FaultsPlottingList(b_FaultIndices, numB_Faults, bFaultIncrRateFuncList, bFaultCumRateFuncList, name);

		// Def Params with Mean Mag correction=-0.1
		ucerf2.setParamDefaults();
		ucerf2.setParameter(UCERF2.MEAN_MAG_CORRECTION, new Double(-0.1));
		ucerf2.updateForecast();
		name = "Def Params w/ change Mean Mag Correction to -0.1";
		addToB_FaultsPlottingList(b_FaultIndices, numB_Faults, bFaultIncrRateFuncList, bFaultCumRateFuncList, name);
		
		// HB Mag Area Rel and Mean Mag correction=-0.1
		ucerf2.setParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME, HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		name = "Def Params w/ change Mean Mag Correction to -0.1 and Mag Area to Hanks Bakun";
		addToB_FaultsPlottingList(b_FaultIndices, numB_Faults, bFaultIncrRateFuncList, bFaultCumRateFuncList, name);
		
		// Def Params with Mean Mag correction=0.1
		ucerf2.setParamDefaults();
		ucerf2.setParameter(UCERF2.MEAN_MAG_CORRECTION, new Double(0.1));
		ucerf2.updateForecast();
		name = "Def Params w/ change Mean Mag Correction to 0.1";
		addToB_FaultsPlottingList(b_FaultIndices, numB_Faults, bFaultIncrRateFuncList, bFaultCumRateFuncList, name);
		
		// HB Mag Area Rel and Mean Mag correction=0.1
		ucerf2.setParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME, HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		name = "Def Params w/ change Mean Mag Correction to 0.1 and Mag Area to Hanks Bakun";
		addToB_FaultsPlottingList(b_FaultIndices, numB_Faults, bFaultIncrRateFuncList, bFaultCumRateFuncList, name);
		
		/* wt-ave MFD WT 
		 EllB, 0.6 	0.3
		 HB, 0.6	0.3
		 EllB,-0.1	0.1
		 HB,-0.1	0.1
		 EllB,0.1	0.1
		 HB,0.1		0.1
		 */
		name  = "Wt Avg MFD";
		for(int i=0; i<bFaultIncrRateFuncList.size(); ++i) {
			DiscretizedFuncList funcList = bFaultIncrRateFuncList.get(i);
			IncrementalMagFreqDist wtAveMFD = (IncrementalMagFreqDist) ((IncrementalMagFreqDist)funcList.get(0)).deepClone();
			DiscretizedFuncAPI func = funcList.get(0);
			
			for(int imag=0; imag<func.getNum(); ++imag) 
				wtAveMFD.set(func.getX(imag), 
						0.3*funcList.get(0).getY(imag) + 0.3*funcList.get(1).getY(imag)+
						0.1*funcList.get(2).getY(imag) + 0.1*funcList.get(3).getY(imag)+
						0.1*funcList.get(4).getY(imag) + 0.1*funcList.get(4).getY(imag));


			wtAveMFD.setName(name);
			bFaultIncrRateFuncList.get(i).add(wtAveMFD);
			EvenlyDiscretizedFunc cumMFD = wtAveMFD.getCumRateDist();
			cumMFD.setName(name);
			bFaultCumRateFuncList.get(i).add(cumMFD);
		}

		//UCERF1 MFD
		name = "UCERF1 MFD";
		for(int i=0; i<bFaultIncrRateFuncList.size(); ++i) {
			String faultName = bFaultNames[i];
			ArbitrarilyDiscretizedFunc ucerf1Rate = UCERF1MfdReader.getUCERF1IncrementalMFD(faultName);
			if(ucerf1Rate.getNum()==0) ucerf1Rate.set(0.0, 0.0);
			bFaultIncrRateFuncList.get(i).add(ucerf1Rate);
			ucerf1Rate.setName(name);
			ArbitrarilyDiscretizedFunc cumMFD = UCERF1MfdReader.getUCERF1CumMFD(faultName);
			if(cumMFD.getNum()==0) cumMFD.set(0.0, 0.0);
			cumMFD.setName(name);
			bFaultCumRateFuncList.get(i).add(cumMFD);
		}

		// PLOT INCR RATES
		for(int i=0; i<bFaultIncrRateFuncList.size(); ++i) {
			DiscretizedFuncList funcList = bFaultIncrRateFuncList.get(i);
			String faultName = bFaultNames[i];
			ArrayList funcArrayList = new ArrayList();
			funcArrayList.add(funcList.get(funcList.size()-1));
			funcArrayList.add(funcList.get(funcList.size()-2));
			//for(int j=0; j<funcList.size()-2; ++j) funcArrayList.add(funcList.get(j));
			GraphWindow graphWindow= new GraphWindow(new A_FaultsMFD_Plotter(funcArrayList, false));
			graphWindow.setPlotLabel(faultName);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		}

		// PLOT CUM RATES
		for(int i=0; i<bFaultCumRateFuncList.size(); ++i) {
			DiscretizedFuncList funcList = bFaultCumRateFuncList.get(i);
			String faultName = bFaultNames[i];
			ArrayList funcArrayList = new ArrayList();
			funcArrayList.add(funcList.get(funcList.size()-1));
			funcArrayList.add(funcList.get(funcList.size()-2));
			//for(int j=0; j<funcList.size()-2; ++j) funcArrayList.add(funcList.get(j));
			GraphWindow graphWindow= new GraphWindow(new A_FaultsMFD_Plotter(funcArrayList, true));
			graphWindow.setPlotLabel(faultName);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		}
	}

	private void addToB_FaultsPlottingList(int[] b_FaultIndices, int numB_Faults, ArrayList<DiscretizedFuncList> bFaultIncrRateFuncList, ArrayList<DiscretizedFuncList> bFaultCumRateFuncList, String name) {
		ArrayList<UnsegmentedSource> bFaultSources = ucerf2.get_B_FaultSources();
		for(int i=0; i<numB_Faults; ++i) {
			IncrementalMagFreqDist incrMFD = bFaultSources.get(b_FaultIndices[i]).getMagFreqDist();
			incrMFD.setName(name);
			incrMFD.setInfo("");
			EvenlyDiscretizedFunc cumMFD = incrMFD.getCumRateDist();
			cumMFD.setName(name);
			cumMFD.setInfo("");
			bFaultIncrRateFuncList.get(i).add(incrMFD);
			bFaultCumRateFuncList.get(i).add(cumMFD);
		}
	}

	/**
	 * Add the MFD and Cum MFD to list for creating figures for report
	 * 
	 * @param aFaultIncrRateFuncList
	 * @param aFaultCumRateFuncList
	 * @param name
	 */
	private void addToFuncListForReportPlots(ArrayList<DiscretizedFuncList> aFaultIncrRateFuncList, 
			ArrayList<DiscretizedFuncList> aFaultCumRateFuncList, String name) {
		IncrementalMagFreqDist incrMFD;
		EvenlyDiscretizedFunc cumMFD;
		String modelType = (String)ucerf2.getParameter(UCERF2.RUP_MODEL_TYPE_NAME).getValue();
		boolean isUnsegmented = false;
		if(modelType.equalsIgnoreCase(UCERF2.UNSEGMENTED_A_FAULT_MODEL)) isUnsegmented = true;
		boolean isSanJacinto;
		ArrayList aFaultSourceGenerators = ucerf2.get_A_FaultSourceGenerators();
		for(int i=0; i<aFaultSourceGenerators.size(); ++i) {
			isSanJacinto = false;
			Object obj = aFaultSourceGenerators.get(i);
			if(obj instanceof A_FaultSegmentedSourceGenerator) {
				// segmented source
				incrMFD =( (A_FaultSegmentedSourceGenerator)obj).getTotalRupMFD();

			} else {
				// unsegmented source
				incrMFD =( (UnsegmentedSource)obj).getMagFreqDist();

				if(i==2) { // combined the 2 faults for San Jacinto
					String faultName1 = ( (UnsegmentedSource)obj).getFaultSegmentData().getFaultName();
					String faultName2 = ( (UnsegmentedSource)aFaultSourceGenerators.get(i+1)).getFaultSegmentData().getFaultName();
					if(!faultName1.equalsIgnoreCase("San Jacinto (SB to C)") || !faultName2.equalsIgnoreCase("San Jacinto (CC to SM)"))
						throw new RuntimeException("Invalid combination of San Jacinto faults");
					isSanJacinto = true;

					IncrementalMagFreqDist incrMFD2 = ( (UnsegmentedSource)aFaultSourceGenerators.get(i+1)).getMagFreqDist();
					((SummedMagFreqDist)incrMFD).addIncrementalMagFreqDist(incrMFD2);
				}

			}
			incrMFD.setName(name);
			cumMFD = incrMFD.getCumRateDist();
			cumMFD.setName(name);
			incrMFD.setInfo("");
			cumMFD.setInfo("");
			if(isUnsegmented && i>2) {
				aFaultIncrRateFuncList.get(i-1).add(incrMFD);
				aFaultCumRateFuncList.get(i-1).add(cumMFD);
			} else {
				aFaultIncrRateFuncList.get(i).add(incrMFD);
				aFaultCumRateFuncList.get(i).add(cumMFD);
			}
			if(isSanJacinto) ++i; // skip next section for San Jacinto as it as already been combined
		}
	}
	
	public static void main(String[] args) {
		UCERF1ComparisonPlotter ucerf1ComparisonPlotter = new UCERF1ComparisonPlotter();
		//ucerf1ComparisonPlotter.plotA_FaultMFDs_forReport();
		ucerf1ComparisonPlotter.plotB_FaultMFDs_forReport();
	}

}
