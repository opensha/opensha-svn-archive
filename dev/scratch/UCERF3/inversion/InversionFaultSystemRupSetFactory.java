package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

/**
 * This class serves as a factory for loading/building FaultSystemRupSet's for each branch of the UCERF3 logic tree.<br>
 * <br>
 * It's worth noting that this class uses each Fault Model's filter basis to determine which deformation model to filter by.
 * This means that when, for example, a FM 3.1 ABM rupture set is generated, it is filtered as if it were FM 3.1 Geologic.
 * 
 * @author Kevin
 *
 */
public class InversionFaultSystemRupSetFactory {
	
	private static File rup_set_store_dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
	
	/**
	 * This loads a rupture set for the specified deformation model (and it's first applicable fault model) using all
	 * other default branch choices and the default laugh test filter.<br>
	 * <br>
	 * It will first attempt to see if a file exists in the precomputed data directory with the same name as
	 * the deformation model. If so, that file will be simply loaded. Otherwise it will be created and the file
	 * will be written to disk for future caching.
	 * 
	 * @param deformationModel
	 * @return
	 * @throws IOException 
	 */
	public static FaultSystemRupSet cachedForBranch(DeformationModels deformationModel) throws IOException {
		return cachedForBranch(deformationModel, false);
	}
	
	/**
	 * This loads a rupture set for the specified deformation model (and it's first applicable fault model) using all
	 * other default branch choices and the default laugh test filter.<br>
	 * <br>
	 * It will first attempt to see if a file exists in the precomputed data directory with the same name as
	 * the deformation model. If so, that file will be simply loaded. Otherwise it will be created and the file
	 * will be written to disk for future caching.
	 * 
	 * @param deformationModel
	 * @return
	 * @throws IOException 
	 */
	public static FaultSystemRupSet cachedForBranch(DeformationModels deformationModel, boolean forceRebuild) throws IOException {
		return cachedForBranch(deformationModel.getApplicableFaultModels().get(0), deformationModel,
				LogicTreeBranch.DEFAULT.getInvModel(), forceRebuild);
	}

	
	/**
	 * This loads a rupture set for the specified fault/deformation model using all other default branch
	 * choices and the default laugh test filter.<br>
	 * <br>
	 * It will first attempt to see if a file exists in the precomputed data directory with the same name as
	 * the deformation model. If so, that file will be simply loaded. Otherwise it will be created and the file
	 * will be written to disk for future caching.
	 * 
	 * @param deformationModel
	 * @return
	 * @throws IOException 
	 */
	public static FaultSystemRupSet cachedForBranch(FaultModels faultModel, DeformationModels deformationModel,
			InversionModels invModel, boolean forceRebuild) throws IOException {
		return cachedForBranch(faultModel, deformationModel, invModel,
				rup_set_store_dir, forceRebuild);
	}

	
	/**
	 * This loads a rupture set for the specified fault/deformation model using all other default branch
	 * choices and the default laugh test filter.<br>
	 * <br>
	 * It will first attempt to see if a file exists in the precomputed data directory with the same name as
	 * the deformation model. If so, that file will be simply loaded. Otherwise it will be created and the file
	 * will be written to disk for future caching.
	 * 
	 * @param deformationModel
	 * @return
	 * @throws IOException 
	 */
	public static FaultSystemRupSet cachedForBranch(
			FaultModels faultModel, DeformationModels deformationModel, InversionModels invModel,
			File directory, boolean forceRebuild)
			throws IOException {
		String fileName = deformationModel.name()+"_"+faultModel.name()+".zip";
		File file = new File(directory, fileName);
		if (!forceRebuild && file.exists()) {
			System.out.println("Loading cached rup set from file: "+file.getAbsolutePath());
			
			try {
				FaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromZipFile(file);
				
				return rupSet;
			} catch (Exception e) {
				System.err.println("Error loading rupset from file: "+file.getAbsolutePath());
				e.printStackTrace();
			}
		}
		// this means the file didn't exist, we had an error loading it, or we're forcing a rebuild
		InversionFaultSystemRupSet rupSet = forBranch(faultModel, deformationModel, invModel);
		System.out.println("Caching rup set to file: "+file.getAbsolutePath());
		if (!directory.exists())
			directory.mkdir();
		new SimpleFaultSystemRupSet(rupSet).toZipFile(file);
		return rupSet;
	}
	
