package scratch.kevin.ucerf3.inversion;

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
		if (args.length != 3) {
			System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(CommandLineInputGenerator.class)
					+ " <rup set file> <inversion model name> <directory>");
			System.exit(2);
		}
		
		try {
			File rupSetFile = new File(args[0]);
			SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(rupSetFile);
			
			InversionModels model = InversionModels.getTypeForName(args[1]);
			InversionConfiguration config = InversionConfiguration.forModel(model, rupSet);
			
			File dir = new File(args[2]);
			if (!dir.exists())
				Preconditions.checkState(dir.mkdir(), "dir doesn't exist and could not be created!");
			
			List<PaleoRateConstraint> paleoRateConstraints =
					UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
			
			double[] improbabilityConstraint = null;
			
			PaleoProbabilityModel paleoProbabilityModel = PaleoProbabilityModel.loadUCERF3PaleoProbabilityModel();
			
			InversionInputGenerator gen = new InversionInputGenerator(rupSet, config, paleoRateConstraints,
					improbabilityConstraint, paleoProbabilityModel);
			
			gen.generateInputs();
			
			File zipFile = new File(dir, "inputs.zip");
			
			gen.writeZipFile(zipFile, dir, false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.exit(0);
	}

}
