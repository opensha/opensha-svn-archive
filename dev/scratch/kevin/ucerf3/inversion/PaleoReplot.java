package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.DocumentException;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;

public class PaleoReplot {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		File dir = new File(args[0]);
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				continue;
			if (!file.getName().endsWith("_sol.zip"))
				continue;
			System.out.println("Working on: "+file.getName());
			SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(file);
			String prefix = file.getName().substring(0, file.getName().indexOf("_sol.zip"));
			
			LogicTreeBranch branch = LogicTreeBranch.parseFileName(file.getName());
			
			ArrayList<PaleoRateConstraint> paleoRateConstraints = CommandLineInversionRunner.getPaleoConstraints(branch.getFaultModel(),
					sol);
			
			CommandLineInversionRunner.writePaleoPlots(paleoRateConstraints, sol, dir, prefix);
		}
	}

}