	/**
	 * Creates a rupture set for the specified deformation model (and it's first applicable fault model) using all
	 * other default branch choices and the default laugh test filter
	 * 
	 * @param deformationModel
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(DeformationModels deformationModel) {
		return forBranch(deformationModel.getApplicableFaultModels().get(0), deformationModel);
	}
	
	/**
	 * Creates a rupture set for the specified fault model/deformation model using all other default branch
	 * choices and the default laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel) {
		return forBranch(faultModel, deformationModel, LogicTreeBranch.DEFAULT.getInvModel());
	}
	
	/**
	 * Creates a rupture set for the specified fault model/deformation model using all other default branch
	 * choices and the default laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @param inversionModel
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel,
			InversionModels inversionModel) {
		return forBranch(faultModel, deformationModel, LogicTreeBranch.DEFAULT.getMagArea(),
				LogicTreeBranch.DEFAULT.getAveSlip(), LogicTreeBranch.DEFAULT.getSlipAlong(), inversionModel);
	}
	
	/**
	 * Creates a rupture set for the specified branch on the logic tree and the default laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @param magAreaRelationships
	 * @param aveSlipForRupModel
	 * @param slipAlongModel
	 * @param inversionModel
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel,
			MagAreaRelationships magAreaRelationships,
			AveSlipForRupModels aveSlipForRupModel,
			SlipAlongRuptureModels slipAlongModel,
			InversionModels inversionModel) {
		return forBranch(faultModel, deformationModel, magAreaRelationships, aveSlipForRupModel,
				slipAlongModel, inversionModel, LaughTestFilter.getDefault());
	}
	
	/**
	 * Creates a rupture set for the specified branch on the logic tree and the given laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @param magAreaRelationships
	 * @param aveSlipForRupModel
	 * @param slipAlongModel
	 * @param inversionModel
	 * @param laughTest
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel,
			MagAreaRelationships magAreaRelationships,
			AveSlipForRupModels aveSlipForRupModel,
			SlipAlongRuptureModels slipAlongModel,
			InversionModels inversionModel,
			LaughTestFilter laughTest) {
		return forBranch(faultModel, deformationModel, magAreaRelationships, aveSlipForRupModel,
				slipAlongModel, inversionModel, laughTest, 0d);
	}
	
	/**
	 * Creates a rupture set for the specified branch on the logic tree and the given laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @param magAreaRelationships
	 * @param aveSlipForRupModel
	 * @param slipAlongModel
	 * @param inversionModel
	 * @param laughTest
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel,
			MagAreaRelationships magAreaRelationships,
			AveSlipForRupModels aveSlipForRupModel,
			SlipAlongRuptureModels slipAlongModel,
			InversionModels inversionModel,
			LaughTestFilter laughTest,
			double defaultAseismicityValue) {
		System.out.println("Building a rupture set for: "+deformationModel+" ("+faultModel+")");
		
		List<MagAreaRelationship> magAreaRelList = magAreaRelationships.getMagAreaRelationships();
		
		if (faultModel == FaultModels.FM2_1 && laughTest.getCoulombFilter() != null) {
			System.out.println("WARNING: removing coulomb filter since this is FM 2.1");
			laughTest.setCoulombFilter(null);
		}
		
		DeformationModels filterBasis = faultModel.getFilterBasis();
		if (filterBasis == null) {
//			System.out.println("No filter basis specified!");
			filterBasis = deformationModel;
		}
		DeformationModelFetcher filterBasisFetcher = new DeformationModelFetcher(faultModel, filterBasis,
				UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, defaultAseismicityValue);
//		System.out.println("Creating clusters with filter basis: "+filterBasis+", Fault Model: "+faultModel);
		SectionClusterList clusters = new SectionClusterList(filterBasisFetcher, laughTest);
		
		List<FaultSectionPrefData> faultSectionData;
		if (filterBasis == deformationModel) {
			faultSectionData = clusters.getFaultSectionData();
		} else {
			// we need to get it outselves
			faultSectionData = new DeformationModelFetcher(faultModel, deformationModel,
					UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, defaultAseismicityValue).getSubSectionList();
		}
		
		InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(
				clusters, deformationModel, faultSectionData, magAreaRelList,
				inversionModel, slipAlongModel, aveSlipForRupModel);
		System.out.println("New rup set has "+rupSet.getNumRuptures()+" ruptures.");
		String info = rupSet.getInfoString();
		if (info == null)
			info = "";
		else
			info += "\n\n";
		info += "\n****** Logic Tree Branch ******";
		info += "\nFaultModel: "+faultModel.name();
		info += "\nDeformationModel: "+deformationModel.name();
		info += "\nMagAreaRelationship: "+magAreaRelationships.name();
		info += "\nAveSlipForRupModel: "+aveSlipForRupModel.name();
		info += "\nSlipAlongRuptureModel: "+slipAlongModel.name();
		info += "\nInversionModel: "+inversionModel.name();
		info += "\n*******************************";
		rupSet.setInfoString(info);
		return rupSet;
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		try {
//			NCAL_SMALL.getRupSet();
//			NCAL_SMALL_UNIFORM.getRupSet();
//			NCAL.getRupSet(true);
//			ALLCAL_SMALL.getRupSet(true);
//			ALLCAL.getRupSet(true);
//			UCERF3_ALLCAL_3_1_KLUDGE.getRupSet(true);
//			UCERF3_GEOLOGIC.getRupSet(true);
//			cachedForBranch(DeformationModels.GEOLOGIC, true);
//			forBranch(DeformationModels.ABM);
//			FaultSystemRupSet rupSet = cachedForBranch(DeformationModels.GEOLOGIC, true);
//			FaultSystemRupSet rupSet = forBranch(FaultModels.FM3_2, DeformationModels.GEOLOGIC_UPPER, InversionModels.CHAR);
//			FaultSystemRupSet rupSet = cachedForBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM, InversionModels.CHAR, true);
//			FaultSystemRupSet rupSet = cachedForBranch(FaultModels.FM2_1, DeformationModels.UCERF2_ALL, true);
//			FaultSystemRupSet rupSet = forBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC, MagAreaRelationships.ELL_B, AveSlipForRupModels.ELLSWORTH_B,
//					SlipAlongRuptureModels.TAPERED, InversionModels.GR, LaughTestFilter.getDefault(), MomentReductions.INCREASE_ASEIS);
			
			FaultSystemRupSet rupSet = forBranch(FaultModels.FM2_1, DeformationModels.UCERF2_ALL, InversionModels.CHAR);
			UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
			
			System.out.println("Total Orig Mo Rate: "+rupSet.getTotalOrigMomentRate());
			System.out.println("Total Reduced Mo Rate: "+rupSet.getTotalSubseismogenicReducedMomentRate());
			System.out.println("Total Mo Rate Reduction: "+rupSet.getTotalSubseismogenicMomentRateReduction());
			System.out.println("Total Mo Rate Reduction Fraction: "+rupSet.getTotalSubseismogenicMomentRateReductionFraction());
			
			// slip for an 8.4
//			int id = 132520;
//			double area = rupSet.getAreaForRup(id);
//			double aveSlip = rupSet.getAveSlipForRup(id);
//			double[] slips = rupSet.getSlipOnSectionsForRup(id);
//			int middle = slips.length / 2;
//			System.out.println("Mag "+rupSet.getMagForRup(id)+": area: "+area+" aveSlip: "+aveSlip+" middle slip: "+slips[middle]);
			
//			FaultSystemRupSet rupSet = cachedForBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM, true);
			
//			for (int sectIndex=0; sectIndex<rupSet.getNumSections(); sectIndex++) {
//				List<Integer> rups = rupSet.getRupturesForSection(sectIndex);
//				if (rups.isEmpty())
//					System.out.println("No ruptures for section: "+sectIndex+". "+rupSet.getFaultSectionData(sectIndex).getSectionName());
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
