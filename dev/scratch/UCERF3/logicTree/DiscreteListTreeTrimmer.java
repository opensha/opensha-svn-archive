package scratch.UCERF3.logicTree;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class DiscreteListTreeTrimmer implements TreeTrimmer,
		Iterable<LogicTreeBranch> {
	
	public static DiscreteListTreeTrimmer getUCERF2_IngredientsTrimmer() {
		List<LogicTreeBranch> branches = Lists.newArrayList();
		branches.add(LogicTreeBranch.UCERF2);
		
		return new DiscreteListTreeTrimmer(branches);
	}
	
	List<LogicTreeBranch> branches;
	
	public DiscreteListTreeTrimmer(List<LogicTreeBranch> branches) {
		this.branches = branches;
	}

	@Override
	public Iterator<LogicTreeBranch> iterator() {
		return branches.iterator();
	}

	@Override
	public boolean isTreeValid(LogicTreeBranch branch) {
		return branches.contains(branch);
	}

}
