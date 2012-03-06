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
	ABM(					"Average Block Model",	"ABM",		FaultModels.FM3_1, "ABM_slip_rake_2012_03_02.csv"),
	GEOBOUND(				"Geobounded",			"GEOB",		FaultModels.FM3_1, "geobound_slip_rake_2012_03_02.csv"),
	NEOKINEMA(				"Neokinema",			"NEOK",		FaultModels.FM3_1, "neokinema_slip_rake_2012_03_02.csv"),
	ZENG(					"Zeng",					"ZENG",		FaultModels.FM3_1, "zeng_slip_rake_2012_03_02.csv"),
	GEOLOGIC(				"Geologic",				"GEOL", 	FaultModels.FM3_1, "geologic_slip_rake_2012_03_02.csv",
																FaultModels.FM3_2, "geologic_slip_rake_fm3pt2_2012_03_02.csv"),
	GEOLOGIC_PLUS_ABM(		"Geologic + ABM",		"GLpABM",	FaultModels.FM3_1, "geologic_plus_ABM_slip_rake_2012_03_02.csv");
	
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
		return UCERF3_DataUtils.locateResource("DeformationModels", getDataFileName(faultModel));
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