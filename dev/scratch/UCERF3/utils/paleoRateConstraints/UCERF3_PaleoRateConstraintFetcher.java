package scratch.UCERF3.utils.paleoRateConstraints;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class UCERF3_PaleoRateConstraintFetcher {
	
	private final static String PALEO_DATA_SUB_DIR = "paleoRateData";
	private final static String PALEO_DATA_FILE_NAME = "UCERF3_PaleoRateData_v03.xls";
	
	protected final static boolean D = true;  // for debugging
	
	public static ArrayList<PaleoRateConstraint> getConstraints(
			List<FaultSectionPrefData> faultSectionData) throws IOException {
		
		ArrayList<PaleoRateConstraint> paleoRateConstraints   = new ArrayList<PaleoRateConstraint>();
		if(D) System.out.println("Reading Paleo Seg Rate Data from "+PALEO_DATA_FILE_NAME);
		InputStream is =
			UCERF3_DataUtils.locateResourceAsStream(PALEO_DATA_SUB_DIR, PALEO_DATA_FILE_NAME);
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowIndex = sheet.getLastRowNum();
		double lat, lon, meanRate, lower68Conf, upper68Conf;
		String siteName;
		for(int r=1; r<=lastRowIndex; ++r) {	
			HSSFRow row = sheet.getRow(r);
			if(row==null) continue;
			HSSFCell cell = row.getCell(1);
			if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
			lat = cell.getNumericCellValue();
			siteName = row.getCell(0).getStringCellValue().trim();
			lon = row.getCell(2).getNumericCellValue();
			// skipping MRI cells
			meanRate = row.getCell(6).getNumericCellValue();
			lower68Conf = row.getCell(8).getNumericCellValue();	// note the labels are swapped in the *_v1 file
			upper68Conf =  row.getCell(7).getNumericCellValue();
				
			// get Closest section
			double minDist = Double.MAX_VALUE, dist;
			int closestFaultSectionIndex=-1;
			Location loc = new Location(lat,lon);
			for(int sectionIndex=0; sectionIndex<faultSectionData.size(); ++sectionIndex) {
				FaultSectionPrefData data = faultSectionData.get(sectionIndex);
				if (siteName.equals("N. San Andreas -  Filoli") && data.getName().contains("Monte Vista"))
					continue; // TODO remove hack!
				dist  = data.getFaultTrace().minDistToLine(loc);
				if(dist<minDist) {
					minDist = dist;
					closestFaultSectionIndex = sectionIndex;
				}
			}
			if(minDist>2) {
				if (D) System.out.println("No match for: "+siteName+" (lat="+lat+", lon="+lon
						+") closest was "+minDist+" away: "+faultSectionData.get(closestFaultSectionIndex).getSectionName());
				continue; // closest fault section is at a distance of more than 2 km
			}
			
			// add to Seg Rate Constraint list
			String name = faultSectionData.get(closestFaultSectionIndex).getSectionName();
			PaleoRateConstraint paleoRateConstraint = new PaleoRateConstraint(name, loc, closestFaultSectionIndex, 
					meanRate, lower68Conf, upper68Conf);
			if(D) System.out.println("\t"+siteName+" (lat="+lat+", lon="+lon+") associated with "+name+
					" (section index = "+closestFaultSectionIndex+")\tdist="+(float)minDist+"\tmeanRate="+(float)meanRate+
					"\tlower68="+(float)lower68Conf+"\tupper68="+(float)upper68Conf);
			paleoRateConstraints.add(paleoRateConstraint);
		}
		return paleoRateConstraints;
	}
	
	public static void showSegRateComparison(ArrayList<PaleoRateConstraint> paleoRateConstraint,
			ArrayList<FaultSystemSolution> solutions) {
		
		Preconditions.checkState(paleoRateConstraint.size() > 0, "Must have at least one rate constraint");
		Preconditions.checkState(solutions.size() > 0, "Must have at least one solution");
		
		int numSolSects = -1;
		for (FaultSystemSolution sol : solutions) {
			if (numSolSects < 0)
				numSolSects = sol.getNumSections();
			Preconditions.checkArgument(sol.getNumSections() == numSolSects,
					"num sections is inconsistant between solutions!");
		}
		
		List<FaultSectionPrefData> datas = solutions.get(0).getFaultSectionDataList();
		
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		
		ArbitrarilyDiscretizedFunc paleoRateMean = new ArbitrarilyDiscretizedFunc();
		paleoRateMean.setName("Paleo Rate Constraint: Mean");
		funcs.add(paleoRateMean);
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, Color.BLACK));
		ArbitrarilyDiscretizedFunc paleoRateUpper = new ArbitrarilyDiscretizedFunc();
		paleoRateUpper.setName("Paleo Rate Constraint: Upper 95% Confidence");
		funcs.add(paleoRateUpper);
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, Color.BLACK));
		ArbitrarilyDiscretizedFunc paleoRateLower = new ArbitrarilyDiscretizedFunc();
		paleoRateLower.setName("Paleo Rate Constraint: Upper 95% Confidence");
		funcs.add(paleoRateLower);
		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.DASH, 5f, Color.BLACK));
		
		final int xGap = 5;
		
		int x = xGap;
		
		HashMap<Integer, Integer> xIndForParentMap = new HashMap<Integer, Integer>();
		
		for (PaleoRateConstraint constr : paleoRateConstraint) {
			int sectID = constr.getSectionIndex();
			int parentID = -1;
			String name = null;
			for (FaultSectionPrefData data : datas) {
				if (data.getSectionId() == sectID) {
					if (data.getParentSectionId() < 0)
						throw new IllegalStateException("parent ID isn't populated for solution!");
					parentID = data.getParentSectionId();
					name = data.getParentSectionName();
					break;
				}
			}
			if (parentID < 0) {
				System.err.println("No match for rate constraint for section "+sectID);
				continue;
			}
			
			int minSect = Integer.MAX_VALUE;
			int maxSect = -1;
			for (FaultSectionPrefData data : datas) {
				if (data.getParentSectionId() == parentID) {
					int mySectID = data.getSectionId();
					if (mySectID < minSect)
						minSect = mySectID;
					if (mySectID > maxSect)
						maxSect = mySectID;
				}
			}
			
			Preconditions.checkState(maxSect >= minSect);
			int numSects = maxSect - minSect;
			
			int relConstSect = sectID - minSect;
			
			double paleoRateX;
			
			if (xIndForParentMap.containsKey(parentID)) {
				// we already have this parent section, just add the new rate constraint
				
				paleoRateX = xIndForParentMap.get(parentID) + relConstSect;
			} else {
				paleoRateX = x + relConstSect;
				
				for (int i=0; i<solutions.size(); i++) {
					FaultSystemSolution sol = solutions.get(i);
					Color color = GraphPanel.defaultColor[i % GraphPanel.defaultColor.length];
					
					EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc((double)x, numSects, 1d);
					func.setName("(x="+x+") Solution "+i+" rates for: "+name);
					for (int j=0; j<numSects; j++) {
						double rate = 0;
						int mySectID = minSect + j;
						for (int rupID : sol.getRupturesForSection(mySectID))
							// TODO is this the right value here?
							rate += sol.getRateForRup(rupID) * sol.getProbPaleoVisible(sol.getMagForRup(rupID));
						func.set(j, rate);
					}
					funcs.add(func);
					plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color));
				}
				
				xIndForParentMap.put(parentID, x);
				
				x += numSects;
				x += xGap;
			}
			
			paleoRateMean.set(paleoRateX, constr.getMeanRate());
			paleoRateUpper.set(paleoRateX, constr.getUpper95ConfOfRate());
			paleoRateLower.set(paleoRateX, constr.getLower95ConfOfRate());
		}
		
		GraphiWindowAPI_Impl w = new GraphiWindowAPI_Impl(funcs, "Paleosiesmic Constraint Fit", plotChars, true);
		w.setX_AxisLabel("");
		w.setY_AxisLabel("Event Rate Per Year");
	}
	
	public static void main(String args[]) throws IOException, DocumentException {
		
   		FaultSystemRupSet faultSysRupSet = InversionFaultSystemRupSetFactory.cachedForBranch(DeformationModels.GEOLOGIC);
   		UCERF3_PaleoRateConstraintFetcher.getConstraints(faultSysRupSet.getFaultSectionDataList());

//		File rupSetsDir = new File(precomp, "FaultSystemRupSets");
//		ArrayList<FaultSystemSolution> sols = new ArrayList<FaultSystemSolution>();
//		sols.add(SimpleFaultSystemSolution.fromFile(new File(rupSetsDir, "UCERF2.xml")));
//		sols.add(SimpleFaultSystemSolution.fromFile(new File(rupSetsDir, "Model1.xml")));
//		
//		showSegRateComparison(getConstraints(precomp, sols.get(0).getFaultSectionDataList()), sols);
	}
}
