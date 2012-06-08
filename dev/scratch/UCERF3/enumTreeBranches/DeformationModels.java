package scratch.UCERF3.enumTreeBranches;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensha.commons.data.ShortNamed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.utils.UCERF3_DataUtils;

public enum DeformationModels implements ShortNamed {
	// UCERF2
	UCERF2_NCAL(			"UCERF2 NCal", 			"NCAL", 	FaultModels.FM2_1),
	UCERF2_BAYAREA(			"UCERF2 Bay Area", 		"BAY",		FaultModels.FM2_1),
	UCERF2_ALL(				"UCERF2 All",			"UC2ALL",	FaultModels.FM2_1),
	
	// UCERF3
	// AVERAGE BLOCK MODEL
	ABM(					"Average Block Model",	"ABM",		FaultModels.FM3_1, "ABM_slip_rake_fm_3_1_2012_06_08.csv",
																FaultModels.FM3_2, "ABM_slip_rake_fm_3_2_2012_06_08.csv"),
	// GEOBOUNDED INVERSION
	GEOBOUND(				"Geobounded",			"GEOB",		FaultModels.FM3_1, "geobound_slip_rake__MAPPED_2012_06_05.csv"),
	// NEOKINEMA
	NEOKINEMA(				"Neokinema",			"NEOK",		FaultModels.FM3_1, "neokinema_slip_rake_fm_3_1_2012_06_08.csv",
																FaultModels.FM3_2, "neokinema_slip_rake_fm_3_2_2012_06_08.csv"),
	// ZENG
	ZENG(					"Zeng",					"ZENG",		FaultModels.FM3_1, "zeng_slip_rake_fm_3_1_2012_06_08.csv",
																FaultModels.FM3_2, "zeng_slip_rake_fm_3_2_2012_06_08.csv"),
	// GEOLOGIC
	GEOLOGIC(				"Geologic",				"GEOL", 	FaultModels.FM3_1, "geologic_slip_rake_fm_3_1_2012_05_29.csv",
																FaultModels.FM3_2, "geologic_slip_rake_fm_3_2_2012_05_29.csv"),
	GEOLOGIC_UPPER(			"Geologic Upper Bound",	"GLUP", 	FaultModels.FM3_1, "geologic_slip_rake_fm_3_1_upperbound__MAPPED_2012_06_05.csv",
																FaultModels.FM3_2, "geologic_slip_rake_fm_3_2_upperbound__MAPPED_2012_06_05.csv"),
	GEOLOGIC_LOWER(			"Geologic Lower Bound",	"GLLOW", 	FaultModels.FM3_1, "geologic_slip_rake_fm_3_1_lowerbound__MAPPED_2012_06_05.csv",
																FaultModels.FM3_2, "geologic_slip_rake_fm_3_2_lowerbound__MAPPED_2012_06_05.csv"),
	// GEOLOGIC + ABM
	GEOLOGIC_PLUS_ABM(		"Geologic + ABM",		"GLpABM",	FaultModels.FM3_1, "geologic_plus_ABM_slip_rake_fm_3_1_2012_06_08.csv",
																FaultModels.FM3_2, "geologic_plus_ABM_slip_rake_fm_3_2_2012_06_08.csv");
	
	private List<FaultModels> faultModels;
	private List<String> fileNames;
	private String name, shortName;
	
	private DeformationModels(String name, String shortName, FaultModels model) {
		this(name, shortName, Lists.newArrayList(model), null);
	}

	private DeformationModels(String name, String shortName, FaultModels model, String file) {
		this(name, shortName, Lists.newArrayList(model), Lists.newArrayList(file));
	}

	private DeformationModels(String name, String shortName, FaultModels model1, String file1,
			FaultModels model2, String file2) {
		this(name, shortName, Lists.newArrayList(model1, model2), Lists.newArrayList(file1, file2));
	}
	
	private DeformationModels(String name, String shortName, List<FaultModels> faultModels, List<String> fileNames) {
		Preconditions.checkNotNull(faultModels, "fault models cannot be null!");
		Preconditions.checkArgument(!faultModels.isEmpty(), "fault models cannot be empty!");
		Preconditions.checkArgument(fileNames == null || fileNames.size() == faultModels.size(),
				"file names must either be null or the same size as fault models!");
		this.faultModels = faultModels;
		this.fileNames = fileNames;
		this.name = name;
		this.shortName = shortName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isApplicableTo(FaultModels faultModel) {
		return faultModels.contains(faultModel);
	}
	
	public List<FaultModels> getApplicableFaultModels() {
		return Collections.unmodifiableList(faultModels);
	}

	public URL getDataFileURL(FaultModels faultModel) {
		String fileName = getDataFileName(faultModel);
		if (fileName == null)
			return null;
		return UCERF3_DataUtils.locateResource("DeformationModels", fileName);
	}
	
	public String getDataFileName(FaultModels faultModel) {
		Preconditions.checkState(isApplicableTo(faultModel),
				"Deformation model "+name()+" isn't applicable to fault model: "+faultModel);
		if (fileNames == null)
			return null;
		return fileNames.get(faultModels.indexOf(faultModel));
	}
	
	public static List<DeformationModels> forFaultModel(FaultModels fm) {
		ArrayList<DeformationModels> mods = new ArrayList<DeformationModels>();
		for (DeformationModels mod : values())
			if (mod.isApplicableTo(fm))
				mods.add(mod);
		return mods;
	}
}