package scratch.UCERF3.enumTreeBranches;

import java.util.List;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2009_MagAreaRel;

import com.google.common.collect.Lists;

public enum MagAreaRelationships {
	
	HB_08(new HanksBakun2002_MagAreaRel()),
	ELL_B(new Ellsworth_B_WG02_MagAreaRel()),
	SHAW_09(new Shaw_2009_MagAreaRel()),
	AVE_UCERF2(Lists.newArrayList(new Ellsworth_B_WG02_MagAreaRel(), new HanksBakun2002_MagAreaRel()));
	
	private List<MagAreaRelationship> rels;
	
	private MagAreaRelationships(MagAreaRelationship rel) {
		this(Lists.newArrayList(rel));
	}
	
	private MagAreaRelationships(List<MagAreaRelationship> rels) {
		this.rels = rels;
	}
	
	public List<MagAreaRelationship> getMagAreaRelationships() {
		return rels;
	}

}
