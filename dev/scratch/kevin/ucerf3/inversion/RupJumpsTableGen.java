package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.CSVFile;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class RupJumpsTableGen {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		FaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(DeformationModels.GEOLOGIC_PLUS_ABM);
		
		DeformationModelFetcher dmFetch = new DeformationModelFetcher(rupSet.getFaultModel(), rupSet.getDeformationModel(),
				UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR);
		
		Map<IDPairing, Double> dists = dmFetch.getSubSectionDistanceMap(5d);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		csv.addLine("RupID", "Mag", "NumJumps", "NumJumps>1KM");
		
		for (int r=0; r<rupSet.getNumRuptures(); r++) {
			List<Integer> sects = rupSet.getSectionsIndicesForRup(r);
			
			int jumps = 0;
			int jumpsOver1 = 0;
			for (int i=1; i<sects.size(); i++) {
				int sect1 = sects.get(i-1);
				int sect2 = sects.get(i);
				
				int parent1 = rupSet.getFaultSectionData(sect1).getParentSectionId();
				int parent2 = rupSet.getFaultSectionData(sect2).getParentSectionId();
				
				if (parent1 != parent2) {
					jumps++;
					double dist = dists.get(new IDPairing(sect1, sect2));
					if (dist > 1)
						jumpsOver1++;
				}
			}
			
			csv.addLine(r+"", rupSet.getMagForRup(r)+"", jumps+"", jumpsOver1+"");
		}
		
		File csvFile = new File("/tmp/rup_jumps.csv");
		File txtFile = new File("/tmp/rup_jumps.txt");
		
		csv.writeToFile(csvFile);
		csv.writeToTabSeparatedFile(txtFile, 1);
	}

}
