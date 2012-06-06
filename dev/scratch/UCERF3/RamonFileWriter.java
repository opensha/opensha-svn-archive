package scratch.UCERF3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.geo.Location;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class RamonFileWriter {
	
	private static void write(File file, FaultModels fm, DeformationModels dm) throws IOException {
		ArrayList<FaultSectionPrefData> subSects =
				new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE).getSubSectionList();
		FileWriter fw = new FileWriter(file);
		
		for (FaultSectionPrefData subSect : subSects) {
			fw.write("#"+subSect.getName()+"\n");
			fw.write(""+subSect.getFaultTrace().size()+"\n");
			for (Location loc : subSect.getFaultTrace()) {
				fw.write(loc.getLatitude()+"\t"+loc.getLongitude()+"\n");
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		write(new File("/tmp/fm3_1_for_ramon.txt"), FaultModels.FM3_1, DeformationModels.GEOLOGIC);
		write(new File("/tmp/fm3_2_for_ramon.txt"), FaultModels.FM3_2, DeformationModels.GEOLOGIC);
	}

}
