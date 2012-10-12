package scratch.UCERF3.analysis;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;

public class FiguresForReportAndTalks {
	
	
	public static void makePreInversionMFDsFig() {
		InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.ZENG, 
				InversionModels.CHAR_CONSTRAINED, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, 
				TotalMag5Rate.RATE_8p7, MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3);
		System.out.println(rupSet.getPreInversionAnalysisData(true));
		FaultSystemRupSetCalc.plotPreInversionMFDs(rupSet, false, false, true, "preInvCharMFDs.pdf");
		
		rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.ZENG, 
				InversionModels.GR_CONSTRAINED, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, 
				TotalMag5Rate.RATE_8p7, MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3);
		FaultSystemRupSetCalc.plotPreInversionMFDs(rupSet, false, false, false, "preInvGR_MFDs.pdf");
		
		rupSet = InversionFaultSystemRupSetFactory.forBranch(FaultModels.FM3_1, DeformationModels.ZENG, 
				InversionModels.GR_CONSTRAINED, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, 
				TotalMag5Rate.RATE_8p7, MaxMagOffFault.MAG_7p6, MomentRateFixes.APPLY_IMPLIED_CC, SpatialSeisPDF.UCERF3);
		FaultSystemRupSetCalc.plotPreInversionMFDs(rupSet, false, false, false, "preInvGR_MFDs_applCC.pdf");
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		makePreInversionMFDsFig();
	}

}
