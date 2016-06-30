package scratch.kevin.ucerf3;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;

public class UCERF3_RSQSimTuningFileGen {

	public static void main(String[] args) throws IOException, DocumentException {
		FaultSystemSolution sol = FaultSystemIO.loadSol(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
						+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		double[] rates = sol.calcParticRateForAllSects(0d, 10d);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		csv.addLine("Subsection Index", "Parent Section ID", "Subsection Name",
				"Supra-Seismogenic Participation Rate", "Supra-Seismogenic Participation RI");
		
		FaultSystemRupSet rupSet = sol.getRupSet();
		Preconditions.checkState(rates.length == rupSet.getNumSections());
		
		for (int s=0; s<rupSet.getNumSections(); s++) {
			FaultSectionPrefData sect = rupSet.getFaultSectionData(s);
			
			double ri = 1d/rates[s];
			
			csv.addLine(s+"", sect.getParentSectionId()+"", sect.getName(), rates[s]+"", ri+"");
		}
		
		csv.writeToFile(new File("/tmp/sub_sect_ris_for_tuning.csv"));
	}

}
