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
import org.opensha.data.function.EvenlyDiscretizedFunc;
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
	private final static double MIN_PROB= 0.05;
	private final static double MAX_PROB= 0.95;
	private final static double DELTA_PROB= 0.1;
	private final static int NUM_PROB= Math.round((float)((MAX_PROB-MIN_PROB)/DELTA_PROB))+1;
	private final static String X_AXIS_LABEL = "Probability";
	private final static String Y_AXIS_LABEL = "Contribution";
	private final static String PLOT_LABEL = "Probability Contribution";
	
	private final PlotCurveCharacterstics PLOT_HISTOGRAM = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
		      new Color(0,0,0), 2); // black
	
	private ArrayList funcs;
	private ArrayList<PlotCurveCharacterstics> plottingCurveChars;
	private HSSFWorkbook workbook;
	
	private double mags[] = { 5.0, 6.0, 6.5, 6.7, 7.0, 7.5, 8.0};
	private String[] sources = { "A-Faults", "B-Faults", "Non-CA B-Faults", "C-Zones", "Background", "Total" };
	/**
	 * Plot histograms of probability contributions from various branches
	 * 
	 * @param minMag
	 */
	public void generateTotalProbHistogramExcelSheet(double duration, String fileName) {
		UCERF2_TimeDependentEpistemicList ucerf2EpistemicList = new UCERF2_TimeDependentEpistemicList();
		
		//	create a sheet that contains param settings for each logic tree branch
		workbook  = new HSSFWorkbook();
		createParamSettingsSheet(ucerf2EpistemicList);
		
		
		int numSources = sources.length; // A-Faults, B-Faults, Non-CA B-Faults, Background, C-Zones, Total
		
		HSSFSheet allCA_RegionSheet = workbook.createSheet("All CA Region");
		HSSFRow row1, row2;
		
		// add column for each magnitude and each source
		 row1 = allCA_RegionSheet.createRow(0);
		 row2 = allCA_RegionSheet.createRow(1);
		 for(int magIndex=0; magIndex<mags.length; ++magIndex) {
			 int colIndex = magIndex*numSources+1;
			 row1.createCell((short)(colIndex)).setCellValue(" Mag "+mags[magIndex]);
			 for(int srcIndex=0; srcIndex<numSources; ++srcIndex) // each source for this magnitude
				 row2.createCell((short)(colIndex+srcIndex)).setCellValue(sources[srcIndex]);
		 }
		
		
		int numERFs = ucerf2EpistemicList.getNumERFs(); // number of logic tree branches
		//	now write the probability contribtuions of each source in each branch of logic tree
		int startRowIndex = 2;
		for(int erfIndex=0; erfIndex<numERFs; ++erfIndex) {
			System.out.println("Doing run "+(erfIndex+1)+" of "+numERFs);
			row1 = allCA_RegionSheet.createRow(startRowIndex+erfIndex); 
			row1.createCell((short)0).setCellValue("Branch "+(erfIndex+1));
			UCERF2 ucerf2 = (UCERF2)ucerf2EpistemicList.getERF(erfIndex);
			ucerf2.getTimeSpan().setDuration(duration);
			ucerf2.updateForecast();
			for(int magIndex=0; magIndex<mags.length; ++magIndex) {
				 int colIndex = magIndex*numSources+1;
				 double mag = mags[magIndex];
				 row1.createCell((short)(colIndex++)).setCellValue(ucerf2.getTotal_A_FaultsProb(mag));
				 row1.createCell((short)(colIndex++)).setCellValue(ucerf2.getTotal_B_FaultsProb(mag));
				 row1.createCell((short)(colIndex++)).setCellValue(ucerf2.getTotal_NonCA_B_FaultsProb(mag));
				 row1.createCell((short)(colIndex++)).setCellValue(ucerf2.getTotal_C_ZoneProb(mag));
				 row1.createCell((short)(colIndex++)).setCellValue(ucerf2.getTotal_BackgroundProb(mag));
				 row1.createCell((short)(colIndex++)).setCellValue(ucerf2.getTotalProb(mag));
			}	
		}
		 
//		 write metadata excel sheet
			try {
				FileOutputStream fileOut = new FileOutputStream(fileName);
				workbook.write(fileOut);
				fileOut.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		
		/*EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
		func.setTolerance(DELTA_PROB);
		
		int numERFs = ucerf2EpistemicList.getNumERFs(); 
		
		for(int i=0; i<numERFs; ++i) {
			System.out.println("Doing run "+(i+1)+" of "+numERFs);
			UCERF2 ucerf2 = (UCERF2)ucerf2EpistemicList.getERF(i);
			ucerf2.updateForecast();
			double wt= ucerf2EpistemicList.getERF_RelativeWeight(i);
			double prob = ucerf2.getTotalProb(minMag);
			func.add(prob, wt);
		}
		funcs = new ArrayList();
		funcs.add(func);
		plottingCurveChars = new ArrayList<PlotCurveCharacterstics>();
		plottingCurveChars.add(PLOT_HISTOGRAM);
		GraphWindow graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(PLOT_LABEL);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(true);*/
	}
	
	
	/**
	 * create a sheet that lists parameter settings for each logic tree branch
	 *
	 */
	private void createParamSettingsSheet(UCERF2_TimeDependentEpistemicList ucerf2EpistemicList) {
		
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
		plotter.generateTotalProbHistogramExcelSheet(30, "ProbabilityContributions_30yrs.xls");
	}

}
