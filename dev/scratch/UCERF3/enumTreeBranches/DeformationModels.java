package scratch.UCERF3.enumTreeBranches;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.utils.UCERF3_DataUtils;

public enum DeformationModels {
	// UCERF2
	UCERF2_NCAL(			FaultModels.FM2_1),
	UCERF2_BAYAREA(			FaultModels.FM2_1),
	UCERF2_ALL(				FaultModels.FM2_1),
	
	// UCERF3
	ABM(					FaultModels.FM3_1, "ABM_slip_rake_2012_02_21.csv"),
	GEOBOUND(				FaultModels.FM3_1, "geobound_slip_rake_2012_02_21.csv"),
	NEOKINEMA(				FaultModels.FM3_1, "neokinema_slip_rake_2012_02_21.csv"),
	ZENG(					FaultModels.FM3_1, "zeng_slip_rake_2012_02_21.csv"),
	GEOLOGIC(				FaultModels.FM3_1, "geologic_slip_rake_2012_02_21.csv",
							FaultModels.FM3_2, "geologic_slip_rake_fm3pt2_2012_02_27.csv"),
	GEOLOGIC_PLUS_ABM(		FaultModels.FM3_1, "geologic_plus_ABM_slip_rake_2012_02_27.csv");
	
	private List<FaultModels> faultModels;
	private List<String> fileNames;
	
	private DeformationModels(FaultModels model) {
		this(Lists.newArrayList(model), null);
	}

	private DeformationModels(FaultModels model, String file) {
		this(Lists.newArrayList(model), Lists.newArrayList(file));
	}

	private DeformationModels(FaultModels model1, String file1, FaultModels model2, String file2) {
		this(Lists.newArrayList(model1, model2), Lists.newArrayList(file1, file2));
	}
	
	private DeformationModels(List<FaultModels> faultModels, List<String> fileNames) {
		Preconditions.checkNotNull(faultModels, "fault models cannot be null!");
		Preconditions.checkArgument(!faultModels.isEmpty(), "fault models cannot be empty!");
		Preconditions.checkArgument(fileNames == null || fileNames.size() == faultModels.size(),
				"file names must either be null or the same size as fault models!");
		this.faultModels = faultModels;
		this.fileNames = fileNames;
	}
	
	public boolean isApplicableTo(FaultModels faultModel) {
		return faultModels.contains(faultModel);
	}
	
	public List<FaultModels> getApplicableFaultModels() {
		return Collections.unmodifiableList(faultModels);
	}

	public URL getDataFileURL(FaultModels faultModel) {
		Preconditions.checkState(isApplicableTo(faultModel),
				"Deformation model "+name()+" isn't applicable to fault model: "+faultModel);
		if (fileNames == null)
			return null;
		String fileName = fileNames.get(faultModels.indexOf(faultModel));
		return UCERF3_DataUtils.locateResource("DeformationModels", fileName);
	}
}