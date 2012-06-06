package scratch.UCERF3.griddedSeismicity;

import java.util.List;
import java.util.Map;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.FaultSystemRupSet;

/**
 * Utility class for working with fault polygons and spatial PDFs of seismicity.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class GriddedSeisUtils {

	private FaultPolyMgr polyMgr;
	private double[] pdf;
	
	
	/**
	 * Create an instance of this ustility class.
	 * @param fsrs
	 * @param pdf
	 */
	public GriddedSeisUtils(List<FaultSectionPrefData> fltSectPrefDataList, SpatialSeisPDF pdf) {
		polyMgr = FaultPolyMgr.create(fltSectPrefDataList, null);
		this.pdf = pdf.getPDF();
	}
	
	/**
	 * Returns the fraction of the originally supplied spatial PDF captured
	 * by the fault section at {@code idx}. This is a weighted value in as much
	 * as multiple overlapping fault in the originally supplied rupture set
	 * never 'consume' more than what's available at a node that all faults
	 * intersect.
	 * @param idx of section to retreive value of
	 * @return the pdf value
	 */
	public double pdfValForSection(int idx) {
		Map<Integer, Double> nodeMap = polyMgr.getSectFractions(idx);
		double sum = 0.0;
		for (int iNode : nodeMap.keySet()) {
			sum += pdf[iNode] * nodeMap.get(iNode);
		}
		return sum;
	}
	
	public static void main(String[] args) {
	}
}
