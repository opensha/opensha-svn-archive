/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.analysis;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;


/**
 * This is used for plotting various logic tree MFDs
 * 
 * @author vipingupta
 *
 */
public class LogicTreeMFDsPlotter implements GraphWindowAPI {
	
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String CUM_Y_AXIS_LABEL = "Cumulative Rate (per year)";
	private final static String INCR_Y_AXIS_LABEL = "Incremental Rate (per year)";
	
	
//	 Eqk Rate Model 2 ERF
	private UCERF2 ucerf2 = new UCERF2();
	private ArrayList<IncrementalMagFreqDist> aFaultMFDsList, bFaultCharMFDsList, bFaultGRMFDsList, totMFDsList;
	private IncrementalMagFreqDist cZoneMFD, bckMFD, nshmp02TotMFD;
	
	private final static String DEFAULT_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/data/logicTreeMFDs/";
	private final static String COUP_COEFF1_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/data/logicTreeMFDs/CoupCoeff1/";
	private final static String BCK_FRAC_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/data/logicTreeMFDs/BackGrdFrac0_1/";
	private final static String BFAULT_BVAL_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_2/data/logicTreeMFDs/BFault_BVal0/";
	
	private final static String A_FAULTS_MFD_FILENAME = DEFAULT_PATH+"A_Faults_MFDs.txt";
	private final static String B_FAULTS_CHAR_MFD_FILENAME = DEFAULT_PATH+"B_FaultsCharMFDs.txt";
	private final static String B_FAULTS_GR_MFD_FILENAME = DEFAULT_PATH+"B_FaultsGR_MFDs.txt";
	private final static String TOT_MFD_FILENAME = DEFAULT_PATH+"TotMFDs.txt";
	private final static String NSHMP_02_MFD_FILENAME = DEFAULT_PATH+"NSHMP02_MFDs.txt";
	private final static String METADATA_EXCEL_SHEET = DEFAULT_PATH+"Metadata.xls";
	private final static String COMBINED_AVG_FILENAME = "CombinedAvgMFDs.txt";
	
