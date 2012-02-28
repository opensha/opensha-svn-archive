package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.commons.geo.Location;

import scratch.UCERF3.utils.DeformationModelFileParser;
import scratch.UCERF3.utils.DeformationModelFileParser.DeformationSection;

public class DoformationModelCombiner {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		HashMap<Integer, DeformationSection> geologicModel = DeformationModelFileParser.load(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/DeformationModels/geologic_slip_rake_2012_02_21.csv"));
		HashMap<Integer, DeformationSection> abmModel = DeformationModelFileParser.load(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/DeformationModels/ABM_slip_rake_2012_02_21.csv"));
		
		ArrayList<DeformationSection> combined = new ArrayList<DeformationModelFileParser.DeformationSection>();
		
		for (int id : geologicModel.keySet()) {
			DeformationSection geologic = geologicModel.get(id);
			DeformationSection abm = abmModel.get(id);
			
			DeformationSection comb = new DeformationSection(id);
			
			for (int i=0; i<geologic.getLocs1().size(); i++) {
				Location loc1 = geologic.getLocs1().get(i);
				Location loc2 = geologic.getLocs2().get(i);
				double slip1 = geologic.getSlips().get(i);
				double slip2 = abm.getSlips().get(i);
				double rake = geologic.getRakes().get(i); // keep rake from geologic
				
				double slip = 0.5*(slip1+slip2);
				comb.add(loc1, loc2, slip, rake);
			}
			combined.add(comb);
		}
		
		DeformationModelFileParser.write(combined,
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/DeformationModels/geologic_plus_ABM_slip_rake_2012_02_27.csv"));
	}

}
