package scratch.UCERF3.utils.paleoRateConstraints;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class UCERF3_PaleoRateConstraintFetcher {
	
	private final static String PALEO_DATA_SUB_DIR = "paleoRateData";
	private final static String PALEO_DATA_FILE_NAME = "UCERF3_PaleoRateData_v05.xls";
	
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
			
			if (lower68Conf == upper68Conf) {
				// TODO we don't want any of these
				System.out.println("Skipping value at "+siteName+" because upper and lower " +
						"values are equal: meanRate="+(float)meanRate+
					"\tlower68="+(float)lower68Conf+"\tupper68="+(float)upper68Conf);
				continue;
			}
				
			// get Closest section
			double minDist = Double.MAX_VALUE, dist;
			int closestFaultSectionIndex=-1;
			Location loc = new Location(lat,lon);
			
			// these hacks along with SCEC-VDO images are described in an e-mail from
			// Kevin on 3/6/12, subject "New MRI table"
			boolean blindThrustHack = siteName.equals("Compton") || siteName.equals("Puente Hills");
			boolean safOffshoreHack = siteName.equals("N. San Andreas -Offshore Noyo");
			
			for(int sectionIndex=0; sectionIndex<faultSectionData.size(); ++sectionIndex) {
				FaultSectionPrefData data = faultSectionData.get(sectionIndex);
				// TODO this is a hack for blind thrust faults
				if (blindThrustHack && !data.getSectionName().contains(siteName))
					continue;
				dist  = data.getFaultTrace().minDistToLine(loc);
				if(dist<minDist) {
					minDist = dist;
					closestFaultSectionIndex = sectionIndex;
				}
			}
			if(minDist>2 && !blindThrustHack && !safOffshoreHack || closestFaultSectionIndex < 0) {
				if (D) {
					if (D) System.out.print("No match for: "+siteName+" (lat="+lat+", lon="+lon
							+") closest was "+minDist+" away: "+closestFaultSectionIndex);
					if (closestFaultSectionIndex >= 0)
						System.out.println(". "+faultSectionData.get(closestFaultSectionIndex).getSectionName());
					else
						System.out.println();
				}
				continue; // closest fault section is at a distance of more than 2 km
			}
			System.out.println("Matching constraint for closest index: "+closestFaultSectionIndex+" site name: "+siteName);
			// add to Seg Rate Constraint list
			String name = faultSectionData.get(closestFaultSectionIndex).getSectionName();
			PaleoRateConstraint paleoRateConstraint = new PaleoRateConstraint(name, loc, closestFaultSectionIndex, 
					meanRate, lower68Conf, upper68Conf);
			paleoRateConstraint.setPaleoSiteName(siteName);
			if(D) System.out.println("\t"+siteName+" (lat="+lat+", lon="+lon+") associated with "+name+
					" (section index = "+closestFaultSectionIndex+")\tdist="+(float)minDist+"\tmeanRate="+(float)meanRate+
					"\tlower68="+(float)lower68Conf+"\tupper68="+(float)upper68Conf);
			paleoRateConstraints.add(paleoRateConstraint);
		}
		return paleoRateConstraints;
	}
	
	public static PlotSpec getSegRateComparisonSpec(ArrayList<PaleoRateConstraint> paleoRateConstraint,
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
		List<Map<Integer, DiscretizedFunc>> funcParentsMapsList = Lists.newArrayList();
		for (int i=0; i<solutions.size(); i++)
			funcParentsMapsList.add(new HashMap<Integer, DiscretizedFunc>());
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
		
		PaleoProbabilityModel paleoProbModel = null;
		try {
			paleoProbModel = UCERF3_PaleoProbabilityModel.load();
		} catch (IOException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		int x = xGap;
		
		HashMap<Integer, Integer> xIndForParentMap = new HashMap<Integer, Integer>();
		
		ArrayList<Double> runningMisfitTotals = Lists.newArrayList();
		for (int i=0; i<solutions.size(); i++)
			runningMisfitTotals.add(0d);
		
		Map<Integer, Double> traceLengthCache = Maps.newHashMap();
		
		for (int p=0; p<paleoRateConstraint.size(); p++) {
			PaleoRateConstraint constr = paleoRateConstraint.get(p);
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
					Map<Integer, DiscretizedFunc> funcParentsMap = funcParentsMapsList.get(i);
					FaultSystemSolution sol = solutions.get(i);
					Color color = GraphPanel.defaultColor[i % GraphPanel.defaultColor.length];
					
					EvenlyDiscretizedFunc paleoRtFunc = new EvenlyDiscretizedFunc((double)x, numSects, 1d);
					EvenlyDiscretizedFunc origRtFunc = null;
					if (solutions.size() == 1)
						origRtFunc = new EvenlyDiscretizedFunc((double)x, numSects, 1d);
					paleoRtFunc.setName("(x="+x+") Solution "+i+" paleo rates for: "+name);
					origRtFunc.setName("(x="+x+") Solution "+i+" original rates for: "+name);
					for (int j=0; j<numSects; j++) {
						int mySectID = minSect + j;
						paleoRtFunc.set(j, getPaleoRateForSect(sol, mySectID, paleoProbModel, traceLengthCache));
						if (origRtFunc != null)
							origRtFunc.set(j, getPaleoRateForSect(sol, mySectID, null, traceLengthCache));
					}
					funcs.add(paleoRtFunc);
					plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, color));
					if (origRtFunc != null) {
						funcs.add(origRtFunc);
						Color origColor = GraphPanel.defaultColor[(i+1) % GraphPanel.defaultColor.length];
						plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, origColor));
					}
					
					funcParentsMap.put(parentID, paleoRtFunc);
				}
				
				xIndForParentMap.put(parentID, x);
				
				x += numSects;
				x += xGap;
			}
			
			for (int i=0; i<solutions.size(); i++) {
				DiscretizedFunc func = funcParentsMapsList.get(i).get(parentID);
				double rate = getPaleoRateForSect(solutions.get(i), sectID, paleoProbModel, traceLengthCache);
//				double misfit = Math.pow(constr.getMeanRate() - rate, 2) / Math.pow(constr.getStdDevOfMeanRate(), 2);
				double misfit = Math.pow((constr.getMeanRate() - rate) / constr.getStdDevOfMeanRate(), 2);
				String info = func.getInfo();
				if (info == null || info.isEmpty())
					info = "";
				else
					info += "\n";
				info += "\tSect "+sectID+". Mean: "+constr.getMeanRate()+"\tStd Dev: "
					+constr.getStdDevOfMeanRate()+"\tSolution: "+rate+"\tMisfit: "+misfit;
				runningMisfitTotals.set(i, runningMisfitTotals.get(i)+misfit);
				func.setInfo(info);
			}
			
			paleoRateMean.set(paleoRateX, constr.getMeanRate());
			paleoRateUpper.set(paleoRateX, constr.getUpper95ConfOfRate());
			paleoRateLower.set(paleoRateX, constr.getLower95ConfOfRate());
		}
		
		int lastIndex = funcs.size() - solutions.size();
		for (int i=0; i<solutions.size(); i++) {
			DiscretizedFunc func = funcs.get(lastIndex++);
			
			String info = func.getInfo();
			double totMisfit = runningMisfitTotals.get(i);
			info += "\n\n\tTOTAL MISFIT: "+totMisfit;
			
			func.setInfo(info);
		}
		
		return new PlotSpec(funcs, plotChars, "Paleosiesmic Constraint Fit", "", "Event Rate Per Year");
	}
	
	private static double getPaleoRateForSect(FaultSystemSolution sol, int sectIndex,
			PaleoProbabilityModel paleoProbModel, Map<Integer, Double> traceLengthCache) {
		double rate = 0;
		for (int rupID : sol.getRupturesForSection(sectIndex)) {
			double rupRate = sol.getRateForRup(rupID);
			if (paleoProbModel != null)
				rupRate *= paleoProbModel.getProbPaleoVisible(sol, rupID, sectIndex);
			rate += rupRate;
		}
		return rate;
	}
	
	public static void showSegRateComparison(ArrayList<PaleoRateConstraint> paleoRateConstraint,
			ArrayList<FaultSystemSolution> solutions) {
		PlotSpec spec = getSegRateComparisonSpec(paleoRateConstraint, solutions);
		
		GraphiWindowAPI_Impl w = new GraphiWindowAPI_Impl(spec.getFuncs(), spec.getTitle(), spec.getChars(), true);
		w.setX_AxisLabel(spec.getxAxisLabel());
		w.setY_AxisLabel(spec.getyAxisLabel());
	}
	
	public static HeadlessGraphPanel getHeadlessSegRateComparison(ArrayList<PaleoRateConstraint> paleoRateConstraint,
			ArrayList<FaultSystemSolution> solutions, boolean yLog) {
		PlotSpec spec = getSegRateComparisonSpec(paleoRateConstraint, solutions);
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setYLog(yLog);
		
		gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(), spec.getFuncs(), spec.getChars(), false, spec.getTitle());
		
		return gp;
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
