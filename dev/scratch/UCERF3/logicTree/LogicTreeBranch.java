package scratch.UCERF3.logicTree;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.util.ClassUtils;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class LogicTreeBranch implements Iterable<LogicTreeBranchNode<? extends Enum<?>>> {
	
	/**
	 * This is the default reference branch // TODO confirm
	 */
	public static final LogicTreeBranch DEFAULT = fromValues(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM,
			ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
			MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3);
	
	private static List<Class<? extends LogicTreeBranchNode<?>>> logicTreeClasses;
	
	public static synchronized List<Class<? extends LogicTreeBranchNode<?>>> getLogicTreeNodeClasses() {
		if (logicTreeClasses == null) {
			logicTreeClasses = Lists.newArrayList();
			
			logicTreeClasses.add(FaultModels.class);
			logicTreeClasses.add(DeformationModels.class);
			logicTreeClasses.add(ScalingRelationships.class);
			logicTreeClasses.add(SlipAlongRuptureModels.class);
			logicTreeClasses.add(InversionModels.class);
			logicTreeClasses.add(TotalMag5Rate.class);
			logicTreeClasses.add(MaxMagOffFault.class);
			logicTreeClasses.add(MomentRateFixes.class);
			logicTreeClasses.add(SpatialSeisPDF.class);
			
			logicTreeClasses = Collections.unmodifiableList(logicTreeClasses);
		}
		
		return logicTreeClasses;
	}
	
	private List<LogicTreeBranchNode<? extends Enum<?>>> branch;
	
	protected LogicTreeBranch(LogicTreeBranch branch) {
		this(branch.branch);
	}
	
	private LogicTreeBranch(List<LogicTreeBranchNode<? extends Enum<?>>> branch) {
		this.branch = branch;
	}
	
	@SuppressWarnings("unchecked")
//	public <E extends Enum<?>> E getValue(Class<? extends LogicTreeBranchNode<?>> clazz) {
	public <E extends Enum<E>> E getValue(Class<? extends LogicTreeBranchNode<E>> clazz) {
		return getValue(clazz, branch);
	}
		
	private static <E extends Enum<E>> E getValue(Class<? extends LogicTreeBranchNode<E>> clazz,
			List<LogicTreeBranchNode<? extends Enum<?>>> branch) {
		clazz = getEnumEnclosingClass(clazz);
		for (LogicTreeBranchNode<?> node : branch) {
			if (node != null && getEnumEnclosingClass(node.getClass()).equals(clazz)) {
				return (E)node;
			}
		}
		return null;
	}
	
	public int size() {
		return branch.size();
	}
	
	public LogicTreeBranchNode<?> getValue(int index) {
		return branch.get(index);
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends LogicTreeBranchNode<?>> Class<E> getEnumEnclosingClass(Class<E> clazz) {
		if (!clazz.isEnum())
			clazz = (Class<E>) clazz.getEnclosingClass();
		return clazz;
	}
	
	public void clearValue(Class<? extends LogicTreeBranchNode<?>> clazz) {
		clazz = getEnumEnclosingClass(clazz);
		branch.set(getLogicTreeNodeClasses().indexOf(clazz), null);
	}
	
	public void setValue(LogicTreeBranchNode<?> value) {
		Class<? extends LogicTreeBranchNode> clazz = getEnumEnclosingClass(value.getClass());
		
//		System.out.println("Clazz? "+clazz);
		
		List<Class<? extends LogicTreeBranchNode<?>>> branchClasses = getLogicTreeNodeClasses();
		Preconditions.checkState(branch.size() == branchClasses.size());
		for (int i=0; i<branchClasses.size(); i++) {
			Class<? extends LogicTreeBranchNode<?>> nodeClazz = branchClasses.get(i);
//			System.out.println("testing: "+nodeClazz);
			if (nodeClazz.equals(clazz)) {
				branch.set(i, value);
				return;
			}
		}
		throw new IllegalArgumentException("Class '"+clazz+"' not part of logic tree node classes");
	}
	
	public boolean isFullySpecified() {
		for (LogicTreeBranchNode<?> val : branch)
			if (val == null)
				return false;
		return true;
	}
	/**
	 * @param branch
	 * @return the number of logic tree branches that are non null in this branch and differ from the given
	 * branch.
	 */
	public int getNumAwayFrom(LogicTreeBranch o) {
		Preconditions.checkArgument(branch.size() == o.branch.size(), "branch sizes inconsistant!");
		int away = 0;
		
		for (int i=0; i<branch.size(); i++) {
			Object mine = branch.get(i);
			Object theirs = o.branch.get(i);
			
			if (mine != null && !mine.equals(theirs))
				away++;
		}
		
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
	
	public String buildFileName() {
		String str = null;
		for (LogicTreeBranchNode<?> value : branch) {
			Preconditions.checkNotNull(value, "Must be fully specified to build file name!");
			if (str == null)
				str = "";
			else
				str += "_";
			str += value.encodeChoiceString();
		}
		return str;
	}
	
	public static LogicTreeBranch fromValues(List<LogicTreeBranchNode<?>> vals) {
		LogicTreeBranchNode<?>[] valsArray = new LogicTreeBranchNode[vals.size()];
		
		for (int i=0; i<vals.size(); i++)
			valsArray[i] = vals.get(i);
		
		return fromValues(valsArray);
	}
	
	public static LogicTreeBranch fromValues(LogicTreeBranchNode<?>... vals) {
		return fromValues(true, vals);
	}
	
	public static LogicTreeBranch fromValues(boolean setNullToDefault, LogicTreeBranchNode<?>... vals) {
		List<Class<? extends LogicTreeBranchNode<?>>> classes = getLogicTreeNodeClasses();
		
		// initialize branch with null
		List<LogicTreeBranchNode<? extends Enum<?>>> branch = Lists.newArrayList();
		for (int i=0; i<classes.size(); i++)
			branch.add(null);
		
		// now add each value
		for (LogicTreeBranchNode<?> val : vals) {
			if (val == null)
				continue;
			
			// find the class
			Class<? extends LogicTreeBranchNode> valClass = getEnumEnclosingClass(val.getClass());
			int ind = -1;
			for (int i=0; i<classes.size(); i++) {
				Class<? extends LogicTreeBranchNode<?>> clazz = classes.get(i);
				if (clazz.equals(valClass)) {
					ind = i;
					break;
				}
			}
			Preconditions.checkArgument(ind >= 0, "Value of class '"+valClass+"' not a valid logic tree branch class");
			branch.set(ind, val);
		}
		
		if (setNullToDefault) {
			// little fault model hack, since fault model can be dependent on deformation model if DM is specified
			if (getValue(FaultModels.class, branch) == null && getValue(DeformationModels.class, branch) != null) {
				int fmIndex = getLogicTreeNodeClasses().indexOf(FaultModels.class);
				DeformationModels dm = getValue(DeformationModels.class, branch);
				FaultModels defaultFM = DEFAULT.getValue(FaultModels.class);
				if (dm.getApplicableFaultModels().contains(defaultFM))
					branch.set(fmIndex, defaultFM);
				else
					branch.set(fmIndex, dm.getApplicableFaultModels().get(0));
			}
			for (int i=0; i<classes.size(); i++) {
				if (branch.get(i) == null)
					branch.set(i, DEFAULT.branch.get(i));
			}
		}
		
		return new LogicTreeBranch(branch);
	}
	
	public static LogicTreeBranch fromStringValues(List<String> strings) {
		return fromFileName(Joiner.on("_").join(strings));
	}
	
	public static LogicTreeBranch fromFileName(String fileName) {
		List<Class<? extends LogicTreeBranchNode<?>>> classes = getLogicTreeNodeClasses();
		List<LogicTreeBranchNode<? extends Enum<?>>> branch = Lists.newArrayList();
		
		for (Class<? extends LogicTreeBranchNode<?>> clazz : classes) {
//			LogicTreeBranchNode<?> value = parseValue(clazz, fileName);
			LogicTreeBranchNode<?> value = null;
			LogicTreeBranchNode<?>[] options = clazz.getEnumConstants();
			for (LogicTreeBranchNode<?> option : options) {
				if (doesStringContainOption(option, fileName)) {
					value = option;
					break;
				}
			}
			branch.add(value);
		}
		return new LogicTreeBranch(branch);
	}
	
	private static boolean doesStringContainOption(LogicTreeBranchNode<?> option, String str) {
		String encoded = option.encodeChoiceString();
		if (str.startsWith(encoded+"_") || str.contains("_"+encoded+"_")
				|| str.contains("_"+encoded+".") || str.endsWith("_"+encoded))
			return true;
		return false;
	}
	
	public static <E extends Enum<E>> E parseValue(Class<? extends LogicTreeBranchNode<E>> clazz, String str) {
		LogicTreeBranchNode<E>[] options = clazz.getEnumConstants();
		for (LogicTreeBranchNode<E> option : options)
			if (doesStringContainOption(option, str))
				return (E)option;
		return null;
	}
	
	@Override
	public String toString() {
		String str = null;
		for (LogicTreeBranchNode<?> val : branch) {
			if (str == null)
				str = ClassUtils.getClassNameWithoutPackage(getClass())+"[";
			else
				str += ", ";
//			str += ClassUtils.getClassNameWithoutPackage(getEnumEnclosingClass(val.getClass()))+"="+val.getShortName();
			if (val == null)
				str += "(null)";
			else
				str += val.encodeChoiceString();
		}
		return str+"]";
	}
	
	public String getTabSepValStringHeader() {
		return "FltMod\tDefMod\tScRel\tSlipAlongMod\tInvModels\tM5Rate\tMmaxOff\tMoRateFix\tSpatSeisPDF";
	}

	
	public String getTabSepValString() {
		String str = "";
		boolean first = true;
		for (LogicTreeBranchNode<?> val : branch) {
			if (!first)
				str += "\t";
			else
				first = false;
			if (val == null)
				str += "(null)";
			else
				str += val.encodeChoiceString();
		}
		return str;
	}
	
	
	public static void main(String[] args) {
//		String str = "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char";
		String str = DEFAULT.buildFileName();
		System.out.println("PARSING: "+str);
		LogicTreeBranch br = fromFileName(str);
		
		FaultModels fm = parseValue(FaultModels.class, str);
		System.out.println("FM? "+fm);
		
//		for (Class<? extends LogicTreeBranchNode<?>> clazz : getLogicTreeNodeClasses())
//			System.out.println(clazz+"\t?\t"+br.getValue(clazz));
		
		System.out.println("Num away? "+br.getNumAwayFrom(br));
		
		LogicTreeBranch br2 = fromFileName(str);
		System.out.println(br2);
		System.out.println("Num away? "+br.getNumAwayFrom(br2));
		br2.setValue(FaultModels.FM3_2);
		System.out.println(br2);
		System.out.println("Num away? "+br.getNumAwayFrom(br2));
	}

	@Override
	public Iterator<LogicTreeBranchNode<? extends Enum<?>>> iterator() {
		return branch.iterator();
	}

}
