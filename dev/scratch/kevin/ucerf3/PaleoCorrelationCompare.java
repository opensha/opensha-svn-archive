package scratch.kevin.ucerf3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.dom4j.DocumentException;
import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoProbabilityModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PaleoCorrelationCompare {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		File solFile = new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"),
		"FM2_1_UC2ALL_EllB_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU2_VarSectNuclMFDWt0.01_VarPaleo1_VarMFDSmooth1000_sol.zip");
		FaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(solFile);
		
		String fileName = solFile.getAbsolutePath().replaceAll(".zip", "")+"_paleo_correlation.xls";
		File outputFile = new File(fileName);
		
		InputStream is = new FileInputStream(new File("D:\\Documents\\temp\\PaleoseisSiteDistancefinal.xls"));
		
		writePaleoComparison(sol, is, outputFile);
	}

	public static void writePaleoComparison(FaultSystemSolution sol, InputStream dataIS, File outputFile) throws IOException {
		List<FaultSectionPrefData> faultSectionData = sol.getFaultSectionDataList();

		PaleoProbabilityModel paleoProb = UCERF3_PaleoProbabilityModel.load();

		POIFSFileSystem fs = new POIFSFileSystem(dataIS);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		int lastRowIndex = sheet.getLastRowNum();

		List<int[]> ranges = Lists.newArrayList();
		int curStart = -1;
		for (int row=0; row<=lastRowIndex; row++) {
			String cellText = sheet.getRow(row).getCell(0).getStringCellValue().trim();
			if (cellText.equals("Site")) {
				curStart = row;
			} else if (cellText.isEmpty()) {
				if (curStart >= 0) {
					int[] range = { curStart+1, row-1 };
					ranges.add(range);
				}
				curStart = -1;
			}
		}

		for (int[] range : ranges) {
			System.out.println("Range: "+range[0]+" => "+range[1]);

			Map<String, Integer> locSubSectMap = Maps.newHashMap();

			for (int row=range[0]; row<=range[1]; row++) {
				HSSFRow theRow = sheet.getRow(row);
				String name = theRow.getCell(0).getStringCellValue().trim();
				double lat = theRow.getCell(4).getNumericCellValue();
				double lon = theRow.getCell(5).getNumericCellValue();
				Location loc = new Location(lat, lon);

				double minDist = Double.MAX_VALUE, dist;
				int closestFaultSectionIndex=-1;

				for(int sectionIndex=0; sectionIndex<faultSectionData.size(); ++sectionIndex) {
					FaultSectionPrefData data = faultSectionData.get(sectionIndex);

					dist  = data.getFaultTrace().minDistToLine(loc);
					if(dist<minDist) {
						minDist = dist;
						closestFaultSectionIndex = sectionIndex;
					}
				}

				Preconditions.checkState(minDist < 10d,
						"Min dist to sub sect greater than 10 KM: "+minDist+"\nloc: "+loc);

				locSubSectMap.put(name, closestFaultSectionIndex);
			}

			int startCol = 6;
			int endCol = startCol+locSubSectMap.size()-1;

			for (int row=range[0]; row<=range[1]; row++) {
				double relativeRow = row-range[0];
				HSSFRow theRow = sheet.getRow(row);
				String name1 = theRow.getCell(0).getStringCellValue().trim();
				Integer sectIndex1 = locSubSectMap.get(name1);

				for (int col=startCol; col<=endCol; col++) {
					int relativeCol = col-startCol;
					if (relativeCol == relativeRow)
						continue;
					if (relativeRow > relativeCol)
						continue;

					String name2 = sheet.getRow(range[0]-1).getCell(col).getStringCellValue().trim();
					Integer sectIndex2 = locSubSectMap.get(name2);
					Preconditions.checkNotNull(sectIndex2, "Name not found: "+name2+" ("+row+","+col+")");

					double rateTogether = 0d;
					double totRate = 0d;

					HashSet<Integer> rups1 = new HashSet<Integer>(sol.getRupturesForSection(sectIndex1));
					HashSet<Integer> rups2 = new HashSet<Integer>(sol.getRupturesForSection(sectIndex2));
					HashSet<Integer> totRups = new HashSet<Integer>();
					totRups.addAll(rups1);
					for (Integer rup : rups2)
						if (!totRups.contains(rup))
							totRups.add(rup);

					for (Integer rup : totRups) {
						double rate = sol.getRateForRup(rup);

						boolean sect1 = rups1.contains(rup);
						boolean sect2 = rups2.contains(rup);
						Preconditions.checkState(sect1 || sect2);
						boolean together = sect1 && sect2;

						if (sect1)
							rate *= paleoProb.getProbPaleoVisible(sol, rup, sectIndex1);
						if (sect2)
							rate *= paleoProb.getProbPaleoVisible(sol, rup, sectIndex2);

						if (together)
							rateTogether += rate;

						totRate += rate;
					}

					double prob = rateTogether / totRate;

					theRow.getCell(col).setCellValue(prob);
				}
			}
		}

		FileOutputStream fos = new FileOutputStream(outputFile);
		wb.write(fos);
		fos.close();
	}

}
