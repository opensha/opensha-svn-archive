package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.dom4j.DocumentException;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;

public class TablesAndPlotsGen {
	
	
	
	/**
	 * This creates the Average Slip data table for the report with columns for each Deformation Model.
	 * 
	 * @param outputFile if null output will be written to the console, otherwise written to the given file
	 * @throws IOException
	 */
	public static void buildAveSlipDataTable(File outputFile) throws IOException {
		boolean includeUCERF2 = true;
		
		List<DeformationModels> dms = Lists.newArrayList();
		if (includeUCERF2)
			dms.add(DeformationModels.UCERF2_ALL);
		dms.add(DeformationModels.ABM);
		dms.add(DeformationModels.GEOLOGIC);
		dms.add(DeformationModels.NEOKINEMA);
		dms.add(DeformationModels.ZENGBB);
		
		Map<FaultModels, List<AveSlipConstraint>> aveSlipConstraints = Maps.newHashMap();
		Map<FaultModels, List<FaultSectionPrefData>> subSectDatasMap = Maps.newHashMap();
		
		List<double[]> dmReducedSlipRates = Lists.newArrayList();
		
		List<String> header = Lists.newArrayList("FM 3.1 Mapping", "Latitude", "Longitude", "Weighted Mean");
		for (DeformationModels dm : dms) {
			FaultModels fm;
			if (dm == DeformationModels.UCERF2_ALL)
				fm = FaultModels.FM2_1;
			else
				fm = FaultModels.FM3_1;
			InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
					InversionModels.CHAR_CONSTRAINED, fm, dm);
			dmReducedSlipRates.add(rupSet.getSlipRateForAllSections());
			if (!aveSlipConstraints.containsKey(fm))
				aveSlipConstraints.put(fm, AveSlipConstraint.load(rupSet.getFaultSectionDataList()));
			if (!subSectDatasMap.containsKey(fm))
				subSectDatasMap.put(fm, rupSet.getFaultSectionDataList());
			
			header.add(dm.getName()+" Reduced Slip Rate");
			header.add(dm.getName()+" Proxy Event Rate");
		}
		
		CSVFile<String> csv = new CSVFile<String>(true);
		csv.addLine(header);
		
		List<AveSlipConstraint> fm2Constraints = aveSlipConstraints.get(FaultModels.FM2_1);
		List<AveSlipConstraint> fm3Constraints = aveSlipConstraints.get(FaultModels.FM3_1);
		
		for (AveSlipConstraint constr : fm3Constraints) {
			List<String> line = Lists.newArrayList();
			
			String subSectName = subSectDatasMap.get(FaultModels.FM3_1).get(constr.getSubSectionIndex()).getSectionName();
			line.add(subSectName);
			line.add(constr.getSiteLocation().getLatitude()+"");
			line.add(constr.getSiteLocation().getLongitude()+"");
			line.add(constr.getWeightedMean()+"");
			for (int i=0; i<dms.size(); i++) {
				DeformationModels dm = dms.get(i);
				
				AveSlipConstraint myConstr = null;
				if (dm == DeformationModels.UCERF2_ALL) {
					// find the equivelant ave slip constraint by comparing locations as the list may be of different
					// size (such as with Compton not existing in FM2.1)
					for (AveSlipConstraint u2Constr : fm2Constraints) {
						if (u2Constr.getSiteLocation().equals(constr.getSiteLocation())) {
							myConstr = u2Constr;
							break;
						}
					}
				} else {
					myConstr = constr;
				}
				
				if (myConstr == null) {
					line.add("");
					line.add("");
				} else {
					double reducedSlip = dmReducedSlipRates.get(i)[myConstr.getSubSectionIndex()];
					line.add(reducedSlip+"");
					double proxyRate = reducedSlip / myConstr.getWeightedMean();
					line.add(proxyRate+"");
				}
			}
			csv.addLine(line);
		}
		
		// TODO add notes:
		//		reduced for char branch
		//		lat/lon: center points of sub section
		
