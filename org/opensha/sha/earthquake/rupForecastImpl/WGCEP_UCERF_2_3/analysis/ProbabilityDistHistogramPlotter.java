/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.region.EvenlyGriddedNoCalRegion;
import org.opensha.data.region.EvenlyGriddedSoCalRegion;
import org.opensha.data.region.EvenlyGriddedWG02_Region;
import org.opensha.data.region.GeographicRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * This class generates histogram plots for contribution to total probability from various logic tree branches
 * 
 * @author vipingupta
 *
 */
public class ProbabilityDistHistogramPlotter implements GraphWindowAPI {
	private final static double MIN_PROB= 0.025;
	private final static double MAX_PROB= 0.975;
	private final static double DELTA_PROB= 0.05;
	private final static int NUM_PROB= Math.round((float)((MAX_PROB-MIN_PROB)/DELTA_PROB))+1;
	private final static String X_AXIS_LABEL = "Probability";
	private final static String Y_AXIS_LABEL = "Contribution";
	private final static String PLOT_LABEL = "Probability Contribution";
	private final PlotCurveCharacterstics HISTOGRAM1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
			new Color(0,0,0), 2); // black
	private final PlotCurveCharacterstics HISTOGRAM2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
			Color.GREEN, 2); // Green
	private final PlotCurveCharacterstics STACKED_BAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.STACKED_BAR,
			new Color(0,0,0), 2); // black
	private final PlotCurveCharacterstics STACKED_BAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.STACKED_BAR,
			Color.GREEN, 2); // Green

	private ArrayList funcs;
	private ArrayList<PlotCurveCharacterstics> plottingCurveChars;
	private HSSFWorkbook workbook;

	private double mags[] = { 5.0, 6.0, 6.5, 6.7, 7.0, 7.5, 8.0};
	private String[] sources = { "A-Faults", "B-Faults", "Non-CA B-Faults", "C-Zones", "Background", "Total" };
	private UCERF2_TimeDependentEpistemicList ucerf2EpistemicList = new UCERF2_TimeDependentEpistemicList();
	private String []bFaultNames = { "San Gregorio Connected", "Greenville Connected", 
			"Green Valley Connected", "Mount Diablo Thrust"};
	private String []aFaultNames = { "Elsinore", "Garlock", "San Jacinto", "N. San Andreas", "S. San Andreas",
			"Hayward-Rodgers Creek", "Calaveras"};
	
	/**
	 * Plot histograms of probability contributions from various branches
	 * 
	 * @param minMag
	 */
	public void generateProbContributionsExcelSheet(double duration, String fileName, GeographicRegion region) {


		//	create a sheet that contains param settings for each logic tree branch
		workbook  = new HSSFWorkbook();
		createParamSettingsSheet();


		int numSources = sources.length; // A-Faults, B-Faults, Non-CA B-Faults, Background, C-Zones, Total

		HSSFSheet allCA_RegionSheet = workbook.createSheet("Entire Region");
		HSSFRow row1, row2;
		
		
		// create sheet for each A-Fault
		for(int i=0; i<aFaultNames.length; ++i) {
			workbook.createSheet(aFaultNames[i]);
		}
		
		//create sheet for each B-Fault
		for(int i=0; i<bFaultNames.length; ++i) {
			workbook.createSheet(bFaultNames[i]);
		}

		// add column for each magnitude and each source
		row1 = allCA_RegionSheet.createRow(0);
		row2 = allCA_RegionSheet.createRow(1);
		for(int magIndex=0; magIndex<mags.length; ++magIndex) {
			int colIndex = magIndex*numSources+1;
			row1.createCell((short)(colIndex)).setCellValue(" Mag "+mags[magIndex]);
			for(int srcIndex=0; srcIndex<numSources; ++srcIndex) // each source for this magnitude
				row2.createCell((short)(colIndex+srcIndex)).setCellValue(sources[srcIndex]);
		}
		

		// create sheet for each A-Fault
		for(int i=0; i<aFaultNames.length; ++i) {
			row1 = workbook.getSheet(aFaultNames[i]).createRow(0);
			for(int magIndex=0; magIndex<mags.length; ++magIndex)
				row1.createCell((short)(magIndex+1)).setCellValue(" Mag "+mags[magIndex]);
		}
		
		//create sheet for each B-Fault
		for(int i=0; i<bFaultNames.length; ++i) {
			row1 = workbook.getSheet(bFaultNames[i]).createRow(0);
			for(int magIndex=0; magIndex<mags.length; ++magIndex)
				row1.createCell((short)(magIndex+1)).setCellValue(" Mag "+mags[magIndex]);

		}
		
		
		int numERFs = ucerf2EpistemicList.getNumERFs(); // number of logic tree branches
		//	now write the probability contribtuions of each source in each branch of logic tree
		int startRowIndex = 2;
		DiscretizedFuncAPI bckgroundProbs = null;
		DiscretizedFuncAPI cZoneProbs =null;
		for(int erfIndex=0; erfIndex<numERFs; ++erfIndex) {
			System.out.println("Doing run "+(erfIndex+1)+" of "+numERFs);
			row1 = allCA_RegionSheet.createRow(startRowIndex+erfIndex); 
			row1.createCell((short)0).setCellValue("Branch "+(erfIndex+1));
			UCERF2 ucerf2 = (UCERF2)ucerf2EpistemicList.getERF(erfIndex);
			ucerf2.getTimeSpan().setDuration(duration);
			ucerf2.updateForecast();
			
			if(bckgroundProbs==null) {
				bckgroundProbs = getDiscretizedFunc();
				cZoneProbs = getDiscretizedFunc();
				ucerf2.getTotal_C_ZoneProb(cZoneProbs, region);
				ucerf2.getTotal_BackgroundProb(bckgroundProbs, region);
			}
			DiscretizedFuncAPI aFaultsProbs = getDiscretizedFunc();
			DiscretizedFuncAPI bFaultsProbs = getDiscretizedFunc();
			DiscretizedFuncAPI nonCA_B_FaultsProbs = getDiscretizedFunc();
			DiscretizedFuncAPI totalProbs = getDiscretizedFunc();
			
			ucerf2.getTotal_A_FaultsProb(aFaultsProbs, region);
			ucerf2.getTotal_B_FaultsProb(bFaultsProbs, region);
			ucerf2.getTotal_NonCA_B_FaultsProb(nonCA_B_FaultsProbs, region);
			getTotalProb(totalProbs, aFaultsProbs, bFaultsProbs, nonCA_B_FaultsProbs, cZoneProbs, bckgroundProbs);
			
			for(int magIndex=0; magIndex<mags.length; ++magIndex) {
				int colIndex = magIndex*numSources+1;
				row1.createCell((short)(colIndex++)).setCellValue(aFaultsProbs.getY(magIndex));
				row1.createCell((short)(colIndex++)).setCellValue(bFaultsProbs.getY(magIndex));
				row1.createCell((short)(colIndex++)).setCellValue(nonCA_B_FaultsProbs.getY(magIndex));
				row1.createCell((short)(colIndex++)).setCellValue(cZoneProbs.getY(magIndex));
				row1.createCell((short)(colIndex++)).setCellValue(bckgroundProbs.getY(magIndex));
				row1.createCell((short)(colIndex++)).setCellValue(totalProbs.getY(magIndex));
			}	
			

			// create sheet for each A-Fault
			for(int i=0; i<aFaultNames.length; ++i) {
				DiscretizedFuncAPI aFaultProbDist = getDiscretizedFunc();
				row1 = workbook.getSheet(aFaultNames[i]).createRow(startRowIndex+erfIndex);
				row1.createCell((short)0).setCellValue("Branch "+(erfIndex+1));
				ucerf2.getProbForA_Fault(aFaultNames[i], aFaultProbDist, region);
				for(int magIndex=0; magIndex<mags.length; ++magIndex)
					row1.createCell((short)(magIndex+1)).setCellValue(aFaultProbDist.getY(magIndex));
			}
			
			//create sheet for each B-Fault
			for(int i=0; i<bFaultNames.length; ++i) {
				DiscretizedFuncAPI bFaultProbDist = getDiscretizedFunc();
				row1 = workbook.getSheet(bFaultNames[i]).createRow(startRowIndex+erfIndex);
				row1.createCell((short)0).setCellValue("Branch "+(erfIndex+1));
				ucerf2.getProbsForB_Fault(bFaultNames[i], bFaultProbDist, region);
				for(int magIndex=0; magIndex<mags.length; ++magIndex)
					row1.createCell((short)(magIndex+1)).setCellValue(bFaultProbDist.getY(magIndex));
			}
			
		}

		//		 write  excel sheet
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
			workbook.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void getTotalProb(DiscretizedFuncAPI totalProbs, 
			DiscretizedFuncAPI aFaultsProbs, 
			DiscretizedFuncAPI bFaultsProbs, DiscretizedFuncAPI nonCA_B_FaultsProbs, 
			DiscretizedFuncAPI cZoneProbs, DiscretizedFuncAPI bckgroundProbs) {
		int numMags = totalProbs.getNum();
		for(int i=0; i<numMags; ++i) {
			double prob = 1;
			prob *= 1-bFaultsProbs.getY(i);
			prob *= 1-nonCA_B_FaultsProbs.getY(i);
			prob *= 1-aFaultsProbs.getY(i);
			prob *= 1-bckgroundProbs.getY(i);
			prob *= 1-cZoneProbs.getY(i);
			totalProbs.set(i, 1.0-prob);
		}
	}
	
	
	/**
	 * Get discretized func
	 * @return
	 */
	private DiscretizedFuncAPI getDiscretizedFunc() {
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		for(int i=0; i<mags.length; ++i) func.set(mags[i], 1.0);
		return func;
	}

	/**
	 * Plot stacked histograms for BPT vs Empirical plots
	 * @param fileName
	 */
	public void plotEmpiricalBPT_ComparisonTotalProbPlot(double mag, String fileName) {
		int probModelColIndex = 25; // column index where Prob Model value is specified in file
		int weightColIndex = 26; // logic tree branch weight  index column
		
		ArrayList<Integer> bptBranchIndices = new ArrayList<Integer>();
		ArrayList<Integer> empiricalBranchIndices = new ArrayList<Integer>();
		
//		 Open the excel file
		try {
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(fileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet paramSettingsSheet = wb.getSheetAt(0); // whole Region
			int lastRowIndex = paramSettingsSheet.getLastRowNum();
			System.out.println("Last row num="+lastRowIndex);
		
			// fill the branch numbers for BPT (or Poisson) and Empirical
			for(int i=1; i<=lastRowIndex; ++i) {
				String probModel = paramSettingsSheet.getRow(i).getCell((short)probModelColIndex).getStringCellValue();
				if(probModel.equals(UCERF2.PROB_MODEL_BPT) || probModel.equals(UCERF2.PROB_MODEL_POISSON))
					bptBranchIndices.add(i);
				else empiricalBranchIndices.add(i);
			}
			
			/*System.out.println("BPT branches:----------------");
			for (int i=0; i<bptBranchIndices.size(); ++i)
				System.out.println(bptBranchIndices.get(i));
			
			System.out.println("Empirical branches:----------------");
			for (int i=0; i<empiricalBranchIndices.size(); ++i)
				System.out.println(empiricalBranchIndices.get(i));*/
			
			// BPT/Poisson
			EvenlyDiscretizedFunc bptFunc = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
			bptFunc.setInfo("Total Probability histogram plot for "+fileName+" for BPT/Poisson");
			bptFunc.setTolerance(DELTA_PROB);
		
			// Empirical
			EvenlyDiscretizedFunc empFunc = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
			empFunc.setInfo("Total Probability histogram plot for "+fileName+" for Empirical");
			empFunc.setTolerance(DELTA_PROB);
			
			int totProbColIndex=getTotalProbColIndexForMag(mag);
			HSSFSheet probSheet = wb.getSheetAt(1); // whole Region
			
			for (int i=0; i<bptBranchIndices.size(); ++i) { // populate BPT func
				int branchNum=bptBranchIndices.get(i);
				double wt= paramSettingsSheet.getRow(branchNum).getCell((short)weightColIndex).getNumericCellValue();
				double prob = probSheet.getRow(branchNum+1).getCell((short)totProbColIndex).getNumericCellValue();
				bptFunc.add(prob, wt);
			}
			
			for (int i=0; i<empiricalBranchIndices.size(); ++i) { // populate BPT func
				int branchNum=empiricalBranchIndices.get(i);
				double wt= paramSettingsSheet.getRow(branchNum).getCell((short)weightColIndex).getNumericCellValue();
				//System.out.println("Rowindex:"+(branchNum+i)+", colIndex="+totProbColIndex);
				double prob = probSheet.getRow(branchNum+1).getCell((short)totProbColIndex).getNumericCellValue();
				empFunc.add(prob, wt);
			}
			
			// plot histograms 
			funcs = new ArrayList();
			funcs.add(bptFunc);
			funcs.add(empFunc);
			plottingCurveChars = new ArrayList<PlotCurveCharacterstics>();
			plottingCurveChars.add(STACKED_BAR1);
			plottingCurveChars.add(STACKED_BAR2);
			GraphWindow graphWindow= new GraphWindow(this);
			graphWindow.setPlotLabel(PLOT_LABEL);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Plot Histogram of total Probs 
	 * 
	 * @param minMag
	 * @param fileName
	 */
	public void plotTotalProbHistogramsAboveMag(double minMag, String fileName) {

		
		int colIndex=getTotalProbColIndexForMag(minMag);

		// Open the excel file
		try {
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(fileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(1); // whole Region

			int startRowIndex = 2;
			EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
			func.setInfo("Total Probability histogram plot for "+fileName+" for Mag>="+minMag);
			func.setTolerance(DELTA_PROB);

			int numERFs = ucerf2EpistemicList.getNumERFs(); 
			for(int i=0; i<numERFs; ++i) {
				System.out.println("Doing run "+(i+1)+" of "+numERFs);
				double wt= ucerf2EpistemicList.getERF_RelativeWeight(i);
				double prob = sheet.getRow(startRowIndex+i).getCell((short)colIndex).getNumericCellValue();
				func.add(prob, wt);
			}

			funcs = new ArrayList();
			funcs.add(func);
			plottingCurveChars = new ArrayList<PlotCurveCharacterstics>();
			plottingCurveChars.add(this.HISTOGRAM1);
			GraphWindow graphWindow= new GraphWindow(this);
			graphWindow.setPlotLabel(PLOT_LABEL);
			graphWindow.plotGraphUsingPlotPreferences();
			graphWindow.setVisible(true);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Get Total Prob column index for a magnitiude
	 * 
	 * @param minMag
	 * @return
	 */
	private int getTotalProbColIndexForMag(double minMag) {
		boolean found = false;
		int colIndex=-1;
		// find the column based on the specfied magnitude
		for(int magIndex=0; magIndex<this.mags.length && !found; ++magIndex) {
			if(mags[magIndex]==minMag) {
				colIndex = (magIndex+1)* sources.length;
				found=true;
			}
		}
		if(!found) throw new RuntimeException("Invalid minimum magnitude. Only 5.0, 6.0, 6.5, 6.7, 7.0, 7.5, 8.0 are allowed");
		return colIndex;
	}


	/**
	 * create a sheet that lists parameter settings for each logic tree branch
	 *
	 */
	private void createParamSettingsSheet() {

		HSSFSheet paramSettingsSheet = workbook.createSheet("Parameter Settings");
		HSSFRow row;
		ParameterList adjustableParams = ucerf2EpistemicList.getParameterList(0);
		Iterator it = adjustableParams.getParametersIterator();
		ArrayList<String> adjustableParamNames = new ArrayList<String>();
		while(it.hasNext()) {
			ParameterAPI param = (ParameterAPI)it.next();
			adjustableParamNames.add(param.getName());
		}

		// add column for each parameter name. 
		row = paramSettingsSheet.createRow(0); 
		for(int i=1; i<=adjustableParamNames.size(); ++i) {
			row.createCell((short)i).setCellValue(adjustableParamNames.get(i-1));
		}

		int weightCol = adjustableParamNames.size()+1;
		row.createCell((short)(weightCol)).setCellValue("Branch Weight");
		
		int numERFs = ucerf2EpistemicList.getNumERFs(); // number of logic tree branches
		//		now write all the parameter settings for each branch in the excel sheet
		for(int i=0; i<numERFs; ++i) {
			row = paramSettingsSheet.createRow(i+1); 
			row.createCell((short)0).setCellValue("Branch "+(i+1));
			adjustableParams = ucerf2EpistemicList.getParameterList(i);
			for( int paramIndex=0; paramIndex<adjustableParamNames.size(); ++paramIndex) {
				String pName = adjustableParamNames.get(paramIndex);
				if(adjustableParams.containsParameter(pName))
					row.createCell((short)(paramIndex+1)).setCellValue(adjustableParams.getValue(pName).toString());
			}
			row.createCell((short)(weightCol)).setCellValue(ucerf2EpistemicList.getERF_RelativeWeight(i));

		}
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return funcs;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public boolean getXLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return X_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		return Y_AXIS_LABEL;
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList<PlotCurveCharacterstics> getPlottingFeatures() {
		return plottingCurveChars;
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		//return 5.0;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		//return 9.255;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		//return 1e-4;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		//return 10;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	public static void main(String[] args) {
		ProbabilityDistHistogramPlotter plotter = new ProbabilityDistHistogramPlotter();
		//plotter.generateProbContributionsExcelSheet(30, "ProbabilityContributions_30yrs_All.xls", null);
		//plotter.generateProbContributionsExcelSheet(30, "ProbabilityContributions_30yrs_WG02.xls", new EvenlyGriddedWG02_Region());
		//plotter.generateProbContributionsExcelSheet(30, "ProbabilityContributions_30yrs_NoCal.xls", new EvenlyGriddedNoCalRegion());
		//plotter.generateProbContributionsExcelSheet(30, "ProbabilityContributions_30yrs_SoCal.xls", new EvenlyGriddedSoCalRegion());
		//plotter.generateProbContributionsExcelSheet(5, "ProbabilityContributions_5yrs_All.xls", null);
		//plotter.generateProbContributionsExcelSheet(5, "ProbabilityContributions_5yrs_WG02.xls", new EvenlyGriddedWG02_Region());
		//plotter.generateProbContributionsExcelSheet(5, "ProbabilityContributions_5yrs_NoCal.xls", new EvenlyGriddedNoCalRegion());
		//plotter.generateProbContributionsExcelSheet(5, "ProbabilityContributions_5yrs_SoCal.xls", new EvenlyGriddedSoCalRegion());
		plotter.plotEmpiricalBPT_ComparisonTotalProbPlot(6.7, "ProbabilityContributions_30yrs_WG02.xls");
		//plotter.plotTotalProbHistogramsAboveMag(7.5, "ProbabilityContributions_30yrs_All.xls");
	}

}
