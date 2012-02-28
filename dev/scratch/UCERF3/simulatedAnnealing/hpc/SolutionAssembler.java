package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;

import org.opensha.commons.util.ClassUtils;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;

public class SolutionAssembler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length <3 || args.length > 4) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(SolutionAssembler.class)
					+" <rates file> <rup set file/name> <output file> [minimum rupture rates]");
			System.exit(2);
		}
		
		try {
			double[] rupRateSolution = MatrixIO.doubleArrayFromFile(new File(args[0]));
			
			FaultSystemRupSet rupSet = null;
			try {
				File rupSetFile = new File(args[1]);
				if (rupSetFile.exists())
					rupSet = SimpleFaultSystemRupSet.fromFile(rupSetFile);
			} catch (Exception e) {};
			String rupSetStr = args[1];
//			if (rupSet == null) {
//				for (InversionFaultSystemRupSetFactory f : InversionFaultSystemRupSetFactory.values()) {
//					if (f.name().equals(rupSetStr)) {
//						File tempDir = FileUtils.createTempDir();
//						f.setStoreDir(tempDir);
//						rupSet = f.getRupSet();
//						FileUtils.deleteRecursive(tempDir);
//						break;
//					}
//				}
//			}
			Preconditions.checkNotNull(rupSet, "Rupture set couldn't be loaded: "+args[1]);
			
			if (args.length == 4) {
				// we have a minimum rates file
				double[] minimumRates = MatrixIO.doubleArrayFromFile(new File(args[3]));
				System.out.println("Adjusting solution for minimum weights...");
				rupRateSolution = InversionInputGenerator.adjustSolutionForMinimumRates(rupRateSolution, minimumRates);
			}
			
			SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rupRateSolution);
			sol.toZipFile(new File(args[2]));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
