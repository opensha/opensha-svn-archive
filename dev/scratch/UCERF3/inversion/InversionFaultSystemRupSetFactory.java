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
 * This class serves as a factory for loading/building FaultSystemRupSet's as defined via e-mail
 * from Morgan 10/12/2011
 * <br>
 * <br>
 * Here are 5 models to get us started.  As you can see, there are really 8 total combinations of the parameters I'm changing here, but we probably don't need that many models for now.
 * <br> 
 * Instead of having the name include all the possible parameters, for these naming conventions I only add additional text to the filename when things change from the "default" of maxAzimuthChange=90, and the tapered slip model.
 * <br>
 * --Morgan
 * <br>
 * <br>
 * These parameters constant for all models:		
 * <br>
 * <br>double maxJumpDist = 5.0;
 * <br>double maxTotAzimuthChange = 90;
 * <br>double maxRakeDiff = 90;
 * <br>int minNumSectInRup = 2;
 * <br>double moRateReduction = 0.1;
 * <br>ArrayList<MagAreaRelationship> magAreaRelList = new ArrayList<MagAreaRelationship>();
 * <br>magAreaRelList.add(new Ellsworth_B_WG02_MagAreaRel());
 * <br>magAreaRelList.add(new HanksBakun2002_MagAreaRel());		
 * <br>
 * <br>NCAL_SMALL
 * <br>double maxAzimuthChange = 45
 * <br>DeformationModelFetcher.DefModName.UCERF2_NCAL
 * <br>InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL
 * <br>
 * <br>NCAL_SMALL_UNIFORM
 * <br>double maxAzimuthChange = 45
 * <br>DeformationModelFetcher.DefModName.UCERF2_NCAL
 * <br>InversionFaultSystemRupSet.SlipModelType.UNIFORM_SLIP_MODEL
 * <br>
 * <br>NCAL
 * <br>double maxAzimuthChange = 90
 * <br>DeformationModelFetcher.DefModName.UCERF2_NCAL
 * <br>InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL
 * <br>
 * <br>ALLCAL_SMALL
 * <br>double maxAzimuthChange = 45
 * <br>DeformationModelFetcher.DefModName.UCERF2_ALL
 * <br>InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL
 * <br>
 * <br>ALLCAL
 * <br>double maxAzimuthChange = 90
 * <br>DeformationModelFetcher.DefModName.UCERF2_ALL
 * <br>InversionFaultSystemRupSet.SlipModelType.TAPERED_SLIP_MODEL
 * 
 * @author Kevin
 *
 */
public class InversionFaultSystemRupSetFactory {
	
	/**
	 * This returns the current default laugh test filter
	 * 
	 * @return
	 */
	public static LaughTestFilter getDefaultLaughTestFilter() {
		double maxAzimuthChange = 90;
		double maxJumpDist = 5d;
		double maxCumJumpDist = 10d;
		double maxTotAzimuthChange = 90d;
		double maxRakeDiff = Double.POSITIVE_INFINITY;
		int minNumSectInRup = 2;
		double maxCmlRakeChange = 360;
		double maxCmlAzimuthChange = 540;
		
		return new LaughTestFilter(maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, maxCumJumpDist,
				maxCmlRakeChange, maxCmlAzimuthChange, minNumSectInRup);
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
	public static FaultSystemRupSet cachedForBranch(DeformationModels deformationModel) throws IOException {
		return cachedForBranch(deformationModel.getApplicableFaultModels().get(0), deformationModel);
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
	public static FaultSystemRupSet cachedForBranch(FaultModels faultModel, DeformationModels deformationModel) throws IOException {
		return cachedForBranch(faultModel, deformationModel,
				new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "FaultSystemRupSets"));
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
	public static FaultSystemRupSet cachedForBranch(FaultModels faultModel, DeformationModels deformationModel, File directory)
			throws IOException {
		String fileName = deformationModel.name()+"_"+faultModel.name()+".zip";
		File file = new File(directory, fileName);
		if (file.exists()) {
			System.out.println("Loading cached rup set from file: "+file.getAbsolutePath());
			
			try {
				FaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromZipFile(file);
				
				return rupSet;
			} catch (Exception e) {
				System.err.println("Error loading rupset from file: "+file.getAbsolutePath());
				e.printStackTrace();
			}
		}
		// this means the file didn't exist or we had an error loading it
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
		return forBranch(deformationModel.getApplicableFaultModels().get(0), deformationModel, MagAreaRelationships.AVE_UCERF2,
				AveSlipForRupModels.AVE_UCERF2, SlipAlongRuptureModels.TAPERED_SLIP_MODEL);
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
				AveSlipForRupModels.AVE_UCERF2, SlipAlongRuptureModels.TAPERED_SLIP_MODEL);
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
		return forBranch(faultModel, deformationModel, magAreaRelationships, aveSlipForRupModel, slipAlongModel, getDefaultLaughTestFilter());
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
						moRateReduction, slipAlongModel, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, aveSlipForRupModel, laughTest);
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
			cachedForBranch(DeformationModels.GEOLOGIC);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
