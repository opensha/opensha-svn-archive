package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

public class CommandLineInputGenerator {
	
	public static void main(String[] args) {
		if (args.length <= 3 || args.length > 4) {
			System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(CommandLineInputGenerator.class)
					+ " <rup set file> <inversion model name> <fname/directory> [<temp dir>]");
			System.exit(2);
		}
		
		try {
			File rupSetFile = new File(args[0]);
			SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(rupSetFile);
			
			InversionModels model = InversionModels.getTypeForName(args[1]);
			InversionConfiguration config = InversionConfiguration.forModel(model, rupSet);
			
			List<PaleoRateConstraint> paleoRateConstraints =
					UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
			
			double[] improbabilityConstraint = null;
			
			PaleoProbabilityModel paleoProbabilityModel = PaleoProbabilityModel.loadUCERF3PaleoProbabilityModel();
			
			InversionInputGenerator gen = new InversionInputGenerator(rupSet, config, paleoRateConstraints,
					improbabilityConstraint, paleoProbabilityModel);
			
			gen.generateInputs();
			
			File f = new File(args[2]);
			if (f.exists() && f.isDirectory()) {
				File zipFile = new File(f, "inputs.zip");
				
				gen.writeZipFile(zipFile, f, false);
			} else {
				if (args.length == 4) {
					File tempDir = new File(args[3]);
					Preconditions.checkState(tempDir.exists() && tempDir.isDirectory(),
							args[3]+" doesn't exist or isn't a directory!");
					String dirName = f.getName().replaceAll(".zip", "");
					dirName += "_tempInputs";
					tempDir = new File(tempDir, dirName);
					if (!tempDir.exists())
						tempDir.mkdir();
					gen.writeZipFile(f, tempDir, true);
				} else {
					gen.writeZipFile(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.exit(0);
	}

}
