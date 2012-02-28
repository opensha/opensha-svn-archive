package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;

public class ParsonsPairingDistanceWriter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Map<IDPairing, Double> pairings = DeformationModelFetcher.readMapFile(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/" +
						"FaultSystemRupSets/UCERF3_GEOLOGIC_2648_Distances_5.0km"));
		File outputFile = new File(
				"/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/preComputedData/" +
				"FaultSystemRupSets/UCERF3_GEOLOGIC_2648_Distances_5.0km_pairings_w_dist.txt");
		DeformationModelFetcher.writePairingsTextFile(outputFile, pairings);
	}

}
