package scratch.UCERF3.enumTreeBranches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensha.commons.data.ShortNamed;

import com.google.common.base.Preconditions;

public class OldLogicTreeBranch {
	
	/**
	 * This is the default reference branch
	 */
	public static final OldLogicTreeBranch DEFAULT =
		new OldLogicTreeBranch(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM,
				MagAreaRelationships.ELL_B, AveSlipForRupModels.ELLSWORTH_B,
				SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED);
	
	FaultModels faultModel;
	DeformationModels defModel;
	MagAreaRelationships magArea;
	AveSlipForRupModels aveSlip;
	SlipAlongRuptureModels slipAlong;
	InversionModels invModel;
	public OldLogicTreeBranch(FaultModels fm,
			DeformationModels dm,
			MagAreaRelationships ma,
			AveSlipForRupModels as,
			SlipAlongRuptureModels sal,
			InversionModels im) {
		this.faultModel = fm;
		this.defModel = dm;
		this.magArea = ma;
		this.aveSlip = as;
		this.slipAlong = sal;
		this.invModel = im;
	}
	
	/**
	 * @param branch
	 * @return the number of logic tree branches that are non null and differ from the given
	 * branch.
	 */
	public int getNumAwayFrom(OldLogicTreeBranch branch) {
		int away = 0;
		if (faultModel != null && faultModel != branch.faultModel)
			away++;
		if (defModel != null && defModel != branch.defModel)
			away++;
		if (magArea != null && magArea != branch.magArea)
			away++;
		if (aveSlip != null && aveSlip != branch.aveSlip)
			away++;
		if (slipAlong != null && slipAlong != branch.slipAlong)
			away++;
		if (invModel != null && invModel != branch.invModel)
			away++;
		return away;
	}
	
	/**
	 * 
	 * @param branch
	 * @return true if every non null value of this branch matches the given branch
	 */
	public boolean matchesNonNulls(OldLogicTreeBranch branch) {
		return getNumAwayFrom(branch) == 0;
	}

	@Override
	public String toString() {
		return "Branch [fm=" + faultModel + ", dm=" + defModel + ", ma=" + magArea + ", as="
				+ aveSlip + ", sal=" + slipAlong + ", im=" + invModel + "]";
	}
	
	private static <E extends ShortNamed> List<E> lengthSorted(E[] vals) {
		ArrayList<E> list = new ArrayList<E>();
		for (E val : vals) {
			int ind = 0;
			for (int i=0; i<list.size(); i++) {
				E lVal = list.get(i);
				if (val.getShortName().length() > lVal.getShortName().length())
					ind = i+1;
			}
			list.add(ind, val);
		}
		Collections.reverse(list);
		return list;
	}
	
	public static OldLogicTreeBranch parseFileName(String fileName) {
		FaultModels fm = null;
		for (FaultModels mod : lengthSorted(FaultModels.values())) {
			if (fileName.contains(mod.encodeChoiceString()+"_")) {
				fm = mod;
				break;
			}
		}
		Preconditions.checkState(fm != null || !fileName.contains("FM"),
				"Fault model found in file name but could not be parsed: "+fileName);
		
		DeformationModels dm = null;
		for (DeformationModels mod : lengthSorted(DeformationModels.values())) {
			if (fileName.contains("_"+mod.getShortName()+"_")) {
				dm = mod;
				break;
			}
		}
		
		MagAreaRelationships ma = null;
		for (MagAreaRelationships mod : lengthSorted(MagAreaRelationships.values())) {
			if (fileName.contains("_Ma"+mod.getShortName()+"_")) {
				ma = mod;
				break;
			}
		}
		Preconditions.checkState(ma != null || !fileName.contains("_Ma"),
				"MA found in file name but could not be parsed: "+fileName);
		
		AveSlipForRupModels as = null;
		for (AveSlipForRupModels mod : lengthSorted(AveSlipForRupModels.values())) {
			if (fileName.contains("_Dr"+mod.getShortName()+"_")) {
				as = mod;
				break;
			}
		}
		if (fileName.contains("_DrCostStressDrop_")) // typo from old files!
			as = AveSlipForRupModels.SHAW_12_CONST_STRESS_DROP;
		Preconditions.checkState(as != null || !fileName.contains("_Dr"),
				"Dr found in file name but could not be parsed: "+fileName);
		
		SlipAlongRuptureModels sal = null;
		for (SlipAlongRuptureModels mod : lengthSorted(SlipAlongRuptureModels.values())) {
			if (fileName.contains("_Dsr"+mod.getShortName()+"_")) {
				sal = mod;
				break;
			}
		}
		Preconditions.checkState(sal != null || !fileName.contains("_Dsr"),
				"Dsr found in file name but could not be parsed: "+fileName);
		
		InversionModels inv = null;
		for (InversionModels mod : lengthSorted(InversionModels.values())) {
			if (fileName.contains("_"+mod.getShortName())) {
				inv = mod;
				break;
			}
		}
		
		return new OldLogicTreeBranch(fm, dm, ma, as, sal, inv);
	}
	
	/**
	 * @return true if every field is non null, false otherwise
	 */
	public boolean isFullySpecified() {
		if (faultModel == null)
			return false;
		if (defModel == null)
			return false;
		if (magArea == null)
			return false;
		if (aveSlip == null)
			return false;
		if (slipAlong == null)
			return false;
		if (invModel == null)
			return false;
		return true;
	}
	
	public String buildFileName() {
		String name = faultModel.getShortName()+"_"+defModel.getShortName()
		+"_Ma"+magArea.getShortName()+"_Dsr"+slipAlong.getShortName()+"_Dr"+aveSlip.getShortName()
		+"_"+invModel.getShortName();
		return name;
	}

	public FaultModels getFaultModel() {
		return faultModel;
	}

	public void setFaultModel(FaultModels faultModel) {
		this.faultModel = faultModel;
	}

	public DeformationModels getDefModel() {
		return defModel;
	}

	public void setDefModel(DeformationModels defModel) {
		this.defModel = defModel;
	}

	public MagAreaRelationships getMagArea() {
		return magArea;
	}

	public void setMagArea(MagAreaRelationships magArea) {
		this.magArea = magArea;
	}

	public AveSlipForRupModels getAveSlip() {
		return aveSlip;
	}

	public void setAveSlip(AveSlipForRupModels aveSlip) {
		this.aveSlip = aveSlip;
	}

	public SlipAlongRuptureModels getSlipAlong() {
		return slipAlong;
	}

	public void setSlipAlong(SlipAlongRuptureModels slipAlong) {
		this.slipAlong = slipAlong;
	}

	public InversionModels getInvModel() {
		return invModel;
	}

	public void setInvModel(InversionModels invModel) {
		this.invModel = invModel;
	}
}