	private ArrayList<String> paramNames;
	private ArrayList<ParamOptions> paramValues;
	private int lastParamIndex;
	private int mfdIndex;
	
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2); // A-Faults
	private final PlotCurveCharacterstics PLOT_CHAR1_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.BLUE, 2); // A-Faults
	private final PlotCurveCharacterstics PLOT_CHAR1_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.BLUE, 2); // A-Faults
	
	
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.LIGHT_GRAY, 2); // B-Faults Char
	private final PlotCurveCharacterstics PLOT_CHAR2_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.LIGHT_GRAY, 2); // B-Faults Char
	private final PlotCurveCharacterstics PLOT_CHAR2_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.LIGHT_GRAY, 2); // B-Faults Char
	
	
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2); // B-Faults GR
	private final PlotCurveCharacterstics PLOT_CHAR3_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.GREEN, 2); // B-Faults GR
	private final PlotCurveCharacterstics PLOT_CHAR3_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.GREEN, 2); // B-Faults GR
	
	
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 2); // Tot MFD
	private final PlotCurveCharacterstics PLOT_CHAR4_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.BLACK, 2); // Tot MFD
	private final PlotCurveCharacterstics PLOT_CHAR4_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.BLACK, 2); // Tot MFD
	
	
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2); //background
	private final PlotCurveCharacterstics PLOT_CHAR5_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.MAGENTA, 2); //background
	private final PlotCurveCharacterstics PLOT_CHAR5_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.MAGENTA, 2); //background
	
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.PINK, 2); // C-zone
	
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2); // best fit MFD
	private final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      Color.RED, 5); // observed MFD
	
	private final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      new Color(188, 143, 143), 2); // NSHMP 2002
	
	private ArrayList funcs;
	private ArrayList<PlotCurveCharacterstics> plottingFeaturesList = new ArrayList<PlotCurveCharacterstics>();
	private boolean isCumulative;
	private double  obs6_5CumRate;
	private HSSFSheet excelSheet;
	private ArrayList<String> adjustableParamNames;
	/**
	 * This method caclulates MFDs for all logic tree branches and saves them to files.
	 * However, if reCalculate is false, it just reads the data from the files wihtout recalculation
	 * 
	 */
	public LogicTreeMFDsPlotter (boolean reCalculate, boolean isCumulative) {
		this.isCumulative = isCumulative;
		fillAdjustableParams();
		lastParamIndex = paramNames.size()-1;
		aFaultMFDsList = new ArrayList<IncrementalMagFreqDist>();
		bFaultCharMFDsList = new ArrayList<IncrementalMagFreqDist>();
		bFaultGRMFDsList = new ArrayList<IncrementalMagFreqDist>();
		totMFDsList = new ArrayList<IncrementalMagFreqDist>();
		boolean includeAfterShocks = ucerf2.areAfterShocksIncluded();
		obs6_5CumRate = ucerf2.getObsCumMFD(includeAfterShocks).get(0).getY(6.5);
		if(reCalculate) {
			HSSFWorkbook wb  = new HSSFWorkbook();
			excelSheet = wb.createSheet();
			HSSFRow row;
			ParameterList adjustableParams = ucerf2.getAdjustableParameterList();
			Iterator it = adjustableParams.getParametersIterator();
			adjustableParamNames = new ArrayList<String>();
			while(it.hasNext()) {
				 ParameterAPI param = (ParameterAPI)it.next();
				 adjustableParamNames.add(param.getName());
			 }
			// add column for each parameter name. Also add a initial blank row for writing figure names
			 row = excelSheet.createRow(0); 
			 for(int i=1; i<=adjustableParamNames.size(); ++i) {
				if(i>0) row.createCell((short)i).setCellValue(adjustableParamNames.get(i-1));
			}
			int colNum = adjustableParams.size()+1;
			// add a row for predicted and observed ratio
			row.createCell((short)(colNum)).setCellValue("M 6.5 pred/obs");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("Weight");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("A-Fault MoRate");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("B-Faults Char MoRate");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("B-Faults GR Mo Rate");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("C-Zone MoRate");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("Background MoRate");
			++colNum;
			row.createCell((short)(colNum)).setCellValue("Total MoRate");
			calcMFDs(0, 1.0);
			saveMFDsToFile(A_FAULTS_MFD_FILENAME, this.aFaultMFDsList);
			saveMFDsToFile(B_FAULTS_CHAR_MFD_FILENAME, this.bFaultCharMFDsList);
			saveMFDsToFile(B_FAULTS_GR_MFD_FILENAME, this.bFaultGRMFDsList);
			saveMFDsToFile(TOT_MFD_FILENAME, this.totMFDsList);
			saveMFDToFile(NSHMP_02_MFD_FILENAME, Frankel02_AdjustableEqkRupForecast.getTotalMFD_InsideRELM_region(false));
			// write metadata excel sheet
			try {
				FileOutputStream fileOut = new FileOutputStream(METADATA_EXCEL_SHEET);
				wb.write(fileOut);
				fileOut.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}  else {
			readMFDsFromFile(A_FAULTS_MFD_FILENAME, this.aFaultMFDsList, false);
			//for(int i=0; i<aFaultMFDsList.size(); ++i)
				//System.out.println(aFaultMFDsList.get(i).getCumRate(6.5));
			readMFDsFromFile(B_FAULTS_CHAR_MFD_FILENAME, this.bFaultCharMFDsList, false);
			readMFDsFromFile(B_FAULTS_GR_MFD_FILENAME, this.bFaultGRMFDsList, false);
			readMFDsFromFile(TOT_MFD_FILENAME, this.totMFDsList, false);
			nshmp02TotMFD = readMFDFromFile(NSHMP_02_MFD_FILENAME, true);
		}
		// calculate ratio of default settings and average value at Mag6.5
		SummedMagFreqDist avgTotMFD = doAverageMFDs(false, false, false, false, false);
		this.ucerf2.setParamDefaults();
		ucerf2.updateForecast();
		System.out.println("Ratio of Rates at preferred settings to Combined Logic tree rate (at Mag 6.5) = "+ucerf2.getTotalMFD().getY(6.5)/avgTotMFD.getY(6.5));
		cZoneMFD = this.ucerf2.getTotal_C_ZoneMFD();
		bckMFD = this.ucerf2.getTotal_BackgroundMFD();
	}
	
	/**
	 * Save MFDs to file
	 *
	 */
	private void saveMFDsToFile(String fileName, ArrayList<IncrementalMagFreqDist> mfdList) {
		try {
			FileWriter fw = new FileWriter(fileName);
			for(int i=0; i<mfdList.size(); ++i) {
				IncrementalMagFreqDist mfd = mfdList.get(i);
				fw.write("#Run "+(i+1)+"\n");
				for(int magIndex=0; magIndex<mfd.getNum(); ++magIndex)
					fw.write(mfd.getX(magIndex)+"\t"+mfd.getY(magIndex)+"\n");
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Save single MFD to file
	 *
	 */
	private void saveMFDToFile(String fileName, IncrementalMagFreqDist mfd) {
		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
		mfdList.add(mfd);
		saveMFDsToFile(fileName, mfdList);
	}
	
	/**
	 * Read MFDs from file
	 * 
	 * @param fileName
	 * @param mfdList
	 */
	private void readMFDsFromFile(String fileName, ArrayList<IncrementalMagFreqDist> mfdList, boolean isNSHMP02) {
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			IncrementalMagFreqDist mfd = null;
			double mag, rate;
			while(line!=null) {
				if(line.startsWith("#")) {
					if(isNSHMP02) mfd = new IncrementalMagFreqDist(4.0, 9.0, 101);
					else mfd = new IncrementalMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
					mfdList.add(mfd);
				} else {
					StringTokenizer tokenizer = new StringTokenizer(line);
					mag = Double.parseDouble(tokenizer.nextToken());
					rate = Double.parseDouble(tokenizer.nextToken());
					mfd.set(mag, rate);
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read single MFD from file
	 * 
	 * @param fileName
	 * @param mfd
	 */
	private IncrementalMagFreqDist readMFDFromFile(String fileName, boolean isNSHMP02) {
		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
		readMFDsFromFile(fileName, mfdList, isNSHMP02);
		return mfdList.get(0);
	}
	
	/**
	 * Paramters that are adjusted in the runs
	 *
	 */
	private void fillAdjustableParams() {
		this.paramNames = new ArrayList<String>();
		this.paramValues = new ArrayList<ParamOptions>();
		
		// Deformation model
		paramNames.add(UCERF2.DEFORMATION_MODEL_PARAM_NAME);
		ParamOptions options = new ParamOptions();
		options.addValueWeight("D2.1", 0.5);
		options.addValueWeight("D2.4", 0.5);
		paramValues.add(options);
		
		// Mag Area Rel
		paramNames.add(UCERF2.MAG_AREA_RELS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(Ellsworth_B_WG02_MagAreaRel.NAME, 0.5);
		options.addValueWeight(HanksBakun2002_MagAreaRel.NAME, 0.5);
		paramValues.add(options);
		
		// A-Fault solution type
		paramNames.add(UCERF2.RUP_MODEL_TYPE_NAME);
		options = new ParamOptions();
		options.addValueWeight(UCERF2.SEGMENTED_A_FAULT_MODEL, 0.9);
		options.addValueWeight(UCERF2.UNSEGMENTED_A_FAULT_MODEL, 0.1);
		paramValues.add(options);
		
		// Aprioti wt param
		paramNames.add(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(1e-4), 0.5);
		options.addValueWeight(new Double(1e10), 0.5);
		paramValues.add(options);
		
		// Mag Correction
		paramNames.add(UCERF2.MEAN_MAG_CORRECTION);
		options = new ParamOptions();
		options.addValueWeight(new Double(-0.1), 0.2);
		options.addValueWeight(new Double(0), 0.6);
		options.addValueWeight(new Double(0.1), 0.2);
		paramValues.add(options);
		
		//	Connect More B-Faults?
		paramNames.add(UCERF2.CONNECT_B_FAULTS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Boolean(true), 0.5);
		options.addValueWeight(new Boolean(false), 0.5);
		paramValues.add(options);
		
		// C-zone weights
		paramNames.add(UCERF2.C_ZONE_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(0.0), 0.5);
		options.addValueWeight(new Double(1.0), 0.5);
		paramValues.add(options);
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		 return this.plottingFeaturesList;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return this.funcs;
	}
	
	
	/**
	 * Calculate MFDs
	 * 
	 * @param paramIndex
	 * @param weight
	 */
	private void calcMFDs(int paramIndex, double weight) {
		
		ParamOptions options = paramValues.get(paramIndex);
		String paramName = paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		for(int i=0; i<numValues; ++i) {
			if(ucerf2.getAdjustableParameterList().containsParameter(paramName)) {
				ucerf2.getParameter(paramName).setValue(options.getValue(i));	
				if(paramName.equalsIgnoreCase(UCERF2.REL_A_PRIORI_WT_PARAM_NAME)) {
					ParameterAPI param = ucerf2.getParameter(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
					if(((Double)param.getValue()).doubleValue()==1e10) {
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME).setValue(new Double(0.0));
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME).setValue(new Double(0.0));	
					} else {
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME).setValue(UCERF2.MIN_A_FAULT_RATE_1_DEFAULT);
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME).setValue(UCERF2.MIN_A_FAULT_RATE_2_DEFAULT);	
					}
				}
			}
			double newWt = weight * options.getWeight(i);
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, save the MFDs
				System.out.println("Doing run:"+(aFaultMFDsList.size()+1));
				ucerf2.updateForecast();
				aFaultMFDsList.add(ucerf2.getTotal_A_FaultsMFD());
				bFaultCharMFDsList.add(ucerf2.getTotal_B_FaultsCharMFD());
				bFaultGRMFDsList.add(ucerf2.getTotal_B_FaultsGR_MFD());
				totMFDsList.add(ucerf2.getTotalMFD());
				short colIndex = (short)totMFDsList.size();
				HSSFRow row = this.excelSheet.createRow(colIndex);
				// write to excel sheet
				row.createCell((short)0).setCellValue("Plot "+colIndex);
				ParameterList paramList = ucerf2.getAdjustableParameterList();
				for(int p=0; p<this.adjustableParamNames.size(); ++p) {
					String pName = adjustableParamNames.get(p);
					if(paramList.containsParameter(pName))
						row.createCell((short)(p+1)).setCellValue(paramList.getValue(pName).toString());
				}
				
				int colNum = adjustableParamNames.size()+1;
				// add a row for predicted and observed ratio
				row.createCell((short)(colNum)).setCellValue(totMFDsList.get(colIndex-1).getCumRate(6.5)/obs6_5CumRate);
				++colNum;
				row.createCell((short)(colNum)).setCellValue(newWt);
				++colNum;
				row.createCell((short)(colNum)).setCellValue(aFaultMFDsList.get(colIndex-1).getTotalMomentRate());
				++colNum;
				row.createCell((short)(colNum)).setCellValue(bFaultCharMFDsList.get(colIndex-1).getTotalMomentRate());
				++colNum;
				row.createCell((short)(colNum)).setCellValue(bFaultGRMFDsList.get(colIndex-1).getTotalMomentRate());
				++colNum;
				row.createCell((short)(colNum)).setCellValue(ucerf2.getTotal_C_ZoneMFD().getTotalMomentRate());
				++colNum;
				row.createCell((short)(colNum)).setCellValue(ucerf2.getTotal_BackgroundMFD().getTotalMomentRate());
				++colNum;
				row.createCell((short)(colNum)).setCellValue(totMFDsList.get(colIndex-1).getTotalMomentRate());
			} else { // recursion 
				calcMFDs(paramIndex+1, newWt);
			}
		}
	}
	
	/**
	 * Plot MFDs for various different paramter settings
	 *
	 */
	public void plotMFDs() {
		
		// combined Logic Tree MFD
		plotMFDs(null, null, true, true, true, true, false);
		
//		 combined Logic Tree MFD comparison with NSHMP2002
		plotMFDs(null, null, false, false, false, false, true);
		
//		 Deformation model
		String paramName = UCERF2.DEFORMATION_MODEL_PARAM_NAME;
		ArrayList values = new ArrayList();
		values.add("D2.1");
		values.add("D2.4");
		plotMFDs(paramName, values, false, true, false, false, false); // plot B-faults only
		
		// Mag Area Rel
		paramName = UCERF2.MAG_AREA_RELS_PARAM_NAME;
		values = new ArrayList();
		values.add(Ellsworth_B_WG02_MagAreaRel.NAME);
		values.add(HanksBakun2002_MagAreaRel.NAME);
		plotMFDs(paramName, values, true, true, false, false, false); // plot both A and B-faults
		
		
		// A-Fault solution type
		paramName = UCERF2.RUP_MODEL_TYPE_NAME;
		values = new ArrayList();
		values.add(UCERF2.SEGMENTED_A_FAULT_MODEL);
		values.add(UCERF2.UNSEGMENTED_A_FAULT_MODEL);
		plotMFDs(paramName, values, true, false, false, false, false); // plot A-faults  only
		
		// Aprioti wt param
		paramName = UCERF2.REL_A_PRIORI_WT_PARAM_NAME;
		values = new ArrayList();
		values.add(new Double(1e-4));
		values.add(new Double(1e10));
		plotMFDs(paramName, values, true, false, false, false, false); // plot A-faults  only
		
		
		// Mag Correction
		paramName = UCERF2.MEAN_MAG_CORRECTION;
		values = new ArrayList();
		values.add(new Double(-0.1));
		values.add(new Double(0.1));
		plotMFDs(paramName, values, true, true, false, false, false); // plot A-faults  and B-faults

		//	Connect More B-Faults?
		paramName = UCERF2.CONNECT_B_FAULTS_PARAM_NAME;
		values = new ArrayList();
		values.add(new Boolean(true));
		values.add(new Boolean(false));
		plotMFDs(paramName, values, false, true, false, false, false); // plot B-faults
		
//		 C-zone weights
		/*paramNames.add(EqkRateModel2_ERF.C_ZONE_WT_PARAM_NAME);
		values = new ArrayList();
		values.add(new Double(0.0));
		values.add(new Double(1.0));
		plotMFDs(paramName, values, false, false, true, false, false); */
		
		plotBackgroundEffectsMFDs();
		
//		B-faults b-values
		paramName = UCERF2.B_FAULTS_B_VAL_PARAM_NAME;
		values = new ArrayList();
		values.add(new Double(0.0));
		plotMFDs(paramName, values, false, true, false, false, false); // plot B-faults

//		fraction MoRate to Background
		paramName = UCERF2.ABC_MO_RATE_REDUCTION_PARAM_NAME;
		values = new ArrayList();
		values.add(new Double(0.1));
		plotMFDs(paramName, values, true, true, false, false, false); // plot A & B-faults
	
	}
	
	
	/**
	 * Plot ethe MFDs after varying the NSHMP Bulge Parameter and Apply M-Max Grid parameter
	 *
	 */
	private void plotBackgroundEffectsMFDs() {
		
		//		 BULGE PARAMETER EFFECT
		SummedMagFreqDist avgTotMFD = doAverageMFDs(false, false, false, false, false);
		SummedMagFreqDist modifiedTotMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
		ucerf2.setParamDefaults();
		this.ucerf2.getParameter(UCERF2.BULGE_REDUCTION_PARAM_NAME).setValue(new Boolean(false));
		ucerf2.updateForecast();
		IncrementalMagFreqDist newBckMFD = ucerf2.getTotal_BackgroundMFD();
		
		for(int i=0; i< avgTotMFD.getNum(); ++i) {
			double mag = avgTotMFD.getX(i);
			modifiedTotMFD.addResampledMagRate(mag, avgTotMFD.getY(i) - this.bckMFD.getY(mag) + newBckMFD.getY(mag), true);
		}
		String metadata="Dotted Dashed Line - ";
		metadata += "("+UCERF2.BULGE_REDUCTION_PARAM_NAME+"=false) ";
		addToFuncList(bckMFD, "Solid Line - Background MFD", PLOT_CHAR5);
		addToFuncList(newBckMFD, metadata+"Background MFD", PLOT_CHAR5_1);
		addToFuncList(modifiedTotMFD, metadata+"Total MFD, M6.5 Cum Ratio = "+modifiedTotMFD.getCumRate(6.5)/avgTotMFD.getCumRate(6.5), PLOT_CHAR4_1);	
		GraphWindow graphWindow= new GraphWindow(this);
	    graphWindow.setPlotLabel("Mag Freq Dist");
	    graphWindow.plotGraphUsingPlotPreferences();
	    graphWindow.setVisible(true);
	    
	    // APPLY MAX_MAG GRID parameter
	    avgTotMFD = doAverageMFDs(false, false, false, false, false);
	    modifiedTotMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
	    ucerf2.setParamDefaults();
	    this.ucerf2.getParameter(UCERF2.MAX_MAG_GRID_PARAM_NAME).setValue(new Boolean(false));
	    ucerf2.updateForecast();
	    newBckMFD = ucerf2.getTotal_BackgroundMFD();
	    
	    for(int i=0; i< avgTotMFD.getNum(); ++i) {
	    	double mag = avgTotMFD.getX(i);
	    	modifiedTotMFD.addResampledMagRate(mag, avgTotMFD.getY(i) - this.bckMFD.getY(mag) + newBckMFD.getY(mag), true);
	    }
	    metadata="Dotted Dashed Line - ";
	    metadata += "("+UCERF2.MAX_MAG_GRID_PARAM_NAME+"=false) ";
	    addToFuncList(bckMFD, "Solid Line - Background MFD", PLOT_CHAR5);
	    addToFuncList(newBckMFD, metadata+"Background MFD", PLOT_CHAR5_1);
	    addToFuncList(modifiedTotMFD, metadata+"Total MFD, M6.5 Cum Ratio = "+modifiedTotMFD.getCumRate(6.5)/avgTotMFD.getCumRate(6.5), PLOT_CHAR4_1);	
	    graphWindow= new GraphWindow(this);
	    graphWindow.setPlotLabel("Mag Freq Dist");
	    graphWindow.plotGraphUsingPlotPreferences();
	    graphWindow.setVisible(true);
	}
	
	/**
	 * It returns 3 MFD: First is A-Fault MFD, Second is B-Fault char MFD, Third is B-Fault GR MFD and last is TotalMFD
	 * 
	 * @param paramName Param Name whose value needs to remain constant. Can be null 
	 * @param value Param Value for constant paramter. can be null
	 * 
	 * @return
	 */
	public void plotMFDs(String paramName, ArrayList values, boolean showAFaults, boolean showBFaults,
			boolean showCZones, boolean showBackground, boolean showNSHMP_TotMFD) {
		String metadata;
		SummedMagFreqDist avgTotMFD = doAverageMFDs(showAFaults, showBFaults, showCZones, showBackground, showNSHMP_TotMFD);
		
		PlotCurveCharacterstics plot1, plot2, plot3, plot4;
		for(int i =0; values!=null && i<values.size(); ++i) {
			IncrementalMagFreqDist aFaultMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
			IncrementalMagFreqDist bFaultCharMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
			IncrementalMagFreqDist bFaultGRMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
			IncrementalMagFreqDist totMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
			mfdIndex = 0;
			
			if(paramName.equalsIgnoreCase(UCERF2.ABC_MO_RATE_REDUCTION_PARAM_NAME)) {
				ArrayList<IncrementalMagFreqDist> mfds = this.getMFDsWhenBckFrac0_1();
				aFaultMFD = mfds.get(0);
				aFaultMFD.setInfo("A-Faults MFD");
				bFaultCharMFD = mfds.get(1);
				bFaultCharMFD.setInfo("B-Faults Char MFD");
				bFaultGRMFD = mfds.get(2);
				bFaultGRMFD.setInfo("B-Faults GR MFD");
				totMFD = mfds.get(3);
				totMFD.setInfo("Total MFD");
			} else if(paramName.equalsIgnoreCase(UCERF2.B_FAULTS_B_VAL_PARAM_NAME)) {
				ArrayList<IncrementalMagFreqDist> mfds = getMFDsWhenBVal0();
				aFaultMFD = mfds.get(0);
				aFaultMFD.setInfo("A-Faults MFD");
				bFaultCharMFD = mfds.get(1);
				bFaultCharMFD.setInfo("B-Faults Char MFD");
				bFaultGRMFD = mfds.get(2);
				bFaultGRMFD.setInfo("B-Faults GR MFD");
				totMFD = mfds.get(3);
				totMFD.setInfo("Total MFD");
			} else doWeightedSum(0, paramName, values.get(i), 1.0, (SummedMagFreqDist)aFaultMFD, (SummedMagFreqDist)bFaultCharMFD, (SummedMagFreqDist)bFaultGRMFD, (SummedMagFreqDist)totMFD);
			
			
			if(i==0) {
				plot1 = PLOT_CHAR1_1;
				plot2 = PLOT_CHAR2_1;
				plot3 = PLOT_CHAR3_1;
				plot4 = PLOT_CHAR4_1;
				metadata="Dotted Dashed Line - ";
			} else {
				plot1 = PLOT_CHAR1_2;
				plot2 = PLOT_CHAR2_2;
				plot3 = PLOT_CHAR3_2;
				plot4 = PLOT_CHAR4_2;
				metadata="Dashed Line - ";
			} 
			metadata += "("+paramName+"="+values.get(i)+")  ";
			
			if(showAFaults) addToFuncList(aFaultMFD, metadata+"A-Fault MFD", plot1);
			if(showBFaults) addToFuncList(bFaultCharMFD, metadata+"Char B-Fault MFD", plot2);
			if(showBFaults) addToFuncList(bFaultGRMFD, metadata+"GR B-Fault MFD", plot3);
			addToFuncList(totMFD, metadata+"Total MFD, M6.5 Cum Ratio = "+totMFD.getCumRate(6.5)/avgTotMFD.getCumRate(6.5), plot4);	
		}
		
		GraphWindow graphWindow= new GraphWindow(this);
	    graphWindow.setPlotLabel("Mag Freq Dist");
	    graphWindow.plotGraphUsingPlotPreferences();
	    graphWindow.setVisible(true);
	 }

	private SummedMagFreqDist doAverageMFDs(boolean showAFaults, boolean showBFaults, boolean showCZones, boolean showBackground, boolean showNSHMP_TotMFD) {
		funcs  = new ArrayList();
		plottingFeaturesList = new ArrayList<PlotCurveCharacterstics>();
		
		// Avg MFDs
		SummedMagFreqDist avgAFaultMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
		SummedMagFreqDist avgBFaultCharMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
		SummedMagFreqDist avgBFaultGRMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
		SummedMagFreqDist avgTotMFD = new SummedMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG,UCERF2. NUM_MAG);
		mfdIndex = 0;
		doWeightedSum(0, null, null, 1.0, avgAFaultMFD, avgBFaultCharMFD, avgBFaultGRMFD, avgTotMFD);
		String metadata = "Solid Line-";
		// Add to function list
		if(showAFaults) addToFuncList(avgAFaultMFD, metadata+"Average A-Fault MFD", PLOT_CHAR1);
		if(showBFaults) addToFuncList(avgBFaultCharMFD, metadata+"Average Char B-Fault MFD", PLOT_CHAR2);
		if(showBFaults) addToFuncList(avgBFaultGRMFD,metadata+ "Average GR B-Fault MFD", PLOT_CHAR3);
		if(showBackground) addToFuncList(this.bckMFD, metadata+"Average Background MFD", PLOT_CHAR5);
		if(showCZones) addToFuncList(this.cZoneMFD, metadata+"Average C-Zones MFD", PLOT_CHAR6);
		if(showNSHMP_TotMFD) { // add NSHMP MFD after resampling for smoothing purposes
			EvenlyDiscretizedFunc nshmpCumMFD = nshmp02TotMFD.getCumRateDist();
			
			ArbitrarilyDiscretizedFunc resampledNSHMP_MFD = new ArbitrarilyDiscretizedFunc();
			for(int i=0; i<nshmpCumMFD.getNum(); i=i+2)
				resampledNSHMP_MFD.set(nshmpCumMFD.getX(i), nshmpCumMFD.getY(i));
			
			resampledNSHMP_MFD.setName("NSHMP-2002 Total MFD");
			if(this.isCumulative) {
				funcs.add(resampledNSHMP_MFD);
			} else {
				ArbIncrementalMagFreqDist nshmpIncrMFD = new ArbIncrementalMagFreqDist(UCERF2.MIN_MAG+UCERF2.DELTA_MAG/2, UCERF2.MAX_MAG-UCERF2.DELTA_MAG/2,UCERF2. NUM_MAG-1);
				nshmpIncrMFD.setCumRateDist(resampledNSHMP_MFD);
				funcs.add(nshmpIncrMFD);
			}
			this.plottingFeaturesList.add(PLOT_CHAR9);
		}
//		 Karen's observed data
		boolean includeAfterShocks = ucerf2.areAfterShocksIncluded();
		
		ArrayList obsMFD;
		if(this.isCumulative)  {
			obsMFD = ucerf2.getObsCumMFD(includeAfterShocks);
			metadata+="Average Total MFD, M6.5 Cum Ratio = "+avgTotMFD.getCumRate(6.5)/((EvenlyDiscretizedFunc)obsMFD.get(0)).getY(6.5);
		}
		else  {
			obsMFD = ucerf2.getObsIncrMFD(includeAfterShocks);
			metadata+="Average Total MFD";
		}
		addToFuncList(avgTotMFD, metadata, PLOT_CHAR4);
		
		
		// historical best fit cum dist
		//funcs.add(eqkRateModel2ERF.getObsBestFitCumMFD(includeAfterShocks));
		funcs.add(obsMFD.get(0));
		this.plottingFeaturesList.add(PLOT_CHAR7);
		// historical cum dist
		funcs.addAll(obsMFD);
		this.plottingFeaturesList.add(PLOT_CHAR8);
		this.plottingFeaturesList.add(PLOT_CHAR8);
		this.plottingFeaturesList.add(PLOT_CHAR8);
		
		return avgTotMFD;
	}
	
	/**
	 * 
	 * @param mfd
	 */
	private void addToFuncList(IncrementalMagFreqDist mfd, String metadata, 
			PlotCurveCharacterstics curveCharateristic) {
		EvenlyDiscretizedFunc func;
		if(this.isCumulative) func = mfd.getCumRateDist();
		else func = mfd;
		func.setName(metadata);
		funcs.add(func);
		this.plottingFeaturesList.add(curveCharateristic);
	}
	 
	
	/**
	 * Do Weighted Sum
	 * 
	 * @param paramIndex
	 * @param paramName
	 * @param value
	 * @param weight
	 * @param aFaultMFD
	 * @param bFaultMFD
	 * @param totMFD
	 */
	private void doWeightedSum( int paramIndex, String constantParamName, Object value, double weight, 
			SummedMagFreqDist aFaultTotMFD, SummedMagFreqDist bFaultTotCharMFD, SummedMagFreqDist bFaultTotGRMFD, SummedMagFreqDist totMFD) {
		
		ParamOptions options = paramValues.get(paramIndex);
		String paramName = paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		double newWt;
		for(int i=0; i<numValues; ++i) {
			if(paramName !=null && paramName.equalsIgnoreCase(constantParamName)) {
				if(options.getValue(i).equals(value)) newWt = 1*weight; // for constant paramter
				else newWt = 0*weight;
			} else newWt = weight * options.getWeight(i);
			
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, add to the MFDs
				if(newWt!=0) {
					addMFDs(aFaultTotMFD, aFaultMFDsList.get(mfdIndex), newWt);
					addMFDs(bFaultTotCharMFD, bFaultCharMFDsList.get(mfdIndex), newWt);
					addMFDs(bFaultTotGRMFD, bFaultGRMFDsList.get(mfdIndex), newWt);
					addMFDs(totMFD, totMFDsList.get(mfdIndex), newWt);
				}
				++mfdIndex;
			} else { // recursion 
				doWeightedSum(paramIndex+1, constantParamName,  value, newWt, 
						 aFaultTotMFD,  bFaultTotCharMFD,  bFaultTotGRMFD, totMFD);
			}
		}
	}
	
	
	/**
	 * Return the MFDs for A-Faults, B-Char, B-GR and Total when B-Fault b-value=0
	 * @return
	 */
	private ArrayList<IncrementalMagFreqDist> getMFDsWhenBVal0() {
		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
		readMFDsFromFile(BFAULT_BVAL_PATH+ COMBINED_AVG_FILENAME, mfdList, false);
		return mfdList;
	}
	
	/**
	 * Return the MFDs for A-Faults, B-Char, B-GR and Total when 
	 * Fraction MoRate to Bck = 0.1
	 * @return
	 */
	private ArrayList<IncrementalMagFreqDist> getMFDsWhenBckFrac0_1() {
		ArrayList<IncrementalMagFreqDist> mfdList = new ArrayList<IncrementalMagFreqDist>();
		readMFDsFromFile(BCK_FRAC_PATH+ COMBINED_AVG_FILENAME, mfdList, false);
		return mfdList;
	}
	
	
	
	/**
	 * Add source MFD to Target MFD after applying the  weight
	 * @param targetMFD
	 * @param sourceMFD
	 * @param wt
	 */
	private void addMFDs(SummedMagFreqDist targetMFD, IncrementalMagFreqDist sourceMFD, double wt) {
		for(int i=0; i<sourceMFD.getNum(); ++i) {
			targetMFD.add(sourceMFD.getX(i), wt*sourceMFD.getY(i));
		}
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
		return true;
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
		if(this.isCumulative)	return CUM_Y_AXIS_LABEL;
		else return INCR_Y_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		return 5.0;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		return 9.255;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		return 1e-4;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		return 10;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	
	public static void main(String []args) {
		
		
		//LogicTreeMFDsPlotter mfdPlotter = new LogicTreeMFDsPlotter(true);
		//mfdPlotter.plotMFDs();
		//System.out.println(mfdPlotter.getMFDs(null, null).get(3).toString());
	}
}

/**
 * Various parameter values and their corresponding weights
 * 
 * @author vipingupta
 *
 */
class ParamOptions {
	private ArrayList values = new ArrayList();
	private ArrayList<Double> weights = new ArrayList<Double>();
	
	/**
	 * Add a value and weight for this parameter 
	 * @param value
	 * @param weight
	 */
	public void addValueWeight(Object value, double weight) {
		values.add(value);
		weights.add(weight);
	}
	
	/**
	 * Number of different options for this parameter
	 * @return
	 */
	public int getNumValues() {
		return values.size();
	}
	
	/**
	 * Get the value at specified index
	 * 
	 * @param index
	 * @return
	 */
	public Object getValue(int index) {
		return values.get(index);
	}
	
	/**
	 * Get the weight at specified index
	 * 
	 * @param index
	 * @return
	 */
	public double getWeight(int index) {
		return weights.get(index);
	}
	
}
