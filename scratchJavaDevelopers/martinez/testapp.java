package scratchJavaDevelopers.martinez;

import java.util.ArrayList;
//import org.opensha.data.function.DiscretizedFunc;

public class testapp {
	public static void main(String[] args) {
		VulnerabilityModel vm = new KLPGAVlnFn();
		StructureType st = new WoodFrame();
		ArrayList<Trackable> availableStructures = st.getSupportedTypes(vm);
		ArrayList<Trackable> availableVulnerabilities = vm.getSupportedTypes(st);
		double val = EALCalculator.testCalc();
		
		System.out.println("For the KLPGAVlnFn, the StructureType reports the following as available:");
		for(int i = 0; i < availableStructures.size(); ++i)
			System.out.println("\t" + availableStructures.get(i).getTrackableId());
		System.out.println("");
		
		System.out.println("For the WoodFrame, the VulnerabilityModel reports the following as available:");
		for(int i = 0; i < availableVulnerabilities.size(); ++i)
			System.out.println("\t" + availableVulnerabilities.get(i).getTrackableId());
		System.out.println("");
		
		System.out.println("The test calculation returned EAL = " + val + "\n");
		
		System.out.println("Here is the template HazardCurve I would pass to the HazardCurveSelecter:");
		System.out.println(vm.getHazardTemplate().toString() + "\n");
		
		System.out.println("Here is the Vulnerability Function I got:");
			System.out.println(vm.getVulnerabilityFunc().toString());

	} // END: main()
}
