package scratch.UCERF3.utils.aveSlip;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class AveSlipConstraint {
	
	public static final String DIR_NAME = "aveSlip";
	public static final String FILE_NAME = "Table R5v4.xls";
	
	private int subSectionIndex;
	private double weightedMean;
	private double upperUncertaintyBound;
	private double lowerUncertaintyBound;
	
	public AveSlipConstraint(int subSectionIndex, double weightedMean,
			double upperUncertaintyBound, double lowerUncertaintyBound) {
		super();
		this.subSectionIndex = subSectionIndex;
		this.weightedMean = weightedMean;
		this.upperUncertaintyBound = upperUncertaintyBound;
		this.lowerUncertaintyBound = lowerUncertaintyBound;
	}

	protected int getSubSectionIndex() {
		return subSectionIndex;
	}

	/**
	 * Weighted mean slip, in meters
	 * 
	 * @return
	 */
	protected double getWeightedMean() {
		return weightedMean;
	}

	/**
	 * "Uncertainties is half-width of COPD peak. For asymetrical uncertainties, we conservatively
	 * use larger uncertainty in weighted mean calculation."
	 * 
	 * @return
	 */
	protected double getUpperUncertaintyBound() {
		return upperUncertaintyBound;
	}

	/**
	 * "Uncertainties is half-width of COPD peak. For asymetrical uncertainties, we conservatively
	 * use larger uncertainty in weighted mean calculation."
	 * 
	 * @return
	 */
	protected double getLowerUncertaintyBound() {
		return lowerUncertaintyBound;
	}
	
	@Override
	public String toString() {
		return "AveSlipConstraint [subSectionIndex=" + subSectionIndex
				+ ", weightedMean=" + weightedMean + ", upperUncertaintyBound="
				+ upperUncertaintyBound + ", lowerUncertaintyBound="
				+ lowerUncertaintyBound + "]";
	}

	public static List<AveSlipConstraint> load(List<FaultSectionPrefData> subSectData) throws IOException {
		return load(UCERF3_DataUtils.locateResourceAsStream(DIR_NAME, FILE_NAME), subSectData);
	}
	
	public static List<AveSlipConstraint> load(
			InputStream is, List<FaultSectionPrefData> subSectData) throws IOException {
		Map<Integer, List<FaultSectionPrefData>> parentSectsMap = Maps.newHashMap();
		for (FaultSectionPrefData data : subSectData) {
			Integer parentID = data.getParentSectionId();
			List<FaultSectionPrefData> subSectsForParent = parentSectsMap.get(parentID);
			if (subSectsForParent == null) {
				subSectsForParent = Lists.newArrayList();
				parentSectsMap.put(parentID, subSectsForParent);
			}
			subSectsForParent.add(data);
		}
		
		POIFSFileSystem fs = new POIFSFileSystem(is);
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheetAt(0);
		
		List<AveSlipConstraint> constraints = Lists.newArrayList();
		
		int lastRowIndex = sheet.getLastRowNum();
		
		int startRowIndex = 0;
		for (int rowIndex=0; rowIndex<=lastRowIndex; rowIndex++) {
			if (sheet.getRow(rowIndex).getCell(0).getStringCellValue().trim().equals("Fault")) {
				startRowIndex = rowIndex+1;
				break;
			}
		}
		Preconditions.checkState(startRowIndex > 0, "Couldn't find start row, data file changed?");
		
		for (int rowIndex=startRowIndex; rowIndex<=lastRowIndex; rowIndex++) {
			HSSFRow row = sheet.getRow(rowIndex);
			if (row == null)
				break;
			String faultName = row.getCell(0).getStringCellValue().trim();
			if (faultName.isEmpty())
				continue;
			if (faultName.equals("EXPLANATION"))
				// we're done
				break;
//			System.out.println(rowIndex+": "+row.getCell(0));
			Integer parentID = (int)row.getCell(1).getNumericCellValue();
			double lat = row.getCell(2).getNumericCellValue();
			double lon = row.getCell(3).getNumericCellValue();
			Location loc = new Location(lat, lon);
			
			FaultSectionPrefData matchSect = null;
			double minDist = Double.POSITIVE_INFINITY;
			for (FaultSectionPrefData subSect : parentSectsMap.get(parentID)) {
				FaultTrace trace = FaultUtils.resampleTrace(subSect.getFaultTrace(), 11);
				for (Location traceLoc : trace) {
					double dist = LocationUtils.horzDistanceFast(loc, traceLoc);
					if (dist < minDist) {
						minDist = dist;
						matchSect = subSect;
					}
				}
			}
			Preconditions.checkNotNull(matchSect, "no sub sects for parent?");
			Preconditions.checkState(minDist < 5d,
					"no sub sect found within 5km for site on "+faultName+" at: "+loc);
			double mean = row.getCell(22).getNumericCellValue();
			double uncertaintyPlus = row.getCell(23).getNumericCellValue();
			double uncertaintyMinus = row.getCell(24).getNumericCellValue();
			
			constraints.add(new AveSlipConstraint(matchSect.getSectionId(), mean, mean+uncertaintyPlus, mean-uncertaintyMinus));
		}
		
		return constraints;
	}
	
	public static void main(String[] args) throws IOException {
		FaultModels fm = FaultModels.FM2_1;
		DeformationModels dm = DeformationModels.forFaultModel(fm).get(0);
		List<FaultSectionPrefData> subSects = new DeformationModelFetcher(
						fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1d).getSubSectionList();
		for (AveSlipConstraint constr : load(subSects)) {
			System.out.println(constr);
		}
	}

}
