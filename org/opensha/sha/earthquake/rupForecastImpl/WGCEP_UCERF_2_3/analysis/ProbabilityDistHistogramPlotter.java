/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
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
	private final static String PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/analysis/files/";
	private final static double MIN_PROB= 0.025;
	private final static double MAX_PROB= 0.975;
	private final static double DELTA_PROB= 0.05;
	private final static int NUM_PROB= Math.round((float)((MAX_PROB-MIN_PROB)/DELTA_PROB))+1;
	private final static String X_AXIS_LABEL = "Probability";
	private final static String Y_AXIS_LABEL = "Contribution";
	private final static String PLOT_LABEL = "Probability Contribution";
	private final PlotCurveCharacterstics HISTOGRAM1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
			new Color(0,0,0), 2); // black
	
	private final PlotCurveCharacterstics STACKED_BAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.STACKED_BAR,
			new Color(0,0,0), 2); // black
	private final PlotCurveCharacterstics STACKED_BAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.STACKED_BAR,
			Color.GREEN, 2); // Green

	private ArrayList funcs;
	private ArrayList<PlotCurveCharacterstics> plottingCurveChars;
	private HSSFWorkbook workbook;

	private double mags[] = { 5.0, 6.0, 6.5, 6.7, 7.0, 7.5, 8.0};
	public final static String A_FAULTS = "A-Faults";
	public final static String B_FAULTS = "B-Faults";
	public final static String NON_CA_B_FAULTS = "Non-CA B-Faults";
	public final static String C_ZONES = "C-Zones";
	public final static String BACKGROUND = "Background";
	public final static String TOTAL = "Total";
	private String[] sources = { A_FAULTS, B_FAULTS, NON_CA_B_FAULTS, C_ZONES, BACKGROUND, TOTAL };
	private UCERF2_TimeDependentEpistemicList ucerf2EpistemicList;
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

		if(ucerf2EpistemicList==null)
			ucerf2EpistemicList = new UCERF2_TimeDependentEpistemicList();
		
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
	
	/**
	 * Calculate Total prob
	 * @param totalProbs
	 * @param aFaultsProbs
	 * @param bFaultsProbs
	 * @param nonCA_B_FaultsProbs
	 * @param cZoneProbs
	 * @param bckgroundProbs
	 */
	private void getTotalProb(DiscretizedFuncAPI totalProbs, 
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
	public void plotEmpiricalBPT_ComparisonProbPlot(double mag, String fileName, String sourceType) {
	
		
		ArrayList<Integer> bptBranchIndices;
		ArrayList<Integer> empiricalBranchIndices;
		
//		 Open the excel file
		try {
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(fileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet paramSettingsSheet = wb.getSheetAt(0); // whole Region
			// BPT and Poisson
			bptBranchIndices = getBranchIndices(paramSettingsSheet, UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_BPT);
			bptBranchIndices.addAll(getBranchIndices(paramSettingsSheet, UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON));
			// Empirical
			empiricalBranchIndices = getBranchIndices(paramSettingsSheet, UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_EMPIRICAL);
					
			int totProbColIndex=getColIndexForMagAndSource(mag, sourceType);
			HSSFSheet probSheet = wb.getSheetAt(1); // whole Region
			
			EvenlyDiscretizedFunc bptFunc = getFunc("Histogram Plot for BPT/Poisson", bptBranchIndices, paramSettingsSheet, totProbColIndex, probSheet);
			EvenlyDiscretizedFunc empFunc = getFunc("Histogram Plot for Empirical", empiricalBranchIndices, paramSettingsSheet, totProbColIndex, probSheet);
			
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
	 * It reads the input file as created by generateProbContributionsExcelSheet() method
	 * and generates min, max, mean and a histogram function for each column in the sheet.
	 * These values are then saved in a separate excel sheet
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 */
	public void mkAvgMinMaxSheet(String inputFileName, String outputFileName) {
		
		int lastColIndex;

		// Open the excel file
		try {
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(inputFileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			
			int numSheets = wb.getNumberOfSheets();
			
			// list of all branch indices
			ArrayList<Integer>  bIndices= getBranchIndices(wb.getSheetAt(0),  UCERF2.PROB_MODEL_PARAM_NAME,  null);
	
			//	create a sheet that contains param settings for each logic tree branch
			HSSFWorkbook newWorkbook  = new HSSFWorkbook();
			
			//	do for each sheet except parameter Settings sheet
			for(int sheetIndex=1; sheetIndex<numSheets; ++sheetIndex) { 
				HSSFSheet origSheet = wb.getSheetAt(sheetIndex); 
				if(sheetIndex==1) lastColIndex = this.mags.length*this.sources.length;
				else lastColIndex = mags.length;
				HSSFSheet newSheet = newWorkbook.createSheet(wb.getSheetName(sheetIndex));
				
				int rowIndex=0;
				
				EvenlyDiscretizedFunc xValuesFunc = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
				// copy first 2 rows from original sheet to final sheet
				for(; rowIndex<2; ++rowIndex) {
					HSSFRow newRow = newSheet.createRow(rowIndex);
					HSSFRow origRow = origSheet.getRow(rowIndex);
					if(origRow == null) continue;
					for(int colIndex=0; colIndex<=lastColIndex; ++colIndex) {
						HSSFCell origCell = origRow.getCell((short)colIndex);
						if(origCell==null) continue;
						newRow.createCell((short)colIndex).setCellValue(origCell.getStringCellValue());
					}
				}
				
				rowIndex = 2;
				// create min/max/avg rows
				newSheet.createRow(rowIndex++).createCell((short)0).setCellValue("Min");
				newSheet.createRow(rowIndex++).createCell((short)0).setCellValue("Max");
				newSheet.createRow(rowIndex++).createCell((short)0).setCellValue("Avg");
				
				rowIndex = 6;
				// now write all x values in first column
				for(int i=0; i<xValuesFunc.getNum(); ++i)
					newSheet.createRow(rowIndex++).createCell((short)0).setCellValue(xValuesFunc.getX(i));
				
				// write min, max, avg and Y values in all subsequent columns
				for(int colIndex=1; colIndex<=lastColIndex; ++colIndex) {
					double[] minMaxAvg = getMinMaxAvg(bIndices, colIndex, origSheet);
					EvenlyDiscretizedFunc func = getFunc("", bIndices, wb.getSheetAt(0), colIndex, origSheet);
					rowIndex = 2;
					// create min/max/avg rows
					newSheet.getRow(rowIndex++).createCell((short)colIndex).setCellValue(minMaxAvg[0]);
					newSheet.createRow(rowIndex++).createCell((short)colIndex).setCellValue(minMaxAvg[1]);
					newSheet.createRow(rowIndex++).createCell((short)colIndex).setCellValue(minMaxAvg[2]);
					
					rowIndex = 6;
					// now write all x values in first column
					for(int i=0; i<func.getNum(); ++i)
						newSheet.createRow(rowIndex++).createCell((short)colIndex).setCellValue(func.getY(i));

				}
			}
			
			// write to output file
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			newWorkbook.write(fileOut);
			fileOut.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 *  Get min/max/avg for a column
	 *  
	 * @param bptBranchIndices
	 * @param totProbColIndex
	 * @param probSheet
	 * @return
	 */
	private double[] getMinMaxAvg(ArrayList<Integer> branchIndices, 
			int probColIndex, HSSFSheet probSheet) {

		double totProb = 0.0;
		double minProb = Double.MAX_VALUE;
		double maxProb = Double.NEGATIVE_INFINITY;
		
		for (int i=0; i<branchIndices.size(); ++i) { 
			int branchNum=branchIndices.get(i);
			double prob = probSheet.getRow(branchNum+1).getCell((short)probColIndex).getNumericCellValue();
			totProb+=prob;
			if(prob>maxProb) maxProb = prob;
			if(prob<minProb) minProb = prob;
		}
		double []minMaxAvg = new double[3];
		minMaxAvg[0] = minProb;
		minMaxAvg[1] = maxProb;
		minMaxAvg[2] = totProb/branchIndices.size();
		return minMaxAvg;
	}
	
	
	/**
	 *  Get the evenly discretized func
	 *  
	 * @param fileName
	 * @param bptBranchIndices
	 * @param paramSettingsSheet
	 * @param weightColIndex
	 * @param totProbColIndex
	 * @param probSheet
	 * @return
	 */
	private EvenlyDiscretizedFunc getFunc(String metadata, ArrayList<Integer> branchIndices, 
			HSSFSheet paramSettingsSheet, int probColIndex, HSSFSheet probSheet) {

		int weightColIndex =  getColIndexForParam(paramSettingsSheet, "Branch Weight"); // logic tree branch weight  index column

		// BPT/Poisson
		EvenlyDiscretizedFunc bptFunc = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
		bptFunc.setInfo(metadata);
		bptFunc.setTolerance(DELTA_PROB);

		
		for (int i=0; i<branchIndices.size(); ++i) { // populate  func
			int branchNum=branchIndices.get(i);
			double wt= paramSettingsSheet.getRow(branchNum).getCell((short)weightColIndex).getNumericCellValue();
			double prob = probSheet.getRow(branchNum+1).getCell((short)probColIndex).getNumericCellValue();
			if(prob>this.MAX_PROB) prob=MAX_PROB;
			//System.out.println(prob);
			bptFunc.add(prob, wt);
		}
		return bptFunc;
	}
	
	/**
	 * Plot Histogram for a particular source or total prob 
	 * 
	 * @param minMag
	 * @param fileName
	 * @param sourceType It can be A_Faults, B_Faults, Non_CA_B_Faults, C-Zones, Background, Total.
	 * These are constant values as defined in this class
	 */
	public void plotHistogramsForMagAndSource(double minMag, String fileName, String sourceType) {

		
		int colIndex=getColIndexForMagAndSource(minMag, sourceType);

		// Open the excel file
		try {
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(fileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(1); // whole Region

			ArrayList<Integer>  bIndices= getBranchIndices(wb.getSheetAt(0),  UCERF2.PROB_MODEL_PARAM_NAME,  null);
			EvenlyDiscretizedFunc func = this.getFunc("Histogram plot for "+fileName+" for Mag>="+minMag, bIndices, wb.getSheetAt(0), colIndex, wb.getSheetAt(1));
				
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
	 * Get column index for a magnitiude for a particular source type
	 * 
	 * @param minMag
	 * @return
	 */
	private int getColIndexForMagAndSource(double minMag, String sourceType) {
		int offSet = -20;
		for(int i=0; i<this.sources.length; ++i)
			if(sources[i].equalsIgnoreCase(sourceType)) offSet = i+1;
		if(offSet<0) throw new RuntimeException("Invalid source type");
		boolean found = false;
		int colIndex=-1;
		// find the column based on the specfied magnitude
		for(int magIndex=0; magIndex<this.mags.length && !found; ++magIndex) {
			if(mags[magIndex]==minMag) {
				colIndex = magIndex* sources.length+offSet;
				found=true;
			}
		}
		if(!found) throw new RuntimeException("Invalid minimum magnitude. Only 5.0, 6.0, 6.5, 6.7, 7.0, 7.5, 8.0 are allowed");
		return colIndex;
	}
	
	/**
	 * Get a list of branch numbers which has the specified value of parameter specified by
	 * paramName.
	 * Indices start from 1.
	 * @param paramName 
	 * @param value null if all rows are required
	 * @return
	 */
	private ArrayList<Integer> getBranchIndices(HSSFSheet sheet, String paramName, String value) {
		int colIndex = getColIndexForParam(sheet, paramName); // column index where Prob Model value is specified in file
		int lastRowIndex = sheet.getLastRowNum();
		ArrayList<Integer> branchIndices = new ArrayList<Integer>();
		// fill the branch numbers for BPT (or Poisson) and Empirical
		for(int i=1; i<=lastRowIndex; ++i) {
			String cellVal = sheet.getRow(i).getCell((short)colIndex).getStringCellValue();
			if(value==null || cellVal.equals(value)) branchIndices.add(i);
		}
		return branchIndices;
	}
	
	
	/**
	 * Get column index for specified parameter name from metadata sheet
	 * 
	 * @param paramName
	 * @return
	 */
	private int getColIndexForParam(HSSFSheet sheet, String paramName) {
		HSSFRow row = sheet.getRow(0);
		int firsColIndex = row.getFirstCellNum();
		int lastColIndex = row.getLastCellNum();
		for(int i=firsColIndex; i<=lastColIndex; ++i)
			if(row.getCell((short)i).getStringCellValue().equals(paramName)) return i;
		throw new RuntimeException("Parameter "+paramName+" does not exist in the sheet");
 	}


	/**
	 * create a sheet that lists parameter settings for each logic tree branch
	 *
	 */
	private void createParamSettingsSheet() {

		HSSFSheet paramSettingsSheet = workbook.createSheet("Parameter Settings");
		HSSFRow row;
		if(ucerf2EpistemicList==null)
			ucerf2EpistemicList = new UCERF2_TimeDependentEpistemicList();
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
		//plotter.generateProbContributionsExcelSheet(30, PATH+"ProbabilityContributions_30yrs_All.xls", null);
		//plotter.generateProbContributionsExcelSheet(30, PATH+"ProbabilityContributions_30yrs_WG02.xls", new EvenlyGriddedWG02_Region());
		//plotter.generateProbContributionsExcelSheet(30, PATH+"ProbabilityContributions_30yrs_NoCal.xls", new EvenlyGriddedNoCalRegion());
		//plotter.generateProbContributionsExcelSheet(30, PATH+"ProbabilityContributions_30yrs_SoCal.xls", new EvenlyGriddedSoCalRegion());
		//plotter.generateProbContributionsExcelSheet(5, PATH+"ProbabilityContributions_5yrs_All.xls", null);
		//plotter.generateProbContributionsExcelSheet(5, PATH+"ProbabilityContributions_5yrs_WG02.xls", new EvenlyGriddedWG02_Region());
		//plotter.generateProbContributionsExcelSheet(5, PATH+"ProbabilityContributions_5yrs_NoCal.xls", new EvenlyGriddedNoCalRegion());
		//plotter.generateProbContributionsExcelSheet(5, PATH+"ProbabilityContributions_5yrs_SoCal.xls", new EvenlyGriddedSoCalRegion());
		
		
		//plotter.plotEmpiricalBPT_ComparisonProbPlot(7.5, PATH+"ProbabilityContributions_30yrs_All.xls", ProbabilityDistHistogramPlotter.TOTAL);
		//plotter.plotHistogramsForMagAndSource(7.5, PATH+"ProbabilityContributions_30yrs_All.xls", ProbabilityDistHistogramPlotter.B_FAULTS);

		//plotter.mkAvgMinMaxSheet(PATH+"ProbabilityContributions_30yrs_All.xls", PATH+"ProbAnalysis_30yrs_All.xls");
		//plotter.mkAvgMinMaxSheet(PATH+"ProbabilityContributions_30yrs_WG02.xls", PATH+"ProbAnalysis_30yrs_WG02.xls");
		//plotter.mkAvgMinMaxSheet(PATH+"ProbabilityContributions_30yrs_NoCal.xls", PATH+"ProbAnalysis_30yrs_NoCal.xls");
		//plotter.mkAvgMinMaxSheet(PATH+"ProbabilityContributions_30yrs_SoCal.xls", PATH+"ProbAnalysis_30yrs_SoCal.xls");
	}

}
