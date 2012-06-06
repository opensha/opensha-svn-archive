package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.util.ClassUtils;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.griddedSeismicity.SpatialSeisPDF;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.inversion.LaughTestFilter;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

public class CommandLineInputGenerator {
	
	public static void main(String[] args) {
		double defaultAsesisValue = 0;
		while (args.length > 2 && args[0].equals("--var")) {
			String variation = args[1];
			
			if (variation.startsWith("DefaultAseis_")) {
				String amtStr = variation.substring(variation.indexOf("_")+1);
				defaultAsesisValue = Double.parseDouble(amtStr);
				System.out.println("Setting default aseis value to: "
						+defaultAsesisValue);
			} else {
				System.out.println("Unknown variaition: "+variation);
			}
			
			String[] newArgs = new String[args.length - 2];
			for (int i=0; i<newArgs.length; i++)
				newArgs[i] = args[i+2];
			args = newArgs;
		}
		
		if (args.length < 3 || args.length > 4) {
			System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(CommandLineInputGenerator.class)
					+ " <rup set file> <inversion model name> <fname/directory> [<temp dir>]");
			System.exit(2);
		}
		
		try {
			File rupSetFile = new File(args[0]);
			FaultSystemRupSet rupSet;
			if (rupSetFile.exists()) {
				rupSet = SimpleFaultSystemRupSet.fromFile(rupSetFile);
			} else {
				// try to create it
				LogicTreeBranch branch = LogicTreeBranch.parseFileName(rupSetFile.getName());
				Preconditions.checkState(branch.isFullySpecified(), "Rup set file doesn't exist and can't be created because " +
						"the logic tree branch could not be fully parsed: "+rupSetFile.getAbsolutePath());
				LaughTestFilter filter = LaughTestFilter.getDefault();
				rupSet = InversionFaultSystemRupSetFactory.forBranch(branch.getFaultModel(), branch.getDefModel(),
						branch.getMagArea(), branch.getAveSlip(), branch.getSlipAlong(), branch.getInvModel(), filter, defaultAsesisValue,
						SpatialSeisPDF.UCERF3); // TODO don't hardcode
				// write it to a file
				new SimpleFaultSystemRupSet(rupSet).toZipFile(rupSetFile);
			}
			
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
