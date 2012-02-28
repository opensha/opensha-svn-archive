package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.dom4j.DocumentException;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.util.FileUtils;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.utils.UCERF3_DataUtils;

/**
 * This class serves as a factory for loading/building FaultSystemRupSet's for each branch of the UCERF3 logic tree.
 * 
 * @author Kevin
 *
 */
public class InversionFaultSystemRupSetFactory {
	
	private static File default_scratch_dir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets");
	
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
		return cachedForBranch(deformationModel.getApplicableFaultModels().get(0), deformationModel, false);
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
		return cachedForBranch(deformationModel.getApplicableFaultModels().get(0), deformationModel, forceRebuild);
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
			boolean forceRebuild) throws IOException {
		return cachedForBranch(faultModel, deformationModel,
				default_scratch_dir, forceRebuild);
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
			FaultModels faultModel, DeformationModels deformationModel, File directory, boolean forceRebuild)
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
		InversionFaultSystemRupSet rupSet = forBranch(faultModel, deformationModel);
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
		return forBranch(faultModel, deformationModel, MagAreaRelationships.AVE_UCERF2,
				AveSlipForRupModels.AVE_UCERF2, SlipAlongRuptureModels.TAPERED);
	}
	
	/**
	 * Creates a rupture set for the specified branch on the logic tree and the default laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @param magAreaRelationships
	 * @param aveSlipForRupModel
	 * @param slipAlongModel
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel,
			MagAreaRelationships magAreaRelationships,
			AveSlipForRupModels aveSlipForRupModel,
			SlipAlongRuptureModels slipAlongModel) {
		return forBranch(faultModel, deformationModel, magAreaRelationships, aveSlipForRupModel, slipAlongModel, LaughTestFilter.getDefault());
	}
	
	/**
	 * Creates a rupture set for the specified branch on the logic tree and the given laugh test filter
	 * 
	 * @param faultModel
	 * @param deformationModel
	 * @param magAreaRelationships
	 * @param aveSlipForRupModel
	 * @param slipAlongModel
	 * @param laughTest
	 * @return
	 */
	public static InversionFaultSystemRupSet forBranch(
			FaultModels faultModel,
			DeformationModels deformationModel,
			MagAreaRelationships magAreaRelationships,
			AveSlipForRupModels aveSlipForRupModel,
			SlipAlongRuptureModels slipAlongModel,
			LaughTestFilter laughTest) {
		
		double moRateReduction = 0.1; // TODO don't hardcode this here
		
		return new InversionFaultSystemRupSet(faultModel, deformationModel, magAreaRelationships.getMagAreaRelationships(),
						moRateReduction, slipAlongModel, default_scratch_dir, aveSlipForRupModel, laughTest);
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
			cachedForBranch(DeformationModels.GEOLOGIC, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