		if (outputFile == null) {
			// print it
			for (List<String> line : csv) {
				System.out.println(Joiner.on('\t').join(line));
			}
		} else {
			// write it
			csv.writeToFile(outputFile);
		}
	}
	
	
	public static void makePreInversionMFDsFig() {
		InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.ZENG, 
				InversionModels.CHAR_CONSTRAINED, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, 
				TotalMag5Rate.RATE_7p9, MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3);
		System.out.println(rupSet.getPreInversionAnalysisData(true));
		FaultSystemRupSetCalc.plotPreInversionMFDs(rupSet, false, false, true, "preInvCharMFDs.pdf");
		
		rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.ZENG, 
				InversionModels.GR_CONSTRAINED, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, 
				TotalMag5Rate.RATE_7p9, MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3);
		FaultSystemRupSetCalc.plotPreInversionMFDs(rupSet, false, false, false, "preInvGR_MFDs.pdf");
		
		rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.ZENG, 
				InversionModels.GR_CONSTRAINED, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, 
				TotalMag5Rate.RATE_7p9, MaxMagOffFault.MAG_7p6, MomentRateFixes.APPLY_IMPLIED_CC, SpatialSeisPDF.UCERF3);
		FaultSystemRupSetCalc.plotPreInversionMFDs(rupSet, false, false, false, "preInvGR_MFDs_applCC.pdf");
	}
	
	
	
	public static void makeDefModSlipRateMaps() {
		Region region = new CaliforniaRegions.RELM_TESTING();
		File saveDir = GMT_CA_Maps.GMT_DIR;
		boolean display = true;
		try {
			FaultBasedMapGen.plotDeformationModelSlips(region, saveDir, display);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static void makeParentSectConvergenceTable(
			File csvOutputFile, AverageFaultSystemSolution aveSol, int parentSectionID) throws IOException {
		List<Integer> rups = aveSol.getRupSet().getRupturesForParentSection(parentSectionID);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		int numSols = aveSol.getNumSolutions();
		
		List<String> header = Lists.newArrayList();
		header.add("Rup ID");
		header.add("Mag");
		header.add("Length (km)");
		header.add("Start Sect");
		header.add("End Sect");
		header.add("Mean Rate");
		header.add("Min Rate");
		header.add("Max Rate");
		header.add("Std Dev Rate");
		for (int i=0; i<numSols; i++)
			header.add("Rate #"+i);
		
		csv.addLine(header);
		
		for (int rup : rups) {
			List<String> line = Lists.newArrayList();
			
			List<FaultSectionPrefData> sects = aveSol.getRupSet().getFaultSectionDataForRupture(rup);
			
			double[] rates = aveSol.getRatesForAllSols(rup);
			
			line.add(rup+"");
			line.add(aveSol.getRupSet().getMagForRup(rup)+"");
			line.add(aveSol.getRupSet().getLengthForRup(rup)/1000d+""); // m => km
			line.add(sects.get(0).getSectionName());
			line.add(sects.get(sects.size()-1).getSectionName());
			line.add(aveSol.getRateForRup(rup)+"");
			line.add(aveSol.getRateMin(rup)+"");
			line.add(aveSol.getRateMax(rup)+"");
			line.add(aveSol.getRateStdDev(rup)+"");
			
			for (int i=0; i<rates.length; i++)
				line.add(rates[i]+"");
			
			csv.addLine(line);
		}
		
		csv.writeToFile(csvOutputFile);
	}
	
	private static final String FAULT_SUPRA_TARGET = "Fault Target Supra Seis Moment Rate";
	private static final String FAULT_SUPRA_SOLUTION = "Fault Solution Supra Seis Moment Rate";
	private static final String FAULT_SUB_TARGET = "Fault Target Sub Seis Moment Rate";
	private static final String FAULT_SUB_SOLUTION = "Fault Solution Sub Seis Moment Rate";
	private static final String TRULY_OFF_TARGET = "Truly Off Fault Target Moment Rate";
	private static final String TRULY_OFF_SOLUTION = "Truly Off Fault Solution Moment Rate";
	
	/**
	 * This writes the moment rates table to a CSV file for the given CompoundFaultSystemSolution
	 * @param cfss
	 * @param csvFile
	 * @throws IOException
	 */
	public static void makeCompoundFSSMomentRatesTable(CompoundFaultSystemSolution cfss, File csvFile)
			throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList();
		for (Class<? extends LogicTreeBranchNode<?>> clazz : LogicTreeBranch.getLogicTreeNodeClasses())
			header.add(ClassUtils.getClassNameWithoutPackage(clazz));
		header.add(FAULT_SUPRA_TARGET);
		header.add(FAULT_SUPRA_SOLUTION);
		header.add(FAULT_SUB_TARGET);
		header.add(FAULT_SUB_SOLUTION);
		header.add(TRULY_OFF_TARGET);
		header.add(TRULY_OFF_SOLUTION);
		
		csv.addLine(header);
		
		List<LogicTreeBranch> branches = Lists.newArrayList(cfss.getBranches());
		Collections.sort(branches);
		
		Splitter sp = Splitter.on("\n");
		
		for (LogicTreeBranch branch : branches) {
			List<String> line = Lists.newArrayList();
			for (int i=0; i<LogicTreeBranch.getLogicTreeNodeClasses().size(); i++)
				line.add(branch.getValue(i).getShortName());
			List<String> info = Lists.newArrayList(sp.split(cfss.getInfo(branch)));
			line.add(getField(info, FAULT_SUPRA_TARGET)+"");
			line.add(getField(info, FAULT_SUPRA_SOLUTION)+"");
			line.add(getField(info, FAULT_SUB_TARGET)+"");
			line.add(getField(info, FAULT_SUB_SOLUTION)+"");
			line.add(getField(info, TRULY_OFF_TARGET)+"");
			line.add(getField(info, TRULY_OFF_SOLUTION)+"");
			
			csv.addLine(line);
		}
		
		csv.writeToFile(csvFile);
	}
	
	private static double getField(List<String> infoLines, String fieldStart) {
		for (String infoLine : infoLines) {
			infoLine = infoLine.trim();
			if (infoLine.startsWith(fieldStart))
				return Double.parseDouble(infoLine.substring(infoLine.lastIndexOf(" ")+1));
		}
		return Double.NaN;
	}
	
	private static HistogramFunction loadSurfaceRupData() throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(
				UCERF3_DataUtils.locateResourceAsStream("misc", "Surface_Rupture_Data_Wells_043013.xls"));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		int allSRL_col = 28;
		
		HistogramFunction hist = buildEmptyLengthHist();
		int cnt = 0;
		
		for (int rowIndex=0; rowIndex<=sheet.getLastRowNum(); rowIndex++) {
			HSSFRow row = sheet.getRow(rowIndex);
			if (row == null)
				continue;
			HSSFCell cell = row.getCell(allSRL_col);
			if (cell == null || cell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC)
				continue;
			double length = cell.getNumericCellValue();
			hist.add(length, 1d);
			cnt++;
		}
		System.out.println("Loaded "+cnt+" values from data file");
		hist.normalizeBySumOfY_Vals();
		return hist;
	}
	
	private static HistogramFunction buildEmptyLengthHist() {
		// should be big enough for all UCERF3 ruptures
		return new HistogramFunction(25, 25, 50d);
	}
	
	public static void buildRupLengthComparisonPlot(CompoundFaultSystemSolution cfss, File dir, String prefix) throws IOException {
		List<HistogramFunction> hists = Lists.newArrayList();
		
		for (LogicTreeBranch branch : cfss.getBranches()) {
			double[] lengths = cfss.getLengths(branch);
			double[] rates = cfss.getRates(branch);
			
			HistogramFunction hist = buildEmptyLengthHist();
			
			for (int r=0; r<lengths.length; r++) {
				hist.add(lengths[r]/1000d, rates[r]);
			}
			hist.normalizeBySumOfY_Vals();
			
			hists.add(hist);
		}
		
		HistogramFunction data = loadSurfaceRupData();
		
		XY_DataSetList xyList = new XY_DataSetList();
		for (HistogramFunction hist : hists) {
			xyList.add(hist);
		}
		APrioriBranchWeightProvider weightProv = new APrioriBranchWeightProvider();
		List<Double> weights = Lists.newArrayList();
		for (LogicTreeBranch branch : cfss.getBranches())
			weights.add(weightProv.getWeight(branch));
		
		List<DiscretizedFunc> solFuncs = CompoundFSSPlots.getFractiles(xyList, weights, "Surface Rupture Length", new double[0]);
		List<PlotCurveCharacterstics> solChars = CompoundFSSPlots.getFractileChars(Color.RED, 0);
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		funcs.addAll(solFuncs);
		chars.addAll(solChars);
		
		funcs.add(data);
		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLUE));
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		CommandLineInversionRunner.setFontSizes(gp);
		gp.setBackgroundColor(Color.WHITE);
		gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.setUserBounds(0d, 500d, 0d, 1d);
		gp.drawGraphPanel("Rupture Length (km)", "Fraction of Earthquakes", funcs, chars, true,
				"Rupture Length Distribution");
		File outputFile = new File(dir, prefix+"_length_dists");
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPNG(outputFile.getAbsolutePath()+".png");
		gp.saveAsPDF(outputFile.getAbsolutePath()+".pdf");
		gp.saveAsTXT(outputFile.getAbsolutePath()+".txt");
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		
//		makePreInversionMFDsFig();
//		makeDefModSlipRateMaps();

		
//		buildAveSlipDataTable(new File("ave_slip_table.csv"));
//		System.exit(0);
		
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File compoundFile = new File(invDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip");
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(compoundFile);
		makeCompoundFSSMomentRatesTable(cfss,
				new File(invDir, compoundFile.getName().replaceAll(".zip", "_mo_rates.csv")));
		
		buildRupLengthComparisonPlot(cfss, invDir, compoundFile.getName().replaceAll(".zip", ""));
		
		
//		int mojaveParentID = 301;
//		int littleSalmonParentID = 17;
//		AverageFaultSystemSolution aveSol = AverageFaultSystemSolution.fromZipFile(
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/" +
//						"InversionSolutions/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7" +
//						"_MMaxOff7.6_NoFix_SpatSeisU3_VarZeros_mean_sol.zip"));
//		makeParentSectConvergenceTable(new File("/tmp/little_salmon_onshore_rups_start_zero.csv"), aveSol, littleSalmonParentID);
	}

}
