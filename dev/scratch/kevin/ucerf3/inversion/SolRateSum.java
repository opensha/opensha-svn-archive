package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;


public class SolRateSum {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		// TODO Auto-generated method stub
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(
				new File("/tmp/FM3_1_ZENG_EllB_DsrTap_GRConst_M5Rate8.7_MMaxOff7.6_ApplyCC_SpatSeisU3_sol.zip"));
		
		double rate = 0;
		for (int r=0; r<sol.getNumRuptures(); r++)
			rate += sol.getRateForRup(r);
		System.out.println("total sol rate: "+rate);
		
		int tot = 0;
		int num = 0;
		for (int s=0; s<sol.getNumSections(); s++) {
			List<Integer> rups = sol.getRupturesForSection(s);
			boolean endOnly = true;
			for (int r : rups) {
				List<Integer> sects = sol.getSectionsIndicesForRup(r);
				if (sects.get(0) != s && sects.get(sects.size()-1) != s) {
					endOnly = false;
					break;
				}
			}
//			if (sol.getCloseSectionsList(s).size() == 1) {
			if (endOnly) {
				System.out.println(sol.getFaultSectionData(s).getSectionName());
				num++;
			}
		}
		System.out.println(num+"/"+tot);
	}

}
