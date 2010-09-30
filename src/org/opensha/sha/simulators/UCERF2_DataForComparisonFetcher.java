package org.opensha.sha.simulators;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

public class UCERF2_DataForComparisonFetcher {
	
	private final static String SEG_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/data/Appendix_C_Table7_091807.xls";
	private final static String PARSONS_PDF_DATA_DIR = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/data/ParsonsMRI_PDFs";
	ArrayList<String> parsonsSiteNames;
	ArrayList<String> parsonsPDF_FileNamesPois;
	ArrayList<String> parsonsPDF_FileNamesBPT;
	ArrayList<Location> parsonsSiteLocs;
	ArrayList<Double> parsonsBestMRIs;
	ArrayList<Double> parsonsMRI_Sigmas;
	ArrayList<Double> parsonsMRI_Lower95s;
	ArrayList<Double> parsonsMRI_Upper95s;
	
	
	public UCERF2_DataForComparisonFetcher() {
		readParsonsXLS_File();
		readParsonsPDF_Data();
		
	}

	/**
	 * This returns only a single best-estimate incremental UCERF2 MFDs for the RELM region because
	 * UCERF2 doesn't give more due to dubious uncertainties.  All the rates are multiplied by 0.5 
	 * in order to approximate the rates expected for Northern or Southern California.  This was 
	 * done because UCERF2 does not supply No and/or So cal rates for MFDs that include aftershocks.  
	 * The 0.5 value is justified given overall uncertainties (e.g., when looking at the obs MFDs 
	 * that exclude aftershocks).
	 * @param includeAftershocks
	 * @return
	 */
	public static DiscretizedFuncAPI getHalf_UCERF2_ObsIncrMFDs(boolean includeAftershocks) {
		UCERF2 ucerf2 = new UCERF2();
		ArrayList<DiscretizedFuncAPI> funcs = new ArrayList<DiscretizedFuncAPI>();
		funcs.addAll(ucerf2.getObsIncrMFD(includeAftershocks));
		for(DiscretizedFuncAPI func:funcs) {
			for(int i=0;i<func.getNum();i++) func.set(i,func.getY(i)*0.5);
			func.setInfo("  ");
		}
		funcs.get(0).setName("UCERF2 Observed Incremental MFD Divided by Two (best estimate)");
		return funcs.get(0);
	}
	
	
	/**
	 * This returns a list of observed cumulative UCERF2 MFDs for the RELM region, where the first is the 
	 * best estimate, the second is the lower 95% confidence bound, and the third is the upper 
	 * 95% confidence bound.  All the rates are multiplied by 0.5 in order to approximate the rates
	 * expected for Northern or Southern California.  This was done because UCERF2 does
	 * not supply No and/or So cal rates for MFDs that include aftershocks.  The 0.5 value is justified 
	 * given overall uncertainties (e.g., when looking at the obs MFDs that exclude aftershocks).
	 * @param includeAftershocks
	 * @return
	 */
	public static ArrayList<DiscretizedFuncAPI> getHalf_UCERF2_ObsCumMFDs(boolean includeAftershocks) {
		ArrayList<DiscretizedFuncAPI> funcs = new ArrayList<DiscretizedFuncAPI>();
		funcs.addAll(UCERF2.getObsCumMFD(includeAftershocks));
		for(DiscretizedFuncAPI func:funcs) {
			for(int i=0;i<func.getNum();i++) func.set(i,func.getY(i)*0.5);
			func.setInfo("  ");
		}
		funcs.get(0).setName("UCERF2 Observed Cumulative MFD Divided by Two (best estimate)");
		funcs.get(1).setName("UCERF2 Observed Cumulative MFD Divided by Two (lower 95% confidence)");
		funcs.get(2).setName("UCERF2 Observed Cumulative MFD Divided by Two (upper 95% confidence)");
		return funcs;
	}

	
	
