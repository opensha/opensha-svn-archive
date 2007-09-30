/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UnsegmentedSource;

/**
 * @author vipingupta
 *
 */
public class MakeB_FaultsTable {
	private final static String FILENAME = "B-Faults.xls";
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.000E0");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat RATE_FORMAT = new DecimalFormat("0.00000");
	private final static DecimalFormat ASEISMSIC_FORMAT = new DecimalFormat("0.00");
	// bFaults name and row Id mapping
	private HashMap<String, Integer> nameRowMapping = new  HashMap<String, Integer>();
	private HashMap<String, String> nameFaultModelMapping = new  HashMap<String, String>();

	private HSSFWorkbook workbook  = new HSSFWorkbook();
	private HSSFSheet sheet;
	private UCERF2 ucerf2 = new UCERF2();
	int rowIndex;
	public MakeB_FaultsTable() {
		workbook  = new HSSFWorkbook();
		sheet = workbook.createSheet("B-Faults");
		HSSFRow row  = sheet.createRow(0);
		row.createCell((short)0).setCellValue("Index");
		row.createCell((short)1).setCellValue("Name");
		row.createCell((short)2).setCellValue("EllB-Mag");
		row.createCell((short)3).setCellValue("Ellb-Rate");
		row.createCell((short)4).setCellValue("HB-Mag");
		row.createCell((short)5).setCellValue("HB-Rate");
		row.createCell((short)6).setCellValue("Prob (Mag>=6.7)");
		row.createCell((short)7).setCellValue("Empirical Correction");
		row.createCell((short)8).setCellValue("Slip Rate (mm/yr)");
		row.createCell((short)9).setCellValue("Area (sq-km)");
		row.createCell((short)10).setCellValue("Length (km)");
		row.createCell((short)11).setCellValue("Moment Rate (Newton-meters/yr)");
		row.createCell((short)12).setCellValue("Fault Model");


		ucerf2 = new UCERF2();
		rowIndex=1;
		makeData(false, "D2.1");
		makeData(false, "D2.4");
		makeData(true, "D2.1");
		makeData(true, "D2.4");
//		 write  excel sheet
		try {
			FileOutputStream fileOut = new FileOutputStream(FILENAME);
			workbook.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
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
			row.createCell((short)0).setCellValue(rowIndex);
			row.createCell((short)1).setCellValue("Name");
			row.createCell((short)2).setCellValue(MAG_FORMAT.format(sourceEllB.getSourceMag()));
			row.createCell((short)3).setCellValue((float)sourceEllB.getMagFreqDist().getTotalIncrRate());
			row.createCell((short)4).setCellValue(MAG_FORMAT.format(sourceHB.getSourceMag()));
			row.createCell((short)5).setCellValue((float)sourceHB.getMagFreqDist().getTotalIncrRate());
			double avgProb6_7 = (sourceEllB.computeTotalProbAbove(6.7) + sourceHB.computeTotalProbAbove(6.7))/2;
			row.createCell((short)6).setCellValue((float)avgProb6_7);
			double emp1 = sourceEllB_Emp.computeTotalProb()/sourceEllB.computeTotalProb();
			double emp2  = sourceHB_Emp.computeTotalProb()/sourceHB.computeTotalProb();
			row.createCell((short)6).setCellValue((float)(emp1+emp2)/2);
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
