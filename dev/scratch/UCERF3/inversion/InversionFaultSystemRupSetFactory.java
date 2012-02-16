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
import scratch.UCERF3.inversion.InversionFaultSystemRupSet.SlipModelType;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.DeformationModelFetcher.DefModName;

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
public enum InversionFaultSystemRupSetFactory {
	
	NCAL_SMALL("NCAL_SMALL.zip", DefModName.UCERF2_NCAL,
				45, SlipModelType.TAPERED_SLIP_MODEL),
	
	NCAL_SMALL_UNIFORM("NCAL_SMALL_UNIFORM.zip", DefModName.UCERF2_NCAL,
				45, SlipModelType.UNIFORM_SLIP_MODEL),
	
	NCAL("NCAL.zip", DefModName.UCERF2_NCAL,
				90, SlipModelType.TAPERED_SLIP_MODEL),
	
	ALLCAL_SMALL("ALLCAL_SMALL.zip", DefModName.UCERF2_ALL,
				45, SlipModelType.TAPERED_SLIP_MODEL),
	
	ALLCAL("ALLCAL.zip", DefModName.UCERF2_ALL,
				90, SlipModelType.TAPERED_SLIP_MODEL),
				
	UCERF3_GEOLOGIC("UCERF3_GEOLOGIC.zip", DefModName.UCERF3_GEOLOGIC,
							45, SlipModelType.TAPERED_SLIP_MODEL);
	
	private static final boolean D = true;
	
	private String fileName;
	
	private DeformationModelFetcher.DefModName defModName;
	private double maxAzimuthChange;
	private SlipModelType slipModelType;
	
	private double maxJumpDist; 
	private double maxCumJumpDist; 
	private double maxTotAzimuthChange;
	private double maxRakeDiff; 
	private int minNumSectInRup;
	private ArrayList<MagAreaRelationship> magAreaRelList; 
	private double moRateReduction;
	
	private File dir = new File("dev/scratch/UCERF3/preComputedData/FaultSystemRupSets");
	
	private String dataURL = "http://opensha.usc.edu/ftp/ucerf3/rup_sets/";
	
	private static ArrayList<MagAreaRelationship> getDefaultMagAreaRelationships() {
		ArrayList<MagAreaRelationship> magAreaRelList = new ArrayList<MagAreaRelationship>();
		magAreaRelList.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelList.add(new HanksBakun2002_MagAreaRel());
		return magAreaRelList;
	}
	
	private InversionFaultSystemRupSetFactory(String fileName, DefModName defModName,
			double maxAzimuthChange, SlipModelType slipModelType) {
		this.fileName = fileName;
		
		this.defModName = defModName;
		this.maxAzimuthChange = maxAzimuthChange;
		this.slipModelType = slipModelType;
		
		this.maxJumpDist = 5d;
		this.maxCumJumpDist = 10d;
		this.maxTotAzimuthChange = 90d;
//		this.maxRakeDiff = 90d;
		this.maxRakeDiff = Double.POSITIVE_INFINITY;
		this.minNumSectInRup = 2;
		this.magAreaRelList = new ArrayList<MagAreaRelationship>();
		magAreaRelList.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelList.add(new HanksBakun2002_MagAreaRel());
		this.moRateReduction = 0.1;
	}
	
	/**
	 * This will load the given rup set. It will first try to load it locally, then it will
	 * attempt to download it, then if all else fails it will recreate it.
	 * 
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public FaultSystemRupSet getRupSet() throws IOException, DocumentException {
		return getRupSet(false);
	}
	
	public void setStoreDir(File dir) {
		this.dir = dir;
	}
	
	/**
	 * This will load the given rup set. It will first try to load it locally, then it will
	 * attempt to download it, then if all else fails it will recreate it.
	 * 
	 * @return
	 * @throws IOException
	 * @throws DocumentException
	 */
	public FaultSystemRupSet getRupSet(boolean forceRebuild) throws IOException, DocumentException {
		File file = new File(dir, fileName);
		
		if (!forceRebuild && !file.exists()) {
			// try downloading it from the internet
			URL url = new URL(dataURL+fileName);
			try {
				FileUtils.downloadURL(url, file);
			} catch (Exception e) {
				if (D) System.out.println("Couldn't download rup set from: "+url+" ("+e.toString()+")");
//				e.printStackTrace();
			}
		}
		
		if (!forceRebuild && file.exists()) {
			if (D) System.out.println("Loading rup set from "+file.getAbsolutePath());
			return SimpleFaultSystemRupSet.fromFile(file);
		}
		
		if (D) System.out.println("Couldn't download or find locally, instantiating new InversionFaultSystemRupSet.");
		// if we made it this far the the file did not already exist, and can't be downloaded
		InversionFaultSystemRupSet rupSet = new InversionFaultSystemRupSet(defModName, maxJumpDist, maxCumJumpDist, maxAzimuthChange,
				maxTotAzimuthChange, maxRakeDiff, minNumSectInRup, magAreaRelList,
				moRateReduction, slipModelType, dir);
		
		if (D) System.out.println("Saving new InversionFaultSystemRupSet to "+file.getAbsolutePath());
		new SimpleFaultSystemRupSet(rupSet).toZipFile(file);
		if (D) System.out.println("Done writing.");
		
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
			UCERF3_GEOLOGIC.getRupSet(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
