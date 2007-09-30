/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.calc.MomentMagCalc;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.data.Location;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_TypeB_EqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.NonCA_FaultsFetcher;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.surface.StirlingGriddedSurface;
import org.opensha.util.FileUtils;

/**
 *  this creates an excel sheet with B-Faults and various values associated with them
 *  After excel sheet is made, some things need to be done explicity like sorting by 
 *  name and then writing Ids of connected faults.
 *  
 * @author vipingupta
 *
 */
public class MakeB_FaultsTable {
	private final static String FILENAME = "Non-CA_B-Faults.xls";
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.000E0");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat RATE_FORMAT = new DecimalFormat("0.00000");
	private final static DecimalFormat ASEISMSIC_FORMAT = new DecimalFormat("0.00");
	// bFaults name and row Id mapping
	private HashMap<String, Integer> nameRowMapping;
	private HashMap<String, String> nameFaultModelMapping;

	private HSSFWorkbook workbook  = new HSSFWorkbook();
	private HSSFSheet sheet;
	private UCERF2 ucerf2 = new UCERF2();
	int rowIndex;
	public MakeB_FaultsTable() {
		workbook  = new HSSFWorkbook();
		ucerf2 = new UCERF2();
		
		/*makeNewSheet("B-Faults");
		rowIndex=2;
		makeData(false, "D2.1");
		makeData(false, "D2.4");
	
		Iterator<String> bFaultNamesIt = nameRowMapping.keySet().iterator();
		while(bFaultNamesIt.hasNext()) {
			String faultName = bFaultNamesIt.next();
			int rId = nameRowMapping.get(faultName);
			sheet.getRow(rId).createCell((short)12).setCellValue(nameFaultModelMapping.get(faultName));
		}
		
		rowIndex=2;
		makeNewSheet("Connected B-Faults");
		makeData(true, "D2.1");
		makeData(true, "D2.4");
		bFaultNamesIt = nameRowMapping.keySet().iterator();
		while(bFaultNamesIt.hasNext()) {
			String faultName = bFaultNamesIt.next();
			int rId = nameRowMapping.get(faultName);
			sheet.getRow(rId).createCell((short)12).setCellValue(nameFaultModelMapping.get(faultName));
		}*/
		
		rowIndex=2;
		makeNewSheet("Non-CA B-Faults");
		makeNonCAB_FaultsData();

	
		
//		 write  excel sheet
		try {
			FileOutputStream fileOut = new FileOutputStream(FILENAME);
			workbook.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	private void makeNewSheet(String sheetName) {
		sheet = workbook.createSheet(sheetName);
		HSSFRow row = sheet.createRow(0);
		row.createCell((short)2).setCellValue("Ellsworth B");
		row.createCell((short)4).setCellValue("Hans & Bakun 2002");
		row  = sheet.createRow(1);
		row.createCell((short)0).setCellValue("Index");
		row.createCell((short)1).setCellValue("Name");
		row.createCell((short)2).setCellValue("Mag");
		row.createCell((short)3).setCellValue("Rate");
		row.createCell((short)4).setCellValue("Mag");
		row.createCell((short)5).setCellValue("Rate");
		row.createCell((short)6).setCellValue("Prob (Mag>=6.7)");
		row.createCell((short)7).setCellValue("Empirical Correction");
		row.createCell((short)8).setCellValue("Slip Rate (mm/yr)");
		row.createCell((short)9).setCellValue("Area (sq-km)");
		row.createCell((short)10).setCellValue("Length (km)");
		row.createCell((short)11).setCellValue("Moment Rate (Newton-meters/yr)");
		row.createCell((short)12).setCellValue("Fault Model");
		nameRowMapping = new  HashMap<String, Integer>();
		nameFaultModelMapping = new  HashMap<String, String>();

	}
	
private void makeNonCAB_FaultsData() {
	
		HashMap<String, Double> sourceRateMapping = new HashMap<String, Double>();
		// Poisson
		ucerf2.setParamDefaults();
		ucerf2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
		ucerf2.updateForecast();
		ArrayList<ProbEqkSource> nonCA_Sources = ucerf2.getNonCA_B_FaultSources();

		double duration = ucerf2.getTimeSpan().getDuration();
		HSSFRow row;
		HashMap<String, Double> sourceMagMapping = new HashMap<String, Double>();
		String fileName = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/NearCA_NSHMP/NonCA_Faults.txt";
		try {
			//FileWriter fw = new FileWriter("NonCA_Sources.txt");
			//fw.write("Total Yearly Rate\tTotal Moment Rate\tSource Name\n");
			ArrayList<String> fileLines = FileUtils.loadJarFile(fileName);
			int numLines = fileLines.size();
			int rakeId, srcTypeId;
			double mag=0;
			FaultTrace faultTrace;
			String faultName;
			for(int i=0; i<numLines; ) {
				String line = fileLines.get(i++);
				StringTokenizer tokenizer = new StringTokenizer(line);
				srcTypeId = Integer.parseInt(tokenizer.nextToken().trim());
				rakeId = Integer.parseInt(tokenizer.nextToken().trim());
				int numMags = Integer.parseInt(tokenizer.nextToken().trim());
				tokenizer.nextToken();  // we were told this element is useless
				faultName = "";
				while(tokenizer.hasMoreTokens()) faultName+=tokenizer.nextToken()+" ";
				// mag, rate & wt
				line = fileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				if(srcTypeId==1) { // Char case
					mag = Double.parseDouble(tokenizer.nextToken().trim());
					double rate = Double.parseDouble(tokenizer.nextToken().trim());
					double moRate = rate*MomentMagCalc.getMoment(mag);
					double wt = Double.parseDouble(tokenizer.nextToken().trim());
					double wt2 = 1;
				}
				else if (srcTypeId==2) {
					double aVal=Double.parseDouble(tokenizer.nextToken().trim());
					double bVal=Double.parseDouble(tokenizer.nextToken().trim());
					double magLower=Double.parseDouble(tokenizer.nextToken().trim());
					double magUpper=Double.parseDouble(tokenizer.nextToken().trim());
					double deltaMag=Double.parseDouble(tokenizer.nextToken());
					//System.out.println(faultName+","+magLower+","+magUpper);
					if(magUpper!=magLower) {
						magLower += deltaMag/2.0;
						magUpper -= deltaMag/2.0;
					}
					else {
						magLower=Math.round( (float)((magUpper-UCERF2.MIN_MAG)/deltaMag)) * deltaMag + UCERF2.MIN_MAG;
						magUpper= magLower;
					}
					mag=magUpper;
				}	
				else throw new RuntimeException("Src type not supported");
				if(sourceMagMapping.containsKey(faultName)) {
					double mag1 = sourceMagMapping.get(faultName);
					if(mag1==mag) throw new RuntimeException("Wrong mags for source "+faultName);
				} else {
					sourceMagMapping.put(faultName, mag);
					double rate=0;
					for(int srcId=0; srcId<nonCA_Sources.size(); ++srcId) {
						ProbEqkSource source = (ProbEqkSource)nonCA_Sources.get(srcId);
						if(source.getName().equalsIgnoreCase(faultName+" GR") ||
								source.getName().equalsIgnoreCase(faultName+" Char"))
							rate+=1-Math.exp(-source.computeTotalProb()*duration);
					}
					sourceRateMapping.put(faultName, rate);
				}

				// dip, surface width, upper seis depth, surface length
				line = fileLines.get(i++);
				tokenizer = new StringTokenizer(line);
				
				//fault trace
				line = fileLines.get(i++);
				int numLocations = Integer.parseInt(line.trim());
				faultTrace = new FaultTrace(faultName);
				for(int locIndex=0; locIndex<numLocations; ++locIndex) {
					line = fileLines.get(i++);
				}				
			}
			}catch (Exception e) {
			e.printStackTrace();
		}	
		
		Iterator<String> it = sourceMagMapping.keySet().iterator();
		while(it.hasNext()) {
			String faultName = it.next();
			double mag = sourceMagMapping.get(faultName);
			double rate = sourceRateMapping.get(faultName);
			row  = sheet.createRow(rowIndex);
			row.createCell((short)1).setCellValue(faultName);
			row.createCell((short)2).setCellValue(MAG_FORMAT.format(mag));
			row.createCell((short)3).setCellValue((float)rate);
			row.createCell((short)4).setCellValue(MAG_FORMAT.format(mag));
			row.createCell((short)5).setCellValue((float)rate);
			rowIndex++;
		}
	
	}
	
	private void makeData(boolean connectMoreB_Faults, String defModelName) {
		
		ucerf2.getParameter(UCERF2.CONNECT_B_FAULTS_PARAM_NAME).setValue(new Boolean(connectMoreB_Faults));
		ucerf2.getParameter(UCERF2.DEFORMATION_MODEL_PARAM_NAME).setValue(defModelName);

		// Poisson
		ucerf2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);

		ucerf2.getParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME).setValue(Ellsworth_B_WG02_MagAreaRel.NAME);
		ucerf2.updateForecast();
		ArrayList<UnsegmentedSource> bFaultSourcesEllB_Poiss = ucerf2.get_B_FaultSources();

		ucerf2.getParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME).setValue(HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		ArrayList<UnsegmentedSource> bFaultSourcesHB_Poiss = ucerf2.get_B_FaultSources();

		// Empirical
		ucerf2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_EMPIRICAL);

