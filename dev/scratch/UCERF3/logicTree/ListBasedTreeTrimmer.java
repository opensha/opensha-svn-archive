package scratch.UCERF3.logicTree;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ListBasedTreeTrimmer implements TreeTrimmer {
	
	private static List<Class<? extends LogicTreeBranchNode<?>>> classes = LogicTreeBranch.getLogicTreeNodeClasses();
	private List<List<LogicTreeBranchNode<?>>> limitations;
	
	public static ListBasedTreeTrimmer getNonZeroWeightsTrimmer() {
		List<List<LogicTreeBranchNode<?>>> limitations = Lists.newArrayList();
		
		for (Class<? extends LogicTreeBranchNode<?>> clazz : classes) {
			List<LogicTreeBranchNode<?>> nonZeros = Lists.newArrayList();
			for (LogicTreeBranchNode<?> option : clazz.getEnumConstants()) {
				if (option.getRelativeWeight() > 0)
					nonZeros.add(option);
			}
			limitations.add(nonZeros);
		}
		
		return new ListBasedTreeTrimmer(limitations);
	}
	
	public ListBasedTreeTrimmer(List<List<LogicTreeBranchNode<?>>> limitationsList) {
		this.limitations = Lists.newArrayList();
		for (int i=0; i<classes.size(); i++)
			limitations.add(null);
		for (List<LogicTreeBranchNode<?>> limits : limitationsList) {
			if (limits == null || limits.isEmpty())
				continue;
			Class<LogicTreeBranchNode<?>> clazz = (Class<LogicTreeBranchNode<?>>) LogicTreeBranch.getEnumEnclosingClass(limits.get(0).getClass());
			int index = classes.indexOf(clazz);
			Preconditions.checkState(index >= 0, "Could not location class in class list: "+clazz);
			limitations.set(index, limits);
		}
		Preconditions.checkState(limitations.size() == classes.size(), "limitations list must be same size as number of classes");
	}

	@Override
	public boolean isTreeValid(LogicTreeBranch branch) {
		for (int i=0; i<branch.size(); i++) {
			LogicTreeBranchNode<?> val = branch.getValue(i);
			List<LogicTreeBranchNode<?>> myLimits = limitations.get(i);
			if (myLimits != null && !limitations.get(i).contains(val))
				return false;
		}
		return true;
	}

}
