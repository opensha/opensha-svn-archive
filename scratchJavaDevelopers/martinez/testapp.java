package scratchJavaDevelopers.martinez;

import java.util.ArrayList;
//import org.opensha.data.function.DiscretizedFunc;

public class testapp {
	public static void main(String[] args) {
		VulnerabilityModel vm = new KLPGAVlnFn();
		StructureType st = new WoodFrame();
		ArrayList<Double> imls = vm.getIMLVals();
		ArrayList<Double> dfs = vm.getDFVals();
		ArrayList<Trackable> availableStructures = st.getSupportedTypes(vm);
		ArrayList<Trackable> availableVulnerabilities = vm.getSupportedTypes(st);
		
		System.out.println("For the KLPGAVlnFn, the StructureType reports the following as available:");
		for(int i = 0; i < availableStructures.size(); ++i)
			System.out.println(availableStructures.get(i).getTrackableId());
		
		System.out.println("For the WoodFrame, the VulnerabilityModel reports the following as available:");
		for(int i = 0; i < availableVulnerabilities.size(); ++i)
			System.out.println(availableVulnerabilities.get(i).getTrackableId());
		
		System.out.println("Here is the template HazardCurve I would pass to the HazardCurveSelecter:");
		System.out.println("\tPending...");
		
		System.out.println("Here is the Vulnerability Function I got:");
		for(int i = 0; i < vm.getNIML(); ++i)
			System.out.println("\t" + imls.get(i) + "\t\t" + dfs.get(i));

	} // END: main()
}