	private void readParsonsXLS_File() {
		try {				
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(SEG_RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			double lat, lon;;
			parsonsSiteNames = new ArrayList<String>();
			parsonsSiteLocs = new ArrayList<Location>();
			parsonsBestMRIs = new ArrayList<Double>();
			parsonsMRI_Sigmas = new ArrayList<Double>();
			parsonsMRI_Lower95s = new ArrayList<Double>();
			parsonsMRI_Upper95s = new ArrayList<Double>();

			for(int r=1; r<=lastRowIndex; ++r) {	
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell cell = row.getCell( (short) 1);
				if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
				parsonsSiteNames.add(row.getCell( (short) 0).getStringCellValue().trim());
				lat = cell.getNumericCellValue();
				lon = row.getCell( (short) 2).getNumericCellValue();
				parsonsSiteLocs.add(new Location(lat,lon));
				parsonsBestMRIs.add(row.getCell( (short) 3).getNumericCellValue());
				parsonsMRI_Sigmas.add(row.getCell( (short) 4).getNumericCellValue());
				parsonsMRI_Lower95s.add(row.getCell( (short) 7).getNumericCellValue());
				parsonsMRI_Upper95s.add(row.getCell( (short) 8).getNumericCellValue());
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
//		for(String name: parsonsSiteNames) System.out.println(name);
//		for(Location loc: parsonsSiteLocs) System.out.println((float)loc.getLatitude()+"\t"+(float)loc.getLongitude());

	}
	
	public ArrayList<String> getParsonsSiteNames() {return parsonsSiteNames;}
	
	public ArrayList<Location> getParsonsSiteLocs() { return parsonsSiteLocs; }
	
	public ArrayList<Double> getParsonsBestMRIs() {return parsonsBestMRIs;}
	
	public ArrayList<Double> getParsonsMRI_Sigmas() {return parsonsMRI_Sigmas;}
	
	public ArrayList<Double> getParsonsMRI_Lower95s() {return parsonsMRI_Lower95s;}
	
	public ArrayList<Double> getParsonsMRI_Upper95s() {return parsonsMRI_Upper95s;}
	
	private void setParsonsPDF_FileNames() {
		parsonsPDF_FileNamesPois = new ArrayList<String>();
		parsonsPDF_FileNamesBPT = new ArrayList<String>();

		// POISSON FILES
		parsonsPDF_FileNamesPois.add("poisson/tally1.cal_n.txt");				//	Calaveras fault - North
		parsonsPDF_FileNamesPois.add("poisson/tally1.els_glen_pois.txt");		//	Elsinore - Glen Ivy
		parsonsPDF_FileNamesPois.add("poisson/tally1.els_jul_pois.txt");		//	Elsinore Fault - Julian
		parsonsPDF_FileNamesPois.add("poisson/tally1.els_tem_pois.txt");		//	Elsinore - Temecula
		parsonsPDF_FileNamesPois.add("poisson/tally1.els_whit_pois.txt");		//	Elsinore - Whittier
		parsonsPDF_FileNamesPois.add("poisson/tally1.gar_c_pois.txt");			//	Garlock - Central
		parsonsPDF_FileNamesPois.add("poisson/tally1.gar_w_pois.txt");			//	Garlock - Western
		parsonsPDF_FileNamesPois.add("poisson/tally1.hayn_pois.txt");			//	Hayward fault - North
		parsonsPDF_FileNamesPois.add(null);				//	Hayward fault - South		**** old version was poisson/tally1.hays_pois.txt ****
		parsonsPDF_FileNamesPois.add(null);										//	N. San Andreas - Vendanta		File "poisson/tally1.nsaf_vend_pois.txt" is empty
		parsonsPDF_FileNamesPois.add(null);										//	SAF - Arano Flat
		parsonsPDF_FileNamesPois.add("poisson/tally1.ft_ross.txt");				//	N. San Andreas -  Fort Ross
		parsonsPDF_FileNamesPois.add("poisson/tally1.san_greg_pois.txt");		//	San Gregorio - North
		parsonsPDF_FileNamesPois.add(null);										//	San Jacinto - Hog Lake			File "poisson/tally1.sjc_hog_pois.txt" is empty
		parsonsPDF_FileNamesPois.add("poisson/tally1.sjc_sup_pois.txt");		//	San Jacinto - Superstition
		parsonsPDF_FileNamesPois.add("poisson/tally_burro_p_new.txt");			//	San Andreas - Burro Flats                         
		parsonsPDF_FileNamesPois.add(null);										//	SAF- Carrizo Bidart
		parsonsPDF_FileNamesPois.add(null);										//	SAF - Combined Carrizo Plain
		parsonsPDF_FileNamesPois.add("poisson/tallly1.indio_pois.txt");			//	San Andrteas - Indio  			File "poisson/tallly1.pallet_pois.txt" is empty
		parsonsPDF_FileNamesPois.add(null);										//	San Andreas - Pallett Creek
		parsonsPDF_FileNamesPois.add("poisson/tallly1.pitman_pois.txt");		//	San Andreas - Pitman Canyon      
		parsonsPDF_FileNamesPois.add("poisson/tallly1.plunge_pois.txt");		//	San Andreas - Plunge Creek   
		parsonsPDF_FileNamesPois.add("poisson/tallly1.thous_palms_pois.txt");	//	Mission Creek - 1000 Palms		File "poisson/tallly1.wrightwood_pois.txt" is empty
		parsonsPDF_FileNamesPois.add(null);	//	San Andreas - Wrightwood     
		// Not allocated:		
		//					poisson/tally1.ssas_pois.txt
		//					poisson/tally1.new_carrizo.txt
		//					poisson/tallly1.carrizo_pois.txt
		//					poisson/New_hays_Poiss.txt			**** Has a problem I asked Tom about in an email on 9-30-10
		
		// BPT FILES
		parsonsPDF_FileNamesBPT.add("bpt/tally_cal_n.txt");			//	Calaveras fault - North
		parsonsPDF_FileNamesBPT.add("bpt/tally_els_glen.txt");		//	Elsinore - Glen Ivy
		parsonsPDF_FileNamesBPT.add("bpt/tally_els_jul.txt");		//	Elsinore Fault - Julian
		parsonsPDF_FileNamesBPT.add("bpt/tally_els_tem.txt");		//	Elsinore - Temecula
		parsonsPDF_FileNamesBPT.add("bpt/tally_els_whit.txt");		//	Elsinore - Whittier
		parsonsPDF_FileNamesBPT.add("bpt/tally_gar_c.txt");			//	Garlock - Central
		parsonsPDF_FileNamesBPT.add("bpt/tally_gar_w.txt");			//	Garlock - Western
		parsonsPDF_FileNamesBPT.add("bpt/tally_hayn.txt");			//	Hayward fault - North
		parsonsPDF_FileNamesBPT.add("bpt/New_hays_BPT_tally.txt");	//	Hayward fault - South
		parsonsPDF_FileNamesBPT.add(null);							//	N. San Andreas - Vendanta
		parsonsPDF_FileNamesBPT.add(null);							//	SAF - Arano Flat
		parsonsPDF_FileNamesBPT.add("bpt/tally_ft_ross.txt");		//	N. San Andreas -  Fort Ross
		parsonsPDF_FileNamesBPT.add("bpt/tally_san_greg.txt");		//	San Gregorio - North
		parsonsPDF_FileNamesBPT.add(null);							//	San Jacinto - Hog Lake
		parsonsPDF_FileNamesBPT.add("bpt/tally_sjc_sup.txt");		//	San Jacinto - Superstition
		parsonsPDF_FileNamesBPT.add("bpt/tally_burro.txt");			//	San Andreas - Burro Flats                         
		parsonsPDF_FileNamesBPT.add(null);							//	SAF- Carrizo Bidart
		parsonsPDF_FileNamesBPT.add(null);							//	SAF - Combined Carrizo Plain     
		parsonsPDF_FileNamesBPT.add("bpt/tally_indio.txt");			//	San Andrteas - Indio  
		parsonsPDF_FileNamesBPT.add(null);							//	San Andreas - Pallett Creek
		parsonsPDF_FileNamesBPT.add("bpt/tally_pitman.txt");		//	San Andreas - Pitman Canyon      
		parsonsPDF_FileNamesBPT.add("bpt/tally_plunge.txt");		//	San Andreas - Plunge Creek   
		parsonsPDF_FileNamesBPT.add("bpt/tally_thous_palms.txt");	//	Mission Creek - 1000 Palms
		parsonsPDF_FileNamesBPT.add(null);							//	San Andreas - Wrightwood     
		// Not allocated:		
		//					bpt/tally_carrizo.txt
		//					bpt/tally_ssas.txt
	}
	
	
	/**
	 * This assumes that all MRI deltas are 10 yrs (true here)
	 */
	private void readParsonsPDF_Data() {
		setParsonsPDF_FileNames();
		ArrayList<EvenlyDiscretizedFunc> parsonsPoisPDF_Funcs = new ArrayList<EvenlyDiscretizedFunc>();
		ArrayList<EvenlyDiscretizedFunc> testList = new ArrayList<EvenlyDiscretizedFunc>();
		// Read Poisson PDF Files
		for(int f=0; f<parsonsPDF_FileNamesPois.size();f++) {
			String fileName = parsonsPDF_FileNamesPois.get(f);
//			System.out.println(fileName);
			if(fileName != null) {
				try {
					String filePath = PARSONS_PDF_DATA_DIR+"/"+fileName;
//					System.out.println(filePath);
					ArrayList<String> fileLines = FileUtils.loadJarFile(filePath);
					ArrayList<Double> mriList = new ArrayList<Double> ();
					ArrayList<Double> numHitsList = new ArrayList<Double> ();
					double totalNumHits=0;
					for(String line: fileLines) {
						StringTokenizer st = new StringTokenizer(line);
						mriList.add(Double.parseDouble(st.nextToken()));
						double numHits = Integer.parseInt(st.nextToken());
						if(numHits == 0)  System.out.println("fileName HAD A ZERO!!!");
						numHitsList.add(numHits);
						totalNumHits += numHits;
					}
					int num = (int)Math.round((mriList.get(mriList.size()-1) - mriList.get(0))/10.0) + 1;
//					System.out.println(mriList.get(0)+"\t"+mriList.get(mriList.size()-1)+"\t"+num);
//					if(fileName.equals("poisson/New_hays_Poiss.txt"))
//						for(Double val: mriList) System.out.println(val);
					EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(mriList.get(0), mriList.get(mriList.size()-1), num);
//					ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
					func.setTolerance(1.0);
					for(int i=0;i<mriList.size();i++) {
						func.set(mriList.get(i), numHitsList.get(i)/totalNumHits);
					}
					func.setName(parsonsSiteNames.get(f));
					func.setInfo("From file: "+fileName);
					parsonsPoisPDF_Funcs.add(func);
					testList = new ArrayList<EvenlyDiscretizedFunc>();
					testList.add(func);
					GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(testList, "Parson's PDFs");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				parsonsPoisPDF_Funcs.add(null);
			}
		}
		
		   


		/*
		ArbDiscrEmpiricalDistFunc func = new ArbDiscrEmpiricalDistFunc();
		for(String fileName: parsonsPDF_FileNamesBPT) {
			if(fileName != null)
				try {
					String filePath = PARSONS_PDF_DATA_DIR+"/"+fileName;
					System.out.println(filePath);
					ArrayList<String> fileLines = FileUtils.loadJarFile(filePath);
					for(String line: fileLines) {
						StringTokenizer st = new StringTokenizer(line);
						double cov = Double.parseDouble(st.nextToken());
						double mri = Double.parseDouble(st.nextToken());
						double numHits = Integer.parseInt(st.nextToken());
						func.set(cov, numHits);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		System.out.println(func);
		*/
	}



	public static void main(String[] args) {
		UCERF2_DataForComparisonFetcher test = new UCERF2_DataForComparisonFetcher();
		
	}
	

	

}
