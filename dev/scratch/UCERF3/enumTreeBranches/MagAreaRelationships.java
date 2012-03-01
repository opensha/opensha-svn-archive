package scratch.UCERF3.enumTreeBranches;

import java.util.Collections;
import java.util.List;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Shaw_2009_MagAreaRel;

import com.google.common.collect.Lists;

public enum MagAreaRelationships {
	
	HB_08("Hanks & Bakun (2002)", "HB08", new HanksBakun2002_MagAreaRel()),
	ELL_B("Ellsworth B", "EllB", new Ellsworth_B_WG02_MagAreaRel()),
	SHAW_09("Shaw (2009)", "Shaw09", new Shaw_2009_MagAreaRel()),
	AVE_UCERF2("Average UCERF2", "AvU2", Lists.newArrayList(new Ellsworth_B_WG02_MagAreaRel(), new HanksBakun2002_MagAreaRel()));
	
	private List<MagAreaRelationship> rels;
	
	private String name, shortName;
	
	private MagAreaRelationships(String name, String shortName, MagAreaRelationship rel) {
		this(name, shortName, Lists.newArrayList(rel));
	}
	
	private MagAreaRelationships(String name, String shortName, List<MagAreaRelationship> rels) {
		this.rels = Collections.unmodifiableList(rels);
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
	
	public List<MagAreaRelationship> getMagAreaRelationships() {
		return rels;
	}

}
