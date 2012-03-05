package scratch.UCERF3.enumTreeBranches;


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

	@Override
	public String toString() {
		return "Branch [fm=" + faultModel + ", dm=" + defModel + ", ma=" + magArea + ", as="
				+ aveSlip + ", sal=" + slipAlong + ", im=" + invModel + "]";
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