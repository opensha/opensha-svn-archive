package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.FileUtils;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.utils.MatrixIO;

public class SolutionAssembler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(SolutionAssembler.class)
					+" <rates file> <rup set file/name> <output file>");
			System.exit(2);
		}
		
		try {
			double[] rupRateSolution = MatrixIO.doubleArrayFromFile(new File(args[0]));
			
			FaultSystemRupSet rupSet = null;
			String rupSetStr = args[1];
			for (InversionFaultSystemRupSetFactory f : InversionFaultSystemRupSetFactory.values()) {
				if (f.name().equals(rupSetStr)) {
					File tempDir = FileUtils.createTempDir();
					f.setStoreDir(tempDir);
					rupSet = f.getRupSet();
					FileUtils.deleteRecursive(tempDir);
					break;
				}
			}
			if (rupSet == null) {
				rupSet = SimpleFaultSystemRupSet.fromFile(new File(args[1]));
			}
			
			SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(rupSet, rupRateSolution);
			sol.toZipFile(new File(args[2]));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (DocumentException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
