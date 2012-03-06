package scratch.UCERF3.enumTreeBranches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensha.commons.data.ShortNamed;
import org.opensha.commons.param.impl.EnumParameter;


public class LogicTreeBranch {
	FaultModels faultModel;
	DeformationModels defModel;
	MagAreaRelationships magArea;
	AveSlipForRupModels aveSlip;
	SlipAlongRuptureModels slipAlong;
	InversionModels invModel;
	public LogicTreeBranch(FaultModels fm, DeformationModels dm,
			MagAreaRelationships ma,
			AveSlipForRupModels as,
			SlipAlongRuptureModels sal, InversionModels im) {
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
	public int getNumAwayFrom(LogicTreeBranch branch) {
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
	public boolean matchesNonNulls(LogicTreeBranch branch) {
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
//		System.out.println("************");
//		for (E val : list)
//			System.out.println(val.getShortName());
		return list;
	}
	
	public static LogicTreeBranch parseFileName(String fileName) {
		FaultModels fm = null;
		for (FaultModels mod : lengthSorted(FaultModels.values())) {
			if (fileName.contains(mod.getShortName())) {
				fm = mod;
				break;
			}
		}
		DeformationModels dm = null;
		for (DeformationModels mod : lengthSorted(DeformationModels.values())) {
			if (fileName.contains(mod.getShortName())) {
				dm = mod;
				break;
			}
		}
		MagAreaRelationships ma = null;
		for (MagAreaRelationships mod : lengthSorted(MagAreaRelationships.values())) {
			if (fileName.contains("Ma"+mod.getShortName())) {
				ma = mod;
				break;
			}
		}
		AveSlipForRupModels as = null;
		for (AveSlipForRupModels mod : lengthSorted(AveSlipForRupModels.values())) {
			if (fileName.contains("Dr"+mod.getShortName())) {
				as = mod;
				break;
			}
		}
		if (fileName.contains("DrCostStressDrop")) // typo from old files!
			as = AveSlipForRupModels.SHAW_12_CONST_STRESS_DROP;
		SlipAlongRuptureModels sal = null;
		for (SlipAlongRuptureModels mod : lengthSorted(SlipAlongRuptureModels.values())) {
			if (fileName.contains("Dsr"+mod.getShortName())) {
				sal = mod;
				break;
			}
		}
		InversionModels inv = null;
		for (InversionModels mod : lengthSorted(InversionModels.values())) {
			if (fileName.contains(mod.getShortName())) {
				inv = mod;
				break;
			}
		}
		return new LogicTreeBranch(fm, dm, ma, as, sal, inv);
	}
	
	public String buildFileName() {
		return null;
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