		ucerf2.getParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME).setValue(Ellsworth_B_WG02_MagAreaRel.NAME);
		ucerf2.updateForecast();
		ArrayList<UnsegmentedSource> bFaultSourcesEllB_Emp = ucerf2.get_B_FaultSources();

		ucerf2.getParameter(UCERF2.MAG_AREA_RELS_PARAM_NAME).setValue(HanksBakun2002_MagAreaRel.NAME);
		ucerf2.updateForecast();
		ArrayList<UnsegmentedSource> bFaultSourcesHB_Emp = ucerf2.get_B_FaultSources();


		HSSFRow row;
		String faultModel = null;
		
		if(defModelName.equalsIgnoreCase("D2.4")) faultModel="F2.2";
		else if(defModelName.equalsIgnoreCase("D2.1")) faultModel="F2.1";
		else throw new RuntimeException("Unsupported deformation model");

		for(int i=0; i<bFaultSourcesEllB_Poiss.size(); ++i) {
			UnsegmentedSource sourceEllB = bFaultSourcesEllB_Poiss.get(i);
			UnsegmentedSource sourceHB = bFaultSourcesHB_Poiss.get(i);
			UnsegmentedSource sourceEllB_Emp = bFaultSourcesEllB_Emp.get(i);
			UnsegmentedSource sourceHB_Emp = bFaultSourcesHB_Emp.get(i);
			FaultSegmentData faultSegmentData = sourceEllB.getFaultSegmentData();
			row  = sheet.createRow(rowIndex);
			String bFaultName = faultSegmentData.getFaultName();
			if(connectMoreB_Faults && bFaultName.indexOf("Connected")==-1) continue;
			if(!connectMoreB_Faults && bFaultName.indexOf("Connected")!=-1) continue;
			if(nameRowMapping.containsKey(bFaultName)) {
				String faultModelName = nameFaultModelMapping.get(bFaultName);
				faultModelName+=",F2.2";
				nameFaultModelMapping.put(bFaultName, faultModelName);
				continue;
			}
			nameRowMapping.put(bFaultName, rowIndex); // bFault and rowId mapping
			nameFaultModelMapping.put(bFaultName, faultModel); // bFault and fault model mapping
			//row.createCell((short)0).setCellValue(rowIndex-1);
			row.createCell((short)1).setCellValue(bFaultName);
			row.createCell((short)2).setCellValue(MAG_FORMAT.format(sourceEllB.getSourceMag()));
			row.createCell((short)3).setCellValue((float)sourceEllB.getMagFreqDist().getTotalIncrRate());
			row.createCell((short)4).setCellValue(MAG_FORMAT.format(sourceHB.getSourceMag()));
			row.createCell((short)5).setCellValue((float)sourceHB.getMagFreqDist().getTotalIncrRate());
			double avgProb6_7 = (sourceEllB.computeTotalProbAbove(6.7) + sourceHB.computeTotalProbAbove(6.7))/2;
			row.createCell((short)6).setCellValue((float)avgProb6_7);
			double emp1 = sourceEllB_Emp.computeTotalProb()/sourceEllB.computeTotalProb();
			double emp2  = sourceHB_Emp.computeTotalProb()/sourceHB.computeTotalProb();
			row.createCell((short)7).setCellValue((float)(emp1+emp2)/2);
			row.createCell((short)8).setCellValue(SLIP_RATE_FORMAT.format(faultSegmentData.getTotalAveSlipRate()*1e3));
			row.createCell((short)9).setCellValue(AREA_LENGTH_FORMAT.format(faultSegmentData.getTotalArea()/1e6));
			row.createCell((short)10).setCellValue(AREA_LENGTH_FORMAT.format(faultSegmentData.getTotalLength()/1e3));
			row.createCell((short)11).setCellValue(MOMENT_FORMAT.format(sourceEllB.getMomentRate()));
			++rowIndex;
		}
	}
	
	public static void main(String []args) {
		new MakeB_FaultsTable();
	}

}
