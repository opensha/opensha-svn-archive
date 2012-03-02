package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.DocumentException;
import org.opensha.commons.util.FaultUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;

public class RakeFixer {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(new File("/tmp/ALLCAL_UCERF2.zip"));
		
		double[] newrakes = new double[sol.getNumRuptures()];
		
		for (int r=0; r<sol.getNumRuptures(); r++) {
			ArrayList<Double> rakes = new ArrayList<Double>();
			ArrayList<Double> areas = new ArrayList<Double>();
			for (int sectIndex : sol.getSectionsIndicesForRup(r)) {
				areas.add(sol.getAreaForSection(sectIndex));
				double rake = sol.getFaultSectionData(sectIndex).getAveRake();
				Preconditions.checkState(!Double.isNaN(rake));
				rakes.add(rake);
			}
			double rake = FaultUtils.getInRakeRange(FaultUtils.getScaledAngleAverage(areas, rakes));
			Preconditions.checkState(!Double.isNaN(rake));
			newrakes[r] = rake;
		}
		
		MatrixIO.doubleArrayToFile(newrakes, new File("/tmp/rakes.bin"));
	}

}
